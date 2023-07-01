/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.dto.clip2.ActionEntry;
import org.openhab.binding.hue.internal.dto.clip2.Alerts;
import org.openhab.binding.hue.internal.dto.clip2.Button;
import org.openhab.binding.hue.internal.dto.clip2.Dimming;
import org.openhab.binding.hue.internal.dto.clip2.Event;
import org.openhab.binding.hue.internal.dto.clip2.LightLevel;
import org.openhab.binding.hue.internal.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.dto.clip2.MirekSchema;
import org.openhab.binding.hue.internal.dto.clip2.Motion;
import org.openhab.binding.hue.internal.dto.clip2.Power;
import org.openhab.binding.hue.internal.dto.clip2.ProductData;
import org.openhab.binding.hue.internal.dto.clip2.RelativeRotary;
import org.openhab.binding.hue.internal.dto.clip2.Resource;
import org.openhab.binding.hue.internal.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.dto.clip2.Resources;
import org.openhab.binding.hue.internal.dto.clip2.Rotation;
import org.openhab.binding.hue.internal.dto.clip2.RotationEvent;
import org.openhab.binding.hue.internal.dto.clip2.Temperature;
import org.openhab.binding.hue.internal.dto.clip2.enums.ActionType;
import org.openhab.binding.hue.internal.dto.clip2.enums.Archetype;
import org.openhab.binding.hue.internal.dto.clip2.enums.BatteryStateType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ButtonEventType;
import org.openhab.binding.hue.internal.dto.clip2.enums.DirectionType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.dto.clip2.enums.RotationEventType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ZigbeeStatus;
import org.openhab.binding.hue.internal.dto.clip2.helper.Setters;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * JUnit test for CLIP 2 DTOs.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class Clip2DtoTest {

    private static final Gson GSON = new Gson();
    private static final Double MINIMUM_DIMMING_LEVEL = Double.valueOf(12.34f);

    /**
     * Load the test JSON payload string from a file
     */
    private String load(String fileName) {
        try (FileReader file = new FileReader(String.format("src/test/resources/%s.json", fileName));
                BufferedReader reader = new BufferedReader(file)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }

    @Test
    void testButton() {
        String json = load(ResourceType.BUTTON.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(43, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.BUTTON, item.getType());
        Button button = item.getButton();
        assertNotNull(button);
        assertEquals(ButtonEventType.SHORT_RELEASE, button.getLastEvent());
    }

    @Test
    void testDevice() {
        String json = load(ResourceType.DEVICE.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(34, list.size());
        boolean itemFound = false;
        for (Resource item : list) {
            assertEquals(ResourceType.DEVICE, item.getType());
            ProductData productData = item.getProductData();
            assertNotNull(productData);
            if (productData.getProductArchetype() == Archetype.BRIDGE_V2) {
                itemFound = true;
                assertEquals("BSB002", productData.getModelId());
                assertEquals("Signify Netherlands B.V.", productData.getManufacturerName());
                assertEquals("Philips hue", productData.getProductName());
                assertNull(productData.getHardwarePlatformType());
                assertTrue(productData.getCertified());
                assertEquals("1.53.1953188020", productData.getSoftwareVersion());
                break;
            }
        }
        assertTrue(itemFound);
    }

    @Test
    void testDevicePower() {
        String json = load(ResourceType.DEVICE_POWER.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(16, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.DEVICE_POWER, item.getType());
        Power power = item.getPowerState();
        assertNotNull(power);
        assertEquals(60, power.getBatteryLevel());
        assertEquals(BatteryStateType.NORMAL, power.getBatteryState());
    }

    @Test
    void testGroupedLight() {
        String json = load(ResourceType.GROUPED_LIGHT.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(15, list.size());
        int itemsFound = 0;
        for (Resource item : list) {
            assertEquals(ResourceType.GROUPED_LIGHT, item.getType());
            Alerts alert;
            switch (item.getId()) {
                case "db4fd630-3798-40de-b642-c1ef464bf770":
                    itemsFound++;
                    assertEquals(OnOffType.OFF, item.getOnOffState());
                    assertEquals(PercentType.ZERO, item.getBrightnessState());
                    alert = item.getAlerts();
                    assertNotNull(alert);
                    for (ActionType actionValue : alert.getActionValues()) {
                        assertEquals(ActionType.BREATHE, actionValue);
                    }
                    break;
                case "9228d710-3c54-4ae4-8c88-bfe57d8fd220":
                    itemsFound++;
                    assertEquals(OnOffType.ON, item.getOnOffState());
                    assertEquals(PercentType.HUNDRED, item.getBrightnessState());
                    alert = item.getAlerts();
                    assertNotNull(alert);
                    for (ActionType actionValue : alert.getActionValues()) {
                        assertEquals(ActionType.BREATHE, actionValue);
                    }
                    break;
                default:
            }
        }
        assertEquals(2, itemsFound);
    }

    @Test
    void testLight() {
        String json = load(ResourceType.LIGHT.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(17, list.size());
        int itemFoundCount = 0;
        for (Resource item : list) {
            assertEquals(ResourceType.LIGHT, item.getType());
            MetaData metaData = item.getMetaData();
            assertNotNull(metaData);
            String name = metaData.getName();
            assertNotNull(name);
            State state;
            if (name.contains("Bay Window Lamp")) {
                itemFoundCount++;
                assertEquals(ResourceType.LIGHT, item.getType());
                assertEquals(OnOffType.OFF, item.getOnOffState());
                state = item.getBrightnessState();
                assertTrue(state instanceof PercentType);
                assertEquals(0, ((PercentType) state).doubleValue(), 0.1);
                item.setOnOff(OnOffType.ON);
                state = item.getBrightnessState();
                assertTrue(state instanceof PercentType);
                assertEquals(93.0, ((PercentType) state).doubleValue(), 0.1);
                assertEquals(UnDefType.UNDEF, item.getColorTemperaturePercentState());
                state = item.getColorState();
                assertTrue(state instanceof HSBType);
                double[] xy = ColorUtil.hsbToXY((HSBType) state);
                assertEquals(0.6367, xy[0], 0.01); // note: rounding errors !!
                assertEquals(0.3503, xy[1], 0.01); // note: rounding errors !!
                assertEquals(item.getBrightnessState(), ((HSBType) state).getBrightness());
                Alerts alert = item.getAlerts();
                assertNotNull(alert);
                for (ActionType actionValue : alert.getActionValues()) {
                    assertEquals(ActionType.BREATHE, actionValue);
                }
            }
            if (name.contains("Table Lamp A")) {
                itemFoundCount++;
                assertEquals(ResourceType.LIGHT, item.getType());
                assertEquals(OnOffType.OFF, item.getOnOffState());
                state = item.getBrightnessState();
                assertTrue(state instanceof PercentType);
                assertEquals(0, ((PercentType) state).doubleValue(), 0.1);
                item.setOnOff(OnOffType.ON);
                state = item.getBrightnessState();
                assertTrue(state instanceof PercentType);
                assertEquals(56.7, ((PercentType) state).doubleValue(), 0.1);
                MirekSchema mirekSchema = item.getMirekSchema();
                assertNotNull(mirekSchema);
                assertEquals(153, mirekSchema.getMirekMinimum());
                assertEquals(454, mirekSchema.getMirekMaximum());

                // test color temperature percent value on light's own scale
                state = item.getColorTemperaturePercentState();
                assertTrue(state instanceof PercentType);
                assertEquals(96.3, ((PercentType) state).doubleValue(), 0.1);
                state = item.getColorTemperatureAbsoluteState();
                assertTrue(state instanceof QuantityType<?>);
                assertEquals(2257.3, ((QuantityType<?>) state).doubleValue(), 0.1);

                // test color temperature percent value on the default (full) scale
                MirekSchema temp = item.getMirekSchema();
                item.setMirekSchema(MirekSchema.DEFAULT_SCHEMA);
                state = item.getColorTemperaturePercentState();
                assertTrue(state instanceof PercentType);
                assertEquals(83.6, ((PercentType) state).doubleValue(), 0.1);
                state = item.getColorTemperatureAbsoluteState();
                assertTrue(state instanceof QuantityType<?>);
                assertEquals(2257.3, ((QuantityType<?>) state).doubleValue(), 0.1);
                item.setMirekSchema(temp);

                // change colour temperature percent to zero
                Setters.setColorTemperaturePercent(item, PercentType.ZERO, null);
                assertEquals(PercentType.ZERO, item.getColorTemperaturePercentState());
                state = item.getColorTemperatureAbsoluteState();
                assertTrue(state instanceof QuantityType<?>);
                assertEquals(6535.9, ((QuantityType<?>) state).doubleValue(), 0.1);

                // change colour temperature percent to 100
                Setters.setColorTemperaturePercent(item, PercentType.HUNDRED, null);
                assertEquals(PercentType.HUNDRED, item.getColorTemperaturePercentState());
                state = item.getColorTemperatureAbsoluteState();
                assertTrue(state instanceof QuantityType<?>);
                assertEquals(2202.6, ((QuantityType<?>) state).doubleValue(), 0.1);

                // change colour temperature kelvin to 4000 K
                Setters.setColorTemperatureAbsolute(item, QuantityType.valueOf("4000 K"), null);
                state = item.getColorTemperaturePercentState();
                assertTrue(state instanceof PercentType);
                assertEquals(32.2, ((PercentType) state).doubleValue(), 0.1);
                assertEquals(QuantityType.valueOf("4000 K"), item.getColorTemperatureAbsoluteState());

                assertEquals(UnDefType.NULL, item.getColorState());
                Alerts alert = item.getAlerts();
                assertNotNull(alert);
                for (ActionType actionValue : alert.getActionValues()) {
                    assertEquals(ActionType.BREATHE, actionValue);
                }
            }
        }
        assertEquals(2, itemFoundCount);
    }

    @Test
    void testLightLevel() {
        String json = load(ResourceType.LIGHT_LEVEL.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(1, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.LIGHT_LEVEL, item.getType());
        Boolean enabled = item.getEnabled();
        assertNotNull(enabled);
        assertTrue(enabled);
        LightLevel lightLevel = item.getLightLevel();
        assertNotNull(lightLevel);
        assertEquals(12725, lightLevel.getLightLevel());
        assertTrue(lightLevel.isLightLevelValid());
    }

    @Test
    void testRelativeRotary() {
        String json = load(ResourceType.RELATIVE_ROTARY.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(1, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.RELATIVE_ROTARY, item.getType());
        RelativeRotary relativeRotary = item.getRelativeRotary();
        assertNotNull(relativeRotary);
        RotationEvent rotationEvent = relativeRotary.getLastEvent();
        assertNotNull(rotationEvent);
        assertEquals(RotationEventType.REPEAT, rotationEvent.getAction());
        Rotation rotation = rotationEvent.getRotation();
        assertNotNull(rotation);
        assertEquals(DirectionType.CLOCK_WISE, rotation.getDirection());
        assertEquals(400, rotation.getDuration());
        assertEquals(30, rotation.getSteps());
        assertEquals(new DecimalType(30), relativeRotary.getStepsState());
        assertEquals(new StringType(ButtonEventType.REPEAT.name()), relativeRotary.getActionState());
    }

    @Test
    void testResourceMerging() {
        // create resource one
        Resource one = new Resource(ResourceType.LIGHT).setId("AARDVARK");
        assertNotNull(one);
        // preset the minimum dimming level
        try {
            Dimming dimming = new Dimming().setMinimumDimmingLevel(MINIMUM_DIMMING_LEVEL);
            Field dimming2 = one.getClass().getDeclaredField("dimming");
            dimming2.setAccessible(true);
            dimming2.set(one, dimming);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            fail();
        }
        Setters.setColorXy(one, HSBType.RED, null);
        Setters.setDimming(one, PercentType.HUNDRED, null);
        assertTrue(one.getColorState() instanceof HSBType);
        assertEquals(PercentType.HUNDRED, one.getBrightnessState());
        assertTrue(HSBType.RED.closeTo((HSBType) one.getColorState(), 0.01));

        // switching off should change HSB and Brightness
        one.setOnOff(OnOffType.OFF);
        assertEquals(0, ((HSBType) one.getColorState()).getBrightness().doubleValue(), 0.01);
        assertEquals(PercentType.ZERO, one.getBrightnessState());
        one.setOnOff(OnOffType.ON);

        // setting brightness to zero should change it to the minimum dimming level
        Setters.setDimming(one, PercentType.ZERO, null);
        assertEquals(MINIMUM_DIMMING_LEVEL, ((HSBType) one.getColorState()).getBrightness().doubleValue(), 0.01);
        assertEquals(MINIMUM_DIMMING_LEVEL, ((PercentType) one.getBrightnessState()).doubleValue(), 0.01);
        one.setOnOff(OnOffType.ON);

        // null its Dimming field
        try {
            Field dimming = one.getClass().getDeclaredField("dimming");
            dimming.setAccessible(true);
            dimming.set(one, null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            fail();
        }

        // confirm that brightness is no longer valid, and therefore that color has also changed
        assertEquals(UnDefType.NULL, one.getBrightnessState());
        assertTrue(one.getColorState() instanceof HSBType);
        assertTrue((new HSBType(DecimalType.ZERO, PercentType.HUNDRED, new PercentType(50)))
                .closeTo((HSBType) one.getColorState(), 0.01));

        PercentType testBrightness = new PercentType(42);

        // create resource two
        Resource two = new Resource(ResourceType.DEVICE).setId("ALLIGATOR");
        assertNotNull(two);
        Setters.setDimming(two, testBrightness, null);
        assertEquals(UnDefType.NULL, two.getColorState());
        assertEquals(testBrightness, two.getBrightnessState());

        // merge two => one
        Setters.setResource(one, two);

        // confirm that brightness and color are both once more valid
        assertEquals("AARDVARK", one.getId());
        assertEquals(ResourceType.LIGHT, one.getType());
        assertEquals(testBrightness, one.getBrightnessState());
        assertTrue(one.getColorState() instanceof HSBType);
        assertTrue((new HSBType(DecimalType.ZERO, PercentType.HUNDRED, testBrightness))
                .closeTo((HSBType) one.getColorState(), 0.01));
    }

    @Test
    void testRoomGroup() {
        String json = load(ResourceType.ROOM.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(6, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.ROOM, item.getType());
        List<ResourceReference> children = item.getChildren();
        assertEquals(2, children.size());
        ResourceReference child = children.get(0);
        assertNotNull(child);
        assertEquals("0d47bd3d-d82b-4a21-893c-299bff18e22a", child.getId());
        assertEquals(ResourceType.DEVICE, child.getType());
        List<ResourceReference> services = item.getServiceReferences();
        assertEquals(1, services.size());
        ResourceReference service = services.get(0);
        assertNotNull(service);
        assertEquals("08947162-67be-4ed5-bfce-f42dade42416", service.getId());
        assertEquals(ResourceType.GROUPED_LIGHT, service.getType());
    }

    @Test
    void testScene() {
        String json = load(ResourceType.SCENE.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(123, list.size());
        Resource item = list.get(0);
        List<ActionEntry> actions = item.getActions();
        assertNotNull(actions);
        assertEquals(3, actions.size());
        ActionEntry actionEntry = actions.get(0);
        assertNotNull(actionEntry);
        Resource action = actionEntry.getAction();
        assertNotNull(action);
        assertEquals(OnOffType.ON, action.getOnOffState());
    }

    @Test
    void testSensor2Motion() {
        String json = load(ResourceType.MOTION.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(1, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.MOTION, item.getType());
        Boolean enabled = item.getEnabled();
        assertNotNull(enabled);
        assertTrue(enabled);
        Motion motion = item.getMotion();
        assertNotNull(motion);
        assertTrue(motion.isMotion());
        assertTrue(motion.isMotionValid());
    }

    @Test
    void testSetGetPureColors() {
        Resource resource = new Resource(ResourceType.LIGHT);
        assertNotNull(resource);

        HSBType cyan = new HSBType("180,100,100");
        HSBType yellow = new HSBType("60,100,100");
        HSBType magenta = new HSBType("300,100,100");

        for (HSBType color : Set.of(HSBType.WHITE, HSBType.RED, HSBType.GREEN, HSBType.BLUE, cyan, yellow, magenta)) {
            Setters.setColorXy(resource, color, null);
            State state = resource.getColorState();
            assertTrue(state instanceof HSBType);
            assertTrue(color.closeTo((HSBType) state, 0.01));
        }
    }

    @Test
    void testSseLightOrGroupEvent() {
        String json = load("event");
        List<Event> eventList = GSON.fromJson(json, Event.EVENT_LIST_TYPE);
        assertNotNull(eventList);
        assertEquals(3, eventList.size());
        Event event = eventList.get(0);
        List<Resource> resources = event.getData();
        assertEquals(9, resources.size());
        for (Resource r : resources) {
            ResourceType type = r.getType();
            assertTrue(ResourceType.LIGHT == type || ResourceType.GROUPED_LIGHT == type);
        }
    }

    @Test
    void testSseSceneEvent() {
        String json = load("event");
        List<Event> eventList = GSON.fromJson(json, Event.EVENT_LIST_TYPE);
        assertNotNull(eventList);
        assertEquals(3, eventList.size());
        Event event = eventList.get(2);
        List<Resource> resources = event.getData();
        assertEquals(6, resources.size());
        Resource resource = resources.get(1);
        assertEquals(ResourceType.SCENE, resource.getType());
        JsonObject status = resource.getStatus();
        assertNotNull(status);
        JsonElement active = status.get("active");
        assertNotNull(active);
        assertTrue(active.isJsonPrimitive());
        assertEquals("inactive", active.getAsString());
        Optional<Boolean> isActive = resource.getSceneActive();
        assertTrue(isActive.isPresent());
        assertEquals(Boolean.FALSE, isActive.get());
    }

    @Test
    void testTemperature() {
        String json = load(ResourceType.TEMPERATURE.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(1, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.TEMPERATURE, item.getType());
        Temperature temperature = item.getTemperature();
        assertNotNull(temperature);
        assertEquals(17.2, temperature.getTemperature(), 0.1);
        assertTrue(temperature.isTemperatureValid());
    }

    @Test
    void testValidJson() {
        for (ResourceType res : ResourceType.values()) {
            if (!ResourceType.SSE_TYPES.contains(res)) {
                try {
                    String file = res.name().toLowerCase();
                    String json = load(file);
                    JsonElement jsonElement = JsonParser.parseString(json);
                    assertTrue(jsonElement.isJsonObject());
                } catch (JsonSyntaxException e) {
                    fail(res.name());
                }
            }
        }
    }

    @Test
    void testZigbeeStatus() {
        String json = load(ResourceType.ZIGBEE_CONNECTIVITY.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(35, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.ZIGBEE_CONNECTIVITY, item.getType());
        ZigbeeStatus zigbeeStatus = item.getZigbeeStatus();
        assertNotNull(zigbeeStatus);
        assertEquals("Connected", zigbeeStatus.toString());
    }

    @Test
    void testZoneGroup() {
        String json = load(ResourceType.ZONE.name().toLowerCase());
        Resources resources = GSON.fromJson(json, Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(7, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.ZONE, item.getType());
        List<ResourceReference> children = item.getChildren();
        assertEquals(1, children.size());
        ResourceReference child = children.get(0);
        assertNotNull(child);
        assertEquals("bcad47a0-3f1f-498c-a8aa-3cf389965219", child.getId());
        assertEquals(ResourceType.LIGHT, child.getType());
        List<ResourceReference> services = item.getServiceReferences();
        assertEquals(1, services.size());
        ResourceReference service = services.get(0);
        assertNotNull(service);
        assertEquals("db4fd630-3798-40de-b642-c1ef464bf770", service.getId());
        assertEquals(ResourceType.GROUPED_LIGHT, service.getType());
    }
}
