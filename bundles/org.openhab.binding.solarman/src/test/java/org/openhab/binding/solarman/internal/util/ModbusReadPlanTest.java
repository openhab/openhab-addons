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
package org.openhab.binding.solarman.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarman.internal.defmodel.Request;

/**
 * Verifies grouping without duplicate polling or crossing protocol function boundaries.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class ModbusReadPlanTest {
    @Test
    void mergesOverlapSplitsLargeWindowsAndPreservesDifferentFunctions() {
        var plan = ModbusReadPlan.normalize(List.of(new Request(3, 0, 200), new Request(3, 100, 256),
                new Request(4, 0, 10), new Request(3, 500, 510)));
        assertEquals(5, plan.size());
        assertEquals(0, plan.get(0).getStart());
        assertEquals(124, plan.get(0).getEnd());
        assertEquals(125, plan.get(1).getStart());
        assertEquals(249, plan.get(1).getEnd());
        assertEquals(250, plan.get(2).getStart());
        assertEquals(256, plan.get(2).getEnd());
        assertEquals(500, plan.get(3).getStart());
        assertEquals(4, plan.get(4).getMbFunctioncode());
        assertThrows(IllegalArgumentException.class, () -> ModbusReadPlan.normalize(List.of(new Request(3, 10, 9))));
    }
}
