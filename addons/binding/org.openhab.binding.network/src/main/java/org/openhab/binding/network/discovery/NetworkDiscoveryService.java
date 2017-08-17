/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.discovery;

import static org.openhab.binding.network.NetworkBindingConstants.*;

import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetection.PingMethod;
import org.openhab.binding.network.internal.PresenceDetectionListener;
import org.openhab.binding.network.internal.PresenceDetectionValue;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.osgi.service.component.ComponentContext;
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
public class NetworkDiscoveryService extends AbstractDiscoveryService implements PresenceDetectionListener {
    static final int PING_TIMEOUT_IN_MS = 500;
    private final Logger logger = LoggerFactory.getLogger(NetworkDiscoveryService.class);

    // TCP port 548 (Apple Filing Protocol (AFP))
    // TCP port 554 (Windows share / Linux samba)
    // TCP port 1025 (XBox / MS-RPC)
    private Set<Integer> tcp_service_ports = Sets.newHashSet(80, 548, 554, 1025);
    private Integer scannedIPcount;
    private ExecutorService executorService = null;
    private boolean canUseARPPing;
    private String arpPingToolPath;
    NetworkUtils networkUtils = new NetworkUtils();

    public NetworkDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS,
                (int) Math.round(new NetworkUtils().getNetworkIPs().size() * (PING_TIMEOUT_IN_MS / 1000.0)), false);
    }

    protected void activate(ComponentContext componentContext) {
        Dictionary<String, Object> properties = componentContext.getProperties();
        arpPingToolPath = (String) properties.get("arp_ping_tool_path");
        if (arpPingToolPath == null) {
            arpPingToolPath = "arping";
        }
        canUseARPPing = networkUtils.isNativeARPpingWorking(arpPingToolPath);
    };

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
        logger.trace("Starting Discovery");

        final Set<String> networkIPs = networkUtils.getNetworkIPs();
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
            s.setDHCPsniffing(false);
            s.setTimeout(PING_TIMEOUT_IN_MS);
            // Ping devices
            s.setPingMethod(PingMethod.SYSTEM_PING);
            s.setUseARPping(canUseARPPing, arpPingToolPath);
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

    static ThingUID createServiceUID(String ip, int tcpPort) {
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
        logger.trace("Found service device at {} with port", ip, tcpPort);

        String label;
        // TCP port 548 (Apple Filing Protocol (AFP))
        // TCP port 554 (Windows share / Linux samba)
        // TCP port 1025 (XBox / MS-RPC)
        switch (tcpPort) {
            case 80:
                label = "Device with webserver";
                break;
            case 548:
                label = "Apple Device";
                break;
            case 554:
                label = "Windows compatible device";
                break;
            case 1025:
                label = "Xbox compatible device";
                break;
            default:
                label = "Computer/Laptop";
        }
        label += "(" + ip + ")";

        Map<String, Object> properties = new HashMap<>();
        properties.put(PARAMETER_HOSTNAME, ip);
        properties.put(PARAMETER_PORT, tcpPort);
        thingDiscovered(DiscoveryResultBuilder.create(createServiceUID(ip, tcpPort)).withTTL(120)
                .withProperties(properties).withLabel(label).build());
    }

    static ThingUID createPingUID(String ip) {
        // uid must not contains dots
        return new ThingUID(PING_DEVICE, ip.replace('.', '_'));
    }

    /**
     * Submit newly discovered devices. This method is called by the spawned threads in {@link startScan}.
     *
     * @param ip The device IP
     */
    public void newPingDevice(String ip) {
        logger.trace("Found service device at {}", ip);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PARAMETER_HOSTNAME, ip);
        thingDiscovered(DiscoveryResultBuilder.create(createPingUID(ip)).withTTL(120).withProperties(properties)
                .withLabel("Network Device (" + ip + ")").build());
    }
}
