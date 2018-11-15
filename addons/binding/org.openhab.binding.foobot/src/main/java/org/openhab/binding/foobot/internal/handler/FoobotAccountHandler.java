/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foobot.internal.handler;

import static org.openhab.binding.foobot.internal.FoobotBindingConstants.DEFAULT_REFRESH_PERIOD_MINUTES;

import java.lang.reflect.Type;
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
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.foobot.internal.config.FoobotAccountConfiguration;
import org.openhab.binding.foobot.internal.json.FoobotDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * HaBridge handler to manage Foobot Account
 *
 * @author George Katsis - Initial contribution
 */

@NonNullByDefault
public class FoobotAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FoobotAccountHandler.class);

    private @Nullable String username;
    public @Nullable String apiKey;
    private @Nullable Integer refreshIntervalInMinutes;
    private static final String URL_TO_FETCH_DEVICES = "https://api.foobot.io/v2/owner/%username%/device/";
    private static final Gson GSON = new Gson();
    private final HttpClient httpClient;
    private @Nullable List<FoobotDevice> foobotDevices = new ArrayList<>();
    private @Nullable ScheduledFuture<?> refreshDeviceListJob;

    public FoobotAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Foobot Account bridge starting...");

        FoobotAccountConfiguration accountConfig = getConfigAs(FoobotAccountConfiguration.class);
        logger.debug("accountConfig username = {}", accountConfig.username);
        logger.debug("accountConfig refreshIntervalInMinutes = {}", accountConfig.refreshIntervalInMinutes);

        List<String> missingParams = new ArrayList<>();
        String errorMsg = "";

        this.apiKey = accountConfig.apiKey;
        if (StringUtils.trimToNull(this.apiKey) == null) {
            missingParams.add("'apikey'");
        }

        this.username = accountConfig.username;
        if (StringUtils.trimToNull(this.username) == null) {
            missingParams.add("'username'");
        }

        if (missingParams.size() > 0) {
            errorMsg = "Parameter" + (missingParams.size() == 1 ? " [" : "s [") + StringUtils.join(missingParams, ",")
                    + (missingParams.size() == 1 ? "] is " : "] are ") + "mandatory and must be configured";

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            return;
        }

        this.refreshIntervalInMinutes = accountConfig.refreshIntervalInMinutes;
        if (this.refreshIntervalInMinutes == null || this.refreshIntervalInMinutes < 5) {
            logger.warn(
                    "Refresh interval time [{}] is not valid. Refresh interval time must be at least 5 minutes.  Setting to 7 sec",
                    accountConfig.refreshIntervalInMinutes);
            this.refreshIntervalInMinutes = DEFAULT_REFRESH_PERIOD_MINUTES;
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Wait to get associated devices");

        this.refreshDeviceListJob = scheduler.scheduleWithFixedDelay(this::refreshDeviceList, 0, 1, TimeUnit.DAYS);

        logger.debug("Foobot account bridge handler started.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);
        if (command instanceof RefreshType) {
            refreshDeviceList();
        }
    }

    @Override
    public void handleRemoval() {
        cleanup();
        super.handleRemoval();
    }

    private void cleanup() {
        logger.debug("cleanup {}", getThing().getUID().getAsString());

        @Nullable
        ScheduledFuture<?> refreshDeviceListJob = this.refreshDeviceListJob;
        if (refreshDeviceListJob != null) {
            refreshDeviceListJob.cancel(true);
            this.refreshDeviceListJob = null;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose {}", getThing().getUID().getAsString());

        cleanup();
        super.dispose();
    }

    public List<FoobotDevice> getAssociatedDevices() {

        HttpClient currentHttpClient = this.httpClient;

        String urlStr = URL_TO_FETCH_DEVICES.replace("%username%", StringUtils.trimToEmpty(this.username));
        logger.debug("URL = {}", urlStr);
        ContentResponse response;
        String errorMsg;

        // Run the HTTP request and get the list of all foobot devices belonging to the user from foobot.io
        Request request = currentHttpClient.newRequest(urlStr).timeout(3, TimeUnit.SECONDS);
        request.header("accept", "application/json");
        request.header("X-API-KEY-TOKEN", this.apiKey);

        List<FoobotDevice> devices;

        try {
            response = request.send();
            logger.debug("foobotDeviceListResponse = {}", response);

            if (response.getStatus() != 200) {
                errorMsg = response.getContentAsString();
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);
                return new ArrayList<FoobotDevice>();
            }

            // Map the JSON response to list of objects
            Type listType = new TypeToken<ArrayList<FoobotDevice>>() {
            }.getType();
            String userDevices = response.getContentAsString();
            devices = GSON.fromJson(userDevices, listType);

            if (devices != null && devices.size() > 0) {
                updateStatus(ThingStatus.ONLINE);
                this.foobotDevices = devices;
            } else {
                updateStatus(ThingStatus.OFFLINE);
                devices = new ArrayList<FoobotDevice>();
            }

        } catch (ExecutionException | TimeoutException | InterruptedException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
            devices = new ArrayList<FoobotDevice>();
        }
        return devices;
    }

    public List<FoobotDevice> getDeviceList() {
        if (this.foobotDevices != null) {
            return this.foobotDevices;
        } else {
            return new ArrayList<FoobotDevice>();
        }
    }

    private void refreshDeviceList() {
        try {
            logger.debug("Refreshing device list {}", getThing().getUID().getAsString());

            // get all devices associated with the account
            this.foobotDevices = getAssociatedDevices();

            // update account state
            if (this.foobotDevices.size() > 0) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            logger.debug("Refresh device list {} finished", getThing().getUID().getAsString());

        } catch (Exception e) {
            logger.error("Refresh device list fails with unexpected error {}", e);
        }
    }
}
