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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.BaseFroniusResponse;
import org.openhab.binding.fronius.internal.api.InverterRealtimeResponse;
import org.openhab.binding.fronius.internal.api.PowerFlowRealtimeResponse;
import org.openhab.binding.fronius.internal.api.ValueUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FroniusSymoInverterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Peter Schraffl - Added device status and error status channels
 */
public class FroniusSymoInverterHandler extends FroniusBaseThingHandler {

    private static final int API_TIMEOUT = 5000;
    private final Logger logger = LoggerFactory.getLogger(FroniusSymoInverterHandler.class);
    private InverterRealtimeResponse inverterRealtimeResponse;
    private PowerFlowRealtimeResponse powerFlowResponse;
    private FroniusBaseDeviceConfiguration config;
    private final Gson gson;

    public FroniusSymoInverterHandler(Thing thing) {
        super(thing);
        gson = new Gson();
    }

    @Override
    protected String getDescription() {
        return "Fronius Symo Inverter";
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

        if (inverterRealtimeResponse == null) {
            return null;
        }
        switch (fieldName) {
            case FroniusBindingConstants.InverterDataChannelDayEnergy:
                ValueUnit day = inverterRealtimeResponse.getBody().getData().getDayEnergy();
                if (day != null) {
                    day.setUnit("kWh");
                }
                return day;
            case FroniusBindingConstants.InverterDataChannelPac:
                return inverterRealtimeResponse.getBody().getData().getPac();
            case FroniusBindingConstants.InverterDataChannelTotal:
                ValueUnit total = inverterRealtimeResponse.getBody().getData().getTotalEnergy();
                if (total != null) {
                    total.setUnit("MWh");
                }
                return total;
            case FroniusBindingConstants.InverterDataChannelYear:
                ValueUnit year = inverterRealtimeResponse.getBody().getData().getYearEnergy();
                if (year != null) {
                    year.setUnit("MWh");
                }
                return year;
            case FroniusBindingConstants.InverterDataChannelFac:
                return inverterRealtimeResponse.getBody().getData().getFac();
            case FroniusBindingConstants.InverterDataChannelIac:
                return inverterRealtimeResponse.getBody().getData().getIac();
            case FroniusBindingConstants.InverterDataChannelIdc:
                return inverterRealtimeResponse.getBody().getData().getIdc();
            case FroniusBindingConstants.InverterDataChannelUac:
                return inverterRealtimeResponse.getBody().getData().getUac();
            case FroniusBindingConstants.InverterDataChannelUdc:
                return inverterRealtimeResponse.getBody().getData().getUdc();
            case FroniusBindingConstants.InverterDataChannelDeviceStatusErrorCode:
                return inverterRealtimeResponse.getBody().getData().getDeviceStatus().getErrorCode();
            case FroniusBindingConstants.InverterDataChannelDeviceStatusStatusCode:
                return inverterRealtimeResponse.getBody().getData().getDeviceStatus().getStatusCode();
        }
        if (powerFlowResponse == null) {
            return null;
        }
        switch (fieldName) {
            case FroniusBindingConstants.PowerFlowpGrid:
                return powerFlowResponse.getBody().getData().getSite().getPgrid();
            case FroniusBindingConstants.PowerFlowpLoad:
                return powerFlowResponse.getBody().getData().getSite().getPload();
            case FroniusBindingConstants.PowerFlowpAkku:
                return powerFlowResponse.getBody().getData().getSite().getPakku();
        }

        return null;
    }

    /**
     * Get new data
     */
    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config) {
        inverterRealtimeResponse = getRealtimeData(bridgeConfiguration.hostname, config.deviceId);
        powerFlowResponse = getPowerFlowRealtime(bridgeConfiguration.hostname);
    }

    /**
     *
     * @param type response class type
     * @param url to request
     * @return the object representation of the json response
     */
    private <T extends BaseFroniusResponse> T collectDataFormUrl(Class<T> type, String url) {
        T result = null;
        boolean resultOk = false;
        String errorMsg = null;

        try {
            logger.debug("URL = {}", url);
            String response = HttpUtil.executeUrl("GET", url, API_TIMEOUT);

            if (response != null) {
                logger.debug("aqiResponse = {}", response);
                result = gson.fromJson(response, type);
            }

            if (result == null) {
                errorMsg = "no data returned";
            } else {
                if (result.getHead().getStatus().getCode() == 0) {
                    resultOk = true;
                } else {
                    errorMsg = result.getHead().getStatus().getReason();
                }
            }
            if (!resultOk) {
                logger.debug("Error in fronius response: {}", errorMsg);
            }
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.debug("Error running fronius request: {}", errorMsg);
        } catch (IOException | IllegalStateException e) {
            logger.debug("Error running fronius request: {}", e.getMessage());
        }

        // Update the thing status
        if (resultOk) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        }
        return resultOk ? result : null;
    }

    /**
     * Make the PowerFlowRealtimeDataRequest
     *
     * @param ip address of the device
     * @return {PowerFlowRealtimeResponse} the object representation of the json response
     */
    private PowerFlowRealtimeResponse getPowerFlowRealtime(String ip) {
        String location = FroniusBindingConstants.POWERFLOW_REALTIME_DATA.replace("%IP%", StringUtils.trimToEmpty(ip));
        return collectDataFormUrl(PowerFlowRealtimeResponse.class, location);
    }

    /**
     * Make the InverterRealtimeDataRequest
     *
     * @param ip address of the device
     * @param deviceId of the device
     * @return {InverterRealtimeResponse} the object representation of the json response
     */
    private InverterRealtimeResponse getRealtimeData(String ip, int deviceId) {
        String location = FroniusBindingConstants.INVERTER_REALTIME_DATA_URL.replace("%IP%",
                StringUtils.trimToEmpty(ip));
        location = location.replace("%DEVICEID%", Integer.toString(deviceId));
        return collectDataFormUrl(InverterRealtimeResponse.class, location);
    }
}
