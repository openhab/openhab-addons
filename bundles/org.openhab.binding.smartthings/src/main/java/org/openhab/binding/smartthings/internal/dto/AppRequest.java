package org.openhab.binding.smartthings.internal.dto;

public class AppRequest {

    public String appName;
    public String displayName;
    public String description;
    public String appType;

    public record webhookSmartApp(String targetUrl) {
    }

    public webhookSmartApp webhookSmartApp;
    public String[] classifications;
}
