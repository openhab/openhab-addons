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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Pfleger - Initial contribution
 * @author Marcin Czeczko - Added support for planner & calendar data
 */
public class Calendar {
    private List<CalendarTask> tasks = new ArrayList<>();

    public List<CalendarTask> getTasks() {
        return tasks;
    }
}
