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
package org.openhab.binding.nest.internal.wwn.handler;

import static java.util.Map.entry;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.openhab.binding.nest.internal.wwn.rest.WWNStreamingRestClient.PUT;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.nest.internal.wwn.config.WWNAccountConfiguration;
import org.openhab.binding.nest.internal.wwn.test.WWNTestAccountHandler;
import org.openhab.binding.nest.internal.wwn.test.WWNTestApiServlet;
import org.openhab.binding.nest.internal.wwn.test.WWNTestHandlerFactory;
import org.openhab.binding.nest.internal.wwn.test.WWNTestServer;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemFactory;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.test.TestPortUtil;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ManagedItemChannelLinkProvider;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WWNThingHandlerOSGiTest} is an abstract base class for Nest OSGi based tests.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public abstract class WWNThingHandlerOSGiTest extends JavaOSGiTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = TestPortUtil.findFreePort();
    private static final int SERVER_TIMEOUT = -1;
    private static final String REDIRECT_URL = "http://" + SERVER_HOST + ":" + SERVER_PORT;

    private final Logger logger = LoggerFactory.getLogger(WWNThingHandlerOSGiTest.class);

    private static @Nullable WWNTestServer server;
    private static WWNTestApiServlet servlet = new WWNTestApiServlet();

    private @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistry;
    private @NonNullByDefault({}) ChannelGroupTypeRegistry channelGroupTypeRegistry;
    private @NonNullByDefault({}) ItemFactory itemFactory;
    private @NonNullByDefault({}) ItemRegistry itemRegistry;
    private @NonNullByDefault({}) EventPublisher eventPublisher;
    private @NonNullByDefault({}) ManagedThingProvider managedThingProvider;
    private @NonNullByDefault({}) ThingTypeRegistry thingTypeRegistry;
    private @NonNullByDefault({}) ManagedItemChannelLinkProvider managedItemChannelLinkProvider;
    private @NonNullByDefault({}) VolatileStorageService volatileStorageService = new VolatileStorageService();

    protected @NonNullByDefault({}) Bridge bridge;
    protected @NonNullByDefault({}) WWNTestAccountHandler bridgeHandler;
    protected @NonNullByDefault({}) Thing thing;
    protected @NonNullByDefault({}) WWNBaseHandler<?> thingHandler;
    private Class<? extends WWNBaseHandler<?>> thingClass;

    private @NonNullByDefault({}) WWNTestHandlerFactory nestTestHandlerFactory;
    private @NonNullByDefault({}) ClientBuilder clientBuilder;
    private @NonNullByDefault({}) SseEventSourceFactory eventSourceFactory;

    public WWNThingHandlerOSGiTest(Class<? extends WWNBaseHandler<?>> thingClass) {
        this.thingClass = thingClass;
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        ServletHolder holder = new ServletHolder(servlet);
        server = new WWNTestServer(SERVER_HOST, SERVER_PORT, SERVER_TIMEOUT, holder);
        server.startServer();
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        WWNTestServer testServer = server;
        if (testServer != null) {
            testServer.stopServer();
        }
    }

    @BeforeEach
    public void setUp() throws ItemNotFoundException {
        registerService(volatileStorageService);

        managedThingProvider = Objects.requireNonNull(getService(ThingProvider.class, ManagedThingProvider.class),
                "Could not get ManagedThingProvider");
        thingTypeRegistry = Objects.requireNonNull(getService(ThingTypeRegistry.class),
                "Could not get ThingTypeRegistry");
        channelTypeRegistry = Objects.requireNonNull(getService(ChannelTypeRegistry.class),
                "Could not get ChannelTypeRegistry");
        channelGroupTypeRegistry = Objects.requireNonNull(getService(ChannelGroupTypeRegistry.class),
                "Could not get ChannelGroupTypeRegistry");
        eventPublisher = Objects.requireNonNull(getService(EventPublisher.class), "Could not get EventPublisher");
        itemFactory = Objects.requireNonNull(getService(ItemFactory.class), "Could not get ItemFactory");
        itemRegistry = Objects.requireNonNull(getService(ItemRegistry.class), "Could not get ItemRegistry");
        managedItemChannelLinkProvider = Objects.requireNonNull(getService(ManagedItemChannelLinkProvider.class),
                "Could not get ManagedItemChannelLinkProvider");
        clientBuilder = Objects.requireNonNull(getService(ClientBuilder.class), "Could not get ClientBuilder");
        eventSourceFactory = Objects.requireNonNull(getService(SseEventSourceFactory.class),
                "Could not get SseEventSourceFactory");

        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getBundleContext()).thenReturn(bundleContext);

        nestTestHandlerFactory = new WWNTestHandlerFactory(clientBuilder, eventSourceFactory);
        nestTestHandlerFactory.activate(componentContext,
                Map.of(WWNTestHandlerFactory.REDIRECT_URL_CONFIG_PROPERTY, REDIRECT_URL));
        registerService(nestTestHandlerFactory);

        ThingTypeProvider thingTypeProvider = mock(ThingTypeProvider.class);
        when(thingTypeProvider.getThingType(ArgumentMatchers.any(ThingTypeUID.class), nullable(Locale.class)))
                .thenReturn(mock(ThingType.class));
        registerService(thingTypeProvider);

        nestTestHandlerFactory = Objects.requireNonNull(
                getService(ThingHandlerFactory.class, WWNTestHandlerFactory.class),
                "Could not get NestTestHandlerFactory");

        bridge = buildBridge();
        thing = buildThing(bridge);

        bridgeHandler = addThing(bridge, WWNTestAccountHandler.class);
        thingHandler = addThing(thing, thingClass);

        createAndLinkItems();
        assertThatAllItemStatesAreNull();
    }

    @AfterEach
    public void tearDown() {
        servlet.reset();
        servlet.closeConnections();

        if (thing != null) {
            managedThingProvider.remove(thing.getUID());
        }
        if (bridge != null) {
            managedThingProvider.remove(bridge.getUID());
        }

        unregisterService(volatileStorageService);
    }

    protected Bridge buildBridge() {
        Map<String, Object> properties = Map.ofEntries( //
                entry(WWNAccountConfiguration.ACCESS_TOKEN,
                        "c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc"),
                entry(WWNAccountConfiguration.PINCODE, "64P2XRYT"),
                entry(WWNAccountConfiguration.PRODUCT_ID, "8fdf9885-ca07-4252-1aa3-f3d5ca9589e0"),
                entry(WWNAccountConfiguration.PRODUCT_SECRET, "QITLR3iyUlWaj9dbvCxsCKp4f"));

        return BridgeBuilder.create(WWNTestAccountHandler.THING_TYPE_TEST_BRIDGE, "test_account")
                .withLabel("Test Account").withConfiguration(new Configuration(properties)).build();
    }

    protected abstract Thing buildThing(Bridge bridge);

    protected List<Channel> buildChannels(ThingTypeUID thingTypeUID, ThingUID thingUID) {
        waitForAssert(() -> assertThat(thingTypeRegistry.getThingType(thingTypeUID), notNullValue()));

        ThingType thingType = thingTypeRegistry.getThingType(thingTypeUID);

        List<Channel> channels = new ArrayList<>(buildChannels(thingUID, thingType.getChannelDefinitions(), id -> id));
        for (ChannelGroupDefinition channelGroupDefinition : thingType.getChannelGroupDefinitions()) {
            ChannelGroupType channelGroupType = channelGroupTypeRegistry
                    .getChannelGroupType(channelGroupDefinition.getTypeUID());
            String groupId = channelGroupDefinition.getId();
            if (channelGroupType != null) {
                channels.addAll(
                        buildChannels(thingUID, channelGroupType.getChannelDefinitions(), id -> groupId + "#" + id));
            }
        }

        channels.sort((Channel c1, Channel c2) -> c1.getUID().getId().compareTo(c2.getUID().getId()));
        return channels;
    }

    protected List<Channel> buildChannels(ThingUID thingUID, List<ChannelDefinition> channelDefinitions,
            Function<String, String> channelIdFunction) {
        List<Channel> result = new ArrayList<>();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            ChannelType channelType = channelTypeRegistry.getChannelType(channelDefinition.getChannelTypeUID());
            if (channelType != null) {
                result.add(ChannelBuilder
                        .create(new ChannelUID(thingUID, channelIdFunction.apply(channelDefinition.getId())),
                                channelType.getItemType())
                        .build());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected <T> T addThing(Thing thing, Class<T> thingHandlerClass) {
        assertThat(thing.getHandler(), is(nullValue()));
        managedThingProvider.add(thing);
        waitForAssert(() -> assertThat(thing.getHandler(), notNullValue()));
        assertThat(thing.getConfiguration(), is(notNullValue()));
        assertThat(thing.getHandler(), is(instanceOf(thingHandlerClass)));
        return (T) thing.getHandler();
    }

    protected String getThingId() {
        return thing.getUID().getId();
    }

    protected ThingUID getThingUID() {
        return thing.getUID();
    }

    protected void putStreamingEventData(String json) throws IOException {
        String singleLineJson = json.replaceAll("\n\r\\s+", "").replaceAll("\n\\s+", "").replaceAll("\n\r", "")
                .replaceAll("\n", "");
        servlet.queueEvent(PUT, singleLineJson);
    }

    protected void createAndLinkItems() {
        thing.getChannels().forEach(c -> {
            String itemName = getItemName(c.getUID().getId());
            Item item = itemFactory.createItem(c.getAcceptedItemType(), itemName);
            if (item != null) {
                itemRegistry.add(item);
            }
            managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, c.getUID()));
        });
    }

    protected void assertThatItemHasState(String channelId, State state) {
        waitForAssert(() -> assertThat("Wrong state for item of channel '" + channelId + "' ", getItemState(channelId),
                is(state)));
    }

    protected void assertThatItemHasNotState(String channelId, State state) {
        waitForAssert(() -> assertThat("Wrong state for item of channel '" + channelId + "' ", getItemState(channelId),
                is(not(state))));
    }

    protected void assertThatAllItemStatesAreNull() {
        thing.getChannels().forEach(c -> assertThatItemHasState(c.getUID().getId(), UnDefType.NULL));
    }

    protected void assertThatAllItemStatesAreNotNull() {
        thing.getChannels().forEach(c -> assertThatItemHasNotState(c.getUID().getId(), UnDefType.NULL));
    }

    protected ChannelUID getChannelUID(String channelId) {
        return new ChannelUID(getThingUID(), channelId);
    }

    protected String getItemName(String channelId) {
        return getThingId() + "_" + channelId.replaceAll("#", "_");
    }

    private State getItemState(String channelId) {
        String itemName = getItemName(channelId);
        try {
            return itemRegistry.getItem(itemName).getState();
        } catch (ItemNotFoundException e) {
            throw new AssertionError("Item with name '" + itemName + "' not found");
        }
    }

    protected void logItemStates() {
        thing.getChannels().forEach(c -> {
            String channelId = c.getUID().getId();
            String itemName = getItemName(channelId);
            logger.debug("{} = {}", itemName, getItemState(channelId));
        });
    }

    protected void updateAllItemStatesToNull() {
        thing.getChannels().forEach(c -> updateItemState(c.getUID().getId(), UnDefType.NULL));
    }

    protected void refreshAllChannels() {
        thing.getChannels().forEach(c -> thingHandler.handleCommand(c.getUID(), RefreshType.REFRESH));
    }

    protected void handleCommand(String channelId, Command command) {
        thingHandler.handleCommand(getChannelUID(channelId), command);
    }

    protected void updateItemState(String channelId, State state) {
        String itemName = getItemName(channelId);
        eventPublisher.post(ItemEventFactory.createStateEvent(itemName, state));
    }

    protected void assertNestApiPropertyState(String nestId, String propertyName, String state) {
        waitForAssert(() -> assertThat(servlet.getNestIdPropertyState(nestId, propertyName), is(state)));
    }

    public static DateTimeType parseDateTimeType(String text) {
        try {
            return new DateTimeType(Instant.parse(text).atZone(TimeZone.getDefault().toZoneId()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date time argument: " + text, e);
        }
    }
}
