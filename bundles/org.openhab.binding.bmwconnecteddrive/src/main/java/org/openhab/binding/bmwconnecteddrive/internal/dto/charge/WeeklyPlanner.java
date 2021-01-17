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
package org.openhab.binding.bmwconnecteddrive.internal.dto.charge;

import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.ChargingMode;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.ChargingPreference;

/**
 * The {@link WeeklyPlanner} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - contributor
 */
public class WeeklyPlanner implements Cloneable {
    public boolean climatizationEnabled; // ": true,
    public String chargingMode;// ": "IMMEDIATE_CHARGING",
    public String chargingPreferences; // ": "CHARGING_WINDOW",
    public Timer timer1; // : {
    public Timer timer2;// ": {
    public Timer timer3;// ":{"departureTime":"00:00","timerEnabled":false,"weekdays":[]},"
    public Timer overrideTimer;// ":{"departureTime":"12:00","timerEnabled":false,"weekdays":["SATURDAY"]},"
    public ChargingWindow preferredChargingWindow;// ":{"startTime":"11:00","endTime":"17:00"}}

    public void completeWeeklyPlanner() {

        if (chargingMode == null) {
            chargingMode = ChargingMode.IMMEDIATE_CHARGING.name();
        }

        if (chargingPreferences == null) {
            chargingPreferences = ChargingPreference.CHARGING_WINDOW.name();
        }

        if (timer1 == null) {
            timer1 = new Timer();
        }
        timer1.completeTimer();

        if (timer2 == null) {
            timer2 = new Timer();
        }
        timer2.completeTimer();

        if (timer3 == null) {
            timer3 = new Timer();
        }
        timer3.completeTimer();

        if (overrideTimer == null) {
            overrideTimer = new Timer();
        }
        overrideTimer.completeTimer();

        if (preferredChargingWindow == null) {
            preferredChargingWindow = new ChargingWindow();
        }
        preferredChargingWindow.completeChargingWindow();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final WeeklyPlanner wp = (WeeklyPlanner) super.clone();
        wp.timer1 = (Timer) timer1.clone();
        wp.timer2 = (Timer) timer2.clone();
        wp.timer3 = (Timer) timer3.clone();
        wp.overrideTimer = (Timer) overrideTimer.clone();
        wp.preferredChargingWindow = (ChargingWindow) preferredChargingWindow.clone();
        return (Object) wp;
    }
}
