package org.openhab.binding.shelly.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYBULB;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;

import sun.misc.Unsafe;

class ShellyLightHandlerLightModelLockTest {

    public class TestLightHandler extends ShellyLightHandler {

        public TestLightHandler(Thing thing, ShellyTranslationProvider translationProvider,
                ShellyBindingRuntimeConfig bindingConfig, ShellyThingTable thingTable, Shelly1CoapServer coapServer,
                HttpClient httpClient, WebSocketClient webSocketClient) {
            super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient, webSocketClient);
        }

        public static TestLightHandler create(ThingTypeUID thingTypeUID) {
            try {
                // Obtain Unsafe
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                Unsafe unsafe = (Unsafe) f.get(null);

                // Allocate ShellyLightHandler WITHOUT calling its constructor
                TestLightHandler handler = (TestLightHandler) unsafe.allocateInstance(TestLightHandler.class);

                // Manually initialize required fields
                handler.profile = new ShellyDeviceProfile(thingTypeUID);
                handler.profile.initialized = true;
                handler.profile.isLight = true;
                handler.profile.isRGBW2 = true;
                handler.profile.inColor = true;
                handler.profile.device.mode = "color";

                Field lmField = ShellyLightHandler.class.getDeclaredField("lightModels");
                lmField.setAccessible(true);
                lmField.set(handler, new TreeMap<Integer, ShellyLightModel>());

                Field baseLog = ShellyBaseHandler.class.getDeclaredField("logger");
                baseLog.setAccessible(true);
                baseLog.set(handler, org.slf4j.LoggerFactory.getLogger("ShellyTest"));

                Field lightLog = ShellyLightHandler.class.getDeclaredField("logger");
                lightLog.setAccessible(true);
                lightLog.set(handler, org.slf4j.LoggerFactory.getLogger("ShellyTest"));

                Thing thing = mock(Thing.class);

                ThingUID uid = new ThingUID(thingTypeUID, "test");

                Configuration cfg = new Configuration();
                cfg.setProperties(new HashMap<>());

                when(thing.getUID()).thenReturn(uid);
                when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
                when(thing.getLabel()).thenReturn("TestThing");
                when(thing.getConfiguration()).thenReturn(cfg);

                Field thingField = BaseThingHandler.class.getDeclaredField("thing");
                thingField.setAccessible(true);
                thingField.set(handler, thing);

                Shelly1HttpApi api = mock(Shelly1HttpApi.class);
                Field apiField = ShellyBaseHandler.class.getDeclaredField("api");
                apiField.setAccessible(true);
                apiField.set(handler, api);

                return handler;

            } catch (Exception e) {
                throw new RuntimeException("Failed to create TestLightHandler", e);
            }
        }

        @Override
        public boolean updateChannel(String channelId, State value, boolean force) {
            return true;
        }

        public ShellyLightModel addTestModel(int id) {
            ShellyLightModel model = ShellyLightModel.create(this, id, new ThingTypeUID("shelly", "rgbw2"),
                    this.profile, Shelly1ApiJsonDTO.SHELLY_DIM_STEPSIZE);

            try {
                Field lmField = ShellyLightHandler.class.getDeclaredField("lightModels");
                lmField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<Integer, ShellyLightModel> map = (Map<Integer, ShellyLightModel>) lmField.get(this);
                map.put(id, model);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return model;
        }
    }

    @Test
    void testAcquireLockGetLightModelReleaseLock() {
        TestLightHandler handler = TestLightHandler.create(THING_TYPE_SHELLYBULB);

        // use a dummy command to ensure the light model is created and initialized
        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYBULB, "test"), "color"), "red"),
                new PercentType(50));

        handler.acquireLock();

        ShellyLightModel model = handler.getLightModel(0);
        assertNotNull(model);

        model.setOnOff(true);
        model.setBrightness(new PercentType(55));
        model.setRGBX(new int[] { 10, 20, 30, 40 });
        model.setColorTemp(3500);

        assertTrue(handler.releaseLock());

        ShellyLightModel m = handler.getLightModel(0);
        assertNotNull(m);

        assertArrayEquals(new int[] { 10, 20, 30, 40 }, m.getRGBX());
        assertEquals(new PercentType(55), m.getBrightnessState());
        assertEquals(OnOffType.ON, m.getOnOffState());
        assertEquals(QuantityType.valueOf(3500, Units.KELVIN), m.getColorTemperatureAbsoluteState());
    }
}
