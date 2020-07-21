/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.ism8.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ism8Server} is responsible for listening to the Ism8 information
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
public class Server {
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private int port;
    private boolean stopServer;
    private int startRetries;
    private boolean connected;
    private ArrayList<IDataPoint> dataPoints = new ArrayList<>();
    private java.net.ServerSocket serverSocket = null;
    private java.net.Socket client;
    private IDataPointChangeListener changeListener;

    public Server(int port) {
        this.port = port;
    }

    /**
     * Gets the port of the server
     *
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Gets the connection state of the server
     *
     */
    public boolean getConnected() {
        return this.connected;
    }

    /**
     * Gets the data points of the server
     *
     */
    public ArrayList<IDataPoint> getDataPoints() {
        return this.dataPoints;
    }

    /**
     * Starts the server
     *
     */
    public void start() {
        this.stopServer = false;
        this.startRetries = 0;
        while (!this.stopServer) {
            try {
                this.handleCommunication();
                if (this.startRetries > 10) {
                    Thread.sleep(6000);
                }
            } catch (Exception e) {
                logger.error("Error Handle Communication - restart communication. {}", e.getMessage(), e);
                this.startRetries++;
            }
        }
    }

    /**
     * Stops the server
     *
     */
    public void stop() {
        this.stopServer = true;
        this.stopServer();
    }

    /**
     * Adds a data-point change listener to the server
     *
     */
    public void addDataPointChangeListener(IDataPointChangeListener listener) {
        if (this.changeListener == null) {
            this.changeListener = listener;
        }
    }

    /**
     * Adds a data-point to the server
     *
     */
    public void addDataPoint(int id, String knxType, String description) {
        IDataPoint dp = DataPointFactory.createDataPoint(id, knxType, description);
        if (dp != null) {
            for (IDataPoint dataPoint : this.dataPoints) {
                if (dataPoint.getId() == dp.getId()) {
                    return;
                }
            }
            this.dataPoints.add(dp);
        }
    }

    /**
     * Sends the data to the ISM8 partner
     *
     */
    public void sendData(byte[] data) throws Exception {
        if (this.client != null && this.client.isConnected() && data.length > 0) {
            OutputStream stream = this.client.getOutputStream();
            stream.write(data);
            logger.debug("Data sent: {}", this.printBytes(data));
        }
    }

    private void stopServer() {
        logger.info("Stop Ism8 server.");
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (this.client != null) {
                if (this.client.isConnected()) {
                    this.client.getInputStream().close();
                }
                this.client.close();
                this.client = null;
            }

            if (this.changeListener != null) {
                this.changeListener.connectionStatusChanged(ThingStatus.OFFLINE);
            }
        } catch (Exception e) {
            logger.error("Error stopping Communication. {}", e.getMessage(), e);
        }
    }

    private void handleCommunication() {
        try {
            logger.info("Waiting for connection in port {}.", this.getPort());
            if (this.changeListener != null) {
                this.changeListener.connectionStatusChanged(ThingStatus.OFFLINE);
            }
            serverSocket = new java.net.ServerSocket(this.getPort());
            this.client = serverSocket.accept();
            logger.info("Connection from Partner established {}", this.client.getRemoteSocketAddress());
            if (this.changeListener != null) {
                this.changeListener.connectionStatusChanged(ThingStatus.ONLINE);
            }

            this.startRetries = 0;
            this.sendUpdateCommand();
            while (true) {
                byte[] bytes = getBytesFromInputStream(this.client.getInputStream());
                int amount = bytes.length;
                ArrayList<byte[]> packages = this.getPackages(bytes, amount);

                for (int i = 0; i < packages.size(); i++) {
                    byte[] pack = packages.get(i);
                    logger.debug("Data received: {}", this.printBytes(pack));
                    KnxNetFrame frame = KnxNetFrame.createKnxNetPackage(pack, pack.length);
                    if (frame != null) {
                        byte[] answer = frame.createFrameAnswer();
                        if (answer.length > 0) {
                            this.sendData(answer);
                        }
                        SetDatapointValueMessage[] messages = frame.getValueMessages();
                        for (int j = 0; j < messages.length; j++) {
                            logger.debug("Message received: {} {}", messages[j].getId(),
                                    this.printBytes(messages[j].getData()));

                            IDataPoint dataPoint = null;
                            for (int k = 0; k < this.dataPoints.size(); k++) {
                                if (this.dataPoints.get(k).getId() == messages[j].getId()) {
                                    dataPoint = this.dataPoints.get(k);
                                    break;
                                }
                            }

                            if (dataPoint != null) {
                                dataPoint.processData(messages[j].getData());
                                logger.debug("{} {}", dataPoint.getDescription(), dataPoint.getValueText());
                                if (this.changeListener != null) {
                                    this.changeListener.dataPointChanged(new DataPointChangedEvent(this, dataPoint));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handle client data stream. {}", e.getMessage(), e);
            this.stopServer();
        }
    }

    private void sendUpdateCommand() throws Exception {
        byte[] data = new byte[] { (byte) 0x06, (byte) 0x20, (byte) 0xF0, (byte) 0x80, (byte) 0x00, (byte) 0x16,
                (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xF0, (byte) 0xD0 };
        this.sendData(data);
    }

    private byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        int len = is.read(buffer);
        os.write(buffer, 0, len);
        return os.toByteArray();
    }

    private ArrayList<byte[]> getPackages(byte[] data, int amount) {
        ArrayList<byte[]> result = new ArrayList<byte[]>();
        if (data.length >= amount) {
            ByteBuffer list = ByteBuffer.allocate(amount);
            for (int i = 0; i < amount; i++) {
                list.put(data[i]);
            }

            int start = -1;
            for (int i = 0; i < amount - 4; i++) {
                if (list.get(i + 0) == (byte) 0x06 && list.get(i + 1) == (byte) 0x20 && list.get(i + 2) == (byte) 0xF0
                        && list.get(i + 3) == (byte) 0x80) {
                    if (start >= 0) {
                        byte[] pkgData = new byte[i - start];
                        for (int j = 0; j < pkgData.length; j++) {
                            pkgData[j] = list.get(start + j);
                        }
                        result.add(pkgData);
                    }
                    start = i;
                }
            }
            if (start >= 0) {
                byte[] pkgData = new byte[amount - start];
                for (int j = 0; j < pkgData.length; j++) {
                    pkgData[j] = list.get(start + j);
                }
                result.add(pkgData);
            }
        }
        return result;
    }

    private String printBytes(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
