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

import static org.openhab.binding.wizlighting.internal.WizLightingBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
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
import org.openhab.binding.wizlighting.internal.entities.WizLightingRequest;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.entities.WizResponseParam;
import org.openhab.binding.wizlighting.internal.enums.WizLightingColorMode;
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
     * Default constructor.
     *
     * @param thing the thing of the handler.
     * @throws MacAddressNotValidException if the mac address isn't valid.
     */
    public WizLightingHandler(final Thing thing, final String myAddress, final String myMacAddress)
            throws MacAddressNotValidException {
        super(thing);

        savebulbMacAddressFromConfiguration(this.getConfig());
        savebulbIpAddressFromConfiguration(this.getConfig());
        saveHomeIdFromConfiguration(this.getConfig());
        saveUpdateIntervalFromConfiguration(this.getConfig());

        logger.debug("Setting my host to {} and mac to {}", myAddress, myMacAddress);
        registrationInfo = new RegistrationRequestParam(myAddress, true, this.homeId, myMacAddress);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            handleRefreshCommand();
            return;
        }

        switch (channelUID.getId()) {
            case COLOR_CHANNEL:
                if (command instanceof HSBType) {
                    handleHSBCommand((HSBType) command);
                } else if (command instanceof PercentType) {
                    handlePercentCommand((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                }
                break;

            case TEMP_CHANNEL:
                if (command instanceof PercentType) {
                    handleTemperatureCommand((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                }
                break;

            case SCENE_CHANNEL:
                logger.trace("Setting bulb light mode.");
                setPilotCommand(new SceneRequestParam(command));
                break;

            case SCENE_SPEED_CHANNEL:
                logger.trace("Setting speed of dynamic light mode.");
                if (command instanceof PercentType) {
                    setPilotCommand(new SpeedRequestParam(command));
                } else if (command instanceof OnOffType) {
                    OnOffType onOffCommand = (OnOffType) command;
                    if (onOffCommand.equals(OnOffType.ON)) {
                        setPilotCommand(new SpeedRequestParam(100));
                    } else {
                        setPilotCommand(new SpeedRequestParam(0));
                    }
                }
                break;
        }
    }

    @Override
    public void handleRemoval() {
        // stop update thread
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob != null) {
            keepAliveJob.cancel(true);;
        }
        super.handleRemoval();
    }

    private void handleRefreshCommand() {
        logger.trace("Requesting current state from bulb.");
        WizLightingResponse response = sendRequestPacket(WizLightingMethodType.getPilot, null);
        if (response != null) {
            WizResponseParam rParam = response.getResult();
            if (rParam != null) {
                updateStatesFromParams(rParam);
            } else {
                logger.trace("No parameters in getPilot response!");
            }
        } else {
            logger.trace("No response from getPilot request!");
        }
    }

    private void handleHSBCommand(HSBType hsb) {
        logger.trace("Setting bulb color.");
        if (hsb.getBrightness().intValue() == 0) {
            setPilotCommand(new StateRequestParam(OnOffType.OFF));
        } else {
            setPilotCommand(new ColorRequestParam(hsb));
        }
    }

    private void handlePercentCommand(PercentType brightness) {
        logger.trace("Setting bulb brightness.");
        if (brightness == PercentType.ZERO) {
            setPilotCommand(new StateRequestParam(OnOffType.OFF));
        } else {
            setPilotCommand(new DimmingRequestParam(brightness));
        }
    }

    private void handleOnOffCommand(OnOffType onOff) {
        logger.trace("Setting bulb state.");
        setPilotCommand(new StateRequestParam(onOff));
    }

    private void handleTemperatureCommand(PercentType temperature) {
        logger.trace("Setting bulb color temperature.");
        setPilotCommand(new ColorTemperatureRequestParam(temperature));
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
                logger.debug(
                        "Begin of Socket keep alive thread routine. Current configuration update interval: {} seconds.",
                        WizLightingHandler.this.updateInterval);

                long now = System.currentTimeMillis();
                long timePassedFromLastUpdateInSeconds = (now - latestUpdate) / 1000;

                logger.trace("MAC address: {}  Latest Update: {} Now: {} Delta: {} seconds", bulbMacAddress,
                        latestUpdate, now, timePassedFromLastUpdateInSeconds);

                boolean considerThingOffline = (latestUpdate < 0)
                        || (timePassedFromLastUpdateInSeconds > (updateInterval * INTERVALS_BEFORE_OFFLINE));
                if (considerThingOffline) {
                    logger.debug(
                            "Since no updates have been received from mac address {}, setting its status to OFFLINE.",
                            getBulbMacAddress());
                    updateStatus(ThingStatus.OFFLINE);
                }

                // refresh the current state
                handleRefreshCommand();

            }
        };
        this.keepAliveJob = scheduler.scheduleWithFixedDelay(runnable, 1, updateInterval, TimeUnit.SECONDS);
    }

    @Override
    public void initialize() {
        saveConfigurationsUsingCurrentStates();
        registerWithBulb();
        initGetStatusAndKeepAliveThread();
    }

    /**
     * Method called by {@link WizLightingMediator} when any "unsolicited" messages
     * come in on the listening socket and appear to be a WiZ bulb "Unsolicited"
     * messages from the bulb are usually heartbeat or sync states.
     *
     * @param receivedMessage the received {@link WizLightingResponse}.
     */
    public void newReceivedResponseMessage(final WizLightingResponse receivedMessage) {
        // Bump up the request ID number and mark the bulb online
        requestId = receivedMessage.getId() + 1;
        updateStatus(ThingStatus.ONLINE);
        latestUpdate = System.currentTimeMillis();

        // Update the state from the parameters, if possible
        WizResponseParam params = receivedMessage.getParams();
        if (params != null) {
            updateStatesFromParams(params);
        }
    }

    /**
     * Updates the channel states based on incoming parameters
     *
     * @param receivedParam The received {@link WizResponseParam}
     */
    private void updateStatesFromParams(final WizResponseParam receivedParam) {
        if (receivedParam.getColorMode() == WizLightingColorMode.RGBMode) {
            // update color channel
            logger.trace("Received color value: {}", receivedParam.getHSBColor());
            updateState(COLOR_CHANNEL, receivedParam.getHSBColor());
        } else {
            logger.trace("Color not reported, setting to WHITE");
            updateState(COLOR_CHANNEL, new HSBType("WHITE"));
        }

        if (receivedParam.getColorMode() == WizLightingColorMode.CTMode) {
            // update color temperature channel
            logger.trace("Received color temperature: {} ({}%)", receivedParam.temp,
                    receivedParam.getTemperaturePercent());
            updateState(TEMP_CHANNEL, receivedParam.getTemperaturePercent());
        }

        // update scene channel
        logger.trace("Received scene: {}", receivedParam.sceneId);
        updateState(SCENE_CHANNEL, new StringType(String.valueOf(receivedParam.sceneId)));

        // update speed channel
        updateState(SCENE_SPEED_CHANNEL, new PercentType(receivedParam.speed));

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
            logger.trace("Received RSSI: {}, Signal Strength: {}", receivedParam.rssi, strength);
            updateState(RSSI_CHANNEL, new DecimalType(strength));
        }
    }

    /**
     * Saves the bulb address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void savebulbIpAddressFromConfiguration(final Configuration configuration) {
        bulbIpAddress = String.valueOf(configuration.get(BULB_IP_ADDRESS_ARG));
        logger.info("Bulb IP Address set to '{}'", bulbIpAddress);
    }

    /**
     * Saves the bulb address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveUpdateIntervalFromConfiguration(final Configuration configuration) {
        updateInterval = DEFAULT_REFRESH_INTERVAL;
        if ((configuration.get(UPDATE_INTERVAL_ARG) instanceof BigDecimal)
                && (((BigDecimal) configuration.get(UPDATE_INTERVAL_ARG)).longValue() > 0)) {
            updateInterval = ((BigDecimal) configuration.get(UPDATE_INTERVAL_ARG)).longValue();
        }
    }

    /**
     * Saves the bulb's mac address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void savebulbMacAddressFromConfiguration(final Configuration configuration)
            throws MacAddressNotValidException {
        String bulbMacAddress = String.valueOf(configuration.get(BULB_MAC_ADDRESS_ARG));

        if (ValidationUtils.isMacNotValid(bulbMacAddress)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.UNINITIALIZED.NONE);
            throw new MacAddressNotValidException("The given MAC address is not valid: {}", bulbMacAddress);
        } else {
            this.bulbMacAddress = bulbMacAddress.replaceAll(":", "").toUpperCase();
            logger.info("Bulb Mac Address set to '{}'", bulbMacAddress);
        }
    }

    /**
     * Saves the home id from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveHomeIdFromConfiguration(final Configuration configuration) {
        if (configuration.get(HOME_ID_ARG) != null) {
            // String homeIdStr = (String) configuration.get(HOME_ID_ARG);
            // this.homeId = Integer.parseInt(homeIdStr);
            this.homeId = ((BigDecimal) configuration.get(HOME_ID_ARG)).intValue();
            ;
            logger.info("HomeId set to '{}'", this.homeId);
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
                DatagramPacket packet = new DatagramPacket(message, message.length, address, BULB_DEFAULT_UDP_PORT);

                // Create a datagram socket, send the packet through it, close it.
                dsocket = new DatagramSocket();
                dsocket.send(packet);
                logger.debug("Sent packet to address: {} and port {}", address, BULB_DEFAULT_UDP_PORT);

                byte[] responseMessage = new byte[1024];
                packet = new DatagramPacket(responseMessage, responseMessage.length);
                dsocket.receive(packet);

                return converter.transformSyncResponsePacket(packet);
            }
        } catch (IOException exception) {
            logger.debug("Something wrong happen sending the packet to address: {} and port {}... msg: {}",
                    bulbIpAddress, BULB_DEFAULT_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }
        }
        return null;
    }

    private boolean setPilotCommand(final @Nullable Param param) {
        WizLightingResponse response = sendRequestPacket(WizLightingMethodType.setPilot, param);
        if (response != null) {
            WizResponseParam respResult = response.getResult();
            if (respResult != null) {
                updateStatus(ThingStatus.ONLINE);
                latestUpdate = System.currentTimeMillis();
                return respResult.success;
            }
        }
        return false;
    }

    /**
     * Registers with the bulb - this tells the bulb to begin sending
     * 5-second heartbeat (hb) status updates
     */
    private void registerWithBulb() {
        WizLightingResponse registrationResponse;
        WizResponseParam responseResult;
        registrationResponse = sendRequestPacket(WizLightingMethodType.registration, registrationInfo);
        if (registrationResponse != null) {
            responseResult = registrationResponse.getResult();
            if (responseResult != null && responseResult.success) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    protected void updateConfiguration(final Configuration configuration) {
        try {
            latestUpdate = -1;

            savebulbMacAddressFromConfiguration(configuration);
            savebulbIpAddressFromConfiguration(configuration);
            saveUpdateIntervalFromConfiguration(configuration);
            saveHomeIdFromConfiguration(configuration);

            initGetStatusAndKeepAliveThread();
            saveConfigurationsUsingCurrentStates();
        } catch (MacAddressNotValidException e) {
            logger.error("The Mac address passed is not valid! {}", e.getBulbMacAddress());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    /**
     * Save the current runtime configuration of the handler in configuration
     * mechanism.
     */
    private void saveConfigurationsUsingCurrentStates() {
        Map<String, Object> map = new HashMap<>();
        map.put(BULB_MAC_ADDRESS_ARG, this.bulbMacAddress);
        map.put(BULB_IP_ADDRESS_ARG, this.bulbIpAddress);
        map.put(UPDATE_INTERVAL_ARG, this.updateInterval);
        map.put(HOME_ID_ARG, this.homeId);

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
