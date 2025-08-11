package ru.otus.processor;

import ru.otus.model.Message;

public class SwapField11AndField12Processor implements Processor {
    @Override
    public Message process(Message message) {
        return message.toBuilder()
                .field11(message.getField12()) // Устанавливаем field11 значением из field12
                .field12(message.getField11()) // Устанавливаем field12 значением из field11
                .build();
    }
}
