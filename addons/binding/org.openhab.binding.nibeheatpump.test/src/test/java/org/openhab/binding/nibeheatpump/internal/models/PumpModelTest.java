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

public class PumpModelTest {

    @Before
    public void Before() {
    }

    @Test
    public void TestF1X45() {
        final String pumpModelString = "F1X45";
        final PumpModel pumpModel = PumpModel.getPumpModel(pumpModelString);
        assertEquals(PumpModel.F1X45, pumpModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badPumpModelTest() {
        PumpModel.getPumpModel("XXXX");
    }
}
