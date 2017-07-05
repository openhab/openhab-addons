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
import java.net.SocketTimeoutException;
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

    private static final int MSG_BUFFER_SIZE = 1024;

    private static final int TIMEOUT = 5000;

    private final Logger logger = LoggerFactory.getLogger(RoboCommunication.class);

    private final String ip;
    private final byte[] token;
    private final byte[] deviceId;
    private AtomicInteger id = new AtomicInteger();

    public RoboCommunication(String ip, byte[] token, byte[] did) {
        this.ip = ip;
        this.token = token;
        this.deviceId = did;
    }

    public String sendCommand(VacuumCommand command) throws RoboCryptoException, IOException {
        return sendCommand(command, "");
    }

    public String sendCommand(VacuumCommand command, String params) throws RoboCryptoException, IOException {
        return sendCommand(command.getCommand(), params);
    }

    public String sendCommand(String command, String params) throws RoboCryptoException, IOException {
        if (params.length() > 0) {
            params = "'params': [" + params + "],";
        }
        String idString = "'id': " + Integer.toString(id.incrementAndGet());
        String fullCommand = "{'method': '" + command + "', " + params + idString + "}";
        logger.debug("Send command: {} -> {} (token: {})", fullCommand, ip, new String(token));
        return sendCommand(fullCommand, token, ip, deviceId);
    }

    private String sendCommand(String command, byte[] token, String ip, byte[] serial)
            throws RoboCryptoException, IOException {
        byte[] encr;
        encr = RoboCrypto.encrypt(command.concat("\0").getBytes(), token);
        byte[] sendMsg = Message.createMsgData(encr, token, serial);
        byte[] response = comms(sendMsg, ip);
        if (response.length == 0) {
            logger.debug("len {}", response.length);
            logger.debug("No response from vacuum for command {}.\r\n{}", command, (new Message(sendMsg)).toSting());
            return null;
        }
        Message roboResponse = new Message(response);
        logger.trace("Message Details:{} ", roboResponse.toSting());
        String decryptedResponse = new String(RoboCrypto.decrypt(roboResponse.getData(), token)).trim();
        // TODO: Change this to trace level later onwards
        logger.debug("Received response from {}: {}", ip, decryptedResponse);
        return decryptedResponse;
    }

    public byte[] comms(byte[] message, String ip) throws IOException {
        InetAddress ipAddress = InetAddress.getByName(ip);
        int p = ipAddress.getAddress()[3];
        p += XiaomiVacuumBindingConstants.PORT;
        try (DatagramSocket clientSocket = new DatagramSocket(p)) {
            logger.debug("Connection {}:{}", ip, clientSocket.getLocalPort());
            byte[] sendData = new byte[MSG_BUFFER_SIZE];
            clientSocket.setSoTimeout(TIMEOUT);
            // clientSocket.setReuseAddress(true);
            sendData = message;
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress,
                    XiaomiVacuumBindingConstants.PORT);
            clientSocket.send(sendPacket);
            sendPacket.setData(new byte[MSG_BUFFER_SIZE]);
            clientSocket.receive(sendPacket);
            byte[] response = sendPacket.getData();
            clientSocket.close();
            return response;
        } catch (SocketTimeoutException e) {
            logger.debug("Communication error for vacuum at {}: {}", ip, e.getMessage());
            return new byte[0];
        }
    }
}
