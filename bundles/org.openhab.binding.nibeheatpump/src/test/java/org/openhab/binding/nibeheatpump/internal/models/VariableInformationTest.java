/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nibeheatpump.internal.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Pauli Anttila - Initial contribution
 */
public class VariableInformationTest {

    @BeforeEach
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

    @Test
    public void TestF1X55Variable() {
        final int coilAddress = 40004;
        final VariableInformation variableInfo = VariableInformation.getVariableInfo(PumpModel.F1X55, coilAddress);
        assertEquals(10, variableInfo.factor);
        assertEquals("BT1 Outdoor Temperature", variableInfo.variable);
        assertEquals(VariableInformation.NibeDataType.S16, variableInfo.dataType);
        assertEquals(VariableInformation.Type.SENSOR, variableInfo.type);
    }

    @Test
    public void TestF750Variable() {
        final int coilAddress = 40004;
        final VariableInformation variableInfo = VariableInformation.getVariableInfo(PumpModel.F750, coilAddress);
        assertEquals(10, variableInfo.factor);
        assertEquals("BT1 Outdoor Temperature", variableInfo.variable);
        assertEquals(VariableInformation.NibeDataType.S16, variableInfo.dataType);
        assertEquals(VariableInformation.Type.SENSOR, variableInfo.type);
    }

    @Test
    public void TestF470Variable() {
        final int coilAddress = 40020;
        final VariableInformation variableInfo = VariableInformation.getVariableInfo(PumpModel.F470, coilAddress);
        assertEquals(10, variableInfo.factor);
        assertEquals("EB100-BT16 Evaporator temp", variableInfo.variable);
        assertEquals(VariableInformation.NibeDataType.S16, variableInfo.dataType);
        assertEquals(VariableInformation.Type.SENSOR, variableInfo.type);
    }
}
