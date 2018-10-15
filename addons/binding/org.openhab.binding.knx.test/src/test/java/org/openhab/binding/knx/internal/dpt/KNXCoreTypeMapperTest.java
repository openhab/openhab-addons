/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.dpt;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.junit.Test;

/**
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class KNXCoreTypeMapperTest {

    @Test
    public void testToDPTValue_trailingZeroesStrippedOff() {
        assertEquals("3", new KNXCoreTypeMapper().toDPTValue(new DecimalType("3"), "17.001"));
        assertEquals("3", new KNXCoreTypeMapper().toDPTValue(new DecimalType("3.0"), "17.001"));
    }

}
