/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.transitapp.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult.ScheduleItem;

import com.google.gson.Gson;

/**
 * The {@link DepartureOrderingTest} is responsible for testing departure sorting.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class DepartureOrderingTest {

    @Test
    public void testDepartureOrdering() {
        Gson gson = new Gson();

        // Simuliere API-Antworten über Gson, um die fehlenden Setter zu umgehen
        ScheduleItem item1 = gson.fromJson("{ \"departure_time\": 1700000000 }", ScheduleItem.class);
        assertNotNull(item1, "Parsed ScheduleItem 1 should not be null");

        ScheduleItem item2 = gson.fromJson("{ \"departure_time\": 1600000000 }", ScheduleItem.class);
        assertNotNull(item2, "Parsed ScheduleItem 2 should not be null");

        List<ScheduleItem> departures = new ArrayList<>();
        departures.add(item1);
        departures.add(item2);

        // Sortier-Logik über die neuen Getter testen
        departures.sort(Comparator.comparing(a -> {
            Instant t = a.getDepartureTime();
            return t != null ? t.getEpochSecond() : 0L;
        }));

        assertNotNull(departures);
        assertEquals(2, departures.size());

        // Erwartung: item2 (früher) steht jetzt an erster Stelle
        Instant firstDeparture = departures.get(0).getDepartureTime();
        Instant secondDeparture = departures.get(1).getDepartureTime();

        assertNotNull(firstDeparture);
        assertNotNull(secondDeparture);
        assertEquals(1600000000L, firstDeparture.getEpochSecond());
        assertEquals(1700000000L, secondDeparture.getEpochSecond());
    }
}
