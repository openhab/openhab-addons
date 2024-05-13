package org.openhab.binding.onecta.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.handler.*;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
public class OnectaBridgeHandlerFactoryTest {

    public static final String USERID = "Userid";
    public static final String PASSWORD = "Password";
    public static final String REFRESH_TOKEN = "ThisIsARefreshToken";
    public static final String UNITID = "ThisIsAUnitID";

    private Map<String, Object> bridgeProperties = new HashMap<>();
    private Configuration thingConfiguration = new Configuration();

    private OnectaBridgeHandlerFactory handler;

    @Mock
    private HttpClientFactory httpClientFactoryMock;
    @Mock
    private TimeZoneProvider timeZoneProviderMock;
    @Mock
    private OnectaBridgeHandler onectaBridgeHandlerMock;
    @Mock
    private OnectaDeviceHandler onectaDeviceHandlerMock;
    @Mock
    private OnectaGatewayHandler onectaGatewayHandlerMock;
    @Mock
    private OnectaWaterTankHandler onectaWaterTankHandlerMock;
    @Mock
    private OnectaIndoorUnitHandler onectaIndoorUnitHandlerMock;
    @Mock
    private BundleContext bundleContextMock;

    @BeforeEach
    public void setUp() {
        handler = new OnectaBridgeHandlerFactory(httpClientFactoryMock, timeZoneProviderMock);
        bridgeProperties.put(CHANNEL_REFRESH_TOKEN, REFRESH_TOKEN);
        bridgeProperties.put(CHANNEL_USERID, USERID);
        bridgeProperties.put(CHANNEL_PASSWORD, PASSWORD);
        bridgeProperties.put(CHANNEL_REFRESHINTERVAL, "10");
        bridgeProperties.put(CHANNEL_UNITID, UNITID);
        thingConfiguration.setProperties(bridgeProperties);
    }

    @Test
    public void supportsThingTypeTest() {
        assertEquals(true, handler.supportsThingType(BRIDGE_THING_TYPE));
        assertEquals(true, handler.supportsThingType(DEVICE_THING_TYPE));
        assertEquals(true, handler.supportsThingType(GATEWAY_THING_TYPE));
        assertEquals(true, handler.supportsThingType(WATERTANK_THING_TYPE));
        assertEquals(true, handler.supportsThingType(INDOORUNIT_THING_TYPE));
    }

    @Test
    public void createHandlerTest() throws NoSuchFieldException, IllegalAccessException {

        Field privateDataTransServiceField = BaseThingHandlerFactory.class.getDeclaredField("bundleContext");
        privateDataTransServiceField.setAccessible(true);
        privateDataTransServiceField.set(handler, bundleContextMock);

        Thing bridgeThing = new DummyBridge(BRIDGE_THING_TYPE, onectaBridgeHandlerMock, ThingStatus.ONLINE);
        ThingHandler thingHandler = handler.createHandler(bridgeThing);
        assertEquals(true, thingHandler instanceof OnectaBridgeHandler);

        Configuration configuration = new Configuration();
        configuration.put(CHANNEL_UNITID, UNITID);

        Thing dummyThing = new DummyThing(DEVICE_THING_TYPE, onectaDeviceHandlerMock, ThingStatus.ONLINE);
        ((DummyThing) dummyThing).setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertEquals(true, thingHandler instanceof OnectaDeviceHandler);

        dummyThing = new DummyThing(GATEWAY_THING_TYPE, onectaGatewayHandlerMock, ThingStatus.ONLINE);
        ((DummyThing) dummyThing).setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertEquals(true, thingHandler instanceof OnectaGatewayHandler);

        dummyThing = new DummyThing(WATERTANK_THING_TYPE, onectaWaterTankHandlerMock, ThingStatus.ONLINE);
        ((DummyThing) dummyThing).setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertEquals(true, thingHandler instanceof OnectaWaterTankHandler);

        dummyThing = new DummyThing(INDOORUNIT_THING_TYPE, onectaIndoorUnitHandlerMock, ThingStatus.ONLINE);
        ((DummyThing) dummyThing).setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertEquals(true, thingHandler instanceof OnectaIndoorUnitHandler);
    }
}
