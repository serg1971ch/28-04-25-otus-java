package ru.skypro.homework.check;

import org.springframework.stereotype.Component;
import ru.skypro.homework.exceptions.WrongNumberException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.skypro.homework.constants.Constants.PHONE_PATTERN;
@Component
public class CheckService {
    private final Pattern pattern = Pattern.compile(PHONE_PATTERN);

    public void checkPhone(String phone){
        Matcher mat = pattern.matcher(phone);
        if(!mat.matches()){
            throw new WrongNumberException("Номер телефона не соответствует образцу");
        }
    }
}
