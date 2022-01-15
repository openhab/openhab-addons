/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.dpt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;

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
