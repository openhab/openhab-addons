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
package org.openhab.binding.siemenshvac.internal.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SiemensHvacBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent ARNAL - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacBindingConstants {

    public static final String BINDING_ID = "siemenshvac";

    public static final String CONFIG_DESCRIPTION_URI_CHANNEL = "channel-type:siemenshvac:config";

    // List of all Thing Type UIDs
    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OZW672 = new ThingTypeUID(BINDING_ID, "ozw672");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

    public static final String IP_ADDRESS = "ipAddress";
    public static final String BASE_URL = "baseUrl";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final String PROPERTY_VENDOR_NAME = "Siemens";

    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_ROLLERSHUTTER = "Rollershutter";
    public static final String ITEM_TYPE_CONTACT = "Contact";
    public static final String ITEM_TYPE_STRING = "String";
    public static final String ITEM_TYPE_NUMBER = "Number";
    public static final String ITEM_TYPE_DIMMER = "Dimmer";
    public static final String ITEM_TYPE_DATETIME = "DateTime";

    public static final String CONFIG_DESCRIPTION_URI_THING_PREFIX = "thing-type";

    public static final String DPT_TYPE_STRING = "String";
    public static final String DPT_TYPE_ENUM = "Enumeration";
    public static final String DPT_TYPE_NUMERIC = "Numeric";
    public static final String DPT_TYPE_RADIO = "RadioButton";
    public static final String DPT_TYPE_DATE = "DateTime";
    public static final String DPT_TYPE_TIME = "TimeOfDay";
    public static final String DPT_TYPE_SCHEDULER = "Scheduler";
    public static final String DPT_TYPE_CALENDAR = "Calendar";

    public static final String CATEGORY_THING_HVAC = "HVAC";

    public static final String CATEGORY_CHANNEL_WIDGETS_NUMBER = "Number";
    public static final String CATEGORY_CHANNEL_WIDGETS_SLIDER = "Slider";
    public static final String CATEGORY_CHANNEL_WIDGETS_SWITCH = "Switch";
    public static final String CATEGORY_CHANNEL_WIDGETS_TEXT = "Text";
    public static final String CATEGORY_CHANNEL_WIDGETS_GROUP = "Group";

    public static final String CATEGORY_CHANNEL_PROPS_TEMP = "Temperature";
    public static final String CATEGORY_CHANNEL_PROPS_TIME = "Time";

    public static final String CATEGORY_CHANNEL_CONTROL_HEATING = "Heating";
}
