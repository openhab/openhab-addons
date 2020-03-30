/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.handler;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.wizlighting.internal.WizLightingBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.wizlighting.internal.config.WizLightingDeviceConfiguration;
import org.openhab.binding.wizlighting.internal.entities.ColorRequestParam;
import org.openhab.binding.wizlighting.internal.entities.ColorTemperatureRequestParam;
import org.openhab.binding.wizlighting.internal.entities.DimmingRequestParam;
import org.openhab.binding.wizlighting.internal.entities.Param;
import org.openhab.binding.wizlighting.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SceneRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SpeedRequestParam;
import org.openhab.binding.wizlighting.internal.entities.StateRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SystemConfigResult;
import org.openhab.binding.wizlighting.internal.entities.WizLightingRequest;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.entities.WizLightingSyncState;
import org.openhab.binding.wizlighting.internal.enums.WizLightingLightMode;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;
import org.openhab.binding.wizlighting.internal.utils.ValidationUtils;
import org.openhab.binding.wizlighting.internal.utils.WizLightingPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WizLightingHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@NonNullByDefault
public class WizLightingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WizLightingHandler.class);

    private @NonNullByDefault({}) WizLightingDeviceConfiguration config;
    private RegistrationRequestParam registrationInfo;
    private int homeId;

    private WizLightingSyncState mostRecentState;

    private final WizLightingPacketConverter converter = new WizLightingPacketConverter();
    private @Nullable ScheduledFuture<?> keepAliveJob;
    private long latestUpdate = -1;
    // private RegistrationRequestParam registrationInfo;
    private int requestId = 0;

    /**
     * Default constructor.
     *
     * @param thing the thing of the handler.
     */
    public WizLightingHandler(final Thing thing, final RegistrationRequestParam registrationPacket) {
        super(thing);
        this.config = getConfigAs(WizLightingDeviceConfiguration.class);
        this.registrationInfo = registrationPacket;
        this.mostRecentState = new WizLightingSyncState();
        logger.trace("Created handler for WiZ bulb with IP {} and MAC address {}.", config.bulbIpAddress,
                config.bulbMacAddress);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        logger.trace("Received command on channel {}: {}", channelUID, command.toFullString());

        // Be patient...
        if (command instanceof RefreshType) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                getPilot();
            }
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    handleHSBCommand((HSBType) command);
                } else if (command instanceof PercentType) {
                    handlePercentCommand((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseCommand(
                            ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? true : false));
                }
                break;

            case CHANNEL_TEMPERATURE:
                if (command instanceof PercentType) {
                    handleTemperatureCommand((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleTemperatureCommand(
                            ((OnOffType) command) == OnOffType.ON ? new PercentType(100) : new PercentType(0));
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseTemperatureCommand(
                            ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? true : false));
                }
                break;

            case CHANNEL_DIMMING:
                if (command instanceof PercentType) {
                    handlePercentCommand((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseCommand(command == IncreaseDecreaseType.INCREASE);
                }
                break;

            case CHANNEL_SWITCH_STATE:
                if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                }
                break;

            case CHANNEL_LIGHT_MODE:
                logger.trace("Setting bulb light mode.");
                mostRecentState.sceneId = Integer.parseInt(command.toString());
                setPilotCommand(new SceneRequestParam(Integer.parseInt(command.toString())));
                break;

            case CHANNEL_DYNAMIC_SPEED:
                if (command instanceof PercentType) {
                    handleSpeedCommand((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleSpeedCommand(
                            ((OnOffType) command) == OnOffType.ON ? new PercentType(100) : new PercentType(0));
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseSpeedCommand(
                            ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? true : false));
                }
                break;
        }
    }

    @Override
    public void handleRemoval() {
        // stop update thread
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob != null) {
            keepAliveJob.cancel(true);
            this.keepAliveJob = null;
        }
        super.handleRemoval();
    }

    private void handleHSBCommand(HSBType hsb) {
        logger.trace("Setting bulb color to HSB: {}.", hsb.toString());
        if (hsb.getBrightness().intValue() == 0) {
            logger.trace("Zero intensity requested, turning bulb off.");
            setPilotCommand(new StateRequestParam(false));
        } else {
            logger.trace("Setting bulb color to {}.", hsb.toString());
            setPilotCommand(new ColorRequestParam(hsb));
        }
        mostRecentState.setHSBColor(hsb);
    }

    private void handlePercentCommand(PercentType brightness) {
        logger.trace("Setting bulb brightness.");
        if (brightness == PercentType.ZERO) {
            logger.trace("Zero brightness requested, turning bulb off.");
            setPilotCommand(new StateRequestParam(false));
        } else {
            logger.trace("Setting bulb brightness to {}%.", brightness.toString());
            setPilotCommand(new DimmingRequestParam(brightness.intValue()));
        }
        mostRecentState.dimming = brightness.intValue();
    }

    private void handleOnOffCommand(OnOffType onOff) {
        logger.trace("Setting bulb state to {}.", onOff.toString());
        setPilotCommand(new StateRequestParam(onOff == OnOffType.ON ? true : false));
        mostRecentState.state = onOff == OnOffType.ON;
    }

    private void handleIncreaseDecreaseCommand(boolean isIncrease) {
        int oldDimming = mostRecentState.dimming;
        int newDimming = 50;
        if (isIncrease) {
            newDimming = Math.min(100, oldDimming + 5);
        } else {
            newDimming = Math.max(10, oldDimming - 5);
        }
        logger.trace("Changing bulb brightness from {}% to {}%.", oldDimming, newDimming);
        handlePercentCommand(new PercentType(newDimming));
    }

    private void handleTemperatureCommand(PercentType temperature) {
        logger.trace("Setting bulb color temperature to {}%.", temperature.toString());
        setPilotCommand(new ColorTemperatureRequestParam(temperature));
        mostRecentState.setTemperaturePercent(temperature);
    }

    private void handleIncreaseDecreaseTemperatureCommand(boolean isIncrease) {
        int oldTempPct = mostRecentState.getTemperaturePercent().intValue();
        int newTempPct = 50;
        if (isIncrease) {
            newTempPct = Math.min(100, oldTempPct + 5);
        } else {
            newTempPct = Math.max(0, oldTempPct - 5);
        }
        logger.trace("Changing color temperature from {}% to {}%.", oldTempPct, newTempPct);
        handleTemperatureCommand(new PercentType(newTempPct));
    }

    private void handleSpeedCommand(PercentType speed) {
        logger.trace("Setting speed of dynamic light mode.");
        // NOTE: We cannot set the speed without also setting the scene
        int currentScene = mostRecentState.sceneId;
        logger.trace("Adjusting the speed of scene {}", currentScene);
        setPilotCommand(new SpeedRequestParam(currentScene, speed.intValue()));
        mostRecentState.speed = speed.intValue();
    }

    private void handleIncreaseDecreaseSpeedCommand(boolean isIncrease) {
        int oldSpeed = mostRecentState.speed;
        int newSpeed = 50;
        if (isIncrease) {
            newSpeed = Math.min(100, oldSpeed + 5);
        } else {
            newSpeed = Math.max(10, oldSpeed - 5);
        }
        logger.trace("Changing dynamic light mode speed from {}% to {}%.", oldSpeed, newSpeed);
        handleTemperatureCommand(new PercentType(newSpeed));
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
        // try with handler port if is null
        Runnable runnable = () -> {
            long now = System.currentTimeMillis();
            long timePassedFromLastUpdateInSeconds = (now - latestUpdate) / 1000;

            if (getThing().getStatus() != ThingStatus.OFFLINE) {
                logger.debug("Polling for status from bulb at {}. Current configuration update interval: {} seconds.",
                        config.bulbIpAddress, config.updateInterval);

                logger.trace("MAC address: {}  Latest Update: {} Now: {} Delta: {} seconds", config.bulbMacAddress,
                        latestUpdate, now, timePassedFromLastUpdateInSeconds);

                boolean considerThingOffline = (latestUpdate < 0)
                        || (timePassedFromLastUpdateInSeconds > MARK_OFFLINE_AFTER_SEC);
                if (considerThingOffline) {
                    logger.debug(
                            "Since no updates have been received from mac address {} in {} seconds, setting its status to OFFLINE and discontinuing polling.",
                            config.bulbMacAddress, MARK_OFFLINE_AFTER_SEC);
                    updateStatus(ThingStatus.OFFLINE);
                } else if (config.useHeartBeats) {
                    // If we're using 5s heart-beats, we must re-register every 30s to maintain connection
                    registerWithBulb();
                } else {
                    // If we're not using heart-beats, just request the current status
                    getPilot();
                }
            } else {
                logger.debug("Bulb at {} - {} is offline.  Will not query for status until a firstBeat is received.",
                        config.bulbIpAddress, config.bulbMacAddress);
            }
        };
        long updateIntervalInUse = config.useHeartBeats ? 30 : config.updateInterval;
        this.keepAliveJob = scheduler.scheduleWithFixedDelay(runnable, 1, updateIntervalInUse, TimeUnit.SECONDS);
    }

    @Override
    public void initialize() {
        logger.trace("Beginning initialization for bulb at {} - {}", config.bulbIpAddress, config.bulbMacAddress);
        this.config = getConfigAs(WizLightingDeviceConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        if (ValidationUtils.isMacNotValid(config.bulbMacAddress)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "MAC address is not valid");
        }
        updateBulbProperties();
        initGetStatusAndKeepAliveThread();
        logger.debug("Finished initialization for bulb at {} - {}", config.bulbIpAddress, config.bulbMacAddress);
    }

    private synchronized void getPilot() {
        logger.trace("Requesting current state from bulb.");
        WizLightingResponse response = sendRequestPacket(WizLightingMethodType.getPilot, null);
        if (response != null) {
            WizLightingSyncState rParam = response.getSyncState();
            if (rParam != null) {
                updateTimestamps();
                updateStatesFromParams(rParam);
            } else {
                logger.trace("No parameters in getPilot response!");
            }
        } else {
            logger.trace("No response from getPilot request!");
        }
    }

    /**
     * Method called by {@link WizLightingMediator} when any "unsolicited" messages
     * come in on the listening socket and appear to be a WiZ bulb "Unsolicited"
     * messages from the bulb are usually heartbeat or sync states.
     *
     * @param receivedMessage the received {@link WizLightingResponse}.
     */
    public synchronized void newReceivedResponseMessage(final WizLightingResponse receivedMessage) {
        // Grab the ID number and mark the bulb online
        requestId = receivedMessage.getId();
        updateTimestamps();

        // Update the state from the parameters, if possible
        WizLightingSyncState params = receivedMessage.getSyncState();
        if (params != null) {
            updateStatesFromParams(params);
        }
    }

    /**
     * Updates the channel states based on incoming parameters
     *
     * @param receivedParam The received {@link WizLightingSyncState}
     */
    private synchronized void updateStatesFromParams(final WizLightingSyncState receivedParam) {
        // Save the current state
        this.mostRecentState = receivedParam;

        if (!receivedParam.state) {
            logger.debug("Light is off");
            updateState(CHANNEL_COLOR, HSBType.BLACK);
            updateState(CHANNEL_DIMMING, PercentType.ZERO);
            updateState(CHANNEL_SWITCH_STATE, OnOffType.OFF);
        } else {
            switch (receivedParam.getColorMode()) {
                case RGBMode:
                    logger.debug("Received color values - R: {} G: {} B: {} W: {} C: {} Dimming: {}", receivedParam.r,
                            receivedParam.g, receivedParam.b, receivedParam.w, receivedParam.c, receivedParam.dimming);
                    logger.debug("Translated to HSBValues {}", receivedParam.getHSBColor());
                    updateState(CHANNEL_COLOR, receivedParam.getHSBColor());
                    updateState(CHANNEL_TEMPERATURE, new PercentType(50));
                    updateState(CHANNEL_DIMMING, new PercentType(receivedParam.dimming));
                    updateState(CHANNEL_SWITCH_STATE, OnOffType.ON);
                    break;
                case CTMode:
                    // update color temperature channel
                    logger.debug("Received color temperature: {} ({}%)", receivedParam.temp,
                            receivedParam.getTemperaturePercent());
                    updateState(CHANNEL_COLOR, new HSBType(new DecimalType(0), new PercentType(0),
                            new PercentType(receivedParam.getDimming())));
                    updateState(CHANNEL_TEMPERATURE, receivedParam.getTemperaturePercent());
                    updateState(CHANNEL_DIMMING, new PercentType(receivedParam.dimming));
                    updateState(CHANNEL_SWITCH_STATE, OnOffType.ON);
                case SingleColorMode:
                    updateState(CHANNEL_COLOR, new HSBType(new DecimalType(0), new PercentType(0),
                            new PercentType(receivedParam.getDimming())));
                    updateState(CHANNEL_TEMPERATURE, new PercentType(50));
                    updateState(CHANNEL_DIMMING, new PercentType(receivedParam.dimming));
                    updateState(CHANNEL_SWITCH_STATE, OnOffType.ON);
            }
        }

        // update scene channel
        if (receivedParam.sceneId > 0) {
            logger.debug("Received scene: {} ({})", receivedParam.sceneId,
                    WizLightingLightMode.getNameFromModeId(receivedParam.sceneId));
            updateState(CHANNEL_LIGHT_MODE, new StringType(String.valueOf(receivedParam.sceneId)));
        }

        // update speed channel
        logger.debug("Received scene speed: {}", receivedParam.speed);
        updateState(CHANNEL_DYNAMIC_SPEED, new PercentType(receivedParam.speed));

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
            logger.debug("Received RSSI: {}, Signal Strength: {}", receivedParam.rssi, strength);
            updateState(CHANNEL_RSSI, new DecimalType(strength));
        }
    }

    /**
     * Sends {@link WizLightingRequest} to the passed {@link InetAddress}.
     *
     * @param requestPacket the {@link WizLightingRequest}.
     * @param address the {@link InetAddress}.
     */
    private synchronized @Nullable WizLightingResponse sendRequestPacket(final WizLightingMethodType method,
            final @Nullable Param param) {
        DatagramSocket dsocket = null;
        try {
            InetAddress address = InetAddress.getByName(config.bulbIpAddress);
            if (address != null) {
                WizLightingRequest request = new WizLightingRequest(method, param);
                request.setId(requestId++);

                byte[] message = this.converter.transformToByteMessage(request);
                // logger.trace("Raw packet to send: {}", message);

                // Initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(message, message.length, address, DEFAULT_BULB_UDP_PORT);

                // Create a datagram socket, send the packet through it, close it.
                dsocket = new DatagramSocket(null);
                dsocket.setReuseAddress(true);
                dsocket.setBroadcast(true);
                dsocket.setSoTimeout(500); // Timeout in 500ms
                dsocket.send(packet);
                logger.debug("Sent packet to address: {} and port {}", address, DEFAULT_BULB_UDP_PORT);

                byte[] responseMessage = new byte[1024];
                packet = new DatagramPacket(responseMessage, responseMessage.length);
                dsocket.receive(packet);

                return converter.transformResponsePacket(packet);
            }
        } catch (SocketTimeoutException e) {
            logger.trace("Socket timeout after sending command; no response from {} - {} within 500ms",
                    config.bulbIpAddress, config.bulbMacAddress);
        } catch (IOException exception) {
            logger.debug("Something wrong happened when sending the packet to address: {} and port {}... msg: {}",
                    config.bulbIpAddress, DEFAULT_BULB_UDP_PORT, exception.getMessage());
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
        WizLightingResponse response = sendRequestPacket(WizLightingMethodType.setPilot, param);
        if (response != null) {
            boolean setSucceeded = response.getResultSuccess();
            if (setSucceeded) {
                updateTimestamps();
                return setSucceeded;
            }
        }
        return false;
    }

    /**
     * Makes note of the latest timestamps
     */
    private synchronized void updateTimestamps() {
        updateStatus(ThingStatus.ONLINE);
        latestUpdate = System.currentTimeMillis();
        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
    }

    /**
     * Asks the bulb for its current system configuration
     */
    private synchronized void updateBulbProperties() {
        logger.trace("Updating metadata for bulb at {}", config.bulbIpAddress);
        WizLightingResponse registrationResponse = sendRequestPacket(WizLightingMethodType.getSystemConfig, null);
        if (registrationResponse != null) {
            SystemConfigResult responseResult = registrationResponse.getSystemConfigResults();
            if (responseResult != null) {
                // Update all the thing properties based on the result
                Map<String, String> thingProperties = new HashMap<String, String>();
                thingProperties.put(PROPERTY_VENDOR, "WiZ Connected");
                thingProperties.put(PROPERTY_FIRMWARE_VERSION, responseResult.fwVersion);
                thingProperties.put(PROPERTY_MAC_ADDRESS, responseResult.mac);
                thingProperties.put(PROPERTY_IP_ADDRESS, registrationResponse.getWizResponseIpAddress());
                thingProperties.put(PROPERTY_HOME_ID, String.valueOf(responseResult.homeId));
                thingProperties.put(PROPERTY_ROOM_ID, String.valueOf(responseResult.roomId));
                thingProperties.put(PROPERTY_HOME_LOCK, String.valueOf(responseResult.homeLock));
                thingProperties.put(PROPERTY_PAIRING_LOCK, String.valueOf(responseResult.pairingLock));
                thingProperties.put(PROPERTY_TYPE_ID, String.valueOf(responseResult.typeId));
                thingProperties.put(PROPERTY_MODULE_NAME, responseResult.moduleName);
                thingProperties.put(PROPERTY_GROUP_ID, String.valueOf(responseResult.groupId));
                updateProperties(thingProperties);
                updateTimestamps();
            } else {
                logger.debug(
                        "Received response to getConfigRequest from bulb at {} - {}, but id did not contain bulb configuration information.",
                        config.bulbIpAddress, config.bulbMacAddress);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            logger.debug("No response to registration request from bulb at {} - {}", config.bulbIpAddress,
                    config.bulbMacAddress);
            // Not calling it "gone" because it's probably just been powered off and will be
            // back any time
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /**
     * Registers with the bulb - this tells the bulb to begin sending 5-second
     * heartbeat (hb) status updates
     */

    private synchronized void registerWithBulb() {
        logger.trace("Registering for updates with bulb at {} - {}", config.bulbIpAddress, config.bulbMacAddress);
        WizLightingResponse registrationResponse = sendRequestPacket(WizLightingMethodType.registration,
                this.registrationInfo);
        if (registrationResponse != null) {
            if (registrationResponse.getResultSuccess()) {
                updateTimestamps();
            } else {
                logger.debug(
                        "Received response to getConfigRequest from bulb at {} - {}, but id did not contain bulb configuration information.",
                        config.bulbIpAddress, config.bulbMacAddress);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            logger.debug("No response to registration request from bulb at {} - {}", config.bulbIpAddress,
                    config.bulbMacAddress);
            // Not calling it "gone" because it's probably just been powered off and will be
            // back any time
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    // SETTERS AND GETTERS
    public String getBulbIpAddress() {
        return config.bulbIpAddress;
    }

    public String getBulbMacAddress() {
        return config.bulbMacAddress;
    }

    public int getHomeId() {
        return homeId;
    }
}
