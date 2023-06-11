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
package org.openhab.binding.souliss.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SoulissBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public final class SoulissBindingConstants {

    public static final String BINDING_ID = "souliss";

    public static final long DISCOVERY_RESEND_TIMEOUT_IN_SECONDS = 45;
    public static final int DISCOVERY_TIMEOUT_IN_SECONDS = 120;
    public static final long SEND_DISPATCHER_MIN_DELAY_CYCLE_IN_MILLIS = 500;

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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(GATEWAY_THING_TYPE, T11_THING_TYPE,
            T12_THING_TYPE, T13_THING_TYPE, T14_THING_TYPE, T16_THING_TYPE, T18_THING_TYPE, T19_THING_TYPE,
            T1A_THING_TYPE, T21_THING_TYPE, T22_THING_TYPE, T31_THING_TYPE, T41_THING_TYPE, T42_THING_TYPE,
            T51_THING_TYPE, T52_THING_TYPE, T53_THING_TYPE, T54_THING_TYPE, T55_THING_TYPE, T56_THING_TYPE,
            T57_THING_TYPE, T58_THING_TYPE, T61_THING_TYPE, T62_THING_TYPE, T63_THING_TYPE, T64_THING_TYPE,
            T65_THING_TYPE, T66_THING_TYPE, T67_THING_TYPE, T68_THING_TYPE, TOPICS_THING_TYPE);

    // List of all Channel ids
    public static final String ONOFF_CHANNEL = "onOff";

    public static final String PULSE_CHANNEL = "pulse";
    public static final String SLEEP_CHANNEL = "sleep";
    public static final String AUTOMODE_CHANNEL = "autoMode";
    public static final String STATEONOFF_CHANNEL = "stateOnOff";
    public static final String STATEOPENCLOSE_CHANNEL = "stateOpenClose";
    public static final String ROLLERSHUTTER_CHANNEL = "rollerShutter";
    public static final String ROLLERSHUTTER_STATE_CHANNEL_CHANNEL = "rollerShutterState";

    public static final String ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL = "opening";
    public static final String ROLLERSHUTTER_MESSAGE_CLOSING_CHANNEL = "closing";
    public static final String ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL = "limSwitchOpen";
    public static final String ROLLERSHUTTER_MESSAGE_LIMITSWITCH_CLOSE_CHANNEL = "limSwitchClose";
    public static final String ROLLERSHUTTER_MESSAGE_STATE_OPEN_CHANNEL = "stateOpen";
    public static final String ROLLERSHUTTER_MESSAGE_STATE_CLOSE_CHANNEL = "stateClose";
    public static final String ROLLERSHUTTER_MESSAGE_NO_LIMITSWITCH_CHANNEL = "NoLimSwitch";
    public static final String ROLLERSHUTTER_MESSAGE_STOP_CHANNEL = "stop";
    public static final String ROLLERSHUTTER_MESSAGE_TIMER_OFF = "timer off";

    public static final String T1A_1_CHANNEL = "one";
    public static final String T1A_2_CHANNEL = "two";
    public static final String T1A_3_CHANNEL = "three";
    public static final String T1A_4_CHANNEL = "four";
    public static final String T1A_5_CHANNEL = "five";
    public static final String T1A_6_CHANNEL = "six";
    public static final String T1A_7_CHANNEL = "seven";
    public static final String T1A_8_CHANNEL = "eight";

    public static final String T31_MODE_CHANNEL = "mode";
    public static final String T31_SYSTEM_CHANNEL = "system";
    public static final String T31_FIRE_CHANNEL = "fire";
    public static final String T31_FAN_CHANNEL = "fan";
    public static final String T31_BUTTON_CHANNEL = "setAsMeasured";
    public static final String T31_VALUE_CHANNEL = "measured";
    public static final String T31_SETPOINT_CHANNEL = "setPoint";

    public static final String T31_COOLINGMODE_MESSAGE_MODE_CHANNEL = "COOLING_MODE";
    public static final String T31_HEATINGMODE_MESSAGE_MODE_CHANNEL = "HEATING_MODE";
    public static final String T31_OFF_MESSAGE_SYSTEM_CHANNEL = "SYSTEM_OFF";
    public static final String T31_ON_MESSAGE_SYSTEM_CHANNEL = "SYSTEM_ON";
    public static final String T31_ON_MESSAGE_FIRE_CHANNEL = "FIRE_ON";
    public static final String T31_OFF_MESSAGE_FIRE_CHANNEL = "FIRE_OFF";

    public static final String T31_FANAUTO_MESSAGE_FAN_CHANNEL = "AUTO";
    public static final String T31_FANOFF_MESSAGE_FAN_CHANNEL = "FANOFF";
    public static final String T31_FANLOW_MESSAGE_FAN_CHANNEL = "LOW";
    public static final String T31_FANMEDIUM_MESSAGE_FAN_CHANNEL = "MEDIUM";
    public static final String T31_FANHIGH_MESSAGE_FAN_CHANNEL = "HIGH";

    public static final String T4N_ONOFFALARM_CHANNEL = "onOffAlarm";
    public static final String T4N_STATUSALARM_CHANNEL = "statusAlarm";
    public static final String T4N_REARMALARM_CHANNEL = "rearmAlarm";
    public static final String T41_RESETALARM_CHANNEL = "resetAlarm";

    public static final String T4N_ALARMON_MESSAGE_CHANNEL = "ALARMON";
    public static final String T4N_ALARMOFF_MESSAGE_CHANNEL = "ALARMOFF";
    public static final String T4N_REARMOFF_MESSAGE_CHANNEL = "REARMOFF";
    public static final String T4N_ARMED_MESSAGE_CHANNEL = "ARMED";

    public static final String WHITE_MODE_CHANNEL = "whitemode";
    public static final String ROLLER_BRIGHTNESS_CHANNEL = "rollerBrightness";
    public static final String DIMMER_BRIGHTNESS_CHANNEL = "dimmerBrightness";
    public static final String LED_COLOR_CHANNEL = "ledcolor";

    public static final String LASTMESSAGE_CHANNEL = "lastMessage";
    public static final String LASTSTATUSSTORED_CHANNEL = "lastStatusStored";
    public static final String HEALTHY_CHANNEL = "healthy";

    public static final String T5N_VALUE_CHANNEL = "value";
    public static final String T6N_VALUE_CHANNEL = "value";
    public static final String FLOATING_POINT_CHANNEL = "float";
    public static final String HUMIDITY_CHANNEL = "humidity";
    public static final String TEMPERATURE_CHANNEL = "temperature";
    public static final String AMPERE_CHANNEL = "ampere";
    public static final String VOLTAGE_CHANNEL = "voltage";
    public static final String POWER_CHANNEL = "power";

    public static final String CONFIG_ID = "ID";
    public static final String CONFIG_IP_ADDRESS = "gatewayLanAddress";

    public static final String UUID_NODE_SLOT_SEPARATOR = "-";

    public static final String UUID_ELEMENTS_SEPARATOR = ":";

    public static final String CONFIG_SLEEP = "sleep";

    public static final String CONFIG_SECURE_SEND = "secureSend";

    public static final String CONFIG_TIMEOUT_TO_REQUEUE = "timeoutToRequeue";

    public static final String CONFIG_TIMEOUT_TO_REMOVE_PACKET = "timeoutToRemovePacket";

    // Properties
    public static final String PROPERTY_NODE = "node";
    public static final String PROPERTY_SLOT = "slot";
    public static final String PROPERTY_UNIQUEID = "uniqueId";

    // private constructor
    private SoulissBindingConstants() {
    }
}
