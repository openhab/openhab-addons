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
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
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
import org.openhab.binding.wizlighting.internal.entities.ColorRequestParam;
import org.openhab.binding.wizlighting.internal.entities.ColorTemperatureRequestParam;
import org.openhab.binding.wizlighting.internal.entities.DimmingRequestParam;
import org.openhab.binding.wizlighting.internal.entities.Param;
import org.openhab.binding.wizlighting.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SceneRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SpeedRequestParam;
import org.openhab.binding.wizlighting.internal.entities.StateRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SyncResponseParam;
import org.openhab.binding.wizlighting.internal.entities.SystemConfigResult;
import org.openhab.binding.wizlighting.internal.entities.WizLightingRequest;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.enums.WizLightingLightMode;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;
import org.openhab.binding.wizlighting.internal.exceptions.MacAddressNotValidException;
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

    private String bulbIpAddress = "bulbIpAddress";
    private String bulbMacAddress = "bulbMacAddress";
    private int homeId;
    private Long updateInterval = DEFAULT_REFRESH_INTERVAL;

    private final WizLightingPacketConverter converter = new WizLightingPacketConverter();
    private @Nullable ScheduledFuture<?> keepAliveJob;
    private long latestUpdate = -1;
    private RegistrationRequestParam registrationInfo;
    private int requestId = 0;

    /**
     * We need to remember some states for increase/decrease commands and scene speed commands
     */
    protected Map<String, Integer> stateMap = Collections.synchronizedMap(new HashMap<String, Integer>());

    /**
     * Default constructor.
     *
     * @param thing the thing of the handler.
     * @throws MacAddressNotValidException if the mac address isn't valid.
     */
    public WizLightingHandler(final Thing thing, final RegistrationRequestParam registrationPacket)
            throws MacAddressNotValidException {
        super(thing);

        savebulbMacAddressFromConfiguration(this.getConfig());
        savebulbIpAddressFromConfiguration(this.getConfig());
        saveUpdateIntervalFromConfiguration(this.getConfig());

        logger.debug("Setting my host to {} and mac to {}", registrationPacket.getPhoneIp(),
                registrationPacket.getPhoneMac());
        this.registrationInfo = registrationPacket;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        logger.trace("Received command on channel {}: {}", channelUID, command.toFullString());

        // Be patient...
        if (command instanceof RefreshType) {
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
                    handleIncreaseDecreaseCommand(
                            ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? true : false));
                }
                break;

            case CHANNEL_SWITCH_STATE:
                if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                }
                break;

            case CHANNEL_LIGHT_MODE:
                logger.trace("Setting bulb light mode.");
                stateMap.put(CHANNEL_LIGHT_MODE, Integer.parseInt(command.toString()));
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
            ;
        }
        super.handleRemoval();
    }

    private void handleHSBCommand(HSBType hsb) {
        logger.trace("Setting bulb color to HSB: {}.", hsb.toString());
        stateMap.put(CHANNEL_DIMMING, hsb.getBrightness().intValue());
        if (hsb.getBrightness().intValue() == 0) {
            logger.trace("Zero intensity requested, turning bulb off.");
            setPilotCommand(new StateRequestParam(false));
        } else {
            logger.trace("Setting bulb color to {}.", hsb.toString());
            setPilotCommand(new ColorRequestParam(hsb));
        }
    }

    private void handlePercentCommand(PercentType brightness) {
        logger.trace("Setting bulb brightness.");
        stateMap.put(CHANNEL_DIMMING, brightness.intValue());
        if (brightness == PercentType.ZERO) {
            logger.trace("Zero brightness requested, turning bulb off.");
            setPilotCommand(new StateRequestParam(false));
        } else {
            logger.trace("Setting bulb brightness to {}%.", brightness.toString());
            setPilotCommand(new DimmingRequestParam(brightness.intValue()));
        }
    }

    private void handleOnOffCommand(OnOffType onOff) {
        logger.trace("Setting bulb state to {}.", onOff.toString());
        stateMap.put(CHANNEL_DIMMING, onOff == OnOffType.ON ? 100 : 0);
        setPilotCommand(new StateRequestParam(onOff == OnOffType.ON ? true : false));
    }

    private void handleIncreaseDecreaseCommand(boolean isIncrease) {
        int oldDimming = 50;
        int newDimming = 50;
        if (stateMap.containsKey(CHANNEL_COLOR)) {
            oldDimming = stateMap.get(CHANNEL_COLOR);
        }
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
        stateMap.put(CHANNEL_TEMPERATURE, (temperature.intValue()));
        setPilotCommand(new ColorTemperatureRequestParam(temperature));
    }

    private void handleIncreaseDecreaseTemperatureCommand(boolean isIncrease) {
        int oldTemp = 50;
        int newTemp = 50;
        if (stateMap.containsKey(CHANNEL_TEMPERATURE)) {
            oldTemp = stateMap.get(CHANNEL_TEMPERATURE);
        }
        if (isIncrease) {
            newTemp = Math.min(100, oldTemp + 5);
        } else {
            newTemp = Math.max(10, oldTemp - 5);
        }
        logger.trace("Changing color temperature from {}% to {}%.", oldTemp, newTemp);
        handleTemperatureCommand(new PercentType(newTemp));
    }

    private void handleSpeedCommand(PercentType speed) {
        logger.trace("Setting speed of dynamic light mode.");
        stateMap.put(CHANNEL_DYNAMIC_SPEED, speed.intValue());
        // NOTE: We cannot set the speed without also setting the scene
        int currentScene = 1;
        if (stateMap.containsKey(CHANNEL_LIGHT_MODE)) {
            currentScene = stateMap.get(CHANNEL_LIGHT_MODE);
            logger.trace("Adjusting the speed of scene {}", currentScene);
        }
        setPilotCommand(new SpeedRequestParam(currentScene, speed.intValue()));
    }

    private void handleIncreaseDecreaseSpeedCommand(boolean isIncrease) {
        int oldSpeed = 50;
        int newSpeed = 50;
        if (stateMap.containsKey(CHANNEL_DYNAMIC_SPEED)) {
            oldSpeed = stateMap.get(CHANNEL_DYNAMIC_SPEED);
        }
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
    private void initGetStatusAndKeepAliveThread() {
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob != null) {
            keepAliveJob.cancel(true);
        }
        // try with handler port if is null
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long timePassedFromLastUpdateInSeconds = (now - latestUpdate) / 1000;

                if (getThing().getStatus() != ThingStatus.OFFLINE) {
                    logger.debug(
                            "Begin of Socket keep alive thread routine for bulb at {}. Current configuration update interval: {} seconds.",
                            bulbIpAddress, updateInterval);

                    logger.trace("MAC address: {}  Latest Update: {} Now: {} Delta: {} seconds", bulbMacAddress,
                            latestUpdate, now, timePassedFromLastUpdateInSeconds);

                    boolean considerThingOffline = (latestUpdate < 0)
                            || (timePassedFromLastUpdateInSeconds > (updateInterval * INTERVALS_BEFORE_OFFLINE));
                    if (considerThingOffline) {
                        logger.debug(
                                "Since no updates have been received from mac address {}, setting its status to OFFLINE.",
                                getBulbMacAddress());
                        updateStatus(ThingStatus.OFFLINE);
                    } else {
                        // refresh the current state
                        getPilot();
                    }
                } else {
                    logger.debug(
                            "Bulb at {} - {} is offline.  Will not query for status until a firstBeat is received.",
                            getBulbIpAddress(), getBulbMacAddress());
                }
            }
        };
        this.keepAliveJob = scheduler.scheduleWithFixedDelay(runnable, 1, updateInterval, TimeUnit.SECONDS);
    }

    @Override
    public void initialize() {
        logger.trace("Beginning initialization for bulb at {} - {}", getBulbIpAddress(), getBulbMacAddress());

        updateStatus(ThingStatus.UNKNOWN);
        updateBulbProperties();
        saveConfigurationsUsingCurrentStates();
        // registerWithBulb();
        initGetStatusAndKeepAliveThread();
        logger.debug("Finished initialization for bulb at {} - {}", getBulbIpAddress(), getBulbMacAddress());
    }

    private void getPilot() {
        logger.trace("Requesting current state from bulb.");
        WizLightingResponse response = sendRequestPacket(WizLightingMethodType.getPilot, null);
        if (response != null) {
            SyncResponseParam rParam = response.getSyncParams();
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
    public void newReceivedResponseMessage(final WizLightingResponse receivedMessage) {
        // Grab the ID number and mark the bulb online
        requestId = receivedMessage.getId();
        updateTimestamps();

        // Update the state from the parameters, if possible
        SyncResponseParam params = receivedMessage.getSyncParams();
        if (params != null) {
            updateStatesFromParams(params);
        }
    }

    /**
     * Updates the channel states based on incoming parameters
     *
     * @param receivedParam The received {@link SyncResponseParam}
     */
    private void updateStatesFromParams(final SyncResponseParam receivedParam) {
        if (!receivedParam.state) {
            logger.debug("Light is off");
            updateState(CHANNEL_COLOR, HSBType.BLACK);
            updateState(CHANNEL_DIMMING, PercentType.ZERO);
            updateState(CHANNEL_SWITCH_STATE, OnOffType.OFF);
            // Always cache the dimming state
            stateMap.put(CHANNEL_DIMMING, 0);
        } else {
            stateMap.put(CHANNEL_DIMMING, receivedParam.dimming);
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
                    stateMap.put(CHANNEL_TEMPERATURE, receivedParam.getTemperaturePercent().intValue());
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
            stateMap.put(CHANNEL_LIGHT_MODE, receivedParam.sceneId);
        }

        // update speed channel
        logger.debug("Received scene speed: {}", receivedParam.speed);
        updateState(CHANNEL_DYNAMIC_SPEED, new PercentType(receivedParam.speed));
        stateMap.put(CHANNEL_DYNAMIC_SPEED, receivedParam.speed);

        // update signal strength
        if (receivedParam.rssi != 0) {
            int strength = -1;
            if (receivedParam.rssi > -60) {
                strength = 4;
            } else if (receivedParam.rssi > -70) {
                strength = 3;
            } else if (receivedParam.rssi > -80) {
                strength = 2;
            } else if (receivedParam.rssi > -90) {
                strength = 1;
            } else {
                strength = 0;
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
    private @Nullable WizLightingResponse sendRequestPacket(final WizLightingMethodType method,
            final @Nullable Param param) {
        DatagramSocket dsocket = null;
        try {
            InetAddress address = InetAddress.getByName(bulbIpAddress);
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
                    getBulbIpAddress(), getBulbMacAddress());
        } catch (IOException exception) {
            logger.debug("Something wrong happened when sending the packet to address: {} and port {}... msg: {}",
                    bulbIpAddress, DEFAULT_BULB_UDP_PORT, exception.getMessage());
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
    private boolean setPilotCommand(final @Nullable Param param) {
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
    private void updateTimestamps() {
        updateStatus(ThingStatus.ONLINE);
        latestUpdate = System.currentTimeMillis();
        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
    }

    /**
     * Asks the bulb for its current system configuration
     */
    private void updateBulbProperties() {
        logger.trace("Updating metadata for bulb at {}", bulbIpAddress);
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
                        getBulbIpAddress(), getBulbMacAddress());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            logger.debug("No response to registration request from bulb at {} - {}", getBulbIpAddress(),
                    getBulbMacAddress());
            // Not calling it "gone" because it's probably just been powered off and will be
            // back any time
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /**
     * Registers with the bulb - this tells the bulb to begin sending 5-second
     * heartbeat (hb) status updates
     */
    /*
    private void registerWithBulb() {
        logger.trace("Registering for updates with bulb at {} - {}", getBulbIpAddress(), getBulbMacAddress());
        WizLightingResponse registrationResponse = sendRequestPacket(WizLightingMethodType.registration,
                this.registrationInfo);
        if (registrationResponse != null) {
            if (registrationResponse.getResultSuccess()) {
                updateTimestamps();
            } else {
                logger.debug(
                        "Received response to getConfigRequest from bulb at {} - {}, but id did not contain bulb configuration information.",
                        getBulbIpAddress(), getBulbMacAddress());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            logger.debug("No response to registration request from bulb at {} - {}", getBulbIpAddress(),
                    getBulbMacAddress());
            // Not calling it "gone" because it's probably just been powered off and will be
            // back any time
            updateStatus(ThingStatus.OFFLINE);
        }
    }
    */

    @Override
    protected void updateConfiguration(final Configuration configuration) {
        try {
            latestUpdate = -1;

            // Save the new configuration parameters to the thing
            savebulbMacAddressFromConfiguration(configuration);
            savebulbIpAddressFromConfiguration(configuration);
            saveUpdateIntervalFromConfiguration(configuration);

            // Re-register to get heartbeats
            // registerWithBulb();

            // Check the bulb's system configuration
            updateBulbProperties();
            saveConfigurationsUsingCurrentStates();
            initGetStatusAndKeepAliveThread();
        } catch (MacAddressNotValidException e) {
            logger.error("The Mac address passed is not valid! {}", e.getBulbMacAddress());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    /**
     * Saves the bulb address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void savebulbIpAddressFromConfiguration(final Configuration configuration) {
        bulbIpAddress = String.valueOf(configuration.get(CONFIG_IP_ADDRESS));
        logger.info("Bulb IP Address set to '{}'", bulbIpAddress);
    }

    /**
     * Saves the bulb address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveUpdateIntervalFromConfiguration(final Configuration configuration) {
        updateInterval = DEFAULT_REFRESH_INTERVAL;
        if ((configuration.get(CONFIG_UPDATE_INTERVAL) instanceof BigDecimal)
                && (((BigDecimal) configuration.get(CONFIG_UPDATE_INTERVAL)).longValue() > 0)) {
            updateInterval = ((BigDecimal) configuration.get(CONFIG_UPDATE_INTERVAL)).longValue();
        }
    }

    /**
     * Saves the bulb's mac address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void savebulbMacAddressFromConfiguration(final Configuration configuration)
            throws MacAddressNotValidException {
        String bulbMacAddress = String.valueOf(configuration.get(CONFIG_MAC_ADDRESS));

        if (ValidationUtils.isMacNotValid(bulbMacAddress)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.UNINITIALIZED.NONE);
            throw new MacAddressNotValidException("The given MAC address is not valid: {}", bulbMacAddress);
        } else {
            this.bulbMacAddress = bulbMacAddress.replaceAll(":", "").toUpperCase();
            logger.info("Bulb Mac Address set to '{}'", bulbMacAddress);
        }
    }

    /**
     * Save the current runtime configuration of the handler in configuration
     * mechanism.
     */
    private void saveConfigurationsUsingCurrentStates() {
        Map<String, Object> map = new HashMap<>();
        map.put(CONFIG_MAC_ADDRESS, this.bulbMacAddress);
        map.put(CONFIG_IP_ADDRESS, this.bulbIpAddress);
        map.put(CONFIG_UPDATE_INTERVAL, this.updateInterval);

        Configuration newConfiguration = new Configuration(map);
        super.updateConfiguration(newConfiguration);
    }

    // SETTERS AND GETTERS
    public String getBulbIpAddress() {
        return bulbIpAddress;
    }

    public String getBulbMacAddress() {
        return bulbMacAddress;
    }

    public int getHomeId() {
        return homeId;
    }
}
