package org.openhab.binding.kermi.internal.handler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.kermi.internal.KermiBridgeConfiguration;
import org.openhab.binding.kermi.internal.KermiCommunicationException;
import org.openhab.binding.kermi.internal.api.DeviceInfo;
import org.openhab.binding.kermi.internal.api.GetDevicesResponse;
import org.openhab.binding.kermi.internal.api.KermiHttpUtil;
import org.openhab.binding.kermi.internal.model.KermiSiteInfo;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

public class KermiHeatpumpManagerThingHandler extends KermiBaseThingHandler {

    public KermiHeatpumpManagerThingHandler(Thing thing, @NonNull KermiHttpUtil httpUtil, KermiSiteInfo kermiSiteInfo) {
        super(thing, httpUtil, kermiSiteInfo);
    }

    @Override
    protected String getDescription() {
        return "HeatpumpManager";
        // DeviceInfo deviceInfo = getKermiSiteInfo().getHeatpumpManagerDeviceInfo();
        // return deviceInfo != null ? deviceInfo.getName() : "Unknown";
    }

    @Override
    protected State getValue(String channelId) {
        return null;
    }

    @Override
    protected void handleRefresh(KermiBridgeConfiguration bridgeConfiguration) throws KermiCommunicationException {
        if (getKermiSiteInfo().getHeatpumpManagerDeviceInfo() == null) {
            // no need to do this regularly
            updateData();
            updateChannels();
            updateProperties();
        }
    }

    private void updateData() throws KermiCommunicationException {
        GetDevicesResponse getDevicesResponse = getHttpUtil().getDevicesByFilter();
        List<DeviceInfo> deviceInfo = getDevicesResponse.getResponseData();
        Map<String, DeviceInfo> _deviceInfo = deviceInfo.stream()
                .collect(Collectors.toMap(DeviceInfo::getDeviceId, Function.identity()));
        getKermiSiteInfo().updateSiteInfo(_deviceInfo);
    }

    private void updateProperties() {
        DeviceInfo deviceInfo = getKermiSiteInfo().getHeatpumpManagerDeviceInfo();
        if (deviceInfo == null) {
            return;
        }

        Map<String, String> properties = editProperties();

        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.getSoftwareVersion());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceInfo.getSerial());

        updateProperties(properties);
    }

}
