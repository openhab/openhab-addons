/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.connection;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.cloud.CloudProvider;
import org.openhab.binding.mideaac.internal.connection.exception.MideaAuthenticationException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaConnectionException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaException;
import org.openhab.binding.mideaac.internal.handler.Callback;
import org.openhab.binding.mideaac.internal.handler.CommandBase;
import org.openhab.binding.mideaac.internal.handler.CommandSet;
import org.openhab.binding.mideaac.internal.handler.EnergyResponse;
import org.openhab.binding.mideaac.internal.handler.Packet;
import org.openhab.binding.mideaac.internal.handler.Response;
import org.openhab.binding.mideaac.internal.handler.capabilities.CapabilitiesResponse;
import org.openhab.binding.mideaac.internal.security.Decryption8370Result;
import org.openhab.binding.mideaac.internal.security.Security;
import org.openhab.binding.mideaac.internal.security.Security.MsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ConnectionManager} class is responsible for managing the state of the TCP connection to the
 * indoor AC unit evaporator.
 *
 * @author Jacek Dobrowolski - Initial Contribution
 * @author Bob Eckhoff - Revised logic to reconnect with security before each poll or command
 * 
 *         This gets around the issue that any command needs to be within 30 seconds of the authorization
 *         in testing this only adds 50 ms, but allows Scheduled polls at longer intervals.
 */
@NonNullByDefault
public class ConnectionManager {
    private Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private final String ipAddress;
    private final int ipPort;
    private final int timeout;
    private String key;
    private String token;
    private final String cloud;
    private final String deviceId;
    private Response lastResponse;
    private CloudProvider cloudProvider;
    private Security security;
    private final int version;
    private final boolean promptTone;
    private boolean deviceIsConnected;
    private int droppedCommands = 0;

    /**
     * True allows command resend if null and timeout response
     */
    private boolean resend = true;

    /**
     * Connection manager configuration
     * 
     * @param ipAddress Device IP
     * @param ipPort Device Port
     * @param timeout Socket timeout
     * @param key Security key V.3
     * @param token Security token V.3
     * @param cloud Cloud Provider
     * @param email Cloud Provider login email
     * @param password Cloud Provider login password
     * @param deviceId Device ID
     * @param version Device version
     * @param promptTone Tone after command true or false
     */
    public ConnectionManager(String ipAddress, int ipPort, int timeout, String key, String token, String cloud,
            String email, String password, String deviceId, int version, boolean promptTone) {
        this.deviceIsConnected = false;
        this.ipAddress = ipAddress;
        this.ipPort = ipPort;
        this.timeout = timeout;
        this.key = key;
        this.token = token;
        this.cloud = cloud;
        this.deviceId = deviceId;
        this.version = version;
        this.promptTone = promptTone;
        this.lastResponse = new Response(HexFormat.of().parseHex("C00042667F7F003C0000046066000000000000000000F9ECDB"),
                version);
        this.cloudProvider = CloudProvider.getCloudProvider(cloud);
        this.security = new Security(cloudProvider);
    }

    private Socket socket = new Socket();
    private InputStream inputStream = new ByteArrayInputStream(new byte[0]);
    private DataOutputStream writer = new DataOutputStream(System.out);

    /**
     * Gets last response
     * 
     * @return byte array of last response
     */
    public Response getLastResponse() {
        return this.lastResponse;
    }

    /**
     * The socket is established with the writer and inputStream (for reading responses)
     * V2 devices will proceed to send the poll or the set command.
     * V3 devices will proceed to authenticate
     * 
     * @throws MideaConnectionException
     * @throws MideaAuthenticationException
     * @throws SocketTimeoutException
     * @throws IOException
     */
    public synchronized void connect()
            throws MideaConnectionException, MideaAuthenticationException, SocketTimeoutException, IOException {
        logger.trace("Connecting to {}:{}", ipAddress, ipPort);

        int maxTries = 3;
        int retrySocket = 0;

        // If resending command add delay to avoid connection rejection
        // Suspect that the AC device needs a few seconds to clear.
        if (!resend) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                logger.debug("An interupted error (resend command delay-connect) has occured {}", ex.getMessage());
            }
        }

        // Open socket
        // RetrySocket addresses the Timeout exception only, others exceptions end the thread. Same as HA python version
        while (retrySocket < maxTries) {
            try {
                socket = new Socket();
                socket.setSoTimeout(timeout * 1000);
                socket.connect(new InetSocketAddress(ipAddress, ipPort), timeout * 1000);
                break;
            } catch (SocketTimeoutException e) {
                retrySocket++;
                if (retrySocket < maxTries) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        logger.debug("An interupted error (socket retry) has occured {}", ex.getMessage());
                    }
                    logger.debug("Socket retry count {}, Socket timeout connecting to {}: {}", retrySocket, ipAddress,
                            e.getMessage());
                }
            } catch (IOException e) {
                logger.debug("Socket retry count {}, IOException connecting to {}: {}", retrySocket, ipAddress,
                        e.getMessage());
                throw new MideaConnectionException(e);
            }
        }
        if (retrySocket == maxTries) {
            deviceIsConnected = false;
            logger.debug("Failed to connect after {} tries. Try again with next scheduled poll", maxTries);
            throw new MideaConnectionException("Failed to connect after maximum tries");
        }

        // Create streams
        try {
            writer = new DataOutputStream(socket.getOutputStream());
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            logger.debug("IOException getting streams for {}: {}", ipAddress, e.getMessage(), e);
            deviceIsConnected = false;
            throw new MideaConnectionException(e);
        }

        if (version == 3) {
            logger.debug("Device at IP: {} requires authentication, going to authenticate", ipAddress);
            try {
                authenticate();
            } catch (MideaAuthenticationException | MideaConnectionException e) {
                deviceIsConnected = false;
                throw e;
            }
        }

        if (!deviceIsConnected) {
            // Info logger on first connection after being disconnected
            logger.info("Connected to IP {}", ipAddress);
        } else {
            logger.debug("Connected to IP {}", ipAddress);
        }
        deviceIsConnected = true;
    }

    /**
     * For V3 devices only. This method checks for the Cloud Provider
     * key and token (and goes offline if any are missing). It will retrieve the
     * missing key and/or token if the account email and password are provided.
     * 
     * @throws MideaAuthenticationException
     * @throws MideaConnectionException
     */
    public void authenticate() throws MideaConnectionException, MideaAuthenticationException {
        logger.trace("Key: {}", key);
        logger.trace("Token: {}", token);
        logger.trace("Cloud {}", cloud);

        if (!token.isBlank() && !key.isBlank() && !cloud.isBlank()) {
            logger.debug("Device at IP: {} authenticating", ipAddress);
            doV3Handshake();
        } else {
            throw new MideaAuthenticationException("Token, Key and / or cloud provider missing");
        }
    }

    /**
     * Sends the Handshake Request to the V3 device. Generally quick response.
     * After success, but without the 1000 ms sleep delay there are problems.
     * Suspect that the device needs a moment to clear before the Poll.
     */
    private void doV3Handshake() throws MideaConnectionException, MideaAuthenticationException {
        byte[] request = security.encode8370(Utils.hexStringToByteArray(token), MsgType.MSGTYPE_HANDSHAKE_REQUEST);
        try {
            logger.trace("Device at IP: {} writing handshake_request: {}", ipAddress, Utils.bytesToHex(request));

            write(request);
            byte[] response = read();

            if (response != null && response.length > 0) {
                logger.trace("Device at IP: {} response for handshake_request length:{}", ipAddress, response.length);
                if (response.length == 72) {
                    boolean success = security.tcpKey(Arrays.copyOfRange(response, 8, 72),
                            Utils.hexStringToByteArray(key));
                    if (success) {
                        logger.debug("Authentication successful");
                        // Altering the sleep can cause write errors problems. Use caution.
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            logger.debug("An interupted error (success) has occured {}", e.getMessage());
                        }
                    } else {
                        throw new MideaAuthenticationException("Invalid Key. Correct Key in configuration.");
                    }
                } else if (Arrays.equals(new String("ERROR").getBytes(), response)) {
                    throw new MideaAuthenticationException("Authentication failed!");
                } else {
                    logger.debug("Authentication reponse unexpected data length ({} instead of 72)!", response.length);
                    throw new MideaAuthenticationException("Unexpected authentication response length");
                }
            }
        } catch (IOException e) {
            throw new MideaConnectionException(e);
        }
    }

    /**
     * Sends the routine polling command from the DoPoll
     * in the MideaACHandler
     * 
     * @param callback
     * @throws MideaConnectionException
     * @throws MideaAuthenticationException
     * @throws MideaException
     */
    public void getStatus(Callback callback)
            throws MideaConnectionException, MideaAuthenticationException, MideaException, IOException {
        CommandBase requestStatusCommand = new CommandBase();
        sendCommand(requestStatusCommand, callback);
    }

    private void ensureConnected() throws MideaConnectionException, MideaAuthenticationException, IOException {
        disconnect();
        connect();
    }

    /**
     * Pulls the packet byte array together. There is a check to
     * make sure to make sure the input stream is empty before sending
     * the new command and another check if input stream is empty after 1.5 seconds.
     * Normal device response in 0.75 - 1 second range
     * If still empty, send the bytes again. If the socket times out with no bytes read()
     * one resend of the command will be sent. A second failure the command is dropped.
     * The scheduler will still send the next poll.
     * 
     * @param command either the set or polling command
     * @param callback communication with the MideaACHandler to update channel status
     * @throws MideaConnectionException
     * @throws MideaAuthenticationException
     * @throws MideaException
     * @throws IOException
     */
    public synchronized void sendCommand(CommandBase command, @Nullable Callback callback)
            throws MideaConnectionException, MideaAuthenticationException, MideaException, IOException {
        ensureConnected();

        if (command instanceof CommandSet) {
            ((CommandSet) command).setPromptTone(promptTone);
        }
        Packet packet = new Packet(command, deviceId, security);
        packet.compose();

        try {
            byte[] bytes = packet.getBytes();
            logger.debug("Writing to {} bytes.length: {}", ipAddress, bytes.length);

            if (version == 3) {
                bytes = security.encode8370(bytes, MsgType.MSGTYPE_ENCRYPTED_REQUEST);
            }

            // Ensure input stream is empty before writing packet
            if (inputStream.available() == 0) {
                logger.debug("Input stream empty sending write {}", command);
                write(bytes);
            }

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                logger.debug("An interupted error (write command2) has occured {}", e.getMessage());
                Thread.currentThread().interrupt();
                // Note, but continue anyway for second write if needed.
            }

            // Input stream is checked after 1.5 seconds
            // Socket timeout (UI parameter) 2 seconds minimum.
            if (inputStream.available() == 0) {
                logger.debug("Input stream empty sending second write {}", command);
                write(bytes);
            }

            byte[] responseBytes = read();

            if (responseBytes != null) {
                resend = true;
                byte[] data = null;
                if (version == 3) {
                    Decryption8370Result result = security.decode8370(responseBytes);
                    for (byte[] response : result.getResponses()) {
                        logger.debug("Response length: {} IP address: {} ", response.length, ipAddress);
                        if (response.length > 40 + 16) {
                            data = security.aesDecrypt(Arrays.copyOfRange(response, 40, response.length - 16));
                            logger.trace("Bytes in HEX, decoded and with header: length: {}, data: {}", data.length,
                                    Utils.bytesToHex(data));
                        }
                    }
                } else if (version == 2) {
                    if (responseBytes.length > 40 + 16) {
                        data = security.aesDecrypt(Arrays.copyOfRange(responseBytes, 40, responseBytes.length - 16));
                        logger.trace("V2 Bytes decoded with header: length: {}, data: {}", data.length,
                                Utils.bytesToHex(data));
                    }
                }
                // The response data from the appliance includes a packet header which we don't want
                if (data != null && data.length > 10) {
                    data = Arrays.copyOfRange(data, 10, data.length);
                    byte bodyType = data[0x0];
                    logger.trace("Response bodyType: {}", bodyType);
                    logger.trace("Bytes in HEX, decoded and stripped without header: length: {}, data: {}", data.length,
                            Utils.bytesToHex(data));
                    logger.debug("Bytes in BINARY, decoded and stripped without header: length: {}, data: {}",
                            data.length, Utils.bytesToBinary(data));

                    // Handle the capabilities response
                    if (bodyType == (byte) 0xB5) {
                        logger.debug("Capabilities response detected with bodyType 0xB5.");
                        CapabilitiesResponse capabilitiesResponse = new CapabilitiesResponse(data);
                        if (callback != null) {
                            callback.updateChannels(capabilitiesResponse);
                        }
                        return;
                    }

                    // Handle the Energy Response
                    if (bodyType == (byte) 0xC1) {
                        logger.debug("Energy response detected with bodyType 0xC1.");
                        EnergyResponse energyUpdate = new EnergyResponse(data);
                        if (callback != null) {
                            callback.updateChannels(energyUpdate);
                        }
                        return;
                    }

                    // Handle the poll response
                    if (data.length < 21) {
                        logger.warn("Response data is {} long, minimum is 21!", data.length);
                        return;
                    }
                    if (bodyType != -64) {
                        if (bodyType == 30) {
                            logger.warn("Error response 0x1E received {} from IP Address:{}", bodyType, ipAddress);
                            return;
                        }
                        logger.warn("Unexpected response bodyType {}", bodyType);
                        return;
                    }
                    lastResponse = new Response(data, version);
                    try {
                        logger.trace("Data length is {}, version is {}, IP address is {}", data.length, version,
                                ipAddress);
                        if (callback != null) {
                            callback.updateChannels(lastResponse);
                        }
                    } catch (Exception ex) {
                        logger.debug("Processing response exception: {}", ex.getMessage());
                        throw new MideaException(ex);
                    }
                    return;
                } else {
                    logger.warn("Decryption failed or insufficient data length to strike header");
                }
                return;
            } else {
                if (resend) {
                    logger.debug("Resending Command {}", command);
                    resend = false;
                    sendCommand(command, callback);
                } else {
                    droppedCommands = droppedCommands + 1;
                    logger.debug("Problem with reading response, skipping {} skipped count since startup {}", command,
                            droppedCommands);
                    resend = true;
                    return;
                }
            }
        } catch (SocketException e) {
            droppedCommands = droppedCommands + 1;
            logger.debug("Socket exception on IP: {}, skipping command {} skipped count since startup {}", ipAddress,
                    command, droppedCommands);
            throw new MideaConnectionException(e);
        } catch (IOException e) {
            droppedCommands = droppedCommands + 1;
            logger.debug("IO exception on IP: {}, skipping command {} skipped count since startup {}", ipAddress,
                    command, droppedCommands);
            throw new MideaConnectionException(e);
        }
    }

    /**
     * Closes all elements of the connection before starting a new one
     * Makes sure writer, inputStream and socket are closed before each command is started
     */
    public synchronized void disconnect() {
        logger.debug("Disconnecting from device at {}", ipAddress);

        InputStream inputStream = this.inputStream;
        DataOutputStream writer = this.writer;
        Socket socket = this.socket;
        try {
            writer.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            logger.warn("IOException closing connection to device at {}: {}", ipAddress, e.getMessage(), e);
        }
        socket = null;
        inputStream = null;
        writer = null;
    }

    /**
     * Reads the inputStream byte array (Handshake or command)
     * 
     * @return byte array or null
     */
    public synchronized byte @Nullable [] read() {
        byte[] bytes = new byte[512];
        InputStream inputStream = this.inputStream;

        try {
            int len = inputStream.read(bytes);
            if (len > 0) {
                logger.debug("Response received length: {} from device at IP: {}", len, ipAddress);
                bytes = Arrays.copyOfRange(bytes, 0, len);
                return bytes;
            }
        } catch (IOException e) {
            String message = e.getMessage();
            logger.debug("Byte read exception {}", message);
        }
        return null;
    }

    /**
     * Writes the packet that will be sent to the device
     * 
     * @param buffer socket writer
     * @throws IOException writer could be null
     */
    public synchronized void write(byte[] buffer) throws IOException {
        DataOutputStream writer = this.writer;

        try {
            writer.write(buffer, 0, buffer.length);
        } catch (IOException e) {
            String message = e.getMessage();
            logger.debug("Write error {}", message);
        }
    }

    /**
     * Disconnects from the AC device
     * 
     * @param force true or false
     */
    public void dispose(boolean force) {
        disconnect();
    }
}
