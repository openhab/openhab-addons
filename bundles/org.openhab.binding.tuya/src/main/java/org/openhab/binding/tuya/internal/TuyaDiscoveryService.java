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
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_DEVICE_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_LOCAL_KEY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_PRODUCT_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_SUB_DEVICE_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.PROPERTY_CATEGORY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_GATEWAY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_SUB_DEVICE;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.cloud.TuyaOpenAPI;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceListInfo;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.cloud.dto.SubDeviceInfo;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.binding.tuya.internal.local.UdpDiscoverySender;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link TuyaDiscoveryService} implements the discovery service for Tuya devices from the cloud
 *
 * @author Jan N. Klug - Initial contribution
 * @author Maciej Jarzebowski - Add gateway and sub-device discovery
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TuyaDiscoveryService.class)
@NonNullByDefault
public class TuyaDiscoveryService extends AbstractThingHandlerDiscoveryService<ProjectHandler> {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_TUYA_DEVICE,
            THING_TYPE_TUYA_GATEWAY, THING_TYPE_TUYA_SUB_DEVICE);
    private static final int SEARCH_TIME = 5;
    private static final int GATEWAY_PROBE_ATTEMPTS = 2;
    private static final int GATEWAY_PROBE_RETRY_DELAY = 10; // Seconds
    // The factory information endpoint accepts at most 20 device ids per request
    private static final int FACTORY_INFORMATION_BATCH_SIZE = 20;

    private final Logger logger = LoggerFactory.getLogger(TuyaDiscoveryService.class);
    private final Gson gson = new Gson();
    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable ScheduledFuture<?> broadcastJob;

    private final UdpDiscoverySender udpDiscoverySender = new UdpDiscoverySender();

    private final ThingRegistry thingRegistry;

    @Activate
    public TuyaDiscoveryService(@Reference ThingRegistry thingRegistry) {
        super(ProjectHandler.class, SUPPORTED_THING_TYPES, SEARCH_TIME);
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void initialize() {
        ((ProjectHandler) thingHandler).setDiscoveryService(this);

        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();

        removeOlderResults(Instant.now());

        ((ProjectHandler) thingHandler).setDiscoveryService(null);
    }

    @Override
    public void startScan() {
        TuyaOpenAPI api = thingHandler.getApi();
        if (!api.isConnected()) {
            logger.debug("Tried to start scan but API for bridge '{}' is not connected.",
                    thingHandler.getThing().getUID());
            return;
        }

        collectDevices(new ArrayList<>(), List.of(), api, 0);
    }

    private void collectDevices(List<DeviceListInfo> allDevices, List<DeviceListInfo> page, TuyaOpenAPI api,
            int pageNo) {
        allDevices.addAll(page);
        if (pageNo == 0 || page.size() == 100) {
            int nextPageNo = pageNo + 1;
            thingHandler.getAllDevices(nextPageNo).whenComplete((nextPage, throwable) -> {
                if (throwable != null || nextPage == null) {
                    // Report what we have rather than nothing at all
                    logger.debug("Could not retrieve device list page {}", nextPageNo);
                    processDevices(allDevices, api);
                } else {
                    collectDevices(allDevices, nextPage, api, nextPageNo);
                }
            });
        } else {
            processDevices(allDevices, api);
        }
    }

    /**
     * Classifies the devices of the account and creates the discovery results.
     *
     * The device list does not tell which gateway a sub-device belongs to, so the gateways have to be identified by
     * asking every device for its sub-devices. The extra request is only made if the account contains sub-devices at
     * all. Note that a gateway can itself be flagged as a sub-device, so no device can be excluded up front.
     */
    private void processDevices(List<DeviceListInfo> allDevices, TuyaOpenAPI api) {
        if (allDevices.stream().noneMatch(device -> device.subDevice)) {
            collectMacAddresses(allDevices, api).thenAccept(macAddresses -> allDevices
                    .forEach(device -> processDevice(device, api, THING_TYPE_TUYA_DEVICE, macAddresses)));
            return;
        }

        probeGateway(new GatewayScan(allDevices, new HashMap<>(), new HashSet<>()), allDevices, 0, 1, api);
    }

    /**
     * Asks each device in turn for its sub-devices.
     *
     * The requests are chained rather than issued at once because a scan already makes several cloud calls per device
     * and the API rejects large bursts.
     */
    private void probeGateway(GatewayScan scan, List<DeviceListInfo> queue, int index, int attempt, TuyaOpenAPI api) {
        if (index < queue.size()) {
            DeviceListInfo device = queue.get(index);
            api.getSubDevices(device.id).handle((subDevices, throwable) -> {
                if (throwable != null) {
                    logger.debug("Could not determine whether device '{}' is a gateway: {}", device.id,
                            throwable.getMessage());
                    scan.unclassifiedDeviceIds().add(device.id);
                } else {
                    scan.unclassifiedDeviceIds().remove(device.id);
                    if (!subDevices.isEmpty()) {
                        scan.subDevicesByGateway().put(device.id, subDevices);
                    }
                }

                probeGateway(scan, queue, index + 1, attempt, api);
                return null;
            });
            return;
        }

        List<DeviceListInfo> failed = queue.stream().filter(device -> scan.unclassifiedDeviceIds().contains(device.id))
                .toList();
        if (!failed.isEmpty() && attempt < GATEWAY_PROBE_ATTEMPTS) {
            // The cloud rejects request bursts and a scan asks for a lot at once. Giving it a moment usually clears
            // a failed probe, and an unresolved device holds back the classification of the whole account.
            logger.debug("Retrying the gateway probe for {} device(s)", failed.size());
            scheduler.schedule(() -> probeGateway(scan, failed, 0, attempt + 1, api), GATEWAY_PROBE_RETRY_DELAY,
                    TimeUnit.SECONDS);
            return;
        }

        if (!failed.isEmpty()) {
            logger.warn("Could not determine whether {} device(s) are gateways, leaving them for the next scan",
                    failed.size());
        }

        reportDevices(scan, api);
    }

    /**
     * Creates the discovery results once every device has been classified. A device that reported sub-devices becomes
     * a gateway together with its children, a device claimed by a gateway is left to that gateway, and everything else
     * is a regular device.
     */
    private void reportDevices(GatewayScan scan, TuyaOpenAPI api) {
        collectMacAddresses(scan.allDevices(), api).thenAccept(macAddresses -> reportDevices(scan, api, macAddresses));
    }

    /**
     * Retrieves the MAC address of every device. The factory information endpoint takes a list of device ids, so the
     * whole account is covered by a handful of requests instead of one per device.
     *
     * The batches are chained rather than issued at once because the cloud rejects request bursts. A MAC address is
     * optional enrichment, so a failed batch only means those devices are reported without one.
     */
    private CompletableFuture<Map<String, String>> collectMacAddresses(List<DeviceListInfo> devices, TuyaOpenAPI api) {
        List<String> deviceIds = devices.stream().map(device -> device.id).toList();
        CompletableFuture<Map<String, String>> macAddresses = CompletableFuture.completedFuture(new HashMap<>());

        for (int from = 0; from < deviceIds.size(); from += FACTORY_INFORMATION_BATCH_SIZE) {
            List<String> batch = deviceIds.subList(from,
                    Math.min(from + FACTORY_INFORMATION_BATCH_SIZE, deviceIds.size()));
            macAddresses = macAddresses
                    .thenCompose(collected -> api.getFactoryInformation(batch).exceptionally(throwable -> {
                        logger.debug("Could not retrieve factory information for {} device(s): {}", batch.size(),
                                throwable.getMessage());
                        return List.of();
                    }).thenApply(factoryInformation -> {
                        factoryInformation.forEach(information -> collected.put(information.id, information.mac));
                        return collected;
                    }));
        }

        return macAddresses;
    }

    private void reportDevices(GatewayScan scan, TuyaOpenAPI api, Map<String, String> macAddresses) {
        Map<String, List<SubDeviceInfo>> subDevicesByGateway = scan.subDevicesByGateway();
        Set<String> claimed = subDevicesByGateway.values().stream().flatMap(List::stream).map(subDevice -> subDevice.id)
                .collect(Collectors.toSet());
        // Only a scan that classified every device knows for certain that no gateway claims a given device
        boolean complete = scan.unclassifiedDeviceIds().isEmpty();
        // If not a single device could be classified, the account cannot use the sub-device API at all. Report the
        // devices as plain ones rather than discovering nothing.
        boolean classified = scan.unclassifiedDeviceIds().size() < scan.allDevices().size();

        for (DeviceListInfo device : scan.allDevices()) {
            List<SubDeviceInfo> subDevices = subDevicesByGateway.get(device.id);
            if (subDevices != null) {
                ThingUID gatewayUid = processDevice(device, api, THING_TYPE_TUYA_GATEWAY, macAddresses);
                if (thingRegistry.get(gatewayUid) != null) {
                    subDevices.forEach(subDevice -> processSubDevice(subDevice, api, gatewayUid));
                } else {
                    // A sub-device cannot be created before its bridge exists, so it is only offered once the gateway
                    // has been added. The next scan reports them.
                    logger.debug("Not reporting the {} sub-devices of '{}' because its gateway thing does not exist",
                            subDevices.size(), device.id);
                }
                continue;
            }

            if (claimed.contains(device.id) || scan.unclassifiedDeviceIds().contains(device.id)) {
                // Reported by its gateway, or this scan could not tell what the device is
                continue;
            }

            if (!complete && classified) {
                // A gateway this scan could not ask may claim this device as its child. The cloud marks sub-devices
                // unreliably, so nothing rules that out here, and reporting the device as a regular one would
                // duplicate the entry its gateway creates once it can be reached again.
                logger.debug("Not reporting device '{}' because the scan could not classify every device", device.id);
                continue;
            }

            processDevice(device, api, THING_TYPE_TUYA_DEVICE, macAddresses);
        }
    }

    /**
     * The state of a single scan while the devices of the account are being classified.
     */
    private record GatewayScan(List<DeviceListInfo> allDevices, Map<String, List<SubDeviceInfo>> subDevicesByGateway,
            Set<String> unclassifiedDeviceIds) {
    }

    private ThingUID processDevice(DeviceListInfo device, TuyaOpenAPI api, ThingTypeUID thingTypeUID,
            Map<String, String> macAddresses) {
        ThingUID thingUid = new ThingUID(thingTypeUID, device.id);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_CATEGORY, device.category);
        properties.put(Thing.PROPERTY_MAC_ADDRESS,
                macAddresses.getOrDefault(device.id, "").replaceAll("(..)(?!$)", "$1:"));
        properties.put(CONFIG_LOCAL_KEY, device.localKey);
        properties.put(CONFIG_DEVICE_ID, device.id);
        properties.put(CONFIG_PRODUCT_ID, device.productId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUid).withLabel(device.name)
                .withRepresentationProperty(CONFIG_DEVICE_ID).withProperties(properties).build();

        refreshSchema(device.id, device.productId, api);

        thingDiscovered(discoveryResult);

        return thingUid;
    }

    private void processSubDevice(SubDeviceInfo subDevice, TuyaOpenAPI api, ThingUID gatewayUid) {
        ThingUID thingUid = new ThingUID(THING_TYPE_TUYA_SUB_DEVICE, gatewayUid, subDevice.id);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_CATEGORY, subDevice.category);
        properties.put(CONFIG_DEVICE_ID, subDevice.id);
        properties.put(CONFIG_PRODUCT_ID, subDevice.productId);
        properties.put(CONFIG_SUB_DEVICE_ID, subDevice.nodeId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUid).withLabel(subDevice.name)
                .withBridge(gatewayUid).withRepresentationProperty(CONFIG_DEVICE_ID).withProperties(properties).build();

        refreshSchema(subDevice.id, subDevice.productId, api);

        thingDiscovered(discoveryResult);
    }

    private void refreshSchema(String deviceId, String productId, TuyaOpenAPI api) {
        if (TuyaSchemaDB.contains(productId)) {
            // The schema of a product does not change, and a scan is already close to the request limits of the cloud
            return;
        }

        api.getDeviceSchema(deviceId).thenAccept(schema -> {
            List<SchemaDp> schemaDps = new ArrayList<>();
            schema.functions.forEach(description -> addUniqueSchemaDp(description, schemaDps, Boolean.FALSE));
            schema.status.forEach(description -> addUniqueSchemaDp(description, schemaDps, Boolean.TRUE));
            TuyaSchemaDB.put(productId, schemaDps);
        });
    }

    private void addUniqueSchemaDp(DeviceSchema.Description description, List<SchemaDp> schemaDps, Boolean readOnly) {
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

        schemaDps.add(SchemaDp.fromRemoteSchema(gson, description, readOnly));
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
