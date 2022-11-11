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
package org.openhab.binding.ahawastecollection.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ahawastecollection.internal.CollectionDate.WasteType;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
@Disabled("These tests use the real website which may not always be available")
public class AhaCollectionScheduleTest {

    @Test
    public void testGetValuesForHannoverCity() throws Exception {
        final AhaCollectionScheduleImpl schedule = new AhaCollectionScheduleImpl("Hannover",
                "02095@Oesterleystr.+/+Südstadt@Südstadt", "10", "", "02095-0010+");
        final Map<WasteType, CollectionDate> dates = schedule.getCollectionDates();
        // No bio waste is collected here
        assertEquals(3, dates.size());
    }

    @Test
    public void testGetValuesForRegion() throws Exception {
        final AhaCollectionScheduleImpl schedule = new AhaCollectionScheduleImpl("Lehrte",
                "70185@Haselnussweg+/+Hämelerwald@Hämelerwald", "12", "", "70185-0012+");
        final Map<WasteType, CollectionDate> dates = schedule.getCollectionDates();
        // All four waste types are collected here.
        assertEquals(4, dates.size());
    }
}
