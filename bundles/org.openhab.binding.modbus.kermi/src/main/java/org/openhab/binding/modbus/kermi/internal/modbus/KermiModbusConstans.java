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
package org.openhab.binding.modbus.kermi.internal.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KermiModbusConstans} Variables for register handling.
 * The numbers are taken from Kermi ExcelChart for Modbus
 * (Registers start from 0 (not 1!) so from the documented registers subtract 1)
 *
 * @author Kai Neuhaus - Initial contribution
 */
@NonNullByDefault
public class KermiModbusConstans {

    public static final int WARMUP_TIME_LONG = 1500;
    public static final int WARMUP_TIME_SHORT = 500;

    // "String" registers at the beginning shall be read with very low frequency - 1 hour
    public static final int SLOW_POLL_REFRESH_TIME_MS = 60 * 60 * 1000;

    // poll every 30 seconds
    public static final int STATE_POLL_REFRESH_TIME_MS = 30 * 1000;

    // Constants where a certain Block starts and block size. Note: General offset is -1 so INFO_REG from E3DC Modbus
    // Spec starts at 1 but it's Register 0!
    public static final int XCENTER_DATA_REG_START = 1;
    public static final int XCENTER_DATA_REG_SIZE = 111;

    public static final int ENERGY_SOURCE_REG_START = 1;
    public static final int ENERGY_SOURCE_REG_SIZE = 3;

    public static final int CHARGING_CIRCUIT_REG_START = 50;
    public static final int CHARGING_CIRCUIT_REG_SIZE = 3;

    public static final int POWER_REG_START = 100;
    public static final int POWER_REG_SIZE = 12;

    /** INFO BLOCK **/
    public static final int WORK_HOURS_REG_START = 150;
    public static final int WORK_HOURS_REG_SIZE = 3;

    public static final int STATE_REG_START = 200;
    public static final int STATE_REG_SIZE = 1;

    public static final int ALARM_REG_START = 250;
    public static final int ALARM_REG_SIZE = 1;
    /** END INFO BLOCK **/

    /** PV BLOCK **/
    public static final int PV_MODULATION_REG_START = 300;
    public static final int PV_MODULATION_REG_SIZE = 4;

    public static final int DATA_REGISTER_LENGTH = 111;
}
