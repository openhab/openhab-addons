/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.glassfish.jersey.server.ResourceConfig;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.dto.HueLightEntry;
import org.openhab.io.hueemulation.internal.dto.HueStatePlug;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyItemRegistry;

/**
 * Tests for the metadata provided hue ID mapping
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ItemUIDtoHueIDMappingTests {
    protected @NonNullByDefault({}) CommonSetup commonSetup;
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;

    LightsAndGroups lightsAndGroups = new LightsAndGroups();

    @Before
    public void setUp() throws IOException {
        commonSetup = new CommonSetup(true);
        commonSetup.start(new ResourceConfig());

        itemRegistry = new DummyItemRegistry();

        lightsAndGroups.cs = commonSetup.cs;
        lightsAndGroups.eventPublisher = commonSetup.eventPublisher;
        lightsAndGroups.userManagement = commonSetup.userManagement;
        lightsAndGroups.itemRegistry = itemRegistry;
        lightsAndGroups.activate();
    }

    @After
    public void tearDown() {
        commonSetup.dispose();
    }

    @Test
    public void determineHighestHueID() {
        ConfigStore cs = new ConfigStore(commonSetup.networkAddressService, commonSetup.configAdmin,
                commonSetup.metadataRegistry, mock(ScheduledExecutorService.class));

        // Pretend there is a metadata entry for the imaginary item "demo1" with hueid 10
        commonSetup.metadataRegistry.add(new Metadata(new MetadataKey(ConfigStore.METAKEY, "demo1"), "10", null));
        cs.activate(Collections.singletonMap("uuid", "demouuid"));

        assertThat(cs.getHighestAssignedHueID(), CoreMatchers.is(10));
    }

    @Test
    public void mapItemWithoutHueID() {
        ConfigStore cs = commonSetup.cs;
        assertThat(cs.getHighestAssignedHueID(), CoreMatchers.is(1));

        SwitchItem item = new SwitchItem("switch1");
        item.setCategory("Light");
        itemRegistry.add(item);

        String hueID = cs.mapItemUIDtoHueID(item);
        assertThat(hueID, CoreMatchers.is("2"));

        HueLightEntry device = cs.ds.lights.get(hueID);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));

        assertThat(cs.getHighestAssignedHueID(), CoreMatchers.is(2));
    }

    @Test
    public void mapItemWithHueID() {
        ConfigStore cs = commonSetup.cs;
        assertThat(cs.getHighestAssignedHueID(), CoreMatchers.is(1));

        SwitchItem item = new SwitchItem("switch1");
        item.setCategory("Light");
        commonSetup.metadataRegistry.add(new Metadata(new MetadataKey(ConfigStore.METAKEY, "switch1"), "10", null));
        itemRegistry.add(item);

        String hueID = cs.mapItemUIDtoHueID(item);
        assertThat(hueID, CoreMatchers.is("10"));

        HueLightEntry device = cs.ds.lights.get(hueID);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));

        assertThat(cs.getHighestAssignedHueID(), CoreMatchers.is(1));
    }
}
