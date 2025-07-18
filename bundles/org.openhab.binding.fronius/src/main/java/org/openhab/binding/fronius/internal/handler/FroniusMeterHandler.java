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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.dto.meter.MeterRealtimeBody;
import org.openhab.binding.fronius.internal.api.dto.meter.MeterRealtimeBodyData;
import org.openhab.binding.fronius.internal.api.dto.meter.MeterRealtimeDetails;
import org.openhab.binding.fronius.internal.api.dto.meter.MeterRealtimeResponse;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusMeterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 * @author Thomas Kordelle - Actually constants should be all upper case.
 * @author Hannes Spenger - Added getValue for power sum
 */
@NonNullByDefault
public class FroniusMeterHandler extends FroniusBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusMeterHandler.class);

    private @Nullable MeterRealtimeBodyData meterRealtimeBodyData;
    private @Nullable FroniusBaseDeviceConfiguration config;

    public FroniusMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getDescription() {
        return "Fronius Smart Meter";
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
    protected @Nullable State getValue(String channelId) {
        MeterRealtimeBodyData localMeterRealtimeBodyData = meterRealtimeBodyData;
        if (localMeterRealtimeBodyData == null) {
            return null;
        }

        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }
        final String fieldName = fields[0];

        return switch (fieldName) {
            case FroniusBindingConstants.METER_ENABLE -> new DecimalType(localMeterRealtimeBodyData.getEnable());
            case FroniusBindingConstants.METER_LOCATION ->
                new DecimalType(localMeterRealtimeBodyData.getMeterLocationCurrent());
            case FroniusBindingConstants.METER_CURRENT_AC_PHASE_1 ->
                new QuantityType<>(localMeterRealtimeBodyData.getCurrentACPhase1(), Units.AMPERE);
            case FroniusBindingConstants.METER_CURRENT_AC_PHASE_2 ->
                new QuantityType<>(localMeterRealtimeBodyData.getCurrentACPhase2(), Units.AMPERE);
            case FroniusBindingConstants.METER_CURRENT_AC_PHASE_3 ->
                new QuantityType<>(localMeterRealtimeBodyData.getCurrentACPhase3(), Units.AMPERE);
            case FroniusBindingConstants.METER_VOLTAGE_AC_PHASE_1 ->
                new QuantityType<>(localMeterRealtimeBodyData.getVoltageACPhase1(), Units.VOLT);
            case FroniusBindingConstants.METER_VOLTAGE_AC_PHASE_2 ->
                new QuantityType<>(localMeterRealtimeBodyData.getVoltageACPhase2(), Units.VOLT);
            case FroniusBindingConstants.METER_VOLTAGE_AC_PHASE_3 ->
                new QuantityType<>(localMeterRealtimeBodyData.getVoltageACPhase3(), Units.VOLT);
            case FroniusBindingConstants.METER_POWER_PHASE_1 ->
                new QuantityType<>(localMeterRealtimeBodyData.getPowerRealPPhase1(), Units.WATT);
            case FroniusBindingConstants.METER_POWER_PHASE_2 ->
                new QuantityType<>(localMeterRealtimeBodyData.getPowerRealPPhase2(), Units.WATT);
            case FroniusBindingConstants.METER_POWER_PHASE_3 ->
                new QuantityType<>(localMeterRealtimeBodyData.getPowerRealPPhase3(), Units.WATT);
            case FroniusBindingConstants.METER_POWER_SUM ->
                new QuantityType<>(localMeterRealtimeBodyData.getPowerRealPSum(), Units.WATT);
            case FroniusBindingConstants.METER_POWER_FACTOR_PHASE_1 ->
                new DecimalType(localMeterRealtimeBodyData.getPowerFactorPhase1());
            case FroniusBindingConstants.METER_POWER_FACTOR_PHASE_2 ->
                new DecimalType(localMeterRealtimeBodyData.getPowerFactorPhase2());
            case FroniusBindingConstants.METER_POWER_FACTOR_PHASE_3 ->
                new DecimalType(localMeterRealtimeBodyData.getPowerFactorPhase3());
            case FroniusBindingConstants.METER_ENERGY_REAL_SUM_CONSUMED ->
                new QuantityType<>(localMeterRealtimeBodyData.getEnergyRealWACSumConsumed(), Units.WATT_HOUR);
            case FroniusBindingConstants.METER_ENERGY_REAL_SUM_PRODUCED ->
                new QuantityType<>(localMeterRealtimeBodyData.getEnergyRealWACSumProduced(), Units.WATT_HOUR);
            default -> null;
        };
    }

    private void updateProperties() {
        MeterRealtimeBodyData localMeterRealtimeBodyData = meterRealtimeBodyData;
        if (localMeterRealtimeBodyData == null) {
            return;
        }
        MeterRealtimeDetails details = localMeterRealtimeBodyData.getDetails();
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
        MeterRealtimeResponse meterRealtimeResponse = getMeterRealtimeData(bridgeConfiguration.scheme,
                bridgeConfiguration.hostname, config.deviceId);
        MeterRealtimeBody meterRealtimeBody = meterRealtimeResponse.getBody();
        if (meterRealtimeBody == null) {
            meterRealtimeBodyData = null;
            return;
        }
        meterRealtimeBodyData = meterRealtimeBody.getData();
    }

    /**
     * Make the MeterRealtimeData request
     *
     * @param scheme http or https
     * @param ip address of the device
     * @param deviceId of the device
     * @return {MeterRealtimeResponse} the object representation of the json response
     */
    private MeterRealtimeResponse getMeterRealtimeData(String scheme, String ip, int deviceId)
            throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getMeterDataUrl(scheme, ip, deviceId);
        return collectDataFromUrl(MeterRealtimeResponse.class, location);
    }
}
