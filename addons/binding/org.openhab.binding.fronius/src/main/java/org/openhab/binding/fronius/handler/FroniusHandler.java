/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.fronius.FroniusBindingConstants;
import org.openhab.binding.fronius.FroniusConfiguration;
import org.openhab.binding.fronius.api.InverterRealtimeResponse;
import org.openhab.binding.fronius.api.PowerFlowRealtimeResponse;
import org.openhab.binding.fronius.api.ValueUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FroniusHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class FroniusHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusHandler.class);

    private static final String INVERTER_REALTIME_DATA_URL = "http://%IP%/solar_api/v1/GetInverterRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=CommonInverterData";
    private static final String POWERFLOW_REALTIME_DATA = "http://%IP%/solar_api/v1/GetPowerFlowRealtimeData.fcgi";
    private static final int API_TIMEOUT = 5000;
    private static final int DEFAULT_REFRESH_PERIOD = 5;

    private ScheduledFuture<?> refreshJob;

    private InverterRealtimeResponse inverterRealtimeResponse;
    private PowerFlowRealtimeResponse powerFlowResponse;
    private boolean connectionFailed = false;
    private boolean connectionFailedPowerFlow = false;

    private Gson gson;

    public FroniusHandler(Thing thing) {
        super(thing);
        gson = new Gson();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        } else {
            logger.debug("The Air Quality binding is read-only and can not handle command {}", command);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Fronius handler.");

        FroniusConfiguration config = getConfigAs(FroniusConfiguration.class);

        boolean validConfig = true;
        String errorMsg = null;
        if (StringUtils.trimToNull(config.ip) == null) {
            errorMsg = "Parameter 'Ip' is mandatory and must be configured";
            validConfig = false;
        }
        if (config.refresh != null && config.refresh <= 0) {
            errorMsg = "Parameter 'refresh' must be at least 1 second";
            validConfig = false;
        }

        if (validConfig) {
            startAutomaticRefresh(config);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job refreshing the data
     */
    private void startAutomaticRefresh(FroniusConfiguration config) {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // Request
                    updateData(config);
                    // Update all channels
                    for (Channel channel : getThing().getChannels()) {
                        updateChannel(channel.getUID().getId());
                    }

                }
            };

            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Fronius handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    /**
     * Update the channel from the last data
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId) {
        if (!isLinked(channelId)) {
            return;
        }
        Object value;
        value = getValue(channelId);

        State state = null;
        if (value == null) {
            state = UnDefType.UNDEF;
        } else if (value instanceof Calendar) {
            state = new DateTimeType((Calendar) value);
        } else if (value instanceof BigDecimal) {
            state = new DecimalType((BigDecimal) value);
        } else if (value instanceof Integer) {
            state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
        } else if (value instanceof String) {
            state = new StringType(value.toString());
        } else if (value instanceof Double) {
            state = new DecimalType((double) value);
        } else if (value instanceof ValueUnit) {
            state = new DecimalType(((ValueUnit) value).getValue());
        } else {
            logger.warn("Update channel {}: Unsupported value type {}", channelId, value.getClass().getSimpleName());
        }
        logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                (value == null) ? "null" : value.getClass().getSimpleName());

        // Update the channel
        if (state != null) {
            updateState(channelId, state);
        }

    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     * @param data
     * @return
     */
    public Object getValue(String channelId) {
        String[] fields = StringUtils.split(channelId, "#");

        if (inverterRealtimeResponse == null) {
            return null;
        }

        String fieldName = fields[0];

        switch (fieldName) {
            case FroniusBindingConstants.InverterDataChannelDayEnergy:
                ValueUnit day = inverterRealtimeResponse.getBody().getData().getDayEnergy();
                day.setUnit("kWh");
                return day;
            case FroniusBindingConstants.InverterDataChannelPac:
                return inverterRealtimeResponse.getBody().getData().getPac();
            case FroniusBindingConstants.InverterDataChannelTotal:
                ValueUnit total = inverterRealtimeResponse.getBody().getData().getTotalEnergy();
                total.setUnit("MWh");
                return total;
            case FroniusBindingConstants.InverterDataChannelYear:
                ValueUnit year = inverterRealtimeResponse.getBody().getData().getYearEnergy();
                year.setUnit("MWh");
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
     *
     */
    private void updateData(FroniusConfiguration config) {
        inverterRealtimeResponse = getRealtimeData(config.ip, config.deviceId);
        powerFlowResponse = getPowerFlowRealtime(config.ip);
    }

    /**
     * Make the PowerFlowRealtimeDataRequest
     *
     * @param ip
     * @return {PowerFlowRealtimeResponse}
     */
    private PowerFlowRealtimeResponse getPowerFlowRealtime(String ip) {
        PowerFlowRealtimeResponse result = null;
        boolean resultOk = false;
        String errorMsg = null;

        try {

            String location = POWERFLOW_REALTIME_DATA.replace("%IP%", StringUtils.trimToEmpty(ip));
            logger.debug("URL = {}", location);
            String response = HttpUtil.executeUrl("GET", location, API_TIMEOUT);
            if (response != null) {
                logger.debug("aqiResponse = {}", response);
                result = gson.fromJson(response, PowerFlowRealtimeResponse.class);
            }

            if (result == null) {
                errorMsg = "no data returned";
            } else if (result.getBody() != null) {
                resultOk = true;
            } else {
                errorMsg = "missing data sub-object";
            }

            if (!resultOk) {
                logger.error("Error in fronius response: {}", errorMsg);
            }
            connectionFailedPowerFlow = false;
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.error("Error running fronius request: {}", errorMsg);
        } catch (IOException | IllegalStateException e) {
            if (!connectionFailedPowerFlow) {
                logger.error("Error running fronius request: {}", e.getMessage());
                connectionFailedPowerFlow = true;
            } else {
                logger.debug("Error running fronius request: {}", e.getMessage());
            }
        }

        return resultOk ? result : null;
    }

    /**
     * Make the InverterRealtimeDataRequest
     *
     * @param ip
     * @return {InverterRealtimeResponse}
     */
    private InverterRealtimeResponse getRealtimeData(String ip, int deviceId) {
        InverterRealtimeResponse result = null;
        boolean resultOk = false;
        String errorMsg = null;

        try {

            String location = INVERTER_REALTIME_DATA_URL.replace("%IP%", StringUtils.trimToEmpty(ip));
            location = location.replace("%DEVICEID%", Integer.toString(deviceId));
            logger.debug("URL = {}", location);

            String response = HttpUtil.executeUrl("GET", location, API_TIMEOUT);
            if (response != null) {
                logger.debug("aqiResponse = {}", response);
                result = gson.fromJson(response, InverterRealtimeResponse.class);
            }

            if (result == null) {
                errorMsg = "no data returned";
            } else if (result.getBody() != null) {
                resultOk = true;
            } else {
                errorMsg = "missing data sub-object";
            }

            if (!resultOk) {
                logger.error("Error in fronius response: {}", errorMsg);
            }
            connectionFailed = false;

        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.error("Error running fronius request: {}", e.getMessage());
        } catch (IOException | IllegalStateException e) {
            errorMsg = "Connection failed";
            if (!connectionFailed) {
                logger.error("Error running fronius request: {}", e.getMessage());
                connectionFailed = true;
            } else {
                logger.debug("Error running fronius request: {}", e.getMessage());
            }
        }

        // Update the thing status
        if (resultOk) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        }

        return resultOk ? result : null;
    }

}
