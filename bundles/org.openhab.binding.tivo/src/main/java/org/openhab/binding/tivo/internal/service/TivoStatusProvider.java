/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tivo.internal.service;

import static org.openhab.binding.tivo.internal.TiVoBindingConstants.CONFIG_SOCKET_TIMEOUT_MS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tivo.internal.handler.TiVoHandler;
import org.openhab.binding.tivo.internal.service.TivoStatusData.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TivoStatusProvider class to maintain a connection out to the Tivo, monitor and process status messages returned..
 *
 * @author Jayson Kubilis - Initial contribution
 * @author Andrew Black - Updates / compilation corrections
 * @author Michael Lobstein - Updated for OH3
 */

@NonNullByDefault
public class TivoStatusProvider {
    private static final Pattern TIVO_STATUS_PATTERN = Pattern.compile("^CH_STATUS (\\d{4}) (?:(\\d{4}))?");
    private static final int TIMEOUT_SEC = 3000;

    private final Logger logger = LoggerFactory.getLogger(TivoStatusProvider.class);
    private @Nullable Socket tivoSocket = null;
    private @Nullable PrintStream streamWriter = null;
    private @Nullable StreamReader streamReader = null;
    private @Nullable TiVoHandler tivoHandler = null;
    private TivoStatusData tivoStatusData = new TivoStatusData();
    private TivoConfigData tivoConfigData = new TivoConfigData();
    private final String thingUid;

    /**
     * Instantiates a new TivoConfigStatusProvider.
     *
     * @param tivoConfigData {@link TivoConfigData} configuration data for the specific thing.
     * @param tivoHandler {@link TiVoHandler} parent handler object for the TivoConfigStatusProvider.
     *
     */

    public TivoStatusProvider(TivoConfigData tivoConfigData, TiVoHandler tivoHandler) {
        this.tivoStatusData = new TivoStatusData(false, -1, -1, false, "INITIALISING", false, ConnectionStatus.UNKNOWN);
        this.tivoConfigData = tivoConfigData;
        this.tivoHandler = tivoHandler;
        this.thingUid = tivoHandler.getThing().getUID().getAsString();
    }

    /**
     * {@link #statusRefresh()} initiates a connection to the TiVo. When a new connection is made and the TiVo is
     * online, the current channel is always returned. The connection is then closed (allows the socket to be used
     * by other devices).
     *
     * @throws InterruptedException
     */
    public void statusRefresh() throws InterruptedException {
        if (tivoStatusData.getConnectionStatus() != ConnectionStatus.INIT) {
            logger.debug(" statusRefresh '{}' - EXISTING status data - '{}'", tivoConfigData.getCfgIdentifier(),
                    tivoStatusData.toString());
        }

        // this will close the connection and re-open every 12 hours
        if (tivoConfigData.isKeepConnActive()) {
            connTivoDisconnect();
            doNappTime();
        }

        connTivoConnect();
        doNappTime();
        if (!tivoConfigData.isKeepConnActive()) {
            connTivoDisconnect();
        }
    }

    /**
     * {@link cmdTivoSend} sends a command to the Tivo.
     *
     * @param tivoCommand the complete command string (KEYWORD + PARAMETERS e.g. SETCH 102) to send.
     * @return {@link TivoStatusData} status data object, contains the result of the command.
     * @throws InterruptedException
     */
    public @Nullable TivoStatusData cmdTivoSend(String tivoCommand) throws InterruptedException {
        boolean connected = connTivoConnect();
        PrintStream streamWriter = this.streamWriter;

        if (!connected || streamWriter == null) {
            return new TivoStatusData(false, -1, -1, false, "CONNECTION FAILED", false, ConnectionStatus.OFFLINE);
        }
        logger.debug("TiVo '{}' - sending command: '{}'", tivoConfigData.getCfgIdentifier(), tivoCommand);
        int repeatCount = 1;
        // Handle special keyboard "repeat" commands
        if (tivoCommand.contains("*")) {
            repeatCount = Integer.parseInt(tivoCommand.substring(tivoCommand.indexOf("*") + 1));
            tivoCommand = tivoCommand.substring(0, tivoCommand.indexOf("*"));
            logger.debug("TiVo '{}' - repeating command: '{}' for '{}' times", tivoConfigData.getCfgIdentifier(),
                    tivoCommand, repeatCount);
        }
        for (int i = 1; i <= repeatCount; i++) {
            // Send the command
            streamWriter.println(tivoCommand + "\r");
            if (streamWriter.checkError()) {
                logger.debug("TiVo '{}' - called cmdTivoSend and encountered an IO error",
                        tivoConfigData.getCfgIdentifier());
                tivoStatusData = new TivoStatusData(false, -1, -1, false, "CONNECTION FAILED", false,
                        ConnectionStatus.OFFLINE);
                connTivoReconnect();
            }
        }
        return tivoStatusData;
    }

    /**
     * {@link statusParse} processes the {@link TivoStatusData} status message returned from the TiVo.
     *
     * For channel status messages form 'CH_STATUS channel reason' or 'CH_STATUS channel sub-channel reason' calls
     * {@link getParsedChannel} and returns the channel number (if a match is found in a valid formatted message).
     *
     * @param rawStatus string representing the message text returned by the TiVo
     * @return TivoStatusData object conditionally populated based upon the raw status message
     */
    private TivoStatusData statusParse(String rawStatus) {
        logger.debug(" statusParse '{}' - running on string '{}'", tivoConfigData.getCfgIdentifier(), rawStatus);

        if (rawStatus.contentEquals("COMMAND_TIMEOUT")) {
            // If connection status was UNKNOWN, COMMAND_TIMEOUT indicates the Tivo is alive, so update the status
            if (this.tivoStatusData.getConnectionStatus() == ConnectionStatus.UNKNOWN) {
                return new TivoStatusData(false, -1, -1, false, "COMMAND_TIMEOUT", false, ConnectionStatus.ONLINE);
            }
            // Otherwise ignore COMMAND_TIMEOUT, they occur a few seconds after each successful command, just return
            // existing status again
            return this.tivoStatusData;
        } else {
            switch (rawStatus) {
                case "":
                    return new TivoStatusData(false, -1, -1, false, "NO_STATUS_DATA_RETURNED", false,
                            tivoStatusData.getConnectionStatus());
                case "LIVETV_READY":
                    return new TivoStatusData(true, -1, -1, false, "LIVETV_READY", true, ConnectionStatus.ONLINE);
                case "CH_FAILED NO_LIVE":
                    return new TivoStatusData(false, -1, -1, false, "CH_FAILED NO_LIVE", true,
                            ConnectionStatus.STANDBY);
                case "CH_FAILED RECORDING":
                case "CH_FAILED MISSING_CHANNEL":
                case "CH_FAILED MALFORMED_CHANNEL":
                case "CH_FAILED INVALID_CHANNEL":
                    return new TivoStatusData(false, -1, -1, false, rawStatus, true, ConnectionStatus.ONLINE);
                case "INVALID_COMMAND":
                    return new TivoStatusData(false, -1, -1, false, "INVALID_COMMAND", false, ConnectionStatus.ONLINE);
                case "CONNECTION_RETRIES_EXHAUSTED":
                    return new TivoStatusData(false, -1, -1, false, "CONNECTION_RETRIES_EXHAUSTED", true,
                            ConnectionStatus.OFFLINE);
            }
        }

        // Only other documented status is in the form 'CH_STATUS channel reason' or
        // 'CH_STATUS channel sub-channel reason'
        Matcher matcher = TIVO_STATUS_PATTERN.matcher(rawStatus);
        int chNum = -1; // -1 used globally to indicate channel number error
        int subChNum = -1;
        boolean isRecording = false;

        if (matcher.find()) {
            logger.debug(" statusParse '{}' - groups '{}' with group count of '{}'", tivoConfigData.getCfgIdentifier(),
                    matcher.group(), matcher.groupCount());
            if (matcher.groupCount() == 1 || matcher.groupCount() == 2) {
                chNum = Integer.parseInt(matcher.group(1).trim());
                logger.debug(" statusParse '{}' - parsed channel '{}'", tivoConfigData.getCfgIdentifier(), chNum);
            }
            if (matcher.groupCount() == 2 && matcher.group(2) != null) {
                subChNum = Integer.parseInt(matcher.group(2).trim());
                logger.debug(" statusParse '{}' - parsed sub-channel '{}'", tivoConfigData.getCfgIdentifier(),
                        subChNum);
            }

            if (rawStatus.contains("RECORDING")) {
                isRecording = true;
            }

            rawStatus = rawStatus.replace(" REMOTE", "");
            rawStatus = rawStatus.replace(" LOCAL", "");
            return new TivoStatusData(true, chNum, subChNum, isRecording, rawStatus, true, ConnectionStatus.ONLINE);
        }
        logger.warn(" TiVo '{}' - Unhandled/unexpected status message: '{}'", tivoConfigData.getCfgIdentifier(),
                rawStatus);
        return new TivoStatusData(false, -1, -1, false, rawStatus, false, tivoStatusData.getConnectionStatus());
    }

    /**
     * {@link connIsConnected} returns the connection state of the Socket, streamWriter and streamReader objects.
     *
     * @return true = connection exists and all objects look OK, false = connection does not exist or a problem has
     *         occurred
     *
     */
    private boolean connIsConnected() {
        Socket tivoSocket = this.tivoSocket;
        PrintStream streamWriter = this.streamWriter;

        if (tivoSocket == null) {
            logger.debug(" connIsConnected '{}' - FALSE: tivoSocket=null", tivoConfigData.getCfgIdentifier());
            return false;
        } else if (!tivoSocket.isConnected()) {
            logger.debug(" connIsConnected '{}' - FALSE: tivoSocket.isConnected=false",
                    tivoConfigData.getCfgIdentifier());
            return false;
        } else if (tivoSocket.isClosed()) {
            logger.debug(" connIsConnected '{}' - FALSE: tivoSocket.isClosed=true", tivoConfigData.getCfgIdentifier());
            return false;
        } else if (streamWriter == null) {
            logger.debug(" connIsConnected '{}' - FALSE: tivoIOSendCommand=null", tivoConfigData.getCfgIdentifier());
            return false;
        } else if (streamWriter.checkError()) {
            logger.debug(" connIsConnected '{}' - FALSE: tivoIOSendCommand.checkError()=true",
                    tivoConfigData.getCfgIdentifier());
            return false;
        } else if (streamReader == null) {
            logger.debug(" connIsConnected '{}' - FALSE: streamReader=null", tivoConfigData.getCfgIdentifier());
            return false;
        }
        return true;
    }

    /**
     * {@link connTivoConnect} manages the creation / retry process of the socket connection.
     *
     * @return true = connected, false = not connected
     * @throws InterruptedException
     */
    public boolean connTivoConnect() throws InterruptedException {
        for (int iL = 1; iL <= tivoConfigData.getNumRetry(); iL++) {
            logger.debug(" connTivoConnect '{}' - starting connection process '{}' of '{}'.",
                    tivoConfigData.getCfgIdentifier(), iL, tivoConfigData.getNumRetry());

            // Sort out the socket connection
            if (connSocketConnect()) {
                logger.debug(" connTivoConnect '{}' - Socket created / connection made.",
                        tivoConfigData.getCfgIdentifier());
                StreamReader streamReader = this.streamReader;
                if (streamReader != null && streamReader.isAlive()) {
                    // Send a newline to poke the Tivo to verify it responds with INVALID_COMMAND/COMMAND_TIMEOUT
                    streamWriter.println("\r");
                    return true;
                }
            } else {
                logger.debug(" connTivoConnect '{}' - Socket creation failed.", tivoConfigData.getCfgIdentifier());
                TiVoHandler tivoHandler = this.tivoHandler;
                if (tivoHandler != null) {
                    tivoHandler.setStatusOffline();
                }
            }
            // Sleep and retry
            doNappTime();
        }
        return false;
    }

    /**
     * {@link connTivoReconnect} disconnect and reconnect the socket connection to the TiVo.
     *
     * @return boolean true = connection succeeded, false = connection failed
     * @throws InterruptedException
     */
    public boolean connTivoReconnect() throws InterruptedException {
        connTivoDisconnect();
        doNappTime();
        return connTivoConnect();
    }

    /**
     * {@link connTivoDisconnect} cleanly closes the socket connection and dependent objects
     *
     */
    public void connTivoDisconnect() throws InterruptedException {
        TiVoHandler tivoHandler = this.tivoHandler;
        StreamReader streamReader = this.streamReader;
        PrintStream streamWriter = this.streamWriter;
        Socket tivoSocket = this.tivoSocket;

        logger.debug(" connTivoSocket '{}' - requested to disconnect/cleanup connection objects",
                tivoConfigData.getCfgIdentifier());

        // if isCfgKeepConnOpen = false, don't set status to OFFLINE since the socket is closed after each command
        if (tivoHandler != null && tivoConfigData.isKeepConnActive()) {
            tivoHandler.setStatusOffline();
        }

        if (streamWriter != null) {
            streamWriter.close();
            this.streamWriter = null;
        }

        try {
            if (tivoSocket != null) {
                tivoSocket.close();
                this.tivoSocket = null;
            }
        } catch (IOException e) {
            logger.debug(" TiVo '{}' - I/O exception while disconnecting: '{}'.  Connection closed.",
                    tivoConfigData.getCfgIdentifier(), e.getMessage());
        }

        if (streamReader != null) {
            streamReader.interrupt();
            streamReader.join(TIMEOUT_SEC);
            this.streamReader = null;
        }
    }

    /**
     * {@link connSocketConnect} opens a Socket connection to the TiVo. Creates a {@link StreamReader} (Input)
     * thread to read the responses from the TiVo and a PrintStream (Output) {@link cmdTivoSend}
     * to send commands to the device.
     *
     * @param pConnect true = make a new connection , false = close existing connection
     * @return boolean true = connection succeeded, false = connection failed
     * @throws InterruptedException
     */
    private synchronized boolean connSocketConnect() throws InterruptedException {
        logger.debug(" connSocketConnect '{}' - attempting connection to host '{}', port '{}'",
                tivoConfigData.getCfgIdentifier(), tivoConfigData.getHost(), tivoConfigData.getTcpPort());

        if (connIsConnected()) {
            logger.debug(" connSocketConnect '{}' - already connected to host '{}', port '{}'",
                    tivoConfigData.getCfgIdentifier(), tivoConfigData.getHost(), tivoConfigData.getTcpPort());
            return true;
        } else {
            // something is wrong, so force a disconnect/clean up so we can try again
            connTivoDisconnect();
        }

        try {
            Socket tivoSocket = new Socket(tivoConfigData.getHost(), tivoConfigData.getTcpPort());
            tivoSocket.setKeepAlive(true);
            tivoSocket.setSoTimeout(CONFIG_SOCKET_TIMEOUT_MS);
            tivoSocket.setReuseAddress(true);

            if (tivoSocket.isConnected() && !tivoSocket.isClosed()) {
                if (streamWriter == null) {
                    streamWriter = new PrintStream(tivoSocket.getOutputStream(), false);
                }
                if (this.streamReader == null) {
                    StreamReader streamReader = new StreamReader(tivoSocket.getInputStream());
                    streamReader.start();
                    this.streamReader = streamReader;
                }
                this.tivoSocket = tivoSocket;
            } else {
                logger.debug(" connSocketConnect '{}' - socket creation failed to host '{}', port '{}'",
                        tivoConfigData.getCfgIdentifier(), tivoConfigData.getHost(), tivoConfigData.getTcpPort());
                return false;
            }

            return true;

        } catch (UnknownHostException e) {
            logger.debug(" TiVo '{}' - while connecting, unexpected host error: '{}'",
                    tivoConfigData.getCfgIdentifier(), e.getMessage());
        } catch (IOException e) {
            if (tivoStatusData.getConnectionStatus() != ConnectionStatus.OFFLINE) {
                logger.debug(" TiVo '{}' - I/O exception while connecting: '{}'", tivoConfigData.getCfgIdentifier(),
                        e.getMessage());
            }
        }
        return false;
    }

    /**
     * {@link doNappTime} sleeps for the period specified by the getCmdWaitInterval parameter. Primarily used to allow
     * the TiVo time to process responses after a command is issued.
     *
     * @throws InterruptedException
     */
    public void doNappTime() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(tivoConfigData.getCmdWaitInterval());
    }

    public TivoStatusData getServiceStatus() {
        return tivoStatusData;
    }

    public void setServiceStatus(TivoStatusData tivoStatusData) {
        this.tivoStatusData = tivoStatusData;
    }

    /**
     * {@link StreamReader} data stream reader that reads the status data returned from the TiVo.
     *
     */
    public class StreamReader extends Thread {
        private @Nullable BufferedReader bufferedReader = null;

        /**
         * {@link StreamReader} construct a data stream reader that reads the status data returned from the TiVo via a
         * BufferedReader.
         *
         * @param inputStream socket input stream.
         */
        public StreamReader(InputStream inputStream) {
            this.setName("OH-binding-" + thingUid + "-" + tivoConfigData.getHost() + ":" + tivoConfigData.getTcpPort());
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                logger.debug("streamReader {} is running. ", tivoConfigData.getCfgIdentifier());
                while (!Thread.currentThread().isInterrupted()) {
                    String receivedData = null;
                    BufferedReader reader = bufferedReader;
                    if (reader == null) {
                        throw new IOException("streamReader failed: input stream is null");
                    }

                    try {
                        receivedData = reader.readLine();
                    } catch (SocketTimeoutException | SocketException e) {
                        // Do nothing. Just allow the thread to check if it has to stop.
                    }

                    if (receivedData != null) {
                        logger.debug("TiVo {} data received: {}", tivoConfigData.getCfgIdentifier(), receivedData);
                        TivoStatusData commandResult = statusParse(receivedData);
                        TiVoHandler handler = tivoHandler;
                        if (handler != null) {
                            handler.updateTivoStatus(tivoStatusData, commandResult);
                        }
                        tivoStatusData = commandResult;
                    }
                }

            } catch (IOException e) {
                closeBufferedReader();
                logger.debug("TiVo {} is disconnected. ", tivoConfigData.getCfgIdentifier(), e);
            }
            closeBufferedReader();
            logger.debug("streamReader {} is stopped. ", tivoConfigData.getCfgIdentifier());
        }

        private void closeBufferedReader() {
            BufferedReader reader = bufferedReader;
            if (reader != null) {
                try {
                    reader.close();
                    this.bufferedReader = null;
                } catch (IOException e) {
                    logger.debug("Error closing bufferedReader: {}", e.getMessage(), e);
                }
            }
        }
    }
}
