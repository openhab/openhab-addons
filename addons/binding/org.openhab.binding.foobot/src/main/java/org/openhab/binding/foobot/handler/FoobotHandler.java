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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
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

    private static final String URL2 = "https://api.foobot.io/v2/device/%uuid%/datapoint/0/last/0/?sensorList=%sensors%";

    private FoobotJsonData foobotData;

    private HttpParams params;

    private Gson gson;

    private String uuid;

    private FoobotConfiguration config;

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
        logger.debug("config username = {}", config.username);
        logger.debug("config mac = {}", config.mac);
        logger.debug("config refresh = {}", config.refresh);

        boolean validConfig = true;
        boolean deviceFound = false;
        String errorMsg = "";

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

        if (!validConfig) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);

            return;
        }
        try {
            String urlStr = URL1.replace("%username%", StringUtils.trimToEmpty(config.username));
            logger.debug("URL = {}", urlStr);

            // Run the HTTP request and get the list of all foobot devices belonging to the user from foobot.io
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(urlStr);
            getRequest.addHeader("accept", "application/json");
            getRequest.addHeader("X-API-KEY-TOKEN", config.apikey);
            HttpResponse response = httpClient.execute(getRequest);
            logger.debug("foobotResponse = {}", response);

            if (response.getStatusLine().getStatusCode() == 404) {
                errorMsg = "Configuration is incorrect";
                logger.warn("Error running foobot.io request: {}", errorMsg);
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);

                return;
            }
            // Map the JSON response to list of objects
            Type listType = new TypeToken<ArrayList<FoobotJsonResponse>>() {
            }.getType();
            String userDevices = EntityUtils.toString(response.getEntity());
            List<FoobotJsonResponse> readFromJson = gson.fromJson(userDevices, listType);
            for (FoobotJsonResponse ob : readFromJson) {
                // Compare the mac address to each record in order to fetch the UUID of the current device.
                if (ob.getMac().equals(config.mac)) {
                    uuid = ob.getUuid();
                    deviceFound = true;
                    break;
                }
            }
            if (!deviceFound) {
                errorMsg = "No device found for this MAC address.";
                logger.warn("Error in foobot.io response: {}", errorMsg);
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);

                return;
            }
        } catch (MalformedURLException e) {
            logger.warn("Constructed url is not valid: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (JsonSyntaxException e) {
            logger.warn("Error running foobot.io request: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (IOException | IllegalStateException e) {
            logger.warn("Error running foobot.io request: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
        updateStatus(ThingStatus.ONLINE);
        startAutomaticRefresh();
    }

    /**
     * Start the job refreshing the Sensor data from Foobot device
     */
    private void startAutomaticRefresh() {
        int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            // Request new sensor data from foobot.io service
            foobotData = updateFoobotData();

            // Update all channels from the updated Sensor data
            for (Channel channel : getThing().getChannels()) {
                updateChannel(channel.getUID().getId(), foobotData);
            }
        }, 0, delay, TimeUnit.MINUTES);
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
            return;
        } else {
            logger.debug("The Foobot binding is read-only and can not handle command {}", command);
        }
    }

    private void updateChannel(String channelId, FoobotJsonData foobotResponse) {
        Object value = null;
        try {
            value = getValue(channelId, foobotResponse);
        } catch (Exception e) {
            logger.debug("Foobot device doesn't provide sensor data for channel id = {}", channelId.toUpperCase());
        }

        if (value == null) {
            updateState(channelId, UnDefType.NULL);
            logger.debug("Update channel {}: null value for the channel", channelId);
        } else if (value instanceof BigDecimal) {
            State state = new DecimalType((BigDecimal) value);
            updateState(channelId, state);
            logger.debug("Update channel {} with state {} ({})", channelId, state, value.getClass().getSimpleName());
        } else {
            updateState(channelId, UnDefType.UNDEF);
            logger.debug("Update channel {}: Unsupported value type {}", channelId, value.getClass().getSimpleName());
        }
    }

    private FoobotJsonData updateFoobotData() {
        FoobotJsonData result = null;
        try {
            // Run the HTTP request and get the list of all foobot devices belonging to the user from foobot.io
            String urlStr = URL2.replace("%uuid%", StringUtils.trimToEmpty(uuid));
            urlStr = urlStr.replace("%sensors%", StringUtils.trimToEmpty(""));
            logger.debug("URL = {}", urlStr);

            // Run the HTTP request and get the JSON response from foobot.io
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(urlStr);
            getRequest.addHeader("accept", "application/json");
            getRequest.addHeader("X-API-KEY-TOKEN", config.apikey);
            HttpResponse response = httpClient.execute(getRequest);

            String responseData = EntityUtils.toString(response.getEntity());
            logger.debug(responseData);

            if (StringUtils.trimToNull(responseData) == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "no data returned");
            } else {
                // Map the JSON response to an object
                result = gson.fromJson(responseData, FoobotJsonData.class);
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            }
        } catch (MalformedURLException e) {
            logger.debug("Constructed url is not valid: {}", e.getMessage());
        } catch (JsonSyntaxException e) {
            logger.debug("Error running foobot.io request: {}", e.getMessage());
        } catch (IOException | IllegalStateException e) {
            logger.debug(e.getMessage());
        }

        return result;
    }

    public static BigDecimal getValue(String channelId, FoobotJsonData data) {
        if (data == null) {
            return null;
        }
        switch (channelId) {
            case FoobotBindingConstants.TMP:
                return new BigDecimal(data.getDatapointsList().get(data.getSensors().indexOf("tmp")));
            case FoobotBindingConstants.CO2:
                return new BigDecimal(data.getDatapointsList().get(data.getSensors().indexOf("co2")));
            case FoobotBindingConstants.GPI:
                return new BigDecimal(data.getDatapointsList().get(data.getSensors().indexOf("allpollu")));
            case FoobotBindingConstants.PM:
                return new BigDecimal(data.getDatapointsList().get(data.getSensors().indexOf("pm")));
            case FoobotBindingConstants.HUM:
                return new BigDecimal(data.getDatapointsList().get(data.getSensors().indexOf("hum")));
            case FoobotBindingConstants.VOC:
                return new BigDecimal(data.getDatapointsList().get(data.getSensors().indexOf("voc")));
        }

        return null;
    }
}
