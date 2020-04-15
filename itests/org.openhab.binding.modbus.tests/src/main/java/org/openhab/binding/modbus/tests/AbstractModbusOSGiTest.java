/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.modbus.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.modbus.internal.ModbusHandlerFactory;
import org.openhab.io.transport.modbus.ModbusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractModbusOSGiTest extends JavaOSGiTest {

    private static class StateSubscriber implements EventSubscriber {

        private final Logger logger = LoggerFactory.getLogger(StateSubscriber.class);

        public Map<String, List<State>> stateUpdates = new HashMap<>();

        @Override
        public Set<@NonNull String> getSubscribedEventTypes() {
            return Collections.singleton(ItemStateEvent.TYPE);
        }

        @Override
        public @Nullable EventFilter getEventFilter() {
            return null;
        }

        @Override
        public void receive(Event event) {
            // Expecting only state updates in the tests
            assertThat(event, is(instanceOf(ItemStateEvent.class)));
            ItemStateEvent stateEvent = (ItemStateEvent) event;
            logger.trace("Captured event: {} of type {}. Payload: {}", event,
                    stateEvent.getItemState().getClass().getSimpleName(), event.getPayload());
            stateUpdates.computeIfAbsent(stateEvent.getItemName(), (item) -> new ArrayList<>())
                    .add(stateEvent.getItemState());
        }

    }

    private final Logger logger = LoggerFactory.getLogger(AbstractModbusOSGiTest.class);

    @Mock
    @NonNullByDefault({})
    protected ModbusManager mockedModbusManager;

    @NonNullByDefault({})
    protected ManagedThingProvider thingProvider;
    @NonNullByDefault({})
    protected ManagedItemProvider itemProvider;
    @NonNullByDefault({})
    protected ManagedItemChannelLinkProvider itemChannelLinkProvider;
    @NonNullByDefault({})
    protected ItemRegistry itemRegistry;
    @NonNullByDefault({})
    protected CoreItemFactory coreItemFactory;

    @NonNullByDefault({})
    private ModbusManager realModbusManager;
    private Set<Item> addedItems = new HashSet<>();
    private Set<Thing> addedThings = new HashSet<>();
    private Set<ItemChannelLink> addedLinks = new HashSet<>();
    private StateSubscriber stateSubscriber = new StateSubscriber();

    public AbstractModbusOSGiTest() {
        super();
    }

    /**
     * Before each test, configure mocked services
     */
    @Before
    public void setUpAbstractModbusOSGiTest() {
        logger.debug("setUpAbstractModbusOSGiTest BEGIN");
        registerVolatileStorageService();
        registerService(mockedModbusManager);
        registerService(stateSubscriber);

        swapModbusManagerToMocked();

        thingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat("Could not get ManagedThingProvider", thingProvider, is(notNullValue()));
        itemProvider = getService(ItemProvider.class, ManagedItemProvider.class);
        assertThat("Could not get ManagedItemProvider", itemProvider, is(notNullValue()));
        itemChannelLinkProvider = getService(ItemChannelLinkProvider.class, ManagedItemChannelLinkProvider.class);
        assertThat("Could not get ManagedItemChannelLinkProvider", itemChannelLinkProvider, is(notNullValue()));
        itemRegistry = getService(ItemRegistry.class);
        assertThat("Could not get ItemRegistry", itemRegistry, is(notNullValue()));

        coreItemFactory = new CoreItemFactory();

        // Clean slate for all tests
        reset(mockedModbusManager);
        stateSubscriber.stateUpdates.clear();
        logger.debug("setUpAbstractModbusOSGiTest END");
    }

    @After
    public void tearDownAbstractModbusOSGiTest() {
        logger.debug("tearDownAbstractModbusOSGiTest BEGIN");
        swapModbusManagerToReal();
        for (Item item : addedItems) {
            assertNotNull(itemProvider.remove(item.getName()));
        }
        for (Thing thing : addedThings) {
            disposeThing(thing);
        }
        for (ItemChannelLink link : addedLinks) {
            logger.debug("Unlinking {} <-> {}", link.getItemName(), link.getLinkedUID());
            assertNotNull(itemChannelLinkProvider.remove(link.getUID()));
        }
        logger.debug("tearDownAbstractModbusOSGiTest END");
    }

    protected void addThing(Thing thing) {
        assertThat(addedThings.contains(thing), not(equalTo(true)));
        ThingHandler mockHandler = thing.getHandler();
        if (mockHandler != null) {
            // If there is a handler attached to fresh thing, it should be mocked (this pattern is used with some tests)
            assertThat(Mockito.mockingDetails(thing.getHandler()).isMock(), is(equalTo(true)));
        }

        thingProvider.add(thing);
        waitForAssert(() -> assertThat(thing.getHandler(), notNullValue()));
        assertThat(thing.getConfiguration(), is(notNullValue()));
        addedThings.add(thing);
        if (mockHandler != null) {
            // Re-attach mock handler
            ThingHandler realHandlerInitedByCore = thing.getHandler();
            assertNotNull(realHandlerInitedByCore);
            assertNotSame(realHandlerInitedByCore, mockHandler);
            realHandlerInitedByCore.dispose();
            thing.setHandler(mockHandler);
        }
    }

    protected void disposeThing(Thing thing) {
        thingProvider.remove(thing.getUID());
    }

    protected void addItem(Item item) {
        assertThat(addedItems.contains(item), not(equalTo(true)));
        itemProvider.add(item);
        addedItems.add(item);
    }

    protected void linkItem(String itemName, ChannelUID channelUID) {
        logger.debug("Linking {} <-> {}", itemName, channelUID);
        ItemChannelLink link = new ItemChannelLink(itemName, channelUID);
        assertThat(addedLinks.contains(link), not(equalTo(true)));
        itemChannelLinkProvider.add(link);
        addedLinks.add(link);
    }

    protected List<State> getStateUpdates(String itemName) {
        return stateSubscriber.stateUpdates.get(itemName);
    }

    protected void mockTransformation(String name, TransformationService service) {
        Dictionary<String, Object> params = new Hashtable<>();
        params.put("smarthome.transform", name);
        registerService(service, params);
    }

    private void swapModbusManagerToMocked() {
        assertNull(realModbusManager);
        realModbusManager = getService(ModbusManager.class);
        assertThat("Could not get ModbusManager", realModbusManager, is(notNullValue()));
        assertThat("Could not get ModbusManagerImpl", realModbusManager.getClass().getSimpleName(),
                is(equalTo("ModbusManagerImpl")));
        assertNotNull(realModbusManager);

        ModbusHandlerFactory modbusHandlerFactory = getService(ThingHandlerFactory.class, ModbusHandlerFactory.class);
        assertThat("Could not get ModbusHandlerFactory", modbusHandlerFactory, is(notNullValue()));
        assertNotNull(modbusHandlerFactory);
        modbusHandlerFactory.unsetModbusManager(realModbusManager);
        modbusHandlerFactory.setModbusManager(mockedModbusManager);
    }

    private void swapModbusManagerToReal() {
        assertNotNull(realModbusManager);
        ModbusHandlerFactory modbusHandlerFactory = getService(ThingHandlerFactory.class, ModbusHandlerFactory.class);
        assertThat("Could not get ModbusHandlerFactory", modbusHandlerFactory, is(notNullValue()));
        assertNotNull(modbusHandlerFactory);
        modbusHandlerFactory.unsetModbusManager(mockedModbusManager);
        modbusHandlerFactory.setModbusManager(realModbusManager);
    }

}
