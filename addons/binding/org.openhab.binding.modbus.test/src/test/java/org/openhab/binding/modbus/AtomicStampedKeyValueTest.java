/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.binding.modbus.internal.AtomicStampedKeyValue;

@RunWith(MockitoJUnitRunner.class)
public class AtomicStampedKeyValueTest {

    @Test(expected = NullPointerException.class)
    public void testInitWithNullKey() {
        new AtomicStampedKeyValue<Object, Object>(0, null, new Object());
    }

    @Test(expected = NullPointerException.class)
    public void testInitWithNullValue() {
        new AtomicStampedKeyValue<Object, Object>(0, new Object(), null);
    }

    @Test
    public void testGetters() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(equalTo(val)));
    }

    @Test
    public void testUpdateWithSameStampAndKey() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        keyValue.update(42L, key, new Object());
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(not(equalTo(val))));
    }

    @Test
    public void testUpdateWithSameStamp() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        keyValue.update(42L, new Object(), new Object());
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(not(equalTo(key))));
        assertThat(keyValue.getValue(), is(not(equalTo(val))));
    }

    @Test
    public void testUpdateWithSameKey() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        keyValue.update(-99L, key, new Object());
        assertThat(keyValue.getStamp(), is(equalTo(-99L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(not(equalTo(val))));
    }

    @Test
    public void testUpdateWithSameValue() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        keyValue.update(-99L, new Object(), val);
        assertThat(keyValue.getStamp(), is(equalTo(-99L)));
        assertThat(keyValue.getKey(), is(not(equalTo(key))));
        assertThat(keyValue.getValue(), is(equalTo(val)));
    }

    @Test
    public void testCopy() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        AtomicStampedKeyValue<Object, Object> copy = keyValue.copy();

        // keyValue unchanged
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        // data matches
        assertThat(keyValue.getStamp(), is(equalTo(copy.getStamp())));
        assertThat(keyValue.getKey(), is(equalTo(copy.getKey())));
        assertThat(keyValue.getValue(), is(equalTo(copy.getValue())));

        // after update they live life of their own
        Object key2 = new Object();
        Object val2 = new Object();
        copy.update(-99L, key2, val2);

        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        assertThat(copy.getStamp(), is(equalTo(-99L)));
        assertThat(copy.getKey(), is(equalTo(key2)));
        assertThat(copy.getValue(), is(equalTo(val2)));
    }

    /**
     * instance(stamp=x).copyIfStampAfter(x)
     */
    @Test
    public void testCopyIfStampAfterEqual() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        AtomicStampedKeyValue<Object, Object> copy = keyValue.copyIfStampAfter(42L);

        // keyValue unchanged
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        // data matches
        assertThat(keyValue.getStamp(), is(equalTo(copy.getStamp())));
        assertThat(keyValue.getKey(), is(equalTo(copy.getKey())));
        assertThat(keyValue.getValue(), is(equalTo(copy.getValue())));

        // after update they live life of their own
        Object key2 = new Object();
        Object val2 = new Object();
        copy.update(-99L, key2, val2);

        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        assertThat(copy.getStamp(), is(equalTo(-99L)));
        assertThat(copy.getKey(), is(equalTo(key2)));
        assertThat(copy.getValue(), is(equalTo(val2)));
    }

    /**
     * instance(stamp=x-1).copyIfStampAfter(x)
     */
    @Test
    public void testCopyIfStampAfterTooOld() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        AtomicStampedKeyValue<Object, Object> copy = keyValue.copyIfStampAfter(43L);

        // keyValue unchanged
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        // copy is null
        assertThat(copy, is(nullValue()));
    }

    /**
     * instance(stamp=x).copyIfStampAfter(x-1)
     */
    @Test
    public void testCopyIfStampAfterFresh() {
        Object key = new Object();
        Object val = new Object();
        AtomicStampedKeyValue<Object, Object> keyValue = new AtomicStampedKeyValue<>(42L, key, val);
        AtomicStampedKeyValue<Object, Object> copy = keyValue.copyIfStampAfter(41L);

        // keyValue unchanged
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        // data matches
        assertThat(keyValue.getStamp(), is(equalTo(copy.getStamp())));
        assertThat(keyValue.getKey(), is(equalTo(copy.getKey())));
        assertThat(keyValue.getValue(), is(equalTo(copy.getValue())));

        // after update they live life of their own
        Object key2 = new Object();
        Object val2 = new Object();
        copy.update(-99L, key2, val2);

        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getKey(), is(equalTo(key)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        assertThat(copy.getStamp(), is(equalTo(-99L)));
        assertThat(copy.getKey(), is(equalTo(key2)));
        assertThat(copy.getValue(), is(equalTo(val2)));
    }

    @Test
    public void testCompare() {
        // equal, smaller, larger
        assertThat(AtomicStampedKeyValue.compare(new AtomicStampedKeyValue<Object, Object>(42L, "", ""),
                new AtomicStampedKeyValue<Object, Object>(42L, "", "")), is(equalTo(0)));
        assertThat(AtomicStampedKeyValue.compare(new AtomicStampedKeyValue<Object, Object>(41L, "", ""),
                new AtomicStampedKeyValue<Object, Object>(42L, "", "")), is(equalTo(-1)));
        assertThat(AtomicStampedKeyValue.compare(new AtomicStampedKeyValue<Object, Object>(42L, "", ""),
                new AtomicStampedKeyValue<Object, Object>(41L, "", "")), is(equalTo(1)));

        // Nulls come first
        assertThat(AtomicStampedKeyValue.compare(null, new AtomicStampedKeyValue<Object, Object>(42L, "", "")),
                is(equalTo(-1)));
        assertThat(AtomicStampedKeyValue.compare(new AtomicStampedKeyValue<Object, Object>(42L, "", ""), null),
                is(equalTo(1)));
    }
}
