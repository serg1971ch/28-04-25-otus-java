package ru.upmt.webServerBot.model;

public enum ChatState {
    IDLE, // Начальное состояние, ничего не ожидаем
    AWAITING_TEXT_FOR_ADD_REMARK,
    AWAITING_PHOTO, // Ожидаем, что пользователь отправит фото
    AWAITING_PHOTO_FOR_ADD_REMARK, // Ожидаем фото для добавления замечания
    AWAITING_PHOTO_FOR_VIEW_REMARKS, // Ожидаем фото для просмотра замечаний/ Ожидаем фото для просмотра замечаний
    AWAITING_EXIST,
    AWAITING_OPERATOR_EXECUTED,
    AWAITING_POSITION_NAME,
    AWAITING_EXECUTED_NAME
}
