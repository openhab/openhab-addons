package org.openhab.binding.amazonechocontrol.internal.smarthome;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants;

public class Constants {

    public static Map<String, Function<String, HandlerBase>> HandlerFactory = new HashMap<String, Function<String, HandlerBase>>() {
        private static final long serialVersionUID = 1L;
        {
            put(HandlerPowerController.INTERFACE, (s) -> new HandlerPowerController());
            put(HandlerBrightnessController.INTERFACE, (s) -> new HandlerBrightnessController());
            put(HandlerColorController.INTERFACE, (s) -> new HandlerColorController());
            put(HandlerColorTemperatureController.INTERFACE, (s) -> new HandlerColorTemperatureController());
            put(HandlerSecurityPanelController.INTERFACE, (s) -> new HandlerSecurityPanelController());
            put(HandlerAcousticEventSensor.INTERFACE, (s) -> new HandlerAcousticEventSensor());
        }
    };

    public static final Set<String> SUPPORTED_INTERFACES = HandlerFactory.keySet();

    // channel types
    public static final ChannelTypeUID CHANNEL_TYPE_POWER_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "powerState");

    public static final ChannelTypeUID CHANNEL_TYPE_COLOR_TEPERATURE_IN_KELVIN = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "colorTemperatureInKelvin");

    public static final ChannelTypeUID CHANNEL_TYPE_COLOR_TEMPERATURE_NAME = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "colorTemperatureName");

    public static final ChannelTypeUID CHANNEL_TYPE_COLOR_NAME = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "colorName");

    public static final ChannelTypeUID CHANNEL_TYPE_COLOR = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "color");

    public static final ChannelTypeUID CHANNEL_TYPE_ARM_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "armState");

    public static final ChannelTypeUID CHANNEL_TYPE_BURGLARY_ALARM = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "burglaryAlarm");

    public static final ChannelTypeUID CHANNEL_TYPE_CARBON_MONOXIDE_ALARM = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "carbonMonoxideAlarm");

    public static final ChannelTypeUID CHANNEL_TYPE_FIRE_ALARM = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "fireAlarm");

    public static final ChannelTypeUID CHANNEL_TYPE_WATER_ALARM = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "waterAlarm");

    public static final ChannelTypeUID CHANNEL_TYPE_GLASS_BREAK_DETECTION_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "glassBreakDetectionState");

    public static final ChannelTypeUID CHANNEL_TYPE_SMOKE_ALARM_DETECTION_STATE = new ChannelTypeUID(
            AmazonEchoControlBindingConstants.BINDING_ID, "smokeAlarmDetectionState");

    // List of Item types
    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_DIMMER = "Dimmer";
    public static final String ITEM_TYPE_STRING = "String";
    public static final String ITEM_TYPE_NUMBER = "Number";
    public static final String ITEM_TYPE_CONTACT = "Contact";
    public static final String ITEM_TYPE_COLOR = "Color";

}
