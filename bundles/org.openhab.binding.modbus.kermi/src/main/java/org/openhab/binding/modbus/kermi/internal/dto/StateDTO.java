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
package org.openhab.binding.modbus.kermi.internal.dto;

import org.openhab.binding.modbus.kermi.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;

/**
 * The {@link StateDTO} Data object for Kermi Xcenter State Block
 *
 * @author Kai Neuhaus - Initial contribution
 */
public class StateDTO implements Data {

    public StringType globalState = STATE_UNKOWN;
    public DecimalType globalStateId = new DecimalType(-1);

    // State definitions
    public static final StringType STATE_STANDBY = StringType.valueOf("Standby");
    public static final StringType STATE_ALARM = StringType.valueOf("Alarm");
    public static final StringType STATE_TWE = StringType.valueOf("TWE");
    public static final StringType STATE_KUEHLEN = StringType.valueOf("Kuehlen");
    public static final StringType STATE_HEIZEN = StringType.valueOf("Heizen");
    public static final StringType STATE_ABTAUUNG = StringType.valueOf("Abtauung");
    public static final StringType STATE_VORBEREITUNG = StringType.valueOf("Vorbereitung");
    public static final StringType STATE_BLOCKIERT = StringType.valueOf("Blockiert");
    public static final StringType STATE_EVU_SPERRE = StringType.valueOf("EVU Sperre");
    public static final StringType STATE_NICHT_VERFUEGBAR = StringType.valueOf("Nicht verfuegbar");
    public static final StringType STATE_UNKOWN = StringType.valueOf("Status unknown");
    public static final StringType[] STATE_ARRAY = new StringType[] { STATE_STANDBY, STATE_ALARM, STATE_TWE,
            STATE_KUEHLEN, STATE_HEIZEN, STATE_ABTAUUNG, STATE_VORBEREITUNG, STATE_BLOCKIERT, STATE_EVU_SPERRE,
            STATE_NICHT_VERFUEGBAR };

    public StateDTO(byte[] bArray) {

        int status = ModbusBitUtilities.extractUInt16(bArray, 0);

        globalStateId = new DecimalType(status);

        if (status >= 0 && status < 10) {
            globalState = STATE_ARRAY[status];
        } else {
            globalState = STATE_UNKOWN;
        }
        //
        // // index handling to calculate the correct start index
        // ValueBuffer wrap = ValueBuffer.wrap(bArray);
        //
        // // int32_swap value = 4 byte
        // int globalState = wrap.getUInt16();
    }
}
