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

import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.MeterRealtimeBodyDataDTO;
import org.openhab.binding.fronius.internal.api.MeterRealtimeResponseDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link FroniusMeterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 * @author Thomas Kordelle - Actually constants should be all upper case.
 * @author Hannes Spenger - Added getValue for power sum
 */
public class FroniusMeterHandler extends FroniusBaseThingHandler {

    private MeterRealtimeBodyDataDTO meterRealtimeBodyData;
    private FroniusBaseDeviceConfiguration config;

    public FroniusMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getDescription() {
        return "Fronius Smart Meter";
    }

    @Override
    protected void handleRefresh(FroniusBridgeConfiguration bridgeConfiguration) throws FroniusCommunicationException {
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
        if (meterRealtimeBodyData == null) {
            return null;
        }

        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }
        final String fieldName = fields[0];

        switch (fieldName) {
            case FroniusBindingConstants.METER_ENABLE:
                return new DecimalType(meterRealtimeBodyData.getEnable());
            case FroniusBindingConstants.METER_LOCATION:
                return new DecimalType(meterRealtimeBodyData.getMeterLocationCurrent());
            case FroniusBindingConstants.METER_CURRENT_AC_PHASE_1:
                return new QuantityType<>(meterRealtimeBodyData.getCurrentACPhase1(), Units.AMPERE);
            case FroniusBindingConstants.METER_CURRENT_AC_PHASE_2:
                return new QuantityType<>(meterRealtimeBodyData.getCurrentACPhase2(), Units.AMPERE);
            case FroniusBindingConstants.METER_CURRENT_AC_PHASE_3:
                return new QuantityType<>(meterRealtimeBodyData.getCurrentACPhase3(), Units.AMPERE);
            case FroniusBindingConstants.METER_VOLTAGE_AC_PHASE_1:
                return new QuantityType<>(meterRealtimeBodyData.getVoltageACPhase1(), Units.VOLT);
            case FroniusBindingConstants.METER_VOLTAGE_AC_PHASE_2:
                return new QuantityType<>(meterRealtimeBodyData.getVoltageACPhase2(), Units.VOLT);
            case FroniusBindingConstants.METER_VOLTAGE_AC_PHASE_3:
                return new QuantityType<>(meterRealtimeBodyData.getVoltageACPhase3(), Units.VOLT);
            case FroniusBindingConstants.METER_POWER_PHASE_1:
                return new QuantityType<>(meterRealtimeBodyData.getPowerRealPPhase1(), Units.WATT);
            case FroniusBindingConstants.METER_POWER_PHASE_2:
                return new QuantityType<>(meterRealtimeBodyData.getPowerRealPPhase2(), Units.WATT);
            case FroniusBindingConstants.METER_POWER_PHASE_3:
                return new QuantityType<>(meterRealtimeBodyData.getPowerRealPPhase3(), Units.WATT);
            case FroniusBindingConstants.METER_POWER_SUM:
                return new QuantityType<>(meterRealtimeBodyData.getPowerRealPSum(), Units.WATT);
            case FroniusBindingConstants.METER_POWER_FACTOR_PHASE_1:
                return new DecimalType(meterRealtimeBodyData.getPowerFactorPhase1());
            case FroniusBindingConstants.METER_POWER_FACTOR_PHASE_2:
                return new DecimalType(meterRealtimeBodyData.getPowerFactorPhase2());
            case FroniusBindingConstants.METER_POWER_FACTOR_PHASE_3:
                return new DecimalType(meterRealtimeBodyData.getPowerFactorPhase3());
            case FroniusBindingConstants.METER_ENERGY_REAL_SUM_CONSUMED:
                return new QuantityType<>(meterRealtimeBodyData.getEnergyRealWACSumConsumed(), Units.WATT_HOUR);
            case FroniusBindingConstants.METER_ENERGY_REAL_SUM_PRODUCED:
                return new QuantityType<>(meterRealtimeBodyData.getEnergyRealWACSumProduced(), Units.WATT_HOUR);
            default:
                break;
        }

        return null;
    }

    private void updateProperties() {
        if (meterRealtimeBodyData == null) {
            return;
        }

        Map<String, String> properties = editProperties();

        properties.put(Thing.PROPERTY_MODEL_ID, meterRealtimeBodyData.getDetails().getModel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, meterRealtimeBodyData.getDetails().getSerial());

        updateProperties(properties);
    }

    /**
     * Get new data
     */
    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config)
            throws FroniusCommunicationException {
        MeterRealtimeResponseDTO meterRealtimeResponse = getMeterRealtimeData(bridgeConfiguration.hostname,
                config.deviceId);
        meterRealtimeBodyData = meterRealtimeResponse.getBody().getData();
    }

    /**
     * Make the MeterRealtimeData request
     *
     * @param ip address of the device
     * @param deviceId of the device
     * @return {MeterRealtimeResponse} the object representation of the json response
     */
    private MeterRealtimeResponseDTO getMeterRealtimeData(String ip, int deviceId)
            throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getMeterDataUrl(ip, deviceId);
        return collectDataFromUrl(MeterRealtimeResponseDTO.class, location);
    }
}
