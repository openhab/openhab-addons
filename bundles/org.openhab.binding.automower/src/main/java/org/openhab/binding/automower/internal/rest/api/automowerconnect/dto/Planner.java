/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author Markus Pfleger - Initial contribution
 * @author Marcin Czeczko - Added support for planner and calendar data
 */
public class Planner {
    private long nextStartTimestamp;
    private RestrictedReason restrictedReason;
    private PlannerOverride override;

    public long getNextStartTimestamp() {
        return nextStartTimestamp;
    }

    public Planner setNextStartTimestamp(long nextStartTimestamp) {
        this.nextStartTimestamp = nextStartTimestamp;
        return this;
    }

    public RestrictedReason getRestrictedReason() {
        return restrictedReason;
    }

    public Planner setRestrictedReason(RestrictedReason restrictedReason) {
        this.restrictedReason = restrictedReason;
        return this;
    }

    public PlannerOverride getOverride() {
        return override;
    }

    public Planner setOverride(PlannerOverride override) {
        this.override = override;
        return this;
    }
}
