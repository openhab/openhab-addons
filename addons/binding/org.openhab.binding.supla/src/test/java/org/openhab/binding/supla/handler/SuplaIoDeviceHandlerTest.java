package org.openhab.binding.supla.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerWrapper;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.SuplaTest;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.api.ServerInfoManager;
import org.openhab.binding.supla.internal.channels.ChannelBuilder;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.supla.entities.*;
import org.openhab.binding.supla.internal.threads.ThreadPool;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.eclipse.smarthome.core.thing.ThingStatus.UNKNOWN;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_PENDING;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_IO_DEVICE_ID;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("UnnecessaryLocalVariable")
public class SuplaIoDeviceHandlerTest extends SuplaTest {
    private final long ioDeviceId = 100L;

    @InjectMocks
    private SuplaIoDeviceHandler handler;
    @Mock
    private Thing thing;
    @Mock
    private ThingRegistry thingRegistry;
    @Mock
    private Bridge bridge;
    @Mock
    private ThingHandlerCallback bridgeCallback;
    @Mock
    private ThingHandlerCallback callback;
    @Mock
    private ThreadPool threadPool;

    // for ApplicationContext
    @Mock
    private ServerInfoManager serverInfoManager;
    @Mock
    private IoDevicesManager ioDevicesManager;
    @Mock
    private ChannelBuilder channelBuilder;

    private ThingUID bridgeUID = new ThingUID("bridge:uid:1");
    private BaseThingHandlerWrapper wrapper;
    private SuplaCloudBridgeHandler bridgeHandler;
    private final Configuration configuration = new Configuration(ImmutableMap.<String, Object>builder()
            .put("server", server.getServer())
            .put("clientId", server.getClientId())
            .put("secret", new String(server.getSecret()))
            .put("username", server.getUsername())
            .put("password", new String(server.getPassword()))
            .put("refreshInterval", 30)
            .build());
    private final ApplicationContext context = new ApplicationContext(server);

    private final Set<SuplaChannel> channels = Sets.newHashSet(
            new SuplaChannel(1, 2, "cap", new SuplaType(4, "type"), new SuplaFunction(7, "func")),
            new SuplaChannel(10, 20, "cap0", new SuplaType(40, "type0"), new SuplaFunction(70, "func0"))
    );
    private final SuplaIoDevice ioDevice = new SuplaIoDevice(ioDeviceId, 1, true, "nameee", "commme",
            null, null, "guiggg", "ver", 1, channels);

    @Before
    public void init() {
        wrapper = new BaseThingHandlerWrapper(handler);
        wrapper.setThingRegistry(thingRegistry);

        given(thing.getBridgeUID()).willReturn(bridgeUID);
        given(thingRegistry.get(bridgeUID)).willReturn(bridge);
        bridgeHandler = spy(new SuplaCloudBridgeHandler(bridge, server -> context));
        bridgeHandler.setCallback(bridgeCallback);
        given(bridge.getHandler()).willReturn(bridgeHandler);

        handler.setCallback(callback);

        // Application Context
        context.setServerInfoManager(serverInfoManager);
        context.setIoDevicesManager(ioDevicesManager);
        context.setChannelBuilder(channelBuilder);

        given(bridge.getConfiguration()).willReturn(configuration);
        given(serverInfoManager.obtainServerInfo()).willReturn(Optional.of(new SuplaServerInfo()));
        bridgeHandler.initialize();

        // ThreadPool runs runnable
        doAnswer(invocationOnMock -> {
            invocationOnMock.getArgumentAt(0, Runnable.class).run();
            return null;
        }).when(threadPool).submit(any(Runnable.class));

        // init getting IoDevice
        Configuration config = new Configuration(ImmutableMap.<String, Object>builder().put(SUPLA_IO_DEVICE_ID, new BigDecimal(ioDeviceId)).build());
        given(thing.getConfiguration()).willReturn(config);

        given(ioDevicesManager.obtainIoDevice(ioDeviceId)).willReturn(Optional.of(ioDevice));
    }

    @Test
    public void shouldUpdateStatusAboutInitializingThing() {

        // given
        final ThingStatus expectedThingStatus = UNKNOWN;
        final ThingStatusDetail expectedThingStatusDetail = CONFIGURATION_PENDING;
        final String expectedDescription = "Thing is being configured asynchronously";

        // when
        handler.initialize();

        // then
        verifyStatusUpdate(expectedThingStatus, expectedThingStatusDetail, expectedDescription);
    }

    @Test
    public void shouldSubmitRunnableToThreadPoolToInitialize() {

        // given

        // when
        handler.initialize();

        // then
        verify(threadPool).submit(any(Runnable.class));
    }

    @Test
    public void shouldUpdateStatusToUnknownIfThereIsNoBridgeHandlerBecauseOfLackBridgeUID() {

        // given
        ThingStatusInfoBuilder statusBuilder = ThingStatusInfoBuilder.create(UNKNOWN, CONFIGURATION_ERROR);
        ThingStatusInfo statusInfo = statusBuilder.withDescription("Required bridge not defined for device").build();

        given(thing.getBridgeUID()).willReturn(null);

        // when
        handler.initialize();

        // then
        verify(callback).statusUpdated(thing, statusInfo);
    }

    @Test
    public void shouldUpdateStatusToUnknownIfThereIsNoBridgeHandlerBecauseOfLackThingRegistry() {

        // given
        ThingStatusInfoBuilder statusBuilder = ThingStatusInfoBuilder.create(UNKNOWN, CONFIGURATION_ERROR);
        ThingStatusInfo statusInfo = statusBuilder.withDescription("Required bridge not defined for device").build();

        wrapper.setThingRegistry(null);

        // when
        handler.initialize();

        // then
        verify(callback).statusUpdated(thing, statusInfo);
    }

    @Test
    public void shouldObtainDeviceByGivenId() {

        // given

        // when
        handler.initialize();

        // then
        verify(ioDevicesManager).obtainIoDevice(ioDeviceId);
    }

    @Test
    public void shouldChangeStatusToUnknownIfThereIsNotIdInConfig() {

        // given
        Configuration config = new Configuration(new HashMap<>());
        given(thing.getConfiguration()).willReturn(config);

        final ThingStatus status = UNKNOWN;
        final ThingStatusDetail statusDetail = CONFIGURATION_ERROR;
        final String description = format("At property \"%s\" should be Supla device ID", SUPLA_IO_DEVICE_ID);

        // when
        handler.initialize();

        // then
        verifyStatusUpdate(status, statusDetail, description);
    }


    @Test
    public void shouldChangeStatusToUnknownIfIdIsNotBigDecimal() {

        // given
        final String notBigDecimalId = "100";
        Configuration config = new Configuration(ImmutableMap.<String, Object>builder().put(SUPLA_IO_DEVICE_ID, notBigDecimalId).build());
        given(thing.getConfiguration()).willReturn(config);

        final ThingStatus status = UNKNOWN;
        final ThingStatusDetail statusDetail = CONFIGURATION_ERROR;
        final String description = format("ID \"%s\" is not valid long! Current type is %s.", notBigDecimalId,
                notBigDecimalId.getClass().getSimpleName());

        // when
        handler.initialize();

        // then
        verifyStatusUpdate(status, statusDetail, description);
    }

    @Test
    public void shouldChangeStatusToUnknownIfThereIsNoIoDevice() {

        // given
        final ThingStatus status = UNKNOWN;
        final ThingStatusDetail statusDetail = CONFIGURATION_ERROR;
        final String description = format("Can not find Supla device with ID \"%s\"!", ioDeviceId);

        given(ioDevicesManager.obtainIoDevice(ioDeviceId)).willReturn(Optional.empty());

        // when
        handler.initialize();

        // then
        verifyStatusUpdate(status, statusDetail, description);
    }

    @Test
    public void shouldBuildChannelsForIoDevice() {

        // given

        // when
        handler.initialize();

        // then
        verify(channelBuilder).buildChannels(thing.getUID(), channels);
    }

    @Test
    public void shouldUpdateThingWitchChannels() {

        // given

        // when
        handler.initialize();

        // then
        verify(callback).thingUpdated(any(Thing.class));
    }

    @Test
    public void shouldPropagateDisposeToBridgeHandler() {

        // given
        handler.initialize();

        // when
        handler.dispose();

        // then
        verify(bridgeHandler).unregisterSuplaIoDeviceManagerHandler(handler);
    }

    private void verifyStatusUpdate(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        ThingStatusInfoBuilder statusBuilder = ThingStatusInfoBuilder.create(status, statusDetail);
        ThingStatusInfo statusInfo = statusBuilder.withDescription(description).build();

        verify(callback).statusUpdated(thing, statusInfo);
    }
}
