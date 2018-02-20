/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LutronBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Allan Tong - Initial contribution
 */
public class LutronBindingConstants {

    public static final String BINDING_ID = "lutron";

    // Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_IPBRIDGE = new ThingTypeUID(BINDING_ID, "ipbridge");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_OCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID, "occupancysensor");
    public static final ThingTypeUID THING_TYPE_KEYPAD = new ThingTypeUID(BINDING_ID, "keypad");

    // List of all Channel ids
    public static final String CHANNEL_LIGHTLEVEL = "lightlevel";
    public static final String CHANNEL_SWITCH = "switchstatus";
    public static final String CHANNEL_OCCUPANCYSTATUS = "occupancystatus";
    public static final String CHANNEL_BUTTON1 = "button1";
    public static final String CHANNEL_BUTTON2 = "button2";
    public static final String CHANNEL_BUTTON3 = "button3";
    public static final String CHANNEL_BUTTON4 = "button4";
    public static final String CHANNEL_BUTTON5 = "button5";
    public static final String CHANNEL_BUTTON6 = "button6";
    public static final String CHANNEL_BUTTON7 = "button7";
    public static final String CHANNEL_BUTTONTOPRAISE = "buttontopraise";
    public static final String CHANNEL_BUTTONTOPLOWER = "buttontoplower";
    public static final String CHANNEL_BUTTONBOTTOMRAISE = "buttonbottomraise";
    public static final String CHANNEL_BUTTONBOTTOMLOWER = "buttonbottomlower";
    public static final String CHANNEL_LED1 = "led1";
    public static final String CHANNEL_LED2 = "led2";
    public static final String CHANNEL_LED3 = "led3";
    public static final String CHANNEL_LED4 = "led4";
    public static final String CHANNEL_LED5 = "led5";
    public static final String CHANNEL_LED6 = "led6";
    public static final String CHANNEL_LED7 = "led7";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String SERIAL_NUMBER = "serialNumber";

    // Thing config properties
    public static final String INTEGRATION_ID = "integrationId";
}
