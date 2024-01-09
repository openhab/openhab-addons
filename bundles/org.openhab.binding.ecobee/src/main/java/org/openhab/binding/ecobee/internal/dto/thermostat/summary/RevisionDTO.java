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
package org.openhab.binding.ecobee.internal.dto.thermostat.summary;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RevisionDTO} contains information indicating what data
 * has changed on the thermostat(s).
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class RevisionDTO {

    /*
     * The thermostat identifier.
     */
    public String identifier;

    /*
     * The thermostat name, otherwise an empty field if one is not set.
     */
    public @Nullable String name;

    /*
     * Whether the thermostat is currently connected to the ecobee servers.
     */
    public @Nullable Boolean connected;

    /*
     * Current thermostat revision. This revision is incremented whenever
     * the thermostat program, hvac mode, settings or configuration change.
     * Changes to the following objects will update the thermostat revision:
     * Settings, Program, Event, Device.
     */
    public String thermostatRevision;

    /*
     * Current revision of the thermostat alerts. This revision is incremented
     * whenever a new Alert is issued or an Alert is modified (acknowledged or deferred).
     */
    public String alertsRevision;

    /*
     * The current revision of the thermostat runtime settings. This revision is
     * incremented whenever the thermostat transmits a new status message, or
     * updates the equipment state or Remote Sensor readings. The shortest interval
     * this revision may change is 3 minutes.
     */
    public String runtimeRevision;

    /*
     * The current revision of the thermostat interval runtime settings. This
     * revision is incremented whenever the thermostat transmits a new status message
     * in the form of a Runtime object. The thermostat updates this on a 15 minute interval.
     */
    public String intervalRevision;

    public RevisionDTO() {
        identifier = "";
        thermostatRevision = "";
        alertsRevision = "";
        runtimeRevision = "";
        intervalRevision = "";
    }

    public String getId() {
        return identifier;
    }

    public RevisionDTO getThis() {
        return this;
    }

    public boolean hasChanged(@Nullable RevisionDTO previous) {
        if (previous == null) {
            return true;
        }
        return !(thermostatRevision.equals(previous.thermostatRevision)
                && alertsRevision.equals(previous.alertsRevision) && runtimeRevision.equals(previous.runtimeRevision)
                && intervalRevision.equals(previous.intervalRevision));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id:").append(identifier);
        sb.append(" tstat:").append(thermostatRevision);
        sb.append(" alert:").append(alertsRevision);
        sb.append(" rntim:").append(runtimeRevision);
        sb.append(" intrv:").append(intervalRevision);
        return sb.toString();
    }
}
