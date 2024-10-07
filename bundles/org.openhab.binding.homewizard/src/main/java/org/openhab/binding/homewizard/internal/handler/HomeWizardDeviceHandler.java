/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homewizard.internal.HomeWizardConfiguration;
import org.openhab.binding.homewizard.internal.dto.DataPayload;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
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
 */
@NonNullByDefault
public abstract class HomeWizardDeviceHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(HomeWizardDeviceHandler.class);
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    protected ScheduledExecutorService executorService = this.scheduler;
    private HomeWizardConfiguration config = new HomeWizardConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;

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
        if (configure()) {
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
            apiURL = String.format("http://%s/api/v1/", config.ipAddress.trim());
            return true;
        }
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
     * Device specific handling of the returned data payload.
     *
     * @param payload The data parsed from the data Json file
     */
    protected abstract void handleDataPayload(DataPayload payload);

    /**
     * @return json response from the remote server
     * @throws IOException
     */
    public String getData() throws IOException {
        return HttpUtil.executeUrl("GET", apiURL + "data", 30000);
    }

    /**
     *
     */
    protected void pollData() {
        final String dataResult;

        try {
            dataResult = getData();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query device data: %s", e.getMessage()));
            return;
        }

        if (dataResult.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty data");
            return;
        }

        DataPayload dataPayload = gson.fromJson(dataResult, DataPayload.class);
        if (dataPayload == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to parse data response from device");
            return;
        }

        if ("".equals(dataPayload.getWifiSsid())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Results from API are empty");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        handleDataPayload(dataPayload);
    }

    /**
     * The actual polling loop
     */
    protected void pollingCode() {
        pollData();
    }
}
