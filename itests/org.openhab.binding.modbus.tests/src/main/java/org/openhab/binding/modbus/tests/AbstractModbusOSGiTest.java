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
package org.openhab.binding.modbus.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.modbus.internal.ModbusHandlerFactory;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemProvider;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ManagedItemProvider;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.link.AbstractLink;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkProvider;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.link.ManagedItemChannelLinkProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sami Salonen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public abstract class AbstractModbusOSGiTest extends JavaOSGiTest {

    /**
     * When Mockito is used for mocking {@link ThingHandler}s it has to be able to load the {@link ChannelTypeUID}
     * class. Bnd will add the package to the generated manifest when the class is referenced here.
     */
    static void mockitoPackageImport() {
        ChannelTypeUID.class.getClass();
    }

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
            stateUpdates.computeIfAbsent(stateEvent.getItemName(), item -> new ArrayList<>())
                    .add(stateEvent.getItemState());
        }
    }

    private final Logger logger = LoggerFactory.getLogger(AbstractModbusOSGiTest.class);

    protected @Mock @NonNullByDefault({}) ModbusManager mockedModbusManager;
    protected @Mock @NonNullByDefault({}) UnitProvider mockedUnitProvider;
    protected @NonNullByDefault({}) ModbusManager realModbusManager;
    protected @NonNullByDefault({}) ManagedThingProvider thingProvider;
    protected @NonNullByDefault({}) ManagedItemProvider itemProvider;
    protected @NonNullByDefault({}) ManagedItemChannelLinkProvider itemChannelLinkProvider;
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;
    protected @NonNullByDefault({}) ItemChannelLinkRegistry itemChannelLinkRegistry;
    protected @NonNullByDefault({}) CoreItemFactory coreItemFactory;

    private Set<Item> addedItems = new HashSet<>();
    private Set<Thing> addedThings = new HashSet<>();
    private Set<ItemChannelLink> addedLinks = new HashSet<>();
    private StateSubscriber stateSubscriber = new StateSubscriber();

    protected @Mock @NonNullByDefault({}) ModbusCommunicationInterface comms;

    /**
     * Before each test, configure mocked services
     */
    @BeforeEach
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
        itemChannelLinkRegistry = getService(ItemChannelLinkRegistry.class);
        assertThat("Could not get ItemChannelLinkRegistry", itemChannelLinkRegistry, is(notNullValue()));

        coreItemFactory = new CoreItemFactory(mockedUnitProvider);

        // Clean slate for all tests
        reset(mockedModbusManager);

        stateSubscriber.stateUpdates.clear();
        logger.debug("setUpAbstractModbusOSGiTest END");
    }

    @AfterEach
    public void tearDownAbstractModbusOSGiTest() throws Exception {
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
        waitForAssert(() -> assertThat(itemChannelLinkRegistry.get(AbstractLink.getIDFor(itemName, channelUID)),
                is(notNullValue())));
        addedLinks.add(link);
    }

    protected @Nullable List<State> getStateUpdates(String itemName) {
        return stateSubscriber.stateUpdates.get(itemName);
    }

    protected void mockTransformation(String name, TransformationService service) {
        Dictionary<String, Object> params = new Hashtable<>();
        params.put("openhab.transform", name);
        registerService(service, params);
    }

    protected void mockCommsToModbusManager() {
        assert comms != null;
        doReturn(comms).when(mockedModbusManager).newModbusCommunicationInterface(any(), any());
    }

    protected void swapModbusManagerToMocked() {
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

    protected void swapModbusManagerToReal() {
        assertNotNull(realModbusManager);
        ModbusHandlerFactory modbusHandlerFactory = getService(ThingHandlerFactory.class, ModbusHandlerFactory.class);
        assertThat("Could not get ModbusHandlerFactory", modbusHandlerFactory, is(notNullValue()));
        assertNotNull(modbusHandlerFactory);
        modbusHandlerFactory.unsetModbusManager(mockedModbusManager);
        modbusHandlerFactory.setModbusManager(realModbusManager);
    }
}
