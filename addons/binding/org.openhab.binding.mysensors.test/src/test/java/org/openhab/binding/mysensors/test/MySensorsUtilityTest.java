/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.MySensorsUtility;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;

public class MySensorsUtilityTest {

    @Test
    public void testSameKey() {
        Map<Integer, Object> m1 = new HashMap<>();
        Map<Integer, Object> m2 = new HashMap<>();

        m1.put(1, new Object());
        m1.put(2, new Object());
        m1.put(3, new Object());

        m2.put(4, new Object());
        m2.put(6, new Object());
        m2.put(7, new Object());

        assertFalse(MySensorsUtility.containsSameKey(m1, m2));

        m1.clear();
        m2.clear();

        m1.put(1, new Object());
        m1.put(232, new Object());
        m1.put(569, new Object());

        m2.put(1, new Object());
        m2.put(232, new Object());
        m2.put(569, new Object());

        assertTrue(MySensorsUtility.containsSameKey(m1, m2));

        m1.clear();
        m2.clear();

        m1.put(569, new Object());
        m1.put(1, new Object());
        m1.put(232, new Object());

        m2.put(232, new Object());
        m2.put(1, new Object());
        m2.put(569, new Object());

        assertTrue(MySensorsUtility.containsSameKey(m1, m2));
    }

    @Test
    public void testMergeMap() {
        Map<Integer, MySensorsNode> m1 = new HashMap<>();
        Map<Integer, MySensorsNode> m2 = new HashMap<>();

        m1.put(1, new MySensorsNode(1));
        m1.put(2, new MySensorsNode(2));

        m2.put(4, new MySensorsNode(4));
        m2.put(6, new MySensorsNode(6));
        m2.put(7, new MySensorsNode(7));

        MySensorsUtility.mergeMap(m1, m2, true);

        assertEquals(5, m1.size());

        assertEquals(1, m1.get(1).getNodeId());
        assertEquals(2, m1.get(2).getNodeId());
        assertEquals(4, m1.get(4).getNodeId());
        assertEquals(6, m1.get(6).getNodeId());
        assertEquals(7, m1.get(7).getNodeId());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionMergeMap() {
        Map<Integer, MySensorsNode> m1 = new HashMap<>();
        Map<Integer, MySensorsNode> m2 = new HashMap<>();

        m1.put(1, new MySensorsNode(1));
        m1.put(2, new MySensorsNode(2));

        m2.put(2, new MySensorsNode(4));
        m2.put(3, new MySensorsNode(6));

        MySensorsUtility.mergeMap(m1, m2, false);

    }

}
