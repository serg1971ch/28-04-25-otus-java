package ru.upmt.webServerBot.listener.handlers;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public interface PhotoHandler {
    void handle(Message msg) throws IOException, URISyntaxException;
}
