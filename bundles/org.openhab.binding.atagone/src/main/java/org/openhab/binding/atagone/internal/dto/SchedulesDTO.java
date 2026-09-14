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
package org.openhab.binding.atagone.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Gson DTO for the {@code schedules} block in a {@code retrieve_reply}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class SchedulesDTO {
    /** Central heating schedule. */
    public ScheduleDTO ch_schedule;
    /** Domestic hot water schedule. */
    public ScheduleDTO dhw_schedule;
}
