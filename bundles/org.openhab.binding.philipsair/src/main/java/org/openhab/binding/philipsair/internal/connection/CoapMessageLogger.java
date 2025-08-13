/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal.connection;

import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.interceptors.MessageInterceptor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple message interceptor to log CoAP messages conversation.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CoapMessageLogger implements MessageInterceptor {

    private final Logger logger = LoggerFactory.getLogger(CoapMessageLogger.class);

    @Override
    public void receiveRequest(@Nullable Request request) {
        // Not used for client-side observation responses
    }

    @Override
    public void receiveResponse(@Nullable Response response) {
        logger.debug("<<<<< COAP RESPONSE RECEIVED <<<<<");
        if (response == null) {
            logger.debug("Response is null");
            return;
        }
        logger.debug("Source: {}", response.getSourceContext().getPeerAddress());
        logger.debug("Type: {}", response.getType());
        logger.debug("MID: {}", response.getMID());
        logger.debug("Token: {}", response.getTokenString());
        logger.debug("Code: {}", response.getCode());
        logger.debug("Options: {}", response.getOptions());
        if (response.getPayload() != null) {
            logger.debug("Payload (hex): {}", toHex(response.getPayload()));
            logger.debug("Payload (string): {}", response.getPayloadString());
        }
    }

    @Override
    public void receiveEmptyMessage(@Nullable EmptyMessage message) {
        logger.debug("<<<<< COAP EMPTY MESSAGE RECEIVED <<<<<");
        if (message == null) {
            logger.debug("Message is null");
            return;
        }

        logger.debug("Source: {}", message.getSourceContext().getPeerAddress());
        logger.debug("Type: {}", message.getType());
        logger.debug("MID: {}", message.getMID());
    }

    @Override
    public void sendRequest(@Nullable Request request) {
        if (request == null) {
            logger.debug("Request is null");
            return;
        }
        logger.debug("Sending CoAP request to {}: {} {}", request.getDestinationContext().getPeerAddress(),
                request.getCode(), request.getURI());
        logger.debug("Source: {}", request.getLocalAddress());
        logger.debug("Type: {}", request.getType());
        logger.debug("MID: {}", request.getMID());
        logger.debug("Token: {}", request.getTokenString());
        logger.debug("Code: {}", request.getCode());
        logger.debug("Options: {}", request.getOptions());
        if (request.getPayload() != null) {
            logger.debug("Payload (hex): {}", toHex(request.getPayload()));
            logger.debug("Payload (string): {}", request.getPayloadString());
        }
    }

    @Override
    public void sendResponse(@Nullable Response response) {
        // This is normally server-side only, but let's log just in case
        if (response == null) {
            logger.debug("Response is null");
            return;
        }
        logger.debug("Sending CoAP response to {}: {} - {}", response.getDestinationContext().getPeerAddress(),
                response.getCode(), response.getPayloadString());
    }

    @Override
    public void sendEmptyMessage(@Nullable EmptyMessage message) {
        if (message == null) {
            logger.debug("Message is null");
            return;
        }
        // Empty messages are usually ACK or RST
        logger.debug("Sending CoAP empty message to {}: Type={}, MID={}",
                message.getDestinationContext().getPeerAddress(), message.getType(), message.getMID());
    }

    private static String toHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
