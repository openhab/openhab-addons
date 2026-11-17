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
package org.openhab.binding.meteostick.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Assembles complete lines from a stream of received bytes.
 * <p>
 * A line is terminated by a carriage return; line feeds are ignored. Lines longer than
 * {@link #MAX_LINE_LENGTH} bytes are discarded as a whole, so a truncated line is never reported as a valid packet.
 *
 * @author Jens Erat - Initial contribution
 */
@NonNullByDefault
class LineBuffer {
    static final int MAX_LINE_LENGTH = 100;

    private final byte[] buffer = new byte[MAX_LINE_LENGTH];
    private int length;
    private boolean overflow;
    private boolean dropped;

    /**
     * Feeds one received byte into the buffer.
     *
     * @param value the received byte
     * @return the completed line without its terminator, or {@code null} if no complete line is available yet
     */
    @Nullable
    String accept(int value) {
        if (value == '\r') {
            if (overflow) {
                overflow = false;
                dropped = true;
                length = 0;
                return null;
            }
            if (length == 0) {
                return null;
            }
            String line = new String(buffer, 0, length);
            length = 0;
            return line;
        }

        if (value == '\n') {
            return null;
        }

        if (length == buffer.length) {
            overflow = true;
            return null;
        }

        buffer[length++] = (byte) value;
        return null;
    }

    /**
     * Reports whether an over-long line was discarded since this method was last called.
     *
     * @return {@code true} if a line was dropped
     */
    boolean consumeDroppedLine() {
        boolean result = dropped;
        dropped = false;
        return result;
    }
}
