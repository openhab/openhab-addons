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
package org.openhab.binding.transit.internal.net.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * The {@link ServiceAlertTest} tests the selection of active and most severe service alerts.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class ServiceAlertTest {

    private final Gson gson = new Gson();

    // values taken from the examples of the ServiceAlert schema in the API documentation
    private static final String JSON = """
            {
              "alerts": [
                {
                  "effect": "NO_SERVICE",
                  "cause": "TECHNICAL_PROBLEM",
                  "severity": "Severe",
                  "title": "Service disruption",
                  "description": "Due to mechanical issues, service is suspended between Station A and Station B.",
                  "created_at": 1639584000,
                  "active_periods": [ { "start": 1639584000, "end": 1639670400 } ]
                },
                {
                  "effect": "DETOUR",
                  "severity": "Info",
                  "title": "Detour",
                  "created_at": 1639584000,
                  "active_periods": [ { "start": 1639584000, "end": null } ]
                },
                {
                  "severity": "Warning",
                  "title": "No period"
                }
              ]
            }
            """;

    @Test
    public void testActiveAndMostSevere() {
        NetworkAlertsResult result = Objects.requireNonNull(gson.fromJson(JSON, NetworkAlertsResult.class));
        List<ServiceAlert> alerts = result.getAlerts();
        assertEquals(3, alerts.size());

        // inside the period of the severe alert: all three are active
        List<ServiceAlert> active = ServiceAlert.getActiveAlerts(alerts, 1639600000L);
        assertEquals(3, active.size());
        ServiceAlert mostSevere = Objects.requireNonNull(ServiceAlert.getMostSevere(active));
        assertEquals("Severe", mostSevere.getSeverity());
        assertEquals("NO_SERVICE", mostSevere.getEffect());
        assertEquals("TECHNICAL_PROBLEM", mostSevere.getCause());

        // after the end of the severe alert: the open-ended and the period-less alert remain
        active = ServiceAlert.getActiveAlerts(alerts, 1639700000L);
        assertEquals(2, active.size());
        assertEquals("No period", Objects.requireNonNull(ServiceAlert.getMostSevere(active)).getTitle());

        // before all periods: only the alert without period is active
        assertEquals(1, ServiceAlert.getActiveAlerts(alerts, 1600000000L).size());

        assertNull(ServiceAlert.getMostSevere(List.of()));
        assertTrue(ServiceAlert.getActiveAlerts(null, 0).isEmpty());
    }
}
