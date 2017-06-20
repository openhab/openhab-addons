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
    private byte[] serial;
    private volatile int id;

    public RoboCommunication(String ip, byte[] token) throws IOException {
        this.ip = ip;
        this.token = token;
        byte[] response = comms(XiaomiVacuumBindingConstants.DISCOVER_STRING, ip);
        Message roboResponse = new Message(response);
        setSerial(roboResponse.getSerialByte());
    }

    public RoboCommunication(String ip, byte[] token, byte[] serial) throws IOException {
        this.ip = ip;
        this.token = token;
        this.serial = serial;
    }

    public String sendCommand(VacuumCommand command) {
        return sendCommand(command, "");
    }

    public String sendCommand(VacuumCommand command, String params) {
        id += 1;
        if (params.length() > 0) {
            params = "'params': [" + params + "],";
        }
        String fullCommand = "{'method': '" + command.getCommand() + "', " + params + "'id': " + Integer.toString(id)
                + "}";
        logger.debug("Send command: {} -> {} (token: {})", fullCommand, ip, new String(token));
        String response;
        try {
            response = sendCommand(fullCommand, token, ip, serial);
            logger.debug("respone {}", response);
            return response;
        } catch (Exception e) {
            logger.debug("Error while sending command: {}", e.getMessage());
        }
        return null;
    }

    public static final String sendCommand(VacuumCommand command, int id, byte[] token, String ip, byte[] serial)
            throws Exception {
        return sendCommand("{'method': '" + command.getCommand() + "', 'id': " + Integer.toString(id) + "}", token, ip,
                serial);
    }

    public static final String sendCommand(String command, byte[] token, String ip, byte[] serial) throws Exception {
        byte[] encr;
        encr = RoboCrypto.encrypt(command.concat("\0").getBytes(), token);
        byte[] response = comms(Message.createMsgData(encr, token, serial), ip);
        Message roboResponse = new Message(response);
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

    public byte[] getSerial() {
        return serial;
    }

    public void setSerial(byte[] serial) {
        this.serial = serial;
    }

}
