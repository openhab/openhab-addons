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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * ClusterRegistry
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class ClusterRegistry {

    public static final Map<Integer, Class<? extends BaseCluster>> CLUSTER_IDS = new HashMap<>();
    static {
        CLUSTER_IDS.put(31, AccessControlCluster.class);
        CLUSTER_IDS.put(1294, AccountLoginCluster.class);
        CLUSTER_IDS.put(37, ActionsCluster.class);
        CLUSTER_IDS.put(114, ActivatedCarbonFilterMonitoringCluster.class);
        CLUSTER_IDS.put(60, AdministratorCommissioningCluster.class);
        CLUSTER_IDS.put(91, AirQualityCluster.class);
        CLUSTER_IDS.put(1293, ApplicationBasicCluster.class);
        CLUSTER_IDS.put(1292, ApplicationLauncherCluster.class);
        CLUSTER_IDS.put(1291, AudioOutputCluster.class);
        CLUSTER_IDS.put(769, BallastConfigurationCluster.class);
        CLUSTER_IDS.put(40, BasicInformationCluster.class);
        CLUSTER_IDS.put(30, BindingCluster.class);
        CLUSTER_IDS.put(69, BooleanStateCluster.class);
        CLUSTER_IDS.put(128, BooleanStateConfigurationCluster.class);
        CLUSTER_IDS.put(57, BridgedDeviceBasicInformationCluster.class);
        CLUSTER_IDS.put(1037, CarbonDioxideConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(1036, CarbonMonoxideConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(1284, ChannelCluster.class);
        CLUSTER_IDS.put(768, ColorControlCluster.class);
        CLUSTER_IDS.put(1873, CommissionerControlCluster.class);
        CLUSTER_IDS.put(1296, ContentAppObserverCluster.class);
        CLUSTER_IDS.put(1295, ContentControlCluster.class);
        CLUSTER_IDS.put(1290, ContentLauncherCluster.class);
        CLUSTER_IDS.put(29, DescriptorCluster.class);
        CLUSTER_IDS.put(152, DeviceEnergyManagementCluster.class);
        CLUSTER_IDS.put(159, DeviceEnergyManagementModeCluster.class);
        CLUSTER_IDS.put(50, DiagnosticLogsCluster.class);
        CLUSTER_IDS.put(93, DishwasherAlarmCluster.class);
        CLUSTER_IDS.put(89, DishwasherModeCluster.class);
        CLUSTER_IDS.put(257, DoorLockCluster.class);
        CLUSTER_IDS.put(1872, EcosystemInformationCluster.class);
        CLUSTER_IDS.put(145, ElectricalEnergyMeasurementCluster.class);
        CLUSTER_IDS.put(144, ElectricalPowerMeasurementCluster.class);
        CLUSTER_IDS.put(153, EnergyEvseCluster.class);
        CLUSTER_IDS.put(157, EnergyEvseModeCluster.class);
        CLUSTER_IDS.put(155, EnergyPreferenceCluster.class);
        CLUSTER_IDS.put(55, EthernetNetworkDiagnosticsCluster.class);
        CLUSTER_IDS.put(514, FanControlCluster.class);
        CLUSTER_IDS.put(64, FixedLabelCluster.class);
        CLUSTER_IDS.put(1028, FlowMeasurementCluster.class);
        CLUSTER_IDS.put(1067, FormaldehydeConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(48, GeneralCommissioningCluster.class);
        CLUSTER_IDS.put(51, GeneralDiagnosticsCluster.class);
        CLUSTER_IDS.put(63, GroupKeyManagementCluster.class);
        CLUSTER_IDS.put(4, GroupsCluster.class);
        CLUSTER_IDS.put(113, HepaFilterMonitoringCluster.class);
        CLUSTER_IDS.put(70, IcdManagementCluster.class);
        CLUSTER_IDS.put(3, IdentifyCluster.class);
        CLUSTER_IDS.put(1024, IlluminanceMeasurementCluster.class);
        CLUSTER_IDS.put(1874, JointFabricDatastoreCluster.class);
        CLUSTER_IDS.put(1875, JointFabricPkiCluster.class);
        CLUSTER_IDS.put(1289, KeypadInputCluster.class);
        CLUSTER_IDS.put(74, LaundryDryerControlsCluster.class);
        CLUSTER_IDS.put(83, LaundryWasherControlsCluster.class);
        CLUSTER_IDS.put(81, LaundryWasherModeCluster.class);
        CLUSTER_IDS.put(8, LevelControlCluster.class);
        CLUSTER_IDS.put(43, LocalizationConfigurationCluster.class);
        CLUSTER_IDS.put(1288, LowPowerCluster.class);
        CLUSTER_IDS.put(1287, MediaInputCluster.class);
        CLUSTER_IDS.put(1286, MediaPlaybackCluster.class);
        CLUSTER_IDS.put(95, MicrowaveOvenControlCluster.class);
        CLUSTER_IDS.put(94, MicrowaveOvenModeCluster.class);
        CLUSTER_IDS.put(80, ModeSelectCluster.class);
        CLUSTER_IDS.put(49, NetworkCommissioningCluster.class);
        CLUSTER_IDS.put(1043, NitrogenDioxideConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(1030, OccupancySensingCluster.class);
        CLUSTER_IDS.put(6, OnOffCluster.class);
        CLUSTER_IDS.put(62, OperationalCredentialsCluster.class);
        CLUSTER_IDS.put(96, OperationalStateCluster.class);
        CLUSTER_IDS.put(41, OtaSoftwareUpdateProviderCluster.class);
        CLUSTER_IDS.put(42, OtaSoftwareUpdateRequestorCluster.class);
        CLUSTER_IDS.put(72, OvenCavityOperationalStateCluster.class);
        CLUSTER_IDS.put(73, OvenModeCluster.class);
        CLUSTER_IDS.put(1045, OzoneConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(1069, Pm10ConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(1068, Pm1ConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(1066, Pm25ConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(47, PowerSourceCluster.class);
        CLUSTER_IDS.put(46, PowerSourceConfigurationCluster.class);
        CLUSTER_IDS.put(156, PowerTopologyCluster.class);
        CLUSTER_IDS.put(1027, PressureMeasurementCluster.class);
        CLUSTER_IDS.put(66, ProxyConfigurationCluster.class);
        CLUSTER_IDS.put(67, ProxyDiscoveryCluster.class);
        CLUSTER_IDS.put(512, PumpConfigurationAndControlCluster.class);
        CLUSTER_IDS.put(1071, RadonConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(87, RefrigeratorAlarmCluster.class);
        CLUSTER_IDS.put(82, RefrigeratorAndTemperatureControlledCabinetModeCluster.class);
        CLUSTER_IDS.put(1029, RelativeHumidityMeasurementCluster.class);
        CLUSTER_IDS.put(85, RvcCleanModeCluster.class);
        CLUSTER_IDS.put(97, RvcOperationalStateCluster.class);
        CLUSTER_IDS.put(84, RvcRunModeCluster.class);
        CLUSTER_IDS.put(98, ScenesManagementCluster.class);
        CLUSTER_IDS.put(336, ServiceAreaCluster.class);
        CLUSTER_IDS.put(92, SmokeCoAlarmCluster.class);
        CLUSTER_IDS.put(52, SoftwareDiagnosticsCluster.class);
        CLUSTER_IDS.put(59, SwitchCluster.class);
        CLUSTER_IDS.put(1285, TargetNavigatorCluster.class);
        CLUSTER_IDS.put(86, TemperatureControlCluster.class);
        CLUSTER_IDS.put(1026, TemperatureMeasurementCluster.class);
        CLUSTER_IDS.put(513, ThermostatCluster.class);
        CLUSTER_IDS.put(516, ThermostatUserInterfaceConfigurationCluster.class);
        CLUSTER_IDS.put(1106, ThreadBorderRouterManagementCluster.class);
        CLUSTER_IDS.put(53, ThreadNetworkDiagnosticsCluster.class);
        CLUSTER_IDS.put(1107, ThreadNetworkDirectoryCluster.class);
        CLUSTER_IDS.put(44, TimeFormatLocalizationCluster.class);
        CLUSTER_IDS.put(56, TimeSynchronizationCluster.class);
        CLUSTER_IDS.put(1070, TotalVolatileOrganicCompoundsConcentrationMeasurementCluster.class);
        CLUSTER_IDS.put(45, UnitLocalizationCluster.class);
        CLUSTER_IDS.put(65, UserLabelCluster.class);
        CLUSTER_IDS.put(68, ValidProxiesCluster.class);
        CLUSTER_IDS.put(129, ValveConfigurationAndControlCluster.class);
        CLUSTER_IDS.put(1283, WakeOnLanCluster.class);
        CLUSTER_IDS.put(148, WaterHeaterManagementCluster.class);
        CLUSTER_IDS.put(158, WaterHeaterModeCluster.class);
        CLUSTER_IDS.put(121, WaterTankLevelMonitoringCluster.class);
        CLUSTER_IDS.put(54, WiFiNetworkDiagnosticsCluster.class);
        CLUSTER_IDS.put(1105, WiFiNetworkManagementCluster.class);
        CLUSTER_IDS.put(258, WindowCoveringCluster.class);
    }
}
