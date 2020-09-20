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
package org.openhab.binding.modbus.e3dc.modbus;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.modbus.e3dc.internal.modbus.Parser;

/**
 * The {@link ParserNameTest} Tests for ModbusCallbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ParserNameTest {

    @Test
    public void testDebugNames() {
        Parser mcInfo = new Parser(DataType.INFO);
        assertEquals("Debug Name Info", "org.openhab.binding.modbus.e3dc.internal.modbus.Parser:INFO",
                mcInfo.toString());

        Parser mcPower = new Parser(DataType.DATA);
        assertEquals("Debug Name Power", "org.openhab.binding.modbus.e3dc.internal.modbus.Parser:DATA",
                mcPower.toString());
    }
}
