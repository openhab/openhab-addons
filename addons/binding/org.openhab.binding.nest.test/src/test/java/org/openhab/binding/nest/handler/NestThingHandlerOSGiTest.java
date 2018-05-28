/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.openhab.binding.nest.internal.rest.NestStreamingRestClient.PUT;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.test.TestPortUtil;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.test.NestTestApiServlet;
import org.openhab.binding.nest.test.NestTestBridgeHandler;
import org.openhab.binding.nest.test.NestTestHandlerFactory;
import org.openhab.binding.nest.test.NestTestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NestThingHandlerOSGiTest} is an abstract base class for Nest OSGi based tests.
 *
 * @author Wouter Born - Increase test coverage
 */
public abstract class NestThingHandlerOSGiTest extends JavaOSGiTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = TestPortUtil.findFreePort();
    private static final int SERVER_TIMEOUT = -1;
    private static final String REDIRECT_URL = "http://" + SERVER_HOST + ":" + SERVER_PORT;

    private final Logger logger = LoggerFactory.getLogger(NestThingHandlerOSGiTest.class);

    private static NestTestServer server;
    private static NestTestApiServlet servlet = new NestTestApiServlet();

    private ChannelTypeRegistry channelTypeRegistry;
    private ItemFactory itemFactory;
    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private ManagedThingProvider managedThingProvider;
    private ThingTypeRegistry thingTypeRegistry;
    private ManagedItemChannelLinkProvider managedItemChannelLinkProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    protected Bridge bridge;
    protected NestTestBridgeHandler bridgeHandler;
    protected Thing thing;
    protected NestBaseHandler<?> thingHandler;
    private Class<? extends NestBaseHandler<?>> thingClass;

    private NestTestHandlerFactory nestTestHandlerFactory;

    public NestThingHandlerOSGiTest(Class<? extends NestBaseHandler<?>> thingClass) {
        this.thingClass = thingClass;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ServletHolder holder = new ServletHolder(servlet);
        server = new NestTestServer(SERVER_HOST, SERVER_PORT, SERVER_TIMEOUT, holder);
        server.startServer();
    }

    @Before
    public void setUp() throws ItemNotFoundException {
        registerService(volatileStorageService);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat("Could not get ManagedThingProvider", managedThingProvider, is(notNullValue()));

        thingTypeRegistry = getService(ThingTypeRegistry.class);
        assertThat("Could not get ThingTypeRegistry", thingTypeRegistry, is(notNullValue()));

        channelTypeRegistry = getService(ChannelTypeRegistry.class);
        assertThat("Could not get ChannelTypeRegistry", channelTypeRegistry, is(notNullValue()));

        eventPublisher = getService(EventPublisher.class);
        assertThat("Could not get EventPublisher", eventPublisher, is(notNullValue()));

        itemFactory = getService(ItemFactory.class);
        assertThat("Could not get ItemFactory", itemFactory, is(notNullValue()));

        itemRegistry = getService(ItemRegistry.class);
        assertThat("Could not get ItemRegistry", itemRegistry, is(notNullValue()));

        managedItemChannelLinkProvider = getService(ManagedItemChannelLinkProvider.class);
        assertThat("Could not get ManagedItemChannelLinkProvider", managedItemChannelLinkProvider, is(notNullValue()));

        nestTestHandlerFactory = getService(ThingHandlerFactory.class, NestTestHandlerFactory.class);
        assertThat("Could not get NestTestHandlerFactory", nestTestHandlerFactory, is(notNullValue()));
        nestTestHandlerFactory.setRedirectUrl(REDIRECT_URL);

        bridge = buildBridge();
        thing = buildThing(bridge);

        bridgeHandler = addThing(bridge, NestTestBridgeHandler.class);
        thingHandler = addThing(thing, thingClass);

        createAndLinkItems();
        assertThatAllItemStatesAreNull();
    }

    @After
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
        Map<String, Object> properties = new HashMap<>();
        properties.put(NestBridgeConfiguration.ACCESS_TOKEN,
                "c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc");
        properties.put(NestBridgeConfiguration.PINCODE, "64P2XRYT");
        properties.put(NestBridgeConfiguration.PRODUCT_ID, "8fdf9885-ca07-4252-1aa3-f3d5ca9589e0");
        properties.put(NestBridgeConfiguration.PRODUCT_SECRET, "QITLR3iyUlWaj9dbvCxsCKp4f");

        return BridgeBuilder.create(NestTestBridgeHandler.THING_TYPE_TEST_BRIDGE, "test_account")
                .withLabel("Test Account").withConfiguration(new Configuration(properties)).build();
    }

    protected abstract Thing buildThing(Bridge bridge);

    protected List<Channel> buildChannels(ThingTypeUID thingTypeUID, ThingUID thingUID) {
        ThingType thingType = thingTypeRegistry.getThingType(thingTypeUID);

        List<Channel> channels = new ArrayList<>();
        channels.addAll(buildChannels(thingUID, thingType.getChannelDefinitions(), (id) -> id));

        for (ChannelGroupDefinition channelGroupDefinition : thingType.getChannelGroupDefinitions()) {
            ChannelGroupType channelGroupType = channelTypeRegistry
                    .getChannelGroupType(channelGroupDefinition.getTypeUID());
            String groupId = channelGroupDefinition.getId();
            if (channelGroupType != null) {
                channels.addAll(
                        buildChannels(thingUID, channelGroupType.getChannelDefinitions(), (id) -> groupId + "#" + id));
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
        String singleLineJson = json.replaceAll("\n\\s+", "").replaceAll("\n", "");
        servlet.queueEvent(PUT, singleLineJson);
    }

    protected void createAndLinkItems() {
        thing.getChannels().forEach(c -> {
            String itemName = getItemName(c.getUID().getId());
            GenericItem item = itemFactory.createItem(c.getAcceptedItemType(), itemName);
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
