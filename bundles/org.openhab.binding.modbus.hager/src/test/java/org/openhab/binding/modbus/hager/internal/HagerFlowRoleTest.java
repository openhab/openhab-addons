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
package org.openhab.binding.modbus.hager.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.BitFlag;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HagerFlowRoleTest} verifies the register maps against the rules of the Modbus protocol and verifies the
 * configuration validation of the roles.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HagerFlowRoleTest {

    /** Highest address a Modbus register request can address. */
    private static final int MAX_ADDRESS = 0xffff;

    @Test
    public void testRoleLookup() {
        for (HagerFlowRole role : HagerFlowRole.values()) {
            assertTrue(HagerFlowBindingConstants.SUPPORTED_THING_TYPES.contains(role.thingType()));
            assertEquals(Optional.of(role), HagerFlowRole.of(role.thingType()));
        }
        assertTrue(HagerFlowRole.of(new ThingTypeUID("modbus", "unknown")).isEmpty());
    }

    @Test
    public void testRegisterAddressesAreInsideModbusRange() {
        for (HagerFlowRole role : HagerFlowRole.values()) {
            assertFalse(role.registers().isEmpty(), role + " has no registers");
            for (Register register : role.registers()) {
                String id = role + "." + register.channelId();
                assertTrue(register.address() >= 0, id + " has a negative address");
                assertTrue(register.length() > 0, id + " has no length");
                assertTrue(register.address() + register.length() <= MAX_ADDRESS + 1,
                        id + " exceeds the address space");
            }
        }
    }

    @Test
    public void testRegistersDoNotOverlap() {
        for (HagerFlowRole role : HagerFlowRole.values()) {
            List<Register> registers = new ArrayList<>(role.registers());
            registers.sort((first, second) -> Integer.compare(first.address(), second.address()));
            for (int i = 1; i < registers.size(); i++) {
                Register previous = registers.get(i - 1);
                Register current = registers.get(i);
                assertTrue(previous.address() + previous.length() <= current.address(),
                        role + ": " + previous.channelId() + " overlaps " + current.channelId());
            }
        }
    }

    @Test
    public void testChannelIdsAreUniquePerRole() {
        for (HagerFlowRole role : HagerFlowRole.values()) {
            Set<String> channelIds = new HashSet<>();
            for (Register register : role.registers()) {
                if (register.bitFlags().isEmpty()) {
                    assertTrue(channelIds.add(register.channelId()), role + ": duplicate " + register.channelId());
                } else {
                    for (BitFlag flag : register.bitFlags()) {
                        assertTrue(channelIds.add(flag.channelId()), role + ": duplicate " + flag.channelId());
                    }
                }
            }
        }
    }

    @Test
    public void testBitFlagsAreInsideTheRegister() {
        for (HagerFlowRole role : HagerFlowRole.values()) {
            for (Register register : role.registers()) {
                int bits = register.length() * 16;
                for (BitFlag flag : register.bitFlags()) {
                    assertTrue(flag.bit() >= 0 && flag.bit() < bits,
                            role + "." + register.channelId() + " bit " + flag.bit() + " is outside the register");
                }
            }
        }
    }

    @Test
    public void testBitstringRegistersDeclareTheirBits() {
        for (HagerFlowRole role : HagerFlowRole.values()) {
            for (Register register : role.registers()) {
                if (register.dataType() == DataType.BITSTRING) {
                    assertFalse(register.bitFlags().isEmpty(), role + "." + register.channelId() + " has no bits");
                }
            }
        }
    }

    @Test
    public void testSlaveIdValidation() {
        assertTrue(HagerFlowRole.EMS.validate(0, 5000).isEmpty());
        assertTrue(HagerFlowRole.EVSE.validate(1, 5000).isEmpty());
        assertTrue(HagerFlowRole.EVSE.validate(8, 5000).isEmpty());
        assertTrue(HagerFlowRole.POWERMETER.validate(30, 5000).isEmpty());
        assertTrue(HagerFlowRole.POWERMETER.validate(37, 5000).isEmpty());
        assertTrue(HagerFlowRole.SGREADY.validate(59, 5000).isEmpty());

        assertTrue(HagerFlowRole.EVSE.validate(0, 5000).isPresent());
        assertTrue(HagerFlowRole.EVSE.validate(9, 5000).isPresent());
        assertTrue(HagerFlowRole.POWERMETER.validate(29, 5000).isPresent());
        assertTrue(HagerFlowRole.SGREADY.validate(60, 5000).isPresent());
    }

    @Test
    public void testRefreshValidation() {
        assertTrue(HagerFlowRole.EMS.validate(0, HagerFlowBindingConstants.MIN_REFRESH_MS).isEmpty());
        assertTrue(HagerFlowRole.EMS.validate(0, HagerFlowBindingConstants.MIN_REFRESH_MS - 1).isPresent());
    }
}
