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
package org.openhab.io.transport.modbus.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openhab.io.transport.modbus.BasicBitArray;

public class BasicBitArrayTest {

    @Test
    public void testGetBitAndSetBit() {
        BasicBitArray data1 = new BasicBitArray(true, false, true);
        assertThat(data1.size(), is(equalTo(3)));
        assertThat(data1.getBit(0), is(equalTo(true)));
        assertThat(data1.getBit(1), is(equalTo(false)));
        assertThat(data1.getBit(2), is(equalTo(true)));

        data1.setBit(1, true);
        data1.setBit(2, false);
        assertThat(data1.size(), is(equalTo(3)));
        assertThat(data1.getBit(0), is(equalTo(true)));
        assertThat(data1.getBit(1), is(equalTo(true)));
        assertThat(data1.getBit(2), is(equalTo(false)));
    }

    @Test
    public void testGetBitAndSetBit2() {
        BasicBitArray data1 = new BasicBitArray(3);
        assertThat(data1.size(), is(equalTo(3)));
        assertThat(data1.getBit(0), is(equalTo(false)));
        assertThat(data1.getBit(1), is(equalTo(false)));
        assertThat(data1.getBit(2), is(equalTo(false)));

        data1.setBit(1, true);
        assertThat(data1.size(), is(equalTo(3)));
        assertThat(data1.getBit(0), is(equalTo(false)));
        assertThat(data1.getBit(1), is(equalTo(true)));
        assertThat(data1.getBit(2), is(equalTo(false)));

        data1.setBit(1, false);
        assertThat(data1.size(), is(equalTo(3)));
        assertThat(data1.getBit(0), is(equalTo(false)));
        assertThat(data1.getBit(1), is(equalTo(false)));
        assertThat(data1.getBit(2), is(equalTo(false)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBounds() {
        BasicBitArray data1 = new BasicBitArray(true, false, true);
        data1.getBit(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBounds2() {
        BasicBitArray data1 = new BasicBitArray(true, false, true);
        data1.getBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBounds3() {
        BasicBitArray data1 = new BasicBitArray(3);
        data1.getBit(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBounds4() {
        BasicBitArray data1 = new BasicBitArray(3);
        data1.getBit(-1);
    }
}
