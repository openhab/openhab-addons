/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.mcp.internal.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.io.mcp.internal.McpTestHelper.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.RuleStatus;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.Visibility;
import org.openhab.core.config.core.Configuration;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link RuleTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class RuleToolsTest {

    @Mock
    @Nullable
    RuleRegistry ruleRegistry;

    @Mock
    @Nullable
    RuleManager ruleManager;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();
    private @Nullable RuleTools ruleTools;

    @BeforeEach
    void setUp() {
        ruleTools = new RuleTools(Objects.requireNonNull(ruleRegistry), Objects.requireNonNull(ruleManager),
                jsonMapper);
    }

    private RuleTools tools() {
        RuleTools rt = ruleTools;
        assertNotNull(rt);
        return rt;
    }

    // --- Helper to create a mock Rule ---

    private Rule createMockRule(String uid, String name, String description, Set<String> tags) {
        Rule rule = mock(Rule.class);
        lenient().when(rule.getUID()).thenReturn(uid);
        lenient().when(rule.getName()).thenReturn(name);
        lenient().when(rule.getDescription()).thenReturn(description);
        lenient().when(rule.getTags()).thenReturn(tags);
        lenient().when(rule.getActions()).thenReturn(List.of());
        lenient().when(rule.getTriggers()).thenReturn(List.of());
        lenient().when(rule.getConditions()).thenReturn(List.of());
        lenient().when(rule.getConfiguration()).thenReturn(new Configuration());
        lenient().when(rule.getConfigurationDescriptions()).thenReturn(List.of());
        lenient().when(rule.getTemplateUID()).thenReturn(null);
        lenient().when(rule.getVisibility()).thenReturn(Visibility.VISIBLE);
        return rule;
    }

    private Rule createMockRuleWithTrigger(String uid, String name, Set<String> tags, String triggerTypeUID) {
        Rule rule = createMockRule(uid, name, "", tags);
        Trigger trigger = mock(Trigger.class);
        lenient().when(trigger.getTypeUID()).thenReturn(triggerTypeUID);
        lenient().when(trigger.getId()).thenReturn("trigger0");
        lenient().when(trigger.getConfiguration()).thenReturn(new Configuration());
        lenient().when(trigger.getLabel()).thenReturn(null);
        lenient().when(trigger.getDescription()).thenReturn(null);
        lenient().when(rule.getTriggers()).thenReturn(List.of(trigger));
        return rule;
    }

    // ========== handleGetRules ==========

    @Test
    void getRulesNoFiltersReturnsAll() throws Exception {
        Rule rule1 = createMockRuleWithTrigger("r1", "Rule 1", Set.of(), "timer.GenericCronTrigger");
        Rule rule2 = createMockRuleWithTrigger("r2", "Rule 2", Set.of("MCP"), "core.ItemStateChangeTrigger");
        when(ruleRegistry.getAll()).thenReturn(List.of(rule1, rule2));
        when(ruleManager.getStatus("r1")).thenReturn(RuleStatus.IDLE);
        when(ruleManager.getStatus("r2")).thenReturn(RuleStatus.IDLE);
        when(ruleManager.isEnabled("r1")).thenReturn(true);
        when(ruleManager.isEnabled("r2")).thenReturn(true);

        CallToolResult result = tools().handleGetRules(createRequest(Map.of()));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(2, parsed.get("total"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rules = (List<Map<String, Object>>) parsed.get("rules");
        assertEquals(2, rules.size());
    }

    @Test
    void getRulesFilterByTag() throws Exception {
        Rule rule1 = createMockRuleWithTrigger("r1", "Rule 1", Set.of(), "timer.GenericCronTrigger");
        Rule rule2 = createMockRuleWithTrigger("r2", "Rule 2", Set.of("MCP"), "core.ItemStateChangeTrigger");
        when(ruleRegistry.getAll()).thenReturn(List.of(rule1, rule2));
        when(ruleManager.getStatus("r2")).thenReturn(RuleStatus.IDLE);
        when(ruleManager.isEnabled("r2")).thenReturn(true);

        CallToolResult result = tools().handleGetRules(createRequest(Map.of("tag", "MCP")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(1, parsed.get("total"));
    }

    @Test
    void getRulesFilterByEnabled() throws Exception {
        Rule rule1 = createMockRuleWithTrigger("r1", "Rule 1", Set.of(), "timer.GenericCronTrigger");
        Rule rule2 = createMockRuleWithTrigger("r2", "Rule 2", Set.of(), "timer.GenericCronTrigger");
        when(ruleRegistry.getAll()).thenReturn(List.of(rule1, rule2));
        when(ruleManager.getStatus("r1")).thenReturn(RuleStatus.IDLE);
        when(ruleManager.getStatus("r2")).thenReturn(RuleStatus.IDLE);
        when(ruleManager.isEnabled("r1")).thenReturn(true);
        when(ruleManager.isEnabled("r2")).thenReturn(false);

        CallToolResult result = tools().handleGetRules(createRequest(Map.of("enabled", false)));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(1, parsed.get("total"));
    }

    @Test
    void getRulesPagination() throws Exception {
        Rule rule1 = createMockRuleWithTrigger("r1", "Rule 1", Set.of(), "timer.GenericCronTrigger");
        Rule rule2 = createMockRuleWithTrigger("r2", "Rule 2", Set.of(), "timer.GenericCronTrigger");
        Rule rule3 = createMockRuleWithTrigger("r3", "Rule 3", Set.of(), "timer.GenericCronTrigger");
        when(ruleRegistry.getAll()).thenReturn(List.of(rule1, rule2, rule3));
        when(ruleManager.getStatus(anyString())).thenReturn(RuleStatus.IDLE);
        when(ruleManager.isEnabled(anyString())).thenReturn(true);

        Map<String, Object> args = new HashMap<>();
        args.put("limit", 2);
        args.put("offset", 1);
        CallToolResult result = tools().handleGetRules(createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(3, parsed.get("total"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rules = (List<Map<String, Object>>) parsed.get("rules");
        assertEquals(2, rules.size());
    }

    // ========== handleManageRule ==========

    @Test
    void manageRuleEnable() throws Exception {
        Rule rule = createMockRule("r1", "Rule 1", "", Set.of());
        when(ruleRegistry.get("r1")).thenReturn(rule);
        when(ruleManager.getStatus("r1")).thenReturn(RuleStatus.IDLE);

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "r1");
        args.put("action", "enable");
        CallToolResult result = tools().handleManageRule(createRequest(args));
        assertSuccess(result);
        verify(ruleManager).setEnabled("r1", true);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("enable", parsed.get("action"));
    }

    @Test
    void manageRuleDisable() throws Exception {
        Rule rule = createMockRule("r1", "Rule 1", "", Set.of());
        when(ruleRegistry.get("r1")).thenReturn(rule);
        when(ruleManager.getStatus("r1")).thenReturn(RuleStatus.IDLE);

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "r1");
        args.put("action", "disable");
        CallToolResult result = tools().handleManageRule(createRequest(args));
        assertSuccess(result);
        verify(ruleManager).setEnabled("r1", false);
    }

    @Test
    void manageRuleTrigger() throws Exception {
        Rule rule = createMockRule("r1", "Rule 1", "", Set.of());
        when(ruleRegistry.get("r1")).thenReturn(rule);
        when(ruleManager.getStatus("r1")).thenReturn(RuleStatus.RUNNING);

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "r1");
        args.put("action", "trigger");
        CallToolResult result = tools().handleManageRule(createRequest(args));
        assertSuccess(result);
        verify(ruleManager).runNow("r1");
    }

    @Test
    void manageRuleRemove() throws Exception {
        Rule rule = createMockRule("r1", "Rule 1", "", Set.of());
        when(ruleRegistry.get("r1")).thenReturn(rule);

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "r1");
        args.put("action", "remove");
        CallToolResult result = tools().handleManageRule(createRequest(args));
        assertSuccess(result);
        verify(ruleRegistry).remove("r1");
        Map<String, Object> parsed = parseResult(result);
        assertEquals("remove", parsed.get("action"));
    }

    @Test
    void manageRuleNotFound() {
        when(ruleRegistry.get("missing")).thenReturn(null);

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "missing");
        args.put("action", "enable");
        CallToolResult result = tools().handleManageRule(createRequest(args));
        assertErrorContains(result, "not found");
    }

    @Test
    void manageRuleUnknownAction() {
        Rule rule = createMockRule("r1", "Rule 1", "", Set.of());
        when(ruleRegistry.get("r1")).thenReturn(rule);

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "r1");
        args.put("action", "explode");
        CallToolResult result = tools().handleManageRule(createRequest(args));
        assertErrorContains(result, "Unknown action");
    }

    @Test
    void manageRuleMissingParams() {
        CallToolResult result = tools().handleManageRule(createRequest(Map.of()));
        assertErrorContains(result, "required");
    }

    // ========== handleCreateRule ==========

    @Test
    void createRuleDatetimeTrigger() throws Exception {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "datetime");
        trigger.put("datetime", "2025-06-15T10:30:00");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Morning Alarm");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Kitchen_Light", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertSuccess(result);
        verify(ruleRegistry).add(any(Rule.class));
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("datetime", parsed.get("triggerType"));
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) parsed.get("tags");
        assertTrue(tags.contains("MCP"));
        assertTrue(tags.contains("MCP-oneshot"));
    }

    @Test
    void createRuleTimeOfDayTrigger() throws Exception {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "time_of_day");
        trigger.put("time", "08:00");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Daily Wake Up");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Bedroom_Light", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertSuccess(result);
        verify(ruleRegistry).add(any(Rule.class));
        Map<String, Object> parsed = parseResult(result);
        assertEquals("time_of_day", parsed.get("triggerType"));
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) parsed.get("tags");
        assertTrue(tags.contains("MCP"));
        assertFalse(tags.contains("MCP-oneshot"));
    }

    @Test
    void createRuleCronTrigger() throws Exception {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "cron");
        trigger.put("cronExpression", "0 0 8 ? * MON-FRI");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Weekday Alarm");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Kitchen_Light", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals("cron", parsed.get("triggerType"));
    }

    @Test
    void createRuleItemStateChangeTrigger() throws Exception {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "item_state_change");
        trigger.put("itemName", "GarageDoor_Contact");
        trigger.put("state", "OPEN");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Garage Door Alert");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Alert_Switch", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals("item_state_change", parsed.get("triggerType"));
    }

    @Test
    void createRuleItemCommandTrigger() throws Exception {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "item_command");
        trigger.put("itemName", "Button_Switch");
        trigger.put("command", "ON");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Button Pressed Rule");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Fan_Switch", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals("item_command", parsed.get("triggerType"));
    }

    @Test
    void createRuleMultipleActions() throws Exception {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "cron");
        trigger.put("cronExpression", "0 0 22 * * ?");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Bedtime Routine");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "OFF"),
                Map.of("itemName", "Light2", "command", "OFF"), Map.of("itemName", "Lock", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertSuccess(result);
        verify(ruleRegistry).add(any(Rule.class));
    }

    @Test
    void createRuleMissingName() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "cron");
        trigger.put("cronExpression", "0 0 8 * * ?");

        Map<String, Object> args = new HashMap<>();
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "name");
    }

    @Test
    void createRuleMissingTrigger() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "No Trigger Rule");
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "trigger");
    }

    @Test
    void createRuleMissingActions() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "cron");
        trigger.put("cronExpression", "0 0 8 * * ?");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "No Actions Rule");
        args.put("trigger", trigger);

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "actions");
    }

    @Test
    void createRuleEmptyActions() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "cron");
        trigger.put("cronExpression", "0 0 8 * * ?");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Empty Actions Rule");
        args.put("trigger", trigger);
        args.put("actions", List.of());

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "actions");
    }

    @Test
    void createRuleUnknownTriggerType() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "weather_change");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Unknown Trigger");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "Unknown trigger type");
    }

    @Test
    void createRuleDatetimeMissingDatetime() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "datetime");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "No Datetime");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "datetime");
    }

    @Test
    void createRuleTimeOfDayMissingTime() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "time_of_day");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "No Time");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "time");
    }

    @Test
    void createRuleCronMissingExpression() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "cron");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "No Cron");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "cronExpression");
    }

    @Test
    void createRuleItemStateChangeMissingItem() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "item_state_change");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "No Item");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "itemName");
    }

    @Test
    void createRuleItemCommandMissingItem() {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("type", "item_command");

        Map<String, Object> args = new HashMap<>();
        args.put("name", "No Item");
        args.put("trigger", trigger);
        args.put("actions", List.of(Map.of("itemName", "Light1", "command", "ON")));

        CallToolResult result = tools().handleCreateRule(createRequest(args));
        assertErrorContains(result, "itemName");
    }

    // ========== handleUpdateRule ==========

    @Test
    @SuppressWarnings("unchecked")
    void updateRuleSuccess() throws Exception {
        Rule existing = createMockRuleWithTrigger("r1", "Old Name", Set.of("MCP"), "timer.GenericCronTrigger");
        lenient().when(existing.getDescription()).thenReturn("Old description");
        when(ruleRegistry.get("r1")).thenReturn(existing);
        when(ruleRegistry.update(any(Rule.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "r1");
        args.put("name", "New Name");
        args.put("description", "New description");

        CallToolResult result = tools().handleUpdateRule(createRequest(args));
        assertSuccess(result);
        verify(ruleRegistry).update(any(Rule.class));
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("r1", parsed.get("ruleUID"));
        assertEquals("New Name", parsed.get("name"));
    }

    @Test
    void updateRuleNotFound() {
        when(ruleRegistry.get("missing")).thenReturn(null);

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "missing");
        args.put("name", "New Name");

        CallToolResult result = tools().handleUpdateRule(createRequest(args));
        assertErrorContains(result, "not found");
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateRulePreservesMcpTag() throws Exception {
        Rule existing = createMockRuleWithTrigger("r1", "My Rule", Set.of("MCP"), "timer.GenericCronTrigger");
        when(ruleRegistry.get("r1")).thenReturn(existing);
        when(ruleRegistry.update(any(Rule.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> args = new HashMap<>();
        args.put("ruleUID", "r1");
        args.put("tags", List.of("custom-tag"));

        CallToolResult result = tools().handleUpdateRule(createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        List<String> tags = (List<String>) parsed.get("tags");
        assertTrue(tags.contains("MCP"), "MCP tag should be preserved");
        assertTrue(tags.contains("custom-tag"), "Custom tag should be present");
    }

    @Test
    void updateRuleMissingRuleUID() {
        CallToolResult result = tools().handleUpdateRule(createRequest(Map.of()));
        assertErrorContains(result, "ruleUID");
    }
}
