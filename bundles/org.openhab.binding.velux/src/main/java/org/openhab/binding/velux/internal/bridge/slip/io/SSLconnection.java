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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
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
class SSLconnection {
    private final Logger logger = LoggerFactory.getLogger(SSLconnection.class);

    // Public definition
    public static final SSLconnection UNKNOWN = new SSLconnection();

    /*
     * ***************************
     * ***** Private Objects *****
     */

    private static final int CONNECTION_BUFFER_SIZE = 4096;

    private boolean ready = false;
    private @Nullable SSLSocket socket;
    private @Nullable DataOutputStream dOut;
    private @Nullable DataInputStreamWithTimeout dIn;
    private int ioTimeoutMSecs = 60000;

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
        ready = false;
        logger.trace("SSLconnection() finished.");
    }

    /**
     * Constructor to setup and establish a connection.
     *
     * @param host as String describing the Service Access Point location i.e. hostname.
     * @param port as String describing the Service Access Point location i.e. TCP port.
     * @throws java.net.ConnectException in case of unrecoverable communication failures.
     * @throws java.io.IOException in case of continuous communication I/O failures.
     * @throws java.net.UnknownHostException in case of continuous communication I/O failures.
     */
    SSLconnection(String host, int port) throws ConnectException, IOException, UnknownHostException {
        logger.debug("SSLconnection({},{}) called.", host, port);
        logger.info("Starting {} bridge connection.", VeluxBindingConstants.BINDING_ID);
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("SSL");
            ctx.init(null, trustAllCerts, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException(String.format("create of an empty trust store failed: %s.", e.getMessage()));
        }
        logger.trace("SSLconnection(): creating socket...");
        // Just for avoidance of Potential null pointer access
        SSLSocket socketX = (SSLSocket) ctx.getSocketFactory().createSocket(host, port);
        logger.trace("SSLconnection(): starting SSL handshake...");
        if (socketX != null) {
            socketX.startHandshake();
            dOut = new DataOutputStream(socketX.getOutputStream());
            dIn = new DataInputStreamWithTimeout(socketX.getInputStream());
            ready = true;
            socket = socketX;
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
        return ready;
    }

    /**
     * Method to pass a message towards the bridge.
     *
     * @param packet as Array of bytes to be transmitted towards the bridge via the established connection.
     * @throws java.io.IOException in case of a communication I/O failure.
     */
    @SuppressWarnings("null")
    synchronized void send(byte[] packet) throws IOException {
        logger.trace("send() called, writing {} bytes.", packet.length);
        if (!ready || (dOut == null)) {
            throw new IOException();
        }
        dOut.write(packet, 0, packet.length);
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (byte b : packet) {
                sb.append(String.format("%02X ", b));
            }
            logger.trace("send() finished after having send {} bytes: {}", packet.length, sb.toString());
        }
    }

    /**
     * Method to verify that there is message from the bridge.
     *
     * @return <b>true</b> if there are any bytes ready to be queried using {@link SSLconnection#receive}.
     * @throws java.io.IOException in case of a communication I/O failure.
     */
    synchronized boolean available() throws IOException {
        logger.trace("available() called.");
        if (!ready || (dIn == null)) {
            throw new IOException();
        }
        @SuppressWarnings("null")
        int availableBytes = dIn.available();
        logger.trace("available(): found {} bytes ready to be read (> 0 means true).", availableBytes);
        return availableBytes > 0;
    }

    /**
     * Method to get a message from the bridge.
     *
     * @return <b>packet</b> as Array of bytes as received from the bridge via the established connection.
     * @throws java.io.IOException in case of a communication I/O failure.
     */
    synchronized byte[] receive() throws IOException {
        logger.trace("receive() called.");
        if (!ready || (dIn == null)) {
            throw new IOException();
        }
        byte[] message = new byte[CONNECTION_BUFFER_SIZE];
        @SuppressWarnings("null")
        int messageLength = dIn.read(message, 0, message.length, ioTimeoutMSecs);
        byte[] packet = new byte[messageLength];
        System.arraycopy(message, 0, packet, 0, messageLength);
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (byte b : packet) {
                sb.append(String.format("%02X ", b));
            }
            logger.trace("receive() finished after having read {} bytes: {}", messageLength, sb.toString());
        }
        return packet;
    }

    /**
     * Destructor to tear down a connection.
     *
     * @throws java.io.IOException in case of a communication I/O failure.
     */
    synchronized void close() throws IOException {
        logger.debug("close() called.");
        ready = false;
        logger.info("Shutting down Velux bridge connection.");
        // Just for avoidance of Potential null pointer access
        DataInputStreamWithTimeout dInX = dIn;
        if (dInX != null) {
            dInX.close();
        }
        // Just for avoidance of Potential null pointer access
        DataOutputStream dOutX = dOut;
        if (dOutX != null) {
            dOutX.close();
        }
        // Just for avoidance of Potential null pointer access
        SSLSocket socketX = socket;
        if (socketX != null) {
            socketX.close();
        }
        logger.trace("close() finished.");
    }

    /**
     * Parameter modification.
     *
     * @param timeoutMSecs the maximum duration in milliseconds for read operations.
     */
    void setTimeout(int timeoutMSecs) {
        logger.debug("setTimeout() set timeout to {} milliseconds.", timeoutMSecs);
        ioTimeoutMSecs = timeoutMSecs;
    }
}
