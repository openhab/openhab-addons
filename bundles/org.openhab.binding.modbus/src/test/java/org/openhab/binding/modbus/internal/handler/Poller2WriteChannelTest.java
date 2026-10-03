/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.library.types.QuantityType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class Poller2WriteChannelTest {

    @Test
    public void createsHoldingWriteFromQuantityMagnitudeAndConstantTransform() {
        Poller2WriteChannel channel = new Poller2WriteChannel(42, ValueType.INT16, List.of("17"), 2, false);

        ModbusWriteRequestBlueprint request = channel.requestFor(new QuantityType<>("4.5 kW"), 9).orElseThrow();

        ModbusWriteRegisterRequestBlueprint registerRequest = assertInstanceOf(ModbusWriteRegisterRequestBlueprint.class,
                request);
        assertEquals(42, registerRequest.getReference());
        assertEquals(2, registerRequest.getMaxTries());
        assertEquals(17, registerRequest.getRegisters().getRegister(0));
    }

    @Test
    public void doesNotCreateWriteWhenTransformOutputIsNotACommand() {
        Poller2WriteChannel channel = new Poller2WriteChannel(42, ValueType.INT16, List.of("not-a-command"), 2, false);

        assertTrue(channel.requestFor(new QuantityType<>("4.5 kW"), 9).isEmpty());
    }
}
