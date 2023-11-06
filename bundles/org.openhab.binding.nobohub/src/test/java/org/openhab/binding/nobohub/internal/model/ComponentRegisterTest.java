/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ComponentRegister model object.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ComponentRegisterTest {

    @Test
    public void testPutGet() throws NoboDataException {
        Component c = Component.fromH02("H02 186170024143 0 Kontor 0 1 -1 -1");
        ComponentRegister sut = new ComponentRegister();
        sut.put(c);
        Assertions.assertEquals(c, sut.get(c.getSerialNumber()));
    }

    @Test
    public void testPutOverwrite() throws NoboDataException {
        Component c1 = Component.fromH02("H02 186170024143 0 Kontor 0 1 -1 -1");
        Component c2 = Component.fromH02("H02 186170024143 0 Bad 0 1 -1 -1");
        ComponentRegister sut = new ComponentRegister();
        sut.put(c1);
        sut.put(c2);
        Assertions.assertEquals(c2, sut.get(c2.getSerialNumber()));
    }

    @Test
    public void testRemove() throws NoboDataException {
        Component c = Component.fromH02("H02 186170024143 0 Kontor 0 1 -1 -1");
        ComponentRegister sut = new ComponentRegister();
        sut.put(c);
        Component res = sut.remove(c.getSerialNumber());
        Assertions.assertEquals(c, res);
    }

    @Test
    public void testRemoveUnknown() {
        ComponentRegister sut = new ComponentRegister();
        Component res = sut.remove(new SerialNumber("123123123123"));
        Assertions.assertEquals(null, res);
    }

    @Test
    public void testGetUnknown() {
        ComponentRegister sut = new ComponentRegister();
        Component z = sut.get(new SerialNumber("123123123123"));
        Assertions.assertEquals(null, z);
    }

    @Test
    public void testValues() throws NoboDataException {
        Component c1 = Component.fromH02("H02 186170024141 0 Kontor 0 1 -1 -1");
        Component c2 = Component.fromH02("H02 186170024142 0 Soverom 0 1 -1 -1");
        ComponentRegister sut = new ComponentRegister();
        sut.put(c1);
        sut.put(c2);
        Assertions.assertEquals(2, sut.values().size());
        Assertions.assertEquals(true, sut.values().contains(c1));
        Assertions.assertEquals(true, sut.values().contains(c2));
    }
}
