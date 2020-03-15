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
package org.openhab.binding.sensibo.internal.model;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SensiboSky} represents a Sensibo Sky schedule
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class Schedule {
    private LocalTime targetTimeLocal;
    private @Nullable ZonedDateTime nextTime;
    private String[] recurringDays;
    private AcState acState;
    private boolean enabled;

    public Schedule(org.openhab.binding.sensibo.internal.dto.poddetails.Schedule dto) {
        this.enabled = dto.enabled;
        if (enabled) {
            this.nextTime = ZonedDateTime.parse(nextTime + "Z"); // API field seems to be in Zulu
        }
        this.targetTimeLocal = LocalTime.parse(dto.targetTimeLocal);
        this.recurringDays = dto.recurringDays;
        this.acState = new AcState(dto.acState);
    }

    public LocalTime getTargetTimeLocal() {
        return targetTimeLocal;
    }

    public @Nullable ZonedDateTime getNextTime() {
        return nextTime;
    }

    public String[] getRecurringDays() {
        return recurringDays;
    }

    public AcState getAcState() {
        return acState;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
