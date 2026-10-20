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
package org.openhab.binding.hue.internal.clip2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.hue.internal.api.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.api.dto.clip2.Resources;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.handler.Clip2BridgeHandler;
import org.openhab.binding.hue.internal.handler.Clip2StateDescriptionProvider;
import org.openhab.binding.hue.internal.handler.Clip2ThingHandler;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * Tests for the back light handling of {@link Clip2ThingHandler}, as used by Hue Twilight devices.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class HueTwilightTest {

    /**
     * Invokes the private updateChannels() method of the handler.
     */
    private static boolean updateChannels(Clip2ThingHandler handler, Resource resource) throws Exception {
        var method = Clip2ThingHandler.class.getDeclaredMethod("updateChannels", Resource.class);
        method.setAccessible(true);
        return (boolean) method.invoke(handler, resource);
    }

    /**
     * A {@link Clip2ThingHandler} subclass that exposes the bridge and records updated things and commands.
     */
    private static class TestHandler extends Clip2ThingHandler {

        @Nullable
        Bridge bridge;
        final List<Thing> updatedThings = new ArrayList<>();
        final List<Map.Entry<ChannelUID, Command>> commands = new ArrayList<>();
        boolean recordCommands;
        java.util.function.Consumer<Thing> onUpdateThing = t -> {
        };

        TestHandler(Thing thing, Clip2StateDescriptionProvider provider, ThingRegistry registry,
                ItemChannelLinkRegistry links) {
            super(thing, provider, registry, links);
        }

        @Override
        protected @Nullable Bridge getBridge() {
            return bridge;
        }

        @Override
        protected void updateThing(Thing thing) {
            updatedThings.add(thing);
            onUpdateThing.accept(thing);
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            if (recordCommands) {
                commands.add(Map.entry(channelUID, command));
            } else {
                super.handleCommand(channelUID, command);
            }
        }
    }

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_DEVICE, "twilight");
    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_BRIDGE_API2, "bridge");
    private static final ChannelTypeUID TYPE_UID = new ChannelTypeUID(BINDING_ID, "test-type");
    private static final String FRONT_ID = "front-light-id";
    private static final String BACK_ID = "back-light-id";

    private static ChannelUID mainUID(String id) {
        return new ChannelUID(THING_UID, id);
    }

    private static ChannelUID backUID(String id) {
        return new ChannelUID(THING_UID, BACK_LIGHT_GROUP_ID, id);
    }

    /**
     * A handler under test with a mocked bridge handler, a mutable channel list, and access to its private fields.
     */
    private static class Fixture {
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final Clip2StateDescriptionProvider stateProvider = mock(Clip2StateDescriptionProvider.class);
        final Clip2BridgeHandler bridgeHandler = mock(Clip2BridgeHandler.class);
        final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        final List<Channel> channels = new ArrayList<>();
        final Thing thing;
        final TestHandler handler;

        Fixture(List<String> mainChannelIds, List<String> backChannelIds) throws Exception {
            thing = spy(ThingBuilder.create(THING_TYPE_DEVICE, THING_UID).withBridge(BRIDGE_UID).build());
            mainChannelIds.forEach(id -> channels.add(channel(mainUID(id))));
            backChannelIds.forEach(id -> channels.add(channel(backUID(id))));
            doAnswer(i -> List.copyOf(channels)).when(thing).getChannels();
            doAnswer(i -> find(i.getArgument(0))).when(thing).getChannel(any(ChannelUID.class));
            doAnswer(i -> find(new ChannelUID(THING_UID, i.getArgument(0)))).when(thing).getChannel(anyString());

            handler = new TestHandler(thing, stateProvider, mock(ThingRegistry.class),
                    mock(ItemChannelLinkRegistry.class));
            handler.setCallback(callback);
            Bridge bridge = mock(Bridge.class);
            when(bridge.getHandler()).thenReturn(bridgeHandler);
            handler.bridge = bridge;

            // the handler's scheduler is a protected field of BaseThingHandler
            setInheritedField(handler, "scheduler", scheduler);
            when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                    .thenAnswer(i -> mock(ScheduledFuture.class));

            setField(Clip2ThingHandler.class, handler, "resourceId", "device-id");
            setField(Clip2ThingHandler.class, handler, "thisResource", new Resource(ResourceType.DEVICE));
            setBackLightId(BACK_ID);
            commandIds().put(ResourceType.LIGHT, FRONT_ID);

            when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                    .thenAnswer(i -> mock(ScheduledFuture.class));
        }

        private static void setInheritedField(Object target, String name, @Nullable Object value) throws Exception {
            Class<?> type = target.getClass();
            while (type != null) {
                try {
                    Field field = type.getDeclaredField(name);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException e) {
                    type = type.getSuperclass();
                }
            }
            throw new NoSuchFieldException(name);
        }

        Channel channel(ChannelUID uid) {
            return ChannelBuilder.create(uid, "Dimmer").withType(TYPE_UID).build();
        }

        @Nullable
        Channel find(ChannelUID uid) {
            return channels.stream().filter(c -> c.getUID().equals(uid)).findFirst().orElse(null);
        }

        void setBackLightId(@Nullable String id) throws Exception {
            setField(Clip2ThingHandler.class, handler, "backLightResourceId", id);
        }

        @SuppressWarnings("unchecked")
        Map<ResourceType, String> commandIds() throws Exception {
            return (Map<ResourceType, String>) getField("commandResourceIds");
        }

        @SuppressWarnings("unchecked")
        Map<String, Resource> contributors() throws Exception {
            return (Map<String, Resource>) getField("serviceContributorsCache");
        }

        @SuppressWarnings("unchecked")
        Set<String> backSupported() throws Exception {
            return (Set<String>) getField("backLightSupportedChannelIds");
        }

        @SuppressWarnings("unchecked")
        Set<String> mainSupported() throws Exception {
            return (Set<String>) getField("supportedChannelIds");
        }

        Object getField(String name) throws Exception {
            Field field = Clip2ThingHandler.class.getDeclaredField(name);
            field.setAccessible(true);
            return Objects.requireNonNull(field.get(handler));
        }

        /**
         * Runs the private updateChannelList() method.
         */
        void updateChannelList() throws Exception {
            var method = Clip2ThingHandler.class.getDeclaredMethod("updateChannelList");
            method.setAccessible(true);
            method.invoke(handler);
        }

        /**
         * Captures the Resource sent to the bridge and returns it after the command has been handled.
         */
        Resource putCapture(ChannelUID uid, Command command) throws Exception {
            ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
            when(bridgeHandler.putResource(captor.capture())).thenReturn(new Resources());
            handler.handleCommandInner(uid, command);
            return captor.getValue();
        }
    }

    private static void setField(Class<?> type, Object target, String name, @Nullable Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Creates a light resource with the given id, on/off state, and brightness.
     */
    private static Resource light(String id, boolean on, double brightness) {
        Resource resource = new Resource(ResourceType.LIGHT).setId(id);
        resource.setOnOff(OnOffType.from(on));
        resource.setBrightness(new PercentType((int) brightness));
        return resource;
    }

    @Test
    void backLightCommandTargetsBackLightResourceId() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH), List.of(CHANNEL_2_SWITCH));

        Resource put = f.putCapture(backUID(CHANNEL_2_SWITCH), OnOffType.ON);

        assertEquals(BACK_ID, put.getId());
    }

    @Test
    void mainCommandTargetsMainLightResourceId() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH), List.of(CHANNEL_2_SWITCH));

        Resource put = f.putCapture(mainUID(CHANNEL_2_SWITCH), OnOffType.ON);

        assertEquals(FRONT_ID, put.getId());
        assertNotEquals(BACK_ID, put.getId());
    }

    @Test
    void backLightCommandUsesBackLightCache() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_BRIGHTNESS), List.of(CHANNEL_2_BRIGHTNESS));
        f.contributors().put(FRONT_ID, lightWithBrightness(FRONT_ID, 80));
        f.contributors().put(BACK_ID, lightWithBrightness(BACK_ID, 20));

        when(f.bridgeHandler.getResources(any(ResourceReference.class))).thenAnswer(i -> {
            ResourceReference ref = i.getArgument(0);
            int percent = BACK_ID.equals(ref.getId()) ? 20 : 80;
            Resources resources = new Resources();
            resources.getResources().add(lightWithBrightness(Objects.requireNonNull(ref.getId()), percent));
            return resources;
        });

        Resource backPut = f.putCapture(backUID(CHANNEL_2_BRIGHTNESS), IncreaseDecreaseType.INCREASE);
        Resource frontPut = f.putCapture(mainUID(CHANNEL_2_BRIGHTNESS), IncreaseDecreaseType.INCREASE);

        assertEquals(BACK_ID, backPut.getId());
        assertEquals(FRONT_ID, frontPut.getId());

        // each increase is relative to its own light's brightness
        double backBrightness = ((PercentType) backPut.getBrightnessState()).doubleValue();
        double frontBrightness = ((PercentType) frontPut.getBrightnessState()).doubleValue();
        assertTrue(backBrightness > 20 && backBrightness < 50, "back light must start from 20%");
        assertTrue(frontBrightness > 80, "front light must start from 80%");
    }

    private static Resource lightWithBrightness(String id, int percent) {
        Resource resource = new Resource(ResourceType.LIGHT).setId(id);
        resource.setBrightness(new PercentType(percent));
        return resource;
    }

    @Test
    void backLightResourceUpdatesBackChannelsOnly() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS),
                List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS));

        updateChannels(f.handler, light(BACK_ID, true, 50));

        verify(f.callback, atLeastOnce()).stateUpdated(eq(backUID(CHANNEL_2_SWITCH)), any(State.class));
        verify(f.callback, atLeastOnce()).stateUpdated(eq(backUID(CHANNEL_2_BRIGHTNESS)), any(State.class));
        verify(f.callback, never()).stateUpdated(eq(mainUID(CHANNEL_2_SWITCH)), any(State.class));
        verify(f.callback, never()).stateUpdated(eq(mainUID(CHANNEL_2_BRIGHTNESS)), any(State.class));
    }

    // 6
    @Test
    void frontLightResourceUpdatesMainChannelsOnly() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS),
                List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS));

        updateChannels(f.handler, light(FRONT_ID, true, 50));

        verify(f.callback, atLeastOnce()).stateUpdated(eq(mainUID(CHANNEL_2_SWITCH)), any(State.class));
        verify(f.callback, never()).stateUpdated(eq(backUID(CHANNEL_2_SWITCH)), any(State.class));
        verify(f.callback, never()).stateUpdated(eq(backUID(CHANNEL_2_BRIGHTNESS)), any(State.class));
    }

    @Test
    void backChannelsAreCreatedFromMainChannelTemplate() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH, CHANNEL_2_EFFECT), List.of());
        f.backSupported().addAll(Set.of(CHANNEL_2_SWITCH, CHANNEL_2_EFFECT));
        f.mainSupported().addAll(Set.of(CHANNEL_2_SWITCH, CHANNEL_2_EFFECT));

        f.updateChannelList();

        assertEquals(1, f.handler.updatedThings.size());
        List<Channel> created = f.handler.updatedThings.get(0).getChannels();
        Channel backSwitch = created.stream().filter(c -> c.getUID().equals(backUID(CHANNEL_2_SWITCH))).findFirst()
                .orElse(null);
        assertNotNull(backSwitch);
        assertEquals(TYPE_UID, backSwitch.getChannelTypeUID());
        assertTrue(created.stream().anyMatch(c -> c.getUID().equals(backUID(CHANNEL_2_EFFECT))));
        assertTrue(created.stream().anyMatch(c -> c.getUID().equals(mainUID(CHANNEL_2_SWITCH))));
    }

    @Test
    void newBackChannelsPushCachedState() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH), List.of());
        f.backSupported().add(CHANNEL_2_SWITCH);
        f.mainSupported().add(CHANNEL_2_SWITCH);
        f.contributors().put(BACK_ID, light(BACK_ID, true, 40));
        // make updateThing() take effect on the fake channel list, as the framework would
        f.handler.onUpdateThing = updated -> {
            f.channels.clear();
            f.channels.addAll(updated.getChannels());
        };

        f.updateChannelList();

        verify(f.callback, atLeastOnce()).stateUpdated(eq(backUID(CHANNEL_2_SWITCH)), any(State.class));
    }

    // 11
    @Test
    void unusedBackChannelsAreRemoved() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS),
                List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS));
        f.mainSupported().addAll(Set.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS));
        f.backSupported().add(CHANNEL_2_SWITCH); // back brightness is NOT supported

        f.updateChannelList();

        assertEquals(1, f.handler.updatedThings.size());
        List<ChannelUID> uids = f.handler.updatedThings.get(0).getChannels().stream().map(Channel::getUID).toList();
        assertTrue(uids.contains(backUID(CHANNEL_2_SWITCH)));
        assertFalse(uids.contains(backUID(CHANNEL_2_BRIGHTNESS)));
        assertTrue(uids.contains(mainUID(CHANNEL_2_BRIGHTNESS)));
    }

    @Test
    void alertResetTasksAreIndependentPerLight() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_ALERT), List.of(CHANNEL_2_ALERT));
        ScheduledFuture<?> frontReset = mock(ScheduledFuture.class);
        ScheduledFuture<?> backReset = mock(ScheduledFuture.class);
        when(f.scheduler.schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS))).thenAnswer(i -> frontReset)
                .thenAnswer(i -> backReset);
        when(f.bridgeHandler.putResource(any(Resource.class))).thenReturn(new Resources());

        f.handler.handleCommandInner(mainUID(CHANNEL_2_ALERT),
                new org.openhab.core.library.types.StringType("BREATHE"));
        f.handler.handleCommandInner(backUID(CHANNEL_2_ALERT),
                new org.openhab.core.library.types.StringType("BREATHE"));

        // the back light alert must not cancel the front light's pending reset
        verify(frontReset, never()).cancel(anyBoolean());
        verify(f.scheduler, times(2)).schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS));

        // a second front alert does cancel only the front light's own reset
        when(f.scheduler.schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS)))
                .thenAnswer(i -> mock(ScheduledFuture.class));
        f.handler.handleCommandInner(mainUID(CHANNEL_2_ALERT),
                new org.openhab.core.library.types.StringType("BREATHE"));
        verify(frontReset).cancel(false);
        verify(backReset, never()).cancel(anyBoolean());
    }

    @Test
    void dynamicsAreIndependentPerLight() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_BRIGHTNESS, CHANNEL_2_DYNAMICS),
                List.of(CHANNEL_2_BRIGHTNESS, CHANNEL_2_DYNAMICS));
        f.contributors().put(FRONT_ID, new Resource(ResourceType.LIGHT).setId(FRONT_ID));
        f.contributors().put(BACK_ID, new Resource(ResourceType.LIGHT).setId(BACK_ID));

        // set a 2 second dynamics duration for the back light only
        f.handler.handleCommandInner(backUID(CHANNEL_2_DYNAMICS),
                new QuantityType<>(2000, MetricPrefix.MILLI(Units.SECOND)));

        Resource backPut = f.putCapture(backUID(CHANNEL_2_BRIGHTNESS), new PercentType(50));
        Resource frontPut = f.putCapture(mainUID(CHANNEL_2_BRIGHTNESS), new PercentType(50));

        assertNotNull(backPut.getDynamics(), "back light command must carry the back light dynamics");
        assertNull(frontPut.getDynamics(), "front light command must not carry the back light dynamics");
    }

    @Test
    void handleDynamicsCommandNormalizesBackLightIds() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_BRIGHTNESS, CHANNEL_2_DYNAMICS),
                List.of(CHANNEL_2_BRIGHTNESS, CHANNEL_2_DYNAMICS));
        QuantityType<?> duration = new QuantityType<>(1500, MetricPrefix.MILLI(Units.SECOND));
        PercentType target = new PercentType(70);
        f.handler.recordCommands = true;

        f.handler.handleDynamicsCommand(BACK_LIGHT_GROUP_ID + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_2_BRIGHTNESS,
                target, duration);

        // the dynamics command comes first, then the target command, both on the back light channels
        assertEquals(List.of(Map.entry(backUID(CHANNEL_2_DYNAMICS), (Command) duration),
                Map.entry(backUID(CHANNEL_2_BRIGHTNESS), (Command) target)), f.handler.commands);
    }

    private static final Gson GSON = new Gson();

    /**
     * Creates a device Resource whose services reference the given light IDs, in the given order.
     */
    private static Resource deviceWithLights(String... lightIds) {
        StringBuilder services = new StringBuilder();
        for (String id : lightIds) {
            if (services.length() > 0) {
                services.append(',');
            }
            services.append("{\"rid\":\"").append(id).append("\",\"rtype\":\"light\"}");
        }
        return Objects
                .requireNonNull(GSON.fromJson("{\"type\":\"device\",\"services\":[" + services + "]}", Resource.class));
    }

    /**
     * Creates a light Resource carrying the given archetype metadata.
     */
    private static Resource lightWithArchetype(String id, String archetype) {
        Resource resource = new Resource(ResourceType.LIGHT).setId(id);
        resource.setMetadata(
                Objects.requireNonNull(GSON.fromJson("{\"archetype\":\"" + archetype + "\"}", MetaData.class)));
        return resource;
    }

    /**
     * Stubs the bridge handler to return the given light resources by their ID.
     */
    private static void stubLights(Fixture f, Map<String, Resource> lights) throws Exception {
        when(f.bridgeHandler.getResources(any(ResourceReference.class))).thenAnswer(i -> {
            ResourceReference ref = i.getArgument(0);
            Resources resources = new Resources();
            Resource light = lights.get(ref.getId());
            if (light != null) {
                resources.getResources().add(light);
            }
            return resources;
        });
    }

    /**
     * A Fixture whose front/back light IDs are NOT pre-injected, so they must be discovered by updateLookups().
     */
    private static Fixture unresolvedFixture(Resource device) throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH), List.of());
        f.setBackLightId(null);
        f.commandIds().clear();
        f.contributors().clear();
        setField(Clip2ThingHandler.class, f.handler, "thisResource", device);
        return f;
    }

    /**
     * Invokes the private updateLookups() method; unwraps and rethrows the underlying exception.
     */
    private static void updateLookups(Fixture f) throws Exception {
        var method = Clip2ThingHandler.class.getDeclaredMethod("updateLookups");
        method.setAccessible(true);
        try {
            method.invoke(f.handler);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }

    private static @Nullable Object backLightId(Fixture f) throws Exception {
        Field field = Clip2ThingHandler.class.getDeclaredField("backLightResourceId");
        field.setAccessible(true);
        return field.get(f.handler);
    }

    /**
     * Asserts discovery results, then channel creation and command routing based on the discovered IDs.
     */
    private static void assertFrontBackResolvedAndRouted(Fixture f) throws Exception {
        // lookups
        assertEquals(BACK_ID, backLightId(f));
        assertEquals(FRONT_ID, f.commandIds().get(ResourceType.LIGHT));
        assertTrue(f.contributors().containsKey(FRONT_ID));
        assertTrue(f.contributors().containsKey(BACK_ID));

        // channel creation: back channels are built from the main channel template
        f.mainSupported().add(CHANNEL_2_SWITCH);
        f.backSupported().add(CHANNEL_2_SWITCH);
        f.updateChannelList();
        assertEquals(1, f.handler.updatedThings.size());
        List<ChannelUID> uids = f.handler.updatedThings.get(0).getChannels().stream().map(Channel::getUID).toList();
        assertTrue(uids.contains(mainUID(CHANNEL_2_SWITCH)));
        assertTrue(uids.contains(backUID(CHANNEL_2_SWITCH)));

        // command routing
        assertEquals(BACK_ID, f.putCapture(backUID(CHANNEL_2_SWITCH), OnOffType.ON).getId());
        assertEquals(FRONT_ID, f.putCapture(mainUID(CHANNEL_2_SWITCH), OnOffType.ON).getId());
    }

    @Test
    void lookupsFrontThenBackServiceOrder() throws Exception {
        Fixture f = unresolvedFixture(deviceWithLights(FRONT_ID, BACK_ID));
        stubLights(f, Map.of(FRONT_ID, lightWithArchetype(FRONT_ID, "twilight_front"), BACK_ID,
                lightWithArchetype(BACK_ID, "twilight_back")));

        updateLookups(f);

        assertFrontBackResolvedAndRouted(f);
    }

    @Test
    void lookupsBackThenFrontServiceOrder() throws Exception {
        Fixture f = unresolvedFixture(deviceWithLights(BACK_ID, FRONT_ID));
        stubLights(f, Map.of(FRONT_ID, lightWithArchetype(FRONT_ID, "twilight_front"), BACK_ID,
                lightWithArchetype(BACK_ID, "twilight_back")));

        updateLookups(f);

        // the front light must be the primary light even though the back light is listed first
        assertFrontBackResolvedAndRouted(f);
    }

    @Test
    void lookupsWithoutTwilightArchetypesHaveNoBackLight() throws Exception {
        Fixture f = unresolvedFixture(deviceWithLights(FRONT_ID));
        stubLights(f, Map.of(FRONT_ID, lightWithArchetype(FRONT_ID, "classic_bulb")));

        updateLookups(f);

        assertNull(backLightId(f));
        assertEquals(FRONT_ID, f.commandIds().get(ResourceType.LIGHT));
        assertEquals(FRONT_ID, f.putCapture(mainUID(CHANNEL_2_SWITCH), OnOffType.ON).getId());
    }

    @Test
    void lookupFailureLeavesStateUntouchedAndRecovers() throws Exception {
        Fixture f = unresolvedFixture(deviceWithLights(FRONT_ID, BACK_ID));

        // first attempt: the bridge fails
        when(f.bridgeHandler.getResources(any(ResourceReference.class))).thenThrow(new ApiException("fail"));
        assertThrows(ApiException.class, () -> updateLookups(f));

        // nothing was half-applied
        assertNull(backLightId(f));
        assertTrue(f.commandIds().isEmpty());
        assertTrue(f.contributors().isEmpty());

        // second attempt: the bridge recovers
        stubLights(f, Map.of(FRONT_ID, lightWithArchetype(FRONT_ID, "twilight_front"), BACK_ID,
                lightWithArchetype(BACK_ID, "twilight_back")));
        updateLookups(f);

        assertFrontBackResolvedAndRouted(f);
    }
}
