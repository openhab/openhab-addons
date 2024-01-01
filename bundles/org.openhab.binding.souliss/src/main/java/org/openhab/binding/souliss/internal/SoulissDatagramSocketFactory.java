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
package org.openhab.binding.souliss.internal;

import java.net.DatagramSocket;
import java.net.SocketException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;

/**
 * The {@link SoulissDatagramSocketFactory} is responsible for creating datagramSocket object for trasmission e
 * receiving.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissDatagramSocketFactory {

    public static @Nullable DatagramSocket getSocketDatagram(Logger logger) {
        return getSocketDatagram(0, logger);
    }

    public static @Nullable DatagramSocket getSocketDatagram(int socketPortNumber, Logger logger) {
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
            logger.warn("Error on creation of Socket: {}", e.getMessage());
        }

        return soulissDatagramSocket;
    }
}
