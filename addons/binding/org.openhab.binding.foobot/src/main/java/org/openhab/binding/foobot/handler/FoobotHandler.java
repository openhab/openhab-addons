/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foobot.handler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
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
import org.openhab.binding.foobot.FoobotBindingConstants;
import org.openhab.binding.foobot.internal.FoobotConfiguration;
import org.openhab.binding.foobot.internal.json.FoobotJsonData;
import org.openhab.binding.foobot.internal.json.FoobotJsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link FoobotHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Divya Chauhan - Initial contribution
 */
public class FoobotHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FoobotHandler.class);

    private static final int DEFAULT_REFRESH_PERIOD = 30;

    private static final String URL1 = "https://api.foobot.io/v2/owner/%username%/device/";

    private static final String URL2 = "https://api.foobot.io/v2/device/%uuid%/datapoint/%period%/last/0/?sensorList=%sensors%";

    FoobotJsonData foobotData;

    private Gson gson;

    private String uuid;

    FoobotConfiguration config;

    private ScheduledFuture<?> refreshJob;

    public FoobotHandler(Thing thing) {
        super(thing);
        gson = new Gson();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Foobot handler.");
        config = getConfigAs(FoobotConfiguration.class);

        logger.debug("config apikey = {}", config.apikey);
        logger.debug("config apikey = {}", config.username);
        logger.debug("config apikey = {}", config.mac);
        logger.debug("config refresh = {}", config.refresh);

        boolean validConfig = true;
        boolean resultOk = false;
        String errorMsg = null;

        if (StringUtils.trimToNull(config.apikey) == null) {
            errorMsg = "Parameter 'apikey' is mandatory and must be configured";
            validConfig = false;
        }
        if (StringUtils.trimToNull(config.username) == null) {
            errorMsg = "Parameter 'username' is mandatory and must be configured";
            validConfig = false;
        }
        if (StringUtils.trimToNull(config.mac) == null) {
            errorMsg = "Parameter 'Mac Address' is mandatory and must be configured";
            validConfig = false;
        }
        if (config.refresh != null && config.refresh < 5) {
            errorMsg = "Parameter 'refresh' must be at least 5 minutes";
            validConfig = false;
        }

        if (validConfig) {

            try {
                String urlStr = URL1.replace("%username%", StringUtils.trimToEmpty(config.username));
                logger.debug("URL = {}", urlStr);

                // Run the HTTP request and get the list of all foobot devices belonging to the user from foobot.io

                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("X-API-KEY-TOKEN", config.apikey);

                try {
                    String response = IOUtils.toString(connection.getInputStream());
                    logger.debug("foobotResponse = {}", response);

                    // Map the JSON response to list of objects

                    Type listType = new TypeToken<ArrayList<FoobotJsonResponse>>() {
                    }.getType();

                    List<FoobotJsonResponse> readFromJson = gson.fromJson(response, listType);

                    for (FoobotJsonResponse ob : readFromJson) {

                        // Compare the mac address to each record in order to fetch the UUID of the current device.

                        if (ob.getMac().equals(config.mac)) {
                            uuid = ob.getUuid();
                            resultOk = true;
                            break;
                        }
                    }
                } finally {
                    IOUtils.closeQuietly(connection.getInputStream());
                }

                if (!resultOk) {
                    logger.warn("Error in foobot.io response: {}", errorMsg);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
                }

            } catch (MalformedURLException e) {
                errorMsg = e.getMessage();
                logger.warn("Constructed url is not valid: {}", errorMsg);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            } catch (JsonSyntaxException e) {
                errorMsg = "Configuration is incorrect";
                logger.warn("Error running foobot.io request: {}", errorMsg);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            } catch (IOException | IllegalStateException e) {
                errorMsg = e.getMessage();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            }

            updateStatus(ThingStatus.ONLINE);
            startAutomaticRefresh();
        } else

        {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job refreshing the Sensor data from Foobot device
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // Request new sensor data from foobot.io service
                        foobotData = updateFoobotData();

                        // Update all channels from the updated Sensor data
                        for (Channel channel : getThing().getChannels()) {
                            updateChannel(channel.getUID().getId(), foobotData);
                        }
                    } catch (Exception e) {
                        logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                }
            };

            config = getConfigAs(FoobotConfiguration.class);
            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Foobot handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId(), foobotData);
        } else {
            logger.debug("The Foobot binding is read-only and can not handle command {}", command);
        }

    }

    private void updateChannel(String channelId, FoobotJsonData foobotResponse) {
        if (isLinked(channelId)) {
            Object value;
            try {
                value = getValue(channelId, foobotResponse);
            } catch (Exception e) {
                logger.debug("Foobot device doesn't provide sensor data for channel id = {}", channelId.toUpperCase());
                return;
            }

            State state = null;
            if (value == null) {
                state = UnDefType.UNDEF;
            } else if (value instanceof BigDecimal) {
                state = new DecimalType((BigDecimal) value);
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

    private FoobotJsonData updateFoobotData() {
        FoobotJsonData result = null;
        boolean resultOk = false;
        String errorMsg = null;

        try {
            config = getConfigAs(FoobotConfiguration.class);

            String urlStr = URL2.replace("%uuid%", StringUtils.trimToEmpty(uuid));
            urlStr = urlStr.replace("%period%", Integer.toString(config.refresh * 60));
            urlStr = urlStr.replace("%sensors%", StringUtils.trimToEmpty(""));

            logger.debug("URL = {}", urlStr);

            // Run the HTTP request and get the JSON response from foobot.io

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-API-KEY-TOKEN", config.apikey);

            try {
                String response = IOUtils.toString(connection.getInputStream());
                logger.debug("FoobotResponse = {}", response);

                // Map the JSON response to an object
                if (StringUtils.trimToNull(response) == null) {
                    errorMsg = "no data returned";
                }

                result = gson.fromJson(response, FoobotJsonData.class);
                resultOk = true;

            } finally {
                IOUtils.closeQuietly(connection.getInputStream());
            }

            if (!resultOk) {
                logger.warn("Error in foobot.io response: {}", errorMsg);
            }

        } catch (MalformedURLException e) {
            errorMsg = e.getMessage();
            logger.warn("Constructed url is not valid: {}", errorMsg);
        } catch (JsonSyntaxException e) {
            errorMsg = "Configuration is incorrect";
            logger.warn("Error running foobot.io request: {}", errorMsg);
        } catch (IOException | IllegalStateException e) {
            errorMsg = e.getMessage();
        }

        // Update the thing status
        if (resultOk) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
        }

        return resultOk ? result : null;
    }

    public static Object getValue(String channelId, FoobotJsonData data) throws Exception {
        String[] fields = StringUtils.split(channelId, "#");

        if (data == null) {
            return null;
        }

        String fieldName = fields[0];

        switch (fieldName) {
            case FoobotBindingConstants.TMP:
                return new BigDecimal(data.getDatapointsListToArray()[data.getSensors().indexOf("tmp")]);
            case FoobotBindingConstants.CO2:
                return new BigDecimal(data.getDatapointsListToArray()[data.getSensors().indexOf("co2")]);
            case FoobotBindingConstants.GPI:
                return new BigDecimal(data.getDatapointsListToArray()[data.getSensors().indexOf("allpollu")]);
            case FoobotBindingConstants.PM:
                return new BigDecimal(data.getDatapointsListToArray()[data.getSensors().indexOf("pm")]);
            case FoobotBindingConstants.HUM:
                return new BigDecimal(data.getDatapointsListToArray()[data.getSensors().indexOf("hum")]);
            case FoobotBindingConstants.VOC:
                return new BigDecimal(data.getDatapointsListToArray()[data.getSensors().indexOf("voc")]);

        }

        return null;
    }
}
