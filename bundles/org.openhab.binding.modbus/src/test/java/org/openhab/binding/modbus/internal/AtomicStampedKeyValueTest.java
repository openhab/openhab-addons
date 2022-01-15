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
package org.openhab.binding.modbus.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class AtomicStampedKeyValueTest {

    @Test
    public void testInitWithNullValue() {
        assertThrows(NullPointerException.class, () -> new AtomicStampedValue<>(0, null));
    }

    @Test
    public void testGetters() {
        Object val = new Object();
        AtomicStampedValue<Object> keyValue = new AtomicStampedValue<>(42L, val);
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(equalTo(val)));
    }

    @Test
    public void testUpdateWithSameStamp() {
        Object val = new Object();
        AtomicStampedValue<Object> keyValue = new AtomicStampedValue<>(42L, val);
        keyValue.update(42L, new Object());
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(not(equalTo(val))));
    }

    @Test
    public void testUpdateWithDifferentStamp() {
        Object val = new Object();
        AtomicStampedValue<Object> keyValue = new AtomicStampedValue<>(42L, val);
        keyValue.update(-99L, new Object());
        assertThat(keyValue.getStamp(), is(equalTo(-99L)));
        assertThat(keyValue.getValue(), is(not(equalTo(val))));
    }

    @Test
    public void testCopy() {
        Object val = new Object();
        AtomicStampedValue<Object> keyValue = new AtomicStampedValue<>(42L, val);
        AtomicStampedValue<Object> copy = keyValue.copy();

        // unchanged
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        // data matches
        assertThat(keyValue.getStamp(), is(equalTo(copy.getStamp())));
        assertThat(keyValue.getValue(), is(equalTo(copy.getValue())));

        // after update they live life of their own
        Object val2 = new Object();
        copy.update(-99L, val2);

        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        assertThat(copy.getStamp(), is(equalTo(-99L)));
        assertThat(copy.getValue(), is(equalTo(val2)));
    }

    /**
     * instance(stamp=x).copyIfStampAfter(x)
     */
    @Test
    public void testCopyIfStampAfterEqual() {
        Object val = new Object();
        AtomicStampedValue<Object> keyValue = new AtomicStampedValue<>(42L, val);
        AtomicStampedValue<Object> copy = keyValue.copyIfStampAfter(42L);

        // keyValue unchanged
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        // data matches
        assertThat(keyValue.getStamp(), is(equalTo(copy.getStamp())));
        assertThat(keyValue.getValue(), is(equalTo(copy.getValue())));

        // after update they live life of their own
        Object val2 = new Object();
        copy.update(-99L, val2);

        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        assertThat(copy.getStamp(), is(equalTo(-99L)));
        assertThat(copy.getValue(), is(equalTo(val2)));
    }

    /**
     * instance(stamp=x-1).copyIfStampAfter(x)
     */
    @Test
    public void testCopyIfStampAfterTooOld() {
        Object val = new Object();
        AtomicStampedValue<Object> keyValue = new AtomicStampedValue<>(42L, val);
        AtomicStampedValue<Object> copy = keyValue.copyIfStampAfter(43L);

        // keyValue unchanged
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        // copy is null
        assertThat(copy, is(nullValue()));
    }

    /**
     * instance(stamp=x).copyIfStampAfter(x-1)
     */
    @Test
    public void testCopyIfStampAfterFresh() {
        Object val = new Object();
        AtomicStampedValue<Object> keyValue = new AtomicStampedValue<>(42L, val);
        AtomicStampedValue<Object> copy = keyValue.copyIfStampAfter(41L);

        // keyValue unchanged
        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        // data matches
        assertThat(keyValue.getStamp(), is(equalTo(copy.getStamp())));
        assertThat(keyValue.getValue(), is(equalTo(copy.getValue())));

        // after update they live life of their own
        Object val2 = new Object();
        copy.update(-99L, val2);

        assertThat(keyValue.getStamp(), is(equalTo(42L)));
        assertThat(keyValue.getValue(), is(equalTo(val)));

        assertThat(copy.getStamp(), is(equalTo(-99L)));
        assertThat(copy.getValue(), is(equalTo(val2)));
    }

    @Test
    public void testCompare() {
        // equal, smaller, larger
        assertThat(AtomicStampedValue.compare(new AtomicStampedValue<>(42L, ""), new AtomicStampedValue<>(42L, "")),
                is(equalTo(0)));
        assertThat(AtomicStampedValue.compare(new AtomicStampedValue<>(41L, ""), new AtomicStampedValue<>(42L, "")),
                is(equalTo(-1)));
        assertThat(AtomicStampedValue.compare(new AtomicStampedValue<>(42L, ""), new AtomicStampedValue<>(41L, "")),
                is(equalTo(1)));

        // Nulls come first
        assertThat(AtomicStampedValue.compare(null, new AtomicStampedValue<>(42L, "")), is(equalTo(-1)));
        assertThat(AtomicStampedValue.compare(new AtomicStampedValue<>(42L, ""), null), is(equalTo(1)));
    }
}
