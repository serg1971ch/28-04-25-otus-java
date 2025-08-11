package ru.otus.listener.homework;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Optional;
import ru.otus.listener.Listener;
import ru.otus.model.Message;

public class HistoryListener implements Listener, HistoryReader {

    private final Map<Long, Message> messageHistory;

    public HistoryListener(Map<Long, Message> messageHistory) {
        this.messageHistory = messageHistory;
    }

    @Override
    public void onUpdated(Message msg) {
        if (msg == null) {
            throw new UnsupportedOperationException();
        }
        Message messageCopy = msg.toBuilder().build();
        messageHistory.put(messageCopy.getId(), messageCopy);
    }

    @Override
    public Optional<Message> findMessageById(long id) {
        return ofNullable(messageHistory.get(id));
    }
}
