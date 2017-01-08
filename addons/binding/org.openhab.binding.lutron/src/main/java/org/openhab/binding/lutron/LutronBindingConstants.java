/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
    public final static ThingTypeUID THING_TYPE_IPBRIDGE = new ThingTypeUID(BINDING_ID, "ipbridge");

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public final static ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public final static ThingTypeUID THING_TYPE_OCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID, "occupancysensor");
    public final static ThingTypeUID THING_TYPE_KEYPAD = new ThingTypeUID(BINDING_ID, "keypad");
    public final static ThingTypeUID THING_TYPE_PHANTOM = new ThingTypeUID(BINDING_ID, "phantom");

    // List of all Channel ids
    public final static String CHANNEL_LIGHTLEVEL = "lightlevel";
    public final static String CHANNEL_SWITCH = "switchstatus";
    public final static String CHANNEL_OCCUPANCYSTATUS = "occupancystatus";
    public final static String CHANNEL_BUTTON1 = "button1";
    public final static String CHANNEL_BUTTON2 = "button2";
    public final static String CHANNEL_BUTTON3 = "button3";
    public final static String CHANNEL_BUTTON4 = "button4";
    public final static String CHANNEL_BUTTON5 = "button5";
    public final static String CHANNEL_BUTTON6 = "button6";
    public final static String CHANNEL_BUTTON7 = "button7";
    public final static String CHANNEL_BUTTONTOPRAISE = "buttontopraise";
    public final static String CHANNEL_BUTTONTOPLOWER = "buttontoplower";
    public final static String CHANNEL_BUTTONBOTTOMRAISE = "buttonbottomraise";
    public final static String CHANNEL_BUTTONBOTTOMLOWER = "buttonbottomlower";
    public final static String CHANNEL_LED1 = "led1";
    public final static String CHANNEL_LED2 = "led2";
    public final static String CHANNEL_LED3 = "led3";
    public final static String CHANNEL_LED4 = "led4";
    public final static String CHANNEL_LED5 = "led5";
    public final static String CHANNEL_LED6 = "led6";
    public final static String CHANNEL_LED7 = "led7";

    // Phantom
    public final static String CHANNEL_PBUTTON1 = "pbutton1";
    public final static String CHANNEL_PBUTTON2 = "pbutton2";
    public final static String CHANNEL_PBUTTON3 = "pbutton3";
    public final static String CHANNEL_PBUTTON4 = "pbutton4";
    public final static String CHANNEL_PBUTTON5 = "pbutton5";
    public final static String CHANNEL_PBUTTON6 = "pbutton6";
    public final static String CHANNEL_PBUTTON7 = "pbutton7";
    public final static String CHANNEL_PBUTTON8 = "pbutton8";
    public final static String CHANNEL_PBUTTON9 = "pbutton9";
    public final static String CHANNEL_PBUTTON10 = "pbutton10";
    /*
     * //Keeping this in case I want to re-add LED support.
     * public final static String CHANNEL_PLED1 = "pled1";
     * public final static String CHANNEL_PLED2 = "pled2";
     * public final static String CHANNEL_PLED3 = "pled3";
     * public final static String CHANNEL_PLED4 = "pled4";
     * public final static String CHANNEL_PLED5 = "pled5";
     * public final static String CHANNEL_PLED6 = "pled6";
     * public final static String CHANNEL_PLED7 = "pled7";
     * public final static String CHANNEL_PLED8 = "pled8";
     * public final static String CHANNEL_PLED9 = "pled9";
     * public final static String CHANNEL_PLED10 = "pled10";
     */

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String SERIAL_NUMBER = "serialNumber";

    // Thing config properties
    public static final String INTEGRATION_ID = "integrationId";
}
