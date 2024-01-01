/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.mqtt.homeassistant.internal.AbstractHomeAssistantTests;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.component.Climate;
import org.openhab.binding.mqtt.homeassistant.internal.component.Sensor;
import org.openhab.binding.mqtt.homeassistant.internal.component.Switch;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for {@link HomeAssistantThingHandler}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class HomeAssistantThingHandlerTests extends AbstractHomeAssistantTests {
    private static final int SUBSCRIBE_TIMEOUT = 10000;
    private static final int ATTRIBUTE_RECEIVE_TIMEOUT = 2000;

    private static final List<String> CONFIG_TOPICS = Arrays.asList("climate/0x847127fffe11dd6a_climate_zigbee2mqtt",
            "switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt",

            "sensor/0x1111111111111111_test_sensor_zigbee2mqtt", "camera/0x1111111111111111_test_camera_zigbee2mqtt",

            "cover/0x2222222222222222_test_cover_zigbee2mqtt", "fan/0x2222222222222222_test_fan_zigbee2mqtt",
            "light/0x2222222222222222_test_light_zigbee2mqtt", "lock/0x2222222222222222_test_lock_zigbee2mqtt");

    private static final List<String> MQTT_TOPICS = CONFIG_TOPICS.stream()
            .map(AbstractHomeAssistantTests::configTopicToMqtt).collect(Collectors.toList());

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;
    private @NonNullByDefault({}) HomeAssistantThingHandler thingHandler;
    private @NonNullByDefault({}) HomeAssistantThingHandler nonSpyThingHandler;

    @BeforeEach
    public void setup() {
        final var config = haThing.getConfiguration();

        config.put(HandlerConfiguration.PROPERTY_BASETOPIC, HandlerConfiguration.DEFAULT_BASETOPIC);
        config.put(HandlerConfiguration.PROPERTY_TOPICS, CONFIG_TOPICS);

        when(callbackMock.getBridge(eq(BRIDGE_UID))).thenReturn(bridgeThing);

        thingHandler = new HomeAssistantThingHandler(haThing, channelTypeProvider, transformationServiceProvider,
                SUBSCRIBE_TIMEOUT, ATTRIBUTE_RECEIVE_TIMEOUT);
        thingHandler.setConnection(bridgeConnection);
        thingHandler.setCallback(callbackMock);
        nonSpyThingHandler = thingHandler;
        thingHandler = spy(thingHandler);
    }

    @Test
    public void testInitialize() {
        // When initialize
        thingHandler.initialize();

        verify(callbackMock).statusUpdated(eq(haThing), any());
        // Expect a call to the bridge status changed, the start, the propertiesChanged method
        verify(thingHandler).bridgeStatusChanged(any());
        verify(thingHandler, timeout(SUBSCRIBE_TIMEOUT)).start(any());

        // Expect subscription on each topic from config
        MQTT_TOPICS.forEach(t -> {
            verify(bridgeConnection, timeout(SUBSCRIBE_TIMEOUT)).subscribe(eq(t), any());
        });

        verify(thingHandler, never()).componentDiscovered(any(), any());
        assertThat(haThing.getChannels().size(), CoreMatchers.is(0));
        // Components discovered after messages in corresponding topics
        var configTopic = "homeassistant/climate/0x847127fffe11dd6a_climate_zigbee2mqtt/config";
        thingHandler.discoverComponents.processMessage(configTopic,
                getResourceAsByteArray("component/configTS0601ClimateThermostat.json"));
        verify(thingHandler, times(1)).componentDiscovered(eq(new HaID(configTopic)), any(Climate.class));

        thingHandler.delayedProcessing.forceProcessNow();
        assertThat(haThing.getChannels().size(), CoreMatchers.is(6));
        verify(channelTypeProvider, times(6)).setChannelType(any(), any());
        verify(channelTypeProvider, times(1)).setChannelGroupType(any(), any());

        configTopic = "homeassistant/switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt/config";
        thingHandler.discoverComponents.processMessage(configTopic,
                getResourceAsByteArray("component/configTS0601AutoLock.json"));
        verify(thingHandler, times(2)).componentDiscovered(any(), any());
        verify(thingHandler, times(1)).componentDiscovered(eq(new HaID(configTopic)), any(Switch.class));

        thingHandler.delayedProcessing.forceProcessNow();
        assertThat(haThing.getChannels().size(), CoreMatchers.is(7));
        verify(channelTypeProvider, times(7)).setChannelType(any(), any());
        verify(channelTypeProvider, times(2)).setChannelGroupType(any(), any());
    }

    /**
     * Test where the same component is published twice to MQTT. The binding should handle this.
     *
     * @throws InterruptedException
     */
    @Test
    public void testDuplicateComponentPublish() throws InterruptedException {
        thingHandler.initialize();

        verify(callbackMock).statusUpdated(eq(haThing), any());
        // Expect a call to the bridge status changed, the start, the propertiesChanged method
        verify(thingHandler).bridgeStatusChanged(any());
        verify(thingHandler, timeout(SUBSCRIBE_TIMEOUT)).start(any());

        // Expect subscription on each topic from config
        MQTT_TOPICS.forEach(t -> {
            verify(bridgeConnection, timeout(SUBSCRIBE_TIMEOUT)).subscribe(eq(t), any());
        });

        verify(thingHandler, never()).componentDiscovered(any(), any());
        assertThat(haThing.getChannels().size(), CoreMatchers.is(0));

        //
        //
        // Publish sensor components with identical payload except for
        // change in "name" field. The binding should respect the latest discovery result.
        //
        // This simulates how multiple OpenMQTTGateway devices would publish
        // the same discovery topics for a particular Bluetooth sensor, and thus "competing" with similar but slightly
        // different discovery topics.
        //
        // In fact, only difference is actually "via_device" additional metadata field telling which OpenMQTTGateway
        // published the discovery topic.
        //
        //

        //
        // 1. publish corridor temperature sensor
        //
        var configTopicTempCorridor = "homeassistant/sensor/tempCorridor/config";
        thingHandler.discoverComponents.processMessage(configTopicTempCorridor, new String("{"//
                + "\"temperature_state_topic\": \"+/+/BTtoMQTT/mysensor\","//
                + "\"temperature_state_template\": \"{{ value_json.temperature }}\", "//
                + "\"name\": \"CorridorTemp\", "//
                + "\"unit_of_measurement\": \"°C\" "//
                + "}").getBytes(StandardCharsets.UTF_8));
        verify(thingHandler, times(1)).componentDiscovered(eq(new HaID(configTopicTempCorridor)), any(Sensor.class));
        thingHandler.delayedProcessing.forceProcessNow();
        waitForAssert(() -> {
            assertThat("1 channel created", thingHandler.getThing().getChannels().size() == 1);
        });

        //
        // 2. publish outside temperature sensor
        //
        var configTopicTempOutside = "homeassistant/sensor/tempOutside/config";
        thingHandler.discoverComponents.processMessage(configTopicTempOutside, new String("{"//
                + "\"temperature_state_topic\": \"+/+/BTtoMQTT/mysensor\","//
                + "\"temperature_state_template\": \"{{ value_json.temperature }}\", " //
                + "\"name\": \"OutsideTemp\", "//
                + "\"source\": \"gateway2\" "//
                + "}").getBytes(StandardCharsets.UTF_8));
        thingHandler.delayedProcessing.forceProcessNow();
        verify(thingHandler, times(1)).componentDiscovered(eq(new HaID(configTopicTempOutside)), any(Sensor.class));
        waitForAssert(() -> {
            assertThat("2 channel created", thingHandler.getThing().getChannels().size() == 2);
        });

        //
        // 3. publish corridor temperature sensor, this time with different name (openHAB channel label)
        //
        thingHandler.discoverComponents.processMessage(configTopicTempCorridor, new String("{"//
                + "\"temperature_state_topic\": \"+/+/BTtoMQTT/mysensor\","//
                + "\"temperature_state_template\": \"{{ value_json.temperature }}\", "//
                + "\"name\": \"CorridorTemp NEW\", "//
                + "\"unit_of_measurement\": \"°C\" "//
                + "}").getBytes(StandardCharsets.UTF_8));
        thingHandler.delayedProcessing.forceProcessNow();

        waitForAssert(() -> {
            assertThat("2 channel created", thingHandler.getThing().getChannels().size() == 2);
        });

        //
        // verify that both channels are there and the label corresponds to newer discovery topic payload
        //
        Channel corridorTempChannel = nonSpyThingHandler.getThing().getChannel("tempCorridor_5Fsensor#sensor");
        assertThat("Corridor temperature channel is created", corridorTempChannel, CoreMatchers.notNullValue());
        Objects.requireNonNull(corridorTempChannel); // for compiler
        assertThat("Corridor temperature channel is having the updated label from 2nd discovery topic publish",
                corridorTempChannel.getLabel(), CoreMatchers.is("CorridorTemp NEW"));

        Channel outsideTempChannel = nonSpyThingHandler.getThing().getChannel("tempOutside_5Fsensor#sensor");
        assertThat("Outside temperature channel is created", outsideTempChannel, CoreMatchers.notNullValue());

        verify(thingHandler, times(2)).componentDiscovered(eq(new HaID(configTopicTempCorridor)), any(Sensor.class));

        waitForAssert(() -> {
            assertThat("2 channel created", thingHandler.getThing().getChannels().size() == 2);
        });
    }

    @Test
    public void testDispose() {
        thingHandler.initialize();

        // Expect subscription on each topic from config
        CONFIG_TOPICS.forEach(t -> {
            var fullTopic = HandlerConfiguration.DEFAULT_BASETOPIC + "/" + t + "/config";
            verify(bridgeConnection, timeout(SUBSCRIBE_TIMEOUT)).subscribe(eq(fullTopic), any());
        });
        thingHandler.discoverComponents.processMessage(
                "homeassistant/climate/0x847127fffe11dd6a_climate_zigbee2mqtt/config",
                getResourceAsByteArray("component/configTS0601ClimateThermostat.json"));
        thingHandler.discoverComponents.processMessage(
                "homeassistant/switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt/config",
                getResourceAsByteArray("component/configTS0601AutoLock.json"));
        thingHandler.delayedProcessing.forceProcessNow();
        assertThat(haThing.getChannels().size(), CoreMatchers.is(7));
        verify(channelTypeProvider, times(7)).setChannelType(any(), any());

        // When dispose
        thingHandler.dispose();

        // Expect unsubscription on each topic from config
        MQTT_TOPICS.forEach(t -> {
            verify(bridgeConnection, timeout(SUBSCRIBE_TIMEOUT)).unsubscribe(eq(t), any());
        });

        // Expect channel types removed, 6 for climate and 1 for switch
        verify(channelTypeProvider, times(7)).removeChannelType(any());
        // Expect channel group types removed, 1 for each component
        verify(channelTypeProvider, times(2)).removeChannelGroupType(any());
    }

    @Test
    public void testProcessMessageFromUnsupportedComponent() {
        thingHandler.initialize();
        thingHandler.discoverComponents.processMessage("homeassistant/unsupportedType/id_zigbee2mqtt/config",
                "{}".getBytes(StandardCharsets.UTF_8));
        // Ignore unsupported component
        thingHandler.delayedProcessing.forceProcessNow();
        assertThat(haThing.getChannels().size(), CoreMatchers.is(0));
    }

    @Test
    public void testProcessMessageWithEmptyConfig() {
        thingHandler.initialize();
        thingHandler.discoverComponents.processMessage("homeassistant/sensor/id_zigbee2mqtt/config",
                "".getBytes(StandardCharsets.UTF_8));
        // Ignore component with empty config
        thingHandler.delayedProcessing.forceProcessNow();
        assertThat(haThing.getChannels().size(), CoreMatchers.is(0));
    }

    @Test
    public void testProcessMessageWithBadFormatConfig() {
        thingHandler.initialize();
        thingHandler.discoverComponents.processMessage("homeassistant/sensor/id_zigbee2mqtt/config",
                "{bad format}}".getBytes(StandardCharsets.UTF_8));
        // Ignore component with bad format config
        thingHandler.delayedProcessing.forceProcessNow();
        assertThat(haThing.getChannels().size(), CoreMatchers.is(0));
    }
}
