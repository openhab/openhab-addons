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
package org.openhab.binding.openwebnet.handler;

import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetDeviceDiscoveryService;
import org.openwebnet4j.BUSGateway;
import org.openwebnet4j.GatewayListener;
import org.openwebnet4j.OpenDeviceType;
import org.openwebnet4j.OpenGateway;
import org.openwebnet4j.USBGateway;
import org.openwebnet4j.communication.OWNAuthException;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetBridgeHandler} is responsible for handling communication with gateways and handling events.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetBridgeHandler extends ConfigStatusBridgeHandler implements GatewayListener {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetBridgeHandler.class);

    private static final int GATEWAY_ONLINE_TIMEOUT = 20; // (sec) Time to wait for the gateway to become connected
    private static final int CONFIG_GATEWAY_DEFAULT_PORT = 20000;
    private static final String CONFIG_GATEWAY_DEFAULT_PASSWD = "12345";
    private static final boolean CONFIG_GATEWAY_DEFAULT_DISCVERY_ACTIVATION = false;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.BRIDGE_SUPPORTED_THING_TYPES;

    // ConcurrentHashMap of devices registered to this BridgeHandler
    // Association is: ownId (String) -> OpenWebNetThingHandler, with ownId = WHO.WHERE
    private Map<String, @Nullable OpenWebNetThingHandler> registeredDevices = new ConcurrentHashMap<>();

    protected @Nullable OpenGateway gateway;
    private boolean isBusGateway = false;

    private boolean isGatewayConnected = false;

    public @Nullable OpenWebNetDeviceDiscoveryService deviceDiscoveryService;
    private boolean reconnecting = false; // we are trying to reconnect to gateway
    private boolean scanIsActive = false; // a device scan has been activated by OpenWebNetDeviceDiscoveryService;
    private boolean discoveryByActivation = CONFIG_GATEWAY_DEFAULT_DISCVERY_ACTIVATION;

    public OpenWebNetBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Nullable
    public OpenGateway getGateway() {
        return gateway;
    }

    public boolean isBusGateway() {
        return isBusGateway;
    }

    @Override
    public void initialize() {
        ThingTypeUID thingType = getThing().getThingTypeUID();
        logger.debug("Bridge type: {}", thingType);

        if (thingType.equals(THING_TYPE_ZB_GATEWAY)) {
            initZigBeeGateway();
        } else {
            initBusGateway();
            isBusGateway = true;
        }

        gateway.subscribe(this);
        if (gateway.isConnected()) { // gateway is already connected, device can go ONLINE
            isGatewayConnected = true;
            logger.info("------------------- ALREADY CONNECTED -> setting status to ONLINE");
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            logger.debug("Trying to connect gateway...");
            try {
                gateway.connect();
                scheduler.schedule(() -> {
                    // if status is still UNKNOWN after timer ends, set the device as OFFLINE
                    if (thing.getStatus().equals(ThingStatus.UNKNOWN)) {
                        logger.info("status still UNKNOWN. Setting device={} to OFFLINE", thing.getUID());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "Could not connect to gateway before " + GATEWAY_ONLINE_TIMEOUT + "s");
                    }
                }, GATEWAY_ONLINE_TIMEOUT, TimeUnit.SECONDS);
            } catch (OWNException e) {
                logger.debug("gateway.connect() returned: {}", e.getMessage());
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        // TODO When initialisation can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    /**
     * Init a ZigBee gateway based on config properties
     */
    private void initZigBeeGateway() {
        String serialPort = (String) (getConfig().get(CONFIG_PROPERTY_SERIAL_PORT));
        if (serialPort == null) {
            logger.warn("Cannot connect to gateway. No serial port has been provided in Bridge configuration.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-serial-port");
        } else {
            gateway = new USBGateway(serialPort);
        }
    }

    /**
     * Init a BUS/SCS gateway based on config properties
     */
    private void initBusGateway() {
        if (getConfig().get(CONFIG_PROPERTY_HOST) != null) {
            String host = (String) (getConfig().get(CONFIG_PROPERTY_HOST));
            int port = CONFIG_GATEWAY_DEFAULT_PORT;
            Object portConfig = getConfig().get(CONFIG_PROPERTY_PORT);
            if (portConfig != null) {
                port = ((BigDecimal) portConfig).intValue();
                // FIXME handle case parameter is not a number
            }
            String passwd = (String) (getConfig().get(CONFIG_PROPERTY_PASSWD));
            if (passwd == null) {
                passwd = CONFIG_GATEWAY_DEFAULT_PASSWD;
            }
            String passwdMasked;
            if (passwd.length() >= 4) {
                passwdMasked = "******" + passwd.substring(passwd.length() - 3, passwd.length());
            } else {
                passwdMasked = "******";
            }
            Object discoveryConfig = getConfig().get(CONFIG_PROPERTY_DISCOVERY_ACTIVATION);
            if (discoveryConfig != null) {
                if (discoveryConfig instanceof java.lang.Boolean) {
                    discoveryByActivation = (boolean) getConfig().get(CONFIG_PROPERTY_DISCOVERY_ACTIVATION);
                    logger.debug("discoveryByActivation={}", discoveryByActivation);
                } else {
                    logger.warn(
                            "invalid discoveryByActivation parameter value (should be true/false). Keeping current value={}.",
                            discoveryByActivation);
                }
            }
            logger.debug("Creating new BUS gateway with config properties: {}:{}, pwd={}", host, port, passwdMasked);
            gateway = new BUSGateway(host, port, passwd);
        } else {
            logger.warn("Cannot connect to gateway. No host/IP has been provided in Bridge configuration.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand (command={} - channel={})", command, channelUID);
        if (!gateway.isConnected()) {
            logger.warn("BridgeHandler gateway is NOT connected, skipping command");
            return;
        } else {
            logger.warn("Channel not supported: channel={}", channelUID);
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages;
        configStatusMessages = Collections.emptyList();
        return configStatusMessages;
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        logger.info("Bridge configuration updated.");
        // for (Thing t : getThing().getThings()) {
        // final ThingHandler thingHandler = t.getHandler();
        // if (thingHandler != null) {
        // thingHandler.thingUpdated(t);
        // }
        // }
    }

    @Override
    public void handleRemoval() {
        logger.debug("handleRemoval() for {}", getThing().getUID());
        disconnectGateway();
        // logger.debug("calling super.handleRemoval()");
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        logger.debug("dispose() for {}", getThing().getUID());
        disconnectGateway();
        // logger.debug("calling super.dispose()");
        super.dispose();
    }

    private void disconnectGateway() {
        if (gateway != null) {
            gateway.closeConnection();
            gateway.unsubscribe(this);
            logger.debug("gateway {} connection closed and unsubscribed", gateway.toString());
            gateway = null;
        }
        reconnecting = false;
    }

    /**
     * Search for devices connected to this bridge handler's gateway
     *
     * @param listener to receive device found notifications
     */
    public synchronized void searchDevices() {
        logger.debug("-------- BridgeHandler.searchDevices()");
        scanIsActive = true;
        logger.debug("-------- scanIsActive={}", scanIsActive);
        if (gateway != null) {
            if (!gateway.isDiscovering()) {
                if (!gateway.isConnected()) {
                    logger.warn("-------- Gateway is NOT connected, cannot search for devices");
                    return;
                }
                logger.info("-------- STARTED active search for devices on gateway '{}'", this.getThing().getUID());
                try {
                    gateway.discoverDevices();
                } catch (OWNException e) {
                    logger.warn("-------- OWNException while discovering devices on gateway {}: {}",
                            this.getThing().getUID(), e.getMessage());
                }
            } else {
                logger.warn("-------- Searching devices on gateway {} already activated", this.getThing().getUID());
                return;
            }
        } else {
            logger.warn("-------- Cannot search devices: no gateway associated to this handler");
        }
    }

    @Override
    public void onNewDevice(@Nullable Where w, @Nullable OpenDeviceType deviceType, @Nullable BaseOpenMessage message) {
        try {
            if (w != null && deviceType != null) {
                deviceDiscoveryService.newDiscoveryResult(w, deviceType, message);
            } else {
                logger.warn("onNewDevice with null where/deviceType msg={}", message);
            }
        } catch (Exception e) {
            logger.warn("Exception while discovering new device WHERE={}, deviceType={}: {}", w, deviceType,
                    e.getMessage());
        }
    }

    @Override
    public void onDiscoveryCompleted() {
        logger.info("-------- FINISHED active search for devices on gateway '{}'", this.getThing().getUID());
    }

    /**
     * Notifies that the scan has been stopped/aborted by OpenWebNetDeviceDiscoveryService
     */
    public void scanStopped() {
        scanIsActive = false;
        logger.debug("-------- scanIsActive={}", scanIsActive);
    }

    private void discoverByActivation(BaseOpenMessage baseMsg) {
        logger.debug("BridgeHandler.discoverByActivation() msg={}", baseMsg);
        if (baseMsg instanceof Lighting) {
            OpenDeviceType type = null;
            try {
                type = baseMsg.detectDeviceType();
            } catch (FrameException e) {
                logger.warn("Exception while detecting device type: {}", e.getMessage());
            }
            if (type != null) {
                deviceDiscoveryService.newDiscoveryResult(baseMsg.getWhere(), type, baseMsg);
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
        logger.info("registered device ownId={}, thing={}", ownId, thingHandler.getThing().getUID());
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

    @Override
    public void onEventMessage(@Nullable OpenMessage msg) {
        logger.trace("RECEIVED <<<<< {}", msg);
        if (msg.isACK() || msg.isNACK()) {
            return; // we ignore ACKS/NACKS
        }
        // GATEWAY MANAGEMENT
        if (msg instanceof GatewayMgmt) {
            // GatewayMgmt gwMgmtMsg = (GatewayMgmt) msg;
            // logger.debug("GatewayMgmt WHAT = {}, ignoring", gwMgmtMsg.getWhat());
            return;
        }

        BaseOpenMessage baseMsg = (BaseOpenMessage) msg;
        // let's try to get the Thing associated with this message...
        if (baseMsg instanceof Lighting) {
            String ownId = ownIdFromMessage(baseMsg);
            logger.debug("ownId={}", ownId);
            OpenWebNetThingHandler deviceHandler = registeredDevices.get(ownId);
            if (deviceHandler == null) {
                if (isBusGateway
                        && ((!gateway.isDiscovering() && scanIsActive) || (discoveryByActivation && !scanIsActive))) {
                    // try device discovery by activation
                    discoverByActivation(baseMsg);
                } else {
                    logger.debug("ownId={} has NO DEVICE associated, ignoring it", ownId);
                }
            } else {
                deviceHandler.handleMessage(baseMsg);
            }
        } else {
            logger.debug("BridgeHandler ignoring frame {}. WHO={} is not supported by the binding", baseMsg,
                    baseMsg.getWho());
        }
    }

    @Override
    public void onConnected() {
        isGatewayConnected = true;
        Map<String, String> properties = editProperties();
        boolean propertiesChanged = false;
        if (gateway instanceof USBGateway) {
            logger.info("------------------- CONNECTED to USB (ZigBee) gateway - USB port: {}",
                    ((USBGateway) gateway).getSerialPortName());
        } else {
            logger.info("------------------- CONNECTED to BUS gateway '{}' ({}:{})", thing.getUID(),
                    ((BUSGateway) gateway).getHost(), ((BUSGateway) gateway).getPort());
            // update gw model //TODO keep this commented until gw model version is decoded from gw answer to *#13**15##
            /*
             * if (properties.get(PROPERTY_MODEL) != ((OpenGatewayBus) gateway).getModelName()) {
             * properties.put(PROPERTY_MODEL, ((OpenGatewayBus) gateway).getModelName());
             * propertiesChanged = true;
             * logger.debug("updated property gw model: {}", properties.get(PROPERTY_MODEL));
             * }
             */
            // update serial number property (with MAC address)
            if (properties.get(PROPERTY_SERIAL_NO) != gateway.getMACAddr().toUpperCase()) {
                properties.put(PROPERTY_SERIAL_NO, gateway.getMACAddr().toUpperCase());
                propertiesChanged = true;
                logger.debug("updated property gw serialNumber: {}", properties.get(PROPERTY_SERIAL_NO));
            }
        }
        if (properties.get(PROPERTY_FIRMWARE_VERSION) != gateway.getFirmwareVersion()) {
            properties.put(PROPERTY_FIRMWARE_VERSION, gateway.getFirmwareVersion());
            propertiesChanged = true;
            logger.debug("updated property gw firmware version: {}", properties.get(PROPERTY_FIRMWARE_VERSION));
        }
        if (propertiesChanged) {
            updateProperties(properties);
            logger.info("properties updated for '{}'", thing.getUID());
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onConnectionError(@Nullable OWNException error) {
        /*
         * switch (error) {
         * case LIB_LINKAGE_ERROR:
         * cause =
         * "Please check that the ZigBee USB Gateway is correctly plugged-in, and the driver installed and loaded";
         * break;
         * case NO_SERIAL_PORTS_ERROR:
         * cause = "No serial ports found";
         * break;
         * case JVM_ERROR:
         * cause = "Make sure you have a working Java Runtime Environment installed in 32 bit version";
         * break;
         * case IO_EXCEPTION_ERROR:
         * cause = "Connection error (IOException). Check network and gateway thing Configuration Parameters ("
         * + errMsg + ")";
         * break;
         * case AUTH_ERROR:
         * cause = "Authentication error. Check gateway password in Thing Configuration Parameters (" + errMsg
         * + ")";
         * break;
         *
         * }
         */
        logger.warn("------------------- ON CONNECTION ERROR: {}", error.getMessage());
        isGatewayConnected = false;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, error.getMessage());
        tryRecconectGateway();
    }

    @Override
    public void onConnectionClosed() {
        isGatewayConnected = false;
        logger.debug("onConnectionClosed() - isGatewayConnected={}", isGatewayConnected);
        // NOTE cannot change to OFFLINE here because we are already in REMOVING state
    }

    @Override
    public void onDisconnected(@Nullable OWNException e) {
        isGatewayConnected = false;
        logger.warn("------------------- DISCONNECTED from gateway. OWNException={}", e.getMessage());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "Disconnected from gateway (onDisconnected - " + e.getMessage() + ")");
        tryRecconectGateway();
    }

    private void tryRecconectGateway() {
        if (!reconnecting) {
            reconnecting = true;
            logger.info("------------------- Starting RECONNECT cycle to gateway");
            try {
                gateway.reconnect();
            } catch (OWNAuthException e) {
                logger.warn("------------------- AUTH error from gateway. Stopping recconnect");
                reconnecting = false;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Authentication error. Check gateway password in Thing Configuration Parameters (" + e + ")");
            }
        } else {
            logger.debug("------------------- reconnecting=true, do nothing");
        }
    }

    @Override
    public void onReconnected() {
        reconnecting = false;
        logger.info("------------------- RE-CONNECTED to gateway!");
        updateStatus(ThingStatus.ONLINE);
        if (gateway.getFirmwareVersion() != null) {
            this.updateProperty(PROPERTY_FIRMWARE_VERSION, gateway.getFirmwareVersion());
            logger.debug("gw firmware version: {}", gateway.getFirmwareVersion());
        }
        // TODO refresh devices' status?
    }

    /**
     * Return a ownId string (=WHO.WHERE) from a deviceWhere thing config parameter (already normalized) and its
     * handler.
     *
     * @param deviceWhere the device WHERE config parameter
     * @param handler the thing handler
     * @return the ownId
     */
    protected String ownIdFromDeviceWhere(@Nullable String deviceWhere, OpenWebNetThingHandler handler) {
        return handler.ownIdPrefix() + "." + deviceWhere;
    }

    /**
     * Returns a ownId string (=WHO.WHERE) from a Where address and Who
     *
     * @param where the Where address (to be normalized)
     * @param who the Who
     * @return the ownId
     */
    public String ownIdFromWhoWhere(Where where, Who who) {
        return who.value() + "." + normalizeWhere(where);
    }

    /**
     * Return a ownId string (=WHO.WHERE) from a BaseOpenMessage
     *
     * @param baseMsg the BaseOpenMessage
     * @return the ownId String
     */
    private String ownIdFromMessage(BaseOpenMessage baseMsg) {
        return baseMsg.getWho().value() + "." + normalizeWhere(baseMsg.getWhere());
    }

    /**
     * Transform a Where address into a Thing id string based on bridge type (BUS/USB ZigBee).
     * '#' in WHERE are changed to 'h'
     *
     * @param where the Where address
     * @return the thing Id
     */
    public String thingIdFromWhere(Where where) {
        return normalizeWhere(where).replace('#', 'h'); // '#' cannot be used in ThingUID;
    }

    /**
     * Normalize a Where address for Thermo and Zigbee devices
     *
     * @param where the Where address
     * @return the normalized address
     */
    public String normalizeWhere(Where where) {
        String str = "";
        if (isBusGateway) {
            if (where.value().indexOf('#') < 0) { // no hash present
                str = where.value();
            } else if (where.value().indexOf("#4#") > 0) { // local bus: APL#4#bus
                str = where.value();
            } else if (where.value().indexOf('#') == 0) { // thermo zone via central unit: #0 or #Z (Z=[1-99]) --> Z
                str = where.value().substring(1);
            } else if (where.value().indexOf('#') > 0) { // thermo zone and actuator N: Z#N (Z=[1-99], N=[1-9]) -- > Z
                str = where.value().substring(0, where.value().indexOf('#'));
            } else {
                logger.warn("normalizeWhere() unexpected WHERE: {}", where);
                str = where.value();
            }
            return str;
        } else {
            return where.value();
        }
    }

    // FIXME
    // @Override
    // public Collection<Class<? extends ThingHandlerService>> getServices() {
    // return Collections.singleton(ZigBeeGatewayDiscoveryService.class);
    // }

}
