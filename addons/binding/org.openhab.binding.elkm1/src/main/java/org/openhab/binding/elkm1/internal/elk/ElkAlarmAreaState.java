/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.elk;

/**
 * The current alarm state of the area.
 *
 * @author David Bennett - Initial Contribution
 */
public enum ElkAlarmAreaState {
    NoAlarmActive,
    EntranceDelayIsActive,
    AlarmAbortDelayActive,
    FireAlarm,
    MedicalAlarm,
    PoliceAlarm,
    BurglarAlarm,
    Aux1Alarm,
    Aux2Alarm,
    Aux3Alarm,
    Aux4Alarm,
    CarbonMonoxideAlarm,
    EmergencyAlarm,
    FreezeAlarm,
    GasAlarm,
    HeatAlarm,
    WaterAlarm,
    FireSupervisory,
    VerifyFire
}
