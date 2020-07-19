/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.modbus;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.e3dc.internal.modbus.ModbusCallback;

/**
 * The {@link ModbusCallbackTest} Tests for ModbusCallbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ModbusCallbackTest {

    @Test
    public void testDebugNames() {
        ModbusCallback mcInfo = new ModbusCallback(DataType.INFO);
        assertEquals("Debug Name Info", "org.openhab.binding.e3dc.internal.modbus.ModbusCallback:INFO",
                mcInfo.toString());

        ModbusCallback mcPower = new ModbusCallback(DataType.POWER);
        assertEquals("Debug Name Power", "org.openhab.binding.e3dc.internal.modbus.ModbusCallback:POWER",
                mcPower.toString());
    }
}
