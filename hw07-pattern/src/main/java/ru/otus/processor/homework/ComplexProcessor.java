package ru.otus.processor.homework;

import ru.otus.listener.Listener;
import ru.otus.model.Message;
import ru.otus.processor.Processor;

import java.util.List;

public class ComplexProcessor {
    private final List<Processor> processors;
    private final List<Listener> listeners;

    public ComplexProcessor(List<Processor> processors, List<Listener> listeners) {
        this.processors = processors;
        this.listeners = listeners;
    }

    public Message process(Message message) {
        Message currentMessage = message;
        for (Processor processor : processors) {
            currentMessage = processor.process(currentMessage);
        }
        // После всех обработок, уведомляем слушателей
        notifyListeners(currentMessage);
        return currentMessage;
    }

    private void notifyListeners(Message message) {
        for (Listener listener : listeners) {
            listener.onUpdated(message);
        }
    }

// Дополнительные методы, если нужны (например, для добавления процессоров/слушателей динамически)
// public void addProcessor(Processor processor) { this.processors.add(processor); }
// public void addListener(Listener listener) { this.listeners.add(listener); }
}
