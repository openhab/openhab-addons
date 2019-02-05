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
package org.openhab.binding.mysensors.test;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSCustom;

public class NodeChildTest {

    @Test(expected = IllegalArgumentException.class)
    public void testWrongChildIdNeg() {
        new MySensorsChildSCustom(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongChildId() {
        new MySensorsChildSCustom(255);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNodeId0() {
        new MySensorsNode(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNodeId255() {
        new MySensorsNode(255);
    }

}
