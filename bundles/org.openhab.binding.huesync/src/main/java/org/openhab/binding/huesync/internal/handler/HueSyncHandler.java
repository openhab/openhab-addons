/*
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
package org.openhab.binding.huesync.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.huesync.internal.HdmiChannels;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDevice;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDetailed;
import org.openhab.binding.huesync.internal.api.dto.execution.HueSyncExecution;
import org.openhab.binding.huesync.internal.api.dto.hdmi.HueSyncHdmi;
import org.openhab.binding.huesync.internal.api.dto.hdmi.HueSyncHdmiConnectionInfo;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.exceptions.HueSyncApiException;
import org.openhab.binding.huesync.internal.exceptions.HueSyncConnectionException;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncRegistrationTask;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncUpdateTask;
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncUpdateTaskResult;
import org.openhab.binding.huesync.internal.types.HueSyncExceptionHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueSyncHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncHandler extends BaseThingHandler {

    /**
     * Exception handler implementation
     * 
     * @author Patrik Gfeller - Initial contribution
     * @author Patrik Gfeller - Issue #18062, improve connection exception handling.
     */
    private class ExceptionHandler implements HueSyncExceptionHandler {
        private final HueSyncHandler handler;

        private ExceptionHandler(HueSyncHandler handler) {
            this.handler = handler;
        }

        @Override
        public void handle(Exception exception) {
            ThingStatusDetail detail = ThingStatusDetail.COMMUNICATION_ERROR;
            String description;

            if (exception instanceof HueSyncConnectionException connectionException) {
                if (connectionException.getInnerException() instanceof HttpResponseException innerException) {
                    switch (innerException.getResponse().getStatus()) {
                        case HttpStatus.BAD_REQUEST_400 -> {
                            detail = ThingStatusDetail.CONFIGURATION_PENDING;
                        }
                        case HttpStatus.UNAUTHORIZED_401 -> {
                            detail = ThingStatusDetail.CONFIGURATION_ERROR;
                        }
                        default -> {
                            detail = ThingStatusDetail.COMMUNICATION_ERROR;
                        }
                    }
                }
                description = connectionException.getLocalizedMessage();
            } else {
                detail = ThingStatusDetail.COMMUNICATION_ERROR;
                description = exception.getLocalizedMessage();
            }

            ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.OFFLINE, detail, description);
            this.handler.thing.setStatusInfo(statusInfo);
        }
    }

    private static final String REGISTER = "Registration";
    private static final String POLL = "Update";

    private static final String PROPERTY_API_VERSION = "apiVersion";

    private final ExceptionHandler exceptionHandler;
    private final Logger logger = LoggerFactory.getLogger(HueSyncHandler.class);

    Map<String, @Nullable ScheduledFuture<?>> tasks = new HashMap<>();

    private Optional<HueSyncDevice> deviceInfo = Optional.empty();
    private Optional<HueSyncDeviceConnection> connection = Optional.empty();

    private final HttpClient httpClient;

    public HueSyncHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);

        this.updateStatus(ThingStatus.UNKNOWN);

        this.exceptionHandler = new ExceptionHandler(this);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    // #region override
    @Override
    protected Configuration editConfiguration() {
        this.logger.debug("Configuration change detected.");

        return new Configuration(this.thing.getConfiguration().getProperties());
    }
    // #endregion

    // #region private
    private Runnable initializeConnection() {
        return () -> {
            try {
                var connectionInstance = new HueSyncDeviceConnection(this.httpClient,
                        this.getConfigAs(HueSyncConfiguration.class), this.exceptionHandler);

                this.connection = Optional.of(connectionInstance);
                this.deviceInfo = Optional.ofNullable(connectionInstance.getDeviceInfo());

                this.deviceInfo.ifPresent(info -> {
                    connect(connectionInstance, info);
                });

            } catch (Exception e) {
                this.exceptionHandler.handle(e);
            }
        };
    }

    private void connect(HueSyncDeviceConnection connectionInstance, HueSyncDevice info) {
        setProperty(Thing.PROPERTY_SERIAL_NUMBER, info.uniqueId != null ? info.uniqueId : "");
        setProperty(Thing.PROPERTY_MODEL_ID, info.deviceType);
        setProperty(Thing.PROPERTY_FIRMWARE_VERSION, info.firmwareVersion);

        setProperty(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", info.apiLevel));

        try {
            this.checkCompatibility();
        } catch (HueSyncApiException e) {
            this.exceptionHandler.handle(e);
        } finally {
            this.startTasks(connectionInstance);
        }
    }

    private @Nullable ScheduledFuture<?> executeTask(Runnable task, long initialDelay, long interval) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    private synchronized void startTasks(HueSyncDeviceConnection connection) {
        this.stopTasks();

        connection.updateConfiguration(this.getConfigAs(HueSyncConfiguration.class));

        Runnable task = null;
        String id = connection.isRegistered() ? POLL : REGISTER;

        this.logger.debug("startTasks - [{}]", id);

        long initialDelay = 0;
        long interval = 0;

        switch (id) {
            case POLL -> {
                this.updateStatus(ThingStatus.ONLINE);

                initialDelay = HueSyncConstants.POLL_INITIAL_DELAY;

                interval = this.getConfigAs(HueSyncConfiguration.class).statusUpdateInterval;
                task = new HueSyncUpdateTask(connection, this.deviceInfo.get(),
                        deviceStatus -> this.handleUpdate(deviceStatus), this.exceptionHandler);
            }
            case REGISTER -> {
                initialDelay = HueSyncConstants.REGISTRATION_INITIAL_DELAY;
                interval = HueSyncConstants.REGISTRATION_INTERVAL;

                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "@text/thing.config.huesync.box.registration");

                task = new HueSyncRegistrationTask(connection, this.deviceInfo.get(),
                        registration -> this.handleRegistration(registration, connection), this.exceptionHandler);
            }
        }

        if (task != null) {
            logger.debug("Starting task [{}]", id);
            this.tasks.put(id, this.executeTask(task, initialDelay, interval));
        }
    }

    private synchronized void stopTasks() {
        logger.debug("Stopping {} task(s): {}", this.tasks.values().size(), String.join(",", this.tasks.keySet()));

        this.tasks.values().forEach(task -> this.stopTask(task));
        this.tasks.clear();

        this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "@text/thing.config.huesync.box.registration");
    }

    private synchronized void stopTask(@Nullable ScheduledFuture<?> task) {
        if (task == null || task.isCancelled() || task.isDone()) {
            return;
        }

        task.cancel(true);
    }

    private void handleUpdate(@Nullable HueSyncUpdateTaskResult dto) {
        synchronized (this) {
            ThingStatus status = this.thing.getStatus();

            switch (status) {
                case ONLINE:
                    Optional.ofNullable(dto).ifPresent(taskResult -> {
                        Optional.ofNullable(taskResult.deviceStatus)
                                .ifPresent(payload -> this.updateFirmwareInformation(payload));
                        Optional.ofNullable(taskResult.hdmiStatus)
                                .ifPresent(payload -> this.updateHdmiInformation(payload));
                        Optional.ofNullable(taskResult.execution)
                                .ifPresent(payload -> this.updateExecutionInformation(payload));
                    });
                    break;
                case OFFLINE:
                    this.stopTasks();

                    this.connection.ifPresent(connectionInstance -> {
                        this.deviceInfo.ifPresent(deviceInfoInstance -> {
                            this.connect(connectionInstance, deviceInfoInstance);
                        });
                    });
                    break;
                default:
                    this.logger.debug("Unable to execute update - Status: [{}]", status);
            }
        }
    }

    private void updateHdmiInformation(HueSyncHdmi hdmiStatus) {
        updateHdmiStatus(HueSyncConstants.CHANNELS.HDMI.IN_1, hdmiStatus.input1);
        updateHdmiStatus(HueSyncConstants.CHANNELS.HDMI.IN_2, hdmiStatus.input2);
        updateHdmiStatus(HueSyncConstants.CHANNELS.HDMI.IN_3, hdmiStatus.input3);
        updateHdmiStatus(HueSyncConstants.CHANNELS.HDMI.IN_4, hdmiStatus.input4);

        updateHdmiStatus(HueSyncConstants.CHANNELS.HDMI.OUT, hdmiStatus.output);
    }

    private void updateHdmiStatus(HdmiChannels channels, @Nullable HueSyncHdmiConnectionInfo hdmiStatusInfo) {
        if (hdmiStatusInfo != null) {
            this.updateState(channels.name, new StringType(hdmiStatusInfo.name));
            this.updateState(channels.type, new StringType(hdmiStatusInfo.type));
            this.updateState(channels.mode, new StringType(hdmiStatusInfo.lastSyncMode));
            this.updateState(channels.status, new StringType(hdmiStatusInfo.status));
        }
    }

    private void updateFirmwareInformation(HueSyncDeviceDetailed deviceStatus) {
        State firmwareState = new StringType(deviceStatus.firmwareVersion);
        State firmwareAvailableState = new StringType(
                deviceStatus.updatableFirmwareVersion != null ? deviceStatus.updatableFirmwareVersion
                        : deviceStatus.firmwareVersion);

        setProperty(Thing.PROPERTY_FIRMWARE_VERSION, deviceStatus.firmwareVersion);
        setProperty(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", deviceStatus.apiLevel));

        this.updateState(HueSyncConstants.CHANNELS.DEVICE.INFORMATION.FIRMWARE, firmwareState);
        this.updateState(HueSyncConstants.CHANNELS.DEVICE.INFORMATION.FIRMWARE_AVAILABLE, firmwareAvailableState);
    }

    private void updateExecutionInformation(HueSyncExecution executionStatus) {
        this.updateState(HueSyncConstants.CHANNELS.COMMANDS.MODE, new StringType(executionStatus.getMode()));
        this.updateState(HueSyncConstants.CHANNELS.COMMANDS.SYNC,
                executionStatus.syncActive ? OnOffType.ON : OnOffType.OFF);
        this.updateState(HueSyncConstants.CHANNELS.COMMANDS.HDMI,
                executionStatus.hdmiActive ? OnOffType.ON : OnOffType.OFF);
        this.updateState(HueSyncConstants.CHANNELS.COMMANDS.SOURCE, new StringType(executionStatus.hdmiSource));
        this.updateState(HueSyncConstants.CHANNELS.COMMANDS.BRIGHTNESS, new DecimalType(executionStatus.brightness));
    }

    private void handleRegistration(HueSyncRegistration registration, HueSyncDeviceConnection connection) {
        this.stopTasks();

        setProperty(HueSyncConstants.REGISTRATION_ID, registration.registrationId);

        Configuration configuration = this.editConfiguration();

        configuration.put(HueSyncConstants.REGISTRATION_ID, registration.registrationId);
        configuration.put(HueSyncConstants.API_TOKEN, registration.accessToken);

        this.updateConfiguration(configuration);

        this.startTasks(connection);
    }

    private void checkCompatibility() throws HueSyncApiException {
        try {
            HueSyncDevice deviceInformation = this.deviceInfo.orElseThrow();

            if (deviceInformation.apiLevel < HueSyncConstants.MINIMAL_API_VERSION) {
                throw new HueSyncApiException("@text/api.minimal-version");
            }
        } catch (NoSuchElementException e) {
            throw new HueSyncApiException("@text/api.communication-problem");
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

    @Override
    public void initialize() {
        try {
            this.stopTasks();
            this.updateStatus(ThingStatus.OFFLINE);

            scheduler.execute(initializeConnection());
        } catch (Exception e) {
            this.stopTasks();
            this.logger.warn("{}", e.getMessage());
            this.exceptionHandler.handle(e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (thing.getStatus() != ThingStatus.ONLINE || this.connection.isEmpty()) {
            this.logger.warn("Device status: {} - Command {} for channel {} will be ignored",
                    thing.getStatus().toString(), command.toFullString(), channelUID.toString());
            return;
        }

        Channel channel = thing.getChannel(channelUID);

        if (channel == null) {
            logger.error("Channel UID:{} does not exist - please report this as an issue", channelUID);
            return;
        }

        this.connection.get().executeCommand(channel, command);
    }

    @Override
    public void dispose() {
        synchronized (this) {
            super.dispose();

            try {
                this.stopTasks();
                this.connection.orElseThrow().dispose();
            } catch (Exception e) {
                this.logger.warn("{}", e.getMessage());
            } finally {
                this.logger.debug("Thing {} ({}) disposed.", this.thing.getLabel(), this.thing.getUID());
            }
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();

        if (this.connection.isPresent()) {
            this.connection.get().unregisterDevice();
        }
    }

    // #endregion
}
