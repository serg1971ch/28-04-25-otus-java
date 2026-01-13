package ru.upmt.webServerBot.listener.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.listener.StateManager;
import ru.upmt.webServerBot.listener.keyboard.KeyboardFactory;
import ru.upmt.webServerBot.listener.messages.MessageSenderImpl;
import ru.upmt.webServerBot.listener.processors.RemarkReceiver;
import ru.upmt.webServerBot.model.ExecuteNotification;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.model.User;
import ru.upmt.webServerBot.service.ExecuteNotificationService;
import ru.upmt.webServerBot.service.NotificationService;
import ru.upmt.webServerBot.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.upmt.webServerBot.CommandConst.*;
import static ru.upmt.webServerBot.model.ChatState.*;


@Slf4j
@Component
public class CallbackQueryHandler {
    private final TelegramBot telegramBot;
    private final StateManager stateManager;
    private final MessageSenderImpl messageSender;
    private final KeyboardFactory keyboardFactory;
    private final RemarksViewHandler remarksViewHandler;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ExecuteNotificationService executeNotificationService;
    private final RemarkReceiver receiver;

    public CallbackQueryHandler(TelegramBot telegramBot, StateManager stateManager,
                                MessageSenderImpl messageSender,
                                KeyboardFactory keyboardFactory, RemarksViewHandler remarksViewHandler, UserService userService, NotificationService notificationService, ExecuteNotificationService executeNotificationService, RemarkReceiver receiver) {
        this.telegramBot = telegramBot;
        this.stateManager = stateManager;
        this.messageSender = messageSender;
        this.keyboardFactory = keyboardFactory;
        this.remarksViewHandler = remarksViewHandler;
        this.userService = userService;
        this.notificationService = notificationService;
        this.executeNotificationService = executeNotificationService;
        this.receiver = receiver;
    }

    public void handle(CallbackQuery callbackQuery) throws IOException {
        long chatId = callbackQuery.message().chat().id();
        String data = callbackQuery.data();
        String cqId = callbackQuery.id();

        Optional<Notification> responseMessage = receiver.getCurrentRemark(chatId);

        log.debug("Callback from chat {}: data='{}'", chatId, data);

        if (data == null) {
            telegramBot.execute(new AnswerCallbackQuery(cqId).text("Пустой callback").showAlert(false));
            return;
        }

        if (data != null && data.startsWith("page_")) {
            try {
                int newPage = Integer.parseInt(data.substring(5)); // "page_2" -> 2
                InlineKeyboardMarkup newKeyboard = keyboardFactory.createPaginatedKeyboard(newPage);

                telegramBot.execute(
                        new EditMessageReplyMarkup(chatId, callbackQuery.message().messageId())
                                .replyMarkup(newKeyboard)
                );

                // обязательно ответить на callback, чтобы Telegram не показывал "часики"
                telegramBot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            } catch (NumberFormatException ex) {
                log.warn("Bad page number in callback: {}", data, ex);
                telegramBot.execute(new AnswerCallbackQuery(callbackQuery.id()).text("Неверная страница"));
            }
            return; // обработано
        }

        // Обработка пагинации (ключевая часть!)
        if (data.startsWith("position_")) {
            try {
                int page = Integer.parseInt(data.substring("position_".length()));
                InlineKeyboardMarkup newKeyboard = keyboardFactory.createPaginatedKeyboardForPositions(page, 4);

                telegramBot.execute(
                        new EditMessageReplyMarkup(chatId, callbackQuery.message().messageId())
                                .replyMarkup(newKeyboard)
                );
                telegramBot.execute(new AnswerCallbackQuery(cqId).showAlert(false));

            } catch (NumberFormatException e) {
                log.warn("Invalid page number in callback: {}", data, e);
                telegramBot.execute(new AnswerCallbackQuery(cqId)
                        .text("Неверный номер страницы").showAlert(true));
            } catch (Exception e) {
                log.error("Error updating pagination", e);
                telegramBot.execute(new AnswerCallbackQuery(cqId).text("Ошибка").showAlert(true));
            }
            return;
        }

        switch (data) {
            case CALLBACK_ADD_REMARK -> {
                stateManager.updateState(chatId, AWAITING_TEXT_FOR_ADD_REMARK);
                messageSender.sendMessage(chatId, "Введите замечание в формате: ...");
            }
            case CALLBACK_ADD_PHOTO_REMARK -> {
//                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                messageSender.sendWithKeyboard(chatId, "Выберите фото:", keyboardFactory.sendMainMenuButtons(chatId));
            }

            case CALLBACK_SENT_ALL_PHOTOS -> {
                log.info("Получена команда: {}", data);
                stateManager.updateState(chatId, IDLE);
                keyboardFactory.sendMainMenuBot(chatId);
            }

            case COMMAND_SENT_PHOTO_CALLBACK -> {
                messageSender.sendMessage(chatId, "Замечание отправлено, хотите добавить еще одно АПК?");
                log.info("Received callback query data: {} from chat {}", data, chatId);
                stateManager.updateState(chatId, IDLE);
                keyboardFactory.sendMainMenuBot(chatId);
            }

            case CALLBACK_MAIN_MENU -> {
                messageSender.sendMessage(chatId, "Здесь будет основное меню бота, а пока нажмите /start");
            }

            case CALLBACK_VIEW_REMARKS -> {
                // Предполагается, что у вас есть метод для получения замечаний
                remarksViewHandler.sendNotificationByChatId(chatId);
//                log.info("Вывод замечаний для чата {}: {}", chatId, builder);
//                messageSender.sendMessage(chatId, String.valueOf(builder));
                stateManager.updateState(chatId, IDLE);
                keyboardFactory.sendMainMenuBot(chatId);
            }

            case CALLBACK_130_MENU, CALLBACK_131_MENU, CALLBACK_132_MENU, CALLBACK_133_MENU, CALLBACK_134_MENU,
                 CALLBACK_135_MENU,
                 CALLBACK_136_MENU, CALLBACK_137_139_MENU, CALLBACK_140_MENU, CALLBACK_141_MENU,
                 CALLBACK_142_MENU, CALLBACK_144_MENU, CALLBACK_146_MENU, CALLBACK_148_MENU, CALLBACK_149_MENU,
                 CALLBACK_150_MENU, CALLBACK_151_MENU, CALLBACK_152_MENU, CALLBACK_153_MENU, CALLBACK_155_MENU,
                 CALLBACK_166_MENU, CALLBACK_168_MENU, CALLBACK_169_MENU, CALLBACK_173_MENU, CALLBACK_80_MENU -> {
                log.info("current state in keyboard of callback position: {} and data {}", stateManager.getCurrentState(chatId), data);
                String position = extractNumbers(data);
                Optional<Notification> notification = receiver.getCurrentRemark(chatId);
                messageSender.sendPositionNotification(chatId, notification, position);

                messageSender.sendMessage(chatId, "Позиция " + position + " cохранена . Теперь добавьте фото замечания.");
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);

                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
            }

            case CALLBACK_SHIBANOV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Шибанов");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: " +
                        responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_NOVOSELOV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Новоселов");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_AGAEV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Агаев");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_ISLAMGALEEV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Исламгалеев");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: " +
                        responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_VAHITOV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Вахитов");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_HISMATULIN_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Хисматулин");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_SHAPOVALOV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Шаповалов");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: " +
                        responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_ZOLOTUHIN_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Золотухин");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_USTIMOV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Устимов");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_BOLSHAKOV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Большаков");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: " +
                        responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_BEGOV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Бегов");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: " +
                        responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_LORICK_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Лорик");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: " +
                        responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_SHARIPOV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Шарипов");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_OMELCHENKO_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Омельченко");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }


            case CALLBACK_KATEEV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Катеев");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: " +
                        responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_VASALATIEV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Васалатев");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_USHAROV_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Ушаров");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: " +
                        responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_SEMERNYA_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }

            case CALLBACK_ZAMESIN_MENU -> {
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                setUsers(chatId, "Замесин");
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + responseMessage.get().getComment() + " после его устранения");
            }


            // ... остальные кейсы
            default -> messageSender.sendMessage(chatId, "Неизвестная команда.");
        }
    }

    public static String extractNumbers(String input) {
        // Регулярное выражение для поиска чисел
        log.info("number is {}", input);
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        // Поиск всех чисел в строке
        boolean found = false; // Флаг для отслеживания наличия совпадений
        while (matcher.find()) {
            found = true; // Устанавливаем флаг, если совпадение найдено
            System.out.println("Найдено число: " + matcher.group());
            input = input.substring(matcher.start(), matcher.end());
        }

        // Проверка, были ли найдены числа
        if (!found) {
            System.out.println("Числа не найдены в строке: " + input);
        }

        return input;
    }

    private void setUsers(long chatId, String name) {
        Notification notification = receiver.getCurrentRemark(chatId).get();
        executeNotificationService.saveNotification(name, notification);
    }
}
