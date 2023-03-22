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
package org.openhab.binding.tradfri.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;
import static org.openhab.binding.tradfri.internal.config.TradfriDeviceConfig.CONFIG_ID;

import java.util.Collection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.tradfri.internal.handler.TradfriGatewayHandler;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultFlag;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests for {@link TradfriDiscoveryService}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Christoph Weitkamp - Added support for remote controller and motion sensor devices (read-only battery level)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TradfriDiscoveryServiceTest {

    private static final ThingUID GATEWAY_THING_UID = new ThingUID("tradfri:gateway:1");

    private @Mock TradfriGatewayHandler handler;

    private final DiscoveryListener listener = new DiscoveryListener() {
        @Override
        public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        }

        @Override
        public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
            discoveryResult = result;
        }

        @Override
        public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
            return null;
        }
    };

    private DiscoveryResult discoveryResult;

    private TradfriDiscoveryService discovery;

    @BeforeEach
    public void setUp() {
        when(handler.getThing()).thenReturn(BridgeBuilder.create(GATEWAY_TYPE_UID, "1").build());

        discovery = new TradfriDiscoveryService();
        discovery.setThingHandler(handler);
        discovery.addDiscoveryListener(listener);
    }

    @AfterEach
    public void cleanUp() {
        discoveryResult = null;
    }

    @Test
    public void correctSupportedTypes() {
        assertThat(discovery.getSupportedThingTypes().size(), is(9));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_DIMMABLE_LIGHT));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_COLOR_TEMP_LIGHT));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_COLOR_LIGHT));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_DIMMER));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_MOTION_SENSOR));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_REMOTE_CONTROL));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_OPEN_CLOSE_REMOTE_CONTROL));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_ONOFF_PLUG));
        assertTrue(discovery.getSupportedThingTypes().contains(THING_TYPE_BLINDS));
    }

    @Test
    public void validDiscoveryResultWhiteLightW() {
        String json = "{\"9001\":\"TRADFRI bulb E27 W opal 1000lm\",\"9002\":1492856270,\"9020\":1507194357,\"9003\":65537,\"3311\":[{\"5850\":1,\"5851\":254,\"9003\":0}],\"9054\":0,\"5750\":2,\"9019\":1,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI bulb E27 W opal 1000lm\",\"2\":\"\",\"3\":\"1.2.214\",\"6\":1}}";
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        discovery.onUpdate("65537", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0100:1:65537")));
        assertThat(discoveryResult.getThingTypeUID(), is(THING_TYPE_DIMMABLE_LIGHT));
        assertThat(discoveryResult.getBridgeUID(), is(GATEWAY_THING_UID));
        assertThat(discoveryResult.getProperties().get(CONFIG_ID), is(65537));
        assertThat(discoveryResult.getRepresentationProperty(), is(CONFIG_ID));
    }

    @Test
    public void validDiscoveryResultWhiteLightWS() {
        String json = "{\"9001\":\"TRADFRI bulb E27 WS opal 980lm\",\"9002\":1492955148,\"9020\":1507200447,\"9003\":65537,\"3311\":[{\"5710\":26909,\"5850\":1,\"5851\":203,\"5707\":0,\"5708\":0,\"5709\":30140,\"5711\":370,\"5706\":\"f1e0b5\",\"9003\":0}],\"9054\":0,\"5750\":2,\"9019\":1,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI bulb E27 WS opal 980lm\",\"2\":\"\",\"3\":\"1.2.217\",\"6\":1}}";
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        discovery.onUpdate("65537", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0220:1:65537")));
        assertThat(discoveryResult.getThingTypeUID(), is(THING_TYPE_COLOR_TEMP_LIGHT));
        assertThat(discoveryResult.getBridgeUID(), is(GATEWAY_THING_UID));
        assertThat(discoveryResult.getProperties().get(CONFIG_ID), is(65537));
        assertThat(discoveryResult.getRepresentationProperty(), is(CONFIG_ID));
    }

    @Test
    public void validDiscoveryResultWhiteLightWSWithIncompleteJson() {
        // We do not always receive a COLOR = "5706" attribute, even the light supports it - but the gateway does not
        // seem to have this information, if the bulb is unreachable.
        String json = "{\"9001\":\"TRADFRI bulb E27 WS opal 980lm\",\"9002\":1492955148,\"9020\":1506968670,\"9003\":65537,\"3311\":[{\"9003\":0}],\"9054\":0,\"5750\":2,\"9019\":0,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI bulb E27 WS opal 980lm\",\"2\":\"\",\"3\":\"1.2.217\",\"6\":1}}";
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        discovery.onUpdate("65537", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0220:1:65537")));
        assertThat(discoveryResult.getThingTypeUID(), is(THING_TYPE_COLOR_TEMP_LIGHT));
        assertThat(discoveryResult.getBridgeUID(), is(GATEWAY_THING_UID));
        assertThat(discoveryResult.getProperties().get(CONFIG_ID), is(65537));
        assertThat(discoveryResult.getRepresentationProperty(), is(CONFIG_ID));
    }

    @Test
    public void validDiscoveryResultColorLightCWS() {
        String json = "{\"9001\":\"TRADFRI bulb E27 CWS opal 600lm\",\"9002\":1505151864,\"9020\":1505433527,\"9003\":65550,\"9019\":1,\"9054\":0,\"5750\":2,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI bulb E27 CWS opal 600lm\",\"2\":\"\",\"3\":\"1.3.002\",\"6\":1},\"3311\":[{\"5850\":1,\"5708\":0,\"5851\":254,\"5707\":0,\"5709\":33137,\"5710\":27211,\"5711\":0,\"5706\":\"efd275\",\"9003\":0}]}";
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        discovery.onUpdate("65550", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0210:1:65550")));
        assertThat(discoveryResult.getThingTypeUID(), is(THING_TYPE_COLOR_LIGHT));
        assertThat(discoveryResult.getBridgeUID(), is(GATEWAY_THING_UID));
        assertThat(discoveryResult.getProperties().get(CONFIG_ID), is(65550));
        assertThat(discoveryResult.getRepresentationProperty(), is(CONFIG_ID));
    }

    @Test
    public void validDiscoveryResultAlternativeColorLightCWS() {
        String json = "{\"3311\":[{\"5850\":1,\"5709\":32886,\"5851\":216,\"5707\":5309,\"5708\":52400,\"5710\":27217,\"5706\":\"efd275\",\"9003\":0}],\"9001\":\"Mushroom lamp\",\"9002\":1571036916,\"9020\":1571588312,\"9003\":65539,\"9054\":0,\"9019\":1,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI bulb E27 C\\/WS opal 600\",\"2\":\"\",\"3\":\"1.3.009\",\"6\":1},\"5750\":2}";
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        discovery.onUpdate("65539", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0210:1:65539")));
        assertThat(discoveryResult.getThingTypeUID(), is(THING_TYPE_COLOR_LIGHT));
        assertThat(discoveryResult.getBridgeUID(), is(GATEWAY_THING_UID));
        assertThat(discoveryResult.getProperties().get(CONFIG_ID), is(65539));
        assertThat(discoveryResult.getRepresentationProperty(), is(CONFIG_ID));
    }

    @Test
    public void validDiscoveryResultRemoteControl() {
        String json = "{\"9001\":\"TRADFRI remote control\",\"9002\":1492843083,\"9020\":1506977986,\"9003\":65536,\"9054\":0,\"5750\":0,\"9019\":1,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI remote control\",\"2\":\"\",\"3\":\"1.2.214\",\"6\":3,\"9\":47},\"15009\":[{\"9003\":0}]}";
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        discovery.onUpdate("65536", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0830:1:65536")));
        assertThat(discoveryResult.getThingTypeUID(), is(THING_TYPE_REMOTE_CONTROL));
        assertThat(discoveryResult.getBridgeUID(), is(GATEWAY_THING_UID));
        assertThat(discoveryResult.getProperties().get(CONFIG_ID), is(65536));
        assertThat(discoveryResult.getRepresentationProperty(), is(CONFIG_ID));
    }

    @Test
    public void validDiscoveryResultWirelessDimmer() {
        String json = "{\"9001\":\"TRADFRI wireless dimmer\",\"9002\":1492843083,\"9020\":1506977986,\"9003\":65536,\"9054\":0,\"5750\":0,\"9019\":1,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI wireless dimmer\",\"2\":\"\",\"3\":\"1.2.214\",\"6\":3,\"9\":47},\"15009\":[{\"9003\":0}]}";
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        discovery.onUpdate("65536", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0820:1:65536")));
        assertThat(discoveryResult.getThingTypeUID(), is(THING_TYPE_DIMMER));
        assertThat(discoveryResult.getBridgeUID(), is(GATEWAY_THING_UID));
        assertThat(discoveryResult.getProperties().get(CONFIG_ID), is(65536));
        assertThat(discoveryResult.getRepresentationProperty(), is(CONFIG_ID));
    }

    @Test
    public void validDiscoveryResultMotionSensor() {
        String json = "{\"9001\":\"TRADFRI motion sensor\",\"9002\":1492955083,\"9020\":1507120083,\"9003\":65538,\"9054\":0,\"5750\":4,\"9019\":1,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI motion sensor\",\"2\":\"\",\"3\":\"1.2.214\",\"6\":3,\"9\":60},\"3300\":[{\"9003\":0}]}";
        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        discovery.onUpdate("65538", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0107:1:65538")));
        assertThat(discoveryResult.getThingTypeUID(), is(THING_TYPE_MOTION_SENSOR));
        assertThat(discoveryResult.getBridgeUID(), is(GATEWAY_THING_UID));
        assertThat(discoveryResult.getProperties().get(CONFIG_ID), is(65538));
        assertThat(discoveryResult.getRepresentationProperty(), is(CONFIG_ID));
    }
}
