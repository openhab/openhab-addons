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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.dto.ApiVersion;
import org.openhab.binding.hue.internal.dto.clip2.ActionEntry;
import org.openhab.binding.hue.internal.dto.clip2.Alerts;
import org.openhab.binding.hue.internal.dto.clip2.Button;
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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * JUnit test for CLIP 2 DTOs.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class Clip2DtoTests {

    private static final Gson GSON = new Gson();

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
                ApiVersion ver = productData.getSoftwareVersion();
                assertEquals(1, ver.getMajor());
                assertEquals(53, ver.getMinor());
                assertEquals(1953188020, ver.getMicro());
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
                    assertEquals(OnOffType.OFF, item.getSwitch());
                    assertEquals(PercentType.ZERO, item.getBrightnessState());
                    alert = item.getAlert();
                    assertNotNull(alert);
                    for (ActionType actionValue : alert.getActionValues()) {
                        assertEquals(ActionType.BREATHE, actionValue);
                    }
                    break;
                case "9228d710-3c54-4ae4-8c88-bfe57d8fd220":
                    itemsFound++;
                    assertEquals(OnOffType.ON, item.getSwitch());
                    assertEquals(PercentType.HUNDRED, item.getBrightnessState());
                    alert = item.getAlert();
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
            if (name.contains("Bay Window Lamp")) {
                itemFoundCount++;
                assertEquals(ResourceType.LIGHT, item.getType());
                assertEquals(OnOffType.OFF, item.getSwitch());
                assertEquals(new PercentType(93), item.getBrightnessState());
                assertEquals(UnDefType.UNDEF, item.getColorTemperaturePercentState(new MirekSchema()));
                State state = item.getColorState();
                assertTrue(state instanceof HSBType);
                float[] xy = Resource.getColorXY((HSBType) state);
                assertEquals(0.6367, xy[0], 0.015); // note: rounding errors !!
                assertEquals(0.3503, xy[1], 0.015); // note: rounding errors !!
                assertEquals(item.getBrightnessState(), ((HSBType) state).getBrightness());
                Alerts alert = item.getAlert();
                assertNotNull(alert);
                for (ActionType actionValue : alert.getActionValues()) {
                    assertEquals(ActionType.BREATHE, actionValue);
                }
            }
            if (name.contains("Table Lamp A")) {
                itemFoundCount++;
                assertEquals(ResourceType.LIGHT, item.getType());
                assertEquals(OnOffType.OFF, item.getSwitch());
                assertEquals(new PercentType(57), item.getBrightnessState());
                MirekSchema mirekSchema = item.getMirekSchema();
                assertNotNull(mirekSchema);
                assertEquals(153, mirekSchema.getMirekMinimum());
                assertEquals(454, mirekSchema.getMirekMaximum());

                // test color temperature percent value on light's own scale
                assertEquals(new PercentType(96), item.getColorTemperaturePercentState(mirekSchema));
                assertEquals(QuantityType.valueOf("2257 K"), item.getColorTemperatureKelvinState());

                // test color temperature percent value on the default (full) scale
                assertEquals(new PercentType(84), item.getColorTemperaturePercentState(new MirekSchema()));
                assertEquals(QuantityType.valueOf("2257 K"), item.getColorTemperatureKelvinState());

                // change colour temperature
                item.setColorTemperaturePercent(PercentType.ZERO, mirekSchema);
                assertEquals(PercentType.ZERO, item.getColorTemperaturePercentState(mirekSchema));
                assertEquals(QuantityType.valueOf("6536 K"), item.getColorTemperatureKelvinState());

                item.setColorTemperaturePercent(PercentType.HUNDRED, mirekSchema);
                assertEquals(PercentType.HUNDRED, item.getColorTemperaturePercentState(mirekSchema));
                assertEquals(QuantityType.valueOf("2203 K"), item.getColorTemperatureKelvinState());

                item.setColorTemperatureKelvin(QuantityType.valueOf("4000 K"));
                assertEquals(new PercentType(32), item.getColorTemperaturePercentState(mirekSchema));
                assertEquals(QuantityType.valueOf("4000 K"), item.getColorTemperatureKelvinState());

                assertEquals(UnDefType.NULL, item.getColorState());
                Alerts alert = item.getAlert();
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
        assertEquals(12725, lightLevel.getLightlevel());
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
    void testResoureceMerging() {
        // create resource one
        Resource one = new Resource(ResourceType.LIGHT).setId("AARDVARK");
        assertNotNull(one);
        one.setColor(HSBType.RED);
        assertEquals(HSBType.RED, one.getColorState());
        assertEquals(PercentType.HUNDRED, one.getBrightnessState());

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
        assertEquals(new HSBType(DecimalType.ZERO, PercentType.HUNDRED, new PercentType(50)), one.getColorState());

        PercentType testBrightness = new PercentType(42);

        // create resource two
        Resource two = new Resource(ResourceType.DEVICE).setId("ALLIGATOR");
        assertNotNull(two);
        two.setBrightness(testBrightness);
        assertEquals(UnDefType.NULL, two.getColorState());
        assertEquals(testBrightness, two.getBrightnessState());

        // merge two => one
        one.copyMissingFieldsFrom(two);

        // confirm that brightness and color are both once more valid
        assertEquals("AARDVARK", one.getId());
        assertEquals(ResourceType.LIGHT, one.getType());
        assertEquals(testBrightness, one.getBrightnessState());
        assertEquals(new HSBType(DecimalType.ZERO, PercentType.HUNDRED, testBrightness), one.getColorState());
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
        assertEquals(OnOffType.ON, action.getSwitch());
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
            resource.setColor(color);
            State state = resource.getColorState();
            assertEquals(color, state);
        }
    }

    @Test
    void testSseEvent() {
        String json = load("event");
        List<Event> eventList = GSON.fromJson(json, Event.EVENT_LIST_TYPE);
        assertNotNull(eventList);
        assertEquals(2, eventList.size());
        Event event = eventList.get(0);
        List<Resource> resources = event.getData();
        assertEquals(9, resources.size());
        for (Resource r : resources) {
            assertNotEquals(ResourceType.ERROR, r.getType());
        }
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
        ZigbeeStatus zigbeeStatus = item.getZigBeeStatus();
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
