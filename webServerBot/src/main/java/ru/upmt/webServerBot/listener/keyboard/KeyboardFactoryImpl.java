package ru.upmt.webServerBot.listener.keyboard;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.listener.StateManager;


import java.util.ArrayList;
import java.util.List;

import static ru.upmt.webServerBot.CommandConst.*;
import static ru.upmt.webServerBot.model.ChatState.IDLE;

@Slf4j
@Component
public class KeyboardFactoryImpl implements KeyboardFactory {
    private final TelegramBot telegramBot;
    private final StateManager stateManager;
    private final PositionService positionService;

    public KeyboardFactoryImpl(TelegramBot telegramBot, StateManager stateManager, PositionService positionService) {
        this.telegramBot = telegramBot;

        this.stateManager = stateManager;
        this.positionService = positionService;
    }

    public void sendMainMenuPhotk(long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{new InlineKeyboardButton("➕ Добавить еще одно замечание").callbackData(COMMAND_SENT_PHOTO_CALLBACK)},
                new InlineKeyboardButton[]{new InlineKeyboardButton("📋 Посмотреть замечания").callbackData(CALLBACK_VIEW_REMARKS)}
        );
        telegramBot.execute(new SendMessage(chatId, "Выбрать фото: ").replyMarkup(inlineKeyboard));
        log.info("Sent main menu with inline buttons to chat {}", chatId);
    }

    public void sendContinueMenuPhotk(long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{new InlineKeyboardButton("\uD83D\uDCE4 Замечание готово к отправке. Отправить").callbackData(CALLBACK_SENT_ALL_PHOTOS)},
                new InlineKeyboardButton[]{new InlineKeyboardButton("📋 Посмотреть замечания").callbackData(CALLBACK_VIEW_REMARKS),
                new InlineKeyboardButton("\uD83D\uDDD1 Удалить замечание").callbackData(CALLBACK_DELETE_REMARK)
                }
        );
        telegramBot.execute(new SendMessage(chatId, "Выберите действие: ").replyMarkup(inlineKeyboard));
        log.info("Sent main menu with inline buttons to chat {}", chatId);
    }


    public void sendMainMenuBot(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Главное меню").callbackData(CALLBACK_MAIN_MENU), // Пример callback
                        new InlineKeyboardButton("Добавить замечание").callbackData(CALLBACK_ADD_REMARK),
                        new InlineKeyboardButton("Удалить замечание").callbackData(CALLBACK_DELETE_REMARK)
                }
        );
        telegramBot.execute(new SendMessage(chatId, "Добавьте замечание: ").replyMarkup(markup));
    }

    public void sendOperatorsNameMenuBot(long chatId) {
        telegramBot.execute(new SendMessage(chatId, " Выберите исполнителя  \uD83D\uDC47 или впишите сами")
                .replyMarkup(createPaginatedKeyboard(1)));
    }

    public void sendMessage(long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        telegramBot.execute(request);
        log.debug("Sent message to chat {}: {}", chatId, text);
    }

    public InlineKeyboardMarkup sendMainMenuButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Главное меню").callbackData(CALLBACK_MAIN_MENU), // Пример callback
                        new InlineKeyboardButton("Добавить фото с устраненным замечанием").callbackData(CALLBACK_ADD_REMARK),
                }
        );
        telegramBot.execute(new SendMessage(chatId, "Выберите действие:").replyMarkup(markup));
        return markup;
    }

    public void sendRemarkMenu(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Добавить замечание к фото").callbackData(CALLBACK_ADD_REMARK),
                        new InlineKeyboardButton("Посмотреть замечания к фото").callbackData(CALLBACK_VIEW_REMARKS)
                }
        );
        telegramBot.execute(new SendMessage(chatId, "Выберите действие с замечаниями:").replyMarkup(markup));
    }

    public void sendExistMenuButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{new InlineKeyboardButton("Отправить все фото").callbackData(COMMAND_SENT_PHOTO_CALLBACK), // Пример callback
                        new InlineKeyboardButton("Добавить замечание").callbackData(CALLBACK_ADD_PHOTO_REMARK),
                        new InlineKeyboardButton("Посмотреть замечания").callbackData(CALLBACK_VIEW_REMARKS)});
        telegramBot.execute(new SendMessage(chatId, "Выберите действие:").replyMarkup(markup));

    }


    // Метод для возврата в начальное состояние и показа главного меню
    public void resetChatStateAndAskAction(long chatId) {
        stateManager.updateState(chatId, IDLE);
        sendMainMenuButtons(chatId);
    }


    public InlineKeyboardMarkup createPaginatedKeyboard(int page) {
        int totalItems = 17; // Всего исполнителей (список из 17 человек)
        int itemsPerPage = 9; // Кнопок на странице
        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalItems);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // Собираем кнопки текущей страницы (по 4 в ряду)
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (int i = start; i < end; i++) {
            String name = getUserNameByIndex(i);
            String callback = getCallbackByIndex(i);

            currentRow.add(new InlineKeyboardButton(name).callbackData(callback));

            // Добавляем ряд, если набралось 4 кнопки или это последняя кнопка
            if (currentRow.size() == 4 || i == end - 1) {
                markup.addRow(currentRow.toArray(new InlineKeyboardButton[0]));
                currentRow.clear();
            }
        }

        // Кнопки навигации
        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (page > 1) {
            navRow.add(new InlineKeyboardButton("Назад").callbackData("page_" + (page - 1)));
        }

        if (end < totalItems) {
            navRow.add(new InlineKeyboardButton("Вперёд").callbackData("page_" + (page + 1)));
        }

        if (!navRow.isEmpty()) {
            markup.addRow(navRow.toArray(new InlineKeyboardButton[0]));
            log.info("page: {}, navRow: {}", page, navRow);
        }

        return markup;
    }

    // Вспомогательные методы (реализуйте по своему сценарию)
    private String getUserNameByIndex(int index) {
        String[] names = {
                "Шибанов", "Новоселов", "Агаев", "Золотухин",
                "Бегов", "Лорик", "Исламгалеев", "Вахитов",
                "Шаповалов", "Устимов", "Хисматулин", "Большаков", "Шарипов", "Омельченко",
                "Семерня", "Катеев", "Васалатьев"
        };
        return names[index];
    }

    private String getCallbackByIndex(int index) {
        String[] callbacks = {
                CALLBACK_SHIBANOV_MENU, CALLBACK_NOVOSELOV_MENU, CALLBACK_AGAEV_MENU, CALLBACK_ZOLOTUHIN_MENU,
                CALLBACK_BEGOV_MENU, CALLBACK_LORICK_MENU, CALLBACK_ISLAMGALEEV_MENU, CALLBACK_VAHITOV_MENU,
                CALLBACK_SHAPOVALOV_MENU, CALLBACK_USTIMOV_MENU, CALLBACK_HISMATULIN_MENU, CALLBACK_BOLSHAKOV_MENU,
                CALLBACK_SHARIPOV_MENU, CALLBACK_OMELCHENKO_MENU,
                CALLBACK_SEMERNYA_MENU, CALLBACK_KATEEV_MENU, CALLBACK_VASALATIEV_MENU
        };
        return callbacks[index];
    }

    @Override
    public InlineKeyboardMarkup createPaginatedKeyboardForPositions(int page, int itemsPerPage) {
        return createPaginatedKeyboardPositions(
                positionService.getAllPositions(),
                positionService.getAllCallbacks(),
                page,
                itemsPerPage
        );
    }

    private InlineKeyboardMarkup createPaginatedKeyboardPositions(List<String> items, List<String> callbacks, int page, int itemsPerPage) {
        if (page < 1) {
            page = 1;
        }
        int totalItems = items.size();
        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalItems);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        // Кнопки текущей страницы
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (int i = start; i < end; i++) {
            currentRow.add(
                    new InlineKeyboardButton(items.get(i))
                            .callbackData(callbacks.get(i))
            );
            if (currentRow.size() == 8 || i == end - 1) {
                markup.addRow(currentRow.toArray(new InlineKeyboardButton[0]));
                currentRow.clear();
            }
        }

        // Навигация
        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (page > 1) {
            navRow.add(new InlineKeyboardButton("Назад")
                    .callbackData("position_" + (page - 1)));
        }
        if (end < totalItems) {
            navRow.add(new InlineKeyboardButton("Вперёд")
                    .callbackData("position_" + (page + 1)));
        }
        if (!navRow.isEmpty()) {
            markup.addRow(navRow.toArray(new InlineKeyboardButton[0]));
        }

        return markup;
    }
}
