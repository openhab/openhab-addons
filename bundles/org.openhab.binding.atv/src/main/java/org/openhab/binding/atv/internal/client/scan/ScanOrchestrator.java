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
package org.openhab.binding.atv.internal.client.scan;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.dto.DeviceInfo;
import org.openhab.binding.atv.internal.client.dto.DeviceModel;
import org.openhab.binding.atv.internal.client.dto.OperatingSystem;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base scanner for service discovery.
 *
 * <p>
 * Holds the registry mapping zeroconf service types to per-protocol scan handlers
 * and device-info extractors, accumulates a {@code FoundDevice} per IP address from
 * mDNS responses and finally builds one {@link AtvConfig} per device. Use
 * {@link ScanProtocols#registerAll} to populate the registry from the four protocol
 * modules, then call {@link #discover(Duration)}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class ScanOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanOrchestrator.class);

    /** Device information keys produced by protocol {@code device_info} extractors. */
    private static final String KEY_OPERATING_SYSTEM = "os";
    private static final String KEY_VERSION = "version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    private static final String KEY_MODEL = "model";
    private static final String KEY_RAW_MODEL = "raw_model";
    private static final String KEY_MAC = "mac";
    private static final String KEY_OUTPUT_DEVICE_ID = "output_device_id";

    private record ServiceTypeEntry(ScanHandler handler, DevInfoExtractor extractor) {
    }

    /** A device found during scanning. */
    private record FoundDevice(String name, Inet4Address address, boolean deepSleep, DeviceModel model,
            List<Service> services) {
    }

    private final Map<String, ServiceTypeEntry> serviceTypes = new LinkedHashMap<>();
    private final Map<String, Function<String, String>> deviceInfoNames = new LinkedHashMap<>();
    private final Map<Protocol, ServiceInfoMethod> serviceInfos = new LinkedHashMap<>();
    private final Map<Inet4Address, FoundDevice> foundDevices = new LinkedHashMap<>();
    private final Map<Inet4Address, Map<String, Map<String, String>>> properties = new LinkedHashMap<>();

    protected ScanOrchestrator() {
        serviceTypes.put(Mdns.DEVICE_INFO_SERVICE,
                new ServiceTypeEntry((service, response) -> null, (serviceType, props) -> Map.of()));
        serviceTypes.put(Mdns.SLEEP_PROXY_SERVICE,
                new ServiceTypeEntry((service, response) -> null, (serviceType, props) -> Map.of()));
        // A sleep proxy service is named "<number> <device name>"
        deviceInfoNames.put(Mdns.SLEEP_PROXY_SERVICE, name -> name.split(" ", 2)[1]);
    }

    /**
     * Adds a service type to discover.
     *
     * @param serviceType zeroconf service type
     * @param handler handler parsing services of this type
     * @param deviceInfoName maps a service name of this type to the name used by
     *            {@code _device-info._tcp.local}
     * @param extractor device information extractor of the owning protocol
     */
    public void addService(String serviceType, ScanHandler handler, Function<String, String> deviceInfoName,
            DevInfoExtractor extractor) {
        serviceTypes.put(serviceType, new ServiceTypeEntry(handler, extractor));
        deviceInfoNames.put(serviceType, deviceInfoName);
    }

    /**
     * Adds a service info updater method for a protocol.
     *
     * @param protocol the protocol
     * @param serviceInfo updater applied to each service after device merge
     */
    public void addServiceInfo(Protocol protocol, ServiceInfoMethod serviceInfo) {
        serviceInfos.put(protocol, serviceInfo);
    }

    /**
     * Returns the list of service types to scan for.
     *
     * @return registered service types (including {@code _device-info._tcp.local} and
     *         {@code _sleep-proxy._udp.local})
     */
    public List<String> services() {
        return new ArrayList<>(serviceTypes.keySet());
    }

    /**
     * Returns the device-info name function registered for a service type.
     *
     * @param serviceType zeroconf service type
     * @return the name mapping function, identity when none was registered
     */
    protected Function<String, String> deviceInfoName(String serviceType) {
        return deviceInfoNames.getOrDefault(serviceType, name -> name);
    }

    /**
     * Starts discovery of devices and services.
     *
     * @param timeout maximum scan duration
     * @return future completing with one configuration per found device address
     */
    public CompletableFuture<Map<InetAddress, AtvConfig>> discover(Duration timeout) {
        return process(timeout).thenApply(unused -> buildDevices());
    }

    /**
     * Starts processing devices and services; subclasses gather mDNS responses and
     * feed them to {@link #handleResponse(MdnsResponse)}.
     *
     * @param timeout maximum scan duration
     * @return future completing when all responses have been handled
     */
    protected abstract CompletableFuture<@Nullable Void> process(Duration timeout);

    /**
     * Handles a received mDNS response.
     *
     * @param response the response
     */
    protected void handleResponse(MdnsResponse response) {
        for (MdnsService service : response.services()) {
            ServiceTypeEntry entry = serviceTypes.get(service.type());
            if (entry == null) {
                LOGGER.debug("Discovered unsupported service {} for device {}", service.name(), service.type());
                continue;
            }
            try {
                serviceDiscovered(service, response, entry);
            } catch (RuntimeException e) {
                LOGGER.error("Failed to parse service: {}", service, e);
            }
        }
    }

    private void serviceDiscovered(MdnsService service, MdnsResponse response, ServiceTypeEntry entry) {
        Inet4Address address = service.address();
        if (address == null || service.port() == 0) {
            return;
        }

        ScanHandler.@Nullable Result result = entry.handler().handle(service, response);
        if (result != null) {
            LOGGER.debug("Auto-discovered {} at {}:{} via {} ({})", service.name(), address.getHostAddress(),
                    service.port(), result.service().protocol(), service.properties());
            foundDevices
                    .computeIfAbsent(address,
                            a -> new FoundDevice(result.name(), a, response.deepSleep(),
                                    DeviceInfoLookup.lookupInternalName(response.model()), new ArrayList<>()))
                    .services().add(result.service());
        }

        // Save properties for all services belonging to a device/address
        properties.computeIfAbsent(address, a -> new LinkedHashMap<>()).put(service.type(), service.properties());
    }

    private Map<InetAddress, AtvConfig> buildDevices() {
        Map<InetAddress, AtvConfig> devices = new LinkedHashMap<>();
        for (FoundDevice foundDevice : foundDevices.values()) {
            DeviceInfo deviceInfo = getDeviceInfo(foundDevice);

            AtvConfig config = new AtvConfig(foundDevice.address(), foundDevice.name(), foundDevice.deepSleep(),
                    properties.get(foundDevice.address()), deviceInfo);
            for (Service service : foundDevice.services()) {
                config.addService(service);
            }

            Map<Protocol, BaseService> propertiesMap = new LinkedHashMap<>();
            for (BaseService service : config.services()) {
                propertiesMap.put(service.protocol(), service);
            }

            // Apply service_info after adding all services in case a merge happened
            for (BaseService deviceService : config.services()) {
                ServiceInfoMethod serviceInfo = serviceInfos.get(deviceService.protocol());
                if (serviceInfo != null) {
                    serviceInfo.update((Service) deviceService, deviceInfo, propertiesMap);
                }
            }
            devices.put(foundDevice.address(), config);
        }
        return devices;
    }

    private DeviceInfo getDeviceInfo(FoundDevice device) {
        Map<String, Object> deviceInfo = new LinkedHashMap<>();

        // Extract device info from all service responses
        Map<String, Map<String, String>> deviceProperties = properties.getOrDefault(device.address(), Map.of());
        for (Map.Entry<String, Map<String, String>> serviceEntry : deviceProperties.entrySet()) {
            ServiceTypeEntry serviceType = serviceTypes.get(serviceEntry.getKey());
            if (serviceType != null) {
                dictMerge(deviceInfo, serviceType.extractor().extract(serviceEntry.getKey(), serviceEntry.getValue()));
            }
        }

        // If model was discovered via _device-info._tcp.local, manually add that to the
        // device info (dict_merge does not overwrite an already extracted model)
        if (device.model() != DeviceModel.Unknown) {
            dictMerge(deviceInfo, Map.of(KEY_MODEL, device.model()));
        }

        return toDeviceInfo(deviceInfo);
    }

    /** Merges items from {@code addition} into {@code target}, not overriding existing keys. */
    private static void dictMerge(Map<String, Object> target, Map<String, Object> addition) {
        for (Map.Entry<String, Object> entry : addition.entrySet()) {
            target.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private static DeviceInfo toDeviceInfo(Map<String, Object> fields) {
        DeviceInfo.Builder builder = DeviceInfo.builder();
        if (fields.get(KEY_OPERATING_SYSTEM) instanceof OperatingSystem os) {
            builder.operatingSystem(os);
        }
        if (fields.get(KEY_VERSION) instanceof String version) {
            builder.version(version);
        }
        if (fields.get(KEY_BUILD_NUMBER) instanceof String buildNumber) {
            builder.buildNumber(buildNumber);
        }
        if (fields.get(KEY_MODEL) instanceof DeviceModel model) {
            builder.model(model);
        }
        if (fields.get(KEY_RAW_MODEL) instanceof String rawModel) {
            builder.rawModel(rawModel);
        }
        if (fields.get(KEY_MAC) instanceof String mac) {
            builder.mac(mac);
        }
        if (fields.get(KEY_OUTPUT_DEVICE_ID) instanceof String outputDeviceId) {
            builder.outputDeviceId(outputDeviceId);
        }
        return builder.build();
    }

    /**
     * Filters discovered devices: devices that are not ready (no service with an
     * identifier) are dropped, and when identifiers are given only devices matching
     * one of them are kept.
     *
     * @param devices discovered configurations
     * @param identifiers identifiers to filter on, or {@code null}/empty for no filter
     * @return filtered devices
     */
    public static List<AtvConfig> filterDevices(Collection<AtvConfig> devices, Set<String> identifiers) {
        List<AtvConfig> filtered = new ArrayList<>();
        for (AtvConfig device : devices) {
            if (!device.ready()) {
                continue;
            }
            if (identifiers != null && !identifiers.isEmpty()
                    && device.allIdentifiers().stream().noneMatch(identifiers::contains)) {
                continue;
            }
            filtered.add(device);
        }
        return filtered;
    }
}
