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
package org.openhab.binding.ihc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link IhcBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class IhcBindingConstants {

    public static final String BINDING_ID = "ihc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");

    public static final String PROPERTY_MANUFACTURER = "controllerManufacturer";
    public static final String PROPERTY_SERIALNUMBER = "controllerSerialNumber";
    public static final String PROPERTY_SW_VERSION = "controllerSwVersion";
    public static final String PROPERTY_FW_VERSION = "controllerHwVersion";
    public static final String PROPERTY_APP_WITHOUT_VIEWER = "controllerIsWithoutViewer";
    public static final String PROPERTY_SW_DATE = "controllerSwDate";
    public static final String PROPERTY_PRODUCTION_DATE = "controllerProductionDate";
    public static final String PROPERTY_PROJECT_DATE = "projectDate";
    public static final String PROPERTY_PROJECT_NUMBER = "projectNumber";
    public static final String PROPERTY_DATALINE_VERSION = "controllerDatalineVersion";
    public static final String PROPERTY_RF_MODULE_VERSION = "controllerRfModSwVersion";
    public static final String PROPERTY_RF_MODULE_SERIALNUMBER = "controllerRfModSerialNumber";

    // List of all Channel ids
    public static final String CHANNEL_CONTROLLER_STATE = "controllerState";
    public static final String CHANNEL_CONTROLLER_UPTIME = "controllerUptime";
    public static final String CHANNEL_CONTROLLER_TIME = "controllerTime";

    // List of all Channel type ids
    public static final String CHANNEL_TYPE_CONTROLLER_STATE = "controller-state";
    public static final String CHANNEL_TYPE_CONTROLLER_SW_VER = "controller-sw-version";
    public static final String CHANNEL_TYPE_CONTROLLER_HW_VER = "controller-hw-version";
    public static final String CHANNEL_TYPE_CONTROLLER_UPTIME = "controller-uptime";
    public static final String CHANNEL_TYPE_CONTROLLER_TIME = "controller-time";

    public static final String CHANNEL_TYPE_NUMBER = "number";
    public static final String CHANNEL_TYPE_SWITCH = "switch";
    public static final String CHANNEL_TYPE_CONTACT = "contact";
    public static final String CHANNEL_TYPE_DIMMER = "dimmer";
    public static final String CHANNEL_TYPE_DATETIME = "datetime";
    public static final String CHANNEL_TYPE_STRING = "string";
    public static final String CHANNEL_TYPE_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_TYPE_RF_SIGNAL_STRENGTH = "rf-device-signal-strength";
    public static final String CHANNEL_TYPE_RF_LOW_BATTERY = "rf-device-low-battery";
    public static final String CHANNEL_TYPE_PUSH_BUTTON_TRIGGER = "push-button-trigger";

    // List of all channel parameters
    public static final String PARAM_RESOURCE_ID = "resourceId";
    public static final String PARAM_DIRECTION = "direction";
    public static final String PARAM_CMD_TO_REACT = "commandToReact";
    public static final String PARAM_SERIAL_NUMBER = "serialNumber";
    public static final String PARAM_PULSE_WIDTH = "pulseWidth";
    public static final String PARAM_LONG_PRESS_TIME = "longPressTime";
    public static final String PARAM_INVERTED = "inverted";
    public static final String PARAM_ON_LEVEL = "onLevel";

    public static final String DIRECTION_READ_WRITE = "ReadWrite";
    public static final String DIRECTION_WRITE_ONLY = "WriteOnly";
    public static final String DIRECTION_READ_ONLY = "ReadOnly";

    public static final String EVENT_PRESSED = "PRESSED";
    public static final String EVENT_RELEASED = "RELEASED";
    public static final String EVENT_SHORT_PRESS = "SHORT_PRESS";
    public static final String EVENT_LONG_PRESS = "LONG_PRESS";
}
