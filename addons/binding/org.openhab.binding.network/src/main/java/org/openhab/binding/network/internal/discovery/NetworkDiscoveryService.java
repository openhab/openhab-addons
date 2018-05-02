/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.discovery;

import static org.openhab.binding.network.NetworkBindingConstants.*;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.network.internal.NetworkBindingConfiguration;
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetectionListener;
import org.openhab.binding.network.internal.PresenceDetectionValue;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link NetworkDiscoveryService} is responsible for discovering devices on
 * the current Network. It uses every Network Interface which is connected to a network.
 * It tries common TCP ports to connect to, ICMP pings and ARP pings.
 *
 * @author David Graeff - Rewritten
 * @author Marc Mettke - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.network")
public class NetworkDiscoveryService extends AbstractDiscoveryService implements PresenceDetectionListener {
    static final int PING_TIMEOUT_IN_MS = 500;
    static final int MAXIMUM_IPS_PER_INTERFACE = 255;
    private static final long DISCOVERY_RESULT_TTL = TimeUnit.MINUTES.toSeconds(10);
    private final Logger logger = LoggerFactory.getLogger(NetworkDiscoveryService.class);

    // TCP port 548 (Apple Filing Protocol (AFP))
    // TCP port 554 (Windows share / Linux samba)
    // TCP port 1025 (Xbox / MS-RPC)
    private Set<Integer> tcp_service_ports = Sets.newHashSet(80, 548, 554, 1025);
    private Integer scannedIPcount;
    private ExecutorService executorService = null;
    private final NetworkBindingConfiguration configuration = new NetworkBindingConfiguration();
    NetworkUtils networkUtils = new NetworkUtils();

    public NetworkDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, (int) Math.round(
                new NetworkUtils().getNetworkIPs(MAXIMUM_IPS_PER_INTERFACE).size() * (PING_TIMEOUT_IN_MS / 1000.0)),
                false);
    }

    @Override
    @Activate
    public void activate(Map<String, Object> config) {
        super.activate(config);
        modified(config);
    };

    @Override
    @Modified
    protected void modified(Map<String, Object> config) {
        super.modified(config);
        // We update instead of replace the configuration object, so that if the user updates the
        // configuration, the values are automatically available in all handlers. Because they all
        // share the same instance.
        configuration.update(new Configuration(config).as(NetworkBindingConfiguration.class));
    }

    @Override
    @Deactivate
    protected void deactivate() {
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
        if (executorService != null) {
            return;
        }
        removeOlderResults(getTimestampOfLastScan(), null);
        logger.trace("Starting Network Device Discovery");

        final Set<String> networkIPs = networkUtils.getNetworkIPs(MAXIMUM_IPS_PER_INTERFACE);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        scannedIPcount = 0;

        for (String ip : networkIPs) {
            final PresenceDetection s = new PresenceDetection(this, 2000);
            try {
                s.setHostname(ip);
            } catch (UnknownHostException unknownHostException) {
                logger.trace("Skip IP that cannot be converted to a InetAddress", unknownHostException);
                continue;
            }
            s.setIOSDevice(true);
            s.setUseDhcpSniffing(false);
            s.setTimeout(PING_TIMEOUT_IN_MS);
            // Ping devices
            s.setUseIcmpPing(true);
            s.setUseArpPing(true, configuration.arpPingToolPath);
            // TCP devices
            s.setServicePorts(tcp_service_ports);

            executorService.execute(() -> {
                Thread.currentThread().setName("Discovery thread " + ip);
                s.performPresenceDetection(true);
                synchronized (scannedIPcount) {
                    scannedIPcount += 1;
                    if (scannedIPcount == networkIPs.size()) {
                        logger.trace("Scan of {} IPs successful", scannedIPcount);
                        stopScan();
                    }
                }
            });
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        if (executorService == null) {
            return;
        }

        try {
            executorService.awaitTermination(PING_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
        }
        executorService.shutdown();
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
        thingDiscovered(DiscoveryResultBuilder.create(createPingUID(ip)).withTTL(120).withProperties(properties)
                .withLabel("Network Device (" + ip + ")").build());
    }
}
