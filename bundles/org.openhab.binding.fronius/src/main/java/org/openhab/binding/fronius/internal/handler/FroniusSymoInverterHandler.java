/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.fronius.internal.handler;

import static org.openhab.binding.fronius.internal.FroniusBindingConstants.API_TIMEOUT;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.action.FroniusSymoInverterActions;
import org.openhab.binding.fronius.internal.api.FroniusBatteryControl;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.FroniusHttpUtil;
import org.openhab.binding.fronius.internal.api.dto.ValueUnit;
import org.openhab.binding.fronius.internal.api.dto.inverter.InverterDeviceStatus;
import org.openhab.binding.fronius.internal.api.dto.inverter.InverterRealtimeBody;
import org.openhab.binding.fronius.internal.api.dto.inverter.InverterRealtimeBodyData;
import org.openhab.binding.fronius.internal.api.dto.inverter.InverterRealtimeResponse;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeBody;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeBodyData;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeInverter;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeResponse;
import org.openhab.binding.fronius.internal.api.dto.powerflow.PowerFlowRealtimeSite;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * The {@link FroniusSymoInverterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Peter Schraffl - Added device status and error status channels
 * @author Thomas Kordelle - Added inverter power, battery state of charge and PV solar yield
 * @author Jimmy Tanagra - Add powerflow autonomy, self consumption channels
 * @author Florian Hotze - Add battery control actions
 */
@NonNullByDefault
public class FroniusSymoInverterHandler extends FroniusBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusSymoInverterHandler.class);
    private final HttpClient httpClient;

    private @Nullable InverterRealtimeResponse inverterRealtimeResponse;
    private @Nullable PowerFlowRealtimeResponse powerFlowResponse;
    private @Nullable FroniusBaseDeviceConfiguration config;
    private @Nullable InverterInfo inverterInfo;
    private @Nullable FroniusBatteryControl batteryControl;

    public FroniusSymoInverterHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    protected String getDescription() {
        return "Fronius Symo Inverter";
    }

    @Override
    protected void handleRefresh(FroniusBridgeConfiguration bridgeConfiguration) throws FroniusCommunicationException {
        FroniusBaseDeviceConfiguration config = this.config;
        if (config == null) {
            logger.warn("config is null in handleRefresh(), this is a bug, please report it.");
            return;
        }
        updateData(bridgeConfiguration, config);
        updateChannels();
    }

    private void initializeBatteryControl(String hostname, @Nullable String username, @Nullable String password) {
        if (username == null || password == null) {
            return;
        }

        String apiPrefix = "";

        InverterInfo localInverterInfo = inverterInfo;
        if (localInverterInfo != null) {
            String firmwareVersion = localInverterInfo.firmware();
            int lastDotIndex = firmwareVersion.lastIndexOf('.');
            float version = Float.parseFloat(firmwareVersion.substring(0, lastDotIndex));
            if (version >= 1.36) {
                apiPrefix = "/api";
            } else {
                logger.warn("Fronius Symo Inverter firmware version {} is not supported for battery control.",
                        firmwareVersion);
                return;
            }
        }

        batteryControl = new FroniusBatteryControl(httpClient, URI.create("http://" + hostname + apiPrefix), username,
                password);
    }

    private void updateProperties() {
        InverterInfo localInverterInfo = inverterInfo;
        if (localInverterInfo == null) {
            return;
        }

        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, localInverterInfo.serial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, localInverterInfo.firmware());
        updateProperties(properties);
    }

    @Override
    public void initialize() {
        config = getConfigAs(FroniusBaseDeviceConfiguration.class);
        Bridge bridge = getBridge();
        if (bridge != null) {
            FroniusBridgeConfiguration bridgeConfig = bridge.getConfiguration().as(FroniusBridgeConfiguration.class);
            inverterInfo = getInverterInfo(bridgeConfig.hostname);
            updateProperties();
            initializeBatteryControl(bridgeConfig.hostname, bridgeConfig.username, bridgeConfig.password);
        }
        super.initialize();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(FroniusSymoInverterActions.class);
    }

    @Override
    public void handleBridgeConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleBridgeConfigurationUpdate(configurationParameters);
        Bridge bridge = getBridge();
        if (bridge != null) {
            FroniusBridgeConfiguration bridgeConfig = bridge.getConfiguration().as(FroniusBridgeConfiguration.class);
            inverterInfo = getInverterInfo(bridgeConfig.hostname);
            updateProperties();
            initializeBatteryControl(bridgeConfig.hostname, bridgeConfig.username, bridgeConfig.password);
        }
    }

    public @Nullable FroniusBatteryControl getBatteryControl() {
        if (batteryControl == null) {
            logger.warn("Battery control is not available. Check the bridge configuration.");
        }
        return batteryControl;
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     * @return the last retrieved data
     */
    @Override
    protected @Nullable State getValue(String channelId) {
        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }
        final String fieldName = fields[0];

        InverterRealtimeBodyData inverterData = getInverterData();
        if (inverterData == null) {
            return null;
        }

        FroniusBaseDeviceConfiguration config = this.config;
        if (config == null) {
            logger.warn("config is null in getValue(String channelId), this is a bug, please report it.");
            return null;
        }

        InverterDeviceStatus deviceStatus;
        switch (fieldName) {
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PAC:
                return getQuantityOrZero(inverterData.getPac(), Units.WATT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_FAC:
                return getQuantityOrZero(inverterData.getFac(), Units.HERTZ);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IAC:
                return getQuantityOrZero(inverterData.getIac(), Units.AMPERE);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC:
                return getQuantityOrZero(inverterData.getIdc(), Units.AMPERE);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC2:
                return getQuantityOrZero(inverterData.getIdc2(), Units.AMPERE);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC3:
                return getQuantityOrZero(inverterData.getIdc3(), Units.AMPERE);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UAC:
                return getQuantityOrZero(inverterData.getUac(), Units.VOLT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC:
                return getQuantityOrZero(inverterData.getUdc(), Units.VOLT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC2:
                return getQuantityOrZero(inverterData.getUdc2(), Units.VOLT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC3:
                return getQuantityOrZero(inverterData.getUdc3(), Units.VOLT);
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC:
                return calculatePower(inverterData.getUdc(), inverterData.getIdc());
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC2:
                return calculatePower(inverterData.getUdc2(), inverterData.getIdc2());
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC3:
                return calculatePower(inverterData.getUdc3(), inverterData.getIdc3());
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DAY_ENERGY:
                // Convert the unit to kWh for backwards compatibility with non-quantity type
                return getQuantityOrZero(inverterData.getDayEnergy(), Units.KILOWATT_HOUR).toUnit("kWh");
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_TOTAL:
                // Convert the unit to MWh for backwards compatibility with non-quantity type
                return getQuantityOrZero(inverterData.getTotalEnergy(), Units.MEGAWATT_HOUR).toUnit("MWh");
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_YEAR:
                // Convert the unit to MWh for backwards compatibility with non-quantity type
                return getQuantityOrZero(inverterData.getYearEnergy(), Units.MEGAWATT_HOUR).toUnit("MWh");
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DEVICE_STATUS_ERROR_CODE:
                deviceStatus = inverterData.getDeviceStatus();
                if (deviceStatus == null) {
                    return null;
                }
                return new DecimalType(deviceStatus.getErrorCode());
            case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DEVICE_STATUS_STATUS_CODE:
                deviceStatus = inverterData.getDeviceStatus();
                if (deviceStatus == null) {
                    return null;
                }
                return new DecimalType(deviceStatus.getStatusCode());
            default:
                break;
        }

        PowerFlowRealtimeBodyData powerFlowData = getPowerFlowRealtimeData();
        if (powerFlowData == null) {
            return null;
        }
        PowerFlowRealtimeSite site = powerFlowData.getSite();
        if (site == null) {
            return null;
        }

        return switch (fieldName) {
            case FroniusBindingConstants.POWER_FLOW_P_GRID -> new QuantityType<>(site.getPgrid(), Units.WATT);
            case FroniusBindingConstants.POWER_FLOW_P_LOAD -> new QuantityType<>(site.getPload(), Units.WATT);
            case FroniusBindingConstants.POWER_FLOW_P_AKKU -> new QuantityType<>(site.getPakku(), Units.WATT);
            case FroniusBindingConstants.POWER_FLOW_P_PV -> new QuantityType<>(site.getPpv(), Units.WATT);
            case FroniusBindingConstants.POWER_FLOW_AUTONOMY ->
                new QuantityType<>(site.getRelAutonomy(), Units.PERCENT);
            case FroniusBindingConstants.POWER_FLOW_SELF_CONSUMPTION ->
                new QuantityType<>(site.getRelSelfConsumption(), Units.PERCENT);
            case FroniusBindingConstants.POWER_FLOW_INVERTER_POWER -> {
                PowerFlowRealtimeInverter inverter = getInverter(config.deviceId);
                if (inverter == null) {
                    yield null;
                }
                yield new QuantityType<>(inverter.getP(), Units.WATT);
            }
            case FroniusBindingConstants.POWER_FLOW_INVERTER_SOC -> {
                PowerFlowRealtimeInverter inverter = getInverter(config.deviceId);
                if (inverter == null) {
                    yield null;
                }
                yield new QuantityType<>(inverter.getSoc(), Units.PERCENT);
            }
            // Kept for backwards compatibility
            case FroniusBindingConstants.POWER_FLOW_INVERTER_1_POWER -> {
                PowerFlowRealtimeInverter inverter = getInverter(1);
                if (inverter == null) {
                    yield null;
                }
                yield new QuantityType<>(inverter.getP(), Units.WATT);
            }
            case FroniusBindingConstants.POWER_FLOW_INVERTER_1_SOC -> {
                PowerFlowRealtimeInverter inverter = getInverter(1);
                if (inverter == null) {
                    yield null;
                }
                yield new QuantityType<>(inverter.getSoc(), Units.PERCENT);
            }

            default -> null;
        };
    }

    private @Nullable InverterRealtimeBodyData getInverterData() {
        InverterRealtimeResponse localInverterRealtimeResponse = inverterRealtimeResponse;
        if (localInverterRealtimeResponse == null) {
            return null;
        }
        InverterRealtimeBody inverterBody = localInverterRealtimeResponse.getBody();
        return (inverterBody != null) ? inverterBody.getData() : null;
    }

    private @Nullable PowerFlowRealtimeBodyData getPowerFlowRealtimeData() {
        PowerFlowRealtimeResponse localPowerFlowResponse = powerFlowResponse;
        if (localPowerFlowResponse == null) {
            return null;
        }
        PowerFlowRealtimeBody powerFlowBody = localPowerFlowResponse.getBody();
        return (powerFlowBody != null) ? powerFlowBody.getData() : null;
    }

    /**
     * get flow data for a specific inverter.
     *
     * @param number The inverter object of the given index
     * @return a PowerFlowRealtimeInverter object.
     */
    private @Nullable PowerFlowRealtimeInverter getInverter(final int number) {
        PowerFlowRealtimeBodyData powerFlowData = getPowerFlowRealtimeData();
        if (powerFlowData == null) {
            return null;
        }
        return powerFlowData.getInverters().get(Integer.toString(number));
    }

    /**
     * Return the value as QuantityType with the unit extracted from ValueUnit
     * or a zero QuantityType with the given unit argument when value is null
     * 
     * @param value The ValueUnit data
     * @param unit The default unit to use when value is null
     * @return a QuantityType from the given value
     */
    private QuantityType<?> getQuantityOrZero(@Nullable ValueUnit value, Unit unit) {
        QuantityType<?> val = null;
        if (value != null) {
            val = value.asQuantityType().toUnit(unit);
        }
        if (val == null) {
            return new QuantityType<>(0, unit);
        }
        return val;
    }

    /**
     * Get new data
     */
    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config)
            throws FroniusCommunicationException {
        inverterRealtimeResponse = getRealtimeData(bridgeConfiguration.hostname, config.deviceId);
        powerFlowResponse = getPowerFlowRealtime(bridgeConfiguration.hostname);
    }

    /**
     * Make the PowerFlowRealtimeDataRequest
     *
     * @param ip address of the device
     * @return {PowerFlowRealtimeResponse} the object representation of the json response
     */
    private PowerFlowRealtimeResponse getPowerFlowRealtime(String ip) throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getPowerFlowDataUrl(ip);
        return collectDataFromUrl(PowerFlowRealtimeResponse.class, location);
    }

    /**
     * Make the InverterRealtimeDataRequest
     *
     * @param ip address of the device
     * @param deviceId of the device
     * @return {InverterRealtimeResponse} the object representation of the json response
     */
    private InverterRealtimeResponse getRealtimeData(String ip, int deviceId) throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getInverterDataUrl(ip, deviceId);
        return collectDataFromUrl(InverterRealtimeResponse.class, location);
    }

    /**
     * Calculate the power value from the given voltage and current channels
     * 
     * @param voltage the voltage ValueUnit
     * @param current the current ValueUnit
     * @return {QuantityType<>} the power value calculated by multiplying voltage and current
     */
    private @Nullable QuantityType<?> calculatePower(ValueUnit voltage, ValueUnit current) {
        QuantityType<?> qtyVoltage = getQuantityOrZero(voltage, Units.VOLT);
        QuantityType<?> qtyCurrent = getQuantityOrZero(current, Units.AMPERE);
        return qtyVoltage.multiply(qtyCurrent).toUnit(Units.WATT);
    }

    /**
     * Get the version information of the inverter
     *
     * @param hostname the IP address of the inverter
     * @return InverterInfo containing serial number and firmware version, or null if not available
     */
    private @Nullable InverterInfo getInverterInfo(String hostname) {
        final String host = "http://" + hostname;
        final String versionPath = "/status/version";

        String url = host + "/api" + versionPath; // try the new API path first
        String response;
        try {
            response = FroniusHttpUtil.executeUrl(HttpMethod.GET, url, API_TIMEOUT);
        } catch (FroniusCommunicationException e) {
            url = host + versionPath; // fallback to the old API path
            try {
                response = FroniusHttpUtil.executeUrl(HttpMethod.GET, url, API_TIMEOUT);
            } catch (FroniusCommunicationException ex) {
                logger.warn("Failed to get version info from Fronius inverter at {}: {}", hostname, ex.getMessage());
                return null;
            }
        }
        JsonElement jsonElement = JsonParser.parseString(response);
        if (!jsonElement.isJsonObject()) {
            logger.warn("Invalid JSON response for version info from Fronius inverter at {}: {}", hostname, response);
            return null;
        }
        try {
            String serial = jsonElement.getAsJsonObject().get("serialNumber").getAsString();
            String firmware = jsonElement.getAsJsonObject().get("swrevisions").getAsJsonObject().get("GEN24")
                    .getAsString();
            return new InverterInfo(serial, firmware);
        } catch (IllegalStateException | UnsupportedOperationException e) {
            logger.warn("Failed to parse version info from Fronius inverter at {}: {}", hostname, e.getMessage());
            return null;
        }
    }

    private record InverterInfo(String serial, String firmware) {
    }
}
