/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Map;
import java.util.Set;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Power;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.ItemUtil;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.dimension.VolumetricFlowRate;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link EnOceanBindingConstants} class defines common constants, which are
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
    public static final ThingTypeUID THING_TYPE_GASSENSOR = new ThingTypeUID(BINDING_ID, "gasSensor");
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

    public static final ThingTypeUID THING_TYPE_HEATRECOVERYVENTILATION = new ThingTypeUID(BINDING_ID,
            "heatRecoveryVentilation");
    public static final ThingTypeUID THING_TYPE_WINDOWSASHHANDLESENSOR = new ThingTypeUID(BINDING_ID,
            "windowSashHandleSensor");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_PUSHBUTTON,
            THING_TYPE_ROCKERSWITCH, THING_TYPE_CLASSICDEVICE, THING_TYPE_CENTRALCOMMAND, THING_TYPE_ROOMOPERATINGPANEL,
            THING_TYPE_MECHANICALHANDLE, THING_TYPE_CONTACT, THING_TYPE_MEASUREMENTSWITCH, THING_TYPE_TEMPERATURESENSOR,
            THING_TYPE_TEMPERATUREHUMIDITYSENSOR, THING_TYPE_GASSENSOR, THING_TYPE_GENERICTHING,
            THING_TYPE_ROLLERSHUTTER, THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR,
            THING_TYPE_LIGHTSENSOR, THING_TYPE_ENVIRONMENTALSENSOR, THING_TYPE_AUTOMATEDMETERSENSOR,
            THING_TYPE_THERMOSTAT, THING_TYPE_MULTFUNCTIONSMOKEDETECTOR, THING_TYPE_HEATRECOVERYVENTILATION,
            THING_TYPE_WINDOWSASHHANDLESENSOR);

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
    public static final String CHANNEL_CO2 = "co2";
    public static final String CHANNEL_CO = "co";
    public static final String CHANNEL_TVOC = "totalVolatileOrganicCompounds";
    public static final String CHANNEL_VOC = "volatileOrganicCompounds";
    public static final String CHANNEL_VOC_ID = "volatileOrganicCompoundsId";
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
    public static final String CHANNEL_PUSHBUTTON2 = "pushButton2";
    public static final String CHANNEL_DOUBLEPRESS = "doublePress";
    public static final String CHANNEL_LONGPRESS = "longPress";

    public static final String CHANNEL_ROCKERSWITCH_CHANNELA = "rockerswitchA";
    public static final String CHANNEL_ROCKERSWITCH_CHANNELB = "rockerswitchB";
    public static final String CHANNEL_ROCKERSWITCH_ACTION = "rockerSwitchAction";
    public static final ChannelTypeUID CHANNELTYPE_ROCKERSWITCH_ACTION_UID = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ROCKERSWITCH_ACTION);

    public static final String CHANNEL_VIRTUALSWITCHA = "virtualSwitchA";
    public static final String CHANNEL_VIRTUALROLLERSHUTTERA = "virtualRollershutterA";
    public static final String CHANNEL_VIRTUALROCKERSWITCHB = "virtualRockerswitchB";
    public static final String CHANNEL_ROCKERSWITCHLISTENERSWITCH = "rockerswitchListenerSwitch";
    public static final String CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER = "rockerswitchListenerRollershutter";
    public static final String CHANNEL_ROCKERSWITCHLISTENER_START = "rockerswitchListener";

    public static final String CHANNEL_WINDOWHANDLESTATE = "windowHandleState";
    public static final String CHANNEL_WINDOWSASHSTATE = "windowSashState";
    public static final String CHANNEL_WINDOWCALIBRATIONSTATE = "windowCalibrationState";
    public static final String CHANNEL_WINDOWCALIBRATIONSTEP = "windowCalibrationStep";
    public static final String CHANNEL_WINDOWBREACHEVENT = "windowBreachEvent";
    public static final String CHANNEL_PROTECTIONPLUSEVENT = "protectionPlusEvent";
    public static final String CHANNEL_VACATIONMODETOGGLEEVENT = "vacationModeToggleEvent";
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

    public static final String CHANNEL_PILOT_WIRE = "pilotWire";
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
    public static final String VIRTUALCHANNEL_SEND_COMMAND = "sendCommand";

    public static final String CHANNEL_VENTILATIONOPERATIONMODE = "ventilationOperationMode";
    public static final String CHANNEL_FIREPLACESAFETYMODE = "fireplaceSafetyMode";
    public static final String CHANNEL_HEATEXCHANGERBYPASSSTATUS = "heatExchangerBypassStatus";
    public static final String CHANNEL_SUPPLYAIRFLAPSTATUS = "supplyAirFlapStatus";
    public static final String CHANNEL_EXHAUSTAIRFLAPSTATUS = "exhaustAirFlapStatus";
    public static final String CHANNEL_DEFROSTMODE = "defrostMode";
    public static final String CHANNEL_COOLINGPROTECTIONMODE = "coolingProtectionMode";
    public static final String CHANNEL_OUTDOORAIRHEATERSTATUS = "outdoorAirHeaterStatus";
    public static final String CHANNEL_SUPPLYAIRHEATERSTATUS = "supplyAirHeaterStatus";
    public static final String CHANNEL_DRAINHEATERSTATUS = "drainHeaterStatus";
    public static final String CHANNEL_TIMEROPERATIONMODE = "timerOperationMode";
    public static final String CHANNEL_WEEKLYTIMERPROGRAMSTATUS = "weeklyTimerProgramStatus";
    public static final String CHANNEL_ROOMTEMPERATURECONTROLSTATUS = "roomTemperatureControlStatus";
    public static final String CHANNEL_AIRQUALITYVALUE1 = "airQualityValue1";
    public static final String CHANNEL_AIRQUALITYVALUE2 = "airQualityValue2";
    public static final String CHANNEL_OUTDOORAIRTEMPERATURE = "outdoorAirTemperature";
    public static final String CHANNEL_SUPPLYAIRTEMPERATURE = "supplyAirTemperature";
    public static final String CHANNEL_INDOORAIRTEMPERATURE = "indoorAirTemperature";
    public static final String CHANNEL_EXHAUSTAIRTEMPERATURE = "exhaustAirTemperature";
    public static final String CHANNEL_SUPPLYAIRFANAIRFLOWRATE = "supplyAirFanAirFlowRate";
    public static final String CHANNEL_EXHAUSTAIRFANAIRFLOWRATE = "exhaustAirFanAirFlowRate";
    public static final String CHANNEL_SUPPLYFANSPEED = "supplyFanSpeed";
    public static final String CHANNEL_EXHAUSTFANSPEED = "exhaustFanSpeed";
    public static final String CHANNEL_DAYNIGHTMODESTATE = "dayNightModeState";

    public static final Map<String, EnOceanChannelDescription> CHANNELID2CHANNELDESCRIPTION = Map.ofEntries(
            Map.entry(CHANNEL_GENERAL_SWITCHING,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERAL_SWITCHING),
                            CoreItemFactory.SWITCH)),

            Map.entry(CHANNEL_GENERAL_SWITCHINGA,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERAL_SWITCHINGA),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_GENERAL_SWITCHINGB,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERAL_SWITCHINGB),
                            CoreItemFactory.SWITCH)),

            Map.entry(CHANNEL_DIMMER,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_DIMMER),
                            CoreItemFactory.DIMMER)),
            Map.entry(CHANNEL_ROLLERSHUTTER,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ROLLERSHUTTER),
                            CoreItemFactory.ROLLERSHUTTER)),
            Map.entry(CHANNEL_ANGLE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ANGLE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Angle.class.getSimpleName())),
            Map.entry(CHANNEL_TEMPERATURE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_TEMPERATURE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName())),
            Map.entry(CHANNEL_HUMIDITY,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_HUMIDITY),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_CO2,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CO2), CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_CO,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CO), CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_VOC,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VOC), CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_VOC_ID,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VOC_ID),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_TVOC,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_TVOC),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_FANSPEEDSTAGE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_FANSPEEDSTAGE),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_OCCUPANCY,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_OCCUPANCY),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_MOTIONDETECTION,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_MOTION.getUID(),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_VIBRATION,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VIBRATION),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_ILLUMINATION,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATION),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Illuminance.class.getSimpleName())),
            Map.entry(CHANNEL_ILLUMINATIONWEST,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATIONWEST),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Illuminance.class.getSimpleName())),
            Map.entry(CHANNEL_ILLUMINATIONSOUTHNORTH,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATIONSOUTHNORTH),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Illuminance.class.getSimpleName())),
            Map.entry(CHANNEL_ILLUMINATIONEAST,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ILLUMINATIONEAST),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Illuminance.class.getSimpleName())),
            Map.entry(CHANNEL_WINDSPEED,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_WIND_SPEED.getUID(),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Speed.class.getSimpleName())),
            Map.entry(CHANNEL_RAINSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_RAINSTATUS),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_COUNTER,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_COUNTER),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_CURRENTNUMBER,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CURRENTNUMBER),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_SMOKEDETECTION,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SMOKEDETECTION),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_SENSORFAULT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SENSORFAULT),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_MAINTENANCESTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SENSORFAULT),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_SENSORANALYSISHUMIDITYRANGE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SENSORFAULT),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_SENSORANALYSISTEMPERATURRANGE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SENSORFAULT),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_TIMESINCELASTMAINTENANCE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_TIMESINCELASTMAINTENANCE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Time.class.getSimpleName())),
            Map.entry(CHANNEL_REMAININGPLT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_REMAININGPLT),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Time.class.getSimpleName())),
            Map.entry(CHANNEL_HYGROCOMFORTINDEX,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_HYGROCOMFORTINDEX),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_INDOORAIRANALYSIS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_INDOORAIRANALYSIS),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_SETPOINT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SETPOINT),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_CONTACT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CONTACT),
                            CoreItemFactory.CONTACT)),
            Map.entry(CHANNEL_WINDOWHANDLESTATE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_WINDOWHANDLESTATE),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_WINDOWSASHSTATE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_WINDOWSASHSTATE),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_WINDOWCALIBRATIONSTATE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_WINDOWCALIBRATIONSTATE),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_WINDOWCALIBRATIONSTEP,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_WINDOWCALIBRATIONSTEP),
                            CoreItemFactory.STRING)),

            Map.entry(CHANNEL_WINDOWBREACHEVENT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_WINDOWBREACHEVENT), null, null,
                            false, true)),
            Map.entry(CHANNEL_PROTECTIONPLUSEVENT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_PROTECTIONPLUSEVENT), null,
                            null, false, true)),
            Map.entry(CHANNEL_VACATIONMODETOGGLEEVENT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VACATIONMODETOGGLEEVENT), null,
                            null, false, true)),
            Map.entry(
                    CHANNEL_BATTERY_VOLTAGE,
                    new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_BATTERY_VOLTAGE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + ElectricPotential.class.getSimpleName())),
            Map.entry(CHANNEL_ENERGY_STORAGE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ENERGY_STORAGE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + ElectricPotential.class.getSimpleName())),
            Map.entry(CHANNEL_BATTERY_LEVEL, new EnOceanChannelDescription(
                    DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_BATTERY_LEVEL.getUID(), CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_BATTERYLOW,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_LOW_BATTERY.getUID(),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_TEACHINCMD,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_TEACHINCMD),
                            CoreItemFactory.SWITCH)),

            Map.entry(CHANNEL_PUSHBUTTON,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID(), null,
                            "Push button", false, true)),
            Map.entry(CHANNEL_PUSHBUTTON2,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID(), null,
                            "Push button 2", false, true)),
            Map.entry(CHANNEL_DOUBLEPRESS,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID(), null,
                            "Double press", false, true)),
            Map.entry(CHANNEL_LONGPRESS,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWBUTTON.getUID(), null,
                            "Long press", false, true)),

            Map.entry(CHANNEL_ROCKERSWITCH_CHANNELA,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER.getUID(), null,
                            "Rocker Switch - Channel A", false, false)),
            Map.entry(CHANNEL_ROCKERSWITCH_CHANNELB,
                    new EnOceanChannelDescription(DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER.getUID(), null,
                            "Rocker Switch - Channel B", false, false)),
            Map.entry(CHANNEL_ROCKERSWITCH_ACTION,
                    new EnOceanChannelDescription(CHANNELTYPE_ROCKERSWITCH_ACTION_UID, null, "Rocker Switch Action",
                            false, false)),

            Map.entry(CHANNEL_VIRTUALSWITCHA,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VIRTUALSWITCHA),
                            CoreItemFactory.SWITCH, "", true, false)),
            Map.entry(CHANNEL_VIRTUALROLLERSHUTTERA,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VIRTUALROLLERSHUTTERA),
                            CoreItemFactory.ROLLERSHUTTER, "", true, false)),
            Map.entry(CHANNEL_VIRTUALROCKERSWITCHB,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VIRTUALROCKERSWITCHB),
                            CoreItemFactory.STRING, "Rocker Switch - Channel B", true, false)),
            Map.entry(CHANNEL_ROCKERSWITCHLISTENERSWITCH,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ROCKERSWITCHLISTENERSWITCH),
                            CoreItemFactory.SWITCH, "Rocker Switch Listener (Switch)", true, false)),
            Map.entry(CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER,
                    new EnOceanChannelDescription(
                            new ChannelTypeUID(BINDING_ID, CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER),
                            CoreItemFactory.ROLLERSHUTTER, "Rocker Switch Listener (Rollershutter)", true, false)),

            Map.entry(CHANNEL_INSTANTPOWER,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_INSTANTPOWER),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Power.class.getSimpleName())),
            Map.entry(CHANNEL_TOTALUSAGE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_TOTALUSAGE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Energy.class.getSimpleName())),
            Map.entry(CHANNEL_CURRENTFLOW,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CURRENTFLOW),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + VolumetricFlowRate.class.getSimpleName())),
            Map.entry(CHANNEL_CUMULATIVEVALUE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_CUMULATIVEVALUE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Volume.class.getSimpleName())),
            Map.entry(CHANNEL_PILOT_WIRE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_PILOT_WIRE),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_AUTOOFF,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_AUTOOFF),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_DELAYRADIOOFF,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_DELAYRADIOOFF),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_EXTERNALINTERFACEMODE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_EXTERNALINTERFACEMODE),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_TWOSTATESWITCH,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_TWOSTATESWITCH),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_ECOMODE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ECOMODE),
                            CoreItemFactory.SWITCH)),

            Map.entry(CHANNEL_RSSI,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_RSSI),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_REPEATCOUNT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_REPEATCOUNT),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_LASTRECEIVED,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_LASTRECEIVED),
                            CoreItemFactory.DATETIME)),

            Map.entry(CHANNEL_GENERIC_SWITCH,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_SWITCH),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_GENERIC_ROLLERSHUTTER,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_ROLLERSHUTTER),
                            CoreItemFactory.ROLLERSHUTTER)),
            Map.entry(CHANNEL_GENERIC_DIMMER,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_DIMMER),
                            CoreItemFactory.DIMMER)),
            Map.entry(CHANNEL_GENERIC_NUMBER,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_NUMBER),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_GENERIC_STRING,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_STRING),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_GENERIC_COLOR,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_COLOR),
                            CoreItemFactory.COLOR)),
            Map.entry(CHANNEL_GENERIC_TEACHINCMD,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_GENERIC_TEACHINCMD),
                            CoreItemFactory.SWITCH)),

            Map.entry(CHANNEL_VALVE_POSITION,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VALVE_POSITION),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_BUTTON_LOCK,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_BUTTON_LOCK),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_DISPLAY_ORIENTATION,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_DISPLAY_ORIENTATION),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_TEMPERATURE_SETPOINT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_TEMPERATURE_SETPOINT),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName())),
            Map.entry(CHANNEL_FEED_TEMPERATURE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_FEED_TEMPERATURE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName())),
            Map.entry(CHANNEL_MEASUREMENT_CONTROL,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_MEASUREMENT_CONTROL),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_FAILURE_CODE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_FAILURE_CODE),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_WAKEUPCYCLE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_WAKEUPCYCLE),
                            CoreItemFactory.NUMBER)),
            Map.entry(CHANNEL_SERVICECOMMAND,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SERVICECOMMAND),
                            CoreItemFactory.NUMBER)),

            Map.entry(CHANNEL_VENTILATIONOPERATIONMODE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_VENTILATIONOPERATIONMODE),
                            CoreItemFactory.STRING)),
            Map.entry(CHANNEL_FIREPLACESAFETYMODE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_FIREPLACESAFETYMODE),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_HEATEXCHANGERBYPASSSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_HEATEXCHANGERBYPASSSTATUS),
                            CoreItemFactory.CONTACT)),
            Map.entry(CHANNEL_SUPPLYAIRFLAPSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SUPPLYAIRFLAPSTATUS),
                            CoreItemFactory.CONTACT)),
            Map.entry(CHANNEL_EXHAUSTAIRFLAPSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_EXHAUSTAIRFLAPSTATUS),
                            CoreItemFactory.CONTACT)),
            Map.entry(CHANNEL_DEFROSTMODE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_DEFROSTMODE),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_COOLINGPROTECTIONMODE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_COOLINGPROTECTIONMODE),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_OUTDOORAIRHEATERSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_OUTDOORAIRHEATERSTATUS),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_SUPPLYAIRHEATERSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SUPPLYAIRHEATERSTATUS),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_DRAINHEATERSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_DRAINHEATERSTATUS),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_TIMEROPERATIONMODE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_TIMEROPERATIONMODE),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_WEEKLYTIMERPROGRAMSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_WEEKLYTIMERPROGRAMSTATUS),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_ROOMTEMPERATURECONTROLSTATUS,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_ROOMTEMPERATURECONTROLSTATUS),
                            CoreItemFactory.SWITCH)),
            Map.entry(CHANNEL_AIRQUALITYVALUE1,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_AIRQUALITYVALUE1),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + Dimensionless.class.getSimpleName())),
            Map.entry(CHANNEL_AIRQUALITYVALUE2,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_AIRQUALITYVALUE2),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + Dimensionless.class.getSimpleName())),
            Map.entry(CHANNEL_OUTDOORAIRTEMPERATURE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_OUTDOORAIRTEMPERATURE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName())),
            Map.entry(CHANNEL_SUPPLYAIRTEMPERATURE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SUPPLYAIRTEMPERATURE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName())),
            Map.entry(CHANNEL_INDOORAIRTEMPERATURE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_INDOORAIRTEMPERATURE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName())),
            Map.entry(CHANNEL_EXHAUSTAIRTEMPERATURE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_EXHAUSTAIRTEMPERATURE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR + Temperature.class.getSimpleName())),
            Map.entry(CHANNEL_SUPPLYAIRFANAIRFLOWRATE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SUPPLYAIRFANAIRFLOWRATE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + VolumetricFlowRate.class.getSimpleName())),
            Map.entry(CHANNEL_EXHAUSTAIRFANAIRFLOWRATE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_EXHAUSTAIRFANAIRFLOWRATE),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + VolumetricFlowRate.class.getSimpleName())),
            Map.entry(CHANNEL_SUPPLYFANSPEED,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_SUPPLYFANSPEED),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + Dimensionless.class.getSimpleName())),
            Map.entry(CHANNEL_EXHAUSTFANSPEED,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_EXHAUSTFANSPEED),
                            CoreItemFactory.NUMBER + ItemUtil.EXTENSION_SEPARATOR
                                    + Dimensionless.class.getSimpleName())),
            Map.entry(CHANNEL_DAYNIGHTMODESTATE,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_DAYNIGHTMODESTATE),
                            CoreItemFactory.NUMBER)),

            Map.entry(CHANNEL_STATUS_REQUEST_EVENT,
                    new EnOceanChannelDescription(new ChannelTypeUID(BINDING_ID, CHANNEL_STATUS_REQUEST_EVENT), null,
                            "", false, true)),

            Map.entry(CHANNEL_REPEATERMODE, new EnOceanChannelDescription(
                    new ChannelTypeUID(BINDING_ID, CHANNEL_REPEATERMODE), CoreItemFactory.STRING)));

    // List of all repeater mode states
    public static final String REPEATERMODE_OFF = "OFF";
    public static final String REPEATERMODE_LEVEL_1 = "LEVEL1";
    public static final String REPEATERMODE_LEVEL_2 = "LEVEL2";

    // Bridge config properties
    public static final String PATH = "path";
    public static final String PARAMETER_NEXT_SENDERID = "nextSenderId";

    // Bridge properties
    public static final String PROPERTY_BASE_ID = "Base ID";
    public static final String PROPERTY_REMAINING_WRITE_CYCLES_BASE_ID = "Remaining Base ID Write Cycles";
    public static final String PROPERTY_APP_VERSION = "APP Version";
    public static final String PROPERTY_API_VERSION = "API Version";
    public static final String PROPERTY_CHIP_ID = "Chip ID";
    public static final String PROPERTY_DESCRIPTION = "Description";

    // Thing properties
    public static final String PROPERTY_SENDINGENOCEAN_ID = "SendingEnoceanId";

    // Thing config parameter
    public static final String PARAMETER_SENDERIDOFFSET = "senderIdOffset";
    public static final String PARAMETER_SENDINGEEPID = "sendingEEPId";
    public static final String PARAMETER_RECEIVINGEEPID = "receivingEEPId";

    public static final String PARAMETER_BROADCASTMESSAGES = "broadcastMessages";
    public static final String PARAMETER_ENOCEANID = "enoceanId";

    // Channel config parameter
    public static final String PARAMETER_CHANNEL_TEACHINMSG = "teachInMSG";
    public static final String PARAMETER_CHANNEL_DURATION = "duration";
    public static final String PARAMETER_CHANNEL_SWITCHMODE = "switchMode";

    // Manufacturer Ids - used to recognize special EEPs during auto discovery
    public static final int ELTAKOID = 0x00d;
    public static final int NODONID = 0x046; // NodOn devices are designed by ID-RF hence use their ID
    public static final int PERMUNDOID = 0x033;

    public static final String EMPTYENOCEANID = "00000000";

    public static final byte ZERO = (byte) 0;
}
