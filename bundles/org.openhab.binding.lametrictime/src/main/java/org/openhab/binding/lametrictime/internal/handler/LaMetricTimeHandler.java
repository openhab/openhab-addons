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
package org.openhab.binding.lametrictime.internal.handler;

import static org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants.*;
import static org.openhab.binding.lametrictime.internal.config.LaMetricTimeConfiguration.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants;
import org.openhab.binding.lametrictime.internal.LaMetricTimeConfigStatusMessage;
import org.openhab.binding.lametrictime.internal.LaMetricTimeUtil;
import org.openhab.binding.lametrictime.internal.StateDescriptionOptionsProvider;
import org.openhab.binding.lametrictime.internal.WidgetRef;
import org.openhab.binding.lametrictime.internal.api.Configuration;
import org.openhab.binding.lametrictime.internal.api.LaMetricTime;
import org.openhab.binding.lametrictime.internal.api.dto.enums.BrightnessMode;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActivationException;
import org.openhab.binding.lametrictime.internal.api.local.LaMetricTimeLocal;
import org.openhab.binding.lametrictime.internal.api.local.NotificationCreationException;
import org.openhab.binding.lametrictime.internal.api.local.UpdateException;
import org.openhab.binding.lametrictime.internal.api.local.dto.Application;
import org.openhab.binding.lametrictime.internal.api.local.dto.Audio;
import org.openhab.binding.lametrictime.internal.api.local.dto.Bluetooth;
import org.openhab.binding.lametrictime.internal.api.local.dto.Device;
import org.openhab.binding.lametrictime.internal.api.local.dto.Display;
import org.openhab.binding.lametrictime.internal.api.local.dto.Widget;
import org.openhab.binding.lametrictime.internal.config.LaMetricTimeConfiguration;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LaMetricTimeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gregory Moyer - Initial contribution
 * @author Kai Kreuzer - Improved status handling, introduced refresh job and app state update
 */
@NonNullByDefault
public class LaMetricTimeHandler extends ConfigStatusBridgeHandler {

    private static final long CONNECTION_CHECK_INTERVAL = 60;

    private final Logger logger = LoggerFactory.getLogger(LaMetricTimeHandler.class);

    private final StateDescriptionOptionsProvider stateDescriptionProvider;

    private final ClientBuilder clientBuilder;

    @NonNullByDefault({})
    private LaMetricTime clock;

    @Nullable
    private ScheduledFuture<?> connectionJob;

    public LaMetricTimeHandler(Bridge bridge, StateDescriptionOptionsProvider stateDescriptionProvider,
            ClientBuilder clientBuilder) {
        super(bridge);
        this.clientBuilder = clientBuilder;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Reading LaMetric Time binding configuration");
        LaMetricTimeConfiguration bindingConfig = getConfigAs(LaMetricTimeConfiguration.class);

        logger.debug("Creating LaMetric Time client");
        Configuration clockConfig = new Configuration().withDeviceHost(bindingConfig.host)
                .withDeviceApiKey(bindingConfig.apiKey).withLogging(logger.isDebugEnabled());
        clock = LaMetricTime.create(clockConfig, clientBuilder);

        connectionJob = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Verifying communication with LaMetric Time");
            try {
                LaMetricTimeLocal api = clock.getLocalApi();
                Device device = api.getDevice();
                if (device == null) {
                    logger.debug("Failed to communicate with LaMetric Time");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to connect to LaMetric Time");
                    return;
                }

                updateProperties(device, api.getBluetooth());
                setAppChannelStateDescription();
            } catch (Exception e) {
                logger.debug("Failed to communicate with LaMetric Time", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to connect to LaMetric Time");
                return;
            }

            logger.debug("Setting LaMetric Time online");
            updateStatus(ThingStatus.ONLINE);
        }, 0, CONNECTION_CHECK_INTERVAL, TimeUnit.SECONDS);
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        if (connectionJob != null && !connectionJob.isCancelled()) {
            connectionJob.cancel(true);
        }
        connectionJob = null;
        clock = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        try {
            switch (channelUID.getId()) {
                case CHANNEL_NOTIFICATIONS_INFO:
                case CHANNEL_NOTIFICATIONS_ALERT:
                case CHANNEL_NOTIFICATIONS_WARN:
                    handleNotificationsCommand(channelUID, command);
                    break;
                case CHANNEL_DISPLAY_BRIGHTNESS:
                case CHANNEL_DISPLAY_BRIGHTNESS_MODE:
                    handleBrightnessChannel(channelUID, command);
                    break;
                case CHANNEL_BLUETOOTH_ACTIVE:
                    handleBluetoothCommand(channelUID, command);
                    break;
                case CHANNEL_AUDIO_VOLUME:
                    handleAudioCommand(channelUID, command);
                    break;
                case CHANNEL_APP:
                    handleAppCommand(channelUID, command);
                default:
                    logger.debug("Channel '{}' not supported", channelUID);
                    break;
            }
        } catch (NotificationCreationException e) {
            logger.debug("Failed to create notification - taking clock offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Exception e) {
            logger.debug("Unexpected error while handling command - taking clock offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * This method can be called by app-specific thing handlers to update the state of the "app" channel on the device.
     * Note: When sending a command to an app, the device automatically switches to this app, so we reflect this here.
     *
     * @param widgetId The current widgetId of the active app
     */
    public void updateActiveApp(String widgetId) {
        updateState(LaMetricTimeBindingConstants.CHANNEL_APP, new StringType(widgetId));
    }

    private void handleNotificationsCommand(ChannelUID channelUID, Command command)
            throws NotificationCreationException {
        if (command instanceof RefreshType) {
            // verify communication
            clock.getLocalApi().getApi();
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_NOTIFICATIONS_INFO:
                clock.notifyInfo(command.toString());
                break;
            case CHANNEL_NOTIFICATIONS_WARN:
                clock.notifyWarning(command.toString());
                break;
            case CHANNEL_NOTIFICATIONS_ALERT:
                clock.notifyCritical(command.toString());
                break;
            default:
                logger.debug("Invalid notification channel: {}", channelUID);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private void handleAudioCommand(ChannelUID channelUID, Command command) {
        Audio audio = clock.getLocalApi().getAudio();
        if (command instanceof RefreshType) {
            updateState(channelUID, new PercentType(audio.getVolume()));
        } else if (command instanceof PercentType percentTypeCommand) {
            try {
                int volume = percentTypeCommand.intValue();
                if (volume >= 0 && volume != audio.getVolume()) {
                    audio.setVolume(volume);
                    clock.getLocalApi().updateAudio(audio);
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (UpdateException e) {
                logger.debug("Failed to update audio volume - taking clock offline", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void handleAppCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Skipping app channel refresh - LaMetric Time does not support querying for the active app");
        } else if (command instanceof StringType) {
            try {
                WidgetRef widgetRef = WidgetRef.fromString(command.toFullString());
                clock.getLocalApi().activateApplication(widgetRef.getPackageName(), widgetRef.getWidgetId());
                updateStatus(ThingStatus.ONLINE);
            } catch (ApplicationActivationException e) {
                logger.debug("Failed to activate app - taking clock offline", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void handleBluetoothCommand(ChannelUID channelUID, Command command) {
        Bluetooth bluetooth = clock.getLocalApi().getBluetooth();
        if (command instanceof RefreshType) {
            readBluetoothValue(channelUID, bluetooth);
        } else {
            updateBluetoothValue(channelUID, command, bluetooth);
        }
    }

    private void updateBluetoothValue(ChannelUID channelUID, Command command, Bluetooth bluetooth) {
        try {
            if (command instanceof OnOffType onOffCommand && channelUID.getId().equals(CHANNEL_BLUETOOTH_ACTIVE)) {
                if (onOffCommand == OnOffType.ON && !bluetooth.isActive()) {
                    bluetooth.setActive(true);
                    clock.getLocalApi().updateBluetooth(bluetooth);
                } else if (bluetooth.isActive()) {
                    bluetooth.setActive(false);
                    clock.getLocalApi().updateBluetooth(bluetooth);
                }
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (UpdateException e) {
            logger.debug("Failed to update bluetooth - taking clock offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void readBluetoothValue(ChannelUID channelUID, Bluetooth bluetooth) {
        switch (channelUID.getId()) {
            case CHANNEL_BLUETOOTH_ACTIVE:
                if (bluetooth.isActive()) {
                    updateState(channelUID, OnOffType.ON);
                } else {
                    updateState(channelUID, OnOffType.OFF);
                }
                break;
        }
    }

    private void handleBrightnessChannel(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            readDisplayValue(channelUID, clock.getLocalApi().getDisplay());
        } else {
            updateDisplayValue(channelUID, command);
        }
    }

    private void updateDisplayValue(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(CHANNEL_DISPLAY_BRIGHTNESS)) {
                if (command instanceof PercentType percentCommand) {
                    int brightness = percentCommand.intValue();
                    logger.debug("Set Brightness to {}.", brightness);
                    Display newDisplay = clock.setBrightness(brightness);
                    updateState(CHANNEL_DISPLAY_BRIGHTNESS_MODE, new StringType(newDisplay.getBrightnessMode()));
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    logger.debug("Unsupported command {} for display brightness! Supported commands: REFRESH", command);
                }
            } else if (channelUID.getId().equals(CHANNEL_DISPLAY_BRIGHTNESS_MODE)) {
                if (command instanceof StringType) {
                    BrightnessMode mode = BrightnessMode.toEnum(command.toFullString());
                    if (mode == null) {
                        logger.warn("Unknown brightness mode: {}", command);
                    } else {
                        clock.setBrightnessMode(mode);
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    logger.debug("Unsupported command {} for display brightness! Supported commands: REFRESH", command);
                }
            }
        } catch (UpdateException e) {
            logger.debug("Failed to update display - taking clock offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void readDisplayValue(ChannelUID channelUID, Display display) {
        if (channelUID.getId().equals(CHANNEL_DISPLAY_BRIGHTNESS)) {
            int brightness = display.getBrightness();
            State state = new PercentType(brightness);
            updateState(channelUID, state);
        } else if (channelUID.getId().equals(CHANNEL_DISPLAY_BRIGHTNESS_MODE)) {
            String mode = display.getBrightnessMode();
            StringType state = new StringType(mode);
            updateState(channelUID, state);
        }
    }

    private void updateProperties(Device device, Bluetooth bluetooth) {
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.getOsVersion());
        properties.put(Thing.PROPERTY_MODEL_ID, device.getModel());
        properties.put(LaMetricTimeBindingConstants.PROPERTY_ID, device.getId());
        properties.put(LaMetricTimeBindingConstants.PROPERTY_NAME, device.getName());
        properties.put(LaMetricTimeBindingConstants.PROPERTY_BT_DISCOVERABLE,
                String.valueOf(bluetooth.isDiscoverable()));
        properties.put(LaMetricTimeBindingConstants.PROPERTY_BT_AVAILABLE, String.valueOf(bluetooth.isAvailable()));
        properties.put(LaMetricTimeBindingConstants.PROPERTY_BT_PAIRABLE, String.valueOf(bluetooth.isPairable()));
        properties.put(LaMetricTimeBindingConstants.PROPERTY_BT_MAC, bluetooth.getMac());
        properties.put(LaMetricTimeBindingConstants.PROPERTY_BT_NAME, bluetooth.getName());
        updateProperties(properties);
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new ArrayList<>();

        LaMetricTimeConfiguration config = getConfigAs(LaMetricTimeConfiguration.class);
        String host = config.host;
        String apiKey = config.apiKey;

        if (host == null || host.isEmpty()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(HOST)
                    .withMessageKeySuffix(LaMetricTimeConfigStatusMessage.HOST_MISSING).withArguments(HOST).build());
        }

        if (apiKey == null || apiKey.isEmpty()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(API_KEY)
                    .withMessageKeySuffix(LaMetricTimeConfigStatusMessage.API_KEY_MISSING).withArguments(API_KEY)
                    .build());
        }

        return configStatusMessages;
    }

    protected LaMetricTime getClock() {
        return clock;
    }

    public SortedMap<String, Application> getApps() {
        return getClock().getLocalApi().getApplications();
    }

    private void setAppChannelStateDescription() {
        List<StateOption> options = new ArrayList<>();
        for (Application app : getApps().values()) {
            for (Widget widget : app.getWidgets().values()) {
                options.add(new StateOption(new WidgetRef(widget.getPackageName(), widget.getId()).toString(),
                        LaMetricTimeUtil.getAppLabel(app, widget)));
            }
        }

        stateDescriptionProvider.setStateOptions(
                new ChannelUID(getThing().getUID(), LaMetricTimeBindingConstants.CHANNEL_APP), options);
    }
}
