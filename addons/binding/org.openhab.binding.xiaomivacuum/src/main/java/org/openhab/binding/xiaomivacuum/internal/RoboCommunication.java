/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.xiaomivacuum.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.openhab.binding.xiaomivacuum.XiaomiVacuumBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RoboCommunication} is responsible for communications with the vacuum
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class RoboCommunication {

    private static final int TIMEOUT = 5000;

    private final Logger logger = LoggerFactory.getLogger(RoboCommunication.class);

    private final String ip;
    private final byte[] token;
    private final byte[] serial;
    private AtomicInteger id = new AtomicInteger();

    public RoboCommunication(String ip, byte[] token, byte[] serial) {
        this.ip = ip;
        this.token = token;
        this.serial = serial;
    }

    public String sendCommand(VacuumCommand command) throws RoboCryptoException, IOException {
        return sendCommand(command, "");
    }

    public String sendCommand(VacuumCommand command, String params) throws RoboCryptoException, IOException {
        if (params.length() > 0) {
            params = "'params': [" + params + "],";
        }
        String idString = "'id': " + Integer.toString(id.incrementAndGet());
        String fullCommand = "{'method': '" + command.getCommand() + "', " + params + idString + "}";
        logger.debug("Send command: {} -> {} (token: {})", fullCommand, ip, new String(token));
        String response = sendCommand(fullCommand, token, ip, serial);
        //TODO: Change this to trace level later onwards
        logger.debug("Received response from {}: {}", ip, response);
        return response;
    }

    private String sendCommand(String command, byte[] token, String ip, byte[] serial)
            throws RoboCryptoException, IOException {
        byte[] encr;
        encr = RoboCrypto.encrypt(command.concat("\0").getBytes(), token);
        byte[] response = comms(Message.createMsgData(encr, token, serial), ip);
        Message roboResponse = new Message(response);
        logger.trace("Message Details:{} ", roboResponse.toSting());
        String decryptedResponse = new String(RoboCrypto.decrypt(roboResponse.getData(), token)).trim();
        return decryptedResponse;
    }

    public static byte[] comms(byte[] message, String ip) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByName(ip);
        byte[] sendData = new byte[1024];
        clientSocket.setSoTimeout(TIMEOUT);
        sendData = message;
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress,
                XiaomiVacuumBindingConstants.PORT);
        clientSocket.send(sendPacket);
        sendPacket.setData(new byte[1024]);
        clientSocket.receive(sendPacket);
        byte[] response = sendPacket.getData();
        clientSocket.close();
        return response;
    }
}
