/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.dhcp.DHCPListenService;
import org.openhab.binding.network.internal.dhcp.IPRequestReceivedCallback;
import org.openhab.binding.network.internal.toberemoved.cache.ExpiringCacheAsync;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.openhab.binding.network.internal.utils.NetworkUtils.ArpPingUtilEnum;
import org.openhab.binding.network.internal.utils.NetworkUtils.IpPingMethodEnum;
import org.openhab.binding.network.internal.utils.PingResult;
import org.openhab.core.cache.ExpiringCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PresenceDetection} handles the connection to the Device
 *
 * @author Marc Mettke - Initial contribution
 * @author David Gräff, 2017 - Rewritten
 * @author Jan N. Klug - refactored host name resolution
 */
@NonNullByDefault
public class PresenceDetection implements IPRequestReceivedCallback {

    public static final double NOT_REACHABLE = -1;
    public static final int DESTINATION_TTL = 300 * 1000; // in ms, 300 s

    NetworkUtils networkUtils = new NetworkUtils();
    private final Logger logger = LoggerFactory.getLogger(PresenceDetection.class);

    /// Configuration variables
    private boolean useDHCPsniffing = false;
    private String arpPingState = "Disabled";
    private String ipPingState = "Disabled";
    protected String arpPingUtilPath = "";
    protected ArpPingUtilEnum arpPingMethod = ArpPingUtilEnum.UNKNOWN_TOOL;
    protected @Nullable IpPingMethodEnum pingMethod = null;
    private boolean iosDevice;
    private Set<Integer> tcpPorts = new HashSet<>();

    private long refreshIntervalInMS = 60000;
    private int timeoutInMS = 5000;
    private long lastSeenInMS;

    private @NonNullByDefault({}) String hostname;
    private @NonNullByDefault({}) ExpiringCache<@Nullable InetAddress> destination;
    private @Nullable InetAddress cachedDestination = null;

    public boolean preferResponseTimeAsLatency;

    /// State variables (cannot be final because of test dependency injections)
    ExpiringCacheAsync<PresenceDetectionValue> cache;
    private final PresenceDetectionListener updateListener;
    private @Nullable ScheduledFuture<?> refreshJob;
    protected @Nullable ExecutorService executorService;
    private String dhcpState = "off";
    Integer currentCheck = 0;
    int detectionChecks;

    public PresenceDetection(final PresenceDetectionListener updateListener, int cacheDeviceStateTimeInMS)
            throws IllegalArgumentException {
        this.updateListener = updateListener;
        cache = new ExpiringCacheAsync<>(cacheDeviceStateTimeInMS, () -> {
            performPresenceDetection(false);
        });
    }

    public @Nullable String getHostname() {
        return hostname;
    }

    public Set<Integer> getServicePorts() {
        return tcpPorts;
    }

    public long getRefreshInterval() {
        return refreshIntervalInMS;
    }

    public int getTimeout() {
        return timeoutInMS;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        this.destination = new ExpiringCache<>(DESTINATION_TTL, () -> {
            try {
                InetAddress destinationAddress = InetAddress.getByName(hostname);
                if (!destinationAddress.equals(cachedDestination)) {
                    logger.trace("host name resolved to other address, (re-)setup presence detection");
                    setUseArpPing(true, destinationAddress);
                    if (useDHCPsniffing) {
                        if (cachedDestination != null) {
                            disableDHCPListen(cachedDestination);
                        }
                        enableDHCPListen(destinationAddress);
                    }
                    cachedDestination = destinationAddress;
                }
                return destinationAddress;
            } catch (UnknownHostException e) {
                logger.trace("hostname resolution failed");
                if (cachedDestination != null) {
                    disableDHCPListen(cachedDestination);
                    cachedDestination = null;
                }
                return null;
            }
        });
    }

    public void setServicePorts(Set<Integer> ports) {
        this.tcpPorts = ports;
    }

    public void setUseDhcpSniffing(boolean enable) {
        this.useDHCPsniffing = enable;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshIntervalInMS = refreshInterval;
    }

    public void setTimeout(int timeout) {
        this.timeoutInMS = timeout;
    }

    public void setPreferResponseTimeAsLatency(boolean preferResponseTimeAsLatency) {
        this.preferResponseTimeAsLatency = preferResponseTimeAsLatency;
    }

    /**
     * Sets the ping method. This method will perform a feature test. If SYSTEM_PING
     * does not work on this system, JAVA_PING will be used instead.
     *
     * @param useSystemPing Set to true to use a system ping method, false to use java ping and null to disable ICMP
     *            pings.
     */
    public void setUseIcmpPing(@Nullable Boolean useSystemPing) {
        if (useSystemPing == null) {
            ipPingState = "Disabled";
            pingMethod = null;
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
     * Enables or disables ARP pings. Will be automatically disabled if the destination
     * is not an IPv4 address. If the feature test for the native arping utility fails,
     * it will be disabled as well.
     *
     * @param enable Enable or disable ARP ping
     * @param arpPingUtilPath c
     */
    private void setUseArpPing(boolean enable, @Nullable InetAddress destinationAddress) {
        if (!enable || arpPingUtilPath.isEmpty()) {
            arpPingState = "Disabled";
            arpPingMethod = ArpPingUtilEnum.UNKNOWN_TOOL;
            return;
        } else if (destinationAddress == null || !(destinationAddress instanceof Inet4Address)) {
            arpPingState = "Destination is not a valid IPv4 address";
            arpPingMethod = ArpPingUtilEnum.UNKNOWN_TOOL;
            return;
        }

        switch (arpPingMethod) {
            case UNKNOWN_TOOL: {
                arpPingState = "Unknown arping tool";
                break;
            }
            case THOMAS_HABERT_ARPING: {
                arpPingState = "Arping tool by Thomas Habets";
                break;
            }
            case THOMAS_HABERT_ARPING_WITHOUT_TIMEOUT: {
                arpPingState = "Arping tool by Thomas Habets (old version)";
                break;
            }
            case ELI_FULKERSON_ARP_PING_FOR_WINDOWS: {
                arpPingState = "Eli Fulkerson ARPing tool for Windows";
                break;
            }
            case IPUTILS_ARPING: {
                arpPingState = "Ipuitls Arping";
                break;
            }
        }
    }

    /**
     * sets the path to arp ping
     *
     * @param enable Enable or disable ARP ping
     * @param arpPingUtilPath enableDHCPListen(useDHCPsniffing);
     */
    public void setUseArpPing(boolean enable, String arpPingUtilPath, ArpPingUtilEnum arpPingUtilMethod) {
        setUseArpPing(enable, destination.getValue());
        this.arpPingUtilPath = arpPingUtilPath;
        this.arpPingMethod = arpPingUtilMethod;
    }

    public String getArpPingState() {
        return arpPingState;
    }

    public String getIPPingState() {
        return ipPingState;
    }

    public String getDhcpState() {
        return dhcpState;
    }

    /**
     * Return true if the device presence detection is performed for an iOS device
     * like iPhone or iPads. An additional port knock is performed before a ping.
     */
    public boolean isIOSdevice() {
        return iosDevice;
    }

    /**
     * Set to true if the device presence detection should be performed for an iOS device
     * like iPhone or iPads. An additional port knock is performed before a ping.
     */
    public void setIOSDevice(boolean value) {
        iosDevice = value;
    }

    /**
     * Return the last seen value in milliseconds based on {@link System.currentTimeMillis()} or 0 if not seen yet.
     */
    public long getLastSeen() {
        return lastSeenInMS;
    }

    /**
     * Return asynchronously the value of the presence detection as a PresenceDetectionValue.
     *
     * @param callback A callback with the PresenceDetectionValue. The callback may
     *            not happen immediately if the cached value expired, but as soon as a new
     *            discovery took place.
     */
    public void getValue(Consumer<PresenceDetectionValue> callback) {
        cache.getValue(callback);
    }

    public ExecutorService getThreadsFor(int threadCount) {
        return Executors.newFixedThreadPool(threadCount);
    }

    /**
     * Perform a presence detection with ICMP-, ARP ping and
     * TCP connection attempts simultaneously. A fixed thread pool will be created with as many
     * thread as necessary to perform all tests at once.
     *
     * This is a NO-OP, if there is already an ongoing detection or if the cached value
     * is not expired yet.
     *
     * Please be aware of the following restrictions:
     * - ARP pings are only executed on IPv4 addresses.
     * - Non system / Java pings are not recommended at all
     * (not interruptible, useless TCP echo service fall back)
     *
     * @param waitForDetectionToFinish If you want to synchronously wait for the result, set this to true
     * @return Return true if a presence detection is performed and false otherwise.
     */
    public boolean performPresenceDetection(boolean waitForDetectionToFinish) {
        if (executorService != null) {
            logger.debug(
                    "There is already an ongoing presence discovery for {} and a new one was issued by the scheduler! TCP Port {}",
                    hostname, tcpPorts);
            return false;
        }

        if (!cache.isExpired()) {
            return false;
        }

        Set<String> interfaceNames = null;

        currentCheck = 0;
        detectionChecks = tcpPorts.size();
        if (pingMethod != null) {
            detectionChecks += 1;
        }
        if (arpPingMethod != ArpPingUtilEnum.UNKNOWN_TOOL) {
            interfaceNames = networkUtils.getInterfaceNames();
            detectionChecks += interfaceNames.size();
        }

        if (detectionChecks == 0) {
            return false;
        }

        final ExecutorService executorService = getThreadsFor(detectionChecks);
        this.executorService = executorService;

        for (Integer tcpPort : tcpPorts) {
            executorService.execute(() -> {
                Thread.currentThread().setName("presenceDetectionTCP_" + hostname + " " + String.valueOf(tcpPort));
                performServicePing(tcpPort);
                checkIfFinished();
            });
        }

        // ARP ping for IPv4 addresses. Use single executor for Windows tool and
        // each own executor for each network interface for other tools
        if (arpPingMethod == ArpPingUtilEnum.ELI_FULKERSON_ARP_PING_FOR_WINDOWS) {
            executorService.execute(() -> {
                Thread.currentThread().setName("presenceDetectionARP_" + hostname + " ");
                // arp-ping.exe tool capable of handling multiple interfaces by itself
                performARPping("");
                checkIfFinished();
            });
        } else if (interfaceNames != null) {
            for (final String interfaceName : interfaceNames) {
                executorService.execute(() -> {
                    Thread.currentThread().setName("presenceDetectionARP_" + hostname + " " + interfaceName);
                    performARPping(interfaceName);
                    checkIfFinished();
                });
            }
        }

        // ICMP ping
        if (pingMethod != null) {
            executorService.execute(() -> {
                if (pingMethod != IpPingMethodEnum.JAVA_PING) {
                    Thread.currentThread().setName("presenceDetectionICMP_" + hostname);
                    performSystemPing();
                } else {
                    performJavaPing();
                }
                checkIfFinished();
            });
        }

        if (waitForDetectionToFinish) {
            waitForPresenceDetection();
        }

        return true;
    }

    /**
     * Calls updateListener.finalDetectionResult() with a final result value.
     * Safe to be called from different threads. After a call to this method,
     * the presence detection process is finished and all threads are forcefully
     * shut down.
     */
    private synchronized void submitFinalResult() {
        // Do nothing if we are not in a detection process
        ExecutorService service = executorService;
        if (service == null) {
            return;
        }
        // Finish the detection process
        service.shutdownNow();
        executorService = null;
        detectionChecks = 0;

        PresenceDetectionValue v;

        // The cache will be expired by now if cache_time < timeoutInMS. But the device might be actually reachable.
        // Therefore use lastSeenInMS here and not cache.isExpired() to determine if we got a ping response.
        if (lastSeenInMS + timeoutInMS + 100 < System.currentTimeMillis()) {
            // We haven't seen the device in the detection process
            v = new PresenceDetectionValue(hostname, -1);
        } else {
            // Make the cache valid again and submit the value.
            v = cache.getExpiredValue();
        }
        cache.setValue(v);

        if (!v.isReachable()) {
            // if target can't be reached, check if name resolution need to be updated
            destination.invalidateValue();
        }
        updateListener.finalDetectionResult(v);
    }

    /**
     * This method is called after each individual check and increases a check counter.
     * If the counter equals the total checks,the final result is submitted. This will
     * happen way before the "timeoutInMS", if all checks were successful.
     * Thread safe.
     */
    private synchronized void checkIfFinished() {
        currentCheck += 1;
        if (currentCheck < detectionChecks) {
            return;
        }
        submitFinalResult();
    }

    /**
     * Waits for the presence detection threads to finish. Returns immediately
     * if no presence detection is performed right now.
     */
    public void waitForPresenceDetection() {
        ExecutorService service = executorService;
        if (service == null) {
            return;
        }
        try {
            // We may get interrupted here by cancelRefreshJob().
            service.awaitTermination(timeoutInMS + 100, TimeUnit.MILLISECONDS);
            submitFinalResult();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
            service.shutdownNow();
            executorService = null;
        }
    }

    /**
     * If the cached PresenceDetectionValue has not expired yet, the cached version
     * is returned otherwise a new reachable PresenceDetectionValue is created with
     * a latency of 0.
     *
     * It is safe to call this method from multiple threads. The returned PresenceDetectionValue
     * might be still be altered in other threads though.
     *
     * @param type The detection type
     * @return The non expired or a new instance of PresenceDetectionValue.
     */
    synchronized PresenceDetectionValue updateReachableValue(PresenceDetectionType type, double latency) {
        lastSeenInMS = System.currentTimeMillis();
        PresenceDetectionValue v;
        if (cache.isExpired()) {
            v = new PresenceDetectionValue(hostname, 0);
        } else {
            v = cache.getExpiredValue();
        }
        v.updateLatency(latency);
        v.addType(type);
        cache.setValue(v);
        return v;
    }

    protected void performServicePing(int tcpPort) {
        logger.trace("Perform TCP presence detection for {} on port: {}", hostname, tcpPort);
        try {
            InetAddress destinationAddress = destination.getValue();
            if (destinationAddress != null) {
                networkUtils.servicePing(destinationAddress.getHostAddress(), tcpPort, timeoutInMS).ifPresent(o -> {
                    if (o.isSuccess()) {
                        PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.TCP_CONNECTION,
                                getLatency(o, preferResponseTimeAsLatency));
                        v.addReachableTcpService(tcpPort);
                        updateListener.partialDetectionResult(v);
                    }
                });
            }
        } catch (IOException e) {
            // This should not happen and might be a user configuration issue, we log a warning message therefore.
            logger.warn("Could not create a socket connection", e);
        }
    }

    /**
     * Performs an "ARP ping" (ARP request) on the given interface.
     * If it is an iOS device, the {@see NetworkUtils.wakeUpIOS()} method is
     * called before performing the ARP ping.
     *
     * @param interfaceName The interface name. You can request a list of interface names
     *            from {@see NetworkUtils.getInterfaceNames()} for example.
     */
    protected void performARPping(String interfaceName) {
        try {
            logger.trace("Perform ARP ping presence detection for {} on interface: {}", hostname, interfaceName);
            InetAddress destinationAddress = destination.getValue();
            if (destinationAddress == null) {
                return;
            }
            if (iosDevice) {
                networkUtils.wakeUpIOS(destinationAddress);
                Thread.sleep(50);
            }

            networkUtils.nativeARPPing(arpPingMethod, arpPingUtilPath, interfaceName,
                    destinationAddress.getHostAddress(), timeoutInMS).ifPresent(o -> {
                        if (o.isSuccess()) {
                            PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.ARP_PING,
                                    getLatency(o, preferResponseTimeAsLatency));
                            updateListener.partialDetectionResult(v);
                        }
                    });
        } catch (IOException e) {
            logger.trace("Failed to execute an arp ping for ip {}", hostname, e);
        } catch (InterruptedException ignored) {
            // This can be ignored, the thread will end anyway
        }
    }

    /**
     * Performs a java ping. It is not recommended to use this, as it is not interruptible,
     * and will not work on windows systems reliably and will fall back from ICMP pings to
     * the TCP echo service on port 7 which barely no device or server supports nowadays.
     * (http://docs.oracle.com/javase/7/docs/api/java/net/InetAddress.html#isReachable%28int%29)
     */
    protected void performJavaPing() {
        logger.trace("Perform java ping presence detection for {}", hostname);

        InetAddress destinationAddress = destination.getValue();
        if (destinationAddress == null) {
            return;
        }

        networkUtils.javaPing(timeoutInMS, destinationAddress).ifPresent(o -> {
            if (o.isSuccess()) {
                PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.ICMP_PING,
                        getLatency(o, preferResponseTimeAsLatency));
                updateListener.partialDetectionResult(v);
            }
        });
    }

    protected void performSystemPing() {
        try {
            logger.trace("Perform native ping presence detection for {}", hostname);
            InetAddress destinationAddress = destination.getValue();
            if (destinationAddress == null) {
                return;
            }

            networkUtils.nativePing(pingMethod, destinationAddress.getHostAddress(), timeoutInMS).ifPresent(o -> {
                if (o.isSuccess()) {
                    PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.ICMP_PING,
                            getLatency(o, preferResponseTimeAsLatency));
                    updateListener.partialDetectionResult(v);
                }
            });
        } catch (IOException e) {
            logger.trace("Failed to execute a native ping for ip {}", hostname, e);
        } catch (InterruptedException e) {
            // This can be ignored, the thread will end anyway
        }
    }

    private double getLatency(PingResult pingResult, boolean preferResponseTimeAsLatency) {
        logger.debug("Getting latency from ping result {} using latency mode {}", pingResult,
                preferResponseTimeAsLatency);
        // Execution time is always set and this value is also the default. So lets use it first.
        double latency = pingResult.getExecutionTimeInMS();

        if (preferResponseTimeAsLatency && pingResult.getResponseTimeInMS().isPresent()) {
            latency = pingResult.getResponseTimeInMS().get();
        }

        return latency;
    }

    @Override
    public void dhcpRequestReceived(String ipAddress) {
        PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.DHCP_REQUEST, 0);
        updateListener.partialDetectionResult(v);
    }

    /**
     * Start/Restart a fixed scheduled runner to update the devices reach-ability state.
     *
     * @param scheduledExecutorService A scheduler to run pings periodically.
     */
    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService) {
        ScheduledFuture<?> future = refreshJob;
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        refreshJob = scheduledExecutorService.scheduleWithFixedDelay(() -> performPresenceDetection(true), 0,
                refreshIntervalInMS, TimeUnit.MILLISECONDS);
    }

    /**
     * Return true if automatic refreshing is enabled.
     */
    public boolean isAutomaticRefreshing() {
        return refreshJob != null;
    }

    /**
     * Stop automatic refreshing.
     */
    public void stopAutomaticRefresh() {
        ScheduledFuture<?> future = refreshJob;
        if (future != null && !future.isDone()) {
            future.cancel(true);
            refreshJob = null;
        }
        if (cachedDestination != null) {
            disableDHCPListen(cachedDestination);
        }
    }

    /**
     * Enables listing for dhcp packets to figure out if devices have entered the network. This does not work
     * for iOS devices. The hostname of this network service object will be registered to the dhcp request packet
     * listener if enabled and unregistered otherwise.
     *
     * @param destinationAddress the InetAddress to listen for.
     */
    private void enableDHCPListen(InetAddress destinationAddress) {
        try {
            if (DHCPListenService.register(destinationAddress.getHostAddress(), this).isUseUnprevilegedPort()) {
                dhcpState = "No access right for port 67. Bound to port 6767 instead. Port forwarding necessary!";
            } else {
                dhcpState = "Running normally";
            }
        } catch (SocketException e) {
            logger.warn("Cannot use DHCP sniffing.", e);
            useDHCPsniffing = false;
            dhcpState = "Cannot use DHCP sniffing: " + e.getLocalizedMessage();
        }
    }

    private void disableDHCPListen(@Nullable InetAddress destinationAddress) {
        if (destinationAddress != null) {
            DHCPListenService.unregister(destinationAddress.getHostAddress());
            dhcpState = "off";
        }
    }
}
