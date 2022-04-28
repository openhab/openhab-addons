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
package org.openhab.binding.mynice.internal.xml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.handler.It4WifiHandler;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link It4WifiConnector} is responsible for connecting reading, writing and disconnecting from the It4Wifi.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class It4WifiConnector extends Thread {
    private static final int DEFAULT_SOCKET_TIMEOUT_MS = 5000;
    private static final int DEFAULT_RECONNECT_TIMEOUT_MS = 5000;
    private static final int MAX_KEEPALIVE_FAILURE = 3;
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final int SERVER_PORT = 443;
    private static final char ETX = '\u0003';
    private static final char STX = '\u0002';
    private static final String ENDL = "\r\n";
    private static final String START_REQUEST = "<Request id=\"%s\" source=\"openhab\" target=\"%mac%\" gw=\"gwID\" protocolType=\"NHK\" protocolVersion=\"1.0\" type=\"%s\">";
    private static final String END_REQUEST = "</Request>";
    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] { TrustAllTrustManager.getInstance() };

    private final Logger logger = LoggerFactory.getLogger(It4WifiConnector.class);
    private final String hostname;
    private final It4WifiHandler handler;
    private final It4WifiSession session;
    private final String requestStart;

    private @NonNullByDefault({}) Socket client;
    private @NonNullByDefault({}) InputStreamReader in;
    private @NonNullByDefault({}) OutputStreamWriter out;

    private int failedKeepalive = 0;
    private boolean waitingKeepaliveResponse = false;
    private String buffer = "";

    public It4WifiConnector(String hostname, String macAddress, It4WifiSession session, It4WifiHandler handler) {
        super(session.getUserName());
        this.hostname = hostname;
        this.handler = handler;
        this.session = session;
        this.requestStart = START_REQUEST.replace("%mac%", macAddress) + ENDL;
        setDaemon(true);
    }

    public void buildMessage(CommandType command/* , Object... bodyParms */) throws Exception {
        String startRequest = String.format(requestStart, session.getCommandId(), command);
        String body = startRequest + command.getBody(session/* , bodyParms */);
        body = body + buildSign(command, body) + END_REQUEST + ENDL;
        logger.debug("Sending ItT4Wifi :{}{}", ENDL, body);
        out.write(STX + body + ETX);
        out.flush();
    }

    private void connect() throws IOException {
        disconnect();
        logger.debug("Connecting {}:{}...", hostname, SERVER_PORT);

        try {
            Socket client = new Socket();

            client.setReuseAddress(true);
            client.connect(new InetSocketAddress(hostname, SERVER_PORT), 5000);
            SSLContext sslContext = SSLContext.getInstance("SSL"); // TODO : voir si on peut remplacer par getDefault()
            sslContext.init(null, TRUST_ALL_CERTS, new SecureRandom());
            client = sslContext.getSocketFactory().createSocket(client, hostname, SERVER_PORT, true);
            in = new InputStreamReader(client.getInputStream());
            out = new OutputStreamWriter(client.getOutputStream());
            ((SSLSocket) client).startHandshake();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException(e);
        }
    }

    private void disconnect() {
        logger.debug("Disconnecting");

        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
            }
            this.in = null;
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException ignore) {
            }
            this.out = null;
        }
        if (client != null) {
            try {
                client.close();
            } catch (IOException ignore) {
            }
            this.client = null;
        }
        logger.debug("Disconnected");
    }

    /**
     * Stop the device thread
     */
    public void destroyAndExit() {
        interrupt();
        disconnect();
    }

    /**
     * Send an arbitrary keepalive command which cause the IPX to send an update.
     * If we don't receive the update maxKeepAliveFailure time, the connection is closed and reopened
     */
    // private void sendKeepalive() {
    // if (out != null) {
    // if (waitingKeepaliveResponse) {
    // failedKeepalive++;
    // logger.debug("Sending keepalive, attempt {}", failedKeepalive);
    // } else {
    // failedKeepalive = 0;
    // logger.debug("Sending keepalive");
    // }
    // out.println("GetIn01");
    // out.flush();
    // waitingKeepaliveResponse = true;
    // }
    // }

    @Override
    public void run() {
        try {
            waitingKeepaliveResponse = false;
            failedKeepalive = 0;
            connect();
            handler.handShaked();
            while (!interrupted()) {
                if (failedKeepalive > MAX_KEEPALIVE_FAILURE) {
                    throw new IOException("Max keep alive attempts has been reached");
                }
                try {
                    int data;
                    while ((data = in.read()) != -1) {
                        switch (data) {
                            case STX:
                                buffer = "";
                                break;
                            case ETX:
                                waitingKeepaliveResponse = false;
                                handler.received(buffer);
                                break;
                            default:
                                buffer += (char) data;
                        }
                    }
                } catch (SocketTimeoutException e) {
                    handleException(e);
                }
            }
            disconnect();
        } catch (IOException e) {
            handleException(e);
        }
        try {
            Thread.sleep(DEFAULT_RECONNECT_TIMEOUT_MS);
        } catch (InterruptedException e) {
            destroyAndExit();
        }
    }

    private void handleException(Exception e) {
        if (!interrupted()) {
            if (e instanceof SocketTimeoutException) {
                // sendKeepalive();
                return;
            } else if (e instanceof IOException) {
                logger.warn("Communication error : '{}', will retry in {} ms", e, DEFAULT_RECONNECT_TIMEOUT_MS);
            }
            // if (parser != null) {
            // parser.errorOccurred(e);
            // }
        }
    }

    private String buildSign(CommandType command, String xmlCommand) throws NoSuchAlgorithmException {
        if (command.signNeeded) {
            byte[] msgHash = Utils.sha256(xmlCommand.getBytes());
            byte[] sign = Utils.sha256(msgHash, session.getSessionPassword());
            return String.format("<Sign>%s</Sign>", BASE64_ENCODER.encodeToString(sign));
        }
        return "";
    }
}
