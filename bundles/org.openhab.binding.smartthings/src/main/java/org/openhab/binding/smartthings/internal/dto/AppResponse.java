package org.openhab.binding.smartthings.internal.dto;

public class AppResponse {

    public class App {
        public String appName;
        public String appId;
        public String appType;
        public String principalType;

        public String[] classifications;

        public String displayName;
        public String description;

        public Boolean singleInstance;

        public record webhookSmartApp(String targetUrl, String targetStatus, String signatureType) {
        }

        public webhookSmartApp webhookSmartApp;
    }

    public App app;
    public String oauthClientId;
    public String oauthClientSecret;
}
