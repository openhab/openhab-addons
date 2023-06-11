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
package org.openhab.binding.mybmw.internal.dto.charge;

import java.util.List;

/**
 * The {@link ChargeProfile} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 */
public class ChargeProfile {
    public static final Timer INVALID_TIMER = new Timer();

    public ChargingWindow reductionOfChargeCurrent;
    public String chargingMode;// ": "immediateCharging",
    public String chargingPreference;// ": "chargingWindow",
    public String chargingControlType;// ": "weeklyPlanner",
    public List<Timer> departureTimes;
    public boolean climatisationOn;// ": false,
    public ChargingSettings chargingSettings;

    public Timer getTimerId(int id) {
        if (departureTimes != null) {
            for (Timer t : departureTimes) {
                if (t.id == id) {
                    return t;
                }
            }
        }
        return INVALID_TIMER;
    }
}
