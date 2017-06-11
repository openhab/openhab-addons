/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.xiaomivacuum.internal;

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

    private static final int PORT = 54321;

    private final static Logger logger = LoggerFactory.getLogger(RoboCommunication.class);

    private String ip;
    private byte[] token;
    private byte[] serial;
    private int id;

    public RoboCommunication(String ip, byte[] token) throws Exception {
        this.ip = ip;
        this.token = token;
        byte[] response = comms(XiaomiVacuumBindingConstants.DISCOVER_STRING, ip);
        Message roboResponse = new Message(response);
        setSerial(roboResponse.getSerialByte());
    }

    public String sendCommand(VacuumCommand command) {
        id += 1;
        return sendCommand("{'method': '" + command.getCommand() + "', 'id': " + Integer.toString(id) + "}", token, ip,
                serial);
    }

    public String sendCommand(VacuumCommand command, String params) {
        id += 1;
        return sendCommand("{'method': '" + command.getCommand() + "', 'params': [" + params + "], 'id': "
                + Integer.toString(id) + "}", token, ip, serial);
    }

    public final static String sendCommand(VacuumCommand command, int id, byte[] token, String ip, byte[] serial) {
        return sendCommand("{'method': '" + command.getCommand() + "', 'id': " + Integer.toString(id) + "}", token, ip,
                serial);
    }

    public final static String sendCommand(String command, byte[] token, String IP, byte[] serial) {

        byte[] encr;
        try {
            encr = RoboCrypto.encrypt(command.concat("\0").getBytes(), token);
            logger.debug("Send command: {} -> {} (token: {})", command, IP, new String(token));
            byte[] response = comms(Message.createMsgData(encr, token, serial), IP);
            Message roboResponse = new Message(response);
            String decryptedResponse = new String(RoboCrypto.decrypt(roboResponse.getData(), token)).trim();
            logger.debug("respone {}", decryptedResponse);
            return decryptedResponse;

        } catch (Exception e) {
            logger.debug("Failed to process data {}", e);

        }
        return null;
    }

    public static byte[] comms(byte[] message, String IP) throws Exception {

        // DatagramSocket clientSocket = new DatagramSocket(PORT);
        DatagramSocket clientSocket = new DatagramSocket();

        InetAddress IPAddress = InetAddress.getByName(IP);
        byte[] sendData = new byte[1024];
        // byte[] receiveData = new byte[1024];

        // InetSocketAddress address = new InetSocketAddress("0.0.0.0", PORT);
        // clientSocket.bind(address);
        clientSocket.setSoTimeout(5000);
        sendData = message;
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
        clientSocket.send(sendPacket);
        // DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        // clientSocket.receive(receivePacket);
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
