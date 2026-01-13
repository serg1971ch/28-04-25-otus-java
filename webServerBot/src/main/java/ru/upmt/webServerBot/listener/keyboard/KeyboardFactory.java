package ru.upmt.webServerBot.listener.keyboard;

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

import java.util.List;

public interface KeyboardFactory {

    void sendMainMenuPhotk(long chatId);

    void sendContinueMenuPhotk(long chatId);

    void sendMainMenuBot(long chatId);

    void sendOperatorsNameMenuBot(long chatId);

    void sendMessage(long chatId, String text);

    InlineKeyboardMarkup sendMainMenuButtons(long chatId);

    void sendRemarkMenu(long chatId);

//    InlineKeyboardMarkup createPaginatedKeyboardPosition(int page);

    void sendExistMenuButtons(long chatId);

    void resetChatStateAndAskAction(long chatId);

    InlineKeyboardMarkup createPaginatedKeyboard(int page);

//    InlineKeyboardMarkup createPaginatedKeyboard(
//            List<String> items,
//            List<String> callbacks,
//            int page,
//            int itemsPerPage
//    );

    InlineKeyboardMarkup createPaginatedKeyboardForPositions(int page, int itemsPerPage);
}
