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

/**
 * The {@link SensiboSky} represents a Sensibo Sky schedule
 *
 * @author Arne Seime - Initial contribution
 */
public class Schedule {
    public LocalTime targetLocalTime;
    public ZonedDateTime nextTime;
    public String[] recurringDays;
    public AcState acState;
    public boolean enabled;

    public Schedule(org.openhab.binding.sensibo.internal.dto.poddetails.Schedule dto) {
        this.targetLocalTime = LocalTime.parse(dto.targetLocalTime);
        this.nextTime = ZonedDateTime.parse(nextTime + "Z"); // API field seems to be in Zulu
        this.recurringDays = dto.recurringDays;
        this.acState = new AcState(dto.acState);
        this.enabled = dto.enabled;
    }
}
