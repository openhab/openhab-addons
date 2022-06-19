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
package org.openhab.binding.rotel.internal.protocol;

import static org.openhab.binding.rotel.internal.RotelBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.binding.rotel.internal.communication.RotelCommand;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for handling a Rotel protocol (build of command messages, decoding of incoming data)
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public abstract class RotelAbstractProtocolHandler {

    private final Logger logger = LoggerFactory.getLogger(RotelAbstractProtocolHandler.class);

    protected final RotelModel model;

    private final List<RotelMessageEventListener> listeners = new ArrayList<>();

    /**
     * Constructor
     *
     * @param model the Rotel model in use
     */
    public RotelAbstractProtocolHandler(RotelModel model) {
        this.model = model;
    }

    public abstract RotelProtocol getProtocol();

    /**
     * Build the message associated to a Rotel command
     *
     * @param cmd the command to execute
     * @param value the integer value to consider for volume, bass or treble adjustment
     *
     * @throws RotelException - In case the command is not supported by the protocol
     */
    public abstract byte[] buildCommandMessage(RotelCommand cmd, @Nullable Integer value) throws RotelException;

    public abstract void handleIncomingData(byte[] inDataBuffer, int length);

    public void handleInIncomingError() {
        dispatchKeyValue(KEY_ERROR, MSG_VALUE_ON);
    }

    /**
     * Analyze an incoming message and dispatch corresponding (key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    protected void handleIncomingMessage(byte[] incomingMessage) {
        logger.debug("handleIncomingMessage: bytes {}", HexUtils.bytesToHex(incomingMessage));

        try {
            validateResponse(incomingMessage);
        } catch (RotelException e) {
            return;
        }

        handleValidMessage(incomingMessage);
    }

    /**
     * Validate the content of a feedback message
     *
     * @param responseMessage the buffer containing the feedback message
     *
     * @throws RotelException - If the message has unexpected content
     */
    protected abstract void validateResponse(byte[] responseMessage) throws RotelException;

    /**
     * Analyze a valid HEX message and dispatch corresponding (key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    protected abstract void handleValidMessage(byte[] incomingMessage);

    /**
     * Add a listener to the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void addEventListener(RotelMessageEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void removeEventListener(RotelMessageEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Dispatch an event (key, value) to the event listeners
     *
     * @param key the key
     * @param value the value
     */
    protected void dispatchKeyValue(String key, String value) {
        RotelMessageEvent event = new RotelMessageEvent(this, key, value);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onNewMessageEvent(event);
        }
    }
}
