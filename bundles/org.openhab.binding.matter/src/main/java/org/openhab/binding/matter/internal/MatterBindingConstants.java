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
    public static final String CHANNEL_ID_AIRQUALITY_AIRQUALITY = "airquality-airquality";
    public static final ChannelTypeUID CHANNEL_AIRQUALITY_AIRQUALITY = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_AIRQUALITY_AIRQUALITY);
    public static final String CHANNEL_ID_ONOFF_ONOFF = "onoffcontrol-onoff";
    public static final ChannelTypeUID CHANNEL_ONOFF_ONOFF = new ChannelTypeUID(BINDING_ID, CHANNEL_ID_ONOFF_ONOFF);
    public static final String CHANNEL_ID_LEVEL_LEVEL = "levelcontrol-level";
    public static final ChannelTypeUID CHANNEL_LEVEL_LEVEL = new ChannelTypeUID(BINDING_ID, CHANNEL_ID_LEVEL_LEVEL);
    public static final String CHANNEL_ID_COLOR_COLOR = "colorcontrol-color";
    public static final ChannelTypeUID CHANNEL_COLOR_COLOR = new ChannelTypeUID(BINDING_ID, CHANNEL_ID_COLOR_COLOR);
    public static final String CHANNEL_ID_COLOR_TEMPERATURE = "colorcontrol-temperature";
    public static final ChannelTypeUID CHANNEL_COLOR_TEMPERATURE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_COLOR_TEMPERATURE);
    public static final String CHANNEL_ID_COLOR_TEMPERATURE_ABS = "colorcontrol-temperature-abs";
    public static final ChannelTypeUID CHANNEL_COLOR_TEMPERATURE_ABS = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_COLOR_TEMPERATURE_ABS);
    public static final String CHANNEL_ID_POWER_BATTERYPERCENT = "powersource-batpercentremaining";
    public static final ChannelTypeUID CHANNEL_POWER_BATTERYPERCENT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_POWER_BATTERYPERCENT);
    public static final String CHANNEL_ID_POWER_CHARGELEVEL = "powersource-batchargelevel";
    public static final ChannelTypeUID CHANNEL_POWER_CHARGELEVEL = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_POWER_CHARGELEVEL);
    public static final String CHANNEL_ID_THERMOSTAT_LOCALTEMPERATURE = "thermostat-localtemperature";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_LOCALTEMPERATURE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_LOCALTEMPERATURE);
    public static final String CHANNEL_ID_THERMOSTAT_OUTDOORTEMPERATURE = "thermostat-outdoortemperature";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_OUTDOORTEMPERATURE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_OUTDOORTEMPERATURE);
    public static final String CHANNEL_ID_THERMOSTAT_OCCUPIEDCOOLING = "thermostat-occupiedcooling";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_OCCUPIEDCOOLING = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_OCCUPIEDCOOLING);
    public static final String CHANNEL_ID_THERMOSTAT_OCCUPIEDHEATING = "thermostat-occupiedheating";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_OCCUPIEDHEATING = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_OCCUPIEDHEATING);
    public static final String CHANNEL_ID_THERMOSTAT_UNOCCUPIEDCOOLING = "thermostat-unoccupiedcooling";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_UNOCCUPIEDCOOLING = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_UNOCCUPIEDCOOLING);
    public static final String CHANNEL_ID_THERMOSTAT_UNOCCUPIEDHEATING = "thermostat-unoccupiedheating";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_UNOCCUPIEDHEATING = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_UNOCCUPIEDHEATING);
    public static final String CHANNEL_ID_THERMOSTAT_SYSTEMMODE = "thermostat-systemmode";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_SYSTEMMODE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_SYSTEMMODE);
    public static final String CHANNEL_ID_THERMOSTAT_RUNNINGMODE = "thermostat-runningmode";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_RUNNINGMODE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_RUNNINGMODE);
    public static final String CHANNEL_ID_THERMOSTAT_HEATING_DEMAND = "thermostat-heatingdemand";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_HEATING_DEMAND = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_HEATING_DEMAND);
    public static final String CHANNEL_ID_THERMOSTAT_COOLING_DEMAND = "thermostat-coolingdemand";
    public static final ChannelTypeUID CHANNEL_THERMOSTAT_COOLING_DEMAND = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_THERMOSTAT_COOLING_DEMAND);
    public static final String CHANNEL_ID_DOORLOCK_STATE = "doorlock-lockstate";
    public static final ChannelTypeUID CHANNEL_DOORLOCK_STATE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_DOORLOCK_STATE);
    public static final String CHANNEL_ID_WINDOWCOVERING_LIFT = "windowcovering-lift";
    public static final ChannelTypeUID CHANNEL_WINDOWCOVERING_LIFT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_WINDOWCOVERING_LIFT);
    public static final String CHANNEL_ID_FANCONTROL_PERCENT = "fancontrol-percent";
    public static final ChannelTypeUID CHANNEL_FANCONTROL_PERCENT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_FANCONTROL_PERCENT);
    public static final String CHANNEL_ID_FANCONTROL_MODE = "fancontrol-fanmode";
    public static final ChannelTypeUID CHANNEL_FANCONTROL_MODE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_FANCONTROL_MODE);
    public static final String CHANNEL_ID_TEMPERATUREMEASURMENT_MEASUREDVALUE = "temperaturemeasurement-measuredvalue";
    public static final ChannelTypeUID CHANNEL_TEMPERATUREMEASURMENT_MEASUREDVALUE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_TEMPERATUREMEASURMENT_MEASUREDVALUE);
    public static final String CHANNEL_ID_HUMIDITYMEASURMENT_MEASUREDVALUE = "relativehumiditymeasurement-measuredvalue";
    public static final ChannelTypeUID CHANNEL_HUMIDITYMEASURMENT_MEASUREDVALUE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_HUMIDITYMEASURMENT_MEASUREDVALUE);
    public static final String CHANNEL_ID_OCCUPANCYSENSING_OCCUPIED = "occupancysensing-occupied";
    public static final ChannelTypeUID CHANNEL_OCCUPANCYSENSING_OCCUPIED = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_OCCUPANCYSENSING_OCCUPIED);
    public static final String CHANNEL_ID_ILLUMINANCEMEASURMENT_MEASUREDVALUE = "illuminancemeasurement-measuredvalue";
    public static final ChannelTypeUID CHANNEL_ILLUMINANCEMEASURMENT_MEASUREDVALUE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_ILLUMINANCEMEASURMENT_MEASUREDVALUE);
    public static final String CHANNEL_ID_MODESELECT_MODE = "modeselect-mode";
    public static final ChannelTypeUID CHANNEL_MODESELECT_MODE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_MODESELECT_MODE);
    public static final String CHANNEL_ID_BOOLEANSTATE_STATEVALUE = "booleanstate-statevalue";
    public static final ChannelTypeUID CHANNEL_BOOLEANSTATE_STATEVALUE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_BOOLEANSTATE_STATEVALUE);
    public static final String CHANNEL_ID_WIFINETWORKDIAGNOSTICS_RSSI = "wifinetworkdiagnostics-rssi";
    public static final ChannelTypeUID CHANNEL_WIFINETWORKDIAGNOSTICS_RSSI = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_WIFINETWORKDIAGNOSTICS_RSSI);
    public static final String CHANNEL_ID_SWITCH_SWITCH = "switch-switch";
    public static final ChannelTypeUID CHANNEL_SWITCH_SWITCH = new ChannelTypeUID(BINDING_ID, CHANNEL_ID_SWITCH_SWITCH);
    public static final String CHANNEL_ID_SWITCH_SWITCHLATECHED = "switch-switchlatched";
    public static final ChannelTypeUID CHANNEL_SWITCH_SWITCHLATECHED = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SWITCH_SWITCHLATECHED);
    public static final String CHANNEL_ID_SWITCH_INITIALPRESS = "switch-initialpress";
    public static final ChannelTypeUID CHANNEL_SWITCH_INITIALPRESS = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SWITCH_INITIALPRESS);
    public static final String CHANNEL_ID_SWITCH_LONGPRESS = "switch-longpress";
    public static final ChannelTypeUID CHANNEL_SWITCH_LONGPRESS = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SWITCH_LONGPRESS);
    public static final String CHANNEL_ID_SWITCH_SHORTRELEASE = "switch-shortrelease";
    public static final ChannelTypeUID CHANNEL_SWITCH_SHORTRELEASE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SWITCH_SHORTRELEASE);
    public static final String CHANNEL_ID_SWITCH_LONGRELEASE = "switch-longrelease";
    public static final ChannelTypeUID CHANNEL_SWITCH_LONGRELEASE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SWITCH_LONGRELEASE);
    public static final String CHANNEL_ID_SWITCH_MULTIPRESSONGOING = "switch-multipressongoing";
    public static final ChannelTypeUID CHANNEL_SWITCH_MULTIPRESSONGOING = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SWITCH_MULTIPRESSONGOING);
    public static final String CHANNEL_ID_SWITCH_MULTIPRESSCOMPLETE = "switch-multipresscomplete";
    public static final ChannelTypeUID CHANNEL_SWITCH_MULTIPRESSCOMPLETE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SWITCH_MULTIPRESSCOMPLETE);
    // shared by energy imported and exported
    public static final ChannelTypeUID CHANNEL_ELECTRICALENERGYMEASUREMENT_ENERGYMEASUREMENT_ENERGY = new ChannelTypeUID(
            BINDING_ID, "electricalenergymeasurement-energymeasurmement-energy");

    public static final String CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYIMPORTED_ENERGY = "electricalenergymeasurement-cumulativeenergyimported-energy";

    public static final String CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_CUMULATIVEENERGYEXPORTED_ENERGY = "electricalenergymeasurement-cumulativeenergyexported-energy";

    public static final String CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYIMPORTED_ENERGY = "electricalenergymeasurement-periodicenergyimported-energy";

    public static final String CHANNEL_ID_ELECTRICALENERGYMEASUREMENT_PERIODICENERGYEXPORTED_ENERGY = "electricalenergymeasurement-periodicenergyexported-energy";

    public static final String CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_VOLTAGE = "electricalpowermeasurement-voltage";
    public static final ChannelTypeUID CHANNEL_ELECTRICALPOWERMEASUREMENT_VOLTAGE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_VOLTAGE);

    public static final String CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT = "electricalpowermeasurement-activecurrent";
    public static final ChannelTypeUID CHANNEL_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVECURRENT);

    public static final String CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER = "electricalpowermeasurement-activepower";
    public static final ChannelTypeUID CHANNEL_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_ELECTRICALPOWERMEASUREMENT_ACTIVEPOWER);

    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME = "threadborderroutermgmt-borderroutername";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID = "threadborderroutermgmt-borderagentid";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_THREADVERSION = "threadborderroutermgmt-threadversion";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED = "threadborderroutermgmt-interfaceenabled";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP = "threadborderroutermgmt-activedatasettimestamp";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP = "threadborderroutermgmt-pendingdatasettimestamp";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET = "threadborderroutermgmt-activedataset";
    public static final String CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET = "threadborderroutermgmt-pendingdataset";

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

    // Robotic Vacuum Cleaner channels
    public static final String CHANNEL_ID_RVCRUNMODE_MODE = "rvcrunmode-mode";
    public static final ChannelTypeUID CHANNEL_RVCRUNMODE_MODE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_RVCRUNMODE_MODE);
    public static final String CHANNEL_ID_RVCCLEANMODE_MODE = "rvccleanmode-mode";
    public static final ChannelTypeUID CHANNEL_RVCCLEANMODE_MODE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_RVCCLEANMODE_MODE);
    public static final String CHANNEL_ID_RVCOPERATIONALSTATE_STATE = "rvcoperationalstate-state";
    public static final ChannelTypeUID CHANNEL_RVCOPERATIONALSTATE_STATE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_RVCOPERATIONALSTATE_STATE);
    public static final String CHANNEL_ID_SERVICEAREA_CURRENTAREA = "servicearea-currentarea";
    public static final ChannelTypeUID CHANNEL_SERVICEAREA_CURRENTAREA = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_SERVICEAREA_CURRENTAREA);
    public static final String CHANNEL_ID_RVCOPERATIONALSTATE_GOHOME = "rvcoperationalstate-gohome";
    public static final ChannelTypeUID CHANNEL_RVCOPERATIONALSTATE_GOHOME = new ChannelTypeUID(BINDING_ID,
            CHANNEL_ID_RVCOPERATIONALSTATE_GOHOME);
    public static final String CHANNEL_ID_SERVICEAREA_SELECTEDAREA_PREFIX = "servicearea-selectedarea-";
    public static final ChannelTypeUID CHANNEL_SERVICEAREA_SELECTEDAREA = new ChannelTypeUID(BINDING_ID,
            "servicearea-selectedarea");

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
    public static final String CONFIG_LABEL_THREAD_BORDER_ROUTER_OPERATIONAL_DATASET = "@text/thing-type.config.matter.node.thread_border_router_operational_dataset.label";
    public static final String CONFIG_LABEL_THREAD_NETWORK_CHANNEL_NUMBER = "@text/thing-type.config.matter.node.thread_network_channel_number.label";
    public static final String CONFIG_LABEL_THREAD_NETWORK_ALLOWED_CHANNELS = "@text/thing-type.config.matter.node.thread_network_allowed_channels.label";
    public static final String CONFIG_LABEL_THREAD_EXTENDED_PAN_ID = "@text/thing-type.config.matter.node.thread_extended_pan_id.label";
    public static final String CONFIG_LABEL_THREAD_MESH_LOCAL_PREFIX = "@text/thing-type.config.matter.node.thread_mesh_local_prefix.label";
    public static final String CONFIG_LABEL_THREAD_NETWORK_NAME = "@text/thing-type.config.matter.node.thread_network_name.label";
    public static final String CONFIG_LABEL_THREAD_NETWORK_KEY = "@text/thing-type.config.matter.node.thread_network_key.label";
    public static final String CONFIG_LABEL_THREAD_PAN_ID = "@text/thing-type.config.matter.node.thread_pan_id.label";
    public static final String CONFIG_LABEL_THREAD_PSKC = "@text/thing-type.config.matter.node.thread_pskc.label";
    public static final String CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_SECONDS = "@text/thing-type.config.matter.node.thread_active_timestamp_seconds.label";
    public static final String CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_TICKS = "@text/thing-type.config.matter.node.thread_active_timestamp_ticks.label";
    public static final String CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_IS_AUTHORITATIVE = "@text/thing-type.config.matter.node.thread_active_timestamp_is_authoritative.label";
    public static final String CONFIG_LABEL_THREAD_DATASET_SECURITY_POLICY = "@text/thing-type.config.matter.node.thread_dataset_security_policy.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_ROTATION_TIME = "@text/thing-type.config.matter.node.security_policy_rotation_time.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_OBTAIN_NETWORK_KEY = "@text/thing-type.config.matter.node.security_policy_obtain_network_key.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_NATIVE_COMMISSIONING = "@text/thing-type.config.matter.node.security_policy_native_commissioning.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_ROUTERS = "@text/thing-type.config.matter.node.security_policy_routers.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_EXTERNAL_COMMISSIONING = "@text/thing-type.config.matter.node.security_policy_external_commissioning.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_COMMERCIAL_COMMISSIONING = "@text/thing-type.config.matter.node.security_policy_commercial_commissioning.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_AUTONOMOUS_ENROLLMENT = "@text/thing-type.config.matter.node.security_policy_autonomous_enrollment.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_NETWORK_KEY_PROVISIONING = "@text/thing-type.config.matter.node.security_policy_network_key_provisioning.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_TO_BLE_LINK = "@text/thing-type.config.matter.node.security_policy_to_ble_link.label";
    public static final String CONFIG_LABEL_SECURITY_POLICY_NON_CCM_ROUTERS = "@text/thing-type.config.matter.node.security_policy_non_ccm_routers.label";

    // Thread Border Router Configuration Descriptions
    public static final String CONFIG_DESC_THREAD_BORDER_ROUTER_OPERATIONAL_DATASET = "@text/thing-type.config.matter.node.thread_border_router_operational_dataset.description";
    public static final String CONFIG_DESC_THREAD_NETWORK_CHANNEL_NUMBER = "@text/thing-type.config.matter.node.thread_network_channel_number.description";
    public static final String CONFIG_DESC_THREAD_NETWORK_ALLOWED_CHANNELS = "@text/thing-type.config.matter.node.thread_network_allowed_channels.description";
    public static final String CONFIG_DESC_THREAD_EXTENDED_PAN_ID = "@text/thing-type.config.matter.node.thread_extended_pan_id.description";
    public static final String CONFIG_DESC_THREAD_MESH_LOCAL_PREFIX = "@text/thing-type.config.matter.node.thread_mesh_local_prefix.description";
    public static final String CONFIG_DESC_THREAD_NETWORK_NAME = "@text/thing-type.config.matter.node.thread_network_name.description";
    public static final String CONFIG_DESC_THREAD_NETWORK_KEY = "@text/thing-type.config.matter.node.thread_network_key.description";
    public static final String CONFIG_DESC_THREAD_PAN_ID = "@text/thing-type.config.matter.node.thread_pan_id.description";
    public static final String CONFIG_DESC_THREAD_PSKC = "@text/thing-type.config.matter.node.thread_pskc.description";
    public static final String CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_SECONDS = "@text/thing-type.config.matter.node.thread_active_timestamp_seconds.description";
    public static final String CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_TICKS = "@text/thing-type.config.matter.node.thread_active_timestamp_ticks.description";
    public static final String CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_IS_AUTHORITATIVE = "@text/thing-type.config.matter.node.thread_active_timestamp_is_authoritative.description";
    public static final String CONFIG_DESC_THREAD_DATASET_SECURITY_POLICY = "@text/thing-type.config.matter.node.thread_dataset_security_policy.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_ROTATION_TIME = "@text/thing-type.config.matter.node.security_policy_rotation_time.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_OBTAIN_NETWORK_KEY = "@text/thing-type.config.matter.node.security_policy_obtain_network_key.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_NATIVE_COMMISSIONING = "@text/thing-type.config.matter.node.security_policy_native_commissioning.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_ROUTERS = "@text/thing-type.config.matter.node.security_policy_routers.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_EXTERNAL_COMMISSIONING = "@text/thing-type.config.matter.node.security_policy_external_commissioning.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_COMMERCIAL_COMMISSIONING = "@text/thing-type.config.matter.node.security_policy_commercial_commissioning.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_AUTONOMOUS_ENROLLMENT = "@text/thing-type.config.matter.node.security_policy_autonomous_enrollment.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_NETWORK_KEY_PROVISIONING = "@text/thing-type.config.matter.node.security_policy_network_key_provisioning.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_TO_BLE_LINK = "@text/thing-type.config.matter.node.security_policy_to_ble_link.description";
    public static final String CONFIG_DESC_SECURITY_POLICY_NON_CCM_ROUTERS = "@text/thing-type.config.matter.node.security_policy_non_ccm_routers.description";
    // Thread Border Router Configuration Group Names
    public static final String CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE = "threadBorderRouterCore";
    public static final String CONFIG_GROUP_SECURITY_POLICY = "securityPolicy";

    // Matter Node Actions
    public static final String THING_ACTION_LABEL_NODE_GENERATE_NEW_PAIRING_CODE = "@text/thing-action.node.generateNewPairingCode.label";
    public static final String THING_ACTION_DESC_NODE_GENERATE_NEW_PAIRING_CODE = "@text/thing-action.node.generateNewPairingCode.description";
    public static final String THING_ACTION_LABEL_NODE_GENERATE_NEW_PAIRING_CODE_MANUAL_PAIRING_CODE = "@text/thing-action.node.generateNewPairingCode.manual-pairing-code.label";
    public static final String THING_ACTION_LABEL_NODE_GENERATE_NEW_PAIRING_CODE_QR_PAIRING_CODE = "@text/thing-action.node.generateNewPairingCode.qr-pairing-code.label";
    public static final String THING_ACTION_LABEL_NODE_DECOMMISSION = "@text/thing-action.node.decommission.label";
    public static final String THING_ACTION_DESC_NODE_DECOMMISSION = "@text/thing-action.node.decommission.description";
    public static final String THING_ACTION_LABEL_NODE_DECOMMISSION_RESULT = "@text/thing-action.node.decommission.result.label";
    public static final String THING_ACTION_LABEL_NODE_GET_FABRICS = "@text/thing-action.node.getFabrics.label";
    public static final String THING_ACTION_DESC_NODE_GET_FABRICS = "@text/thing-action.node.getFabrics.description";
    public static final String THING_ACTION_LABEL_NODE_GET_FABRICS_RESULT = "@text/thing-action.node.getFabrics.result.label";
    public static final String THING_ACTION_LABEL_NODE_REMOVE_FABRIC = "@text/thing-action.node.removeFabric.label";
    public static final String THING_ACTION_DESC_NODE_REMOVE_FABRIC = "@text/thing-action.node.removeFabric.description";
    public static final String THING_ACTION_LABEL_NODE_REMOVE_FABRIC_RESULT = "@text/thing-action.node.removeFabric.result.label";
    public static final String THING_ACTION_LABEL_NODE_REMOVE_FABRIC_INDEX = "@text/thing-action.node.removeFabric.index.label";
    public static final String THING_ACTION_DESC_NODE_REMOVE_FABRIC_INDEX = "@text/thing-action.node.removeFabric.index.description";

    // Action Result Messages
    public static final String THING_ACTION_RESULT_SUCCESS = "@text/thing-action.result.success";
    public static final String THING_ACTION_RESULT_NO_HANDLER = "@text/thing-action.result.no-handler";
    public static final String THING_ACTION_RESULT_NO_FABRICS = "@text/thing-action.result.no-fabrics";

    // Matter OTBR Actions
    public static final String THING_ACTION_LABEL_OTBR_LOAD_EXTERNAL_DATASET = "@text/thing-action.otbr.loadExternalDataset.label";
    public static final String THING_ACTION_DESC_OTBR_LOAD_EXTERNAL_DATASET = "@text/thing-action.otbr.loadExternalDataset.description";
    public static final String THING_ACTION_LABEL_OTBR_LOAD_EXTERNAL_DATASET_DATASET = "@text/thing-action.otbr.loadExternalDataset.dataset.label";
    public static final String THING_ACTION_DESC_OTBR_LOAD_EXTERNAL_DATASET_DATASET = "@text/thing-action.otbr.loadExternalDataset.dataset.description";
    public static final String THING_ACTION_LABEL_OTBR_LOAD_EXTERNAL_DATASET_RESULT = "@text/thing-action.otbr.loadExternalDataset.result.label";

    public static final String THING_ACTION_LABEL_OTBR_LOAD_ACTIVE_DATASET = "@text/thing-action.otbr.loadActiveDataset.label";
    public static final String THING_ACTION_DESC_OTBR_LOAD_ACTIVE_DATASET = "@text/thing-action.otbr.loadActiveDataset.description";
    public static final String THING_ACTION_LABEL_OTBR_LOAD_ACTIVE_DATASET_RESULT = "@text/thing-action.otbr.loadActiveDataset.result.label";
    public static final String THING_ACTION_LABEL_OTBR_LOAD_ACTIVE_DATASET_DATASET = "@text/thing-action.otbr.loadActiveDataset.dataset.label";

    public static final String THING_ACTION_LABEL_OTBR_PUSH_DATASET = "@text/thing-action.otbr.pushDataset.label";
    public static final String THING_ACTION_DESC_OTBR_PUSH_DATASET = "@text/thing-action.otbr.pushDataset.description";
    public static final String THING_ACTION_LABEL_OTBR_PUSH_DATASET_RESULT = "@text/thing-action.otbr.pushDataset.result.label";
    public static final String THING_ACTION_LABEL_OTBR_PUSH_DATASET_DELAY = "@text/thing-action.otbr.pushDataset.delay.label";
    public static final String THING_ACTION_DESC_OTBR_PUSH_DATASET_DELAY = "@text/thing-action.otbr.pushDataset.delay.description";
    public static final String THING_ACTION_LABEL_OTBR_PUSH_DATASET_GENERATE_TIME = "@text/thing-action.otbr.pushDataset.generateTime.label";
    public static final String THING_ACTION_DESC_OTBR_PUSH_DATASET_GENERATE_TIME = "@text/thing-action.otbr.pushDataset.generateTime.description";
    public static final String THING_ACTION_LABEL_OTBR_PUSH_DATASET_INCREMENT_TIME = "@text/thing-action.otbr.pushDataset.incrementTime.label";
    public static final String THING_ACTION_DESC_OTBR_PUSH_DATASET_INCREMENT_TIME = "@text/thing-action.otbr.pushDataset.incrementTime.description";

    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET = "@text/thing-action.otbr.generateDataset.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET = "@text/thing-action.otbr.generateDataset.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_RESULT = "@text/thing-action.otbr.generateDataset.result.label";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_JSON = "@text/thing-action.otbr.generateDataset.json.label";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_HEX = "@text/thing-action.otbr.generateDataset.hex.label";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_SAVE = "@text/thing-action.otbr.generateDataset.save.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_SAVE = "@text/thing-action.otbr.generateDataset.save.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_CHANNEL = "@text/thing-action.otbr.generateDataset.channel.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_CHANNEL = "@text/thing-action.otbr.generateDataset.channel.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_TIMESTAMP_SECONDS = "@text/thing-action.otbr.generateDataset.timestampSeconds.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_TIMESTAMP_SECONDS = "@text/thing-action.otbr.generateDataset.timestampSeconds.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_TIMESTAMP_TICKS = "@text/thing-action.otbr.generateDataset.timestampTicks.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_TIMESTAMP_TICKS = "@text/thing-action.otbr.generateDataset.timestampTicks.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_TIMESTAMP_AUTHORITATIVE = "@text/thing-action.otbr.generateDataset.timestampAuthoritative.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_TIMESTAMP_AUTHORITATIVE = "@text/thing-action.otbr.generateDataset.timestampAuthoritative.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_PAN_ID = "@text/thing-action.otbr.generateDataset.panId.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_PAN_ID = "@text/thing-action.otbr.generateDataset.panId.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_EXTENDED_PAN_ID = "@text/thing-action.otbr.generateDataset.extendedPanId.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_EXTENDED_PAN_ID = "@text/thing-action.otbr.generateDataset.extendedPanId.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_MESH_PREFIX = "@text/thing-action.otbr.generateDataset.meshPrefix.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_MESH_PREFIX = "@text/thing-action.otbr.generateDataset.meshPrefix.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NETWORK_NAME = "@text/thing-action.otbr.generateDataset.networkName.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_NETWORK_NAME = "@text/thing-action.otbr.generateDataset.networkName.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NETWORK_KEY = "@text/thing-action.otbr.generateDataset.networkKey.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_NETWORK_KEY = "@text/thing-action.otbr.generateDataset.networkKey.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_PASSPHRASE = "@text/thing-action.otbr.generateDataset.passphrase.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_PASSPHRASE = "@text/thing-action.otbr.generateDataset.passphrase.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_ROTATION_TIME = "@text/thing-action.otbr.generateDataset.rotationTime.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_ROTATION_TIME = "@text/thing-action.otbr.generateDataset.rotationTime.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_OBTAIN_NETWORK_KEY = "@text/thing-action.otbr.generateDataset.obtainNetworkKey.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_OBTAIN_NETWORK_KEY = "@text/thing-action.otbr.generateDataset.obtainNetworkKey.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NATIVE_COMMISSIONING = "@text/thing-action.otbr.generateDataset.nativeCommissioning.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_NATIVE_COMMISSIONING = "@text/thing-action.otbr.generateDataset.nativeCommissioning.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_ROUTERS = "@text/thing-action.otbr.generateDataset.routers.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_ROUTERS = "@text/thing-action.otbr.generateDataset.routers.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_EXTERNAL_COMMISSIONING = "@text/thing-action.otbr.generateDataset.externalCommissioning.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_EXTERNAL_COMMISSIONING = "@text/thing-action.otbr.generateDataset.externalCommissioning.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_COMMERCIAL_COMMISSIONING = "@text/thing-action.otbr.generateDataset.commercialCommissioning.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_COMMERCIAL_COMMISSIONING = "@text/thing-action.otbr.generateDataset.commercialCommissioning.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_AUTONOMOUS_ENROLLMENT = "@text/thing-action.otbr.generateDataset.autonomousEnrollment.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_AUTONOMOUS_ENROLLMENT = "@text/thing-action.otbr.generateDataset.autonomousEnrollment.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NETWORK_KEY_PROVISIONING = "@text/thing-action.otbr.generateDataset.networkKeyProvisioning.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_NETWORK_KEY_PROVISIONING = "@text/thing-action.otbr.generateDataset.networkKeyProvisioning.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_TOBLE_LINK = "@text/thing-action.otbr.generateDataset.tobleLink.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_TOBLE_LINK = "@text/thing-action.otbr.generateDataset.tobleLink.description";
    public static final String THING_ACTION_LABEL_OTBR_GENERATE_DATASET_NON_CCM_ROUTERS = "@text/thing-action.otbr.generateDataset.nonCcmRouters.label";
    public static final String THING_ACTION_DESC_OTBR_GENERATE_DATASET_NON_CCM_ROUTERS = "@text/thing-action.otbr.generateDataset.nonCcmRouters.description";

    // Matter OTBR Action Results
    public static final String THING_ACTION_RESULT_NO_CONVERTER = "@text/thing-action.result.no-converter";
    public static final String THING_ACTION_RESULT_INVALID_JSON = "@text/thing-action.result.invalid-json";
    public static final String THING_ACTION_RESULT_ERROR_GENERATING_KEY = "@text/thing-action.result.error-generating-key";
    public static final String THING_ACTION_RESULT_ERROR_SETTING_DATASET = "@text/thing-action.result.error-setting-dataset";

    // Matter Controller Actions
    public static final String THING_ACTION_LABEL_CONTROLLER_PAIR_DEVICE = "@text/thing-action.controller.pairDevice.label";
    public static final String THING_ACTION_DESC_CONTROLLER_PAIR_DEVICE = "@text/thing-action.controller.pairDevice.description";
    public static final String THING_ACTION_LABEL_CONTROLLER_PAIR_DEVICE_CODE = "@text/thing-action.controller.pairDevice.code.label";
    public static final String THING_ACTION_DESC_CONTROLLER_PAIR_DEVICE_CODE = "@text/thing-action.controller.pairDevice.code.description";
    public static final String THING_ACTION_LABEL_CONTROLLER_PAIR_DEVICE_RESULT = "@text/thing-action.controller.pairDevice.result.label";
    public static final String THING_ACTION_RESULT_DEVICE_ADDED = "@text/thing-action.result.device-added";
    public static final String THING_ACTION_RESULT_PAIRING_FAILED = "@text/thing-action.result.pairing-failed";
    public static final String THING_ACTION_LABEL_CONTROLLER_GET_DEBUG_NODE_DATA = "@text/thing-action.controller.getDebugNodeData.label";
    public static final String THING_ACTION_DESC_CONTROLLER_GET_DEBUG_NODE_DATA = "@text/thing-action.controller.getDebugNodeData.description";
    public static final String THING_ACTION_LABEL_CONTROLLER_GET_DEBUG_NODE_DATA_RESULT = "@text/thing-action.controller.getDebugNodeData.result.label";
    public static final String THING_ACTION_LABEL_CONTROLLER_GET_DEBUG_NODE_DATA_FAILED = "@text/thing-action.controller.getDebugNodeData.failed.label";

    // Matter Controller Statuses
    public static final String THING_STATUS_DETAIL_CONTROLLER_WAITING_FOR_DATA = "@text/thing-status.detail.controller.waitingForData";
    public static final String THING_STATUS_DETAIL_ENDPOINT_THING_NOT_REACHABLE = "@text/thing-status.detail.endpoint.thingNotReachable";

    // Discovery
    public static final String DISCOVERY_MATTER_BRIDGE_ENDPOINT_LABEL = "@text/discovery.matter.bridge-endpoint.label";
    public static final String DISCOVERY_MATTER_NODE_DEVICE_LABEL = "@text/discovery.matter.node-device.label";
    public static final String DISCOVERY_MATTER_UNKNOWN_NODE_LABEL = "@text/discovery.matter.unknown-node.label";
    public static final String DISCOVERY_MATTER_SCAN_INPUT_LABEL = "@text/discovery.matter.scan-input.label";
    public static final String DISCOVERY_MATTER_SCAN_INPUT_DESCRIPTION = "@text/discovery.matter.scan-input.description";
}
