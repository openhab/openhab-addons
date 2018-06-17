/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc;

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

    // List of all Channel ids
    public static final String CHANNEL_CONTROLLER_STATE = "controllerState";
    public static final String CHANNEL_CONTROLLER_UPTIME = "controllerUptime";
    public static final String CHANNEL_CONTROLLER_TIME = "controllerTime";
    public static final String CHANNEL_CONTROLLER_HW_VERSION = "controllerHwVersion";
    public static final String CHANNEL_CONTROLLER_SW_VERSION = "controllerSwVersion";

    // List of all Channel type ids
    public final static String CHANNEL_TYPE_CONTROLLER_STATE = "controller-state";
    public final static String CHANNEL_TYPE_CONTROLLER_SW_VER = "controller-sw-version";
    public final static String CHANNEL_TYPE_CONTROLLER_HW_VER = "controller-hw-version";
    public final static String CHANNEL_TYPE_CONTROLLER_UPTIME = "controller-uptime";
    public final static String CHANNEL_TYPE_CONTROLLER_TIME = "controller-time";

    public final static String CHANNEL_TYPE_NUMBER = "number-channel";
    public final static String CHANNEL_TYPE_SWITCH = "switch-channel";
    public final static String CHANNEL_TYPE_CONTACT = "contact-channel";
    public final static String CHANNEL_TYPE_DIMMER = "dimmer-channel";
    public final static String CHANNEL_TYPE_DATETIME = "datetime-channel";
    public final static String CHANNEL_TYPE_STRING = "string-channel";
    public final static String CHANNEL_TYPE_ROLLERSHUTTER = "rollershutter-channel";
    public final static String CHANNEL_TYPE_RF_SIGNAL_STRENGTH = "rf-device-signal-strength-channel";
    public final static String CHANNEL_TYPE_RF_LOW_BATTERY = "rf-device-low-battery-channel";
    public final static String CHANNEL_TYPE_PUSH_BUTTON_TRIGGER = "push-button-trigger";

    // List of all channel parameters
    public static final String PARAM_RESOURCE_ID = "resourceId";
    public final static String PARAM_DIRECTION = "direction";
    public final static String PARAM_CMD_TO_REACT = "commandToReact";
    public static final String PARAM_SERIAL_NUMBER = "serialNumber";
    public static final String PARAM_PULSE_WIDTH = "pulseWidth";
    public final static String PARAM_EXTRA_LONG_PRESS_MAX_TIME = "extraLongPressMaxTime";
    public final static String PARAM_LONG_PRESS_MAX_TIME = "longPressMaxTime";
    public final static String PARAM_SHORT_PRESS_MAX_TIME = "shortPressMaxTime";
    public final static String PARAM_SPECIAL_COMMAND = "specialCommand";
    public static final String PARAM_INVERTED = "inverted";

    public final static String DIRECTION_READ_WRITE = "ReadWrite";
    public final static String DIRECTION_WRITE_ONLY = "WriteOnly";
    public final static String DIRECTION_READ_ONLY = "ReadOnly";

    public final static String EVENT_SHORT_PRESS = "SHORT_PRESS";
    public final static String EVENT_LONG_PRESS = "LONG_PRESS";
    public final static String EVENT_EXTRA_LONG_PRESS = "EXTRA_LONG_PRESS";
}
