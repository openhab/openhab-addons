/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonNetworkConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.DeviceFeatureListener;
import org.openhab.binding.insteon.internal.device.DeviceType;
import org.openhab.binding.insteon.internal.device.DeviceTypeLoader;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonDevice.DeviceStatus;
import org.openhab.binding.insteon.internal.device.RequestQueueManager;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.driver.DriverListener;
import org.openhab.binding.insteon.internal.driver.ModemDBEntry;
import org.openhab.binding.insteon.internal.driver.Poller;
import org.openhab.binding.insteon.internal.driver.Port;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonNetworkHandler;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.message.MsgListener;
import org.openhab.binding.insteon.internal.utils.Utils;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * A majority of the code in this file is from the openHAB 1 binding
 * org.openhab.binding.insteonplm.InsteonPLMActiveBinding. Including the comments below.
 *
 * -----------------------------------------------------------------------------------------------
 *
 * This class represents the actual implementation of the binding, and controls the high level flow
 * of messages to and from the InsteonModem.
 *
 * Writing this binding has been an odyssey through the quirks of the Insteon protocol
 * and Insteon devices. A substantial redesign was necessary at some point along the way.
 * Here are some of the hard learned lessons that should be considered by anyone who wants
 * to re-architect the binding:
 *
 * 1) The entries of the link database of the modem are not reliable. The category/subcategory entries in
 * particular have junk data. Forget about using the modem database to generate a list of devices.
 * The database should only be used to verify that a device has been linked.
 *
 * 2) Querying devices for their product information does not work either. First of all, battery operated devices
 * (and there are a lot of those) have their radio switched off, and may generally not respond to product
 * queries. Even main stream hardwired devices sold presently (like the 2477s switch and the 2477d dimmer)
 * don't even have a product ID. Although supposedly part of the Insteon protocol, we have yet to
 * encounter a device that would cough up a product id when queried, even among very recent devices. They
 * simply return zeros as product id. Lesson: forget about querying devices to generate a device list.
 *
 * 3) Polling is a thorny issue: too much traffic on the network, and messages will be dropped left and right,
 * and not just the poll related ones, but others as well. In particular sending back-to-back messages
 * seemed to result in the second message simply never getting sent, without flow control back pressure
 * (NACK) from the modem. For now the work-around is to space out the messages upon sending, and
 * in general poll as infrequently as acceptable.
 *
 * 4) Instantiating and tracking devices when reported by the modem (either from the database, or when
 * messages are received) leads to complicated state management because there is no guarantee at what
 * point (if at all) the binding configuration will be available. It gets even more difficult when
 * items are created, destroyed, and modified while the binding runs.
 *
 * For the above reasons, devices are only instantiated when they are referenced by binding information.
 * As nice as it would be to discover devices and their properties dynamically, we have abandoned that
 * path because it had led to a complicated and fragile system which due to the technical limitations
 * above was inherently squirrely.
 *
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class InsteonBinding {
    private static final int DEAD_DEVICE_COUNT = 10;

    private final Logger logger = LoggerFactory.getLogger(InsteonBinding.class);

    private Driver driver;
    private Map<InsteonAddress, InsteonDevice> devices = new ConcurrentHashMap<>();
    private Map<String, InsteonChannelConfiguration> bindingConfigs = new ConcurrentHashMap<>();
    private PortListener portListener = new PortListener();
    private int devicePollIntervalMilliseconds = 300000;
    private int deadDeviceTimeout = -1;
    private boolean driverInitialized = false;
    private int messagesReceived = 0;
    private boolean isActive = false; // state of binding
    private int x10HouseUnit = -1;
    private InsteonNetworkHandler handler;

    public InsteonBinding(InsteonNetworkHandler handler, InsteonNetworkConfiguration config,
            SerialPortManager serialPortManager, ScheduledExecutorService scheduler) {
        this.handler = handler;

        String port = config.getPort();
        logger.debug("port = '{}'", Utils.redactPassword(port));

        driver = new Driver(port, portListener, serialPortManager, scheduler);
        driver.addMsgListener(portListener);

        Integer devicePollIntervalSeconds = config.getDevicePollIntervalSeconds();
        if (devicePollIntervalSeconds != null) {
            devicePollIntervalMilliseconds = devicePollIntervalSeconds * 1000;
        }
        logger.debug("device poll interval set to {} seconds", devicePollIntervalMilliseconds / 1000);

        String additionalDevices = config.getAdditionalDevices();
        if (additionalDevices != null) {
            try {
                DeviceTypeLoader instance = DeviceTypeLoader.instance();
                if (instance != null) {
                    instance.loadDeviceTypesXML(additionalDevices);
                    logger.debug("read additional device definitions from {}", additionalDevices);
                } else {
                    logger.warn("device type loader instance is null");
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                logger.warn("error reading additional devices from {}", additionalDevices, e);
            }
        }

        String additionalFeatures = config.getAdditionalFeatures();
        if (additionalFeatures != null) {
            logger.debug("reading additional feature templates from {}", additionalFeatures);
            DeviceFeature.readFeatureTemplates(additionalFeatures);
        }

        deadDeviceTimeout = devicePollIntervalMilliseconds * DEAD_DEVICE_COUNT;
        logger.debug("dead device timeout set to {} seconds", deadDeviceTimeout / 1000);
    }

    public Driver getDriver() {
        return driver;
    }

    public boolean isDriverInitialized() {
        return driverInitialized;
    }

    public boolean startPolling() {
        logger.debug("starting to poll {}", driver.getPortName());
        driver.start();
        return driver.isRunning();
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void sendCommand(String channelName, Command command) {
        if (!isActive) {
            logger.debug("not ready to handle commands yet, returning.");
            return;
        }

        InsteonChannelConfiguration bindingConfig = bindingConfigs.get(channelName);
        if (bindingConfig == null) {
            logger.warn("unable to find binding config for channel {}", channelName);
            return;
        }

        InsteonDevice dev = getDevice(bindingConfig.getAddress());
        if (dev == null) {
            logger.warn("no device found with insteon address {}", bindingConfig.getAddress());
            return;
        }

        dev.processCommand(driver, bindingConfig, command);

        logger.debug("found binding config for channel {}", channelName);
    }

    public void addFeatureListener(InsteonChannelConfiguration bindingConfig) {
        logger.debug("adding listener for channel {}", bindingConfig.getChannelName());

        InsteonAddress address = bindingConfig.getAddress();
        InsteonDevice dev = getDevice(address);
        if (dev == null) {
            logger.warn("device for address {} is null", address);
            return;
        }
        @Nullable
        DeviceFeature f = dev.getFeature(bindingConfig.getFeature());
        if (f == null || f.isFeatureGroup()) {
            StringBuilder buf = new StringBuilder();
            ArrayList<String> names = new ArrayList<>(dev.getFeatures().keySet());
            Collections.sort(names);
            for (String name : names) {
                DeviceFeature feature = dev.getFeature(name);
                if (feature != null && !feature.isFeatureGroup()) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(name);
                }
            }

            logger.warn("channel {} references unknown feature: {}, it will be ignored. Known features for {} are: {}.",
                    bindingConfig.getChannelName(), bindingConfig.getFeature(), bindingConfig.getProductKey(),
                    buf.toString());
            return;
        }

        DeviceFeatureListener fl = new DeviceFeatureListener(this, bindingConfig.getChannelUID(),
                bindingConfig.getChannelName());
        fl.setParameters(bindingConfig.getParameters());
        f.addListener(fl);

        bindingConfigs.put(bindingConfig.getChannelName(), bindingConfig);
    }

    public void removeFeatureListener(ChannelUID channelUID) {
        String channelName = channelUID.getAsString();

        logger.debug("removing listener for channel {}", channelName);

        for (Iterator<Entry<InsteonAddress, InsteonDevice>> it = devices.entrySet().iterator(); it.hasNext();) {
            InsteonDevice dev = it.next().getValue();
            boolean removedListener = dev.removeFeatureListener(channelName);
            if (removedListener) {
                logger.trace("removed feature listener {} from dev {}", channelName, dev);
            }
        }
    }

    public void updateFeatureState(ChannelUID channelUID, State state) {
        handler.updateState(channelUID, state);
    }

    public @Nullable InsteonDevice makeNewDevice(InsteonAddress addr, String productKey,
            Map<String, Object> deviceConfigMap) {
        DeviceTypeLoader instance = DeviceTypeLoader.instance();
        if (instance == null) {
            return null;
        }
        DeviceType dt = instance.getDeviceType(productKey);
        if (dt == null) {
            return null;
        }
        InsteonDevice dev = InsteonDevice.makeDevice(dt);
        dev.setAddress(addr);
        dev.setProductKey(productKey);
        dev.setDriver(driver);
        dev.setIsModem(productKey.equals(InsteonDeviceHandler.PLM_PRODUCT_KEY));
        dev.setDeviceConfigMap(deviceConfigMap);
        if (!dev.hasValidPollingInterval()) {
            dev.setPollInterval(devicePollIntervalMilliseconds);
        }
        if (driver.isModemDBComplete() && dev.getStatus() != DeviceStatus.POLLING) {
            int ndev = checkIfInModemDatabase(dev);
            if (dev.hasModemDBEntry()) {
                dev.setStatus(DeviceStatus.POLLING);
                Poller.instance().startPolling(dev, ndev);
            }
        }
        devices.put(addr, dev);

        handler.insteonDeviceWasCreated();

        return (dev);
    }

    public void removeDevice(InsteonAddress addr) {
        InsteonDevice dev = devices.remove(addr);
        if (dev == null) {
            return;
        }

        if (dev.getStatus() == DeviceStatus.POLLING) {
            Poller.instance().stopPolling(dev);
        }
    }

    /**
     * Checks if a device is in the modem link database, and, if the database
     * is complete, logs a warning if the device is not present
     *
     * @param dev The device to search for in the modem database
     * @return number of devices in modem database
     */
    private int checkIfInModemDatabase(InsteonDevice dev) {
        try {
            InsteonAddress addr = dev.getAddress();
            Map<InsteonAddress, ModemDBEntry> dbes = driver.lockModemDBEntries();
            if (dbes.containsKey(addr)) {
                if (!dev.hasModemDBEntry()) {
                    logger.debug("device {} found in the modem database and {}.", addr, getLinkInfo(dbes, addr, true));
                    dev.setHasModemDBEntry(true);
                }
            } else {
                if (driver.isModemDBComplete() && !addr.isX10()) {
                    logger.warn("device {} not found in the modem database. Did you forget to link?", addr);
                    handler.deviceNotLinked(addr);
                }
            }
            return dbes.size();
        } finally {
            driver.unlockModemDBEntries();
        }
    }

    public Map<String, String> getDatabaseInfo() {
        try {
            Map<String, String> databaseInfo = new HashMap<>();
            Map<InsteonAddress, ModemDBEntry> dbes = driver.lockModemDBEntries();
            for (InsteonAddress addr : dbes.keySet()) {
                String a = addr.toString();
                databaseInfo.put(a, a + ": " + getLinkInfo(dbes, addr, false));
            }

            return databaseInfo;
        } finally {
            driver.unlockModemDBEntries();
        }
    }

    public boolean reconnect() {
        driver.stop();
        return startPolling();
    }

    /**
     * Everything below was copied from Insteon PLM v1
     */

    /**
     * Clean up all state.
     */
    public void shutdown() {
        logger.debug("shutting down Insteon bridge");
        driver.stop();
        devices.clear();
        RequestQueueManager.destroyInstance();
        Poller.instance().stop();
        isActive = false;
    }

    /**
     * Method to find a device by address
     *
     * @param aAddr the insteon address to search for
     * @return reference to the device, or null if not found
     */
    public @Nullable InsteonDevice getDevice(@Nullable InsteonAddress aAddr) {
        InsteonDevice dev = (aAddr == null) ? null : devices.get(aAddr);
        return (dev);
    }

    private String getLinkInfo(Map<InsteonAddress, ModemDBEntry> dbes, InsteonAddress a, boolean prefix) {
        ModemDBEntry dbe = dbes.get(a);
        if (dbe == null) {
            return "";
        }
        List<Byte> controls = dbe.getControls();
        List<Byte> responds = dbe.getRespondsTo();

        Port port = dbe.getPort();
        if (port == null) {
            return "";
        }
        String deviceName = port.getDeviceName();
        String s = deviceName.startsWith("/hub") ? "hub" : "plm";
        StringBuilder buf = new StringBuilder();
        if (port.isModem(a)) {
            if (prefix) {
                buf.append("it is the ");
            }
            buf.append(s);
            buf.append(" (");
            buf.append(Utils.redactPassword(deviceName));
            buf.append(")");
        } else {
            if (prefix) {
                buf.append("the ");
            }
            buf.append(s);
            buf.append(" controls groups (");
            buf.append(toGroupString(controls));
            buf.append(") and responds to groups (");
            buf.append(toGroupString(responds));
            buf.append(")");
        }

        return buf.toString();
    }

    private String toGroupString(List<Byte> group) {
        List<Byte> sorted = new ArrayList<>(group);
        Collections.sort(sorted, new Comparator<Byte>() {
            @Override
            public int compare(Byte b1, Byte b2) {
                int i1 = b1 & 0xFF;
                int i2 = b2 & 0xFF;
                return i1 < i2 ? -1 : i1 == i2 ? 0 : 1;
            }
        });

        StringBuilder buf = new StringBuilder();
        for (Byte b : sorted) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(b & 0xFF);
        }

        return buf.toString();
    }

    public void logDeviceStatistics() {
        String msg = String.format("devices: %3d configured, %3d polling, msgs received: %5d", devices.size(),
                Poller.instance().getSizeOfQueue(), messagesReceived);
        logger.debug("{}", msg);
        messagesReceived = 0;
        for (InsteonDevice dev : devices.values()) {
            if (dev.isModem()) {
                continue;
            }
            if (deadDeviceTimeout > 0 && dev.getPollOverDueTime() > deadDeviceTimeout) {
                logger.debug("device {} has not responded to polls for {} sec", dev.toString(),
                        dev.getPollOverDueTime() / 3600);
            }
        }
    }

    /**
     * Handles messages that come in from the ports.
     * Will only process one message at a time.
     */
    private class PortListener implements MsgListener, DriverListener {
        @Override
        public void msg(Msg msg) {
            if (msg.isEcho() || msg.isPureNack()) {
                return;
            }
            messagesReceived++;
            logger.debug("got msg: {}", msg);
            if (msg.isX10()) {
                handleX10Message(msg);
            } else {
                handleInsteonMessage(msg);
            }
        }

        @Override
        public void driverCompletelyInitialized() {
            List<String> missing = new ArrayList<>();
            try {
                Map<InsteonAddress, ModemDBEntry> dbes = driver.lockModemDBEntries();
                logger.debug("modem database has {} entries!", dbes.size());
                if (dbes.isEmpty()) {
                    logger.warn("the modem link database is empty!");
                }
                for (InsteonAddress k : dbes.keySet()) {
                    logger.debug("modem db entry: {}", k);
                }
                Set<InsteonAddress> addrs = new HashSet<>();
                for (InsteonDevice dev : devices.values()) {
                    InsteonAddress a = dev.getAddress();
                    if (!dbes.containsKey(a)) {
                        if (!a.isX10()) {
                            logger.warn("device {} not found in the modem database. Did you forget to link?", a);
                            handler.deviceNotLinked(a);
                        }
                    } else {
                        if (!dev.hasModemDBEntry()) {
                            addrs.add(a);
                            logger.debug("device {} found in the modem database and {}.", a,
                                    getLinkInfo(dbes, a, true));
                            dev.setHasModemDBEntry(true);
                        }
                        if (dev.getStatus() != DeviceStatus.POLLING) {
                            Poller.instance().startPolling(dev, dbes.size());
                        }
                    }
                }

                for (InsteonAddress k : dbes.keySet()) {
                    if (!addrs.contains(k)) {
                        logger.debug("device {} found in the modem database, but is not configured as a thing and {}.",
                                k, getLinkInfo(dbes, k, true));

                        missing.add(k.toString());
                    }
                }
            } finally {
                driver.unlockModemDBEntries();
            }

            if (!missing.isEmpty()) {
                handler.addMissingDevices(missing);
            }

            driverInitialized = true;
        }

        @Override
        public void disconnected() {
            handler.bindingDisconnected();
        }

        private void handleInsteonMessage(Msg msg) {
            InsteonAddress toAddr = msg.getAddr("toAddress");
            if (!msg.isBroadcast() && !driver.isMsgForUs(toAddr)) {
                // not for one of our modems, do not process
                return;
            }
            InsteonAddress fromAddr = msg.getAddr("fromAddress");
            if (fromAddr == null) {
                logger.debug("invalid fromAddress, ignoring msg {}", msg);
                return;
            }
            handleMessage(fromAddr, msg);
        }

        private void handleX10Message(Msg msg) {
            try {
                int x10Flag = msg.getByte("X10Flag") & 0xff;
                int rawX10 = msg.getByte("rawX10") & 0xff;
                if (x10Flag == 0x80) { // actual command
                    if (x10HouseUnit != -1) {
                        InsteonAddress fromAddr = new InsteonAddress((byte) x10HouseUnit);
                        handleMessage(fromAddr, msg);
                    }
                } else if (x10Flag == 0) {
                    // what unit the next cmd will apply to
                    x10HouseUnit = rawX10 & 0xFF;
                }
            } catch (FieldException e) {
                logger.warn("got bad X10 message: {}", msg, e);
                return;
            }
        }

        private void handleMessage(InsteonAddress fromAddr, Msg msg) {
            InsteonDevice dev = getDevice(fromAddr);
            if (dev == null) {
                logger.debug("dropping message from unknown device with address {}", fromAddr);
            } else {
                dev.handleMessage(msg);
            }
        }
    }
}
