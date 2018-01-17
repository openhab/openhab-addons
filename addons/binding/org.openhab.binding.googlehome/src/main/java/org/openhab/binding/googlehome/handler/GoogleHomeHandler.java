/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.googlehome.handler;

import static org.openhab.binding.googlehome.GoogleHomeBindingConstants.CHANNEL_UPTIME;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.googlehome.GoogleHomeBindingConstants;
import org.openhab.binding.googlehome.internal.GoogleHomeConfiguration;
import org.openhab.binding.googlehome.json.EurekaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link GoogleHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class GoogleHomeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GoogleHomeHandler.class);

    private static final int DEFAULT_REFRESH_PERIOD = 30;

    private ScheduledFuture<?> statusUpdaterFuture;

    EurekaInfo eurekaInfo;

    private Gson gson;

    @Nullable
    private GoogleHomeConfiguration config;

    public GoogleHomeHandler(@NonNull Thing thing) {
        super(thing);
        gson = new Gson();
    }

    private int getIntConfigParameter(String key, int defaultValue) {
        Object obj = this.getConfig().get(key);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt(obj.toString());
        }
        return defaultValue;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_UPTIME)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Google Home handler.");
        // try {
        // String host = getConfig().get(HOST_PARAMETER).toString();
        // if (host == null || host.isEmpty()) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "No network address specified");
        // } else {
        // connection.connect(host, getIntConfigParameter(PORT_PARAMETER, 8008), scheduler, getBaseUrl());
        //
        // connectionCheckerFuture = scheduler.scheduleWithFixedDelay(() -> {
        // if (!connection.checkConnection()) {
        // updateStatus(ThingStatus.OFFLINE);
        // }
        // }, 1, 10, TimeUnit.SECONDS);
        //
        // statusUpdaterFuture = scheduler.scheduleWithFixedDelay(() -> {
        // if (KodiState.Play.equals(connection.getState())) {
        // connection.updatePlayerStatus();
        // }
        // }, 1, getIntConfigParameter(REFRESH_PARAMETER, 10), TimeUnit.SECONDS);
        // }
        // } catch (Exception e) {
        // logger.debug("error during opening connection: {}", e.getMessage(), e);
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        // }

        config = getConfigAs(GoogleHomeConfiguration.class);

        logger.debug("config ipAddress = {}", config.getIpAddress());
        logger.debug("config port = {}", config.getPort());
        logger.debug("config refreshInterval = {}", config.getRefreshInterval());

        boolean validConfig = true;
        String errorMsg = null;

        if (StringUtils.trimToNull(config.getIpAddress()) == null) {
            errorMsg = "Parameter 'ipAddress' is mandatory and must be configured";
            validConfig = false;
        }
        if (config.getPort() == null) {
            errorMsg = "Parameter 'port' is mandatory and must be configured";
            validConfig = false;
        }
        if (config.getRefreshInterval() != null && config.getRefreshInterval() < 10) {
            errorMsg = "Parameter 'refreshInterval' cannot exceed 10 seconds";
            validConfig = false;
        }

        if (validConfig) {
            updateStatus(ThingStatus.ONLINE);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job refreshing the Google Home data
     */
    private void startAutomaticRefresh() {
        if (statusUpdaterFuture == null || statusUpdaterFuture.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // Request new data to the Google Home REST API
                        eurekaInfo = updateGoogleHomeData();

                        // Update all channels from the updated data
                        for (Channel channel : getThing().getChannels()) {
                            updateChannel(channel.getUID().getId(), eurekaInfo);
                        }
                    } catch (Exception e) {
                        logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                }
            };

            config = getConfigAs(GoogleHomeConfiguration.class);
            int delay = (config.getRefreshInterval() != null) ? config.getRefreshInterval().intValue()
                    : DEFAULT_REFRESH_PERIOD;
            statusUpdaterFuture = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.SECONDS);
        }
    }

    /**
     * Wrap the given String in a new {@link StringType} or returns {@link UnDefType#UNDEF} if the String is empty.
     */
    private State createState(String string) {
        if (string.isEmpty()) {
            return UnDefType.UNDEF;
        } else {
            return new StringType(string);
        }
    }

    /**
     * Get new data from Google Home REST API
     *
     * @return {EurekaInfo}
     */
    private EurekaInfo updateGoogleHomeData() {
        config = getConfigAs(GoogleHomeConfiguration.class);
        return getGoogleHomeData();
    }

    /**
     * Update the channel from the last Air Quality data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId, EurekaInfo eurekaInfo) {
        if (isLinked(channelId)) {
            Object value;
            try {
                value = getValue(channelId, eurekaInfo);
            } catch (Exception e) {
                logger.debug("This Google Home doesn't provide {} value", channelId.toUpperCase());
                return;
            }

            State state = null;
            if (value == null) {
                state = UnDefType.UNDEF;
            } else if (value instanceof Boolean) {
                state = new StringType(value.toString());
            } else if (value instanceof Long) {
                state = new DecimalType((Long) value);
            } else if (value instanceof Double) {
                state = new DecimalType((Double) value);
            } else if (value instanceof Integer) {
                state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
            } else if (value instanceof String) {
                state = new StringType(value.toString());
            } else {
                logger.warn("Update channel {}: Unsupported value type {}", channelId,
                        value.getClass().getSimpleName());
            }
            logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                    (value == null) ? "null" : value.getClass().getSimpleName());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            }
        }
    }

    /**
     * Request new air quality data to the Google Home REST API
     *
     * @param location geo-coordinates from config
     * @param stationId station ID from config
     *
     * @return the air quality data object mapping the JSON response or null in case of error
     */
    private @Nullable EurekaInfo getGoogleHomeData() {
        EurekaInfo result = null;
        boolean resultOk = false;
        String errorMsg = null;

        try {
            // Build a valid URL for the Google Home REST API
            config = getConfigAs(GoogleHomeConfiguration.class);
            String host = config.getIpAddress();
            int port = config.getPort();

            // Run the HTTP request and get the JSON response
            URL url = new URL("http://" + host + ":" + port + "/setup/eureka_info");
            URLConnection connection = url.openConnection();

            try {
                String response = IOUtils.toString(connection.getInputStream());
                logger.debug("eurekaInfo = {}", response);

                // Map the JSON response to an object
                result = gson.fromJson(response, EurekaInfo.class);
            } finally {
                IOUtils.closeQuietly(connection.getInputStream());
            }

            if (result.getBssid() != null) {
                resultOk = true;
            } else {
                errorMsg = "missing data sub-object";
            }

            if (!resultOk) {
                logger.warn("Error in Google Home response: {}", errorMsg);
            }

        } catch (MalformedURLException e) {
            errorMsg = e.getMessage();
            logger.warn("Constructed URL is not valid: {}", errorMsg);
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.warn("Error running Google Home request: {}", errorMsg);
        } catch (IOException | IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        // Update the thing status
        if (resultOk) {
            String buildVersion = "Build version: " + result.getBuildVersion();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, buildVersion);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        }

        if (resultOk) {
            return result;
        } else {
            return null;
        }
    }

    public static Object getValue(String channelId, EurekaInfo eurekaInfo) throws Exception {
        String[] fields = StringUtils.split(channelId, "#");

        if (eurekaInfo == null) {
            return null;
        }

        String fieldName = fields[0];

        switch (fieldName) {
            case GoogleHomeBindingConstants.CHANNEL_UPTIME:
                return eurekaInfo.getUptime();
            case GoogleHomeBindingConstants.CHANNEL_HAS_UPDATE:
                return eurekaInfo.getHasUpdate();
            case GoogleHomeBindingConstants.CHANNEL_NOISE_LEVEL:
                return eurekaInfo.getNoiseLevel();
            case GoogleHomeBindingConstants.CHANNEL_SIGNAL_LEVEL:
                return eurekaInfo.getSignalLevel();
            // case GoogleHomeBindingConstants.CHANNEL_NIGHT_MODE:
            // return eurekaInfo.getNightMode();
            // case GoogleHomeBindingConstants.CHANNEL_ALARM:
            // return eurekaInfo.getAlarm();
            // case GoogleHomeBindingConstants.CHANNEL_TIMER:
            // return eurekaInfo.getTimer();
        }

        return null;
    }

}
