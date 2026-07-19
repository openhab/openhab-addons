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
package org.openhab.binding.atv.internal.client.conf;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.DeviceInfo;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.exceptions.NoServiceError;
import org.openhab.binding.atv.internal.client.settings.Settings;

/**
 * Representation of a device configuration.
 *
 * <p>
 * An instance of this class represents a single device. A device can have several services depending on the
 * protocols it supports, e.g. MRP or AirPlay.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AtvConfig {

    /** Priority order used when selecting an identifier. */
    private static final List<Protocol> PROTOCOL_PRIORITY = List.of(Protocol.MRP, Protocol.AirPlay, Protocol.RAOP,
            Protocol.Companion);

    /**
     * Priority order used when selecting the main service. Deliberately excludes Companion: it is never treated as
     * a primary connection service, so a Companion-only configuration raises {@code NoServiceError}.
     */
    private static final List<Protocol> MAIN_SERVICE_PRIORITY = List.of(Protocol.MRP, Protocol.AirPlay, Protocol.RAOP);

    private final InetAddress address;
    private final String name;
    private final boolean deepSleep;
    private final Map<String, Map<String, String>> properties;
    private final Map<Protocol, BaseService> services = new LinkedHashMap<>();
    private DeviceInfo deviceInfo;

    /**
     * Creates a new configuration.
     *
     * @param address IP address of device
     * @param name name of device
     */
    public AtvConfig(InetAddress address, String name) {
        this(address, name, false, null, null);
    }

    /**
     * Creates a new configuration.
     *
     * @param address IP address of device
     * @param name name of device
     * @param deepSleep if the device is in deep sleep
     * @param properties Zeroconf service properties (per service type) or {@code null}
     * @param deviceInfo general device information or {@code null}
     */
    public AtvConfig(InetAddress address, String name, boolean deepSleep,
            @Nullable Map<String, Map<String, String>> properties, @Nullable DeviceInfo deviceInfo) {
        this.address = Objects.requireNonNull(address, "address");
        this.name = Objects.requireNonNull(name, "name");
        this.deepSleep = deepSleep;
        this.properties = properties == null ? new HashMap<>() : new HashMap<>(properties);
        this.deviceInfo = deviceInfo == null ? DeviceInfo.empty() : deviceInfo;
    }

    /**
     * Returns the IP address of the device.
     *
     * @return IP address
     */
    public InetAddress address() {
        return address;
    }

    /**
     * Returns the name of the device.
     *
     * @return device name
     */
    public String name() {
        return name;
    }

    /**
     * Returns if the device is in deep sleep.
     *
     * @return {@code true} if in deep sleep
     */
    public boolean deepSleep() {
        return deepSleep;
    }

    /**
     * Returns the Zeroconf properties (per service type).
     *
     * @return Zeroconf properties
     */
    public Map<String, Map<String, String>> properties() {
        return properties;
    }

    /**
     * Returns general device information.
     *
     * @return device information
     */
    public DeviceInfo deviceInfo() {
        return deviceInfo;
    }

    /**
     * Sets general device information.
     *
     * @param deviceInfo new device information
     */
    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = Objects.requireNonNull(deviceInfo, "deviceInfo");
    }

    /**
     * Adds a new service. If a service with the same protocol already exists, the new service is merged into it.
     *
     * @param service service to add
     */
    public void addService(BaseService service) {
        BaseService existing = services.get(service.protocol());
        if (existing != null) {
            existing.merge(service);
        } else {
            services.put(service.protocol(), service);
        }
    }

    /**
     * Looks up a service based on protocol.
     *
     * @param protocol protocol to look up
     * @return service if available
     */
    public Optional<BaseService> getService(Protocol protocol) {
        return Optional.ofNullable(services.get(protocol));
    }

    /**
     * Returns all supported services.
     *
     * @return all services
     */
    public List<BaseService> services() {
        return new ArrayList<>(services.values());
    }

    /**
     * Returns if the configuration is ready, i.e. has at least one service with an identifier.
     *
     * @return {@code true} if ready
     */
    public boolean ready() {
        return services.values().stream().anyMatch(service -> service.identifier().isPresent());
    }

    /**
     * Returns the main identifier associated with this device, using the priority order MRP, AirPlay, RAOP,
     * Companion.
     *
     * @return main identifier if available
     */
    public Optional<String> identifier() {
        for (Protocol protocol : PROTOCOL_PRIORITY) {
            BaseService service = services.get(protocol);
            if (service != null && service.identifier().isPresent()) {
                return service.identifier();
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all unique identifiers for this device.
     *
     * @return all identifiers
     */
    public List<String> allIdentifiers() {
        return services.values().stream().flatMap(service -> service.identifier().stream()).toList();
    }

    /**
     * Returns the suggested service used to establish a connection, using the priority order MRP, AirPlay, RAOP.
     * Companion is never chosen, so a Companion-only configuration raises {@link NoServiceError}.
     *
     * @return service to connect to
     * @throws NoServiceError if no service is available
     */
    public BaseService mainService() {
        for (Protocol protocol : MAIN_SERVICE_PRIORITY) {
            BaseService service = services.get(protocol);
            if (service != null) {
                return service;
            }
        }
        throw new NoServiceError("no service to connect to");
    }

    /**
     * Returns the service for a specific protocol to establish a connection with.
     *
     * @param protocol requested protocol
     * @return service to connect to
     * @throws NoServiceError if the service is not available
     */
    public BaseService mainService(Protocol protocol) {
        BaseService service = services.get(protocol);
        if (service == null) {
            throw new NoServiceError("no service to connect to");
        }
        return service;
    }

    /**
     * Sets credentials for a protocol if it exists.
     *
     * @param protocol protocol to set credentials for
     * @param credentials credentials to set
     * @return {@code true} if the service exists and credentials were set
     */
    public boolean setCredentials(Protocol protocol, String credentials) {
        BaseService service = services.get(protocol);
        if (service != null) {
            service.setCredentials(credentials);
            return true;
        }
        return false;
    }

    /**
     * Applies settings to this configuration, i.e. copies credentials and passwords into matching services.
     *
     * @param settings settings to apply
     */
    public void apply(Settings settings) {
        for (BaseService service : services.values()) {
            Map<String, Object> serviceSettings = new HashMap<>();
            switch (service.protocol()) {
                case AirPlay -> {
                    putIfPresent(serviceSettings, "credentials", settings.protocols().airplay().credentials());
                    putIfPresent(serviceSettings, "password", settings.protocols().airplay().password());
                }
                case Companion ->
                    putIfPresent(serviceSettings, "credentials", settings.protocols().companion().credentials());
                case MRP -> putIfPresent(serviceSettings, "credentials", settings.protocols().mrp().credentials());
                case RAOP -> {
                    putIfPresent(serviceSettings, "credentials", settings.protocols().raop().credentials());
                    putIfPresent(serviceSettings, "password", settings.protocols().raop().password());
                }
                default -> {
                    continue;
                }
            }
            service.apply(serviceSettings);
        }
    }

    private static void putIfPresent(Map<String, Object> map, String key, @Nullable String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof AtvConfig other && getClass() == other.getClass()
                && Objects.equals(identifier(), other.identifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier());
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("       Name: ").append(name).append('\n');
        output.append("   Model/SW: ").append(deviceInfo).append('\n');
        output.append("    Address: ").append(address.getHostAddress()).append('\n');
        output.append("        MAC: ").append(deviceInfo.mac().orElse(null)).append('\n');
        output.append(" Deep Sleep: ").append(deepSleep).append('\n');
        output.append("Identifiers:\n");
        for (String id : allIdentifiers()) {
            output.append(" - ").append(id).append('\n');
        }
        output.append("Services:");
        for (BaseService service : services.values()) {
            output.append("\n - ").append(service);
        }
        return output.toString();
    }
}
