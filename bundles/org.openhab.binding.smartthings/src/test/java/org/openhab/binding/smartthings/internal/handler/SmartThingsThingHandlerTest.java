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
package org.openhab.binding.smartthings.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.converter.SmartThingsAirConditionerFanModeConverter;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverterFactory;
import org.openhab.binding.smartthings.internal.converter.SmartThingsDefaultConverter;
import org.openhab.binding.smartthings.internal.converter.SmartThingsMediaControlConverter;
import org.openhab.binding.smartthings.internal.dto.SmartThingsArgument;
import org.openhab.binding.smartthings.internal.dto.SmartThingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCommand;
import org.openhab.binding.smartthings.internal.dto.SmartThingsEnumCommand;
import org.openhab.binding.smartthings.internal.dto.SmartThingsProperty;
import org.openhab.binding.smartthings.internal.dto.SmartThingsSchema;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatus;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusCapabilities;
import org.openhab.binding.smartthings.internal.dto.SmartThingsStatusProperties;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistryImpl;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link SmartThingsThingHandler}.
 */
@NonNullByDefault
class SmartThingsThingHandlerTest {

    @Test
    void resolveDeviceIdIgnoresThingProperty() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Washer"),
                        new ThingUID("smartthings:Washer:account:Washer"))
                .withProperties(Map.of(SmartThingsBindingConstants.DEVICE_ID, "device-123")).build();
        SmartThingsThingHandler handler = new SmartThingsThingHandler(thing);

        assertEquals("", handler.resolveDeviceId());
    }

    @Test
    void resolveDeviceIdUsesThingConfiguration() {
        Configuration config = new Configuration(Map.of(SmartThingsBindingConstants.DEVICE_ID, "configured-device"));
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Washer"),
                        new ThingUID("smartthings:Washer:account:Washer"))
                .withConfiguration(config)
                .withProperties(Map.of(SmartThingsBindingConstants.DEVICE_ID, "property-device")).build();
        SmartThingsThingHandler handler = new SmartThingsThingHandler(thing);

        assertEquals("configured-device", handler.resolveDeviceId());
    }

    @Test
    void refreshDevicePropsDoesNotExposeDeviceIdProperty() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"),
                        new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner"))
                .withProperties(Map.of(SmartThingsBindingConstants.DEVICE_ID, "device-123")).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsStatusCapabilities capa = new SmartThingsStatusCapabilities();
        SmartThingsStatusProperties property = new SmartThingsStatusProperties();
        property.value = "device-123";
        capa.put("di", property);

        handler.refreshDeviceProps(capa, "main", "ocf");

        assertFalse(handler.lastUpdatedProperties.containsKey(SmartThingsBindingConstants.DEVICE_ID));
        assertFalse(handler.lastUpdatedProperties.containsKey("Device ID"));
    }

    @Test
    void removeDeviceIdPropertyRemovesDeviceIdThingProperty() {
        Configuration config = new Configuration(Map.of(SmartThingsBindingConstants.DEVICE_ID, "configured-device"));
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Washer"),
                        new ThingUID("smartthings:Washer:account:Washer"))
                .withConfiguration(config)
                .withProperties(Map.of(SmartThingsBindingConstants.DEVICE_ID, "configured-device")).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);

        handler.removeDeviceIdProperty();

        assertFalse(handler.lastUpdatedProperties.containsKey(SmartThingsBindingConstants.DEVICE_ID));
        assertEquals("configured-device",
                handler.getThing().getConfiguration().get(SmartThingsBindingConstants.DEVICE_ID));
    }

    @Test
    void refreshDeviceIgnoresSmartThingsAttributesWithoutDeclaredXmlChannel() {
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(createAirConditionerThing());
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "execute", "supportedOperatingStates", "ready");

        assertEquals(0, handler.updatedStates);
    }

    @Test
    void refreshDeviceUpdatesDeclaredXmlChannel() {
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(createAirConditionerThing());
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "switch", "switch", "on");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "control", "switch"), handler.lastUpdatedChannel);
        assertEquals(OnOffType.ON, handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesStaticAirConditionerSetpointChannel() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "control", "setpoint", SmartThingsBindingConstants.TYPE_NUMBER,
                        "thermostatCoolingSetpoint", "coolingSetpoint"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "thermostatCoolingSetpoint", "coolingSetpoint", 21.0);

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "control", "setpoint"), handler.lastUpdatedChannel);
        assertEquals(new DecimalType(21), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesStaticFrameTvChannelAlias() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(createChannel(thingUID, "control", "channel", SmartThingsBindingConstants.TYPE_STRING,
                        "tvChannel", "tvChannel"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_The_Frame", "main", "tvChannel", "tvChannel", "12");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "control", "channel"), handler.lastUpdatedChannel);
        assertEquals(new StringType("12"), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesStaticFrameArtModeAlias() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(createChannel(thingUID, "control", "art-mode", SmartThingsBindingConstants.TYPE_SWITCH,
                        "samsungvd.ambient", "ambient"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_The_Frame", "main", "samsungvd.ambient", "ambient", "on");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "control", "art-mode"), handler.lastUpdatedChannel);
        assertEquals(OnOffType.ON, handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceIgnoresSupportedPlaybackCommandsMetadata() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Soundbar:account:soundbar");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Soundbar"), thingUID)
                .withChannel(createChannel(thingUID, "control", "playback", SmartThingsBindingConstants.TYPE_PLAYER,
                        "mediaPlayback", "supportedPlaybackCommands"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Soundbar", "main", "mediaPlayback", "supportedPlaybackCommands",
                java.util.List.of("play", "pause", "stop"));

        assertEquals(0, handler.updatedStates);
    }

    @Test
    void refreshDeviceUpdatesStaticOvenOperatingStateChannel() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Oven:account:oven");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Oven"), thingUID)
                .withChannel(createChannel(thingUID, "status", "operating-state",
                        SmartThingsBindingConstants.TYPE_STRING, "samsungce.ovenOperatingState", "operatingState"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Oven", "main", "samsungce.ovenOperatingState", "operatingState", "ready");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "status", "operating-state"),
                handler.lastUpdatedChannel);
        assertEquals(new StringType("ready"), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesStaticOvenCompletionTimeAsDateTime() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Oven:account:oven");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Oven"), thingUID)
                .withChannel(createChannel(thingUID, "status", "completion-time",
                        SmartThingsBindingConstants.TYPE_DATETIME, "samsungce.ovenOperatingState", "completionTime"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Oven", "main", "samsungce.ovenOperatingState", "completionTime",
                "2026-06-17T19:30:00+02:00");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "status", "completion-time"),
                handler.lastUpdatedChannel);
        assertEquals(DateTimeType.valueOf("2026-06-17T19:30:00+02:00"), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesStaticOvenOperationTimeFromTimestamp() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Oven:account:oven");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Oven"), thingUID)
                .withChannel(createChannel(thingUID, "status", "operation-time",
                        SmartThingsBindingConstants.TYPE_DATETIME, "samsungce.ovenOperatingState", "operationTime"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());
        SmartThingsStatusCapabilities capa = new SmartThingsStatusCapabilities();
        SmartThingsStatusProperties property = new SmartThingsStatusProperties();
        property.value = "00:00:00";
        property.timestamp = "2026-06-08T23:03:09.980Z";
        capa.put("operationTime", property);

        handler.refreshDeviceFromCapa(capa, "main", "samsungce.ovenOperatingState");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "status", "operation-time"),
                handler.lastUpdatedChannel);
        assertEquals(DateTimeType.valueOf("2026-06-08T23:03:09.980Z"), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceMergesActiveAirConditionerOptionalModeIntoFanMode() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "control", "fan-mode", SmartThingsBindingConstants.TYPE_STRING,
                        "airConditionerFanMode", "fanMode"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "custom.airConditionerOptionalMode", "acOptionalMode",
                "sleep");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "control", "fan-mode"), handler.lastUpdatedChannel);
        assertEquals(new StringType("sleep"), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceKeepsOptionalModeStateUntilOptionalModeTurnsOff() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "control", "fan-mode", SmartThingsBindingConstants.TYPE_STRING,
                        "airConditionerFanMode", "fanMode"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "airConditionerFanMode", "fanMode", "high");
        handler.refreshDevice("Samsung_Room_A_C", "main", "custom.airConditionerOptionalMode", "acOptionalMode",
                "windFree");
        handler.refreshDevice("Samsung_Room_A_C", "main", "airConditionerFanMode", "fanMode", "auto");

        assertEquals(2, handler.updatedStates);
        assertEquals(new StringType("windFree"), handler.lastUpdatedState);

        handler.refreshDevice("Samsung_Room_A_C", "main", "custom.airConditionerOptionalMode", "acOptionalMode", "off");

        assertEquals(3, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "control", "fan-mode"), handler.lastUpdatedChannel);
        assertEquals(new StringType("auto"), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceClearsOptionalModeStateWhenNormalFanModeIsUnknown() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "control", "fan-mode", SmartThingsBindingConstants.TYPE_STRING,
                        "airConditionerFanMode", "fanMode"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "custom.airConditionerOptionalMode", "acOptionalMode",
                "sleep");
        handler.refreshDevice("Samsung_Room_A_C", "main", "custom.airConditionerOptionalMode", "acOptionalMode", "off");

        assertEquals(2, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "control", "fan-mode"), handler.lastUpdatedChannel);
        assertEquals(UnDefType.UNDEF, handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesSeparateOptionalModeChannelWhenDeclared() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:dynamic-air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "advanced", "ac-optional-mode",
                        SmartThingsBindingConstants.TYPE_STRING, "custom.airConditionerOptionalMode", "acOptionalMode"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "custom.airConditionerOptionalMode", "acOptionalMode",
                "sleep");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "advanced", "ac-optional-mode"),
                handler.lastUpdatedChannel);
        assertEquals(new StringType("sleep"), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesStaticAirConditionerPowerConsumptionSubChannel() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "energy", "power", SmartThingsBindingConstants.TYPE_NUMBER,
                        "powerConsumptionReport", "powerConsumption"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "powerConsumptionReport", "powerConsumption",
                Map.of("power", 123.0));

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "energy", "power"), handler.lastUpdatedChannel);
        assertEquals(new DecimalType(123), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesStaticAirConditionerEnergySubChannelAsWattHours() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, "energy", "energy"), "Number:Energy")
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "powerConsumptionReport",
                                SmartThingsBindingConstants.ATTRIBUTE, "powerConsumption",
                                SmartThingsBindingConstants.UNIT, "Wh"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "powerConsumptionReport", "powerConsumption",
                Map.of("energy", 3014134.0));

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "energy", "energy"), handler.lastUpdatedChannel);
        assertEquals(new QuantityType<>("3014134 Wh"), handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUpdatesStaticAirConditionerAutoCleaningModeAsSwitch() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "advanced", "auto-cleaning-mode",
                        SmartThingsBindingConstants.TYPE_SWITCH, "custom.autoCleaningMode", "autoCleaningMode"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "custom.autoCleaningMode", "autoCleaningMode", "off");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), "advanced", "auto-cleaning-mode"),
                handler.lastUpdatedChannel);
        assertEquals(OnOffType.OFF, handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceUsesDynamicChannelGroupWhenAvailable() {
        String dynamicGroupId = SmartThingsTypeRegistryImpl.getChannelGroupId("Samsung_Room_A_C", "main", "switch");
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:dynamic-air-conditioner");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createSwitchChannel(thingUID, dynamicGroupId)).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "switch", "switch", "on");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), dynamicGroupId, "switch"), handler.lastUpdatedChannel);
        assertEquals(OnOffType.ON, handler.lastUpdatedState);
    }

    @Test
    void refreshDeviceFromCapaUsesOriginalDeviceTypeForGeneratedThingType() {
        String dynamicGroupId = SmartThingsTypeRegistryImpl.getChannelGroupId("Robot_Vacuum", "main", "switch");
        ThingUID thingUID = new ThingUID("smartthings:Robot_Vacuum:account:robot-vacuum");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Robot_Vacuum"), thingUID)
                .withProperties(Map.of(SmartThingsBindingConstants.DEVICE_TYPE, "Robot_Vacuum"))
                .withChannel(createSwitchChannel(thingUID, dynamicGroupId)).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());
        SmartThingsStatusCapabilities capa = new SmartThingsStatusCapabilities();
        SmartThingsStatusProperties property = new SmartThingsStatusProperties();
        property.value = "on";
        capa.put("switch", property);

        handler.refreshDeviceFromCapa(capa, "main", "switch");

        assertEquals(1, handler.updatedStates);
        assertEquals(new ChannelUID(handler.getThing().getUID(), dynamicGroupId, "switch"), handler.lastUpdatedChannel);
        assertEquals(OnOffType.ON, handler.lastUpdatedState);
    }

    @Test
    void handleCommandUsesStaticCommandMetadataWithoutUpdatingState() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "setpoint");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_NUMBER)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "thermostatCoolingSetpoint",
                                SmartThingsBindingConstants.ATTRIBUTE, "coolingSetpoint",
                                SmartThingsBindingConstants.COMMAND, "setCoolingSetpoint"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, new DecimalType(24));

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(api).sendCommand(eq(""), body.capture());
        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"thermostatCoolingSetpoint\",\"command\":\"setCoolingSetpoint\",\"arguments\":[24.0]}]}",
                body.getValue());
        assertEquals(0, handler.updatedStates);
        assertNull(handler.lastUpdatedChannel);
        assertNull(handler.lastUpdatedState);
        assertNull(handler.lastStatus);
    }

    @Test
    void handleCommandRoutesOptionalFanModeToOptionalModeCapability() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "fan-mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_STRING)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "airConditionerFanMode",
                                SmartThingsBindingConstants.ATTRIBUTE, "fanMode", SmartThingsBindingConstants.COMMAND,
                                "setFanMode"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, new StringType("windFree"));

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(api).sendCommand(eq(""), body.capture());
        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"custom.airConditionerOptionalMode\",\"command\":\"setAcOptionalMode\",\"arguments\":[\"windFree\"]}]}",
                body.getValue());
    }

    @Test
    void handleCommandRoutesNormalFanModeToFanModeCapability() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "fan-mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_STRING)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "airConditionerFanMode",
                                SmartThingsBindingConstants.ATTRIBUTE, "fanMode", SmartThingsBindingConstants.COMMAND,
                                "setFanMode"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, new StringType("high"));

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(api).sendCommand(eq(""), body.capture());
        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"airConditionerFanMode\",\"command\":\"setFanMode\",\"arguments\":[\"high\"]}]}",
                body.getValue());
    }

    @Test
    void handleCommandSendsFrameRemoteChannelCommandWithoutArguments() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        ChannelUID channelUID = new ChannelUID(thingUID, "remote", "channel-up");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_SWITCH)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "tvChannel",
                                SmartThingsBindingConstants.ATTRIBUTE, "tvChannel", SmartThingsBindingConstants.COMMAND,
                                "channelUp", SmartThingsBindingConstants.CONVERTER, "no-argument-command"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, OnOffType.ON);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(api).sendCommand(eq(""), body.capture());
        assertEquals("{\"commands\":[{\"component\":\"main\",\"capability\":\"tvChannel\",\"command\":\"channelUp\"}]}",
                body.getValue());
    }

    @Test
    void handleCommandIgnoresFrameRemoteChannelOffCommand() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        ChannelUID channelUID = new ChannelUID(thingUID, "remote", "channel-down");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_SWITCH)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "tvChannel",
                                SmartThingsBindingConstants.ATTRIBUTE, "tvChannel", SmartThingsBindingConstants.COMMAND,
                                "channelDown", SmartThingsBindingConstants.CONVERTER, "no-argument-command"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, OnOffType.OFF);

        verify(api, never()).sendCommand(anyString(), anyString());
    }

    @Test
    void handleCommandSendsFrameArtModeCommandWithoutArguments() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "art-mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_SWITCH)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "samsungvd.ambient",
                                SmartThingsBindingConstants.ATTRIBUTE, "ambient", SmartThingsBindingConstants.COMMAND,
                                "setAmbientOn", SmartThingsBindingConstants.CONVERTER, "no-argument-command"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, OnOffType.ON);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(api).sendCommand(eq(""), body.capture());
        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"samsungvd.ambient\",\"command\":\"setAmbientOn\"}]}",
                body.getValue());
    }

    @Test
    void handleCommandDoesNotUseNoArgumentConverterForChannelNameAlone() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Some_TV:account:tv");
        ChannelUID channelUID = new ChannelUID(thingUID, "remote", "channel-up");
        Thing thing = ThingBuilder.create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Some_TV"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_SWITCH)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "tvChannel",
                                SmartThingsBindingConstants.ATTRIBUTE, "tvChannel", SmartThingsBindingConstants.COMMAND,
                                "channelUp"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, OnOffType.ON);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(api).sendCommand(eq(""), body.capture());
        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"tvChannel\",\"command\":\"channelUp\",\"arguments\":[\"on\"]}]}",
                body.getValue());
    }

    @Test
    void handleCommandKeepsNonAirConditionerFanModeOnOriginalCapability() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Other_Device:account:other-device");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "fan-mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Other_Device"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_STRING)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "custom.fanMode",
                                SmartThingsBindingConstants.ATTRIBUTE, "fanMode", SmartThingsBindingConstants.COMMAND,
                                "setFanMode"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, new StringType("sleep"));

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(api).sendCommand(eq(""), body.capture());
        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"custom.fanMode\",\"command\":\"setFanMode\",\"arguments\":[\"sleep\"]}]}",
                body.getValue());
    }

    @Test
    void handleCommandSendsSoundbarInputSourceCommand() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Soundbar:account:soundbar");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "input-source");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Soundbar"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_STRING)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "samsungvd.audioInputSource",
                                SmartThingsBindingConstants.ATTRIBUTE, "inputSource",
                                SmartThingsBindingConstants.COMMAND, "setInputSource"))
                        .build())
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, new StringType("wifi"));

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(api).sendCommand(eq(""), body.capture());
        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"samsungvd.audioInputSource\",\"command\":\"setInputSource\",\"arguments\":[\"wifi\"]}]}",
                body.getValue());
    }

    @Test
    void handleCommandKeepsThingStatusOnConversionFailure() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "fan-mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "control", "fan-mode", SmartThingsBindingConstants.TYPE_STRING,
                        "airConditionerFanMode", "fanMode"))
                .build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.handleCommand(api, channelUID, new StringType("auto"));

        verify(api, never()).sendCommand(anyString(), anyString());
        assertEquals(0, handler.updatedStates);
        assertNull(handler.lastStatus);
    }

    @Test
    void handleCommandKeepsThingStatusOnSendFailure() throws SmartThingsException {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "switch");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createSwitchChannel(thingUID, "control")).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());
        when(api.sendCommand(anyString(), anyString())).thenThrow(new SmartThingsException("bad command"));

        handler.handleCommand(api, channelUID, OnOffType.ON);

        assertEquals(0, handler.updatedStates);
        assertNull(handler.lastStatus);
    }

    @Test
    void refreshDeviceKeepsThingStatusOnStatusFailure() throws SmartThingsException {
        Thing thing = ThingBuilder.create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"),
                new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv")).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsApi api = mock(SmartThingsApi.class);
        when(api.getStatus(anyString())).thenThrow(new SmartThingsException("status failed"));
        SmartThingsAccountHandler accountHandler = mock(SmartThingsAccountHandler.class);
        when(accountHandler.getSmartThingsApi()).thenReturn(api);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        handler.bridge = bridge;

        handler.refreshDevice();

        assertNull(handler.lastStatus);
    }

    @Test
    void refreshDeviceSetsThingOfflineAfterRepeatedCommunicationFailures() throws SmartThingsException {
        Thing thing = ThingBuilder.create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"),
                new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv")).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsException networkFailure = new SmartThingsException("network failed", new Exception("timed out"),
                true);
        SmartThingsException statusFailure = new SmartThingsException("status failed", networkFailure);
        SmartThingsApi api = mock(SmartThingsApi.class);
        when(api.getStatus(anyString())).thenThrow(statusFailure);
        SmartThingsAccountHandler accountHandler = mock(SmartThingsAccountHandler.class);
        when(accountHandler.getSmartThingsApi()).thenReturn(api);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        handler.bridge = bridge;

        handler.refreshDevice();
        assertNull(handler.lastStatus);

        handler.refreshDevice();
        assertNull(handler.lastStatus);

        handler.refreshDevice();

        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.lastStatusDetail);
    }

    @Test
    void refreshDeviceReturnsOnlineAfterSuccessfulStatusRefresh() throws SmartThingsException {
        Thing thing = ThingBuilder.create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"),
                new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv")).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsException networkFailure = new SmartThingsException("network failed", new Exception("timed out"),
                true);
        SmartThingsException statusFailure = new SmartThingsException("status failed", networkFailure);
        SmartThingsStatus status = new SmartThingsStatus();
        status.components = new Hashtable<>();
        SmartThingsApi api = mock(SmartThingsApi.class);
        when(api.getStatus(anyString())).thenThrow(statusFailure).thenThrow(statusFailure).thenThrow(statusFailure)
                .thenReturn(status);
        SmartThingsAccountHandler accountHandler = mock(SmartThingsAccountHandler.class);
        when(accountHandler.getSmartThingsApi()).thenReturn(api);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler.bridge = bridge;

        handler.refreshDevice();
        handler.refreshDevice();
        handler.refreshDevice();
        handler.refreshDevice();

        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.NONE, handler.lastStatusDetail);
    }

    @Test
    void refreshDeviceKeepsCommunicationErrorWhenBridgeIsOffline() throws SmartThingsException {
        Thing thing = ThingBuilder.create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"),
                new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv")).build();
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "previous communication error"));
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsStatus status = new SmartThingsStatus();
        status.components = new Hashtable<>();
        SmartThingsApi api = mock(SmartThingsApi.class);
        when(api.getStatus(anyString())).thenReturn(status);
        SmartThingsAccountHandler accountHandler = mock(SmartThingsAccountHandler.class);
        when(accountHandler.getSmartThingsApi()).thenReturn(api);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);
        when(bridge.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.OFFLINE,
                ThingStatusDetail.COMMUNICATION_ERROR, "bridge communication error"));
        handler.bridge = bridge;

        handler.refreshDevice();

        assertNull(handler.lastStatus);
    }

    @Test
    void eventRefreshReturnsThingOnlineWhenBridgeIsOnline() {
        Thing thing = createAirConditionerThing();
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "previous communication error"));
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler.bridge = bridge;
        SmartThingsConverterFactory.registerConverters(new SmartThingsTypeRegistryImpl());

        handler.refreshDevice("Samsung_Room_A_C", "main", "switch", "switch", "on");

        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.NONE, handler.lastStatusDetail);
        assertEquals(OnOffType.ON, handler.lastUpdatedState);
    }

    @Test
    void statusRefreshCommunicationFailureCounterSurvivesOtherStatusFailures() throws SmartThingsException {
        Thing thing = ThingBuilder.create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"),
                new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv")).build();
        TestSmartThingsThingHandler handler = new TestSmartThingsThingHandler(thing);
        SmartThingsException networkFailure = new SmartThingsException("network failed", new Exception("timed out"),
                true);
        SmartThingsException statusFailure = new SmartThingsException("status failed", networkFailure);
        SmartThingsApi api = mock(SmartThingsApi.class);
        when(api.getStatus(anyString())).thenThrow(statusFailure).thenThrow(new SmartThingsException("api failed"))
                .thenThrow(statusFailure).thenThrow(statusFailure);
        SmartThingsAccountHandler accountHandler = mock(SmartThingsAccountHandler.class);
        when(accountHandler.getSmartThingsApi()).thenReturn(api);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        handler.bridge = bridge;

        handler.refreshDevice();
        handler.refreshDevice();
        handler.refreshDevice();
        assertNull(handler.lastStatus);

        handler.refreshDevice();

        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.lastStatusDetail);
    }

    @Test
    void defaultConverterSendsQuantityCommandAsNumericSetterArgument() throws SmartThingsException {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("thermostatCoolingSetpoint"))
                .thenReturn(createSetterCapability("thermostatCoolingSetpoint", "coolingSetpoint",
                        SmartThingsBindingConstants.SM_TYPE_NUMBER, "setCoolingSetpoint"));
        SmartThingsDefaultConverter converter = new SmartThingsDefaultConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "setpoint");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "control", "setpoint", SmartThingsBindingConstants.TYPE_NUMBER,
                        "thermostatCoolingSetpoint", "coolingSetpoint"))
                .build();

        String json = converter.convertToSmartThings(thing, channelUID, new QuantityType<>("21 °C"));

        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"thermostatCoolingSetpoint\",\"command\":\"setCoolingSetpoint\",\"arguments\":[21.0]}]}",
                json);
    }

    @Test
    void defaultConverterUsesEnumCommandMapping() throws SmartThingsException {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("airConditionerMode"))
                .thenReturn(createEnumCapability("airConditionerMode", "airConditionerMode", "cool", "setCoolMode"));
        SmartThingsDefaultConverter converter = new SmartThingsDefaultConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createChannel(thingUID, "control", "mode", SmartThingsBindingConstants.TYPE_STRING,
                        "airConditionerMode", "airConditionerMode"))
                .build();

        String json = converter.convertToSmartThings(thing, channelUID, new StringType("cool"));

        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"airConditionerMode\",\"command\":\"setCoolMode\"}]}",
                json);
    }

    @Test
    void defaultConverterPrefersEnumCommandOverStaticCommandMetadata() throws SmartThingsException {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("airConditionerMode"))
                .thenReturn(createEnumCapability("airConditionerMode", "airConditionerMode", "cool", "setCoolMode"));
        SmartThingsDefaultConverter converter = new SmartThingsDefaultConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_STRING)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "airConditionerMode",
                                SmartThingsBindingConstants.ATTRIBUTE, "airConditionerMode",
                                SmartThingsBindingConstants.COMMAND, "setAirConditionerMode"))
                        .build())
                .build();

        String json = converter.convertToSmartThings(thing, channelUID, new StringType("cool"));

        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"airConditionerMode\",\"command\":\"setCoolMode\"}]}",
                json);
    }

    @Test
    void defaultConverterUsesStaticCommandMetadataWhenCapabilityHasNoSetter() throws SmartThingsException {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("airConditionerFanMode")).thenReturn(createAttributeOnlyCapability(
                "airConditionerFanMode", "fanMode", SmartThingsBindingConstants.SM_TYPE_STRING));
        SmartThingsDefaultConverter converter = new SmartThingsDefaultConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "fan-mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(ChannelBuilder.create(channelUID, SmartThingsBindingConstants.TYPE_STRING)
                        .withProperties(Map.of(SmartThingsBindingConstants.COMPONENT, "main",
                                SmartThingsBindingConstants.CAPABILITY, "airConditionerFanMode",
                                SmartThingsBindingConstants.ATTRIBUTE, "fanMode", SmartThingsBindingConstants.COMMAND,
                                "setFanMode"))
                        .build())
                .build();

        String json = converter.convertToSmartThings(thing, channelUID, new StringType("turbo"));

        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"airConditionerFanMode\",\"command\":\"setFanMode\",\"arguments\":[\"turbo\"]}]}",
                json);
    }

    @Test
    void airConditionerFanModeConverterKeepsOtherFanModeCapabilitiesUnchanged() throws SmartThingsException {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("custom.fanMode")).thenReturn(createSetterCapability("custom.fanMode", "fanMode",
                SmartThingsBindingConstants.SM_TYPE_STRING, "setFanMode"));
        SmartThingsAirConditionerFanModeConverter converter = new SmartThingsAirConditionerFanModeConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Other_Device:account:other-device");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "fan-mode");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Other_Device"), thingUID)
                .withChannel(createChannel(thingUID, "control", "fan-mode", SmartThingsBindingConstants.TYPE_STRING,
                        "custom.fanMode", "fanMode"))
                .build();

        String json = converter.convertToSmartThings(thing, channelUID, new StringType("sleep"));

        assertEquals(
                "{\"commands\":[{\"component\":\"main\",\"capability\":\"custom.fanMode\",\"command\":\"setFanMode\",\"arguments\":[\"sleep\"]}]}",
                json);
    }

    @Test
    void defaultConverterMapsPlaybackCommandsToSmartThingsPlaybackStatusValues() throws SmartThingsException {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("mediaPlayback")).thenReturn(createSetterCapability("mediaPlayback",
                "playbackStatus", SmartThingsBindingConstants.SM_TYPE_STRING, "setPlaybackStatus"));
        SmartThingsDefaultConverter converter = new SmartThingsDefaultConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "playback");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(createChannel(thingUID, "control", "playback", SmartThingsBindingConstants.TYPE_PLAYER,
                        "mediaPlayback", "playbackStatus"))
                .build();

        assertEquals(playbackCommandJson("playing"),
                converter.convertToSmartThings(thing, channelUID, PlayPauseType.PLAY));
        assertEquals(playbackCommandJson("paused"),
                converter.convertToSmartThings(thing, channelUID, PlayPauseType.PAUSE));
        assertEquals(playbackCommandJson("rewinding"),
                converter.convertToSmartThings(thing, channelUID, RewindFastforwardType.REWIND));
        assertEquals(playbackCommandJson("fast forwarding"),
                converter.convertToSmartThings(thing, channelUID, RewindFastforwardType.FASTFORWARD));
    }

    @Test
    void defaultConverterRejectsUnsupportedPlaybackNavigationCommands() {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("mediaPlayback")).thenReturn(createSetterCapability("mediaPlayback",
                "playbackStatus", SmartThingsBindingConstants.SM_TYPE_STRING, "setPlaybackStatus"));
        SmartThingsDefaultConverter converter = new SmartThingsDefaultConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "playback");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(createChannel(thingUID, "control", "playback", SmartThingsBindingConstants.TYPE_PLAYER,
                        "mediaPlayback", "playbackStatus"))
                .build();

        assertThrows(SmartThingsException.class,
                () -> converter.convertToSmartThings(thing, channelUID, NextPreviousType.NEXT));
        assertThrows(SmartThingsException.class,
                () -> converter.convertToSmartThings(thing, channelUID, NextPreviousType.PREVIOUS));
    }

    @Test
    void mediaControlConverterMapsNavigationCommandsToTrackControlCommands() throws SmartThingsException {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("mediaPlayback")).thenReturn(createSetterCapability("mediaPlayback",
                "playbackStatus", SmartThingsBindingConstants.SM_TYPE_STRING, "setPlaybackStatus"));
        SmartThingsMediaControlConverter converter = new SmartThingsMediaControlConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "playback");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(createChannel(thingUID, "control", "playback", SmartThingsBindingConstants.TYPE_PLAYER,
                        "mediaPlayback", "playbackStatus"))
                .build();

        assertEquals(mediaTrackControlCommandJson("nextTrack"),
                converter.convertToSmartThings(thing, channelUID, NextPreviousType.NEXT));
        assertEquals(mediaTrackControlCommandJson("previousTrack"),
                converter.convertToSmartThings(thing, channelUID, NextPreviousType.PREVIOUS));
        assertEquals(playbackCommandJson("playing"),
                converter.convertToSmartThings(thing, channelUID, PlayPauseType.PLAY));
    }

    @Test
    void defaultConverterMapsSupportedPlaybackCommandsToDirectMediaPlaybackCommands() throws SmartThingsException {
        SmartThingsTypeRegistry registry = mock(SmartThingsTypeRegistry.class);
        when(registry.getCapability("mediaPlayback")).thenReturn(createAttributeOnlyCapability("mediaPlayback",
                "supportedPlaybackCommands", SmartThingsBindingConstants.SM_TYPE_ARRAY));
        SmartThingsDefaultConverter converter = new SmartThingsDefaultConverter(registry);
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Soundbar:account:soundbar");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "playback");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Soundbar"), thingUID)
                .withChannel(createChannel(thingUID, "control", "playback", SmartThingsBindingConstants.TYPE_PLAYER,
                        "mediaPlayback", "supportedPlaybackCommands"))
                .build();

        assertEquals(directMediaPlaybackCommandJson("play"),
                converter.convertToSmartThings(thing, channelUID, PlayPauseType.PLAY));
        assertEquals(directMediaPlaybackCommandJson("pause"),
                converter.convertToSmartThings(thing, channelUID, PlayPauseType.PAUSE));
        assertEquals(directMediaPlaybackCommandJson("stop"),
                converter.convertToSmartThings(thing, channelUID, StopMoveType.STOP));

        assertThrows(SmartThingsException.class,
                () -> converter.convertToSmartThings(thing, channelUID, NextPreviousType.NEXT));
        assertThrows(SmartThingsException.class,
                () -> converter.convertToSmartThings(thing, channelUID, NextPreviousType.PREVIOUS));
        assertThrows(SmartThingsException.class,
                () -> converter.convertToSmartThings(thing, channelUID, RewindFastforwardType.REWIND));
        assertThrows(SmartThingsException.class,
                () -> converter.convertToSmartThings(thing, channelUID, RewindFastforwardType.FASTFORWARD));
    }

    @Test
    void defaultConverterMapsStoppedPlaybackStatusToPauseState() throws SmartThingsException {
        SmartThingsDefaultConverter converter = new SmartThingsDefaultConverter(mock(SmartThingsTypeRegistry.class));
        ThingUID thingUID = new ThingUID("smartthings:Samsung_The_Frame:account:frame-tv");
        ChannelUID channelUID = new ChannelUID(thingUID, "control", "playback");
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_The_Frame"), thingUID)
                .withChannel(createChannel(thingUID, "control", "playback", SmartThingsBindingConstants.TYPE_PLAYER,
                        "mediaPlayback", "playbackStatus"))
                .build();

        assertEquals(PlayPauseType.PAUSE, converter.convertToOpenHab(thing, channelUID, "stopped"));
    }

    private Thing createAirConditionerThing() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");

        return ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createSwitchChannel(thingUID, "control")).build();
    }

    private Channel createSwitchChannel(ThingUID thingUID, String groupId) {
        return createChannel(thingUID, groupId, "switch", SmartThingsBindingConstants.TYPE_SWITCH, "switch", "switch");
    }

    private Channel createChannel(ThingUID thingUID, String groupId, String channelId, String itemType,
            String capability, String attribute) {
        return ChannelBuilder.create(new ChannelUID(thingUID, groupId, channelId), itemType)
                .withProperties(
                        Map.of(SmartThingsBindingConstants.COMPONENT, "main", SmartThingsBindingConstants.CAPABILITY,
                                capability, SmartThingsBindingConstants.ATTRIBUTE, attribute))
                .build();
    }

    private String playbackCommandJson(String playbackStatus) {
        return "{\"commands\":[{\"component\":\"main\",\"capability\":\"mediaPlayback\",\"command\":\"setPlaybackStatus\",\"arguments\":[\""
                + playbackStatus + "\"]}]}";
    }

    private String directMediaPlaybackCommandJson(String command) {
        return "{\"commands\":[{\"component\":\"main\",\"capability\":\"mediaPlayback\",\"command\":\"" + command
                + "\"}]}";
    }

    private String mediaTrackControlCommandJson(String command) {
        return "{\"commands\":[{\"component\":\"main\",\"capability\":\"mediaTrackControl\",\"command\":\"" + command
                + "\"}]}";
    }

    private SmartThingsCapability createSetterCapability(String capabilityId, String attributeId, String valueType,
            String commandName) {
        SmartThingsCapability capability = createCapability(capabilityId);
        SmartThingsAttribute attribute = createAttribute(attributeId, valueType);
        attribute.setter = commandName;
        capability.attributes.put(attributeId, attribute);

        SmartThingsCommand command = new SmartThingsCommand();
        command.name = commandName;
        command.arguments = new SmartThingsArgument[] { new SmartThingsArgument() };
        capability.commands.put(commandName, command);
        return capability;
    }

    private SmartThingsCapability createEnumCapability(String capabilityId, String attributeId, String value,
            String commandName) {
        SmartThingsCapability capability = createCapability(capabilityId);
        SmartThingsAttribute attribute = createAttribute(attributeId, SmartThingsBindingConstants.SM_TYPE_STRING);
        SmartThingsEnumCommand enumCommand = new SmartThingsEnumCommand();
        enumCommand.value = value;
        enumCommand.command = commandName;
        attribute.enumCommands = new SmartThingsEnumCommand[] { enumCommand };
        capability.attributes.put(attributeId, attribute);

        SmartThingsCommand command = new SmartThingsCommand();
        command.name = commandName;
        command.arguments = new SmartThingsArgument[0];
        capability.commands.put(commandName, command);
        return capability;
    }

    private SmartThingsCapability createAttributeOnlyCapability(String capabilityId, String attributeId,
            String valueType) {
        SmartThingsCapability capability = createCapability(capabilityId);
        capability.attributes.put(attributeId, createAttribute(attributeId, valueType));
        return capability;
    }

    private SmartThingsCapability createCapability(String capabilityId) {
        SmartThingsCapability capability = new SmartThingsCapability();
        capability.id = capabilityId;
        capability.attributes = new Hashtable<>();
        capability.commands = new Hashtable<>();
        return capability;
    }

    private SmartThingsAttribute createAttribute(String attributeId, String valueType) {
        SmartThingsAttribute attribute = new SmartThingsAttribute();
        attribute.schema = new SmartThingsSchema();
        attribute.schema.properties = new Hashtable<>();

        SmartThingsProperty value = new SmartThingsProperty();
        value.type = valueType;
        value.title = attributeId;
        attribute.schema.properties.put("value", value);
        return attribute;
    }

    private static class TestSmartThingsThingHandler extends SmartThingsThingHandler {
        private int updatedStates;
        private @Nullable ChannelUID lastUpdatedChannel;
        private @Nullable State lastUpdatedState;
        private @Nullable ThingStatus lastStatus;
        private @Nullable ThingStatusDetail lastStatusDetail;
        private @Nullable Bridge bridge;
        private Map<String, String> lastUpdatedProperties = Map.of();

        TestSmartThingsThingHandler(Thing thing) {
            super(thing);
        }

        @Override
        protected void updateState(ChannelUID channelUID, State state) {
            updatedStates++;
            lastUpdatedChannel = channelUID;
            lastUpdatedState = state;
        }

        @Override
        protected void updateProperties(@Nullable Map<String, String> properties) {
            if (properties != null) {
                lastUpdatedProperties = new HashMap<>(properties);
            }
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
            lastStatus = status;
            lastStatusDetail = statusDetail;
            getThing().setStatusInfo(new ThingStatusInfo(status, statusDetail, description));
        }

        @Override
        public @Nullable Bridge getBridge() {
            return bridge;
        }
    }
}
