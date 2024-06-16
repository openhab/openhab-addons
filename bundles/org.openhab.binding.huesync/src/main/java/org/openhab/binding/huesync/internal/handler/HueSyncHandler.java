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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDto;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDtoDetailed;
import org.openhab.binding.huesync.internal.api.dto.execution.HueSyncExecutionDto;
import org.openhab.binding.huesync.internal.api.dto.hdmi.HueSyncHdmiDto;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationDto;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.exceptions.HueSyncApiException;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncRegistrationTask;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncUpdateTask;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncUpdateTaskResultDto;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;

/**
 * The {@link HueSyncHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncHandler extends BaseThingHandler {
    private static final String REGISTER = "Registration";
    private static final String POLL = "Update";

    private static final String PROPERTY_API_VERSION = "apiVersion";

    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncHandler.class);

    Map<String, @Nullable ScheduledFuture<?>> tasks = new HashMap<>();

    private Optional<HueSyncDeviceDto> deviceInfo = Optional.empty();
    private HueSyncDeviceConnection connection;

    private HttpClient httpClient;

    public HueSyncHandler(Thing thing, HttpClientFactory httpClientFactory)
            throws CertificateException, IOException, URISyntaxException {
        super(thing);

        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.httpClient.setName(this.thing.getUID().getAsString());

        this.connection = new HueSyncDeviceConnection(this.httpClient, this.getConfigAs(HueSyncConfiguration.class));
    }

    // #region private

    @SuppressWarnings("null")
    private Runnable initializeConnection() {
        return () -> {
            this.deviceInfo = Optional.ofNullable(this.connection.getDeviceInfo());
            this.deviceInfo.ifPresent(info -> {
                setProperty(Thing.PROPERTY_SERIAL_NUMBER, info.uniqueId != null ? info.uniqueId : "");
                setProperty(Thing.PROPERTY_MODEL_ID, info.deviceType);
                setProperty(Thing.PROPERTY_FIRMWARE_VERSION, info.firmwareVersion);

                setProperty(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", info.apiLevel));

                try {
                    this.checkCompatibility();
                } catch (HueSyncApiException e) {
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                } finally {
                    this.startTasks();
                }
            });
        };
    }

    private void stopTask(@Nullable ScheduledFuture<?> task) {
        if (task == null || task.isCancelled() || task.isDone()) {
            return;
        }

        task.cancel(true);
    }

    private @Nullable ScheduledFuture<?> executeTask(Runnable task, long initialDelay, long interval) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    // TODO: Life cycle handling for connection should resolve complex null problem (➡️ mode
    // constructor)
    private void startTasks() {
        this.stopTasks();

        this.connection.updateConfiguration(this.getConfigAs(HueSyncConfiguration.class));

        Runnable task = null;
        String id = this.connection.isRegistered() ? POLL : REGISTER;

        long initialDelay = 0;
        long interval = 0;

        switch (id) {
            case POLL:
                initialDelay = 0;
                interval = this.getConfigAs(HueSyncConfiguration.class).statusUpdateInterval;

                this.updateStatus(ThingStatus.ONLINE);

                task = new HueSyncUpdateTask(this.connection, this.deviceInfo.get(),
                        deviceStatus -> this.handleUpdate(deviceStatus));

                break;
            case REGISTER:
                initialDelay = HueSyncConstants.REGISTRATION_INITIAL_DELAY;
                interval = HueSyncConstants.REGISTRATION_INTERVAL;

                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/thing.config.huesync.box.registration");

                task = new HueSyncRegistrationTask(this.connection, this.deviceInfo.get(),
                        registration -> this.handleRegistration(registration));

                break;
        }

        if (task != null) {
            logger.trace("Starting task [{}]", id);
            this.tasks.put(id, this.executeTask(task, initialDelay, interval));
        }
    }

    private void stopTasks() {
        logger.trace("Stopping {} task(s): {}", this.tasks.values().size(), String.join(",", this.tasks.keySet()));

        this.tasks.values().forEach(task -> this.stopTask(task));
        this.tasks.clear();
    }

    @SuppressWarnings("null")
    private void handleUpdate(@Nullable HueSyncUpdateTaskResultDto dto) {
        try {
            HueSyncUpdateTaskResultDto update = Optional.ofNullable(dto).get();

            try {
                this.updateFirmwareInformation(Optional.ofNullable(update.deviceStatus).get());
            } catch (NoSuchElementException e) {
                this.logMissingUpdateInformation("device");
            }

            try {
                this.updateHdmiInformation(Optional.ofNullable(update.hdmiStatus).get());
            } catch (NoSuchElementException e) {
                this.logMissingUpdateInformation("hdmi");
            }

            try {
                this.updateExecutionInformation(Optional.ofNullable(update.execution).get());
            } catch (NoSuchElementException e) {
                this.logMissingUpdateInformation("execution");
            }
        } catch (NoSuchElementException e) {
            this.startTasks();
        }
    }

    private void logMissingUpdateInformation(String api) {
        this.logger.warn("⚠️ Device information - {} status missing ⚠️", api);
    }

    @SuppressWarnings("null")
    private void updateHdmiInformation(HueSyncHdmiDto hdmiStatus) {
        // TODO: Resolve warnings ➡️ consider to encapsulate hdmi status obj to avoid complex null
        // handling ...
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_1.NAME, new StringType(hdmiStatus.input1.name));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_1.TYPE, new StringType(hdmiStatus.input1.type));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_1.STATUS, new StringType(hdmiStatus.input1.status));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_1.MODE, new StringType(hdmiStatus.input1.lastSyncMode));

        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_2.NAME, new StringType(hdmiStatus.input2.name));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_2.TYPE, new StringType(hdmiStatus.input2.type));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_2.STATUS, new StringType(hdmiStatus.input2.status));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_2.MODE, new StringType(hdmiStatus.input2.lastSyncMode));

        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_3.NAME, new StringType(hdmiStatus.input3.name));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_3.TYPE, new StringType(hdmiStatus.input3.type));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_3.STATUS, new StringType(hdmiStatus.input3.status));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_3.MODE, new StringType(hdmiStatus.input3.lastSyncMode));

        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_4.NAME, new StringType(hdmiStatus.input4.name));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_4.TYPE, new StringType(hdmiStatus.input4.type));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_4.STATUS, new StringType(hdmiStatus.input4.status));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.IN_4.MODE, new StringType(hdmiStatus.input4.lastSyncMode));

        this.updateState(HueSyncConstants.CHANNELS.HDMI.OUT.NAME, new StringType(hdmiStatus.output.name));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.OUT.TYPE, new StringType(hdmiStatus.output.type));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.OUT.STATUS, new StringType(hdmiStatus.output.status));
        this.updateState(HueSyncConstants.CHANNELS.HDMI.OUT.MODE, new StringType(hdmiStatus.output.lastSyncMode));
    }

    private void updateFirmwareInformation(HueSyncDeviceDtoDetailed deviceStatus) {
        State firmwareState = new StringType(deviceStatus.firmwareVersion);
        State firmwareAvailableState = new StringType(
                deviceStatus.updatableFirmwareVersion != null ? deviceStatus.updatableFirmwareVersion
                        : deviceStatus.firmwareVersion);

        setProperty(Thing.PROPERTY_FIRMWARE_VERSION, deviceStatus.firmwareVersion);
        setProperty(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", deviceStatus.apiLevel));

        this.updateState(HueSyncConstants.CHANNELS.DEVICE.INFORMATION.FIRMWARE, firmwareState);
        this.updateState(HueSyncConstants.CHANNELS.DEVICE.INFORMATION.FIRMWARE_AVAILABLE, firmwareAvailableState);
    }

    private void updateExecutionInformation(HueSyncExecutionDto executionStatus) {
        this.updateState(HueSyncConstants.CHANNELS.COMMANDS.MODE, new StringType(executionStatus.getMode()));
    }

    private void handleRegistration(HueSyncRegistrationDto registration) {
        this.stopTasks();

        setProperty(HueSyncConstants.REGISTRATION_ID, registration.registrationId);

        Configuration configuration = this.editConfiguration();

        configuration.put(HueSyncConstants.REGISTRATION_ID, registration.registrationId);
        configuration.put(HueSyncConstants.API_TOKEN, registration.accessToken);

        this.updateConfiguration(configuration);

        this.startTasks();
    }

    private void checkCompatibility() throws HueSyncApiException {
        try {
            HueSyncDeviceDto deviceInformation = this.deviceInfo.orElseThrow();

            if (deviceInformation.apiLevel < HueSyncConstants.MINIMAL_API_VERSION) {
                throw new HueSyncApiException("@text/api.minimal-version", this.logger);
            }
        } catch (NoSuchElementException e) {
            throw new HueSyncApiException("@text/api.communication-problem", logger);
        }
    }

    private void setProperty(String key, @Nullable String value) {
        if (value != null) {
            Map<String, String> properties = this.editProperties();

            if (properties.containsKey(key)) {
                @Nullable
                String currentValue = properties.get(key);
                if (!(value.equals(currentValue))) {
                    saveProperty(key, value, properties);
                }
            } else {
                saveProperty(key, value, properties);
            }
        }
    }

    private void saveProperty(String key, String value, Map<String, String> properties) {
        properties.put(key, value);
        this.updateProperties(properties);
    }

    // #endregion

    // #region Override
    // TODO: Life cycle handling for connection should resolve complex null problem (➡️ mode
    // constructor)
    @Override
    public void initialize() {
        try {
            updateStatus(ThingStatus.UNKNOWN);

            this.stopTasks();

            scheduler.execute(initializeConnection());
        } catch (Exception e) {
            this.logger.error("{}", e.getMessage());

            this.updateStatus(ThingStatus.OFFLINE);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.logger.trace("Channel UID: {} - Command: {}", channelUID.getAsString(), command.toFullString());

        if (thing.getStatus() != ThingStatus.ONLINE) {
            this.logger.warn("Device status: {} ➡️ Command {} for chanel {} will be ignored",
                    thing.getStatus().toString(), command.toFullString(), channelUID.toString());
            return;
        }

        Channel channel = thing.getChannel(channelUID);

        if (channel == null) {
            logger.error("Channel UID:{} does not exist - please report this as an issue", channelUID);
            return;
        }

        if (RefreshType.REFRESH.equals(command)) {
            this.logger.trace("Channel UID: {} - Command: {}", channelUID.getAsString(), command.toFullString());
            return;
        }
        String commandId = channel.getUID().getId();

        // TODO: Consider to move this code to the connection (do not expose command executors ...)
        if (this.connection.DeviceCommandsExecutors.containsKey(commandId)) {
            this.connection.DeviceCommandsExecutors.get(commandId).accept(command);
        } else {
            this.logger.error("No executor registered for command {} - please report this as an issue", commandId);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        try {
            this.stopTasks();
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

        this.connection.unregisterDevice();
    }

    // #endregion
}
