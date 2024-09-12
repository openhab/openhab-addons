/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.magentatv.internal.network;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;
import static org.openhab.binding.magentatv.internal.MagentaTVUtil.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.magentatv.internal.MagentaTVHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVPoweroffListener} implements a UPnP listener to detect
 * power-off of the receiver
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVPoweroffListener extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVPoweroffListener.class);

    private final MagentaTVHandlerFactory handlerFactory;

    public static final String UPNP_MULTICAST_ADDRESS = "239.255.255.250";
    public static final int UPNP_PORT = 1900;
    public static final String UPNP_BYEBYE_MESSAGE = "ssdp:byebye";

    protected final MulticastSocket socket;
    protected @Nullable NetworkInterface networkInterface;
    protected byte[] buf = new byte[256];
    private boolean started = false;

    public MagentaTVPoweroffListener(MagentaTVHandlerFactory handlerFactory,
            @Nullable NetworkInterface networkInterface) throws IOException {
        setName("OH-Binding-magentatv-upnp-listener");
        setDaemon(true);

        this.handlerFactory = handlerFactory;
        this.networkInterface = networkInterface;
        socket = new MulticastSocket(UPNP_PORT);
    }

    @Override
    public void start() {
        if (!isStarted()) {
            logger.debug("Listening to SSDP shutdown messages");
            started = true;
            super.start();
        }
    }

    /**
     * Listening thread. Receive SSDP multicast packets and filter for byebye If
     * such a packet is received the handlerFactory is called, which then dispatches
     * the event to the thing handler.
     */
    @Override
    public void run() {
        try {
            logger.debug("SSDP listener started");
            socket.setReceiveBufferSize(1024);
            socket.setReuseAddress(true);

            // Join the Multicast group on the selected network interface
            socket.setNetworkInterface(networkInterface);
            InetAddress group = InetAddress.getByName(UPNP_MULTICAST_ADDRESS);
            socket.joinGroup(group);

            // read the SSDP messages
            while (!socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                try {
                    String ipAddress = substringAfter(packet.getAddress().toString(), "/");
                    if (message.contains("NTS: ")) {
                        String ssdpMsg = substringBetween(message, "NTS: ", "\r");
                        if (ssdpMsg != null) {
                            if (message.contains(MR400_DEF_DESCRIPTION_URL)
                                    || message.contains(MR401B_DEF_DESCRIPTION_URL)) {
                                if (ssdpMsg.contains(UPNP_BYEBYE_MESSAGE)) {
                                    handlerFactory.onPowerOff(ipAddress);
                                }
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    logger.debug("Unable to process SSDP message: {}", message);
                }
            }
        } catch (IOException | RuntimeException e) {
            logger.debug("Poweroff listener failure: {}", e.getMessage());
        } finally {
            close();
        }
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Make sure the socket gets closed
     */
    public void close() {
        if (started) { // if (isStarted()) {
            logger.debug("No longer listening to SSDP messages");
            if (!socket.isClosed()) {
                socket.close();
            }
            started = false;
        }
    }

    /**
     * Make sure the socket gets closed
     */
    public void dispose() {
        logger.debug("SSDP listener terminated");
        close();
    }
}
