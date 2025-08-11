package ru.otus;

import ru.otus.handler.ComplexProcessor;
import ru.otus.listener.Listener;
import ru.otus.listener.homework.HistoryListener;
import ru.otus.model.Message;
import ru.otus.model.ObjectForMessage;
import ru.otus.processor.Processor;
import ru.otus.processor.SwapField11AndField12Processor;
import ru.otus.processor.homework.CurrentDateTimeProvider;
import ru.otus.processor.homework.DateTimeProvider;
import ru.otus.processor.homework.ExceptionIfEvenProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HomeWork {

    /*
    Реализовать to do:
      1. Добавить поля field11 - field13 (для field13 используйте класс ObjectForMessage)
      2. Сделать процессор, который поменяет местами значения field11 и field12
      3. Сделать процессор, который будет выбрасывать исключение в четную секунду (сделайте тест с гарантированным результатом)
            Секунда должна определяьться во время выполнения.
            Тест - важная часть задания
            Обязательно посмотрите пример к паттерну Мементо!
      4. Сделать Listener для ведения истории (подумайте, как сделать, чтобы сообщения не портились)
         Уже есть заготовка - класс HistoryListener, надо сделать его реализацию
         Для него уже есть тест, убедитесь, что тест проходит
    */

    public static void main(String[] args) {
        /*
          по аналогии с Demo.class
          из элеменов "to do" создать new ComplexProcessor и обработать сообщение
        */
        // 1. Создаем слушателя истории
        HistoryListener historyListener = new HistoryListener();

        // 2. Создаем процессоры
        // Для процессора с проверкой секунд используем реальное время
        DateTimeProvider currentDateTimeProvider = new CurrentDateTimeProvider();
        Processor swapProcessor = new SwapField11AndField12Processor();
        Processor exceptionProcessor = new ExceptionIfEvenProcessor(currentDateTimeProvider);

        // 3. Создаем список процессоров
        List<Processor> processors = new ArrayList<>();
        processors.add(swapProcessor);
        processors.add(exceptionProcessor); // Добавляем процессор, который может выбросить исключение

        // 4. Создаем список слушателей
        List<Listener> listeners = new ArrayList<>();
        listeners.add(historyListener);

        // 5. Создаем ComplexProcessor
        ComplexProcessor complexProcessor = new ComplexProcessor(processors, (Consumer<Exception>) listeners);

        // 6. Создаем исходное сообщение
        Message initialMessage = new Message.Builder(1)
                .field1("Message 1")
                .field2("Message 2")
                .field3("Message 3")
                .field4("Message 4")
                .field5("Message 5")
                .field6("Message 6")
                .field7("Message 7")
                .field8("Message 8")
                .field9("Message 9")
                .field10("Message 10")
                .field11("Initial Field 11") // Значение для field11
                .field12("Initial Field 12") // Значение для field12
                .field13(new ObjectForMessage("Object Data")) // Значение для field13
                .build();

        System.out.println("--- Начальное сообщение ---");
        System.out.println(initialMessage);
        System.out.println("--------------------------\n");

        // 7. Обрабатываем сообщение
        try {
            Message processedMessage = complexProcessor.process(initialMessage);

            System.out.println("--- Обработанное сообщение ---");
            System.out.println(processedMessage);
            System.out.println("------------------------------\n");

            // 8. Выводим историю сообщений (если обработка прошла успешно)
            System.out.println("--- История сообщений ---");
            List<Message> history = historyListener.getHistory();
            if (history.isEmpty()) {
                System.out.println("История пуста.");
            } else {
                for (int i = 0; i < history.size(); i++) {
                    System.out.println("Шаг " + (i + 1) + ": " + history.get(i));
                }
            }
            System.out.println("--------------------------");

        } catch (RuntimeException e) {
            // Если процессор ExceptionIfEvenSecondProcessor выбросил исключение
            System.out.println("--- Произошла ошибка ---");
            System.err.println("Ошибка: " + e.getMessage());
            System.out.println("-------------------------\n");

            // Выводим историю, которая могла накопиться до возникновения исключения
            System.out.println("--- История сообщений (до ошибки) ---");
            List<Message> history = historyListener.getHistory();
            if (history.isEmpty()) {
                System.out.println("История пуста.");
            } else {
                for (int i = 0; i < history.size(); i++) {
                    System.out.println("Шаг " + (i + 1) + ": " + history.get(i));
                }
            }
            System.out.println("-------------------------------------");
        }
}
