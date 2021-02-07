/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link E3DCBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Björn Brings - Initial contribution
 */
@NonNullByDefault
public class E3DCBindingConstants {

    private static final String BINDING_ID = "e3dc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_E3DC = new ThingTypeUID(BINDING_ID, "e3dc");

    // List of all Channel ids
    public static final String CHANNEL_CurrentPowerPV = "CurrentPowerPV";
    public static final String CHANNEL_CurrentPowerBat = "CurrentPowerBat";
    public static final String CHANNEL_CurrentPowerHome = "CurrentPowerHome";
    public static final String CHANNEL_CurrentPowerGrid = "CurrentPowerGrid";
    public static final String CHANNEL_CurrentPowerAdd = "CurrentPowerAdd";
    public static final String CHANNEL_BatterySOC = "BatterySOC";
    public static final String CHANNEL_SelfConsumption = "SelfConsumption";
    public static final String CHANNEL_Autarky = "Autarky";
    public static final String CHANNEL_CurrentPMEnergyL1 = "CurrentPMEnergyL1";
    public static final String CHANNEL_CurrentPMEnergyL2 = "CurrentPMEnergyL2";
    public static final String CHANNEL_CurrentPMEnergyL3 = "CurrentPMEnergyL3";
    public static final String CHANNEL_CurrentPMPowerL1 = "CurrentPMPowerL1";
    public static final String CHANNEL_CurrentPMPowerL2 = "CurrentPMPowerL2";
    public static final String CHANNEL_CurrentPMPowerL3 = "CurrentPMPowerL3";
    public static final String CHANNEL_CurrentPMVoltageL1 = "CurrentPMVoltageL1";
    public static final String CHANNEL_CurrentPMVoltageL2 = "CurrentPMVoltageL2";
    public static final String CHANNEL_CurrentPMVoltageL3 = "CurrentPMVoltageL3";
    public static final String CHANNEL_Mode = "Mode";
    public static final String CHANNEL_PowerLimitsUsed = "PowerLimitsUsed";
    public static final String CHANNEL_MaxDischarge = "MaxDischarge";
    public static final String CHANNEL_MaxCharge = "MaxCharge";
    public static final String CHANNEL_DischargeStart = "DischargeStart";
    public static final String CHANNEL_WeatherRegulatedCharge = "WeatherRegulatedCharge";
    public static final String CHANNEL_PowerSave = "PowerSave";
    public static final String CHANNEL_EmergencyPowerStatus = "EmergencyPowerStatus";
    public static final String CHANNEL_GridConnected = "GridConnected";
    public static final String CHANNEL_SWRelease = "SWRelease";
}
