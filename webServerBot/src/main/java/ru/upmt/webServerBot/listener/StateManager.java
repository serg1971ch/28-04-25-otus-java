package ru.upmt.webServerBot.listener;

import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.model.ChatState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.upmt.webServerBot.model.ChatState.IDLE;

@Component
public class StateManager {

    private final Map<Long, ChatState> chatStates = new ConcurrentHashMap<>();

    public ChatState getCurrentState(long chatId) {
        return chatStates.getOrDefault(chatId, IDLE);
    }

    public void updateState(long chatId, ChatState newState) {
        chatStates.put(chatId, newState);
    }

    public void resetState(long chatId) {
        chatStates.put(chatId, IDLE);
    }
}

