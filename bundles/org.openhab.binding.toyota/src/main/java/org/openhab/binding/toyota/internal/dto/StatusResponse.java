package org.openhab.binding.toyota.internal.dto;

public class StatusResponse {
    public Event event;
    public String tripStatus;
    public ProtectionState protectionState;
    public Climate climate;
}
