/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.dto.ohmpilot.OhmpilotRealtimeBodyDTO;
import org.openhab.binding.fronius.internal.api.dto.ohmpilot.OhmpilotRealtimeBodyDataDTO;
import org.openhab.binding.fronius.internal.api.dto.ohmpilot.OhmpilotRealtimeDetailsDTO;
import org.openhab.binding.fronius.internal.api.dto.ohmpilot.OhmpilotRealtimeResponseDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link FroniusOhmpilotHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Hannes Spenger - Initial contribution
 *
 */
public class FroniusOhmpilotHandler extends FroniusBaseThingHandler {

    private @Nullable OhmpilotRealtimeBodyDataDTO ohmpilotRealtimeBodyData;
    private FroniusBaseDeviceConfiguration config;

    public FroniusOhmpilotHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getDescription() {
        return "Fronius Ohmpilot";
    }

    @Override
    public void handleRefresh(FroniusBridgeConfiguration bridgeConfiguration) throws FroniusCommunicationException {
        updateData(bridgeConfiguration, config);
        updateChannels();
        updateProperties();
    }

    @Override
    public void initialize() {
        config = getConfigAs(FroniusBaseDeviceConfiguration.class);
        super.initialize();
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     * @return the last retrieved data
     */
    @Override
    protected State getValue(String channelId) {
        OhmpilotRealtimeBodyDataDTO localOhmpilotRealtimeBodyData = ohmpilotRealtimeBodyData;
        if (localOhmpilotRealtimeBodyData == null) {
            return null;
        }

        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }
        final String fieldName = fields[0];

        switch (fieldName) {
            case FroniusBindingConstants.OHMPILOT_POWER_REAL_SUM:
                return new QuantityType<>(localOhmpilotRealtimeBodyData.getPowerPACSum(), Units.WATT);
            case FroniusBindingConstants.OHMPILOT_ENERGY_REAL_SUM_CONSUMED:
                return new QuantityType<>(localOhmpilotRealtimeBodyData.getEnergyRealWACSumConsumed(), Units.WATT_HOUR);
            case FroniusBindingConstants.OHMPILOT_ENERGY_SENSOR_TEMPERATURE_CHANNEL_1:
                return new QuantityType<>(localOhmpilotRealtimeBodyData.getTemperatureChannel1(), Units.KELVIN);
            case FroniusBindingConstants.OHMPILOT_STATE_CODE:
                return new DecimalType(localOhmpilotRealtimeBodyData.getStateCode());
            case FroniusBindingConstants.OHMPILOT_ERROR_CODE:
                return new DecimalType(localOhmpilotRealtimeBodyData.getErrorCode());

            default:
                break;
        }

        return null;
    }

    private void updateProperties() {
        OhmpilotRealtimeBodyDataDTO localOhmpilotRealtimeBodyData = ohmpilotRealtimeBodyData;
        if (localOhmpilotRealtimeBodyData == null) {
            return;
        }
        OhmpilotRealtimeDetailsDTO details = localOhmpilotRealtimeBodyData.getDetails();
        if (details == null) {
            return;
        }

        Map<String, String> properties = editProperties();

        properties.put(Thing.PROPERTY_MODEL_ID, details.getModel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, details.getSerial());

        updateProperties(properties);
    }

    /**
     * Get new data
     */
    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config)
            throws FroniusCommunicationException {
        OhmpilotRealtimeResponseDTO ohmpilotRealtimeResponse = getOhmpilotRealtimeData(bridgeConfiguration.hostname,
                config.deviceId);
        OhmpilotRealtimeBodyDTO ohmpilotRealtimeBody = ohmpilotRealtimeResponse.getBody();
        if (ohmpilotRealtimeBody == null) {
            ohmpilotRealtimeBodyData = null;
            return;
        }
        ohmpilotRealtimeBodyData = ohmpilotRealtimeBody.getData();
    }

    /**
     * Make the OhmpilotRealtimeData request
     *
     * @param ip address of the device
     * @param deviceId of the device
     * @return {OhmpilotRealtimeResponse} the object representation of the json response
     */
    private OhmpilotRealtimeResponseDTO getOhmpilotRealtimeData(String ip, int deviceId)
            throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getOhmPilotDataUrl(ip, deviceId);
        return collectDataFromUrl(OhmpilotRealtimeResponseDTO.class, location);
    }
}
