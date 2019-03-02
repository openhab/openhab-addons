/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * An event received from Loxone Miniserver with control's state update
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxWsStateUpdateEvent {
    private final LxUuid uuid;
    private Object updateValue;
    private final int size;

    /**
     * Create new state update event from binary message
     *
     * @param isValueEvent true if this event updates double value, false if it updates text message
     * @param data         buffer with binary message received from Miniserver
     * @param offset       offset in buffer where event is expected
     */
    LxWsStateUpdateEvent(boolean isValueEvent, byte data[], int offset) throws IndexOutOfBoundsException {
        uuid = new LxUuid(data, offset);
        int idx = offset + 16;
        if (isValueEvent) {
            updateValue = ByteBuffer.wrap(data, idx, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            size = 24;
            return;
        }

        // unused today: iconUuid = new LxUuid(data, idx);
        idx += 16;

        int textLen = ByteBuffer.wrap(data, idx, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        idx += 4;

        updateValue = new String(data, idx, textLen);
        size = 36 + (textLen % 4 > 0 ? textLen + 4 - (textLen % 4) : textLen);
    }

    /**
     * Get UUID of this state
     *
     * @return UUID of this state
     */
    public LxUuid getUuid() {
        return uuid;
    }

    /**
     * Get current value of this state
     *
     * @return current value of the state or null if state has no value
     */
    public Object getUpdateValue() {
        return updateValue;
    }

    /**
     * Get size of binary representation of state update event in bytes, as received from Loxone Miniserver
     * Used to traverse a binary buffer with more than one state update events
     *
     * @return size of event in binary buffer, in bytes
     */
    int getSize() {
        return size;
    }
}
