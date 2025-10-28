package ru.otus.minioBot;

public class CommandConst {
    public static final String START_CMD = "/add";
    public static final String WELCOME = "Привет, ";
    public static final String ADD_REMARK_CMD = "1. Добавьте описание замечания;-через точку с запятой , номер \n" +
            "2. позиции(130-173)\n" +
            "3. да/нет ";
    public static final String HELP_MSG_UNCOMPLETED_PHOTO = "Выберите фото с нарушением: ";
    public static final String HELP_MSG_DESCRIPTION = "Описание замечания: ";

    public static final String ADD_REMARK = "action_add_remark";
    public static final String REMARK_ADDED = "Ваше замечание:";
    //    public static final String CALLBACK_ADD_REMARK = "/add";
    public static final String INVALID_MSG = "Неверное сообщение или команда";
    public static final String CALLBACK_ADD_REMARK = "action_add_remark";
    public static final String CALLBACK_VIEW_REMARKS = "action_view_remarks";
}
