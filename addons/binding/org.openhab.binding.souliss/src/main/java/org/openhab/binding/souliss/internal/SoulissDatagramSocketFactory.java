/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal;

import java.net.DatagramSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissDatagramSocketFactory} is responsible for creating datagramSocket object for trasmission e
 * receiving.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissDatagramSocketFactory {
    // static DatagramSocket soulissDatagramSocket;
    // static DatagramSocket soulissDatagramSocket_port230;

    // public static Integer serverPort;
    private static Logger logger = LoggerFactory.getLogger(SoulissDatagramSocketFactory.class);

    public static DatagramSocket getSocketDatagram() {
        return getSocketDatagram(0);
    }

    public static DatagramSocket getSocketDatagram(int socketPortNumber) {
        // return DatagramSocket for packet trasmission
        DatagramSocket soulissDatagramSocket = null;
        logger.debug("Setup socket");
        try {
            if (socketPortNumber != 0) {
                soulissDatagramSocket = new DatagramSocket(socketPortNumber);
            } else {
                soulissDatagramSocket = new DatagramSocket();
            }
            logger.debug("Datagram Socket Created on port {}", soulissDatagramSocket.getLocalPort());
        } catch (SocketException e) {
            logger.error("Error on creation of Socket");
            logger.error(e.getMessage());
        }

        return soulissDatagramSocket;

    }
}
