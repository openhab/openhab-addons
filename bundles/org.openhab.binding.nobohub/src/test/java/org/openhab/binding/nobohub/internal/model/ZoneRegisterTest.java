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
package org.openhab.binding.nobohub.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ZoneRegister model object.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ZoneRegisterTest {

    @Test
    public void testPutGet() throws NoboDataException {
        Zone z = Zone.fromH01("H01 1 1. etage 20 22 16 1 -1");
        ZoneRegister sut = new ZoneRegister();
        sut.put(z);
        assertEquals(z, sut.get(z.getId()));
    }

    @Test
    public void testPutOverwrite() throws NoboDataException {
        Zone z1 = Zone.fromH01("H01 1 1. etage 20 22 16 1 -1");
        Zone z2 = Zone.fromH01("H01 1 2. etage 20 22 16 1 -1");
        ZoneRegister sut = new ZoneRegister();
        sut.put(z1);
        sut.put(z2);
        assertEquals(z2, sut.get(z2.getId()));
    }

    @Test
    public void testRemove() throws NoboDataException {
        Zone z = Zone.fromH01("H01 1 1. etage 20 22 16 1 -1");
        ZoneRegister sut = new ZoneRegister();
        sut.put(z);
        Zone res = sut.remove(z.getId());
        assertEquals(z, res);
    }

    @Test
    public void testRemoveUnknown() {
        ZoneRegister sut = new ZoneRegister();
        Zone res = sut.remove(666);
        assertEquals(null, res);
    }

    @Test
    public void testGetUnknown() {
        ZoneRegister sut = new ZoneRegister();
        Zone z = sut.get(666);
        assertEquals(null, z);
    }

    @Test
    public void testValues() throws NoboDataException {
        Zone z1 = Zone.fromH01("H01 1 1. etage 20 22 16 1 -1");
        Zone z2 = Zone.fromH01("H01 2 2. etage 20 22 16 1 -1");
        ZoneRegister sut = new ZoneRegister();
        sut.put(z1);
        sut.put(z2);
        assertEquals(2, sut.values().size());
        assertEquals(true, sut.values().contains(z1));
        assertEquals(true, sut.values().contains(z2));
    }
}
