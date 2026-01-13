package ru.upmt.webServerBot.listener.processors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.model.Notification;

import java.util.Optional;

@Component
@Qualifier("remarkable")
public interface RemarkReceiver {
    Optional<Notification> getCurrentRemark(long chatId);
}
