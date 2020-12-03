/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.MeterRealtimeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusMeterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class FroniusMeterHandler extends FroniusBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusMeterHandler.class);
    private MeterRealtimeResponse meterRealtimeResponse;
    private FroniusBaseDeviceConfiguration config;

    public FroniusMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getDescription() {
        return "Fronius Smart Meter";
    }

    @Override
    public void refresh(FroniusBridgeConfiguration bridgeConfiguration) {
        updateData(bridgeConfiguration, config);
        updateChannels();
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
    protected Object getValue(String channelId) {
        String[] fields = StringUtils.split(channelId, "#");

        String fieldName = fields[0];

        if (meterRealtimeResponse == null) {
            return null;
        }
        switch (fieldName) {
            case FroniusBindingConstants.MeterModel:
                return meterRealtimeResponse.getBody().getData().getDetails().getModel();
            case FroniusBindingConstants.MeterSerial:
                return meterRealtimeResponse.getBody().getData().getDetails().getSerial();
            case FroniusBindingConstants.MeterEnable:
                return meterRealtimeResponse.getBody().getData().getEnable();
            case FroniusBindingConstants.MeterLocation:
                return meterRealtimeResponse.getBody().getData().getMeterLocationCurrent();
            case FroniusBindingConstants.MeterCurrentAcPhase1:
                return meterRealtimeResponse.getBody().getData().getCurrentACPhase1();
            case FroniusBindingConstants.MeterCurrentAcPhase2:
                return meterRealtimeResponse.getBody().getData().getCurrentACPhase2();
            case FroniusBindingConstants.MeterCurrentAcPhase3:
                return meterRealtimeResponse.getBody().getData().getCurrentACPhase3();
            case FroniusBindingConstants.MeterVoltageAcPhase1:
                return meterRealtimeResponse.getBody().getData().getVoltageACPhase1();
            case FroniusBindingConstants.MeterVoltageAcPhase2:
                return meterRealtimeResponse.getBody().getData().getVoltageACPhase2();
            case FroniusBindingConstants.MeterVoltageAcPhase3:
                return meterRealtimeResponse.getBody().getData().getVoltageACPhase3();
            case FroniusBindingConstants.MeterPowerPhase1:
                return meterRealtimeResponse.getBody().getData().getPowerRealPPhase1();
            case FroniusBindingConstants.MeterPowerPhase2:
                return meterRealtimeResponse.getBody().getData().getPowerRealPPhase2();
            case FroniusBindingConstants.MeterPowerPhase3:
                return meterRealtimeResponse.getBody().getData().getPowerRealPPhase3();
            case FroniusBindingConstants.MeterPowerFactorPhase1:
                return meterRealtimeResponse.getBody().getData().getPowerFactorPhase1();
            case FroniusBindingConstants.MeterPowerFactorPhase2:
                return meterRealtimeResponse.getBody().getData().getPowerFactorPhase2();
            case FroniusBindingConstants.MeterPowerFactorPhase3:
                return meterRealtimeResponse.getBody().getData().getPowerFactorPhase3();
            case FroniusBindingConstants.MeterEnergyRealSumConsumed:
                return meterRealtimeResponse.getBody().getData().getEnergyRealWACSumConsumed();
            case FroniusBindingConstants.MeterEnergyRealSumProduced:
                return meterRealtimeResponse.getBody().getData().getEnergyRealWACSumProduced();
        }

        return null;
    }

    /**
     * Get new data
     */
    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config) {
        meterRealtimeResponse = getMeterRealtimeData(bridgeConfiguration.hostname, config.deviceId);
    }

    /**
     * Make the MeterRealtimeData request
     *
     * @param ip address of the device
     * @param deviceId of the device
     * @return {MeterRealtimeResponse} the object representation of the json response
     */
    private MeterRealtimeResponse getMeterRealtimeData(String ip, int deviceId) {
        String location = FroniusBindingConstants.METER_REALTIME_DATA_URL.replace("%IP%", StringUtils.trimToEmpty(ip));
        location = location.replace("%DEVICEID%", Integer.toString(deviceId));
        return collectDataFormUrl(MeterRealtimeResponse.class, location);
    }
}
