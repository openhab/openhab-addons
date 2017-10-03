/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.messages;

import java.nio.ByteBuffer;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

/**
 * This defines the interface that every message class should implement.
 *
 * @author Mike Jagdis - Initial contribution
 */
public interface LightifyMessage {

    static int getSeqNo(ByteBuffer buffer) {
        return buffer.getInt(4);
    }

    /**
     * Convert the message data to a string.
     * Used for logging purposes.
     *
     */
    String toString();

    /**
     * Is this message used for periodic polling?
     * If so it will only be logged as trace rather than debug.
     *
     * @return true if used for periodic polling.
     */
    public boolean isPoller();

    // ****************************************
    //      Request transmission section
    // ****************************************

    /**
     * Set the sequence number.
     *
     * @param seqNo sequence number
     */
    public void setSeqNo(int seqNo);

    /**
     * Encode a message for transmission.
     *
     * @return the encoded message.
     */
    ByteBuffer encodeMessage() throws LightifyMessageTooLongException;

    // ****************************************
    //        Response handling section
    // ****************************************

    /**
     * Get the sequence number.
     *
     * @return sequence number
     */
    public int getSeqNo();

    /**
     * Handle a received response message.
     * The handler is responsible for sending the relevant commands and/or
     * state updates on the channel(s) for the affected thing(s).
     *
     * @param bridgeHandler the handler for the gateway that sent the message.
     * @param data the encoded message.
     */
    boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException;
}
