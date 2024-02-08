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
package org.openhab.binding.tsmart.internal;

import static org.openhab.binding.tsmart.internal.TSmartBindingConstants.T_SMART_PORT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TSmartUDPUtils} provides mechanism to send UDP packets with
 * a correct checksum for the T-Smart device
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class TSmartUDPUtils {

    private final Logger logger = LoggerFactory.getLogger(TSmartUDPUtils.class);

    public static TSmartUDPUtils client = new TSmartUDPUtils();

    /**
     * Send a UDP Packet to a T-Smart device with the correct checksum
     *
     * @param addr Network address of device
     * @param request Byte array for the request to append with checksum
     */
    public void sendUDPPacket(InetAddress addr, byte[] request) {

        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket reqPacket = new DatagramPacket(applyChecksum(request), request.length + 1, addr,
                    T_SMART_PORT);
            socket.send(reqPacket);
            socket.close();

        } catch (SocketTimeoutException ex) {
            logger.debug("Timeout error: {}", ex.getMessage());
        } catch (IOException ex) {
            logger.debug("Client error: {}", ex.getMessage());
        }
    }

    private byte[] applyChecksum(byte[] request) {
        byte checksum = 0;
        for (int i = 0; i < request.length; i++) {
            checksum ^= request[i];
        }
        checksum ^= 0x55;
        byte newRequest[] = Arrays.copyOf(request, request.length + 1);
        newRequest[request.length] = checksum;
        return newRequest;
    }
}
