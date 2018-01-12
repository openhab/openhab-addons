/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.toberemoved.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.network.internal.toberemoved.cache.ExpiringCacheAsync.ExpiringCacheUpdate;

/**
 * Tests cases for {@see ExpiringAsyncCache}
 *
 * @author David Graeff - Initial contribution
 */
public class ExpiringCacheAsyncTest {
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWrongCacheTime() {
        // Fail if cache time is <= 0
        new ExpiringCacheAsync<Double>(0, () -> {
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoRefrehCommand() {
        new ExpiringCacheAsync<Double>(2000, null);
    }

    @Test
    public void testFetchValue() {
        ExpiringCacheUpdate u = mock(ExpiringCacheUpdate.class);
        ExpiringCacheAsync<Double> t = new ExpiringCacheAsync<Double>(2000, u);
        assertTrue(t.isExpired());
        // Request a value
        Consumer<Double> consumer = mock(Consumer.class);
        t.getValue(consumer);
        // We expect a call to the updater object
        verify(u).requestCacheUpdate();
        // Update the value now
        t.setValue(10.0);
        // The value should be valid
        assertFalse(t.isExpired());
        // We expect a call to the consumer
        ArgumentCaptor<Double> valueCaptor = ArgumentCaptor.forClass(Double.class);
        verify(consumer).accept(valueCaptor.capture());
        assertEquals(10.0, valueCaptor.getValue(), 0);
    }

    @Test
    public void testExpiring() {
        ExpiringCacheUpdate u = mock(ExpiringCacheUpdate.class);
        Consumer<Double> consumer = mock(Consumer.class);

        ExpiringCacheAsync<Double> t = new ExpiringCacheAsync<Double>(100, u);
        t.setValue(10.0);
        assertFalse(t.isExpired());

        // Request a value
        t.getValue(consumer);
        // There should be no call to update the cache
        verify(u, times(0)).requestCacheUpdate();
        // Wait
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            return;
        }
        // Request a value two times
        t.getValue(consumer);
        t.getValue(consumer);
        // There should be one call to update the cache
        verify(u, times(1)).requestCacheUpdate();
        assertTrue(t.isExpired());
    }

    @Test
    public void testFetchExpiredValue() {
        ExpiringCacheUpdate u = mock(ExpiringCacheUpdate.class);
        ExpiringCacheAsync<Double> t = new ExpiringCacheAsync<Double>(2000, u);
        t.setValue(10.0);
        // We should always be able to get the raw value, expired or not
        assertEquals(10.0, t.getExpiredValue(), 0);
        t.invalidateValue();
        assertTrue(t.isExpired());
        assertEquals(10.0, t.getExpiredValue(), 0);
    }
}
