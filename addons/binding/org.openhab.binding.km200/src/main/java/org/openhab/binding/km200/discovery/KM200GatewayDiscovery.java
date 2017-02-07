/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.km200.internal.KM200Comm;
import org.openhab.binding.km200.internal.KM200Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

/**
 * The {@link KM200GatewayDiscovery} class discovers KM50/100/200 devices on the
 * network by testing all local devices on port 80.
 *
 * @author Markus Eckhardt
 *
 */
public class KM200GatewayDiscovery {

    private Logger logger = LoggerFactory.getLogger(KM200GatewayDiscovery.class);

    private HashSet<InetAddress> listOfBroadcasts = new HashSet<InetAddress>();
    private HashSet<InetAddress> listOfKNDevices = new HashSet<InetAddress>();

    static protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(KM200GatewayDiscovery.class.getName());
    private ScheduledFuture<?> scanFuture;
    private ScheduledFuture<?> timeoutFuture;
    private int timeout;

    private List<KM200GatewayDiscoveryListener> listeners = new CopyOnWriteArrayList<KM200GatewayDiscoveryListener>();

    /**
     *
     * @param timeout
     *            how long we discover for
     */
    public KM200GatewayDiscovery(int timeout) {
        this.timeout = timeout;
        listeners = new LinkedList<KM200GatewayDiscoveryListener>();
    }

    /**
     * Adds a km200GatewayDiscoveryListener
     *
     * @param listener
     */
    public void addListener(KM200GatewayDiscoveryListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a km200GatewayDiscoveryListener
     *
     * @param listener
     */
    public void removeListener(KM200GatewayDiscoveryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Starts discovery for KM50/100/200 gateways
     */
    public synchronized void startDiscovery() {
        if (scanFuture != null && scanFuture.isDone()) {
            return;
        }

        determineLocalBrAddresses();

        scanFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                discoveryKNDevices();
                logger.debug("startDiscovery");
            }
        }, 0, TimeUnit.SECONDS);

        timeoutFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                stopDiscovery();
            }
        }, timeout, TimeUnit.SECONDS);
    }

    /**
     * Stops discovery of KM50/100/200 gateways
     */
    public synchronized void stopDiscovery() {
        if (scanFuture != null) {
            scanFuture.cancel(true);
        }
        if (timeoutFuture != null) {
            scanFuture.cancel(true);
        }
        for (KM200GatewayDiscoveryListener listener : listeners) {
            listener.gatewayDiscoveryFinished();
        }
    }

    /**
     * This function discovers devices in network
     *
     */
    public HashSet<InetAddress> getKNDevices() {
        return listOfKNDevices;
    }

    /**
     * This function checks whether a network device is reachable
     *
     */
    private boolean isReachable(InetAddress addr, int openPort, int timeOutMillis) {
        logger.debug("Testing: {} {} {}", addr, openPort, timeOutMillis);
        try {
            Socket soc = new Socket();
            soc.connect(new InetSocketAddress(addr.getHostAddress(), openPort), timeOutMillis);
            soc.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * This function checks whether a network interface is a private one
     *
     */
    private boolean isPrivateV4Address(InetAddress ip) {
        int address = InetAddresses.coerceToInteger(ip);
        return (((address >>> 24) & 0xFF) == 10)
                || ((((address >>> 24) & 0xFF) == 172) && ((address >>> 16) & 0xFF) >= 16
                        && ((address >>> 16) & 0xFF) <= 31)
                || ((((address >>> 24) & 0xFF) == 192) && (((address >>> 16) & 0xFF) == 168));
    }

    /**
     * This function discovers the broadcast address of all private network interfaces
     *
     */
    private void determineLocalBrAddresses() {
        InetAddress broadcast = null;
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback()) {
                    continue; // Don't want to broadcast to the loopback interface
                }
                if (!networkInterface.isUp()) {
                    continue; // Don't want to broadcast a offline interface
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (!isPrivateV4Address(interfaceAddress.getAddress())) {
                        continue;
                    }
                    if (interfaceAddress.getNetworkPrefixLength() < 24) {
                        continue;
                    }
                    broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    listOfBroadcasts.add(broadcast);
                }
            }
        } catch (SocketException e) {
            logger.error("Device autodetection fails, error: {}", e.getMessage());
        }
    }

    /**
     * This function discovers devices in network
     *
     */
    private void discoveryKNDevices() {
        KM200Device device = new KM200Device();
        KM200Comm<KM200Device> comm = null;
        try {
            for (InetAddress broadcast : listOfBroadcasts) {
                byte[] ip = broadcast.getAddress();

                logger.debug("Send request packet to: {}", broadcast);
                for (int i = 1; i <= 254; i++) {
                    ip[3] = (byte) i;
                    InetAddress address = InetAddress.getByAddress(ip);
                    if (isReachable(address, 80, 100)) {
                        String output = address.toString().substring(1);
                        logger.debug("Found device in this network: {}", output);
                        device.setIP4Address(address.getHostAddress());
                        comm = new KM200Comm<KM200Device>(device);
                        byte[] recData = comm.getDataFromService("/gateway/DateTime");
                        if (recData == null) {
                            logger.debug("Communication is not possible!");
                            continue;
                        }
                        if (recData.length == 0) {
                            logger.debug("No reply from KM200!");
                            continue;
                        }
                        logger.debug("Device is a KMXXX geteway: {}", address.getHostAddress());

                        KM200GatewayDiscoveryResult result = new KM200GatewayDiscoveryResult(address);
                        for (KM200GatewayDiscoveryListener listener : listeners) {
                            listener.gatewayDiscovered(result);
                        }
                    }
                }
            }
        } catch (UnknownHostException e) {
            logger.error("Error in discovering of devices: {}", e.getMessage());
        }
    }
}
