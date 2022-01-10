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
package org.openhab.binding.danfossairunit.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.test.java.JavaTest;

/**
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class ValueCacheTest extends JavaTest {

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
    public void updateValueInCacheUnchangedButCacheDurationExpired() {
        ValueCache valueCache = new ValueCache(1);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
        waitForAssert(() -> assertTrue(valueCache.updateValue("channel", OnOffType.ON)));
    }

    @Test
    public void updateValueMultipleCacheUpdatesButNotReportedAsToUpdate() {
        ValueCache valueCache = new ValueCache(100);
        assertTrue(valueCache.updateValue("channel", OnOffType.ON));
        waitForAssert(() -> assertFalse(valueCache.updateValue("channel", OnOffType.ON)));
        waitForAssert(() -> assertTrue(valueCache.updateValue("channel", OnOffType.ON)));
    }
}
