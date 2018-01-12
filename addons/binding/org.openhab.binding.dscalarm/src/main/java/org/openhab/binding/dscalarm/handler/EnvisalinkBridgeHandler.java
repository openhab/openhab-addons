/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.dscalarm.internal.config.EnvisalinkBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for the EyezOn Envisalink 3/2DS Ethernet interface.
 *
 * @author Russell Stephens - Initial Contribution
 */

public class EnvisalinkBridgeHandler extends DSCAlarmBaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(EnvisalinkBridgeHandler.class);

    /**
     * Constructor.
     *
     * @param bridge
     */
    public EnvisalinkBridgeHandler(Bridge bridge) {
        super(bridge, DSCAlarmBridgeType.Envisalink, DSCAlarmProtocol.ENVISALINK_TPI);
    }

    // Variables for TCP connection.
    private String ipAddress;
    private int tcpPort;
    private int connectionTimeout;
    private Socket tcpSocket = null;
    private OutputStreamWriter tcpOutput = null;
    private BufferedReader tcpInput = null;

    @Override
    public void initialize() {
        logger.debug("Initializing the Envisalink Bridge handler.");

        EnvisalinkBridgeConfiguration configuration = getConfigAs(EnvisalinkBridgeConfiguration.class);

        if (configuration.ipAddress != null) {

            ipAddress = configuration.ipAddress;
            tcpPort = configuration.port.intValue();
            setPassword(configuration.password);
            connectionTimeout = configuration.connectionTimeout.intValue();
            pollPeriod = configuration.pollPeriod.intValue();

            if (this.pollPeriod > 15) {
                this.pollPeriod = 15;
            } else if (this.pollPeriod < 1) {
                this.pollPeriod = 1;
            }

            logger.debug("Envisalink Bridge Handler Initialized");
            logger.debug("   IP Address:         {},", ipAddress);
            logger.debug("   Port:               {},", tcpPort);
            logger.debug("   Password:           {},", getPassword());
            logger.debug("   PollPeriod:         {},", pollPeriod);
            logger.debug("   Connection Timeout: {}.", connectionTimeout);

            updateStatus(ThingStatus.OFFLINE);
            startPolling();
        }
    }

    @Override
    public void dispose() {
        stopPolling();
        closeConnection();
        super.dispose();
    }

    @Override
    public void openConnection() {
        try {
            closeConnection();

            logger.debug("openConnection(): Connecting to Envisalink ");

            tcpSocket = new Socket();
            SocketAddress tpiSocketAddress = new InetSocketAddress(ipAddress, tcpPort);
            tcpSocket.connect(tpiSocketAddress, connectionTimeout);
            tcpOutput = new OutputStreamWriter(tcpSocket.getOutputStream(), "US-ASCII");
            tcpInput = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

            Thread tcpListener = new Thread(new TCPListener());
            tcpListener.start();

            setConnected(true);
        } catch (UnknownHostException unknownHostException) {
            logger.error("openConnection(): Unknown Host Exception: {}", unknownHostException.getMessage());
            setConnected(false);
        } catch (SocketException socketException) {
            logger.error("openConnection(): Socket Exception: {}", socketException.getMessage());
            setConnected(false);
        } catch (IOException ioException) {
            logger.error("openConnection(): IO Exception: {}", ioException.getMessage());
            setConnected(false);
        } catch (Exception exception) {
            logger.error("openConnection(): Unable to open a connection: {} ", exception.getMessage(), exception);
            setConnected(false);
        }
    }

    @Override
    public void write(String writeString) {
        try {
            logger.debug("write(): Attempting to Send Message: {}", writeString);
            tcpOutput.write(writeString);
            tcpOutput.flush();
        } catch (IOException ioException) {
            logger.error("write(): {}", ioException.getMessage());
            setConnected(false);
        } catch (Exception exception) {
            logger.error("write(): Unable to write to socket: {} ", exception.getMessage(), exception);
            setConnected(false);
        }
    }

    @Override
    public String read() {
        String message = "";

        try {
            message = tcpInput.readLine();
            logger.debug("read(): Message Received: {}", message);
        } catch (IOException ioException) {
            logger.error("read(): IO Exception: {}", ioException.getMessage());
            setConnected(false);
        } catch (Exception exception) {
            logger.error("read(): Exception: {} ", exception.getMessage(), exception);
            setConnected(false);
        }

        return message;
    }

    @Override
    public void closeConnection() {
        try {
            if (tcpSocket != null) {
                logger.debug("closeConnection(): Closing Socket!");
                tcpSocket.close();
                tcpSocket = null;
            }
            if (tcpInput != null) {
                logger.debug("closeConnection(): Closing Output Writer!");
                tcpInput.close();
                tcpInput = null;
            }
            if (tcpOutput != null) {
                logger.debug("closeConnection(): Closing Input Reader!");
                tcpOutput.close();
                tcpOutput = null;
            }

            setConnected(false);
            logger.debug("closeConnection(): Closed TCP Connection!");
        } catch (IOException ioException) {
            logger.error("closeConnection(): Unable to close connection - {}", ioException.getMessage());
        } catch (Exception exception) {
            logger.error("closeConnection(): Error closing connection - {}", exception.getMessage());
        }
    }

    /**
     * Gets the IP Address of the Envisalink
     *
     * @return ipAddress
     */
    public String getIPAddress() {
        return ipAddress;
    }

    /**
     * TCPMessageListener: Receives messages from the DSC Alarm Panel API.
     */
    private class TCPListener implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(TCPListener.class);

        /**
         * Run method. Runs the MessageListener thread
         */
        @Override
        public void run() {
            String messageLine;

            try {
                while (isConnected()) {
                    if ((messageLine = read()) != null) {
                        try {
                            handleIncomingMessage(messageLine);
                        } catch (Exception e) {
                            logger.error("TCPListener(): Message not handled by bridge: {}", e.getMessage());
                        }
                    } else {
                        setConnected(false);
                    }
                }
            } catch (Exception e) {
                logger.error("TCPListener(): Unable to read message: {} ", e.getMessage(), e);
                closeConnection();
            }
        }
    }
}
