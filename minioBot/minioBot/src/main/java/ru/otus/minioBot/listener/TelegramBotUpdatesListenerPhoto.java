package ru.otus.minioBot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.otus.minioBot.CommandConst;
import ru.otus.minioBot.model.ChatState;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.service.ImageServiceDB;
import ru.otus.minioBot.service.NotificationService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.otus.minioBot.CommandConst.CALLBACK_ADD_REMARK;
import static ru.otus.minioBot.CommandConst.CALLBACK_VIEW_REMARKS;


@Service
public class TelegramBotUpdatesListenerPhoto implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListenerPhoto.class);
    private final String downloadDirectory;
    private final TelegramBot telegramBot;
    private final NotificationService notificationService;
    private final ImageServiceDB imageService;
    private final Map<Long, ChatState> chatStates = new ConcurrentHashMap<>();// Ваш сервис
    private Notification responseMessageNote;
    String noteName;

    private static final Pattern REMARK_INPUT_PATTERN = Pattern.compile("(.*;)(.[0-9]{3})(.[да|нет]).+");

    public TelegramBotUpdatesListenerPhoto(TelegramBot telegramBot, NotificationService notificationService,
                                           @Value("${app.download.dir:resources/photos}") String downloadDirectory, ImageServiceDB imageService) {
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
        this.downloadDirectory = downloadDirectory;
        this.imageService = imageService;
    }

    @PostConstruct
    public void init() {
        Path path = Paths.get(downloadDirectory);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                logger.info("Created download directory: {}", downloadDirectory);
            } catch (IOException e) {
                logger.error("Failed to create download directory: {}", downloadDirectory, e);
            }
        }
        telegramBot.setUpdatesListener(this);
        logger.info("Telegram bot listener initialized.");
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            long chatId;
            long userId;

            if (update.message() != null) {
                Message message = update.message();
                chatId = message.chat().id();
                String chatText = message.text();
                PhotoSize[] photoSizes = message.photo();

                ChatState currentState = chatStates.getOrDefault(chatId, ChatState.IDLE);
                logger.debug("Received message from chat {}. Current state: {}. Text: {}, Photo present: {}", chatId, currentState, chatText, photoSizes != null && photoSizes.length > 0);

                if (currentState == ChatState.WAITING_FOR_PHOTO_UPLOAD && photoSizes != null && photoSizes.length > 0) {
                    handlePhotoUpload(chatId, photoSizes);
                    chatStates.put(chatId, ChatState.IDLE);
                    logger.info("Processed uploaded photo for chat {}. State reset to IDLE.", chatId);
                } else if (currentState == ChatState.WAITING_FOR_REMARK_DETAILS) {
                    if (chatText != null && !chatText.isEmpty()) {
                        handleRemarkInput(chatId, chatText);
                    } else {
                        telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG + " Ожидался текст или фото."));
                    }
                } else if (currentState == ChatState.IDLE) {
                    if (chatText != null && !chatText.isEmpty()) {
                        // Обработка текстовых команд
                        if (chatText.startsWith(CommandConst.START_CMD)) {
                            handleStartCommand(chatId, message.from().firstName());
                        } else if (chatText.startsWith(CommandConst.ADD_REMARK_CMD)) {
                            handleAddRemarkCommand(chatId, chatText);
                            chatStates.put(chatId, ChatState.WAITING_FOR_REMARK_DETAILS); // Устанавливаем состояние ожидания ввода
                        } else {
                            telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG));
                        }
                    } else if (photoSizes != null && photoSizes.length > 0) {
                        logger.info("Received photo in chat {} while in IDLE state. Attempting to download.", chatId);
//                        String downloadedFilePath = downloadPhotoFile(chatId, photoSizes);
                        Pair<byte[],Notification> downloadedFile = downloadPhotoFileToDB(chatId, photoSizes);
                        if (downloadedFile != null) {
                            telegramBot.execute(new SendMessage(chatId, "Фотография '" + downloadedFile.getFirst().toString() + "' скачана."));
//                            Pair<byte[], String> file = imageService.getImageFromFS(responseMessageNote.getId());
                            imageService.uploadImageFromFStoDB(downloadedFile);
                        } else {
                            telegramBot.execute(new SendMessage(chatId, "Не удалось скачать фотографию."));
                        }
                    }
                }
            } else if (update.callbackQuery() != null) {
                CallbackQuery callbackQuery = update.callbackQuery();
                chatId = callbackQuery.message().chat().id();
                userId = callbackQuery.from().id();
                String callbackData = callbackQuery.data();
                logger.debug("Received callback query from chat {}. Data: '{}'. User: {}", chatId, callbackData, userId);
                handleCallbackData(chatId, userId, callbackData);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handlePhotoUpload(long chatId, PhotoSize[] photoSizes) {
        logger.info("Received photo upload from chat {}.", chatId);
    }


    /**
     * Обрабатывает ввод текста пользователем.
     *
     * @param chatId    ID чата
     *                  //     * @param userId    ID пользователя
     * @param inputText Текст, введенный пользователем
     */
    private void handleRemarkInput(long chatId, String inputText) {
        Matcher matcher = REMARK_INPUT_PATTERN.matcher(inputText);

        if (matcher.matches()) {
            try {
                responseMessageNote = notificationService.processRemarkInput((int) chatId, inputText);
                noteName = responseMessageNote.getComment();

                telegramBot.execute(new SendMessage(chatId, noteName));
                logger.info("Remark details processed for chat {}. State reset to IDLE.", chatId);
                chatStates.put(chatId, ChatState.IDLE);
            } catch (Exception e) {
                logger.error("Unexpected error processing remark input from chat {}: {}", chatId, e.getMessage(), e);
                telegramBot.execute(new SendMessage(chatId, "Произошла внутренняя ошибка. Попробуйте позже."));
                chatStates.put(chatId, ChatState.IDLE);
            }
        } else {
            logger.warn("Invalid remark input format from chat {}. Input: '{}'. Expected format: 'Описание; Позиция; да/нет'", chatId, inputText);
            telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG + " Формат ввода неверен.\n" +
                    "Пожалуйста, введите в формате: Описание; Позиция; да/нет\n" +
                    "Например: Мусор возде 10Д-1,2; 130; нет"));
        }
    }

    /**
     * Обрабатывает команду /add_remark, отправленную вручную.
     * Устанавливает состояние WAITING_FOR_REMARK_DETAILS.
     */
    private void handleAddRemarkCommand(long chatId, String userInput) {
        if (userInput.startsWith(CommandConst.ADD_REMARK_CMD)) {
            String commandAndData = userInput.substring(CommandConst.ADD_REMARK_CMD.length()).trim();

            if (commandAndData.isEmpty()) {
                telegramBot.execute(new SendMessage(chatId, "Пожалуйста, введите ваше замечание в формате:\n" +
                        "Описание замечания; Позиция установки (число); да/нет\n" +
                        "Например: Мусор возде 10Д-1,2; 130; нет"));
                logger.info("User initiated remark input via command /add_remark in chat {}, but no data provided. Sent instructions. State is WAITING_FOR_REMARK_DETAILS.", chatId);
            } else {
                handleRemarkInput(chatId, commandAndData);
            }
        } else {
            logger.error("handleAddRemarkCommand called with userInput '{}' which does not start with '{}'", userInput, CommandConst.ADD_REMARK_CMD);
            telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG));
        }
    }

    /**
     * Скачивает фотографию, используя fileId, и сохраняет ее в указанную директорию.
     *
     * @param chatId     ID чата для отправки сообщений об успехе/ошибке.
     * @param photoSizes Массив PhotoSize, содержащий информацию о версиях фото.
     * @return Полный путь к скачанному файлу, или null в случае ошибки.
     */
    private String downloadPhotoFile(long chatId, PhotoSize[] photoSizes) {
        if (photoSizes == null || photoSizes.length == 0) {
            logger.error("Попытка скачать пустой файл фото {}", chatId);
            return null;
        }

        PhotoSize largestPhoto = photoSizes[photoSizes.length - 1];
        String fileId = largestPhoto.fileId();

        try {
            GetFile getFileRequest = new GetFile(fileId);
            GetFileResponse fileResponse = telegramBot.execute(getFileRequest);

            if (!fileResponse.isOk()) {
                logger.error("Failed to get file info from Telegram API for fileId {}. Error: {}", fileId, fileResponse.errorCode() + ": " + fileResponse.description());
                telegramBot.execute(new SendMessage(chatId, "Не удалось получить информацию о фотографии."));
            } else {
                File fileInfo = fileResponse.file();
                String filePath = fileInfo.filePath(); // Например: "photos/file_1.jpg"
                String botToken = telegramBot.getToken();
                URL downloadUrl = new URL("https://api.telegram.org/file/bot" + botToken + "/" + filePath);
                Path fileName = Paths.get(filePath).getFileName();
                logger.info("Downloading file from URL: {} to path: {}", downloadUrl, fileName);
                try (InputStream in = downloadUrl.openStream()) {
                    Files.copy(in, fileName, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Successfully downloaded file to: {}", fileName);
                    return fileName.toString();
                }
            }
        } catch (IOException e) {
            logger.error("IO error downloading photo with fileId {} for chat {}: {}", fileId, chatId, e.getMessage(), e);
            telegramBot.execute(new SendMessage(chatId, "Произошла ошибка при скачивании фотографии (IO)."));
        } catch (Exception e) {
            logger.error("Unexpected error downloading photo with fileId {} for chat {}: {}", fileId, chatId, e.getMessage(), e);
            telegramBot.execute(new SendMessage(chatId, "Произошла неожиданная ошибка при скачивании фотографии."));
        }
        return null;
    }

    /**
     * Обрабатывает нажатие на инлайн-кнопку.
     *
     * @param chatId       ID чата
     * @param userId       ID пользователя
     * @param callbackData Данные, связанные с кнопкой
     */
    private void handleCallbackData(long chatId, long userId, String callbackData) {
        logger.info("Callback data received for chat {}: '{}'. User: {}", chatId, callbackData, userId);

        if (CALLBACK_ADD_REMARK.equals(callbackData)) {
            chatStates.put(chatId, ChatState.WAITING_FOR_REMARK_DETAILS);
            telegramBot.execute(new SendMessage(chatId, "Пожалуйста, введите ваше замечание в формате:\n" +
                    "Описание замечания; Позиция установки (число); да/нет\n" +
                    "Например: Мусор возде 10Д-1,2; 130; да"));
            logger.info("Set state to WAITING_FOR_REMARK_DETAILS for chat {} after callback '{}'.", chatId, callbackData);

        } else if (CALLBACK_VIEW_REMARKS.equals(callbackData)) {
            // Пользователь нажал "Посмотреть замечания"
            // Здесь должна быть логика для получения и отправки списка замечаний
            try {
                String remarksList = getRemarksForChat(chatId, userId);
                telegramBot.execute(new SendMessage(chatId, remarksList));
                logger.info("Sent remarks list to chat {}", chatId);
            } catch (Exception e) {
                logger.error("Error fetching remarks for chat {}: {}", chatId, e.getMessage(), e);
                telegramBot.execute(new SendMessage(chatId, "Не удалось загрузить ваши замечания. Попробуйте позже."));
            } finally {
                // После выполнения действия (или ошибки), сбрасываем состояние в IDLE
                chatStates.put(chatId, ChatState.IDLE);
                logger.info("Set state to IDLE for chat {} after viewing remarks. State was reset.", chatId);
            }
        } else {
            // Если получен неизвестный callback data
            telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG));
            chatStates.put(chatId, ChatState.IDLE); // Сбрасываем состояние
            logger.warn("Received unknown callback data '{}' for chat {}. Set state to IDLE.", callbackData, chatId);
        }
    }

    private void sendMainMenu(long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{new InlineKeyboardButton("➕ Добавить замечание").callbackData(CALLBACK_ADD_REMARK)},
                new InlineKeyboardButton[]{new InlineKeyboardButton("📋 Посмотреть замечания").callbackData(CALLBACK_VIEW_REMARKS)}
        );
        telegramBot.execute(new SendMessage(chatId, "Выберите действие:")
                .replyMarkup(inlineKeyboard));
        logger.info("Sent main menu with inline buttons to chat {}", chatId);
    }

    // Обработка команды /start
    private void handleStartCommand(long chatId, String firstName) {
        telegramBot.execute(new SendMessage(chatId, "Привет, " + firstName + "! 👋"));
        sendMainMenu(chatId); // Показываем главное меню
        chatStates.put(chatId, ChatState.IDLE); // Убедимся, что состояние IDLE
        logger.info("Sent welcome and main menu to chat {}. State set to IDLE.", chatId);
    }

    // Заглушка для получения списка замечаний
    private String getRemarksForChat(long chatId, long userId) {
        // TODO: Реализуйте логику получения замечаний через NotificationService
        return "Здесь будет список ваших замечаний.";
    }

    private Pair<byte[], Notification> downloadPhotoFileToDB(long chatId, PhotoSize[] photoSizes) {
        Pair<byte[], Notification> imagePair = null;
        // Existing code to download the photo...
        if (photoSizes == null || photoSizes.length == 0) {
            logger.error("Попытка скачать пустой файл фото {}", chatId);
            return null;
        }

        PhotoSize largestPhoto = photoSizes[photoSizes.length - 1];
        String fileId = largestPhoto.fileId();

        try {
            GetFile getFileRequest = new GetFile(fileId);
            GetFileResponse fileResponse = telegramBot.execute(getFileRequest);

            if (!fileResponse.isOk()) {
                logger.error("Failed to get file info from Telegram API for fileId {}. Error: {}", fileId, fileResponse.errorCode() + ": " + fileResponse.description());
                telegramBot.execute(new SendMessage(chatId, "Не удалось получить информацию о фотографии."));
            } else {
                File fileInfo = fileResponse.file();
                String filePath = fileInfo.filePath(); // Например: "photos/file_1.jpg"
                int size = Math.toIntExact(fileInfo.fileSize());
                // 4. Формируем полный URL для скачивания
                String botToken = telegramBot.getToken();
                URL downloadUrl = new URL("https://api.telegram.org/file/bot" + botToken + "/" + filePath);

                Path targetDir = Paths.get(this.downloadDirectory);
                Path fileName = Paths.get(filePath).getFileName(); // e.g., "file_1.jpg"

                Path targetFilePath = targetDir.resolve(fileName); // Full path to save the file

                try (InputStream in = downloadUrl.openStream()) {
                    Files.copy(in, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                    imagePair = Pair.of(in.readAllBytes(), responseMessageNote);

//                    if(noteId != null) {
//                        imageService.uploadImage(targetFilePath.toFile(), noteId);
//                    }

                    logger.info("Successfully downloaded file to: {}", targetFilePath);
                }
                // Existing error handling...
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return imagePair;
    }
}
//        MultipartFile multipartFile = new PathMultipartFile(
//                path, // Путь к файлу
//                originalFileName, // Имя файла
//                contentType, // MIME-тип
//                "file" // Имя поля формы (если нужно)
//        );
// --- Создаем ImageTask ---
//
// Вспомогательный метод для определения Content-Type
//    private String getContentType(String fileName) {
//        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
//            return "image/jpeg";
//        } else if (fileName.toLowerCase().endsWith(".png")) {
//            return "image/png";
//        } else if (fileName.toLowerCase().endsWith(".gif")) {
//            return "image/gif";
//        }
//        // Можно добавить другие типы или использовать MimeTypeMap
//        return "application/octet-stream"; // По умолчанию
//    }
//}


