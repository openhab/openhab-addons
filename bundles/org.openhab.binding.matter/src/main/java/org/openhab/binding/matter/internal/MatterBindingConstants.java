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
package org.openhab.binding.matter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link MatterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterBindingConstants {

    public static final String BINDING_ID = "matter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_NODE = new ThingTypeUID(BINDING_ID, "node");
    public static final ThingTypeUID THING_TYPE_ENDPOINT = new ThingTypeUID(BINDING_ID, "endpoint");

    public static final String CONFIG_DESCRIPTION_URI_THING_PREFIX = "thing";

    // List of Channel UIDs
    public static final String CHANNEL_LABEL_ONOFF_ONOFF = "On Off";
    public static final String CHANNEL_ID_ONOFF_ONOFF = "onoffcontrol-onoff";
    public static final ChannelTypeUID CHANNEL_ONOFF_ONOFF = new ChannelTypeUID("matter:onoffcontrol-onoff");

    public static final String CHANNEL_LABEL_LEVEL_LEVEL = "Level Control";
    public static final String CHANNEL_ID_LEVEL_LEVEL = "levelcontrol-level";
    public static final ChannelTypeUID CHANNEL_LEVEL_LEVEL = new ChannelTypeUID("matter:levelcontrol-level");

    public static final String CHANNEL_LABEL_COLOR_COLOR = "Color Control";
    public static final String CHANNEL_ID_COLOR_COLOR = "colorcontrol-color";
    public static final ChannelTypeUID CHANNEL_COLOR_COLOR = new ChannelTypeUID("matter:colorcontrol-color");

    public static final String CHANNEL_LABEL_COLOR_TEMPERATURE = "Color Temperature";
    public static final String CHANNEL_ID_COLOR_TEMPERATURE = "colorcontrol-temperature";
    public static final ChannelTypeUID CHANNEL_COLOR_TEMPERATURE = new ChannelTypeUID(
            "matter:colorcontrol-temperature");

    public static final String CHANNEL_LABEL_COLOR_TEMPERATURE_ABS = "Color Temperature";
    public static final String CHANNEL_ID_COLOR_TEMPERATURE_ABS = "colorcontrol-temperature-abs";
    public static final ChannelTypeUID CHANNEL_COLOR_TEMPERATURE_ABS = new ChannelTypeUID(
            "matter:colorcontrol-temperature-abs");

    public static final String CHANNEL_LABEL_POWER_BATTERYPERCENT = "Battery Percent Remaining";
    public static final String CHANNEL_ID_POWER_BATTERYPERCENT = "powersource-batpercentremaining";
    public static final ChannelTypeUID CHANNEL_POWER_BATTERYPERCENT = new ChannelTypeUID(
            "matter:powersource-batpercentremaining");

    public static final String CHANNEL_LABEL_POWER_CHARGELEVEL = "Battery Charge Level";
    public static final String CHANNEL_ID_POWER_CHARGELEVEL = "powersource-batchargelevel";
    public static final ChannelTypeUID CHANNEL_POWER_CHARGELEVEL = new ChannelTypeUID(
            "matter:powersource-batchargelevel");

    public static final String CHANNEL_LABEL_THERMOSTAT_LOCALTEMPERATURE = "Local Temperature";
    public static final String CHANNEL_ID_THERMOSTAT_LOCALTEMPERATURE = "thermostat-localtemperature";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_LOCALTEMPERATURE = new ChannelTypeUID(
            "matter:thermostat-localtemperature");

    public static final String CHANNEL_LABEL_THERMOSTAT_OUTDOORTEMPERATURE = "Outdoor Temperature";
    public static final String CHANNEL_ID_THERMOSTAT_OUTDOORTEMPERATURE = "thermostat-outdoortemperature";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_OUTDOORTEMPERATURE = new ChannelTypeUID(
            "matter:thermostat-outdoortemperature");

    public static final String CHANNEL_LABEL_THERMOSTAT_OCCUPIEDCOOLING = "Occupied Cooling Setpoint";
    public static final String CHANNEL_ID_THERMOSTAT_OCCUPIEDCOOLING = "thermostat-occupiedcooling";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_OCCUPIEDCOOLING = new ChannelTypeUID(
            "matter:thermostat-occupiedcooling");

    public static final String CHANNEL_LABEL_THERMOSTAT_OCCUPIEDHEATING = "Occupied Heating Setpoint";
    public static final String CHANNEL_ID_THERMOSTAT_OCCUPIEDHEATING = "thermostat-occupiedheating";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_OCCUPIEDHEATING = new ChannelTypeUID(
            "matter:thermostat-occupiedheating");

    public static final String CHANNEL_LABEL_THERMOSTAT_UNOCCUPIEDCOOLING = "Unoccupied Cooling Setpoint";
    public static final String CHANNEL_ID_THERMOSTAT_UNOCCUPIEDCOOLING = "thermostat-unoccupiedcooling";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_UNOCCUPIEDCOOLING = new ChannelTypeUID(
            "matter:thermostat-unoccupiedcooling");

    public static final String CHANNEL_LABEL_THERMOSTAT_UNOCCUPIEDHEATING = "Unoccupied Heating Setpoint";
    public static final String CHANNEL_ID_THERMOSTAT_UNOCCUPIEDHEATING = "thermostat-unoccupiedheating";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_UNOCCUPIEDHEATING = new ChannelTypeUID(
            "matter:thermostat-unoccupiedheating");

    public static final String CHANNEL_LABEL_THERMOSTAT_SYSTEMMODE = "System Mode";
    public static final String CHANNEL_ID_THERMOSTAT_SYSTEMMODE = "thermostat-systemmode";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_SYSTEMMODE = new ChannelTypeUID(
            "matter:thermostat-systemmode");

    public static final String CHANNEL_LABEL_THERMOSTAT_RUNNINGMODE = "Running Mode";
    public static final String CHANNEL_ID_THERMOSTAT_RUNNINGMODE = "thermostat-runningmode";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_RUNNINGMODE = new ChannelTypeUID(
            "matter:thermostat-runningmode");

    public static final String CHANNEL_LABEL_THERMOSTAT_HEATING_DEMAND = "Heating Demand";
    public static final String CHANNEL_ID_THERMOSTAT_HEATING_DEMAND = "thermostat-heatingdemand";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_HEATING_DEMAND = new ChannelTypeUID(
            "matter:thermostat-heatingdemand");

    public static final String CHANNEL_LABEL_THERMOSTAT_COOLING_DEMAND = "Cooling Demand";
    public static final String CHANNEL_ID_THERMOSTAT_COOLING_DEMAND = "thermostat-coolingdemand";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_COOLING_DEMAND = new ChannelTypeUID(
            "matter:thermostat-coolingdemand");

    public static final String CHANNEL_LABEL_DOORLOCK_STATE = "Door Lock State";
    public static final String CHANNEL_ID_DOORLOCK_STATE = "doorlock-lockstate";
    public static final ChannelTypeUID CHANNEL_DOORLOCK_STATE = new ChannelTypeUID("matter:doorlock-lockstate");

    public static final String CHANNEL_LABEL_WINDOWCOVERING_LIFT = "Window Covering Lift";
    public static final String CHANNEL_ID_WINDOWCOVERING_LIFT = "windowcovering-lift";
    public static final ChannelTypeUID CHANNEL_WINDOWCOVERING_LIFT = new ChannelTypeUID("matter:windowcovering-lift");

    public static final String CHANNEL_LABEL_FANCONTROL_PERCENT = "Fan Control Percent";
    public static final String CHANNEL_ID_FANCONTROL_PERCENT = "fancontrol-percent";
    public static final ChannelTypeUID CHANNEL_FANCONTROL_PERCENT = new ChannelTypeUID("matter:fancontrol-percent");

    public static final String CHANNEL_LABEL_FANCONTROL_MODE = "Fan Control Mode";
    public static final String CHANNEL_ID_FANCONTROL_MODE = "fancontrol-fanmode";
    public static final ChannelTypeUID CHANNEL_FANCONTROL_MODE = new ChannelTypeUID("matter:fancontrol-fanmode");

    public static final String CHANNEL_LABEL_TEMPERATUREMEASURMENT_MEASUREDVALUE = "Temperature";
    public static final String CHANNEL_ID_TEMPERATUREMEASURMENT_MEASUREDVALUE = "temperaturemeasurement-measuredvalue";
    public static final ChannelTypeUID CHANNEL_TEMPERATUREMEASURMENT_MEASUREDVALUE = new ChannelTypeUID(
            "matter:temperaturemeasurement-measuredvalue");

    public static final String CHANNEL_LABEL_HUMIDITYMEASURMENT_MEASUREDVALUE = "Humidity";
    public static final String CHANNEL_ID_HUMIDITYMEASURMENT_MEASUREDVALUE = "relativehumiditymeasurement-measuredvalue";
    public static final ChannelTypeUID CHANNEL_HUMIDITYMEASURMENT_MEASUREDVALUE = new ChannelTypeUID(
            "matter:relativehumiditymeasurement-measuredvalue");

    public static final String CHANNEL_LABEL_OCCUPANCYSENSING_OCCUPIED = "Occupied";
    public static final String CHANNEL_ID_OCCUPANCYSENSING_OCCUPIED = "occupancysensing-occupied";
    public static final ChannelTypeUID CHANNEL_OCCUPANCYSENSING_OCCUPIED = new ChannelTypeUID(
            "matter:occupancysensing-occupied");

    public static final String CHANNEL_LABEL_ILLUMINANCEMEASURMENT_MEASUREDVALUE = "Illuminance";
    public static final String CHANNEL_ID_ILLUMINANCEMEASURMENT_MEASUREDVALUE = "illuminancemeasurement-measuredvalue";
    public static final ChannelTypeUID CHANNEL_ILLUMINANCEMEASURMENT_MEASUREDVALUE = new ChannelTypeUID(
            "matter:illuminancemeasurement-measuredvalue");

    public static final String CHANNEL_ID_MODESELECT_MODE = "modeselect-mode";
    public static final ChannelTypeUID CHANNEL_MODESELECT_MODE = new ChannelTypeUID("matter:modeselect-mode");

    public static final String CHANNEL_LABEL_BOOLEANSTATE_STATEVALUE = "State Value";
    public static final String CHANNEL_ID_BOOLEANSTATE_STATEVALUE = "booleanstate-statevalue";
    public static final ChannelTypeUID CHANNEL_BOOLEANSTATE_STATEVALUE = new ChannelTypeUID(
            "matter:booleanstate-statevalue");

    public static final String CHANNEL_LABEL_WIFINETWORKDIAGNOSTICS_RSSI = "Signal Strength";
    public static final String CHANNEL_ID_WIFINETWORKDIAGNOSTICS_RSSI = "wifinetworkdiagnostics-rssi";
    public static final ChannelTypeUID CHANNEL_WIFINETWORKDIAGNOSTICS_RSSI = new ChannelTypeUID(
            "matter:wifinetworkdiagnostics-rssi");

    public static final String CHANNEL_LABEL_SWITCH_SWITCH = "Switch";
    public static final String CHANNEL_ID_SWITCH_SWITCH = "switch-switch";
    public static final ChannelTypeUID CHANNEL_SWITCH_SWITCH = new ChannelTypeUID("matter:switch-switch");

    public static final String CHANNEL_ID_SWITCH_SWITCHLATECHED = "switch-switchlatched";
    public static final ChannelTypeUID CHANNEL_SWITCH_SWITCHLATECHED = new ChannelTypeUID(
            "matter:switch-switchlatched");
    public static final String CHANNEL_LABEL_SWITCH_SWITCHLATECHED = "Switch Latched Trigger";

    public static final String CHANNEL_ID_SWITCH_INITIALPRESS = "switch-initialpress";
    public static final ChannelTypeUID CHANNEL_SWITCH_INITIALPRESS = new ChannelTypeUID("matter:switch-initialpress");
    public static final String CHANNEL_LABEL_SWITCH_INITIALPRESS = "Initial Press Trigger";

    public static final String CHANNEL_ID_SWITCH_LONGPRESS = "switch-longpress";
    public static final ChannelTypeUID CHANNEL_SWITCH_LONGPRESS = new ChannelTypeUID("matter:switch-longpress");
    public static final String CHANNEL_LABEL_SWITCH_LONGPRESS = "Long Press Trigger";

    public static final String CHANNEL_ID_SWITCH_SHORTRELEASE = "switch-shortrelease";
    public static final ChannelTypeUID CHANNEL_SWITCH_SHORTRELEASE = new ChannelTypeUID("matter:switch-shortrelease");
    public static final String CHANNEL_LABEL_SWITCH_SHORTRELEASE = "Short Release Trigger";

    public static final String CHANNEL_ID_SWITCH_LONGRELEASE = "switch-longrelease";
    public static final ChannelTypeUID CHANNEL_SWITCH_LONGRELEASE = new ChannelTypeUID("matter:switch-longrelease");
    public static final String CHANNEL_LABEL_SWITCH_LONGRELEASE = "Long Release Trigger";

    public static final String CHANNEL_ID_SWITCH_MULTIPRESSONGOING = "switch-multipressongoing";
    public static final ChannelTypeUID CHANNEL_SWITCH_MULTIPRESSONGOING = new ChannelTypeUID(
            "matter:switch-multipressongoing");
    public static final String CHANNEL_LABEL_SWITCH_MULTIPRESSONGOING = "Multi-press Ongoing Trigger";

    public static final String CHANNEL_ID_SWITCH_MULTIPRESSCOMPLETE = "switch-multipresscomplete";
    public static final ChannelTypeUID CHANNEL_SWITCH_MULTIPRESSCOMPLETE = new ChannelTypeUID(
            "matter:switch-multipresscomplete");
    public static final String CHANNEL_LABEL_SWITCH_MULTIPRESSCOMPLETE = "Multi-press Complete Trigger";

    // shared by energy imported and exported
    public static final ChannelTypeUID CHANNEL_ELECTRICALENERGYMEASUREMENT_ENERGYMEASUREMENT_ENERGY = new ChannelTypeUID(
            "matter:electricalenergymeasurement-energymeasurmement-energy");

    public static final String CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY = "Cumulative Energy Imported";
    public static final String CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY = "electricalenergymeasurement-cumulativeenergyimported-energy";

    public static final String CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYEXPORTED_ENERGY = "Cumulative Energy Exported";
    public static final String CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYEXPORTED_ENERGY = "electricalenergymeasurement-cumulativeenergyexported-energy";

    public static final String CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY = "Periodic Energy Imported";
    public static final String CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY = "electricalenergymeasurement-periodicenergyimported-energy";

    public static final String CHANNEL_LABEL_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYEXPORTED_ENERGY = "Periodic Energy Exported";
    public static final String CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYEXPORTED_ENERGY = "electricalenergymeasurement-periodicenergyexported-energy";

    public static final String CHANNEL_LABEL_ELECTRICALPOWERMEASUREMENT_VOLTAGE = "Voltage";
    public static final String CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_VOLTAGE = "electricalpowermeasurement-voltage";
    public static final ChannelTypeUID CHANNEL_ELECTRICALPOWERMEASUREMENT_VOLTAGE = new ChannelTypeUID(
            "matter:" + CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_VOLTAGE);

    public static final String CHANNEL_LABEL_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT = "Active Current";
    public static final String CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT = "electricalpowermeasurement-activecurrent";
    public static final ChannelTypeUID CHANNEL_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT = new ChannelTypeUID(
            "matter:" + CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT);

    public static final String CHANNEL_LABEL_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER = "Active Power";
    public static final String CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER = "electricalpowermeasurement-activepower";
    public static final ChannelTypeUID CHANNEL_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER = new ChannelTypeUID(
            "matter:" + CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER);

    public static final String ITEM_TYPE_COLOR = "Color";
    public static final String ITEM_TYPE_CONTACT = "Contact";
    public static final String ITEM_TYPE_DIMMER = "Dimmer";
    public static final String ITEM_TYPE_NUMBER = "Number";
    public static final String ITEM_TYPE_NUMBER_PRESSURE = "Number:Pressure";
    public static final String ITEM_TYPE_NUMBER_TEMPERATURE = "Number:Temperature";
    public static final String ITEM_TYPE_NUMBER_ILLUMINANCE = "Number:Illuminance";
    public static final String ITEM_TYPE_NUMBER_POWER = "Number:Power";
    public static final String ITEM_TYPE_NUMBER_ELECTRICCURRENT = "Number:ElectricCurrent";
    public static final String ITEM_TYPE_NUMBER_ELECTRICPOTENTIAL = "Number:ElectricPotential";
    public static final String ITEM_TYPE_NUMBER_ENERGY = "Number:Energy";
    public static final String ITEM_TYPE_NUMBER_DIMENSIONLESS = "Number:Dimensionless";

    public static final String ITEM_TYPE_ROLLERSHUTTER = "Rollershutter";
    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_STRING = "String";

    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME = "threadborderroutermgmt-borderroutername";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID = "threadborderroutermgmt-borderagentid";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_THREADVERSION = "threadborderroutermgmt-threadversion";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED = "threadborderroutermgmt-interfaceenabled";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP = "threadborderroutermgmt-activedatasettimestamp";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP = "threadborderroutermgmt-pendingdatasettimestamp";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET = "threadborderroutermgmt-activedataset";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET = "threadborderroutermgmt-pendingdataset";

    public static final String CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME = "Border Router Name";
    public static final String CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID = "Border Agent ID";
    public static final String CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_THREADVERSION = "Thread Version";
    public static final String CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED = "Interface Enabled";
    public static final String CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP = "Active Dataset Timestamp";
    public static final String CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP = "Pending Dataset Timestamp";
    public static final String CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET = "Active Dataset";
    public static final String CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET = "Pending Dataset";

    public static final ChannelTypeUID CHANNEL_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME = new ChannelTypeUID(
            BINDING_ID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME);
    public static final ChannelTypeUID CHANNEL_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID = new ChannelTypeUID(
            BINDING_ID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID);
    public static final ChannelTypeUID CHANNEL_THREADBORDERROUTERMANAGEMENT_THREADVERSION = new ChannelTypeUID(
            BINDING_ID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_THREADVERSION);
    public static final ChannelTypeUID CHANNEL_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED = new ChannelTypeUID(
            BINDING_ID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED);
    public static final ChannelTypeUID CHANNEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP = new ChannelTypeUID(
            BINDING_ID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP);
    public static final ChannelTypeUID CHANNEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP = new ChannelTypeUID(
            BINDING_ID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP);
    public static final ChannelTypeUID CHANNEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET = new ChannelTypeUID(
            BINDING_ID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET);
    public static final ChannelTypeUID CHANNEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET = new ChannelTypeUID(
            BINDING_ID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET);

    // Thread Border Router Configuration Keys
    public static final String CONFIG_THREAD_CHANNEL = "channel";
    public static final String CONFIG_THREAD_ALLOWED_CHANNELS = "allowedChannels";
    public static final String CONFIG_THREAD_EXTENDED_PAN_ID = "extendedPanId";
    public static final String CONFIG_THREAD_MESH_LOCAL_PREFIX = "meshLocalPrefix";
    public static final String CONFIG_THREAD_NETWORK_NAME = "networkName";
    public static final String CONFIG_THREAD_NETWORK_KEY = "networkKey";
    public static final String CONFIG_THREAD_PAN_ID = "panId";
    public static final String CONFIG_THREAD_PSKC = "pskc";
    public static final String CONFIG_THREAD_ACTIVE_TIMESTAMP_SECONDS = "activeTimestampSeconds";
    public static final String CONFIG_THREAD_ACTIVE_TIMESTAMP_TICKS = "activeTimestampTicks";
    public static final String CONFIG_THREAD_ACTIVE_TIMESTAMP_AUTHORITATIVE = "activeTimestampAuthoritative";
    public static final String CONFIG_THREAD_DELAY_TIMER = "delayTimer";
    public static final String CONFIG_THREAD_ROTATION_TIME = "rotationTime";
    public static final String CONFIG_THREAD_OBTAIN_NETWORK_KEY = "obtainNetworkKey";
    public static final String CONFIG_THREAD_NATIVE_COMMISSIONING = "nativeCommissioning";
    public static final String CONFIG_THREAD_ROUTERS = "routers";
    public static final String CONFIG_THREAD_EXTERNAL_COMMISSIONING = "externalCommissioning";
    public static final String CONFIG_THREAD_COMMERCIAL_COMMISSIONING = "commercialCommissioning";
    public static final String CONFIG_THREAD_AUTONOMOUS_ENROLLMENT = "autonomousEnrollment";
    public static final String CONFIG_THREAD_NETWORK_KEY_PROVISIONING = "networkKeyProvisioning";
    public static final String CONFIG_THREAD_TOBLE_LINK = "tobleLink";
    public static final String CONFIG_THREAD_NON_CCM_ROUTERS = "nonCcmRouters";

    // Thread Border Router Configuration Labels
    public static final String CONFIG_LABEL_THREAD_BORDER_ROUTER_OPERATIONAL_DATASET = "Thread Border Router Operational Dataset";
    public static final String CONFIG_LABEL_THREAD_NETWORK_CHANNEL_NUMBER = "Thread network channel number";
    public static final String CONFIG_LABEL_THREAD_NETWORK_ALLOWED_CHANNELS = "Thread network allowed channels";
    public static final String CONFIG_LABEL_THREAD_EXTENDED_PAN_ID = "Thread extended PAN ID";
    public static final String CONFIG_LABEL_THREAD_MESH_LOCAL_PREFIX = "Thread mesh-local prefix";
    public static final String CONFIG_LABEL_THREAD_NETWORK_NAME = "Thread network name";
    public static final String CONFIG_LABEL_THREAD_NETWORK_KEY = "Thread network key";
    public static final String CONFIG_LABEL_THREAD_PAN_ID = "Thread PAN ID";
    public static final String CONFIG_LABEL_THREAD_PSKC = "Thread PSKC";
    public static final String CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_SECONDS = "Thread active timestamp seconds";
    public static final String CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_TICKS = "Thread active timestamp ticks";
    public static final String CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_IS_AUTHORITATIVE = "Thread active timestamp is authoritative";
    public static final String CONFIG_LABEL_THREAD_DATASET_SECURITY_POLICY = "Thread Dataset Security Policy";
    public static final String CONFIG_LABEL_SECURITY_POLICY_ROTATION_TIME = "Security Policy Rotation Time";
    public static final String CONFIG_LABEL_SECURITY_POLICY_OBTAIN_NETWORK_KEY = "Security Policy: Obtain Network Key";
    public static final String CONFIG_LABEL_SECURITY_POLICY_NATIVE_COMMISSIONING = "Security Policy: Native Commissioning";
    public static final String CONFIG_LABEL_SECURITY_POLICY_ROUTERS = "Security Policy: Routers";
    public static final String CONFIG_LABEL_SECURITY_POLICY_EXTERNAL_COMMISSIONING = "Security Policy: External Commissioning";
    public static final String CONFIG_LABEL_SECURITY_POLICY_COMMERCIAL_COMMISSIONING = "Security Policy: Commercial Commissioning";
    public static final String CONFIG_LABEL_SECURITY_POLICY_AUTONOMOUS_ENROLLMENT = "Security Policy: Autonomous Enrollment";
    public static final String CONFIG_LABEL_SECURITY_POLICY_NETWORK_KEY_PROVISIONING = "Security Policy: Network Key Provisioning";
    public static final String CONFIG_LABEL_SECURITY_POLICY_TO_BLE_LINK = "Security Policy: TO BLE Link";
    public static final String CONFIG_LABEL_SECURITY_POLICY_NON_CCM_ROUTERS = "Security Policy: Non-CCM Routers";

    // Thread Border Router Configuration Descriptions
    public static final String CONFIG_DESC_THREAD_BORDER_ROUTER_OPERATIONAL_DATASET = "Thread border router operational dataset values. Use actions to push changes to the device after saving.";
    public static final String CONFIG_DESC_THREAD_NETWORK_CHANNEL_NUMBER = "The thread network channel number to set (11-26)";
    public static final String CONFIG_DESC_THREAD_NETWORK_ALLOWED_CHANNELS = "A comma separated list of channel numbers allowed on the Thread network (ex: 11,12,13).  Recommended to include all channels (11-26)";
    public static final String CONFIG_DESC_THREAD_EXTENDED_PAN_ID = "The thread extended PAN ID to set (ex: 11111111222222AB)";
    public static final String CONFIG_DESC_THREAD_MESH_LOCAL_PREFIX = "The thread mesh-local prefix to set (ex: fd11:22::/64)";
    public static final String CONFIG_DESC_THREAD_NETWORK_NAME = "The thread network name to set (ex: openHAB-Thread)";
    public static final String CONFIG_DESC_THREAD_NETWORK_KEY = "The thread network key to set, leave blank to use a random secure key (ex: 00112233445566778899aabbccddeeff)";
    public static final String CONFIG_DESC_THREAD_PAN_ID = "The thread PAN ID to set (ex: 1234)";
    public static final String CONFIG_DESC_THREAD_PSKC = "The thread PSKC to set (hex)";
    public static final String CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_SECONDS = "The thread encoded active timestamp seconds";
    public static final String CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_TICKS = "The thread encoded active timestamp ticks";
    public static final String CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_IS_AUTHORITATIVE = "The thread encoded active timestamp is authoritative (GPS, NTP, Cellular, etc.)";
    public static final String CONFIG_DESC_THREAD_DATASET_SECURITY_POLICY = "Thread dataset security policy values. Default values recommended for most use cases.";
    public static final String CONFIG_DESC_SECURITY_POLICY_ROTATION_TIME = "Security Policy Rotation Time (hours). Defaults to 672.";
    public static final String CONFIG_DESC_SECURITY_POLICY_OBTAIN_NETWORK_KEY = "Security Policy: Obtain Network Key. Defaults to true.";
    public static final String CONFIG_DESC_SECURITY_POLICY_NATIVE_COMMISSIONING = "Security Policy: Native Commissioning. Defaults to true.";
    public static final String CONFIG_DESC_SECURITY_POLICY_ROUTERS = "Security Policy: Routers Enabled. Defaults to true.";
    public static final String CONFIG_DESC_SECURITY_POLICY_EXTERNAL_COMMISSIONING = "Security Policy: External Commissioning. Defaults to true.";
    public static final String CONFIG_DESC_SECURITY_POLICY_COMMERCIAL_COMMISSIONING = "Security Policy: Commercial Commissioning. Defaults to false.";
    public static final String CONFIG_DESC_SECURITY_POLICY_AUTONOMOUS_ENROLLMENT = "Security Policy: Autonomous Enrollment. Defaults to true.";
    public static final String CONFIG_DESC_SECURITY_POLICY_NETWORK_KEY_PROVISIONING = "Security Policy: Network Key Provisioning. Defaults to true.";
    public static final String CONFIG_DESC_SECURITY_POLICY_TO_BLE_LINK = "Security Policy: TO BLE Link. Defaults to true.";
    public static final String CONFIG_DESC_SECURITY_POLICY_NON_CCM_ROUTERS = "Security Policy: Non-CCM Routers. Defaults to false.";

    // Thread Border Router Configuration Group Names
    public static final String CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE = "threadBorderRouterCore";
    public static final String CONFIG_GROUP_SECURITY_POLICY = "securityPolicy";
}
