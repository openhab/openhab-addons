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
package org.openhab.binding.nobohub.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for OverrideRegister model object.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class OverridePlanRegisterTest {

    @Test
    public void testPutGet() throws NoboDataException {
        OverridePlan o = OverridePlan.fromH04("H04 4 0 0 -1 -1 0 -1");
        OverrideRegister sut = new OverrideRegister();
        sut.put(o);
        assertEquals(o, sut.get(o.getId()));
    }

    @Test
    public void testPutOverwrite() throws NoboDataException {
        OverridePlan o1 = OverridePlan.fromH04("H04 4 0 0 -1 -1 0 -1");
        OverridePlan o2 = OverridePlan.fromH04("H04 4 3 0 -1 -1 0 -1");
        OverrideRegister sut = new OverrideRegister();
        sut.put(o1);
        sut.put(o2);
        assertEquals(o2, sut.get(o2.getId()));
    }

    @Test
    public void testRemove() throws NoboDataException {
        OverridePlan o = OverridePlan.fromH04("H04 4 0 0 -1 -1 0 -1");
        OverrideRegister sut = new OverrideRegister();
        sut.put(o);
        OverridePlan res = sut.remove(o.getId());
        assertEquals(o, res);
    }

    @Test
    public void testRemoveUnknown() {
        OverrideRegister sut = new OverrideRegister();
        OverridePlan res = sut.remove(666);
        assertNull(res);
    }

    @Test
    public void testGetUnknown() {
        OverrideRegister sut = new OverrideRegister();
        OverridePlan o = sut.get(666);
        assertNull(o);
    }
}
