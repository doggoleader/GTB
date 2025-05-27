package org.doggo.bbgtb.updates;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateInfo {
    @SerializedName("tag_name")
    private String version;       // Версия релиза (например "v1.0.0")
    @SerializedName("html_url")
    private String url;       // Ссылка на страницу релиза

    @SerializedName("body")
    private String body;           // Описание изменений
    @SerializedName("assets")
    private List<Asset> assets;    // Список прикрепленных файлов


    // Геттеры и сеттеры


    public String getVersion() {
        return version;
    }

    public void setVersion(String tag_name) {
        this.version = tag_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    public static class Asset {
        @SerializedName("browser_download_url")
        private String downloadUrl;

        @SerializedName("name")
        private String name;
        // Геттеры и сеттеры


        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String browser_download_url) {
            this.downloadUrl = browser_download_url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
