package com.leonjr.ldo;

import com.leonjr.ldo.app.models.LLMConfig;
import com.leonjr.ldo.app.models.StartupConfiguration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppStore {
    private StartupConfiguration startupConfiguration;
    private static AppStore instance;
    private String tableName;

    public static AppStore getInstance(StartupConfiguration startupConfiguration, String tableName) {
        if (instance == null) {
            instance = new AppStore(startupConfiguration, tableName);
        }
        return instance;
    }

    public static AppStore getInstance() {
        return instance;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public LLMConfig getLlmConfig() {
        return startupConfiguration.getApp().getLlmConfig();
    }

}
