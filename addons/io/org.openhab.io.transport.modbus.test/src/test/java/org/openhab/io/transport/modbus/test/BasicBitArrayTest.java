/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
