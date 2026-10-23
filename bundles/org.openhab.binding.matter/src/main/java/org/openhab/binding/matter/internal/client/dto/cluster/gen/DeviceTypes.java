/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * DeviceTypes
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DeviceTypes {

    public static final Map<Integer, String> DEVICE_MAPPING = new HashMap<>();
    static {
        DEVICE_MAPPING.put(14, "Aggregator");
        DEVICE_MAPPING.put(45, "AirPurifier");
        DEVICE_MAPPING.put(44, "AirQualitySensor");
        DEVICE_MAPPING.put(321, "AudioDoorbell");
        DEVICE_MAPPING.put(40, "BasicVideoPlayer");
        DEVICE_MAPPING.put(24, "BatteryStorage");
        DEVICE_MAPPING.put(19, "BridgedNode");
        DEVICE_MAPPING.put(327, "CameraController");
        DEVICE_MAPPING.put(322, "Camera");
        DEVICE_MAPPING.put(41, "CastingVideoClient");
        DEVICE_MAPPING.put(35, "CastingVideoPlayer");
        DEVICE_MAPPING.put(326, "Chime");
        DEVICE_MAPPING.put(574, "ClosureController");
        DEVICE_MAPPING.put(560, "Closure");
        DEVICE_MAPPING.put(561, "ClosurePanel");
        DEVICE_MAPPING.put(261, "ColorDimmerSwitch");
        DEVICE_MAPPING.put(268, "ColorTemperatureLight");
        DEVICE_MAPPING.put(21, "ContactSensor");
        DEVICE_MAPPING.put(36, "ContentApp");
        DEVICE_MAPPING.put(2112, "ControlBridge");
        DEVICE_MAPPING.put(119, "CookSurface");
        DEVICE_MAPPING.put(120, "Cooktop");
        DEVICE_MAPPING.put(1293, "DeviceEnergyManagement");
        DEVICE_MAPPING.put(257, "DimmableLight");
        DEVICE_MAPPING.put(267, "DimmablePlugInUnit");
        DEVICE_MAPPING.put(260, "DimmerSwitch");
        DEVICE_MAPPING.put(117, "Dishwasher");
        DEVICE_MAPPING.put(11, "DoorLockController");
        DEVICE_MAPPING.put(10, "DoorLock");
        DEVICE_MAPPING.put(328, "Doorbell");
        DEVICE_MAPPING.put(1299, "ElectricalEnergyTariff");
        DEVICE_MAPPING.put(1300, "ElectricalMeter");
        DEVICE_MAPPING.put(1296, "ElectricalSensor");
        DEVICE_MAPPING.put(1297, "ElectricalUtilityMeter");
        DEVICE_MAPPING.put(1292, "EnergyEvse");
        DEVICE_MAPPING.put(269, "ExtendedColorLight");
        DEVICE_MAPPING.put(122, "ExtractorHood");
        DEVICE_MAPPING.put(43, "Fan");
        DEVICE_MAPPING.put(324, "FloodlightCamera");
        DEVICE_MAPPING.put(774, "FlowSensor");
        DEVICE_MAPPING.put(15, "GenericSwitch");
        DEVICE_MAPPING.put(777, "HeatPump");
        DEVICE_MAPPING.put(775, "HumiditySensor");
        DEVICE_MAPPING.put(320, "Intercom");
        DEVICE_MAPPING.put(64, "IrrigationSystem");
        DEVICE_MAPPING.put(304, "JointFabricAdministrator");
        DEVICE_MAPPING.put(124, "LaundryDryer");
        DEVICE_MAPPING.put(115, "LaundryWasher");
        DEVICE_MAPPING.put(262, "LightSensor");
        DEVICE_MAPPING.put(1298, "MeterReferencePoint");
        DEVICE_MAPPING.put(121, "MicrowaveOven");
        DEVICE_MAPPING.put(39, "ModeSelect");
        DEVICE_MAPPING.put(272, "MountedDimmableLoadControl");
        DEVICE_MAPPING.put(271, "MountedOnOffControl");
        DEVICE_MAPPING.put(144, "NetworkInfrastructureManager");
        DEVICE_MAPPING.put(263, "OccupancySensor");
        DEVICE_MAPPING.put(256, "OnOffLight");
        DEVICE_MAPPING.put(259, "OnOffLightSwitch");
        DEVICE_MAPPING.put(266, "OnOffPlugInUnit");
        DEVICE_MAPPING.put(2128, "OnOffSensor");
        DEVICE_MAPPING.put(20, "OtaProvider");
        DEVICE_MAPPING.put(18, "OtaRequestor");
        DEVICE_MAPPING.put(123, "Oven");
        DEVICE_MAPPING.put(17, "PowerSource");
        DEVICE_MAPPING.put(773, "PressureSensor");
        DEVICE_MAPPING.put(772, "PumpController");
        DEVICE_MAPPING.put(771, "Pump");
        DEVICE_MAPPING.put(68, "RainSensor");
        DEVICE_MAPPING.put(112, "Refrigerator");
        DEVICE_MAPPING.put(116, "RoboticVacuumCleaner");
        DEVICE_MAPPING.put(114, "RoomAirConditioner");
        DEVICE_MAPPING.put(22, "RootNode");
        DEVICE_MAPPING.put(25, "SecondaryNetworkInterface");
        DEVICE_MAPPING.put(118, "SmokeCoAlarm");
        DEVICE_MAPPING.put(325, "SnapshotCamera");
        DEVICE_MAPPING.put(69, "SoilSensor");
        DEVICE_MAPPING.put(23, "SolarPower");
        DEVICE_MAPPING.put(34, "Speaker");
        DEVICE_MAPPING.put(113, "TemperatureControlledCabinet");
        DEVICE_MAPPING.put(770, "TemperatureSensor");
        DEVICE_MAPPING.put(778, "ThermostatController");
        DEVICE_MAPPING.put(769, "Thermostat");
        DEVICE_MAPPING.put(145, "ThreadBorderRouter");
        DEVICE_MAPPING.put(323, "VideoDoorbell");
        DEVICE_MAPPING.put(42, "VideoRemoteControl");
        DEVICE_MAPPING.put(65, "WaterFreezeDetector");
        DEVICE_MAPPING.put(1295, "WaterHeater");
        DEVICE_MAPPING.put(67, "WaterLeakDetector");
        DEVICE_MAPPING.put(66, "WaterValve");
        DEVICE_MAPPING.put(515, "WindowCoveringController");
        DEVICE_MAPPING.put(514, "WindowCovering");
    }
    /**
     * This device type aggregates endpoints as a collection. Clusters on the endpoint indicating this device type
     * provide functionality for the collection of descendant endpoints present in the PartsList of the endpoint's
     * descriptor, for example the Actions cluster.
     * The purpose of this device type is to aggregate functionality for a collection of endpoints. The definition of
     * the collection or functionality is not defined here.
     * When using this device type as a collection of bridged nodes, please see the "Bridge" section in the System Model
     * specification.
     * ### Cluster Requirements
     * The Identify cluster SHOULD be used in case this device type is used to represent a Bridge which has a mechanism
     * to identify itself to the user (e.g. blinking LED on the bridge itself).
     * For the Identify-functionality of the individual bridged devices, see the Identify cluster on the endpoint for a
     * bridged device.
     * ### Endpoint Composition
     * An Aggregator endpoint's Descriptor cluster PartsList attribute shall list the collection of all endpoints
     * aggregated by the Aggregator device type, i.e. the full-family pattern defined in the System Model specification.
     * #### Multiple aggregators
     * When a Node has multiple instances of the Aggregator device type, the composition shall comply with one of the
     * following two patterns for any given pair (A,B) of endpoints with the Aggregator device type:
     * - No overlap: The endpoints in the PartsList attribute of Aggregator A do not appear in the PartsList attribute
     * of Aggregator B, and vice versa.
     * - Example: A Node which bridges to two non-Matter independent technologies (e.g. Zigbee and Z-Wave), see the
     * aggregators on endpoints 11 and 31 in the figure below - their lists of endpoints (12-14, 21-23 versus 32-33) do
     * not overlap.
     * - Strict subset: The endpoint where aggregator B is exposed and all endpoints in its PartsList attribute (the
     * subset) are included in the PartsList attribute of Aggregator A (the superset).
     * - This maintains the rule that there shall be a single path from the Root Node to each endpoint (see System
     * Model).
     * - Example: A Node which implements a bridge to Zigbee, and one of those Zigbee devices is connected to a string
     * of DALI lights, which can be addressed individually and thus this Zigbee/DALI device functions as a bridge from
     * Zigbee to DALI; in the figure below one can see that the endpoints for the Zigbee/DALI bridge listed in the
     * PartsList of the aggregator on endpoint 14 (21-23) form a strict subset of the endpoints for the Zigbee bridge in
     * the PartsList of the aggregator on endpoint 11 (12-14, 21-23), and the endpoint 14 of the "subset" aggregator is
     * included in the PartsList of the "superset" aggregator on endpoint 11.
     * ### Disambiguation
     * If the Duplicate condition applies to child endpoints of an Aggregator endpoint that represent multiple
     * independent bridged devices, the endpoints SHOULD make available metadata to allow a client to disambiguate
     * distinct bridged devices with an overlap in application device types.
     * Typically this is done using the NodeLabel attribute of the Bridged Device Basic Information cluster - thus
     * reusing the naming information which the bridge already has to allow disambiguation to the user when using a
     * direct user interface to the bridge.
     * > [!NOTE]
     * > Example: the Aggregator in this figure (copied from the "Bridge for non-Matter devices" section in the Core
     * Specification) exposes several Color Temperature Lights (endpoints 13 and 22) which are disambiguated with their
     * NodeLabel. Note that the compound device at endpoints 24, 25 and 26 also uses a TagList (for information rather
     * than disambiguation) since, for this case, the bridge knows the lighting direction of both elements of the
     * compound device.
     **/
    public static final Integer AGGREGATOR = 14;
    /**
     * An Air Purifier is a standalone device that is designed to clean the air in a room.
     * It is a device that has a fan to control the air speed while it is operating. Optionally, it can report on the
     * condition of its filters.
     * ### Device Type Requirements
     * An Air Purifier may expose elements of its functionality through one or more additional device types on different
     * endpoints. All devices used in compositions shall adhere to the disambiguation requirements of the System Model.
     * Other device types, not explicitly listed in the table, may also be included in device compositions but are not
     * considered part of the core functionality of the device.
     * ### Cluster Restrictions
     * #### On/Off Cluster (Server) Clarifications
     * The On/Off cluster is independent from the Fan Control Cluster's FanMode attribute, which also includes an Off
     * setting.
     * If the FanMode attribute of the Fan Control cluster is set to a value other than Off when the OnOff attribute of
     * the On/Off cluster transitions from TRUE to FALSE, it may be desirable to restore the FanMode, SpeedSetting and
     * PercentSetting attribute values of the Fan Control cluster when the OnOff attribute of the On/Off cluster later
     * transitions from FALSE to TRUE. If the FanMode is set to Off when the device is turned off, this information is
     * lost, as the SpeedSetting and PercentSetting will be set to zero. Using the On/Off cluster alongside the Fan
     * Control cluster allows the FanMode, SpeedSetting and PercentSetting to remain unchanged when the device is turned
     * off. In this case, the On/Off cluster would be set to Off, and the SpeedCurrent and PercentCurrent set to zero,
     * without changing FanMode, SpeedSetting and PercentSetting.
     **/
    public static final Integer AIR_PURIFIER = 45;
    /**
     * This defines conformance for the Air Quality Sensor device type.
     * An air quality sensor is a device designed to monitor and measure various parameters related to the quality of
     * ambient air in indoor or outdoor environments.
     **/
    public static final Integer AIR_QUALITY_SENSOR = 44;
    /**
     * An Audio Doorbell device is composed in all cases with a generic switch to provide a doorbell with Audio only
     * streaming.
     **/
    public static final Integer AUDIO_DOORBELL = 321;
    /**
     * This defines conformance to the Basic Video Player device type.
     * A Video Player (either Basic or Casting) represents a device that is able to play media to a physical output or
     * to a display screen which is part of the device.
     * A Basic Video Player has playback controls (play, pause, etc.) and keypad remote controls (up, down, number
     * input), but is not able to launch content and is not a content app platform (the Casting Video Player device type
     * is used for these functions).
     * For example, a Basic Video Player can be a traditional TV device a physical media playback device such as a DVD
     * Player, or a device that provides input to another device like a TV or computer monitor.
     * See Section 10.1, "Video Player Architecture" for additional Basic Video Player requirements relating to Video
     * Player device endpoint composition, commissioning, feature representation in clusters, and UI context.
     **/
    public static final Integer BASIC_VIDEO_PLAYER = 40;
    /**
     * A Battery Storage device is a device that allows a DC battery, which can optionally be comprised of a set
     * parallel strings of battery packs and associated controller, and an AC inverter, to be monitored and controlled
     * by an Energy Management System in order to manage the peaks and troughs of supply and demand, and/or to optimize
     * cost of the energy consumed in premises. It is not intended to be used for a UPS directly supplying a set of
     * appliances, nor for portable battery storage devices.
     * ### Battery Storage Architecture
     * A Battery Storage device is always defined via endpoint composition. See Section 14.4.6, "Device Type
     * Requirements" for more details.
     * An example of a Battery Storage device with single phase AC output is illustrated below.
     * An example of a Battery Storage device which also includes a directly connected Solar Power device supplying DC
     * power to the battery and using a single common inverter to the single phase AC input and output is illustrated
     * below.
     * ### Device Type Requirements
     * A Battery Storage device shall be composed of at least two endpoints with device types as defined by the
     * conformance below. There may be more endpoints with additional instances of these device types or additional
     * device types existing in the Battery Storage device.
     * The Solar Power devices, if included, shall have separate endpoints, and include their own Power Source,
     * Electrical Sensor, and Device Energy Management devices, as defined by the Solar Power device.
     * #### Cluster Requirements on Component Device Types
     * > [!NOTE]
     * > NOTE: The use of 1st and 2nd to annotate the device types is purely to distinguish the two from each other. It
     * does NOT specify any order or structure of the composition.
     * #### Element Requirements on Component Device Types
     * The Power Source cluster in the Power Source device shall support the RECHG feature if it can be charged as well
     * as discharged through the connection to the premises wiring.
     * The Electrical Sensor device shall also conform to the following:
     * - An Electrical Sensor device shall measure the energy and power flows of the Battery Storage device at the AC
     * grid connection point.
     * - The Electrical Power Measurement cluster of this Electrical Sensor device shall support the PolyphasePower
     * feature if the Battery Storage device is connected via polyphase wiring, and SHOULD support the ReactivePower
     * attribute.
     * - The Electrical Energy Measurement cluster of this Electrical Sensor device shall support the ImportedEnergy
     * feature if it can be charged as well as discharged through the connection to the premises wiring, and SHOULD
     * support the CumulativeEnergy feature.
     * If a Battery Storage device supports two or three phase power output then it may include two or three additional
     * endpoints, each including an Electrical Sensor Device Type as child elements. For each such child endpoint it
     * shall include a semantic tag from the Electrical Measurement Namespace in the TagList attribute of the Descriptor
     * cluster to describe the endpoint for the relevant Electrical Power Measurement and Electrical Energy Measurement
     * clusters indicating the relevant AC phase that is being measured.
     * If a Battery Storage device supports measurement of the input and output of individual batteries or sets of
     * batteries then it may include additional endpoints for each such measurement, including an Electrical Sensor
     * Device Type as child elements. For each such child endpoint: it shall include a semantic tag from the Common
     * Number Namespace, or a Manufacturer defined Tag and Label, in the TagList attribute of the Descriptor cluster to
     * describe the endpoint for the relevant Electrical Power Measurement and Electrical Energy Measurement clusters
     * indicating the relevant device port, battery, or set of batteries that is being measured. it SHOULD also include
     * a User Label cluster to allow an installer to add identifying information if the device permits flexible
     * connection of the actual batteries at installation time.
     * Any Temperature Sensors included shall include Tag(s), and for non-standard Namespaces, Label(s) in the
     * Descriptor clusters of their endpoints to identify the temperature being measured.
     **/
    public static final Integer BATTERY_STORAGE = 24;
    /**
     * This defines conformance for a Bridged Node root endpoint. This endpoint is akin to a "read me first" endpoint
     * that describes itself and any other endpoints that make up the Bridged Node. A Bridged Node endpoint represents a
     * device on a foreign network, but is not the root endpoint of the bridge itself.
     * ### Device Type Requirements
     * This device type shall only be indicated on endpoints which are listed in the Descriptor cluster PartsList of
     * another endpoint with an Aggregator device type.
     * ### Endpoint Composition
     * A Bridged Node endpoint shall support one of the following composition patterns:
     * - Separate Endpoints: All application device types are supported on separate descendant endpoints, and shall NOT
     * be hosted on the Bridged Node endpoint. The Bridged Node endpoint's Descriptor cluster PartsList attribute shall
     * indicate a list of all endpoints representing the functionality of the bridged device, including the endpoints
     * supporting the application device types, i.e. the full-family pattern defined in the System Model specification.
     * This is used for the following cases:
     * - Exposing a compound device - the child endpoints each have a part of the functionality of the bridged device.
     * See endpoints 31-34 in the example below; the bridged device is a PIR sensor which also has temperature and
     * illuminance measurement. Endpoints 32-34 host the associated application device types and clusters. Endpoint 31
     * (the endpoint with the Bridged Node device type) functions as parent for these endpoints and has no application
     * device types.
     * - Exposing a composed device type - a child endpoint of the endpoint with the Bridged Node device type has the
     * composed device type; this endpoint with the composed device type has child endpoints for the device type(s) that
     * are mandatory or optional for the composed device type. See endpoints 41-43 in the example below; this is a
     * refrigerator, which is a composed device type, hosted on endpoint 42, with the associated temperature controlled
     * cabinet device type on child endpoint 43. Endpoint 41 (the endpoint with the Bridged Node device type) functions
     * as parent for the endpoint hosting the composed device type and has no application clusters.
     * - Combinations of the above.
     * - One Endpoint: Both the Bridged Node and one or more application device types are supported on the same endpoint
     * (following application device type rules). The PartsList attribute in the Descriptor cluster shall be empty.
     * Since compound devices and composed device types each need more than one endpoint to expose their functionality,
     * they cannot use the "One Endpoint" pattern and need to use the "Separate Endpoints" model described above.
     * - Example in the figure below: endpoint 21 hosts the Bridged Node utility device type, plus the application
     * device type for a dimmable light on same endpoint. Since the dimmable light device type is a superset of on/off
     * light, that subset device type may be added here as well.
     * In all these composition patterns, endpoint composition shall conform to the application device type(s)
     * definition.
     **/
    public static final Integer BRIDGED_NODE = 19;
    /**
     * A Camera controller device is a device that provides interfaces for controlling and managing camera devices.
     * ### Cluster Requirements
     * A node shall only ever have, at most, one instance of the Camera Controller's required clusters.
     **/
    public static final Integer CAMERA_CONTROLLER = 327;
    /**
     * A Camera device is a camera that provides interfaces for controlling and transporting captured media, such as
     * Audio, Video or Snapshots.
     * ### Device Type Requirements
     * A Camera may expose elements of its functionality through one or more additional device types on different
     * endpoints. All devices used in compositions shall adhere to the disambiguation requirements of the System Model.
     * Other device types, not explicitly listed in the table, may also be included in device compositions but are not
     * considered part of the core functionality of the device.
     * Cameras which implement occupancy detection based on the signals from the optical sensor, may expose this
     * functionality using an Occupancy Sensing cluster on the primary camera endpoint along with the other camera
     * functionality. The device type Occupancy Sensor shall NOT be added to the DeviceTypeList of this endpoint.
     * Cameras may have an Occupancy Sensor of a different type for occupancy detection independent of the optical
     * sensor. If this sensor is exposed, it shall be placed on a child endpoint of the primary camera endpoint, with
     * the corresponding device type Occupancy Sensor, as indicated in the following table:
     **/
    public static final Integer CAMERA = 322;
    /**
     * This defines conformance to the Casting Video Client device type.
     * A Casting Video Client is a client that can launch content on a Casting Video Player, for example, a Smart
     * Speaker or a Content Provider phone app.
     * ### Cluster Requirements
     * See Section 1.1.7, "Cluster Requirements" for additional clusters including the Binding cluster.
     **/
    public static final Integer CASTING_VIDEO_CLIENT = 41;
    /**
     * This defines conformance to the Casting Video Player device type.
     * A Video Player (either Basic or Casting) represents a device that is able to play media to a physical output or
     * to a display screen which is part of the device.
     * A Casting Video Player has basic controls for playback (play, pause, etc.) and keypad input (up, down, number
     * input), and is able to launch content.
     * For example, a Casting Video Player can be a smart TV device, a TV Set Top Box, or a content streaming device
     * that provides input to another device like a TV or computer monitor.
     * See Section 10.1, "Video Player Architecture" for additional Casting Video Player requirements relating to Video
     * Player device endpoint composition, commissioning, feature representation in clusters, and UI context.
     **/
    public static final Integer CASTING_VIDEO_PLAYER = 35;
    /**
     * A Chime device is a device which can play from a range of pre installed sounds and is typically used with a
     * Doorbell, Audio Doorbell, or Video Doorbell.
     * ### Device Type Requirements
     * A chime may expose elements of its functionality through additional device types on different endpoints. All
     * devices used in compositions shall adhere to the disambiguation requirements of the System Model. Other device
     * types, not explicitly listed in the table, may also be included in device compositions but are not considered
     * part of the core functionality of the device.
     * Chimes having sound actuators may compose a child endpoint with Speaker device type to specifically allow control
     * of the volume or mute/unmute state of sound generation.
     * When a Speaker device is composed under a chime, the following treatment applies:
     * - While different mechanisms could generate the sound, with more or less fidelity, the requirement is that the
     * standard definition for muting and sound level control of the Speaker device type shall apply. This holds even if
     * the sound generator does not actually employ a real "speaker" element (such as tuned bars being struck), whose
     * actuation shall be gated by the On/Off Cluster on the Speaker endpoint.
     * - Other clusters in the Speaker device type would also apply with the requisite data dependencies against the
     * functionality of sound output.
     * - Other modalities of chime actuation, such as visual/light feedback SHOULD NOT be impacted by the state of the
     * clusters in the Speaker endpoint, which are reserved for sound feedback.
     * > [!NOTE]
     * > NOTE: The Chime device type also includes the Chime server cluster, which has an Enabled attribute. This
     * attribute completely disables the Chime, which also means that any visual indicators would also be disabled, and
     * also acts as a form of global muting. For example, if the On/Off cluster of the child Speaker device is set to
     * from On (not muted) to Off (muted) and the Chime cluster's Enabled attribute is already set to true, the Enabled
     * attribute would not change state. Similarly the Chime cluster's Enabled attribute changing would not change the
     * child Speaker's On/Off cluster via data dependency if there are other modalities to allow a user to determine
     * Chime actuation (e.g. visual indicators).
     **/
    public static final Integer CHIME = 326;
    /**
     * A Closure Controller is capable of controlling a Closure.
     * ### Introduction
     * Two levels of control are available:
     * - Basic Level (Closure Control Cluster):
     * - Used for simple controller with buttons like wall switches.
     * - Also all the general status and information remain at this level.
     * - Advanced Level (Closure Dimension Cluster):
     * - Provides advanced information, controls and settings.
     * - Used for advanced controller.
     **/
    public static final Integer CLOSURE_CONTROLLER = 574;
    /**
     * A Closure is an element that seals an opening (such as a window, door, cabinet, wall, facade, ceiling, or roof).
     * It may contain one or more instances of a Closure Panel device type on separate child endpoints of the Closure
     * parent. Each Closure Panel is a sub-component of a Closure, capable of some change in state, primarily through a
     * movement.
     * All the common characteristics of a Closure are gathered within Closure Control Cluster. Moving parts or other
     * physical aspects of the device are exposed using Closure Dimension Cluster.
     * ### Closure Architecture
     * A Closure is a composed device type that may include additional device types on separate child endpoints. See the
     * Device Type Requirements section below for details.
     * A Closure shall use exactly one semantic tag from the Closure namespace in the TagList attribute of the
     * Descriptor cluster to describe the primary function of the device, e.g., "Window", "Covering", or "Cabinet".
     * Semantic tags from the Closure Window, Closure Covering and Closure Cabinet namespaces, in addition to the Common
     * namespaces, may be used to convey additional configuration information.
     * An example of a Closure device with multiple Closure Panel devices on separate child endpoints is illustrated
     * below.
     * An example of a Closure with a single panel on a separate child endpoint is illustrated below.
     * An example of a Closure as a standalone device type is illustrated below.
     * ### Device Type Requirements
     * A Closure device may be composed of other device types listed in the table below subject to the conformance
     * column of the table. All devices used in compositions shall adhere to the disambiguation and superset
     * requirements of the System Model.
     * Note that the On/Off Light listed in the table below is part of a Superset Device Type relationship as defined by
     * the System Model (see Superset Device Types in MatterCore), and so the rules defined in that section apply to the
     * use of an On/Off Light or its Superset device types when included on separate child endpoints.
     * The use of semantic tags in the Descriptor cluster TagList shall adhere to the disambiguation and superset
     * requirements of the System Model. Other semantic tags from other namespaces may be used to convey additional
     * configuration information.
     * All instances of Closure Panel devices included in a composition shall reflect the current state of the
     * associated panels, and operate in relation to the ClosureControl cluster at the top of the hierarchy. In other
     * words, successfully opening/closing the closure via the ClosureControl cluster shall be followed by appropriate
     * attribute updates in the ClosureDimension cluster that reflect the panels being in the state requested.
     * Similarly, operating the ClosureDimension cluster instances shall cause the attributes of the ClosureControl
     * instance at the top of the hierarchy to reflect the correct state of opening and closing at the same time.
     * Additional device types not listed in this table may also be included in device compositions.
     * ### Cluster Requirements
     * The Window Covering cluster shall NOT be present on the same endpoint for this device type. This restriction
     * prevents conflicts between future standardized uses of the Window Covering cluster and any current non-standard
     * implementations, until appropriate data dependency language is defined.
     * ### Element Requirements
     * The TagList in the Descriptor cluster of an endpoint with this device type shall meet the following constraints:
     * - There shall be exactly one tag from the Closure namespace (namespace 0x44) to identify the type of closure
     * device.
     * - There shall NOT be any tag from the ClosurePanel namespace (namespace 0x45), among all the other tags.
     **/
    public static final Integer CLOSURE = 560;
    /**
     * A Closure Panel shall ONLY exist as a part (child) of a Closure device type. It represents a single panel aspect
     * (e.g. position of a blind, tilt of slats, etc) within that Closure.
     * This panel can be used to express the following:
     * - Translation : panel translates along one axis
     * - Rotation : panel rotates around an axis of rotation
     * - Modulation : panel modifies its aspect to modulate a flow
     * A Closure Panel shall use exactly one semantic tag from the ClosurePanel namespace (0x45) in the TagList
     * attribute of the Descriptor cluster to describe the spatial aspect of the dimension, e.g., "Lift", "Tilt", etc.
     * ### Cluster Requirements
     * The Window Covering cluster shall NOT be present on the same endpoint for this device type. This restriction
     * prevents conflicts between potential future standardized use of the Window Covering cluster and any existing
     * non-standard implementations, until appropriate data dependency language is defined.
     * ### Element Requirements
     * The TagList in the Descriptor cluster of an endpoint with this device type shall meet the following constraints:
     * - There shall be exactly one tag from the ClosurePanel namespace (namespace 0x45).
     * - There shall NOT be any tag from the Closure namespace (namespace 0x44).
     **/
    public static final Integer CLOSURE_PANEL = 561;
    /**
     * A Color Dimmer Switch is a controller device that, when bound to a lighting device such as an Extended Color
     * Light, is capable of being used to adjust the color of the light being emitted.
     **/
    public static final Integer COLOR_DIMMER_SWITCH = 261;
    /**
     * A Color Temperature Light is a lighting device that is capable of being switched on or off, the intensity of its
     * light adjusted, and its color temperature adjusted by means of a bound controller device such as a Color Dimmer
     * Switch.
     **/
    public static final Integer COLOR_TEMPERATURE_LIGHT = 268;
    /**
     * This defines conformance to the Contact Sensor device type.
     * ### Cluster Requirements
     * #### Identify Cluster
     * This is used to identify the endpoint.
     * #### Boolean State Cluster
     * This is used to indicate the state of the sensor/detector.
     * The state of the Boolean State cluster shall reflect the sensor detection using this scheme:
     * #### Boolean State Configuration Cluster
     * This is used to configure the sensor/detector and is for this device type linked to the configuration of the
     * Boolean State cluster.
     **/
    public static final Integer CONTACT_SENSOR = 21;
    /**
     * This defines conformance to the Content App device type.
     * A Content App is usually an application built by a Content Provider. A Casting Video Player with a Content App
     * Platform is able to launch Content Apps and represent these apps as separate endpoints.
     * ### Endpoint Composition
     * Endpoints with this device type shall support Dynamic Endpoint Allocation as specified in the System Model
     * specification.
     * ### Disambiguation
     * When there is more than one sibling endpoint with this device type in a PartsList, disambiguation information
     * shall be provided by having a unique value in the ApplicationName attribute of the Application Basic cluster on
     * each of these endpoints.
     **/
    public static final Integer CONTENT_APP = 36;
    /**
     * A Control Bridge is a controller device that, when bound to a lighting device such as an Extended Color Light, is
     * capable of being used to switch the device on or off, adjust the intensity of the light being emitted and adjust
     * the color of the light being emitted. In addition, a Control Bridge device is capable of being used for setting
     * scenes.
     **/
    public static final Integer CONTROL_BRIDGE = 2112;
    /**
     * A Cook Surface device type represents a heating object on a cooktop or other similar device. It shall only be
     * used when composed as part of another device type.
     * ### Cluster Restrictions
     * #### On/Off Cluster (Server) Clarifications
     * The OffOnly feature is required for the On/Off cluster in this device type due to safety requirements.
     * ### Element Requirements
     * Whenever the Temperature Control cluster is included on a Cook Surface, the Temperature Control cluster shall use
     * the TemperatureLevel feature rather than the TemperatureNumber feature. This is because users are usually in the
     * loop for controlling the temperature of the food being cooked within a heated cooking utensil. For example, while
     * the surface temperature of a cooktop may be significantly above 100°C, an open pot of water will never exceed the
     * boiling point of water as all excess energy transmitted is spent on the water's phase change to steam and the
     * liquid within the pot reaches an equilibrium temperature.
     **/
    public static final Integer COOK_SURFACE = 119;
    /**
     * A cooktop is a cooking surface that heats food either by transferring currents from an electromagnetic field
     * located below the glass surface directly to the magnetic induction cookware placed above or through traditional
     * gas or electric burners.
     * ### Device Type Requirements
     * A Cooktop shall be composed of zero or more endpoints with the Cook Surface device type as defined by the
     * conformance below.
     * A cooktop falls under strict regulatory control in some regions. One of these restrictions for non-induction
     * cooktops is that the only remote commands available are to turn off the entire device or read out the temperature
     * setting. This one scenario for allowed remote operation is specifically to address the use case of a device that
     * is left on after a user leaves the home. The individual cooking surfaces cannot be shut off. This leads to a
     * model of a cooktop that has 0 controllable cooking surfaces. For example, the requirements that exist for a gas
     * cooktop would result in zero Cook Surface instances being exposed.
     * If the Cooktop contains more than one instance of a Cook Surface, those instances shall include a semantic tag in
     * the TagList attribute of the Descriptor cluster to disambiguate the cook surface, e.g., "front", "left", or
     * "back". Such a semantic tag shall be from the Common namespaces.
     * ### Cluster Restrictions
     * #### On/Off Cluster (Server) Clarifications
     * The OffOnly feature is required for the On/Off cluster in this device type due to safety requirements.
     **/
    public static final Integer COOKTOP = 120;
    /**
     * A Device Energy Management device provides reporting and optionally adjustment of the electrical power planned on
     * being consumed or produced by the device.
     **/
    public static final Integer DEVICE_ENERGY_MANAGEMENT = 1293;
    /**
     * A Dimmable Light is a lighting device that is capable of being switched on or off and the intensity of its light
     * adjusted by means of a bound controller device such as a Dimmer Switch or a Color Dimmer Switch. In addition, a
     * Dimmable Light device is also capable of being switched by means of a bound occupancy sensor or other device(s).
     **/
    public static final Integer DIMMABLE_LIGHT = 257;
    /**
     * A Dimmable Plug-In Unit is a device that provides power to another device that is plugged into it, and is capable
     * of being switched on or off and have its level adjusted. The Dimmable Plug-in Unit is typically used to control a
     * conventional non-communicating light through its mains connection using phase cutting.
     * The Mounted Dimmable Load Control (added in Matter 1.4) has identical cluster requirements as the Dimmable
     * Plug-In Unit, and is marked as a superset of this device type (since Matter 1.4.2). For devices intended to be
     * mounted permanently, the Mounted Dimmable Load Control device type shall be used, with the Dimmable Plug-In Unit
     * device type optionally added to the DeviceTypeList of the Descriptor cluster in addition to the Mounted Dimmable
     * Load Control device type (see Mounted Dimmable Load Control server guidance section).
     * Before Matter 1.4, mounted dimmable load control units typically used the Dimmable Plug-In Unit device type.
     * Clients can encounter devices which were made before or after these specification updates. Therefore, clients
     * SHOULD use the following heuristic to distinguish the type of physical device based on the device type revision
     * found on an endpoint ("--" means the device type is not listed).
     **/
    public static final Integer DIMMABLE_PLUG_IN_UNIT = 267;
    /**
     * A Dimmer Switch is a controller device that, when bound to a lighting device such as a Dimmable Light, is capable
     * of being used to switch the device on or off and adjust the intensity of the light being emitted.
     **/
    public static final Integer DIMMER_SWITCH = 260;
    /**
     * A dishwasher is a device that is generally installed in residential homes and is capable of washing dishes,
     * cutlery, and other items associate with food preparation and consumption. The device can be permanently installed
     * or portable and can have variety of filling and draining methods.
     * ### Cluster Requirements
     * > [!NOTE]
     * > NOTE: A dishwasher cycle is a combination of a mode (if supported) and a temperature (if supported). The
     * operational state cluster is then used to start the cycle once these selections have been made via the client.
     * ### Cluster Restrictions
     * #### Temperature Control Cluster (Server) Clarifications
     * Given that different markets have different customary methods of providing temperature settings (e.g. North
     * America often prefers levels, whereas many other markets provide temperatures in °C), it is recommended that when
     * the Temperature Control cluster is present, the TemperatureLevel or TemperatureNumber feature of that cluster is
     * chosen to follow the most widely applied convention for the market where the product is sold.
     * #### On/Off Cluster (Server) Clarifications
     * As indicated in the Element Requirements section below, the DF (Dead Front) feature is required for the On/Off
     * cluster in this device type. See the "DeadFrontBehavior feature" section in the On/Off cluster description for
     * detailed requirements. The "dead front" state is linked to the OnOff attribute in the On/Off cluster having the
     * value False. Thus, the Off command of the On/Off cluster shall move the device into the "dead front" state, the
     * On command of the On/Off cluster shall bring the device out of the "dead front" state, and the device shall
     * adhere with the associated requirements on subscription handling and event reporting.
     * #### Best Effort Attribute Values in "Dead Front" State
     * When in "dead front", should the operational values of the cluster attributes not be available or accessible, the
     * following are the recommended best effort values for per cluster attributes when responding to a new subscription
     * request or a read request. Attributes not listed have no change in their defined or expected values.
     **/
    public static final Integer DISHWASHER = 117;
    /**
     * A Door Lock Controller is a device capable of controlling a door lock.
     **/
    public static final Integer DOOR_LOCK_CONTROLLER = 11;
    /**
     * A Door Lock is a device used to secure a door. It is possible to actuate a door lock either by means of a manual
     * or a remote method.
     **/
    public static final Integer DOOR_LOCK = 10;
    /**
     * A Doorbell device is a switch which when pressed usually causes a Chime to activate.
     **/
    public static final Integer DOORBELL = 328;
    /**
     * A Electrical Energy Tariff is a device that defines a tariff for the consumption or production of electrical
     * energy.
     **/
    public static final Integer ELECTRICAL_ENERGY_TARIFF = 1299;
    /**
     * An Electrical Meter device meters the electrical energy being imported and/or exported for billing purposes.
     * ### Device Type Requirements
     * An Electrical Meter shall be composed of at least one endpoint with device types as defined by the conformance
     * below.
     **/
    public static final Integer ELECTRICAL_METER = 1300;
    /**
     * An Electrical Sensor device measures the electrical power and/or energy being imported and/or exported.
     * ### Cluster Requirements
     * > [!NOTE]
     * > NOTE: Electrical measurements made by either the Electrical Power Measurement cluster, the Electrical Energy
     * Measurement cluster, or both shall apply to the endpoints indicated by the Power Topology cluster.
     **/
    public static final Integer ELECTRICAL_SENSOR = 1296;
    /**
     * An Electrical Utility Meter device provides utility account information, as well as optional details about
     * tariffs and metering.
     * ### Electrical Utility Meter Topology
     * #### Basic Utility Meter
     * A basic Electrical Utility Meter device type has a simple import tariff endpoint for grid power, tagged as Grid,
     * Import, AC, and Current.
     * Optionally, this endpoint may have a child endpoint representing an upcoming tariff, if available, tagged as
     * Grid, Import, AC, and Upcoming.
     * Optionally, this endpoint may have child endpoints representing measurements of individual phases of a polyphase
     * power supply.
     * #### Separate EV Rate
     * Building on the basic topology, an Electrical Utility Meter device type which has a separate rate for EV charging
     * would add a second endpoint, tagged as EV, Import, AC, and Current.
     * Optionally, this endpoint may have a child endpoint representing an upcoming EV tariff, if available, tagged as
     * EV, Import, AC, and Upcoming.
     * #### Export Rate
     * Similarly, an Electrical Utility Meter device type which has a separate rate for exported electrical energy would
     * add a second endpoint, tagged as Grid, Export, AC, and Current.
     * Optionally, this endpoint may have a child endpoint representing an upcoming export tariff, if available, tagged
     * as Grid, Export, AC, and Upcoming.
     * #### Combination of EV and Export
     * The above topologies can be composed to represent various combinations of tariffs. In this example, a tariff has
     * separate rates for an EV and for exporting energy to the grid.
     **/
    public static final Integer ELECTRICAL_UTILITY_METER = 1297;
    /**
     * An EVSE (Electric Vehicle Supply Equipment) is a device that allows an EV (Electric Vehicle) to be connected to
     * the mains electricity supply to allow it to be charged (or discharged in case of Vehicle to Grid / Vehicle to
     * Home applications).
     * ### EVSE Architecture
     * An EVSE is always defined via endpoint composition. See Section 14.1.6, "Device Type Requirements" for more
     * details.
     * An example of an EVSE with single phase AC supply is illustrated below.
     * The EVSE may also indicate its internal temperature using the temperature measurement cluster (not shown).
     * An example of an EVSE with a 3 phase AC supply is illustrated below.
     * ### Device Type Requirements
     * An EVSE shall be composed of at least one endpoint with device types as defined by the conformance below. There
     * may be more endpoints with other device types existing in the EVSE.
     * #### Cluster Requirements on Component Device Types
     * The Electrical Sensor device shall include both the Electrical Energy Measurement and Electrical Power
     * Measurement clusters, measuring the total energy and power of the EVSE.
     * #### Element Requirements on Component Device Types
     * If an EVSE supports three phase power then it shall include three additional endpoints including an Electrical
     * Sensor Device Type as child elements. For each child endpoint it shall include a semantic tag from the Electrical
     * Measurement Namespace in the TagList attribute of the Descriptor cluster to describe the endpoint for the
     * relevant Electrical Power Measurement and Electrical Energy Measurement clusters indicating the relevant AC phase
     * that is being measured.
     * If the EVSE supports the V2X feature then the Device Energy Management cluster included in the Device Energy
     * Management device shall support the PowerAdjustment (PA) feature.
     **/
    public static final Integer ENERGY_EVSE = 1292;
    /**
     * An Extended Color Light is a lighting device that is capable of being switched on or off, the intensity of its
     * light adjusted, and its color adjusted by means of a bound controller device such as a Color Dimmer Switch or
     * Control Bridge. The device supports adjustment of color by means of hue/saturation, enhanced hue, color looping,
     * XY coordinates, and color temperature. In addition, the extended color light is also capable of being switched by
     * means of a bound occupancy sensor.
     **/
    public static final Integer EXTENDED_COLOR_LIGHT = 269;
    /**
     * An Extractor Hood is a device that is generally installed above a cooking surface in residential kitchens. An
     * Extractor Hood's primary purpose is to reduce odors that arise during the cooking process by either extracting
     * the air above the cooking surface or by recirculating and filtering it. It may also contain a light for
     * illuminating the cooking surface.
     * Extractor Hoods may also be known by the following names:
     * - Hoods
     * - Extractor Fans
     * - Extractors
     * - Range Hoods
     * - Telescoping Hoods
     * - Telescoping Extractors
     * ### Device Type Requirements
     * An Extractor Hood is composed of other device types listed in this table subject to the conformance column of the
     * table. All devices used in compositions shall adhere to the disambiguation and superset requirements of the
     * System Model. Specifically, please note that the On/Off Light as listed is a Superset Device Type as defined by
     * the System Model (see Superset Device Types in MatterCore), and so the rules defined in that section apply to the
     * use of On/Off Light as a superset when composed in this device type. Additional device types not listed in this
     * table may also be included in device compositions.
     **/
    public static final Integer EXTRACTOR_HOOD = 122;
    /**
     * A Fan device is typically standalone or mounted on a ceiling or wall and is used to circulate air in a room.
     * ### Device Type Requirements
     * A fan may expose elements of its functionality through one or more additional device types on different
     * endpoints. All devices used in compositions shall adhere to the disambiguation requirements of the System Model.
     * Other device types, not explicitly listed in the table, may also be included in device compositions but are not
     * considered part of the core functionality of the device.
     * ### Cluster Restrictions
     * #### On/Off Cluster (Server) Clarifications
     * The On/Off cluster is independent from the Fan Control Cluster's FanMode attribute, which also includes an Off
     * setting.
     * If the FanMode attribute of the Fan Control cluster is set to a value other than Off when the OnOff attribute of
     * the On/Off cluster transitions from TRUE to FALSE, it may be desirable to restore the FanMode, SpeedSetting and
     * PercentSetting attribute values of the Fan Control cluster when the OnOff attribute of the On/Off cluster later
     * transitions from FALSE to TRUE. If the FanMode is set to Off when the device is turned off, this information is
     * lost, as the SpeedSetting and PercentSetting will be set to zero. Using the On/Off cluster alongside the Fan
     * Control cluster allows the FanMode, SpeedSetting and PercentSetting to remain unchanged when the device is turned
     * off. In this case, the On/Off cluster would be set to Off, and the SpeedCurrent and PercentCurrent set to zero,
     * without changing FanMode, SpeedSetting and PercentSetting.
     **/
    public static final Integer FAN = 43;
    /**
     * A Floodlight Camera device is a composite device which combines a camera and a light, primarily used in security
     * use cases.
     * ### Device Type Requirements
     * A Floodlight Camera is composed of other device types listed in this table subject to the conformance column of
     * the table. All devices used in compositions shall adhere to the disambiguation and superset requirements of the
     * System Model. Specifically, please note that the On/Off Light, as listed, is a Superset Device Type as defined by
     * the System Model (see Superset Device Types in MatterCore), and so the rules defined in that section apply to the
     * use of On/Off Light as a superset when composed in this device type. Additional device types not listed in this
     * table may also be included in device compositions.
     **/
    public static final Integer FLOODLIGHT_CAMERA = 324;
    /**
     * A Flow Sensor device measures and reports the flow rate of a fluid.
     **/
    public static final Integer FLOW_SENSOR = 774;
    /**
     * This defines conformance for the Generic Switch device type.
     * ### Cluster Requirements
     * #### Instantaneous reporting
     * The generic mechanism for subscriptions and events might not ensure that detected interactions with the switch
     * will be delivered "instantaneously" to the Switch client cluster in the interested party (they might be sent only
     * after some time, e.g. due to batching of events and the Min Interval behavior for subscriptions). In order to
     * achieve a good user experience, a device of this device type shall send updates of attributes and events defined
     * in the Switch cluster without delay to subscribed parties.
     * #### Labeling for multi-switch devices
     * A Node which contains multiple switches will need to expose multiple endpoints each hosting an instance of this
     * device type and the associated Switch cluster. This means the Duplicate condition in Matter base device
     * requirements applies, so a TagList shall be included in the Descriptor cluster on each such endpoint. The tag(s)
     * in this TagList are used to indicate orientation (e.g. left and right for a two-button switch) or labeling (e.g.
     * "dim up" and "dim down" icons printed on the buttons) relevant to the user. A client SHOULD use these tags to
     * convey such information to the user (e.g. showing it in a user interface), to help the user identify which
     * endpoint maps to a certain orientation or labeling.
     * For the case where a server indicates tags from the Common Number Namespace, and the client presents entities
     * related to the endpoints (e.g. icons for the various switches), it SHOULD present them in numerical order as
     * indicated by the tags from the Common Number Namespace.
     * For a Node which has only one endpoint hosting an instance of this device type and the associated Switch cluster,
     * a TagList may be used. This can be beneficial in cases where the switch has some user-recognizable labeling.
     * The TagList can contain a combination of tags from the namespaces defined in the Matter Semantic Tag Namespaces,
     * including the namespace for switches as well as tags from a manufacturer-specific namespace.
     * In case the buttons have an intended function (e.g. engraved icon), the semantic tags from the Switches Namespace
     * shall be used where applicable. If there is no corresponding tag, a manufacturer-specific tag with a string Label
     * SHOULD be used (see Example 2 below).
     * To identify the location of a button on the device (e.g. top button of a two-button device), the semantic tags
     * from the Common Position Namespace shall be used where applicable.
     * For devices where these are not applicable or not sufficient (e.g. a switch device with four buttons in a row),
     * the semantic tags from the Common Number Namespace shall be used to enumerate the position of the buttons on the
     * device, in left to right, top to bottom order, starting with Number.One for the first button.
     * For devices to control a Closure (e.g. Window Covering), the semantic tags from the Switches Namespace shall be
     * used where applicable.
     * Example 1: a device with two rocker switches (mounted side by side), which has two endpoints (11,12) for the
     * switch-related functionality
     * - endpoint 11 has device type Generic Switch and contains
     * - cluster Switch (feature flags: LS) exposing the state and events of the left button
     * - cluster Descriptor with its TagList containing two tags: Position.Left and Number.One
     * - endpoint 12 has device type Generic Switch and contains
     * - cluster Switch (feature flags: LS) exposing the state and events of the right button
     * - cluster Descriptor with its TagList containing two tags: Position.Right and Number.Two
     * If this device were to have labeling on the buttons like an "up" and "down" icon, the TagList would have a third
     * tag (from the Switches Namespace) with values Switches.Up and Switches.Down respectively.
     * Example 2: a device with four push buttons (mounted in a square), each labeled with an icon for a certain scene
     * setting, which has four endpoints (21,22,23,24) for the switch-related functionality
     * - endpoint 21 has device type Generic Switch and contains
     * - cluster Switch (feature flags: MS) exposing the events of the top-left button
     * - cluster Descriptor with its TagList containing four tags: Position.Top, Position.Left, Number.One and
     * (Tag=Switches.Custom, Label="watch tv")
     * - This last tag is a Switches.Custom tag accompanied with a label (the other three tags do not need a Label
     * field).
     * - endpoint 22 has device type Generic Switch and contains
     * - cluster Switch (feature flags: MS) exposing the events of the top-right button
     * - cluster Descriptor with its TagList containing four tags: Position.Top, Position.Right, Number.Two and
     * (Tag=Switches.Custom, Label="dinner")
     * - endpoint 23 has device type Generic Switch and contains
     * - cluster Switch (feature flags: MS) exposing the events of the bottom-left button
     * - cluster Descriptor with its TagList containing four tags: Position.Bottom, Position.Left, Number.Three and
     * (Tag=Switches.Custom, Label="reading")
     * - endpoint 24 has device type Generic Switch and contains
     * - cluster Switch (feature flags: MS) exposing the events of the bottom-right button
     * - cluster Descriptor with its TagList containing four tags: Position.Bottom, Position.Right, Number.Four and
     * (Tag=Switches.Custom, Label="nightlight")
     * ### Relation with other Switch device types (informative)
     * The Generic Switch device type and the On/Off Light Switch device type both convey information about interactions
     * with a switch to another device.
     * - The On/Off Light Switch will send On/Off/Toggle commands from its On/Off (client) cluster to a device
     * implementing the On/Off (server) cluster to control the on/off functionality of that device. An On/Off Light
     * Switch device can also implement Groups and Scenes Management clusters and thus send group and scene commands.
     * Basically, it is targeted at directly sending control commands to other devices. The binding table is used to
     * tell the device where to send the commands.
     * - The Generic Switch device type will send updates of attributes (for Latching Switch only) and events to
     * subscribed parties which implement the Switch client cluster, as indications of interaction with the switch -
     * leaving the interpretation (e.g. which device should be actuated because of the interaction) to the subscribed
     * party. So it can be compared to a sensor-type device. This allows a more comprehensive controller to combine the
     * information from the switch with other inputs or information sources (e.g. time of day, user presence) to
     * determine which control commands (e.g. on/off, scene recall, attribute change) are sent to other devices in the
     * network.
     * A device manufacturer may implement both device types on the same switch device, to allow it to be used for both
     * types of control, as in this example for a rocker switch which implements:
     * - endpoint 31 with device type On/Off Light Switch which contains
     * - (client) cluster On/Off exposing the On/Off/Toggle commands
     * - endpoint 32 with device type Generic Switch which contains
     * - (server) cluster Switch (feature flags: LS) exposing the state and events of the switch
     * When this device is used in a particular setup, binding tables and subscriptions can be used to determine how it
     * is used:
     * - used as an On/Off Light Switch (no subscriptions to endpoint 32)
     * - used as a Generic Switch (no bindings on endpoint 31)
     * - used as both at the same time. In this case, an interaction with the switch would result in an On/Off/Toggle
     * command being sent to devices listed in the binding table of endpoint 31, as well as attribute update and events
     * being sent towards devices having a subscription with endpoint 32.
     **/
    public static final Integer GENERIC_SWITCH = 15;
    /**
     * A Heat Pump device is a device that uses electrical energy to heat either spaces or water tanks using ground,
     * water or air as the heat source. These typically can heat the air or can pump water via central heating radiators
     * or underfloor heating systems. It is typical to also heat hot water and store the heat in a hot water tank.
     * Note that the Water Heater device type can also be heated by a heat pump and has similar requirements, but that
     * cannot be used for space heating.
     * ### Heat Pump Architecture
     * A Heat Pump device is always defined via endpoint composition. See Section 14.5.6, "Device Type Requirements" for
     * more details.
     * The Heat Pump device may contain Temperature Sensors for example to measure the flow and return temperatures of
     * the water it is providing to the premises heating system.
     * The Heat Pump device may also include Thermostats located in the rooms that are being heated by it, which in turn
     * may also include Temperature Sensor clusters which can report the temperatures in those rooms. These Thermostats
     * may be included as servers within Thermostat devices within the Heat Pump device itself, or may be separate
     * third-party Thermostat devices for which the Heat Pump has a client to use them.
     * An example of a Heat Pump device is illustrated below.
     * ### Device Type Requirements
     * A Heat Pump device shall be composed of at least one endpoint with device types as defined by the conformance
     * below. There may be more endpoints with additional instances of these device types or additional device types
     * existing in the Heat Pump device.
     * The Heat Pump device shall include either one or more Thermostat devices, or include a Thermostat client.
     * #### Element Requirements on Component Device Types
     * If a Heat Pump device supports two or three phase power input then it may include two or three additional
     * endpoints, each including an Electrical Sensor Device Type as child elements. For each such child endpoint it
     * shall include a semantic tag from the Electrical Measurement Namespace in the TagList attribute of the Descriptor
     * cluster to describe the endpoint for the relevant Electrical Power Measurement and Electrical Energy Measurement
     * clusters indicating the relevant AC phase that is being measured.
     * The Electrical Energy Measurement and Electrical Power Measurement clusters of the mandatory Electrical Sensor
     * device shall measure the energy and power of the Heat Pump device at the AC grid connection point.
     * The Electrical Power Measurement clusters of the mandatory Electrical Sensor device shall support the
     * PolyPhasePower feature if the Heat Pump device is connected via polyphase wiring.
     * The Electrical Energy Measurement cluster of the mandatory Electrical Sensor device SHOULD support the
     * ImportedEnergy and CumulativeEnergy features.
     * Any Temperature Sensors using non-standard Namespaces for their Tags shall include Label(s) in the Descriptor
     * clusters of their endpoints to identify the temperature being measured.
     * Any Thermostat shall include a semantic tag from a Common Namespace, or a Manufacturer defined Tag and Label, in
     * the TagList attribute of the Descriptor cluster to describe the endpoint for the relevant Thermostat clusters,
     * indicating the relevant device (or its connected port) that is being measured. It shall also include a User Label
     * cluster to allow an installer to add identifying information for the rooms or spaces where the Thermostat
     * measurement point is located.
     **/
    public static final Integer HEAT_PUMP = 777;
    /**
     * A humidity sensor (in most cases a Relative humidity sensor) reports humidity measurements.
     **/
    public static final Integer HUMIDITY_SENSOR = 775;
    /**
     * An Intercom is a device which provides two-way on demand communication facilities between devices.
     * Examples include but are not limited to:
     * - Room to room systems in a house
     * - Entry door to individual units in a multi-tenant building
     * ### Device Type Requirements
     * An Intercom shall be composed of at least one endpoint with the Generic Switch device type as defined by the
     * conformance below. There may be more endpoints with other device types existing in the Intercom. The Generic
     * Switch shall model the mechanism used by the Intercom to trigger an alert of the desired connected party.
     * All devices used in compositions shall adhere to the disambiguation and superset requirements of the System
     * Model.
     * ### Cluster Requirements
     * An Audio connection may be established with an instance of an Intercom in one of two ways:
     * - via WebRTC, with the Intercom acting as a WebRTC Transport Requestor Client.
     * - via WebRTC, with a Controller acting as the WebRTC Transport Requestor and the Intercom, in this instance,
     * acting as a WebRTC Transport Provider Server. In this case, the Controller may trigger the establishment of Audio
     * through knowledge that there is user intent via subscriptions to the attributes of the Generic Switch, or other
     * means.
     **/
    public static final Integer INTERCOM = 320;
    /**
     * This defines conformance to the Irrigation System device type. An irrigation system is used to control a group of
     * irrigation zones to water landscape. Irrigation systems are also commonly referred to as "Sprinkler Controllers"
     * since they are often used in residential and commercial settings to control and schedule in-ground sprinkler
     * systems for lawns. A physical irrigation system typically has a set of electrical terminals to which in-ground
     * water valves are connected so that the system can actuate them.
     * ### Irrigation System Architecture
     * An irrigation system is always defined via endpoint composition. Irrigation system manufacturers determine how
     * many watering "zone" terminals are present on the physical device. Each zone is represented by a disambiguated
     * Water Valve endpoint:
     * ### Device Type Requirements
     * An irrigation system shall be composed of at least one endpoint with the Water Valve device type. Any instance of
     * the Valve Configuration and Control Cluster on an endpoint is scoped to the valve on that endpoint and not the
     * whole node.
     * If more than one instance of the Water Valve device type is present, each instance shall include semantic tags
     * from the common namespaces in the TagList attribute of the Descriptor cluster to disambiguate which watering zone
     * on the device each valve endpoint represents.
     * Some irrigation systems include a master valve installed at the main water supply line. When a master valve is
     * present, the physical system is responsible for opening it first when receiving a command to open a downstream
     * watering valve. In addition, the physical system is responsible for closing the master valve when the last open
     * watering valve is closed. Since the master valve is orchestrated by the device, it is not represented as a Water
     * Valve endpoint.
     * ### Cluster Requirements
     * #### Identify Cluster
     * This cluster is used to identify the entire irrigation system device.
     * #### Operational State Cluster
     * This cluster, if present, is used to denote the current state of the irrigation system. An irrigation system may
     * report the general operational states "Running" when at least one of the composed water valves is open for any
     * reason, and "Stopped" when all water valves are closed. In addition, the system may support operational state
     * commands "Pause" and "Resume" as well as the operational state "Paused" if it supports temporarily pausing a
     * water valve that is currently open for a known duration. In this case, it SHOULD report the CountdownTime
     * attribute to denote the remaining open duration of a currently open valve.
     * #### Flow Measurement Cluster
     * This cluster, if present, is used to measure the net flow through the irrigation system. When present, the
     * cluster shall report the total flow through the irrigation system and not any individual valve.
     * If present, the flow measurement client cluster is used via binding to measure flow from external flow sensors.
     **/
    public static final Integer IRRIGATION_SYSTEM = 64;
    /**
     * A Joint Fabric Administrator device provides capabilities to manage the Joint Fabric Datastore and issue an ICAC
     * signed by the Joint Fabric Anchor Root CA.
     * A client wanting to access the capabilities of the Joint Fabric Administrator may use the Joint Commissioning
     * Method (as specified in the Matter core specification) to be commissioned onto the Joint Fabric. Once
     * commissioned, a client may access the capabilities of the Joint Fabric Administrator.
     * ### Joint Fabric Architecture
     * See the Joint Fabric section of the Multiple Fabrics chapter in the Matter core specification for more
     * information on the Joint Fabric architecture.
     **/
    public static final Integer JOINT_FABRIC_ADMINISTRATOR = 304;
    /**
     * A Laundry Dryer represents a device that is capable of drying laundry items.
     * ### Cluster Restrictions
     * #### Temperature Control Cluster (Server) Clarifications
     * Given that different markets have different customary methods of providing temperature settings (e.g. North
     * America often prefers levels, whereas many other markets provide temperatures in °C), it is recommended that when
     * the Temperature Control cluster is present, the TemperatureLevel or TemperatureNumber feature of that cluster is
     * chosen to follow the most widely applied convention for the market where the product is sold.
     * #### On/Off Cluster (Server) Clarifications
     * The actions carried out by a Laundry Dryer device on receipt of specific commands are shown below. As indicated
     * in the Element Requirements section below, the DF (Dead Front) feature is required for the On/Off cluster in this
     * device type. See the "DeadFrontBehavior feature" section in the On/Off cluster description for detailed
     * requirements. The "dead front" state is linked to the OnOff attribute in the On/Off cluster having the value
     * False. Thus, the Off command of the On/Off cluster shall move the device into the "dead front" state, the On
     * command of the On/Off cluster shall bring the device out of the "dead front" state, and the device shall adhere
     * with the associated requirements on subscription handling and event reporting.
     * #### Best Effort Attribute Values in "Dead Front" State
     * When in "dead front", should the operational values of the cluster attributes not be available or accessible, the
     * following are the recommended best effort values for per cluster attributes when responding to a new subscription
     * request or a read request. Note that some of these attributes may be missing for the clusters not implemented on
     * the endpoint due to optionality.
     **/
    public static final Integer LAUNDRY_DRYER = 124;
    /**
     * A Laundry Washer represents a device that is capable of laundering consumer items. Any laundry washer product may
     * utilize this device type.
     * A Laundry Washer shall be composed of at least one endpoint with the Laundry Washer device type.
     * ### Cluster Restrictions
     * #### Temperature Control Cluster (Server) Clarifications
     * Given that different markets have different customary methods of providing temperature settings (e.g. North
     * America often prefers levels, whereas many other markets provide temperatures in °C), it is recommended that when
     * the Temperature Control cluster is present, the TemperatureLevel or TemperatureNumber feature of that cluster is
     * chosen to follow the most widely applied convention for the market where the product is sold.
     * #### On/Off Cluster (Server) Clarifications
     * As indicated in the Element Requirements section below, the DF (Dead Front) feature is required for the On/Off
     * cluster in this device type. See the "DeadFrontBehavior feature" section in the On/Off cluster description for
     * detailed requirements. The "dead front" state is linked to the OnOff attribute in the On/Off cluster having the
     * value False. Thus, the Off command of the On/Off cluster shall move the device into the "dead front" state, the
     * On command of the On/Off cluster shall bring the device out of the "dead front" state, and the device shall
     * adhere with the associated requirements on subscription handling and event reporting.
     * #### Best Effort Attribute Values in "Dead Front" State
     * When in "dead front", should the operational values of the cluster attributes not be available or accessible, the
     * following are the recommended best effort values for per cluster attributes when responding to a new subscription
     * request or a read request. Attributes not listed have no change in their defined or expected values.
     **/
    public static final Integer LAUNDRY_WASHER = 115;
    /**
     * A Light Sensor device is a measurement and sensing device that is capable of measuring and reporting the
     * intensity of light (illuminance) to which the sensor is being subjected.
     **/
    public static final Integer LIGHT_SENSOR = 262;
    /**
     * A Meter Reference Point device provides details about tariffs and metering.
     * ### Device Type Requirements
     * A Meter Reference Point is composed of other endpoints with device types listed in this table, subject to the
     * conformance column of the table. Additional device types not listed in this table may also be included in device
     * compositions.
     * ### Meter Reference Point Topology
     * #### Basic Electrical Meter Reference Point
     * A basic electrical Meter Reference Point device type has a simple import tariff endpoint for grid power, tagged
     * as Grid, Import, AC, and Current.
     * Optionally, this endpoint may have a child endpoint representing an upcoming tariff, if available, tagged as
     * Grid, Import, AC, and Upcoming.
     * Optionally, the tariff endpoint may have child endpoints representing tariffs for individual phases of a
     * polyphase power supply.
     * #### Separate EV Rate
     * Building on the basic topology, a Meter Reference Point device type which has a separate rate for EV charging
     * would add a second endpoint, tagged as EV, Import, AC, and Current.
     * Optionally, this endpoint may have a child endpoint representing an upcoming EV tariff, if available, tagged as
     * EV, Import, AC, and Upcoming.
     * #### Export Rate
     * Similarly, a Meter Reference Point device type which has a separate rate for exported electrical energy would add
     * a second endpoint, tagged as Grid, Export, AC, and Current.
     * Optionally, this endpoint may have a child endpoint representing an upcoming export tariff, if available, tagged
     * as Grid, Export, AC, and Upcoming.
     * #### Combination of EV and Export
     * The above topologies can be composed to represent various combinations of tariffs. In this example, a tariff has
     * separate rates for an EV and for exporting energy to the grid.
     * #### Inclusion of Metering Data
     * Instead of Electrical Energy Tariff endpoints, a Meter Reference Point may use endpoints with the Electrical
     * Meter device type to represent tariffs with associated metering data.
     **/
    public static final Integer METER_REFERENCE_POINT = 1298;
    /**
     * This defines conformance to the Microwave Oven device type.
     * A Microwave Oven is a device with the primary function of heating foods and beverages using a magnetron.
     * ### Microwave Oven Architecture
     * A Microwave Oven is a device which at a minimum is capable of being started and stopped and of setting a power
     * level.
     * A Microwave Oven may also support additional capabilities via endpoint composition. See Section 13.11.5, "Device
     * Type Requirements" for typical device types.
     * The following diagram shows an example Microwave Oven consisting of a parent endpoint that is the Microwave Oven
     * device type and a child endpoint providing additional capabilities.
     * A microwave oven placed above a thermal oven or cooktop/hob may also include a light for illuminating the cooking
     * surface of the thermal oven or cooktop/hob and an exhaust fan for removing cooking odors.
     * ### Device Type Requirements
     * When a light is included as part of a composed device type, it is intended to be used as surface light when the
     * microwave oven is installed above a range in an "over the range" configuration rather than the internal light of
     * the microwave oven cavity.
     * ### Cluster Requirements
     * When the Fan Control cluster is supported on an endpoint of this device type, it is intended to be used as a
     * ventilation fan when the microwave oven is installed above a range in an "over the range" configuration rather
     * than the internal fan of the microwave oven cavity.
     * ### Cluster Usage
     * This section describes how to control and monitor the operation of a Microwave Oven device. This information is
     * meant to clarify how the data dependencies within the device type's cluster composition are to be used.
     * Note that the device operations may also be the result of, or affected by, out-of-band actions such as physical
     * button presses on the device, internally scheduled events, vendor application requests, commands invoked via
     * other fabrics, internal device timeouts, etc. For example, a user may pause the oven during operation by opening
     * the door to check on the food.
     * #### Starting the Oven
     * The oven operational attributes are set by sending the SetCookingParameters command of the Microwave Oven Control
     * cluster with the values as intended by the user via a client. Oven operation can be started by sending the Start
     * command via the Operational State cluster or one of its derivatives, if supported, or within the
     * SetCookingParameters command via the StartAfterSetting attribute, if supported.
     * Upon setting the CookTime attribute via the SetCookingParameters command, the CountdownTime attribute of the
     * Operational State cluster or one of its derivatives , if supported, is set to the same value as the CookTime
     * attribute.
     * Once oven operation is started, the values previously sent by the SetCookingParameters command are used to
     * control the oven operation and the CountdownTime attribute of the Operational State cluster or one of its
     * derivatives begins counting down.
     * #### During Operation
     * While the oven is in the Running state, the CountdownTime attribute of the Operational State cluster or one of
     * its derivatives counts down and the CookTime attribute of the Microwave Oven Control cluster remains fixed.
     * #### Stopping the Oven
     * Oven operation will end when either the Stop command of the Operational State cluster or one of its derivatives,
     * if supported, is sent, the CountdownTime value reaches zero, or the oven is stopped via an out-of-band method.
     * It is recommended that when the oven enters the Stopped state of the Operational State cluster or one of its
     * derived clusters, the attribute values of the Microwave Oven Control cluster be set to their default values by
     * the server.
     * #### Adding More Time
     * When time is added to the CookTime attribute using the AddMoreTime command of the Microwave Oven Control cluster,
     * the same amount of time is also added to the CountdownTime attribute of the Operational State cluster or one of
     * its derivatives. See the CookTime attribute constraints and AddMoreTime command for more details.
     **/
    public static final Integer MICROWAVE_OVEN = 121;
    /**
     * This defines conformance to the Mode Select device type.
     **/
    public static final Integer MODE_SELECT = 39;
    /**
     * A Mounted Dimmable Load Control is a fixed device that provides power to a load connected to it, and is capable
     * of being switched on or off and have its level adjusted. The Mounted Dimmable Load Control is typically used to
     * control a conventional non-communicating light through its mains connection using phase cutting.
     * This device type is intended for any wall-mounted or hardwired dimmer-capable load controller, while Dimmable
     * Plug-In Unit is intended only for dimmer-capable smart plugs that are not permanently connected, and which can be
     * unplugged from their power source.
     * > [!NOTE]
     * > NOTE: Since this device type was added in Matter 1.4, for endpoints using this device type it is recommended to
     * add the subset device type Dimmable Plug-In Unit to the DeviceTypeList of the Descriptor cluster on the same
     * endpoint for backward compatibility with existing clients. See Dimmable Plug-In Unit client guidance for
     * additional information, regarding the inclusion of these two device types.
     **/
    public static final Integer MOUNTED_DIMMABLE_LOAD_CONTROL = 272;
    /**
     * A Mounted On/Off Control is a fixed device that provides power to another device or power circuit that is
     * connected to it, and is capable of switching that provided power on or off.
     * This device type is intended for any wall-mounted or hardwired load controller, while On/Off Plug-in Unit is
     * intended only for smart plugs and other power switching devices that are not permanently connected, and which can
     * be unplugged from their power source.
     * > [!NOTE]
     * > NOTE: Since this device type was added in Matter 1.4, for endpoints using this device type it is recommended to
     * add the subset device type On/Off Plug-in Unit to the DeviceTypeList of the Descriptor cluster on the same
     * endpoint for backward compatibility with existing clients. See On/Off Plug-in Unit client guidance for additional
     * information, regarding the inclusion of these two device types.
     * ### Cluster Requirements
     * The inclusion of the Level Control cluster on this device is recommended to provide a consistent user experience
     * when the device is grouped with additional dimmable lights and the “with on/off” commands are used. For this
     * device, since its only states are on or off, if the Level Control cluster is implemented, it shall NOT have any
     * effect on the actual light level except for those commands that cause an on/off state change, that is, the “with
     * on/off” commands. In addition, if the Level Control cluster is implemented, the device shall accept and process
     * Level Control cluster commands, adjusting the value of the CurrentLevel attribute accordingly and, where
     * necessary, adjusting the On/Off cluster OnOff attribute.
     * ### Element Requirements
     * As the TriggerEffect command of the Identify cluster and the OffWithEffect command of the On/Off cluster specify
     * light effects that require dimming of the light output, and such is not possible on this device type, the
     * specified light effects may be replaced by pure on/off light effects.
     **/
    public static final Integer MOUNTED_ON_OFF_CONTROL = 271;
    /**
     * A Network Infrastructure Manager provides interfaces that allow for the management of the Wi-Fi, Thread, and
     * Ethernet networks underlying a Matter deployment, realizing the Star Network Topology described in MatterCore.
     * Examples of physical devices that implement the Matter Network Infrastructure Manager device type include Wi-Fi
     * gateway routers.
     * Relevant hardware and software requirements for Network Infrastructure Manager devices are defined in Section
     * 15.3.6, "Other Requirements" and within the clusters mandated by this device type.
     * A Network Infrastructure Manager device may be managed by a service associated with the device vendor, for
     * example, an Internet Service Provider. Sometimes this managing service will have policies that require the use of
     * the Managed Device feature of the Access Control Cluster (see Section 15.3.4.1, "ManagedAclAllowed Condition").
     * Consequently, Commissioners of this device type should be aware of this feature and its use.
     * ### Other Requirements
     * The Network Infrastructure Manager shall implement a bridged Wi-Fi / Ethernet hub network, enabling IPv6
     * connectivity between Matter Nodes across transports.
     * The Network Infrastructure Manager shall support IP communication with at least 300 Matter devices on this
     * network. If the Network Infrastructure Manager operates a DHCPv4 server, then it SHOULD be configured by default
     * with a subnet mask and DHCP pool size that allow for at least 300 devices.
     * > [!NOTE]
     * > NOTE: This recommendation is meant to avoid IPv4 address exhaustion if a large number of devices request IPv4
     * addresses e.g. for non-Matter traffic.
     * #### Ethernet Requirements
     * The device shall provide an Ethernet LAN interface that is part of the bridged hub network.
     * The Root Node endpoint of the device may include a Network Commissioning cluster associated with this Ethernet
     * interface.
     * #### Wi-Fi Requirements
     * The device shall support the operation of an IEEE 802.11 Wi-Fi network (ESS) and provide access to the SSID and
     * credentials of this network via the Wi-Fi Network Management cluster. The mechanisms by which this network is
     * configured are outside the scope of this specification. The device shall support concurrently operating BSSs for
     * this ESS in the 2.4 GHz and 5 GHz frequency bands; it may support operating additional BSSs for this ESS in other
     * frequency bands such as 6 GHz or sub-1 GHz. All BSSs in this ESS shall be part of the bridged hub network, i.e.
     * bridged to each other and to the Ethernet interface.
     * The device may support operating additional Wi-Fi networks (e.g. a "guest network"); the requirements above do
     * not apply to any such additional networks.
     * The device SHOULD NOT include any Network Commissioning cluster instances associated with the Wi-Fi Access Point
     * interface.
     * The device shall be certified by the Wi-Fi Alliance in the Access Point role for Wi-Fi 6 or above. It shall
     * additionally be certified in the Access Point role for Wi-Fi 6E if it supports operating in the 6 GHz band, and
     * for Wi-Fi HaLow if it supports operating in the sub-1 GHz band.
     * To support the efficient operation of the network generally, and for low-power stations in particular, the
     * Network Infrastructure Manager shall support, and upon (or before) Matter commissioning shall enable, the
     * following Wi-Fi features:
     * - Extended Sleep Time with a sleep time support up to at least 60 minutes, which includes the following IEEE
     * 802.11 features:
     * - Basic Service Set (BSS) Max Idle Period
     * - dot11BSSMaxIdlePeriodIndicationByNonAPSTA
     * - IPv6 Proxy Neighbor Discovery Protocol (NDP) including IPv6 duplicate address detection
     * - IPv4 Proxy Address Resolution Protocol (ARP)
     * - 802.11 WNM Sleep Mode with GTK/IGTK/BIGTK update support
     * The device shall support at least 100 simultaneous Wi-Fi associations - irrespective of the distribution of the
     * Matter Wi-Fi devices over the supported bands; all Matter Wi-Fi devices could be on the same band. This includes
     * associations of low-power Matter Wi-Fi devices that are asleep for a long time.
     * > [!NOTE]
     * > NOTE: For the case of in-field upgrades of pre-Matter Wi-Fi access points, an exemption (simultaneous
     * association requirement reduced to 64) can be requested when applying for Matter certification, e.g. in case of
     * Wi-Fi chipset limitations (see the Alliance Certification Policy).
     * #### Thread Requirements
     * The Network Infrastructure Manager device shall be certified by the Thread Group as Built on Thread: Border
     * Router based on Thread 1.4.0 or above.
     * The device shall implement a Thread Border Router as described by the Thread specification, and provide
     * connectivity between the Thread network and the Wi-Fi / Ethernet hub network. The Thread Interface associated
     * with the Border Router shall be exposed via the Thread Border Router Management cluster.
     * The Thread Network Diagnostics cluster included on the endpoint shall be the instance corresponding to the Thread
     * Interface associated with the Border Router functionality.
     * The device SHOULD NOT include any Network Commissioning cluster instances associated with the Thread Border
     * Router.
     * The Network Infrastructure Manager device shall support working as a Thread Parent for a minimum of 64 Thread
     * Children simultaneously in any combination of Children End Device types. The Network Infrastructure Manager
     * device shall support operating as a Thread Border Router in any Thread Network with up to 150 Thread nodes.
     * #### Discovery and Commissioning
     * A Network Infrastructure Manager device SHOULD implement Extended Discovery in order to be discoverable by
     * entities on the local IP network, even when not in Commissioning Mode, and SHOULD populate the optional device
     * type subtype (e.g., _T144) to allow for filtering of discovery results to find only Nodes that match the Network
     * Infrastructure Manager device type (see Commissioning Subtypes).
     * A Network Infrastructure Manager SHOULD populate the following DNS-SD TXT record key/value pairs in the
     * Commissionable Node Discovery response: Commissioning Pairing Hint, and Commissioning Pairing Instruction so that
     * the Commissioner can guide the user through the steps needed to put the Commissionee into Commissioning Mode. If
     * the Network Infrastructure Manager provides its own app or website which includes a UX for putting the device
     * into Commissioning Mode, then the device SHOULD populate the Commissioning VID/PID key/value pair and SHOULD set
     * bit 1 of the Pairing Hint (Device Manufacturer URL), so that the Commissioner can utilize the URL specified in
     * the CommissioningCustomFlowUrl of the DeviceModel schema entry indexed by the Vendor ID and Product ID in the
     * Distributed Compliance Ledger and utilize flows described in Custom Commissioning Flow to redirect the user to a
     * custom app or website specified by the device vendor, and receive the user back following the callback flow which
     * contains the onboarding payload. This flow is described in detail in the Initiating Commissioning section of the
     * Matter Core specification, under the User Journey titled User-Initiated Beacon Detection, Already Commissioned
     * Device.
     **/
    public static final Integer NETWORK_INFRASTRUCTURE_MANAGER = 144;
    /**
     * An Occupancy Sensor is a measurement and sensing device that is capable of measuring and reporting the occupancy
     * state in a designated area.
     * ### Cluster Requirements
     * #### Identify Cluster
     * This is used to identify the endpoint.
     * #### Boolean State Configuration Cluster
     * This is used to configure the sensor/detector (e.g. sensitivity) and is for this device type linked to the
     * configuration of the Occupancy Sensing cluster on the same endpoint.
     * #### Occupancy Sensing Cluster
     * This is used to indicate occupancy as well as the type of occupancy sensor used for detection and configuring the
     * delays related to the occupied and unoccupied transitions.
     * ### Multi-modality sensors
     * The Occupancy Sensing cluster defines multiple modalities that can be employed to sense occupancy. A device
     * implementing multiple such modalities (exposed in the feature flags) can be implemented in two ways:
     * - A single endpoint with an Occupancy Sensing cluster which has two or more of these feature bits set to 1.
     * - This requires reporting the combination the sensing results as a single bit in the Occupancy attribute (and the
     * OccupancyChanged event, when supported), with a single set of timing parameters applied.
     * - Sensitivity setting (via a Boolean State Configuration cluster on the same endpoint) applies to all the sensing
     * modalities together via a manufacturer-specific mapping.
     * - Multiple endpoints each hosting an Occupancy Sensing cluster (each with one feature bit set):
     * - The sensing result of each modality is reported separately in the Occupancy attribute (and the OccupancyChanged
     * event, when supported) of each endpoint, governed by the set of timing parameters provided in the cluster on that
     * endpoint.
     * - This implies some of these attributes can have a different values than their counterparts on other endpoints
     * and that a client may have to combine these values if it wants to derive a single value.
     * - Each modality can be provided with an independent sensitivity setting via a Boolean State Configuration cluster
     * located on one or more of the endpoints.
     **/
    public static final Integer OCCUPANCY_SENSOR = 263;
    /**
     * The On/Off Light is a lighting device that is capable of being switched on or off by means of a bound controller
     * device such as an On/Off Light Switch or a Dimmer Switch. In addition, an on/off light is also capable of being
     * switched by means of a bound occupancy sensor.
     * ### Cluster Requirements
     * The inclusion of the Level Control cluster on this device is recommended to provide a consistent user experience
     * when the device is grouped with additional dimmable lights and the “with on/off” commands are used. For this
     * device, since its only states are on or off, if the Level Control cluster is implemented, it shall NOT have any
     * effect on the actual light level except for those commands that cause an on/off state change, that is, the “with
     * on/off” commands. In addition, if the Level Control cluster is implemented, the device shall accept and process
     * Level Control cluster commands, adjusting the value of the CurrentLevel attribute accordingly and, where
     * necessary, adjusting the On/Off cluster OnOff attribute.
     * ### Element Requirements
     * As the TriggerEffect command of the Identify cluster and the OffWithEffect command of the On/Off cluster specify
     * light effects that require dimming of the light output, and such is not possible on this device type, the
     * specified light effects may be replaced by pure on/off light effects.
     **/
    public static final Integer ON_OFF_LIGHT = 256;
    /**
     * An On/Off Light Switch is a controller device that, when bound to a lighting device such as an On/Off Light, is
     * capable of being used to switch the device on or off.
     **/
    public static final Integer ON_OFF_LIGHT_SWITCH = 259;
    /**
     * An On/Off Plug-in Unit is a device that provides power to another device that is plugged into it, and is capable
     * of switching that provided power on or off.
     * The Mounted On/Off Control (added in Matter 1.4) has identical cluster requirements as the On/Off Plug-In Unit,
     * and is marked as superset of this device type (since Matter 1.4.2). For devices intended to be mounted
     * permanently, the Mounted On/Off Control device type shall be used, with the On/Off Plug-In Unit device type
     * optionally added in the DeviceTypeList of the Descriptor cluster in addition to the On/Off Plug-In Unit device
     * type (see Mounted On/Off Control server guidance section).
     * Before Matter 1.4, mounted units typically used the On/Off Plug-In Unit device type. Clients can encounter
     * devices which were made before or after these specification updates. Therefore, clients SHOULD use the following
     * heuristic to distinguish the type of physical device based on the device type revision found on an endpoint ("--"
     * means the device type is not listed).
     * ### Cluster Requirements
     * The inclusion of the Level Control cluster on this device is recommended to provide a consistent user experience
     * when the device is grouped with additional dimmable lights and the “with on/off” commands are used. For this
     * device, since its only states are on or off, if the Level Control cluster is implemented, it shall NOT have any
     * effect on the actual light level except for those commands that cause an on/off state change, that is, the “with
     * on/off” commands. In addition, if the Level Control cluster is implemented, the device shall accept and process
     * Level Control cluster commands, adjusting the value of the CurrentLevel attribute accordingly and, where
     * necessary, adjusting the On/Off cluster OnOff attribute.
     * ### Element Requirements
     * As the TriggerEffect command of the Identify cluster and the OffWithEffect command of the On/Off cluster specify
     * light effects that require dimming of the light output, and such is not possible on this device type, the
     * specified light effects may be replaced by pure on/off light effects.
     **/
    public static final Integer ON_OFF_PLUG_IN_UNIT = 266;
    /**
     * An On/Off Sensor is a measurement and sensing device that, when bound to a lighting device such as a Dimmable
     * Light, is capable of being used to switch the device on or off.
     **/
    public static final Integer ON_OFF_SENSOR = 2128;
    /**
     * An OTA Provider is a node that is capable of providing an OTA software update to other nodes on the same fabric.
     * ### Cluster Requirements
     * A node shall only ever have, at most, one instance of the OTA Provider's required clusters.
     **/
    public static final Integer OTA_PROVIDER = 20;
    /**
     * An OTA Requestor is a device that is capable of receiving an OTA software update.
     **/
    public static final Integer OTA_REQUESTOR = 18;
    /**
     * An oven represents a device that contains one or more cabinets, and optionally a single cooktop, that are all
     * capable of heating food. Examples of consumer products implementing this device type include ovens, wall ovens,
     * convection ovens, etc.
     * ### Oven Architecture
     * An oven is always defined via endpoint composition. See Section 13.9.6, "Device Type Requirements" for more
     * details.
     * An example of an oven with two cabinets (one above the other) and a cooktop (with two cook surfaces) is
     * illustrated below.
     * ### Device Type Requirements
     * An Oven shall be composed of at least one endpoint with Temperature Controlled Cabinet device type. There may be
     * more endpoints with other device types existing in the Oven. Note that any instance of the TemperatureControl
     * cluster on an endpoint is scoped to the device type on that endpoint, and not the whole node.
     * If the Oven contains more than one instance of a Temperature Controlled Cabinet, those instances shall include a
     * semantic tag in the TagList attribute of the Descriptor cluster to disambiguate the cabinet, e.g., "Top" or
     * "Bottom". Such a semantic tag shall be from the defined Common Position namespaces.
     * Regional restrictions and safety regulations may dictate which aspects of a Temperature Controlled Cabinet may be
     * remotely accessible. In such cases, clusters exposed by an instance of a Temperature Controlled Cabinet may have
     * limitations on what commands are supported or what attributes are mutable.
     **/
    public static final Integer OVEN = 123;
    /**
     * A Power Source device type provides information about the source of power.
     * ### Cluster Requirements
     * This device shall support the clusters listed in the following table.
     **/
    public static final Integer POWER_SOURCE = 17;
    /**
     * A Pressure Sensor device measures and reports the pressure of a fluid.
     **/
    public static final Integer PRESSURE_SENSOR = 773;
    /**
     * A Pump Controller device is capable of configuring and controlling a Pump device.
     **/
    public static final Integer PUMP_CONTROLLER = 772;
    /**
     * A Pump device is a pump that may have variable speed. It may have optional built-in sensors and a regulation
     * mechanism. It is typically used for pumping fluids like water.
     * ### Cluster Restrictions
     * #### On/Off Cluster (Server) Clarifications
     * The actions carried out by a Pump device on receipt of commands are shown in the following.
     * #### Level Control Cluster (Server) Clarifications
     * The Level Control cluster shall allow controlling the pump setpoints. However, the transition time is always
     * ignored.
     * The setpoint of the pump is a percentage related to the level according to the following table.
     **/
    public static final Integer PUMP = 771;
    /**
     * This defines conformance to the Rain Sensor device type.
     * ### Cluster Requirements
     * #### Identify Cluster
     * This is used to identify the endpoint.
     * #### Boolean State Cluster
     * This is used to indicate the state of the sensor/detector.
     * The state of the Boolean State cluster shall reflect the sensor detection using this scheme of:
     * #### Boolean State Configuration Cluster
     * This is used to configure the sensor/detector and is for this device type linked to the configuration of the
     * Boolean State cluster.
     **/
    public static final Integer RAIN_SENSOR = 68;
    /**
     * A refrigerator represents a device that contains one or more cabinets that are capable of chilling or freezing
     * food. Examples of consumer products that may make use of this device type include refrigerators, freezers, and
     * wine coolers.
     * ### Refrigerator Architecture
     * A Refrigerator is always defined via endpoint composition. See Section 13.2.6, "Device Type Requirements" for
     * more details.
     * A Refrigerator may include a semantic tag in the TagList attribute of the Descriptor cluster to describe the
     * primary function of the device, e.g., "Refrigerator" or "Freezer".
     * An example of a Refrigerator with multiple cabinets is illustrated below.
     * ### Device Type Requirements
     * A Refrigerator shall be composed of at least one endpoint with the Temperature Controlled Cabinet device type as
     * defined by the conformance below. There may be more endpoints with other device types existing in the
     * Refrigerator.
     * If the Refrigerator contains more than one instance of a Temperature Controlled Cabinet, those instances shall
     * include a semantic tag in the TagList attribute of the Descriptor cluster to disambiguate the cabinet, e.g.,
     * "freezer" or "refrigerator". Such a semantic tag shall be from either the defined Common or Refrigerator
     * namespaces.
     * ### Cluster Requirements
     * #### Activated Carbon Filter Monitoring Cluster
     * This cluster is used to represent the status of a water filter, if present on the device.
     **/
    public static final Integer REFRIGERATOR = 112;
    /**
     * This defines conformance for the Robotic Vacuum Cleaner device type.
     * ### Cluster Usage
     * This section describes how to control and monitor the operation of a Robotic Vacuum Cleaner device. This
     * information is meant to clarify how the data dependencies within the device type's cluster composition are to be
     * used.
     * Note that the device operations may also be the result of, or affected by, out-of-band actions such as robot
     * physical button presses, internally scheduled events, vendor application requests, commands sent from other
     * fabrics, internal device timeouts, etc. For example, a user may pause the robot during cleaning by using a Matter
     * client and then resume cleaning by using a physical button of the device, or a robot may stop cleaning after an
     * internal timeout occurs, and so forth.
     * The RVC Operational State cluster's OperationalState attribute shall be updated according to the state of the
     * device, and therefore it SHOULD be used for monitoring purposes. Note that while the robot is in a cleaning cycle
     * it may automatically seek the charger, recharge, and then resume cleaning.
     * The sections below describe various operational flows with preconditions and actions. The behavior in case the
     * preconditions are not met is described in the corresponding cluster descriptions.
     * #### Starting Cleaning
     * ##### Preconditions
     * If the DirectModeChange feature is not present, cleaning can only be started when the RVC Run Mode cluster's
     * CurrentMode attribute is set to a mode that has the Idle mode tag associated with it, and the RVC Operational
     * State cluster's OperationalState attribute is set to the Stopped, Paused, Docked or Charging state.
     * Note that if the RVC Clean Mode cluster is implemented, it determines the type of cleaning.
     * ##### Actions
     * To attempt starting a cleaning operation, the RVC Run Mode cluster can be sent a ChangeToMode command with the
     * NewMode field set to a mode that has the Cleaning mode tag associated with it.
     * #### Pausing Cleaning
     * ##### Preconditions
     * Cleaning can only be paused when the RVC Operational State cluster's OperationalState attribute is set to a
     * Pause-compatible state. See the Pause Compatibility table and the RVC Pause Compatibility Table.
     * Note that even if the Pause command is not implemented, the RVC Operational State cluster's OperationalState
     * attribute may report that the device is in the Paused state due to an out-of-band action, such as the user
     * pressing a physical button on the device.
     * ##### Actions
     * To attempt pausing a cleaning operation, the RVC Operational State cluster can be sent a Pause command.
     * #### Resuming Cleaning
     * ##### Preconditions
     * Cleaning can only be resumed if the RVC Operational State cluster's OperationalState attribute is set to a
     * Resume-compatible state (see Resume Compatibility table and the RVC Resume Compatibility table), and the RVC Run
     * Mode cluster's CurrentMode is set to a mode with the Cleaning mode tag.
     * Note that even if the Resume command is not implemented, the RVC Operational State cluster's OperationalState
     * attribute may indicate that the device transitioned from the Paused state to the Running state due to an
     * out-of-band action, such as the user pressing a physical button on the device.
     * ##### Actions
     * To attempt resuming a cleaning operation, the RVC Operational State cluster can be sent a Resume command.
     * #### Stopping Cleaning
     * ##### Preconditions
     * Stopping cleaning can only happen if the RVC Run Mode cluster's CurrentMode attribute is set to a mode that has
     * the Cleaning mode tag associated with it.
     * ##### Actions
     * To attempt stopping a cleaning operation, the RVC Run Mode cluster can be sent a ChangeToMode command with the
     * NewMode field set to a mode that has the Idle mode tag associated with it.
     * ##### Side Effects
     * Note that the device may seek the charger after successfully switching the RVC Run Mode cluster to an Idle mode.
     * The OperationalState attribute indicates whether the device is seeking the charger, stopped, charging, docked
     * etc.
     * #### Other Device Operations
     * The RVC Run Mode cluster's SupportedModes attribute list may include modes that have neither the Idle nor the
     * Cleaning mode tags, for example the Mapping mode tag.
     * Starting, pausing, resuming and stopping these other operations have similar preconditions, actions and side
     * effects as those described above for the cleaning operations.
     * #### Device Error Handling
     * When in an error condition, as indicated by the RVC Operational State cluster's OperationalState attribute,
     * out-of-band action will be required to clear that condition.
     * If an error occurs while the device operates, such as while cleaning or while mapping, the device may pause and
     * set the RVC Operational State cluster's OperationalState attribute to Error. If the operation can be resumed
     * after the error is cleared, the device shall set the RVC Operational State cluster's OperationalState attribute
     * to Paused and may be resumed either via a Resume command, if implemented, or by out-of-band actions, such as by
     * pressing the robot's physical buttons.
     * Note that certain errors may not pause or disable the device. For example, a dual-function device, that can both
     * vacuum and mop, may report a WaterTankEmpty error but may still be able to be used if it has a vacuum only
     * cleaning mode. Certain modes of the RVC Run Mode and the RVC Cleaning Mode clusters may become unavailable and
     * the ChangeToModeResponse commands' StatusCode shall be set to InvalidInMode, when attempting to switch to those
     * modes.
     **/
    public static final Integer ROBOTIC_VACUUM_CLEANER = 116;
    /**
     * This defines conformance to the Room Air Conditioner device type.
     * A Room Air Conditioner is a device with the primary function of controlling the air temperature in a single room.
     * ### Room Air Conditioner Architecture
     * A Room Air Conditioner is a device which at a minimum is capable of being turned on and off and of controlling
     * the temperature in the living space.
     * A Room Air Conditioner may also support additional capabilities via endpoint composition. See Section 13.3.5,
     * "Device Type Requirements" for typical device types.
     * The following diagram shows an example Room Air Conditioner consisting of a parent endpoint that is the Room Air
     * Conditioner device type and several child endpoints providing additional capabilities. Note that two of the child
     * endpoints are of the same device type, Temperature Sensor, which are being disambiguated via the requirements of
     * endpoint composition defined in the system model.
     * ### Device Type Requirements
     * A Room Air Conditioner may have zero or more of each device type listed in this table subject to the conformance
     * column of the table. All devices used in compositions shall adhere to the disambiguation requirements of the
     * System Model. Additional device types not listed in this table may also be included in device compositions.
     * ### Cluster Restrictions
     * #### On/Off Cluster (Server) Clarifications
     * As indicated in the Element Requirements section below, the DF (Dead Front) feature is required for the On/Off
     * cluster in this device type. See the "DeadFrontBehavior feature" section in the On/Off cluster description for
     * detailed requirements. The "dead front" state is linked to the OnOff attribute in the On/Off cluster having the
     * value False. Thus, the Off command of the On/Off cluster shall move the device into the "dead front" state, the
     * On command of the On/Off cluster shall bring the device out of the "dead front" state, and the device shall
     * adhere with the associated requirements on subscription handling and event reporting.
     * #### Best Effort Attribute Values in "Dead Front" State
     * When in "dead front", should the operational values of the cluster attributes not be available or accessible, the
     * following are the recommended best effort values for per cluster attributes when responding to a new subscription
     * request or a read request. Attributes not listed have no change in their defined or expected values.
     **/
    public static final Integer ROOM_AIR_CONDITIONER = 114;
    /**
     * This defines conformance for a root node endpoint (see System Model specification). This endpoint is akin to a
     * "read me first" endpoint that describes itself and the other endpoints that make up the node.
     * - Device types with Endpoint scope shall NOT be supported on the same endpoint as this device type.
     * - Clusters with an Application role shall NOT be supported on the same endpoint as this device type.
     * - Other device types with Node scope may be supported on the same endpoint as this device type.
     * ### Cluster Requirements
     * > [!NOTE]
     * > NOTE: The Network Diagnostics clusters present on the Root Node shall serve the primary network interface as
     * specified in the Network Commissioning cluster if it exists, or the out-of-band-configured networking interfaces.
     * ### Element Requirements
     * #### Access Control MNGD Conformance
     * The MNGD (Managed Device) feature of the Access Control Cluster on the device's Root Node endpoint is restricted
     * to devices that contain an Application Endpoint type that explicitly permits its use, such as the Network
     * Infrastructure Manager device type (Device Type ID 0x0090).
     * ### Endpoint Composition
     * A Root Node endpoint's Descriptor cluster PartsList attribute shall contain a list of all other endpoints on the
     * node, i.e. the full-family pattern defined in the System Model specification.
     **/
    public static final Integer ROOT_NODE = 22;
    /**
     * A Secondary Network Interface device provides an additional network interface supported by the Node,
     * supplementing the primary interface hosted by the Root Node endpoint.
     * A Node supporting multiple network interfaces shall include the primary interface on the Root Node endpoint,
     * along with secondary interfaces on other endpoints. The priorities of these network interfaces are determined by
     * the order of their endpoints, where interfaces with smaller endpoint numbers are higher priority.
     * ### Cluster Requirements
     * > [!NOTE]
     * > NOTE: The Network Diagnostics cluster present in this device type shall serve the secondary network interface
     * as specified in the Network Commissioning cluster.
     **/
    public static final Integer SECONDARY_NETWORK_INTERFACE = 25;
    /**
     * A Smoke CO Alarm device is capable of sensing smoke, carbon monoxide or both. It is capable of issuing a visual
     * and audible alert to indicate elevated concentration of smoke or carbon monoxide.
     * Smoke CO Alarms are capable of monitoring themselves and issuing visual and audible alerts for hardware faults,
     * critical low battery conditions, and end of service. Optionally, some of the audible alerts can be temporarily
     * silenced. Smoke CO Alarms are capable of performing a self-test which performs a diagnostic of the primary sensor
     * and issuing a cycle of the audible and visual life safety alarm indications.
     * Some smoke alarms may be capable of adjusting sensitivity. Smoke CO Alarm may have the ability to detect and
     * report humidity levels, temperature levels, and contamination levels.
     * ### Device Type Requirements
     * A Smoke CO Alarm device type shall support an instance of a Power Source device type on some endpoint. See the
     * Power Source cluster for more information.
     **/
    public static final Integer SMOKE_CO_ALARM = 118;
    /**
     * A Snapshot Camera device is a camera which can only support retrieving still images on-demand via the Capture
     * Snapshot command in the Camera AV Stream Management cluster.
     * ### Device Type Requirements
     * A Snapshot Camera may expose elements of its functionality through one or more additional device types on
     * different endpoints. All devices used in compositions shall adhere to the disambiguation requirements of the
     * System Model. Other device types, not explicitly listed in the table, may also be included in device compositions
     * but are not considered part of the core functionality of the device.
     * Snapshot Cameras which implement occupancy detection based on the signals from the optical sensor, may expose
     * this functionality using an Occupancy Sensing cluster on the primary camera endpoint along with the other camera
     * functionality. The device type Occupancy Sensor shall NOT be added to the DeviceTypeList of this endpoint.
     * Snapshot Cameras may have an Occupancy Sensor of a different type for occupancy detection independent of the
     * optical sensor. If this sensor is exposed, it shall be placed on a child endpoint of the primary camera endpoint,
     * with the corresponding device type Occupancy Sensor, as indicated in the following table:
     **/
    public static final Integer SNAPSHOT_CAMERA = 325;
    /**
     * A Soil Sensor device reports measurements of soil values, such as moisture and (optionally) temperature.
     * ### Cluster Requirements
     * #### Identify Cluster
     * This is used to identify the endpoint.
     * #### Temperature Measurement Cluster
     * This is used to provide the temperature of the soil. Measurements SHOULD be done either in the soil or very close
     * to the soil, in order to NOT provide ambient temperature measurements.
     * #### Soil Measurement Cluster
     * This is used to provide the humidity of the soil.
     **/
    public static final Integer SOIL_SENSOR = 69;
    /**
     * A Solar Power device is a device that allows a solar panel array, which can optionally be comprised of a set
     * parallel strings of solar panels, and its associated controller and, if appropriate, inverter, to be monitored
     * and controlled by an Energy Management System.
     * ### Solar Power Architecture
     * A Solar Power device is always defined via endpoint composition. See Section 14.3.6, "Device Type Requirements"
     * for more details.
     * An example of a Solar Power device with single phase AC output is illustrated below.
     * An example of a Solar Power device with single phase AC output, but with the ability to measure the output from 4
     * sets of solar panels supplying the overall device is illustrated below.
     * An example of a Solar Power device with single phase AC output, but with the ability to measure the output from 4
     * individual solar panels, arranged as 2 strings or 2 panels each is illustrated below.
     * ### Device Type Requirements
     * A Solar Power device shall be composed of at least one endpoint with device types as defined by the conformance
     * below. There may be more endpoints with additional instances of these device types or additional device types
     * existing in the Solar Power device.
     * #### Device Energy Management Device Type
     * If the Solar Power device output power can be controlled, then the Device Energy Management device shall be
     * included.
     * #### Cluster Requirements on Component Device Types
     * If a Solar Power device supports measurement of the output of individual solar panels or strings of solar panels
     * then it may include additional endpoints for each such measurement, including an Electrical Sensor Device Type as
     * child elements. For each such child endpoint:
     * - It shall include a User Label cluster to allow an installer to add identifying information for the panel or
     * string of panels.
     * #### Element Requirements on Component Device Types
     * The Electrical Sensor device shall also conform to the following:
     * - An Electrical Sensor device shall measure the energy and power flows of the Solar Power device at the AC grid
     * or DC connection point.
     * - If the Solar Power device is connected to AC wiring, this Electrical Power Measurement cluster shall support
     * the AlternatingCurrent feature, and shall support the PolyPhasePower feature if the Solar Power device is
     * connected via polyphase wiring.
     * - If the Solar Power device is connected to DC wiring, this Electrical Power Measurement cluster shall support
     * the DirectCurrent feature.
     * - This Electrical Power Measurement cluster SHOULD support the ReactivePower attribute if connected to AC wiring.
     * - This Electrical Energy Measurement cluster SHOULD support the CumulativeEnergy feature.
     * #### Semantic Tag Requirements on Component Device Types
     * The Descriptor cluster for the endpoint including the Power Source device shall include the Grid tag if it is
     * connected to the premises wiring.
     * If a Solar Power device supports two or three phase power output then it may include two or three additional
     * endpoints, each including an Electrical Sensor Device Type as child elements. For each such child endpoint it
     * shall include a semantic tag from the Electrical Measurement Namespace in the TagList attribute of the Descriptor
     * cluster to describe the endpoint for the relevant Electrical Power Measurement and Electrical Energy Measurement
     * clusters indicating the relevant AC phase that is being measured.
     * If a Solar Power device supports measurement of the output of individual solar panels or strings of solar panels
     * then it may include additional endpoints for each such measurement, including an Electrical Sensor Device Type as
     * child elements. For each such child endpoint:
     * - It shall include a semantic tag from a Common Namespace, or a Manufacturer defined Tag and Label, in the
     * TagList attribute of the Descriptor cluster to describe the endpoint for the relevant Electrical Power
     * Measurement and Electrical Energy Measurement clusters, indicating the relevant device port, panel, or string of
     * panels that is being measured.
     * Any Temperature Sensors included shall include Tag(s), and for non-standard Namespaces, Label(s) in the
     * Descriptor clusters of their endpoints to identify the temperature being measured.
     **/
    public static final Integer SOLAR_POWER = 23;
    /**
     * This defines conformance to the Speaker device type.
     * This feature controls the speaker volume of the device.
     * To control unmute/mute, the On/Off cluster shall be used. A value of TRUE for the OnOff attribute shall represent
     * the volume on (not muted) state, while a value of FALSE shall represent the volume off (muted) state. For volume
     * level control, the Level cluster shall be used.
     * A dedicated endpoint is needed because the On/Off cluster can also be used for other purposes, such as for power
     * control.
     * The decision to use Level and On/Off clusters for volume (rather than defining a new audio control cluster) was
     * made in order to treat volume in a fashion consistent with lighting which also uses these clusters and has
     * matching functional requirements.
     **/
    public static final Integer SPEAKER = 34;
    /**
     * A Temperature Controlled Cabinet only exists composed as part of another device type. It represents a single
     * cabinet that is capable of having its temperature controlled. Such a cabinet may be chilling or freezing food,
     * for example as part of a refrigerator, freezer, wine chiller, or other similar device. Equally, such a cabinet
     * may be warming or heating food, for example as part of an oven, range, or similar device.
     * ### Element Requirements
     * Temperature Controlled cabinets only allow the Temperature Control cluster to use the TemperatureNumber feature
     * (i.e. actual temperature in °C). This is because using qualitative temperature levels (e.g. Low/Medium/High) does
     * not allow the behavior expected by the majority of clients. Clients would be trying to "set the temperature" of a
     * cabinet using that cluster, such as an oven's cooking temperature, or a refrigerator's internal cabinet
     * temperature setpoint.
     **/
    public static final Integer TEMPERATURE_CONTROLLED_CABINET = 113;
    /**
     * A Temperature Sensor device reports measurements of temperature.
     * ### Cluster Requirements
     * #### Thermostat User Interface Configuration Cluster
     * This cluster provides an interface to allow configuration of the user interface for a temperature sensor that
     * supports keypad or screen.
     **/
    public static final Integer TEMPERATURE_SENSOR = 770;
    /**
     * A Thermostat Controller is a device capable of controlling a Thermostat.
     **/
    public static final Integer THERMOSTAT_CONTROLLER = 778;
    /**
     * A Thermostat device is capable of having either built-in or separate sensors for temperature, humidity or
     * occupancy. It allows the desired temperature to be set either remotely or locally. The thermostat is capable of
     * sending heating and/or cooling requirement notifications to a heating/cooling unit (for example, an indoor air
     * handler) or is capable of including a mechanism to control a heating or cooling unit directly.
     * ### Thermostat Suggestion and Predicted Activity Considerations
     * The thermostat cluster contains the Thermostat Suggestion feature, which allows clients to provide suggestions
     * based on external contexts. The thermostat might also support the Ambient Context Sensing client and take action
     * based on the context provided by the Predicted Activity feature. Thermostats may support either of these
     * features. If both functionalities are supported, the following recommendations are provided to Thermostat
     * devices:
     * - Changes which the thermostat wants to apply as a result of the data provided by the Ambient Context Sensing
     * server, SHOULD be translated into a Thermostat Suggestion and SHOULD NOT be directly applied, in order to let the
     * thermostat evaluate the change in state with any other suggestion provided by other clients.
     * - If the resulting action from the context provided by the Ambient Context Sensing client can not be translated
     * into a supported Thermostat Suggestion, the Thermostat may apply the change directly, but should be aware that it
     * might impact the evaluation of any current suggestions and the behavior related to conflict resolution between
     * the current suggestions and the input from the Ambient Context Sensing client is manufacturer specific.
     * - The thermostat may prioritize the data provided by the Ambient Context Sensing server, in case there are
     * multiple suggestions present, and use this as input when deciding which suggestion to apply.
     **/
    public static final Integer THERMOSTAT = 769;
    /**
     * A Thread Border Router device type provides interfaces for querying and configuring the associated Thread
     * network.
     * Instances of physical devices categorized as Thread Border Routers encompass standalone Thread Border Routers,
     * conventional application devices like smart speakers, media streamers, and lighting fixtures equipped with a
     * Thread Border Router, as well as Wi-Fi Routers incorporating Thread Border Router functionality.
     * The necessary hardware and software prerequisites are detailed within the clusters that are mandated by this
     * device type.
     * ### Device Type Requirements
     * If a Thread Border Router endpoint supports the Secondary Network Interface device type, then
     * - The Thread Border Router Management cluster and the Network Commissioning Cluster shall reflect the same
     * underlying network configuration, i.e. changes made via either cluster shall also be reflected in the other.
     * - The MaxNetworks attribute in the Network Commissioning Cluster shall have a value of 1.
     * ### Other Requirements
     * The device shall implement a Thread Border Router as described by the Thread specification, and provide
     * connectivity between the Thread network and a hub network when connected via a functioning adjacent
     * infrastructure link.
     * The Thread Interface associated with the Border Router shall be exposed via the Thread Border Router Management
     * cluster.
     * The device may include a Network Commissioning cluster instance associated with the Wi-Fi or Ethernet adjacent
     * infrastructure link interface on its Root Node. It SHOULD NOT include a Network Commissioning cluster instance
     * associated with the Thread interface of the Border Router on its Root Node.
     * #### Thread Requirements
     * A device exposing the Thread Border Router device type shall be certified by the Thread Group as Built on Thread:
     * Border Router based on Thread 1.4.0 or above.
     * The device shall implement a Thread Border Router as described by the Thread specification, and provide
     * connectivity between the Thread network and the Wi-Fi / Ethernet hub network. The Thread Interface associated
     * with the Border Router shall be exposed via the Thread Border Router Management cluster.
     * The Thread Border Router device shall support working as a Thread Parent for a minimum of 64 Thread Children
     * simultaneously in any combination of Children End Device types. The Thread Border Router device shall support
     * operating as a Thread Border Router in any Thread Network with up to 150 Thread nodes.
     * ### Cluster Usage
     * This section describes how to control and monitor the operation of a Thread Border Router.
     * The Thread Border Router Device Type provides the Thread Border Router Management Cluster with the goal of
     * ensuring a seamless user experience when adding a new Border Router to form a new Thread network or join an
     * existing one. This section presents informative configuration sequences that a Fabric Admin can set up to improve
     * coverage and Internet access redundancy for Thread networks using the Thread Border Router device type. Four use
     * cases are covered:
     * - Initial configuration of a Thread Border Router when no PAN exists
     * - Joining an existing PAN
     * - Sharing an existing PAN
     * - Moving to a new PAN
     * #### Initial configuration of Thread Border Router
     * In this use case, there is initially no PAN at the user's home. The user installs a Matter certified Thread
     * Border Router to use Thread connectivity for their Matter devices.
     * After installation, the PAN configured by Admin A on the Thread Border Router is used to install the Thread
     * Matter device on Fabric A.
     * #### Joining an existing PAN
     * In this use case, there is already a PAN in the user's home that is managed by a Fabric A with TBR1 (e.g. as a
     * result of the above sequence diagram applied previously between Admin 1 and TBR1). The user installs a
     * Matter-certified Thread Border Router (TBR2) to extend the Thread coverage and Internet access redundancy of
     * Thread networks for their Matter devices.
     * After installation, a single Thread PAN is shared by all Border Routers.
     * #### Share an existing PAN
     * In this use case, there is already a PAN in the user's home that is managed by Fabric A with TBR1 (e.g. as a
     * result of the above sequence diagram applied previously between Admin 1 and TBR1). The user wants to use the
     * Thread connectivity provided by the Matter-certified Thread Border Router managed by Fabric A with Fabric B to
     * share its connectivity.
     * After the installation, the Thread PAN is shared by Fabric A and B.
     * > [!NOTE]
     * > NOTE: The user must use the Multiple Fabrics feature process between Fabrics A and B to commission the Thread
     * Border Router from Fabric A to Fabric B.
     * #### Merging to a new PAN
     * This use case is a specific configuration where the user already has 2 PANs. One is managed by a Fabric A through
     * a Matter Thread Border Router (TBR1) and the other is managed by a Fabric B through another Matter Thread Border
     * Router (TBR2).
     * The user wishes to merge the existing Thread connectivity provided by the two Matter certified Thread Border
     * Routers to share their connectivity to extend the Thread coverage and Internet access redundancy of the Thread
     * networks for their Matter devices.
     * After installation, the Thread PAN is shared between Fabrics A and B.
     * Note 1 : Activation of the pending dataset is Thread stack dependent and propagation of the new dataset to all
     * Matter devices may fail, for example if devices are disconnected during the process.
     * Note 2 : This configuration should remain very rare if the previous installations followed the previous use
     * cases.
     **/
    public static final Integer THREAD_BORDER_ROUTER = 145;
    /**
     * A Video Doorbell device is a composite device which combines a camera and a switch to provide a doorbell with
     * Video and Audio streaming.
     * ### Device Type Requirements
     * This device type is composed of other device types listed in this table subject to the conformance column of the
     * table. All devices used in compositions shall adhere to the disambiguation and superset requirements of the
     * System Model. Additional device types not listed in this table may also be included in device compositions.
     **/
    public static final Integer VIDEO_DOORBELL = 323;
    /**
     * This defines conformance to the Video Remote Control device type.
     * A Video Remote Control is a client that can control a Video Player, for example, a traditional universal remote
     * control.
     **/
    public static final Integer VIDEO_REMOTE_CONTROL = 42;
    /**
     * This defines conformance to the Water Freeze Detector device type.
     * ### Cluster Requirements
     * #### Identify Cluster
     * This is used to identify the endpoint.
     * #### Boolean State Cluster
     * This is used to indicate the state of the sensor/detector.
     * The state of the Boolean State cluster shall reflect the sensor detection using this scheme of:
     * Due to the difficulty in quantifying the risk of freezing based on the dependency on external factors such as
     * temperature, humidity, pressure, etc, the actual triggering of a detector of this type depends on the physical
     * construction and characteristics of the device and is therefore considered manufacturer specific.
     * #### Boolean State Configuration Cluster
     * This is used to configure the sensor/detector and is for this device type linked to the configuration of the
     * Boolean State cluster.
     **/
    public static final Integer WATER_FREEZE_DETECTOR = 65;
    /**
     * A water heater is a device that is generally installed in properties to heat water for showers, baths etc.
     * ### Water Heater Architecture
     * A Water Heater is always defined via endpoint composition.
     * If a Water Heater supports multiple temperature measurement sensors as child elements, it shall include a
     * separate endpoint for each sensor. Each endpoint shall include a semantic tag in the TagList attribute of the
     * Descriptor cluster to describe the relevant position of the sensor. Such a semantic tag shall be from the defined
     * Common Position namespace (i.e. Top, Middle, Bottom etc).
     * For basic control features the Water Heater re-uses the Thermostat cluster with the HEAT and SCH features. This
     * allows it to have daily schedules set as to when the hot water heating is enabled, as well as setting the desired
     * setpoint of the hot water.
     * Additional features of the Water Heater (such as reporting estimated hot water content, and smart reheating
     * functions) are provided by the Water Heater application cluster.
     * In order to add energy management capability, the Device Energy Management cluster may be optionally supported,
     * and if so, the Electrical Power Measurement and Electrical Energy Measurement clusters are supported via the
     * Electrical Sensor device type.
     * An example of a Water Heater device is illustrated below.
     * ### Element Requirements
     * The Energy Management feature of the Water Heater cluster shall be supported if the Device Energy Management
     * device type is included.
     * If Off is a supported SystemMode in the Thermostat cluster, setting the SystemMode of the Thermostat cluster to
     * Off shall set the CurrentMode attribute of the Water Heater Mode cluster to a mode having the Off mode tag value
     * and vice versa.
     * At least one entry in the SupportedModes attribute of the Water Heater Mode cluster shall include the Timed mode
     * tag in the ModeTags field list.
     * ### Device Type Requirements
     * A Water Heater shall be composed of at least one endpoint with device types as defined by the conformance below.
     * There may be more endpoints with other device types existing in the Water Heater.
     * #### Electrical Sensor Device Type
     * If a Device Energy Management device type is included as part of a composition, the Electrical Sensor device type
     * shall also be included.
     * #### Cluster Requirements on Component Device Types
     * If an Electrical Sensor device is included as part of a composition, it shall include both the Electrical Energy
     * Measurement and Electrical Power Measurement clusters, measuring the total energy and power of the Water Heater.
     * #### Element Requirements on Component Device Types
     * If a Device Energy Management device type is included on a separate endpoint as part of a composition and the
     * Device Energy Management cluster is supported on the same endpoint, the PowerForecastReporting feature of the
     * Device Energy Management cluster shall also be supported.
     **/
    public static final Integer WATER_HEATER = 1295;
    /**
     * This defines conformance to the Water Leak Detector device type.
     * ### Cluster Requirements
     * #### Identify Cluster
     * This is used to identify the endpoint.
     * #### Boolean State Cluster
     * This is used to indicate the state of the sensor/detector.
     * The state of the Boolean State cluster shall reflect the sensor detection using this scheme of:
     * #### Boolean State Configuration Cluster
     * This is used to configure the sensor/detector and is for this device type linked to the configuration of the
     * Boolean State cluster.
     **/
    public static final Integer WATER_LEAK_DETECTOR = 67;
    /**
     * This defines conformance to the Water Valve device type.
     * ### Cluster Requirements
     * #### Identify Cluster
     * This cluster is used to identify the device.
     * #### Valve Configuration and Control Cluster
     * This cluster is used to configure and control (Open/Close) the valve.
     * #### Flow Measurement Cluster
     * The cluster server, if present, shall be used to report the measured flow through the valve.
     * The cluster client, if present, may be used via binding to close a control loop of flow through the valve.
     * ### Device implementation recommendations
     * #### Start Up Behavior
     * The start up behavior of a device with this device type, is currently not specified and is considered
     * manufacturer specific. This means that the start up behavior and what is considered the "safe state", most
     * suitable for the specific device, is defined by the manufacturer.
     * #### Firmware Update
     * When a device with this device type needs to update its firmware (or restart for another reason), it is strongly
     * recommended to only perform the update/restart when the valve is in its closed state, as well as ignoring any
     * open request during this update/restart, given the chance a valve can unintentionally be left in the open state,
     * for longer periods of time.
     **/
    public static final Integer WATER_VALVE = 66;
    /**
     * A Window Covering Controller is a device that controls an automatic window covering.
     **/
    public static final Integer WINDOW_COVERING_CONTROLLER = 515;
    /**
     * This defines conformance to the Window Covering device type.
     * ### Cluster Requirements
     * Furthermore, in revision at or before revision 5 for this device type, either or both of the Closure Control
     * cluster and the Closure Dimension cluster shall NOT appear on the same endpoint. This is to avoid potential
     * future usage of the closely related Closure Control cluster in this device type from interfering with
     * non-standard usage of that cluster until proper data dependency language can be introduced, if any.
     **/
    public static final Integer WINDOW_COVERING = 514;
}
