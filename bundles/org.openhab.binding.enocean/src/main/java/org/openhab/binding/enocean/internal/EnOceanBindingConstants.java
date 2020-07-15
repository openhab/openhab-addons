/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import javax.measure.quantity.Angle;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Power;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.ItemUtil;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.dimension.VolumetricFlowRate;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link EnOceanBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class EnOceanBindingConstants {

    public static final String BINDING_ID = "enocean";

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PUSHBUTTON = new ThingTypeUID(BINDING_ID, "pushButton");
    public static final ThingTypeUID THING_TYPE_ROCKERSWITCH = new ThingTypeUID(BINDING_ID, "rockerSwitch");
    public static final ThingTypeUID THING_TYPE_CLASSICDEVICE = new ThingTypeUID(BINDING_ID, "classicDevice");

    public static final ThingTypeUID THING_TYPE_CENTRALCOMMAND = new ThingTypeUID(BINDING_ID, "centralCommand");
    public static final ThingTypeUID THING_TYPE_ROOMOPERATINGPANEL = new ThingTypeUID(BINDING_ID, "roomOperatingPanel");
    public static final ThingTypeUID THING_TYPE_MECHANICALHANDLE = new ThingTypeUID(BINDING_ID, "mechanicalHandle");
    public static final ThingTypeUID THING_TYPE_CONTACT = new ThingTypeUID(BINDING_ID, "contact");
    public static final ThingTypeUID THING_TYPE_MEASUREMENTSWITCH = new ThingTypeUID(BINDING_ID, "measurementSwitch");
    public static final ThingTypeUID THING_TYPE_TEMPERATURESENSOR = new ThingTypeUID(BINDING_ID, "temperatureSensor");
    public static final ThingTypeUID THING_TYPE_TEMPERATUREHUMIDITYSENSOR = new ThingTypeUID(BINDING_ID,
            "temperatureHumiditySensor");
    public static final ThingTypeUID THING_TYPE_AUTOMATEDMETERSENSOR = new ThingTypeUID(BINDING_ID,
            "automatedMeterSensor");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public static final ThingTypeUID THING_TYPE_OCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID, "occupancySensor");
    public static final ThingTypeUID THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID,
            "lightTemperatureOccupancySensor");
    public static final ThingTypeUID THING_TYPE_LIGHTSENSOR = new ThingTypeUID(BINDING_ID, "lightSensor");
    public static final ThingTypeUID THING_TYPE_ENVIRONMENTALSENSOR = new ThingTypeUID(BINDING_ID,
            "environmentalSensor");
    public static final ThingTypeUID THING_TYPE_GENERICTHING = new ThingTypeUID(BINDING_ID, "genericThing");
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");
    public static final ThingTypeUID THING_TYPE_MULTFUNCTIONSMOKEDETECTOR = new ThingTypeUID(BINDING_ID,
            "multiFunctionSmokeDetector");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(
            THING_TYPE_PUSHBUTTON, THING_TYPE_ROCKERSWITCH, THING_TYPE_CLASSICDEVICE, THING_TYPE_CENTRALCOMMAND,
            THING_TYPE_ROOMOPERATINGPANEL, THING_TYPE_MECHANICALHANDLE, THING_TYPE_CONTACT,
            THING_TYPE_MEASUREMENTSWITCH, THING_TYPE_TEMPERATURESENSOR, THING_TYPE_TEMPERATUREHUMIDITYSENSOR,
            THING_TYPE_GENERICTHING, THING_TYPE_ROLLERSHUTTER, THING_TYPE_OCCUPANCYSENSOR,
            THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, THING_TYPE_LIGHTSENSOR, THING_TYPE_ENVIRONMENTALSENSOR,
            THING_TYPE_AUTOMATEDMETERSENSOR, THING_TYPE_THERMOSTAT, THING_TYPE_MULTFUNCTIONSMOKEDETECTOR));

    // List of all Channel Type Ids, these type ids are also used as channel ids during dynamic creation of channels
    // this makes it a lot easier as we do not have to manage a type id and an id, drawback long channel names
    public static final String CHANNEL_REPEATERMODE = "repeaterMode";
    public static final String CHANNEL_SETBASEID = "setBaseId";
    public static final String CHANNEL_GENERAL_SWITCHING = "generalSwitch";

    public static final String CHANNEL_GENERAL_SWITCHINGA = "generalSwitchA"; // used for D2-01-12 EEP
    public static final String CHANNEL_GENERAL_SWITCHINGB = "generalSwitchB"; // used for D2-01-12 EEP

    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_ANGLE = "angle";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_SETPOINT = "setPoint";
    public static final String CHANNEL_FANSPEEDSTAGE = "fanSpeedStage";
    public static final String CHANNEL_OCCUPANCY = "occupancy";
    public static final String CHANNEL_MOTIONDETECTION = "motionDetection";
    public static final String CHANNEL_VIBRATION = "vibration";
    public static final String CHANNEL_ILLUMINATION = "illumination";
    public static final String CHANNEL_ILLUMINATIONWEST = "illuminationWest";
    public static final String CHANNEL_ILLUMINATIONSOUTHNORTH = "illuminationSouthNorth";
    public static final String CHANNEL_ILLUMINATIONEAST = "illuminationEast";
    public static final String CHANNEL_WINDSPEED = "windspeed";
    public static final String CHANNEL_RAINSTATUS = "rainStatus";
    public static final String CHANNEL_COUNTER = "counter";
    public static final String CHANNEL_CURRENTNUMBER = "currentNumber";
    public static final String CHANNEL_SMOKEDETECTION = "smokeDetection";
    public static final String CHANNEL_SENSORFAULT = "sensorFault";
    public static final String CHANNEL_MAINTENANCESTATUS = "maintenanceStatus";
    public static final String CHANNEL_SENSORANALYSISHUMIDITYRANGE = "saHumidityRange";
    public static final String CHANNEL_SENSORANALYSISTEMPERATURRANGE = "saTemperatureRange";
    public static final String CHANNEL_TIMESINCELASTMAINTENANCE = "timeSinceLastMaintenance";
    public static final String CHANNEL_REMAININGPLT = "remainingPLT";
    public static final String CHANNEL_HYGROCOMFORTINDEX = "hygroComfortIndex";
    public static final String CHANNEL_INDOORAIRANALYSIS = "indoorAirAnalysis";

    public static final String CHANNEL_PUSHBUTTON = "pushButton";
    public static final String CHANNEL_DOUBLEPRESS = "doublePress";
    public static final String CHANNEL_LONGPRESS = "longPress";

    public static final String CHANNEL_ROCKERSWITCH_CHANNELA = "rockerswitchA";
    public static final String CHANNEL_ROCKERSWITCH_CHANNELB = "rockerswitchB";

    public static final String CHANNEL_VIRTUALSWITCHA = "virtualSwitchA";
    public static final String CHANNEL_VIRTUALROLLERSHUTTERA = "virtualRollershutterA";
    public static final String CHANNEL_VIRTUALROCKERSWITCHB = "virtualRockerswitchB";
    public static final String CHANNEL_ROCKERSWITCHLISTENERSWITCH = "rockerswitchListenerSwitch";
    public static final String CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER = "rockerswitchListenerRollershutter";
    public static final String CHANNEL_ROCKERSWITCHLISTENER_START = "rockerswitchListener";

    public static final String CHANNEL_WINDOWHANDLESTATE = "windowHandleState";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_TEACHINCMD = "teachInCMD";
    public static final String CHANNEL_INSTANTPOWER = "instantpower";
    public static final String CHANNEL_TOTALUSAGE = "totalusage";
    public static final String CHANNEL_CURRENTFLOW = "currentFlow";
    public static final String CHANNEL_CUMULATIVEVALUE = "cumulativeValue";
    public static final String CHANNEL_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String CHANNEL_ENERGY_STORAGE = "energyStorage";
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_BATTERYLOW = "batteryLow";

    public static final String CHANNEL_AUTOOFF = "autoOFF";
    public static final String CHANNEL_DELAYRADIOOFF = "delayRadioOFF";
    public static final String CHANNEL_EXTERNALINTERFACEMODE = "externalInterfaceMode";
    public static final String CHANNEL_TWOSTATESWITCH = "twoStateSwitch";
    public static final String CHANNEL_ECOMODE = "ecoMode";

    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_REPEATCOUNT = "repeatCount";
    public static final String CHANNEL_LASTRECEIVED = "lastReceived";

    public static final String CHANNEL_GENERIC_SWITCH = "genericSwitch";
    public static final String CHANNEL_GENERIC_ROLLERSHUTTER = "genericRollershutter";
    public static final String CHANNEL_GENERIC_DIMMER = "genericDimmer";
    public static final String CHANNEL_GENERIC_NUMBER = "genericNumber";
    public static final String CHANNEL_GENERIC_STRING = "genericString";
    public static final String CHANNEL_GENERIC_COLOR = "genericColor";
    public static final String CHANNEL_GENERIC_TEACHINCMD = "genericTeachInCMD";

    public static final String CHANNEL_VALVE_POSITION = "valvePosition";
    public static final String CHANNEL_BUTTON_LOCK = "buttonLock";
    public static final String CHANNEL_DISPLAY_ORIENTATION = "displayOrientation";
    public static final String CHANNEL_TEMPERATURE_SETPOINT = "temperatureSetPoint";
    public static final String CHANNEL_FEED_TEMPERATURE = "feedTemperature";
    public static final String CHANNEL_MEASUREMENT_CONTROL = "measurementControl";
    public static final String CHANNEL_FAILURE_CODE = "failureCode";
    public static final String CHANNEL_WAKEUPCYCLE = "wakeUpCycle";
    public static final String CHANNEL_SERVICECOMMAND = "serviceCommand";
    public static final String CHANNEL_STATUS_REQUEST_EVENT = "statusRequestEvent";
    public static final String CHANNEL_SEND_COMMAND = "sendCommand";

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
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Angle.class.getSimpleName()));
                    put(CHANNEL_TEMPERATURE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TEMPERATURE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName()));
                    put(CHANNEL_HUMIDITY, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_HUMIDITY), CoreItemFactory.NUMBER));
                    put(CHANNEL_FANSPEEDSTAGE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_FANSPEEDSTAGE), CoreItemFactory.STRING));
                    put(CHANNEL_OCCUPANCY, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_OCCUPANCY), CoreItemFactory.SWITCH));
                    put(CHANNEL_MOTIONDETECTION, new EnOceanChannelDescription(
                            DefaultSystemChannelTypeProvider.SYSTEM_MOTION.getUID(), CoreItemFactory.SWITCH));
                    put(CHANNEL_VIBRATION, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_VIBRATION), CoreItemFactory.SWITCH));
                    put(CHANNEL_ILLUMINATION, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATION),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Illuminance.class.getSimpleName()));
                    put(CHANNEL_ILLUMINATIONWEST, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATIONWEST),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Illuminance.class.getSimpleName()));
                    put(CHANNEL_ILLUMINATIONSOUTHNORTH, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATIONSOUTHNORTH),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Illuminance.class.getSimpleName()));
                    put(CHANNEL_ILLUMINATIONEAST, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATIONEAST),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Illuminance.class.getSimpleName()));
                    put(CHANNEL_WINDSPEED, new EnOceanChannelDescription(
                            DefaultSystemChannelTypeProvider.SYSTEM_WIND_SPEED.getUID(),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Speed.class.getSimpleName()));
                    put(CHANNEL_RAINSTATUS, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_RAINSTATUS), CoreItemFactory.SWITCH));
                    put(CHANNEL_COUNTER, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_COUNTER),
                            CoreItemFactory.NUMBER));
                    put(CHANNEL_CURRENTNUMBER, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_CURRENTNUMBER), CoreItemFactory.NUMBER));
                    put(CHANNEL_SMOKEDETECTION, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SMOKEDETECTION), CoreItemFactory.SWITCH));
                    put(CHANNEL_SENSORFAULT, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SENSORFAULT), CoreItemFactory.SWITCH));
                    put(CHANNEL_MAINTENANCESTATUS, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SENSORFAULT), CoreItemFactory.SWITCH));
                    put(CHANNEL_SENSORANALYSISHUMIDITYRANGE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SENSORFAULT), CoreItemFactory.SWITCH));
                    put(CHANNEL_SENSORANALYSISTEMPERATURRANGE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SENSORFAULT), CoreItemFactory.SWITCH));
                    put(CHANNEL_TIMESINCELASTMAINTENANCE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TIMESINCELASTMAINTENANCE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Time.class.getSimpleName()));
                    put(CHANNEL_REMAININGPLT, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_REMAININGPLT),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Time.class.getSimpleName()));
                    put(CHANNEL_HYGROCOMFORTINDEX, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_HYGROCOMFORTINDEX), CoreItemFactory.STRING));
                    put(CHANNEL_INDOORAIRANALYSIS, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_INDOORAIRANALYSIS), CoreItemFactory.STRING));
                    put(CHANNEL_SETPOINT, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_SETPOINT), CoreItemFactory.NUMBER));
                    put(CHANNEL_CONTACT, new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CONTACT),
                            CoreItemFactory.CONTACT));
                    put(CHANNEL_WINDOWHANDLESTATE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_WINDOWHANDLESTATE), CoreItemFactory.STRING));
                    put(CHANNEL_BATTERY_VOLTAGE,
                            new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_BATTERY_VOLTAGE),
                                    CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                            + ElectricPotential.class.getSimpleName()));
                    put(CHANNEL_ENERGY_STORAGE,
                            new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ENERGY_STORAGE),
                                    CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                            + ElectricPotential.class.getSimpleName()));
                    put(CHANNEL_BATTERY_LEVEL,
                            new EnOceanChannelDescription(
                                    DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_BATTERY_LEVEL.getUID(),
                                    CoreItemFactory.NUMBER));
                    put(CHANNEL_BATTERYLOW,
                            new EnOceanChannelDescription(
                                    DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_LOW_BATTERY.getUID(),
                                    CoreItemFactory.SWITCH));
                    put(CHANNEL_TEACHINCMD, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TEACHINCMD), CoreItemFactory.SWITCH));

                    put(CHANNEL_PUSHBUTTON,
                            new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID(),
                                    null, "Push button", false, true));
                    put(CHANNEL_DOUBLEPRESS,
                            new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID(),
                                    null, "Double press", false, true));
                    put(CHANNEL_LONGPRESS,
                            new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID(),
                                    null, "Long press", false, true));

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
                            new ChannelTypeUID(BINDING_ID, CHANNEL_INSTANTPOWER),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Power.class.getSimpleName()));
                    put(CHANNEL_TOTALUSAGE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TOTALUSAGE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Energy.class.getSimpleName()));
                    put(CHANNEL_CURRENTFLOW,
                            new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CURRENTFLOW),
                                    CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                            + VolumetricFlowRate.class.getSimpleName()));
                    put(CHANNEL_CUMULATIVEVALUE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_CUMULATIVEVALUE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Volume.class.getSimpleName()));
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
                            new ChannelTypeUID(BINDING_ID, CHANNEL_TEMPERATURE_SETPOINT),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName()));
                    put(CHANNEL_FEED_TEMPERATURE, new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_FEED_TEMPERATURE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName()));
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
    public static final String REPEATERMODE_OFF = "OFF";
    public static final String REPEATERMODE_LEVEL_1 = "LEVEL1";
    public static final String REPEATERMODE_LEVEL_2 = "LEVEL2";

    // Bridge config properties
    public static final String SENDERID = "senderId";
    public static final String PATH = "path";
    public static final String HOST = "host";
    public static final String RS485 = "rs485";
    public static final String NEXTSENDERID = "nextSenderId";

    // Bridge properties
    public static final String PROPERTY_BASE_ID = "Base ID";
    public static final String PROPERTY_REMAINING_WRITE_CYCLES_Base_ID = "Remaining Base ID Write Cycles";
    public static final String PROPERTY_APP_VERSION = "APP Version";
    public static final String PROPERTY_API_VERSION = "API Version";
    public static final String PROPERTY_CHIP_ID = "Chip ID";
    public static final String PROPERTY_DESCRIPTION = "Description";

    // Thing properties
    public static final String PROPERTY_ENOCEAN_ID = "enoceanId";

    // Thing config parameter
    public static final String PARAMETER_SENDERIDOFFSET = "senderIdOffset";
    public static final String PARAMETER_SENDINGEEPID = "sendingEEPId";
    public static final String PARAMETER_RECEIVINGEEPID = "receivingEEPId";
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

    public static final byte ZERO = (byte) 0;
}
