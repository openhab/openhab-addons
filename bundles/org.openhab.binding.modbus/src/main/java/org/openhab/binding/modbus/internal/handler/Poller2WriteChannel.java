/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.internal.handler;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.internal.ModbusTransformation;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteCoilRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.Command;

/**
 * Converts a poller-owned channel command into one scalar Modbus write request.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class Poller2WriteChannel {
    private final int address;
    private final ValueType valueType;
    private final ModbusTransformation transformation;
    private final int maxTries;
    private final boolean writeMultiple;
    private final boolean coil;

    public Poller2WriteChannel(int address, ValueType valueType, List<String> writeTransform, int maxTries,
            boolean writeMultiple) {
        this(address, valueType, writeTransform, maxTries, writeMultiple, false);
    }

    public Poller2WriteChannel(int address, ValueType valueType, List<String> writeTransform, int maxTries,
            boolean writeMultiple, boolean coil) {
        this.address = address;
        this.valueType = valueType;
        this.transformation = new ModbusTransformation(writeTransform);
        this.maxTries = maxTries;
        this.writeMultiple = writeMultiple;
        this.coil = coil;
    }

    public Optional<ModbusWriteRequestBlueprint> requestFor(Command command, int slaveId) {
        Command magnitudeCommand = command instanceof QuantityType<?> quantity
                ? new DecimalType(quantity.toBigDecimal())
                : command;
        String output = transformation.transform(magnitudeCommand.toString());
        Optional<Command> transformed = ModbusTransformation.tryConvertToCommand(output);
        if (transformed.isEmpty()) {
            return Optional.empty();
        }
        if (coil) {
            return ModbusBitUtilities.translateCommand2Boolean(transformed.get()).<ModbusWriteRequestBlueprint> map(
                    value -> new ModbusWriteCoilRequestBlueprint(slaveId, address, value, writeMultiple, maxTries));
        }
        try {
            ModbusRegisterArray registers = ModbusBitUtilities.commandToRegisters(transformed.get(), valueType);
            return Optional.of(new ModbusWriteRegisterRequestBlueprint(slaveId, address, registers,
                    writeMultiple || registers.size() > 1, maxTries));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean isCoil() {
        return coil;
    }
}
