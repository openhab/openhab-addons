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
package org.openhab.binding.huesync.internal.handler;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDetailedDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.connection.HueSyncConnection;
import org.openhab.binding.huesync.internal.exceptions.HueSyncApiException;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncRegistrationTask;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncUpdateTask;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;

/**
 * The {@link HueSyncHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncHandler extends BaseThingHandler {
    private static final String PROPERTY_API_VERSION = "apiVersion";
    private static final String PROPERTY_NETWORK_STATE = "networkState";

    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncHandler.class);

    private @Nullable ScheduledFuture<HueSyncRegistrationTask> deviceRegistrationTask;
    private @Nullable ScheduledFuture<HueSyncUpdateTask> deviceUpdateTask;

    private @Nullable HueSyncDeviceInfo deviceInfo;

    private HueSyncConnection connection;
    private HueSyncConfiguration config;

    protected class HueSyncProperties {

    };

    public HueSyncHandler(Thing thing, HttpClientFactory httpClientFactory) throws CertificateException, IOException {
        super(thing);

        HttpClient httpClient = httpClientFactory.getCommonHttpClient();

        this.config = getConfigAs(HueSyncConfiguration.class);
        this.connection = new HueSyncConnection(httpClient, this.config.host, this.config.port,
                this.config.apiAccessToken, this.config.registrationId);
    }

    // #region private
    private void stopTask(@Nullable ScheduledFuture<?> task) {
        try {
            if (task != null && !task.isDone()) {
                task.cancel(true);
            }
        } catch (Exception e) {
            // TODO: Handle exception ...
        } finally {
            task = null;
        }
    }

    private @Nullable ScheduledFuture<?> executeTask(Runnable task, long initialDelay, long interval) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private void startBackgroundTasks() {
        Optional.ofNullable(this.deviceInfo).ifPresent((device) -> {
            Runnable statusUpdateTask = new HueSyncUpdateTask(this.connection, device,
                    (deviceStatus) -> this.updateDeviceStatus(deviceStatus));

            if (this.connection.isRegistered()) {
                // TODO: Create helper to log nullable strings ...
                // this.logger.debug("Device {} {}:{} is already registered", device.name, device.deviceType,
                // device.uniqueId);

                this.startUpdateTask(statusUpdateTask);
            } else {
                // this.logger.info("Starting device registration for {} {}:{}", device.name, device.deviceType,
                // device.uniqueId);

                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/thing.config.huesync.box.registration");

                Runnable task = new HueSyncRegistrationTask(connection, device, () -> thing.getStatus(),
                        (registration) -> {
                            this.setRegistration(registration);
                            this.startUpdateTask(statusUpdateTask);
                        });

                this.deviceRegistrationTask = (ScheduledFuture<HueSyncRegistrationTask>) this.executeTask(task,
                        HueSyncConstants.REGISTRATION_INITIAL_DELAY, HueSyncConstants.REGISTRATION_INTERVAL);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void startUpdateTask(Runnable updateTask) {
        this.deviceUpdateTask = (ScheduledFuture<HueSyncUpdateTask>) this.executeTask(updateTask, 0,
                this.config.statusUpdateInterval);
    }

    private void updateDeviceStatus(@Nullable HueSyncDetailedDeviceInfo deviceState) {
        ThingStatus currentStatus = this.thing.getStatus();

        logger.trace("Current status: {}", currentStatus);

        if (deviceState == null) {
            this.updateStatus(ThingStatus.OFFLINE);
        } else {
            this.updateStatus(ThingStatus.ONLINE);
        }
    }

    private void setRegistration(HueSyncRegistration registration) {
        if (registration.registrationId != null && registration.accessToken != null) {

            this.stopTask(deviceRegistrationTask);

            addProperty(HueSyncConstants.REGISTRATION_ID, registration.registrationId);

            Configuration configuration = this.editConfiguration();

            configuration.put(HueSyncConstants.REGISTRATION_ID, this.config.registrationId);
            configuration.put(HueSyncConstants.API_TOKEN, this.config.apiAccessToken);

            this.updateConfiguration(configuration);
            this.updateStatus(ThingStatus.ONLINE);

            this.logger.info("Device registration for {} complete - Id: {}",
                    this.deviceInfo != null
                            ? this.deviceInfo.name != null ? this.deviceInfo.name : "⚠️ unknown device ⚠️"
                            : "⚠️ unknown device ⚠️",

                    registration.registrationId);
        }
    }

    private void checkCompatibility() throws HueSyncApiException {
        if (this.deviceInfo != null && this.deviceInfo.apiLevel < HueSyncConstants.MINIMAL_API_VERSION) {
            throw new HueSyncApiException("@text/api.minimal-version", this.logger);
        }
    }

    private void addProperty(String key, @Nullable String value) {
        if (value != null) {
            Map<String, String> properties = this.editProperties();

            properties.put(key, value);

            this.updateProperties(properties);
        }
    }

    // #endregion

    // #region Override
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                this.deviceInfo = this.connection.getDeviceInfo();

                Optional.ofNullable(this.deviceInfo).ifPresent((info) -> {
                    // Redundant null check required to avoid warning during build/development ...
                    if (info != null) {
                        addProperty(Thing.PROPERTY_SERIAL_NUMBER, info.uniqueId);
                        addProperty(Thing.PROPERTY_MODEL_ID, info.deviceType);
                        addProperty(Thing.PROPERTY_FIRMWARE_VERSION, info.firmwareVersion);

                        addProperty(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", info.apiLevel));
                        addProperty(HueSyncHandler.PROPERTY_NETWORK_STATE, info.wifiState);
                    }

                    try {
                        this.checkCompatibility();
                        this.startBackgroundTasks();
                    } catch (HueSyncApiException e) {
                        this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                });

            } catch (Exception e) {
                this.logger.error("{}", e.getMessage());
                this.updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO: Implementation ...
    }

    @Override
    public void dispose() {
        super.dispose();

        try {
            this.connection.stop();

            this.stopTask(deviceRegistrationTask);
            this.stopTask(deviceUpdateTask);
        } catch (Exception e) {
            this.logger.error("{}", e.getMessage());
        } finally {
            this.logger.info("Thing {} ({}) disposed.", this.thing.getLabel(), this.thing.getUID());
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();

        if (this.connection != null && !this.connection.unregisterDevice()) {
            this.logger.error(
                    "It was not possible to unregister {} ({}). You may use id: {} Key: {} to manually re-configure the thing, or to manually remove the device via API.",
                    this.thing.getLabel(), this.thing.getUID(), this.config.registrationId, this.config.apiAccessToken);
        }
    }

    // #endregion
}
