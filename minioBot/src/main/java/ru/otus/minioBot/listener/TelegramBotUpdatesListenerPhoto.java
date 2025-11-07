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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.otus.minioBot.model.ChatState;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.service.ImageServiceDB;
import ru.otus.minioBot.service.ImageServiceFS;
import ru.otus.minioBot.service.ImageServiceMinio;
import ru.otus.minioBot.service.NotificationService;
import ru.otus.minioBot.web.dto.RemarkWithImageDTO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.otus.minioBot.CommandConst.*;
import static ru.otus.minioBot.CommandConst.CALLBACK_ADD_REMARK;
import static ru.otus.minioBot.model.ChatState.*;

@Slf4j
@Service
public class TelegramBotUpdatesListenerPhoto implements UpdatesListener {
    private final String downloadDirectory;
    private final TelegramBot telegramBot;
    private final NotificationService notificationService;
    private final ImageServiceDB imageService; // Используйте его для сохранения фото
    private final Map<Long, ChatState> chatStates = new ConcurrentHashMap<>();
    private Notification responseMessage;
    String noteName; // Предполагается, что это имя фотографии/заметки, которое может быть связано с состоянием

    private static final Pattern REMARK_INPUT_PATTERN = Pattern.compile("(.*;)(.[0-9]{3})(.[да|нет])."); // Этот паттерн, кажется, не используется в текущем коде

    public TelegramBotUpdatesListenerPhoto(TelegramBot telegramBot, NotificationService notificationService,
                                           @Value("${app.download.dir:resources/photos}") String downloadDirectory, ImageServiceDB imageService) {
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
        this.downloadDirectory = downloadDirectory;
        this.imageService = imageService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        log.info("Telegram bot listener initialized.");
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            long chatId = 0;
            long userId = 0;
            String chatText = null;
            PhotoSize[] photoSize = null;
            CallbackQuery callbackQuery = update.callbackQuery();
            Message message = update.message();

            if (message != null) {
                chatId = message.chat().id();
                chatText = message.text();
                photoSize = message.photo();
                // Обработка других типов сообщений, если нужно
            } else if (callbackQuery != null) {
                chatId = callbackQuery.message().chat().id();
                chatText = callbackQuery.data(); // Данные из callback кнопки
                // Здесь мы не ожидаем фото, а данные из кнопок
            } else {
                continue; // Пропускаем другие типы обновлений
            }

            ChatState currentState = chatStates.getOrDefault(chatId, IDLE);
            log.debug("Received update from chat {}. Current state: {}. Text: {}, Photo present: {}",
                    chatId, currentState, chatText, false);

            if (callbackQuery != null) {
                log.debug("Received callback query from chat {}. Data: '{}'. User: {}", chatId, callbackQuery, userId);
                handleCallbackQuery(callbackQuery, chatId);
            } else {
                // Логика обработки текстовых сообщений и фото
                handleMessage(message, chatId, currentState);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery, long chatId) {
        String data = callbackQuery.data();
        log.info("Received callback query data: {} from chat {}", data, chatId);

        if (CALLBACK_ADD_REMARK.equals(data)) {
            chatStates.put(chatId, AWAITING_TEXT_FOR_ADD_REMARK);
            telegramBot.execute(new SendMessage(chatId, "Пожалуйста, введите ваше замечание в формате:\n" + "Описание замечания; через запятую номер позиции да/нет\n" + "Например: Мусор в районе дегазатора 10Д-1/1,2; 130 нет"));
            log.info("Текущее состояние бота: {}", chatStates);
        } else if (CALLBACK_ADD_PHOTO_REMARK.equals(data)) {
            sendMessage(chatId, "Выберите из файла свое фото для отправки выполненного замечания");
            log.info("Received callback query data: {} from chat {} chatState {}", data, chatId, chatStates);
            chatStates.put(chatId, AWAITING_PHOTO);
            log.info("Текущее состояние бота на стадии добавлении фото: {}", chatStates);
        } else if (CALLBACK_SENT_ALL_PHOTOS.equals(data)) {
            log.info("Получена команда: {}", data);
            sendMessage(chatId, "Все ваши фото будут отправлены");
            chatStates.put(chatId, IDLE);
            sendMainMenuButtons(chatId);
        } else if (COMMAND_SENT_PHOTO_CALLBACK.equals(data)) {
            sendMessage(chatId, "Замечание отправлено, хотите добавить еще одно АПК?");
            log.info("Received callback query data: {} from chat {}", data, chatId);
            chatStates.put(chatId, IDLE);
            sendMainMenuBot(chatId);
        } else if (CALLBACK_MAIN_MENU.equals(data)) {
            sendMessage(chatId, "Здесь будет основное меню бота, а пока нажмите /start");
        } else if (CALLBACK_VIEW_REMARKS.equals(data)) {
            // Предполагается, что у вас есть метод для получения замечаний
            List<RemarkWithImageDTO> remarks = getRemarksForChat(chatId);
            sendRemarksAsMessage(chatId, remarks);
            log.info("Вывод замечаний для чата {}: {}", chatId, remarks);
            chatStates.put(chatId, IDLE);
        } else {
            sendMessage(chatId, "Неизвестная команда из меню.");
        }
    }

    private void handleMessage(Message message, long chatId, ChatState currentState) {
        String text = message.text();
        PhotoSize[] photoSizes = message.photo();
        chatStates.put(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
        // --- Обработка фото ---
        if (photoSizes != null && photoSizes.length > 0) {
            PhotoSize largestPhoto = photoSizes[photoSizes.length - 1];
            var filePair = downloadPhotoFile(chatId, largestPhoto); // Скачиваем фото
            if (filePair != null) {
                largestPhoto = photoSizes[photoSizes.length - 1];

                chatStates.put(chatId, IDLE);
                imageService.uploadImageFromFStoDB(filePair);

                log.info("Результат сохраненного в базе фото: {}. Состояние бота на входе в обработку полученного фото: {}\n" +
                        " Messenger содержит текст - {}", filePair.getFirst().toString(), currentState, text);

            } else {
                sendMessage(chatId, "Не удалось сохранить фото. Нету замечания.");
                sendExistMenuButtons(chatId);
            }

            chatStates.put(chatId, AWAITING_PHOTO);

            switch (currentState) {
                case AWAITING_PHOTO_FOR_ADD_REMARK:
//                        if (responseMessage != null && responseMessage.getId() != null) {
                    log.info("Описание фото: {}. Состояние бота на входе в обработку полученного фото: {}\n Messenger содержит текст - {}", text, currentState, text);
                    log.info("Photo downloaded to: {}. Processing based on state: {}", filePair, currentState);
                    log.info("state handle photo: {} of notification_id: {},", chatStates, responseMessage.getId());
//                             }
                    chatStates.put(chatId, AWAITING_PHOTO);
                    break;

                case AWAITING_PHOTO_FOR_VIEW_REMARKS:
                    // Ищем замечания к этому фото
//                            List<Notification> remarks = imageService.getRemarksForImage(filePath); // Предполагаем, что у вас есть такой метод
//                            if (remarks != null && !remarks.isEmpty()) {
//                                StringBuilder remarksText = new StringBuilder("Замечания к вашему фото:\n");
//                                for (Notification remark : remarks) {
//                                    remarksText.append("- ").append(remark.getText()).append("\n"); // Пример: берем текст замечания
//                                }
//                                sendMessage(chatId, remarksText.toString());
//                            } else {
//                                sendMessage(chatId, "К этому фото пока нет замечаний.");
//                            }
                    // После просмотра замечаний, возвращаемся в состояние выбора действий
                    resetChatStateAndAskAction(chatId);
                    break;

                case AWAITING_PHOTO: // Если есть простое состояние ожидания фото
//                        sendMainMenuPhotk(chatId);
                    log.info("Здесь нужно закончить передачу фото");
                    sendExistMenuButtons(chatId);
                    chatStates.put(chatId, IDLE);
                    break;

                case AWAITING_EXIST:
                    sendMessage(chatId, "Все фото вашего замечания: " + responseMessage.getComment() + "успешно добавлено!");
//                    sendMainMenuButtons(chatId); // Показываем главное меню
                    chatStates.put(chatId, IDLE); // Возвращаемся в Idle
                    break;

                default:
                    // Если фото пришло, а мы его не ждали в текущем состоянии
//                    sendMessage(chatId, "Спасибо за фото!  \n" +
//                            "Выберите действие:");
                    sendMainMenuPhotk(chatId); // Показываем главное меню
                    chatStates.put(chatId, IDLE); // Возвращаемся в Idle
                    break;
            }
        }

        // --- Обработка текстовых сообщений ---
        else if (text != null) {
            switch (currentState) {
                case AWAITING_TEXT_FOR_ADD_REMARK:
                    if (!text.isEmpty()) {
                        responseMessage = handleRemarkInput(chatId, text);
                        chatStates.put(chatId, IDLE);
                    } else {
                        telegramBot.execute(new SendMessage(chatId, INVALID_MSG));
                    }
                    break;
                case AWAITING_PHOTO:
                    if (!text.isEmpty()) {
//                        sendMessage(chatId, HELP_MSG_UNCOMPLETED_PHOTO );
                        log.info("состояние {}", chatStates.get(chatId).name());
                    }
                case IDLE: // Если в начальном состоянии, ищем команды
                    if (COMMAND_START.equals(text)) {
                        sendWelcomeMessage(chatId, message);
                    } else if (COMMAND_ADD_PHOTO.equals(text)) { // Пример команды для начала добавления фото
                        chatStates.put(chatId, AWAITING_PHOTO_FOR_ADD_REMARK); // Переходим в состояние ожидания фото
                        sendMessage(chatId, "Отлично! Пожалуйста, отправьте фото, которое хотите добавить.");
                    } else if (COMMAND_ADD_REMARK_MENU.equals(text)) { // Пример команды для добавления замечания
                        sendRemarkMenu(chatId);
                    } else {
                        sendMessage(chatId, "Я вас не понимаю. Используйте команды или меню.");
                    }
                    break;

                default:
                    // Если получили текст, а не ждали его в текущем состоянии
                    sendMessage(chatId, "Я не ожидал текстовый ввод в этом состоянии. Пожалуйста, используйте меню.");
                    resetChatStateAndAskAction(chatId);
                    break;
            }
        }
    }

    // Обработка команды /start
//    private void handleStartCommand(long chatId, String firstName) {
//        telegramBot.execute(new SendMessage(chatId, "Привет, " + firstName + "! 👋"));
//        sendMainMenu(chatId); // Показываем главное меню
//        chatStates.put(chatId, IDLE); // Убедимся, что состояние IDLE
//        log.info("Sent welcome and main menu to chat {}. State set to IDLE.", chatId);
//    }


    private Notification handleRemarkInput(long chatId, String inputText) {
        Matcher matcher = REMARK_INPUT_PATTERN.matcher(inputText);

        if (matcher.matches()) {
            try {
                responseMessage = notificationService.processRemarkInput(chatId, inputText);
                telegramBot.execute(new SendMessage(chatId, responseMessage.getComment().replace(";", "") + " добавлено. Выберите фото для этого замечания. "));
                log.info("Remark details processed for chat {}. State reset to IDLE.", chatId);
//                sendMainMenuPhotk(chatId);
//                chatStates.put(chatId, IDLE);
            } catch (Exception e) {
                log.error("Unexpected error processing remark input from chat {}: {}", chatId, e.getMessage(), e);
                telegramBot.execute(new SendMessage(chatId, "Произошла внутренняя ошибка. Попробуйте позже."));
            }
        } else {
            log.warn("Invalid remark input format from chat {}. Input: '{}'. Expected format: 'Описание; Позиция; да/нет'", chatId, inputText);
            telegramBot.execute(new SendMessage(chatId, INVALID_MSG + " Формат ввода неверен.\n" +
                    "Пожалуйста, введите в формате: Описание; Позиция; да/нет\n" +
                    "Например: Мусор возде 10Д-1,2; 130; нет"));
        }
        return responseMessage;
    }

    // Заглушка для получения списка замечаний
    private List<RemarkWithImageDTO> getRemarksForChat(long chatId) {
        return notificationService.getRemarksForChat(chatId);
    }

    private void sendMainMenuPhotk(long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{new InlineKeyboardButton("Замечание готово к отправке. Отправить").callbackData(CALLBACK_SENT_ALL_PHOTOS)},
                new InlineKeyboardButton[]{new InlineKeyboardButton("➕ Добавить фото").callbackData(CALLBACK_ADD_PHOTO_REMARK)},
                new InlineKeyboardButton[]{new InlineKeyboardButton("📋 Посмотреть замечания").callbackData(CALLBACK_VIEW_REMARKS)}
        );
        telegramBot.execute(new SendMessage(chatId, "Выбрать фото: ").replyMarkup(inlineKeyboard));
        log.info("Sent main menu with inline buttons to chat {}", chatId);
    }


    private void sendMainMenuBot(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Главное меню").callbackData(CALLBACK_MAIN_MENU), // Пример callback
                        new InlineKeyboardButton("Добавить замечание").callbackData(CALLBACK_ADD_REMARK),
                }
        );
        telegramBot.execute(new SendMessage(chatId, "Добавьте замечание: ").replyMarkup(markup));
    }


    private void handleAddRemarkCommand(long chatId) {
        telegramBot.execute(new SendMessage(chatId, "Пожалуйста, введите ваше замечание в формате:\n" +
                "Описание замечания; Позиция установки (число); да/нет\n" +
                "Например: Мусор возде 10Д-1,2; 130; нет"));
        log.info("User initiated remark input via command /add_remark in chat {}, but no data provided. Sent instructions. State is WAITING_FOR_REMARK_DETAILS.", chatId);
    }

// --- Вспомогательные методы ---

    private void sendMessage(long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        telegramBot.execute(request);
        log.debug("Sent message to chat {}: {}", chatId, text);
    }

    private void sendMainMenuButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Отправить фото").callbackData(COMMAND_SENT_PHOTO_CALLBACK), // Пример callback
                        new InlineKeyboardButton("Добавить замечание").callbackData(CALLBACK_ADD_REMARK),
                        new InlineKeyboardButton("Посмотреть замечания").callbackData(CALLBACK_VIEW_REMARKS)
                }
        );
        telegramBot.execute(new SendMessage(chatId, "Выберите действие:").replyMarkup(markup));
    }

    private void sendRemarkMenu(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Добавить замечание к фото").callbackData(CALLBACK_ADD_REMARK),
                        new InlineKeyboardButton("Посмотреть замечания к фото").callbackData(CALLBACK_VIEW_REMARKS)
                }
        );
        telegramBot.execute(new SendMessage(chatId, "Выберите действие с замечаниями:").replyMarkup(markup));
    }

    private void sendExistMenuButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{new InlineKeyboardButton("Отправить все фото").callbackData(COMMAND_SENT_PHOTO_CALLBACK), // Пример callback
                        new InlineKeyboardButton("Добавить замечание").callbackData(CALLBACK_ADD_PHOTO_REMARK),
                        new InlineKeyboardButton("Посмотреть замечания").callbackData(CALLBACK_VIEW_REMARKS)});
        telegramBot.execute(new SendMessage(chatId, "Выберите действие:").replyMarkup(markup));

    }

    private void sendWelcomeMessage(long chatId, Message message) {
        sendMessage(chatId, "Привет, " + message.from().firstName() + " Я бот для работы с фотографиями и замечаниями.");
        sendMainMenuBot(chatId);
        chatStates.put(chatId, IDLE);
    }

    // Метод для возврата в начальное состояние и показа главного меню
    private void resetChatStateAndAskAction(long chatId) {
        chatStates.put(chatId, IDLE);
        sendMainMenuButtons(chatId);
    }

    // Ваш метод скачивания фото (перенесен сюда для удобства)
    private Pair downloadPhotoFile(long chatId, PhotoSize photoSize) {
        Pair<byte[], Notification> notificationPair = null;
        String fileId = photoSize.fileId();
        try {
            GetFile getFileRequest = new GetFile(fileId);
            GetFileResponse fileResponse = telegramBot.execute(getFileRequest);

            if (!fileResponse.isOk()) {
                log.error("Failed to get file info from Telegram API for fileId {}. Error: {}", fileId, fileResponse.errorCode() + ": " + fileResponse.description());
                sendMessage(chatId, "Не удалось получить информацию о фотографии.");
                return null;
            }

            File fileInfo = fileResponse.file();
            String filePath = fileInfo.filePath(); // Например: "photos/file_1.jpg"

            String botToken = telegramBot.getToken();
            URL downloadUrl = new URL("https://api.telegram.org/file/bot" + botToken + "/" + filePath);

            Path targetDir = Paths.get(this.downloadDirectory);
            String fileName = Paths.get(filePath).getFileName().toString();
            Path targetFilePath = targetDir.resolve(fileName);

            log.info("Downloading file from URL: {} to path: {}", downloadUrl, targetFilePath);

            try (InputStream in = downloadUrl.openStream()) {
                Files.copy(in, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Successfully downloaded file to: {}, chatState now: {}", targetFilePath, chatStates.get(chatId));
//                chatStates.put(chatId, IDLE);
//                sendMainMenuPhotk(chatId);
                chatStates.put(chatId, AWAITING_PHOTO);
                notificationPair = Pair.of(in.readAllBytes(), responseMessage);
                log.info("Successfully downloaded file to: {}, chatState now: {}", notificationPair.getSecond().getComment().replace(";", ""), chatStates.get(chatId));

                return notificationPair;  // Возвращаем путь к скачанному файлу
            }
        } catch (IOException e) {
            log.error("IO error downloading photo with fileId {} for chat {}: {}", fileId, chatId, e.getMessage(), e);
            sendMessage(chatId, "Произошла ошибка при скачивании фотографии (IO).");
            return null;
        } catch (Exception e) {
            log.error("Unexpected error downloading photo with fileId {} for chat {}: {}", fileId, chatId, e.getMessage(), e);
            sendMessage(chatId, "Произошла неожиданная ошибка при скачивании фотографии.");
            return null;
        }
    }

    public void sendRemarksAsMessage(long chatId, List<RemarkWithImageDTO> remarks) {
        StringBuilder messageBuilder = new StringBuilder();

        for (RemarkWithImageDTO remark : remarks) {
            // Добавьте комментарий
            messageBuilder.append("Комментарий: ").append(remark.getComment()).append("n");

            // Если изображение доступно, его можно закодировать в Base64 и добавить в сообщение
            if (remark.getImageData() != null) {
                List<byte[]> base64Image = remark.getImageData();
                messageBuilder.append("![Изображение](data:image/png;base64,").append(base64Image).append(")n");
            }

            messageBuilder.append("n"); // Разделитель между замечаниями
        }

        sendMessage(chatId, messageBuilder.toString());
    }
}
