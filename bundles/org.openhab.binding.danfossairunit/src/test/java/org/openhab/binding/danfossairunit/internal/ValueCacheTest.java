/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Test;

/**
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class ValueCacheTest {

    @Test
    public void updateValueNotInCache() {
        ValueCache valueCache = new ValueCache(10000);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
    }

    @Test
    public void updateValueInCacheUnchangedWithinCacheDuration() {
        ValueCache valueCache = new ValueCache(10000);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
        assertFalse(valueCache.updateValue("channel", OnOffType.ON));
    }

    @Test
    public void updateValueInCacheChangedWithinCacheDuration() {
        ValueCache valueCache = new ValueCache(10000);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
        assertTrue(valueCache.updateValue("channel", OnOffType.OFF));
    }

    @Test
    public void updateValueInCacheUnchangedButCacheDurationExpired() throws InterruptedException {
        ValueCache valueCache = new ValueCache(1);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
        Thread.sleep(2);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
    }

    @Test
    public void updateValueMultipleCacheUpdatesButNotReportedAsToUpdate() throws InterruptedException {
        ValueCache valueCache = new ValueCache(60);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
        Thread.sleep(30);
        assertFalse(valueCache.updateValue("channel", OnOffType.ON));
        Thread.sleep(35);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
    }
}
