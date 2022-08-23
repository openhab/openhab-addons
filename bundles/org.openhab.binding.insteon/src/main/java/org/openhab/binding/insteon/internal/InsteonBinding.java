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
package org.openhab.binding.insteon.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceCache;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.database.LinkDBRecord;
import org.openhab.binding.insteon.internal.device.database.ModemDB;
import org.openhab.binding.insteon.internal.device.database.ModemDBEntry;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.driver.DriverListener;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonSceneHandler;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class InsteonBinding implements DriverListener {
    private final Logger logger = LoggerFactory.getLogger(InsteonBinding.class);

    private InsteonBridgeHandler handler;
    private Driver driver;
    private Storage<DeviceCache> deviceStorage;
    private int msgsReceived = 0;

    public InsteonBinding(InsteonBridgeHandler handler, InsteonBridgeConfiguration config,
            ScheduledExecutorService scheduler, @Nullable SerialPortManager serialPortManager,
            StorageService storageService) {
        this.handler = handler;
        this.driver = new Driver(this, config, scheduler, serialPortManager);
        this.deviceStorage = storageService.getStorage(
                InsteonBindingConstants.BINDING_ID + DeviceCache.class.getSimpleName(),
                DeviceCache.class.getClassLoader());
    }

    public InsteonBridgeHandler getHandler() {
        return handler;
    }

    public Driver getDriver() {
        return driver;
    }

    public InsteonAddress getModemAddress() {
        return driver.getModemAddress();
    }

    public @Nullable InsteonDevice getModemDevice() {
        return driver.getModemDevice();
    }

    public ModemDB getModemDB() {
        return driver.getModemDB();
    }

    public boolean isModemDBComplete() {
        return driver.isModemDBComplete();
    }

    public @Nullable InsteonDevice getDevice(InsteonAddress address) {
        return handler.getDevice(address);
    }

    public @Nullable InsteonDevice getDevice(String address) {
        return InsteonAddress.isValid(address) ? getDevice(new InsteonAddress(address)) : null;
    }

    public @Nullable InsteonScene getScene(int group) {
        return handler.getScene(group);
    }

    public @Nullable DeviceCache getDeviceCache(InsteonAddress address) {
        return deviceStorage.get(address.toString());
    }

    public void storeDeviceCache(InsteonAddress address, DeviceCache cache) {
        deviceStorage.put(address.toString(), cache);
    }

    /**
     * Starts the binding polling
     *
     * @return true if driver started, otherwise false
     */
    public boolean startPolling() {
        return driver.start();
    }

    /**
     * Stops the binding polling
     */
    public void stopPolling() {
        driver.stop();
    }

    /**
     * Reconnects the driver port
     *
     * @return true if successful
     */
    public boolean reconnect() {
        stopPolling();
        return startPolling();
    }

    /**
     * Returns list of missing devices
     *
     * @return list of missing device addresses and product data
     */
    public Map<String, @Nullable ProductData> getMissingDevices() {
        return driver.getModemDB().getEntries().stream().filter(dbe -> handler.getDevice(dbe.getAddress()) == null)
                .peek(dbe -> logger.debug("device {} in the modem database, but not configured", dbe.getAddress()))
                .collect(HashMap::new, (map, dbe) -> {
                    DeviceCache cache = getDeviceCache(dbe.getAddress());
                    // use cached product data if not defined in modem db entry
                    ProductData productData = dbe.getProductData() == null && cache != null ? cache.getProductData()
                            : dbe.getProductData();
                    map.put(dbe.getAddress().toString(), productData);
                }, Map::putAll);
    }

    /**
     * Returns a list of missing scenes
     *
     * @return list of missing scene groups
     */
    public List<Integer> getMissingScenes() {
        return driver.getModemDB().getBroadcastGroups().stream().filter(group -> handler.getScene(group) == null)
                .peek(group -> logger.debug("scene {} in the modem database, but not configured", group))
                .collect(Collectors.toList());
    }

    /**
     * Returns list of available channels information
     *
     * @param thingId the thing id to match if provided
     * @return the list available channels information
     */
    public Map<String, String> getChannelsInfo(@Nullable String thingId) {
        return Stream.concat(Stream.of(handler), handler.getChildHandlers())
                .filter(handler -> thingId == null || handler.getThingId().equals(thingId))
                .flatMap(handler -> handler.getChannelsInfo().entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Returns map of configured devices information
     *
     * @return the map of configured devices information
     */
    public Map<String, String> getDevicesInfo() {
        return handler.getDeviceHandlers()
                .collect(Collectors.toMap(InsteonDeviceHandler::getThingId, InsteonDeviceHandler::getThingInfo));
    }

    /**
     * Returns map of configured scenes information
     *
     * @return the map of configured scenes information
     */
    public Map<String, String> getScenesInfo() {
        return handler.getSceneHandlers()
                .collect(Collectors.toMap(InsteonSceneHandler::getThingId, InsteonSceneHandler::getThingInfo));
    }

    /**
     * Returns specific device link database information
     *
     * @param address the device address
     * @return the list of link db records relevant to device
     */
    public @Nullable List<String> getDeviceDBInfo(String address) {
        InsteonDevice device = getDevice(address);
        if (device != null) {
            return device.getLinkDB().getRecords().stream().map(LinkDBRecord::toString).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Returns specific device product data information
     *
     * @param address the device address
     * @return the product data information relavant to device
     */
    public @Nullable String getDeviceProductData(String address) {
        InsteonDevice device = getDevice(address);
        if (device != null) {
            ProductData productData = device.getProductData();
            return productData != null ? productData.toString().replace("|", "\n") : "";
        }
        return null;
    }

    /**
     * Returns modem database information
     *
     * @return the list of modem db entries
     */
    public Map<String, String> getModemDBInfo() {
        return driver.getModemDB().getEntries().stream()
                .collect(Collectors.toMap(ModemDBEntry::getId, ModemDBEntry::toString));
    }

    /**
     * Logs device statistics
     */
    public void logDeviceStatistics() {
        if (logger.isDebugEnabled()) {
            logger.debug("devices: {} configured, {} polling, msgs received: {}", handler.getDevices().count(),
                    driver.getPoller().getSizeOfQueue(), msgsReceived);
        }
        msgsReceived = 0;
    }

    /**
     * Logs configured devices
     */
    private void logDevices() {
        if (logger.isDebugEnabled()) {
            logger.debug("configured {} devices:", handler.getDevices().count());
            handler.getDevices().map(String::valueOf).forEach(logger::debug);
        }
    }

    /**
     * Notifies that the driver port has disconnected
     */
    @Override
    public void disconnected() {
        handler.disconnected();
    }

    /**
     * Notifies that the modem database has been completed
     */
    @Override
    public void modemDBCompleted() {
        logger.debug("modem database completed");
        // refresh devices
        handler.getDevices().forEach(device -> {
            logger.trace("refreshing device {}", device.getAddress());
            device.refresh();
        });
        // refresh scenes
        handler.getScenes().forEach(scene -> {
            logger.trace("refreshing scene {}", scene.getGroup());
            scene.refresh();
        });
        // refresh bridge handler
        handler.refresh();
        // discover missing things
        handler.discoverMissingThings();
        // log devices
        logDevices();
    }

    /**
     * Notifies that the modem database has been updated
     *
     * @param address the updated device address
     * @param group the updated link group
     */
    @Override
    public void modemDBUpdated(InsteonAddress address, int group) {
        if (!isModemDBComplete()) {
            return;
        }
        logger.debug("modem database link updated for device {} group {}", address, group);
        // refresh updated device if configured
        InsteonDevice device = handler.getDevice(address);
        if (device != null) {
            logger.trace("refreshing device {}", device.getAddress());
            device.refresh();
            // set link db to refresh on next device poll
            device.getLinkDB().setRefresh(true);
        }
        // refresh updated scene if configured
        InsteonScene scene = handler.getScene(group);
        if (scene != null) {
            logger.trace("refreshing scene {}", scene.getGroup());
            scene.refresh();
        }
        // discover missing things
        handler.discoverMissingThings();
    }

    /**
     * Notifies that the modem has been found
     *
     * @param device the modem device
     */
    @Override
    public void modemFound(InsteonDevice device) {
        logger.debug("found modem {}", device);
        // set modem poll interval
        int devicePollInterval = handler.getInsteonBridgeConfig().getDevicePollInterval();
        device.setPollInterval(devicePollInterval);
        // update bridge properties
        handler.updateProperties(device);
    }

    /**
     * Notifies that the modem has been reset
     */
    @Override
    public void modemReset() {
        logger.debug("modem has been reset");
        // refresh devices
        handler.getDevices().forEach(device -> {
            logger.trace("refreshing device {}", device.getAddress());
            device.refresh();
        });
        // refresh scenes
        handler.getScenes().forEach(scene -> {
            logger.trace("refreshing scene {}", scene.getGroup());
            scene.refresh();
        });
    }

    /**
     * Notifies that a product data has been updated
     *
     * @param address the updated product data device address
     */
    @Override
    public void productDataUpdated(InsteonAddress address) {
        if (!isModemDBComplete()) {
            return;
        }
        logger.debug("product data updated for device {}", address);
        ProductData productData = driver.getModemDB().getProductData(address);
        if (productData == null) {
            return;
        }
        InsteonDevice device = handler.getDevice(address);
        if (device != null) {
            // update device product data if configured
            device.updateProductData(productData);
        } else if (handler.isDeviceDiscoveryEnabled()) {
            // discover device otherwise if device discovery enabled
            handler.discoverInsteonDevice(address, productData);
        }
    }

    /**
     * Notifies that a message has been received from a device
     *
     * @param address the device address the message was received from
     * @param msg the message received
     */
    @Override
    public void messageReceived(InsteonAddress address, Msg msg) {
        InsteonDevice device = handler.getDevice(address);
        if (device == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("unknown device with address {}, dropping message", address);
            }
        } else {
            device.handleMessage(msg);
            msgsReceived++;
        }
    }

    /**
     * Notifies that a request has been sent to a device
     *
     * @param address the device address the request was sent to
     * @param time the time the request was sent
     */
    @Override
    public void requestSent(InsteonAddress address, long time) {
        InsteonDevice device = handler.getDevice(address);
        if (device != null) {
            device.requestSent(time);
        }
    }

    /**
     * Notifies that an in message has been received
     *
     * @param msg the message received
     */
    @Override
    public void imMessageReceived(Msg msg) {
        InsteonDevice device = getModemDevice();
        if (device == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("modem not initialized, dropping message");
            }
        } else {
            device.handleMessage(msg);
            msgsReceived++;
        }
    }

    /**
     * Notifies that an im request has been sent
     *
     * @param time the time the request was sent
     */
    @Override
    public void imRequestSent(long time) {
        InsteonDevice device = getModemDevice();
        if (device != null) {
            device.requestSent(time);
        }
    }
}
