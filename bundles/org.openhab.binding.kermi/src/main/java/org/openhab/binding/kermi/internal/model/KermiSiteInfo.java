package org.openhab.binding.kermi.internal.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.kermi.internal.KermiBindingConstants;
import org.openhab.binding.kermi.internal.api.DeviceInfo;

import io.micrometer.core.instrument.util.StringUtils;

public class KermiSiteInfo {

    private Map<String, DeviceInfo> deviceIdToDeviceInfo;
    private Map<String, String> addressToDeviceId;

    public KermiSiteInfo() {
        deviceIdToDeviceInfo = Collections.synchronizedMap(new HashMap<String, DeviceInfo>());
        addressToDeviceId = new HashMap<String, String>();
    }

    public DeviceInfo getHeatpumpManagerDeviceInfo() {
        return deviceIdToDeviceInfo.get(KermiBindingConstants.DEVICE_ID_HEATPUMP_MANAGER);
    }

    public DeviceInfo getDeviceInfoByAddress(String address) {
        String deviceId = addressToDeviceId.get(address);
        return deviceId != null ? deviceIdToDeviceInfo.get(deviceId) : null;
    }

    public void updateSiteInfo(Map<String, DeviceInfo> deviceInfo) {
        clearSiteInfo();
        deviceIdToDeviceInfo.putAll(deviceInfo);
        deviceInfo.values().stream().filter(di -> StringUtils.isNotEmpty(di.getAddress()))
                .forEach(di -> addressToDeviceId.put(di.getAddress(), di.getDeviceId()));
    }

    public void clearSiteInfo() {
        deviceIdToDeviceInfo.clear();
        addressToDeviceId.clear();
    }

    public String getDeviceIdByAddress(@NonNull String address) {
        return addressToDeviceId.get(address);
    }

    public void updateDeviceInfo(String deviceId, DeviceInfo deviceInfo) {
        deviceIdToDeviceInfo.put(deviceId, deviceInfo);
    }

}
