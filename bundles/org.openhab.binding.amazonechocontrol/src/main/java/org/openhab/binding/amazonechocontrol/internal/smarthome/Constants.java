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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.BINDING_ID;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class Constants {
    public static final Map<String, Function<SmartHomeDeviceHandler, InterfaceHandler>> HANDLER_FACTORY = Map.ofEntries(
            Map.entry(HandlerPowerController.INTERFACE, HandlerPowerController::new),
            Map.entry(HandlerBrightnessController.INTERFACE, HandlerBrightnessController::new),
            Map.entry(HandlerColorController.INTERFACE, HandlerColorController::new),
            Map.entry(HandlerColorTemperatureController.INTERFACE, HandlerColorTemperatureController::new),
            Map.entry(HandlerSecurityPanelController.INTERFACE, HandlerSecurityPanelController::new),
            Map.entry(HandlerAcousticEventSensor.INTERFACE, HandlerAcousticEventSensor::new),
            Map.entry(HandlerHumiditySensor.INTERFACE, HandlerHumiditySensor::new),
            Map.entry(HandlerTemperatureSensor.INTERFACE, HandlerTemperatureSensor::new),
            Map.entry(HandlerThermostatController.INTERFACE, HandlerThermostatController::new),
            Map.entry(HandlerPercentageController.INTERFACE, HandlerPercentageController::new),
            Map.entry(HandlerPowerLevelController.INTERFACE, HandlerPowerLevelController::new),
            Map.entry(HandlerRangeController.INTERFACE, HandlerRangeController::new),
            Map.entry(HandlerMotionSensor.INTERFACE, HandlerMotionSensor::new),
            Map.entry(HandlerContactSensor.INTERFACE, HandlerContactSensor::new),
            Map.entry(HandlerLocation.INTERFACE, HandlerLocation::new),
            Map.entry(HandlerEndpointHealth.INTERFACE, HandlerEndpointHealth::new),
            Map.entry(HandlerLockController.INTERFACE, HandlerLockController::new));

    public static final Set<String> SUPPORTED_INTERFACES = HANDLER_FACTORY.keySet();

    // channel types
    public static final ChannelTypeUID CHANNEL_TYPE_TEMPERATURE = new ChannelTypeUID(BINDING_ID, "temperature");
    public static final ChannelTypeUID CHANNEL_TYPE_TARGETSETPOINT = new ChannelTypeUID(BINDING_ID, "targetSetpoint");
    public static final ChannelTypeUID CHANNEL_TYPE_LOWERSETPOINT = new ChannelTypeUID(BINDING_ID, "lowerSetpoint");
    public static final ChannelTypeUID CHANNEL_TYPE_UPPERSETPOINT = new ChannelTypeUID(BINDING_ID, "upperSetpoint");
    public static final ChannelTypeUID CHANNEL_TYPE_THERMOSTATMODE = new ChannelTypeUID(BINDING_ID, "thermostatMode");
    public static final ChannelTypeUID CHANNEL_TYPE_AIR_QUALITY_INDOOR_AIR_QUALITY = new ChannelTypeUID(BINDING_ID,
            "indoorAirQuality");
    public static final ChannelTypeUID CHANNEL_TYPE_AIR_QUALITY_HUMIDITY = new ChannelTypeUID(BINDING_ID,
            "relativeHumidity");
    public static final ChannelTypeUID CHANNEL_TYPE_AIR_QUALITY_PM25 = new ChannelTypeUID(BINDING_ID, "pm25");
    public static final ChannelTypeUID CHANNEL_TYPE_AIR_QUALITY_CARBON_MONOXIDE = new ChannelTypeUID(BINDING_ID,
            "carbonMonoxide");
    public static final ChannelTypeUID CHANNEL_TYPE_AIR_QUALITY_VOC = new ChannelTypeUID(BINDING_ID, "voc");
    public static final ChannelTypeUID CHANNEL_TYPE_FAN_SPEED = new ChannelTypeUID(BINDING_ID, "fanSpeed");
    public static final ChannelTypeUID CHANNEL_TYPE_POWER_STATE = new ChannelTypeUID(BINDING_ID, "powerState");
    public static final ChannelTypeUID CHANNEL_TYPE_LOCK_STATE = new ChannelTypeUID(BINDING_ID, "lockState");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_ACOUSTIC_EVENT_DETECTION = new ChannelTypeUID(BINDING_ID,
            "acousticEventDetectionState");
    public static final ChannelTypeUID CHANNEL_TYPE_BRIGHTNESS = new ChannelTypeUID(BINDING_ID, "brightness");
    public static final ChannelTypeUID CHANNEL_TYPE_COLOR_NAME = new ChannelTypeUID(BINDING_ID, "colorName");
    public static final ChannelTypeUID CHANNEL_TYPE_COLOR = new ChannelTypeUID(BINDING_ID, "color");
    public static final ChannelTypeUID CHANNEL_TYPE_COLOR_TEMPERATURE_NAME = new ChannelTypeUID(BINDING_ID,
            "colorTemperatureName");
    public static final ChannelTypeUID CHANNEL_TYPE_PERCENTAGE = new ChannelTypeUID(BINDING_ID, "percentage");
    public static final ChannelTypeUID CHANNEL_TYPE_POWER_LEVEL = new ChannelTypeUID(BINDING_ID, "powerLevel");
    public static final ChannelTypeUID CHANNEL_TYPE_ARM_STATE = new ChannelTypeUID(BINDING_ID, "armState");
    public static final ChannelTypeUID CHANNEL_TYPE_BURGLARY_ALARM = new ChannelTypeUID(BINDING_ID, "burglaryAlarm");
    public static final ChannelTypeUID CHANNEL_TYPE_CARBON_MONOXIDE_ALARM = new ChannelTypeUID(BINDING_ID,
            "carbonMonoxideAlarm");
    public static final ChannelTypeUID CHANNEL_TYPE_FIRE_ALARM = new ChannelTypeUID(BINDING_ID, "fireAlarm");
    public static final ChannelTypeUID CHANNEL_TYPE_WATER_ALARM = new ChannelTypeUID(BINDING_ID, "waterAlarm");
    public static final ChannelTypeUID CHANNEL_TYPE_MOTION_DETECTED = new ChannelTypeUID(BINDING_ID, "motionDetected");
    public static final ChannelTypeUID CHANNEL_TYPE_CONTACT_STATUS = new ChannelTypeUID(BINDING_ID, "contact");
    public static final ChannelTypeUID CHANNEL_TYPE_GEOLOCATION = new ChannelTypeUID(BINDING_ID, "geoLocation");
    public static final ChannelTypeUID CHANNEL_TYPE_CONNECTIVITY = new ChannelTypeUID(BINDING_ID, "connectivity");
}
