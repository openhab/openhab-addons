package org.openhab.io.transport.modbus.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openhab.io.transport.modbus.BitArrayImpl;

public class BitArrayImplTest {

    @Test
    public void testGetBitAndSetBit() {
        BitArrayImpl data1 = new BitArrayImpl(true, false, true);
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
        BitArrayImpl data1 = new BitArrayImpl(3);
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
        BitArrayImpl data1 = new BitArrayImpl(true, false, true);
        data1.getBit(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBounds2() {
        BitArrayImpl data1 = new BitArrayImpl(true, false, true);
        data1.getBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBounds3() {
        BitArrayImpl data1 = new BitArrayImpl(3);
        data1.getBit(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBounds4() {
        BitArrayImpl data1 = new BitArrayImpl(3);
        data1.getBit(-1);
    }
}
