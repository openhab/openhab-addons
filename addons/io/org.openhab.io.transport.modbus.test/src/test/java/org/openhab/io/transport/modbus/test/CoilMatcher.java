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
import org.openhab.io.transport.modbus.ModbusWriteCoilRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusWriteFunctionCode;

class CoilMatcher extends AbstractRequestComparer<ModbusWriteCoilRequestBlueprint> {

    private Boolean[] expectedCoils;

    public CoilMatcher(int expectedUnitId, int expectedAddress, int expectedMaxTries,
            ModbusWriteFunctionCode expectedFunctionCode, Boolean... expectedCoils) {
        super(expectedUnitId, expectedAddress, expectedFunctionCode, expectedMaxTries);
        this.expectedCoils = expectedCoils;
    }

    @Override
    public void describeTo(Description description) {
        super.describeTo(description);
        description.appendText(" coils=");
        description.appendValue(Arrays.toString(expectedCoils));
    }

    @Override
    protected boolean doMatchData(ModbusWriteCoilRequestBlueprint item) {
        Object[] actual = StreamSupport.stream(item.getCoils().spliterator(), false).toArray();
        return Objects.deepEquals(actual, expectedCoils);
    }
}