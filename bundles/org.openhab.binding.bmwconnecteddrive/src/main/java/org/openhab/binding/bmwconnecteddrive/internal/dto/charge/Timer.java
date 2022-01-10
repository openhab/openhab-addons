/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.charge;

import java.util.List;

/**
 * The {@link Timer} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 */
public class Timer {
    public String departureTime;// ": "05:00",
    public Boolean timerEnabled;// ": false,
    public List<String> weekdays;
    /**
     * "MONDAY",
     * "TUESDAY",
     * "WEDNESDAY",
     * "THURSDAY",
     * "FRIDAY"
     * ] '
     */
}
