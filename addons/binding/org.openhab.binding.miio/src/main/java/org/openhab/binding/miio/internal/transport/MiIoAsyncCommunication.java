/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.miio.internal.transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.miio.MiIoBindingConstants;
import org.openhab.binding.miio.internal.Message;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoCrypto;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.openhab.binding.miio.internal.MiIoMessageListener;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MiIoAsyncCommunication} is responsible for communications with the Mi IO devices
 * *
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoAsyncCommunication {

    private static final int MSG_BUFFER_SIZE = 2048;

    private final Logger logger = LoggerFactory.getLogger(MiIoAsyncCommunication.class);

    private final String ip;
    private final byte[] token;
    private byte[] deviceId;
    private DatagramSocket socket;

    private List<MiIoMessageListener> listeners = new CopyOnWriteArrayList<>();

    private AtomicInteger id = new AtomicInteger();
    private int timeDelta;
    private int timeStamp;
    private final JsonParser parser;
    private MessageSenderThread senderThread;
    private boolean connected;
    private ThingStatusDetail status;
    private int errorCounter;
    private int timeout;
    private boolean needPing = true;
    private static final int MAX_ERRORS = 3;

    private ConcurrentLinkedQueue<MiIoSendCommand> concurrentLinkedQueue = new ConcurrentLinkedQueue<MiIoSendCommand>();

    public MiIoAsyncCommunication(String ip, byte[] token, byte[] did, int id, int timeout) {
        this.ip = ip;
        this.token = token;
        this.deviceId = did;
        this.timeout = timeout;
        setId(id);
        parser = new JsonParser();
        senderThread = new MessageSenderThread();
        senderThread.start();
    }

    protected List<MiIoMessageListener> getListeners() {
        return listeners;
    }

    /**
     * Registers a {@link XiaomiSocketListener} to be called back, when data is received.
     * If no {@link XiaomiSocket} exists, when the method is called, it is being set up.
     *
     * @param listener - {@link XiaomiSocketListener} to be called back
     */
    public synchronized void registerListener(MiIoMessageListener listener) {
        needPing = true;
        startReceiver();
        if (!getListeners().contains(listener)) {
            logger.trace("Adding socket listener {}", listener);
            getListeners().add(listener);
        }
    }

    /**
     * Unregisters a {@link XiaomiSocketListener}. If there are no listeners left,
     * the {@link XiaomiSocket} is being closed.
     *
     * @param listener - {@link XiaomiSocketListener} to be unregistered
     */
    public synchronized void unregisterListener(MiIoMessageListener listener) {
        getListeners().remove(listener);
    }

    public int queueCommand(MiIoCommand command) throws MiIoCryptoException, IOException {
        return queueCommand(command, "[]");
    }

    public int queueCommand(MiIoCommand command, String params) throws MiIoCryptoException, IOException {
        return queueCommand(command.getCommand(), params);
    }

    public int queueCommand(String command, String params)
            throws MiIoCryptoException, IOException, JsonSyntaxException {
        try {
            JsonObject fullCommand = new JsonObject();
            int cmdId = id.incrementAndGet();
            fullCommand.addProperty("id", cmdId);
            fullCommand.addProperty("method", command);
            fullCommand.add("params", parser.parse(params));
            MiIoSendCommand sendCmd = new MiIoSendCommand(cmdId, MiIoCommand.getCommand(command),
                    fullCommand.toString());
            concurrentLinkedQueue.add(sendCmd);
            logger.debug("Command added to Queue {} -> {} (Device: {} token: {} Queue: {})", fullCommand.toString(), ip,
                    Utils.getHex(deviceId), Utils.getHex(token), concurrentLinkedQueue.size());
            if (needPing) {
                sendPing(ip);
            }
            return cmdId;
        } catch (JsonSyntaxException e) {
            logger.warn("Send command '{}' with parameters {} -> {} (Device: {}) gave error {}", command, params, ip,
                    Utils.getHex(deviceId), e.getMessage());
            throw e;
        }
    }

    MiIoSendCommand sendMiIoSendCommand(MiIoSendCommand miIoSendCommand) {
        String errorMsg = "Unknown Error while sending command";
        String decryptedResponse = "";
        try {
            decryptedResponse = sendCommand(miIoSendCommand.getCommandString(), token, ip, deviceId);
            // hack due to avoid invalid json errors from some misbehaving device firmwares
            decryptedResponse = decryptedResponse.replace(",,", ",");
            JsonElement response;
            response = parser.parse(decryptedResponse);
            if (response.isJsonObject()) {
                logger.trace("Received  JSON message {}", response.toString());
                miIoSendCommand.setResponse(response.getAsJsonObject());
                return miIoSendCommand;
            } else {
                errorMsg = "Received message is invalid JSON";
                logger.debug("{}: {}", errorMsg, decryptedResponse);
            }
        } catch (MiIoCryptoException | IOException e) {
            logger.warn("Send command '{}'  -> {} (Device: {}) gave error {}", miIoSendCommand.getCommandString(), ip,
                    Utils.getHex(deviceId), e.getMessage());
            errorMsg = e.getMessage();
        } catch (JsonSyntaxException e) {
            logger.warn("Could not parse '{}' <- {} (Device: {}) gave error {}", decryptedResponse,
                    miIoSendCommand.getCommandString(), Utils.getHex(deviceId), e.getMessage());
            errorMsg = "Received message is invalid JSON";
        }
        JsonObject erroResp = new JsonObject();
        erroResp.addProperty("error", errorMsg);
        miIoSendCommand.setResponse(erroResp);
        return miIoSendCommand;
    }

    public synchronized void startReceiver() {
        if (senderThread == null) {
            senderThread = new MessageSenderThread();
        }
        if (!senderThread.isAlive()) {
            senderThread.start();
        }
    }

    private class MessageSenderThread extends Thread {
        public MessageSenderThread() {
            super("Mi IO MessageSenderThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            logger.debug("Starting Mi IO MessageSenderThread");
            while (!interrupted()) {
                try {
                    if (concurrentLinkedQueue.isEmpty()) {
                        Thread.sleep(100);
                        continue;
                    }
                    MiIoSendCommand queuedMessage = concurrentLinkedQueue.remove();
                    MiIoSendCommand miIoSendCommand = sendMiIoSendCommand(queuedMessage);
                    for (MiIoMessageListener listener : listeners) {
                        logger.trace("inform listener {}, data {} from {}", listener, queuedMessage, miIoSendCommand);
                        try {
                            listener.onMessageReceived(miIoSendCommand);
                        } catch (Exception e) {
                            logger.debug("Could not inform listener {}: {}: ", listener, e.getMessage(), e);
                        }
                    }
                } catch (NoSuchElementException e) {
                    // ignore
                } catch (InterruptedException e) {
                    // That's our signal to stop
                    break;
                } catch (Exception e) {
                    logger.warn("Error while polling/sending message", e);
                }
            }
            logger.debug("Finished Mi IO MessageSenderThread");
        }
    }

    private String sendCommand(String command, byte[] token, String ip, byte[] deviceId)
            throws MiIoCryptoException, IOException {
        byte[] encr;
        encr = MiIoCrypto.encrypt(command.getBytes(), token);
        timeStamp = (int) TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTime().getTime());
        byte[] sendMsg = Message.createMsgData(encr, token, deviceId, timeStamp + timeDelta);
        Message miIoResponseMsg = sendData(sendMsg, ip);
        if (miIoResponseMsg == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No response from device {} at {} for command {}.\r\n{}", Utils.getHex(deviceId), ip,
                        command, (new Message(sendMsg)).toSting());
            } else {
                logger.debug("No response from device {} at {} for command {}.", Utils.getHex(deviceId), ip, command);
            }
            errorCounter++;
            if (errorCounter > MAX_ERRORS) {
                status = ThingStatusDetail.CONFIGURATION_ERROR;
                sendPing(ip);
            }
            return "{\"error\":\"No Response\"}";
        }
        if (!miIoResponseMsg.isChecksumValid()) {
            return "{\"error\":\"Message has invalid checksum\"}";
        }
        if (errorCounter > 0) {
            errorCounter = 0;
            status = ThingStatusDetail.NONE;
            updateStatus(ThingStatus.ONLINE, status);
        }
        if (!connected) {
            pingSuccess();
        }
        String decryptedResponse = new String(MiIoCrypto.decrypt(miIoResponseMsg.getData(), token), "UTF-8").trim();
        logger.trace("Received response from {}: {}", ip, decryptedResponse);
        return decryptedResponse;
    }

    public Message sendPing(String ip) throws IOException {
        for (int i = 0; i < 3; i++) {
            logger.debug("Sending Ping {} ({})", Utils.getHex(deviceId), ip);
            Message resp = sendData(MiIoBindingConstants.DISCOVER_STRING, ip);
            if (resp != null) {
                pingSuccess();
                return resp;
            }
        }
        pingFail();
        return null;
    }

    private void pingFail() {
        logger.debug("Ping {} ({}) failed", Utils.getHex(deviceId), ip);
        connected = false;
        status = ThingStatusDetail.COMMUNICATION_ERROR;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    private void pingSuccess() {
        logger.debug("Ping {} ({}) success", Utils.getHex(deviceId), ip);
        if (!connected) {
            connected = true;
            status = ThingStatusDetail.NONE;
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            if (ThingStatusDetail.CONFIGURATION_ERROR.equals(status)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } else {
                status = ThingStatusDetail.NONE;
                updateStatus(ThingStatus.ONLINE, status);
            }
        }
    }

    private void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        for (MiIoMessageListener listener : listeners) {
            logger.trace("inform listener {}, data {} from {}", listener, status, statusDetail);
            try {
                listener.onStatusUpdated(status, statusDetail);
            } catch (Exception e) {
                logger.debug("Could not inform listener {}: {}", listener, e.getMessage(), e);
            }
        }
    }

    private Message sendData(byte[] sendMsg, String ip) throws IOException {
        byte[] response = comms(sendMsg, ip);
        if (response.length >= 32) {
            Message miIoResponse = new Message(response);
            timeStamp = (int) TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTime().getTime());
            timeDelta = miIoResponse.getTimestampAsInt() - timeStamp;
            logger.trace("Message Details:{} ", miIoResponse.toSting());
            return miIoResponse;
        } else {
            logger.trace("Reponse length <32 : {}", response.length);
            return null;
        }
    }

    private synchronized byte[] comms(byte[] message, String ip) throws IOException {
        InetAddress ipAddress = InetAddress.getByName(ip);
        DatagramSocket clientSocket = getSocket();
        DatagramPacket receivePacket = new DatagramPacket(new byte[MSG_BUFFER_SIZE], MSG_BUFFER_SIZE);
        try {
            logger.trace("Connection {}:{}", ip, clientSocket.getLocalPort());
            byte[] sendData = new byte[MSG_BUFFER_SIZE];
            sendData = message;
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress,
                    MiIoBindingConstants.PORT);
            clientSocket.send(sendPacket);
            sendPacket.setData(new byte[MSG_BUFFER_SIZE]);
            clientSocket.receive(receivePacket);
            byte[] response = Arrays.copyOfRange(receivePacket.getData(), receivePacket.getOffset(),
                    receivePacket.getOffset() + receivePacket.getLength());
            return response;
        } catch (SocketTimeoutException e) {
            logger.debug("Communication error for Mi IO device at {}: {}", ip, e.getMessage());
            needPing = true;
            return new byte[0];
        }
    }

    private DatagramSocket getSocket() throws SocketException {
        if (socket == null || socket.isClosed()) {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            return socket;
        } else {
            return socket;
        }
    }

    public void close() {
        if (socket != null) {
            socket.close();
        }
        senderThread.interrupt();
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

    public byte[] getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(byte[] deviceId) {
        this.deviceId = deviceId;
    }

    public int getQueueLenght() {
        return concurrentLinkedQueue.size();
    }
}
