/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal.model;

import java.util.List;

/**
 * Represents a Gardena recurrence.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Recurrence {

    private String type;
    private List<String> weekdays;

    /**
     * Returns the type of the recurrence.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns a list of weekdays.
     */
    public List<String> getWeekdays() {
        return weekdays;
    }
}
