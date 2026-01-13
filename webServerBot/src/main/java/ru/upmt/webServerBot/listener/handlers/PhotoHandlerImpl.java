package ru.upmt.webServerBot.listener.handlers;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.listener.StateManager;
import ru.upmt.webServerBot.listener.keyboard.KeyboardFactory;
import ru.upmt.webServerBot.listener.messages.MessageSenderImpl;
import ru.upmt.webServerBot.listener.processors.PhotoProcessor;
import ru.upmt.webServerBot.listener.processors.RemarkReceiver;
import ru.upmt.webServerBot.model.ChatState;
import ru.upmt.webServerBot.model.ImageTask;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.model.TaskComplete;
import ru.upmt.webServerBot.service.ImageServiceDB;

import java.net.URISyntaxException;


import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import static ru.upmt.webServerBot.model.ChatState.*;


@Slf4j
@Component
public class PhotoHandlerImpl implements PhotoHandler {
    private final ImageServiceDB imageServiceDB;
    private final StateManager stateManager;
    private final PhotoProcessor photoProcessor;
    private final MessageSenderImpl messageSender;
    private final KeyboardFactory keyboardFactory;
    private final RemarkReceiver remarkReceiver;
    private Optional<Notification> responseMessage;
    private ImageTask imageTask;

    public PhotoHandlerImpl(ImageServiceDB imageServiceDB, StateManager stateManager, PhotoProcessor photoProcessor, MessageSenderImpl messageSender, KeyboardFactory keyboardFactory, RemarkReceiver remarkReceiver) {
        this.imageServiceDB = imageServiceDB;
        this.stateManager = stateManager;
        this.photoProcessor = photoProcessor;
        this.messageSender = messageSender;
        this.keyboardFactory = keyboardFactory;
        this.remarkReceiver = remarkReceiver;
    }

    public void handle(Message message) throws IOException, URISyntaxException {
        PhotoSize[] photoSizes = message.photo();
        long chatId = message.chat().id();

        ChatState currentState = stateManager.getCurrentState(chatId);

        if (photoSizes != null && photoSizes.length > 0) {
            switch (currentState) {
                case AWAITING_PHOTO_FOR_ADD_REMARK:
                    PhotoSize largestPhoto = photoSizes[photoSizes.length - 1];
                    responseMessage = remarkReceiver.getCurrentRemark(chatId);
                    photoProcessor.processPhoto(largestPhoto, chatId);
//                    imageTask = imageServiceDB.downloadAndSaveImageFromUrl(imageUrl, responseMessage.getId())
                    int sizeImages = imageServiceDB.findSizeImages(responseMessage.get().getId());
                    if (sizeImages != 2) { // Если еще не 2 фото, ждем второе;
                        keyboardFactory.sendOperatorsNameMenuBot(chatId);
                        log.info("current state " + stateManager.getCurrentState(chatId));

                        stateManager.updateState(chatId,AWAITING_OPERATOR_EXECUTED);
                    } else {
                        messageSender.sendMessage(chatId, "Все фото вашего замечания успешно добавлены!");
                        keyboardFactory.sendContinueMenuPhotk(chatId);
                        stateManager.updateState(chatId, IDLE);// Возвращаемся в IDLE
                        log.info("Два фото получено. Состояние изменено на {}", stateManager.getCurrentState(chatId));
                    }

                    break;

                case AWAITING_PHOTO_FOR_VIEW_REMARKS:
                    keyboardFactory.resetChatStateAndAskAction(chatId);
                    break;

                case AWAITING_PHOTO: // Если есть простое состояние ожидания фото
                    messageSender.sendMessage(chatId, "Выберите фото вашего замечания: " + responseMessage.get().getComment() + " после его устранения");
                    log.info("Здесь нужно закончить передачу фото");
//                    sendExistMenuButtons(chatId);
                    stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                    break;

                case AWAITING_EXIST:
                    messageSender.sendMessage(chatId, "Все фото вашего замечания: " + responseMessage.get().getComment() + "успешно добавлено!\uD83D\uDE03");
                    keyboardFactory.sendMainMenuPhotk(chatId);
                    stateManager.updateState(chatId, IDLE); // Возвращаемся в Idle
                    break;


                default:
                    // Если фото пришло, а мы его не ждали в текущем состоянии
                    messageSender.sendMessage(chatId, "Спасибо за фото!  \n" +
                            "Выберите действие:");
                    keyboardFactory.sendMainMenuPhotk(chatId);
                    // Показываем главное меню
                    stateManager.updateState(chatId, AWAITING_PHOTO); // Возвращаемся в Idle
                    break;
            }
        }
    }
}
