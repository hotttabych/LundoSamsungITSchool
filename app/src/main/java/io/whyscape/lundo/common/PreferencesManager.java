package io.whyscape.lundo.common;

import android.content.Context;
import android.content.SharedPreferences;
import io.whyscape.lundo.domain.model.AiMode;

public class PreferencesManager {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_AI_MODE = "ai_mode";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAiMode(AiMode mode) {
        prefs.edit().putString(KEY_AI_MODE, mode.name()).apply();
    }

    public AiMode getAiMode() {
        String modeName = prefs.getString(KEY_AI_MODE, AiMode.LUNO.name());
        for (AiMode mode : AiMode.getEntries()) {
            if (mode.name().equals(modeName)) {
                return mode;
            }
        }
        return AiMode.LUNO;
    }
}