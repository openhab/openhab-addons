/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.model.script.actions.Ping;
import org.openhab.binding.network.service.dhcp.ReceiveDHCPRequestPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkService} handles the connection to the Device
 *
 * @author Marc Mettke
 * @author David Gräff, 2016 - Add DHCP listen for request packets
 */
public class NetworkService {

    private Logger logger = LoggerFactory.getLogger(NetworkService.class);

    private ScheduledFuture<?> refreshJob;

    private String hostname;
    private int port;
    private int retry;
    private boolean dhcplisten;
    private long refreshInterval;
    private int timeout;
    private boolean useSystemPing;
    private final StateUpdateListener updateListener;

    public NetworkService(final StateUpdateListener updateListener) {
        this(updateListener, "", 0, 1, true, 60000, 5000, false);
    }

    public NetworkService(final StateUpdateListener updateListener, String hostname, int port, int retry,
            boolean dhcplisten, long refreshInterval, int timeout, boolean useSystemPing) {
        super();
        this.updateListener = updateListener;
        this.hostname = hostname;
        this.port = port;
        this.retry = retry;
        this.dhcplisten = dhcplisten;
        this.refreshInterval = refreshInterval;
        this.timeout = timeout;
        this.useSystemPing = useSystemPing;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getRetry() {
        return retry;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isUseSystemPing() {
        return useSystemPing;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setDHCPListen(boolean dhcplisten) {
        this.dhcplisten = dhcplisten;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setUseSystemPing(boolean useSystemPing) {
        this.useSystemPing = useSystemPing;
    }

    /**
     * Create a runner to be called by a scheduler periodically. The runner
     * will update the device state and inform the callback listener.
     *
     * @return Return a runner.
     */
    private Runnable createUpdateListenerRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    updateListener.updatedDeviceState(updateDeviceState());
                } catch (InvalidConfigurationException e) {
                    updateListener.invalidConfig();
                }
            }
        };
    }

    /**
     * Cancel a running refreshJob. Will not throw if no job is running
     * or the job is already canceled.
     */
    private void cancelRefreshJob() {
        try {
            if (refreshJob != null) {
                refreshJob.cancel(true);
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Start/Restart a fixed scheduled runner to update the devices reach-ability state.
     *
     * @param scheduledExecutorService A scheduler to run pings periodically.
     * @param stateChangeListener Your callback listener
     */
    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService) {
        cancelRefreshJob();
        refreshJob = scheduledExecutorService.scheduleAtFixedRate(createUpdateListenerRunnable(), 0, refreshInterval,
                TimeUnit.MILLISECONDS);

        enableDHCPListen(dhcplisten);
    }

    public void stopAutomaticRefresh() {
        cancelRefreshJob();
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
        try {
            if (enabled) {
                try {
                    ReceiveDHCPRequestPackets.register(InetAddress.getByName(hostname).getHostAddress(),
                            updateListener);
                } catch (SocketException e) {
                    logger.warn("Cannot use DHCP listen. You may not have approriate access rights!", e);
                }
            } else {
                ReceiveDHCPRequestPackets.unregister(InetAddress.getByName(hostname).getHostAddress());
            }
        } catch (UnknownHostException e) {
            logger.warn("Hostname invalid: {}", hostname, e);
        }
    }

    /**
     * Updates one device to a new status
     */
    public double updateDeviceState() throws InvalidConfigurationException {
        int currentTry = 0;
        do {
            boolean success;
            double pingTime;

            try {
                pingTime = System.nanoTime();
                if (!useSystemPing) {
                    success = Ping.checkVitality(hostname, port, timeout);
                } else {
                    success = NetworkUtils.nativePing(hostname, port, timeout);
                }
                pingTime = System.nanoTime() - pingTime;
                if (success) {
                    logger.debug("established connection [host '{}' port '{}' timeout '{}']",
                            new Object[] { hostname, port, timeout });
                    return pingTime / 1000000.0f;
                }
            } catch (SocketTimeoutException se) {
                logger.debug("timed out while connecting to host '{}' port '{}' timeout '{}'",
                        new Object[] { hostname, port, timeout });
            } catch (IOException ioe) {
                logger.debug("couldn't establish network connection [host '{}' port '{}' timeout '{}']",
                        new Object[] { hostname, port, timeout });
            } catch (InterruptedException e) {
                logger.debug("ping program was interrupted");
            }

        } while (currentTry++ < this.retry);

        return -1;
    }

    @Override
    public String toString() {
        return this.hostname + ";" + this.port + ";" + this.retry + ";" + this.refreshInterval + ";" + this.timeout
                + ";" + this.useSystemPing;
    }
}
