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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.actions.OpenWebNetBridgeActions;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetDeviceDiscoveryService;
import org.openhab.binding.openwebnet.internal.handler.config.OpenWebNetBusBridgeConfig;
import org.openhab.binding.openwebnet.internal.handler.config.OpenWebNetZigBeeBridgeConfig;
import org.openhab.binding.openwebnet.internal.serial.SerialPortProviderAdapter;
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
import org.openwebnet4j.message.Alarm;
import org.openwebnet4j.message.Automation;
import org.openwebnet4j.message.Auxiliary;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.CEN;
import org.openwebnet4j.message.EnergyManagement;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Scenario;
import org.openwebnet4j.message.Thermoregulation;
import org.openwebnet4j.message.What;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetBridgeHandler} is responsible for handling communication
 * with gateways and handling events.
 *
 * @author Massimo Valla - Initial contribution, Lighting, Automation, Scenario
 * @author Andrea Conte - Energy management, Thermoregulation
 * @author Gilberto Cocchi - Thermoregulation
 * @author Giovanni Fabiani - Aux
 */
@NonNullByDefault
public class OpenWebNetBridgeHandler extends ConfigStatusBridgeHandler implements GatewayListener {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetBridgeHandler.class);

    private static final int GATEWAY_ONLINE_TIMEOUT_SEC = 20; // Time to wait for the gateway to become connected

    private static final int REFRESH_ALL_DEVICES_DELAY_MSEC = 500; // Delay to wait before trying again another all
                                                                   // devices refresh request after a connect/reconnect
    private static final int REFRESH_ALL_DEVICES_DELAY_MAX_MSEC = 15000; // Maximum delay to wait for all devices
                                                                         // refresh after a connect/reconnect

    private static final int REFRESH_ALL_CHECK_DELAY_SEC = 20; // Delay to wait to check which devices are
                                                               // online/offline

    private static final int DATETIME_SYNCH_DIFF_SEC = 60; // Difference from BUS date time

    private long lastRegisteredDeviceTS = -1; // timestamp when the last device has been associated to the bridge
    private long refreshAllDevicesDelay = 0; // delay waited before starting all devices refresh

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.BRIDGE_SUPPORTED_THING_TYPES;

    // ConcurrentHashMap of devices registered to this BridgeHandler
    // association is: ownId (String) -> OpenWebNetThingHandler, with ownId =
    // WHO.WHERE
    private Map<String, @Nullable OpenWebNetThingHandler> registeredDevices = new ConcurrentHashMap<>();
    private Map<String, Long> discoveringDevices = new ConcurrentHashMap<>();

    protected @Nullable LightAutomHandlersMap lightsMap; // a LightAutomHandlersMap storing lights handlers organised by
                                                         // the AREA they belong to

    protected @Nullable OpenGateway gateway;
    private boolean isBusGateway = false;

    private boolean isGatewayConnected = false;

    public @Nullable OpenWebNetDeviceDiscoveryService deviceDiscoveryService;
    private boolean reconnecting = false; // we are trying to reconnect to gateway
    private @Nullable ScheduledFuture<?> refreshAllSchedule;
    private @Nullable ScheduledFuture<?> connectSchedule;

    private boolean scanIsActive = false; // a device scan has been activated by OpenWebNetDeviceDiscoveryService;
    private boolean discoveryByActivation;
    private boolean dateTimeSynch = false;

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
                    connectSchedule = scheduler.schedule(() -> {
                        // if status is still UNKNOWN after timer ends, set the device OFFLINE
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
     * Init a Zigbee gateway based on config
     */
    private @Nullable OpenGateway initZigBeeGateway() {
        logger.debug("Initializing Zigbee USB Gateway");
        OpenWebNetZigBeeBridgeConfig zbBridgeConfig = getConfigAs(OpenWebNetZigBeeBridgeConfig.class);
        String serialPort = zbBridgeConfig.getSerialPort();
        if (serialPort == null || serialPort.isEmpty()) {
            logger.warn("Cannot connect Zigbee USB Gateway. No serial port has been provided in Bridge configuration.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-serial-port");
            return null;
        } else {
            USBGateway tmpUSBGateway = new USBGateway(serialPort);
            tmpUSBGateway.setSerialPortProvider(new SerialPortProviderAdapter());
            logger.debug("**** -SPI- ****  OpenWebNetBridgeHandler :: setSerialPortProvider to: {}",
                    tmpUSBGateway.getSerialPortProvider());
            return tmpUSBGateway;
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
            dateTimeSynch = busBridgeConfig.getDateTimeSynch();
            logger.debug(
                    "Creating new BUS gateway with config properties: {}:{}, pwd={}, discoveryByActivation={}, dateTimeSynch={}",
                    host, port, passwdMasked, discoveryByActivation, dateTimeSynch);
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
                refreshAllBridgeDevices();
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
        ScheduledFuture<?> rSc = refreshAllSchedule;
        if (rSc != null) {
            rSc.cancel(true);
        }
        ScheduledFuture<?> cs = connectSchedule;
        if (cs != null) {
            cs.cancel(true);
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

    /**
     * Return the OpenGateway linked to this BridgeHandler
     *
     * @return the linked OpenGateway
     */
    public @Nullable OpenGateway getGateway() {
        return gateway;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(OpenWebNetDeviceDiscoveryService.class, OpenWebNetBridgeActions.class);
    }

    /**
     * Search for devices connected to this bridge handler's gateway
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
            if (deviceType != null) {
                discService.newDiscoveryResult(w, deviceType, message);
            } else {
                logger.warn("onNewDevice with null deviceType, msg={}", message);
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
     * Notifies that the scan has been stopped/aborted by
     * OpenWebNetDeviceDiscoveryService
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
                || baseMsg instanceof Thermoregulation || baseMsg instanceof CEN || baseMsg instanceof Scenario
                || baseMsg instanceof Alarm) {
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
        lastRegisteredDeviceTS = System.currentTimeMillis();
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
    public @Nullable OpenWebNetThingHandler getRegisteredDevice(@Nullable String ownId) {
        return registeredDevices.get(ownId);
    }

    /**
     * Adds a light handler to the light map for this bridge, grouped by Area
     *
     * @param area the light area
     * @param lightHandler the light handler to be added
     */
    protected void addLight(int area, OpenWebNetThingHandler lightHandler) {
        if (lightsMap == null) {
            lightsMap = new LightAutomHandlersMap();
        }
        LightAutomHandlersMap lm = lightsMap;
        if (lm != null) {
            lm.add(area, lightHandler);
            logger.debug("Added APL {} to lightsMap", lightHandler.ownId);
        }
    }

    /**
     * Remove a light handler to the light map for this bridge
     *
     * @param area the light area
     * @param lightHandler the light handler to be removed
     */
    protected void removeLight(int area, OpenWebNetThingHandler lightHandler) {
        LightAutomHandlersMap lightsMap = this.lightsMap;
        if (lightsMap != null) {
            lightsMap.remove(area, lightHandler);
            logger.debug("Removed APL {} from lightsMap", lightHandler.ownId);
        }
    }

    @Nullable
    protected List<OpenWebNetThingHandler> getAllLights() {
        LightAutomHandlersMap lightsMap = this.lightsMap;
        return (lightsMap != null) ? lightsMap.getAllHandlers() : null;
    }

    @Nullable
    public LightAutomHandlersMap getLightsMap() {
        return this.lightsMap;
    }

    private void refreshAllBridgeDevices() {
        logger.debug("--- --- ABOUT TO REFRESH ALL devices for bridge {}", thing.getUID());
        int howMany = 0;
        final List<Thing> things = getThing().getThings();
        int total = things.size();
        logger.debug("--- FOUND {} things by getThings()", total);
        if (total > 0) {
            if (registeredDevices.isEmpty()) { // no registered device yet
                if (refreshAllDevicesDelay < REFRESH_ALL_DEVICES_DELAY_MAX_MSEC) {
                    logger.debug("--- REGISTER device not started yet... re-scheduling refreshAllBridgeDevices()");
                    refreshAllDevicesDelay += REFRESH_ALL_DEVICES_DELAY_MSEC * 3;
                    refreshAllSchedule = scheduler.schedule(this::refreshAllBridgeDevices,
                            REFRESH_ALL_DEVICES_DELAY_MSEC * 3, TimeUnit.MILLISECONDS);
                    return;
                } else {
                    logger.warn(
                            "--- --- NONE OF {} CHILD DEVICE(S) has REGISTERED with bridge {}: check Things configuration (stopping refreshAllBridgeDevices)",
                            total, thing.getUID());
                    refreshAllDevicesDelay = 0;
                    return;
                }
            } else if (System.currentTimeMillis() - lastRegisteredDeviceTS < REFRESH_ALL_DEVICES_DELAY_MSEC) {
                // a device has been registered with the bridge just now, let's wait for other
                // devices: re-schedule refreshAllDevices
                logger.debug("--- REGISTER device just called... re-scheduling refreshAllBridgeDevices()");
                refreshAllSchedule = scheduler.schedule(this::refreshAllBridgeDevices, REFRESH_ALL_DEVICES_DELAY_MSEC,
                        TimeUnit.MILLISECONDS);
                return;
            }
            for (Thing ownThing : things) {
                OpenWebNetThingHandler hndlr = (OpenWebNetThingHandler) ownThing.getHandler();
                if (hndlr != null) {
                    howMany++;
                    logger.debug("--- REFRESHING ALL DEVICES FOR thing #{}/{}: {}", howMany, total, ownThing.getUID());
                    hndlr.refreshAllDevices();
                } else {
                    logger.warn("--- No handler for thing {}", ownThing.getUID());
                }
            }
            logger.debug("--- --- COMPLETED REFRESH all devices for bridge {}", thing.getUID());
            refreshAllDevicesDelay = 0;
            // set a check that all things are Online
            refreshAllSchedule = scheduler.schedule(() -> checkAllRefreshed(things), REFRESH_ALL_CHECK_DELAY_SEC,
                    TimeUnit.SECONDS);
        } else {
            logger.debug("--- --- NO CHILD DEVICE to REFRESH for bridge {}", thing.getUID());
        }
    }

    private void checkAllRefreshed(List<Thing> things) {
        int howMany = 0;
        int total = things.size();
        boolean allOnline = true;
        for (Thing ownThing : things) {
            howMany++;
            ThingStatus ts = ownThing.getStatus();
            if (ThingStatus.ONLINE == ts) {
                logger.debug("--- CHECKED ONLINE thing #{}/{}: {}", howMany, total, ownThing.getUID());
            } else {
                logger.debug("--- CHECKED ^^^OFFLINE^^^ thing #{}/{}: {}", howMany, total, ownThing.getUID());
                allOnline = false;
            }
        }
        if (allOnline) {
            logger.debug("--- --- REFRESH CHECK COMPLETED: all things ONLINE for bridge {}", thing.getUID());
        } else {
            logger.debug("--- --- REFRESH CHECK COMPLETED: NOT all things ONLINE for bridge {}", thing.getUID());
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
        if (msg instanceof GatewayMgmt gwMsg) {
            if (dateTimeSynch && GatewayMgmt.DimGatewayMgmt.DATETIME.equals(gwMsg.getDim())) {
                checkDateTimeDiff(gwMsg);
            }
            return;
        }

        // LIGHTING multiple messages for BUS
        if (msg instanceof Lighting lmsg && isBusGateway) {
            WhereLightAutom whereLightAutom = (WhereLightAutom) lmsg.getWhere();
            if (whereLightAutom != null && (whereLightAutom.isGeneral() || whereLightAutom.isArea())) {
                LightAutomHandlersMap lightsMap = this.lightsMap;
                if (lightsMap != null && !lightsMap.isEmpty()) {
                    OpenWebNetLightingHandler lightingHandler = (OpenWebNetLightingHandler) lightsMap.getOneHandler();
                    if (lightingHandler != null) {
                        lightingHandler.handleMultipleMessage(lmsg);
                    }
                }
                return;
            }
        }

        BaseOpenMessage baseMsg = (BaseOpenMessage) msg;

        // let's try to get the Thing associated with this message...
        if (baseMsg instanceof Lighting || baseMsg instanceof Automation || baseMsg instanceof EnergyManagement
                || baseMsg instanceof Thermoregulation || baseMsg instanceof CEN || baseMsg instanceof Auxiliary
                || baseMsg instanceof Scenario || baseMsg instanceof Alarm) {
            String ownId = ownIdFromMessage(baseMsg);
            logger.debug("ownIdFromMessage({}) --> {}", baseMsg, ownId);
            OpenWebNetThingHandler deviceHandler = registeredDevices.get(ownId);
            if (deviceHandler == null) {
                OpenGateway gw = gateway;
                if (isBusGateway && ((gw != null && !gw.isDiscovering() && scanIsActive)
                        || (discoveryByActivation && !scanIsActive))) {
                    discoverByActivation(baseMsg);
                } else {
                    logger.debug("ownId={} has NO DEVICE associated to bridge {}: ignoring it", ownId, thing.getUID());
                }
            } else {
                deviceHandler.handleMessage(baseMsg);
            }
        } else {
            logger.debug("BridgeHandler ignoring frame {}. WHO={} is not supported by this binding", baseMsg,
                    baseMsg.getWho());
        }
    }

    private void checkDateTimeDiff(GatewayMgmt gwMsg) {
        try {
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime gwTime = GatewayMgmt.parseDateTime(gwMsg);
            long diff = Math.abs(Duration.between(now, gwTime).toSeconds());
            if (diff > DATETIME_SYNCH_DIFF_SEC) {
                logger.debug("checkDateTimeDiff: difference is more than 60s: {}s", diff);
                OpenGateway gw = gateway;
                if (gw != null) {
                    logger.debug("checkDateTimeDiff: synch DateTime to: {}", now);
                    try {
                        gw.send(GatewayMgmt.requestSetDateTime(now));
                    } catch (OWNException e) {
                        logger.warn("checkDateTimeDiff: Exception while sending set DateTime command: {}",
                                e.getMessage());
                    }
                }
            } else {
                logger.debug("checkDateTimeDiff: DateTime difference: {}s", diff);
            }
        } catch (FrameException e) {
            logger.warn("checkDateTimeDiff: FrameException while parsing {}", e.getMessage());
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
        if (gw instanceof USBGateway usbGateway) {
            logger.info("---- CONNECTED to Zigbee USB gateway bridge '{}' (serialPort: {})", thing.getUID(),
                    usbGateway.getSerialPortName());
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
        refreshAllSchedule = scheduler.schedule(this::refreshAllBridgeDevices, REFRESH_ALL_DEVICES_DELAY_MSEC,
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
                logger.debug("---- already reconnecting");
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
            refreshAllSchedule = scheduler.schedule(this::refreshAllBridgeDevices, REFRESH_ALL_DEVICES_DELAY_MSEC,
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
        Who w = handler.getManagedWho();
        return w.value().toString() + "." + normalizeWhere(w, where);
    }

    /**
     * Returns a ownId string (=WHO.WHERE) from a Who and Where address
     *
     * @param who the Who
     * @param where the Where address (to be normalized)
     * @return the ownId String
     */
    public String ownIdFromWhoWhere(Who who, Where where) {
        return who.value() + "." + normalizeWhere(who, where);
    }

    /**
     * Return a ownId string (=WHO.WHERE) from a BaseOpenMessage
     *
     * @param baseMsg the BaseOpenMessage
     * @return the ownId String
     */
    public String ownIdFromMessage(BaseOpenMessage baseMsg) {
        @Nullable
        Where w = baseMsg.getWhere();
        if (w != null) {
            return baseMsg.getWho().value() + "." + normalizeWhere(baseMsg.getWho(), w);
        } else if (baseMsg instanceof Alarm) { // null and Alarm
            return baseMsg.getWho().value() + "." + "0"; // Alarm System --> where=0
        } else {
            logger.warn("ownIdFromMessage with null where: {}", baseMsg);
            return "";
        }
    }

    /**
     * Given a Who and a Where address, return a Thing id string
     *
     * @param who the Who
     * @param where the Where address
     * @return the thing Id string
     */
    public String thingIdFromWhoWhere(Who who, Where where) {
        return normalizeWhere(who, where); // '#' cannot be used in ThingUID;
    }

    /**
     * Normalize, based on Who, a Where address. Used to generate ownId and Thing id
     *
     * @param who the Who
     * @param where the Where address
     * @return the normalized address as String
     */
    public String normalizeWhere(Who who, Where where) {
        String str = where.value();
        if (where instanceof WhereZigBee whereZigBee) {
            str = whereZigBee.valueWithUnit(WhereZigBee.UNIT_ALL); // 76543210X#9 --> 765432100#9
        } else {
            if (str.indexOf("#4#") == -1) { // skip APL#4#bus case
                if (str.indexOf('#') == 0) {
                    if (who.equals(Who.THERMOREGULATION) || who.equals(Who.THERMOREGULATION_DIAGNOSTIC)
                            || who.equals(Who.BURGLAR_ALARM)) {
                        // Thermo central unit (#0) or zone via central unit (#Z, Z=[1-99]) --> Z
                        // or Alarm zone #Z --> Z
                        str = str.substring(1);
                    } // else leave the initial hash (for example for LightAutomWhere GROUPs #GR -->
                      // hGR)
                } else if (str.indexOf('#') > 0 && str.charAt(0) != '0') {
                    // Thermo zone Z and actuator N (Z#N, Z=[1-99], N=[1-9]) --> Z)
                    str = str.substring(0, str.indexOf('#'));
                }
            }
        }
        return str.replace('#', 'h');
    }
}
