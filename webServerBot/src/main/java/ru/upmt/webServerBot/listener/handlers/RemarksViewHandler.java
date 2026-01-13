package ru.upmt.webServerBot.listener.handlers;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InputFile;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.model.request.InputMedia;

import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.MessagesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.exceptions.ImageNotFoundException;
import ru.upmt.webServerBot.listener.messages.MessageSender;
import ru.upmt.webServerBot.listener.processors.RemarkReceiver;
import ru.upmt.webServerBot.model.*;
import ru.upmt.webServerBot.repository.ExecuterRepository;
import ru.upmt.webServerBot.service.ExecuteNotificationService;
import ru.upmt.webServerBot.web.service.NotificationDtoService;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.core.convert.TypeDescriptor.map;

@Slf4j
@Component
public class RemarksViewHandler {
    private static final DateTimeFormatter SENT_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TelegramBot bot;
    private final NotificationDtoService dtoService;
    private final RemarkReceiver remarkReceiver;
    private final MessageSender messageSender;
    private final ExecuterRepository executerRepository;
    private final ExecuteNotificationService executeNotificationService;

    public RemarksViewHandler(TelegramBot bot, NotificationDtoService dtoService, RemarkReceiver remarkReceiver, MessageSender messageSender, ExecuterRepository executerRepository, ExecuteNotificationService executeNotificationService) {
        this.bot = bot;
        this.dtoService = dtoService;
        this.remarkReceiver = remarkReceiver;
        this.messageSender = messageSender;
        this.executerRepository = executerRepository;
        this.executeNotificationService = executeNotificationService;
    }

    public void sendNotificationsByChatId(long chatId) throws ImageNotFoundException, IOException {
        Optional<Notification> remark = remarkReceiver.getCurrentRemark(chatId);
        log.info("Found {} notifications", remark.get().getComment());

        List<ImageTask> imageTasks = remark.get().getImageTasks();

        // Отправляем комментарий
        String text = "Комментарий: " + Optional.ofNullable(remark.get().getComment()).orElse("");
        bot.execute(new SendMessage(chatId, text));

        // Собираем фото в медиагруппу
        List<InputMedia> mediaGroup = new ArrayList<>();
        for (ImageTask img : imageTasks) {

            byte[] bytes = img.getBytes();
            if (bytes == null || bytes.length == 0) continue;

// Используем конструктор с File

            InputStream inputStream = new ByteArrayInputStream(img.getBytes());
            InputFile inputFile = new InputFile(inputStream.readAllBytes(), img.getName(), img.getMediaType());

            SendResponse resp = bot.execute(new SendPhoto(chatId, String.valueOf(new InputFile(inputStream.readAllBytes(), img.getName(), img.getMediaType()))));

            log.info("SendPhoto ok={} code={} desc={}", resp.isOk(), resp.errorCode(), resp.description());
        }
    }

    public void sendNotificationByChatId(long chatId) {
        Optional<Notification> remark = remarkReceiver.getCurrentRemark(chatId);
        if (!remark.isPresent()) {
            bot.execute(new SendMessage(chatId, "Замечаний нет"));
            return;
        }

        String comleted = "";
        TaskComplete statusComplete = remark.get().getTaskComplete();
        if (statusComplete.equals(TaskComplete.COMPLETE)) {
            comleted = "Устранено";
        } else {
            comleted = "Не устранено";
        }

        String sentStr = Optional.ofNullable(remark.get().getSentDate())
                .map(d -> d.format(SENT_DATE_FMT))
                .orElse("не указана");

        List<User> users = executeNotificationService.getUsers(remark.get().getId());
        String names = users.stream().map(user -> user.getFirstName() + " " + user.getLastName()).collect(Collectors.joining(", "));

        // Отправляем комментарий
        String text = "Замечание АПК: " + Optional.ofNullable(remark.get().getComment()).orElse("")
                + "\n Позиция: " + Optional.ofNullable(remark.get().getPosition()).orElse("")
                + "\n Исполнитель: " + names
                + "\n Дата отправки замечания: " + sentStr
                + "\n Состояние: " + comleted;
        bot.execute(new SendMessage(chatId, text));

        List<ImageTask> imageTasks = remark.get().getImageTasks();
        List<InputMedia> mediaGroup = new ArrayList<>();

        for (ImageTask img : imageTasks) {
            byte[] bytes = img.getBytes();
            if (bytes == null || bytes.length == 0) {
                log.warn("Пустые данные изображения: {}", img.getName());
                continue;
            }

            try {
                InputFile inputFile = new InputFile(bytes, img.getName(), img.getMediaType());
                InputMediaPhoto mediaPhoto = new InputMediaPhoto(inputFile.getBytes());
                mediaGroup.add(mediaPhoto);
            } catch (Exception e) {
                log.error("Ошибка при подготовке фото {}: {}", img.getName(), e.getMessage());
            }
        }

        // Отправляем все фото одним сообщением
        if (!mediaGroup.isEmpty()) {
            SendMediaGroup sendMediaGroup =
                    new SendMediaGroup(chatId, mediaGroup.toArray(new InputMedia[0]));
            MessagesResponse response = bot.execute(sendMediaGroup);
            log.info("Отправка фото: ok={}, код={}, описание={}",
                    response.isOk(), response.errorCode(), response.description());
        }
    }

    private List<ImageTask> getFileName(long chatId) {
        Optional<Notification> notification = remarkReceiver.getCurrentRemark(chatId);
        return notification.get().getImageTasks();
    }
}
