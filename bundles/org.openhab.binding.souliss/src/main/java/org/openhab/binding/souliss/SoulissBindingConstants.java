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
package org.openhab.binding.souliss;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link SoulissBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tonino Fazio - Initial contribution
 */
@NonNullByDefault
public class SoulissBindingConstants {

    public static final String BINDING_ID = "souliss";

    public static final long DISCOVERY_RESEND_TIMEOUT_IN_MILLIS = 5000;
    public static final int DISCOVERY_TIMEOUT_IN_SECONDS = 60;
    public static final long SERVER_CICLE_IN_MILLIS = 10;
    public static final long SEND_DISPATCHER_MIN_DELAY_cicleInMillis = 10;

    // public static final int PING_resendTimeoutInSeconds = 5;

    // List of all Thing Type UIDs
    public static final ThingTypeUID GATEWAY_THING_TYPE = new ThingTypeUID(BINDING_ID, "gateway");

    public static final String T11 = "t11";
    public static final String T12 = "t12";
    public static final String T13 = "t13";
    public static final String T14 = "t14";
    public static final String T16 = "t16";
    public static final String T18 = "t18";
    public static final String T19 = "t19";
    public static final String T1A = "t1a";
    // public static final String LYT = "lyt";
    public static final String T21 = "t21";
    public static final String T22 = "t22";
    public static final String T31 = "t31";
    public static final String T41 = "t41";
    public static final String T42 = "t42";
    public static final String T51 = "t51";
    public static final String T52 = "t52";
    public static final String T53 = "t53";
    public static final String T54 = "t54";
    public static final String T55 = "t55";
    public static final String T56 = "t56";
    public static final String T57 = "t57";
    public static final String T58 = "t58";
    public static final String T61 = "t61";
    public static final String T62 = "t62";
    public static final String T63 = "t63";
    public static final String T64 = "t64";
    public static final String T65 = "t65";
    public static final String T66 = "t66";
    public static final String T67 = "t67";
    public static final String T68 = "t68";
    public static final String TOPICS = "topic";

    public static final ThingTypeUID T11_THING_TYPE = new ThingTypeUID(BINDING_ID, T11);
    public static final ThingTypeUID T12_THING_TYPE = new ThingTypeUID(BINDING_ID, T12);
    public static final ThingTypeUID T13_THING_TYPE = new ThingTypeUID(BINDING_ID, T13);
    public static final ThingTypeUID T14_THING_TYPE = new ThingTypeUID(BINDING_ID, T14);
    public static final ThingTypeUID T16_THING_TYPE = new ThingTypeUID(BINDING_ID, T16);
    public static final ThingTypeUID T18_THING_TYPE = new ThingTypeUID(BINDING_ID, T18);
    public static final ThingTypeUID T19_THING_TYPE = new ThingTypeUID(BINDING_ID, T19);
    public static final ThingTypeUID T1A_THING_TYPE = new ThingTypeUID(BINDING_ID, T1A);
    // public static final ThingTypeUID LYT_THING_TYPE = new ThingTypeUID(BINDING_ID, LYT);
    public static final ThingTypeUID T21_THING_TYPE = new ThingTypeUID(BINDING_ID, T21);
    public static final ThingTypeUID T22_THING_TYPE = new ThingTypeUID(BINDING_ID, T22);
    public static final ThingTypeUID T31_THING_TYPE = new ThingTypeUID(BINDING_ID, T31);
    public static final ThingTypeUID T41_THING_TYPE = new ThingTypeUID(BINDING_ID, T41);
    public static final ThingTypeUID T42_THING_TYPE = new ThingTypeUID(BINDING_ID, T42);
    public static final ThingTypeUID T51_THING_TYPE = new ThingTypeUID(BINDING_ID, T51);
    public static final ThingTypeUID T52_THING_TYPE = new ThingTypeUID(BINDING_ID, T52);
    public static final ThingTypeUID T53_THING_TYPE = new ThingTypeUID(BINDING_ID, T53);
    public static final ThingTypeUID T54_THING_TYPE = new ThingTypeUID(BINDING_ID, T54);
    public static final ThingTypeUID T55_THING_TYPE = new ThingTypeUID(BINDING_ID, T55);
    public static final ThingTypeUID T56_THING_TYPE = new ThingTypeUID(BINDING_ID, T56);
    public static final ThingTypeUID T57_THING_TYPE = new ThingTypeUID(BINDING_ID, T57);
    public static final ThingTypeUID T58_THING_TYPE = new ThingTypeUID(BINDING_ID, T58);
    public static final ThingTypeUID T61_THING_TYPE = new ThingTypeUID(BINDING_ID, T61);
    public static final ThingTypeUID T62_THING_TYPE = new ThingTypeUID(BINDING_ID, T62);
    public static final ThingTypeUID T63_THING_TYPE = new ThingTypeUID(BINDING_ID, T63);
    public static final ThingTypeUID T64_THING_TYPE = new ThingTypeUID(BINDING_ID, T64);
    public static final ThingTypeUID T65_THING_TYPE = new ThingTypeUID(BINDING_ID, T65);
    public static final ThingTypeUID T66_THING_TYPE = new ThingTypeUID(BINDING_ID, T66);
    public static final ThingTypeUID T67_THING_TYPE = new ThingTypeUID(BINDING_ID, T67);
    public static final ThingTypeUID T68_THING_TYPE = new ThingTypeUID(BINDING_ID, T68);
    public static final ThingTypeUID TOPICS_THING_TYPE = new ThingTypeUID(BINDING_ID, TOPICS);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(GATEWAY_THING_TYPE,
            T11_THING_TYPE, T12_THING_TYPE, T13_THING_TYPE, T14_THING_TYPE, T16_THING_TYPE, T18_THING_TYPE,
            T19_THING_TYPE, T1A_THING_TYPE, T21_THING_TYPE, T22_THING_TYPE, T31_THING_TYPE, T41_THING_TYPE,
            T42_THING_TYPE, T51_THING_TYPE, T52_THING_TYPE, T53_THING_TYPE, T54_THING_TYPE, T55_THING_TYPE,
            T56_THING_TYPE, T57_THING_TYPE, T58_THING_TYPE, T61_THING_TYPE, T62_THING_TYPE, T63_THING_TYPE,
            T64_THING_TYPE, T65_THING_TYPE, T66_THING_TYPE, T67_THING_TYPE, T68_THING_TYPE, TOPICS_THING_TYPE);

    // List of all Channel ids
    public final static String ONOFF_CHANNEL = "onoff";

    public final static String PULSE_CHANNEL = "pulse";
    public final static String SLEEP_CHANNEL = "sleep";
    public final static String AUTOMODE_CHANNEL = "automode";
    public final static String STATEONOFF_CHANNEL = "stateOnOff";
    public final static String STATEOPENCLOSE_CHANNEL = "stateOpenClose";
    public final static String ROLLERSHUTTER_CHANNEL = "rollershutter";
    public final static String ROLLERSHUTTER_STATE_CHANNEL_CHANNEL = "rollershutter_state";
    public static final String ROLLERSHUTTER_STATE_NUMBER_CHANNEL = "rollershutter_state_number";
    public final static String ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL = "opening";
    public final static String ROLLERSHUTTER_MESSAGE_CLOSING_CHANNEL = "closing";
    public final static String ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL = "limSwitch_open";
    public final static String ROLLERSHUTTER_MESSAGE_LIMITSWITCH_CLOSE_CHANNEL = "limSwitch_close";
    public final static String ROLLERSHUTTER_MESSAGE_STATE_OPEN_CHANNEL = "state_open";
    public final static String ROLLERSHUTTER_MESSAGE_STATE_CLOSE_CHANNEL = "state_close";
    public final static String ROLLERSHUTTER_MESSAGE_NO_LIMITSWITCH_CHANNEL = "NoLimSwitch";
    public static final String ROLLERSHUTTER_MESSAGE_STOP_CHANNEL = "stop";
    public static final String ROLLERSHUTTER_MESSAGE_TIMER_OFF = "timer off";

    public final static String T1A_1_CHANNEL = "one";
    public final static String T1A_2_CHANNEL = "two";
    public final static String T1A_3_CHANNEL = "three";
    public final static String T1A_4_CHANNEL = "four";
    public final static String T1A_5_CHANNEL = "five";
    public final static String T1A_6_CHANNEL = "six";
    public final static String T1A_7_CHANNEL = "seven";
    public final static String T1A_8_CHANNEL = "eight";

    public final static String T31_MODE_CHANNEL = "mode";
    public final static String T31_SYSTEM_CHANNEL = "system";
    public final static String T31_FIRE_CHANNEL = "fire";
    public final static String T31_FAN_CHANNEL = "fan";
    public final static String T31_BUTTON_CHANNEL = "setAsMeasured";
    public final static String T31_VALUE_CHANNEL = "measured";
    public final static String T31_SETPOINT_CHANNEL = "setpoint";

    public final static String T31_COOLINGMODE_MESSAGE_MODE_CHANNEL = "COOLING_MODE";
    public final static String T31_HEATINGMODE_MESSAGE_MODE_CHANNEL = "HEATING_MODE";
    public final static String T31_OFF_MESSAGE_SYSTEM_CHANNEL = "SYSTEM_OFF";
    public final static String T31_ON_MESSAGE_SYSTEM_CHANNEL = "SYSTEM_ON";
    public final static String T31_ON_MESSAGE_FIRE_CHANNEL = "FIRE_ON";
    public final static String T31_OFF_MESSAGE_FIRE_CHANNEL = "FIRE_OFF";

    public final static String T31_FANAUTO_MESSAGE_FAN_CHANNEL = "AUTO";
    public final static String T31_FANOFF_MESSAGE_FAN_CHANNEL = "FANOFF";
    public final static String T31_FANLOW_MESSAGE_FAN_CHANNEL = "LOW";
    public final static String T31_FANMEDIUM_MESSAGE_FAN_CHANNEL = "MEDIUM";
    public final static String T31_FANHIGH_MESSAGE_FAN_CHANNEL = "HIGH";

    public final static String T4N_ONOFFALARM_CHANNEL = "onOffAlarm";
    public final static String T4N_STATUSALARM_CHANNEL = "statusAlarm";
    public final static String T4N_REARMALARM_CHANNEL = "rearmAlarm";
    public final static String T41_RESETALARM_CHANNEL = "resetAlarm";

    public final static String T4N_ALARMON_MESSAGE_CHANNEL = "ALARMON";
    public final static String T4N_ALARMOFF_MESSAGE_CHANNEL = "ALARMOFF";
    public final static String T4N_REARMOFF_MESSAGE_CHANNEL = "REARMOFF";
    public final static String T4N_ARMED_MESSAGE_CHANNEL = "ARMED";

    public static final String WHITE_MODE_CHANNEL = "whitemode";
    public static final String ROLLER_BRIGHTNESS_CHANNEL = "roller_brightness";
    public static final String DIMMER_BRIGHTNESS_CHANNEL = "dimmer_brightness";
    public static final String LED_COLOR_CHANNEL = "ledcolor";

    public static final String LASTMESSAGE_CHANNEL = "lastMessage";
    public static final String LASTSTATUSSTORED_CHANNEL = "lastStatusStored";
    public static final String HEALTY_CHANNEL = "healty";

    public static final String T5N_VALUE_CHANNEL = "value";
    public static final String T6N_VALUE_CHANNEL = "value";
    public static final String FLOATING_POINT_CHANNEL = "float";
    public static final String HUMIDITY_CHANNEL = "humidity";
    public static final String TEMPERATURE_CHANNEL = "temperature";
    public static final String AMPERE_CHANNEL = "ampere";
    public static final String VOLTAGE_CHANNEL = "voltage";
    public static final String POWER_CHANNEL = "power";

    public static final String CONFIG_IP_ADDRESS = "GATEWAY_IP_ADDRESS";
    public static final String CONFIG_PORT = "GATEWAY_PORT_NUMBER";
    public static final String CONFIG_LOCAL_PORT = "PREFERRED_LOCAL_PORT_NUMBER";
    public static final String CONFIG_USER_INDEX = "USER_INDEX"; // DEFAULT 70;
    public static final String CONFIG_NODE_INDEX = "NODE_INDEX"; // DEFAULT 120; // 0..127
    public static final String CONFIG_ID = "ID";
    public static final String CONFIG_PING_REFRESH = "PING_INTERVAL";
    public static final String CONFIG_SUBSCRIPTION_REFRESH = "SUBSCRIBTION_INTERVAL";
    public static final String CONFIG_HEALTHY_REFRESH = "HEALTHY_INTERVAL";
    public static final String CONFIG_SEND_REFRESH = "SEND_INTERVAL";

    public static final String UUID_NODE_SLOT_SEPARATOR = "-";

    public static final String UUID_ELEMENTS_SEPARATOR = ":";

    public static final String CONFIG_SLEEP = "sleep";

    public static final String CONFIG_SECURE_SEND = "secureSend";

    public static final String CONFIG_TIMEOUT_TO_REQUEUE = "TIMEOUT_TO_REQUEUE";

    public static final String CONFIG_TIMEOUT_TO_REMOVE_PACKET = "TIMEOUT_TO_REMOVE_PACKET";
}
