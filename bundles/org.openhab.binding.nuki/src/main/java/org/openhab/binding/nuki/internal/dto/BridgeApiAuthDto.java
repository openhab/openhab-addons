package org.openhab.binding.nuki.internal.dto;

public class BridgeApiAuthDto {
    private String token;
    private boolean success;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
