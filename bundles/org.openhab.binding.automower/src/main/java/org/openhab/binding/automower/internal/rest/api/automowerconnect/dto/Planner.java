/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
    private int externalReason;

    public long getNextStartTimestamp() {
        return nextStartTimestamp;
    }

    public void setNextStartTimestamp(long nextStartTimestamp) {
        this.nextStartTimestamp = nextStartTimestamp;
    }

    public RestrictedReason getRestrictedReason() {
        return restrictedReason;
    }

    public void setRestrictedReason(RestrictedReason restrictedReason) {
        this.restrictedReason = restrictedReason;
    }

    public PlannerOverride getOverride() {
        return override;
    }

    public void setOverride(PlannerOverride override) {
        this.override = override;
    }

    public int getExternalReason() {
        return externalReason;
    }

    public void setExternalReason(int externalReason) {
        this.externalReason = externalReason;
    }
}
