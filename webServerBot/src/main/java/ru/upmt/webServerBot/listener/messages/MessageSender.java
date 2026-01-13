package ru.upmt.webServerBot.listener.messages;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import ru.upmt.webServerBot.model.Notification;

import java.util.Optional;

public interface MessageSender {

    void sendMessage(long chatId, String text);

    void sendWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard);

    void sendWelcomeMessage(long chatId, Message message);

    //    void sendRemarksAsMessage(long chatId, RemarkWithImageDTO remark);
//    void sendNotificationsByChatId(long chatId);

    void sendPositionsMenu(long chatId);

    void sendPositionNotification(long chatId, Optional<Notification> notification, String position);

}
