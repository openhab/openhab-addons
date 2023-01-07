/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.modbus.internal;

import static org.openhab.binding.modbus.ModbusBindingConstants.BINDING_ID;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ModbusBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusBindingConstantsInternal {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MODBUS_TCP = new ThingTypeUID(BINDING_ID, "tcp");
    public static final ThingTypeUID THING_TYPE_MODBUS_SERIAL = new ThingTypeUID(BINDING_ID, "serial");
    public static final ThingTypeUID THING_TYPE_MODBUS_POLLER = new ThingTypeUID(BINDING_ID, "poller");
    public static final ThingTypeUID THING_TYPE_MODBUS_DATA = new ThingTypeUID(BINDING_ID, "data");

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_DATETIME = "datetime";
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_NUMBER = "number";
    public static final String CHANNEL_STRING = "string";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_LAST_READ_SUCCESS = "lastReadSuccess";
    public static final String CHANNEL_LAST_READ_ERROR = "lastReadError";
    public static final String CHANNEL_LAST_WRITE_SUCCESS = "lastWriteSuccess";
    public static final String CHANNEL_LAST_WRITE_ERROR = "lastWriteError";

    public static final String[] DATA_CHANNELS = { CHANNEL_SWITCH, CHANNEL_CONTACT, CHANNEL_DATETIME, CHANNEL_DIMMER,
            CHANNEL_NUMBER, CHANNEL_STRING, CHANNEL_ROLLERSHUTTER };

    public static final String[] DATA_CHANNELS_TO_COPY_FROM_READ_TO_READWRITE = { CHANNEL_SWITCH, CHANNEL_CONTACT,
            CHANNEL_DATETIME, CHANNEL_DIMMER, CHANNEL_NUMBER, CHANNEL_STRING, CHANNEL_ROLLERSHUTTER,
            CHANNEL_LAST_READ_SUCCESS, CHANNEL_LAST_READ_ERROR };

    public static final String[] DATA_CHANNELS_TO_DELEGATE_COMMAND_FROM_READWRITE_TO_WRITE = { CHANNEL_SWITCH,
            CHANNEL_CONTACT, CHANNEL_DATETIME, CHANNEL_DIMMER, CHANNEL_NUMBER, CHANNEL_STRING, CHANNEL_ROLLERSHUTTER };

    public static final String WRITE_TYPE_COIL = "coil";
    public static final String WRITE_TYPE_HOLDING = "holding";

    public static final String READ_TYPE_COIL = "coil";
    public static final String READ_TYPE_HOLDING_REGISTER = "holding";
    public static final String READ_TYPE_DISCRETE_INPUT = "discrete";
    public static final String READ_TYPE_INPUT_REGISTER = "input";

    public static final Map<String, ModbusReadFunctionCode> READ_FUNCTION_CODES = new HashMap<>();
    static {
        READ_FUNCTION_CODES.put(READ_TYPE_COIL, ModbusReadFunctionCode.READ_COILS);
        READ_FUNCTION_CODES.put(READ_TYPE_DISCRETE_INPUT, ModbusReadFunctionCode.READ_INPUT_DISCRETES);
        READ_FUNCTION_CODES.put(READ_TYPE_INPUT_REGISTER, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
        READ_FUNCTION_CODES.put(READ_TYPE_HOLDING_REGISTER, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
    }
}
