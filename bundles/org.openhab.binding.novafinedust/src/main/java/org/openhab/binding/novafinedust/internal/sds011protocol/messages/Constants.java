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
package org.openhab.binding.novafinedust.internal.sds011protocol.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants for sensor messages
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class Constants {

    private Constants() {
    }

    public static final byte MESSAGE_START = (byte) 0xAA;
    public static final int MESSAGE_START_AS_INT = 170;
    public static final byte MESSAGE_END = (byte) 0xAB;

    public static final int REPLY_LENGTH = 10;

    public static final byte QUERY_ACTION = (byte) 0x00;
    public static final byte SET_ACTION = (byte) 0x01;
}
