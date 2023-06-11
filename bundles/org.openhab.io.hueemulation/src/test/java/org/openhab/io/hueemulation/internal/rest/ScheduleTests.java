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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.util.RuleBuilder;
import org.openhab.core.automation.util.TriggerBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.DeviceType;
import org.openhab.io.hueemulation.internal.RuleUtils;
import org.openhab.io.hueemulation.internal.dto.HueLightEntry;
import org.openhab.io.hueemulation.internal.dto.HueSceneEntry;
import org.openhab.io.hueemulation.internal.dto.HueScheduleEntry;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueCommand;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyRuleRegistry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Tests for various schedule API endpoints.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ScheduleTests {
    protected @NonNullByDefault({}) CommonSetup commonSetup;
    protected @NonNullByDefault({}) ConfigStore cs;
    protected @NonNullByDefault({}) RuleRegistry ruleRegistry;

    Schedules subject = new Schedules();

    @BeforeEach
    public void setUp() throws IOException {
        commonSetup = new CommonSetup(false);
        this.cs = commonSetup.cs;

        ruleRegistry = new DummyRuleRegistry();

        subject.cs = commonSetup.cs;
        subject.userManagement = commonSetup.userManagement;
        subject.ruleManager = mock(RuleManager.class);
        when(subject.ruleManager.isEnabled(anyString())).thenReturn(true);
        subject.ruleRegistry = ruleRegistry;
        subject.activate();

        // Add simulated lights
        cs.ds.lights.put("1", new HueLightEntry(new SwitchItem("switch"), "switch", DeviceType.SwitchType));
        cs.ds.lights.put("2", new HueLightEntry(new ColorItem("color"), "color", DeviceType.ColorType));
        cs.ds.lights.put("3", new HueLightEntry(new ColorItem("white"), "white", DeviceType.WhiteTemperatureType));

        commonSetup.start(new ResourceConfig().registerInstances(subject));

        // Mock random -> always return int=10 or the highest possible int if bounded
        Random random = mock(Random.class, withSettings().withoutAnnotations());
        doReturn(10).when(random).nextInt();
        doAnswer(a -> {
            Integer bound = a.getArgument(0);
            return bound - 1;
        }).when(random).nextInt(anyInt());
        RuleUtils.random = random;
    }

    @AfterEach
    public void tearDown() throws Exception {
        RuleUtils.random = new Random();
        commonSetup.dispose();
    }

    @SuppressWarnings("null")
    @Test
    public void addUpdateRemoveScheduleToRegistry() {
        HueCommand command = new HueCommand("/api/testuser/lights/1/state", "PUT", "{'on':true}");
        String localtime = "2020-02-01T12:12:00";

        Rule rule = RuleBuilder.create("demo1").withName("test name").withTags(Schedules.SCHEDULE_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(RuleUtils.createTriggerForTimeString(localtime)).build();

        ruleRegistry.add(rule);

        // Check hue entry
        HueScheduleEntry sceneEntry = cs.ds.schedules.get("demo1");
        assertThat(sceneEntry.command.address, is("/api/testuser/lights/1/state"));
        assertThat(sceneEntry.command.method, is("PUT"));
        assertThat(sceneEntry.command.body, is("{'on':true}"));
        assertThat(sceneEntry.localtime, is(localtime));

        // Update
        localtime = "2021-03-01T17:12:00";
        rule = RuleBuilder.create("demo1").withName("test name").withTags(Schedules.SCHEDULE_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(RuleUtils.createTriggerForTimeString(localtime)).build();
        ruleRegistry.update(rule);

        sceneEntry = cs.ds.schedules.get("demo1");
        assertThat(sceneEntry.command.address, is("/api/testuser/lights/1/state"));
        assertThat(sceneEntry.localtime, is(localtime));

        // Remove

        ruleRegistry.remove("demo1");
        sceneEntry = cs.ds.schedules.get("demo1");
        assertThat(sceneEntry, nullValue());
    }

    @SuppressWarnings("null")
    @Test
    public void addGetRemoveScheduleViaRest() {
        // 1. Create
        String body = "{ 'name':'Wake up', 'description':'My wake up alarm', 'localtime':'2015-06-30T14:24:40'," + //
                "'command':{'address':'/api/testuser/lights/1/state','method':'PUT','body':'{\"on\":true}'} }";
        Response response = commonSetup.client.target(commonSetup.basePath + "/testuser/schedules").request()
                .post(Entity.json(body));
        assertEquals(200, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("success"));

        // 1.1 Check for entry
        Entry<String, HueScheduleEntry> entry = cs.ds.schedules.entrySet().stream().findAny().get();
        assertThat(entry.getValue().name, is("Wake up"));
        assertThat(entry.getValue().command.address, is("/api/testuser/lights/1/state"));
        assertThat(entry.getValue().command.method, is("PUT"));
        assertThat(entry.getValue().command.body, is("{\"on\":true}"));
        assertThat(entry.getValue().localtime, is("2015-06-30T14:24:40"));

        // 1.2 Check for rule
        Rule rule = ruleRegistry.get(entry.getKey());
        assertThat(rule.getName(), is("Wake up"));
        assertThat(rule.getActions().get(0).getId(), is("command"));
        assertThat(rule.getActions().get(0).getTypeUID(), is("rules.HttpAction"));

        // 2. Get
        response = commonSetup.client.target(commonSetup.basePath + "/testuser/schedules/" + entry.getKey()).request()
                .get();
        assertEquals(200, response.getStatus());
        HueSceneEntry fromJson = new Gson().fromJson(response.readEntity(String.class), HueSceneEntry.class);
        assertThat(fromJson.name, is(entry.getValue().name));

        // 3. Remove
        response = commonSetup.client.target(commonSetup.basePath + "/testuser/schedules/" + entry.getKey()).request()
                .delete();
        assertEquals(200, response.getStatus());
        assertTrue(cs.ds.schedules.isEmpty());
    }

    @SuppressWarnings("null")
    @Test
    public void updateScheduleViaRest() {
        HueCommand command = new HueCommand("/api/testuser/lights/1/state", "PUT", "{'on':true}");
        String localtime = "2020-02-01T12:12:00";

        Rule rule = RuleBuilder.create("demo1").withName("test name").withTags(Schedules.SCHEDULE_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(RuleUtils.createTriggerForTimeString(localtime)).build();

        ruleRegistry.add(rule);

        // Modify (just the name)
        String body = "{ 'name':'A new name'}";
        Response response = commonSetup.client.target(commonSetup.basePath + "/testuser/schedules/demo1").request()
                .put(Entity.json(body));
        assertEquals(200, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("name"));

        Entry<String, HueScheduleEntry> entry = cs.ds.schedules.entrySet().stream().findAny().get();
        assertThat(entry.getValue().name, is("A new name"));
        assertThat(entry.getValue().command.address, is("/api/testuser/lights/1/state")); // nothing else should have
                                                                                          // changed
        assertThat(entry.getValue().localtime, is(localtime));

        // Reset
        rule = RuleBuilder.create("demo1").withName("test name").withTags(Schedules.SCHEDULE_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(RuleUtils.createTriggerForTimeString(localtime)).build();

        ruleRegistry.update(rule); // Reset rule

        entry = cs.ds.schedules.entrySet().stream().findAny().get();
        String uid = entry.getKey();

        // Modify (Change time)
        body = "{ 'localtime':'2015-06-30T14:24:40'}";
        response = commonSetup.client.target(commonSetup.basePath + "/testuser/schedules/demo1").request()
                .put(Entity.json(body));
        assertEquals(200, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("localtime"));

        entry = cs.ds.schedules.entrySet().stream().findAny().get();
        assertThat(entry.getValue().name, is("test name")); // should not have changed
        assertThat(entry.getKey(), is(uid));
        assertThat(entry.getValue().localtime, is("2015-06-30T14:24:40"));

        // Modify (Change command)
        body = "{ 'command':{'address':'/api/testuser/lights/2/state','method':'PUT','body':'{\"on\":true}'} }";
        response = commonSetup.client.target(commonSetup.basePath + "/testuser/schedules/demo1").request()
                .put(Entity.json(body));
        assertEquals(200, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("command"));

        entry = cs.ds.schedules.entrySet().stream().findAny().get();
        assertThat(entry.getValue().name, is("test name")); // should not have changed
        assertThat(entry.getKey(), is(uid));
        assertThat(entry.getValue().command.address, is("/api/testuser/lights/2/state"));
    }

    @Test
    public void getAll() {
        HueCommand command = new HueCommand("/api/testuser/lights/1/state", "POST", "{'on':true}");
        String localtime = "2020-02-01T12:12:00";

        Rule rule = RuleBuilder.create("demo1").withName("test name").withTags(Schedules.SCHEDULE_TAG) //
                .withActions(RuleUtils.createHttpAction(command, "command")) //
                .withTriggers(RuleUtils.createTriggerForTimeString(localtime)).build();

        ruleRegistry.add(rule);

        Response response = commonSetup.client.target(commonSetup.basePath + "/testuser/schedules").request().get();
        Type type = new TypeToken<Map<String, HueSceneEntry>>() {
        }.getType();
        Map<String, HueSceneEntry> fromJson = new Gson().fromJson(response.readEntity(String.class), type);
        assertTrue(fromJson.containsKey("demo1"));
    }

    @Test
    public void timeStringToTrigger() {
        String timeString;
        Trigger trigger;
        Configuration configuration;

        // absolute time
        timeString = "2020-02-01T12:12:00";
        trigger = RuleUtils.createTriggerForTimeString(timeString);
        configuration = trigger.getConfiguration();

        assertThat(trigger.getTypeUID(), is("timer.AbsoluteDateTimeTrigger"));
        assertThat(configuration.get("date"), is("2020-02-01"));
        assertThat(configuration.get("time"), is("12:12:00"));

        // absolute randomized time
        timeString = "2020-02-01T12:12:00A14:12:34";
        trigger = RuleUtils.createTriggerForTimeString(timeString);
        configuration = trigger.getConfiguration();

        assertThat(trigger.getTypeUID(), is("timer.AbsoluteDateTimeTrigger"));
        assertThat(configuration.get("date"), is("2020-02-01"));
        assertThat(configuration.get("time"), is("12:12:00"));
        assertThat(configuration.get("randomizeTime"), is("14:12:34"));

        // Recurring times,Monday = 64, Tuesday = 32, Wednesday = 16, Thursday = 8, Friday = 4, Saturday = 2, Sunday= 1
        // Cron expression: min hour day month weekdays
        timeString = "W3/T12:15:17";
        trigger = RuleUtils.createTriggerForTimeString(timeString);
        configuration = trigger.getConfiguration();

        assertThat(trigger.getTypeUID(), is("timer.GenericCronTrigger"));
        assertThat(configuration.get("cronExpression"), is("15 12 * * 6,7"));

        // Recurring randomized times
        timeString = "W127/T12:15:17A14:12:34";
        trigger = RuleUtils.createTriggerForTimeString(timeString);
        configuration = trigger.getConfiguration();

        assertThat(trigger.getTypeUID(), is("timer.GenericCronTrigger"));
        assertThat(configuration.get("cronExpression"), is("15 14 * * 1,2,3,4,5,6,7"));

        // Timer, expiring after given time
        timeString = "PT12:12:00";
        trigger = RuleUtils.createTriggerForTimeString(timeString);
        configuration = trigger.getConfiguration();

        assertThat(trigger.getTypeUID(), is("timer.TimerTrigger"));
        assertThat(configuration.get("time"), is("12:12:00"));

        // Timer with random element
        timeString = "PT12:12:00A14:12:34";
        trigger = RuleUtils.createTriggerForTimeString(timeString);
        configuration = trigger.getConfiguration();

        assertThat(trigger.getTypeUID(), is("timer.TimerTrigger"));
        assertThat(configuration.get("time"), is("12:12:00"));
        assertThat(configuration.get("randomizeTime"), is("14:12:34"));

        // Timers, Recurring timer
        timeString = "R/PT12:12:00";
        trigger = RuleUtils.createTriggerForTimeString(timeString);
        configuration = trigger.getConfiguration();

        assertThat(trigger.getTypeUID(), is("timer.TimerTrigger"));
        assertThat(configuration.get("time"), is("12:12:00"));
        assertThat(configuration.get("repeat"), is("-1"));

        // Recurring timer with random element
        timeString = "R12/PT12:12:00A14:12:34";
        trigger = RuleUtils.createTriggerForTimeString(timeString);
        configuration = trigger.getConfiguration();

        assertThat(trigger.getTypeUID(), is("timer.TimerTrigger"));
        assertThat(configuration.get("time"), is("12:12:00"));
        assertThat(configuration.get("randomizeTime"), is("14:12:34"));
        assertThat(configuration.get("repeat"), is("12"));
    }

    @Test
    public void triggerToTimestring() {
        String timeString;
        Trigger trigger;
        Configuration configuration;

        // absolute time
        configuration = new Configuration();
        configuration.put("date", "2020-02-01");
        configuration.put("time", "12:12:00");
        trigger = TriggerBuilder.create().withId("absolutetrigger").withTypeUID("timer.AbsoluteDateTimeTrigger")
                .withConfiguration(configuration).build();
        timeString = RuleUtils.timeStringFromTrigger(Collections.singletonList(trigger));

        assertThat(timeString, is("2020-02-01T12:12:00"));

        // absolute randomized time
        configuration = new Configuration();
        configuration.put("date", "2020-02-01");
        configuration.put("time", "12:12:00");
        configuration.put("randomizeTime", "14:12:34");
        trigger = TriggerBuilder.create().withId("absolutetrigger").withTypeUID("timer.AbsoluteDateTimeTrigger")
                .withConfiguration(configuration).build();
        timeString = RuleUtils.timeStringFromTrigger(Collections.singletonList(trigger));

        assertThat(timeString, is("2020-02-01T12:12:00A14:12:34"));

        // Recurring times,Monday = 64, Tuesday = 32, Wednesday = 16, Thursday = 8, Friday = 4, Saturday = 2, Sunday= 1
        // Cron expression: min hour day month weekdays
        configuration = new Configuration();
        configuration.put("cronExpression", "15 12 * * 6,7");
        trigger = TriggerBuilder.create().withId("crontrigger").withTypeUID("timer.GenericCronTrigger")
                .withConfiguration(configuration).build();
        timeString = RuleUtils.timeStringFromTrigger(Collections.singletonList(trigger));

        assertThat(timeString, is("W3/T12:15:00"));

        // Recurring randomized times (not possible, the cron rule has no way to store that info)
        configuration = new Configuration();
        configuration.put("cronExpression", "15 14 * * 1,2,3,4,5,6,7");
        trigger = TriggerBuilder.create().withId("crontrigger").withTypeUID("timer.GenericCronTrigger")
                .withConfiguration(configuration).build();
        timeString = RuleUtils.timeStringFromTrigger(Collections.singletonList(trigger));

        assertThat(timeString, is("W127/T14:15:00"));

        // Timer, expiring after given time
        configuration = new Configuration();
        configuration.put("time", "12:12:00");
        trigger = TriggerBuilder.create().withId("timertrigger").withTypeUID("timer.TimerTrigger")
                .withConfiguration(configuration).build();
        timeString = RuleUtils.timeStringFromTrigger(Collections.singletonList(trigger));

        assertThat(timeString, is("PT12:12:00"));

        // Timer with random element
        configuration = new Configuration();
        configuration.put("time", "12:12:00");
        configuration.put("randomizeTime", "14:12:34");
        trigger = TriggerBuilder.create().withId("timertrigger").withTypeUID("timer.TimerTrigger")
                .withConfiguration(configuration).build();
        timeString = RuleUtils.timeStringFromTrigger(Collections.singletonList(trigger));

        assertThat(timeString, is("PT12:12:00A14:12:34"));

        // Timers, Recurring timer
        configuration = new Configuration();
        configuration.put("time", "12:12:00");
        configuration.put("repeat", -1);
        trigger = TriggerBuilder.create().withId("timertrigger").withTypeUID("timer.TimerTrigger")
                .withConfiguration(configuration).build();
        timeString = RuleUtils.timeStringFromTrigger(Collections.singletonList(trigger));

        assertThat(timeString, is("R/PT12:12:00"));

        // Recurring timer with random element
        configuration = new Configuration();
        configuration.put("time", "12:12:00");
        configuration.put("randomizeTime", "14:12:34");
        configuration.put("repeat", 12);
        trigger = TriggerBuilder.create().withId("timertrigger").withTypeUID("timer.TimerTrigger")
                .withConfiguration(configuration).build();
        timeString = RuleUtils.timeStringFromTrigger(Collections.singletonList(trigger));

        assertThat(timeString, is("R12/PT12:12:00A14:12:34"));
    }
}
