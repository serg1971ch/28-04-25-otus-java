package ru.upmt.webServerBot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.listener.handlers.CallbackQueryHandler;
import ru.upmt.webServerBot.listener.handlers.MessageHandler;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Component
public class TelegramUpdateHandler implements UpdatesListener {
    private final CallbackQueryHandler callbackQueryHandler;
    private final MessageHandler messageHandler;
    private final TelegramBot telegramBot;

    @Autowired
    public TelegramUpdateHandler(CallbackQueryHandler callbackQueryHandler,
                                 MessageHandler messageHandler,
                                 TelegramBot telegramBot) {
        this.callbackQueryHandler = callbackQueryHandler;
        this.messageHandler = messageHandler;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        log.info("Telegram bot listener initialized.");
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null) {
                try {
                    messageHandler.handle(update.message());
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else if (update.callbackQuery() != null) {
                try {
                    callbackQueryHandler.handle(update.callbackQuery());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
