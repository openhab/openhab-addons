/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.tivo.internal.service;

import static org.openhab.binding.tivo.TiVoBindingConstants.CONFIG_SOCKET_TIMEOUT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.tivo.handler.TiVoHandler;
import org.openhab.binding.tivo.internal.service.TivoStatusData.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TivoStatusProvider class to maintain a connection out to the Tivo, monitor and process status messages returned..
 *
 * @author Jayson Kubilis - Initial contribution
 * @author Andrew Black - Updates / compilation corrections
 */

public class TivoStatusProvider {

    private Socket tivoSocket = null;
    private PrintStream streamWriter = null;
    public StreamReader streamReader = null;
    private TivoStatusData tivoStatusData = null;
    private TivoConfigData tivoConfigData = null;
    private TiVoHandler tivoHandler = null;
    private final Logger logger = LoggerFactory.getLogger(TivoStatusProvider.class);

    private static final Integer READ_TIMEOUT = 1000;

    /**
     * Instantiates a new TivoConfigStatusProvider.
     *
     * @param tivoConfigData {@link TivoConfigData} configuration data for the specific thing.
     * @param tivoStatusData {@link TivoStatusData} status data for the specific thing.
     * @param tivoHandler {@link TivoHandler} parent handler object for the TivoConfigStatusProvider.
     *
     */

    public TivoStatusProvider(TivoConfigData tivoConfigData, TiVoHandler tivoHandler) {
        this.tivoStatusData = new TivoStatusData(false, -1, "INITIALISING", false, ConnectionStatus.UNKNOWN);
        this.tivoConfigData = tivoConfigData;
        this.tivoHandler = tivoHandler;
    }

    /**
     * {@link statusRefresh} initiates a connection to the TiVo. When a new connection is made and the TiVo is online,
     * the current channel is always returned. The connection is then closed (allows the socket to be used by other
     * devices).
     *
     * * @return {@link TivoStatusData} object
     */
    public void statusRefresh() {
        if (tivoStatusData != null) {
            logger.debug(" statusRefresh '{}' - EXISTING status data - '{}'", tivoConfigData.getCfgIdentifier(),
                    tivoStatusData.toString());
        }
        connTivoConnect();
        doNappTime();
        connTivoDisconnect(false);
    }

    /**
     * {@link cmdTivoSend} sends a command to the Tivo.
     *
     * @param tivoCommand the complete command string (KEYWORD + PARAMETERS e.g. SETCH 102) to send.
     * @return {@link TivoStatusData} status data object, contains the result of the command.
     */
    public TivoStatusData cmdTivoSend(String tivoCommand) {
        if (!connTivoConnect()) {
            return new TivoStatusData(false, -1, "CONNECTION FAILED", false, ConnectionStatus.OFFLINE);
        }
        logger.info("TiVo '{}' - sending command: '{}'", tivoConfigData.getCfgIdentifier(), tivoCommand);
        int repeatCount = 1;
        // Handle special keyboard "repeat" commands
        if (tivoCommand.contains("*")) {
            repeatCount = Integer.parseInt(tivoCommand.substring(tivoCommand.indexOf("*") + 1));
            tivoCommand = tivoCommand.substring(0, tivoCommand.indexOf("*"));
            logger.info("TiVo '{}' - repeating command: '{}' for '{}' times", tivoConfigData.getCfgIdentifier(),
                    tivoCommand, repeatCount);
        }
        for (int i = 1; i <= repeatCount; i++) {
            // Send the command
            streamWriter.println(tivoCommand.toString() + "\r");
            if (streamWriter.checkError()) {
                logger.error("TiVo '{}' - called cmdTivoSend and encountered an IO error",
                        tivoConfigData.getCfgIdentifier(), tivoSocket.isConnected(), tivoSocket.isClosed());
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

        if (rawStatus == null) {
            return new TivoStatusData(false, -1, "NO_STATUS_DATA_RETURNED", false,
                    tivoStatusData.getConnectionStatus());
        } else if (rawStatus.contentEquals("COMMAND_TIMEOUT")) {
            return new TivoStatusData(false, -1, "COMMAND_TIMEOUT", false, tivoStatusData.getConnectionStatus());
        } else {
            switch (rawStatus) {
                case "":
                    return new TivoStatusData(false, -1, "NO_STATUS_DATA_RETURNED", false,
                            tivoStatusData.getConnectionStatus());
                case "LIVETV_READY":
                    return new TivoStatusData(true, -1, "LIVETV_READY", true, ConnectionStatus.ONLINE);
                case "CH_FAILED NO_LIVE":
                    return new TivoStatusData(false, -1, "CH_FAILED NO_LIVE", true, ConnectionStatus.STANDBY);
                case "CH_FAILED RECORDING":
                    return new TivoStatusData(false, -1, "CH_FAILED RECORDING", true, ConnectionStatus.ONLINE);
                case "CH_FAILED MISSING_CHANNEL":
                    return new TivoStatusData(false, -1, "CH_FAILED MISSING_CHANNEL", true, ConnectionStatus.ONLINE);
                case "CH_FAILED MALFORMED_CHANNEL":
                    return new TivoStatusData(false, -1, "CH_FAILED MALFORMED_CHANNEL", true, ConnectionStatus.ONLINE);
                case "CH_FAILED INVALID_CHANNEL":
                    return new TivoStatusData(false, -1, "CH_FAILED INVALID_CHANNEL", true, ConnectionStatus.ONLINE);
                case "INVALID_COMMAND":
                    return new TivoStatusData(false, -1, "INVALID_COMMAND", false, ConnectionStatus.ONLINE);
                case "CONNECTION_RETRIES_EXHAUSTED":
                    return new TivoStatusData(false, -1, "CONNECTION_RETRIES_EXHAUSTED", true,
                            ConnectionStatus.OFFLINE);
            }
        }

        // Only other documented status is in the form 'CH_STATUS channel reason' or
        // 'CH_STATUS channel sub-channel reason'
        Pattern tivoStatusPattern = Pattern.compile("[0]+(\\d+)\\s+");
        Matcher matcher = tivoStatusPattern.matcher(rawStatus);
        Integer chNum = -1; // -1 used globally to indicate channel number error

        if (matcher.find()) {
            logger.debug(" statusParse '{}' - groups '{}' with group count of '{}'", tivoConfigData.getCfgIdentifier(),
                    matcher.group(), matcher.groupCount());
            if (matcher.groupCount() == 1 | matcher.groupCount() == 2) {
                chNum = new Integer(Integer.parseInt(matcher.group(1).trim()));
            }
            logger.debug(" statusParse '{}' - parsed channel '{}'", tivoConfigData.getCfgIdentifier(), chNum);
            rawStatus = rawStatus.replace(" REMOTE", "");
            rawStatus = rawStatus.replace(" LOCAL", "");
            return new TivoStatusData(true, chNum, rawStatus, true, ConnectionStatus.ONLINE);
        }
        logger.warn(" TiVo '{}' - Unhandled/unexpected status message: '{}'", tivoConfigData.getCfgIdentifier(),
                rawStatus);
        return new TivoStatusData(false, -1, rawStatus, false, tivoStatusData.getConnectionStatus());
    }

    /**
     * {@link connIsConnected} returns the connection state of the Socket, streamWriter and streamReader objects.
     *
     * @return true = connection exists and all objects look OK, false = connection does not exist or a problem has
     *         occurred
     *
     */

    private boolean connIsConnected() {
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
     */

    public boolean connTivoConnect() {
        for (int iL = 1; iL <= tivoConfigData.getCfgNumConnRetry(); iL++) {
            logger.debug(" connTivoConnect '{}' - starting connection process '{}' of '{}'.",
                    tivoConfigData.getCfgIdentifier(), iL, tivoConfigData.getCfgNumConnRetry());

            // Sort out the socket connection
            if (connSocketConnect()) {
                logger.debug(" connTivoConnect '{}' - Socket created / connection made.",
                        tivoConfigData.getCfgIdentifier());
                if (streamReader.isAlive()) {
                    return true;
                }
            } else {
                logger.debug(" connTivoConnect '{}' - Socket creation failed.", tivoConfigData.getCfgIdentifier());
            }
            // Sleep and retry
            doNappTime();
        }
        return false;
    }

    /**
     * {@link connTivoDisconnect} conditionally closes the Socket connection. When 'keep connection open' or 'channel
     * scanning' is true, the disconnection process is ignored. Disconnect can be forced by setting forceDisconnect to
     * true.
     *
     * @param forceDisconnect true = forces a disconnection , false = disconnects in specific situations
     */

    public void connTivoDisconnect(boolean forceDisconnect) {
        if (forceDisconnect) {
            connSocketDisconnect();
        } else {
            if (!tivoConfigData.isCfgKeepConnOpen() && !tivoConfigData.doChannelScan()) {
                doNappTime();
                connSocketDisconnect();
            }
        }
    }

    /**
     * {@link connTivoReconnect} disconnect and reconnect the socket connection to the TiVo.
     *
     * @return boolean true = connection succeeded, false = connection failed
     */

    public boolean connTivoReconnect() {
        connSocketDisconnect();
        doNappTime();
        return connTivoConnect();
    }

    /**
     * {@link connSocketDisconnect} cleanly closes the socket connection and dependent objects
     *
     */
    private void connSocketDisconnect() {
        logger.debug(" connTivoSocket '{}' - requested to disconnect/cleanup connection objects",
                tivoConfigData.getCfgIdentifier());
        try {
            if (streamReader != null) {
                while (streamReader.isAlive()) {
                    streamReader.stopReader();
                    // doNappTime();
                }
                streamReader = null;
            }
            if (streamWriter != null) {
                streamWriter.close();
                streamWriter = null;
            }
            if (tivoSocket != null) {
                tivoSocket.close();
                tivoSocket = null;
            }

        } catch (IOException e) {
            logger.error(" TiVo '{}' - I/O exception while disconnecting: '{}'.  Connection closed.",
                    tivoConfigData.getCfgIdentifier(), e.getMessage());
        }
    }

    /**
     * {@link connSocketConnect} opens a Socket connection to the TiVo. Creates a {@link StreamReader} (Input)
     * thread to read the responses from the TiVo and a PrintStream (Output) {@link cmdTivoSend}
     * to send commands to the device.
     *
     * @param pConnect true = make a new connection , false = close existing connection
     * @return boolean true = connection succeeded, false = connection failed
     */
    private synchronized boolean connSocketConnect() {
        logger.debug(" connSocketConnect '{}' - attempting connection to host '{}', port '{}'",
                tivoConfigData.getCfgIdentifier(), tivoConfigData.getCfgHost(), tivoConfigData.getCfgTcpPort());

        if (connIsConnected()) {
            logger.debug(" connSocketConnect '{}' - already connected to host '{}', port '{}'",
                    tivoConfigData.getCfgIdentifier(), tivoConfigData.getCfgHost(), tivoConfigData.getCfgTcpPort());
            return true;
        } else {
            // something is wrong, so force a disconnect/clean up so we can try again
            connTivoDisconnect(true);
        }

        try {

            tivoSocket = new Socket(tivoConfigData.getCfgHost(), tivoConfigData.getCfgTcpPort());
            tivoSocket.setKeepAlive(true);
            tivoSocket.setSoTimeout(CONFIG_SOCKET_TIMEOUT);
            tivoSocket.setReuseAddress(true);

            if (tivoSocket.isConnected() && !tivoSocket.isClosed()) {
                if (streamWriter == null) {
                    streamWriter = new PrintStream(tivoSocket.getOutputStream(), false);
                }
                if (streamReader == null) {
                    streamReader = new StreamReader(tivoSocket.getInputStream());
                    streamReader.start();
                }
            } else {
                logger.error(" connSocketConnect '{}' - socket creation failed to host '{}', port '{}'",
                        tivoConfigData.getCfgIdentifier(), tivoConfigData.getCfgHost(), tivoConfigData.getCfgTcpPort());
                return false;
            }

            return true;

        } catch (UnknownHostException e) {
            logger.error(" TiVo '{}' - while connecting, unexpected host error: '{}'",
                    tivoConfigData.getCfgIdentifier(), e.getMessage());
        } catch (IOException e) {
            if (tivoStatusData.getConnectionStatus() != ConnectionStatus.OFFLINE) {
                logger.error(" TiVo '{}' - I/O exception while connecting: '{}'", tivoConfigData.getCfgIdentifier(),
                        e.getMessage());
            }
        }
        return false;
    }

    /**
     * {@link doNappTime} sleeps for the period specified by the getCfgCmdWait parameter. Primarily used to allow the
     * TiVo time to process responses after a command is issued.
     */
    public void doNappTime() {
        try {
            logger.debug(" doNappTime '{}' - I feel like napping for '{}' milliseconds",
                    tivoConfigData.getCfgIdentifier(), tivoConfigData.getCfgCmdWait());
            TimeUnit.MILLISECONDS.sleep(tivoConfigData.getCfgCmdWait());
        } catch (Exception e) {
        }
    }

    public TivoStatusData getServiceStatus() {
        return tivoStatusData;
    }

    public void setServiceStatus(TivoStatusData tivoStatusData) {
        this.tivoStatusData = tivoStatusData;
    }

    public void setChScan(boolean chScan) {
        this.tivoStatusData.setChScan(chScan);
    }

    /**
     * {@link StreamReader} data stream reader that reads the status data returned from the TiVo.
     *
     */
    public class StreamReader extends Thread {
        private BufferedReader bufferedReader = null;
        private volatile boolean stopReader;

        private CountDownLatch stopLatch;

        /**
         * {@link StreamReader} construct a data stream reader that reads the status data returned from the TiVo via a
         * BufferedReader.
         *
         * @param inputStream socket input stream.
         * @throws IOException
         */
        public StreamReader(InputStream inputStream) {
            this.setName("tivoStreamReader-" + tivoConfigData.getCfgIdentifier());
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            this.stopLatch = new CountDownLatch(1);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                logger.debug("streamReader {} is running. ", tivoConfigData.getCfgIdentifier());
                while (!stopReader && !Thread.currentThread().isInterrupted()) {

                    String receivedData = null;
                    try {
                        receivedData = bufferedReader.readLine();
                    } catch (SocketTimeoutException e) {
                        // Do nothing. Just allow the thread to check if it has to stop.
                    }

                    if (receivedData != null) {
                        logger.debug("TiVo {} data received: {}", tivoConfigData.getCfgIdentifier(), receivedData);
                        TivoStatusData commandResult = statusParse(receivedData);
                        tivoHandler.updateTivoStatus(tivoStatusData, commandResult);
                        tivoStatusData = commandResult;
                        // }
                    }
                }

            } catch (IOException e) {
                logger.warn("TiVo {} is disconnected. ", tivoConfigData.getCfgIdentifier(), e);
            }
            // Notify the stopReader method caller that the reader is stopped.
            this.stopLatch.countDown();
            logger.debug("streamReader {} is stopped. ", tivoConfigData.getCfgIdentifier());
        }

        /**
         * {@link stopReader} cleanly stops the {@link StreamReader} thread. Blocks until the reader is stopped.
         */

        public void stopReader() {
            this.stopReader = true;
            try {
                this.stopLatch.await(READ_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Do nothing. The timeout is just here for safety and to be sure that the call to this method will not
                // block the caller indefinitely.
            }
        }

    }
}
