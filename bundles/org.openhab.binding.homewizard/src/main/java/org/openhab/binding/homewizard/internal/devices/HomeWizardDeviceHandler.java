/**
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
package org.openhab.binding.homewizard.internal.devices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homewizard.internal.HomeWizardConfiguration;
import org.openhab.binding.homewizard.internal.devices.dto.HomeWizardDeviceInformationPayload;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link HomeWizardDeviceHandler} is a base class for all
 * HomeWizard devices. It provides configuration and polling of
 * data from a device. It also processes common data.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - changes to API calls and ground work for v2
 *
 */
@NonNullByDefault
public abstract class HomeWizardDeviceHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(HomeWizardDeviceHandler.class);
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    protected ScheduledExecutorService executorService = this.scheduler;
    private HomeWizardConfiguration config = new HomeWizardConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;

    protected List<String> supportedTypes = new ArrayList<String>();
    protected int apiVersion = 1;
    protected String apiURL = "";

    /**
     * Constructor
     *
     * @param thing The thing to handle
     */
    public HomeWizardDeviceHandler(Thing thing) {
        super(thing);
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(HomeWizardConfiguration.class);
        apiVersion = config.apiVersion;
        if (configure() && processDeviceInformation()) {
            pollingJob = executorService.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshDelay,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.ipAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing ipAddress/host configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            if (apiVersion == 2) {
                apiURL = String.format("https://%s/api/", config.ipAddress.trim());
            } else {
                apiURL = String.format("http://%s/api/", config.ipAddress.trim());
            }
            return true;
        }
    }

    private boolean processDeviceInformation() {
        final String deviceInformation;

        try {
            deviceInformation = getDeviceInformationData();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Device is offline or doesn't support the API version"));
            return false;
        }

        if (deviceInformation.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty data");
            return false;
        }

        var payload = gson.fromJson(deviceInformation, HomeWizardDeviceInformationPayload.class);
        if (payload == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to parse data response from device");
            return false;
        }

        if ("".equals(payload.getProductType())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Results from API are empty");
            return false;
        }

        if (!supportedTypes.contains(payload.getProductType())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Device is not compatible with this thing type");
            return false;
        }

        updateProperty("productName", payload.getProductName());
        updateProperty("productType", payload.getProductType());
        updateProperty("firmwareVersion", payload.getFirmwareVersion());

        return true;
    }

    /**
     * dispose: stop the poller
     */
    @Override
    public void dispose() {
        var job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        pollingJob = null;
    }

    /**
     *
     * Updates the state of the thing.
     *
     * @param groupID id of the channel, which was updated
     * @param channelID id of the channel, which was updated
     * @param state new state
     */
    protected void updateState(String groupID, String channelID, State state) {
        updateState(groupID + "#" + channelID, state);
    }

    /**
     * Device specific handling of the returned data.
     *
     * @param payload The data obtained form the API call
     */
    protected abstract void handleDataPayload(String data);

    /**
     * @return json response from the device information api
     * @throws IOException
     */
    public String getDeviceInformationData() throws IOException {
        return HttpUtil.executeUrl("GET", apiURL, 30000);
    }

    /**
     * @return json response from the measurement api
     * @throws IOException
     */
    public String getMeasurementData() throws IOException {
        if (apiVersion == 2) {
            return HttpUtil.executeUrl("GET", apiURL + "measurement", 30000);
        } else {
            return HttpUtil.executeUrl("GET", apiURL + "v1/data", 30000);
        }
    }

    protected void pollData() {
        final String measurementData;

        try {
            measurementData = getMeasurementData();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Device is offline or doesn't support the API version"));
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        handleDataPayload(measurementData);
    }

    /**
     * The actual polling loop
     */
    protected void pollingCode() {
        pollData();
    }
}
