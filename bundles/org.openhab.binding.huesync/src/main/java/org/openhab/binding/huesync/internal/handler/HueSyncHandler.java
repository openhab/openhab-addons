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
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.NoSuchElementException;
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
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.exceptions.HueSyncApiException;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncRegistrationTask;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncUpdateTask;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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

    private HueSyncDeviceConnection connection;
    private HueSyncConfiguration config;

    protected class HueSyncProperties {

    };

    public HueSyncHandler(Thing thing, HttpClientFactory httpClientFactory)
            throws CertificateException, IOException, URISyntaxException {
        super(thing);

        HttpClient httpClient = httpClientFactory.getCommonHttpClient();
        httpClient.setName(this.thing.getUID().getAsString());

        this.config = getConfigAs(HueSyncConfiguration.class);
        this.connection = new HueSyncDeviceConnection(httpClient, this.config.host, this.config.port);
    }

    // #region private
    private void stopTask(@Nullable ScheduledFuture<?> task) {
        try {
            Optional.ofNullable(task).ifPresent((job) -> {
                if (!job.isCancelled() && !job.isDone()) {
                    job.cancel(true);
                }
            });
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
                this.logger.debug("Device {} {}:{} is already registered", device.name, device.deviceType,
                        device.uniqueId);

                this.startUpdateTask(statusUpdateTask);
            } else {
                this.logger.info("Starting device registration for {} {}:{}", device.name, device.deviceType,
                        device.uniqueId);

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

            State firmwareState = new StringType(deviceState.firmwareVersion);
            State firmwareAvailableState = new StringType(
                    Optional.ofNullable(deviceState.updatableFirmwareVersion).isPresent()
                            ? deviceState.updatableFirmwareVersion
                            : deviceState.firmwareVersion);

            this.updateState(HueSyncConstants.CHANNELS.DEVICE.INFORMATION.FIRMWARE, firmwareState);
            this.updateState(HueSyncConstants.CHANNELS.DEVICE.INFORMATION.FIRMWARE_AVAILABLE, firmwareAvailableState);
        }
    }

    private void setRegistration(HueSyncRegistration registration) {
        Optional<String> id = Optional.ofNullable(registration.registrationId);
        Optional<String> token = Optional.ofNullable(registration.accessToken);

        if (id.isPresent() && token.isPresent()) {
            this.stopTask(deviceRegistrationTask);

            addProperty(HueSyncConstants.REGISTRATION_ID, id.get());

            Configuration configuration = this.editConfiguration();
            configuration.put(HueSyncConstants.REGISTRATION_ID, id.get());
            configuration.put(HueSyncConstants.REGISTRATION_ID, token.get());
            this.updateConfiguration(configuration);

            String deviceName = "⚠️ unknown device ⚠️";
            Optional<HueSyncDeviceInfo> deviceInfo = Optional.ofNullable(this.deviceInfo);

            if (deviceInfo.isPresent()) {
                deviceName = Optional.ofNullable(deviceInfo.get().name).orElse(deviceName);
            }

            this.logger.info("Device registration for {} complete - Id: {}", deviceName, id.get());
        }
    }

    private void checkCompatibility() throws HueSyncApiException {
        try {
            HueSyncDeviceInfo info = Optional.ofNullable(this.deviceInfo).orElseThrow();
            if (info.apiLevel < HueSyncConstants.MINIMAL_API_VERSION) {
                throw new HueSyncApiException("@text/api.minimal-version", this.logger);
            }
        } catch (NoSuchElementException e) {
            throw new HueSyncApiException("@text/api.communication-problem", logger);
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
        // TODO: Check if we need to handle enable/disable state ...

        updateStatus(ThingStatus.UNKNOWN);

        this.stopTask(this.deviceRegistrationTask);
        this.stopTask(this.deviceUpdateTask);

        this.config = getConfigAs(HueSyncConfiguration.class);
        this.connection.updateConfig(this.config);

        scheduler.execute(() -> {
            try {
                this.deviceInfo = this.connection.getDeviceInfo();

                Optional.ofNullable(this.deviceInfo).ifPresent((info) -> {
                    addProperty(Thing.PROPERTY_SERIAL_NUMBER, info.uniqueId);
                    addProperty(Thing.PROPERTY_MODEL_ID, info.deviceType);
                    addProperty(Thing.PROPERTY_FIRMWARE_VERSION, info.firmwareVersion);

                    addProperty(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", info.apiLevel));
                    addProperty(HueSyncHandler.PROPERTY_NETWORK_STATE, info.wifiState);
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
            this.stopTask(deviceRegistrationTask);
            this.stopTask(deviceUpdateTask);

            this.connection.dispose();
        } catch (Exception e) {
            this.logger.error("{}", e.getMessage());
        } finally {
            this.logger.info("Thing {} ({}) disposed.", this.thing.getLabel(), this.thing.getUID());
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();

        if (this.connection.unregisterDevice()) {
            this.logger.error(
                    "It was not possible to unregister {} ({}). You may use id: {} Key: {} to manually re-configure the thing, or to manually remove the device via API.",
                    this.thing.getLabel(), this.thing.getUID(), this.config.registrationId, this.config.apiAccessToken);
        }
    }

    // #endregion
}
