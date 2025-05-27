package org.doggo.bbgtb;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.doggo.bbgtb.updates.GithubUpdateChecker;
import org.doggo.bbgtb.updates.UpdateInfo;

import java.util.*;

public class Main extends Application {
    private ComboBox<String> languageCombo = new ComboBox<>();
    private ListView<String> resultList = new ListView<>();
    private Map<String, ObservableList<String>> themesByLanguage = new HashMap<>();
    private ObservableList<String> currentThemes;
    @Override
    public void start(Stage primaryStage) {
        GithubUpdateChecker checker = new GithubUpdateChecker();
        checker.checkForUpdates(this::showUpdateDialog);

        initData();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        resultList.setCellFactory(lv -> new ListCell<String>() {
            private final Text text = new Text();
            private final Tooltip tooltip = new Tooltip();

            {
                TextFlow flow = new TextFlow(text);
                flow.setMaxWidth(resultList.getWidth() - 20); // Отступы
                setGraphic(flow);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setTooltip(tooltip);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText("");
                    tooltip.setText("");
                } else {
                    text.setText(item);
                    tooltip.setText(item);
                    text.setWrappingWidth(resultList.getWidth() - 20);
                }
            }
        });

        HBox languageBox = new HBox(10);
        languageCombo.getItems().addAll("Русский", "English");
        languageCombo.setValue("Русский");
        languageCombo.setOnAction(e -> updateThemes());
        languageBox.getChildren().addAll(new Label("Язык:"), languageCombo);

        TextField patternField = new TextField();
        patternField.setPromptText("Введите шаблон (например, __н_а_ _ом__т_)");

        HBox buttonBox = new HBox(10);
        Button clear = new Button("Очистить");
        clear.setOnAction(a -> {
            patternField.setText("");
            showFullList();
        });

        buttonBox.getChildren().add(clear);



        patternField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) performSearch(newValue);
            else showFullList();
        });


        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Копировать");
        copyItem.setOnAction(event -> {
            String selectedItem = resultList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedItem);
                clipboard.setContent(content);
            }
        });
        contextMenu.getItems().add(copyItem);

        // Устанавливаем контекстное меню для ListView
        resultList.setContextMenu(contextMenu);

        resultList.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) { // Проверяем Ctrl+C
                String selectedItem = resultList.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(selectedItem);
                    clipboard.setContent(content);
                }
            }
        });

        root.getChildren().addAll(languageBox, patternField, buttonBox, resultList);

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("Угадай постройку");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initData() {
        themesByLanguage.put("Русский", FXCollections.observableList(Constants.RUSSIAN_THEMES));

        themesByLanguage.put("English", FXCollections.observableList(Constants.ENGLISH_THEMES));

        currentThemes = themesByLanguage.get("Русский");
        showFullList();
    }



    private void updateThemes() {
        String lang = languageCombo.getValue();
        currentThemes = themesByLanguage.get(lang);
        showFullList();
    }

    private List<String> performSearch(String pattern) {
        String patternWithoutSpaces = pattern.replace(" ", "");
        int length = patternWithoutSpaces.length();

        try {
            List<String> filtered = filterThemes(currentThemes, pattern, length);
            List<String> sorted = sortAlphabetically(filtered);

            resultList.setItems(FXCollections.observableList(sorted));
            return sorted;
        } catch (NumberFormatException ex) {
            showAlert("Ошибка", "Некорректная длина!");
            return null;
        }
    }

    private List<String> filterThemes(List<String> themes, String pattern, int requiredLength) {
        List<String> result = new ArrayList<>();
        for (String theme : themes) {
            String themeWithoutSpaces = theme.replace(" ", "");
            if (themeWithoutSpaces.length() != requiredLength) continue;

            if (theme.length() != pattern.length()) continue;

            boolean matches = true;
            for (int i = 0; i < theme.length(); i++) {
                char tChar = theme.charAt(i);
                char pChar = pattern.charAt(i);

                if (pChar == '_') {
                    if (tChar == ' ') {
                        matches = false;
                        break;
                    }
                } else if (pChar != ' ' && Character.toLowerCase(tChar) != Character.toLowerCase(pChar)) {
                    matches = false;
                    break;
                }
            }

            if (matches) result.add(theme);
        }
        return result;
    }

    private List<String> sortAlphabetically(List<String> sorted) {
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    private void showFullList() {
        List<String> sorted = new ArrayList<>(currentThemes);
        sorted.sort((a, b) -> {
            int lenCompare = Integer.compare(
                    a.replace(" ", "").length(),
                    b.replace(" ", "").length()
            );
            return lenCompare != 0 ? lenCompare : a.compareToIgnoreCase(b);
        });
        resultList.setItems(FXCollections.observableList(sorted));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {

        launch(args);
    }

    private void showUpdateDialog(UpdateInfo info) {
        VBox content = new VBox(10);
        Label versionLabel = new Label("Доступна новая версия: " + info.getVersion());
        TextArea changes = new TextArea(info.getBody());
        changes.setEditable(false);
        changes.setWrapText(true);

        HBox linksBox = new HBox(10);
        for (UpdateInfo.Asset asset : info.getAssets()) {
            Hyperlink link = new Hyperlink(asset.getName());
            link.setOnAction(e -> getHostServices().showDocument(asset.getDownloadUrl()));
            linksBox.getChildren().add(link);
        }

        Hyperlink releaseLink = new Hyperlink("Страница релиза");
        releaseLink.setOnAction(e -> getHostServices().showDocument(info.getUrl()));

        content.getChildren().addAll(
                versionLabel,
                new Label("Изменения:"),
                changes,
                new Label("Скачать:"),
                linksBox,
                releaseLink
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Доступно обновление");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefSize(600, 400);
        alert.showAndWait();
    }
}