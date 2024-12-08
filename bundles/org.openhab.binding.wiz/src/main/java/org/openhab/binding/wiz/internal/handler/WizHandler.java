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
package org.openhab.binding.wiz.internal.handler;

import static org.openhab.binding.wiz.internal.WizBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.WizStateDescriptionProvider;
import org.openhab.binding.wiz.internal.config.WizDeviceConfiguration;
import org.openhab.binding.wiz.internal.entities.ColorRequestParam;
import org.openhab.binding.wiz.internal.entities.ColorTemperatureRequestParam;
import org.openhab.binding.wiz.internal.entities.DimmingRequestParam;
import org.openhab.binding.wiz.internal.entities.FanModeRequestParam;
import org.openhab.binding.wiz.internal.entities.FanReverseRequestParam;
import org.openhab.binding.wiz.internal.entities.FanSpeedRequestParam;
import org.openhab.binding.wiz.internal.entities.FanStateRequestParam;
import org.openhab.binding.wiz.internal.entities.ModelConfigResult;
import org.openhab.binding.wiz.internal.entities.Param;
import org.openhab.binding.wiz.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wiz.internal.entities.SceneRequestParam;
import org.openhab.binding.wiz.internal.entities.SpeedRequestParam;
import org.openhab.binding.wiz.internal.entities.StateRequestParam;
import org.openhab.binding.wiz.internal.entities.SystemConfigResult;
import org.openhab.binding.wiz.internal.entities.WizRequest;
import org.openhab.binding.wiz.internal.entities.WizResponse;
import org.openhab.binding.wiz.internal.entities.WizSyncState;
import org.openhab.binding.wiz.internal.enums.WizLightMode;
import org.openhab.binding.wiz.internal.enums.WizMethodType;
import org.openhab.binding.wiz.internal.utils.ValidationUtils;
import org.openhab.binding.wiz.internal.utils.WizPacketConverter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WizHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@NonNullByDefault
public class WizHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WizHandler.class);

    private @NonNullByDefault({}) WizDeviceConfiguration config;
    private @Nullable RegistrationRequestParam registrationRequestParam;
    private int homeId;

    private WizSyncState mostRecentState;

    private final WizPacketConverter converter = new WizPacketConverter();
    private final WizStateDescriptionProvider stateDescriptionProvider;
    private final TimeZoneProvider timeZoneProvider;
    private final ChannelUID colorTempChannelUID;
    private @Nullable ScheduledFuture<?> keepAliveJob;
    private long latestUpdate = -1;
    private long latestOfflineRefresh = -1;
    private int requestId = 0;
    private final boolean isFan;
    private final boolean isFanOnly;
    private int minColorTemp = MIN_COLOR_TEMPERATURE;
    private int maxColorTemp = MAX_COLOR_TEMPERATURE;

    private volatile boolean disposed;
    private volatile boolean fullyInitialized;

    /**
     * Default constructor.
     *
     * @param thing the thing of the handler.
     * @param stateDescriptionProvider A state description provider
     */
    public WizHandler(final Thing thing, final WizMediator mediator,
            WizStateDescriptionProvider stateDescriptionProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);
        try {
            registrationRequestParam = mediator.getRegistrationParams();
        } catch (IllegalStateException e) {
            registrationRequestParam = null;
        }
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.mostRecentState = new WizSyncState();
        this.isFan = thing.getThingTypeUID().equals(THING_TYPE_FAN)
                || thing.getThingTypeUID().equals(THING_TYPE_FAN_WITH_DIMMABLE_BULB);
        this.isFanOnly = thing.getThingTypeUID().equals(THING_TYPE_FAN);
        colorTempChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_ABS);
        fullyInitialized = false;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (hasConfigurationError() || disposed || !fullyInitialized) {
            logger.debug(
                    "[{}] WiZ handler for device {} received command {} on channel {} but is not yet prepared to handle it.",
                    config.ipAddress, config.macAddress, command, channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            long now = System.currentTimeMillis();
            long timePassedFromLastUpdateInSeconds = (now - latestUpdate) / 1000;
            // Be patient...
            if (latestUpdate < 0 || timePassedFromLastUpdateInSeconds > 5) {
                getPilot();
            }
            return;
        }

        if (isFanOnly || (isFan && CHANNEL_GROUP_FAN.equals(channelUID.getGroupId()))) {
            handleFanCommand(channelUID.getIdWithoutGroup(), command);
        } else if (!isFan || (isFan && CHANNEL_GROUP_LIGHT.equals(channelUID.getGroupId()))) {
            handleLightCommand(channelUID.getIdWithoutGroup(), command);
        }
    }

    private void handleLightCommand(final String channelId, final Command command) {
        switch (channelId) {
            case CHANNEL_COLOR:
                if (command instanceof HSBType hsbCommand) {
                    handleHSBCommand(hsbCommand);
                } else if (command instanceof PercentType percentCommand) {
                    handlePercentCommand(percentCommand);
                } else if (command instanceof OnOffType onOffCommand) {
                    handleOnOffCommand(onOffCommand);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseCommand(command == IncreaseDecreaseType.INCREASE);
                }
                break;
            case CHANNEL_TEMPERATURE:
                if (command instanceof PercentType percentCommand) {
                    handleTemperatureCommand(percentToColorTemp(percentCommand));
                } else if (command instanceof OnOffType onOffCommand) {
                    handleTemperatureCommand(
                            percentToColorTemp(onOffCommand == OnOffType.ON ? PercentType.HUNDRED : PercentType.ZERO));
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseTemperatureCommand(command == IncreaseDecreaseType.INCREASE);
                }
                break;
            case CHANNEL_TEMPERATURE_ABS:
                QuantityType<?> kelvinQt;
                if (command instanceof QuantityType<?> commandQt
                        && (kelvinQt = commandQt.toInvertibleUnit(Units.KELVIN)) != null) {
                    handleTemperatureCommand(kelvinQt.intValue());
                } else {
                    handleTemperatureCommand(Integer.valueOf(command.toString()));
                }
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType percentCommand) {
                    handlePercentCommand(percentCommand);
                } else if (command instanceof OnOffType onOffCommand) {
                    handleOnOffCommand(onOffCommand);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseCommand(command == IncreaseDecreaseType.INCREASE);
                }
                break;
            case CHANNEL_STATE:
                if (command instanceof OnOffType onOffCommand) {
                    handleOnOffCommand(onOffCommand);
                }
                break;
            case CHANNEL_MODE:
                handleLightModeCommand(command);
                break;
            case CHANNEL_SPEED:
                if (command instanceof PercentType percentCommand) {
                    handleSpeedCommand(percentCommand);
                } else if (command instanceof OnOffType onOffCommand) {
                    handleSpeedCommand(onOffCommand == OnOffType.ON ? PercentType.HUNDRED : PercentType.ZERO);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseSpeedCommand(command == IncreaseDecreaseType.INCREASE);
                }
                break;
        }
    }

    private void handleFanCommand(final String channelId, final Command command) {
        switch (channelId) {
            case CHANNEL_STATE:
                if (command instanceof OnOffType onOffCommand) {
                    handleFanOnOffCommand(onOffCommand);
                }
                break;
            case CHANNEL_MODE:
                if (command instanceof DecimalType decimalCommand) {
                    handleFanModeCommand(decimalCommand);
                }
                break;
            case CHANNEL_SPEED:
                if (command instanceof DecimalType numberCommand) {
                    if (numberCommand.equals(DecimalType.ZERO)) {
                        handleFanOnOffCommand(OnOffType.OFF);
                    } else {
                        handleFanSpeedCommand(numberCommand);
                    }
                }
                break;
            case CHANNEL_REVERSE:
                if (command instanceof OnOffType onOffCommand) {
                    handleFanReverseCommand(onOffCommand);
                }
                break;
        }
    }

    @Override
    public void handleRemoval() {
        disposed = true;
        fullyInitialized = false;
        // stop update thread
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob != null) {
            keepAliveJob.cancel(true);
            this.keepAliveJob = null;
        }
        super.handleRemoval();
    }

    private void handleLightModeCommand(Command command) {
        String commandAsString = command.toString();

        Integer commandAsInt = Integer.MIN_VALUE;
        WizLightMode commandAsLightMode = null;

        try {
            commandAsInt = Integer.parseInt(commandAsString);
        } catch (Exception ex) {
        }

        if (commandAsInt > 0) {
            commandAsLightMode = WizLightMode.fromSceneId(commandAsInt);
        }

        if (commandAsLightMode == null) {
            commandAsLightMode = WizLightMode.fromSceneName(commandAsString);
        }

        if (commandAsLightMode != null) {
            mostRecentState.sceneId = commandAsLightMode.getSceneId();
            setPilotCommand(new SceneRequestParam(commandAsLightMode.getSceneId()));
        } else {
            logger.warn("[{}] Command [{}] not a recognized Light Mode!", config.ipAddress, command);
        }
    }

    private void handleHSBCommand(HSBType hsb) {
        if (hsb.getBrightness().intValue() == 0) {
            logger.debug("[{}] Zero intensity requested, turning bulb off.", config.ipAddress);
            setPilotCommand(new StateRequestParam(false));
        } else {
            setPilotCommand(new ColorRequestParam(hsb));
        }
        mostRecentState.setHSBColor(hsb);
    }

    private void handlePercentCommand(PercentType brightness) {
        if (brightness.equals(PercentType.ZERO)) {
            logger.debug("[{}] Zero brightness requested, turning bulb off.", config.ipAddress);
            setPilotCommand(new StateRequestParam(false));
        } else {
            setPilotCommand(new DimmingRequestParam(brightness.intValue()));
        }
        mostRecentState.dimming = brightness.intValue();
    }

    private void handleOnOffCommand(OnOffType onOff) {
        setPilotCommand(new StateRequestParam(onOff == OnOffType.ON));
        mostRecentState.state = onOff == OnOffType.ON;
    }

    private void handleFanOnOffCommand(OnOffType onOff) {
        int value = onOff == OnOffType.ON ? 1 : 0;
        setPilotCommand(new FanStateRequestParam(value));
        mostRecentState.fanState = value;
    }

    private void handleFanSpeedCommand(DecimalType speed) {
        setPilotCommand(new FanSpeedRequestParam(speed.intValue()));
        mostRecentState.fanSpeed = speed.intValue();
    }

    private void handleFanReverseCommand(OnOffType onOff) {
        int value = onOff == OnOffType.ON ? 1 : 0;
        setPilotCommand(new FanReverseRequestParam(value));
        mostRecentState.fanRevrs = value;
    }

    private void handleFanModeCommand(DecimalType mode) {
        setPilotCommand(new FanModeRequestParam(mode.intValue()));
        mostRecentState.fanMode = mode.intValue();
    }

    private void handleIncreaseDecreaseCommand(boolean isIncrease) {
        int oldDimming = mostRecentState.dimming;
        int newDimming;
        if (isIncrease) {
            newDimming = Math.min(100, oldDimming + 5);
        } else {
            newDimming = Math.max(10, oldDimming - 5);
        }
        logger.debug("[{}] Changing bulb brightness from {}% to {}%.", config.ipAddress, oldDimming, newDimming);
        handlePercentCommand(new PercentType(newDimming));
    }

    private void handleTemperatureCommand(int temperature) {
        setPilotCommand(new ColorTemperatureRequestParam(temperature));
        mostRecentState.setTemperature(temperature);
    }

    private void handleIncreaseDecreaseTemperatureCommand(boolean isIncrease) {
        float oldTempPct = colorTempToPercent(mostRecentState.getTemperature()).floatValue();
        float newTempPct;
        if (isIncrease) {
            newTempPct = Math.min(100, oldTempPct + 5);
        } else {
            newTempPct = Math.max(0, oldTempPct - 5);
        }
        logger.debug("[{}] Changing color temperature from {}% to {}%.", config.ipAddress, oldTempPct, newTempPct);
        handleTemperatureCommand(percentToColorTemp(new PercentType(BigDecimal.valueOf(newTempPct))));
    }

    private void handleSpeedCommand(PercentType speed) {
        // NOTE: We cannot set the speed without also setting the scene
        int currentScene = mostRecentState.sceneId;
        setPilotCommand(new SpeedRequestParam(currentScene, speed.intValue()));
        mostRecentState.speed = speed.intValue();
    }

    private void handleIncreaseDecreaseSpeedCommand(boolean isIncrease) {
        int oldSpeed = mostRecentState.speed;
        int newSpeed;
        if (isIncrease) {
            newSpeed = Math.min(100, oldSpeed + 5);
        } else {
            newSpeed = Math.max(10, oldSpeed - 5);
        }
        handleSpeedCommand(new PercentType(newSpeed));
    }

    /**
     * Starts one thread that querys the state of the socket, after the defined
     * refresh interval.
     */
    private synchronized void initGetStatusAndKeepAliveThread() {
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob != null) {
            keepAliveJob.cancel(true);
        }

        Runnable runnable = () -> {
            long now = System.currentTimeMillis();
            long timePassedFromLastUpdateInSeconds = (now - latestUpdate) / 1000;
            long timePassedFromLastRefreshInSeconds = (now - latestOfflineRefresh) / 1000;

            // If the device has an online status, check if we it's been too long since the
            // last response and re-set offline accordingly
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                logger.trace("[{}] MAC address: {}  Latest Update: {} Now: {} Delta: {} seconds", config.ipAddress,
                        config.macAddress, latestUpdate, now, timePassedFromLastUpdateInSeconds);

                boolean considerThingOffline = (latestUpdate < 0)
                        || (timePassedFromLastUpdateInSeconds > MARK_OFFLINE_AFTER_SEC);
                if (considerThingOffline) {
                    logger.debug(
                            "[{}] Since no updates have been received from mac address {} in {} seconds, setting its status to OFFLINE and discontinuing polling.",
                            config.ipAddress, config.macAddress, MARK_OFFLINE_AFTER_SEC);
                    updateStatus(ThingStatus.OFFLINE);

                }
            }

            // If we're not offline ither re-register for heart-beats or request status
            if (getThing().getStatus() != ThingStatus.OFFLINE) {
                if (config.useHeartBeats) {
                    // If we're using 5s heart-beats, we must re-register every 30s to maintain
                    // connection
                    logger.debug("[{}] Re-registering for heart-beats.", config.ipAddress);
                    registerWithDevice();
                } else {
                    // If we're not using heart-beats, just request the current status
                    logger.debug("[{}] Polling for status from device at {}.", config.ipAddress, config.macAddress);
                    getPilot();
                }

                // Else if we are offline, but it's been a while, re-check if the device re-appeared
            } else if (timePassedFromLastRefreshInSeconds > config.reconnectInterval * 60) {
                // Request the current status
                logger.debug("[{}] Checking for reappearance of offline device at {}.", config.ipAddress,
                        config.macAddress);
                latestOfflineRefresh = now;
                getPilot();
            }
        };
        /**
         * Schedule the keep-alive job.
         *
         * The scheduling inteval is:
         * - every 30 seconds for online devices receiving heart-beats
         * - every config.updateInterval for other online devices
         */
        long updateIntervalInUse = config.useHeartBeats ? 30 : config.updateInterval;
        logger.debug("[{}] Scheduling reoccuring keep alive for every {} seconds for device at {}.", config.ipAddress,
                updateIntervalInUse, config.macAddress);
        this.keepAliveJob = scheduler.scheduleWithFixedDelay(runnable, 1, updateIntervalInUse, TimeUnit.SECONDS);
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(WizDeviceConfiguration.class);
        fullyInitialized = false;
        disposed = false;

        if (registrationRequestParam == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to determine openHAB's IP or MAC address");
            return;
        }
        if (!ValidationUtils.isMacValid(config.macAddress)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "MAC address is not valid");
            return;
        }

        // set the thing status to UNKNOWN temporarily
        updateStatus(ThingStatus.UNKNOWN);
        updateDeviceProperties();
        initGetStatusAndKeepAliveThread();
        fullyInitialized = true;
    }

    @Override
    public void dispose() {
        disposed = true;
        fullyInitialized = false;
        // stop update thread
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob != null) {
            keepAliveJob.cancel(true);
            this.keepAliveJob = null;
        }
        stateDescriptionProvider.remove(colorTempChannelUID);
        super.dispose();
    }

    private synchronized void getPilot() {
        WizResponse response = sendRequestPacket(WizMethodType.GetPilot, null);
        if (response != null) {
            WizSyncState rParam = response.getSyncState();
            if (rParam != null) {
                updateTimestamps();
                updateStatesFromParams(rParam);
            } else {
                logger.trace("[{}] No parameters in getPilot response!", config.ipAddress);
            }
        } else {
            logger.trace("[{}] No response from getPilot request!", config.ipAddress);
        }
    }

    /**
     * Method called by {@link WizMediator} when any "unsolicited" messages
     * come in on the listening socket and appear to be a WiZ device. "Unsolicited"
     * messages from the device could be:
     * - a "firstBeat" broadcast to the subnet by the device on first powering up
     * - an "hb" (heartbeat) specifically directed to openHAB within 30 seconds of registration
     * - or a response to a registration request broadcast by this binding to all devices on the subnet
     *
     * @note The mediator finds the correct handler for the device based on the (unchanging) device
     *       MAC address. If the mediator matches a message to the handler by MAC address, but the IP address
     *       the message came from doesn't match the device's configured IP address, this will update the
     *       device's configuration to reflect whatever the current IP is.
     *
     * @param receivedMessage the received {@link WizResponse}.
     */
    public synchronized void newReceivedResponseMessage(final WizResponse receivedMessage) {
        Boolean updatePropertiesAfterParams = false;

        // Check if the device still has the same IP address it had previously
        // If not, we need to update the configuration for the thing.
        if (!receivedMessage.getWizResponseIpAddress().isEmpty()
                && !receivedMessage.getWizResponseIpAddress().equals(this.getIpAddress())) {
            // get the old config
            Configuration priorConfig = getConfig();
            // change the ip address property
            priorConfig.put(CONFIG_IP_ADDRESS, receivedMessage.getWizResponseIpAddress());
            // save the changes to the thing
            updateConfiguration(priorConfig);
            // and then refresh the config within the handler
            this.config = getConfigAs(WizDeviceConfiguration.class);
            // finally, make note that we want to update properties
            updatePropertiesAfterParams = true;
        }

        // Grab the ID number and mark the device online
        requestId = receivedMessage.getId();
        updateTimestamps();

        // Update the state from the parameters, if possible
        WizSyncState params = receivedMessage.getSyncState();
        if (params != null) {
            updateStatesFromParams(params);
        }

        // After updating state, we'll update all other device parameters from devices that
        // presented with a new IP address.
        if (updatePropertiesAfterParams) {
            updateDeviceProperties();
        }
    }

    /**
     * Updates the channel states based on incoming parameters
     *
     * @param receivedParam The received {@link WizSyncState}
     */
    private synchronized void updateStatesFromParams(final WizSyncState receivedParam) {
        // Save the current state
        this.mostRecentState = receivedParam;

        if (hasConfigurationError() || disposed) {
            return;
        }

        if (isFan) {
            updateFanStatesFromParams(receivedParam);
        }
        if (!isFanOnly) {
            updateLightStatesFromParams(receivedParam);
        }

        // update signal strength
        if (receivedParam.rssi != 0) {
            int strength = -1;
            if (receivedParam.rssi < -90) {
                strength = 0;
            } else if (receivedParam.rssi < -80) {
                strength = 1;
            } else if (receivedParam.rssi < -70) {
                strength = 2;
            } else if (receivedParam.rssi < -67) {
                strength = 3;
            } else {
                strength = 4;
            }
            updateDeviceState(CHANNEL_SIGNAL_STRENGTH, new DecimalType(strength));
            updateDeviceState(CHANNEL_RSSI, new QuantityType<>(receivedParam.rssi, Units.DECIBEL_MILLIWATTS));
        }
    }

    /**
     * Updates the channel states for a light based on incoming parameters
     *
     * @param receivedParam The received {@link WizSyncState}
     */
    private void updateLightStatesFromParams(final WizSyncState receivedParam) {
        if (!receivedParam.state) {
            updateLightState(CHANNEL_COLOR, HSBType.BLACK);
            updateLightState(CHANNEL_BRIGHTNESS, PercentType.ZERO);
            updateLightState(CHANNEL_STATE, OnOffType.OFF);
            updateLightState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
            updateLightState(CHANNEL_TEMPERATURE_ABS, UnDefType.UNDEF);
        } else {
            updateLightState(CHANNEL_BRIGHTNESS, new PercentType(receivedParam.dimming));
            updateLightState(CHANNEL_STATE, OnOffType.ON);
            switch (receivedParam.getColorMode()) {
                case RGBMode:
                    logger.trace(
                            "[{}] Received color values - R: {} G: {} B: {} W: {} C: {} Dimming: {}; translate to HSBType: {}",
                            config.ipAddress, receivedParam.r, receivedParam.g, receivedParam.b, receivedParam.w,
                            receivedParam.c, receivedParam.dimming, receivedParam.getHSBColor());

                    updateLightState(CHANNEL_COLOR, receivedParam.getHSBColor());
                    updateLightState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
                    updateLightState(CHANNEL_TEMPERATURE_ABS, UnDefType.UNDEF);
                    break;
                case CTMode:
                    double[] xy = ColorUtil.kelvinToXY(receivedParam.getTemperature());
                    HSBType color = ColorUtil.xyToHsb(xy);
                    updateLightState(CHANNEL_COLOR, new HSBType(color.getHue(), color.getSaturation(),
                            new PercentType(receivedParam.getDimming())));
                    updateLightState(CHANNEL_TEMPERATURE, colorTempToPercent(receivedParam.getTemperature()));
                    updateLightState(CHANNEL_TEMPERATURE_ABS,
                            new QuantityType<>(receivedParam.getTemperature(), Units.KELVIN));
                    break;
                case SingleColorMode:
                    updateLightState(CHANNEL_COLOR, new HSBType(DecimalType.ZERO, PercentType.ZERO,
                            new PercentType(receivedParam.getDimming())));
                    updateLightState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
                    updateLightState(CHANNEL_TEMPERATURE_ABS, UnDefType.UNDEF);
                    break;
            }
        }

        updateLightState(CHANNEL_MODE, new StringType(String.valueOf(receivedParam.sceneId)));
        updateLightState(CHANNEL_SPEED, new PercentType(receivedParam.speed));
    }

    /**
     * Updates the channel states for a fan based on incoming parameters
     *
     * @param receivedParam The received {@link WizSyncState}
     */
    private void updateFanStatesFromParams(final WizSyncState receivedParam) {
        updateFanState(CHANNEL_STATE, receivedParam.fanState == 0 ? OnOffType.OFF : OnOffType.ON);
        updateFanState(CHANNEL_SPEED, new DecimalType(receivedParam.fanSpeed));
        updateFanState(CHANNEL_REVERSE, receivedParam.fanRevrs == 0 ? OnOffType.OFF : OnOffType.ON);
        updateFanState(CHANNEL_MODE, new DecimalType(receivedParam.fanMode));
    }

    /**
     * Sends {@link WizRequest} to the passed {@link InetAddress}.
     *
     * @param requestPacket the {@link WizRequest}.
     * @param address the {@link InetAddress}.
     */
    private synchronized @Nullable WizResponse sendRequestPacket(final WizMethodType method,
            final @Nullable Param param) {
        DatagramSocket dsocket = null;
        try {
            InetAddress address = InetAddress.getByName(config.ipAddress);
            if (address != null) {
                WizRequest request = new WizRequest(method, param);
                request.setId(requestId++);

                byte[] message = this.converter.transformToByteMessage(request);
                logger.trace("Raw packet to send: {}", message);

                // Initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(message, message.length, address, DEFAULT_UDP_PORT);

                // Create a datagram socket, send the packet through it, close it.
                dsocket = new DatagramSocket(null);
                dsocket.setReuseAddress(true);
                dsocket.setBroadcast(true);
                dsocket.setSoTimeout(500); // Timeout in 500ms
                dsocket.send(packet);
                logger.debug("[{}] Sent packet to address: {} and port {}", config.ipAddress, address,
                        DEFAULT_UDP_PORT);

                byte[] responseMessage = new byte[1024];
                packet = new DatagramPacket(responseMessage, responseMessage.length);
                dsocket.receive(packet);

                return converter.transformResponsePacket(packet);
            }
        } catch (SocketTimeoutException e) {
            logger.trace("[{}] Socket timeout after sending command; no response from {} within 500ms",
                    config.ipAddress, config.macAddress);
        } catch (IOException exception) {
            logger.debug("[{}] Something wrong happened when sending the packet to port {}... msg: {}",
                    config.ipAddress, DEFAULT_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }
        }
        return null;
    }

    /**
     * Sends a setPilot request and checks for success
     */
    private synchronized boolean setPilotCommand(final @Nullable Param param) {
        WizResponse response = sendRequestPacket(WizMethodType.SetPilot, param);
        if (response != null) {
            boolean setSucceeded = response.getResultSuccess();
            if (setSucceeded) {
                // can't process this response it doens't have a syncstate, so request updated state
                // let the getPilot response update the timestamps
                try {
                    // wait for state change to apply
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                }
                getPilot();
                return setSucceeded;
            }
        }
        return false;
    }

    /**
     * Makes note of the latest timestamps and sets the device online
     */
    private synchronized void updateTimestamps() {
        if (hasConfigurationError() || disposed) {
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        latestUpdate = System.currentTimeMillis();
        latestOfflineRefresh = System.currentTimeMillis();
        final ZonedDateTime zonedDateTime = ZonedDateTime.now(timeZoneProvider.getTimeZone());
        updateDeviceState(CHANNEL_LAST_UPDATE, new DateTimeType(zonedDateTime));
    }

    /**
     * Asks the device for its current system configuration
     */
    private synchronized void updateDeviceProperties() {
        if (hasConfigurationError() || disposed) {
            return;
        }
        WizResponse registrationResponse = sendRequestPacket(WizMethodType.GetSystemConfig, null);
        if (registrationResponse != null) {
            SystemConfigResult systemConfigResult = registrationResponse.getSystemConfigResults();
            if (systemConfigResult != null) {
                // Update all the thing properties based on the result
                Map<String, String> thingProperties = new HashMap<String, String>();
                thingProperties.put(PROPERTY_VENDOR, "WiZ Connected");
                thingProperties.put(PROPERTY_FIRMWARE_VERSION, systemConfigResult.fwVersion);
                thingProperties.put(PROPERTY_MAC_ADDRESS, systemConfigResult.mac);
                thingProperties.put(PROPERTY_IP_ADDRESS, registrationResponse.getWizResponseIpAddress());
                thingProperties.put(PROPERTY_HOME_ID, String.valueOf(systemConfigResult.homeId));
                thingProperties.put(PROPERTY_ROOM_ID, String.valueOf(systemConfigResult.roomId));
                thingProperties.put(PROPERTY_HOME_LOCK, String.valueOf(systemConfigResult.homeLock));
                thingProperties.put(PROPERTY_PAIRING_LOCK, String.valueOf(systemConfigResult.pairingLock));
                thingProperties.put(PROPERTY_TYPE_ID, String.valueOf(systemConfigResult.typeId));
                thingProperties.put(PROPERTY_MODULE_NAME, systemConfigResult.moduleName);
                thingProperties.put(PROPERTY_GROUP_ID, String.valueOf(systemConfigResult.groupId));
                updateProperties(thingProperties);
                updateTimestamps();
            } else {
                logger.debug(
                        "[{}] Received response to getConfigRequest from device at {}, but it did not contain device configuration information.",
                        config.ipAddress, config.macAddress);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }

            // Firmware versions > 1.22 support more details
            registrationResponse = sendRequestPacket(WizMethodType.GetModelConfig, null);
            if (registrationResponse != null) {
                ModelConfigResult modelConfigResult = registrationResponse.getModelConfigResults();
                if (modelConfigResult != null && modelConfigResult.cctRange.length > 0) {
                    minColorTemp = Arrays.stream(modelConfigResult.cctRange).min().getAsInt();
                    maxColorTemp = Arrays.stream(modelConfigResult.cctRange).max().getAsInt();
                    StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                            .withMinimum(BigDecimal.valueOf(minColorTemp)).withMaximum(BigDecimal.valueOf(maxColorTemp))
                            .withPattern("%.0f K").build().toStateDescription();
                    stateDescriptionProvider.setDescription(colorTempChannelUID,
                            Objects.requireNonNull(stateDescription));
                }
            } else {
                // Not a big deal; probably just an older device
                logger.warn("[{}] No response to getModelConfig request from device", config.ipAddress);
            }
        } else {
            logger.debug("[{}] No response to getSystemConfig request from device at {}", config.ipAddress,
                    config.macAddress);
            // Not calling it "gone" because it's probably just been powered off and will beback any time
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /**
     * Registers with the device - this tells the device to begin sending 5-second
     * heartbeat (hb) status updates. Status updates are sent by the device every 5
     * sec and on any state change for 30s after registration. For continuous
     * heart-beats the registration must be re-sent after 30s.
     */
    private synchronized void registerWithDevice() {
        WizResponse registrationResponse = sendRequestPacket(WizMethodType.Registration,
                Objects.requireNonNull(registrationRequestParam));
        if (registrationResponse != null) {
            if (registrationResponse.getResultSuccess()) {
                updateTimestamps();
            } else {
                logger.debug(
                        "[{}] Received response to getConfigRequest from device at {}, but it did not contain device configuration information.",
                        config.ipAddress, config.macAddress);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            logger.debug("[{}] No response to registration request from device at {}", config.ipAddress,
                    config.macAddress);
            // Not calling it "gone" because it's probably just been powered off and will be
            // back any time
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private boolean hasConfigurationError() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        return statusInfo.getStatus() == ThingStatus.OFFLINE
                && statusInfo.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR;
    }

    private int percentToColorTemp(PercentType command) {
        int range = maxColorTemp - minColorTemp;
        // NOTE: 0% is cold (highest K) and 100% is warm (lowest K)
        return maxColorTemp - Math.round((range * command.floatValue()) / 100);
    }

    private PercentType colorTempToPercent(int temp) {
        return new PercentType(BigDecimal.valueOf(((float) temp - minColorTemp) / (maxColorTemp - minColorTemp) * 100));
    }

    // SETTERS AND GETTERS
    public String getIpAddress() {
        return config.ipAddress;
    }

    public String getMacAddress() {
        return config.macAddress;
    }

    public int getHomeId() {
        return homeId;
    }

    private void updateLightState(String channelId, State state) {
        if (isFan) {
            updateState(new ChannelUID(this.getThing().getUID(), CHANNEL_GROUP_LIGHT, channelId), state);
        } else {
            updateState(channelId, state);
        }
    }

    private void updateFanState(String channelId, State state) {
        if (isFanOnly) {
            updateState(channelId, state);
        } else {
            updateState(new ChannelUID(this.getThing().getUID(), CHANNEL_GROUP_FAN, channelId), state);
        }
    }

    private void updateDeviceState(String channelId, State state) {
        if (isFan && !isFanOnly) {
            updateState(new ChannelUID(this.getThing().getUID(), CHANNEL_GROUP_DEVICE, channelId), state);
        } else {
            updateState(channelId, state);
        }
    }
}
