/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;

/**
 * The {@link E3DCBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Björn Brings - Initial contribution
 * @author Marco Loose - Extensions
 */
@NonNullByDefault
public class E3DCBindingConstants {

    private static final String BINDING_ID = "e3dc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_S10 = new ThingTypeUID(BINDING_ID, "s10");

    // List of all properties
    public static final String PROPERTY_SERIAL = "serialNumber";
    public static final String PROPERTY_PROD = "productionDate";
    public static final String PROPERTY_VERSION = "softwareVersion";
    public static final String PROPERTY_IP = "ipAddress";
    public static final String PROPERTY_MAC = "macAddress";
    public static final String PROPERTY_SUBNET = "subnetMask";
    public static final String PROPERTY_GW = "gatwayAddress";
    public static final String PROPERTY_DNS = "dnsAddress";
    public static final String PROPERTY_DHCP = "dhcpUsed";
    public static final String PROPERTY_BUILD = "bindingBuild";

    public static final String PROPERTY_PM_CONNECTED_DEVICES = "connectedPowermeters";
    public static final String PROPERTY_WB_CONNECTED_DEVICES = "connectedWallboxes";
    public static final String PROPERTY_FMS_CONNECTED_DEVICES = "connectedFarmingDevices";
    public static final String PROPERTY_FMS_REV_CONNECTED_DEVICES = "connectedFarmingDevicesRev";
    public static final String PROPERTY_PVI_USED_STRING_COUNT = "connectedStrings";
    public static final String PROPERTY_QPI_INVERTER_COUNT = "connectedInverters";
    public static final String PROPERTY_SE_SE_COUNT = "connectedStorageExtensions";

    // List of channel groups types
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_EMS = new ChannelGroupTypeUID(BINDING_ID, "ems");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_PVI = new ChannelGroupTypeUID(BINDING_ID, "pvi");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_BAT = new ChannelGroupTypeUID(BINDING_ID, "battery");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_DCDC = new ChannelGroupTypeUID(BINDING_ID, "dcdc");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_PM = new ChannelGroupTypeUID(BINDING_ID, "powermeter");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_DB = new ChannelGroupTypeUID(BINDING_ID, "database");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_SRV = new ChannelGroupTypeUID(BINDING_ID, "server");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_FMS = new ChannelGroupTypeUID(BINDING_ID, "farming");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_FMSREV = new ChannelGroupTypeUID(BINDING_ID,
            "farmingRev");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_HA = new ChannelGroupTypeUID(BINDING_ID,
            "homeAutomation");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_INFO = new ChannelGroupTypeUID(BINDING_ID,
            "information");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_EP = new ChannelGroupTypeUID(BINDING_ID,
            "emergencyPower");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_SYS = new ChannelGroupTypeUID(BINDING_ID, "system");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_UM = new ChannelGroupTypeUID(BINDING_ID, "update");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_WB = new ChannelGroupTypeUID(BINDING_ID, "wallbox");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_QPI = new ChannelGroupTypeUID(BINDING_ID, "inverter");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_SE = new ChannelGroupTypeUID(BINDING_ID,
            "storageExtension");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_DEBUG = new ChannelGroupTypeUID(BINDING_ID, "debug");

    // List of channel groups
    public static final String CHANNEL_GROUP_RSCP = "rscp";
    public static final String CHANNEL_GROUP_EMS = "ems";
    public static final String CHANNEL_GROUP_PVI = "pvi";
    public static final String CHANNEL_GROUP_BAT = "battery";
    public static final String CHANNEL_GROUP_DCDC = "dcdc";
    public static final String CHANNEL_GROUP_PM = "powermeter";
    public static final String CHANNEL_GROUP_DB = "database";
    public static final String CHANNEL_GROUP_FMS = "farmingSystem";
    public static final String CHANNEL_GROUP_FARM = "farming";
    public static final String CHANNEL_GROUP_FARMREV = "farmingRev";
    public static final String CHANNEL_GROUP_SRV = "server";
    public static final String CHANNEL_GROUP_HA = "homeAutomation";
    public static final String CHANNEL_GROUP_INFO = "information";
    public static final String CHANNEL_GROUP_EP = "emergencyPower";
    public static final String CHANNEL_GROUP_SYS = "system";
    public static final String CHANNEL_GROUP_UM = "update";
    public static final String CHANNEL_GROUP_WB = "wallbox";
    public static final String CHANNEL_GROUP_QPI = "inverter";
    public static final String CHANNEL_GROUP_SE = "storageExtension";
    public static final String CHANNEL_GROUP_MBS = "modbus";
    public static final String CHANNEL_GROUP_DEBUG = "debug";

    // List of channels
    public static final String CHANNEL_CurrentPowerPV = "CurrentPowerPV";
    public static final String CHANNEL_CurrentPowerBat = "CurrentPowerBat";
    public static final String CHANNEL_CurrentPowerHome = "CurrentPowerHome";
    public static final String CHANNEL_CurrentPowerGrid = "CurrentPowerGrid";
    public static final String CHANNEL_CurrentPowerAdd = "CurrentPowerAdd";
    public static final String CHANNEL_BatterySOC = "BatterySOC";
    public static final String CHANNEL_SelfConsumption = "SelfConsumption";
    public static final String CHANNEL_Autarky = "Autarky";
    public static final String CHANNEL_PMCurrentEnergyL1 = "PMCurrentEnergyL1";
    public static final String CHANNEL_PMCurrentEnergyL2 = "PMCurrentEnergyL2";
    public static final String CHANNEL_PMCurrentEnergyL3 = "PMCurrentEnergyL3";
    public static final String CHANNEL_PMCurrentPowerL1 = "PMCurrentPowerL1";
    public static final String CHANNEL_PMCurrentPowerL2 = "PMCurrentPowerL2";
    public static final String CHANNEL_PMCurrentPowerL3 = "PMCurrentPowerL3";
    public static final String CHANNEL_PMCurrentVoltageL1 = "PMCurrentVoltageL1";
    public static final String CHANNEL_PMCurrentVoltageL2 = "PMCurrentVoltageL2";
    public static final String CHANNEL_PMCurrentVoltageL3 = "PMCurrentVoltageL3";
    public static final String CHANNEL_PMMode = "Mode";
    public static final String CHANNEL_PhaseActive_1 = "PMPhaseActiveL1";
    public static final String CHANNEL_PhaseActive_2 = "PMPhaseActiveL2";
    public static final String CHANNEL_PhaseActive_3 = "PMPhaseActiveL3";
    public static final String CHANNEL_PMType = "PMType";

    public static final String CHANNEL_PowerLimitsUsed = "PowerLimitsUsed";
    public static final String CHANNEL_MaxDischarge = "MaxDischarge";
    public static final String CHANNEL_MaxCharge = "MaxCharge";
    public static final String CHANNEL_DischargeStart = "DischargeStart";
    public static final String CHANNEL_WeatherRegulatedCharge = "WeatherRegulatedCharge";
    public static final String CHANNEL_PowerSave = "PowerSave";
    public static final String CHANNEL_EmergencyPowerStatus = "EmergencyPowerStatus";

    public static final String CHANNEL_StartManualCharge = "StartManualCharge";
    public static final String CHANNEL_StartEmergencyPowerTest = "StartEmergencyPowerTest";

    public static final String CHANNEL_IsReadyForSwitch = "IsReadyForSwitch";
    public static final String CHANNEL_GridConnected = "GridConnected";
    public static final String CHANNEL_IsIslandGrid = "IsIslandGrid";
    public static final String CHANNEL_IsInvalidState = "IsInvalidState";
    public static final String CHANNEL_IsPossible = "IsPossible";

    public static final String CHANNEL_SerialNumber = "SerialNumber";
    public static final String CHANNEL_ProductionDate = "ProductionDate";
    public static final String CHANNEL_IPAddress = "IPAddress";
    public static final String CHANNEL_SubnetMask = "SubnetMask";
    public static final String CHANNEL_MACAddress = "MACAddress";
    public static final String CHANNEL_Gateway = "Gateway";
    public static final String CHANNEL_DNS = "DNS";
    public static final String CHANNEL_DHCP = "DHCP";
    public static final String CHANNEL_Time = "Time";
    public static final String CHANNEL_UTCTime = "UTCTime";
    public static final String CHANNEL_TimeZone = "TimeZone";
    public static final String CHANNEL_SWRelease = "SWRelease";

    public static final String CHANNEL_WB_EnergyAll = "EnergyAll";
    public static final String CHANNEL_WB_EnergySolar = "EnergySolar";
    public static final String CHANNEL_WB_BatterySOC = "WBSOC";
    public static final String CHANNEL_WB_EnergyL1 = "WBEnergyL1";
    public static final String CHANNEL_WB_EnergyL2 = "WBEnergyL2";
    public static final String CHANNEL_WB_EnergyL3 = "WBEnergyL3";
    public static final String CHANNEL_WB_PowerL1 = "WBPowerL1";
    public static final String CHANNEL_WB_PowerL2 = "WBPowerL2";
    public static final String CHANNEL_WB_PowerL3 = "WBPowerL3";
    public static final String CHANNEL_WB_Mode = "WBMode";
    public static final String CHANNEL_WB_PM_Mode = "WBPMMode";
    public static final String CHANNEL_WB_PhaseActive_1 = "WBPhaseActiveL1";
    public static final String CHANNEL_WB_PhaseActive_2 = "WBPhaseActiveL2";
    public static final String CHANNEL_WB_PhaseActive_3 = "WBPhaseActiveL3";

    public static final String CHANNEL_Rebooting = "IsRebooting";

    public static final String CHANNEL_IsModbusEnabled = "IsModbusEnabled";

    public static final String CHANNEL_UpdateStatus = "UpdateStatus";

    public static final String CHANNEL_DebugQuery = "DebugQuery";
}
