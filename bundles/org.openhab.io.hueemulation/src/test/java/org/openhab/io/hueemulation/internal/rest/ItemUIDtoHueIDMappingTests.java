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
package org.openhab.io.hueemulation.internal.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.glassfish.jersey.server.ResourceConfig;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.library.items.SwitchItem;
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

    @BeforeEach
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

    @AfterEach
    public void tearDown() throws Exception {
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

    @Test
    public void uniqueIdForLargeHueID() {
        ConfigStore cs = commonSetup.cs;
        assertThat(cs.getHighestAssignedHueID(), CoreMatchers.is(1));

        SwitchItem item = new SwitchItem("switch1");
        item.setCategory("Light");
        commonSetup.metadataRegistry.add(new Metadata(new MetadataKey(ConfigStore.METAKEY, "switch1"), "255", null));
        itemRegistry.add(item);

        String hueID = cs.mapItemUIDtoHueID(item);
        assertThat(hueID, CoreMatchers.is("255"));

        HueLightEntry device = cs.ds.lights.get(hueID);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
        assertThat(device.uniqueid, CoreMatchers.is("A6:68:DC:9B:71:72:00:00-FF"));

        item = new SwitchItem("switch2");
        item.setCategory("Light");
        commonSetup.metadataRegistry.add(new Metadata(new MetadataKey(ConfigStore.METAKEY, "switch2"), "256000", null));
        itemRegistry.add(item);

        hueID = cs.mapItemUIDtoHueID(item);
        assertThat(hueID, CoreMatchers.is("256000"));

        device = cs.ds.lights.get(hueID);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
        assertThat(device.uniqueid, CoreMatchers.is("A6:68:DC:9B:71:72:03:E8-00"));
    }
}
