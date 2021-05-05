package org.openhab.binding.nuki.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

public class WebApiBridgeDiscoveryDto {
    private List<WebApiBridgeDto> bridges;
    private Integer errorCode;

    @NonNull
    public List<WebApiBridgeDto> getBridges() {
        if (bridges == null) {
            bridges = new ArrayList<>();
        }
        return bridges;
    }

    public void setBridges(List<WebApiBridgeDto> bridges) {
        this.bridges = bridges;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "WebApiBridgeDiscoveryDto{" + "bridges=" + bridges + ", errorCode=" + errorCode + '}';
    }
}
