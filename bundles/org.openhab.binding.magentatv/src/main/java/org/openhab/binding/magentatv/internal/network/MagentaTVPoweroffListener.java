/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.magentatv.internal.MagentaTVHandlerFactory;
import org.openhab.binding.magentatv.internal.utils.MagentaTVLogger;

/**
 * The {@link MagentaTVPoweroffListener} implements a UPnP listener to detect
 * power-off of the receiver
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVPoweroffListener extends Thread {
    private final MagentaTVLogger logger = new MagentaTVLogger(MagentaTVPoweroffListener.class, "UPnP");
    private final MagentaTVHandlerFactory handlerFactory;

    public static final String UPNP_MULTICAST_ADDRESS = "239.255.255.250";
    public static final int UPNP_PORT = 1900;
    public static final String UPNP_BYEBYE_MESSAGE = "ssdp:byebye";

    protected @Nullable MulticastSocket socket = null;
    protected @Nullable NetworkInterface networkInterface = null;
    protected byte[] buf = new byte[256];

    public MagentaTVPoweroffListener(MagentaTVHandlerFactory handlerFactory,
            @Nullable NetworkInterface networkInterface) {
        Validate.notNull(handlerFactory);
        Validate.notNull(networkInterface);
        this.handlerFactory = handlerFactory;
        this.networkInterface = networkInterface;
    }

    @Override
    public void start() {
        logger.debug("Listring to SSDP shutdown messages");
        super.start();
    }

    /**
     * Listening thread. Receive SSDP multicast packets and filter for byebye If
     * such a packet is received the handlerFactory is called, which then dispatches
     * the event to the thing handler.
     */
    @SuppressWarnings("null")
    @Override
    public void run() {
        try {
            logger.debug("SSDP listener started");
            socket = new MulticastSocket(UPNP_PORT);
            Validate.notNull(socket);
            socket.setReceiveBufferSize(1024);
            socket.setReuseAddress(true);

            // Join the Multicast group on the selected network interface
            socket.setNetworkInterface(networkInterface);
            InetAddress group = InetAddress.getByName(UPNP_MULTICAST_ADDRESS);
            Validate.notNull(group);
            socket.joinGroup(group);

            // read the SSDP messages
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                try {
                    String ipAddress = StringUtils.substringAfter(packet.getAddress().toString(), "/");
                    if ((message != null) && message.contains("NTS: ")) {
                        String ssdpMsg = StringUtils.substringBetween(message, "NTS: ", "\r");
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
                    logger.exception(e, "Unable to process SSDP message: {0}", message);
                }
            }
        } catch (IOException | RuntimeException e) {
            logger.exception(e, "Poweroff listener failure");
        } finally {
            close();
        }

    }

    public boolean isStarted() {
        return socket != null;
    }

    /**
     * Make sure the socket gets closed
     */
    @SuppressWarnings("null")
    public void close() {
        if (isStarted()) {
            Validate.notNull(socket);
            socket.close();
            socket = null;
            logger.debug("No longer listening to SSDP messages");
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
