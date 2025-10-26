/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.http.dto;

/**
 * Representation of the JSON returned by /getAlarmClock:{n}.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class AlarmClockInfo {

    /** "1" when the alarm is active, "0" when disabled */
    public int enable;

    /** Trigger type (0-5) – see API documentation for meaning */
    public int trigger;

    /** Operation to perform (0 shell, 1 playback/ring, 2 stop) */
    public int operation;

    /** Date in format YYYYMMDD for one-time alarms (only present when trigger==1) */
    public String date;

    /** Week day bitmask / value depending on trigger */
    public String weekDay;

    /** Day-of-month for monthly alarms (trigger==5) */
    public String day;

    /** Alarm time in HH:MM:SS (UTC) */
    public String time;

    /** Path or playback URL associated with the alarm */
    public String path;
}
