/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.awtrixng.internal.dto;

/**
 * The settings JSON published retained on {@code <prefix>/state/settings} and accepted as a partial update on
 * {@code <prefix>/cmd/settings}. Null fields are omitted, so a command only changes what was set.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class Settings {
    public Boolean autoBrightness;
    public Integer brightness;
    public Integer saturation;
    public Double gamma;
    public String textColor;
    public Boolean uppercase;
    public ScrollConfig scroll;
    public Boolean autoTransition;
    public Integer appDurationMs;
    public String transitionEffect;
    public String transitionDirection;
    public Integer transitionDurationMs;
    public Integer timeMode;
    public String timeColor;
    public String calendarHeaderColor;
    public String calendarTextColor;
    public String calendarBodyColor;
    public Boolean time24h;
    public Boolean timeLeadingZero;
    public Boolean timeShowSeconds;
    public Boolean timeShowAmPm;
    public String timeSeparatorMode;
    public String dateOrder;
    public String dateSeparator;
    public String dateYearMode;
    public Boolean dateShowWeekday;
    public Boolean dateMonthNames;
    public String dateColor;
    public WeekdayBar weekdayBar;
    public WeekdayBar dateWeekdayBar;
    public Boolean useCelsius;
    public String temperatureColor;
    public String humidityColor;
    public String batteryColor;
    public Integer volume;
    public Integer appVolume;
    public Integer alertVolume;
    public Boolean blockNavigation;
}
