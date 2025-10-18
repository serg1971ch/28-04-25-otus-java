package ru.shiba;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.httpBot.service.NotificationService;

import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationService notificationService; // Ваш сервис

    // Константы для callback data, чтобы избежать "магических строк"
    private static final String CALLBACK_ADD_REMARK = "action_add_remark";
    private static final String CALLBACK_VIEW_REMARKS = "action_view_remarks"; // Пример другой кнопки

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationService notificationService) {
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
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
            if (update.message() != null) {
                Message message = update.message();
                String chatText = message.text();
                long chatId = message.chat().id();
                long userId = message.from().id(); // ID пользователя Telegram

                logger.debug("Received message from chat {}: '{}'", chatId, chatText);

                if (chatText != null) {
                    if (chatText.startsWith(CommandConst.START_CMD)) {
                        handleStartCommand(chatId, message.from().firstName());
                        logger.info("Telegram bot started successfully and handleStartCommand was called.");
                    } else if (chatText.startsWith(CommandConst.ADD_REMARK)) { // Если пользователь ввел команду вручную
                        handleAddRemarkCommand(chatId, chatText);
                        telegramBot.execute(new SendMessage(chatId, chatText));
                    } else {
                        // Если это не команда, и мы ожидаем ввод данных (например, после нажатия кнопки)
                        // TODO: Добавить логику для обработки ввода данных, если бот находится в "режиме ожидания"
                        // Пока что, если это не команда, то сообщение считается некорректным
                        telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG));
                    }
                }
            }
            // Обработка нажатия инлайн-кнопки (callbackQuery)
            else if (update.callbackQuery() != null) {
                CallbackQuery callbackQuery = update.callbackQuery();
                // Важно: callbackQuery.message() может быть null, если сообщение было удалено.
                // Но для большинства случаев оно есть.
                logger.debug("Received callback query: {}", callbackQuery.data());
                if (callbackQuery.message() != null) {
                    long chatId = callbackQuery.message().chat().id();
                    String callbackData = callbackQuery.data();
                    logger.info("месседж от callbackQuery: {}", callbackQuery.message());
                    logger.info("кнопочка !!Добавить замечание !! нажата и ->>> код входит в handleCallBackData()  {}. Data: '{}'", chatId, callbackData);
                    handleCallbackData(chatId, callbackData);//только выводит сообщение о формате ввода, дальше нужно поймать введенные данные.
//                    logger.info("текст мессенджа {}", update.message().text());
//                    if (update.message().text()!= null) {
//                        logger.info("текст введеного сообщения {}", update.message().text());
//                    }
                } else {
                    telegramBot.execute(new SendMessage(update.message().chat().id(), "хуйня какая-та...."));
                    logger.warn("Received callback query with null message. Data: '{}'", callbackQuery.data());
                    // Возможно, стоит ответить пользователю, если это возможно (например, если есть chat ID)
                }
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleStartCommand(long chatId, String firstName) {
        telegramBot.execute(new SendMessage(chatId, firstName + "!"));
        // Показываем меню с инлайн-кнопками после команды /start
        sendMainMenu(chatId);
        logger.info("Sent welcome and main menu to chat {}", chatId);
    }

    // Обработка команды для добавления замечания (ручной ввод)
    private void handleAddRemarkCommand(long chatId, String userInput) {
        telegramBot.execute(new SendMessage(chatId, "обработка комманды"));
        if (userInput.startsWith(CommandConst.ADD_REMARK)) {
            // Извлекаем текст ПОСЛЕ команды ADD_REMARK_CMD
            // Например, если userInput = "/add_remark Описание; 123; да",
            // то `commandAndData` будет " Описание; 123; да"
            String commandAndData = userInput.substring(CommandConst.ADD_REMARK_CMD.length()).trim();
            logger.info("command response user: {}", commandAndData);
            // Передаем в сервис для парсинга и сохранения.
            // Убедитесь, что NotificationService.processRemarkInput ожидает chatId как int
            // В ваших предыдущих примерах он был long. Приведите к типу, который ожидает сервис.
            // Если сервис ожидает long chatId, то casting (int)chatId не нужен.
            String responseMessage = notificationService.processRemarkInput((int) chatId, commandAndData); // Или notificationService.processRemarkInput(chatId, commandAndData);
            telegramBot.execute(new SendMessage(chatId, responseMessage));
        } else {
            // Это случай, когда пользователь отправил текст, но он не начинается с нужной команды.
            // Возможно, это ответ после нажатия кнопки, который нужно обработать иначе.
            // Или это просто некорректное сообщение.
            // TODO: Добавить логику обработки, если это не команда ADD_REMARK_CMD,
            // но является ожидаемым вводом данных.
            // В текущей версии, если это не команда, то выводим ошибку.
            logger.warn("Комманда не содержит какого либо сообщения in handleAddRemarkCommand from chat {}, и приложение тупо охлопывается по ошибке 130: '{}'", chatId, userInput);
            telegramBot.execute(new SendMessage(chatId, CommandConst.INVALID_MSG));
        }
    }

    // Обработка callback data от инлайн-кнопок
    private void handleCallbackData(long chatId, String callbackData) {
        // Теперь здесь будет логика обработки нажатий на инлайн-кнопки
        logger.info("После установки замечание в методе содержится текстовое значение: '{}'", callbackData);
        if (CALLBACK_ADD_REMARK.equals(callbackData)) {
            // Пользователь нажал "Добавить замечание" через инлайн-кнопку
            // Нам нужно либо отправить сообщение с инструкцией, либо перевести бота в режим ожидания ввода
            // Например:
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
}
