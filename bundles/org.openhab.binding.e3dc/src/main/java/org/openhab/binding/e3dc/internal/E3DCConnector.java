/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RSCPData;
import org.openhab.binding.e3dc.internal.rscp.RSCPFrame;
import org.openhab.binding.e3dc.internal.rscp.util.AES256Helper;
import org.openhab.binding.e3dc.internal.rscp.util.BouncyAES256Helper;
import org.openhab.binding.e3dc.internal.rscp.util.ByteUtils;
import org.openhab.binding.e3dc.internal.rscp.util.FrameLoggerHelper;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCConnector} is responsible for handling the connection to E3DC, frame sending and receiving.
 *
 * @author Brendon Votteler - Initial Contribution
 * @author Björn Brings - Rework for OpenHAB
 */
public class E3DCConnector {
    private static final int maxRetries = 3;
    private static final long sleepMillisBeforeRetry = 5000;
    private static final Logger logger = LoggerFactory.getLogger(E3DCConnector.class);

    private @Nullable E3DCConfiguration config;
    private @Nullable E3DCHandler handle;
    private AES256Helper aesHelper;
    private Socket socket;

    public E3DCConnector(@NonNull E3DCHandler handle, E3DCConfiguration config) {
        this.handle = handle;
        this.config = config;

        final String aesPwd = config.getRscppassword(); // password set on E3DC for AES
        aesHelper = BouncyAES256Helper.createBouncyAES256Helper(aesPwd);

        connectE3DC();
    }

    /**
     * Connect & authenticate
     */
    public void connectE3DC() {
        if (config != null) {
            final String address = config.getIp();
            final int port = config.getPort();
            final String user = config.getWebusername(); // typically email address
            final String pwd = config.getWebpassword(); // used to log into E3DC portal

            logger.warn("Open connection to server {}:{} ...", address, port);
            try {
                openConnection(address, port);
                logger.warn("Sending authentication frame to server...");
                byte[] authFrame = E3DCRequests.buildAuthenticationMessage(user, pwd);
                Integer bytesSent = sendFrameToServer(aesHelper::encrypt, authFrame);
                byte[] decBytesReceived = receiveFrameFromServer(aesHelper::decrypt);
                logger.warn("Authentication: Received {} decrypted bytes from server.", decBytesReceived.length);
            } catch (UnknownHostException e) {
                handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Could not connect to host");
            } catch (IOException e) {
                handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Connection error");
            }
        }
    }

    public void requestE3DCData() {
        if (isNotConnected()) {
            connectE3DC();
        }
        try {
            byte[] reqFrame = E3DCRequests.buildRequestFrame();
            Integer bytesSent = sendFrameToServer(aesHelper::encrypt, reqFrame);
            byte[] decBytesReceived = receiveFrameFromServer(aesHelper::decrypt);
            logger.warn("Decrypted frame received: {}", ByteUtils.byteArrayToHexString(decBytesReceived));
            RSCPFrame responseFrame = RSCPFrame.builder().buildFromRawBytes(decBytesReceived);

            handleE3DCResponse(responseFrame);
            FrameLoggerHelper.logFrame(responseFrame);

        } catch (Exception e) {
            handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not establish connection");
        }
    }

    public void handleE3DCResponse(RSCPFrame responseFrame) {
        List<RSCPData> dataList = responseFrame.getData();
        for (RSCPData data : dataList) {
            handleUpdateDate(data);
        }
    }

    public void handleUpdateDate(RSCPData data) {
        String dt = data.getDataTag().name();

        if (dt.equals("TAG_EMS_POWER_PV")) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerPV,
                    new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
        } else if (dt.equals("TAG_EMS_POWER_BAT")) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerBat,
                    new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
        } else if (dt.equals("TAG_EMS_POWER_HOME")) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerHome,
                    new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
        } else if (dt.equals("TAG_EMS_POWER_GRID")) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerGrid,
                    new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
        } else if (dt.equals("TAG_EMS_POWER_ADD")) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerAdd,
                    new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
        }
    }

    private boolean isNotConnected() {
        return socket == null || socket.isClosed();
    }

    public void close() {
        try {
            socket.close();
            socket = null;
        } catch (IOException e) {
            logger.info("Couldn't close connection: {}", e);
        }
    }

    public void openConnection(String ipAddress, int port, int maxRetries, long sleepMillisBeforeRetry)
            throws IOException {
        socket = null;
        int retries = 0;
        while (isNotConnected() && retries++ < maxRetries) {
            try {
                socket = new Socket(ipAddress, port);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(10000);
            } catch (UnknownHostException e) {
                socket.close();
                throw e;
            } catch (IOException e) {
                socket.close();
                if (retries < maxRetries) {
                    try {
                        Thread.sleep(sleepMillisBeforeRetry);
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }

        if (socket == null) {
            // retries exhausted, still no connection
            throw new RuntimeException("Failed to establish connection to server.");
        }
    }

    public void openConnection(String ipAddress, int port) throws IOException {
        openConnection(ipAddress, port, maxRetries, sleepMillisBeforeRetry);
    }

    /**
     * Send a encrypt and send a byte array through a provided socket.
     *
     * @param socket The socket to write to.
     * @param encryptFunc A function to encrypt the provided frame.
     * @param frame The unencrypted frame as byte array.
     * @return Either an exception or the number of bytes sent.
     */
    public Integer sendFrameToServer(Function<byte[], byte[]> encryptFunc, byte[] frame) {
        if (isNotConnected()) {
            throw new IllegalStateException("Not connected to server. Must connect to server first before sending.");
        }

        try {
            byte[] encryptedFrame = encryptFunc.apply(frame);
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            dOut.write(encryptedFrame);
            dOut.flush();
            return encryptedFrame.length;
        } catch (Exception e) {
            logger.error("Error while encrypting and sending frame.", e);
        }
        return null;
    }

    /**
     * Receive a frame from a socket and decrypted it.
     *
     * @param socket A socket to read from.
     * @param decryptFunc A function to decrypt the received byte array.
     * @return Either an exception or the decrypted response as byte array.
     */
    public byte[] receiveFrameFromServer(Function<byte[], byte[]> decryptFunc) {
        if (isNotConnected()) {
            throw new IllegalStateException("Not connected to server. Must connect to server first before sending.");
        }

        try {
            int totalBytesRead = 0;
            DataInputStream dIn = new DataInputStream(socket.getInputStream());
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            do {
                int bytesRead = dIn.read(data, 0, data.length);
                logger.info("Received {} bytes, append to buffer... ", bytesRead);
                if (bytesRead == -1) {
                    logger.warn("Socket closed unexpectedly by server.");
                    break;
                }
                buffer.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
            } while (dIn.available() > 0);

            logger.info("Finished reading {} bytes.", totalBytesRead);
            buffer.flush();

            byte[] decryptedData = decryptFunc.apply(buffer.toByteArray());
            logger.debug("Decrypted frame data.");

            return decryptedData;
        } catch (Exception e) {
            logger.error("Error while receiving and decrypting frame.", e);
        }
        return null;
    }
}
