/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foobot.internal.handler;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
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
import org.openhab.binding.foobot.internal.FoobotBindingConstants;
import org.openhab.binding.foobot.internal.config.FoobotDeviceConfiguration;
import org.openhab.binding.foobot.internal.json.FoobotDevice;
import org.openhab.binding.foobot.internal.json.FoobotJsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link FoobotHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Divya Chauhan - Initial contribution
 * @author George Katsis - Add Bridge thing type
 */
public class FoobotHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FoobotHandler.class);

    private static final String URL_TO_FETCH_SENSOR_DATA = "https://api.foobot.io/v2/device/%uuid%/datapoint/0/last/0/?sensorList=%sensors%";
    private static final Gson GSON = new Gson();

    private @Nullable FoobotDevice device;
    private @Nullable FoobotAccountHandler account;
    private @Nullable String apiKey;
    private @Nullable String username;
    private @Nullable String mac;
    private @Nullable Integer refreshIntervalInMinutes;
    private FoobotJsonData foobotData;
    private FoobotDeviceConfiguration config;
    private ScheduledFuture<?> retrieveFoobotJob;
    private ScheduledFuture<?> refreshDataJob;
    private final HttpClient httpClient;

    public FoobotHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Foobot handler.");

        FoobotDeviceConfiguration config = getThing().getConfiguration().as(FoobotDeviceConfiguration.class);

        Bridge bridge = this.getBridge();
        if (bridge != null) {
            FoobotAccountHandler account = (FoobotAccountHandler) bridge.getHandler();
            if (account != null) {
                this.account = account;
            }
        }

        List<String> missingParams = new ArrayList<>();
        String errorMsg = "";

        this.apiKey = config.apiKey;
        if (StringUtils.trimToNull(this.apiKey) == null) {
            missingParams.add("'apikey'");
        }
        this.username = config.username;
        if (StringUtils.trimToNull(this.username) == null) {
            missingParams.add("'username'");
        }
        this.mac = config.mac;
        if (StringUtils.trimToNull(this.mac) == null) {
            missingParams.add("'mac'");
        }

        if (missingParams.size() > 0) {
            errorMsg = "Parameter" + (missingParams.size() == 1 ? " [" : "s [") + StringUtils.join(missingParams, ",")
                    + (missingParams.size() == 1 ? "] is " : "] are ") + "mandatory and must be configured";

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            return;
        }

        this.refreshIntervalInMinutes = config.refreshIntervalInMinutes;
        if (this.refreshIntervalInMinutes == null || this.refreshIntervalInMinutes < 5) {
            logger.warn(
                    "Refresh interval time [{}] is not valid. Refresh interval time must be at least 5 minutes.  Setting to 7 sec",
                    this.refreshIntervalInMinutes);
            this.refreshIntervalInMinutes = FoobotBindingConstants.DEFAULT_REFRESH_PERIOD_MINUTES;
        }

        this.retrieveFoobotJob = scheduler.schedule(() -> retrieveFoobot(), 0, TimeUnit.SECONDS);
    }

    private void retrieveFoobot() {

        List<FoobotDevice> devices;
        String errorMsg;

        if (this.account == null) {

            String urlStr = FoobotBindingConstants.URL_TO_FETCH_UUID.replace("%username%",
                    StringUtils.trimToEmpty(config.username));
            logger.debug("URL = {}", urlStr);
            ContentResponse response;

            // Run the HTTP request and get the list of all foobot devices belonging to the user from foobot.io
            Request request = httpClient.newRequest(urlStr).timeout(3, TimeUnit.SECONDS);
            request.header("accept", "application/json");
            request.header("X-API-KEY-TOKEN", config.apiKey);

            try {
                response = request.send();
                logger.debug("foobotResponse = {}", response);

                if (response.getStatus() != 200) {
                    errorMsg = response.getContentAsString();
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);
                    return;
                }
                // Map the JSON response to list of objects
                Type listType = new TypeToken<ArrayList<FoobotDevice>>() {
                }.getType();
                String userDevices = response.getContentAsString();
                devices = GSON.fromJson(userDevices, listType);
            } catch (ExecutionException | TimeoutException | InterruptedException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
                return;
            }
        } else {
            devices = this.account.getAssociatedDevices();
        }

        if (devices == null) {
            errorMsg = "No devices found on this account.";
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            return;
        }

        for (FoobotDevice ob : devices) {
            // Compare the mac address to each record in order to fetch the UUID of the current device.
            if (config.mac.equals(ob.getMac())) {
                this.device = ob;
                break;
            }
        }

        if (this.device == null) {
            errorMsg = "No device found for this MAC address.";
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        startAutomaticRefresh();

    }

    /**
     * Start the job refreshing the Sensor data from Foobot device
     */
    private void startAutomaticRefresh() {
        int delay = (config.refreshIntervalInMinutes != null) ? config.refreshIntervalInMinutes.intValue()
                : FoobotBindingConstants.DEFAULT_REFRESH_PERIOD_MINUTES;
        refreshDataJob = scheduler.scheduleWithFixedDelay(() -> {
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
        if (refreshDataJob != null && !refreshDataJob.isCancelled()) {
            refreshDataJob.cancel(true);
            refreshDataJob = null;
        }
    }

    @Override
    public void handleCommand(@NonNullByDefault ChannelUID channelUID, @NonNullByDefault Command command) {
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
        } catch (RuntimeException rte) {
            logger.debug("RuntimeException while getting Foobot sensor data for channel id = {}: {}",
                    channelId.toUpperCase(), rte);
        } catch (Exception e) {
            logger.debug("Foobot device doesn't provide sensor data for channel id = {}: {}", channelId.toUpperCase(),
                    e);
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
        // Run the HTTP request and get the list of all foobot devices belonging to the user from foobot.io
        String urlStr = URL_TO_FETCH_SENSOR_DATA.replace("%uuid%", StringUtils.trimToEmpty(this.device.getUuid()));
        urlStr = urlStr.replace("%sensors%", StringUtils.trimToEmpty(""));
        logger.debug("URL = {}", urlStr);

        ContentResponse response;

        Request request = httpClient.newRequest(urlStr).timeout(3, TimeUnit.SECONDS);
        request.header("accept", "application/json");
        request.header("X-API-KEY-TOKEN", config.apiKey);

        try {
            // Run the HTTP request and get the JSON response from foobot.io
            response = request.send();
            logger.debug("foobotResponse = {}", response);

            String responseData = response.getContentAsString();
            logger.debug(responseData);

            if (StringUtils.trimToNull(responseData) == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "no data returned");
            } else {
                // Map the JSON response to an object
                result = GSON.fromJson(responseData, FoobotJsonData.class);
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            }
        } catch (ExecutionException ee) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ee.getMessage());
        } catch (TimeoutException te) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, te.getMessage());
        } catch (InterruptedException ie) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ie.getMessage());
        }

        return result;
    }

    public static BigDecimal getValue(String channelId, FoobotJsonData data) {
        if (data == null) {
            return null;
        }
        return getBigDecimalForLabelAndData(channelId, data);
    }

    private static BigDecimal getBigDecimalForLabelAndData(String channelId, FoobotJsonData data) {

        if (channelId != null && FoobotBindingConstants.SENSOR_MAP.containsKey(channelId)) {
            return new BigDecimal(data.getDatapointsList()
                    .get(data.getSensors().indexOf(FoobotBindingConstants.SENSOR_MAP.get(channelId))));
        } else {
            return null;
        }
    }
}
