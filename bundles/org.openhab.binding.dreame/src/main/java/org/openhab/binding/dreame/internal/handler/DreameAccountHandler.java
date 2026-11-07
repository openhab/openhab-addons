/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dreame.internal.handler;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.api.DreameCloudException;
import org.openhab.binding.dreame.internal.api.DreameCloudService;
import org.openhab.binding.dreame.internal.api.DreameMowerApi;
import org.openhab.binding.dreame.internal.api.DreameVacuumApi;
import org.openhab.binding.dreame.internal.config.DreameAccountConfiguration;
import org.openhab.binding.dreame.internal.discovery.DreameMowerDiscoveryService;
import org.openhab.binding.dreame.internal.discovery.DreameVacuumDiscoveryService;
import org.openhab.binding.dreame.internal.model.DreameAction;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameMapData;
import org.openhab.binding.dreame.internal.model.DreameMowingStatistics;
import org.openhab.binding.dreame.internal.model.DreameMqttConfiguration;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;
import org.openhab.binding.dreame.internal.model.DreameVacuumAction;
import org.openhab.binding.dreame.internal.model.DreameVacuumProperties;
import org.openhab.binding.dreame.internal.model.DreameVacuumSetting;
import org.openhab.binding.dreame.internal.util.DreameVacuumMapImage.MapListRevision;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the selected cloud session shared by mower and vacuum things.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameAccountHandler extends BaseBridgeHandler {

    private static final long DEVICE_METADATA_REFRESH_MINUTES = 15;
    private static final long INITIAL_RETRY_MINUTES = 1;

    private final Logger logger = LoggerFactory.getLogger(DreameAccountHandler.class);
    private final DreameMowerApi apiClient;
    private final AtomicInteger lifecycleGeneration = new AtomicInteger();
    private final Object lifecycleLock = new Object();
    private volatile List<DreameDevice> devices = List.of();
    private volatile @Nullable ScheduledFuture<?> scheduledJob;
    private volatile @Nullable DreameMowerDiscoveryService discoveryService;
    private volatile @Nullable DreameVacuumDiscoveryService vacuumDiscoveryService;

    public DreameAccountHandler(Bridge bridge, DreameMowerApi apiClient) {
        super(bridge);
        this.apiClient = apiClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The account bridge does not expose command channels.
    }

    @Override
    public void initialize() {
        int generation;
        synchronized (lifecycleLock) {
            stopScheduledJob();
            generation = lifecycleGeneration.incrementAndGet();
        }
        DreameAccountConfiguration config = getConfigAs(DreameAccountConfiguration.class);
        if (config.cloudService.isBlank() || config.username.isBlank() || config.password.isBlank()
                || config.country.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.account-credentials");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> connect(config, generation, INITIAL_RETRY_MINUTES));
    }

    private void connect(DreameAccountConfiguration config, int generation, long retryMinutes) {
        if (generation != lifecycleGeneration.get()) {
            return;
        }
        DreameCloudService cloudService;
        try {
            cloudService = DreameCloudService.fromConfiguration(config.cloudService);
            apiClient.login(config.username, config.password, config.country, cloudService);
            List<DreameDevice> discoveredDevices = apiClient.getDevices();
            if (generation != lifecycleGeneration.get()) {
                return;
            }
            devices = List.copyOf(discoveredDevices);
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Connected to {}; account contains {} supported device records", cloudService.label(),
                    devices.size());
            DreameMowerDiscoveryService discovery = discoveryService;
            if (discovery != null) {
                discovery.discoverDevices();
            }
            DreameVacuumDiscoveryService vacuumDiscovery = vacuumDiscoveryService;
            if (vacuumDiscovery != null) {
                vacuumDiscovery.discoverDevices();
            }
            scheduleMetadataRefresh(generation);
        } catch (DreameCloudException e) {
            if (generation != lifecycleGeneration.get()) {
                return;
            }
            logger.debug("Cloud connection failed: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.cloud");
            scheduleConnectionRetry(config, generation, retryMinutes);
        }
    }

    private void refreshDeviceMetadata(int generation) {
        if (generation != lifecycleGeneration.get()) {
            return;
        }
        try {
            List<DreameDevice> refreshedDevices = List.copyOf(apiClient.getDevices());
            if (generation != lifecycleGeneration.get()) {
                return;
            }
            List<DreameDevice> previousDevices = devices;
            devices = refreshedDevices;
            if (!previousDevices.equals(refreshedDevices)) {
                logger.debug("Refreshed cloud metadata for {} device records", refreshedDevices.size());
                DreameMowerDiscoveryService discovery = discoveryService;
                if (discovery != null) {
                    discovery.discoverDevices();
                }
                DreameVacuumDiscoveryService vacuumDiscovery = vacuumDiscoveryService;
                if (vacuumDiscovery != null) {
                    vacuumDiscovery.discoverDevices();
                }
            }
        } catch (DreameCloudException e) {
            if (generation == lifecycleGeneration.get()) {
                logger.debug("Cloud device metadata refresh failed; retaining the previous metadata");
            }
        }
    }

    public @Nullable DreameDevice getDevice(String deviceId) {
        return getDevices().stream().filter(device -> device.id().equals(deviceId)).findFirst().orElse(null);
    }

    public @Nullable DreameDevice getVacuumDevice(String deviceId) {
        return getVacuumDevices().stream().filter(device -> device.id().equals(deviceId)).findFirst().orElse(null);
    }

    public DreameStatus getMowerProperties(DreameDevice device, List<DreameProperty> properties)
            throws DreameCloudException {
        return apiClient.getProperties(device, properties);
    }

    public DreameMowingStatistics getMowingStatistics(DreameDevice device) throws DreameCloudException {
        return apiClient.getMowingStatistics(device);
    }

    public DreameMapData getMowerMapData(DreameDevice device) throws DreameCloudException {
        return apiClient.getMapData(device);
    }

    public void setMowerDnd(DreameDevice device, boolean enabled, @Nullable String taskConfiguration,
            BooleanSupplier isCurrent) throws DreameCloudException {
        apiClient.setDnd(device, enabled, taskConfiguration, isCurrent);
    }

    public void executeMowerAction(DreameDevice device, DreameAction action, BooleanSupplier isCurrent)
            throws DreameCloudException {
        apiClient.callAction(device, action, isCurrent);
    }

    public void startMowerZones(DreameDevice device, List<Integer> zoneIds, BooleanSupplier isCurrent)
            throws DreameCloudException {
        apiClient.startZoneMowing(device, zoneIds, isCurrent);
    }

    public void selectMowerMap(DreameDevice device, int mapIndex, BooleanSupplier isCurrent)
            throws DreameCloudException {
        apiClient.selectMap(device, mapIndex, isCurrent);
    }

    public BigDecimal getMowerCuttingHeight(DreameDevice device, int mapIndex) throws DreameCloudException {
        return apiClient.getCuttingHeight(device, mapIndex);
    }

    public void setMowerCuttingHeight(DreameDevice device, int mapIndex, BigDecimal height, BooleanSupplier isCurrent)
            throws DreameCloudException {
        apiClient.setCuttingHeight(device, mapIndex, height, isCurrent);
    }

    public DreameMqttConfiguration getMqttConfiguration(DreameDevice device) throws DreameCloudException {
        return apiClient.mqttConfiguration(device);
    }

    public DreameVacuumProperties getVacuumProperties(DreameDevice device, BooleanSupplier isCurrent)
            throws DreameCloudException {
        return vacuumApi().getVacuumProperties(device, isCurrent);
    }

    public @Nullable MapListRevision getVacuumMapListRevision(DreameDevice device, BooleanSupplier isCurrent)
            throws DreameCloudException {
        return vacuumApi().getVacuumMapListRevision(device, isCurrent);
    }

    public @Nullable String getVacuumMap(DreameDevice device, BooleanSupplier isCurrent) throws DreameCloudException {
        return vacuumApi().getVacuumMap(device, isCurrent);
    }

    public String getVacuumMapList(DreameDevice device, String objectName, BooleanSupplier isCurrent)
            throws DreameCloudException {
        return vacuumApi().getVacuumMapList(device, objectName, isCurrent);
    }

    public void executeVacuumAction(DreameDevice device, DreameVacuumAction action, BooleanSupplier isCurrent)
            throws DreameCloudException {
        vacuumApi().callVacuumAction(device, action, isCurrent);
    }

    public void setVacuumSetting(DreameDevice device, DreameVacuumSetting setting, int value, BooleanSupplier isCurrent)
            throws DreameCloudException {
        vacuumApi().setVacuumSetting(device, setting, value, isCurrent);
    }

    public void cleanVacuumRooms(DreameDevice device, List<Integer> roomIds, int suctionLevel, int waterVolume,
            BooleanSupplier isCurrent) throws DreameCloudException {
        vacuumApi().cleanVacuumRooms(device, roomIds, suctionLevel, waterVolume, isCurrent);
    }

    private DreameVacuumApi vacuumApi() throws DreameCloudException {
        if (apiClient instanceof DreameVacuumApi vacuumApi) {
            return vacuumApi;
        }
        throw new DreameCloudException("Vacuum operations are unavailable");
    }

    public List<DreameDevice> getDevices() {
        return devices.stream().filter(DreameDevice::isMower).toList();
    }

    public List<DreameDevice> getVacuumDevices() {
        return devices.stream().filter(DreameDevice::isVacuum).toList();
    }

    public void setVacuumDiscoveryService(@Nullable DreameVacuumDiscoveryService service) {
        vacuumDiscoveryService = service;
    }

    public void setDiscoveryService(@Nullable DreameMowerDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(DreameMowerDiscoveryService.class, DreameVacuumDiscoveryService.class);
    }

    @Override
    public void dispose() {
        synchronized (lifecycleLock) {
            lifecycleGeneration.incrementAndGet();
            stopScheduledJob();
        }
        discoveryService = null;
        vacuumDiscoveryService = null;
        devices = List.of();
        apiClient.logout();
        super.dispose();
    }

    private void scheduleMetadataRefresh(int generation) {
        synchronized (lifecycleLock) {
            if (generation != lifecycleGeneration.get()) {
                return;
            }
            stopScheduledJob();
            scheduledJob = scheduler.scheduleWithFixedDelay(() -> refreshDeviceMetadata(generation),
                    DEVICE_METADATA_REFRESH_MINUTES, DEVICE_METADATA_REFRESH_MINUTES, TimeUnit.MINUTES);
        }
    }

    private void scheduleConnectionRetry(DreameAccountConfiguration config, int generation, long retryMinutes) {
        synchronized (lifecycleLock) {
            if (generation != lifecycleGeneration.get()) {
                return;
            }
            stopScheduledJob();
            long nextRetryMinutes = Math.min(retryMinutes * 2, DEVICE_METADATA_REFRESH_MINUTES);
            scheduledJob = scheduler.schedule(() -> connect(config, generation, nextRetryMinutes), retryMinutes,
                    TimeUnit.MINUTES);
        }
    }

    private void stopScheduledJob() {
        ScheduledFuture<?> job = scheduledJob;
        scheduledJob = null;
        if (job != null) {
            job.cancel(false);
        }
    }
}
