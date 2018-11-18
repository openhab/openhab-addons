/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication.messages;

/**
 * Constants representing packet headers / messages which are easier written as static final byte arrays
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class IpMessagesConstants {
    public static final byte[] UNKNOWN_IP150_REQUEST_MESSAGE01 = { 0x0A, 0x50, 0x08, 0x00, 0x00, 0x01, 0x00, 0x00, 0x59 };

    public static final byte[] EPROM_REQUEST_HEADER = { (byte) 0xAA, 0x08, 0x00, 0x04, 0x08, 0x00, 0x00, 0x14, (byte) 0xEE,
            (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE };
}
