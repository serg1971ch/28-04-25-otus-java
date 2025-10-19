package ru.otus.httpBot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.httpBot.CommandConst;
import ru.otus.httpBot.service.NotificationService;

import java.util.List;

@Service
public class  TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationService notificationService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationService notificationService) {
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
//        for (Update update : updates) {
//            Message message = update.message();
//            if (message != null && message.text() != null) {
//                String chatText = message.text();
//                long chatId = message.chat().id();
//
//                if (chatText.startsWith(CommandConst.START_CMD)) {
//                    telegramBot.execute(new SendMessage(chatId, CommandConst.WELCOME + message.from().firstName() + "!"));
//                    telegramBot.execute(new SendMessage(chatId, CommandConst.HELP_MSG_TITLE));
//                    notificationService.parseMessage((int) chatId, chatText);
//                    // Вызов метода для отправки кнопки меню
//                    sendMainMenu(chatId);
//                }
//            }
//
//            // Обработка нажатия кнопки
//            if (update.callbackQuery() != null) {
//                assert message != null;
//                String chatText = message.text();
//                long chatId = message.chat().id();
//                String callbackData = update.callbackQuery().data();
//                if ("callback_data".equals(callbackData)) {
//                    telegramBot.execute(new SendMessage(chatId, "Вы нажали кнопку!"));
//                }
//            }
//        }
//        return UpdatesListener.CONFIRMED_UPDATES_ALL;
        for (Update update : updates) {
            try {
                // Handle message updates
                if (update.message() != null && update.message().text() != null) {
                    processMessageUpdate(update.message());
                }

                // Handle callback query updates
                if (update.callbackQuery() != null) {
                    processCallbackQuery(update.callbackQuery());
                }
            } catch (Exception e) {
                logger.error("Error processing update", e);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processMessageUpdate(Message message) {
        String chatText = message.text();
        long chatId = message.chat().id();

        if (chatText.startsWith(CommandConst.START_CMD)) {
            telegramBot.execute(new SendMessage(chatId, CommandConst.WELCOME + message.from().firstName() + "!"));
            telegramBot.execute(new SendMessage(chatId, CommandConst.HELP_MSG_TITLE));
            notificationService.parseMessage((int) chatId, chatText);
            sendMainMenu(chatId);
        }
    }

    private void processCallbackQuery(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.message().chat().id();
        String callbackData = callbackQuery.data();

        if ("callback_data".equals(callbackData)) {
            telegramBot.execute(new SendMessage(chatId, "Вы нажали кнопку!"));
        }
    }

    private void sendMainMenu(long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[][] {
                        { new KeyboardButton("Отправить замечания") }
                }
        ).resizeKeyboard(true).oneTimeKeyboard(true);

        SendMessage message = new SendMessage(chatId, "Выберите опцию:")
                .replyMarkup(keyboard);

        telegramBot.execute(message);
    }

}

//    private void sendNotificationButton(Message message) {
//        long chatId = message.chat().id();
//
//        // Создание кнопки
//        KeyboardButton button = new KeyboardButton("Отправить уведомления");
//        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(button).resizeKeyboard(true);
//
//        // Отправка сообщения с кнопкой
//        telegramBot.execute(chatId, "Нажмите кнопку для отправки уведомлений:", keyboard);
//    }

//    private void sendMainMenu(long chatId) {
//        KeyboardButton button = new KeyboardButton("Отправить замечания");
//        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(button).resizeKeyboard(true).oneTimeKeyboard(true);
//
//        SendMessage message = new SendMessage(chatId, "Выберите опцию:")
//                .replyMarkup(keyboard);
//
//        telegramBot.execute(message);
//    }
//}


//private void handleCallbackQuery(CallbackQuery callbackQuery) {
//    var data = callbackQuery.getData;
//    var chatId = callbackQuery.getFrom().getId();
//    var user = callbackQuery.getFrom();
//    switch (data):
//        case "my_name" -> sendMyName(chatId, user);
//        case "random" -> sendRandom(chatId);
//        case "long_process" -> sendImage(chatId);
//    default -> sendMessage(chatId, "Unckown command');
//}
//
//
//private void sendMessage(Long chatId, String message) {
//    SendMessage message = SendMessage.buildeer()
//            .text(message)
//            .chatId(chatId)
//            .build();
//    telegramClient.execute(message);
//}
//
//private void sendRandom(Long chatId) {
//    var randomInt = ThreadLocalRandom.current().next();
//    sendMessage(chatId, "Ваше рандомное число" + randomInt);
//}
//
//private void sendImage(Long chatId) {
//    new Thread(() -> {
//        var imageUrl = "https://picsum.photos/200";
//        try {
//            URL url = new URL(imageUrl);
//            var inputStream = url.openStream();
//
//            SendPhoto sendPhoto = SendPhoto.builder()
//                    .chatId(chatId)
//                    .photo(new InputFile())
//                    .caption("your random photo")
//                    .build()
//
//            telegramClient.execute(sendPhoto);
//
//        } catch (TelegramApiException | IOException e) {
//            throw new RuntimeException(e);
//        }
//
//
//    }).start();
//}

