/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.test;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.hamcrest.Description;
import org.openhab.io.transport.modbus.ModbusWriteFunctionCode;
import org.openhab.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;

class RegisterMatcher extends AbstractRequestComparer<ModbusWriteRegisterRequestBlueprint> {

    private Integer[] expectedRegisterValues;

    public RegisterMatcher(int expectedUnitId, int expectedAddress, int expectedMaxTries,
            ModbusWriteFunctionCode expectedFunctionCode, Integer... expectedRegisterValues) {
        super(expectedUnitId, expectedAddress, expectedFunctionCode, expectedMaxTries);
        this.expectedRegisterValues = expectedRegisterValues;
    }

    @Override
    public void describeTo(Description description) {
        super.describeTo(description);
        description.appendText(" registers=");
        description.appendValue(Arrays.toString(expectedRegisterValues));
    }

    @Override
    protected boolean doMatchData(ModbusWriteRegisterRequestBlueprint item) {
        Object[] actual = StreamSupport.stream(item.getRegisters().spliterator(), false).map(r -> r.getValue())
                .toArray();
        return Objects.deepEquals(actual, expectedRegisterValues);
    }
}