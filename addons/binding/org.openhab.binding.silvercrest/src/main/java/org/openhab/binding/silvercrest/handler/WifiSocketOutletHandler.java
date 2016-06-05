/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrest.handler;

import static org.openhab.binding.silvercrest.SilvercrestBindingConstants.SOCKET_TYPE_CHANNEL_ID;

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
import org.openhab.binding.silvercrest.SilvercrestBindingConstants;
import org.openhab.binding.silvercrest.exceptions.MacAddressNotValidException;
import org.openhab.binding.silvercrest.utils.NetworkUtils;
import org.openhab.binding.silvercrest.utils.ValidationUtils;
import org.openhab.binding.silvercrest.wifisocketoutlet.entities.WifiSocketOutletRequest;
import org.openhab.binding.silvercrest.wifisocketoutlet.entities.WifiSocketOutletResponse;
import org.openhab.binding.silvercrest.wifisocketoutlet.enums.WifiSocketOutletRequestType;
import org.openhab.binding.silvercrest.wifisocketoutlet.utils.WifiSocketOutletPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WifiSocketOutletHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jaime Vaz - Initial contribution
 */
public class WifiSocketOutletHandler extends BaseThingHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WifiSocketOutletHandler.class);

    private String hostAddress;
    private String macAddress;
    private Long updateInterval = SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_DEFAULT_REFRESH_INTERVAL;

    private final WifiSocketOutletPacketConverter converter = new WifiSocketOutletPacketConverter();
    private ScheduledFuture<?> keepAliveJob;
    private long latestUpdate = -1;

    /**
     * Default constructor.
     *
     * @param thing the thing of the handler.
     * @throws MacAddressNotValidException if the mac address isn't valid.
     */
    public WifiSocketOutletHandler(final Thing thing) throws MacAddressNotValidException {
        super(thing);
        this.saveMacAddressFromConfiguration(this.getConfig());
        this.saveHostAddressFromConfiguration(this.getConfig());
        this.saveUpdateIntervalFromConfiguration(this.getConfig());
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (channelUID.getId().equals(SOCKET_TYPE_CHANNEL_ID)) {
            LOG.debug("Silvercrest socket command received: {}", command);
            if (OnOffType.ON.name().equals(command.toString())) {
                this.sendCommand(WifiSocketOutletRequestType.ON);
            } else if (OnOffType.OFF.name().equals(command.toString())) {
                this.sendCommand(WifiSocketOutletRequestType.OFF);
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
     * Starts one thread that querys the state of the socket outlet, after the defined refresh interval.
     */
    private void initGetStatusAndKeepAliveThread() {
        if (this.keepAliveJob != null) {
            this.keepAliveJob.cancel(true);
        }
        // try with handler port if is null
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LOG.debug("Invoked the start of status and keep alive thread. current update interval: {}",
                        WifiSocketOutletHandler.this.updateInterval);

                long timePassedFromLastUpdateInSeconds = (System.currentTimeMillis()
                        - WifiSocketOutletHandler.this.latestUpdate) / 1000;

                LOG.debug("It has been passed {} seconds since the latest update on thing with mac address {}.",
                        timePassedFromLastUpdateInSeconds, WifiSocketOutletHandler.this.macAddress);

                boolean mustUpdateHostAddress = timePassedFromLastUpdateInSeconds > (WifiSocketOutletHandler.this.updateInterval
                        * 2);

                if (mustUpdateHostAddress) {
                    WifiSocketOutletHandler.LOG.debug(
                            "No updates have been received for a long time, search the mac address {} in network...",
                            WifiSocketOutletHandler.this.getMacAddress());
                    WifiSocketOutletHandler.this.lookupForOutletHostAddress();
                }

                boolean considerThingOffline = (WifiSocketOutletHandler.this.latestUpdate < 0)
                        || (timePassedFromLastUpdateInSeconds > (WifiSocketOutletHandler.this.updateInterval * 4));
                if (considerThingOffline) {
                    WifiSocketOutletHandler.LOG.debug(
                            "No updates have been received for a long long time will put the thing with mac address {} OFFLINE.",
                            WifiSocketOutletHandler.this.getMacAddress());
                    WifiSocketOutletHandler.this.updateStatus(ThingStatus.OFFLINE);
                }

                // request gpio status
                WifiSocketOutletHandler.this.sendCommand(WifiSocketOutletRequestType.GPIO_STATUS);

            }
        };
        this.keepAliveJob = this.scheduler.scheduleAtFixedRate(runnable, 1, WifiSocketOutletHandler.this.updateInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void initialize() {
        this.initGetStatusAndKeepAliveThread();
        super.initialize();
        this.saveConfigurationsUsingCurrentStates();
    }

    /**
     * Lookup for outlet host address, by sending one broadcast discovery message. Eventually the socket will respond to
     * the message. When the mediator receives the message, it will set the host address in this handler, for future
     * communications.
     */
    private void lookupForOutletHostAddress() {
        WifiSocketOutletRequest requestPacket = new WifiSocketOutletRequest(this.macAddress,
                WifiSocketOutletRequestType.DISCOVERY);

        for (InetAddress broadcastAddressFound : NetworkUtils.getAllBroadcastAddresses()) {
            LOG.debug("Will query for device with mac address {} in network with broadcast address {}", this.macAddress,
                    broadcastAddressFound);
            this.sendRequestPacket(requestPacket, broadcastAddressFound);
        }
    }

    /**
     * Method called by {@link WifiSocketOutletMediator} when one new message has been received for this handler.
     *
     * @param receivedMessage the received {@link WifiSocketOutletResponse}.
     */
    public void newReceivedResponseMessage(final WifiSocketOutletResponse receivedMessage) {
        // if the host of the packet is different that the host address setted in handler, update the host
        // address.
        if (!receivedMessage.getHostAddress().equals(this.hostAddress)) {
            LOG.debug(
                    "The host of the packet is different that the host address setted in handler. "
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
                this.updateState(SilvercrestBindingConstants.SOCKET_TYPE_CHANNEL_ID, OnOffType.OFF);
                break;
            case ON:
                this.updateState(SilvercrestBindingConstants.SOCKET_TYPE_CHANNEL_ID, OnOffType.ON);
                break;
            default:
                LOG.debug("Command not found!");
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
        if ((configuration != null) && (configuration.get(SilvercrestBindingConstants.HOST_ADDRESS_ARG) != null)) {
            this.hostAddress = String.valueOf(configuration.get(SilvercrestBindingConstants.HOST_ADDRESS_ARG));
        }
    }

    /**
     * Saves the host address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveUpdateIntervalFromConfiguration(final Configuration configuration) {
        this.updateInterval = SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_DEFAULT_REFRESH_INTERVAL;
        if ((configuration != null)
                && (configuration
                        .get(SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_UPDATE_INTERVAL_ARG) instanceof BigDecimal)
                && (((BigDecimal) configuration.get(SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_UPDATE_INTERVAL_ARG))
                        .longValue() > 0)) {
            this.updateInterval = ((BigDecimal) configuration
                    .get(SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_UPDATE_INTERVAL_ARG)).longValue();
        }
    }

    /**
     * Saves the mac address from configuration in field.
     *
     * @param configuration The {@link Configuration}
     */
    private void saveMacAddressFromConfiguration(final Configuration configuration) throws MacAddressNotValidException {
        if ((configuration != null) && (configuration.get(SilvercrestBindingConstants.MAC_ADDRESS_ARG) != null)) {
            String macAddress = String.valueOf(configuration.get(SilvercrestBindingConstants.MAC_ADDRESS_ARG));
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
     * Sends one command to the Wifi Socket Outlet. If the host address is not setted, it will trigger the lookup of the
     * host address and discard the command queried.
     *
     * @param type the {@link WifiSocketOutletRequestType} of the command.
     */
    private void sendCommand(final WifiSocketOutletRequestType type) {
        LOG.debug("Send command for mac addr: {} with type: {} with hostaddress: {}", this.getMacAddress(), type.name(),
                this.hostAddress);
        if (this.hostAddress == null) {
            LOG.debug(
                    "Send command cannot proceed until one Host Address is setted for mac address: {} Will invoque one mac address lookup!",
                    this.macAddress);
            this.lookupForOutletHostAddress();
        } else {
            InetAddress address;
            try {
                address = InetAddress.getByName(this.hostAddress);
                this.sendRequestPacket(new WifiSocketOutletRequest(this.macAddress, type), address);
            } catch (UnknownHostException e) {
                LOG.debug("Host Address not found: {}. Will lookup Mac address.");
                this.hostAddress = null;
                this.lookupForOutletHostAddress();
            }
        }
    }

    /**
     * Sends {@link WifiSocketOutletRequest} to the passed {@link InetAddress}.
     *
     * @param requestPacket the {@link WifiSocketOutletRequest}.
     * @param address the {@link InetAddress}.
     */
    private void sendRequestPacket(final WifiSocketOutletRequest requestPacket, final InetAddress address) {
        try {
            if (address != null) {
                byte[] message = this.converter.transformToByteMessage(requestPacket);
                LOG.trace("Preparing packet to send...");
                // Initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(message, message.length, address,
                        SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_DEFAULT_UDP_PORT);

                // Create a datagram socket, send the packet through it, close it.
                DatagramSocket dsocket = new DatagramSocket();

                dsocket.send(packet);
                dsocket.close();
                LOG.debug("Sent packet to address: {} and port {}", address,
                        SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_DEFAULT_UDP_PORT);
            }
        } catch (Exception e) {
            LOG.debug("Something wrong happen sending the packet to address: {} and port {}... msg: {}", address,
                    SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_DEFAULT_UDP_PORT, e.getMessage());
        }
    }

    @Override
    protected void updateConfiguration(final Configuration configuration) {
        try {
            this.latestUpdate = -1;
            this.updateStatus(ThingStatus.INITIALIZING);

            this.saveMacAddressFromConfiguration(configuration);

            this.hostAddress = null;
            this.saveHostAddressFromConfiguration(configuration);
            if (this.hostAddress == null) {
                this.lookupForOutletHostAddress();
            }
            this.saveUpdateIntervalFromConfiguration(configuration);

            this.initGetStatusAndKeepAliveThread();
            this.saveConfigurationsUsingCurrentStates();
        } catch (MacAddressNotValidException e) {
            LOG.error("The Mac address passed is not valid! {}", e.getMacAddress());
            this.updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR);
        }

    }

    /**
     * Save the current runtime configuration of the handler in configuration mechanism.
     */
    private void saveConfigurationsUsingCurrentStates() {
        Map<String, Object> map = new HashMap<>();
        map.put(SilvercrestBindingConstants.MAC_ADDRESS_ARG, this.macAddress);
        map.put(SilvercrestBindingConstants.HOST_ADDRESS_ARG, this.hostAddress);
        map.put(SilvercrestBindingConstants.WIFI_SOCKET_OUTLET_UPDATE_INTERVAL_ARG, this.updateInterval);

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
}
