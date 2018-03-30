/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal.models;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class VariableInformationTest {

    @Before
    public void Before() {
    }

    @Test
    public void TestF1X45Variable() {
        final int coilAddress = 40004;
        final VariableInformation variableInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, coilAddress);
        assertEquals(10, variableInfo.factor);
        assertEquals("BT1 Outdoor temp", variableInfo.variable);
        assertEquals(VariableInformation.NibeDataType.S16, variableInfo.dataType);
        assertEquals(VariableInformation.Type.SENSOR, variableInfo.type);
    }
}
