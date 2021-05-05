package org.openhab.binding.nuki.internal.dataexchange;

import java.util.Collections;
import java.util.List;

import org.openhab.binding.nuki.internal.dto.BridgeApiListDeviceDto;

public class BridgeListResponse extends NukiBaseResponse {

    private final List<BridgeApiListDeviceDto> devices;

    public BridgeListResponse(int status, String message, List<BridgeApiListDeviceDto> devices) {
        super(status, message);
        setSuccess(devices != null);
        this.devices = devices == null ? Collections.emptyList() : Collections.unmodifiableList(devices);
    }

    public BridgeListResponse(NukiBaseResponse nukiBaseResponse) {
        this(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage(), null);
    }

    public List<BridgeApiListDeviceDto> getDevices() {
        return devices;
    }

    public BridgeApiListDeviceDto getDevice(String nukiId) {
        for (BridgeApiListDeviceDto device : this.devices) {
            if (device.getNukiId().equals(nukiId)) {
                return device;
            }
        }
        return null;
    }
}
