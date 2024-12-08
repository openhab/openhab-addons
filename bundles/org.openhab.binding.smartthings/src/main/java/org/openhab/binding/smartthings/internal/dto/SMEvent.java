package org.openhab.binding.smartthings.internal.dto;

public class SMEvent {

    public String sourceType;

    public record device(String deviceId, String componentId, Boolean stateChangeOnly, String[] modes) {
    }

    public device device;
}
