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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.converter.SmartThingsConverterFactory;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistryImpl;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;

/**
 * Tests for {@link SmartThingsThingHandler}.
 */
@NonNullByDefault
class SmartThingsThingHandlerTest {

    @Test
    void resolveDeviceIdFallsBackToDiscoveredThingProperty() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Washer"),
                        new ThingUID("smartthings:Washer:account:Washer"))
                .withProperties(Map.of(SmartThingsBindingConstants.DEVICE_ID, "device-123")).build();
        SmartThingsThingHandler handler = new SmartThingsThingHandler(thing);

        assertEquals("device-123", handler.resolveDeviceId());
    }

    @Test
    void resolveDeviceIdPrefersThingConfiguration() {
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
        assertEquals(new ChannelUID(handler.getThing().getUID(), "main", "switch"), handler.lastUpdatedChannel);
        assertEquals(OnOffType.ON, handler.lastUpdatedState);
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

    private Thing createAirConditionerThing() {
        ThingUID thingUID = new ThingUID("smartthings:Samsung_Room_A_C:account:air-conditioner");

        return ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Samsung_Room_A_C"), thingUID)
                .withChannel(createSwitchChannel(thingUID, "main")).build();
    }

    private Channel createSwitchChannel(ThingUID thingUID, String groupId) {
        return ChannelBuilder
                .create(new ChannelUID(thingUID, groupId, "switch"), SmartThingsBindingConstants.TYPE_SWITCH)
                .withProperties(
                        Map.of(SmartThingsBindingConstants.COMPONENT, "main", SmartThingsBindingConstants.CAPABILITY,
                                "switch", SmartThingsBindingConstants.ATTRIBUTE, "switch"))
                .build();
    }

    private static class TestSmartThingsThingHandler extends SmartThingsThingHandler {
        private int updatedStates;
        private @Nullable ChannelUID lastUpdatedChannel;
        private @Nullable State lastUpdatedState;

        TestSmartThingsThingHandler(Thing thing) {
            super(thing);
        }

        @Override
        protected void updateState(ChannelUID channelUID, State state) {
            updatedStates++;
            lastUpdatedChannel = channelUID;
            lastUpdatedState = state;
        }
    }
}
