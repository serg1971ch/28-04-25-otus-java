package ru.upmt.webServerBot.listener.processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.listener.StateManager;
import ru.upmt.webServerBot.listener.keyboard.KeyboardFactory;
import ru.upmt.webServerBot.listener.messages.MessageSenderImpl;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.service.ImageServiceDB;
import ru.upmt.webServerBot.model.ImageTask;
import ru.upmt.webServerBot.service.NotificationService;

import java.nio.file.Path;
import java.io.IOException;
import java.net.URL;

import java.util.Optional;

import static ru.upmt.webServerBot.model.ChatState.AWAITING_PHOTO_FOR_ADD_REMARK;

@Component
@Slf4j
public class PhotoProcessor {

    private final TelegramBot telegramBot;
    private final ImageServiceDB imageServiceDB;
    private final StateManager stateManager;
    private final MessageSenderImpl messageSender;
    private final RemarkReceiver remarkReceiver;

    @Autowired
    public PhotoProcessor(TelegramBot telegramBot,
                          ImageServiceDB imageServiceDB, NotificationService notificationService,
                          StateManager stateManager,
                          MessageSenderImpl messageSender, KeyboardFactory keyboardFactory,
                          RemarkReceiver remarkReceiver) {
        this.telegramBot = telegramBot;
        this.imageServiceDB = imageServiceDB;
        this.stateManager = stateManager;
        this.messageSender = messageSender;
        this.remarkReceiver = remarkReceiver;
    }

    /**
     * Обрабатывает полученное фото, скачивает его и сохраняет
     *
     * @param photoSize объект фото из Telegram
     * @param chatId    идентификатор чата
     * @return путь к сохранённому файлу или null при ошибке
     */
    public void processPhoto(PhotoSize photoSize, long chatId) throws IOException {
        Optional<Notification> notification = remarkReceiver.getCurrentRemark(chatId);
        URL url = downloadAndSavePhoto(photoSize, chatId);
        log.info("Processing photo received url: {}", url);
        if (url != null && notification.isPresent()) {

            try {
                ImageTask image = imageServiceDB.saveImageFromUrl(url, notification.get().getId());
                log.info("почему image  null here: image ------ {}", image);
                if (image != null) {
                    log.info("imageTask have been saved: {}", image.getName());
                }
//                imageServiceDB.saveImageTask(Optional.ofNullable(image));
                log.info("Photo successfully saved for chat {} at path: {}", chatId, notification.get().getImageTasks().stream().map(ImageTask::getPathFile).toList());
            } catch (Exception e) {
                log.error("Error processing photo for chat {}: {}", chatId, e.getMessage(), e);
                messageSender.sendMessage(chatId, "Произошла ошибка при обработке фото. Попробуйте снова.");
            }
        } else {
            log.warn("Image download failed for URL: {} and notificationId: {}", url, notification.get().getId());
            messageSender.sendMessage(chatId, "Не удалось загрузить фото. Проверьте ссылку.");
        }
    }

    /**
     * Скачивает фото из Telegram и сохраняет на диск
     *
     * @param photoSize объект фото
     * @param chatId    идентификатор чата
     * @return путь к сохранённому файлу
     * @throws IOException при ошибках скачивания/сохранения
     */
    public URL downloadAndSavePhoto(PhotoSize photoSize, long chatId) throws IOException {
        String fileId = photoSize.fileId();

        // Получаем информацию о файле
        GetFile getFileRequest = new GetFile(fileId);
        GetFileResponse fileResponse = telegramBot.execute(getFileRequest);

        if (!fileResponse.isOk()) {
            throw new IOException("Failed to get file info: " + fileResponse.errorCode() + " - " + fileResponse.description());
        }

        File fileInfo = fileResponse.file();
        String filePath = fileInfo.filePath();

        // Формируем URL для скачивания
        String botToken = telegramBot.getToken();
        URL downloadUrl = new URL("https://api.telegram.org/file/bot" + botToken + "/" + filePath);

        log.info("Downloading photo from {} to database", downloadUrl);

        stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
        return downloadUrl;
    }
}

