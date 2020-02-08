/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.presence.internal.dhcp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulates a DHCP packet listener. It will listen for packets from devices requesting
 * an IPv4 address. If it's sees an IP address being delivered, it will callback the registered Ping
 * client that is listening for the IP address so that Ping handler can update its state to online.
 *
 * @author mdabbs - Initial contribution with help from Network binding
 */
@NonNullByDefault
public class DHCPListener extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(DHCPListener.class);

    static public volatile @Nullable DHCPListener instance;
    static public volatile int port;
    static Map<String, Callback> listeners = new HashMap<>();

    public static interface Callback {
        void dhcpReceived();
    }

    private static interface ServiceCallback {
        void dhcpReceived(String ipAddress);
    }

    /*
     * This is the callback from the listening thread when a valid IP address is found.
     * It searches the list of registered listeners for the IP address and notifies
     * the listener if found
     */
    @SuppressWarnings("null")
    private static ServiceCallback serviceCallback = (ipAddress -> {
        synchronized (listeners) {
            Callback listener = listeners.get(ipAddress);
            if (listener != null) {
                listener.dhcpReceived();
            }
        }
    });

    /*
     * This method is used to register a ping device listener. If this is the first listener
     * it starts the DHCP listening thread.
     */
    public static synchronized void register(String hostname, Callback callback) {
        if (instance == null) {
            try {
                instance = new DHCPListener();
                instance.start();
            } catch (SocketException e) {
                logger.warn("Failed to start listening for DHCP traffic", e);
            }
        }

        synchronized (listeners) {
            listeners.put(hostname, callback);
        }
    }

    /*
     * This method will shutdown the DHCP listening thread. This is only called when the Binding configuration
     * changes and allowDHCPListening is disabled.
     */
    public static synchronized void shutdown() {
        if (instance != null) {
            instance.interrupt();
        }
    }

    /*
     * This method is called when a ping device is going away or changing parameters. It removes them
     * from the list of listeners. If the list is subsequently empty, then DHCP listening thread is shutdown.
     */
    public static void unregister(String hostname) {
        synchronized (listeners) {
            listeners.remove(hostname);
            if (listeners.isEmpty() && instance != null) {
                instance.interrupt();
            }
        }
    }

    private DatagramSocket socket;

    @SuppressWarnings("null")
    @Override
    public void interrupt() {
        super.interrupt();
        logger.trace("DHCP Listener is shutting down");
        socket.close();
        try {
            instance.join();
            logger.debug("DHCP Listener has shut down");
        } catch (InterruptedException e) {
            logger.error("DHCP Listener was interrupted while shutting down", e);
        }
        instance = null;
    }

    public DHCPListener() throws SocketException {
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            socket.bind(new InetSocketAddress(67));
            port = 67;
            logger.info("Listening on port 67");
        } catch (SocketException e) {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            socket.bind(new InetSocketAddress(6776));
            port = 6776;
            logger.info("Listening on port 6776");
        }
        this.setName("PresenceDHCPListener");
    }

    @Override
    public void run() {
        final byte[] buffer = new byte[1526];
        DatagramPacket p = new DatagramPacket(buffer, buffer.length);
        while (true) {
            try {
                socket.receive(p);
                logger.debug("Received a packet on DHCPListener port, length {}", p.getLength());
                processPacket(p);
            } catch (IOException e) {
                logger.trace("DHCP Listener detected shutdown request");
                break;
            }
        }
    }

    // Magic cookie
    private static final int MAGIC_COOKIE = 0x63825363;
    private static final byte BOOTREQUEST = 1;
    private static final byte DHCP_REQUESTED_ADDRESS = 50;
    private static final byte DHCP_MESSAGE_TYPE = 53;
    private static final byte PAD = 0;
    private static final byte END = -1;

    /** DHCP MESSAGE CODES **/
    private static final byte DHCPREQUEST = 3;

    private void doCallback(boolean gotType, @Nullable InetAddress address) {
        if (gotType && address != null) {
            try {
                DHCPListener.serviceCallback.dhcpReceived(address.getHostAddress());
            } catch (Exception e) {
            }
            return;
        }
    }

    private void processPacket(DatagramPacket p) {
        ByteArrayInputStream is = new ByteArrayInputStream(p.getData());
        DataInputStream in = new DataInputStream(is);

        try {
            byte op = in.readByte();
            if (op != BOOTREQUEST) {
                return;
            }
            in.skip(235);

            // check for DHCP MAGIC_COOKIE
            if (in.readInt() != MAGIC_COOKIE) {
                logger.debug("Magic cookie mismatch");
                return;
            }

            boolean gotType = false;
            InetAddress address = null;

            // int cc = 50;
            // while (cc > 0) {
            int b, l, cc = 50;
            do {
                b = in.read();
                if (b == DHCP_MESSAGE_TYPE) {
                    if ((l = in.read()) != 1) {
                        logger.debug("Invalid length for DHCP Message Type option");
                        return;
                    }
                    if ((l = in.read()) != DHCPREQUEST) {
                        return;
                    }
                    gotType = true;
                    doCallback(gotType, address);
                } else if (b == DHCP_REQUESTED_ADDRESS) {
                    if ((l = in.read()) != 4) {
                        logger.debug("Invalid length for DHCP Requested Address option");
                        return;
                    }
                    byte[] addr = new byte[4];
                    if (in.read(addr) != 4) {
                        logger.debug("Truncated IP address");
                        return;
                    }
                    address = InetAddress.getByAddress(addr);
                    doCallback(gotType, address);
                } else if (b > 0 && b != PAD) {
                    if ((l = in.read()) > 0) {
                        if (in.skip(l) != l) {
                            logger.debug("Truncated/invalid packet");
                            return;
                        }
                    }
                }
            } while (b > 0 && b != END && --cc > 0);
            // }
        } catch (IOException e) {
        }
    }
}
