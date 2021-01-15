/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homewizard.data.P1Payload;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link HomeWizardHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class HomeWizardHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HomeWizardHandler.class);

    private HomeWizardConfiguration config = new HomeWizardConfiguration();

    private final Lock pollingJobLock = new ReentrantLock();
    private @Nullable ScheduledFuture<?> pollingJob;
    private int currentRefreshDelay = 0;

    private final GsonBuilder builder = new GsonBuilder();
    private final Gson gson = builder.create();

    private final Lock configurationLock = new ReentrantLock(); // to protected fields accessed in init/dispose AND the
                                                                // poller
    private String apiURL = "";

    private String meterModel = "";
    private int meterVersion = 0;

    /**
     * Constructor
     *
     * @param thing The thing to handle
     */
    public HomeWizardHandler(Thing thing) {
        super(thing);
    }

    /**
     * Not listening to any commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(HomeWizardConfiguration.class);

        if (configure()) {
            stopPolling(true);
            startPolling();
        }
    }

    /**
     * Handle updates to the configuration gracefully
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        Object cfgObject = configurationParameters.get("refreshDelay");
        if (cfgObject != null) {
            BigDecimal bd = (BigDecimal) cfgObject;
            config.refreshDelay = bd.intValue();
        }

        cfgObject = configurationParameters.get("ipAddress");
        if (cfgObject != null) {
            config.ipAddress = (String) cfgObject;
        }

        if (configure()) {
            // If the new configuration is proper, stop the poller if needed
            stopPolling(true);
            // Then start it again if it was stopped
            startPolling();
        } else {
            // Stop polling if the new config is invalid
            stopPolling(false);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.ipAddress.trim().isEmpty()) {
            // since it is marked as required, initialize() should not be called if this field is empty
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing ipAddress/host configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);

            try {
                configurationLock.lock();
                apiURL = String.format("http://%s/api/v1/data", config.ipAddress.trim());
            } finally {
                configurationLock.unlock();
            }
            return true;
        }
    }

    /**
     * Stop the poller unconditionally
     */
    @Override
    public void dispose() {
        stopPolling(false);
    }

    /**
     * Stop the polling job
     *
     * @param onlyIfNeeded if true polling will only actually stop if a new refresh interval should be set
     */
    private void stopPolling(boolean onlyIfNeeded) {
        try {
            pollingJobLock.lock();
            if (pollingJob != null && !pollingJob.isCancelled()) {
                if (!onlyIfNeeded || config.refreshDelay != currentRefreshDelay) {
                    if (pollingJob != null) {
                        pollingJob.cancel(true);
                    }
                    pollingJob = null;
                }
            }
        } finally {
            pollingJobLock.unlock();
        }
    }

    /**
     * Start a polling job if it is not already running.
     */
    private void startPolling() {
        try {
            pollingJobLock.lock();
            boolean startPoller = false;
            if (pollingJob != null) {
                startPoller = pollingJob.isCancelled();
            } else {
                startPoller = true;
            }

            if (startPoller) {
                currentRefreshDelay = config.refreshDelay;
                pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, currentRefreshDelay,
                        TimeUnit.SECONDS);
            }
        } finally {
            pollingJobLock.unlock();
        }
    }

    /**
     * The actual polling loop
     */
    private void pollingCode() {
        final String query;
        final String result;

        // get a local copy of the configuration parameters
        try {
            configurationLock.lock();
            query = apiURL;
        } catch (Exception e) {
            return;
        } finally {
            configurationLock.unlock();
        }

        try {
            result = HttpUtil.executeUrl("GET", query, 750);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to query P1 Meter");
            return;
        }

        if (result.trim().isEmpty()) {
            logger.warn("P1 Wi-Fi meter API at URI {} returned empty result", apiURL);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "P1 Meter API returned empty status");
            return;
        }

        P1Payload payload = gson.fromJson(result, P1Payload.class);
        if (payload != null) {
            if (payload.getMeter_model() == "") {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Results from API seem empty");
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            if (meterModel != payload.getMeter_model()) {
                meterModel = payload.getMeter_model();
                updateProperty(HomeWizardBindingConstants.PROPERTY_METER_MODEL, meterModel);
            }

            if (meterVersion != payload.getSmr_version()) {
                meterVersion = payload.getSmr_version();
                updateProperty(HomeWizardBindingConstants.PROPERTY_METER_VERSION, String.format("%d", meterVersion));
            }

            updateState(HomeWizardBindingConstants.CHANNEL_POWER_IMPORT_T1,
                    new QuantityType<>(payload.getTotal_power_import_t1_kwh(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_POWER_IMPORT_T2,
                    new QuantityType<>(payload.getTotal_power_import_t2_kwh(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_POWER_EXPORT_T1,
                    new QuantityType<>(payload.getTotal_power_export_t1_kwh(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_POWER_EXPORT_T2,
                    new QuantityType<>(payload.getTotal_power_export_t2_kwh(), Units.WATT));

            updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER,
                    new QuantityType<>(payload.getActive_power_w(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L1,
                    new QuantityType<>(payload.getActive_power_l1_w(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L2,
                    new QuantityType<>(payload.getActive_power_l2_w(), Units.WATT));
            updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L3,
                    new QuantityType<>(payload.getActive_power_l3_w(), Units.WATT));

            updateState(HomeWizardBindingConstants.CHANNEL_TOTAL_GAS,
                    new QuantityType<>(payload.getTotal_gas_m3(), Units.ONE)); // could convert, 1m^3 = 1000 liters
            updateState(HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP,
                    new QuantityType<>(payload.getGas_timestamp(), Units.SECOND));
        }
    }
}
