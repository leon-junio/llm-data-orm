package com.leonjr.ldo;

import com.leonjr.ldo.app.models.StartupConfiguration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppStore {
    private StartupConfiguration startupConfiguration;
    private static AppStore instance;

    public static AppStore getInstance(StartupConfiguration startupConfiguration) {
        if (instance == null) {
            instance = new AppStore(startupConfiguration);
        }
        return instance;
    }

}
