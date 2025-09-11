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
package org.openhab.binding.insteon.internal.handler;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonHub1Configuration;
import org.openhab.binding.insteon.internal.config.InsteonHub2Configuration;
import org.openhab.binding.insteon.internal.config.InsteonPLMConfiguration;
import org.openhab.binding.insteon.internal.device.Device;
import org.openhab.binding.insteon.internal.device.DeviceAddress;
import org.openhab.binding.insteon.internal.device.DeviceCache;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.discovery.InsteonDiscoveryService;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * The {@link InsteonBridgeHandler} represents an insteon bridge handler.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonBridgeHandler extends InsteonBaseThingHandler implements BridgeHandler {
    private static final int DEVICE_STATISTICS_INTERVAL = 600; // seconds
    private static final int RETRY_INTERVAL = 30; // seconds
    private static final int START_DELAY = 5; // seconds

    private final ScheduledExecutorService insteonScheduler = ThreadPoolManager
            .getScheduledPool(BINDING_ID + "-" + getThingId());

    private @Nullable InsteonModem modem;
    private @Nullable InsteonDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> connectJob;
    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> resetJob;
    private @Nullable ScheduledFuture<?> statisticsJob;
    private HttpClient httpClient;
    private SerialPortManager serialPortManager;
    private Storage<DeviceCache> storage;
    private ThingRegistry thingRegistry;

    public InsteonBridgeHandler(Bridge bridge, HttpClient httpClient, SerialPortManager serialPortManager,
            StorageService storageService, ThingRegistry thingRegistry) {
        super(bridge);
        this.httpClient = httpClient;
        this.serialPortManager = serialPortManager;
        this.storage = storageService.getStorage(bridge.getUID().toString(), DeviceCache.class.getClassLoader());
        this.thingRegistry = thingRegistry;
    }

    @Override
    public Bridge getThing() {
        return (Bridge) super.getThing();
    }

    @Override
    public @Nullable InsteonModem getDevice() {
        return getModem();
    }

    @Override
    public @Nullable InsteonModem getModem() {
        return modem;
    }

    public @Nullable ProductData getProductData(DeviceAddress address) {
        DeviceCache deviceCache = getDeviceCache(address);
        if (deviceCache != null) {
            ProductData productData = deviceCache.getProductData();
            if (productData != null) {
                return productData;
            }
        }

        InsteonModem modem = getModem();
        if (modem != null) {
            return modem.getProductData(address);
        }

        return null;
    }

    protected InsteonBridgeConfiguration getBridgeConfig() {
        ThingTypeUID thingTypeUID = getThing().getThingTypeUID();
        if (THING_TYPE_HUB1.equals(thingTypeUID)) {
            return getConfigAs(InsteonHub1Configuration.class);
        } else if (THING_TYPE_HUB2.equals(thingTypeUID)) {
            return getConfigAs(InsteonHub2Configuration.class);
        } else if (THING_TYPE_PLM.equals(thingTypeUID)) {
            return getConfigAs(InsteonPLMConfiguration.class);
        } else {
            throw new UnsupportedOperationException("Unsupported bridge configuration");
        }
    }

    public int getDevicePollInterval() {
        return getBridgeConfig().getDevicePollInterval();
    }

    public int getDeviceResponseTimeout() {
        return getBridgeConfig().getDeviceResponseTimeout();
    }

    public boolean isDeviceDiscoveryEnabled() {
        return getBridgeConfig().isDeviceDiscoveryEnabled();
    }

    public boolean isSceneDiscoveryEnabled() {
        return getBridgeConfig().isSceneDiscoveryEnabled();
    }

    public boolean isDeviceSyncEnabled() {
        return getBridgeConfig().isDeviceSyncEnabled();
    }

    protected @Nullable InsteonDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public void setDiscoveryService(InsteonDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public @Nullable DeviceCache getDeviceCache(DeviceAddress address) {
        return storage.get(address.toString());
    }

    public void loadDeviceCache(Device device) {
        DeviceCache cache = getDeviceCache(device.getAddress());
        if (cache != null) {
            cache.load(device);
        }
    }

    public void storeDeviceCache(DeviceAddress address, DeviceCache cache) {
        storage.put(address.toString(), cache);
    }

    @Override
    public void initialize() {
        logger.debug("starting bridge {}", getThing().getUID());

        InsteonBridgeConfiguration config = getBridgeConfig();
        if (isDuplicateBridge(config)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Duplicate bridge.");
            return;
        }

        InsteonLegacyNetworkHandler legacyHandler = getLegacyNetworkHandler(config);
        if (legacyHandler != null) {
            logger.info("Disabling Insteon legacy network bridge {} in favor of bridge {}",
                    legacyHandler.getThing().getUID(), getThing().getUID());
            legacyHandler.disable();
        }

        InsteonModem modem = InsteonModem.makeModem(this, config, httpClient, insteonScheduler, serialPortManager);
        this.modem = modem;

        if (isInitialized()) {
            getChildHandlers().forEach(handler -> handler.bridgeThingUpdated(config, modem));
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Connecting to modem.");

        scheduler.execute(() -> {
            connectJob = scheduler.scheduleWithFixedDelay(() -> {
                if (!modem.connect()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Unable to connect to modem.");
                    return;
                }

                statisticsJob = scheduler.scheduleWithFixedDelay(() -> modem.logDeviceStatistics(), 0,
                        DEVICE_STATISTICS_INTERVAL, TimeUnit.SECONDS);

                cancelJob(connectJob, false);
            }, START_DELAY, RETRY_INTERVAL, TimeUnit.SECONDS);
        });
    }

    @Override
    public void dispose() {
        logger.debug("shutting down bridge {}", getThing().getUID());

        cancelJob(connectJob, true);
        cancelJob(reconnectJob, true);
        cancelJob(resetJob, true);
        cancelJob(statisticsJob, true);

        getChildHandlers().forEach(InsteonThingHandler::bridgeThingDisposed);

        InsteonModem modem = getModem();
        if (modem != null) {
            if (modem.isInitialized()) {
                storeDeviceCache(modem.getAddress(), DeviceCache.builder().withProductData(modem.getProductData())
                        .withDatabase(modem.getDB()).withFeatures(modem.getFeatures()).build());
            }
            modem.stopPolling();
            modem.disconnect();
        }
        this.modem = null;

        super.dispose();
    }

    @Override
    protected BridgeBuilder editThing() {
        return BridgeBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(thing.getBridgeUID())
                .withChannels(thing.getChannels()).withConfiguration(thing.getConfiguration())
                .withLabel(thing.getLabel()).withLocation(thing.getLocation()).withProperties(thing.getProperties());
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("added thing {}", childThing.getUID());
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("removed thing {}", childThing.getUID());
    }

    @Override
    protected String getConfigInfo() {
        return getBridgeConfig().toString();
    }

    @Override
    public void updateStatus() {
        InsteonModem modem = getModem();
        if (modem == null || !modem.isInitialized()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to determine modem.");
            return;
        }

        if (!modem.getDB().isComplete()) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Loading modem database.");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public Stream<InsteonThingHandler> getChildHandlers() {
        return getThing().getThings().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                .filter(InsteonThingHandler.class::isInstance).map(InsteonThingHandler.class::cast);
    }

    private @Nullable InsteonLegacyNetworkHandler getLegacyNetworkHandler(InsteonBridgeConfiguration config) {
        return thingRegistry.getAll().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                .filter(InsteonLegacyNetworkHandler.class::isInstance).map(InsteonLegacyNetworkHandler.class::cast)
                .filter(handler -> config.equals(handler.getBridgeConfig())).findFirst().orElse(null);
    }

    private boolean isDuplicateBridge(InsteonBridgeConfiguration config) {
        return thingRegistry.getAll().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                .filter(InsteonBridgeHandler.class::isInstance).map(InsteonBridgeHandler.class::cast)
                .anyMatch(handler -> !this.equals(handler) && config.equals(handler.getBridgeConfig()));
    }

    private void cleanUpStorage() {
        storage.getKeys().stream().filter(InsteonAddress::isValid).map(InsteonAddress::new)
                .forEach(this::cleanUpStorage);
    }

    private void cleanUpStorage(InsteonAddress address) {
        InsteonModem modem = getModem();
        if (modem != null && modem.getDB().isComplete() && !modem.getDB().hasEntry(address)
                && !modem.getAddress().equals(address)) {
            storage.remove(address.toString());
        }
    }

    protected void discoverInsteonDevice(InsteonAddress address, @Nullable ProductData productData) {
        InsteonDiscoveryService discoveryService = getDiscoveryService();
        if (discoveryService != null && isDeviceDiscoveryEnabled()) {
            scheduler.execute(() -> discoveryService.discoverInsteonDevice(address, productData));
        }
    }

    protected void discoverInsteonScene(int group) {
        InsteonDiscoveryService discoveryService = getDiscoveryService();
        if (discoveryService != null && isSceneDiscoveryEnabled()) {
            scheduler.execute(() -> discoveryService.discoverInsteonScene(group));
        }
    }

    protected void discoverMissingThings() {
        InsteonDiscoveryService discoveryService = getDiscoveryService();
        if (discoveryService != null) {
            scheduler.execute(() -> discoveryService.discoverMissingThings());
        }
    }

    public void reconnect(InsteonModem modem) {
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            if (!modem.reconnect()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to reconnect to modem.");
                return;
            }

            updateStatus();

            cancelJob(reconnectJob, false);
        }, 0, RETRY_INTERVAL, TimeUnit.SECONDS);
    }

    public void reset(long delay) {
        scheduler.execute(() -> {
            logger.trace("resetting bridge {}", getThing().getUID());

            dispose();

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "Resetting bridge.");

            resetJob = scheduler.schedule(this::initialize, delay, TimeUnit.SECONDS);
        });
    }

    /**
     * Notifies that the modem has been discovered
     *
     * @param modem the discovered modem
     */
    public void modemDiscovered(InsteonModem modem) {
        initializeChannels(modem);
        updateProperties(modem);
        loadDeviceCache(modem);

        if (!modem.getDB().isComplete()) {
            modem.getDB().load();
        }

        updateStatus();
    }

    /**
     * Notifies that the modem database has completed
     */
    public void modemDBCompleted() {
        discoverMissingThings();
        cleanUpStorage();
    }

    /**
     * Notifies that a modem database link has been updated
     *
     * @param address the link address
     * @param group the link group
     */
    public void modemDBLinkUpdated(InsteonAddress address, int group) {
        discoverInsteonDevice(address, getProductData(address));
        discoverInsteonScene(group);
        cleanUpStorage(address);
    }

    /**
     * Notifies that a modem database product data has been updated
     *
     * @param address the device address
     * @param productData the product data
     */
    public void modemDBProductDataUpdated(InsteonAddress address, ProductData productData) {
        discoverInsteonDevice(address, productData);
    }
}
