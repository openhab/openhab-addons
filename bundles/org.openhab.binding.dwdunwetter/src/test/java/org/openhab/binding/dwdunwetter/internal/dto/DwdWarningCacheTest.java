/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dwdunwetter.internal.dto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link org.openhab.binding.dwdunwetter.internal.dto.DwdWarningCache}
 *
 * @author Martin Koehler - Initial contribution
 */
public class DwdWarningCacheTest {

    private DwdWarningCache cache;

    @BeforeEach
    public void setUp() {
        cache = new DwdWarningCache();
    }

    @Test
    public void testAddEntry() {
        DwdWarningData data = createData("ID", 0);

        assertThat(cache.addEntry(data), is(true));
        assertThat(cache.addEntry(data), is(false));
    }

    @Test
    public void testDeleteOldEntries() {
        DwdWarningData data = createData("ID", 0);
        cache.addEntry(data);

        cache.deleteOldEntries();
        assertThat(cache.addEntry(data), is(false));

        data = createData("ID", 60 * 60);
        assertThat(cache.addEntry(data), is(false));
        cache.deleteOldEntries();
        assertThat(cache.addEntry(data), is(true));
    }

    private DwdWarningData createData(String id, long secondsBeforeNow) {
        DwdWarningData data = new DwdWarningData();
        data.setId(id);
        data.setExpires(Instant.now().minusSeconds(secondsBeforeNow));
        return data;
    }
}
