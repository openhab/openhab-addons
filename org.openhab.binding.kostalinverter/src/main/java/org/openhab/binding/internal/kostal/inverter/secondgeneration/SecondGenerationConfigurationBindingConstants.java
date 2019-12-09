/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.internal.kostal.inverter.secondgeneration;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SecondGenerationConfigurationBindingConstants} class defines constants, which are
 * used in the second generation part of the binding.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */

public class SecondGenerationConfigurationBindingConstants {

    private static final String BINDING_ID = "kostalinverter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_KOSTALINVERTER = new ThingTypeUID(BINDING_ID, "kostalinverterpiko1020");

    // List of all Channel IDs
    public static final String CHANNEL_CHARGETIMEEND = "chargeTimeEnd";
    public static final String CHANNEL_BATTERYTYPE = "batteryType";
    public static final String CHANNEL_BATTERYUSAGECONSUMPTION = "batteryUsageConsumption";
    public static final String CHANNEL_BATTERYUSAGESTRATEGY = "batteryUsageStrategy";
    public static final String CHANNEL_SMARTBATTERYCONTROL = "smartBatteryControl";
    public static final String CHANNEL_SMARTBATTERYCONTROL_TEXT = "smartBatteryControl_Text";
    public static final String CHANNEL_BATTERYCHARGETIMEFROM = "batteryChargeTimeFrom";
    public static final String CHANNEL_BATTERYCHARGETIMETO = "batteryChargeTimeTo";
    public static final String CHANNEL_MAXDEPTHOFDISCHARGE = "maxDepthOfDischarge";
    public static final String CHANNEL_SHADOWMANAGEMENT = "shadowManagement";
    public static final String CHANNEL_EXTERNALMODULECONTROL = "externalModuleControl";
    public static final String CHANNEL_INVERTERNAME = "inverterName";
}
