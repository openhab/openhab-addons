/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.wizlighting.internal.WizLightingBindingConstants;
import org.openhab.binding.wizlighting.internal.entities.ColorRequestParam;
import org.openhab.binding.wizlighting.internal.entities.DimmingRequestParam;
import org.openhab.binding.wizlighting.internal.entities.Param;
import org.openhab.binding.wizlighting.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SceneRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SpeedRequestParam;
import org.openhab.binding.wizlighting.internal.entities.StateRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SyncResponseParam;
import org.openhab.binding.wizlighting.internal.entities.WizLightingRequest;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;
import org.openhab.binding.wizlighting.internal.exceptions.MacAddressNotValidException;
import org.openhab.binding.wizlighting.internal.utils.ImageUtils;
import org.openhab.binding.wizlighting.internal.utils.ValidationUtils;
import org.openhab.binding.wizlighting.internal.utils.WizLightingPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
    private Long updateInterval = WizLightingBindingConstants.DEFAULT_REFRESH_INTERVAL;

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
            sendRequestPacket(WizLightingMethodType.registration, registrationInfo);
        }

        switch (channelUID.getId()) {
        case WizLightingBindingConstants.BULB_SWITCH_CHANNEL_ID:
            if (sendRequestPacket(WizLightingMethodType.setPilot, new StateRequestParam((OnOffType) command))) {
                updateState(WizLightingBindingConstants.BULB_SWITCH_CHANNEL_ID, (OnOffType) command);
            }
            break;

        case WizLightingBindingConstants.BULB_COLOR_CHANNEL_ID:
            if (command instanceof HSBType) {
                HSBType hsbCommand = (HSBType) command;
                if (hsbCommand.getBrightness().intValue() == 0) {
                    if (sendRequestPacket(WizLightingMethodType.setPilot, new StateRequestParam(OnOffType.OFF))) {
                        updateState(WizLightingBindingConstants.BULB_SWITCH_CHANNEL_ID, (OnOffType) command);
                        updateState(WizLightingBindingConstants.BULB_COLOR_CHANNEL_ID, (HSBType) command);
                    }
                } else {
                    if (sendRequestPacket(WizLightingMethodType.setPilot, new ColorRequestParam((HSBType) command))) {
                        updateState(WizLightingBindingConstants.BULB_COLOR_CHANNEL_ID, (HSBType) command);
                    }
                }
            } else if (command instanceof PercentType) {
                if (sendRequestPacket(WizLightingMethodType.setPilot, new DimmingRequestParam(command))) {
                    updateState(WizLightingBindingConstants.BULB_COLOR_CHANNEL_ID, (HSBType) command);
                }
            } else if (command instanceof OnOffType) {
                if (sendRequestPacket(WizLightingMethodType.setPilot, new StateRequestParam((OnOffType) command))) {
                    updateState(WizLightingBindingConstants.BULB_SWITCH_CHANNEL_ID, (OnOffType) command);
                    updateState(WizLightingBindingConstants.BULB_COLOR_CHANNEL_ID, (HSBType) command);
                }
            }
            break;

        case WizLightingBindingConstants.BULB_SCENE_CHANNEL_ID:
            if (sendRequestPacket(WizLightingMethodType.setPilot, new SceneRequestParam(command))) {
                updateState(WizLightingBindingConstants.BULB_SCENE_CHANNEL_ID, (StringType) command);
            }
            break;

        case WizLightingBindingConstants.BULB_SPEED_CHANNEL_ID:
            if (command instanceof PercentType) {
                if (sendRequestPacket(WizLightingMethodType.setPilot, new SpeedRequestParam(command))) {
                    updateState(WizLightingBindingConstants.BULB_SPEED_CHANNEL_ID, (PercentType) command);

                }
            } else if (command instanceof OnOffType) {
                OnOffType onOffCommand = (OnOffType) command;
                if (onOffCommand.equals(OnOffType.ON)) {
                    if (sendRequestPacket(WizLightingMethodType.setPilot, new SpeedRequestParam(100))) {
                        updateState(WizLightingBindingConstants.BULB_SPEED_CHANNEL_ID, (OnOffType) command);
                    }
                } else {
                    if (sendRequestPacket(WizLightingMethodType.setPilot, new SpeedRequestParam(0))) {
                        updateState(WizLightingBindingConstants.BULB_SPEED_CHANNEL_ID, (OnOffType) command);
                    }
                }
            }
            break;
        }
    }

    @Override
    public void handleRemoval() {
        // stop update thread
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob == null) {
            return;
        }
        keepAliveJob.cancel(true);
        super.handleRemoval();
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
                        || (timePassedFromLastUpdateInSeconds > (updateInterval * 4));
                if (considerThingOffline) {
                    logger.debug(
                            "Since no updates have been received from mac address {}, setting its status to OFFLINE.",
                            getBulbMacAddress());
                    updateStatus(ThingStatus.OFFLINE);
                }

                // request registration to get heartbeat (hb) status updates
                sendRequestPacket(WizLightingMethodType.registration, registrationInfo);

            }
        };
        this.keepAliveJob = scheduler.scheduleWithFixedDelay(runnable, 1, updateInterval, TimeUnit.SECONDS);
    }

    @Override
    public void initialize() {
        initGetStatusAndKeepAliveThread();
        updateStatus(ThingStatus.ONLINE);
        saveConfigurationsUsingCurrentStates();
    }

    /**
     * Method called by {@link WizLightingMediator} when one new message has been
     * received for this handler.
     *
     * @param receivedMessage the received {@link WizLightingResponse}.
     */
    public void newReceivedResponseMessage(final WizLightingResponse receivedMessage) {
        SyncResponseParam params = receivedMessage.getParams();

        updateStatus(ThingStatus.ONLINE);
        latestUpdate = System.currentTimeMillis();

        if (params != null) {
            // update on-off switch channel
            updateState(WizLightingBindingConstants.BULB_SWITCH_CHANNEL_ID, OnOffType.from(params.state));

            // update scene channel
            updateState(WizLightingBindingConstants.BULB_SCENE_CHANNEL_ID,
                    new StringType(String.valueOf(params.sceneId)));

            // update speed channel
            updateState(WizLightingBindingConstants.BULB_SPEED_CHANNEL_ID, new PercentType(params.speed));

            // check whether the bulb sent temperature
            HSBType colorHSB = null;
            if (params.temp != 0) {
                Color color = ImageUtils.getRGBFromK(params.temp);
                colorHSB = HSBType.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
            } else {
                colorHSB = HSBType.fromRGB(params.r, params.g, params.b);
            }

            // update color channel
            updateState(WizLightingBindingConstants.BULB_COLOR_CHANNEL_ID, colorHSB);

            // update signal strength
            if (params.rssi != 0) {
                updateState(WizLightingBindingConstants.BULB_RSSI_CHANNEL_ID, new DecimalType(params.rssi));
            }
        }
    }

    /**
     * Saves the bulb address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void savebulbIpAddressFromConfiguration(final Configuration configuration) {
        bulbIpAddress = String.valueOf(configuration.get(WizLightingBindingConstants.BULB_IP_ADDRESS_ARG));
        logger.info("Bulb IP Address set to '{}'", bulbIpAddress);
    }

    /**
     * Saves the bulb address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveUpdateIntervalFromConfiguration(final Configuration configuration) {
        updateInterval = WizLightingBindingConstants.DEFAULT_REFRESH_INTERVAL;
        if ((configuration.get(WizLightingBindingConstants.UPDATE_INTERVAL_ARG) instanceof BigDecimal)
                && (((BigDecimal) configuration.get(WizLightingBindingConstants.UPDATE_INTERVAL_ARG))
                        .longValue() > 0)) {
            updateInterval = ((BigDecimal) configuration.get(WizLightingBindingConstants.UPDATE_INTERVAL_ARG))
                    .longValue();
        }
    }

    /**
     * Saves the bulb's mac address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void savebulbMacAddressFromConfiguration(final Configuration configuration)
            throws MacAddressNotValidException {
        String bulbMacAddress = String.valueOf(configuration.get(WizLightingBindingConstants.BULB_MAC_ADDRESS_ARG));

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
        if (configuration.get(WizLightingBindingConstants.HOME_ID_ARG) != null) {
            String homeIdStr = (String) configuration.get(WizLightingBindingConstants.HOME_ID_ARG);
            this.homeId = Integer.parseInt(homeIdStr);
            logger.info("HomeId set to '{}'", this.homeId);
        }
    }

    /**
     * Sends {@link WizLightingRequest} to the passed {@link InetAddress}.
     *
     * @param requestPacket the {@link WizLightingRequest}.
     * @param address       the {@link InetAddress}.
     */
    private boolean sendRequestPacket(final WizLightingMethodType method, final Param param) {
        DatagramSocket dsocket = null;
        try {
            InetAddress address = InetAddress.getByName(bulbIpAddress);
            if (address != null) {
                WizLightingRequest request = new WizLightingRequest(method, param);
                request.setId(requestId++);

                byte[] message = this.converter.transformToByteMessage(request);
                logger.trace("Packet to send: {}", message);

                // Initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(message, message.length, address,
                        WizLightingBindingConstants.BULB_DEFAULT_UDP_PORT);

                // Create a datagram socket, send the packet through it, close it.
                dsocket = new DatagramSocket();
                dsocket.send(packet);
                logger.debug("Sent packet to address: {} and port {}", address,
                        WizLightingBindingConstants.BULB_DEFAULT_UDP_PORT);

                byte[] responseMessage = new byte[1024];
                packet = new DatagramPacket(responseMessage, responseMessage.length);
                dsocket.receive(packet);

                WizLightingResponse response = converter.transformSyncResponsePacket(packet);
                if (response.getResult() != null) {
                    updateStatus(ThingStatus.ONLINE);
                    return response.getResult().success;
                }
            }
        } catch (IOException exception) {
            logger.debug("Something wrong happen sending the packet to address: {} and port {}... msg: {}",
                    bulbIpAddress, WizLightingBindingConstants.BULB_DEFAULT_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }
        }
        return false;
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
        map.put(WizLightingBindingConstants.BULB_MAC_ADDRESS_ARG, this.bulbMacAddress);
        map.put(WizLightingBindingConstants.BULB_IP_ADDRESS_ARG, this.bulbIpAddress);
        map.put(WizLightingBindingConstants.UPDATE_INTERVAL_ARG, this.updateInterval);
        map.put(WizLightingBindingConstants.HOME_ID_ARG, String.valueOf(this.homeId));

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
