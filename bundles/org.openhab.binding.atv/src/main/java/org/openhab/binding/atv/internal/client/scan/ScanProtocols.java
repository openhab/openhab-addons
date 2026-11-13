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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlay;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayUtils;
import org.openhab.binding.atv.internal.client.protocols.companion.CompanionProtocolModule;
import org.openhab.binding.atv.internal.client.protocols.mrp.Mrp;
import org.openhab.binding.atv.internal.client.protocols.raop.Raop;

/**
 * Scan registry wiring for the four supported protocol modules (MRP, Companion,
 * AirPlay, RAOP). DMAP is out of scope and its service types are omitted.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class ScanProtocols {

    private ScanProtocols() {
    }

    /**
     * Registers scan handlers, device info extractors and service info methods for all
     * four supported protocols on a scanner.
     *
     * @param scanner scanner to register on
     */
    public static void registerAll(ScanOrchestrator scanner) {
        registerAll(scanner, null);
    }

    /**
     * Registers scan handlers, device info extractors and service info methods on a
     * scanner, restricted to the given protocols.
     *
     * @param scanner scanner to register on
     * @param protocols protocols to include, {@code null} or empty for all
     */
    public static void registerAll(ScanOrchestrator scanner, @Nullable Set<Protocol> protocols) {
        Function<String, String> uniqueShortName = name -> name;

        if (included(protocols, Protocol.MRP)) {
            scanner.addServiceInfo(Protocol.MRP, (service, deviceInfo, services) -> Mrp.serviceInfo(service));
            scanner.addService(Mrp.MEDIAREMOTE_SERVICE, ScanProtocols::mrpServiceHandler, uniqueShortName,
                    Mrp::deviceInfo);
        }
        if (included(protocols, Protocol.Companion)) {
            scanner.addServiceInfo(Protocol.Companion,
                    (service, deviceInfo, services) -> CompanionProtocolModule.serviceInfo(service));
            scanner.addService(CompanionProtocolModule.SERVICE_TYPE, ScanProtocols::companionServiceHandler,
                    uniqueShortName, (serviceType, properties) -> CompanionProtocolModule.deviceInfo(properties));
        }
        if (included(protocols, Protocol.AirPlay)) {
            scanner.addServiceInfo(Protocol.AirPlay,
                    (service, deviceInfo, services) -> AirPlayUtils.updateServiceDetails(service));
            scanner.addService(AirPlay.AIRPLAY_SERVICE, ScanProtocols::airplayServiceHandler, uniqueShortName,
                    AirPlay::deviceInfo);
        }
        if (included(protocols, Protocol.RAOP)) {
            scanner.addServiceInfo(Protocol.RAOP,
                    (service, deviceInfo, services) -> Raop.serviceInfo(service, services.get(Protocol.AirPlay)));
            scanner.addService(Raop.RAOP_SERVICE, ScanProtocols::raopServiceHandler, Raop::raopNameFromServiceName,
                    Raop::deviceInfo);
            scanner.addService(Raop.AIRPORT_SERVICE, (service, response) -> null, uniqueShortName, Raop::deviceInfo);
        }
    }

    private static boolean included(@Nullable Set<Protocol> protocols, Protocol protocol) {
        return protocols == null || protocols.isEmpty() || protocols.contains(protocol);
    }

    /**
     * Parses and returns a new MRP service; delegates to {@link Mrp#mrpServiceHandler},
     * which also disables the service on tvOS 15 or later.
     *
     * @param service discovered mDNS service
     * @param response full response
     * @return device name and service
     */
    public static ScanHandler.Result mrpServiceHandler(MdnsService service, MdnsResponse response) {
        Mrp.ScanResult result = Mrp.mrpServiceHandler(service.port(), service.properties());
        return new ScanHandler.Result(result.name(), result.service());
    }

    /**
     * Parses and returns a new Companion service.
     *
     * @param service discovered mDNS service
     * @param response full response
     * @return device name and service
     */
    public static ScanHandler.Result companionServiceHandler(MdnsService service, MdnsResponse response) {
        return new ScanHandler.Result(service.name(),
                new Service(getUniqueId(service.type(), service.name(), service.properties()), Protocol.Companion,
                        service.port(), service.properties()));
    }

    /**
     * Parses and returns a new AirPlay service.
     *
     * @param service discovered mDNS service
     * @param response full response
     * @return device name and service
     */
    public static ScanHandler.Result airplayServiceHandler(MdnsService service, MdnsResponse response) {
        return new ScanHandler.Result(service.name(),
                new Service(getUniqueId(service.type(), service.name(), service.properties()), Protocol.AirPlay,
                        service.port(), service.properties()));
    }

    /**
     * Parses and returns a new RAOP service.
     *
     * @param service discovered mDNS service
     * @param response full response
     * @return device name and service
     */
    public static ScanHandler.Result raopServiceHandler(MdnsService service, MdnsResponse response) {
        return new ScanHandler.Result(Raop.raopNameFromServiceName(service.name()),
                new Service(getUniqueId(service.type(), service.name(), service.properties()), Protocol.RAOP,
                        service.port(), service.properties()));
    }

    /**
     * Returns the unique identifier from a zeroconf service.
     *
     * @param serviceType zeroconf service type, e.g. {@code _mediaremotetv._tcp.local}
     * @param serviceName name of the service, e.g. {@code Office}
     * @param properties all key-value properties belonging to the service
     * @return the unique identifier, or {@code null} when not available
     */
    public static @Nullable String getUniqueId(String serviceType, String serviceName, Map<String, String> properties) {
        switch (serviceType) {
            case Mrp.MEDIAREMOTE_SERVICE:
                return properties.get("UniqueIdentifier");
            case AirPlay.AIRPLAY_SERVICE:
                return properties.get("deviceid");
            case CompanionProtocolModule.SERVICE_TYPE:
                // Apple TV devices on tvOS 16 (maybe earlier) have a static rpMRtID
                // identifier
                return properties.get("rpmrtid");
            case Raop.RAOP_SERVICE: {
                // Normally a RAOP device announces with "id@name" as zeroconf name, but
                // some devices leave out the id; some of those have the public key
                // ("pk") available, which is used as identifier in that case
                String[] split = serviceName.split("@", 2);
                if (split.length == 2) {
                    return split[0];
                }
                return properties.get("pk");
            }
            default:
                return null;
        }
    }
}
