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
package org.openhab.binding.velux.internal.bridge.slip.io;

import java.io.IOException;
import java.net.ConnectException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeInstance;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2nd Level I/O interface towards the <B>Velux</B> bridge.
 * It provides methods for a pure client/server communication.
 * <P>
 * The following class access methods exist:
 * <UL>
 * <LI>{@link Connection#io} for a complete pair of request and response messages,</LI>
 * <LI>{@link Connection#isAlive} to check the presence of a connection,</LI>
 * <LI>{@link Connection#isMessageAvailable} to check the presence of an incoming message,</LI>
 * <LI>{@link Connection#lastSuccessfulCommunication} returns the timestamp of the last successful communication,</LI>
 * <LI>{@link Connection#lastCommunication} returns the timestamp of the last communication,</LI>
 * <LI>{@link Connection#resetConnection} for resetting the current connection.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class Connection {
    private final Logger logger = LoggerFactory.getLogger(Connection.class);

    /*
     * ***************************
     * ***** Private Objects *****
     */

    /**
     * Timestamp of last successful communication in milliseconds.
     */
    private long lastSuccessfulCommunicationInMSecs = 0;
    /**
     * Timestamp of last communication in milliseconds.
     */
    private long lastCommunicationInMSecs = 0;
    /**
     * SSL socket for communication.
     */
    private SSLconnection connectivity = SSLconnection.UNKNOWN;

    private String host = VeluxBindingConstants.UNKNOWN_IP_ADDRESS;

    /*
     * **************************
     * ***** Public Methods *****
     */

    /**
     * Base level communication with the <B>SlipVeluxBridg</B>.
     *
     * @param bridgeInstance describing the Service Access Point location i.e. hostname and TCP port.
     * @param request as Array of bytes representing the structure of the message to be send.
     * @return <b>response</b> of type Array of byte containing all received informations.
     * @throws java.net.ConnectException in case of unrecoverable communication failures.
     * @throws java.io.IOException in case of continuous communication I/O failures.
     */
    public synchronized byte[] io(VeluxBridgeInstance bridgeInstance, byte[] request)
            throws ConnectException, IOException {
        logger.trace("io() on {}: called.", host);

        lastCommunicationInMSecs = System.currentTimeMillis();

        /** Local handles */
        int retryCount = 0;
        IOException lastIOE = new IOException("Unexpected I/O exception.");

        do {
            try {
                if (!connectivity.isReady()) {
                    try {
                        // From configuration
                        host = bridgeInstance.veluxBridgeConfiguration().ipAddress;
                        int port = bridgeInstance.veluxBridgeConfiguration().tcpPort;
                        int timeoutMsecs = bridgeInstance.veluxBridgeConfiguration().timeoutMsecs;

                        logger.trace("io() on {}: connecting to port {}", host, port);
                        connectivity = new SSLconnection(host, port);
                        connectivity.setTimeout(timeoutMsecs);
                    } catch (ConnectException ce) {
                        throw new ConnectException(String
                                .format("raised a non-recoverable error during connection setup: %s", ce.getMessage()));
                    } catch (IOException e) {
                        logger.warn("io() on {}: raised an error during connection setup: {}.", host, e.getMessage());
                        lastIOE = new IOException(String.format("error during connection setup: %s.", e.getMessage()));
                        continue;
                    }
                }
                if (request.length > 0) {
                    try {
                        if (logger.isTraceEnabled()) {
                            logger.trace("io() on {}: sending packet with {} bytes: {}", host, request.length,
                                    new Packet(request));
                        } else {
                            logger.debug("io() on {}: sending packet of size {}.", host, request.length);
                        }
                        if (connectivity.isReady()) {
                            connectivity.send(request);
                        }
                    } catch (IOException e) {
                        logger.info("io() on {}: raised an error during sending: {}.", host, e.getMessage());
                        break;
                    }

                    // Give the bridge some time to breathe
                    if (bridgeInstance.veluxBridgeConfiguration().timeoutMsecs > 0) {
                        logger.trace("io() on {}: wait time {} msecs.", host,
                                bridgeInstance.veluxBridgeConfiguration().timeoutMsecs);
                        try {
                            Thread.sleep(bridgeInstance.veluxBridgeConfiguration().timeoutMsecs);
                        } catch (InterruptedException ie) {
                            logger.trace("io() on {}: wait interrupted.", host);
                        }
                    }
                }
                byte[] packet = new byte[0];
                logger.trace("io() on {}: receiving bytes.", host);
                if (connectivity.isReady()) {
                    packet = connectivity.receive();
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("io() on {}: received packet with {} bytes: {}", host, packet.length,
                            new Packet(packet));
                } else {
                    logger.debug("io() on {}: received packet with {} bytes.", host, packet.length);
                }
                lastSuccessfulCommunicationInMSecs = System.currentTimeMillis();
                lastCommunicationInMSecs = lastSuccessfulCommunicationInMSecs;
                logger.trace("io() on {}: finished.", host);
                return packet;
            } catch (IOException ioe) {
                logger.info("io() on {}: Exception occurred during I/O: {}.", host, ioe.getMessage());
                lastIOE = ioe;
                // Error Retries with Exponential Backoff
                long waitTime = ((long) Math.pow(2, retryCount)
                        * bridgeInstance.veluxBridgeConfiguration().timeoutMsecs);
                logger.trace("io() on {}: wait time {} msecs.", host, waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    logger.trace("io() on {}: wait interrupted.", host);
                }
            }
        } while (retryCount++ < bridgeInstance.veluxBridgeConfiguration().retries);
        if (retryCount >= bridgeInstance.veluxBridgeConfiguration().retries) {
            logger.info("io() on {}: socket I/O failed {} times.", host,
                    bridgeInstance.veluxBridgeConfiguration().retries);
        }
        logger.trace("io() on {}: shutting down connection.", host);
        if (connectivity.isReady()) {
            connectivity.close();
        }
        logger.trace("io() on {}: finishes with failure by throwing exception.", host);
        throw lastIOE;
    }

    /**
     * Returns the status of the current connection.
     *
     * @return state as boolean.
     */
    public boolean isAlive() {
        logger.trace("isAlive() on {}: called.", host);
        return connectivity.isReady();
    }

    /**
     * Returns the availability of an incoming message.
     *
     * @return state as boolean.
     */
    public synchronized boolean isMessageAvailable() {
        logger.trace("isMessageAvailable() on {}: called.", host);
        try {
            if ((connectivity.isReady()) && (connectivity.available())) {
                logger.trace("isMessageAvailable() on {}: there is a message waiting.", host);
                return true;
            }
        } catch (IOException e) {
            logger.trace("isMessageAvailable() on {}: lost connection due to {}.", host, e.getMessage());
            resetConnection();
        }
        logger.trace("isMessageAvailable() on {}: no message waiting.", host);
        return false;
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last successful communication.
     *
     * @return timestamp in milliseconds.
     */
    public long lastSuccessfulCommunication() {
        return lastSuccessfulCommunicationInMSecs;
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last communication.
     *
     * @return timestamp in milliseconds.
     */
    public long lastCommunication() {
        return lastCommunicationInMSecs;
    }

    /**
     * Resets an open connectivity by closing the socket and resetting the authentication information.
     */
    public synchronized void resetConnection() {
        logger.trace("resetConnection() on {}: called.", host);
        try {
            connectivity.close();
        } catch (IOException e) {
            logger.info("resetConnection() on {}: raised an error during connection close: {}.", host, e.getMessage());
        }
        logger.trace("resetConnection() on {}: done.", host);
    }
}
