/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.events.Event;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.DeviceType;
import org.openhab.io.hueemulation.internal.dto.HueGroupEntry;
import org.openhab.io.hueemulation.internal.dto.HueLightEntry;
import org.openhab.io.hueemulation.internal.dto.HueStateColorBulb;
import org.openhab.io.hueemulation.internal.dto.HueStatePlug;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyItemRegistry;

/**
 * Tests for {@link LightsAndGroups}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class LightsAndGroupsTests {
    protected @NonNullByDefault({}) CommonSetup commonSetup;
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;
    protected @NonNullByDefault({}) ConfigStore cs;

    LightsAndGroups subject = new LightsAndGroups();

    @BeforeEach
    public void setUp() throws IOException {
        commonSetup = new CommonSetup(false);
        itemRegistry = new DummyItemRegistry();

        this.cs = commonSetup.cs;

        subject.cs = cs;
        subject.eventPublisher = commonSetup.eventPublisher;
        subject.userManagement = commonSetup.userManagement;
        subject.itemRegistry = itemRegistry;
        subject.activate();

        // Add simulated lights
        cs.ds.lights.put("1", new HueLightEntry(new SwitchItem("switch"), "switch", DeviceType.SwitchType));
        cs.ds.lights.put("2", new HueLightEntry(new ColorItem("color"), "color", DeviceType.ColorType));
        cs.ds.lights.put("3", new HueLightEntry(new ColorItem("white"), "white", DeviceType.WhiteTemperatureType));

        // Add group item
        cs.ds.groups.put("10",
                new HueGroupEntry("name", new GroupItem("white", new SwitchItem("switch")), DeviceType.SwitchType));

        commonSetup.start(new ResourceConfig().registerInstances(subject));
    }

    @AfterEach
    public void tearDown() throws Exception {
        commonSetup.dispose();
    }

    @Test
    public void addSwitchableByCategory() {
        SwitchItem item = new SwitchItem("switch1");
        item.setCategory("Light");
        itemRegistry.add(item);
        HueLightEntry device = cs.ds.lights.get(cs.mapItemUIDtoHueID(item));
        assertNotNull(device);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
    }

    @Test
    public void addSwitchableByTag() {
        SwitchItem item = new SwitchItem("switch1");
        item.addTag("Switchable");
        itemRegistry.add(item);
        HueLightEntry device = cs.ds.lights.get(cs.mapItemUIDtoHueID(item));
        assertNotNull(device);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
    }

    @Test
    public void ignoreByTag() {
        SwitchItem item = new SwitchItem("switch1");
        item.addTags("Switchable", "internal"); // The ignore tag will win
        itemRegistry.add(item);
        HueLightEntry device = cs.ds.lights.get(cs.mapItemUIDtoHueID(item));
        assertThat(device, is(nullValue()));
    }

    @Test
    public void addGroupSwitchableByTag() {
        GroupItem item = new GroupItem("group1", new SwitchItem("switch1"));
        item.addTag("Switchable");
        itemRegistry.add(item);
        HueGroupEntry device = cs.ds.groups.get(cs.mapItemUIDtoHueID(item));
        assertNotNull(device);
        assertThat(device.groupItem, is(item));
        assertThat(device.action, is(instanceOf(HueStatePlug.class)));
    }

    @Test
    public void addDeviceAsGroupSwitchableByTag() {
        GroupItem item = new GroupItem("group1", new SwitchItem("switch1"));
        item.addTag("Switchable");
        item.addTag("Huelight");
        itemRegistry.add(item);
        HueLightEntry device = cs.ds.lights.get(cs.mapItemUIDtoHueID(item));
        assertNotNull(device);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
    }

    @Test
    public void addGroupWithoutTypeByTag() {
        GroupItem item = new GroupItem("group1", null);
        item.addTag("Switchable");

        itemRegistry.add(item);

        HueGroupEntry device = cs.ds.groups.get(cs.mapItemUIDtoHueID(item));
        assertNotNull(device);
        assertThat(device.groupItem, is(item));
        assertThat(device.action, is(instanceOf(HueStatePlug.class)));
        assertThat(device.groupItem, is(item));
    }

    @Test
    public void removeGroupWithoutTypeAndTag() {
        String groupName = "group1";
        GroupItem item = new GroupItem(groupName, null);
        item.addTag("Switchable");
        itemRegistry.add(item);

        String hueID = cs.mapItemUIDtoHueID(item);
        assertThat(cs.ds.groups.get(hueID), notNullValue());

        subject.updated(item, new GroupItem(groupName, null));

        assertThat(cs.ds.groups.get(hueID), nullValue());
    }

    @Test
    public void removeGroupNoLongerGroup() {
        String groupName = "group1";
        GroupItem item = new GroupItem(groupName, null);
        item.addTag("Switchable");
        itemRegistry.add(item);

        String hueID = cs.mapItemUIDtoHueID(item);
        assertThat(cs.ds.groups.get(hueID), notNullValue());

        subject.updated(item, new StringItem(groupName));

        assertThat(cs.ds.groups.get(hueID), nullValue());
    }

    @Test
    public void updateSwitchable() {
        SwitchItem item = new SwitchItem("switch1");
        item.setLabel("labelOld");
        item.addTag("Switchable");
        itemRegistry.add(item);
        String hueID = cs.mapItemUIDtoHueID(item);
        HueLightEntry device = cs.ds.lights.get(hueID);
        assertNotNull(device);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
        assertThat(device.name, is("labelOld"));

        SwitchItem newitem = new SwitchItem("switch1");
        newitem.setLabel("labelNew");
        newitem.addTag("Switchable");
        subject.updated(item, newitem);
        device = cs.ds.lights.get(hueID);
        assertNotNull(device);
        assertThat(device.item, is(newitem));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
        assertThat(device.name, is("labelNew"));
    }

    @Test
    public void updateSwitchableNoTags() {
        SwitchItem item = new SwitchItem("switch1");
        item.setLabel("labelOld");
        item.addTag("Switchable");
        itemRegistry.add(item);
        String hueID = cs.mapItemUIDtoHueID(item);
        HueLightEntry device = cs.ds.lights.get(hueID);
        assertNotNull(device);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
        assertThat(device.name, is("labelOld"));

        // Update with an item that has no tags anymore -> should be removed
        SwitchItem newitem = new SwitchItem("switch1");
        newitem.setLabel("labelNew");
        subject.updated(item, newitem);

        device = cs.ds.lights.get(hueID);
        assertThat(device, nullValue());
    }

    @Test
    public void updateSwitchableUnsupportedItemType() {
        SwitchItem item = new SwitchItem("switch1");
        item.setLabel("label");
        item.addTag("Switchable");
        itemRegistry.add(item);
        String hueID = cs.mapItemUIDtoHueID(item);
        HueLightEntry device = cs.ds.lights.get(hueID);
        assertNotNull(device);
        assertThat(device.item, is(item));
        assertThat(device.state, is(instanceOf(HueStatePlug.class)));
        assertThat(device.name, is("label"));

        // Update with an item where item type is not supported anymore -> should be removed
        StringItem newitem = new StringItem("switch1");
        newitem.setLabel("label");
        newitem.addTag("Switchable");
        subject.updated(item, newitem);
        device = cs.ds.lights.get(hueID);
        assertThat(device, nullValue());
    }

    @Test
    public void changeSwitchState() throws Exception {
        HueLightEntry hueLightEntry = cs.ds.lights.get("1");
        assertNotNull(hueLightEntry);
        HueStatePlug statePlug = assertInstanceOf(HueStatePlug.class, hueLightEntry.state);
        assertThat(statePlug.on, is(false));

        String body = "{'on':true}";
        ContentResponse response = commonSetup.sendPut("/testuser/lights/1/state", body);
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentAsString(), containsString("success"));
        hueLightEntry = cs.ds.lights.get("1");
        assertNotNull(hueLightEntry);
        statePlug = assertInstanceOf(HueStatePlug.class, hueLightEntry.state);
        assertThat(statePlug.on, is(true));
        verify(commonSetup.eventPublisher).post(argThat((Event t) -> {
            assertThat(t.getPayload(), is("{\"type\":\"OnOff\",\"value\":\"ON\"}"));
            return true;
        }));
    }

    @Test
    public void changeGroupItemSwitchState() throws Exception {
        HueGroupEntry hueGroupEntry = cs.ds.groups.get("10");
        assertNotNull(hueGroupEntry);
        HueStatePlug statePlug = assertInstanceOf(HueStatePlug.class, hueGroupEntry.action);
        assertThat(statePlug.on, is(false));

        String body = "{'on':true}";
        ContentResponse response = commonSetup.sendPut("/testuser/groups/10/action", body);
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentAsString(), containsString("success"));
        hueGroupEntry = cs.ds.groups.get("10");
        assertNotNull(hueGroupEntry);
        statePlug = assertInstanceOf(HueStatePlug.class, hueGroupEntry.action);
        assertThat(statePlug.on, is(true));
        verify(commonSetup.eventPublisher).post(argThat((Event t) -> {
            assertThat(t.getPayload(), is("{\"type\":\"OnOff\",\"value\":\"ON\"}"));
            return true;
        }));
    }

    @Test
    public void changeOnValue() throws Exception {
        HueLightEntry hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        HueStateColorBulb stateColorBulb = assertInstanceOf(HueStateColorBulb.class, hueLightEntry.state);
        assertThat(stateColorBulb.on, is(false));

        String body = "{'on':true}";
        ContentResponse response = commonSetup.sendPut("/testuser/lights/2/state", body);
        assertThat(response.getStatus(), is(200));
        String entity = response.getContentAsString();
        assertThat(entity, is("[{\"success\":{\"/lights/2/state/on\":true}}]"));
        hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        stateColorBulb = assertInstanceOf(HueStateColorBulb.class, hueLightEntry.state);
        assertThat(stateColorBulb.on, is(true));
    }

    @Test
    public void changeOnAndBriValues() throws Exception {
        HueLightEntry hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        HueStateColorBulb stateColorBulb = assertInstanceOf(HueStateColorBulb.class, hueLightEntry.state);
        assertThat(stateColorBulb.on, is(false));
        assertThat(stateColorBulb.bri, is(1));

        String body = "{'on':true,'bri':200}";
        ContentResponse response = commonSetup.sendPut("/testuser/lights/2/state", body);
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentAsString(), containsString("success"));
        hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        stateColorBulb = assertInstanceOf(HueStateColorBulb.class, hueLightEntry.state);
        assertThat(stateColorBulb.on, is(true));
        assertThat(stateColorBulb.bri, is(200));
    }

    @Test
    public void changeHueSatValues() throws Exception {
        HueLightEntry hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        hueLightEntry.item.setState(OnOffType.ON);
        hueLightEntry.state.as(HueStateColorBulb.class).on = true;

        String body = "{'hue':1000,'sat':50}";
        ContentResponse response = commonSetup.sendPut("/testuser/lights/2/state", body);
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentAsString(), containsString("success"));
        hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        HueStateColorBulb stateColorBulb = assertInstanceOf(HueStateColorBulb.class, hueLightEntry.state);
        assertThat(stateColorBulb.on, is(true));
        assertThat(stateColorBulb.hue, is(1000));
        assertThat(stateColorBulb.sat, is(50));

        verify(commonSetup.eventPublisher).post(argThat(ce -> assertHueValue((ItemCommandEvent) ce, 1000)));
    }

    /**
     * Amazon echos are setting ct only, if commanded to turn a light white.
     */
    @Test
    public void changeCtValue() throws Exception {
        HueLightEntry hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        hueLightEntry.item.setState(OnOffType.ON);
        hueLightEntry.state.as(HueStateColorBulb.class).on = true;

        String body = "{'ct':500}";
        ContentResponse response = commonSetup.sendPut("/testuser/lights/2/state", body);
        assertThat(response.getStatus(), is(200));
        body = response.getContentAsString();
        assertThat(body, containsString("success"));
        assertThat(body, containsString("ct"));
        hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        HueStateColorBulb stateColorBulb = assertInstanceOf(HueStateColorBulb.class, hueLightEntry.state);
        assertThat(stateColorBulb.on, is(true));
        assertThat(stateColorBulb.ct, is(500));
        assertThat(stateColorBulb.sat, is(0));

        // Saturation is expected to be 0 -> white light
        verify(commonSetup.eventPublisher).post(argThat(ce -> assertSatValue((ItemCommandEvent) ce, 0)));
    }

    @Test
    public void switchOnWithXY() throws Exception {
        HueLightEntry hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        HueStateColorBulb stateColorBulb = assertInstanceOf(HueStateColorBulb.class, hueLightEntry.state);
        assertThat(stateColorBulb.on, is(false));
        assertThat(stateColorBulb.bri, is(1));

        String body = "{'on':true,'bri':200,'xy':[0.5119,0.4147]}";
        ContentResponse response = commonSetup.sendPut("/testuser/lights/2/state", body);
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentAsString(), containsString("success"));
        assertThat(response.getContentAsString(), containsString("xy"));
        hueLightEntry = cs.ds.lights.get("2");
        assertNotNull(hueLightEntry);
        stateColorBulb = assertInstanceOf(HueStateColorBulb.class, hueLightEntry.state);
        assertThat(stateColorBulb.on, is(true));
        assertThat(stateColorBulb.bri, is(200));
        assertThat(stateColorBulb.xy[0], is(0.5119));
        assertThat(stateColorBulb.xy[1], is(0.4147));
        assertThat(stateColorBulb.colormode, is(HueStateColorBulb.ColorMode.xy));
        assertThat(stateColorBulb.toHSBType().getHue().intValue(), is((int) 27.47722590981918));
        assertThat(stateColorBulb.toHSBType().getSaturation().intValue(), is(88));
        assertThat(stateColorBulb.toHSBType().getBrightness().intValue(), is(78));
    }

    @Test
    public void allLightsAndSingleLight() throws Exception {
        ContentResponse response = commonSetup.sendGet("/testuser/lights");
        assertThat(response.getStatus(), is(200));

        String body = response.getContentAsString();

        assertThat(body, containsString("switch"));
        assertThat(body, containsString("color"));
        assertThat(body, containsString("white"));

        // Single light access test
        response = commonSetup.sendGet("/testuser/lights/2");
        assertThat(response.getStatus(), is(200));
        body = response.getContentAsString();
        assertThat(body, containsString("color"));
    }

    private boolean assertHueValue(ItemCommandEvent ce, int hueValue) {
        assertThat(((HSBType) ce.getItemCommand()).getHue().intValue(), is(hueValue * 360 / HueStateColorBulb.MAX_HUE));
        return true;
    }

    private boolean assertSatValue(ItemCommandEvent ce, int satValue) {
        assertThat(((HSBType) ce.getItemCommand()).getSaturation().intValue(),
                is(satValue * 100 / HueStateColorBulb.MAX_SAT));
        return true;
    }
}
