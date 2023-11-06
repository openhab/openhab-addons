/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.types;

/**
 * A header of a binary message received from Loxone Miniserver on a websocket connection.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxWsBinaryHeader {
    /**
     * Type of a binary message received from the Miniserver
     *
     * @author Pawel Pieczul
     *
     */
    public enum LxWsMessageType {
        /**
         * Text message - jetty websocket client will pass it on automatically to a callback
         */
        TEXT_MESSAGE,
        /**
         * Binary file
         */
        BINARY_FILE,
        /**
         * A set of value states for controls that changed their state
         */
        EVENT_TABLE_OF_VALUE_STATES,
        /**
         * A set of text states for controls that changed their state
         */
        EVENT_TABLE_OF_TEXT_STATES,
        EVENT_TABLE_OF_DAYTIMER_STATES,
        OUT_OF_SERVICE_INDICATOR,
        /**
         * Response to keepalive request message
         */
        KEEPALIVE_RESPONSE,
        EVENT_TABLE_OF_WEATHER_STATES,
        /**
         * Unknown header
         */
        UNKNOWN
    }

    private LxWsMessageType type = LxWsMessageType.UNKNOWN;

    /**
     * Create header from binary buffer at a given offset
     *
     * @param buffer buffer with received message
     * @param offset offset in bytes at which header is expected
     */
    public LxWsBinaryHeader(byte[] buffer, int offset) throws IndexOutOfBoundsException {
        if (buffer[offset] != 0x03) {
            return;
        }
        switch (buffer[offset + 1]) {
            case 0:
                type = LxWsMessageType.TEXT_MESSAGE;
                break;
            case 1:
                type = LxWsMessageType.BINARY_FILE;
                break;
            case 2:
                type = LxWsMessageType.EVENT_TABLE_OF_VALUE_STATES;
                break;
            case 3:
                type = LxWsMessageType.EVENT_TABLE_OF_TEXT_STATES;
                break;
            case 4:
                type = LxWsMessageType.EVENT_TABLE_OF_DAYTIMER_STATES;
                break;
            case 5:
                type = LxWsMessageType.OUT_OF_SERVICE_INDICATOR;
                break;
            case 6:
                type = LxWsMessageType.KEEPALIVE_RESPONSE;
                break;
            case 7:
                type = LxWsMessageType.EVENT_TABLE_OF_WEATHER_STATES;
                break;
            default:
                type = LxWsMessageType.UNKNOWN;
                break;
        }
        // These fields are not used today , but left it for future reference
        // estimated = ((buffer[offset + 2] & 0x01) != 0);
        // length = ByteBuffer.wrap(buffer, offset + 3, 4).getInt();
    }

    public LxWsMessageType getType() {
        return type;
    }
}
