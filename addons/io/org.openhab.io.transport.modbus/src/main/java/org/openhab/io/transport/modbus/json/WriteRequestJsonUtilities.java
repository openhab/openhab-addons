/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.json;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.transport.modbus.BasicBitArray;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.BasicModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusRegister;
import org.openhab.io.transport.modbus.BasicModbusWriteCoilRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusWriteFunctionCode;
import org.openhab.io.transport.modbus.BasicModbusWriteRegisterRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusWriteRequestBlueprint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Utilities for converting JSON to {@link ModbusWriteRequestBlueprint}
 *
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class WriteRequestJsonUtilities {
    /**
     * Constant for the function code key in the JSON
     */
    public static final String JSON_FUNCTION_CODE = "functionCode";
    /**
     * Constant for the write address key in the JSON
     */
    public static final String JSON_ADDRESS = "address";
    /**
     * Constant for the value key in the JSON
     */
    public static final String JSON_VALUE = "value";
    /**
     * Constant for the maxTries key in the JSON
     */
    public static final String JSON_MAX_TRIES = "maxTries";

    /**
     * Default maxTries when it has not been specified
     */
    public static final int DEFAULT_MAX_TRIES = 3;

    private static final JsonParser PARSER = new JsonParser();

    private WriteRequestJsonUtilities() {
        throw new NotImplementedException();
    }

    /**
     * Parse JSON string to collection of {@link ModbusWriteRequestBlueprint}
     *
     * JSON string should represent a JSON array, with JSON objects. Each JSON object represents a write request. The
     * JSON object must have the following keys
     * - functionCode: numeric function code
     * - address: reference or start address of the write
     * - value: array of data to be written. Use zero and one when writing coils. With registers, each number
     * corresponds to register's 16 bit data.
     * - maxTries: number of tries with the write in case of errors
     *
     *
     * @param unitId unit id for the constructed {@link ModbusWriteRequestBlueprint}
     * @param jsonString json to be parsed in string format
     * @return collection of {@link ModbusWriteRequestBlueprint} representing the json
     * @throws IllegalArgumentException in case of unexpected function codes
     * @throws IllegalStateException in case of parsing errors and unexpected json structure
     *
     * @see WriteRequestJsonUtilities.JSON_FUNCTION_CODE
     * @see WriteRequestJsonUtilities.JSON_ADDRESS
     * @see WriteRequestJsonUtilities.JSON_VALUE
     * @see WriteRequestJsonUtilities.JSON_MAX_TRIES
     */
    public static Collection<ModbusWriteRequestBlueprint> fromJson(int unitId, String jsonString) {
        JsonArray jsonArray = PARSER.parse(jsonString).getAsJsonArray();
        if (jsonArray.size() == 0) {
            return new LinkedList<>();
        }
        Deque<ModbusWriteRequestBlueprint> writes = new LinkedList<>();
        jsonArray.forEach(writeElem -> {
            writes.add(constructBluerint(unitId, writeElem));
        });
        return writes;
    }

    private static ModbusWriteRequestBlueprint constructBluerint(int unitId, JsonElement arrayElement) {
        final JsonObject writeObject;
        try {
            writeObject = arrayElement.getAsJsonObject();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("JSON array contained something else than a JSON object!", e);
        }
        @Nullable
        JsonElement functionCode = writeObject.get(JSON_FUNCTION_CODE);
        @Nullable
        JsonElement address = writeObject.get(JSON_ADDRESS);
        @Nullable
        JsonElement maxTries = writeObject.get(JSON_MAX_TRIES);
        @Nullable
        JsonArray valuesElem;

        try {
            valuesElem = writeObject.get(JSON_VALUE).getAsJsonArray();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(String.format("JSON object '%s' is not an JSON array!", JSON_VALUE), e);
        }
        return constructBluerint(unitId, functionCode, address, maxTries, valuesElem);
    }

    private static ModbusWriteRequestBlueprint constructBluerint(int unitId, @Nullable JsonElement functionCodeElem,
            @Nullable JsonElement addressElem, @Nullable JsonElement maxTriesElem, @Nullable JsonArray valuesElem) {
        int functionCodeNumeric;
        if (functionCodeElem == null || functionCodeElem.isJsonNull()) {
            throw new IllegalStateException(String.format("Value for '%s' is invalid", JSON_FUNCTION_CODE));
        }
        try {
            functionCodeNumeric = functionCodeElem.getAsInt();
        } catch (ClassCastException | IllegalStateException e) {
            throw new IllegalStateException(String.format("Value for '%s' is invalid", JSON_FUNCTION_CODE), e);
        }
        ModbusWriteFunctionCode functionCode = ModbusWriteFunctionCode.fromFunctionCode(functionCodeNumeric);
        int address;
        if (addressElem == null || addressElem.isJsonNull()) {
            throw new IllegalStateException(String.format("Value for '%s' is invalid", JSON_ADDRESS));
        }
        try {
            address = addressElem.getAsInt();
        } catch (ClassCastException | IllegalStateException e) {
            throw new IllegalStateException(String.format("Value for '%s' is invalid", JSON_ADDRESS), e);
        }
        int maxTries;
        if (maxTriesElem == null || maxTriesElem.isJsonNull()) {
            // Go with default
            maxTries = DEFAULT_MAX_TRIES;
        } else {
            try {
                maxTries = maxTriesElem.getAsInt();
            } catch (ClassCastException | IllegalStateException e) {
                throw new IllegalStateException(String.format("Value for '%s' is invalid", JSON_MAX_TRIES), e);
            }
        }

        if (valuesElem == null || valuesElem.isJsonNull()) {
            throw new IllegalArgumentException(String.format("Expecting non-null value, got: %s", valuesElem));
        }

        AtomicBoolean writeSingle = new AtomicBoolean(false);
        switch (functionCode) {
            case WRITE_COIL:
                writeSingle.set(true);
                if (valuesElem.size() != 1) {
                    throw new IllegalArgumentException(String
                            .format("Expecting single value with functionCode=%s, got: %d", functionCode, valuesElem));
                }
                // fall-through to WRITE_MULTIPLE_COILS
            case WRITE_MULTIPLE_COILS:
                if (valuesElem.size() == 0) {
                    throw new IllegalArgumentException("Must provide at least one coil");
                }
                BasicBitArray bits = new BasicBitArray(valuesElem.size());
                for (int i = 0; i < valuesElem.size(); i++) {
                    bits.setBit(i, valuesElem.get(i).getAsInt() != 0);
                }
                return new BasicModbusWriteCoilRequestBlueprint(unitId, address, bits, !writeSingle.get(), maxTries);
            case WRITE_SINGLE_REGISTER:
                writeSingle.set(true);
                if (valuesElem.size() != 1) {
                    throw new IllegalArgumentException(String
                            .format("Expecting single value with functionCode=%s, got: %d", functionCode, valuesElem));
                }
                // fall-through to WRITE_MULTIPLE_REGISTERS
            case WRITE_MULTIPLE_REGISTERS: {
                ModbusRegister[] registers = new ModbusRegister[valuesElem.size()];
                if (registers.length == 0) {
                    throw new IllegalArgumentException("Must provide at least one register");
                }
                for (int i = 0; i < valuesElem.size(); i++) {
                    registers[i] = new BasicModbusRegister(valuesElem.get(i).getAsInt());
                }
                return new BasicModbusWriteRegisterRequestBlueprint(unitId, address,
                        new BasicModbusRegisterArray(registers), !writeSingle.get(), maxTries);
            }
            default:
                throw new IllegalArgumentException("Unknown function code");
        }
    }

}
