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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class Poller2WriteChannelConfigurationTest {

    @Test
    public void createsOptionalHoldingWriteEndpointWithPerWriteSettings() {
        Poller2WriteChannelConfiguration configuration = Poller2WriteChannelConfiguration.create("setpoint",
                Map.of("writeStart", "42", "writeValueType", "int16", "writeTransform", "17", "writeMaxTries", 2,
                        "writeMultipleEvenWithSingleRegisterOrCoil", true),
                ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);

        Poller2WriteChannel channel = configuration.channel().orElseThrow();
        assertEquals(42, channel.requestFor(new org.openhab.core.library.types.DecimalType(1), 9).orElseThrow()
                .getReference());
        assertTrue(channel.requestFor(new org.openhab.core.library.types.DecimalType(1), 9).isPresent());
    }

    @Test
    public void leavesChannelReadOnlyWhenNoWriteEndpointIsConfigured() {
        Poller2WriteChannelConfiguration configuration = Poller2WriteChannelConfiguration.create("value", Map.of(),
                ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER);

        assertTrue(configuration.channel().isEmpty());
    }

    @Test
    public void defaultsCoilWriteToBitValueType() {
        Poller2WriteChannelConfiguration configuration = Poller2WriteChannelConfiguration.create("output",
                Map.of("writeStart", "7"), ModbusBindingConstantsInternal.READ_TYPE_COIL);

        assertEquals(ValueType.BIT, configuration.valueType().orElseThrow());
    }
}
