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
package org.openhab.binding.verisure.internal.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The system health alerts of a Verisure installation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSystemHealthDTO {

    public static final String URGENCY_INFORMATION = "INFORMATION";
    private static final List<String> URGENCIES = List.of(URGENCY_INFORMATION, "WARNING", "CRITICAL");

    private @Nullable Data data;

    /**
     * @return the highest urgency among the alerts, INFORMATION if there are none
     */
    public String getUrgency() {
        String result = URGENCY_INFORMATION;
        for (Alert alert : getAlerts()) {
            String urgency = alert.urgencyType;
            if (urgency != null && URGENCIES.indexOf(urgency) > URGENCIES.indexOf(result)) {
                result = urgency;
            }
        }
        return result;
    }

    /**
     * @return comma separated types of the alerts that are more urgent than INFORMATION
     */
    public String getAlertTypes() {
        return getAlerts().stream()
                .filter(alert -> alert.alertType != null && !URGENCY_INFORMATION.equals(alert.urgencyType))
                .map(alert -> String.valueOf(alert.alertType)).collect(Collectors.joining(","));
    }

    private List<Alert> getAlerts() {
        Data localData = data;
        Installation installation = localData != null ? localData.installation : null;
        List<Alert> alerts = installation != null ? installation.systemHealthAlerts : null;
        return alerts != null ? alerts : List.of();
    }

    private static class Data {
        private @Nullable Installation installation;
    }

    private static class Installation {
        private @Nullable List<Alert> systemHealthAlerts;
    }

    private static class Alert {
        private @Nullable String urgencyType;
        private @Nullable String alertType;
    }
}
