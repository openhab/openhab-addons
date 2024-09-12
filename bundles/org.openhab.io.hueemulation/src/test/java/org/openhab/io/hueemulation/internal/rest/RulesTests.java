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
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.util.RuleBuilder;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.RuleUtils;
import org.openhab.io.hueemulation.internal.dto.HueRuleEntry;
import org.openhab.io.hueemulation.internal.dto.HueRuleEntry.Operator;
import org.openhab.io.hueemulation.internal.dto.HueSceneEntry;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueCommand;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyItemRegistry;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyRuleRegistry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Tests for various rules API endpoints.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RulesTests {

    protected @NonNullByDefault({}) CommonSetup commonSetup;
    protected @NonNullByDefault({}) ConfigStore cs;
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;
    protected @NonNullByDefault({}) RuleRegistry ruleRegistry;

    Rules subject = new Rules();
    LightsAndGroups lightsAndGroups = new LightsAndGroups();

    private void addItemToReg(GenericItem item, State state, String tag, String label) {
        item.setState(state);
        item.setLabel(label);
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
        subject.ruleRegistry = ruleRegistry;
        subject.itemRegistry = itemRegistry;
        subject.activate();

        // We need the LightsAndGroups class to convert registry entries into HueDatastore
        // light entries
        lightsAndGroups.cs = cs;
        lightsAndGroups.eventPublisher = commonSetup.eventPublisher;
        lightsAndGroups.userManagement = commonSetup.userManagement;
        lightsAndGroups.itemRegistry = itemRegistry;
        lightsAndGroups.activate();

        addItemToReg(new SwitchItem("switch1"), OnOffType.ON, "Switchable", "name1");
        addItemToReg(new SwitchItem("switch2"), OnOffType.ON, "Switchable", "name2");
        addItemToReg(new ColorItem("color1"), HSBType.BLUE, "ColorLighting", "");

        commonSetup.start(new ResourceConfig().registerInstances(subject));
    }

    @AfterEach
    public void tearDown() throws Exception {
        RuleUtils.random = new Random();
        commonSetup.dispose();
    }

    @Test
    public void addUpdateRemoveScheduleToRegistry() {
        assertThat(cs.ds.lights.get("switch1"), is(notNullValue()));

        HueCommand command = new HueCommand("/api/testuser/lights/switch1/state", "PUT", "{'on':true}");
        HueRuleEntry.Condition condition = new HueRuleEntry.Condition("/lights/switch1/state/on", Operator.dx, null);

        Entry<Trigger, Condition> triggerCond = Rules.hueConditionToAutomation(command.address.replace("/", "-"),
                condition, itemRegistry);

        Rule rule = RuleBuilder.create("demo1").withName("test name").withTags(Rules.RULES_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(triggerCond.getKey()).withConditions(triggerCond.getValue()).build();

        ruleRegistry.add(rule);

        // Check hue entry
        HueRuleEntry entry = cs.ds.rules.get("demo1");
        assertThat(entry.conditions.get(0).address, is("/lights/switch1/state/on"));
        assertThat(entry.conditions.get(0).operator, is(Operator.dx));
        assertThat(entry.actions.get(0).address, is("/lights/switch1/state"));
        assertThat(entry.actions.get(0).method, is("PUT"));
        assertThat(entry.actions.get(0).body, is("{'on':true}"));

        // Update
        command = new HueCommand("/api/testuser/lights/switch2/state", "PUT", "{'on':false}");
        rule = RuleBuilder.create("demo1").withName("name2").withTags(Rules.RULES_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(triggerCond.getKey()).withConditions(triggerCond.getValue()).build();
        ruleRegistry.update(rule);

        entry = cs.ds.rules.get("demo1");
        assertThat(entry.actions.get(0).address, is("/lights/switch2/state"));
        assertThat(entry.actions.get(0).method, is("PUT"));
        assertThat(entry.actions.get(0).body, is("{'on':false}"));
        assertThat(entry.name, is("name2"));

        // Remove

        ruleRegistry.remove("demo1");
        entry = cs.ds.rules.get("demo1");
        assertThat(entry, nullValue());
    }

    @SuppressWarnings("null")
    @Test
    public void addGetRemoveRuleViaRest() throws Exception {
        // 1. Create
        String body = "{\"name\":\"test name\",\"description\":\"\",\"owner\":\"\",\"conditions\":[{\"address\":\"/lights/switch1/state/on\",\"operator\":\"dx\"}],\"actions\":[{\"address\":\"/lights/switch1/state\",\"method\":\"PUT\",\"body\":\"{\\u0027on\\u0027:true}\"}]}";
        ContentResponse response = commonSetup.sendPost("/testuser/rules", body);
        assertEquals(200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("success"));

        // 1.1 Check for entry
        Entry<String, HueRuleEntry> idAndEntry = cs.ds.rules.entrySet().stream().findAny().get();
        HueRuleEntry entry = idAndEntry.getValue();
        assertThat(entry.name, is("test name"));
        assertThat(entry.actions.get(0).address, is("/lights/switch1/state"));
        assertThat(entry.conditions.get(0).address, is("/lights/switch1/state/on"));

        // 1.2 Check for rule
        Rule rule = ruleRegistry.get(idAndEntry.getKey());
        assertThat(rule.getName(), is("test name"));
        assertThat(rule.getActions().get(0).getId(), is("-api-testuser-lights-switch1-state"));
        assertThat(rule.getActions().get(0).getTypeUID(), is("rules.HttpAction"));

        // 2. Get
        response = commonSetup.sendGet("/testuser/rules/" + idAndEntry.getKey());
        assertEquals(200, response.getStatus());
        HueSceneEntry fromJson = new Gson().fromJson(response.getContentAsString(), HueSceneEntry.class);
        assertThat(fromJson.name, is(idAndEntry.getValue().name));

        // 3. Remove
        response = commonSetup.sendDelete("/testuser/rules/" + idAndEntry.getKey());
        assertEquals(200, response.getStatus());
        assertTrue(cs.ds.rules.isEmpty());
    }

    @Test
    public void updateRuleViaRest() throws Exception {
        HueCommand command = new HueCommand("/api/testuser/lights/switch1/state", "PUT", "{'on':true}");
        HueRuleEntry.Condition condition = new HueRuleEntry.Condition("/lights/switch1/state/on", Operator.dx, null);

        Entry<Trigger, Condition> triggerCond = Rules.hueConditionToAutomation(command.address.replace("/", "-"),
                condition, itemRegistry);

        Rule rule = RuleBuilder.create("demo1").withName("test name").withTags(Rules.RULES_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(triggerCond.getKey()).withConditions(triggerCond.getValue()).build();

        ruleRegistry.add(rule);

        // Modify (just the name)
        String body = "{ 'name':'A new name'}";
        ContentResponse response = commonSetup.sendPut("/testuser/rules/demo1", body);
        assertEquals(200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("name"));

        Entry<String, HueRuleEntry> idAndEntry = cs.ds.rules.entrySet().stream().findAny().get();
        HueRuleEntry entry = idAndEntry.getValue();
        assertThat(entry.name, is("A new name"));
        assertThat(entry.actions.get(0).address, is("/lights/switch1/state"));
        assertThat(entry.conditions.get(0).address, is("/lights/switch1/state/on"));

        // Reset
        rule = RuleBuilder.create("demo1").withName("test name").withTags(Rules.RULES_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(triggerCond.getKey()).withConditions(triggerCond.getValue()).build();

        ruleRegistry.update(rule); // Reset rule

        idAndEntry = cs.ds.rules.entrySet().stream().findAny().get();

        // Modify (Change condition)
        body = "{\"conditions\":[{\"address\":\"/lights/switch1/state/on\",\"operator\":\"ddx\"}]}";
        response = commonSetup.sendPut("/testuser/rules/demo1", body);
        assertEquals(200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("conditions"));

        idAndEntry = cs.ds.rules.entrySet().stream().findAny().get();
        entry = idAndEntry.getValue();
        assertThat(entry.name, is("test name")); // should not have changed
        assertThat(entry.conditions.get(0).operator, is(Operator.ddx));

        // Modify (Change action)
        body = "{\"actions\":[{\"address\":\"/lights/switch2/state\",\"method\":\"PUT\",\"body\":\"{\\u0027on\\u0027:false}\"}]}";
        response = commonSetup.sendPut("/testuser/rules/demo1", body);
        assertEquals(200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("actions"));

        idAndEntry = cs.ds.rules.entrySet().stream().findAny().get();
        entry = idAndEntry.getValue();
        assertThat(entry.name, is("test name")); // should not have changed
        assertThat(entry.actions.get(0).address, is("/lights/switch2/state"));
    }

    @Test
    public void getAll() throws Exception {
        HueCommand command = new HueCommand("/api/testuser/lights/switch1/state", "PUT", "{'on':true}");
        HueRuleEntry.Condition condition = new HueRuleEntry.Condition("/lights/switch1/state/on", Operator.dx, null);

        Entry<Trigger, Condition> triggerCond = Rules.hueConditionToAutomation(command.address.replace("/", "-"),
                condition, itemRegistry);

        Rule rule = RuleBuilder.create("demo1").withName("test name").withTags(Rules.RULES_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(triggerCond.getKey()).withConditions(triggerCond.getValue()).build();

        ruleRegistry.add(rule);

        ContentResponse response = commonSetup.sendGet("/testuser/rules");
        Type type = new TypeToken<Map<String, HueRuleEntry>>() {
        }.getType();
        String body = response.getContentAsString();
        Map<String, HueRuleEntry> fromJson = new Gson().fromJson(body, type);
        HueRuleEntry entry = fromJson.get("demo1");
        assertThat(entry.name, is("test name"));
        assertThat(entry.actions.get(0).address, is("/lights/switch1/state"));
        assertThat(entry.conditions.get(0).address, is("/lights/switch1/state/on"));
    }
}
