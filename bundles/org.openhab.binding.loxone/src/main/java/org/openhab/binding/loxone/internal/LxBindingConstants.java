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
package org.openhab.binding.loxone.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Common constants used across the whole binding.
 *
 * @author Pawel Pieczul - Initial contribution
 */
@NonNullByDefault
public class LxBindingConstants {

    public static final String BINDING_ID = "loxone";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MINISERVER = new ThingTypeUID(BINDING_ID, "miniserver");

    // Channel Type IDs - read/write
    public static final String MINISERVER_CHANNEL_TYPE_SWITCH = "switchTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_LIGHT_CTRL = "lightCtrlTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RADIO_BUTTON = "radioButtonTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_ROLLERSHUTTER = "rollerShutterTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_DIMMER = "dimmerTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_NUMBER = "numberTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_COLORPICKER = "colorPickerTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_IROOM_V2_ACTIVE_MODE = "iRoomV2ActiveModeTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_IROOM_V2_OPERATING_MODE = "iRoomV2OperatingModeTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_IROOM_V2_PREPARE_STATE = "iRoomV2PrepareStateTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_IROOM_V2_COMFORT_TOLERANCE = "iRoomV2ComfortToleranceTypeId";
    // Channel Type IDs - read only
    public static final String MINISERVER_CHANNEL_TYPE_RO_TEXT = "roTextTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RO_SWITCH = "roSwitchTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RO_ANALOG = "roAnalogTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RO_NUMBER = "roNumberTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RO_DATETIME = "roDateTimeTypeId";

    // Miniserver properties and parameters
    public static final String MINISERVER_PARAM_HOST = "host";
    public static final String MINISERVER_PARAM_PORT = "port";
    public static final String MINISERVER_PROPERTY_MINISERVER_NAME = "name";
    public static final String MINISERVER_PROPERTY_PROJECT_NAME = "project";
    public static final String MINISERVER_PROPERTY_CLOUD_ADDRESS = "cloudAddress";

    // Location as configured on the Miniserver - it may be different to the Thing location property, which is user
    // defined and influences the grouping of items in the UI
    public static final String MINISERVER_PROPERTY_PHYSICAL_LOCATION = "physicalLocation";
}
