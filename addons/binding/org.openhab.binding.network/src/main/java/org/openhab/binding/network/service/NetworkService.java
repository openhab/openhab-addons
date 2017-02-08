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

    private static Logger logger = LoggerFactory.getLogger(NetworkService.class);

    private ScheduledFuture<?> refreshJob;

    private String hostname;
    private int port;
    private int retry;
    private boolean dhcplisten;
    private long refreshInterval;
    private int timeout;
    private boolean useSystemPing;

    public NetworkService() {
        this("", 0, 1, true, 60000, 5000, false);
    }

    public NetworkService(String hostname, int port, int retry, boolean dhcplisten, long refreshInterval, int timeout,
            boolean useSystemPing) {
        super();
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

    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService,
            final StateUpdate stateUpdate) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    stateUpdate.newState(updateDeviceState());
                } catch (InvalidConfigurationException e) {
                    stateUpdate.invalidConfig();
                }
            }
        };

        refreshJob = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, refreshInterval, TimeUnit.MILLISECONDS);

        if (dhcplisten) {
            try {
                ReceiveDHCPRequestPackets.register(InetAddress.getByName(hostname).getHostAddress(), stateUpdate);
            } catch (SocketException | UnknownHostException e) {
                logger.error("Cannot use DHCP listen: " + e.getMessage());
            }
        }
    }

    public void stopAutomaticRefresh() {
        refreshJob.cancel(true);
        try {
            ReceiveDHCPRequestPackets.unregister(InetAddress.getByName(hostname).getHostAddress());
        } catch (UnknownHostException e) {
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
