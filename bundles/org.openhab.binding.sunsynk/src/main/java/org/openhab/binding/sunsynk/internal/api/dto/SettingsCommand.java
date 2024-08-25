/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SettingsCommand} is the internal class for sending a
 * Charge setting command to a Sun Synk Connect Account.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SettingsCommand {
    protected String sn = "";
    protected String safetyType = "";
    protected String battMode = "";
    protected String solarSell = "";
    protected String pvMaxLimit = "";
    protected int energyMode;
    protected int peakAndVallery;
    protected int sysWorkMode;
    protected String sellTime1 = "";
    protected String sellTime2 = "";
    protected String sellTime3 = "";
    protected String sellTime4 = "";
    protected String sellTime5 = "";
    protected String sellTime6 = "";
    protected int sellTime1Pac;
    protected int sellTime2Pac;
    protected int sellTime3Pac;
    protected int sellTime4Pac;
    protected int sellTime5Pac;
    protected int sellTime6Pac;
    protected int cap1;
    protected int cap2;
    protected int cap3;
    protected int cap4;
    protected int cap5;
    protected int cap6;
    protected String sellTime1Volt = "";
    protected String sellTime2Volt = "";
    protected String sellTime3Volt = "";
    protected String sellTime4Volt = "";
    protected String sellTime5Volt = "";
    protected String sellTime6Volt = "";
    protected String zeroExportPower = "";
    protected String solarMaxSellPower = "";
    protected String mondayOn = "";
    protected String tuesdayOn = "";
    protected String wednesdayOn = "";
    protected String thursdayOn = "";
    protected String fridayOn = "";
    protected String saturdayOn = "";
    protected String sundayOn = "";
    protected Boolean time1on = false;
    protected Boolean time2on = false;
    protected Boolean time3on = false;
    protected Boolean time4on = false;
    protected Boolean time5on = false;
    protected Boolean time6on = false;
    protected Boolean genTime1on = false;
    protected Boolean genTime2on = false;
    protected Boolean genTime3on = false;
    protected Boolean genTime4on = false;
    protected Boolean genTime5on = false;
    protected Boolean genTime6on = false;

    protected SettingsCommand() {
    }
}
