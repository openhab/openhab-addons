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
package org.openhab.binding.velux.internal.bridge.slip.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
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
public class Connection implements Closeable {
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
    public synchronized byte[] io(VeluxBridgeHandler bridgeInstance, byte[] request)
            throws ConnectException, IOException {
        VeluxBridgeConfiguration cfg = bridgeInstance.veluxBridgeConfiguration();
        host = cfg.ipAddress;
        logger.trace("io() on {}: called.", host);

        lastCommunicationInMSecs = System.currentTimeMillis();

        /** Local handles */
        int retryCount = 0;
        IOException lastIOE = new IOException("Unexpected I/O exception.");

        do {
            try {
                if (!connectivity.isReady()) {
                    // dispose old connectivity class instances (if any)
                    resetConnection();
                    try {
                        logger.trace("io() on {}: connecting to port {}", cfg.ipAddress, cfg.tcpPort);
                        connectivity = new SSLconnection(bridgeInstance);
                    } catch (ConnectException ce) {
                        throw new ConnectException(String
                                .format("raised a non-recoverable error during connection setup: %s", ce.getMessage()));
                    } catch (IOException e) {
                        logger.warn("io() on {}: raised an error during connection setup: {}.", host, e.getMessage());
                        lastIOE = new IOException(String.format("error during connection setup: %s.", e.getMessage()));
                        continue;
                    }
                }
                boolean sending = request.length > 0;
                if (sending) {
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
                }
                byte[] packet = new byte[0];
                logger.trace("io() on {}: receiving bytes.", host);
                if (connectivity.isReady()) {
                    packet = connectivity.receive();
                    // in receive-only mode, a zero length response packet is NOT a timeout
                    if (sending && (packet.length == 0)) {
                        throw new SocketTimeoutException("read time out after send");
                    }
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
        resetConnection();
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
        if (!connectivity.isReady()) {
            logger.trace("isMessageAvailable() on {}: lost connection, there may be messages", host);
            return false;
        }
        boolean result = connectivity.available();
        logger.trace("isMessageAvailable() on {}: there are {}messages waiting.", host, result ? "" : "no ");
        return result;
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

    @Override
    public void close() throws IOException {
        resetConnection();
    }
}
