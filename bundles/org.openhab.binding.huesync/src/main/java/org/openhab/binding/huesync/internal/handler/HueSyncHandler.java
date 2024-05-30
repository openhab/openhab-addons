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
import org.openhab.binding.huesync.internal.handler.tasks.HueSyncUpdateInfo;
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
    private static final String JOB_REGISTRATION = "Registration";
    private static final String JOB_UPDATE = "Update";

    private static final String PROPERTY_API_VERSION = "apiVersion";

    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncHandler.class);

    Map<String, @Nullable ScheduledFuture<?>> tasks = new HashMap<>();

    private @Nullable HueSyncDeviceDto deviceInfo;
    private @Nullable HueSyncDeviceConnection connection;

    private HueSyncConfiguration config;
    private HttpClient httpClient;

    public HueSyncHandler(Thing thing, HttpClientFactory httpClientFactory)
            throws CertificateException, IOException, URISyntaxException {
        super(thing);

        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.httpClient.setName(this.thing.getUID().getAsString());

        this.config = getConfigAs(HueSyncConfiguration.class);
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

    private void startBackgroundTasks(HueSyncDeviceDto device, HueSyncDeviceConnection connection) {
        Runnable update = new HueSyncUpdateTask(connection, device, (deviceStatus) -> this.updateStatus(deviceStatus));
        Runnable register = new HueSyncRegistrationTask(connection, device, () -> thing.getStatus(), (registration) -> {
            this.setRegistration(registration);
        });

        this.tasks.put(JOB_REGISTRATION, this.executeTask(register, HueSyncConstants.REGISTRATION_INITIAL_DELAY,
                HueSyncConstants.REGISTRATION_INTERVAL));
        this.tasks.put(JOB_UPDATE, this.executeTask(update, 0, this.config.statusUpdateInterval));
    }

    private void updateStatus(@Nullable HueSyncUpdateInfo update) {
        ThingStatus currentStatus = this.thing.getStatus();

        logger.trace("Current status: {}", currentStatus);

        @SuppressWarnings("null")
        HueSyncDeviceDtoDetailed deviceStatus = update.deviceStatus;

        if (deviceStatus == null) {
            this.updateStatus(ThingStatus.OFFLINE);
        } else {
            this.updateStatus(ThingStatus.ONLINE);

            this.updateFirmwareInformation(deviceStatus);
            this.updateHdmiInformation(update.hdmiStatus);
            this.updateExecutionnformation(update.execution);
        }
    }

    @SuppressWarnings("null")
    private void updateHdmiInformation(@Nullable HueSyncHdmiDto hdmiStatus) {
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
                Optional.ofNullable(deviceStatus.updatableFirmwareVersion).isPresent()
                        ? deviceStatus.updatableFirmwareVersion
                        : deviceStatus.firmwareVersion);

        setProperty(Thing.PROPERTY_FIRMWARE_VERSION, deviceStatus.firmwareVersion);
        setProperty(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", deviceStatus.apiLevel));

        this.updateState(HueSyncConstants.CHANNELS.DEVICE.INFORMATION.FIRMWARE, firmwareState);
        this.updateState(HueSyncConstants.CHANNELS.DEVICE.INFORMATION.FIRMWARE_AVAILABLE, firmwareAvailableState);
    }

    @SuppressWarnings("null")
    private void updateExecutionnformation(@Nullable HueSyncExecutionDto executionStatus) {
        this.updateState(HueSyncConstants.CHANNELS.COMMANDS.MODE, new StringType(executionStatus.getMode()));
    }

    private void setRegistration(HueSyncRegistrationDto registration) {
        Optional<String> id = Optional.ofNullable(registration.registrationId);
        Optional<String> token = Optional.ofNullable(registration.accessToken);

        if (id.isPresent() && token.isPresent()) {
            this.stopTask(this.tasks.get(JOB_REGISTRATION));

            setProperty(HueSyncConstants.REGISTRATION_ID, id.get());

            Configuration configuration = this.editConfiguration();
            configuration.put(HueSyncConstants.REGISTRATION_ID, id.get());
            configuration.put(HueSyncConstants.API_TOKEN, token.get());
            this.updateConfiguration(configuration);

            String deviceName = "⚠️ unknown device ⚠️";
            Optional<HueSyncDeviceDto> deviceInfo = Optional.ofNullable(this.deviceInfo);

            if (deviceInfo.isPresent()) {
                deviceName = Optional.ofNullable(deviceInfo.get().name).orElse(deviceName);
            }

            this.logger.info("Device registration for {} complete - Id: {}", deviceName, id.get());
        }
    }

    private void checkCompatibility() throws HueSyncApiException {
        try {
            HueSyncDeviceDto info = Optional.ofNullable(this.deviceInfo).orElseThrow();
            if (info.apiLevel < HueSyncConstants.MINIMAL_API_VERSION) {
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
    @Override
    public void initialize() {
        try {
            updateStatus(ThingStatus.UNKNOWN);

            this.tasks.values().forEach(task -> {
                this.stopTask(task);
            });

            this.connection = new HueSyncDeviceConnection(httpClient, this.config.host, this.config.port);

            Optional.ofNullable(this.connection).ifPresent((connection) -> {
                this.config = getConfigAs(HueSyncConfiguration.class);

                Optional.ofNullable(this.connection).ifPresent((config) -> {
                    connection.updateConfig(this.config);

                    if (!connection.isRegistered()) {
                        this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "@text/thing.config.huesync.box.registration");
                    }

                    scheduler.execute(() -> {
                        try {
                            this.deviceInfo = connection.getDeviceInfo();
                            Optional.ofNullable(this.deviceInfo).ifPresent((info) -> {
                                setProperty(Thing.PROPERTY_SERIAL_NUMBER, info.uniqueId);
                                setProperty(Thing.PROPERTY_MODEL_ID, info.deviceType);

                                setProperty(Thing.PROPERTY_FIRMWARE_VERSION, info.firmwareVersion);
                                setProperty(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", info.apiLevel));

                                try {
                                    this.checkCompatibility();
                                    this.startBackgroundTasks(info, connection);
                                } catch (HueSyncApiException e) {
                                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                            e.getMessage());
                                }
                            });

                        } catch (Exception e) {
                            this.logger.error("{}", e.getMessage());
                            this.updateStatus(ThingStatus.OFFLINE);
                        }
                    });
                });

            });
            // throw new HueSyncConnectionException("@text/exception.generic.connection",
            // HueSyncConnectionExceptionType.GENERIC, this.logger);
        } catch (Exception e) {
            this.logger.error("{}", e.getMessage());
            this.updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.logger.info("Channel UID: {} - Command: {}", channelUID.getAsString(), command.toFullString());
    }

    @Override
    public void dispose() {
        super.dispose();

        try {
            Optional.ofNullable(this.connection).ifPresent((connection) -> {
                connection.dispose();
            });

            this.connection = null;

            this.tasks.values().forEach((task) -> {
                this.stopTask(task);
            });
        } catch (Exception e) {
            this.logger.error("{}", e.getMessage());
        } finally {
            this.logger.info("Thing {} ({}) disposed.", this.thing.getLabel(), this.thing.getUID());
            this.connection = null;
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();

        Optional.ofNullable(this.connection).ifPresent((connection) -> {
            if (connection.unregisterDevice()) {
                this.logger.error(
                        "It was not possible to unregister {} ({}). You may use id: {} Key: {} to manually re-configure the thing, or to manually remove the device via API.",
                        this.thing.getLabel(), this.thing.getUID(), this.config.registrationId,
                        this.config.apiAccessToken);
            }
        });
    }

    // #endregion
}
