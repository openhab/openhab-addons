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
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.hue.internal.api.dto.clip2.Gradient;
import org.openhab.binding.hue.internal.api.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.api.dto.clip2.Resources;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ContentType;
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
 * Tests for the auxiliary light handling of {@link Clip2ThingHandler}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class AuxiliaryLightTest {

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
        Consumer<Thing> onUpdateThing = t -> {
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
    private static final String AUX_ID = "aux-light-id";
    private static final String AUX_GROUP = LIGHT_GROUP_FORMAT.formatted(2); // "light2"

    private static ChannelUID mainUID(String id) {
        return new ChannelUID(THING_UID, id);
    }

    private static ChannelUID auxUID(String id) {
        return auxUID(AUX_GROUP, id);
    }

    private static ChannelUID auxUID(String groupId, String id) {
        return new ChannelUID(THING_UID, groupId, id);
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

        Fixture(List<String> mainChannelIds, List<String> auxChannelIds) throws Exception {
            thing = spy(ThingBuilder.create(THING_TYPE_DEVICE, THING_UID).withBridge(BRIDGE_UID).build());
            mainChannelIds.forEach(id -> channels.add(channel(mainUID(id))));
            auxChannelIds.forEach(id -> channels.add(channel(auxUID(id))));
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
            setAuxiliaryLight(AUX_GROUP, AUX_ID);
            commandIds().put(ResourceType.LIGHT, FRONT_ID);
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

        /**
         * Replaces the auxiliary light groups with a single entry, or with none if the id is null.
         */
        void setAuxiliaryLight(String groupId, @Nullable String lightId) throws Exception {
            auxiliaryLightGroups().clear();
            if (lightId != null) {
                auxiliaryLightGroups().put(groupId, lightId);
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, String> auxiliaryLightGroups() throws Exception {
            return (Map<String, String>) getField("auxiliaryLightGroups");
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
        Set<ChannelUID> supportedUIDs() throws Exception {
            return (Set<ChannelUID>) getField("supportedChannelUIDs");
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
    void auxiliaryLightCommandTargetsAuxiliaryLightResourceId() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH), List.of(CHANNEL_2_SWITCH));

        Resource put = f.putCapture(auxUID(CHANNEL_2_SWITCH), OnOffType.ON);

        assertEquals(AUX_ID, put.getId());
    }

    @Test
    void mainCommandTargetsMainLightResourceId() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH), List.of(CHANNEL_2_SWITCH));

        Resource put = f.putCapture(mainUID(CHANNEL_2_SWITCH), OnOffType.ON);

        assertEquals(FRONT_ID, put.getId());
        assertNotEquals(AUX_ID, put.getId());
    }

    @Test
    void auxiliaryLightCommandUsesAuxiliaryLightCache() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_BRIGHTNESS), List.of(CHANNEL_2_BRIGHTNESS));
        f.contributors().put(FRONT_ID, lightWithBrightness(FRONT_ID, 80));
        f.contributors().put(AUX_ID, lightWithBrightness(AUX_ID, 20));

        when(f.bridgeHandler.getResources(any(ResourceReference.class))).thenAnswer(i -> {
            ResourceReference ref = i.getArgument(0);
            int percent = AUX_ID.equals(ref.getId()) ? 20 : 80;
            Resources resources = new Resources();
            resources.getResources().add(lightWithBrightness(Objects.requireNonNull(ref.getId()), percent));
            return resources;
        });

        Resource auxPut = f.putCapture(auxUID(CHANNEL_2_BRIGHTNESS), IncreaseDecreaseType.INCREASE);
        Resource frontPut = f.putCapture(mainUID(CHANNEL_2_BRIGHTNESS), IncreaseDecreaseType.INCREASE);

        assertEquals(AUX_ID, auxPut.getId());
        assertEquals(FRONT_ID, frontPut.getId());

        // each increase is relative to its own light's brightness
        double auxBrightness = ((PercentType) auxPut.getBrightnessState()).doubleValue();
        double frontBrightness = ((PercentType) frontPut.getBrightnessState()).doubleValue();
        assertTrue(auxBrightness > 20 && auxBrightness < 50, "auxiliary light must start from 20%");
        assertTrue(frontBrightness > 80, "front light must start from 80%");
    }

    private static Resource lightWithBrightness(String id, int percent) {
        Resource resource = new Resource(ResourceType.LIGHT).setId(id);
        resource.setBrightness(new PercentType(percent));
        return resource;
    }

    @Test
    void auxiliaryLightResourceUpdatesAuxiliaryChannelsOnly() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS),
                List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS));

        updateChannels(f.handler, light(AUX_ID, true, 50));

        verify(f.callback, atLeastOnce()).stateUpdated(eq(auxUID(CHANNEL_2_SWITCH)), any(State.class));
        verify(f.callback, atLeastOnce()).stateUpdated(eq(auxUID(CHANNEL_2_BRIGHTNESS)), any(State.class));
        verify(f.callback, never()).stateUpdated(eq(mainUID(CHANNEL_2_SWITCH)), any(State.class));
        verify(f.callback, never()).stateUpdated(eq(mainUID(CHANNEL_2_BRIGHTNESS)), any(State.class));
    }

    @Test
    void frontLightResourceUpdatesMainChannelsOnly() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS),
                List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS));

        updateChannels(f.handler, light(FRONT_ID, true, 50));

        verify(f.callback, atLeastOnce()).stateUpdated(eq(mainUID(CHANNEL_2_SWITCH)), any(State.class));
        verify(f.callback, never()).stateUpdated(eq(auxUID(CHANNEL_2_SWITCH)), any(State.class));
        verify(f.callback, never()).stateUpdated(eq(auxUID(CHANNEL_2_BRIGHTNESS)), any(State.class));
    }

    @Test
    void auxiliaryChannelsAreCreatedFromMainChannelTemplate() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH, CHANNEL_2_EFFECT), List.of());
        f.supportedUIDs().addAll(Set.of(mainUID(CHANNEL_2_SWITCH), mainUID(CHANNEL_2_EFFECT)));
        f.supportedUIDs().addAll(Set.of(auxUID(CHANNEL_2_SWITCH), auxUID(CHANNEL_2_EFFECT)));

        f.updateChannelList();

        assertEquals(1, f.handler.updatedThings.size());
        List<Channel> created = f.handler.updatedThings.get(0).getChannels();
        Channel auxSwitch = created.stream().filter(c -> c.getUID().equals(auxUID(CHANNEL_2_SWITCH))).findFirst()
                .orElse(null);
        assertNotNull(auxSwitch);
        assertEquals(TYPE_UID, auxSwitch.getChannelTypeUID());
        assertTrue(created.stream().anyMatch(c -> c.getUID().equals(auxUID(CHANNEL_2_EFFECT))));
        assertTrue(created.stream().anyMatch(c -> c.getUID().equals(mainUID(CHANNEL_2_SWITCH))));
    }

    @Test
    void newAuxiliaryChannelsPushCachedState() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH), List.of());
        f.supportedUIDs().add(auxUID(CHANNEL_2_SWITCH));
        f.supportedUIDs().add(mainUID(CHANNEL_2_SWITCH));
        f.contributors().put(AUX_ID, light(AUX_ID, true, 40));
        // make updateThing() take effect on the fake channel list, as the framework would
        f.handler.onUpdateThing = updated -> {
            f.channels.clear();
            f.channels.addAll(updated.getChannels());
        };

        f.updateChannelList();

        verify(f.callback, atLeastOnce()).stateUpdated(eq(auxUID(CHANNEL_2_SWITCH)), any(State.class));
    }

    @Test
    void unusedAuxiliaryChannelsAreRemoved() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS),
                List.of(CHANNEL_2_SWITCH, CHANNEL_2_BRIGHTNESS));
        f.supportedUIDs().addAll(Set.of(mainUID(CHANNEL_2_SWITCH), mainUID(CHANNEL_2_BRIGHTNESS)));
        f.supportedUIDs().add(auxUID(CHANNEL_2_SWITCH)); // auxiliary brightness is NOT supported

        f.updateChannelList();

        assertEquals(1, f.handler.updatedThings.size());
        List<ChannelUID> uids = f.handler.updatedThings.get(0).getChannels().stream().map(Channel::getUID).toList();
        assertTrue(uids.contains(auxUID(CHANNEL_2_SWITCH)));
        assertFalse(uids.contains(auxUID(CHANNEL_2_BRIGHTNESS)));
        assertTrue(uids.contains(mainUID(CHANNEL_2_BRIGHTNESS)));
    }

    @Test
    void alertResetTasksAreIndependentPerLight() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_ALERT), List.of(CHANNEL_2_ALERT));
        ScheduledFuture<?> frontReset = mock(ScheduledFuture.class);
        ScheduledFuture<?> auxReset = mock(ScheduledFuture.class);
        when(f.scheduler.schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS))).thenAnswer(i -> frontReset)
                .thenAnswer(i -> auxReset);
        when(f.bridgeHandler.putResource(any(Resource.class))).thenReturn(new Resources());

        f.handler.handleCommandInner(mainUID(CHANNEL_2_ALERT),
                new org.openhab.core.library.types.StringType("BREATHE"));
        f.handler.handleCommandInner(auxUID(CHANNEL_2_ALERT), new org.openhab.core.library.types.StringType("BREATHE"));

        // the auxiliary light alert must not cancel the front light's pending reset
        verify(frontReset, never()).cancel(anyBoolean());
        verify(f.scheduler, times(2)).schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS));

        // a second front alert does cancel only the front light's own reset
        when(f.scheduler.schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS)))
                .thenAnswer(i -> mock(ScheduledFuture.class));
        f.handler.handleCommandInner(mainUID(CHANNEL_2_ALERT),
                new org.openhab.core.library.types.StringType("BREATHE"));
        verify(frontReset).cancel(false);
        verify(auxReset, never()).cancel(anyBoolean());
    }

    @Test
    void dynamicsAreIndependentPerLight() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_BRIGHTNESS, CHANNEL_2_DYNAMICS),
                List.of(CHANNEL_2_BRIGHTNESS, CHANNEL_2_DYNAMICS));
        f.contributors().put(FRONT_ID, new Resource(ResourceType.LIGHT).setId(FRONT_ID));
        f.contributors().put(AUX_ID, new Resource(ResourceType.LIGHT).setId(AUX_ID));

        // set a 2 second dynamics duration for the auxiliary light only
        f.handler.handleCommandInner(auxUID(CHANNEL_2_DYNAMICS),
                new QuantityType<>(2000, MetricPrefix.MILLI(Units.SECOND)));

        Resource auxPut = f.putCapture(auxUID(CHANNEL_2_BRIGHTNESS), new PercentType(50));
        Resource frontPut = f.putCapture(mainUID(CHANNEL_2_BRIGHTNESS), new PercentType(50));

        assertNotNull(auxPut.getDynamics(), "auxiliary light command must carry the auxiliary light dynamics");
        assertNull(frontPut.getDynamics(), "front light command must not carry the auxiliary light dynamics");
    }

    @Test
    void handleDynamicsCommandNormalizesAuxiliaryLightIds() throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_BRIGHTNESS, CHANNEL_2_DYNAMICS),
                List.of(CHANNEL_2_BRIGHTNESS, CHANNEL_2_DYNAMICS));
        QuantityType<?> duration = new QuantityType<>(1500, MetricPrefix.MILLI(Units.SECOND));
        PercentType target = new PercentType(70);
        f.handler.recordCommands = true;

        f.handler.handleDynamicsCommand(AUX_GROUP + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_2_BRIGHTNESS, target,
                duration);

        // the dynamics command comes first, then the target command, both on the auxiliary light channels
        assertEquals(List.of(Map.entry(auxUID(CHANNEL_2_DYNAMICS), (Command) duration),
                Map.entry(auxUID(CHANNEL_2_BRIGHTNESS), (Command) target)), f.handler.commands);
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
     * A Fixture whose front/auxiliary light IDs are NOT pre-injected, so they must be discovered by updateLookups().
     */
    private static Fixture unresolvedFixture(Resource device) throws Exception {
        Fixture f = new Fixture(List.of(CHANNEL_2_SWITCH), List.of());
        f.setAuxiliaryLight(AUX_GROUP, null);
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

    /**
     * Asserts discovery results, then channel creation and command routing based on the discovered IDs.
     */
    private static void assertFrontAuxiliaryResolvedAndRouted(Fixture f) throws Exception {
        // lookups
        assertEquals(Map.of(AUX_GROUP, AUX_ID), f.auxiliaryLightGroups());
        assertEquals(FRONT_ID, f.commandIds().get(ResourceType.LIGHT));
        assertTrue(f.contributors().containsKey(FRONT_ID));
        assertTrue(f.contributors().containsKey(AUX_ID));

        // channel creation: auxiliary channels are built from the main channel template
        f.supportedUIDs().add(mainUID(CHANNEL_2_SWITCH));
        f.supportedUIDs().add(auxUID(CHANNEL_2_SWITCH));
        f.updateChannelList();
        assertEquals(1, f.handler.updatedThings.size());
        List<ChannelUID> uids = f.handler.updatedThings.get(0).getChannels().stream().map(Channel::getUID).toList();
        assertTrue(uids.contains(mainUID(CHANNEL_2_SWITCH)));
        assertTrue(uids.contains(auxUID(CHANNEL_2_SWITCH)));

        // command routing
        assertEquals(AUX_ID, f.putCapture(auxUID(CHANNEL_2_SWITCH), OnOffType.ON).getId());
        assertEquals(FRONT_ID, f.putCapture(mainUID(CHANNEL_2_SWITCH), OnOffType.ON).getId());
    }

    @Test
    void lookupsFrontThenBackServiceOrder() throws Exception {
        Fixture f = unresolvedFixture(deviceWithLights(FRONT_ID, AUX_ID));
        stubLights(f, Map.of(FRONT_ID, lightWithArchetype(FRONT_ID, "twilight_front"), AUX_ID,
                lightWithArchetype(AUX_ID, "twilight_back")));

        updateLookups(f);

        assertFrontAuxiliaryResolvedAndRouted(f);
    }

    @Test
    void lookupsBackThenFrontServiceOrder() throws Exception {
        Fixture f = unresolvedFixture(deviceWithLights(AUX_ID, FRONT_ID));
        stubLights(f, Map.of(FRONT_ID, lightWithArchetype(FRONT_ID, "twilight_front"), AUX_ID,
                lightWithArchetype(AUX_ID, "twilight_back")));

        updateLookups(f);

        // the front light must be the primary light even though the back light is listed first
        assertFrontAuxiliaryResolvedAndRouted(f);
    }

    @Test
    void lookupsFiveLightsWithoutArchetypesUseServiceOrder() throws Exception {
        String[] ids = { "l1", "l2", "l3", "l4", "l5" };
        Fixture f = unresolvedFixture(deviceWithLights(ids));
        stubLights(f, Map.of("l1", lightWithArchetype("l1", "centris_ceiling"), "l2",
                lightWithArchetype("l2", "centris_ceiling"), "l3", lightWithArchetype("l3", "centris_ceiling"), "l4",
                lightWithArchetype("l4", "centris_ceiling"), "l5", lightWithArchetype("l5", "centris_ceiling")));

        updateLookups(f);

        // first light is the primary light, the others are light2 .. light5 in service order
        assertEquals("l1", f.commandIds().get(ResourceType.LIGHT));
        assertEquals(
                Map.of(LIGHT_GROUP_FORMAT.formatted(2), "l2", LIGHT_GROUP_FORMAT.formatted(3), "l3",
                        LIGHT_GROUP_FORMAT.formatted(4), "l4", LIGHT_GROUP_FORMAT.formatted(5), "l5"),
                f.auxiliaryLightGroups());
        assertEquals("l4",
                f.putCapture(auxUID(LIGHT_GROUP_FORMAT.formatted(4), CHANNEL_2_SWITCH), OnOffType.ON).getId());
    }

    @Test
    void lookupsWithoutTwilightArchetypesHaveNoAuxiliaryLight() throws Exception {
        Fixture f = unresolvedFixture(deviceWithLights(FRONT_ID));
        stubLights(f, Map.of(FRONT_ID, lightWithArchetype(FRONT_ID, "classic_bulb")));

        updateLookups(f);

        assertTrue(f.auxiliaryLightGroups().isEmpty());
        assertEquals(FRONT_ID, f.commandIds().get(ResourceType.LIGHT));
        assertEquals(FRONT_ID, f.putCapture(mainUID(CHANNEL_2_SWITCH), OnOffType.ON).getId());
    }

    @Test
    void lookupFailureLeavesStateUntouchedAndRecovers() throws Exception {
        Fixture f = unresolvedFixture(deviceWithLights(FRONT_ID, AUX_ID));

        // first attempt: the bridge fails
        when(f.bridgeHandler.getResources(any(ResourceReference.class))).thenThrow(new ApiException("fail"));
        assertThrows(ApiException.class, () -> updateLookups(f));

        // nothing was half-applied
        assertTrue(f.auxiliaryLightGroups().isEmpty());
        assertTrue(f.commandIds().isEmpty());
        assertTrue(f.contributors().isEmpty());

        // second attempt: the bridge recovers
        stubLights(f, Map.of(FRONT_ID, lightWithArchetype(FRONT_ID, "twilight_front"), AUX_ID,
                lightWithArchetype(AUX_ID, "twilight_back")));
        updateLookups(f);

        assertFrontAuxiliaryResolvedAndRouted(f);
    }

    private static Gradient solidGradient(int capable) {
        return Objects.requireNonNull(GSON.fromJson("{\"points_capable\":" + capable + "}", Gradient.class));
    }

    private static List<String> gradientIds() {
        return List.of(CHANNEL_2_GRADIENT_FORMAT.formatted(1), CHANNEL_2_GRADIENT_FORMAT.formatted(2),
                CHANNEL_2_GRADIENT_FORMAT.formatted(3), CHANNEL_2_GRADIENT_FORMAT.formatted(4),
                CHANNEL_2_GRADIENT_FORMAT.formatted(5));
    }

    private static List<ChannelUID> updatedChannelUIDs(Fixture f) {
        assertEquals(1, f.handler.updatedThings.size());
        return f.handler.updatedThings.get(0).getChannels().stream().map(Channel::getUID).toList();
    }

    @Test
    void solidModeGradientKeepsAllCapableGradientChannels() throws Exception {
        Fixture f = new Fixture(gradientIds(), List.of());
        f.setAuxiliaryLight(AUX_GROUP, null);
        Resource front = new Resource(ResourceType.LIGHT).setId(FRONT_ID).setGradient(solidGradient(5))
                .setContentType(ContentType.FULL_STATE);

        updateChannels(f.handler, front);
        f.updateChannelList();

        // all five are supported although the gradient has no points
        for (String id : gradientIds()) {
            assertTrue(f.supportedUIDs().contains(mainUID(id)), id);
        }
        // nothing was removed, so the thing was not updated
        assertTrue(f.handler.updatedThings.isEmpty());
    }

    @Test
    void solidModeGradientRemovesChannelsBeyondCapacity() throws Exception {
        Fixture f = new Fixture(gradientIds(), List.of());
        f.setAuxiliaryLight(AUX_GROUP, null);
        Resource front = new Resource(ResourceType.LIGHT).setId(FRONT_ID).setGradient(solidGradient(3))
                .setContentType(ContentType.FULL_STATE);

        updateChannels(f.handler, front);
        f.updateChannelList();

        List<ChannelUID> uids = updatedChannelUIDs(f);
        assertEquals(3, uids.size());
        assertTrue(uids.contains(mainUID(CHANNEL_2_GRADIENT_FORMAT.formatted(1))));
        assertTrue(uids.contains(mainUID(CHANNEL_2_GRADIENT_FORMAT.formatted(3))));
        assertFalse(uids.contains(mainUID(CHANNEL_2_GRADIENT_FORMAT.formatted(4))));
        assertFalse(uids.contains(mainUID(CHANNEL_2_GRADIENT_FORMAT.formatted(5))));
    }

    @Test
    void lightWithoutGradientRemovesAllGradientChannels() throws Exception {
        Fixture f = new Fixture(gradientIds(), List.of());
        f.setAuxiliaryLight(AUX_GROUP, null);
        Resource front = new Resource(ResourceType.LIGHT).setId(FRONT_ID).setContentType(ContentType.FULL_STATE);

        updateChannels(f.handler, front);
        f.updateChannelList();

        assertTrue(updatedChannelUIDs(f).isEmpty());
    }
}
