package com.leonjr.ldo.confs.helper;

import com.leonjr.ldo.Main;
import com.leonjr.ldo.confs.models.StartupConfiguration;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;

public class YmlHelper {

    public static StartupConfiguration getStartupConfiguration() {
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("app_config.yml");

        if (inputStream == null) {
            throw new RuntimeException("Arquivo config.yml não encontrado!");
        }

        StartupConfiguration config = yaml.loadAs(inputStream, StartupConfiguration.class);

        if (config == null) {
            throw new RuntimeException("Arquivo config.yml não encontrado!");
        }

        return config;
    }

}