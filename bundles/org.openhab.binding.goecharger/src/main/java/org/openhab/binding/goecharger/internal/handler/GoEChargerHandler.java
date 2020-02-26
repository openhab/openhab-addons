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
package org.openhab.binding.goecharger.internal.handler;

import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.DEFAULT_REFRESH_INTERVAL;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.MAX_AMPERE;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.PWM_SIGNAL;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.ERROR;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.ALLOW_CHARGING;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.STOP_STATE;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.CABLE_ENCODING;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.PHASES;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.TEMPERATURE;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.SESSION_CHARGE_CONSUMPTION;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.SESSION_CHARGE_CONSUMPTION_LIMIT;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.TOTAL_CONSUMPTION;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.FIRMWARE;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.VOLTAGE_L1;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.VOLTAGE_L2;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.VOLTAGE_L3;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.CURRENT_L1;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.CURRENT_L2;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.CURRENT_L3;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.POWER_L1;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.POWER_L2;
import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.POWER_L3;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.binding.goecharger.internal.GoEChargerBindingConstants;
import org.openhab.binding.goecharger.internal.GoEChargerConfiguration;
import org.openhab.binding.goecharger.internal.api.GoEStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * The {@link GoEChargerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Samuel Brucksch - Initial contribution
 */
public class GoEChargerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GoEChargerHandler.class);

    private @Nullable GoEChargerConfiguration config;

    private Gson gson;
    private GoEStatusResponse goeResponse;

    private ScheduledFuture<?> refreshJob;
    private int retryCounter = 0;

    public GoEChargerHandler(Thing thing) {
        super(thing);
        gson = new Gson();
    }

    public Object getValue(String channelId) {
        String[] fields = StringUtils.split(channelId, "#");

        if (goeResponse != null) {
            switch (fields[0]) {
            case MAX_AMPERE:
                return goeResponse.getMaxChargeAmps();
            case PWM_SIGNAL:
                return goeResponse.getPwmSignal();
            case ERROR:
                int error = goeResponse.getErrorCode();
                switch (error) {
                case 0:
                    return "NONE"; // TODO evaluate
                case 1:
                    return "RCCB";
                case 3:
                    return "PHASE";
                case 8:
                    return "NO_GROUND";
                case 10:
                    return "INTERNAL";
                default:
                    return "NONE"; // TODO evaluate
                }
            case ALLOW_CHARGING:
                return goeResponse.getAllowCharging() == 1;
            case STOP_STATE:
                return goeResponse.getAutomaticStop() == 2;
            case CABLE_ENCODING:
                return goeResponse.getCableEncoding();
            case PHASES:
                return goeResponse.getPhases();
            case TEMPERATURE:
                return goeResponse.getTemperature();
            case SESSION_CHARGE_CONSUMPTION:
                return (Double) (goeResponse.getSessionChargeConsumption() / 360000d);
            case SESSION_CHARGE_CONSUMPTION_LIMIT:
                return (Double) (goeResponse.getSessionChargeConsumptionLimit() / 10d);
            case TOTAL_CONSUMPTION:
                return (Double) (goeResponse.getTotalChargeConsumption() / 10d);
            case FIRMWARE:
                return goeResponse.getFirmware();
            case VOLTAGE_L1:
                return goeResponse.getEnergy()[0];
            case VOLTAGE_L2:
                return goeResponse.getEnergy()[1];
            case VOLTAGE_L3:
                return goeResponse.getEnergy()[2];
            case CURRENT_L1:
                return (Double) (goeResponse.getEnergy()[4] / 10d);
            case CURRENT_L2:
                return (Double) (goeResponse.getEnergy()[5] / 10d);
            case CURRENT_L3:
                return (Double) (goeResponse.getEnergy()[6] / 10d);
            case POWER_L1:
                return (Double) (goeResponse.getEnergy()[7] / 10d);
            case POWER_L2:
                return (Double) (goeResponse.getEnergy()[8] / 10d);
            case POWER_L3:
                return (Double) (goeResponse.getEnergy()[9] / 10d);
            default:
                return null;
            }
        }
        return null;
    }

    /**
     * Update the channel from the last data
     *
     * @param channelId the id identifying the channel to be updated
     */
    protected void updateChannel(String channelId) {
        if (!isLinked(channelId)) {
            return;
        }

        Object value = getValue(channelId);
        if (value == null) {
            logger.debug("Value retrieved for channel '{}' was null. Can't update.", channelId);
            return;
        }

        State state = null;
        if (value instanceof Boolean) {
            state = (Boolean) value ? OnOffType.ON : OnOffType.OFF;
        } else if (value instanceof Double) {
            state = new DecimalType((Double) value);
        } else if (value instanceof Integer) {
            state = new DecimalType((Integer) value);
        } else if (value instanceof String) {
            state = new StringType((String) value);
        } else {
            logger.warn("Update channel {}: Unsupported value type {}", channelId, value.getClass().getSimpleName());
        }
        logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                value.getClass().getSimpleName());

        // Update the channel
        if (state != null) {
            updateState(channelId, state);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
            return;
        }

        if (MAX_AMPERE.equals(channelUID.getId())) {
            // TODO: handle commands

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(GoEChargerConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        String errorMsg = null;

        if (StringUtils.trimToNull(config.ip) == null) {
            errorMsg = "Parameter 'ip' is mandatory and must be configured";
        }

        if (errorMsg != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        } else {
            // updateStatus(ThingStatus.ONLINE); // TODO should it already be online here???
            startAutomaticRefresh();
        }
        logger.debug("Finished initializing!");
    }

    /**
     * Request new data from Go-E charger
     *
     * @return the Go-E charger object mapping the JSON response or null in case of
     *         error
     */
    private GoEStatusResponse getGoEData() {
        GoEStatusResponse result = null;
        String errorMsg = null;

        GoEChargerConfiguration config = getConfigAs(GoEChargerConfiguration.class);

        String urlStr = GoEChargerBindingConstants.API_URL;
        urlStr = urlStr.replace("%IP%", StringUtils.trimToEmpty(config.ip));
        logger.debug("URL = {}, IP = {}", urlStr, config.ip);

        try {
            // Run the HTTP request and get the JSON response
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();

            try {
                String response = IOUtils.toString(connection.getInputStream());

                // Map the JSON response to an object
                result = gson.fromJson(response, GoEStatusResponse.class);
            } finally {
                IOUtils.closeQuietly(connection.getInputStream());
            }

            if (result != null) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                return result;
            } else {
                retryCounter++;
                if (retryCounter == 1) {
                    logger.warn("Error in getting data from Go-E charger, retrying once");
                    return getGoEData();
                }
                logger.warn("Error in Go-E charger response: {}", errorMsg);
            }
        } catch (MalformedURLException e) {
            errorMsg = e.getMessage();
        } catch (JsonSyntaxException e) {
            errorMsg = e.getMessage();
        } catch (IOException | IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        return null;
    }

    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                try {
                    // Request new GoE data
                    retryCounter = 0;
                    goeResponse = getGoEData();

                    // Update all channels from the updated GoE data
                    for (Channel channel : getThing().getChannels()) {
                        updateChannel(channel.getUID().getId());
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                }
            };

            GoEChargerConfiguration config = getConfigAs(GoEChargerConfiguration.class);
            int delay = (config.refreshInterval != null) ? config.refreshInterval.intValue() : DEFAULT_REFRESH_INTERVAL;
            logger.debug("Running refresh job with delay {} s", delay);
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Go-E Charger handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }
}
