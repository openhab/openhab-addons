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
package org.openhab.binding.mielecloud.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.setPrivate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.auth.OpenHabOAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.ReflectionUtil;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebservice;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebserviceFactory;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.ProgramStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemBuilder;
import org.openhab.core.items.ItemBuilderFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMieleThingHandlerTest extends JavaOSGiTest {
    protected static final State NULL_VALUE_STATE = UnDefType.UNDEF;

    @Nullable
    private Bridge bridge;
    @Nullable
    private MieleBridgeHandler bridgeHandler;
    @Nullable
    private ThingRegistry thingRegistry;
    @Nullable
    private MieleWebservice webserviceMock;
    @Nullable
    private AbstractMieleThingHandler thingHandler;

    @Nullable
    private ItemRegistry itemRegistry;

    protected Bridge getBridge() {
        assertNotNull(bridge);
        return Objects.requireNonNull(bridge);
    }

    protected MieleBridgeHandler getBridgeHandler() {
        assertNotNull(bridgeHandler);
        return Objects.requireNonNull(bridgeHandler);
    }

    protected ThingRegistry getThingRegistry() {
        assertNotNull(thingRegistry);
        return Objects.requireNonNull(thingRegistry);
    }

    protected MieleWebservice getWebserviceMock() {
        assertNotNull(webserviceMock);
        return Objects.requireNonNull(webserviceMock);
    }

    protected AbstractMieleThingHandler getThingHandler() {
        assertNotNull(thingHandler);
        return Objects.requireNonNull(thingHandler);
    }

    protected ItemRegistry getItemRegistry() {
        assertNotNull(itemRegistry);
        return Objects.requireNonNull(itemRegistry);
    }

    @Override
    protected void waitForAssert(Runnable assertion) {
        // Use a larger timeout to avoid test failures in the CI build.
        waitForAssert(assertion, 2 * DFL_TIMEOUT, 2 * DFL_SLEEP_TIME);
    }

    private void setUpThingRegistry() {
        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertNotNull(thingRegistry, "Thing registry is missing");
    }

    private void setUpItemRegistry() {
        itemRegistry = getService(ItemRegistry.class, ItemRegistry.class);
        assertNotNull(itemRegistry);
    }

    private void setUpWebservice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        webserviceMock = mock(MieleWebservice.class);
        when(getWebserviceMock().hasAccessToken()).thenReturn(true);

        MieleWebserviceFactory webserviceFactory = mock(MieleWebserviceFactory.class);
        when(webserviceFactory.create(any())).thenReturn(getWebserviceMock());

        MieleHandlerFactory factory = getService(ThingHandlerFactory.class, MieleHandlerFactory.class);
        assertNotNull(factory);
        setPrivate(Objects.requireNonNull(factory), "webserviceFactory", webserviceFactory);
    }

    private void setUpBridge() throws Exception {
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken(ACCESS_TOKEN);

        OAuthClientService oAuthClientService = mock(OAuthClientService.class);
        when(oAuthClientService.getAccessTokenResponse()).thenReturn(accessTokenResponse);

        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        when(oAuthFactory
                .getOAuthClientService(MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString()))
                .thenReturn(oAuthClientService);

        OpenHabOAuthTokenRefresher tokenRefresher = getService(OAuthTokenRefresher.class,
                OpenHabOAuthTokenRefresher.class);
        assertNotNull(tokenRefresher);
        setPrivate(Objects.requireNonNull(tokenRefresher), "oauthFactory", oAuthFactory);

        bridge = BridgeBuilder
                .create(MieleCloudBindingConstants.THING_TYPE_BRIDGE,
                        MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID)
                .withLabel("Miele@home Account")
                .withConfiguration(new Configuration(Map.of(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL,
                        MieleCloudBindingIntegrationTestConstants.EMAIL)))
                .build();
        assertNotNull(bridge);

        getThingRegistry().add(getBridge());

        // then:
        waitForAssert(() -> {
            assertNotNull(getBridge().getHandler());
            assertTrue(getBridge().getHandler() instanceof MieleBridgeHandler, "Handler type is wrong");
        });

        MieleBridgeHandler bridgeHandler = (MieleBridgeHandler) getBridge().getHandler();
        assertNotNull(bridgeHandler);

        waitForAssert(() -> {
            assertNotNull(bridgeHandler.getThing());
        });

        bridgeHandler.initialize();
        bridgeHandler.onConnectionAlive();
        setPrivate(bridgeHandler, "discoveryService", null);
        this.bridgeHandler = bridgeHandler;
    }

    protected AbstractMieleThingHandler createThingHandler(ThingTypeUID thingTypeUid, ThingUID thingUid,
            Class<? extends AbstractMieleThingHandler> expectedHandlerClass, String deviceIdentifier,
            String thingTypeVersion) {
        ThingRegistry registry = getThingRegistry();

        List<Channel> channels = createChannelsForThingHandler(thingTypeUid, thingUid);

        Thing thing = ThingBuilder.create(thingTypeUid, thingUid)
                .withConfiguration(new Configuration(
                        Map.of(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER, deviceIdentifier)))
                .withBridge(getBridge().getUID()).withChannels(channels).withLabel("DA-6996")
                .withProperty("thingTypeVersion", thingTypeVersion).build();
        assertNotNull(thing);

        registry.add(thing);

        waitForAssert(() -> {
            ThingHandler handler = thing.getHandler();
            assertNotNull(handler);
            assertTrue(expectedHandlerClass.isAssignableFrom(handler.getClass()), "Handler type is wrong");
        });

        createItemsForChannels(thing);
        linkChannelsToItems(thing);

        ThingHandler handler = thing.getHandler();
        assertNotNull(handler);
        AbstractMieleThingHandler mieleThingHandler = (AbstractMieleThingHandler) Objects.requireNonNull(handler);

        waitForAssert(() -> {
            try {
                assertNotNull(ReflectionUtil.invokePrivate(mieleThingHandler, "getBridge"));
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
            assertNotNull(getBridge().getThing(thingUid));
        });

        return mieleThingHandler;
    }

    private List<Channel> createChannelsForThingHandler(ThingTypeUID thingTypeUid, ThingUID thingUid) {
        ChannelTypeRegistry channelTypeRegistry = getService(ChannelTypeRegistry.class, ChannelTypeRegistry.class);
        assertNotNull(channelTypeRegistry);

        ThingTypeRegistry thingTypeRegistry = getService(ThingTypeRegistry.class, ThingTypeRegistry.class);
        assertNotNull(thingTypeRegistry);

        ThingType thingType = thingTypeRegistry.getThingType(thingTypeUid);
        assertNotNull(thingType);

        List<ChannelDefinition> channelDefinitions = thingType.getChannelDefinitions();
        assertNotNull(channelDefinitions);

        List<Channel> channels = new ArrayList<>();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            ChannelTypeUID channelTypeUid = channelDefinition.getChannelTypeUID();
            assertNotNull(channelTypeUid);

            ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUid);
            assertNotNull(channelType);

            String acceptedItemType = channelType.getItemType();
            assertNotNull(acceptedItemType);

            String channelId = channelDefinition.getId();
            assertNotNull(channelId);

            ChannelUID channelUid = new ChannelUID(thingUid, channelId);
            assertNotNull(channelUid);

            Channel channel = ChannelBuilder.create(channelUid, acceptedItemType).build();
            assertNotNull(channel);

            channels.add(channel);
        }

        return channels;
    }

    private void createItemsForChannels(Thing thing) {
        ItemBuilderFactory itemBuilderFactory = getService(ItemBuilderFactory.class);
        assertNotNull(itemBuilderFactory);

        for (Channel channel : thing.getChannels()) {
            String acceptedItemType = channel.getAcceptedItemType();
            assertNotNull(acceptedItemType);

            ItemBuilder itemBuilder = itemBuilderFactory.newItemBuilder(Objects.requireNonNull(acceptedItemType),
                    channel.getUID().getId());
            assertNotNull(itemBuilder);

            Item item = itemBuilder.build();
            assertNotNull(item);

            getItemRegistry().add(item);
        }
    }

    private void linkChannelsToItems(Thing thing) {
        ItemChannelLinkRegistry itemChannelLinkRegistry = getService(ItemChannelLinkRegistry.class,
                ItemChannelLinkRegistry.class);
        assertNotNull(itemChannelLinkRegistry);

        for (Channel channel : thing.getChannels()) {
            String itemName = channel.getUID().getId();
            assertNotNull(itemName);

            ChannelUID channelUid = channel.getUID();
            assertNotNull(channelUid);

            ItemChannelLink link = itemChannelLinkRegistry.add(new ItemChannelLink(itemName, channelUid));
            assertNotNull(link);
        }
    }

    protected ChannelUID channel(String id) {
        return new ChannelUID(getThingHandler().getThing().getUID(), id);
    }

    @BeforeEach
    public void setUpAbstractMieleThingHandlerTest() throws Exception {
        registerVolatileStorageService();
        setUpThingRegistry();
        setUpItemRegistry();
        setUpWebservice();
    }

    protected void setUpBridgeAndThing() throws Exception {
        setUpBridge();
        thingHandler = setUpThingHandler();
    }

    private void assertThingStatusIs(Thing thing, ThingStatus expectedStatus, ThingStatusDetail expectedStatusDetail) {
        assertThingStatusIs(thing, expectedStatus, expectedStatusDetail, null);
    }

    private void assertThingStatusIs(Thing thing, ThingStatus expectedStatus, ThingStatusDetail expectedStatusDetail,
            @Nullable String expectedDescription) {
        assertEquals(expectedStatus, thing.getStatus());
        assertEquals(expectedStatusDetail, thing.getStatusInfo().getStatusDetail());
        if (expectedDescription == null) {
            assertNull(thing.getStatusInfo().getDescription());
        } else {
            assertEquals(expectedDescription, thing.getStatusInfo().getDescription());
        }
    }

    protected State getChannelState(String channelUid) {
        Item item = getItemRegistry().get(channelUid);
        assertNotNull(item, "Item for channel UID " + channelUid + " is null.");
        return item.getState();
    }

    /**
     * Sets up the {@link ThingHandler} under test.
     *
     * @return The created {@link ThingHandler}.
     */
    protected abstract AbstractMieleThingHandler setUpThingHandler();

    @AfterEach
    public void tearDownAbstractMieleThingHandlerTest() {
        getThingRegistry().forceRemove(getThingHandler().getThing().getUID());
        getThingRegistry().forceRemove(getBridge().getUID());
    }

    @Test
    public void testCachedStateIsQueriedOnInitialize() throws Exception {
        // given:
        setUpBridgeAndThing();

        // then:
        verify(getWebserviceMock()).dispatchDeviceState(SERIAL_NUMBER);
    }

    @Test
    public void testThingStatusIsOfflineWithDetailGoneAndDetailMessageWhenDeviceIsRemoved() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getBridgeHandler().onDeviceRemoved(SERIAL_NUMBER);

        // then:
        Thing thing = getThingHandler().getThing();
        assertThingStatusIs(thing, ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                "@text/mielecloud.thing.status.removed");
    }

    private DeviceState createDeviceStateMock(StateType stateType, String localizedState) {
        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getDeviceIdentifier()).thenReturn(getThingHandler().getThing().getUID().getId());
        when(deviceState.getRawType()).thenReturn(DeviceType.UNKNOWN);
        when(deviceState.getStateType()).thenReturn(Optional.of(stateType));
        when(deviceState.isInState(any())).thenCallRealMethod();
        when(deviceState.getStatus()).thenReturn(Optional.of(localizedState));
        return deviceState;
    }

    @Test
    public void testStatusIsSetToOnlineWhenDeviceStateIsValid() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = createDeviceStateMock(StateType.ON, "On");

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        assertThingStatusIs(getThingHandler().getThing(), ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testStatusIsSetToOfflineWhenDeviceIsNotConnected() throws Exception {
        // given:
        setUpBridgeAndThing();

        DeviceState deviceState = createDeviceStateMock(StateType.NOT_CONNECTED, "Not connected");

        // when:
        getBridgeHandler().onDeviceStateUpdated(deviceState);

        // then:
        assertThingStatusIs(getThingHandler().getThing(), ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/mielecloud.thing.status.disconnected");
    }

    @Test
    public void testFailingPutProcessActionDoesNotSetTheDeviceToOffline() throws Exception {
        // given:
        doThrow(MieleWebserviceException.class).when(getWebserviceMock()).putProcessAction(any(),
                eq(ProcessAction.STOP));

        setUpBridgeAndThing();

        DeviceState deviceState = createDeviceStateMock(StateType.ON, "On");
        getBridgeHandler().onDeviceStateUpdated(deviceState);
        assertThingStatusIs(getThingHandler().getThing(), ThingStatus.ONLINE, ThingStatusDetail.NONE);

        // when:
        getThingHandler().triggerProcessAction(ProcessAction.STOP);

        // then:
        assertThingStatusIs(getThingHandler().getThing(), ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testHandleCommandProgramStartToStartStopChannel() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(PROGRAM_START_STOP),
                new StringType(ProgramStatus.PROGRAM_STARTED.getState()));

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(), ProcessAction.START);
        });
    }

    @Test
    public void testHandleCommandProgramStopToStartStopChannel() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(PROGRAM_START_STOP),
                new StringType(ProgramStatus.PROGRAM_STOPPED.getState()));

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(), ProcessAction.STOP);
        });
    }

    @Test
    public void testHandleCommandProgramStartToStartStopPauseChannel() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(PROGRAM_START_STOP_PAUSE),
                new StringType(ProgramStatus.PROGRAM_STARTED.getState()));

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(), ProcessAction.START);
        });
    }

    @Test
    public void testHandleCommandProgramStopToStartStopPauseChannel() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(PROGRAM_START_STOP_PAUSE),
                new StringType(ProgramStatus.PROGRAM_STOPPED.getState()));

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(), ProcessAction.STOP);
        });
    }

    @Test
    public void testHandleCommandProgramPauseToStartStopPauseChannel() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(PROGRAM_START_STOP_PAUSE),
                new StringType(ProgramStatus.PROGRAM_PAUSED.getState()));

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putProcessAction(getThingHandler().getDeviceId(), ProcessAction.PAUSE);
        });
    }

    @Test
    public void testFailingPutLightDoesNotSetTheDeviceToOffline() throws Exception {
        // given:
        doThrow(MieleWebserviceException.class).when(getWebserviceMock()).putLight(any(), eq(true));

        setUpBridgeAndThing();

        DeviceState deviceState = createDeviceStateMock(StateType.ON, "On");
        getBridgeHandler().onDeviceStateUpdated(deviceState);
        assertThingStatusIs(getThingHandler().getThing(), ThingStatus.ONLINE, ThingStatusDetail.NONE);

        // when:
        getThingHandler().triggerLight(true);

        // then:
        assertThingStatusIs(getThingHandler().getThing(), ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testHandleCommandLightOff() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(LIGHT_SWITCH), OnOffType.OFF);

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putLight(getThingHandler().getDeviceId(), false);
        });
    }

    @Test
    public void testHandleCommandLightOn() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(LIGHT_SWITCH), OnOffType.ON);

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putLight(getThingHandler().getDeviceId(), true);
        });
    }

    @Test
    public void testHandleCommandDoesNothingWhenCommandIsNotOfOnOffType() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(LIGHT_SWITCH), new DecimalType(0));

        // then:
        verify(getWebserviceMock(), never()).putLight(anyString(), anyBoolean());
    }

    @Test
    public void testHandleCommandPowerOn() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(POWER_ON_OFF), OnOffType.ON);

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putPowerState(getThingHandler().getDeviceId(), true);
        });
    }

    @Test
    public void testHandleCommandPowerOff() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(POWER_ON_OFF), OnOffType.OFF);

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).putPowerState(getThingHandler().getDeviceId(), false);
        });
    }

    @Test
    public void testHandleCommandDoesNothingWhenPowerCommandIsNotOfOnOffType() throws Exception {
        // given:
        setUpBridgeAndThing();

        // when:
        getThingHandler().handleCommand(channel(POWER_ON_OFF), new DecimalType(0));

        // then:
        verify(getWebserviceMock(), never()).putPowerState(anyString(), anyBoolean());
    }

    @Test
    public void testMissingPropertiesAreSetWhenAStateUpdateIsReceivedFromTheCloud() throws Exception {
        // given:
        setUpBridgeAndThing();

        assertFalse(getThingHandler().getThing().getProperties().containsKey(Thing.PROPERTY_SERIAL_NUMBER));
        assertFalse(getThingHandler().getThing().getProperties().containsKey(Thing.PROPERTY_MODEL_ID));

        var deviceState = mock(DeviceState.class);
        when(deviceState.getRawType()).thenReturn(DeviceType.UNKNOWN);
        when(deviceState.getDeviceIdentifier()).thenReturn(MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER);
        when(deviceState.getFabNumber())
                .thenReturn(Optional.of(MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER));
        when(deviceState.getType()).thenReturn(Optional.of("Unknown device type"));
        when(deviceState.getTechType()).thenReturn(Optional.of("UK-4567"));

        // when:
        getThingHandler().onDeviceStateUpdated(deviceState);

        // then:
        assertEquals(MieleCloudBindingIntegrationTestConstants.SERIAL_NUMBER,
                getThingHandler().getThing().getProperties().get(Thing.PROPERTY_SERIAL_NUMBER));
        assertEquals("Unknown device type UK-4567",
                getThingHandler().getThing().getProperties().get(Thing.PROPERTY_MODEL_ID));
    }
}
