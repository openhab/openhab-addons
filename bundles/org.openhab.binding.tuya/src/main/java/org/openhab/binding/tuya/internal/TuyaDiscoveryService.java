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
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_DEVICE_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_LOCAL_KEY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_PRODUCT_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.PROPERTY_CATEGORY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.cloud.TuyaOpenAPI;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceListInfo;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.binding.tuya.internal.local.UdpDiscoverySender;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link TuyaDiscoveryService} implements the discovery service for Tuya devices from the cloud
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TuyaDiscoveryService.class)
@NonNullByDefault
public class TuyaDiscoveryService extends AbstractThingHandlerDiscoveryService<ProjectHandler> {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_TUYA_DEVICE);
    private static final int SEARCH_TIME = 5;

    private final Logger logger = LoggerFactory.getLogger(TuyaDiscoveryService.class);
    private final Gson gson = new Gson();
    private @NonNullByDefault({}) Storage<String> storage;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable ScheduledFuture<?> broadcastJob;

    private final UdpDiscoverySender udpDiscoverySender = new UdpDiscoverySender();

    public TuyaDiscoveryService() {
        super(ProjectHandler.class, SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Override
    protected void startScan() {
        TuyaOpenAPI api = thingHandler.getApi();
        if (!api.isConnected()) {
            logger.debug("Tried to start scan but API for bridge '{}' is not connected.",
                    thingHandler.getThing().getUID());
            return;
        }

        processDeviceResponse(List.of(), api, 0);
    }

    private void processDeviceResponse(List<DeviceListInfo> deviceList, TuyaOpenAPI api, int page) {
        deviceList.forEach(device -> processDevice(device, api));
        if (page == 0 || deviceList.size() == 100) {
            int nextPage = page + 1;
            thingHandler.getAllDevices(nextPage)
                    .thenAccept(nextDeviceList -> processDeviceResponse(nextDeviceList, api, nextPage));
        }
    }

    private void processDevice(DeviceListInfo device, TuyaOpenAPI api) {
        api.getFactoryInformation(List.of(device.id)).thenAccept(fiList -> {
            ThingUID thingUid = new ThingUID(THING_TYPE_TUYA_DEVICE, device.id);
            String deviceMac = fiList.stream().filter(fi -> fi.id.equals(device.id)).findAny().map(fi -> fi.mac)
                    .orElse("");

            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_CATEGORY, device.category);
            properties.put(Thing.PROPERTY_MAC_ADDRESS,
                    Objects.requireNonNull(deviceMac).replaceAll("(..)(?!$)", "$1:"));
            properties.put(CONFIG_LOCAL_KEY, device.localKey);
            properties.put(CONFIG_DEVICE_ID, device.id);
            properties.put(CONFIG_PRODUCT_ID, device.productId);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUid).withLabel(device.name)
                    .withRepresentationProperty(CONFIG_DEVICE_ID).withProperties(properties).build();

            api.getDeviceSchema(device.id).thenAccept(schema -> {
                List<SchemaDp> schemaDps = new ArrayList<>();
                schema.functions.forEach(description -> addUniqueSchemaDp(description, schemaDps));
                schema.status.forEach(description -> addUniqueSchemaDp(description, schemaDps));
                storage.put(device.id, gson.toJson(schemaDps));
            });

            thingDiscovered(discoveryResult);
        });
    }

    private void addUniqueSchemaDp(DeviceSchema.Description description, List<SchemaDp> schemaDps) {
        if (description.dp_id == 0 || schemaDps.stream().anyMatch(schemaDp -> schemaDp.id == description.dp_id)) {
            // dp is missing or already present, skip it
            return;
        }
        // some devices report the same function code for different dps
        // we add an index only if this is the case
        String originalCode = description.code;
        int index = 1;
        while (schemaDps.stream().anyMatch(schemaDp -> schemaDp.code.equals(description.code))) {
            description.code = originalCode + "_" + index;
        }

        schemaDps.add(SchemaDp.fromRemoteSchema(gson, description));
    }

    @Override
    protected synchronized void stopScan() {
        ScheduledFuture<?> broadcastJob = this.broadcastJob;
        if (broadcastJob != null) {
            broadcastJob.cancel(true);
            this.broadcastJob = null;
        }
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public void initialize() {
        this.storage = thingHandler.getStorage();
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startBackgroundDiscovery() {
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            this.discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 1, 5, TimeUnit.MINUTES);
        }

        ScheduledFuture<?> broadcastJob = this.broadcastJob;
        if (broadcastJob == null || broadcastJob.isDone() || broadcastJob.isCancelled()) {
            this.broadcastJob = scheduler.scheduleWithFixedDelay(udpDiscoverySender::sendMessage, 5, 10,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopBackgroundDiscovery() {
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
        ScheduledFuture<?> broadcastJob = this.broadcastJob;
        if (broadcastJob != null) {
            broadcastJob.cancel(true);
            this.broadcastJob = null;
        }
    }
}
