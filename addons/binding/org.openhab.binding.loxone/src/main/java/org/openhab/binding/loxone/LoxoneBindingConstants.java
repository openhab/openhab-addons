/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Common constants used across the whole binding.
 *
 * @author Pawel Pieczul - Initial contribution
 */
public class LoxoneBindingConstants {

    public static final String BINDING_ID = "loxone";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MINISERVER = new ThingTypeUID(BINDING_ID, "miniserver");

    // Channel Type IDs - read/write
    public static final String MINISERVER_CHANNEL_TYPE_SWITCH = "switchTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_LIGHT_CTRL = "lightCtrlTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RADIO_BUTTON = "radioButtonTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_ROLLERSHUTTER = "rollerShutterTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_DIMMER = "dimmerTypeId";

    // Channel Type IDs - read only
    public static final String MINISERVER_CHANNEL_TYPE_RO_TEXT = "roTextTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RO_SWITCH = "roSwitchTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RO_ANALOG = "roAnalogTypeId";
    public static final String MINISERVER_CHANNEL_TYPE_RO_NUMBER = "roNumberTypeId";

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
