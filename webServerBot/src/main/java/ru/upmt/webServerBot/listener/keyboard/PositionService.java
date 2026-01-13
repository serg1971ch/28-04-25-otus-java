package ru.upmt.webServerBot.listener.keyboard;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static ru.upmt.webServerBot.CommandConst.*;


@Service
public class PositionService {

    private final String[] names = {
            "130", "131", "132", "133",
            "134","135", "148", "149",
            "150", "151", "152", "140", "136", "137-139", "141",
            "142", "144", "173",
            "168", "169", "143", "144",
            "146", "155", "166", "153", "80"
    };

    private final String[] callbacks = {
            CALLBACK_130_MENU, CALLBACK_131_MENU, CALLBACK_132_MENU, CALLBACK_133_MENU, CALLBACK_134_MENU,
            CALLBACK_135_MENU, CALLBACK_148_MENU, CALLBACK_149_MENU,
            CALLBACK_150_MENU, CALLBACK_151_MENU, CALLBACK_152_MENU, CALLBACK_140_MENU, CALLBACK_136_MENU,
            CALLBACK_137_139_MENU, CALLBACK_141_MENU, CALLBACK_142_MENU, CALLBACK_144_MENU, CALLBACK_173_MENU,
            CALLBACK_168_MENU, CALLBACK_169_MENU, CALLBACK_143_MENU, CALLBACK_144_MENU,
            CALLBACK_146_MENU, CALLBACK_155_MENU, CALLBACK_166_MENU, CALLBACK_153_MENU,
            CALLBACK_80_MENU
    };

    public List<String> getAllPositions() {
        return Arrays.asList(names);
    }

    public List<String> getAllCallbacks() {
        return Arrays.asList(callbacks);
    }
}
