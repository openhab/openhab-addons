/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.onkyo.internal.eiscp.EiscpCommand;
import org.openhab.binding.onkyo.internal.eiscp.EiscpException;
import org.openhab.binding.onkyo.internal.eiscp.EiscpMessage;
import org.openhab.binding.onkyo.internal.eiscp.EiscpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class open a TCP/IP connection to the Onkyo device and send a command.
 *
 * @author Pauli Anttila
 */
public class OnkyoConnection {

    private Logger logger = LoggerFactory.getLogger(OnkyoConnection.class);

    /** default eISCP port. **/
    public static final int DEFAULT_EISCP_PORT = 60128;

    /** Connection timeout in milliseconds **/
    private static final int CONNECTION_TIMEOUT = 5000;

    /** Connection test interval in milliseconds **/
    private static final int CONNECTION_TEST_INTERVAL = 60000;

    /** Socket timeout in milliseconds **/
    private static final int SOCKET_TIMEOUT = CONNECTION_TEST_INTERVAL + 10000;

    /** Connection retry count on error situations **/
    private static final int FAST_CONNECTION_RETRY_COUNT = 3;

    /** Connection retry delays in milliseconds **/
    private static final int FAST_CONNECTION_RETRY_DELAY = 1000;
    private static final int SLOW_CONNECTION_RETRY_DELAY = 60000;

    private String ip;
    private int port;
    private Socket eiscpSocket = null;
    private DataListener dataListener = null;
    private DataOutputStream outStream = null;
    private DataInputStream inStream = null;
    private boolean connected = false;
    private List<OnkyoEventListener> listeners = new ArrayList<OnkyoEventListener>();
    private int retryCount = 1;
    private ConnectionSupervisor connectionSupervisor = null;

    public OnkyoConnection(String ip) {
        this.ip = ip;
        this.port = DEFAULT_EISCP_PORT;
    }

    public OnkyoConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Open connection to the Onkyo device.
     *
     **/
    public void openConnection() {
        connectSocket();
    }

    /**
     * Closes the connection to the Onkyo device.
     *
     **/
    public void closeConnection() {
        closeSocket();
    }

    public void addEventListener(OnkyoEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeEventListener(OnkyoEventListener listener) {
        this.listeners.remove(listener);
    }

    public String getConnectionName() {
        return ip + ":" + port;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Sends a command to Onkyo device.
     *
     * @param cmd eISCP command to send
     */
    public void send(final String cmd, final String value) {

        try {
            sendCommand(new EiscpMessage.MessageBuilder().command(cmd).value(value).build());
        } catch (Exception e) {
            logger.error("Could not send command to device on {}:{}: ", ip, port, e);
        }

    }

    private void sendCommand(EiscpMessage msg) {
        logger.debug("Send command: {} to {}:{} ({})", msg.toString(), ip, port, eiscpSocket);
        sendCommand(msg, retryCount);
    }

    /**
     * Sends to command to the receiver.
     *
     * @param eiscpCmd the eISCP command to send.
     * @param retry retry count when connection fails.
     **/
    private void sendCommand(EiscpMessage msg, int retry) {

        if (connectSocket()) {
            try {

                String data = EiscpProtocol.createEiscpPdu(msg);
                if (logger.isTraceEnabled()) {
                    logger.trace("Sending {} bytes: {}", data.length(),
                            DatatypeConverter.printHexBinary(data.toString().getBytes()));
                }

                outStream.writeBytes(data);
                outStream.flush();
            } catch (IOException ioException) {
                logger.error("Error occurred when sending command: {}", ioException.getMessage());

                if (retry > 0) {
                    logger.debug("Retry {}...", retry);
                    closeSocket();
                    sendCommand(msg, retry--);
                } else {
                    sendConnectionErrorEvent();
                }
            }
        }
    }

    /**
     * Connects to the receiver by opening a socket connection through the
     * IP and port.
     **/
    private synchronized boolean connectSocket() {

        if (eiscpSocket == null || !connected || !eiscpSocket.isConnected()) {
            try {
                // Creating a socket to connect to the server
                eiscpSocket = new Socket();

                // start connection tester
                if (connectionSupervisor == null) {
                    connectionSupervisor = new ConnectionSupervisor(CONNECTION_TEST_INTERVAL);
                }

                eiscpSocket.connect(new InetSocketAddress(ip, port), CONNECTION_TIMEOUT);

                logger.debug("Connected to {}:{}", ip, port);

                // Get Input and Output streams
                outStream = new DataOutputStream(eiscpSocket.getOutputStream());
                inStream = new DataInputStream(eiscpSocket.getInputStream());

                eiscpSocket.setSoTimeout(SOCKET_TIMEOUT);
                outStream.flush();
                connected = true;

                // start status update listener
                if (dataListener == null) {
                    dataListener = new DataListener();
                    dataListener.start();
                }

            } catch (UnknownHostException unknownHost) {
                logger.error("You are trying to connect to an unknown host: {}", unknownHost.getMessage());
            } catch (IOException ioException) {
                logger.error("Can't connect: {}", ioException.getMessage());
            }
        }

        return connected;
    }

    /**
     * Closes the socket connection.
     *
     * @return true if the closed successfully
     **/
    private boolean closeSocket() {
        try {
            if (dataListener != null) {
                dataListener.setInterrupted(true);
                dataListener = null;
                logger.debug("closed data listener!");
            }
            if (connectionSupervisor != null) {
                connectionSupervisor.stopConnectionTester();
                connectionSupervisor = null;
                logger.debug("closed connection tester!");
            }
            if (inStream != null) {
                IOUtils.closeQuietly(inStream);
                inStream = null;
                logger.debug("closed input stream!");
            }
            if (outStream != null) {
                IOUtils.closeQuietly(outStream);
                outStream = null;
                logger.debug("closed output stream!");
            }
            if (eiscpSocket != null) {
                IOUtils.closeQuietly(eiscpSocket);
                eiscpSocket = null;
                logger.debug("closed socket!");
            }
            connected = false;
        } catch (Exception e) {
            logger.error("Closing connection throws an exception, {}", e.getMessage());
        }

        return connected;
    }

    /**
     * This method wait any state messages form receiver.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws EiscpException
     **/
    private void waitStateMessages() throws NumberFormatException, IOException, InterruptedException, EiscpException {

        if (connected) {

            logger.trace("Waiting status messages");

            while (true) {
                EiscpMessage message = EiscpProtocol.getNextMessage(inStream);
                sendMessageEvent(message);
            }

        } else {
            throw new IOException("Not Connected to Receiver");
        }
    }

    private class DataListener extends Thread {

        private boolean interrupted = false;

        DataListener() {
        }

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
            this.interrupt();
        }

        @Override
        public void run() {

            logger.debug("Data listener started");

            boolean restartConnection = false;
            long connectionAttempts = 0;

            // as long as no interrupt is requested, continue running
            while (!interrupted) {
                try {
                    waitStateMessages();
                    connectionAttempts = 0;
                } catch (EiscpException e) {

                    logger.error("Error occurred during message waiting: {}", e.getMessage());

                } catch (SocketTimeoutException e) {

                    logger.error("No data received during supervision interval ({} ms)!", SOCKET_TIMEOUT);

                    restartConnection = true;

                } catch (Exception e) {

                    if (!interrupted && !this.isInterrupted()) {
                        logger.debug("Error occurred during message waiting: {}", e.getMessage());
                        sendConnectionErrorEvent();
                        restartConnection = true;

                        // sleep a while, to prevent fast looping if error situation is permanent
                        if (++connectionAttempts < FAST_CONNECTION_RETRY_COUNT) {
                            mysleep(FAST_CONNECTION_RETRY_DELAY);
                        } else {
                            // slow down after few faster attempts
                            if (connectionAttempts == FAST_CONNECTION_RETRY_COUNT) {
                                logger.info(
                                        "Connection failed {} times to {}:{}, slowing down automatic connection to {} seconds.",
                                        FAST_CONNECTION_RETRY_COUNT, ip, port, SLOW_CONNECTION_RETRY_DELAY / 1000);
                            }
                            mysleep(SLOW_CONNECTION_RETRY_DELAY);
                        }
                    }
                }

                if (restartConnection) {
                    restartConnection = false;

                    // reopen connection
                    logger.debug("Reconnecting...");

                    try {
                        connected = false;
                        connectSocket();
                        logger.debug("Test connection to {}:{}", ip, port);
                        sendCommand(new EiscpMessage.MessageBuilder().command(EiscpCommand.POWER_QUERY.getCommand())
                                .value(EiscpCommand.POWER_QUERY.getValue()).build());

                    } catch (Exception ex) {
                        logger.error("Reconnection invoking error: {}", ex.getMessage());
                    }
                }
            }

            logger.debug("Data listener stopped");
        }

        private void mysleep(long milli) {
            try {
                sleep(milli);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }

    private class ConnectionSupervisor {
        private Timer timer;

        public ConnectionSupervisor(int milliseconds) {
            logger.debug("Connection supervisor started, interval {} milliseconds", milliseconds);

            timer = new Timer();
            timer.schedule(new Task(), milliseconds, milliseconds);
        }

        public void stopConnectionTester() {
            timer.cancel();
        }

        class Task extends TimerTask {
            @Override
            public void run() {
                logger.debug("Test connection to {}:{}", ip, port);
                sendCommand(new EiscpMessage.MessageBuilder().command(EiscpCommand.POWER_QUERY.getCommand())
                        .value(EiscpCommand.POWER_QUERY.getValue()).build());
            }
        }
    }

    private void sendConnectionErrorEvent() {
        // send message to event listeners
        try {
            Iterator<OnkyoEventListener> iterator = listeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().connectionError(ip);
            }

        } catch (Exception ex) {
            logger.debug("Event listener invoking error: {}", ex.getMessage());
        }
    }

    private void sendMessageEvent(EiscpMessage message) {
        // send message to event listeners
        try {
            Iterator<OnkyoEventListener> iterator = listeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().statusUpdateReceived(ip, message);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error: {}", e.getMessage());
        }
    }
}