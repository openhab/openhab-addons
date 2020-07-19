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
package org.openhab.binding.e3dc.internal.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link E3DCModbusConstans} Variables for register handling.
 * The numbers are taken from E3DC Modbus Spec Chapter 3.1 page 14 ff
 * Registers start from 0 (not 1!) so from the documented registers subtract 1
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCModbusConstans {
    public static final int INFO_REG_START = 0;
    public static final int INFO_REG_SIZE = 67;
    public static final int POWER_REG_START = 67;
    public static final int POWER_REG_SIZE = 16;
    public static final int EMS_REG_START = 83;
    public static final int EMS_REG_SIZE = 2;
    public static final int WALLBOX_REG_START = 87;
    public static final int WALLBOX_REG_SIZE = 8;
    public static final int STRINGS_REG_START = 95;
    public static final int STRINGS_REG_SIZE = 9;
    public static final int REGISTER_LENGTH = 104;
    public static final int INFO_POLL_REFRESH_TIME_MS = 60 * 60 * 1000;
}
