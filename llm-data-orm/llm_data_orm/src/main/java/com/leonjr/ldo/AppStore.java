package com.leonjr.ldo;

import java.util.List;

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
    private boolean debugAll;
    private String testSetPath;
    private String pagesRegex;

    public static AppStore getInstance(StartupConfiguration startupConfiguration, String tableName, boolean debugAll,
            String testSetPath, String pagesRegex) {
        if (instance == null) {
            instance = new AppStore(startupConfiguration, tableName, debugAll, testSetPath, pagesRegex);
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

    public static StartupConfiguration getStartConfigs() {
        return AppStore.getInstance().getStartupConfiguration();
    }

    /**
     * Checks if the pages regex is set to pick specific pages or a range of pages.
     * 
     * @return true if the regex is comma-separated or a range, false otherwise.
     */
    public boolean isPagePicksOrRange() {
        String regex = AppStore.getInstance().getPagesRegex();
        if (regex == null || regex.isEmpty()) {
            return false;
        }
        boolean commaSeparated = regex.contains(",");
        boolean range = regex.contains("-");
        return commaSeparated || range;
    }

    /**
     * Returns a list of page numbers based on the regex defined in the AppStore.
     * 
     * @param totalPages the total number of pages in the document
     * @return a list of page numbers or a range of pages based on the regex
     * @throws Exception if the regex is invalid or if any page number is out of
     *                   bounds
     */
    public List<Integer> getPicksOrRangePages(int totalPages) throws Exception {
        String regex = AppStore.getInstance().getPagesRegex();
        if (regex == null || regex.isEmpty()) {
            return null;
        }
        boolean commaSeparated = regex.contains(",");
        boolean range = regex.contains("-");

        if (commaSeparated && range) {
            throw new Exception("Invalid pages regex: " + regex);
        }

        if (commaSeparated) {
            String[] parts = regex.split(",");
            for (String part : parts) {
                int page = Integer.parseInt(part.trim());
                if (page < 1 || page > totalPages) {
                    throw new Exception("Invalid page number: " + page + " in " + regex);
                }
            }
            return List.of(parts).stream().map(Integer::parseInt).toList();
        } else if (range) {
            String[] parts = regex.split("-");
            if (parts.length != 2) {
                throw new Exception("Invalid range format: " + regex);
            }
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            if (start > end) {
                throw new Exception("Invalid range: start cannot be greater than end in " + regex);
            }
            if (start < 1 || end < 1 || start > totalPages || end > totalPages) {
                throw new Exception("Invalid range: pages must be between 1 and " + totalPages + " in " + regex);
            }
            return java.util.stream.IntStream.rangeClosed(start, end).boxed().toList();
        } else {
            if (Integer.parseInt(regex) < 1 || Integer.parseInt(regex) > totalPages) {
                throw new Exception("Invalid page number: " + regex + " in " + regex);
            }
            return List.of(Integer.parseInt(regex));
        }
    }
}
