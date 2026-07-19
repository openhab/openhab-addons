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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.support.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service discovery based on multicast browsing through a {@link JmDNS} instance.
 *
 * <p>
 * openHAB provides a shared jmDNS instance, so multicast scanning wraps it instead
 * of speaking mDNS directly (jmDNS cannot unicast-query a specific host, which is why
 * the unicast path in {@link UnicastScanner} has its own DNS implementation). All
 * registered service types are browsed and the results converted into the same
 * internal model that unicast scanning produces, then fed through the shared
 * orchestrator logic.
 *
 * <p>
 * Known limitation: device model lookup from {@code _device-info._tcp.local} is
 * best-effort only, since jmDNS resolves a service only when it has SRV and address
 * records, which that pseudo service usually lacks.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class JmdnsScanner extends ScanOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmdnsScanner.class);

    private final List<JmDNS> instances;
    private final boolean includeLoopback;

    /**
     * Creates a new scanner on an existing jmDNS instance.
     *
     * @param jmdns jmDNS instance to browse with (not closed by this class)
     */
    public JmdnsScanner(JmDNS jmdns) {
        this(jmdns, false);
    }

    /**
     * Creates a new scanner on an existing jmDNS instance.
     *
     * @param jmdns jmDNS instance to browse with (not closed by this class)
     * @param includeLoopback whether loopback addresses are acceptable service
     *            addresses; filtered out by default, but tests running against a
     *            loopback-bound jmDNS instance need them included
     */
    public JmdnsScanner(JmDNS jmdns, boolean includeLoopback) {
        this(List.of(jmdns), includeLoopback);
    }

    /**
     * Creates a new scanner browsing through several jmDNS instances, typically one per
     * local network interface. Results from all instances are merged before
     * aggregation, so a device answering on several interfaces collapses onto its
     * per-address configurations.
     *
     * @param instances jmDNS instances to browse with (not closed by this class)
     * @param includeLoopback whether loopback addresses are acceptable service addresses
     */
    public JmdnsScanner(List<JmDNS> instances, boolean includeLoopback) {
        this.instances = List.copyOf(instances);
        this.includeLoopback = includeLoopback;
    }

    @Override
    protected CompletableFuture<@Nullable Void> process(Duration timeout) {
        CompletableFuture<@Nullable Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("atv-jmdns-scan").start(() -> {
            try {
                processSync(timeout);
                result.complete(null);
            } catch (RuntimeException e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private void processSync(Duration timeout) {
        List<ServiceInfo> infos = new ArrayList<>();
        List<Thread> browsers = new ArrayList<>();
        Map<String, String> nameToModel = new LinkedHashMap<>();
        for (JmDNS jmdns : instances) {
            for (String serviceType : services()) {
                if (Mdns.DEVICE_INFO_SERVICE.equals(serviceType)) {
                    continue; // device-info is only queried per already known device, below
                }
                browsers.add(Thread.ofVirtual().start(() -> {
                    ServiceInfo[] found = jmdns.list(serviceType + ".", timeout.toMillis());
                    synchronized (infos) {
                        infos.addAll(List.of(found));
                    }
                }));
            }
            // Also browse device-info to (best-effort) learn device models
            browsers.add(Thread.ofVirtual().start(() -> {
                for (ServiceInfo info : jmdns.list(Mdns.DEVICE_INFO_SERVICE + ".", timeout.toMillis())) {
                    byte[] model = info.getPropertyBytes("model");
                    if (model != null) {
                        synchronized (nameToModel) {
                            nameToModel.put(info.getName(), Mdns.decodeValue(model));
                        }
                    }
                }
            }));
        }
        for (Thread browser : browsers) {
            try {
                browser.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Aggregate services per address
        Map<Inet4Address, List<MdnsService>> servicesByAddress = new LinkedHashMap<>();
        Map<Inet4Address, String> modelByAddress = new LinkedHashMap<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (ServiceInfo info : infos) {
            String type = stripDot(info.getType());
            // Every valid address gets the full service set, so a dual-homed device yields
            // one complete configuration per address
            for (Inet4Address address : info.getInet4Addresses()) {
                if (!isValidAddress(address)) {
                    continue;
                }
                if (!seen.add(type + "|" + info.getName() + "|" + address.getHostAddress())) {
                    continue; // same service reported by several jmDNS instances
                }
                servicesByAddress.computeIfAbsent(address, a -> new ArrayList<>())
                        .add(toMdnsService(type, info, address));
                if (!modelByAddress.containsKey(address)) {
                    String deviceName = deviceInfoName(type).apply(info.getName());
                    String model = nameToModel.get(deviceName);
                    if (model != null) {
                        modelByAddress.put(address, model);
                    }
                }
            }
        }

        // Process and callback each aggregated response
        for (Map.Entry<Inet4Address, List<MdnsService>> entry : servicesByAddress.entrySet()) {
            boolean deepSleep = entry.getValue().stream()
                    .allMatch(service -> service.port() == 0 && !Mdns.SLEEP_PROXY_SERVICE.equals(service.type()));
            handleResponse(new MdnsResponse(entry.getValue(), deepSleep, modelByAddress.get(entry.getKey())));
        }
    }

    private boolean isValidAddress(Inet4Address address) {
        if (address.isLinkLocalAddress() || address.isAnyLocalAddress()) {
            return false;
        }
        return includeLoopback || !address.isLoopbackAddress();
    }

    private static MdnsService toMdnsService(String type, ServiceInfo info, Inet4Address address) {
        CaseInsensitiveMap<String> properties = new CaseInsensitiveMap<>();
        Enumeration<String> names = info.getPropertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            byte[] value = info.getPropertyBytes(name);
            properties.put(name, value == null ? "" : Mdns.decodeValue(value));
        }
        LOGGER.debug("Found {} at {}:{} via jmDNS", info.getName(), address.getHostAddress(), info.getPort());
        return new MdnsService(type, info.getName(), address, info.getPort(), properties);
    }

    private static String stripDot(String type) {
        return type.endsWith(".") ? type.substring(0, type.length() - 1) : type;
    }
}
