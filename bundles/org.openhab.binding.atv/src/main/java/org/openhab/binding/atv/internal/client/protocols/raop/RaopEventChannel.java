/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.auth.HapSession;
import org.openhab.binding.atv.internal.client.support.http.HttpParser;
import org.openhab.binding.atv.internal.client.support.http.HttpRequest;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side of the AirPlay 2 event channel.
 *
 * <p>
 * A TCP connection to the receiver's event port, HAP-encrypted with keys derived from
 * pair-verify. Incoming requests are decrypted, parsed and answered with an empty 200
 * response to satisfy the other end; nothing is ever initiated from our side.
 *
 * <p>
 * A dedicated virtual thread serves the channel until {@link #close()}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class RaopEventChannel implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaopEventChannel.class);

    private final Socket socket;
    private final HapSession session;
    private final Thread readerThread;
    private volatile boolean closed;

    /**
     * Connects the event channel.
     *
     * @param address receiver address
     * @param port receiver event port
     * @param outputKey key encrypting data we send
     * @param inputKey key decrypting data we receive
     * @throws IOException if the connection cannot be established
     */
    RaopEventChannel(String address, int port, byte[] outputKey, byte[] inputKey) throws IOException {
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(address, port));
        this.session = new HapSession();
        this.session.enable(outputKey, inputKey);
        this.readerThread = Thread.ofVirtual().name("raop-event-channel").start(this::readLoop);
        LOGGER.debug("Event channel connected to {}:{}", address, port);
    }

    /**
     * Closes the channel.
     */
    @Override
    public void close() {
        closed = true;
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.debug("Failed to close event channel: {}", e.toString());
        }
    }

    private void readLoop() {
        byte[] buffer = new byte[0];
        byte[] chunk = new byte[4096];
        try {
            InputStream in = socket.getInputStream();
            while (true) {
                int read = in.read(chunk);
                if (read < 0) {
                    break;
                }
                byte[] plaintext = session.decrypt(Arrays.copyOf(chunk, read));
                if (plaintext.length == 0) {
                    continue;
                }
                byte[] combined = Arrays.copyOf(buffer, buffer.length + plaintext.length);
                System.arraycopy(plaintext, 0, combined, buffer.length, plaintext.length);
                buffer = handleReceived(combined);
            }
        } catch (IOException e) {
            if (!closed) {
                LOGGER.debug("Event channel connection lost: {}", e.toString());
            }
        } catch (RuntimeException e) {
            LOGGER.debug("Failed to handle message on event channel", e);
        }
        LOGGER.debug("Event channel closed");
    }

    /** Parses buffered requests and sends positive responses. */
    private byte[] handleReceived(byte[] buffer) throws IOException {
        while (buffer.length > 0) {
            HttpParser.ParseResult<HttpRequest> parsed = HttpParser.parseRequest(buffer);
            HttpRequest request = parsed.message();
            if (request == null) {
                LOGGER.debug("Not enough data to parse request on event channel");
                break;
            }
            LOGGER.debug("Got message on event channel: {} {}", request.method(), request.path());
            buffer = parsed.remainder();

            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Length", "0");
            headers.put("Audio-Latency", "0");
            Object server = request.headers().get("Server");
            if (server != null) {
                headers.put("Server", server.toString());
            }
            Object cseq = request.headers().get("CSeq");
            if (cseq != null) {
                headers.put("CSeq", cseq.toString());
            }
            byte[] response = HttpParser.formatResponse(
                    new HttpResponse(request.protocol(), request.version(), 200, "OK", headers, new byte[0]));
            OutputStream out = socket.getOutputStream();
            out.write(session.encrypt(response));
            out.flush();
        }
        return buffer;
    }
}
