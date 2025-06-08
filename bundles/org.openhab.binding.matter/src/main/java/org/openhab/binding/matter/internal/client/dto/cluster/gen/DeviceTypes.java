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
        DEVICE_MAPPING.put(40, "BasicVideoPlayer");
        DEVICE_MAPPING.put(24, "BatteryStorage");
        DEVICE_MAPPING.put(19, "BridgedNode");
        DEVICE_MAPPING.put(41, "CastingVideoClient");
        DEVICE_MAPPING.put(35, "CastingVideoPlayer");
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
        DEVICE_MAPPING.put(1296, "ElectricalSensor");
        DEVICE_MAPPING.put(1292, "EnergyEvse");
        DEVICE_MAPPING.put(269, "ExtendedColorLight");
        DEVICE_MAPPING.put(122, "ExtractorHood");
        DEVICE_MAPPING.put(43, "Fan");
        DEVICE_MAPPING.put(774, "FlowSensor");
        DEVICE_MAPPING.put(15, "GenericSwitch");
        DEVICE_MAPPING.put(777, "HeatPump");
        DEVICE_MAPPING.put(775, "HumiditySensor");
        DEVICE_MAPPING.put(304, "JointFabricAdministrator");
        DEVICE_MAPPING.put(124, "LaundryDryer");
        DEVICE_MAPPING.put(115, "LaundryWasher");
        DEVICE_MAPPING.put(262, "LightSensor");
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
        DEVICE_MAPPING.put(23, "SolarPower");
        DEVICE_MAPPING.put(34, "Speaker");
        DEVICE_MAPPING.put(113, "TemperatureControlledCabinet");
        DEVICE_MAPPING.put(770, "TemperatureSensor");
        DEVICE_MAPPING.put(769, "Thermostat");
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
     * provide functionality for the collection of descendant endpoints present in the PartsList of the endpoint’s
     * descriptor, for example the Actions cluster.
     * The purpose of this device type is to aggregate functionality for a collection of endpoints. The definition of
     * the collection or functionality is not defined here.
     * When using this device type as a collection of bridged nodes, please see the &quot;Bridge&quot; section in the
     * System Model specification.
     **/
    public static final Integer AGGREGATOR = 14;
    /**
     * An Air Purifier is a standalone device that is designed to clean the air in a room.
     * It is a device that has a fan to control the air speed while it is operating. Optionally, it can report on the
     * condition of its filters.
     **/
    public static final Integer AIR_PURIFIER = 45;
    /**
     * This defines conformance for the Air Quality Sensor device type.
     * An air quality sensor is a device designed to monitor and measure various parameters related to the quality of
     * ambient air in indoor or outdoor environments.
     **/
    public static final Integer AIR_QUALITY_SENSOR = 44;
    /**
     * This defines conformance to the Basic Video Player device type.
     * A Video Player (either Basic or Casting) represents a device that is able to play media to a physical output or
     * to a display screen which is part of the device.
     * A Basic Video Player has playback controls (play, pause, etc.) and keypad remote controls (up, down, number
     * input), but is not able to launch content and is not a content app platform (the Casting Video Player device type
     * is used for these functions).
     * For example, a Basic Video Player can be a traditional TV device a physical media playback device such as a DVD
     * Player, or a device that provides input to another device like a TV or computer monitor.
     * Please see Video Player Architecture for additional Basic Video Player requirements relating to Video Player
     * device endpoint composition, commissioning, feature representation in clusters, and UI context.
     **/
    public static final Integer BASIC_VIDEO_PLAYER = 40;
    /**
     * A Battery Storage device is a device that allows a DC battery, which can optionally be comprised of a set
     * parallel strings of battery packs and associated controller, and an AC inverter, to be monitored and controlled
     * by an Energy Management System in order to manage the peaks and troughs of supply and demand, and/or to optimize
     * cost of the energy consumed in premises. It is not intended to be used for a UPS directly supplying a set of
     * appliances, nor for portable battery storage devices.
     **/
    public static final Integer BATTERY_STORAGE = 24;
    /**
     * This defines conformance for a Bridged Node root endpoint. This endpoint is akin to a &quot;read me first&quot;
     * endpoint that describes itself and any other endpoints that make up the Bridged Node. A Bridged Node endpoint
     * represents a device on a foreign network, but is not the root endpoint of the bridge itself.
     **/
    public static final Integer BRIDGED_NODE = 19;
    /**
     * This defines conformance to the Casting Video Client device type.
     * A Casting Video Client is a client that can launch content on a Casting Video Player, for example, a Smart
     * Speaker or a Content Provider phone app.
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
     * Please see Video Player Architecture for additional Casting Video Player requirements relating to Video Player
     * device endpoint composition, commissioning, feature representation in clusters, and UI context.
     **/
    public static final Integer CASTING_VIDEO_PLAYER = 35;
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
     **/
    public static final Integer CONTACT_SENSOR = 21;
    /**
     * This defines conformance to the Content App device type.
     * A Content App is usually an application built by a Content Provider. A Casting Video Player with a Content App
     * Platform is able to launch Content Apps and represent these apps as separate endpoints.
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
     **/
    public static final Integer COOK_SURFACE = 119;
    /**
     * A cooktop is a cooking surface that heats food either by transferring currents from an electromagnetic field
     * located below the glass surface directly to the magnetic induction cookware placed above or through traditional
     * gas or electric burners.
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
     * An Electrical Sensor device measures the electrical power and/or energy being imported and/or exported.
     **/
    public static final Integer ELECTRICAL_SENSOR = 1296;
    /**
     * An EVSE (Electric Vehicle Supply Equipment) is a device that allows an EV (Electric Vehicle) to be connected to
     * the mains electricity supply to allow it to be charged (or discharged in case of Vehicle to Grid / Vehicle to
     * Home applications).
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
     * Extractor Hood’s primary purpose is to reduce odors that arise during the cooking process by either extracting
     * the air above the cooking surface or by recirculating and filtering it. It may also contain a light for
     * illuminating the cooking surface.
     * Extractor Hoods may also be known by the following names:
     * • Hoods
     * • Extractor Fans
     * • Extractors
     * • Range Hoods
     * • Telescoping Hoods
     * • Telescoping Extractors
     **/
    public static final Integer EXTRACTOR_HOOD = 122;
    /**
     * A Fan device is typically standalone or mounted on a ceiling or wall and is used to circulate air in a room.
     **/
    public static final Integer FAN = 43;
    /**
     * A Flow Sensor device measures and reports the flow rate of a fluid.
     **/
    public static final Integer FLOW_SENSOR = 774;
    /**
     * This defines conformance for the Generic Switch device type.
     **/
    public static final Integer GENERIC_SWITCH = 15;
    /**
     * A Heat Pump device is a device that uses electrical energy to heat either spaces or water tanks using ground,
     * water or air as the heat source. These typically can heat the air or can pump water via central heating radiators
     * or underfloor heating systems. It is typical to also heat hot water and store the heat in a hot water tank.
     * Note that the Water Heater device type can also be heated by a heat pump and has similar requirements, but that
     * cannot be used for space heating.
     **/
    public static final Integer HEAT_PUMP = 777;
    /**
     * A humidity sensor (in most cases a Relative humidity sensor) reports humidity measurements.
     **/
    public static final Integer HUMIDITY_SENSOR = 775;
    /**
     * A Joint Fabric Administrator device provides capabilities to manage the Joint Fabric Datastore and issue an ICAC
     * signed by the Joint Fabric Anchor Root CA.
     * A client wanting to access the capabilities of the Joint Fabric Administrator may use the Joint Commissioning
     * Method to be commissioned onto the Joint Fabric. Once commissioned, a client may access the capabilities of the
     * Joint Fabric Administrator.
     **/
    public static final Integer JOINT_FABRIC_ADMINISTRATOR = 304;
    /**
     * A Laundry Dryer represents a device that is capable of drying laundry items.
     **/
    public static final Integer LAUNDRY_DRYER = 124;
    /**
     * A Laundry Washer represents a device that is capable of laundering consumer items. Any laundry washer product may
     * utilize this device type.
     * A Laundry Washer shall be composed of at least one endpoint with the Laundry Washer device type.
     **/
    public static final Integer LAUNDRY_WASHER = 115;
    /**
     * A Light Sensor device is a measurement and sensing device that is capable of measuring and reporting the
     * intensity of light (illuminance) to which the sensor is being subjected.
     **/
    public static final Integer LIGHT_SENSOR = 262;
    /**
     * This defines conformance to the Microwave Oven device type.
     * A Microwave Oven is a device with the primary function of heating foods and beverages using a magnetron.
     **/
    public static final Integer MICROWAVE_OVEN = 121;
    /**
     * This defines conformance to the Mode Select device type.
     **/
    public static final Integer MODE_SELECT = 39;
    /**
     * A Mounted Dimmable Load Control is a fixed device that provides power to another device that is plugged into it,
     * and is capable of being switched on or off and have its level adjusted. The Mounted Dimmable Load Control is
     * typically used to control a conventional non-communicating light through its mains connection using phase
     * cutting.
     **/
    public static final Integer MOUNTED_DIMMABLE_LOAD_CONTROL = 272;
    /**
     * A Mounted On/Off Control is a fixed device that provides power to another device that is plugged into it, and is
     * capable of switching that provided power on or off.
     **/
    public static final Integer MOUNTED_ON_OFF_CONTROL = 271;
    /**
     * A Network Infrastructure Manager provides interfaces that allow for the management of the Wi-Fi, Thread, and
     * Ethernet networks underlying a Matter deployment, realizing the Star Network Topology described in [MatterCore].
     * Examples of physical devices that implement the Matter Network Infrastructure Manager device type include Wi-Fi
     * gateway routers.
     * Relevant hardware and software requirements for Network Infrastructure Manager devices are defined in Section
     * 15.2.6, “Other Requirements” and within the clusters mandated by this device type.
     * A Network Infrastructure Manager device may be managed by a service associated with the device vendor, for
     * example, an Internet Service Provider. Sometimes this managing service will have policies that require the use of
     * the Managed Device feature of the Access Control Cluster (see Section 15.2.5.1, “Access Control MNGD
     * Conformance”). Consequently, Commissioners of this device type should be aware of this feature and its use.
     **/
    public static final Integer NETWORK_INFRASTRUCTURE_MANAGER = 144;
    /**
     * An Occupancy Sensor is a measurement and sensing device that is capable of measuring and reporting the occupancy
     * state in a designated area.
     **/
    public static final Integer OCCUPANCY_SENSOR = 263;
    /**
     * The On/Off Light is a lighting device that is capable of being switched on or off by means of a bound controller
     * device such as an On/Off Light Switch or a Dimmer Switch. In addition, an on/off light is also capable of being
     * switched by means of a bound occupancy sensor.
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
     **/
    public static final Integer ON_OFF_PLUG_IN_UNIT = 266;
    /**
     * An On/Off Sensor is a measurement and sensing device that, when bound to a lighting device such as a Dimmable
     * Light, is capable of being used to switch the device on or off.
     **/
    public static final Integer ON_OFF_SENSOR = 2128;
    /**
     * An OTA Provider is a node that is capable of providing an OTA software update to other nodes on the same fabric.
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
     **/
    public static final Integer OVEN = 123;
    /**
    * 
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
     **/
    public static final Integer PUMP = 771;
    /**
     * This defines conformance to the Rain Sensor device type.
     **/
    public static final Integer RAIN_SENSOR = 68;
    /**
     * A refrigerator represents a device that contains one or more cabinets that are capable of chilling or freezing
     * food. Examples of consumer products that may make use of this device type include refrigerators, freezers, and
     * wine coolers.
     **/
    public static final Integer REFRIGERATOR = 112;
    /**
     * This defines conformance for the Robotic Vacuum Cleaner device type.
     **/
    public static final Integer ROBOTIC_VACUUM_CLEANER = 116;
    /**
     * This defines conformance to the Room Air Conditioner device type.
     * A Room Air Conditioner is a device with the primary function of controlling the air temperature in a single room.
     **/
    public static final Integer ROOM_AIR_CONDITIONER = 114;
    /**
     * This defines conformance for a root node endpoint (see System Model specification). This endpoint is akin to a
     * &quot;read me first&quot; endpoint that describes itself and the other endpoints that make up the node.
     * • Device types with Endpoint scope shall NOT be supported on the same endpoint as this device type.
     * • Clusters with an Application role shall NOT be supported on the same endpoint as this device type.
     * • Other device types with Node scope may be supported on the same endpoint as this device type.
     **/
    public static final Integer ROOT_NODE = 22;
    /**
     * A Secondary Network Interface device provides an additional network interface supported by the Node,
     * supplementing the primary interface hosted by the Root Node endpoint.
     * A Node supporting multiple network interfaces shall include the primary interface on the Root Node endpoint,
     * along with secondary interfaces on other endpoints. The priorities of these network interfaces are determined by
     * the order of their endpoints, where interfaces with smaller endpoint numbers are higher priority.
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
     **/
    public static final Integer SMOKE_CO_ALARM = 118;
    /**
     * A Solar Power device is a device that allows a solar panel array, which can optionally be comprised of a set
     * parallel strings of solar panels, and its associated controller and, if appropriate, inverter, to be monitored
     * and controlled by an Energy Management System.
     **/
    public static final Integer SOLAR_POWER = 23;
    /**
     * This defines conformance to the Speaker device type. This feature controls the speaker volume of the device.
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
     **/
    public static final Integer TEMPERATURE_CONTROLLED_CABINET = 113;
    /**
     * A Temperature Sensor device reports measurements of temperature.
     **/
    public static final Integer TEMPERATURE_SENSOR = 770;
    /**
     * A Thermostat device is capable of having either built-in or separate sensors for temperature, humidity or
     * occupancy. It allows the desired temperature to be set either remotely or locally. The thermostat is capable of
     * sending heating and/or cooling requirement notifications to a heating/cooling unit (for example, an indoor air
     * handler) or is capable of including a mechanism to control a heating or cooling unit directly.
     **/
    public static final Integer THERMOSTAT = 769;
    /**
     * This defines conformance to the Video Remote Control device type.
     * A Video Remote Control is a client that can control a Video Player, for example, a traditional universal remote
     * control.
     **/
    public static final Integer VIDEO_REMOTE_CONTROL = 42;
    /**
     * This defines conformance to the Water Freeze Detector device type.
     **/
    public static final Integer WATER_FREEZE_DETECTOR = 65;
    /**
     * A water heater is a device that is generally installed in properties to heat water for showers, baths etc.
     **/
    public static final Integer WATER_HEATER = 1295;
    /**
     * This defines conformance to the Water Leak Detector device type.
     **/
    public static final Integer WATER_LEAK_DETECTOR = 67;
    /**
     * This defines conformance to the Water Valve device type.
     **/
    public static final Integer WATER_VALVE = 66;
    /**
     * A Window Covering Controller is a device that controls an automatic window covering.
     **/
    public static final Integer WINDOW_COVERING_CONTROLLER = 515;
    /**
     * This defines conformance to the Window Covering device type.
     **/
    public static final Integer WINDOW_COVERING = 514;
}
