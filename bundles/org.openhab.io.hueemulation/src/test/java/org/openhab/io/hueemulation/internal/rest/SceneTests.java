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
package org.openhab.io.hueemulation.internal.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.util.RuleBuilder;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.dto.HueSceneEntry;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyItemRegistry;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyRuleRegistry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Tests for various scene API endpoints.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SceneTests {
    protected @NonNullByDefault({}) CommonSetup commonSetup;
    protected @NonNullByDefault({}) ConfigStore cs;
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;
    protected @NonNullByDefault({}) RuleRegistry ruleRegistry;

    Scenes subject = new Scenes();

    private void addItemToReg(GenericItem item, State state, String tag) {
        item.setState(state);
        item.addTag(tag);
        itemRegistry.add(item);
    }

    @BeforeEach
    public void setUp() throws IOException {
        commonSetup = new CommonSetup(false);
        this.cs = commonSetup.cs;

        itemRegistry = new DummyItemRegistry();
        ruleRegistry = new DummyRuleRegistry();

        subject.cs = commonSetup.cs;
        subject.userManagement = commonSetup.userManagement;
        subject.itemRegistry = itemRegistry;
        subject.ruleRegistry = ruleRegistry;
        subject.activate();

        // Add simulated lights
        addItemToReg(new SwitchItem("switch1"), OnOffType.ON, "Switchable");
        addItemToReg(new ColorItem("color1"), HSBType.BLUE, "ColorLighting");
        addItemToReg(new DimmerItem("white1"), new PercentType(12), "Lighting");
        addItemToReg(new RollershutterItem("roller1"), new PercentType(12), "Lighting");
        addItemToReg(new DimmerItem("white1"), new PercentType(12), "Lighting");
        addItemToReg(new GroupItem("group1"), OnOffType.ON, "Switchable");

        commonSetup.start(new ResourceConfig().registerInstances(subject));
    }

    @AfterEach
    public void tearDown() throws Exception {
        commonSetup.dispose();
    }

    @SuppressWarnings("null")
    @Test
    public void addUpdateRemoveSceneToRegistry() {
        Rule rule = RuleBuilder.create("demo1").withTags("scene") //
                .withActions(Scenes.actionFromState("switch1", (Command) OnOffType.ON)).build();

        ruleRegistry.add(rule);

        HueSceneEntry sceneEntry = cs.ds.scenes.get("demo1");
        assertThat(sceneEntry.lights.get(0), CoreMatchers.is("switch1"));

        // Update
        rule = RuleBuilder.create("demo1").withTags("scene") //
                .withActions(Scenes.actionFromState("white1", (Command) OnOffType.ON)).build();
        ruleRegistry.update(rule);

        sceneEntry = cs.ds.scenes.get("demo1");
        assertThat(sceneEntry.lights.get(0), CoreMatchers.is("white1"));

        // Remove

        ruleRegistry.remove("demo1");
        sceneEntry = cs.ds.scenes.get("demo1");
        assertThat(sceneEntry, CoreMatchers.nullValue());
    }

    @SuppressWarnings("null")
    @Test
    public void addGetRemoveSceneViaRest() throws Exception {
        // 1. Create
        String body = "{ 'name':'Cozy dinner', 'recycle':false, 'lights':['switch1','white1'], 'type':'LightScene'}";
        ContentResponse response = commonSetup.sendPost("/testuser/scenes", body);
        assertEquals(200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("success"));

        // 1.1 Check for scene entry
        Entry<String, HueSceneEntry> entry = cs.ds.scenes.entrySet().stream().findAny().get();
        assertThat(entry.getValue().name, is("Cozy dinner"));
        assertThat(entry.getValue().lights.get(0), is("switch1"));
        assertThat(entry.getValue().lights.get(1), is("white1"));

        // 1.2 Check for rule
        Rule rule = ruleRegistry.get(entry.getKey());
        assertThat(rule.getName(), is("Cozy dinner"));
        assertThat(rule.getActions().get(0).getId(), is("switch1"));
        assertThat(rule.getActions().get(1).getId(), is("white1"));

        // 2. Get
        response = commonSetup.sendGet("/testuser/scenes/" + entry.getKey());
        assertEquals(200, response.getStatus());
        HueSceneEntry fromJson = new Gson().fromJson(response.getContentAsString(), HueSceneEntry.class);
        assertThat(fromJson.name, is(entry.getValue().name));

        // 3. Remove
        response = commonSetup.sendDelete("/testuser/scenes/" + entry.getKey());
        assertEquals(200, response.getStatus());
        assertTrue(cs.ds.scenes.isEmpty());
    }

    @SuppressWarnings("null")
    @Test
    public void updateSceneViaRest() throws Exception {
        Rule rule = RuleBuilder.create("demo1").withTags("scene").withName("Some name") //
                .withActions(Scenes.actionFromState("switch1", (Command) OnOffType.ON)).build();

        ruleRegistry.add(rule);

        // 3. Modify (just the name)
        String body = "{ 'name':'A new name'}";
        ContentResponse response = commonSetup.sendPut("/testuser/scenes/demo1", body);
        assertEquals(200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("name"));

        Entry<String, HueSceneEntry> sceneEntry = cs.ds.scenes.entrySet().stream().findAny().get();
        assertThat(sceneEntry.getValue().name, is("A new name"));
        assertThat(sceneEntry.getValue().lights.get(0), is("switch1")); // nothing else should have changed

        // 3. Modify (just the lights)
        rule = RuleBuilder.create("demo1").withTags("scene").withName("Some name") //
                .withActions(Scenes.actionFromState("switch1", (Command) OnOffType.ON)).build();

        ruleRegistry.update(rule); // Reset rule

        sceneEntry = cs.ds.scenes.entrySet().stream().findAny().get();
        String uid = sceneEntry.getKey();

        // Without store lights
        body = "{ 'lights':['white1']}";
        response = commonSetup.sendPut("/testuser/scenes/demo1", body);
        assertEquals(200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("lights"));

        sceneEntry = cs.ds.scenes.entrySet().stream().findAny().get();
        assertThat(sceneEntry.getValue().name, is("Some name")); // should not have changed
        assertThat(sceneEntry.getKey(), is(uid));
        assertThat(sceneEntry.getValue().lights.get(0), is("switch1")); // storelightstate not set, lights not changed

        // With store lights
        body = "{ 'lights':['white1'], 'storelightstate':true }";
        response = commonSetup.sendPut("/testuser/scenes/demo1", body);
        assertEquals(200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("lights"));

        sceneEntry = cs.ds.scenes.entrySet().stream().findAny().get();
        assertThat(sceneEntry.getValue().lights.get(0), is("white1"));
    }

    @Test
    public void getAll() throws Exception {
        Rule rule = RuleBuilder.create("demo1").withTags("scene") //
                .withActions(Scenes.actionFromState("switch1", (Command) OnOffType.ON)).build();

        ruleRegistry.add(rule);

        ContentResponse response = commonSetup.sendGet("/testuser/scenes");
        Type type = new TypeToken<Map<String, HueSceneEntry>>() {
        }.getType();
        Map<String, HueSceneEntry> fromJson = new Gson().fromJson(response.getContentAsString(), type);
        assertTrue(fromJson.containsKey("demo1"));
    }
}
