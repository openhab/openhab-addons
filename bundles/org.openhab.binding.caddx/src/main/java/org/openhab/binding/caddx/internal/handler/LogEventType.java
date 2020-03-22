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
package org.openhab.binding.caddx.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * All the log event types
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public enum LogEventType {
    Alarm(0, ZoneUserDevice.Zone, true, "Alarm"),
    AlarmRestore(1, ZoneUserDevice.Zone, true, "Alarm restore"),
    Bypass(2, ZoneUserDevice.Zone, true, "Bypass"),
    BypassRestore(3, ZoneUserDevice.Zone, true, "Bypass restore"),
    Tamper(4, ZoneUserDevice.Zone, true, "Tamper"),
    TamperRestore(5, ZoneUserDevice.Zone, true, "Tamper restore"),
    Trouble(6, ZoneUserDevice.Zone, true, "Trouble"),
    TroubleRestore(7, ZoneUserDevice.Zone, true, "Trouble restore"),
    TXLowBattery(8, ZoneUserDevice.Zone, true, "TX low battery"),
    TXLowBatteryRestore(9, ZoneUserDevice.Zone, true, "TX low battery restore"),
    ZoneLost(10, ZoneUserDevice.Zone, true, "Zone lost"),
    ZoneLostRestore(11, ZoneUserDevice.Zone, true, "Zone lost restore"),
    StartOfCrossTime(12, ZoneUserDevice.Zone, true, "Start of cross time"),
    SpecialExpansionEvent(17, ZoneUserDevice.None, false, "Special expansion event"),
    Duress(18, ZoneUserDevice.None, true, "Duress"),
    ManualFire(19, ZoneUserDevice.None, true, "Manual fire"),
    Auxiliary2Panic(20, ZoneUserDevice.None, true, "Auxiliary 2 panic"),
    Panic(22, ZoneUserDevice.None, true, "Panic"),
    KeypadTamper(23, ZoneUserDevice.None, true, "Keypad tamper"),
    ControlBoxTamper(24, ZoneUserDevice.Device, false, "Control box tamper"),
    ControlBoxTamperRestore(25, ZoneUserDevice.Device, false, "Control box tamper restore"),
    ACFail(26, ZoneUserDevice.Device, false, "AC fail"),
    ACFailRestore(27, ZoneUserDevice.Device, false, "AC fail restore"),
    LowBattery(28, ZoneUserDevice.Device, false, "Low battery"),
    LowBatteryRestore(29, ZoneUserDevice.Device, false, "Low battery restore"),
    OverCurrent(30, ZoneUserDevice.Device, false, "Over-current"),
    OverCurrentRestore(31, ZoneUserDevice.Device, false, "Over-current restore"),
    SirenTamper(32, ZoneUserDevice.Device, false, "Siren tamper"),
    SirenTamperRestore(33, ZoneUserDevice.Device, false, "Siren tamper restore"),
    TelephoneFault(34, ZoneUserDevice.None, false, "Telephone fault"),
    TelephoneFaultRestore(35, ZoneUserDevice.None, false, "Telephone fault restore"),
    ExpanderTrouble(36, ZoneUserDevice.Device, false, "Expander trouble"),
    ExpanderTroubleRestore(37, ZoneUserDevice.Device, false, "Expander trouble restore"),
    FailToCommunicate(38, ZoneUserDevice.None, false, "Fail to communicate"),
    LogFull(39, ZoneUserDevice.None, false, "Log full"),
    Opening(40, ZoneUserDevice.User, true, "Opening"),
    Closing(41, ZoneUserDevice.User, true, "Closing"),
    ExitError(42, ZoneUserDevice.User, true, "Exit error"),
    RecentClosing(43, ZoneUserDevice.User, true, "Recent closing"),
    AutoTest(44, ZoneUserDevice.None, false, "Auto-test"),
    StartProgram(45, ZoneUserDevice.None, false, "Start program"),
    EndProgram(46, ZoneUserDevice.None, false, "End program"),
    StartDownload(47, ZoneUserDevice.None, false, "Start download"),
    EndDownload(48, ZoneUserDevice.None, false, "End download"),
    Cancel(49, ZoneUserDevice.User, true, "Cancel"),
    GroundFault(50, ZoneUserDevice.None, false, "Ground fault"),
    GroundFaultRestore(51, ZoneUserDevice.None, false, "Ground fault restore"),
    ManualTest(52, ZoneUserDevice.None, false, "Manual test"),
    ClosedWithZonesBypassed(53, ZoneUserDevice.User, true, "Closed with zones bypassed"),
    StartOfListenIn(54, ZoneUserDevice.None, false, "Start of listen in"),
    TechnicianOnSite(55, ZoneUserDevice.None, false, "Technician on site"),
    TechnicianLeft(56, ZoneUserDevice.None, false, "Technician left"),
    ControlPowerUp(57, ZoneUserDevice.None, false, "Control power up"),
    FirstToOpen(120, ZoneUserDevice.User, true, "First to open"),
    LastToClose(121, ZoneUserDevice.User, true, "Last toC close"),
    PINEnteredWithBit7Set(122, ZoneUserDevice.User, true, "PIN entered with bit 7 set"),
    BeginWalkTest(123, ZoneUserDevice.None, false, "Begin walk-test"),
    EndWalkTest(124, ZoneUserDevice.None, false, "End walk-test"),
    ReExit(125, ZoneUserDevice.None, true, "Re-exit"),
    OutputTrip(126, ZoneUserDevice.User, false, "Output trip"),
    DataLost(127, ZoneUserDevice.None, false, "Data Lost");

    private static final Map<Integer, LogEventType> BY_LOG_EVENT_TYPE = new HashMap<>();
    public final int eventType;
    public final ZoneUserDevice zud;
    public final boolean isPartitionValid;
    public final String description;

    LogEventType(int eventType, ZoneUserDevice zud, boolean isPartitionValid, String description) {
        this.eventType = eventType;
        this.zud = zud;
        this.isPartitionValid = isPartitionValid;
        this.description = description;
    }

    static {
        for (LogEventType logEventType : values()) {
            BY_LOG_EVENT_TYPE.put(logEventType.eventType, logEventType);
        }
    }

    public static @Nullable LogEventType valueOfLogEventType(int eventType) {
        return BY_LOG_EVENT_TYPE.get(eventType);
    }
}
