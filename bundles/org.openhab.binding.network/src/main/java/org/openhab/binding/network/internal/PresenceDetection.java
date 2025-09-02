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
package org.openhab.binding.network.internal;

import static org.openhab.binding.network.internal.PresenceDetectionType.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.dhcp.DHCPListenService;
import org.openhab.binding.network.internal.dhcp.DHCPPacketListenerServer;
import org.openhab.binding.network.internal.dhcp.IPRequestReceivedCallback;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.openhab.binding.network.internal.utils.NetworkUtils.ArpPingUtilEnum;
import org.openhab.binding.network.internal.utils.NetworkUtils.IpPingMethodEnum;
import org.openhab.binding.network.internal.utils.PingResult;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.cache.ExpiringCacheAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PresenceDetection} handles the connection to the Device. This class is not thread-safe.
 *
 * @author Marc Mettke - Initial contribution
 * @author David Gräff, 2017 - Rewritten
 * @author Jan N. Klug - refactored host name resolution
 * @author Wouter Born - Reuse ExpiringCacheAsync from Core
 */
@NonNullByDefault
public class PresenceDetection implements IPRequestReceivedCallback {

    private static final Duration DESTINATION_TTL = Duration.ofMinutes(5);

    NetworkUtils networkUtils = new NetworkUtils();
    private final Logger logger = LoggerFactory.getLogger(PresenceDetection.class);

    /// Configuration variables
    private boolean useDHCPsniffing = false;
    private String ipPingState = "Disabled";
    protected String arpPingUtilPath = "";
    private ArpPingUtilEnum arpPingMethod = ArpPingUtilEnum.DISABLED;
    protected IpPingMethodEnum pingMethod = IpPingMethodEnum.DISABLED;
    private boolean iosDevice;
    private boolean useArpPing;
    private boolean useIcmpPing;
    private Set<Integer> tcpPorts = new HashSet<>();

    private Duration timeout = Duration.ofSeconds(5);

    /** All access must be guarded by "this" */
    private @Nullable Instant lastSeen;

    private @NonNullByDefault({}) String hostname;
    private @NonNullByDefault({}) ExpiringCache<@Nullable InetAddress> destination;
    private @Nullable InetAddress cachedDestination;

    private boolean preferResponseTimeAsLatency;

    // State variables (cannot be final because of test dependency injections)
    ExpiringCacheAsync<PresenceDetectionValue> cache;

    private final PresenceDetectionListener updateListener;

    private Set<String> networkInterfaceNames = Set.of();
    private String dhcpState = "off";
    int detectionChecks;
    private String lastReachableNetworkInterfaceName = "";

    private final Executor executor;

    public PresenceDetection(final PresenceDetectionListener updateListener, Duration cacheDeviceStateTime,
            Executor executor) {
        this.updateListener = updateListener;
        this.executor = executor;
        cache = new ExpiringCacheAsync<>(cacheDeviceStateTime);
    }

    public @Nullable String getHostname() {
        return hostname;
    }

    public Set<Integer> getServicePorts() {
        return tcpPorts;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        this.destination = new ExpiringCache<>(DESTINATION_TTL, () -> {
            try {
                InetAddress destinationAddress = InetAddress.getByName(hostname);
                InetAddress cached = cachedDestination;
                if (!destinationAddress.equals(cached)) {
                    logger.trace("Hostname {} resolved to other address {}, (re-)setup presence detection", hostname,
                            destinationAddress);
                    setUseArpPing(true, destinationAddress);
                    if (useDHCPsniffing) {
                        if (cached != null) {
                            disableDHCPListen(cached);
                        }
                        enableDHCPListen(destinationAddress);
                    }
                    cachedDestination = destinationAddress;
                }
                return destinationAddress;
            } catch (UnknownHostException e) {
                logger.trace("Hostname resolution for {} failed", hostname);
                InetAddress cached = cachedDestination;
                if (cached != null) {
                    disableDHCPListen(cached);
                    cachedDestination = null;
                }
                return null;
            }
        });
    }

    public void setNetworkInterfaceNames(Set<String> networkInterfaceNames) {
        this.networkInterfaceNames = networkInterfaceNames;
    }

    public void setServicePorts(Set<Integer> ports) {
        this.tcpPorts = ports;
    }

    public void setUseDhcpSniffing(boolean enable) {
        this.useDHCPsniffing = enable;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public void setPreferResponseTimeAsLatency(boolean preferResponseTimeAsLatency) {
        this.preferResponseTimeAsLatency = preferResponseTimeAsLatency;
    }

    /**
     * Sets the ping method. This method will perform a feature test. If {@link IpPingMethodEnum#SYSTEM_PING}
     * does not work on this system, {@link IpPingMethodEnum#JAVA_PING} will be used instead.
     *
     * @param useSystemPing Set to <code>true</code> to use a system ping method, <code>false</code> to use Java ping
     *            and <code>null</code> to disable ICMP pings.
     */
    public void setUseIcmpPing(@Nullable Boolean useSystemPing) {
        if (useSystemPing == null) {
            ipPingState = "Disabled";
            pingMethod = IpPingMethodEnum.DISABLED;
        } else if (useSystemPing) {
            final IpPingMethodEnum pingMethod = networkUtils.determinePingMethod();
            this.pingMethod = pingMethod;
            ipPingState = pingMethod == IpPingMethodEnum.JAVA_PING ? "System ping feature test failed. Using Java ping"
                    : pingMethod.name();
        } else {
            pingMethod = IpPingMethodEnum.JAVA_PING;
            ipPingState = "Java ping";
        }
    }

    /**
     * Sets the ping method directly. The exact ping method must be known, or things will fail.
     * Use {@link NetworkUtils#determinePingMethod()} to find a supported method.
     *
     * @param pingMethod the {@link IpPingMethodEnum}.
     */
    public void setIcmpPingMethod(IpPingMethodEnum pingMethod) {
        this.pingMethod = pingMethod;
        switch (pingMethod) {
            case DISABLED:
                ipPingState = "Disabled";
                break;
            case JAVA_PING:
                ipPingState = "Java ping";
                break;
            default:
                ipPingState = pingMethod.name();
                break;
        }
    }

    /**
     * Enables or disables ARP pings. Will be automatically disabled if the destination
     * is not an IPv4 address. If the feature test for the native arping utility fails,
     * it will be disabled as well.
     *
     * @param enable Enable or disable ARP ping
     * @param destinationAddress target ip address
     */
    private void setUseArpPing(boolean enable, @Nullable InetAddress destinationAddress) {
        if (!enable || arpPingUtilPath.isEmpty()) {
            arpPingMethod = ArpPingUtilEnum.DISABLED;
        } else if (!(destinationAddress instanceof Inet4Address)) {
            arpPingMethod = ArpPingUtilEnum.DISABLED_INVALID_IP;
        }
    }

    /**
     * Sets the path to ARP ping.
     *
     * @param enable enable or disable ARP ping
     * @param arpPingUtilPath path to Arping tool
     * @param arpPingUtilMethod Arping tool method
     */
    public void setUseArpPing(boolean enable, String arpPingUtilPath, ArpPingUtilEnum arpPingUtilMethod) {
        if (!enable) {
            arpPingMethod = ArpPingUtilEnum.DISABLED;
        } else {
            setUseArpPing(enable, destination.getValue());
            this.arpPingUtilPath = arpPingUtilPath;
            this.arpPingMethod = arpPingUtilMethod;
        }
    }

    public String getArpPingState() {
        return arpPingMethod.description;
    }

    public String getIPPingState() {
        return ipPingState;
    }

    public String getDhcpState() {
        return dhcpState;
    }

    /**
     * Return <code>true</code> if the device presence detection is performed for an iOS device
     * like iPhone or iPads. An additional port knock is performed before a ping.
     */
    public boolean isIOSdevice() {
        return iosDevice;
    }

    /**
     * Set to <code>true</code> if the device presence detection should be performed for an iOS device
     * like iPhone or iPads. An additional port knock is performed before a ping.
     */
    public void setIOSDevice(boolean value) {
        iosDevice = value;
    }

    /**
     * Return <code>true</code> if the device presence detection is also performed using arp ping. This gives
     * less accurate ping latency results when used for an IPv4 destination host.
     */
    public boolean isUseArpPing() {
        return useArpPing;
    }

    /**
     * Set to <code>true</code> if the device presence detection should also be performed using arp ping. This gives
     * less accurate ping latency results when used for an IPv4 destination host.
     */
    public void setUseArpPing(boolean useArpPing) {
        this.useArpPing = useArpPing;
    }

    /**
     * Return <code>true</code> if the device presence detection is also performed using icmp ping.
     */
    public boolean isUseIcmpPing() {
        return useIcmpPing;
    }

    /**
     * Set to <code>true</code> if the device presence detection should also be performed using icmp ping.
     */
    public void setUseIcmPing(boolean useIcmpPing) {
        this.useIcmpPing = useIcmpPing;
    }

    /**
     * Return the last seen value as an {@link Instant} or <code>null</code> if not yet seen.
     */
    public @Nullable synchronized Instant getLastSeen() {
        return lastSeen;
    }

    /**
     * Gets the presence detection value synchronously as a {@link PresenceDetectionValue}.
     * <p>
     * The value is only updated if the cached value has not expired.
     */
    public PresenceDetectionValue getValue() throws InterruptedException, ExecutionException {
        return cache.getValue(this::performPresenceDetection).get();
    }

    /**
     * Gets the presence detection value asynchronously as a {@link PresenceDetectionValue}.
     * <p>
     * The value is only updated if the cached value has not expired.
     *
     * @param callback a callback with the {@link PresenceDetectionValue}. The callback may
     *            not happen immediately if the cached value expired, but as soon as a new
     *            discovery took place.
     */
    public void getValue(Consumer<PresenceDetectionValue> callback) {
        cache.getValue(this::performPresenceDetection).thenAccept(callback);
    }

    private void withDestinationAddress(Consumer<InetAddress> consumer) {
        InetAddress destinationAddress = destination.getValue();
        if (destinationAddress == null) {
            logger.trace("The destinationAddress for {} is null", hostname);
        } else {
            consumer.accept(destinationAddress);
        }
    }

    /**
     * Perform a presence detection with ICMP-, ARP ping and TCP connection attempts simultaneously.
     *
     * Please be aware of the following restrictions:
     * <ul>
     * <li>ARP pings are only executed on IPv4 addresses.
     * <li>Non system / Java pings are not recommended at all (not interruptible, useless TCP echo service fall back)
     * </ul>
     *
     * @return a {@link CompletableFuture} for obtaining the {@link PresenceDetectionValue}
     */
    public CompletableFuture<PresenceDetectionValue> performPresenceDetection() {
        Set<String> interfaceNames = null;

        detectionChecks = tcpPorts.size();
        if (pingMethod != IpPingMethodEnum.DISABLED) {
            detectionChecks += 1;
        }
        if (arpPingMethod.canProceed) {
            if (!lastReachableNetworkInterfaceName.isEmpty()) {
                interfaceNames = Set.of(lastReachableNetworkInterfaceName);
            } else if (!networkInterfaceNames.isEmpty()) {
                interfaceNames = networkInterfaceNames;
            } else {
                interfaceNames = networkUtils.getInterfaceNames();
            }
            detectionChecks += interfaceNames.size();
        }

        logger.trace("Performing {} presence detection checks for {}", detectionChecks, hostname);

        PresenceDetectionValue pdv = new PresenceDetectionValue(hostname, PresenceDetectionValue.UNREACHABLE);

        if (detectionChecks == 0) {
            return CompletableFuture.completedFuture(pdv);
        }

        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();

        for (Integer tcpPort : tcpPorts) {
            addAsyncDetection(completableFutures, () -> {
                performServicePing(pdv, tcpPort);
            });
        }

        // ARP ping for IPv4 addresses. Use single executor for Windows tool and
        // each own executor for each network interface for other tools
        if (arpPingMethod == ArpPingUtilEnum.ELI_FULKERSON_ARP_PING_FOR_WINDOWS) {
            addAsyncDetection(completableFutures, () -> {
                performArpPing(pdv, "");
            });
        } else if (interfaceNames != null) {
            for (final String interfaceName : interfaceNames) {
                addAsyncDetection(completableFutures, () -> {
                    performArpPing(pdv, interfaceName);
                });
            }
        }

        // ICMP ping
        if (pingMethod != IpPingMethodEnum.DISABLED) {
            addAsyncDetection(completableFutures, () -> {
                if (pingMethod == IpPingMethodEnum.JAVA_PING) {
                    performJavaPing(pdv);
                } else {
                    performSystemPing(pdv);
                }
            });
        }

        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Waiting for {} detection futures for {} to complete", completableFutures.size(), hostname);
            try {
                CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new)).join();
                logger.debug("All {} detection futures for {} have completed", completableFutures.size(), hostname);
            } catch (CancellationException e) {
                logger.debug("Detection future for {} was cancelled", hostname);
            } catch (CompletionException e) {
                if (e.getCause() instanceof TimeoutException) {
                    logger.debug("Detection future for {} timed out", hostname);
                } else {
                    logger.debug("Detection future failed to complete {}", e.getMessage());
                    logger.trace("", e);
                }
            }

            if (!pdv.isReachable()) {
                logger.trace("{} is unreachable, invalidating cached destination value", hostname);
                destination.invalidateValue();
            }

            logger.debug("Sending listener final result: {}", pdv);
            updateListener.finalDetectionResult(pdv);

            return pdv;
        }, executor);
    }

    private void addAsyncDetection(List<CompletableFuture<Void>> completableFutures, Runnable detectionRunnable) {
        completableFutures.add(CompletableFuture.runAsync(detectionRunnable, executor));
    }

    /**
     * Creates a new {@link PresenceDetectionValue} when a host is reachable. Also updates the {@link #lastSeen}
     * value and sends a partial detection result to the {@link #updateListener}.
     * <p>
     * It is safe to call this method from multiple threads.
     *
     * @param type the detection type
     * @param latency the latency
     */
    PresenceDetectionValue updateReachable(PresenceDetectionType type, Duration latency) {
        PresenceDetectionValue pdv = new PresenceDetectionValue(hostname, latency);
        updateReachable(pdv, type, latency);
        return pdv;
    }

    /**
     * Updates the given {@link PresenceDetectionValue} when a host is reachable. Also updates the {@link #lastSeen}
     * value and sends a partial detection result to the {@link #updateListener}.
     * <p>
     * It is safe to call this method from multiple threads.
     *
     * @param pdv the {@link PresenceDetectionValue} to update
     * @param type the detection type
     * @param latency the latency
     */
    void updateReachable(PresenceDetectionValue pdv, PresenceDetectionType type, Duration latency) {
        updateReachable(pdv, type, latency, -1);
    }

    void updateReachable(PresenceDetectionValue pdv, PresenceDetectionType type, Duration latency, int tcpPort) {
        Instant now = Instant.now();
        pdv.addReachableDetectionType(type);
        pdv.updateLatency(latency);
        if (0 <= tcpPort) {
            pdv.addReachableTcpPort(tcpPort);
        }
        logger.debug("Sending listener partial result: {}", pdv);
        synchronized (this) {
            lastSeen = now;
            updateListener.partialDetectionResult(pdv);
        }
    }

    protected void performServicePing(PresenceDetectionValue pdv, int tcpPort) {
        logger.trace("Perform TCP presence detection for {} on port: {}", hostname, tcpPort);

        withDestinationAddress(destinationAddress -> {
            try {
                PingResult pingResult = networkUtils.servicePing(destinationAddress.getHostAddress(), tcpPort, timeout);
                if (pingResult.isSuccess()) {
                    updateReachable(pdv, TCP_CONNECTION, getLatency(pingResult), tcpPort);
                }
            } catch (IOException e) {
                // This should not happen and might be a user configuration issue, we log a warning message therefore.
                logger.warn("Could not create a socket connection", e);
            }
        });
    }

    /**
     * Performs an "ARP ping" (ARP request) on the given interface.
     * If it is an iOS device, the {@link NetworkUtils#wakeUpIOS(InetAddress)} method is
     * called before performing the ARP ping.
     *
     * @param pdv the {@link PresenceDetectionValue} to update
     * @param interfaceName the interface name. You can request a list of interface names
     *            from {@link NetworkUtils#getInterfaceNames()} for example.
     */
    protected void performArpPing(PresenceDetectionValue pdv, String interfaceName) {
        logger.trace("Perform ARP ping presence detection for {} on interface: {}", hostname, interfaceName);

        withDestinationAddress(destinationAddress -> {
            Runnable arpPingTask = () -> {
                try {
                    PingResult pingResult = networkUtils.nativeArpPing(arpPingMethod, arpPingUtilPath, interfaceName,
                            destinationAddress.getHostAddress(), timeout);
                    if (pingResult != null) {
                        if (pingResult.isSuccess()) {
                            updateReachable(pdv, ARP_PING, getLatency(pingResult));
                            lastReachableNetworkInterfaceName = interfaceName;
                        } else if (lastReachableNetworkInterfaceName.equals(interfaceName)) {
                            logger.trace("{} is no longer reachable on network interface: {}", hostname, interfaceName);
                            lastReachableNetworkInterfaceName = "";
                        }
                    }
                } catch (IOException e) {
                    logger.trace("Failed to execute an ARP ping for {}", hostname, e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            };

            if (iosDevice) {
                CompletableFuture.runAsync(() -> {
                    try {
                        networkUtils.wakeUpIOS(destinationAddress);
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (IOException e) {
                        logger.trace("Failed to wake up iOS device for {}", hostname, e);
                    }
                }, executor).thenRunAsync(arpPingTask, executor);
            } else {
                arpPingTask.run();
            }
        });
    }

    /**
     * Performs a Java ping. It is not recommended to use this, as it is not interruptible,
     * and will not work on Windows systems reliably and will fall back from ICMP pings to
     * the TCP echo service on port 7 which barely no device or server supports nowadays.
     *
     * @see InetAddress#isReachable(int)
     */
    protected void performJavaPing(PresenceDetectionValue pdv) {
        logger.trace("Perform Java ping presence detection for {}", hostname);

        withDestinationAddress(destinationAddress -> {
            PingResult pingResult = networkUtils.javaPing(timeout, destinationAddress);
            if (pingResult.isSuccess()) {
                updateReachable(pdv, ICMP_PING, getLatency(pingResult));
            }
        });
    }

    protected void performSystemPing(PresenceDetectionValue pdv) {
        logger.trace("Perform native ping presence detection for {}", hostname);

        withDestinationAddress(destinationAddress -> {
            try {
                PingResult pingResult = networkUtils.nativePing(pingMethod, destinationAddress.getHostAddress(),
                        timeout);
                if (pingResult != null && pingResult.isSuccess()) {
                    updateReachable(pdv, ICMP_PING, getLatency(pingResult));
                }
            } catch (IOException e) {
                logger.trace("Failed to execute a native ping for {}", hostname, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        });
    }

    private Duration getLatency(PingResult pingResult) {
        logger.trace("Getting latency from ping result {} using latency mode {}", pingResult,
                preferResponseTimeAsLatency);
        Duration executionTime = pingResult.getExecutionTime();
        Duration responseTime = pingResult.getResponseTime();
        return preferResponseTimeAsLatency && responseTime != null ? responseTime : executionTime;
    }

    @Override
    public void dhcpRequestReceived(String ipAddress) {
        updateReachable(DHCP_REQUEST, Duration.ZERO);
    }

    public void refresh() {
        try {
            logger.debug("Refreshing {} reachability state", hostname);
            getValue();
        } catch (ExecutionException e) {
            logger.debug("Failed to refresh {} presence detection", hostname, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }

    public void dispose() {
        InetAddress cached = cachedDestination;
        if (cached != null) {
            disableDHCPListen(cached);
        }
    }

    /**
     * Enables listening for DHCP packets to figure out if devices have entered the network. This does not work
     * for iOS devices. The hostname of this network service object will be registered to the DHCP request packet
     * listener if enabled and unregistered otherwise.
     *
     * @param destinationAddress the {@link InetAddress} to listen for.
     */
    private void enableDHCPListen(InetAddress destinationAddress) {
        try {
            DHCPPacketListenerServer listener = DHCPListenService.register(destinationAddress.getHostAddress(), this);
            dhcpState = String.format("Bound to port %d - %s", listener.getCurrentPort(),
                    (listener.usingPrivilegedPort() ? "Running normally" : "Port forwarding necessary!"));
        } catch (SocketException e) {
            dhcpState = String.format("Cannot use DHCP sniffing: %s", e.getMessage());
            logger.warn("{}", dhcpState);
            useDHCPsniffing = false;
        }
    }

    private void disableDHCPListen(InetAddress destinationAddress) {
        DHCPListenService.unregister(destinationAddress.getHostAddress());
        dhcpState = "off";
    }
}
