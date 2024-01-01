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
package org.openhab.binding.velux.internal.bridge.slip.io;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transport layer supported by the Velux bridge.
 * <P>
 * SLIP-based 2nd Level I/O interface towards the <B>Velux</B> bridge.
 * <P>
 * It provides methods for pre- and post-communication
 * as well as a common method for the real communication.
 * <UL>
 * <LI>{@link SSLconnection#SSLconnection} for establishing the connection,</LI>
 * <LI>{@link SSLconnection#send} for sending a message to the bridge,</LI>
 * <LI>{@link SSLconnection#available} for observation whether there are bytes available,</LI>
 * <LI>{@link SSLconnection#receive} for receiving a message from the bridge,</LI>
 * <LI>{@link SSLconnection#close} for tearing down the connection.</LI>
 * <LI>{@link SSLconnection#setTimeout} for adapting communication parameters.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SSLconnection implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(SSLconnection.class);

    // Public definition
    public static final SSLconnection UNKNOWN = new SSLconnection();

    /*
     * ***************************
     * ***** Private Objects *****
     */

    private @Nullable SSLSocket socket;
    private @Nullable DataOutputStream dOut;
    private @Nullable DataInputStreamWithTimeout dIn;

    private int readTimeoutMSecs = 2000;
    private int connTimeoutMSecs = 6000;

    /**
     * Fake trust manager to suppress any certificate errors,
     * used within {@link #SSLconnection} for seamless operation
     * even on self-signed certificates like provided by <B>Velux</B>.
     */
    private final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        @Override
        public X509Certificate @Nullable [] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate @Nullable [] arg0, @Nullable String arg1)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate @Nullable [] arg0, @Nullable String arg1)
                throws CertificateException {
        }
    } };

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Constructor for initialization of an unfinished connectivity.
     */
    SSLconnection() {
        logger.debug("SSLconnection() called.");
    }

    /**
     * Constructor to setup and establish a connection.
     *
     * @param bridgeInstance the actual Bridge Thing instance
     * @throws java.net.ConnectException in case of unrecoverable communication failures.
     * @throws java.io.IOException in case of continuous communication I/O failures.
     * @throws java.net.UnknownHostException in case of continuous communication I/O failures.
     */
    SSLconnection(VeluxBridgeHandler bridgeInstance) throws ConnectException, IOException, UnknownHostException {
        logger.debug("Starting {} bridge connection.", VeluxBindingConstants.BINDING_ID);
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("SSL");
            ctx.init(null, trustAllCerts, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException(String.format("create of an empty trust store failed: %s.", e.getMessage()));
        }
        logger.trace("SSLconnection(): creating socket...");
        SSLSocket socket = this.socket = (SSLSocket) ctx.getSocketFactory().createSocket();
        if (socket != null) {
            VeluxBridgeConfiguration cfg = bridgeInstance.veluxBridgeConfiguration();
            readTimeoutMSecs = cfg.timeoutMsecs;
            connTimeoutMSecs = Math.max(connTimeoutMSecs, readTimeoutMSecs);
            // use longer timeout when establishing the connection
            socket.setSoTimeout(connTimeoutMSecs);
            socket.setKeepAlive(true);
            socket.connect(new InetSocketAddress(cfg.ipAddress, cfg.tcpPort), connTimeoutMSecs);
            logger.trace("SSLconnection(): starting SSL handshake...");
            socket.startHandshake();
            // use shorter timeout for normal communications
            socket.setSoTimeout(readTimeoutMSecs);
            dOut = new DataOutputStream(socket.getOutputStream());
            dIn = new DataInputStreamWithTimeout(socket.getInputStream(), bridgeInstance);
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "SSLconnection(): connected... (ip={}, port={}, sslTimeout={}, soTimeout={}, soKeepAlive={})",
                        cfg.ipAddress, cfg.tcpPort, connTimeoutMSecs, socket.getSoTimeout(),
                        socket.getKeepAlive() ? "true" : "false");
            }
        }
        logger.trace("SSLconnection() finished.");
    }

    /*
     * **************************
     * ***** Public Methods *****
     */

    /**
     * Method to query the readiness of the connection.
     *
     * @return <b>ready</b> as boolean for an established connection.
     */
    synchronized boolean isReady() {
        return socket != null && dIn != null && dOut != null;
    }

    /**
     * Method to pass a message towards the bridge. This method gets called when we are initiating a new SLIP
     * transaction.
     *
     * @param <b>packet</b> as Array of bytes to be transmitted towards the bridge via the established connection.
     * @throws java.io.IOException in case of a communication I/O failure
     */
    synchronized void send(byte[] packet) throws IOException {
        logger.trace("send() called, writing {} bytes.", packet.length);
        DataOutputStream dOutX = dOut;
        if (dOutX == null) {
            throw new IOException("DataOutputStream not initialised");
        }
        try {
            // copy packet data to the write buffer
            dOutX.write(packet, 0, packet.length);
            // force the write buffer data to be written to the socket
            dOutX.flush();
            if (logger.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (byte b : packet) {
                    sb.append(String.format("%02X ", b));
                }
                logger.trace("send() finished after having send {} bytes: {}", packet.length, sb.toString());
            }
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    /**
     * Method to verify that there is message from the bridge.
     *
     * @return <b>true</b> if there are any messages ready to be queried using {@link SSLconnection#receive}.
     */
    synchronized boolean available() {
        logger.trace("available() called.");
        DataInputStreamWithTimeout dInX = dIn;
        if (dInX != null) {
            int availableMessages = dInX.available();
            logger.trace("available(): found {} messages ready to be read (> 0 means true).", availableMessages);
            return availableMessages > 0;
        }
        return false;
    }

    /**
     * Method to get a message from the bridge.
     *
     * @return <b>packet</b> as Array of bytes as received from the bridge via the established connection.
     * @throws java.io.IOException in case of a communication I/O failure.
     */
    synchronized byte[] receive() throws IOException {
        logger.trace("receive() called.");
        DataInputStreamWithTimeout dInX = dIn;
        if (dInX == null) {
            throw new IOException("DataInputStreamWithTimeout not initialised");
        }
        try {
            byte[] packet = dInX.readSlipMessage(readTimeoutMSecs);
            if (logger.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (byte b : packet) {
                    sb.append(String.format("%02X ", b));
                }
                logger.trace("receive() finished after having read {} bytes: {}", packet.length, sb.toString());
            }
            return packet;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    /**
     * Destructor to tear down a connection.
     *
     * @throws java.io.IOException in case of a communication I/O failure.
     *             But actually eats all exceptions to ensure sure that all shutdown code is executed
     */
    @Override
    public synchronized void close() throws IOException {
        logger.debug("close() called.");
        DataInputStreamWithTimeout dInX = dIn;
        if (dInX != null) {
            try {
                dInX.close();
            } catch (IOException e) {
                // eat the exception so the following will always be executed
            }
        }
        DataOutputStream dOutX = dOut;
        if (dOutX != null) {
            try {
                dOutX.close();
            } catch (IOException e) {
                // eat the exception so the following will always be executed
            }
        }
        SSLSocket socketX = socket;
        if (socketX != null) {
            logger.debug("Shutting down Velux bridge connection.");
            try {
                socketX.close();
            } catch (IOException e) {
                // eat the exception so the following will always be executed
            }
        }
        dIn = null;
        dOut = null;
        socket = null;
        logger.trace("close() finished.");
    }
}
