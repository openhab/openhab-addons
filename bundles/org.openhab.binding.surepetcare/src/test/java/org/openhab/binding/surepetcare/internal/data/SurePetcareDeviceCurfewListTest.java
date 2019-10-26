/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.data;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * The {@link SurePetcareDeviceCurfewListTest} class implements unit test case for {@link SurePetcareDeviceCurfewList}
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDeviceCurfewListTest {

    @Test
    public void testGet() {
        SurePetcareDeviceCurfewList orig = new SurePetcareDeviceCurfewList();
        orig.add(new SurePetcareDeviceCurfew());
        orig.add(new SurePetcareDeviceCurfew(true, "12:00", "13:00"));

        assertEquals(orig.size(), 2);
        assertFalse(orig.get(0).enabled);
        assertTrue(orig.get(1).enabled);
        assertEquals(orig.size(), 2);

        assertFalse(orig.get(2).enabled);
        assertEquals(orig.size(), 3);

        assertFalse(orig.get(3).enabled);
        assertEquals(orig.size(), 4);

        assertFalse(orig.get(9).enabled);
        assertEquals(orig.size(), 10);
    }

    @Test
    public void testOrder() {
        SurePetcareDeviceCurfewList orig = new SurePetcareDeviceCurfewList();
        orig.add(new SurePetcareDeviceCurfew());
        orig.add(new SurePetcareDeviceCurfew(true, "12:00", "13:00"));
        orig.add(new SurePetcareDeviceCurfew(true, "19:00", "20:00"));

        assertEquals(orig.size(), 3);
        assertFalse(orig.get(0).enabled);
        assertTrue(orig.get(1).enabled);
        assertTrue(orig.get(2).enabled);

        SurePetcareDeviceCurfewList ordered = orig.order();
        assertEquals(ordered.size(), 4);

        assertTrue(ordered.get(0).enabled);
        assertEquals(ordered.get(0).lockTime, "12:00");
        assertTrue(ordered.get(1).enabled);
        assertFalse(ordered.get(2).enabled);
        assertFalse(ordered.get(3).enabled);
    }

    @Test
    public void testCompact() {
        SurePetcareDeviceCurfewList orig = new SurePetcareDeviceCurfewList();
        orig.add(new SurePetcareDeviceCurfew());
        orig.add(new SurePetcareDeviceCurfew(true, "12:00", "13:00"));
        orig.add(new SurePetcareDeviceCurfew(true, "19:00", "20:00"));
        orig.add(new SurePetcareDeviceCurfew());

        assertEquals(orig.size(), 4);
        assertFalse(orig.get(0).enabled);
        assertTrue(orig.get(1).enabled);
        assertTrue(orig.get(2).enabled);
        assertFalse(orig.get(3).enabled);

        SurePetcareDeviceCurfewList compact = orig.compact();
        assertEquals(compact.size(), 2);

        assertTrue(compact.get(0).enabled);
        assertEquals(compact.get(0).lockTime, "12:00");
        assertTrue(compact.get(1).enabled);
        assertEquals(compact.get(1).lockTime, "19:00");
    }

}
