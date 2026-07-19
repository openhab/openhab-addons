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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.auth.HapChannel;
import org.openhab.binding.atv.internal.client.support.http.HttpParser;
import org.openhab.binding.atv.internal.client.support.http.HttpRequest;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection used to handle the AirPlay 2 event channel.
 *
 * <p>
 * This is "reverse HTTP": even though we open the TCP connection (to the event port
 * announced in the SETUP response), the receiver acts as the client and sends us HTTP
 * <em>requests</em>, which are answered with a positive response to satisfy the other end.
 * The channel content is not used for anything else.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class EventChannel extends HapChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventChannel.class);

    private byte[] buffer = new byte[0];

    /**
     * Creates an event channel with encryption enabled.
     *
     * @param outputKey key encrypting outgoing data
     * @param inputKey key decrypting incoming data
     */
    public EventChannel(byte[] outputKey, byte[] inputKey) {
        super(outputKey, inputKey);
    }

    @Override
    protected void onReceive(byte[] plaintext) {
        buffer = concat(buffer, plaintext);
        while (buffer.length > 0) {
            try {
                HttpParser.ParseResult<HttpRequest> parsed = HttpParser.parseRequest(buffer);
                if (parsed.message() == null) {
                    LOGGER.debug("Not enough data to parse request on event channel");
                    break;
                }
                buffer = parsed.remainder();
                HttpRequest request = parsed.message();

                LOGGER.debug("Got message on event channel: {} {}", request.method(), request.path());

                // Send a positive response to satisfy the other end of the channel
                Map<String, Object> headers = new LinkedHashMap<>();
                headers.put("Content-Length", "0");
                headers.put("Audio-Latency", "0");
                Object server = request.headers().get("Server");
                if (server != null) {
                    headers.put("Server", server);
                }
                Object cseq = request.headers().get("CSeq");
                if (cseq != null) {
                    headers.put("CSeq", cseq);
                }
                send(HttpParser.formatResponse(
                        new HttpResponse(request.protocol(), request.version(), 200, "OK", headers, new byte[0])));
            } catch (RuntimeException e) {
                // Breaking here avoids spinning on a buffer that can never be parsed
                LOGGER.warn("Failed to handle message on event channel", e);
                break;
            }
        }
    }

    private static byte[] concat(byte[] first, byte[] second) {
        if (first.length == 0) {
            return second;
        }
        byte[] result = java.util.Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
