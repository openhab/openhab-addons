/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrestwifisocket.handler;

import static org.openhab.binding.silvercrestwifisocket.SilvercrestWifiSocketBindingConstants.WIFI_SOCKET_CHANNEL_ID;

import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.silvercrestwifisocket.SilvercrestWifiSocketBindingConstants;
import org.openhab.binding.silvercrestwifisocket.internal.entities.SilvercrestWifiSocketRequest;
import org.openhab.binding.silvercrestwifisocket.internal.entities.SilvercrestWifiSocketResponse;
import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketRequestType;
import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketVendor;
import org.openhab.binding.silvercrestwifisocket.internal.exceptions.MacAddressNotValidException;
import org.openhab.binding.silvercrestwifisocket.internal.utils.NetworkUtils;
import org.openhab.binding.silvercrestwifisocket.internal.utils.ValidationUtils;
import org.openhab.binding.silvercrestwifisocket.internal.utils.WifiSocketPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SilvercrestWifiSocketHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jaime Vaz - Initial contribution
 * @author Christian Heimerl - for integration of EasyHome
 */
public class SilvercrestWifiSocketHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SilvercrestWifiSocketHandler.class);

    private String hostAddress;
    private String macAddress;
    private SilvercrestWifiSocketVendor vendor = SilvercrestWifiSocketBindingConstants.DEFAULT_VENDOR;
    private Long updateInterval = SilvercrestWifiSocketBindingConstants.DEFAULT_REFRESH_INTERVAL;

    private final WifiSocketPacketConverter converter = new WifiSocketPacketConverter();
    private ScheduledFuture<?> keepAliveJob;
    private long latestUpdate = -1;

    /**
     * Default constructor.
     *
     * @param thing the thing of the handler.
     * @throws MacAddressNotValidException if the mac address isn't valid.
     */
    public SilvercrestWifiSocketHandler(final Thing thing) throws MacAddressNotValidException {
        super(thing);
        this.saveMacAddressFromConfiguration(this.getConfig());
        this.saveHostAddressFromConfiguration(this.getConfig());
        this.saveUpdateIntervalFromConfiguration(this.getConfig());
        this.saveVendorFromConfiguration(this.getConfig());
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (channelUID.getId().equals(WIFI_SOCKET_CHANNEL_ID)) {
            logger.debug("Silvercrest socket command received: {}", command);

            if (command == OnOffType.ON) {
                this.sendCommand(SilvercrestWifiSocketRequestType.ON);

            } else if (command == OnOffType.OFF) {
                this.sendCommand(SilvercrestWifiSocketRequestType.OFF);

            } else if (command == RefreshType.REFRESH) {
                this.sendCommand(SilvercrestWifiSocketRequestType.GPIO_STATUS);
            }
        }
    }

    @Override
    public void handleRemoval() {
        // stop update thread
        this.keepAliveJob.cancel(true);
        super.handleRemoval();
    }

    /**
     * Starts one thread that querys the state of the socket, after the defined refresh interval.
     */
    private void initGetStatusAndKeepAliveThread() {
        if (this.keepAliveJob != null) {
            this.keepAliveJob.cancel(true);
        }
        // try with handler port if is null
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.debug(
                        "Begin of Socket keep alive thread routine. Current configuration update interval: {} seconds.",
                        SilvercrestWifiSocketHandler.this.updateInterval);

                long now = System.currentTimeMillis();
                long timePassedFromLastUpdateInSeconds = (now - SilvercrestWifiSocketHandler.this.latestUpdate) / 1000;

                logger.trace("Latest Update: {} Now: {} Delta: {} seconds",
                        SilvercrestWifiSocketHandler.this.latestUpdate, now, timePassedFromLastUpdateInSeconds);

                logger.debug("It has been passed {} seconds since the last update on socket with mac address {}.",
                        timePassedFromLastUpdateInSeconds, SilvercrestWifiSocketHandler.this.macAddress);

                boolean mustUpdateHostAddress = timePassedFromLastUpdateInSeconds > (SilvercrestWifiSocketHandler.this.updateInterval
                        * 2);

                if (mustUpdateHostAddress) {
                    logger.debug(
                            "No updates have been received for a long time, search the mac address {} in network...",
                            SilvercrestWifiSocketHandler.this.getMacAddress());
                    SilvercrestWifiSocketHandler.this.lookupForSocketHostAddress();
                }

                boolean considerThingOffline = (SilvercrestWifiSocketHandler.this.latestUpdate < 0)
                        || (timePassedFromLastUpdateInSeconds > (SilvercrestWifiSocketHandler.this.updateInterval * 4));
                if (considerThingOffline) {
                    logger.debug(
                            "No updates have been received for a long long time will put the thing with mac address {} OFFLINE.",
                            SilvercrestWifiSocketHandler.this.getMacAddress());
                    SilvercrestWifiSocketHandler.this.updateStatus(ThingStatus.OFFLINE);
                }

                // request gpio status
                SilvercrestWifiSocketHandler.this.sendCommand(SilvercrestWifiSocketRequestType.GPIO_STATUS);
            }
        };
        this.keepAliveJob = this.scheduler.scheduleWithFixedDelay(runnable, 1,
                SilvercrestWifiSocketHandler.this.updateInterval, TimeUnit.SECONDS);
    }

    @Override
    public void initialize() {
        this.initGetStatusAndKeepAliveThread();
        updateStatus(ThingStatus.ONLINE);
        this.saveConfigurationsUsingCurrentStates();
    }

    /**
     * Lookup for socket host address, by sending one broadcast discovery message. Eventually the socket will respond to
     * the message. When the mediator receives the message, it will set the host address in this handler, for future
     * communications.
     */
    private void lookupForSocketHostAddress() {
        SilvercrestWifiSocketRequest requestPacket = new SilvercrestWifiSocketRequest(this.macAddress,
                SilvercrestWifiSocketRequestType.DISCOVERY, this.vendor);

        for (InetAddress broadcastAddressFound : NetworkUtils.getAllBroadcastAddresses()) {
            logger.debug("Will query for device with mac address {} in network with broadcast address {}",
                    this.macAddress, broadcastAddressFound);
            this.sendRequestPacket(requestPacket, broadcastAddressFound);
        }
    }

    /**
     * Method called by {@link SilvercrestWifiSocketMediator} when one new message has been received for this handler.
     *
     * @param receivedMessage the received {@link SilvercrestWifiSocketResponse}.
     */
    public void newReceivedResponseMessage(final SilvercrestWifiSocketResponse receivedMessage) {
        // if the host of the packet is different from the host address set in handler, update the host
        // address.
        if (!receivedMessage.getHostAddress().equals(this.hostAddress)) {
            logger.debug(
                    "The host of the packet is different from the host address set in handler. "
                            + "Will update the host address. handler of mac: {}. "
                            + "Old host address: '{}' -> new host address: '{}'",
                    this.macAddress, this.hostAddress, receivedMessage.getHostAddress());

            this.hostAddress = receivedMessage.getHostAddress();
            this.saveConfigurationsUsingCurrentStates();
        }

        switch (receivedMessage.getType()) {
            case ACK:
                break;
            case DISCOVERY:
                break;
            case OFF:
                this.updateState(SilvercrestWifiSocketBindingConstants.WIFI_SOCKET_CHANNEL_ID, OnOffType.OFF);
                break;
            case ON:
                this.updateState(SilvercrestWifiSocketBindingConstants.WIFI_SOCKET_CHANNEL_ID, OnOffType.ON);
                break;
            default:
                logger.debug("Command not found!");
                break;
        }

        this.updateStatus(ThingStatus.ONLINE);
        this.latestUpdate = System.currentTimeMillis();
    }

    /**
     * Saves the host address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveHostAddressFromConfiguration(final Configuration configuration) {
        if ((configuration != null)
                && (configuration.get(SilvercrestWifiSocketBindingConstants.HOST_ADDRESS_ARG) != null)) {
            this.hostAddress = String
                    .valueOf(configuration.get(SilvercrestWifiSocketBindingConstants.HOST_ADDRESS_ARG));
        }
    }

    /**
     * Saves the host address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveUpdateIntervalFromConfiguration(final Configuration configuration) {
        this.updateInterval = SilvercrestWifiSocketBindingConstants.DEFAULT_REFRESH_INTERVAL;
        if ((configuration != null)
                && (configuration.get(SilvercrestWifiSocketBindingConstants.UPDATE_INTERVAL_ARG) instanceof BigDecimal)
                && (((BigDecimal) configuration.get(SilvercrestWifiSocketBindingConstants.UPDATE_INTERVAL_ARG))
                        .longValue() > 0)) {
            this.updateInterval = ((BigDecimal) configuration
                    .get(SilvercrestWifiSocketBindingConstants.UPDATE_INTERVAL_ARG)).longValue();
        }
    }

    /**
     * Saves the mac address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveMacAddressFromConfiguration(final Configuration configuration) throws MacAddressNotValidException {
        if ((configuration != null)
                && (configuration.get(SilvercrestWifiSocketBindingConstants.MAC_ADDRESS_ARG) != null)) {
            String macAddress = String
                    .valueOf(configuration.get(SilvercrestWifiSocketBindingConstants.MAC_ADDRESS_ARG));
            if (ValidationUtils.isMacNotValid(macAddress)) {
                throw new MacAddressNotValidException("Mac address is not valid", macAddress);
            }
            this.macAddress = macAddress.replaceAll(":", "").toUpperCase();
        }
        if (this.macAddress == null) {
            throw new MacAddressNotValidException("Mac address is not valid", this.macAddress);
        }
    }

    /**
     * Saves the vendor from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveVendorFromConfiguration(final Configuration configuration) {
        if ((configuration != null) && (configuration.get(SilvercrestWifiSocketBindingConstants.VENDOR_ARG) != null)) {
            this.vendor = SilvercrestWifiSocketVendor
                    .valueOf(String.valueOf(configuration.get(SilvercrestWifiSocketBindingConstants.VENDOR_ARG)));
        }
    }

    /**
     * Sends one command to the Wifi Socket. If the host address is not set, it will trigger the lookup of the
     * host address and discard the command queried.
     *
     * @param type the {@link SilvercrestWifiSocketRequestType} of the command.
     */
    private void sendCommand(final SilvercrestWifiSocketRequestType type) {
        logger.debug("Send command for mac addr: {} with type: {} with hostaddress: {}", this.getMacAddress(),
                type.name(), this.hostAddress);
        if (this.hostAddress == null) {
            logger.debug(
                    "Send command cannot proceed until one Host Address is set for mac address: {} Will invoke one mac address lookup!",
                    this.macAddress);
            this.lookupForSocketHostAddress();
        } else {
            InetAddress address;
            try {
                address = InetAddress.getByName(this.hostAddress);
                this.sendRequestPacket(new SilvercrestWifiSocketRequest(this.macAddress, type, this.vendor), address);
            } catch (UnknownHostException e) {
                logger.debug("Host Address not found: {}. Will lookup Mac address.");
                this.hostAddress = null;
                this.lookupForSocketHostAddress();
            }
        }
    }

    /**
     * Sends {@link SilvercrestWifiSocketRequest} to the passed {@link InetAddress}.
     *
     * @param requestPacket the {@link SilvercrestWifiSocketRequest}.
     * @param address the {@link InetAddress}.
     */
    private void sendRequestPacket(final SilvercrestWifiSocketRequest requestPacket, final InetAddress address) {
        DatagramSocket dsocket = null;
        try {
            if (address != null) {
                byte[] message = this.converter.transformToByteMessage(requestPacket);
                logger.trace("Preparing packet to send...");
                // Initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(message, message.length, address,
                        SilvercrestWifiSocketBindingConstants.WIFI_SOCKET_DEFAULT_UDP_PORT);

                // Create a datagram socket, send the packet through it, close it.
                dsocket = new DatagramSocket();

                dsocket.send(packet);
                logger.debug("Sent packet to address: {} and port {}", address,
                        SilvercrestWifiSocketBindingConstants.WIFI_SOCKET_DEFAULT_UDP_PORT);
            }
        } catch (Exception exception) {
            logger.debug("Something wrong happen sending the packet to address: {} and port {}... msg: {}", address,
                    SilvercrestWifiSocketBindingConstants.WIFI_SOCKET_DEFAULT_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }

        }
    }

    @Override
    protected void updateConfiguration(final Configuration configuration) {
        try {
            this.latestUpdate = -1;

            this.saveMacAddressFromConfiguration(configuration);

            this.hostAddress = null;
            this.saveHostAddressFromConfiguration(configuration);
            if (this.hostAddress == null) {
                this.lookupForSocketHostAddress();
            }
            this.saveUpdateIntervalFromConfiguration(configuration);
            this.saveVendorFromConfiguration(configuration);

            this.initGetStatusAndKeepAliveThread();
            this.saveConfigurationsUsingCurrentStates();
        } catch (MacAddressNotValidException e) {
            logger.error("The Mac address passed is not valid! {}", e.getMacAddress());
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

    }

    /**
     * Save the current runtime configuration of the handler in configuration mechanism.
     */
    private void saveConfigurationsUsingCurrentStates() {
        Map<String, Object> map = new HashMap<>();
        map.put(SilvercrestWifiSocketBindingConstants.MAC_ADDRESS_ARG, this.macAddress);
        map.put(SilvercrestWifiSocketBindingConstants.HOST_ADDRESS_ARG, this.hostAddress);
        map.put(SilvercrestWifiSocketBindingConstants.VENDOR_ARG, this.vendor.toString());
        map.put(SilvercrestWifiSocketBindingConstants.UPDATE_INTERVAL_ARG, this.updateInterval);

        Configuration newConfiguration = new Configuration(map);
        super.updateConfiguration(newConfiguration);
    }

    // SETTERS AND GETTERS
    public String getHostAddress() {
        return this.hostAddress;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public SilvercrestWifiSocketVendor getVendor() {
        return this.vendor;
    }
}
