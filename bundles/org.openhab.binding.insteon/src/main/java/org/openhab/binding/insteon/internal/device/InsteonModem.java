/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.device.database.DatabaseManager;
import org.openhab.binding.insteon.internal.device.database.ModemDB;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.binding.insteon.internal.transport.Port;
import org.openhab.binding.insteon.internal.transport.PortListener;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.io.transport.serial.SerialPortManager;

/**
 * The {@link InsteonModem} represents an Insteom modem
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonModem extends BaseDevice<InsteonAddress, InsteonBridgeHandler> implements PortListener {
    private static final int RESET_TIME = 20; // in seconds

    private Port port;
    private ModemDB modemDB;
    private DatabaseManager dbm;
    private LinkManager linker;
    private PollManager poller;
    private RequestManager requester;
    private Map<DeviceAddress, Device> devices = new ConcurrentHashMap<>();
    private Map<Integer, Scene> scenes = new ConcurrentHashMap<>();
    private @Nullable X10Address lastX10Address;
    private boolean initialized = false;
    private int msgsReceived = 0;

    public InsteonModem(InsteonBridgeConfiguration config, HttpClient httpClient, ScheduledExecutorService scheduler,
            SerialPortManager serialPortManager) {
        super(InsteonAddress.UNKNOWN);
        this.port = new Port(config, httpClient, scheduler, serialPortManager);
        this.modemDB = new ModemDB(this);
        this.dbm = new DatabaseManager(this, scheduler);
        this.linker = new LinkManager(this, scheduler);
        this.poller = new PollManager(config.getDevicePollInterval(), scheduler);
        this.requester = new RequestManager(scheduler);
    }

    @Override
    public @Nullable InsteonModem getModem() {
        return this;
    }

    public Port getPort() {
        return port;
    }

    public ModemDB getDB() {
        return modemDB;
    }

    public DatabaseManager getDBM() {
        return dbm;
    }

    public LinkManager getLinkManager() {
        return linker;
    }

    public PollManager getPollManager() {
        return poller;
    }

    public RequestManager getRequestManager() {
        return requester;
    }

    public @Nullable Device getDevice(DeviceAddress address) {
        return devices.get(address);
    }

    public boolean hasDevice(DeviceAddress address) {
        return devices.containsKey(address);
    }

    public List<Device> getDevices() {
        return devices.values().stream().toList();
    }

    public @Nullable InsteonDevice getInsteonDevice(InsteonAddress address) {
        return (InsteonDevice) getDevice(address);
    }

    public List<InsteonDevice> getInsteonDevices() {
        return getDevices().stream().filter(InsteonDevice.class::isInstance).map(InsteonDevice.class::cast).toList();
    }

    public @Nullable X10Device getX10Device(X10Address address) {
        return (X10Device) getDevice(address);
    }

    public List<X10Device> getX10Devices() {
        return getDevices().stream().filter(X10Device.class::isInstance).map(X10Device.class::cast).toList();
    }

    public @Nullable Scene getScene(int group) {
        return scenes.get(group);
    }

    public boolean hasScene(int group) {
        return scenes.containsKey(group);
    }

    public List<Scene> getScenes() {
        return scenes.values().stream().toList();
    }

    public @Nullable InsteonScene getInsteonScene(int group) {
        return (InsteonScene) getScene(group);
    }

    public List<InsteonScene> getInsteonScenes() {
        return getScenes().stream().filter(InsteonScene.class::isInstance).map(InsteonScene.class::cast).toList();
    }

    public @Nullable ProductData getProductData(DeviceAddress address) {
        Device device = getDevice(address);
        if (device != null && device.getProductData() != null) {
            return device.getProductData();
        } else if (address instanceof InsteonAddress insteonAddress) {
            return modemDB.getProductData(insteonAddress);
        }
        return null;
    }

    public void addDevice(Device device) {
        devices.put(device.getAddress(), device);
    }

    public void removeDevice(Device device) {
        devices.remove(device.getAddress());
    }

    public void addScene(InsteonScene scene) {
        scenes.put(scene.getGroup(), scene);
    }

    public void removeScene(InsteonScene scene) {
        scenes.remove(scene.getGroup());
    }

    public void deleteSceneEntries(InsteonDevice device) {
        getInsteonScenes().stream().filter(scene -> scene.getDevices().contains(device.getAddress()))
                .forEach(scene -> scene.deleteEntries(device.getAddress()));
    }

    public void updateSceneEntries(InsteonDevice device) {
        getInsteonScenes().stream()
                .filter(scene -> modemDB.getRelatedDevices(scene.getGroup()).contains(device.getAddress()))
                .forEach(scene -> scene.updateEntries(device));
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void writeMessage(Msg msg) {
        port.writeMessage(msg);
    }

    public boolean connect() {
        logger.debug("connecting to modem");
        if (!port.start()) {
            return false;
        }

        port.registerListener(this);

        discover();

        return true;
    }

    public void disconnect() {
        logger.debug("disconnecting from modem");
        requester.stop();
        poller.stop();
        linker.stop();
        dbm.stop();
        port.stop();
    }

    public boolean reconnect() {
        logger.debug("reconnecting to modem");
        requester.stop();
        poller.pause();
        linker.stop();
        dbm.stop();
        port.stop();

        if (!port.start()) {
            return false;
        }

        poller.resume();

        return true;
    }

    private void discover() {
        if (isInitialized()) {
            logger.debug("modem {} already initialized", address);
        } else {
            logger.debug("discovering modem");
            getModemInfo();
        }
    }

    private void getModemInfo() {
        try {
            Msg msg = Msg.makeMessage("GetIMInfo");
            writeMessage(msg);
        } catch (InvalidMessageTypeException e) {
            logger.warn("error creating message", e);
        }
    }

    private void handleModemInfo(Msg msg) throws FieldException {
        InsteonAddress address = msg.getInsteonAddress("IMAddress");
        int deviceCategory = msg.getInt("DeviceCategory");
        int subCategory = msg.getInt("DeviceSubCategory");

        ProductData productData = ProductData.makeInsteonProduct(deviceCategory, subCategory);
        productData.setFirmwareVersion(msg.getInt("FirmwareVersion"));

        DeviceType deviceType = productData.getDeviceType();
        if (deviceType == null) {
            logger.warn("unsupported product data for modem {} devCat:{} subCat:{}", address,
                    HexUtils.getHexString(deviceCategory), HexUtils.getHexString(subCategory));
            return;
        }
        setAddress(address);
        setProductData(productData);
        instantiateFeatures(deviceType);
        setFlags(deviceType.getFlags());

        initialized = true;

        logger.debug("modem discovered: {}", this);

        InsteonBridgeHandler handler = getHandler();
        if (handler != null) {
            handler.modemDiscovered(this);
        }
    }

    public void reset() {
        try {
            Msg msg = Msg.makeMessage("ResetIM");
            writeMessage(msg);
        } catch (InvalidMessageTypeException e) {
            logger.warn("error creating message", e);
        }
    }

    private void handleModemReset() {
        logger.debug("modem reset initiated");

        InsteonBridgeHandler handler = getHandler();
        if (handler != null) {
            handler.reset(RESET_TIME);
        }
    }

    public void logDeviceStatistics() {
        logger.debug("devices: {} configured, {} polling, msgs received: {}", getDevices().size(),
                getPollManager().getPollCount(), msgsReceived);
        msgsReceived = 0;
    }

    private void logDevicesAndScenes() {
        if (!getInsteonDevices().isEmpty()) {
            logger.debug("configured {} insteon devices", getInsteonDevices().size());
            if (logger.isTraceEnabled()) {
                getInsteonDevices().stream().map(String::valueOf).forEach(logger::trace);
            }
        }
        if (!getX10Devices().isEmpty()) {
            logger.debug("configured {} x10 devices", getX10Devices().size());
            if (logger.isTraceEnabled()) {
                getX10Devices().stream().map(String::valueOf).forEach(logger::trace);
            }
        }
        if (!getScenes().isEmpty()) {
            logger.debug("configured {} insteon scenes", getScenes().size());
            if (logger.isTraceEnabled()) {
                getScenes().stream().map(String::valueOf).forEach(logger::trace);
            }
        }
    }

    /**
     * Polls related devices to a broadcast group
     *
     * @param group the broadcast group
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollRelatedDevices(int group, long delay) {
        modemDB.getRelatedDevices(group).stream().map(this::getInsteonDevice).filter(Objects::nonNull)
                .map(Objects::requireNonNull).forEach(device -> {
                    logger.debug("polling related device {} to broadcast group {}", device.getAddress(), group);
                    device.pollResponders(address, group, delay);
                });
    }

    /**
     * Notifies that the database has been completed
     */
    public void databaseCompleted() {
        logger.debug("modem database completed");

        poller.setDeviceCount(modemDB.getDevices().size());

        getDevices().forEach(Device::refresh);
        getScenes().forEach(Scene::refresh);

        logDevicesAndScenes();

        startPolling();
        refresh();

        InsteonBridgeHandler handler = getHandler();
        if (handler != null) {
            handler.modemDBCompleted();
        }
    }

    /**
     * Notifies that a database link has been updated
     *
     * @param address the link address
     * @param group the link group
     * @param is2Way if two way update
     */
    public void databaseLinkUpdated(InsteonAddress address, int group, boolean is2Way) {
        if (!modemDB.isComplete()) {
            return;
        }
        logger.debug("modem database link updated for device {} group {} 2way {}", address, group, is2Way);

        poller.setDeviceCount(modemDB.getDevices().size());

        InsteonDevice device = getInsteonDevice(address);
        if (device != null) {
            device.refresh();
            // set link db to reload on next device poll if still in modem db and is two way update
            if (device.hasModemDBEntry() && is2Way) {
                device.getLinkDB().setReload(true);
            }
        }
        InsteonScene scene = getInsteonScene(group);
        if (scene != null) {
            scene.refresh();
        }
        InsteonBridgeHandler handler = getHandler();
        if (handler != null) {
            handler.modemDBLinkUpdated(address, group);
        }
    }

    /**
     * Notifies that a database product data has been updated
     *
     * @param address the device address
     * @param productData the updated product data
     */
    public void databaseProductDataUpdated(InsteonAddress address, ProductData productData) {
        if (!modemDB.isComplete()) {
            return;
        }
        logger.debug("product data updated for device {} {}", address, productData);

        InsteonDevice device = getInsteonDevice(address);
        if (device != null) {
            device.updateProductData(productData);
        }
        InsteonBridgeHandler handler = getHandler();
        if (handler != null) {
            handler.modemDBProductDataUpdated(address, productData);
        }
    }

    /**
     * Notifies that the modem port has disconnected
     */
    @Override
    public void disconnected() {
        logger.debug("modem port disconnected");

        InsteonBridgeHandler handler = getHandler();
        if (handler != null) {
            handler.reconnect(this);
        }
    }

    /**
     * Notifies that the modem port has received a message
     *
     * @param msg the message received
     */
    @Override
    public void messageReceived(Msg msg) {
        if (msg.isPureNack()) {
            return;
        }
        try {
            if (msg.isX10()) {
                handleX10Message(msg);
            } else if (msg.isInsteon()) {
                handleInsteonMessage(msg);
            } else {
                handleIMMessage(msg);
            }
        } catch (FieldException e) {
            logger.warn("error parsing msg: {}", msg, e);
        }
    }

    /**
     * Notifies that the modem port has sent a message
     *
     * @param msg the message sent
     */
    @Override
    public void messageSent(Msg msg) {
        if (msg.isAllLinkBroadcast()) {
            return;
        }
        try {
            DeviceAddress address = msg.isInsteon() ? msg.getInsteonAddress("toAddress")
                    : msg.isX10Address() ? msg.getX10Address() : msg.isX10Command() ? lastX10Address : getAddress();
            if (address == null) {
                return;
            }
            long time = System.currentTimeMillis();
            Device device = getAddress().equals(address) ? this : getDevice(address);
            if (device != null) {
                device.requestSent(msg, time);
            }
        } catch (FieldException e) {
            logger.warn("error parsing msg: {}", msg, e);
        }
    }

    private void handleIMMessage(Msg msg) throws FieldException {
        if (msg.getCommand() == 0x60 && msg.isReplyAck()) {
            // we got an im info reply ack
            handleModemInfo(msg);
        } else if (msg.getCommand() == 0x55 || msg.getCommand() == 0x67 && msg.isReplyAck()) {
            // we got a user reset detected message or im reset reply ack
            handleModemReset();
        } else {
            handleMessage(msg);
        }
    }

    private void handleInsteonMessage(Msg msg) throws FieldException {
        if (msg.isAllLinkBroadcast() && msg.isReply()) {
            return;
        }
        InsteonAddress toAddr = msg.getInsteonAddress("toAddress");
        if (msg.isReply()) {
            handleMessage(toAddr, msg);
        } else if (msg.isBroadcast() || msg.isAllLinkBroadcast() || getAddress().equals(toAddr)) {
            InsteonAddress fromAddr = msg.getInsteonAddress("fromAddress");
            handleMessage(fromAddr, msg);
        }
    }

    private void handleX10Message(Msg msg) throws FieldException {
        X10Address address = msg.isX10Address() ? msg.getX10Address() : lastX10Address;
        if (address != null) {
            handleMessage(address, msg);
            // store the x10 address to use with the next cmd
            lastX10Address = msg.isX10Address() ? address : null;

        }
    }

    private void handleMessage(DeviceAddress address, Msg msg) throws FieldException {
        Device device = getDevice(address);
        if (device == null) {
            logger.trace("unknown device with address {}, dropping message", address);
        } else if (msg.isReply()) {
            device.requestReplied(msg);
        } else {
            device.handleMessage(msg);
            msgsReceived++;
        }
    }

    /**
     * Factory method for creating a InsteonModem
     *
     * @param handler the bridge handler
     * @param config the bridge config
     * @param httpClient the http client
     * @param scheduler the scheduler service
     * @param serialPortManager the serial port manager
     * @return the newly created InsteonModem
     */
    public static InsteonModem makeModem(InsteonBridgeHandler handler, InsteonBridgeConfiguration config,
            HttpClient httpClient, ScheduledExecutorService scheduler, SerialPortManager serialPortManager) {
        InsteonModem modem = new InsteonModem(config, httpClient, scheduler, serialPortManager);
        modem.setHandler(handler);
        return modem;
    }
}
