/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sonyprojector.internal.communication.sdcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.binding.sonyprojector.internal.SonyProjectorModel;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorConnector;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorItem;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with Sony Projectors through an IP connection
 * using Pj Talk service (SDCP protocol)
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Refactoring to consider SonyProjectorConnector and add a full check of responses
 */
@NonNullByDefault
public class SonyProjectorSdcpConnector extends SonyProjectorConnector {

    private final Logger logger = LoggerFactory.getLogger(SonyProjectorSdcpConnector.class);

    private static final int DEFAULT_PORT = 53484;
    private static final String DEFAULT_COMMUNITY = "SONY";
    private static final long READ_TIMEOUT_MS = TimeUnit.MILLISECONDS.toMillis(3500);
    private static final int MSG_MIN_SIZE = 10;
    private static final int MSG_MAX_SIZE = 34;

    protected static final byte[] HEADER = new byte[] { 0x02, 0x0A };
    private static final byte SET = 0x00;
    private static final byte GET = 0x01;
    protected static final byte OK = 0x01;

    private String address;
    private int port;
    private String community;

    private @Nullable Socket clientSocket;

    /**
     * Constructor
     *
     * @param address the IP address of the projector
     * @param port the TCP port to be used
     * @param community the community name of the equipment
     * @param model the projector model in use
     */
    public SonyProjectorSdcpConnector(String address, @Nullable Integer port, @Nullable String community,
            SonyProjectorModel model) {
        this(address, port, community, model, false);
    }

    /**
     * Constructor
     *
     * @param address the IP address of the projector
     * @param port the TCP port to be used
     * @param community the community name of the equipment
     * @param model the projector model in use
     * @param simu whether the communication is simulated or real
     */
    protected SonyProjectorSdcpConnector(String address, @Nullable Integer port, @Nullable String community,
            SonyProjectorModel model, boolean simu) {
        super(model, false);

        this.address = address;

        // init port
        if (port != null && port > 0) {
            this.port = port;
        } else {
            this.port = DEFAULT_PORT;
        }

        // init community
        if (community != null && !community.isEmpty() && community.length() == 4) {
            this.community = community;
        } else {
            this.community = DEFAULT_COMMUNITY;
        }
    }

    /**
     * Get the community name of the equipment
     *
     * @return the community name of the equipment
     */
    public String getCommunity() {
        return community;
    }

    @Override
    public synchronized void open() throws ConnectionException {
        if (!connected) {
            logger.debug("Opening SDCP connection IP {} port {}", this.address, this.port);
            try {
                Socket clientSocket = new Socket(this.address, this.port);
                clientSocket.setSoTimeout(200);

                dataOut = new DataOutputStream(clientSocket.getOutputStream());
                dataIn = new DataInputStream(clientSocket.getInputStream());

                this.clientSocket = clientSocket;

                connected = true;

                logger.debug("SDCP connection opened");
            } catch (IOException | SecurityException | IllegalArgumentException e) {
                throw new ConnectionException("@text/exception.opening-sdcp-connection-failed", e);
            }
        }
    }

    @Override
    public synchronized void close() {
        if (connected) {
            logger.debug("closing SDCP connection");
            super.close();
            Socket clientSocket = this.clientSocket;
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                }
                this.clientSocket = null;
            }
            connected = false;
        }
    }

    @Override
    protected byte[] buildMessage(SonyProjectorItem item, boolean getCommand, byte[] data) {
        byte[] communityData = community.getBytes();
        byte[] message = new byte[10 + data.length];
        message[0] = HEADER[0];
        message[1] = HEADER[1];
        message[2] = communityData[0];
        message[3] = communityData[1];
        message[4] = communityData[2];
        message[5] = communityData[3];
        message[6] = getCommand ? GET : SET;
        message[7] = item.getCode()[0];
        message[8] = item.getCode()[1];
        message[9] = getCommand ? 0 : (byte) data.length;
        if (!getCommand) {
            System.arraycopy(data, 0, message, 10, data.length);
        }
        return message;
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer.
     * In case of socket timeout, the returned value is 0.
     *
     * @param dataBuffer the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     * @throws CommunicationException if the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     */
    @Override
    protected int readInput(byte[] dataBuffer) throws CommunicationException {
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new CommunicationException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (SocketTimeoutException e) {
            return 0;
        } catch (IOException e) {
            logger.debug("readInput failed: {}", e.getMessage());
            throw new CommunicationException("readInput failed", e);
        }
    }

    @Override
    protected synchronized byte[] readResponse() throws CommunicationException {
        logger.debug("readResponse (timeout = {} ms)...", READ_TIMEOUT_MS);
        byte[] message = new byte[MSG_MAX_SIZE];
        boolean timeout = false;
        byte[] dataBuffer = new byte[MSG_MAX_SIZE];
        int count = 0;
        long startTimeRead = System.currentTimeMillis();
        while ((count < MSG_MIN_SIZE) && !timeout) {
            logger.trace("readResponse readInput...");
            int len = readInput(dataBuffer);
            logger.trace("readResponse readInput {} => {}", len, HexUtils.bytesToHex(dataBuffer));
            if (len > 0) {
                int oldCount = count;
                count = ((oldCount + len) > MSG_MAX_SIZE) ? MSG_MAX_SIZE : (oldCount + len);
                System.arraycopy(dataBuffer, 0, message, oldCount, count - oldCount);
            }
            timeout = (System.currentTimeMillis() - startTimeRead) > READ_TIMEOUT_MS;
        }
        if ((count < MSG_MIN_SIZE) && timeout) {
            logger.debug("readResponse timeout: only {} bytes read after {} ms", count, READ_TIMEOUT_MS);
            throw new CommunicationException("readResponse failed: timeout");
        }
        logger.debug("readResponse: {}", HexUtils.bytesToHex(message));
        if (count < MSG_MIN_SIZE) {
            logger.debug("readResponse: unexpected response data length: {}", count);
            throw new CommunicationException("Unexpected response data length");
        }
        return message;
    }

    @Override
    protected void validateResponse(byte[] responseMessage, SonyProjectorItem item) throws CommunicationException {
        // Check response size
        if (responseMessage.length < MSG_MIN_SIZE) {
            logger.debug("Unexpected response data length: {}", responseMessage.length);
            throw new CommunicationException("Unexpected response data length");
        }

        // Header should be a sony projector header
        byte[] headerMsg = Arrays.copyOf(responseMessage, HEADER.length);
        if (!Arrays.equals(headerMsg, HEADER)) {
            logger.debug("Unexpected HEADER in response: {} rather than {}", HexUtils.bytesToHex(headerMsg),
                    HexUtils.bytesToHex(HEADER));
            throw new CommunicationException("Unexpected HEADER in response");
        }

        // Community should be the same as used for sending
        byte[] communityResponseMsg = Arrays.copyOfRange(responseMessage, 2, 6);
        if (!Arrays.equals(communityResponseMsg, community.getBytes())) {
            logger.debug("Unexpected community in response: {} rather than {}",
                    HexUtils.bytesToHex(communityResponseMsg), HexUtils.bytesToHex(community.getBytes()));
            throw new CommunicationException("Unexpected community in response");
        }

        // Item number should be the same as used for sending
        byte[] itemResponseMsg = Arrays.copyOfRange(responseMessage, 7, 9);
        if (!Arrays.equals(itemResponseMsg, item.getCode())) {
            logger.debug("Unexpected item number in response: {} rather than {}", HexUtils.bytesToHex(itemResponseMsg),
                    HexUtils.bytesToHex(item.getCode()));
            throw new CommunicationException("Unexpected item number in response");
        }

        // Check response size
        int dataLength = responseMessage[9] & 0x000000FF;
        if (responseMessage.length < (10 + dataLength)) {
            logger.debug("Unexpected response data length: {}", dataLength);
            throw new CommunicationException("Unexpected response data length");
        }

        // byte 7 is expected to be 1, which indicates that the request was successful
        if (responseMessage[6] != OK) {
            String msg = "KO";
            if (dataLength == 12) {
                byte[] errorCode = Arrays.copyOfRange(responseMessage, 10, 12);
                try {
                    SonyProjectorSdcpError error = SonyProjectorSdcpError.getFromDataCode(errorCode);
                    msg = error.getMessage();
                } catch (CommunicationException e) {
                }
            }
            logger.debug("{} received in response", msg);
            throw new CommunicationException(msg + " received in response");
        }
    }

    @Override
    protected byte[] getResponseData(byte[] responseMessage) {
        // Data length is in 10th byte of message
        int dataLength = responseMessage[9] & 0x000000FF;
        if (dataLength > 0) {
            return Arrays.copyOfRange(responseMessage, 10, 10 + dataLength);
        } else {
            return new byte[] { (byte) 0xFF };
        }
    }

    /**
     * Request the model name
     *
     * @return the model name
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getModelName() throws SonyProjectorException {
        return new String(getSetting(SonyProjectorItem.MODEL_NAME), StandardCharsets.UTF_8);
    }
}
