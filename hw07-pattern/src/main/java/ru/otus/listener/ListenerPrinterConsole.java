package ru.otus.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListenerPrinterConsole implements Listener {
    private static final Logger logger = LoggerFactory.getLogger(ListenerPrinterConsole.class);
    private final List<Message> messageHistory = new ArrayList<>();

    @Override
    public void onUpdated(Message msg) {
        Message messageCopy = msg.toBuilder().build();
        messageHistory.add(messageCopy);
        logger.info("oldMsg:{}", msg);
    }

    public List<Message> getHistory() {
        return Collections.unmodifiableList(messageHistory);
    }

    public void clearHistory() {
        messageHistory.clear();
    }
}
