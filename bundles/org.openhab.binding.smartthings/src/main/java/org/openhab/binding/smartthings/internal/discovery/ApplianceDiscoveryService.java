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
package org.openhab.binding.smartthings.internal.discovery;

import static org.openhab.binding.smartthings.internal.SmartThingsBindingConstants.THING_TYPE_APPLIANCE;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.ocf.Discovery;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.ScanListener;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User-triggered, bounded discovery on an explicit private subnet or the primary IPv4 interface's subnet.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Kai Kreuzer - Network-derived discovery defaults
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "binding.smartthings")
public class ApplianceDiscoveryService extends AbstractDiscoveryService {
    static final int CONCURRENCY = 4;
    static final int HOST_TIMEOUT_SECONDS = 3;
    static final int SCAN_TIMEOUT_SECONDS = 210;

    @FunctionalInterface
    interface Probe {
        Discovery.Descriptor discover(InetAddress host, int timeoutSeconds) throws IOException;
    }

    private final Logger logger = LoggerFactory.getLogger(ApplianceDiscoveryService.class);
    private final ScheduledExecutorService control;
    private final ScheduledExecutorService workers;
    private final Probe probe;
    private final NetworkAddressService networkAddressService;
    private final Supplier<Collection<CidrAddress>> interfaceAddresses;
    private final Semaphore slots = new Semaphore(CONCURRENCY);

    // Lifecycle transitions and publication are serialized; network requests never occupy this executor.
    private boolean active;
    private @Nullable Object configuredSubnet;
    private @Nullable Scan scan;

    @Activate
    public ApplianceDiscoveryService(@Reference NetworkAddressService networkAddressService) {
        super(Set.of(THING_TYPE_APPLIANCE), SCAN_TIMEOUT_SECONDS, false);
        control = ThreadPoolManager.getPoolBasedSequentialScheduledExecutorService("smartthings-discovery-control",
                "smartthings-discovery-control");
        workers = ThreadPoolManager.getScheduledPool("smartthings-discovery-probes");
        probe = Discovery::discoverSamsung;
        this.networkAddressService = networkAddressService;
        interfaceAddresses = NetUtil::getAllInterfaceAddresses;
    }

    ApplianceDiscoveryService(ScheduledExecutorService control, ScheduledExecutorService workers, Probe probe,
            NetworkAddressService networkAddressService, Supplier<Collection<CidrAddress>> interfaceAddresses) {
        super(control, Set.of(THING_TYPE_APPLIANCE), SCAN_TIMEOUT_SECONDS, false, null, null);
        this.control = control;
        this.workers = workers;
        this.probe = probe;
        this.networkAddressService = networkAddressService;
        this.interfaceAddresses = interfaceAddresses;
    }

    @Activate
    @Override
    protected void activate(@Nullable Map<String, Object> properties) {
        execute(() -> {
            active = true;
            configure(properties);
        });
    }

    @Modified
    @Override
    protected void modified(@Nullable Map<String, Object> properties) {
        execute(() -> {
            abortScanInternal();
            configure(properties);
        });
    }

    private void configure(@Nullable Map<String, Object> properties) {
        configuredSubnet = properties == null ? null : properties.get("discoverySubnet");
    }

    private List<InetAddress> scanAddresses() {
        Object subnet = configuredSubnet;
        if (subnet == null || "".equals(subnet)) {
            String primaryAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (primaryAddress == null) {
                return List.of();
            }
            // Resolve afresh for each scan, but retain a stable address snapshot throughout the scan.
            List<CidrAddress> matches = interfaceAddresses.get().stream()
                    .filter(address -> address.getAddress() instanceof Inet4Address
                            && address.getAddress().getHostAddress().equals(primaryAddress))
                    .distinct().toList();
            if (matches.size() != 1) {
                return List.of();
            }
            subnet = matches.getFirst().toString();
        }
        try {
            if (!(subnet instanceof String cidr)) {
                throw new IllegalArgumentException("Discovery subnet must be text");
            }
            return DiscoverySubnet.addresses(cidr);
        } catch (IllegalArgumentException e) {
            logger.debug("Samsung appliance discovery skipped: {}. Configure a private IPv4 discoverySubnet",
                    e.getMessage());
            return List.of();
        }
    }

    @Override
    public void startScan(@Nullable ScanListener listener) {
        execute(() -> {
            if (active) {
                abortScanInternal();
                synchronized (this) {
                    scanListener = listener;
                }
                startScan();
            }
        });
    }

    @Override
    public void startScan(String input, @Nullable ScanListener listener) {
        startScan(listener);
    }

    @Override
    protected void startScan() {
        List<InetAddress> addresses = scanAddresses();
        if (addresses.isEmpty()) {
            logger.debug(
                    "No suitable discovery subnet; configure a private IPv4 discoverySubnet before scanning for Samsung appliances");
            super.stopScan();
            return;
        }
        Scan current = new Scan(addresses);
        scan = current;
        // Own the timeout so completion, cancellation and disposal release every scheduled task.
        current.tasks.add(control.schedule(() -> execute(() -> {
            if (current.equals(scan)) {
                stopScanInternal();
            }
        }), getScanTimeout(), TimeUnit.SECONDS));
        for (int worker = 0; worker < CONCURRENCY; worker++) {
            int offset = worker;
            current.tasks.add(workers.submit(() -> probeHosts(current, offset)));
        }
    }

    private void probeHosts(Scan current, int offset) {
        boolean acquired = false;
        try {
            // Retain the bound even if a cancelled worker is still unwinding during a rescan.
            acquired = slots.tryAcquire(SCAN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                return;
            }
            for (int index = offset; index < current.addresses.size(); index += CONCURRENCY) {
                if (current.cancelled || Thread.currentThread().isInterrupted()
                        || System.nanoTime() >= current.deadline) {
                    return;
                }
                InetAddress host = current.addresses.get(index);
                try {
                    current.results.put(host.getHostAddress(), probe.discover(host, HOST_TIMEOUT_SECONDS));
                } catch (IOException e) {
                    // Unreachable hosts and unsupported public advertisements are normal scan outcomes.
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.warn("Unexpected Samsung appliance discovery failure", e);
            current.failed = true;
        } finally {
            if (acquired) {
                slots.release();
            }
            if (current.remaining.decrementAndGet() == 0 && !current.cancelled) {
                execute(() -> finish(current));
            }
        }
    }

    private void finish(Scan current) {
        if (!active || !current.equals(scan) || current.cancelled) {
            return;
        }
        if (!current.failed && System.nanoTime() < current.deadline) {
            Map<String, DiscoveryResult> unique = new HashMap<>();
            Set<String> conflicts = new HashSet<>();
            current.results.forEach((host, descriptor) -> {
                if (unique.putIfAbsent(descriptor.deviceId(), result(host, descriptor)) != null) {
                    conflicts.add(descriptor.deviceId());
                }
            });
            // Do not choose arbitrarily when distinct addresses advertise the same identity.
            conflicts.forEach(unique::remove);
            unique.values().forEach(this::thingDiscovered);
        }
        stopScanInternal();
    }

    static DiscoveryResult result(String host, Discovery.Descriptor descriptor) {
        return DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_APPLIANCE, descriptor.deviceId()))
                .withLabel(descriptor.name()).withProperty("host", host).withProperty("port", descriptor.securePort())
                .withProperty("deviceId", descriptor.deviceId()).withRepresentationProperty("deviceId").build();
    }

    @Override
    protected void stopScan() {
        execute(this::stopScanInternal);
    }

    private void stopScanInternal() {
        cancelWorkers();
        super.stopScan();
    }

    private void execute(Runnable action) {
        try {
            control.execute(action);
        } catch (RejectedExecutionException e) {
            // Deactivation has shut down this service's controller.
        }
    }

    private void cancelWorkers() {
        Scan current = scan;
        scan = null;
        if (current != null) {
            current.cancelled = true;
            current.tasks.forEach(task -> task.cancel(true));
        }
    }

    @Override
    public void abortScan() {
        execute(this::abortScanInternal);
    }

    private void abortScanInternal() {
        cancelWorkers();
        super.abortScan();
    }

    @Deactivate
    @Override
    protected void deactivate() {
        execute(() -> {
            active = false;
            try {
                abortScanInternal();
            } finally {
                control.shutdown();
            }
        });
    }

    private static final class Scan {
        final List<InetAddress> addresses;
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(SCAN_TIMEOUT_SECONDS);
        final List<Future<?>> tasks = new ArrayList<>();
        final Map<String, Discovery.Descriptor> results = new ConcurrentHashMap<>();
        final AtomicInteger remaining = new AtomicInteger(CONCURRENCY);
        volatile boolean cancelled;
        volatile boolean failed;

        Scan(List<InetAddress> addresses) {
            this.addresses = addresses;
        }
    }
}
