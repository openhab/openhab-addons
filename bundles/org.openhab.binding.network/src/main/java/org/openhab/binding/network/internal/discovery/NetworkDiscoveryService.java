/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.network.internal.NetworkBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.NetworkBindingConfiguration;
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetectionListener;
import org.openhab.binding.network.internal.PresenceDetectionValue;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
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
    static final int PING_TIMEOUT_IN_MS = 500;
    static final int MAXIMUM_IPS_PER_INTERFACE = 255;
    private static final long DISCOVERY_RESULT_TTL = TimeUnit.MINUTES.toSeconds(10);
    private final Logger logger = LoggerFactory.getLogger(NetworkDiscoveryService.class);

    // TCP port 548 (Apple Filing Protocol (AFP))
    // TCP port 554 (Windows share / Linux samba)
    // TCP port 1025 (Xbox / MS-RPC)
    private Set<Integer> tcpServicePorts = Collections
            .unmodifiableSet(Stream.of(80, 548, 554, 1025).collect(Collectors.toSet()));
    private AtomicInteger scannedIPcount = new AtomicInteger(0);
    private @Nullable ExecutorService executorService = null;
    private final NetworkBindingConfiguration configuration = new NetworkBindingConfiguration();
    private final NetworkUtils networkUtils = new NetworkUtils();

    public NetworkDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, (int) Math.round(
                new NetworkUtils().getNetworkIPs(MAXIMUM_IPS_PER_INTERFACE).size() * (PING_TIMEOUT_IN_MS / 1000.0)),
                false);
    }

    @Override
    @Activate
    public void activate(@Nullable Map<String, Object> config) {
        super.activate(config);
        modified(config);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, Object> config) {
        super.modified(config);
        // We update instead of replace the configuration object, so that if the user updates the
        // configuration, the values are automatically available in all handlers. Because they all
        // share the same instance.
        configuration.update(new Configuration(config).as(NetworkBindingConfiguration.class));
    }

    @Override
    @Deactivate
    protected void deactivate() {
        if (executorService != null) {
            executorService.shutdown();
        }
        super.deactivate();
    }

    @Override
    public void partialDetectionResult(PresenceDetectionValue value) {
        final String ip = value.getHostAddress();
        if (value.isPingReachable()) {
            newPingDevice(ip);
        } else if (value.isTCPServiceReachable()) {
            List<Integer> tcpServices = value.getReachableTCPports();
            for (int port : tcpServices) {
                newServiceDevice(ip, port);
            }
        }
    }

    @Override
    public void finalDetectionResult(PresenceDetectionValue value) {
    }

    /**
     * Starts the DiscoveryThread for each IP on each interface on the network
     */
    @Override
    protected void startScan() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        }
        final ExecutorService service = executorService;
        if (service == null) {
            return;
        }
        removeOlderResults(getTimestampOfLastScan(), null);
        logger.trace("Starting Network Device Discovery");

        final Set<String> networkIPs = networkUtils.getNetworkIPs(MAXIMUM_IPS_PER_INTERFACE);
        scannedIPcount.set(0);

        for (String ip : networkIPs) {
            final PresenceDetection s = new PresenceDetection(this, 2000);
            s.setHostname(ip);
            s.setIOSDevice(true);
            s.setUseDhcpSniffing(false);
            s.setTimeout(PING_TIMEOUT_IN_MS);
            // Ping devices
            s.setUseIcmpPing(true);
            s.setUseArpPing(true, configuration.arpPingToolPath, configuration.arpPingUtilMethod);
            // TCP devices
            s.setServicePorts(tcpServicePorts);

            service.execute(() -> {
                Thread.currentThread().setName("Discovery thread " + ip);
                s.performPresenceDetection(true);
                int count = scannedIPcount.incrementAndGet();
                if (count == networkIPs.size()) {
                    logger.trace("Scan of {} IPs successful", scannedIPcount);
                    stopScan();
                }
            });
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        final ExecutorService service = executorService;
        if (service == null) {
            return;
        }

        try {
            service.awaitTermination(PING_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
        }
        service.shutdown();
        executorService = null;
    }

    public static ThingUID createServiceUID(String ip, int tcpPort) {
        // uid must not contains dots
        return new ThingUID(SERVICE_DEVICE, ip.replace('.', '_') + "_" + String.valueOf(tcpPort));
    }

    /**
     * Submit newly discovered devices. This method is called by the spawned threads in {@link startScan}.
     *
     * @param ip The device IP
     * @param tcpPort The TCP port
     */
    public void newServiceDevice(String ip, int tcpPort) {
        logger.trace("Found reachable service for device with IP address {} on port {}", ip, tcpPort);

        String label;
        // TCP port 548 (Apple Filing Protocol (AFP))
        // TCP port 554 (Windows share / Linux samba)
        // TCP port 1025 (Xbox / MS-RPC)
        switch (tcpPort) {
            case 80:
                label = "Device providing a Webserver";
                break;
            case 548:
                label = "Device providing the Apple AFP Service";
                break;
            case 554:
                label = "Device providing Network/Samba Shares";
                break;
            case 1025:
                label = "Device providing Xbox/MS-RPC Capability";
                break;
            default:
                label = "Network Device";
        }
        label += " (" + ip + ":" + tcpPort + ")";

        Map<String, Object> properties = new HashMap<>();
        properties.put(PARAMETER_HOSTNAME, ip);
        properties.put(PARAMETER_PORT, tcpPort);
        thingDiscovered(DiscoveryResultBuilder.create(createServiceUID(ip, tcpPort)).withTTL(DISCOVERY_RESULT_TTL)
                .withProperties(properties).withLabel(label).build());
    }

    public static ThingUID createPingUID(String ip) {
        // uid must not contains dots
        return new ThingUID(PING_DEVICE, ip.replace('.', '_'));
    }

    /**
     * Submit newly discovered devices. This method is called by the spawned threads in {@link startScan}.
     *
     * @param ip The device IP
     */
    public void newPingDevice(String ip) {
        logger.trace("Found pingable network device with IP address {}", ip);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PARAMETER_HOSTNAME, ip);
        thingDiscovered(DiscoveryResultBuilder.create(createPingUID(ip)).withTTL(DISCOVERY_RESULT_TTL)
                .withProperties(properties).withLabel("Network Device (" + ip + ")").build());
    }
}
