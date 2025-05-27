package org.doggo.bbgtb.updates;

import com.google.gson.Gson;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GithubUpdateChecker {
        private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/%s/releases/latest";

        public void checkForUpdates(Consumer<UpdateInfo> onUpdateAvailable) {
            Task<UpdateInfo> task = new Task<>() {
                @Override
                protected UpdateInfo call() throws Exception {
                    String apiUrl = String.format(GITHUB_API_URL,
                            AppConfig.GITHUB_OWNER,
                            AppConfig.GITHUB_REPO);

                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/vnd.github+json");
                    conn.setRequestProperty("User-Agent", "JavaFX-Update-Checker");

                    if (conn.getResponseCode() != 200) {
                        throw new IOException("HTTP error code: " + conn.getResponseCode());
                    }

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()))) {
                        String json = reader.lines().collect(Collectors.joining());
                        return parseGitHubResponse(json);
                    }
                }
            };

            task.setOnSucceeded(e -> {
                UpdateInfo info = task.getValue();
                if (isNewVersion(info.getVersion())) {
                    onUpdateAvailable.accept(info);
                }
            });

            task.setOnFailed(e -> {
                System.err.println("Update check failed: " + task.getException().getMessage());
                task.getException().printStackTrace();
            });

            new Thread(task).start();
        }

        private UpdateInfo parseGitHubResponse(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json, UpdateInfo.class);
        }

        private boolean isNewVersion(String githubVersion) {
            // Удаляем нецифровые символы из версий
            String cleanCurrent = AppConfig.CURRENT_VERSION.replaceAll("[^\\d.]", "");
            String cleanGithub = githubVersion.replaceAll("[^\\d.]", "");

            String[] currentParts = cleanCurrent.split("\\.");
            String[] githubParts = cleanGithub.split("\\.");

            for (int i = 0; i < Math.max(currentParts.length, githubParts.length); i++) {
                int current = (i < currentParts.length) ? Integer.parseInt(currentParts[i]) : 0;
                int github = (i < githubParts.length) ? Integer.parseInt(githubParts[i]) : 0;

                if (github > current) return true;
                if (github < current) return false;
            }
            return false;
        }
}
