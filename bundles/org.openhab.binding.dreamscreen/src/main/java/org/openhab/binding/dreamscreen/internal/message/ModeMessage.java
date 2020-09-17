/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dreamscreen.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dreamscreen.internal.model.DreamScreenMode;

/**
 * {@link ModeMessage} handles the Mode Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class ModeMessage extends DreamScreenMessage {
    static final byte COMMAND_UPPER = 0x03;
    static final byte COMMAND_LOWER = 0x01;

    protected ModeMessage(final byte[] data) {
        super(data);
    }

    public ModeMessage(byte group, byte mode) {
        super(group, COMMAND_UPPER, COMMAND_LOWER, new byte[] { mode });
    }

    static boolean matches(final byte[] data) {
        return matches(data, COMMAND_UPPER, COMMAND_LOWER);
    }

    public byte getMode() {
        return this.payload.get(0);
    }

    @Override
    public String toString() {
        final DreamScreenMode mode = DreamScreenMode.fromDevice(getMode());
        return "Mode " + (mode == null ? "SLEEP" : mode.name());
    }
}
