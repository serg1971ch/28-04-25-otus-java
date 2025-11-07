package ru.shiba.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton; // Импорт для инлайн-кнопки
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup; // Импорт для инлайн-клавиатуры
import com.pengrad.telegrambot.model.request.KeyboardButton; // Импорт для обычной кнопки
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup; // Импорт для обычной клавиатуры
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.shiba.CommandConst;
import ru.shiba.exceptions.IncorrectMessageException;
import ru.shiba.service.NotificationService;

import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.shiba.CommandConst.REMARK_ADD_PHOTO;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    ;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final String downloadDirectory;
    private final TelegramBot telegramBot;
    private final NotificationService notificationService; // Ваш сервис

    // Константы для callback data, чтобы избежать "магических строк"
    private static final String CALLBACK_ADD_REMARK = "action_add_remark";
    private static final String CALLBACK_VIEW_REMARKS = "action_view_remarks"; //
    private static final Pattern INPUT_PATTERN = Pattern.compile("(.*;)(.[0-9]{3})(.[да|нет]).+");

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationService notificationService, @Value("${app.download.dir:/path/to/downloads}") String downloadDirectory) {
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
        this.downloadDirectory = downloadDirectory;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        logger.info("Telegram bot listener initialized.");
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            // Обработка обычных сообщений (команды, ввод данных)
            long chatId;
            CallbackQuery callbackQuery = null;
            if (update.message() != null) {
                Message message = update.message();
                String chatText = message.text();
                chatId = message.chat().id();
                Matcher matcher =  null;
                if(chatText != null) {
                    matcher = INPUT_PATTERN.matcher(chatText);
                    logger.debug("Received message from chat {}: '{}'", chatId, chatText);
                }

                else if (chatText.startsWith(CommandConst.START_CMD)) {
                    handleStartCommand(chatId, message.from().firstName(), update);
                    logger.info("Telegram bot started successfully and handleStartCommand was called.");
                } else if (matcher.matches()) {
                    handleAddRemarkCommand(chatId, chatText);
                } else {
                    telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG));
                }
            } else if (update.callbackQuery() != null) {
                callbackQuery = update.callbackQuery();
                String callbackData = callbackQuery.data();
                chatId = callbackQuery.message().chat().id();
                logger.debug("Received callback query: {}", callbackQuery.data());
                if (callbackQuery.message() != null) {
                    logger.info("кнопочка !!Добавить замечание !! нажата и ->>> код входит в handleCallBackData()  {}. Data: '{}'", chatId, callbackData);
                    handleCallbackData(chatId, callbackData);//только выводит сообщение о формате ввода, дальше нужно поймать введенные данные.
                }

            } else if (update.message() != null && update.message().photo() != null) {
                PhotoSize[] photoSizes = update.message().photo();
                PhotoSize largestPhoto = photoSizes[photoSizes.length - 1]; // Берем самую большую версию
                String fileId = largestPhoto.fileId(); // Получаем fileId
                chatId = update.message().chat().id(); // ID чата, чтобы отправить ответ
                long userId = update.message().from().id(); // ID пользователя

                logger.info("Found photo with fileId: {} in chat {}. Dimensions: {}x{}", fileId, chatId, largestPhoto.width(), largestPhoto.height());

                // Теперь нужно скачать файл, используя этот fileId
                downloadPhotoFile(chatId, fileId);
            } else {
                telegramBot.execute(new SendMessage(update.message().chat().id(), "хуйня какая-та...."));
                logger.warn("Received callback query with null message. Data: '{}'", callbackQuery.data());
                // Возможно, стоит ответить пользователю, если это возможно (например, если есть chat ID)
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleStartCommand(long chatId, String firstName, Update update) {
        if (update.message().text().equals(CommandConst.START_CMD)) {
            telegramBot.execute(new SendMessage(chatId, CommandConst.WELCOME + firstName + "!"));
            sendMainMenu(chatId);
            logger.info("Sent welcome and main menu to chat {}", chatId);
        } else {
            telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG));
        }
    }

    // Обработка команды для добавления замечания (ручной ввод)
    private void handleAddRemarkCommand(long chatId, String notificationInput) {
        notificationService.processRemarkInput((int) chatId, notificationInput); // Или notificationService.processRemarkInput(chatId, commandAndData);
        logger.info("Notification remark command executed. Input '{}'", notificationInput);
    }

    // Обработка callback data от инлайн-кнопок
    private void handleCallbackData(long chatId, String callbackData) {
        // Теперь здесь будет логика обработки нажатий на инлайн-кнопки
        logger.info("После установки замечание в методе содержится текстовое значение: '{}'", callbackData);
        if (CALLBACK_ADD_REMARK.equals(callbackData)) {
            telegramBot.execute(new SendMessage(chatId, "Пожалуйста, введите ваше замечание в формате:\n" +
                    "Описание замечания; Позиция установки (число); да/нет\n" +
                    "Например: `Мусор возде 10Д-1,2; 130; нет`"));
            // TODO: Возможно, нужно установить флаг для этого чата, что бот ожидает ввод данных
            // и затем в блоке обработки обычных сообщений проверять этот флаг.

        } else if (CALLBACK_VIEW_REMARKS.equals(callbackData)) {
            // Пользователь нажал "Просмотреть замечания"
            // Тут будет логика получения замечаний из БД и отправки их пользователю
            // Например:
            telegramBot.execute(new SendMessage(chatId, "Показываю список ваших замечаний..."));
            // notificationService.showRemarks(chatId); // Пример вызова метода из сервиса

        } else {
            telegramBot.execute(new SendMessage(chatId, "Неизвестная команда."));
        }
    }

    // Метод для отправки главного меню с ИНЛАЙН-кнопками
    private void sendMainMenu(long chatId) {
        // Создаем инлайн-кнопки
        InlineKeyboardButton addRemarkButton = new InlineKeyboardButton("Добавить замечание")
                .callbackData(CALLBACK_ADD_REMARK); // Указываем callback data для этой кнопки

        InlineKeyboardButton viewRemarksButton = new InlineKeyboardButton("Просмотреть замечания")
                .callbackData(CALLBACK_VIEW_REMARKS); // Указываем callback data для другой кнопки

        // Создаем инлайн-клавиатуру
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[][]{ // Строка 1
                        {addRemarkButton, viewRemarksButton}
                        // Можно добавить другие строки кнопок, если нужно
                        // , {anotherButton}
                });

        SendMessage message = new SendMessage(chatId, "Выберите действие:")
                .replyMarkup(keyboard); // Прикрепляем инлайн-клавиатуру

        telegramBot.execute(message);
    }

    private void downloadPhotoFile(long chatId, String fileId) {
        try {
            // 1. Создаем запрос GetFile
            GetFile getFileRequest = new GetFile(fileId);

            // 2. Выполняем запрос

            GetFileResponse fileResponse = telegramBot.execute(getFileRequest);

//            if (fileResponse.ok()) {
            // 3. Получаем информацию о файле, включая filePath
            com.pengrad.telegrambot.model.File fileInfo = fileResponse.file();
            String filePath = fileInfo.filePath(); // Например: "photos/file_1.jpg"

            // 4. Формируем полный URL для скачивания
            // Можно использовать getFullFilePath от pengrad, если он доступен и настроен
            // Или сформировать вручную:
            String botToken = telegramBot.getToken(); // Получаем токен бота
            URL downloadUrl = new URL("https://api.telegram.org/file/bot" + botToken + "/" + filePath);

            // 5. Скачиваем файл
            Path targetDir = Paths.get(this.downloadDirectory); // Укажите путь к директории для сохранения
            String fileName = Paths.get(filePath).getFileName().toString(); // Получаем имя файла из filePath
            Path targetFilePath = targetDir.resolve(fileName); // Полный путь для сохранения

            logger.info("Downloading file from URL: {} to path: {}", downloadUrl, targetFilePath);

            try (InputStream in = downloadUrl.openStream()) {
                Files.copy(in, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Successfully downloaded file to: {}", targetFilePath);

                // Отправляем подтверждение пользователю
                telegramBot.execute(new SendMessage(chatId, "Фотография успешно скачана: " + fileName));
            }
//            } else {
//                logger.error("Failed to get file info from Telegram API. Error: {}", fileResponse.errorCode() + ": " + fileResponse.description());
//                telegramBot.execute(new SendMessage(chatId, "Не удалось скачать фотографию. Попробуйте позже."));
//            }
        } catch (Exception e) {
            logger.error("Error downloading photo with fileId {} for chat {}: {}", fileId, chatId, e.getMessage(), e);
            telegramBot.execute(new SendMessage(chatId, "Произошла ошибка при скачивании фотографии."));
        }
    }
}
