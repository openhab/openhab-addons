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
package org.openhab.binding.philipsair.internal.connection;

import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.interceptors.MessageInterceptor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;
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
        logger.trace("<<<<< COAP RESPONSE RECEIVED <<<<<");
        if (response == null) {
            logger.trace("Response is null");
            return;
        }
        logger.trace("Source: {}", response.getSourceContext().getPeerAddress());
        logger.trace("Type: {}", response.getType());
        logger.trace("MID: {}", response.getMID());
        logger.trace("Token: {}", response.getTokenString());
        logger.trace("Code: {}", response.getCode());
        logger.trace("Options: {}", response.getOptions());
        if (response.getPayload() != null) {
            logger.trace("Payload (hex): {}", HexUtils.bytesToHex(response.getPayload()));
            logger.trace("Payload (string): {}", response.getPayloadString());
        }
    }

    @Override
    public void receiveEmptyMessage(@Nullable EmptyMessage message) {
        logger.trace("<<<<< COAP EMPTY MESSAGE RECEIVED <<<<<");
        if (message == null) {
            logger.trace("Message is null");
            return;
        }

        logger.trace("Source: {}", message.getSourceContext().getPeerAddress());
        logger.trace("Type: {}", message.getType());
        logger.trace("MID: {}", message.getMID());
    }

    @Override
    public void sendRequest(@Nullable Request request) {
        if (request == null) {
            logger.trace("Request is null");
            return;
        }
        logger.trace("Sending CoAP request to {}: {} {}", request.getDestinationContext().getPeerAddress(),
                request.getCode(), request.getURI());
        logger.trace("Source: {}", request.getLocalAddress());
        logger.trace("Type: {}", request.getType());
        logger.trace("MID: {}", request.getMID());
        logger.trace("Token: {}", request.getTokenString());
        logger.trace("Code: {}", request.getCode());
        logger.trace("Options: {}", request.getOptions());
        if (request.getPayload() != null) {
            logger.trace("Payload (hex): {}", HexUtils.bytesToHex(request.getPayload()));
            logger.trace("Payload (string): {}", request.getPayloadString());
        }
    }

    @Override
    public void sendResponse(@Nullable Response response) {
        // This is normally server-side only, but let's log just in case
        if (response == null) {
            logger.trace("Response is null");
            return;
        }
        logger.trace("Sending CoAP response to {}: {} - {}", response.getDestinationContext().getPeerAddress(),
                response.getCode(), response.getPayloadString());
    }

    @Override
    public void sendEmptyMessage(@Nullable EmptyMessage message) {
        if (message == null) {
            logger.trace("Message is null");
            return;
        }
        // Empty messages are usually ACK or RST
        logger.trace("Sending CoAP empty message to {}: Type={}, MID={}",
                message.getDestinationContext().getPeerAddress(), message.getType(), message.getMID());
    }
}
