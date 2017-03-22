/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
