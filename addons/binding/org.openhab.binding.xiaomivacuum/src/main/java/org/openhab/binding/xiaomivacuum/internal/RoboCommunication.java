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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openhab.binding.xiaomivacuum.XiaomiVacuumBindingConstants;
import org.openhab.binding.xiaomivacuum.internal.robot.VacuumCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link RoboCommunication} is responsible for communications with the vacuum
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class RoboCommunication {

    private static final int MSG_BUFFER_SIZE = 1024;
    private static final int TIMEOUT = 15000;

    private final Logger logger = LoggerFactory.getLogger(RoboCommunication.class);

    private final String ip;
    private final byte[] token;
    private final byte[] deviceId;
    private DatagramSocket socket;
    private AtomicInteger id = new AtomicInteger();
    private int timeDelta;
    private int timeStamp;
    JsonParser parser;

    public RoboCommunication(String ip, byte[] token, byte[] did, int id) {
        this.ip = ip;
        this.token = token;
        this.deviceId = did;
        setId(id);
        parser = new JsonParser();
    }

    public String sendCommand(VacuumCommand command) throws RoboCryptoException, IOException {
        return sendCommand(command, "");
    }

    public String sendCommand(VacuumCommand command, String params) throws RoboCryptoException, IOException {
        return sendCommand(command.getCommand(), params);
    }

    public String sendCommand(String command, String params)
            throws RoboCryptoException, IOException, JsonSyntaxException {
        JsonObject fullCommand = new JsonObject();
        fullCommand.addProperty("id", id.incrementAndGet());
        fullCommand.addProperty("method", command);
        fullCommand.add("params", parser.parse("[" + params + "]").getAsJsonArray());
        logger.debug("Send command: {} -> {} (Device: {} token: {})", fullCommand.toString(), ip,
                Utils.getHex(deviceId), Utils.getHex(token));
        return sendCommand(fullCommand.toString(), token, ip, deviceId);
    }

    private String sendCommand(String command, byte[] token, String ip, byte[] deviceId)
            throws RoboCryptoException, IOException {
        byte[] encr;
        encr = RoboCrypto.encrypt(command.getBytes(), token);
        timeStamp = (int) TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTime().getTime());
        byte[] sendMsg = Message.createMsgData(encr, token, deviceId, timeStamp + timeDelta);
        Message roboResponseMsg = sendData(sendMsg, ip);
        if (roboResponseMsg == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No response from device {} at {} for command {}.\r\n{}", Utils.getHex(deviceId), ip,
                        command, (new Message(sendMsg)).toSting());
            } else {
                logger.debug("No response from device {} at {} for command {}.", Utils.getHex(deviceId), ip, command);
            }
            return null;
        }

        String decryptedResponse = new String(RoboCrypto.decrypt(roboResponseMsg.getData(), token)).trim();
        // TODO: Change this to trace level later onwards
        logger.debug("Received response from {}: {}", ip, decryptedResponse);
        return decryptedResponse;
    }

    public Message sendPing(String ip) throws IOException {
        timeStamp = (int) TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTime().getTime());
        return sendData(XiaomiVacuumBindingConstants.DISCOVER_STRING, ip);
    }

    private Message sendData(byte[] sendMsg, String ip) throws IOException {
        byte[] response = comms(sendMsg, ip);
        if (response.length > 32) {
            Message roboResponse = new Message(response);
            timeDelta = roboResponse.getTimestampAsInt() - timeStamp;
            // TODO: remove this line shortly
            logger.trace("time {}, robo {} DELTA TIME: {}. sendtime {}", timeStamp, roboResponse.getTimestampAsInt(),
                    timeDelta, timeStamp + timeDelta);
            logger.trace("Message Details:{} ", roboResponse.toSting());
            return roboResponse;
        } else {
            return null;
        }
    }

    private synchronized byte[] comms(byte[] message, String ip) throws IOException {
        InetAddress ipAddress = InetAddress.getByName(ip);
        DatagramSocket clientSocket = getSocket();
        try {
            logger.trace("Connection {}:{}", ip, clientSocket.getLocalPort());
            byte[] sendData = new byte[MSG_BUFFER_SIZE];
            sendData = message;
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress,
                    XiaomiVacuumBindingConstants.PORT);
            clientSocket.send(sendPacket);
            sendPacket.setData(new byte[MSG_BUFFER_SIZE]);
            clientSocket.receive(sendPacket);
            byte[] response = sendPacket.getData();
            return response;
        } catch (SocketTimeoutException e) {
            logger.debug("Communication error for vacuum at {}: {}", ip, e.getMessage());
            clientSocket.close();
            return new byte[0];
        }
    }

    private DatagramSocket getSocket() throws SocketException {
        if (socket == null || socket.isClosed()) {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
            return socket;
        } else {
            return socket;
        }
    }

    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    /**
     * @return the id
     */
    public int getId() {
        return id.incrementAndGet();
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id.set(id);
    }

    /**
     * Time delta between device time & server time
     */
    public int getTimeDelta() {
        return timeDelta;
    }
}
