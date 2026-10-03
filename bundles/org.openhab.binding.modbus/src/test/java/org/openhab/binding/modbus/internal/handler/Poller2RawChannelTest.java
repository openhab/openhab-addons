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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class Poller2RawChannelTest {

    @Test
    public void rendersTheConfiguredRegisterRangeAsCanonicalUppercaseHex() {
        Poller2RawChannel channel = new Poller2RawChannel(101, 2, PollerReadFailurePolicy.UNDEF);

        assertEquals(new StringType("1234ABCD"),
                channel.acceptRegisters(new ModbusRegisterArray(0x0001, 0x1234, 0xABCD), 100));
    }

    @Test
    public void returnsUndefWhenTheConfiguredRangeIsOutsideThePollResult() {
        Poller2RawChannel channel = new Poller2RawChannel(101, 2, PollerReadFailurePolicy.UNDEF);

        assertEquals(UnDefType.UNDEF, channel.acceptRegisters(new ModbusRegisterArray(0x0001, 0x1234), 100));
    }
}
