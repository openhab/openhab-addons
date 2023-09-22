package org.openhab.binding.kermi.internal.handler;

import java.util.Map;
import java.util.Optional;

import org.openhab.binding.kermi.internal.KermiBaseDeviceConfiguration;
import org.openhab.binding.kermi.internal.KermiBindingConstants;
import org.openhab.binding.kermi.internal.KermiBridgeConfiguration;
import org.openhab.binding.kermi.internal.KermiCommunicationException;
import org.openhab.binding.kermi.internal.api.DatapointValue;
import org.openhab.binding.kermi.internal.api.DeviceInfo;
import org.openhab.binding.kermi.internal.api.GetDeviceResponse;
import org.openhab.binding.kermi.internal.api.KermiHttpUtil;
import org.openhab.binding.kermi.internal.model.KermiSiteInfo;
import org.openhab.binding.kermi.internal.model.KermiSiteInfoUtil;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

import tech.units.indriya.unit.Units;

public class KermiDrinkingWaterHeatingThingHandler extends KermiBaseThingHandler {

    private KermiBaseDeviceConfiguration config;

    public KermiDrinkingWaterHeatingThingHandler(Thing thing, KermiHttpUtil httpUtil, KermiSiteInfo kermiSiteInfo) {
        super(thing, httpUtil, kermiSiteInfo);
    }

    @Override
    protected String getDescription() {
        return "DrinkingWaterHeating";
        // DeviceInfo deviceInfo = getKermiSiteInfo().getDeviceInfoByAddress(config.address.toString());
        // return deviceInfo != null ? deviceInfo.getName() : "No device on address " + config.address;
    }

    @Override
    protected State getValue(String channelId) {
        DeviceInfo deviceInfo = getKermiSiteInfo().getDeviceInfoByAddress(config.address.toString());
        if (deviceInfo == null) {
            return null;
        }

        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }

        final String fieldName = fields[0];
        Optional<DatapointValue> dpv = KermiSiteInfoUtil.getVisualizationDatapointValueByWellKnownName(fieldName,
                deviceInfo);
        if (!dpv.isPresent()) {
            return null;
        }
        Object value = dpv.get().getValue();
        State result = null;
        switch (fieldName) {
            case KermiBindingConstants.WELL_KNOWN_NAME_BS_TWE_TEMP_ACT: {
                result = new QuantityType<>((float) value, Units.CELSIUS);
            }
                break;
            default:
                break;
        }

        return result;
    }

    @Override
    protected void handleRefresh(KermiBridgeConfiguration bridgeConfiguration) throws KermiCommunicationException {
        updateData();
        updateChannels();
        updateProperties();
    }

    private void updateProperties() {
        DeviceInfo deviceInfo = getKermiSiteInfo().getDeviceInfoByAddress(config.address.toString());
        if (deviceInfo == null) {
            return;
        }

        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceInfo.getSerial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.getSoftwareVersion());
        updateProperties(properties);
    }

    private void updateData() throws KermiCommunicationException {
        String deviceId = getKermiSiteInfo().getDeviceIdByAddress(config.address.toString());
        GetDeviceResponse deviceResponse = getHttpUtil().getDeviceInfoByDeviceId(deviceId);
        getKermiSiteInfo().updateDeviceInfo(deviceId, deviceResponse.getResponseData());
    }

    @Override
    public void initialize() {
        config = getConfigAs(KermiBaseDeviceConfiguration.class);
        super.initialize();
    }

}
