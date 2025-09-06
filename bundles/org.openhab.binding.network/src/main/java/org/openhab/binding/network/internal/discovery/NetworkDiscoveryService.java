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
package org.openhab.binding.network.internal.discovery;

import static org.openhab.binding.network.internal.NetworkBindingConstants.BINDING_CONFIGURATION_PID;
import static org.openhab.binding.network.internal.NetworkBindingConstants.PARAMETER_HOSTNAME;
import static org.openhab.binding.network.internal.NetworkBindingConstants.PARAMETER_PORT;
import static org.openhab.binding.network.internal.NetworkBindingConstants.PING_DEVICE;
import static org.openhab.binding.network.internal.NetworkBindingConstants.SERVICE_DEVICE;
import static org.openhab.binding.network.internal.NetworkBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.network.internal.utils.NetworkUtils.durationToMillis;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.NetworkBindingConfiguration;
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetectionListener;
import org.openhab.binding.network.internal.PresenceDetectionValue;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.openhab.binding.network.internal.utils.NetworkUtils.IpPingMethodEnum;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkDiscoveryService} is responsible for discovering devices on
 * the current Network. It uses every Network Interface which is connected to a network.
 * It tries common TCP ports to connect to, ICMP pings and ARP pings.
 *
 * @author Marc Mettke - Initial contribution
 * @author David Graeff - Rewritten
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.network")
public class NetworkDiscoveryService extends AbstractDiscoveryService implements PresenceDetectionListener {
    static final Duration PING_TIMEOUT = Duration.ofMillis(500);
    static final int MAXIMUM_IPS_PER_INTERFACE = 254;
    private static final long DISCOVERY_RESULT_TTL = TimeUnit.MINUTES.toSeconds(10);
    private final Logger logger = LoggerFactory.getLogger(NetworkDiscoveryService.class);

    // TCP port 548 (Apple Filing Protocol (AFP))
    // TCP port 554 (Windows share / Linux samba)
    // TCP port 1025 (Xbox / MS-RPC)
    private Set<Integer> tcpServicePorts = Set.of(80, 548, 554, 1025);

    /* All access must be guarded by "this" */
    private @Nullable ExecutorService executorService;
    private final NetworkUtils networkUtils = new NetworkUtils();
    private final ConfigurationAdmin admin;

    @Activate
    public NetworkDiscoveryService(@Reference ConfigurationAdmin admin) {
        super(SUPPORTED_THING_TYPES_UIDS,
                (int) Math.round(new NetworkUtils().getNetworkIPs(MAXIMUM_IPS_PER_INTERFACE).size()
                        * (durationToMillis(PING_TIMEOUT) / 1000.0)),
                false);
        this.admin = admin;
    }

    @Override
    @Deactivate
    protected void deactivate() {
        synchronized (this) {
            if (executorService != null) {
                executorService.shutdownNow();
                executorService = null;
            }
        }
        super.deactivate();
    }

    @Override
    public void partialDetectionResult(PresenceDetectionValue value) {
        final String ip = value.getHostAddress();
        if (value.isPingReachable()) {
            newPingDevice(ip);
        } else if (value.isTcpServiceReachable()) {
            List<Integer> tcpServices = value.getReachableTcpPorts();
            for (int port : tcpServices) {
                newServiceDevice(ip, port);
            }
        }
    }

    @Override
    public void finalDetectionResult(PresenceDetectionValue value) {
    }

    private ExecutorService createDiscoveryExecutor(@Nullable NetworkBindingConfiguration configuration) {
        AtomicInteger count = new AtomicInteger(1);
        int numThreads = configuration == null ? NetworkBindingConfiguration.DEFAULT_DISCOVERY_THREADS
                : configuration.numberOfDiscoveryThreads;
        if (numThreads > 0) {
            return Executors.newFixedThreadPool(numThreads, r -> {
                Thread t = new Thread(r, "OH-binding-network-discoveryWorker-" + count.getAndIncrement());
                t.setDaemon(true);
                return t;
            });
        } else {
            return Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "OH-binding-network-discoveryWorker-" + count.getAndIncrement());
                t.setDaemon(true);
                return t;
            });
        }
    }

    /**
     * Starts the DiscoveryThread for each IP on each interface on the network
     */
    @Override
    protected void startScan() {
        NetworkBindingConfiguration configuration = getConfig();
        final ExecutorService service;
        synchronized (this) {
            if (executorService == null) {
                executorService = createDiscoveryExecutor(configuration);
            }
            service = executorService;
        }
        if (service == null) {
            return;
        }

        removeOlderResults(getTimestampOfLastScan(), null);
        logger.debug("Starting Network Device Discovery");

        Map<String, Set<CidrAddress>> discoveryList = networkUtils.getNetworkIPsPerInterface();

        // Track completion for all interfaces
        final int totalInterfaces = discoveryList.size();
        final AtomicInteger completedInterfaces = new AtomicInteger(0);

        service.execute(() -> {
            Thread.currentThread().setName("OH-binding-network-discoveryCoordinator");
            IpPingMethodEnum pingMethod = networkUtils.determinePingMethod();
            for (Entry<String, Set<CidrAddress>> discovery : discoveryList.entrySet()) {
                final String networkInterface = discovery.getKey();
                final Set<String> networkIPs = networkUtils.getNetworkIPs(discovery.getValue(),
                        MAXIMUM_IPS_PER_INTERFACE);
                logger.debug("Scanning {} IPs on interface {} ", networkIPs.size(), networkInterface);
                final AtomicInteger scannedIPcount = new AtomicInteger(0);
                final int targetCount = networkIPs.size();

                for (String ip : networkIPs) {
                    final PresenceDetection pd = new PresenceDetection(this, Duration.ofSeconds(2), service);
                    pd.setHostname(ip);
                    pd.setIOSDevice(true);
                    pd.setUseDhcpSniffing(false);
                    pd.setTimeout(PING_TIMEOUT);
                    // Ping devices
                    pd.setIcmpPingMethod(pingMethod);
                    if (configuration == null) {
                        pd.setUseArpPing(true, NetworkBindingConfiguration.DEFAULT_ARPING_TOOL_PATH,
                                NetworkBindingConfiguration.DEFAULT_ARPING_METHOD);
                    } else {
                        pd.setUseArpPing(true, configuration.arpPingToolPath, configuration.arpPingUtilMethod);
                    }
                    // TCP devices
                    pd.setServicePorts(tcpServicePorts);
                    pd.getValue((v) -> {
                        int count = scannedIPcount.incrementAndGet();
                        if (count >= targetCount) {
                            logger.debug("Scan of {} IPs on interface {} completed", scannedIPcount.get(),
                                    networkInterface);
                            // Only call stopScan after all interfaces are done
                            if (completedInterfaces.incrementAndGet() >= totalInterfaces) {
                                logger.debug("All network interface scans completed. Stopping scan.");
                                stopScan();
                                logger.debug("Finished Network Device Discovery");
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void stopScan() {
        final ExecutorService service;
        synchronized (this) {
            super.stopScan();
            service = executorService;
            if (service == null) {
                return;
            }
            executorService = null;
        }
        logger.debug("Stopping Network Device Discovery");

        service.shutdownNow(); // Initiate shutdown
        try {
            if (!service.awaitTermination(PING_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                logger.warn("Network discovery scan failed to stop within the timeout of {}", PING_TIMEOUT);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static ThingUID createServiceUID(String ip, int tcpPort) {
        // uid must not contains dots
        return new ThingUID(SERVICE_DEVICE, ip.replace('.', '_') + "_" + tcpPort);
    }

    /**
     * Submit newly discovered devices. This method is called by the spawned threads in {@link #startScan()}.
     *
     * @param ip The device IP
     * @param tcpPort The TCP port
     */
    public void newServiceDevice(String ip, int tcpPort) {
        logger.trace("Found reachable service for device with IP address {} on port {}", ip, tcpPort);

        // TCP port 548 (Apple Filing Protocol (AFP))
        // TCP port 554 (Windows share / Linux samba)
        // TCP port 1025 (Xbox / MS-RPC)
        String label = switch (tcpPort) {
            case 80 -> "Device providing a Webserver";
            case 548 -> "Device providing the Apple AFP Service";
            case 554 -> "Device providing Network/Samba Shares";
            case 1025 -> "Device providing Xbox/MS-RPC Capability";
            default -> "Network Device";
        };
        label += " (" + ip + ":" + tcpPort + ")";
        final String fLabel = label;

        // A thread that isn't part of the executor is needed, because registering new discoveries is slow,
        // and the executor is shut down when the scan is finished or aborted.
        new Thread(() -> {
            thingDiscovered(DiscoveryResultBuilder.create(createServiceUID(ip, tcpPort)).withTTL(DISCOVERY_RESULT_TTL)
                    .withProperty(PARAMETER_HOSTNAME, ip).withProperty(PARAMETER_PORT, tcpPort).withLabel(fLabel)
                    .build());
        }, "OH-binding-network-discoveryResultCourier").start();
    }

    public static ThingUID createPingUID(String ip) {
        // uid must not contains dots
        return new ThingUID(PING_DEVICE, ip.replace('.', '_'));
    }

    /**
     * Submit newly discovered devices. This method is called by the spawned threads in {@link #startScan()}.
     *
     * @param ip The device IP
     */
    public void newPingDevice(String ip) {
        logger.trace("Found pingable network device with IP address {}", ip);

        // A thread that isn't part of the executor is needed, because registering new discoveries is slow,
        // and the executor is shut down when the scan is finished or aborted.
        new Thread(() -> {
            thingDiscovered(DiscoveryResultBuilder.create(createPingUID(ip)).withTTL(DISCOVERY_RESULT_TTL)
                    .withProperty(PARAMETER_HOSTNAME, ip).withLabel("Network Device (" + ip + ")").build());
        }, "OH-binding-network-discoveryPingCourier").start();
    }

    private @Nullable NetworkBindingConfiguration getConfig() {
        ConfigurationAdmin admin = this.admin;
        try {
            Configuration configOnline = admin.getConfiguration(BINDING_CONFIGURATION_PID, null);
            if (configOnline != null) {
                Dictionary<String, Object> props = configOnline.getProperties();
                if (props != null) {
                    Map<String, Object> propMap = Collections.list(props.keys()).stream()
                            .collect(Collectors.toMap(Function.identity(), props::get));
                    return new org.openhab.core.config.core.Configuration(propMap)
                            .as(NetworkBindingConfiguration.class);
                }
            }
        } catch (IOException e) {
            logger.warn("Unable to read configuration: {}", e.getMessage());
        }
        return null;
    }
}
