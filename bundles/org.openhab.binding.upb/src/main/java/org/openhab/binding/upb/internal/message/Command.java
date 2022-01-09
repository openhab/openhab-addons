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
package org.openhab.binding.upb.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An enum of possible commands.
 *
 * @author cvanorman - Initial contribution
 */
@NonNullByDefault
public enum Command {
    NULL(0),
    ACTIVATE(0x20),
    DEACTIVATE(0x21),
    GOTO(0x22),
    START_FADE(0x23),
    STOP_FADE(0x24),
    BLINK(0x25),
    REPORT_STATE(0x30),
    STORE_STATE(0x31),
    DEVICE_STATE(0x86);

    private final byte mdid;

    Command(final int mdid) {
        this.mdid = (byte) mdid;
    }

    /**
     * @return the protocol Message Data ID (MDID) for this Command
     */
    public byte toByte() {
        return mdid;
    }

    /**
     * Returns the Command for a given Message Data ID byte.
     *
     * @param value the MDID byte
     * @return the Command for the given MDID
     */
    public static Command valueOf(final byte value) {
        for (final Command cmd : values()) {
            if (cmd.toByte() == value) {
                return cmd;
            }
        }
        return NULL;
    }
}
