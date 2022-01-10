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

/**
 * The {@link WeeklyPlanner} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 */
public class WeeklyPlanner {
    public Boolean climatizationEnabled; // ": true,
    public String chargingMode;// ": "IMMEDIATE_CHARGING",
    public String chargingPreferences; // ": "CHARGING_WINDOW",
    public Timer timer1; // : {
    public Timer timer2;// ": {
    public Timer timer3;// ":{"departureTime":"00:00","timerEnabled":false,"weekdays":[]},"
    public Timer overrideTimer;// ":{"departureTime":"12:00","timerEnabled":false,"weekdays":["SATURDAY"]},"
    public ChargingWindow preferredChargingWindow;// ":{"startTime":"11:00","endTime":"17:00"}}
}
