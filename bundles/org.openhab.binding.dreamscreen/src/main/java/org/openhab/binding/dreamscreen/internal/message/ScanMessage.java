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

/**
 * {@link ScanMessage} handles the Scan Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class ScanMessage extends DreamScreenMessage {
    private static final byte COMMAND_UPPER = 0x01;
    private static final byte COMMAND_LOWER = 0x03;

    public ScanMessage() {
        super((byte) 0xFF, COMMAND_UPPER, COMMAND_LOWER, new byte[0]);
    }

    @Override
    public String toString() {
        return "Scan";
    }
}
