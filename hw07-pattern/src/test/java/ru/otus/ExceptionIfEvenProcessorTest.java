package ru.otus;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.otus.model.Message;
import ru.otus.processor.Processor;
import ru.otus.processor.homework.DateTimeProvider;
import ru.otus.processor.homework.ExceptionIfEvenProcessor;

public class ExceptionIfEvenProcessorTest {
    @Test
    void processMessageWhenEvenSecondThrowsException() {
        DateTimeProvider mockDateTimeProvider = Mockito.mock(DateTimeProvider.class);

        LocalDateTime evenSecondTime = LocalDateTime.of(2023, 10, 27, 10, 30, 10); // Секунда = 10 (четная)
        Mockito.when(mockDateTimeProvider.getCurrentDateTime()).thenReturn(evenSecondTime);

        Processor processor = new ExceptionIfEvenProcessor(mockDateTimeProvider);

        Message message = new Message.Builder(1).field1("Test Field 1").build();
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            processor.process(message);
        });

        assertTrue(thrownException.getMessage().contains("Четная секунда!"));
        assertTrue(thrownException.getMessage().contains(evenSecondTime.toString()));
    }

    @Test
    void processMessageWhenOddSecondDoesNotThrowException() {
        DateTimeProvider mockDateTimeProvider = Mockito.mock(DateTimeProvider.class);

        LocalDateTime oddSecondTime = LocalDateTime.of(2023, 10, 27, 10, 30, 11); // Секунда = 11 (нечетная)
        Mockito.when(mockDateTimeProvider.getCurrentDateTime()).thenReturn(oddSecondTime);

        Processor processor = new ExceptionIfEvenProcessor(mockDateTimeProvider);

        Message message = new Message.Builder(1).field1("Test Field 1").build();

        Message resultMessage = processor.process(message);

        assertNotNull(resultMessage);

        assertEquals(message, resultMessage);
    }
}
