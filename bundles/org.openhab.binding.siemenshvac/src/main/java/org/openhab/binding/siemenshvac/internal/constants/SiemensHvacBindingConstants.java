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
package org.openhab.binding.siemenshvac.internal.constants;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SiemensHvacBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacBindingConstants {

    public static final String BINDING_ID = "siemenshvac";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OZW = new ThingTypeUID(BINDING_ID, "ozw");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(SiemensHvacBindingConstants.THING_TYPE_OZW);

    public static final String IP_ADDRESS = "ipAddress";
    public static final String BASE_URL = "baseUrl";

    public static final String PROPERTY_VENDOR_NAME = "Siemens";

    public static final String CONFIG_DESCRIPTION_URI_THING_PREFIX = "thing-type";

    public static final String DPT_TYPE_ENUM = "Enumeration";
    public static final String DPT_TYPE_NUMERIC = "Numeric";
    public static final String DPT_TYPE_RADIO = "RadioButton";
    public static final String DPT_TYPE_DATE_TIME = "DateTime";
    public static final String DPT_TYPE_TIMEOFDAY = "TimeOfDay";
    public static final String DPT_TYPE_STRING = "String";

    public static final String DPT_TYPE_CHECKBOX = "CheckBox";
    public static final String DPT_TYPE_SCHEDULER = "Scheduler";
    public static final String DPT_TYPE_CALENDAR = "Calendar";

    public static final String CATEGORY_THING_HVAC = "HVAC";

    public static final String CATEGORY_CHANNEL_NUMBER = "Number";
    public static final String CATEGORY_CHANNEL_SWITCH = "Switch";
    public static final String CATEGORY_CHANNEL_TEMP = "Temperature";
    public static final String CATEGORY_CHANNEL_TIME = "Time";

    public static final String CATEGORY_CHANNEL_CONTROL_HEATING = "Heating";
}
