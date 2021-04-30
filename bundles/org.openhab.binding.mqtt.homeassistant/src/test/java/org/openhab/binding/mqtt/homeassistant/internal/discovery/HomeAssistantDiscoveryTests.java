package org.openhab.binding.mqtt.homeassistant.internal.discovery;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.homeassistant.internal.component.HAConfigurationTests;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.thing.type.ThingTypeRegistry;

@ExtendWith(MockitoExtension.class)
public class HomeAssistantDiscoveryTests {
    static final String BINDING_ID = "mqtt";
    static final String BRODGE_TYPE_ID = "homeassistant";
    static final String BRIDGE_TYPE_LABEL = "Homeassistant";
    static final ThingTypeUID BRIDGE_TYPE_UID = new ThingTypeUID(BINDING_ID, BRODGE_TYPE_ID);
    static final String BRIDGE_ID = UUID.randomUUID().toString();
    static final ThingUID BRIDGE_UID = new ThingUID(BRIDGE_TYPE_UID, BRIDGE_ID);

    private @Mock MqttBrokerConnection brokerConnection;
    private @Mock ThingTypeRegistry thingTypeRegistry;
    private HomeAssistantDiscovery discovery;

    @BeforeEach
    public void beforeEach() {
        discovery = new TestHomeAssistantDiscovery(new MqttChannelTypeProvider(thingTypeRegistry));
        ThingType baseThingType = ThingTypeBuilder.instance(BRIDGE_TYPE_UID, BRIDGE_TYPE_LABEL).buildBridge();
        Mockito.when(thingTypeRegistry.getThingType(BRIDGE_TYPE_UID)).thenReturn(baseThingType);
    }

    @Test
    public void testClimateThingDiscovery() throws Exception {
        var json = Files.readString(
                Path.of(HAConfigurationTests.class.getResource("configTS0601ClimateThermostat.json").toURI()));
        var climateUID = "0x847127fffe11dd6a_climate_zigbee2mqtt";
        var discoveryListener = new LatchDiscoveryListener();
        var latch = discoveryListener.createWaitForThingsDiscoveredLatch(1);

        discovery.addDiscoveryListener(discoveryListener);
        discovery.receivedMessage(BRIDGE_UID, brokerConnection, "homeassistant/climate/" + climateUID + "/config",
                json.getBytes(StandardCharsets.UTF_8));

        assert latch.await(3, TimeUnit.SECONDS);
        var discoveryResults = discoveryListener.getDiscoveryResults();
        assertThat(discoveryResults.size(), is(1));
        var result = discoveryResults.get(0);

        assertThat(result.getBridgeUID(), is(BRIDGE_UID));
        assertThat(result.getProperties().get(Thing.PROPERTY_MODEL_ID),
                is("Radiator valve with thermostat (TS0601_thermostat)"));
        assertThat(result.getProperties().get(Thing.PROPERTY_VENDOR), is("TuYa"));
        assertThat(result.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("Zigbee2MQTT 1.18.2"));
    }

    private static class TestHomeAssistantDiscovery extends HomeAssistantDiscovery {
        public TestHomeAssistantDiscovery(MqttChannelTypeProvider typeProvider) {
            this.typeProvider = typeProvider;
        }
    }

    @NonNullByDefault
    private static class LatchDiscoveryListener implements DiscoveryListener {
        private final CopyOnWriteArrayList<DiscoveryResult> discoveryResults = new CopyOnWriteArrayList<>();
        private @Nullable CountDownLatch latch;

        public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
            discoveryResults.add(result);
            if (latch != null) {
                latch.countDown();
            }
        }

        public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        }

        public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
            return Collections.emptyList();
        }

        public CopyOnWriteArrayList<DiscoveryResult> getDiscoveryResults() {
            return discoveryResults;
        }

        public CountDownLatch createWaitForThingsDiscoveredLatch(int count) {
            final var newLatch = new CountDownLatch(count);
            latch = newLatch;
            return newLatch;
        }
    }
}
