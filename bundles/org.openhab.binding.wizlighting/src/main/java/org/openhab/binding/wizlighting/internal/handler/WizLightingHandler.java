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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.net.NetworkAddressService;
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
import org.openhab.binding.wizlighting.internal.entities.WizLightingSyncResponse;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;
import org.openhab.binding.wizlighting.internal.exceptions.MacAddressNotValidException;
import org.openhab.binding.wizlighting.internal.utils.ImageUtils;
import org.openhab.binding.wizlighting.internal.utils.ValidationUtils;
import org.openhab.binding.wizlighting.internal.utils.NetworkUtils;
import org.openhab.binding.wizlighting.internal.utils.WizLightingPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link WizLightingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@NonNullByDefault
public class WizLightingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WizLightingHandler.class);

    private @Nullable NetworkAddressService networkAddressService;
    private @Nullable String myIpAddress;
    private @Nullable String myMacAddress;

    private String bulbIpAddress = "bulbIpAddress";
    private String bulbMacAddress = "bulbMacAddress";
    private int homeId;
    private Long updateInterval = WizLightingBindingConstants.DEFAULT_REFRESH_INTERVAL;

    private final WizLightingPacketConverter converter = new WizLightingPacketConverter();
    private @Nullable ScheduledFuture<?> keepAliveJob;
    private long latestUpdate = -1;
    private @Nullable RegistrationRequestParam registrationInfo;
    private int requestId = 0;

    /**
     * Default constructor.
     *
     * @param thing the thing of the handler.
     * @throws MacAddressNotValidException if the mac address isn't valid.
     */
    public WizLightingHandler(final Thing thing)
            throws MacAddressNotValidException {
        super(thing);

        savebulbMacAddressFromConfiguration(this.getConfig());
        savebulbIpAddressFromConfiguration(this.getConfig());
        saveHomeIdFromConfiguration(this.getConfig());
        saveUpdateIntervalFromConfiguration(this.getConfig());
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            return;
        }

        switch (channelUID.getId()) {
            case WizLightingBindingConstants.BULB_SWITCH_CHANNEL_ID:
                sendRequestPacket(WizLightingMethodType.setPilot, new StateRequestParam((OnOffType) command));
                break;

            case WizLightingBindingConstants.BULB_COLOR_CHANNEL_ID:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        sendRequestPacket(WizLightingMethodType.setPilot, new StateRequestParam(OnOffType.OFF));
                    } else {
                        sendRequestPacket(WizLightingMethodType.setPilot, new ColorRequestParam((HSBType) command));
                    }
                } else if (command instanceof PercentType) {
                    sendRequestPacket(WizLightingMethodType.setPilot, new DimmingRequestParam(command));
                } else if (command instanceof OnOffType) {
                    sendRequestPacket(WizLightingMethodType.setPilot, new StateRequestParam((OnOffType) command));
                }
                break;

            case WizLightingBindingConstants.BULB_SCENE_CHANNEL_ID:
                sendRequestPacket(WizLightingMethodType.setPilot, new SceneRequestParam(command));
                break;

            case WizLightingBindingConstants.BULB_SPEED_CHANNEL_ID:
                if (command instanceof PercentType) {
                    sendRequestPacket(WizLightingMethodType.setPilot, new SpeedRequestParam(command));
                } else if (command instanceof OnOffType) {
                    OnOffType onOffCommand = (OnOffType) command;
                    if (onOffCommand.equals(OnOffType.ON)) {
                        sendRequestPacket(WizLightingMethodType.setPilot, new SpeedRequestParam(100));
                    } else {
                        sendRequestPacket(WizLightingMethodType.setPilot, new SpeedRequestParam(0));
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
     * Starts one thread that querys the state of the socket, after the defined refresh interval.
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

                logger.trace("Latest Update: {} Now: {} Delta: {} seconds", latestUpdate, now,
                        timePassedFromLastUpdateInSeconds);

                logger.debug("It has been passed {} seconds since the last update on socket with mac address {}.",
                        timePassedFromLastUpdateInSeconds, bulbMacAddress);

                boolean mustUpdateBulbIpAddress = timePassedFromLastUpdateInSeconds > (updateInterval * 2);

                if (mustUpdateBulbIpAddress) {
                    logger.debug(
                            "No updates have been received for a long time, searching for the mac address {} in network...",
                            getBulbMacAddress());
                }

                boolean considerThingOffline = (latestUpdate < 0)
                        || (timePassedFromLastUpdateInSeconds > (updateInterval * 4));
                if (considerThingOffline) {
                    logger.debug(
                            "Since no updates have been received from mac address {}, setting its status to OFFLINE.",
                            getBulbMacAddress());
                    updateStatus(ThingStatus.OFFLINE);
                }

                @Nullable
                String myIp = getMyIpAddress();
                @Nullable
                String myMac = getMyMacAddress();

                if (myIp != null && myMac != null) {
                    logger.debug("Setting request for sync from host {} and mac {}", myIp, myMac);
                    registrationInfo = new RegistrationRequestParam(myIp, true, homeId, myMac);
                }

                RegistrationRequestParam regisInfo = registrationInfo;
                if (regisInfo != null) {
                    // request gpio status
                    sendRequestPacket(WizLightingMethodType.registration, regisInfo);
                }
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
     * Method called by {@link WizLightingMediator} when one new message has been received for this handler.
     *
     * @param receivedMessage the received {@link WizLightingSyncResponse}.
     */
    public void newReceivedResponseMessage(final WizLightingSyncResponse receivedMessage) {
        SyncResponseParam params = receivedMessage.getParams();

        if (params != null) {
            // update on-off switch channel
            updateState(WizLightingBindingConstants.BULB_SWITCH_CHANNEL_ID, OnOffType.from(params.state));

            // update scene channel
            updateState(WizLightingBindingConstants.BULB_SCENE_CHANNEL_ID, new StringType(String.valueOf(params.sceneId)));

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

            updateStatus(ThingStatus.ONLINE);
            latestUpdate = System.currentTimeMillis();
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
    private void savebulbMacAddressFromConfiguration(final Configuration configuration) throws MacAddressNotValidException {
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
     * @param address the {@link InetAddress}.
     */
    private @Nullable WizLightingSyncResponse sendRequestPacket(final WizLightingMethodType method, final Param param) {
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

                WizLightingSyncResponse response = converter.transformSyncResponsePacket(packet);
                return response;
            }
        } catch (IOException exception) {
            logger.debug("Something wrong happen sending the packet to address: {} and port {}... msg: {}", bulbIpAddress,
                    WizLightingBindingConstants.BULB_DEFAULT_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }
        }
        return null;
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
     * Save the current runtime configuration of the handler in configuration mechanism.
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

    private @Nullable String getMyIpAddress() {
        NetworkAddressService networkAddressService = this.networkAddressService;
        String ohIpAddress = String.valueOf(WizLightingBindingConstants.OH_IP_ADDRESS_ARG);
        if (myIpAddress != null) {
            return myIpAddress;
        } else if (ohIpAddress != null) {
            return ohIpAddress;
        } else if (networkAddressService != null) {
            myIpAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (myIpAddress == null) {
                logger.warn("No network interface could be found.  IP of OpenHab device is unknown");
                return null;
            }
            logger.info("IP of OpenHab device is {}.", myIpAddress);
            return myIpAddress;
        } else {
            return null;
        }
    }

    private @Nullable String getMyMacAddress() {
        String ohMacAddress = String.valueOf(WizLightingBindingConstants.OH_MAC_ADDRESS_ARG);
        if (myMacAddress != null) {
            return myMacAddress;
        } else if (ohMacAddress != null && ValidationUtils.isMacValid(ohMacAddress)) {
            logger.info("Mac Address of OpenHab device is {}.", ohMacAddress);
            return ohMacAddress;
        } else {
            try {
                myMacAddress = NetworkUtils.getMyMacAddress();
                if (myMacAddress == null) {
                    logger.warn("No network interface could be found.  Mac of OpenHab device is unknown.");
                    return null;
                }
            } catch (Exception e) {
                logger.warn("Mac Address of OpenHab device is invalid.");
                return null;
            }
            logger.info("Mac Address of OpenHab device is {}.", myMacAddress);
            return myMacAddress;
        }
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
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
