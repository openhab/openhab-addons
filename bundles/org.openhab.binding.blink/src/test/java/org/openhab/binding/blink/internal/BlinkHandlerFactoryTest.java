package org.openhab.binding.blink.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.blink.internal.handler.AccountHandler;
import org.openhab.binding.blink.internal.handler.CameraHandler;
import org.openhab.binding.blink.internal.handler.NetworkHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * The {@link BlinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class BlinkHandlerFactoryTest {

    private static final String BINDING_NAME = "blink";
    private static final String ACCOUNT = "account";
    private static final String CAMERA = "camera";
    private static final String NETWORK = "network";
    private @Mock @NonNullByDefault({}) HttpService httpService;
    private @Mock @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @Mock @NonNullByDefault({}) BundleContext mockBundleContext;

    private BlinkHandlerFactory factory = new BlinkHandlerFactory(httpService, httpClientFactory);

    static List<@Nullable String> thingUIDs() {
        ArrayList<@Nullable String> uids = new ArrayList<>();
        uids.add(ACCOUNT);
        uids.add(CAMERA);
        uids.add(NETWORK);
        uids.add(null);
        return uids;
    }

    void setupMocks() {
        when(httpClientFactory.getCommonHttpClient()).thenReturn(new HttpClient());
        factory = new BlinkHandlerFactory(httpService, httpClientFactory) {
            @Override
            protected BundleContext getBundleContext() {
                return mockBundleContext;
            }
        };
    }

    @Test
    void supportsGivenNumberOfThings() {
        assertThat(BlinkHandlerFactory.SUPPORTED_THING_TYPES_UIDS.size(), is(thingUIDs().size() - 1));
    }

    @ParameterizedTest
    @MethodSource("thingUIDs")
    void supportsCorrectThingType(@Nullable String uid) {
        ThingTypeUID thingTypeUID = (uid == null) ?
                new ThingTypeUID(BINDING_NAME, "hurz") :
                new ThingTypeUID(BINDING_NAME, uid);
        assertThat(factory.supportsThingType(thingTypeUID), is(uid != null));
    }

    private @Mock @NonNullByDefault({}) Thing thing;

    @Test
    void createHandler_unknown() {
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID(BINDING_NAME, "hurz"));
        ThingHandler handler = factory.createHandler(thing);
        assertThat(handler, is(nullValue()));
    }

    private @Mock @NonNullByDefault({}) Bridge bridge;

    @Test
    void createHandler_account() {
        setupMocks();
        when(bridge.getThingTypeUID()).thenReturn(new ThingTypeUID(BINDING_NAME, ACCOUNT));
        ThingHandler handler = factory.createHandler(bridge);
        assertThat(handler, is(notNullValue()));
        assertThat(handler.getClass(), is(AccountHandler.class));
        // could test assignment of field values of handler here
    }

    @Test
    void createHandler_camera() {
        setupMocks();
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID(BINDING_NAME, CAMERA));
        ThingHandler handler = factory.createHandler(thing);
        assertThat(handler, is(notNullValue()));
        assertThat(handler.getClass(), is(CameraHandler.class));
        // could test assignment of field values of handler here
    }

    @Test
    void createHandler_network() {
        setupMocks();
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID(BINDING_NAME, NETWORK));
        ThingHandler handler = factory.createHandler(thing);
        assertThat(handler, is(notNullValue()));
        assertThat(handler.getClass(), is(NetworkHandler.class));
        // could test assignment of field values of handler here
    }

}