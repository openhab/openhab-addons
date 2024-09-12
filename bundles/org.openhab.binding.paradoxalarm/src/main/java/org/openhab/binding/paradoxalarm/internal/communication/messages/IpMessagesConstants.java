/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants representing packet headers / messages which are easier written as static final byte arrays
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class IpMessagesConstants {

    public static final byte[] UNKNOWN_IP150_REQUEST_MESSAGE01 = { 0x0A, 0x50, 0x08, 0x00, 0x00, 0x01, 0x00, 0x00,
            0x59 };

    public static final byte[] EPROM_REQUEST_HEADER = { (byte) 0xAA, 0x08, 0x00, 0x04, 0x08, 0x00, 0x00, 0x14,
            (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE };

    public static final byte[] LOGOUT_MESAGE_BYTES = new byte[] { 0x00, 0x07, 0x05, 0x00, 0x00, 0x00, 0x00 };
}
