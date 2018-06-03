/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.network.internal.dhcp.DHCPListenService;
import org.openhab.binding.network.internal.dhcp.IPRequestReceivedCallback;
import org.openhab.binding.network.internal.toberemoved.cache.ExpiringCacheAsync;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.openhab.binding.network.internal.utils.NetworkUtils.ArpPingUtilEnum;
import org.openhab.binding.network.internal.utils.NetworkUtils.IpPingMethodEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PresenceDetection} handles the connection to the Device
 *
 * @author David Gräff, 2017 - Rewritten
 * @author Marc Mettke - Initial contribution
 */
public class PresenceDetection implements IPRequestReceivedCallback {
    public static final double NOT_REACHABLE = -1;
    NetworkUtils networkUtils = new NetworkUtils();
    private Logger logger = LoggerFactory.getLogger(PresenceDetection.class);

    /// Configuration variables
    private boolean useDHCPsniffing = false;
    private ArpPingUtilEnum arpPingMethod = null;
    private String arpPingUtilPath = "arping";
    private IpPingMethodEnum pingMethod = null;
    private boolean iosDevice;
    private Set<Integer> tcpPorts = new HashSet<Integer>();

    private long refreshIntervalInMS = 60000;
    private int timeoutInMS = 5000;
    private long lastSeenInMS;

    private String hostname;

    /// State variables (cannot be final because of test dependency injections)
    ExpiringCacheAsync<PresenceDetectionValue> cache;
    private final PresenceDetectionListener updateListener;
    private ScheduledFuture<?> refreshJob;
    private InetAddress destination;
    ExecutorService executorService;
    private String dhcpState = "off";
    Integer currentCheck = 0;
    int detectionChecks;

    public PresenceDetection(final PresenceDetectionListener updateListener, int cacheDeviceStateTimeInMS)
            throws IllegalArgumentException {
        this.updateListener = updateListener;
        cache = new ExpiringCacheAsync<PresenceDetectionValue>(cacheDeviceStateTimeInMS, () -> {
            performPresenceDetection(false);
        });
    }

    public String getHostname() {
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

    public void setHostname(String hostname) throws UnknownHostException {
        this.hostname = hostname;
        this.destination = InetAddress.getByName(hostname);
        if (arpPingMethod != null) {
            if (destination instanceof Inet4Address) {
                setUseArpPing(true, arpPingUtilPath);
            } else {
                arpPingMethod = null;
            }
        }
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

    /**
     * Sets the ping method. This method will perform a feature test. If SYSTEM_PING
     * does not work on this system, JAVA_PING will be used instead.
     *
     * @param useSystemPing Set to true to use a system ping method, false to use java ping and null to disable ICMP
     *            pings.
     */
    public void setUseIcmpPing(Boolean useSystemPing) {
        if (useSystemPing == null) {
            pingMethod = null;
        } else if (useSystemPing) {
            pingMethod = networkUtils.determinePingMethod();
        } else {
            pingMethod = IpPingMethodEnum.JAVA_PING;
        }
    }

    /**
     * Enables or disables ARP pings. Will be automatically disabled if the destination
     * is not an IPv4 address. If the feature test for the native arping utility fails,
     * it will be disabled as well.
     *
     * @param enable Enable or disable ARP ping
     * @param arpPingUtilPath The file path to the utility
     */
    public void setUseArpPing(boolean enable, String arpPingUtilPath) {
        this.arpPingUtilPath = arpPingUtilPath;
        if (!enable || StringUtils.isBlank(arpPingUtilPath)) {
            arpPingMethod = null;
            return;
        } else if (destination == null || !(destination instanceof Inet4Address)) {
            arpPingMethod = null;
            return;
        }
        arpPingMethod = networkUtils.determineNativeARPpingMethod(arpPingUtilPath);
    }

    public ArpPingUtilEnum arpPingMethod() {
        return arpPingMethod;
    }

    public IpPingMethodEnum getPingMethod() {
        return pingMethod;
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
        if (arpPingMethod != null) {
            interfaceNames = networkUtils.getInterfaceNames();
            detectionChecks += interfaceNames.size();
        }

        if (detectionChecks == 0) {
            return false;
        }

        executorService = getThreadsFor(detectionChecks);

        for (Integer tcpPort : tcpPorts) {
            executorService.execute(() -> {
                Thread.currentThread().setName("presenceDetectionTCP_" + hostname + " " + String.valueOf(tcpPort));
                performServicePing(tcpPort);
                checkIfFinished();
            });
        }

        // ARP ping for IPv4 addresses. Use an own executor for each network interface
        if (interfaceNames != null) {
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
        if (executorService == null) {
            return;
        }
        // Finish the detection process
        executorService.shutdownNow();
        executorService = null;
        detectionChecks = 0;

        PresenceDetectionValue v;

        // The cache will be expired by now if cache_time < timeoutInMS. But the device might be actually reachable.
        // Therefore use lastSeenInMS here and not cache.isExpired() to determine if we got a ping response.
        if (lastSeenInMS + timeoutInMS + 100 < System.currentTimeMillis()) {
            // We haven't seen the device in the detection process
            v = new PresenceDetectionValue(destination.getHostAddress(), -1);
        } else {
            // Make the cache valid again and submit the value.
            v = cache.getExpiredValue();
        }
        cache.setValue(v);
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
        if (executorService == null) {
            return;
        }
        try {
            // We may get interrupted here by cancelRefreshJob().
            executorService.awaitTermination(timeoutInMS + 100, TimeUnit.MILLISECONDS);
            submitFinalResult();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
            executorService.shutdownNow();
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
            v = new PresenceDetectionValue(destination.getHostAddress(), 0);
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
            double pingTime = System.nanoTime();
            if (networkUtils.servicePing(destination.getHostAddress(), tcpPort, timeoutInMS)) {
                final double latency = Math.round((System.nanoTime() - pingTime) / 1000000.0f);
                PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.TCP_CONNECTION, latency);
                v.addReachableTcpService(tcpPort);
                updateListener.partialDetectionResult(v);
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
            if (iosDevice) {
                networkUtils.wakeUpIOS(destination);
                Thread.sleep(50);
            }
            double pingTime = System.nanoTime();
            if (networkUtils.nativeARPPing(arpPingMethod, arpPingUtilPath, interfaceName, destination.getHostAddress(),
                    timeoutInMS)) {
                final double latency = Math.round((System.nanoTime() - pingTime) / 1000000.0f);
                PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.ARP_PING, latency);
                updateListener.partialDetectionResult(v);
            }
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
        try {
            logger.trace("Perform java ping presence detection for {}", hostname);
            double pingTime = System.nanoTime();
            if (destination.isReachable(timeoutInMS)) {
                final double latency = Math.round((System.nanoTime() - pingTime) / 1000000.0f);
                PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.ICMP_PING, latency);
                updateListener.partialDetectionResult(v);
            }
        } catch (IOException e) {
            logger.trace("Failed to execute a java ping for ip {}", hostname, e);
        }
    }

    protected void performSystemPing() {
        try {
            logger.trace("Perform native ping presence detection for {}", hostname);
            double pingTime = System.nanoTime();
            if (networkUtils.nativePing(pingMethod, destination.getHostAddress(), timeoutInMS)) {
                final double latency = Math.round((System.nanoTime() - pingTime) / 1000000.0f);
                PresenceDetectionValue v = updateReachableValue(PresenceDetectionType.ICMP_PING, latency);
                updateListener.partialDetectionResult(v);
            }
        } catch (IOException e) {
            logger.trace("Failed to execute a native ping for ip {}", hostname, e);
        } catch (InterruptedException e) {
            // This can be ignored, the thread will end anyway
        }
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
        if (refreshJob != null && !refreshJob.isDone()) {
            refreshJob.cancel(true);
        }
        refreshJob = scheduledExecutorService.scheduleWithFixedDelay(() -> performPresenceDetection(true), 0,
                refreshIntervalInMS, TimeUnit.MILLISECONDS);

        enableDHCPListen(useDHCPsniffing);
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
        if (refreshJob != null && !refreshJob.isDone()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        enableDHCPListen(false);
    }

    /**
     * Enables/Disables listing for dhcp packets to figure out if devices have entered the network. This does not work
     * for iOS devices. The hostname of this network service object will be registered to the dhcp request packet
     * listener if enabled and unregistered otherwise.
     *
     * @param enabled Enable/Disable the dhcp listen service for this hostname.
     */
    private void enableDHCPListen(boolean enabled) {
        if (enabled) {
            try {
                if (DHCPListenService.register(destination.getHostAddress(), this).isUseUnprevilegedPort()) {
                    dhcpState = "No access right for port 67. Bound to port 6767 instead. Port forwarding necessary!";
                } else {
                    dhcpState = "Running normally";
                }
            } catch (SocketException e) {
                logger.warn("Cannot use DHCP sniffing.", e);
                useDHCPsniffing = false;
                dhcpState = "Cannot use DHCP sniffing: " + e.getLocalizedMessage();
            }
        } else {
            DHCPListenService.unregister(destination.getHostAddress());
            dhcpState = "off";
        }
    }
}
