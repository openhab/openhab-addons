/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * An event received from Loxone Miniserver with control's state update
 *
 * @author Pawel Pieczul - initial commit
 *
 */
class LxWsStateUpdateEvent {
    private LxUuid uuid;
    private double value = 0;
    private String text = null;

    /**
     * Create new state update event from binary message
     *
     * @param data
     *            buffer with binary message received from Miniserver
     * @param offset
     *            offset in buffer where event is expected
     */
    LxWsStateUpdateEvent(byte data[], int offset) {
        uuid = new LxUuid(data, offset);
        value = ByteBuffer.wrap(data, offset + 16, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    /**
     * Create new state update event from parameters - version for value states
     *
     * @param uuid
     *            UUID of the state to update
     * @param value
     *            value of the state
     */
    LxWsStateUpdateEvent(LxUuid uuid, double value) {
        this.uuid = uuid;
        this.value = value;
    }

    /**
     * Create new state update event from parameters - version for text states
     *
     * @param uuid
     *            UUID of the state to update
     * @param text
     *            text value of the state
     */
    LxWsStateUpdateEvent(LxUuid uuid, String text) {
        this.uuid = uuid;
        this.text = text;
    }

    /**
     * Get UUID of this state
     *
     * @return
     *         UUID of this state
     */
    LxUuid getUuid() {
        return uuid;
    }

    /**
     * Get current value of this state
     *
     * @return
     *         current value of the state
     */
    double getValue() {
        return value;
    }

    /**
     * Get current text value of this state
     * 
     * @return
     *         current text value of this state
     */
    String getText() {
        return text;
    }

    /**
     * Get size of binary representation of state update event in bytes, as received from Loxone Miniserver
     * Used to traverse a binary buffer with more than one state update events
     *
     * @return
     *         size of event in binary buffer, in bytes
     */
    int getSize() {
        // text events not implemented yet, this will change when they are
        return 24;
    }
}
