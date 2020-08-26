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

package org.openhab.binding.elkm1.internal.elk;

/**
 * The definition for a zone.
 *
 * @author David Bennett - Initial COntribution
 */
public enum ElkDefinition {
    Disabled,
    BurglarEntryExit1,
    BurglarEntryExit2,
    BurglarPerimeterInstant,
    BurglarInterior,
    BurglarInteriorFollower,
    BurglarInteriorNight,
    BurglarInteriorNightDelay,
    Burglar24Hour,
    BurglarBoxTamper,
    FireAlarm,
    FireVerified,
    FireSupervisory,
    AuxAlarm1,
    AuxAlarm2,
    Keyfob,
    NonAlarm,
    CarbonMonoxide,
    EmergencyAlarm,
    FreezeAlarm,
    GasAlarm,
    HeatAlarm,
    MedicalAlarm,
    PoliceAlarm,
    PoliceNoIndication,
    WaterAlarm,
    KeyMomentaryArmDisarm,
    KeyMomentaryArmAway,
    KeyMomentaryArmStay,
    KeyMomentaryDisarm,
    KeyOnOff,
    MuteAudibles,
    PowerSupervisory,
    Temperature,
    AnalogZone,
    PhoneKey,
    IntercomKey,
}
