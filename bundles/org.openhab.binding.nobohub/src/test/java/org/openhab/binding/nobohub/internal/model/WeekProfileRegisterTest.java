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
 * Unit tests for WeekProfileRegister model object.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class WeekProfileRegisterTest {

    @Test
    public void testPutGet() throws NoboDataException {
        WeekProfile p1 = WeekProfile.fromH03(
                "H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfileRegister sut = new WeekProfileRegister();
        sut.put(p1);
        assertEquals(p1, sut.get(p1.getId()));
    }

    @Test
    public void testPutOverwrite() throws NoboDataException {
        WeekProfile p1 = WeekProfile.fromH03(
                "H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfile p2 = WeekProfile.fromH03(
                "H03 2 HomeOffice 00000,06001,09000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfileRegister sut = new WeekProfileRegister();
        sut.put(p1);
        sut.put(p2);
        assertEquals(p2, sut.get(p2.getId()));
    }

    @Test
    public void testRemove() throws NoboDataException {
        WeekProfile p1 = WeekProfile.fromH03(
                "H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfileRegister sut = new WeekProfileRegister();
        sut.put(p1);
        WeekProfile res = sut.remove(p1.getId());
        assertEquals(p1, res);
    }

    @Test
    public void testRemoveUnknown() {
        WeekProfileRegister sut = new WeekProfileRegister();
        WeekProfile res = sut.remove(666);
        assertEquals(null, res);
    }

    @Test
    public void testGetUnknown() {
        WeekProfileRegister sut = new WeekProfileRegister();
        WeekProfile o = sut.get(666);
        assertEquals(null, o);
    }

    @Test
    public void testIsEmpty() throws NoboDataException {
        WeekProfile p1 = WeekProfile.fromH03(
                "H03 1 Default 00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,23000,00000,06001,08000,15001,00000,07001,00000,07001,23000");
        WeekProfileRegister sut = new WeekProfileRegister();
        assertEquals(true, sut.isEmpty());
        sut.put(p1);
        assertEquals(false, sut.isEmpty());
    }
}
