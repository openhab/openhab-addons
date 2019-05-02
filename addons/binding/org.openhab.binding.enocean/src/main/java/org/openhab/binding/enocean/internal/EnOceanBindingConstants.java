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
package org.openhab.binding.enocean.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link EnOceanBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanBindingConstants {

    public static final String BINDING_ID = "enocean";

    // bridge
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_PUSHBUTTON = new ThingTypeUID(BINDING_ID, "pushButton");
    public final static ThingTypeUID THING_TYPE_ROCKERSWITCH = new ThingTypeUID(BINDING_ID, "rockerSwitch");
    public final static ThingTypeUID THING_TYPE_CLASSICDEVICE = new ThingTypeUID(BINDING_ID, "classicDevice");

    public final static ThingTypeUID THING_TYPE_CENTRALCOMMAND = new ThingTypeUID(BINDING_ID, "centralCommand");
    public final static ThingTypeUID THING_TYPE_ROOMOPERATINGPANEL = new ThingTypeUID(BINDING_ID, "roomOperatingPanel");
    public final static ThingTypeUID THING_TYPE_MECHANICALHANDLE = new ThingTypeUID(BINDING_ID, "mechanicalHandle");
    public final static ThingTypeUID THING_TYPE_CONTACT = new ThingTypeUID(BINDING_ID, "contact");
    public final static ThingTypeUID THING_TYPE_MEASUREMENTSWITCH = new ThingTypeUID(BINDING_ID, "measurementSwitch");
    public final static ThingTypeUID THING_TYPE_TEMPERATURESENSOR = new ThingTypeUID(BINDING_ID, "temperatureSensor");
    public final static ThingTypeUID THING_TYPE_TEMPERATUREHUMIDITYSENSOR = new ThingTypeUID(BINDING_ID,
            "temperatureHumiditySensor");
    public final static ThingTypeUID THING_TYPE_AUTOMATEDMETERSENSOR = new ThingTypeUID(BINDING_ID,
            "automatedMeterSensor");
    public final static ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    public final static ThingTypeUID THING_TYPE_OCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID, "occupancySensor");
    public final static ThingTypeUID THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID,
            "lightTemperatureOccupancySensor");
    public final static ThingTypeUID THING_TYPE_LIGHTSENSOR = new ThingTypeUID(BINDING_ID, "lightSensor");
    public final static ThingTypeUID THING_TYPE_GENERICTHING = new ThingTypeUID(BINDING_ID, "genericThing");
    public final static ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_PUSHBUTTON, THING_TYPE_ROCKERSWITCH, THING_TYPE_CLASSICDEVICE,
                    THING_TYPE_CENTRALCOMMAND, THING_TYPE_ROOMOPERATINGPANEL, THING_TYPE_MECHANICALHANDLE,
                    THING_TYPE_CONTACT, THING_TYPE_MEASUREMENTSWITCH, THING_TYPE_TEMPERATURESENSOR,
                    THING_TYPE_TEMPERATUREHUMIDITYSENSOR, THING_TYPE_GENERICTHING, THING_TYPE_ROLLERSHUTTER,
                    THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, THING_TYPE_LIGHTSENSOR,
                    THING_TYPE_AUTOMATEDMETERSENSOR, THING_TYPE_THERMOSTAT));

    // List of all Channel Type Ids, these type ids are also used as channel ids during dynamic creation of channels
    // this makes it a lot easier as we do not have to manage a type id and an id, drawback long channel names
    public final static String CHANNEL_REPEATERMODE = "repeaterMode";
    public final static String CHANNEL_SETBASEID = "setBaseId";
    public final static String CHANNEL_GENERAL_SWITCHING = "generalSwitch";

    public final static String CHANNEL_GENERAL_SWITCHINGA = "generalSwitchA"; // used for D2-01-12 EEP
    public final static String CHANNEL_GENERAL_SWITCHINGB = "generalSwitchB"; // used for D2-01-12 EEP

    public final static String CHANNEL_DIMMER = "dimmer";
    public final static String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public final static String CHANNEL_ANGLE = "angle";
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_SETPOINT = "setPoint";
    public final static String CHANNEL_FANSPEEDSTAGE = "fanSpeedStage";
    public final static String CHANNEL_OCCUPANCY = "occupancy";
    public final static String CHANNEL_MOTIONDETECTION = "motionDetection";
    public final static String CHANNEL_ILLUMINATION = "illumination";
    public final static String CHANNEL_COUNTER = "counter";
    public final static String CHANNEL_CURRENTNUMBER = "currentNumber";

    public final static String CHANNEL_PUSHBUTTON = "pushButton";

    public final static String CHANNEL_ROCKERSWITCH_CHANNELA = "rockerswitchA";
    public final static String CHANNEL_ROCKERSWITCH_CHANNELB = "rockerswitchB";

    public final static String CHANNEL_VIRTUALSWITCHA = "virtualSwitchA";
    public final static String CHANNEL_VIRTUALROLLERSHUTTERA = "virtualRollershutterA";
    public final static String CHANNEL_VIRTUALROCKERSWITCHB = "virtualRockerswitchB";
    public final static String CHANNEL_ROCKERSWITCHLISTENERSWITCH = "rockerswitchListenerSwitch";
    public final static String CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER = "rockerswitchListenerRollershutter";
    public final static String CHANNEL_ROCKERSWITCHLISTENER_START = "rockerswitchListener";

    public final static String CHANNEL_WINDOWHANDLESTATE = "windowHandleState";
    public final static String CHANNEL_CONTACT = "contact";
    public final static String CHANNEL_TEACHINCMD = "teachInCMD";
    public final static String CHANNEL_INSTANTPOWER = "instantpower";
    public final static String CHANNEL_TOTALUSAGE = "totalusage";
    public final static String CHANNEL_INSTANTLITRE = "amrLitre";
    public final static String CHANNEL_TOTALCUBICMETRE = "amrCubicMetre";
    public final static String CHANNEL_BATTERY_VOLTAGE = "batteryVoltage";
    public final static String CHANNEL_ENERGY_STORAGE = "energyStorage";

    public final static String CHANNEL_AUTOOFF = "autoOFF";
    public final static String CHANNEL_DELAYRADIOOFF = "delayRadioOFF";
    public final static String CHANNEL_EXTERNALINTERFACEMODE = "externalInterfaceMode";
    public final static String CHANNEL_TWOSTATESWITCH = "twoStateSwitch";
    public final static String CHANNEL_ECOMODE = "ecoMode";

    public final static String CHANNEL_RSSI = "rssi";
    public final static String CHANNEL_REPEATCOUNT = "repeatCount";
    public final static String CHANNEL_LASTRECEIVED = "lastReceived";

    public final static String CHANNEL_GENERIC_SWITCH = "genericSwitch";
    public final static String CHANNEL_GENERIC_ROLLERSHUTTER = "genericRollershutter";
    public final static String CHANNEL_GENERIC_DIMMER = "genericDimmer";
    public final static String CHANNEL_GENERIC_NUMBER = "genericNumber";
    public final static String CHANNEL_GENERIC_STRING = "genericString";
    public final static String CHANNEL_GENERIC_COLOR = "genericColor";
    public final static String CHANNEL_GENERIC_TEACHINCMD = "genericTeachInCMD";

    public final static String CHANNEL_VALVE_POSITION = "valvePosition";
    public final static String CHANNEL_BUTTON_LOCK = "buttonLock";
    public final static String CHANNEL_DISPLAY_ORIENTATION = "displayOrientation";
    public final static String CHANNEL_TEMPERATURE_SETPOINT = "temperatureSetPoint";
    public final static String CHANNEL_FEED_TEMPERATURE = "feedTemperature";
    public final static String CHANNEL_MEASUREMENT_CONTROL = "measurementControl";
    public final static String CHANNEL_FAILURE_CODE = "failureCode";
    public final static String CHANNEL_WAKEUPCYCLE = "wakeUpCycle";
    public final static String CHANNEL_SERVICECOMMAND = "serviceCommand";
    public final static String CHANNEL_STATUS_REQUEST_EVENT = "statusRequestEvent";
    public final static String CHANNEL_SEND_COMMAND = "sendCommand";

    public static final Map<String, EnOceanChannelDescription> CHANNELID2CHANNELDESCRIPTION = Collections
            .unmodifiableMap(new HashMap<String, EnOceanChannelDescription>() {
                private static final long serialVersionUID = 1L;

                {
                    put(CHANNEL_GENERAL_SWITCHING, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERAL_SWITCHING), CoreItemFactory.SWITCH));

                    put(CHANNEL_GENERAL_SWITCHINGA, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERAL_SWITCHINGA), CoreItemFactory.SWITCH));
                    put(CHANNEL_GENERAL_SWITCHINGB, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERAL_SWITCHINGB), CoreItemFactory.SWITCH));

                    put(CHANNEL_DIMMER, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_DIMMER),
                            CoreItemFactory.DIMMER));
                    put(CHANNEL_ROLLERSHUTTER, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ROLLERSHUTTER), CoreItemFactory.ROLLERSHUTTER));
                    put(CHANNEL_ANGLE, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ANGLE),
                            CoreItemFactory.NUMBER));
                    put(CHANNEL_TEMPERATURE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TEMPERATURE), CoreItemFactory.NUMBER));
                    put(CHANNEL_HUMIDITY, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_HUMIDITY), CoreItemFactory.NUMBER));
                    put(CHANNEL_FANSPEEDSTAGE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_FANSPEEDSTAGE), CoreItemFactory.STRING));
                    put(CHANNEL_OCCUPANCY, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_OCCUPANCY), CoreItemFactory.SWITCH));
                    put(CHANNEL_MOTIONDETECTION, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_MOTIONDETECTION), CoreItemFactory.SWITCH));
                    put(CHANNEL_ILLUMINATION, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATION), CoreItemFactory.NUMBER));
                    put(CHANNEL_COUNTER, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_COUNTER),
                            CoreItemFactory.NUMBER));
                    put(CHANNEL_CURRENTNUMBER, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_CURRENTNUMBER), CoreItemFactory.NUMBER));
                    put(CHANNEL_SETPOINT, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SETPOINT), CoreItemFactory.NUMBER));
                    put(CHANNEL_CONTACT, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CONTACT),
                            CoreItemFactory.CONTACT));
                    put(CHANNEL_WINDOWHANDLESTATE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_WINDOWHANDLESTATE), CoreItemFactory.STRING));
                    put(CHANNEL_BATTERY_VOLTAGE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_BATTERY_VOLTAGE), CoreItemFactory.NUMBER));
                    put(CHANNEL_ENERGY_STORAGE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ENERGY_STORAGE), CoreItemFactory.NUMBER));
                    put(CHANNEL_TEACHINCMD, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TEACHINCMD), CoreItemFactory.SWITCH));

                    put(CHANNEL_PUSHBUTTON,
                            new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID(),
                                    null, "Push button", false, true));

                    put(CHANNEL_ROCKERSWITCH_CHANNELA,
                            new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER.getUID(),
                                    null, "Rocker Switch - Channel A", false, false));
                    put(CHANNEL_ROCKERSWITCH_CHANNELB,
                            new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER.getUID(),
                                    null, "Rocker Switch - Channel B", false, false));

                    put(CHANNEL_VIRTUALSWITCHA,
                            new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VIRTUALSWITCHA),
                                    CoreItemFactory.SWITCH, "", true, false));
                    put(CHANNEL_VIRTUALROLLERSHUTTERA,
                            new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VIRTUALROLLERSHUTTERA),
                                    CoreItemFactory.ROLLERSHUTTER, "", true, false));
                    put(CHANNEL_VIRTUALROCKERSWITCHB,
                            new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VIRTUALROCKERSWITCHB),
                                    CoreItemFactory.STRING, "Rocker Switch - Channel B", true, false));
                    put(CHANNEL_ROCKERSWITCHLISTENERSWITCH,
                            new EnOceanChannelDescription(
                                    new ChannelTypeUID(BINDING_ID, CHANNEL_ROCKERSWITCHLISTENERSWITCH),
                                    CoreItemFactory.SWITCH, "Rocker Switch Listener (Switch)", true, false));
                    put(CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER),
                            CoreItemFactory.ROLLERSHUTTER, "Rocker Switch Listener (Rollershutter)", true, false));

                    put(CHANNEL_INSTANTPOWER, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_INSTANTPOWER), CoreItemFactory.NUMBER));
                    put(CHANNEL_TOTALUSAGE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TOTALUSAGE), CoreItemFactory.NUMBER));
                    put(CHANNEL_INSTANTLITRE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_INSTANTLITRE), CoreItemFactory.NUMBER));
                    put(CHANNEL_TOTALCUBICMETRE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TOTALCUBICMETRE), CoreItemFactory.NUMBER));
                    put(CHANNEL_AUTOOFF, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_AUTOOFF),
                            CoreItemFactory.NUMBER));
                    put(CHANNEL_DELAYRADIOOFF, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_DELAYRADIOOFF), CoreItemFactory.NUMBER));
                    put(CHANNEL_EXTERNALINTERFACEMODE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_EXTERNALINTERFACEMODE), CoreItemFactory.STRING));
                    put(CHANNEL_TWOSTATESWITCH, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TWOSTATESWITCH), CoreItemFactory.SWITCH));
                    put(CHANNEL_ECOMODE, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ECOMODE),
                            CoreItemFactory.SWITCH));

                    put(CHANNEL_RSSI, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_RSSI),
                            CoreItemFactory.NUMBER));
                    put(CHANNEL_REPEATCOUNT, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_REPEATCOUNT), CoreItemFactory.NUMBER));
                    put(CHANNEL_LASTRECEIVED, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_LASTRECEIVED), CoreItemFactory.DATETIME));

                    put(CHANNEL_GENERIC_SWITCH, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_SWITCH), CoreItemFactory.SWITCH));
                    put(CHANNEL_GENERIC_ROLLERSHUTTER,
                            new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_ROLLERSHUTTER),
                                    CoreItemFactory.ROLLERSHUTTER));
                    put(CHANNEL_GENERIC_DIMMER, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_DIMMER), CoreItemFactory.DIMMER));
                    put(CHANNEL_GENERIC_NUMBER, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_NUMBER), CoreItemFactory.NUMBER));
                    put(CHANNEL_GENERIC_STRING, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_STRING), CoreItemFactory.STRING));
                    put(CHANNEL_GENERIC_COLOR, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_COLOR), CoreItemFactory.COLOR));
                    put(CHANNEL_GENERIC_TEACHINCMD, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_TEACHINCMD), CoreItemFactory.SWITCH));

                    put(CHANNEL_VALVE_POSITION, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_VALVE_POSITION), CoreItemFactory.NUMBER));
                    put(CHANNEL_BUTTON_LOCK, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_BUTTON_LOCK), CoreItemFactory.SWITCH));
                    put(CHANNEL_DISPLAY_ORIENTATION, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_DISPLAY_ORIENTATION), CoreItemFactory.NUMBER));
                    put(CHANNEL_TEMPERATURE_SETPOINT, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TEMPERATURE_SETPOINT), CoreItemFactory.NUMBER));
                    put(CHANNEL_FEED_TEMPERATURE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_FEED_TEMPERATURE), CoreItemFactory.NUMBER));
                    put(CHANNEL_MEASUREMENT_CONTROL, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_MEASUREMENT_CONTROL), CoreItemFactory.SWITCH));
                    put(CHANNEL_FAILURE_CODE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_FAILURE_CODE), CoreItemFactory.NUMBER));
                    put(CHANNEL_WAKEUPCYCLE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_WAKEUPCYCLE), CoreItemFactory.NUMBER));
                    put(CHANNEL_SERVICECOMMAND, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SERVICECOMMAND), CoreItemFactory.NUMBER));

                    put(CHANNEL_STATUS_REQUEST_EVENT, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_STATUS_REQUEST_EVENT), null, "", false, true));
                    put(CHANNEL_SEND_COMMAND, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SEND_COMMAND), CoreItemFactory.SWITCH));

                    put(CHANNEL_REPEATERMODE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_REPEATERMODE), CoreItemFactory.STRING));
                }
            });

    // List of all repeater mode states
    @NonNull
    public final static String REPEATERMODE_OFF = "OFF";
    public final static String REPEATERMODE_LEVEL_1 = "LEVEL1";
    public final static String REPEATERMODE_LEVEL_2 = "LEVEL2";

    // Bridge config properties
    public static final String SENDERID = "senderId";
    public static final String PATH = "path";
    public static final String HOST = "host";
    public static final String RS485 = "rs485";
    public static final String NEXTSENDERID = "nextSenderId";

    // Bridge properties
    @NonNull
    public static final String PROPERTY_BASE_ID = "Base ID";
    @NonNull
    public static final String PROPERTY_REMAINING_WRITE_CYCLES_Base_ID = "Remaining Base ID Write Cycles";
    @NonNull
    public static final String PROPERTY_APP_VERSION = "APP Version";
    @NonNull
    public static final String PROPERTY_API_VERSION = "API Version";
    @NonNull
    public static final String PROPERTY_CHIP_ID = "Chip ID";
    @NonNull
    public static final String PROPERTY_DESCRIPTION = "Description";

    // Thing properties
    public static final String PROPERTY_ENOCEAN_ID = "enoceanId";

    // Thing config parameter
    public static final String PARAMETER_SENDERIDOFFSET = "senderIdOffset";
    public static final String PARAMETER_SENDINGEEPID = "sendingEEPId";
    public static final String PARAMETER_RECEIVINGEEPID = "receivingEEPId";
    @NonNull
    public static final String PARAMETER_EEPID = "eepId";

    public static final String PARAMETER_BROADCASTMESSAGES = "broadcastMessages";
    public static final String PARAMETER_ENOCEANID = "enoceanId";

    // Channel config parameter
    public static final String PARAMETER_CHANNEL_TeachInMSG = "teachInMSG";
    public static final String PARAMETER_CHANNEL_Duration = "duration";
    public static final String PARAMETER_CHANNEL_SwitchMode = "switchMode";

    // Manufacturer Ids - used to recognize special EEPs during auto discovery
    public static final int ELTAKOID = 0x00d;
    public static final int NODONID = 0x046; // NodOn devices are designed by ID-RF hence use their ID
    public static final int PERMUNDOID = 0x033;

    public static final String EMPTYENOCEANID = "00000000";

}
