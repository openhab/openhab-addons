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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.ApplianceFamily;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.ocf.Discovery;
import org.openhab.core.common.NamedThreadFactory;
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
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "binding.smartthings")
public class ApplianceDiscoveryService extends AbstractDiscoveryService {
    static final int DEFAULT_CONCURRENCY = 16;
    static final int MAX_CONCURRENCY = 32;
    static final int HOST_TIMEOUT_SECONDS = 3;
    static final int SCAN_TIMEOUT_SECONDS = 210;
    private static final int DEACTIVATION_TIMEOUT_SECONDS = 5;

    @FunctionalInterface
    interface Probe {
        Discovery.Descriptor discover(InetAddress host, int timeoutSeconds) throws IOException;
    }

    private final Logger logger = LoggerFactory.getLogger(ApplianceDiscoveryService.class);
    private final ScheduledExecutorService control;
    private final ThreadPoolExecutor workers;
    private final boolean ownsWorkers;
    private final Object workerGate = new Object();
    private final Probe probe;
    private final NetworkAddressService networkAddressService;
    private final Supplier<Collection<CidrAddress>> interfaceAddresses;
    private int concurrency = DEFAULT_CONCURRENCY;
    private int activeWorkers;

    private static Set<org.openhab.core.thing.ThingTypeUID> discoveryThingTypes() {
        Set<org.openhab.core.thing.ThingTypeUID> thingTypes = new HashSet<>();
        for (ApplianceFamily family : ApplianceFamily.values()) {
            thingTypes.add(family.thingTypeUID());
        }
        thingTypes.add(SmartThingsBindingConstants.THING_TYPE_APPLIANCE);
        return Set.copyOf(thingTypes);
    }

    // Lifecycle transitions and publication are serialized; network requests never occupy this executor.
    private volatile boolean active;
    private @Nullable Object configuredSubnet;
    private boolean discoverUnclassified;
    private volatile @Nullable Scan scan;

    @Activate
    public ApplianceDiscoveryService(@Reference NetworkAddressService networkAddressService) {
        super(discoveryThingTypes(), SCAN_TIMEOUT_SECONDS, false);
        control = ThreadPoolManager.getPoolBasedSequentialScheduledExecutorService("smartthings-discovery-control",
                "smartthings-discovery-control");
        workers = new ThreadPoolExecutor(MAX_CONCURRENCY, MAX_CONCURRENCY, 65, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(MAX_CONCURRENCY),
                new NamedThreadFactory("smartthings-discovery-probes", true));
        workers.allowCoreThreadTimeOut(true);
        ownsWorkers = true;
        probe = Discovery::discoverSamsung;
        this.networkAddressService = networkAddressService;
        interfaceAddresses = NetUtil::getAllInterfaceAddresses;
    }

    ApplianceDiscoveryService(ScheduledExecutorService control, ScheduledExecutorService workers, Probe probe,
            NetworkAddressService networkAddressService, Supplier<Collection<CidrAddress>> interfaceAddresses) {
        super(control, discoveryThingTypes(), SCAN_TIMEOUT_SECONDS, false, null, null);
        this.control = control;
        this.workers = (ThreadPoolExecutor) workers;
        ownsWorkers = false;
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
        discoverUnclassified = properties != null && Boolean.TRUE.equals(properties.get("discoverUnclassified"));
        Object configuredConcurrency = properties == null ? null : properties.get("discoveryConcurrency");
        concurrency = parseConcurrency(configuredConcurrency);
        synchronized (workerGate) {
            workerGate.notifyAll();
        }
    }

    private int parseConcurrency(@Nullable Object value) {
        if (value == null) {
            return DEFAULT_CONCURRENCY;
        }
        try {
            int configured = Integer.parseInt(value.toString());
            if (configured >= 1 && configured <= MAX_CONCURRENCY) {
                return configured;
            }
        } catch (NumberFormatException e) {
            logger.warn("Ignoring invalid SmartThings discovery concurrency {}; using default {}", value,
                    DEFAULT_CONCURRENCY);
            return DEFAULT_CONCURRENCY;
        }
        logger.warn("Ignoring out-of-range SmartThings discovery concurrency {}; using default {}", value,
                DEFAULT_CONCURRENCY);
        return DEFAULT_CONCURRENCY;
    }

    private List<InetAddress> scanAddresses() {
        Object subnet = configuredSubnet;
        if (subnet == null || "".equals(subnet)) {
            String primaryAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (primaryAddress == null) {
                logger.debug("Samsung appliance discovery skipped: no primary IPv4 address is available");
                return List.of();
            }
            // Resolve afresh for each scan, but retain a stable address snapshot throughout the scan.
            List<CidrAddress> matches = interfaceAddresses.get().stream()
                    .filter(address -> address.getAddress() instanceof Inet4Address
                            && address.getAddress().getHostAddress().equals(primaryAddress))
                    .distinct().toList();
            if (matches.size() != 1) {
                logger.debug(
                        "Samsung appliance discovery skipped: primary IPv4 address {} matches {} interface subnets",
                        primaryAddress, matches.size());
                return List.of();
            }
            subnet = matches.getFirst().toString();
            logger.debug("Samsung appliance discovery using primary IPv4 subnet {}", subnet);
        }
        try {
            if (!(subnet instanceof String cidr)) {
                throw new IllegalArgumentException("Discovery subnet must be text");
            }
            List<InetAddress> addresses = DiscoverySubnet.addresses(cidr);
            logger.debug("Samsung appliance discovery subnet {} contains {} scan addresses", cidr, addresses.size());
            return addresses;
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
        int scanTimeout = getScanTimeout();
        Scan current = new Scan(addresses, concurrency, scanTimeout);
        scan = current;
        logger.debug("Starting Samsung appliance discovery: {} addresses, {} workers, {} seconds per host",
                addresses.size(), current.concurrency, HOST_TIMEOUT_SECONDS);
        // Own the timeout so completion, cancellation and disposal release every scheduled task.
        current.addTask(control.schedule(() -> execute(() -> {
            if (current.equals(scan)) {
                logger.debug("Samsung appliance discovery timed out; discarding incomplete scan results");
                stopScanInternal();
            }
        }), scanTimeout, TimeUnit.SECONDS));
        for (int worker = 0; worker < current.concurrency; worker++) {
            scheduleWorker(current, worker);
        }
    }

    private void scheduleWorker(Scan current, int offset) {
        long delayNanos = TimeUnit.SECONDS.toNanos(HOST_TIMEOUT_SECONDS) * offset / current.concurrency;
        if (delayNanos == 0) {
            submitWorker(current, offset);
        } else {
            current.addTask(control.schedule(() -> submitWorker(current, offset), delayNanos, TimeUnit.NANOSECONDS));
        }
    }

    private void submitWorker(Scan current, int offset) {
        if (current.cancelled || !current.equals(scan)) {
            return;
        }
        try {
            current.addTask(workers.submit(() -> probeHosts(current, offset)));
        } catch (RejectedExecutionException e) {
            logger.warn("Unable to schedule Samsung appliance discovery probe", e);
            current.failed = true;
            if (current.remaining.decrementAndGet() == 0) {
                execute(() -> finish(current));
            }
        }
    }

    private boolean acquireWorker(Scan current) throws InterruptedException {
        synchronized (workerGate) {
            while (!current.cancelled && !Thread.currentThread().isInterrupted() && activeWorkers >= current.concurrency
                    && System.nanoTime() < current.deadline) {
                long remaining = current.deadline - System.nanoTime();
                if (remaining <= 0) {
                    return false;
                }
                TimeUnit.NANOSECONDS.timedWait(workerGate, remaining);
            }
            if (current.cancelled || Thread.currentThread().isInterrupted() || System.nanoTime() >= current.deadline) {
                return false;
            }
            activeWorkers++;
            return true;
        }
    }

    private void probeHosts(Scan current, int offset) {
        boolean acquired = false;
        try {
            acquired = acquireWorker(current);
            if (!acquired) {
                return;
            }
            for (int index = offset; index < current.addresses.size(); index += current.concurrency) {
                if (current.cancelled || Thread.currentThread().isInterrupted()
                        || System.nanoTime() >= current.deadline) {
                    return;
                }
                InetAddress host = current.addresses.get(index);
                try {
                    logger.debug("Probing Samsung appliance discovery advertisements at {}", host.getHostAddress());
                    Discovery.Descriptor descriptor = probe.discover(host, HOST_TIMEOUT_SECONDS);
                    current.results.put(host.getHostAddress(), descriptor);
                    logger.debug("Discovered Samsung appliance {} at {} with secure port {}", descriptor.deviceId(),
                            host.getHostAddress(), descriptor.securePort());
                } catch (IOException e) {
                    // Unreachable hosts and unsupported public advertisements are normal scan outcomes.
                    logger.debug("No usable Samsung appliance advertisement at {} ({})", host.getHostAddress(),
                            e.getClass().getSimpleName());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.warn("Unexpected Samsung appliance discovery failure", e);
            current.failed = true;
        } finally {
            if (acquired) {
                synchronized (workerGate) {
                    activeWorkers--;
                    workerGate.notifyAll();
                }
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
                DiscoveryResult discoveryResult = result(host, descriptor, discoverUnclassified);
                if (discoveryResult != null && unique.putIfAbsent(descriptor.deviceId(), discoveryResult) != null) {
                    conflicts.add(descriptor.deviceId());
                }
            });
            // Do not choose arbitrarily when distinct addresses advertise the same identity.
            conflicts.forEach(identity -> {
                logger.debug("Ignoring Samsung appliance {} advertised by multiple addresses", identity);
                unique.remove(identity);
            });
            logger.debug(
                    "Samsung appliance discovery completed: {} advertisements, {} conflicting identities, {} results",
                    current.results.size(), conflicts.size(), unique.size());
            unique.values().forEach(this::thingDiscovered);
        } else {
            logger.debug("Samsung appliance discovery failed or exceeded its deadline; discarding incomplete results");
        }
        stopScanInternal();
    }

    static @Nullable DiscoveryResult result(String host, Discovery.Descriptor descriptor,
            boolean discoverUnclassified) {
        ApplianceFamily family = ApplianceFamily.forDeviceTypes(descriptor.properties().getOrDefault("deviceType", ""));
        if (family == null && !discoverUnclassified) {
            return null;
        }
        var thingTypeUID = family == null ? SmartThingsBindingConstants.THING_TYPE_APPLIANCE : family.thingTypeUID();
        DiscoveryResultBuilder builder = DiscoveryResultBuilder
                .create(new ThingUID(thingTypeUID, descriptor.deviceId())).withLabel(descriptor.name())
                .withProperty("host", host).withProperty("port", descriptor.securePort())
                .withProperty("deviceId", descriptor.deviceId()).withRepresentationProperty("deviceId");
        descriptor.properties().forEach(builder::withProperty);
        return builder.build();
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
            logger.debug("Stopping Samsung appliance discovery workers; {} advertisements collected",
                    current.results.size());
            current.cancelTasks();
            workers.purge();
            synchronized (workerGate) {
                workerGate.notifyAll();
            }
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
        active = false;
        cancelWorkers();
        if (ownsWorkers) {
            workers.shutdownNow();
        }
        if (control.isShutdown()) {
            return;
        }
        try {
            control.submit(this::abortScanInternal).get(DEACTIVATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | RejectedExecutionException | TimeoutException e) {
            logger.warn("Unable to complete Samsung discovery cleanup during deactivation", e);
        } finally {
            shutdownExecutors();
        }
    }

    private void shutdownExecutors() {
        control.shutdownNow();
        if (ownsWorkers) {
            workers.shutdownNow();
        }
    }

    private static final class Scan {
        final List<InetAddress> addresses;
        final int concurrency;
        final long deadline;
        final List<Future<?>> tasks = new ArrayList<>();
        final Map<String, Discovery.Descriptor> results = new ConcurrentHashMap<>();
        final AtomicInteger remaining;
        volatile boolean cancelled;
        volatile boolean failed;

        Scan(List<InetAddress> addresses, int concurrency, int timeoutSeconds) {
            this.addresses = addresses;
            this.concurrency = concurrency;
            deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
            remaining = new AtomicInteger(concurrency);
        }

        synchronized void addTask(Future<?> task) {
            if (cancelled) {
                task.cancel(true);
            } else {
                tasks.add(task);
            }
        }

        synchronized void cancelTasks() {
            cancelled = true;
            tasks.forEach(task -> task.cancel(true));
        }
    }
}
