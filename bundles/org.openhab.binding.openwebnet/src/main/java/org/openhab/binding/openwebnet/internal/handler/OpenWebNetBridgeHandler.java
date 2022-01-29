/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetDeviceDiscoveryService;
import org.openhab.binding.openwebnet.internal.handler.config.OpenWebNetBusBridgeConfig;
import org.openhab.binding.openwebnet.internal.handler.config.OpenWebNetZigBeeBridgeConfig;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openwebnet4j.BUSGateway;
import org.openwebnet4j.GatewayListener;
import org.openwebnet4j.OpenDeviceType;
import org.openwebnet4j.OpenGateway;
import org.openwebnet4j.USBGateway;
import org.openwebnet4j.communication.OWNAuthException;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.Automation;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.CEN;
import org.openwebnet4j.message.EnergyManagement;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Thermoregulation;
import org.openwebnet4j.message.What;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetBridgeHandler} is responsible for handling communication with gateways and handling events.
 *
 * @author Massimo Valla - Initial contribution
 * @author Andrea Conte - Energy management, Thermoregulation
 * @author Gilberto Cocchi - Thermoregulation
 */
@NonNullByDefault
public class OpenWebNetBridgeHandler extends ConfigStatusBridgeHandler implements GatewayListener {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetBridgeHandler.class);

    private static final int GATEWAY_ONLINE_TIMEOUT_SEC = 20; // Time to wait for the gateway to become connected

    private static final int REFRESH_ALL_DEVICES_DELAY_MSEC = 500; // Delay to wait before sending all devices refresh
                                                                   // request after a connect/reconnect

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.BRIDGE_SUPPORTED_THING_TYPES;

    // ConcurrentHashMap of devices registered to this BridgeHandler
    // association is: ownId (String) -> OpenWebNetThingHandler, with ownId = WHO.WHERE
    private Map<String, @Nullable OpenWebNetThingHandler> registeredDevices = new ConcurrentHashMap<>();
    private Map<String, Long> discoveringDevices = new ConcurrentHashMap<>();

    protected @Nullable OpenGateway gateway;
    private boolean isBusGateway = false;

    private boolean isGatewayConnected = false;

    public @Nullable OpenWebNetDeviceDiscoveryService deviceDiscoveryService;
    private boolean reconnecting = false; // we are trying to reconnect to gateway
    private @Nullable ScheduledFuture<?> refreshSchedule;

    private boolean scanIsActive = false; // a device scan has been activated by OpenWebNetDeviceDiscoveryService;
    private boolean discoveryByActivation;

    public OpenWebNetBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public boolean isBusGateway() {
        return isBusGateway;
    }

    @Override
    public void initialize() {
        ThingTypeUID thingType = getThing().getThingTypeUID();
        OpenGateway gw;
        if (thingType.equals(THING_TYPE_ZB_GATEWAY)) {
            gw = initZigBeeGateway();
        } else {
            gw = initBusGateway();
            isBusGateway = true;
        }
        if (gw != null) {
            gateway = gw;
            gw.subscribe(this);
            if (gw.isConnected()) { // gateway is already connected, device can go ONLINE
                isGatewayConnected = true;
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.UNKNOWN);
                logger.debug("Trying to connect gateway {}... ", gw);
                try {
                    gw.connect();
                    scheduler.schedule(() -> {
                        // if status is still UNKNOWN after timer ends, set the device as OFFLINE
                        if (thing.getStatus().equals(ThingStatus.UNKNOWN)) {
                            logger.info("status still UNKNOWN. Setting device={} to OFFLINE", thing.getUID());
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                    "@text/offline.comm-error-timeout");
                        }
                    }, GATEWAY_ONLINE_TIMEOUT_SEC, TimeUnit.SECONDS);
                    logger.debug("bridge {} initialization completed", thing.getUID());
                } catch (OWNException e) {
                    logger.debug("gw.connect() returned OWNException: {}", e.getMessage());
                    // status is updated by callback onConnectionError()
                }
            }
        }
    }

    /**
     * Init a ZigBee gateway based on config
     */
    private @Nullable OpenGateway initZigBeeGateway() {
        logger.debug("Initializing ZigBee USB Gateway");
        OpenWebNetZigBeeBridgeConfig zbBridgeConfig = getConfigAs(OpenWebNetZigBeeBridgeConfig.class);
        String serialPort = zbBridgeConfig.getSerialPort();
        if (serialPort == null || serialPort.isEmpty()) {
            logger.warn("Cannot connect ZigBee USB Gateway. No serial port has been provided in Bridge configuration.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-serial-port");
            return null;
        } else {
            return new USBGateway(serialPort);
        }
    }

    /**
     * Init a BUS gateway based on config
     */
    private @Nullable OpenGateway initBusGateway() {
        logger.debug("Initializing BUS gateway");
        OpenWebNetBusBridgeConfig busBridgeConfig = getConfigAs(OpenWebNetBusBridgeConfig.class);
        String host = busBridgeConfig.getHost();
        if (host == null || host.isEmpty()) {
            logger.warn("Cannot connect to BUS Gateway. No host/IP has been provided in Bridge configuration.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
            return null;
        } else {
            int port = busBridgeConfig.getPort().intValue();
            String passwd = busBridgeConfig.getPasswd();
            String passwdMasked;
            if (passwd.length() >= 4) {
                passwdMasked = "******" + passwd.substring(passwd.length() - 3, passwd.length());
            } else {
                passwdMasked = "******";
            }
            discoveryByActivation = busBridgeConfig.getDiscoveryByActivation();
            logger.debug("Creating new BUS gateway with config properties: {}:{}, pwd={}, discoveryByActivation={}",
                    host, port, passwdMasked, discoveryByActivation);
            return new BUSGateway(host, port, passwd);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand (command={} - channel={})", command, channelUID);
        OpenGateway gw = gateway;
        if (gw == null || !gw.isConnected()) {
            logger.warn("Gateway is NOT connected, skipping command");
            return;
        } else {
            if (command instanceof RefreshType) {
                refreshAllDevices();
            } else {
                logger.warn("Command or channel not supported: channel={} command={}", channelUID, command);
            }
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList();
    }

    @Override
    public void handleRemoval() {
        disconnectGateway();
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> rSc = refreshSchedule;
        if (rSc != null) {
            rSc.cancel(true);
        }
        disconnectGateway();
        super.dispose();
    }

    private void disconnectGateway() {
        OpenGateway gw = gateway;
        if (gw != null) {
            gw.closeConnection();
            gw.unsubscribe(this);
            logger.debug("Gateway {} connection closed and unsubscribed", gw.toString());
            gateway = null;
        }
        reconnecting = false;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(OpenWebNetDeviceDiscoveryService.class);
    }

    /**
     * Search for devices connected to this bridge handler's gateway
     *
     * @param listener to receive device found notifications
     */
    public synchronized void searchDevices() {
        scanIsActive = true;
        logger.debug("------$$ scanIsActive={}", scanIsActive);
        OpenGateway gw = gateway;
        if (gw != null) {
            if (!gw.isDiscovering()) {
                if (!gw.isConnected()) {
                    logger.debug("------$$ Gateway '{}' is NOT connected, cannot search for devices", gw);
                    return;
                }
                logger.info("------$$ STARTED active SEARCH for devices on bridge '{}'", thing.getUID());
                try {
                    gw.discoverDevices();
                } catch (OWNException e) {
                    logger.warn("------$$ OWNException while discovering devices on bridge '{}': {}", thing.getUID(),
                            e.getMessage());
                }
            } else {
                logger.debug("------$$ Searching devices on bridge '{}' already activated", thing.getUID());
                return;
            }
        } else {
            logger.warn("------$$ Cannot search devices: no gateway associated to this handler");
        }
    }

    @Override
    public void onNewDevice(@Nullable Where w, @Nullable OpenDeviceType deviceType, @Nullable BaseOpenMessage message) {
        OpenWebNetDeviceDiscoveryService discService = deviceDiscoveryService;
        if (discService != null) {
            if (w != null && deviceType != null) {
                discService.newDiscoveryResult(w, deviceType, message);
            } else {
                logger.warn("onNewDevice with null where/deviceType, msg={}", message);
            }
        } else {
            logger.warn("onNewDevice but null deviceDiscoveryService");
        }
    }

    @Override
    public void onDiscoveryCompleted() {
        logger.info("------$$ FINISHED active SEARCH for devices on bridge '{}'", thing.getUID());
    }

    /**
     * Notifies that the scan has been stopped/aborted by OpenWebNetDeviceDiscoveryService
     */
    public void scanStopped() {
        scanIsActive = false;
        logger.debug("------$$ scanIsActive={}", scanIsActive);
    }

    private void discoverByActivation(BaseOpenMessage baseMsg) {
        logger.debug("discoverByActivation: msg={}", baseMsg);
        OpenWebNetDeviceDiscoveryService discService = deviceDiscoveryService;
        if (discService == null) {
            logger.warn("discoverByActivation: null OpenWebNetDeviceDiscoveryService, ignoring msg={}", baseMsg);
            return;
        }
        // we support these types only
        if (baseMsg instanceof Lighting || baseMsg instanceof Automation || baseMsg instanceof EnergyManagement
                || baseMsg instanceof Thermoregulation || baseMsg instanceof CEN) {
            BaseOpenMessage bmsg = baseMsg;
            if (baseMsg instanceof Lighting) {
                What what = baseMsg.getWhat();
                if (Lighting.WhatLighting.OFF.equals(what)) { // skipping OFF msg: cannot distinguish dimmer/switch
                    logger.debug("discoverByActivation: skipping OFF msg: cannot distinguish dimmer/switch");
                    return;
                }
                if (Lighting.WhatLighting.ON.equals(what)) { // if not already done just now, request light status to
                    // distinguish dimmer from switch
                    if (discoveringDevices.containsKey(ownIdFromMessage(baseMsg))) {
                        logger.debug(
                                "discoverByActivation: we just requested status for this device and it's ON -> it's a switch");
                    } else {
                        OpenGateway gw = gateway;
                        if (gw != null) {
                            try {
                                discoveringDevices.put(ownIdFromMessage(baseMsg),
                                        Long.valueOf(System.currentTimeMillis()));
                                gw.send(Lighting.requestStatus(baseMsg.getWhere().value()));
                                return;
                            } catch (OWNException e) {
                                logger.warn("discoverByActivation: Exception while requesting light state: {}",
                                        e.getMessage());
                                return;
                            }
                        }
                    }
                }
                discoveringDevices.remove(ownIdFromMessage(baseMsg));
            }
            OpenDeviceType type = null;
            try {
                type = bmsg.detectDeviceType();
            } catch (FrameException e) {
                logger.warn("Exception while detecting device type: {}", e.getMessage());
            }
            if (type != null) {
                discService.newDiscoveryResult(bmsg.getWhere(), type, bmsg);
            } else {
                logger.debug("discoverByActivation: no device type detected from msg: {}", bmsg);
            }
        }
    }

    /**
     * Register a device ThingHandler to this BridgHandler
     *
     * @param ownId the device OpenWebNet id
     * @param thingHandler the thing handler to be registered
     */
    protected void registerDevice(String ownId, OpenWebNetThingHandler thingHandler) {
        if (registeredDevices.containsKey(ownId)) {
            logger.warn("registering device with an existing ownId={}", ownId);
        }
        registeredDevices.put(ownId, thingHandler);
        logger.debug("registered device ownId={}, thing={}", ownId, thingHandler.getThing().getUID());
    }

    /**
     * Un-register a device from this bridge handler
     *
     * @param ownId the device OpenWebNet id
     */
    protected void unregisterDevice(String ownId) {
        if (registeredDevices.remove(ownId) != null) {
            logger.debug("un-registered device ownId={}", ownId);
        } else {
            logger.warn("could not un-register ownId={} (not found)", ownId);
        }
    }

    /**
     * Get an already registered device on this bridge handler
     *
     * @param ownId the device OpenWebNet id
     * @return the registered device Thing handler or null if the id cannot be found
     */
    public @Nullable OpenWebNetThingHandler getRegisteredDevice(String ownId) {
        return registeredDevices.get(ownId);
    }

    private void refreshAllDevices() {
        logger.debug("Refreshing all devices for bridge {}", thing.getUID());
        for (Thing ownThing : getThing().getThings()) {
            OpenWebNetThingHandler hndlr = (OpenWebNetThingHandler) ownThing.getHandler();
            if (hndlr != null) {
                hndlr.refreshDevice(true);
            }
        }
    }

    @Override
    public void onEventMessage(@Nullable OpenMessage msg) {
        logger.trace("RECEIVED <<<<< {}", msg);
        if (msg == null) {
            logger.warn("received event msg is null");
            return;
        }
        if (msg.isACK() || msg.isNACK()) {
            return; // we ignore ACKS/NACKS
        }
        // GATEWAY MANAGEMENT
        if (msg instanceof GatewayMgmt) {
            // noop
            return;
        }

        BaseOpenMessage baseMsg = (BaseOpenMessage) msg;
        // let's try to get the Thing associated with this message...
        if (baseMsg instanceof Lighting || baseMsg instanceof Automation || baseMsg instanceof EnergyManagement
                || baseMsg instanceof Thermoregulation || baseMsg instanceof CEN) {
            String ownId = ownIdFromMessage(baseMsg);
            logger.debug("ownIdFromMessage({}) --> {}", baseMsg, ownId);
            OpenWebNetThingHandler deviceHandler = registeredDevices.get(ownId);
            if (deviceHandler == null) {
                OpenGateway gw = gateway;
                if (isBusGateway && ((gw != null && !gw.isDiscovering() && scanIsActive)
                        || (discoveryByActivation && !scanIsActive))) {
                    discoverByActivation(baseMsg);
                } else {
                    logger.debug("ownId={} has NO DEVICE associated, ignoring it", ownId);
                }
            } else {
                deviceHandler.handleMessage(baseMsg);
            }
        } else {
            logger.debug("BridgeHandler ignoring frame {}. WHO={} is not supported by this binding", baseMsg,
                    baseMsg.getWho());
        }
    }

    @Override
    public void onConnected() {
        isGatewayConnected = true;
        Map<String, String> properties = editProperties();
        boolean propertiesChanged = false;
        OpenGateway gw = gateway;
        if (gw == null) {
            logger.warn("received onConnected() but gateway is null");
            return;
        }
        if (gw instanceof USBGateway) {
            logger.info("---- CONNECTED to ZigBee USB gateway bridge '{}' (serialPort: {})", thing.getUID(),
                    ((USBGateway) gw).getSerialPortName());
        } else {
            logger.info("---- CONNECTED to BUS gateway bridge '{}' ({}:{})", thing.getUID(),
                    ((BUSGateway) gw).getHost(), ((BUSGateway) gw).getPort());
            // update serial number property (with MAC address)
            if (!Objects.equals(properties.get(PROPERTY_SERIAL_NO), gw.getMACAddr().toUpperCase())) {
                properties.put(PROPERTY_SERIAL_NO, gw.getMACAddr().toUpperCase());
                propertiesChanged = true;
                logger.debug("updated property gw serialNumber: {}", properties.get(PROPERTY_SERIAL_NO));
            }
        }
        if (!Objects.equals(properties.get(PROPERTY_FIRMWARE_VERSION), gw.getFirmwareVersion())) {
            properties.put(PROPERTY_FIRMWARE_VERSION, gw.getFirmwareVersion());
            propertiesChanged = true;
            logger.debug("updated property gw firmware version: {}", properties.get(PROPERTY_FIRMWARE_VERSION));
        }
        if (propertiesChanged) {
            updateProperties(properties);
            logger.info("properties updated for bridge '{}'", thing.getUID());
        }
        updateStatus(ThingStatus.ONLINE);
        // schedule a refresh for all devices
        refreshSchedule = scheduler.schedule(this::refreshAllDevices, REFRESH_ALL_DEVICES_DELAY_MSEC,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void onConnectionError(@Nullable OWNException error) {
        String errMsg;
        if (error == null) {
            errMsg = "unknown error";
        } else {
            errMsg = error.getMessage();
        }
        logger.info("---- ON CONNECTION ERROR for gateway {}: {}", gateway, errMsg);
        isGatewayConnected = false;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "@text/offline.comm-error-connection" + " (onConnectionError - " + errMsg + ")");
        tryReconnectGateway();
    }

    @Override
    public void onConnectionClosed() {
        isGatewayConnected = false;
        logger.debug("onConnectionClosed() - isGatewayConnected={}", isGatewayConnected);
        // NOTE: cannot change to OFFLINE here because we are already in REMOVING state
    }

    @Override
    public void onDisconnected(@Nullable OWNException e) {
        isGatewayConnected = false;
        String errMsg;
        if (e == null) {
            errMsg = "unknown error";
        } else {
            errMsg = e.getMessage();
        }
        logger.info("---- DISCONNECTED from gateway {}. OWNException: {}", gateway, errMsg);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "@text/offline.comm-error-disconnected" + " (onDisconnected - " + errMsg + ")");
        tryReconnectGateway();
    }

    private void tryReconnectGateway() {
        OpenGateway gw = gateway;
        if (gw != null) {
            if (!reconnecting) {
                reconnecting = true;
                logger.info("---- Starting RECONNECT cycle to gateway {}", gw);
                try {
                    gw.reconnect();
                } catch (OWNAuthException e) {
                    logger.info("---- AUTH error from gateway. Stopping re-connect");
                    reconnecting = false;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "@text/offline.conf-error-auth" + " (" + e + ")");
                }
            } else {
                logger.debug("---- reconnecting=true");
            }
        } else {
            logger.warn("---- cannot start RECONNECT, gateway is null");
        }
    }

    @Override
    public void onReconnected() {
        reconnecting = false;
        OpenGateway gw = gateway;
        logger.info("---- RE-CONNECTED to bridge {}", thing.getUID());
        if (gw != null) {
            updateStatus(ThingStatus.ONLINE);
            if (gw.getFirmwareVersion() != null) {
                this.updateProperty(PROPERTY_FIRMWARE_VERSION, gw.getFirmwareVersion());
                logger.debug("gw firmware version: {}", gw.getFirmwareVersion());
            }

            // schedule a refresh for all devices
            refreshSchedule = scheduler.schedule(this::refreshAllDevices, REFRESH_ALL_DEVICES_DELAY_MSEC,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Return a ownId string (=WHO.WHERE) from the device Where address and handler
     *
     * @param where the Where address (to be normalized)
     * @param handler the device handler
     * @return the ownId String
     */
    protected String ownIdFromDeviceWhere(Where where, OpenWebNetThingHandler handler) {
        return handler.ownIdPrefix() + "." + normalizeWhere(where);
    }

    /**
     * Returns a ownId string (=WHO.WHERE) from a Who and Where address
     *
     * @param who the Who
     * @param where the Where address (to be normalized)
     * @return the ownId String
     */
    public String ownIdFromWhoWhere(Who who, Where where) {
        return who.value() + "." + normalizeWhere(where);
    }

    /**
     * Return a ownId string (=WHO.WHERE) from a BaseOpenMessage
     *
     * @param baseMsg the BaseOpenMessage
     * @return the ownId String
     */
    public String ownIdFromMessage(BaseOpenMessage baseMsg) {
        return baseMsg.getWho().value() + "." + normalizeWhere(baseMsg.getWhere());
    }

    /**
     * Transform a Where address into a Thing id string
     *
     * @param where the Where address
     * @return the thing Id string
     */
    public String thingIdFromWhere(Where where) {
        return normalizeWhere(where); // '#' cannot be used in ThingUID;
    }

    /**
     * Normalize a Where address to generate ownId and Thing id
     *
     * @param where the Where address
     * @return the normalized address as String
     */
    public String normalizeWhere(Where where) {
        String str = where.value();
        if (where instanceof WhereZigBee) {
            str = ((WhereZigBee) where).valueWithUnit(WhereZigBee.UNIT_ALL); // 76543210X#9 --> 765432100#9
        } else {
            if (str.indexOf("#4#") == -1) { // skip APL#4#bus case
                if (str.indexOf('#') == 0) { // Thermo central unit (#0) or zone via central unit (#Z, Z=[1-99]) --> Z
                    str = str.substring(1);
                } else if (str.indexOf('#') > 0) { // Thermo zone Z and actuator N (Z#N, Z=[1-99], N=[1-9]) --> Z
                    str = str.substring(0, str.indexOf('#'));
                }
            }
        }
        return str.replace('#', 'h');
    }
}
