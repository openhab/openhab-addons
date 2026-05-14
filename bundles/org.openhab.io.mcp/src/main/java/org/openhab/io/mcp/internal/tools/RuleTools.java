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

import static org.openhab.io.mcp.internal.tools.McpToolUtils.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.RuleStatus;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.automation.util.RuleBuilder;
import org.openhab.core.config.core.Configuration;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tools for rule listing, creation, and management.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class RuleTools {

    private static final String MCP_TAG = "MCP";
    private static final String MCP_ONESHOT_TAG = "MCP-oneshot";

    private final RuleRegistry ruleRegistry;
    private final RuleManager ruleManager;
    private final McpJsonMapper jsonMapper;

    /**
     * Creates a new {@code RuleTools} instance.
     *
     * @param ruleRegistry the rule registry for CRUD operations on automation rules
     * @param ruleManager the rule manager for querying status and controlling rule execution
     * @param jsonMapper the JSON mapper used to serialize tool results
     */
    public RuleTools(RuleRegistry ruleRegistry, RuleManager ruleManager, McpJsonMapper jsonMapper) {
        this.ruleRegistry = ruleRegistry;
        this.ruleManager = ruleManager;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns the {@code get_rules} tool schema.
     * Defines a tool that lists automation rules with optional filtering by tag and enabled state.
     *
     * @return the {@code get_rules} tool definition
     */
    public McpSchema.Tool getRulesTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("tag", Map.of("type", "string", "description", "Filter rules by tag"));
        properties.put("enabled", Map.of("type", "boolean", "description", "Filter by enabled/disabled state"));
        properties.put("limit", Map.of("type", "integer", "description", "Maximum results to return (default: 50)"));
        properties.put("offset", Map.of("type", "integer", "description", "Number of results to skip (default: 0)"));

        return McpSchema.Tool.builder().name("get_rules").description(
                "List automation rules with their status, triggers, and tags. Use tag='MCP' to see rules created by this agent.")
                .inputSchema(new McpSchema.JsonSchema("object", properties, List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code get_rules} call.
     * Retrieves rules from the registry, applies optional tag and enabled filters, and returns a paginated result.
     *
     * @param request the tool request containing optional {@code tag}, {@code enabled}, {@code limit}, and
     *            {@code offset} arguments
     * @return the paginated list of matching rules with their status summaries
     */
    public CallToolResult handleGetRules(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String tagFilter = getStringArg(args, "tag");
        Boolean enabledFilter = getNullableBooleanArg(args, "enabled");
        int limit = getIntArg(args, "limit", 50);
        int offset = getIntArg(args, "offset", 0);

        List<Map<String, Object>> rules = new ArrayList<>();
        for (Rule rule : ruleRegistry.getAll()) {
            if (tagFilter != null && !rule.getTags().contains(tagFilter)) {
                continue;
            }
            RuleStatus status = ruleManager.getStatus(rule.getUID());
            boolean isEnabled = ruleManager.isEnabled(rule.getUID());
            if (enabledFilter != null && isEnabled != enabledFilter) {
                continue;
            }
            rules.add(buildRuleSummary(rule, status));
        }

        int total = rules.size();
        List<Map<String, Object>> page = rules.subList(Math.min(offset, total), Math.min(offset + limit, total));

        Map<String, Object> response = new HashMap<>();
        response.put("rules", page);
        response.put("total", total);
        response.put("offset", offset);
        response.put("limit", limit);
        return textResult(jsonMapper, response);
    }

    /**
     * Returns the {@code manage_rule} tool schema.
     * Defines a tool that enables, disables, triggers, or removes an automation rule.
     *
     * @return the {@code manage_rule} tool definition
     */
    public McpSchema.Tool getManageRuleTool() {
        return McpSchema.Tool.builder().name("manage_rule")
                .description("Enable, disable, manually trigger, or permanently remove an automation rule.")
                .inputSchema(new McpSchema.JsonSchema("object",
                        Map.of("ruleUID", Map.of("type", "string", "description", "The unique identifier of the rule"),
                                "action",
                                Map.of("type", "string", "description",
                                        "Action to perform: 'enable', 'disable', 'trigger', or 'remove'", "enum",
                                        List.of("enable", "disable", "trigger", "remove"))),
                        List.of("ruleUID", "action"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code manage_rule} call.
     * Performs the requested action (enable, disable, trigger, or remove) on the specified rule.
     *
     * @param request the tool request containing {@code ruleUID} and {@code action} arguments
     * @return the result indicating success and the new rule status
     */
    public CallToolResult handleManageRule(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String ruleUID = getStringArg(args, "ruleUID");
        String action = getStringArg(args, "action");

        if (ruleUID == null || action == null) {
            return errorResult("Both 'ruleUID' and 'action' are required.");
        }

        Rule rule = ruleRegistry.get(ruleUID);
        if (rule == null) {
            return errorResult("Rule '" + ruleUID + "' not found.");
        }

        switch (action.toLowerCase(Locale.ROOT)) {
            case "enable":
                ruleManager.setEnabled(ruleUID, true);
                break;
            case "disable":
                ruleManager.setEnabled(ruleUID, false);
                break;
            case "trigger":
                ruleManager.runNow(ruleUID);
                break;
            case "remove":
                ruleRegistry.remove(ruleUID);
                Map<String, Object> removeResult = new HashMap<>();
                removeResult.put("success", true);
                removeResult.put("ruleUID", ruleUID);
                removeResult.put("action", "remove");
                return textResult(jsonMapper, removeResult);
            default:
                return errorResult("Unknown action '" + action + "'. Use 'enable', 'disable', 'trigger', or 'remove'.");
        }

        RuleStatus newStatus = ruleManager.getStatus(ruleUID);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ruleUID", ruleUID);
        result.put("action", action);
        result.put("status", newStatus != null ? newStatus.name() : "UNKNOWN");
        return textResult(jsonMapper, result);
    }

    /**
     * Returns the {@code create_rule} tool schema.
     * Defines a tool that creates an automation rule with a trigger and one or more item-command actions.
     *
     * @return the {@code create_rule} tool definition
     */
    public McpSchema.Tool getCreateRuleTool() {
        Map<String, Object> triggerProps = new LinkedHashMap<>();
        triggerProps.put("type", Map.of("type", "string", "description", """
                Trigger type: 'datetime' (one-time at ISO-8601 date/time), \
                'time_of_day' (daily at HH:MM), 'cron' (cron expression), \
                'item_state_change' (when item state changes), \
                'item_command' (when item receives command)""", "enum",
                List.of("datetime", "time_of_day", "cron", "item_state_change", "item_command")));
        triggerProps.put("datetime", Map.of("type", "string", "description",
                "ISO-8601 datetime for 'datetime' trigger (e.g. 2026-04-17T15:00:00)"));
        triggerProps.put("time", Map.of("type", "string", "description", "HH:MM time for 'time_of_day' trigger"));
        triggerProps.put("cronExpression",
                Map.of("type", "string", "description", "Cron expression for 'cron' trigger"));
        triggerProps.put("itemName", Map.of("type", "string", "description",
                "Item name for 'item_state_change' or 'item_command' triggers"));
        triggerProps.put("state",
                Map.of("type", "string", "description", "Optional: only trigger when item changes TO this state"));
        triggerProps.put("previousState",
                Map.of("type", "string", "description", "Optional: only trigger when item changes FROM this state"));
        triggerProps.put("command",
                Map.of("type", "string", "description", "Optional: only trigger on this specific command"));

        Map<String, Object> actionProps = new LinkedHashMap<>();
        actionProps.put("itemName", Map.of("type", "string", "description", "Item to command"));
        actionProps.put("command", Map.of("type", "string", "description", "Command to send (ON, OFF, 0-100, etc.)"));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", Map.of("type", "string", "description", "Human-readable name for the rule"));
        properties.put("description", Map.of("type", "string", "description", "Optional description"));
        properties.put("trigger", Map.of("type", "object", "description", "What triggers the rule", "properties",
                triggerProps, "required", List.of("type")));
        properties.put("actions", Map.of("type", "array", "description",
                "List of item commands to execute when triggered", "items",
                Map.of("type", "object", "properties", actionProps, "required", List.of("itemName", "command"))));

        return McpSchema.Tool.builder().name("create_rule").description("""
                Create an automation rule. \
                When the user says 'create a rule' or asks for a persistent automation, always use a \
                recurring trigger ('time_of_day', 'cron', 'item_state_change', or 'item_command'). \
                Only use 'datetime' (one-time, auto-deleted after firing) when the user explicitly \
                requests a one-shot action with a specific date (e.g. 'at 3pm today', 'tomorrow at 7am'). \
                Use 'cron' for complex schedules (e.g. 'weekdays at 8am'). \
                Use 'item_state_change' or 'item_command' for event-driven rules \
                (e.g. 'when the garage door opens'). \
                Created rules are tagged 'MCP' — list them with get_rules(tag='MCP'). \
                Remove with manage_rule(action='remove').""").inputSchema(
                new McpSchema.JsonSchema("object", properties, List.of("name", "trigger", "actions"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code create_rule} call.
     * Builds a new rule from the provided name, trigger, and actions, then adds it to the registry.
     *
     * @param request the tool request containing {@code name}, {@code trigger}, {@code actions}, and optional
     *            {@code description} arguments
     * @return the result with the new rule's UID, name, tags, and trigger type
     */
    @SuppressWarnings("unchecked")
    public CallToolResult handleCreateRule(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();

        String name = getStringArg(args, "name");
        if (name == null || name.isBlank()) {
            return errorResult("'name' is required.");
        }
        String description = getStringArg(args, "description");

        Object triggerObj = args.get("trigger");
        if (!(triggerObj instanceof Map<?, ?> triggerMap)) {
            return errorResult("'trigger' object is required.");
        }
        Map<String, Object> typedTriggerMap = (Map<String, Object>) triggerMap;

        Object actionsObj = args.get("actions");
        if (!(actionsObj instanceof List<?> actionsList) || actionsList.isEmpty()) {
            return errorResult("'actions' must be a non-empty array of {itemName, command} objects.");
        }

        Trigger trigger;
        try {
            trigger = buildTrigger(typedTriggerMap);
        } catch (IllegalArgumentException e) {
            return errorResult(Objects.requireNonNullElse(e.getMessage(), "Invalid trigger configuration."));
        }

        List<Action> actions;
        try {
            actions = parseActions(actionsList);
        } catch (IllegalArgumentException e) {
            return errorResult(Objects.requireNonNullElse(e.getMessage(), "Invalid actions."));
        }

        boolean isOneShot = "datetime".equals(typedTriggerMap.get("type"));
        Set<String> tags = new HashSet<>();
        tags.add(MCP_TAG);
        if (isOneShot) {
            tags.add(MCP_ONESHOT_TAG);
        }

        String ruleUID = "mcp-" + UUID.randomUUID().toString();
        Configuration ruleConfig = new Configuration();
        if (isOneShot) {
            ruleConfig.put("mcpFireAt", String.valueOf(typedTriggerMap.get("datetime")));
        }
        Rule rule = RuleBuilder.create(ruleUID).withName(name).withDescription(description != null ? description : "")
                .withTags(tags).withTriggers(trigger).withActions(actions).withConfiguration(ruleConfig).build();

        ruleRegistry.add(rule);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ruleUID", ruleUID);
        result.put("name", name);
        result.put("tags", tags);
        result.put("triggerType", Objects.requireNonNullElse(typedTriggerMap.get("type"), "unknown"));
        return textResult(jsonMapper, result);
    }

    private static List<Action> parseActions(List<?> actionsList) {
        List<Action> actions = new ArrayList<>();
        int idx = 0;
        for (Object actionObj : actionsList) {
            if (!(actionObj instanceof Map<?, ?> actionMap)) {
                throw new IllegalArgumentException("Each action must be an object with 'itemName' and 'command'.");
            }
            Object itemNameObj = actionMap.get("itemName");
            Object commandObj = actionMap.get("command");
            if (!(itemNameObj instanceof String itemName) || !(commandObj instanceof String command)) {
                throw new IllegalArgumentException("Each action requires string 'itemName' and 'command'.");
            }
            Configuration actionConfig = new Configuration();
            actionConfig.put("itemName", itemName);
            actionConfig.put("command", command);
            actions.add(ModuleBuilder.createAction().withId("action" + idx).withTypeUID("core.ItemCommandAction")
                    .withConfiguration(actionConfig).build());
            idx++;
        }
        return actions;
    }

    private static Trigger buildTrigger(Map<String, Object> triggerMap) {
        String type = (String) triggerMap.get("type");
        if (type == null) {
            throw new IllegalArgumentException("trigger.type is required.");
        }

        Configuration config = new Configuration();
        return switch (type) {
            case "datetime" -> {
                String datetime = (String) triggerMap.get("datetime");
                if (datetime == null || datetime.isBlank()) {
                    throw new IllegalArgumentException("trigger.datetime is required for type 'datetime'.");
                }
                String cron = datetimeToCron(datetime);
                config.put("cronExpression", cron);
                yield ModuleBuilder.createTrigger().withId("trigger0").withTypeUID("timer.GenericCronTrigger")
                        .withConfiguration(config).build();
            }
            case "time_of_day" -> {
                String time = (String) triggerMap.get("time");
                if (time == null || time.isBlank()) {
                    throw new IllegalArgumentException("trigger.time (HH:MM) is required for type 'time_of_day'.");
                }
                config.put("time", time);
                yield ModuleBuilder.createTrigger().withId("trigger0").withTypeUID("timer.TimeOfDayTrigger")
                        .withConfiguration(config).build();
            }
            case "cron" -> {
                String cronExpression = (String) triggerMap.get("cronExpression");
                if (cronExpression == null || cronExpression.isBlank()) {
                    throw new IllegalArgumentException("trigger.cronExpression is required for type 'cron'.");
                }
                config.put("cronExpression", cronExpression);
                yield ModuleBuilder.createTrigger().withId("trigger0").withTypeUID("timer.GenericCronTrigger")
                        .withConfiguration(config).build();
            }
            case "item_state_change" -> {
                String itemName = (String) triggerMap.get("itemName");
                if (itemName == null || itemName.isBlank()) {
                    throw new IllegalArgumentException("trigger.itemName is required for type 'item_state_change'.");
                }
                config.put("itemName", itemName);
                Object state = triggerMap.get("state");
                if (state instanceof String s && !s.isBlank()) {
                    config.put("state", s);
                }
                Object previousState = triggerMap.get("previousState");
                if (previousState instanceof String s && !s.isBlank()) {
                    config.put("previousState", s);
                }
                yield ModuleBuilder.createTrigger().withId("trigger0").withTypeUID("core.ItemStateChangeTrigger")
                        .withConfiguration(config).build();
            }
            case "item_command" -> {
                String itemName = (String) triggerMap.get("itemName");
                if (itemName == null || itemName.isBlank()) {
                    throw new IllegalArgumentException("trigger.itemName is required for type 'item_command'.");
                }
                config.put("itemName", itemName);
                Object command = triggerMap.get("command");
                if (command instanceof String s && !s.isBlank()) {
                    config.put("command", s);
                }
                yield ModuleBuilder.createTrigger().withId("trigger0").withTypeUID("core.ItemCommandTrigger")
                        .withConfiguration(config).build();
            }
            default -> throw new IllegalArgumentException("Unknown trigger type '" + type
                    + "'. Use: datetime, time_of_day, cron, item_state_change, item_command.");
        };
    }

    private static String datetimeToCron(String isoDatetime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDatetime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return String.format("%d %d %d %d %d ? %d", dt.getSecond(), dt.getMinute(), dt.getHour(),
                    dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Cannot parse datetime '" + isoDatetime + "'. Expected ISO-8601 format like 2026-04-17T15:00:00.");
        }
    }

    /**
     * Returns the {@code update_rule} tool schema.
     * Defines a tool that updates a rule's name, description, tags, or actions without recreating it.
     *
     * @return the {@code update_rule} tool definition
     */
    public McpSchema.Tool getUpdateRuleTool() {
        Map<String, Object> actionProps = new LinkedHashMap<>();
        actionProps.put("itemName", Map.of("type", "string", "description", "Item to command"));
        actionProps.put("command", Map.of("type", "string", "description", "Command to send (ON, OFF, 0-100, etc.)"));

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("ruleUID", Map.of("type", "string", "description", "UID of the rule to update"));
        props.put("name", Map.of("type", "string", "description", "New name (omit to keep current)"));
        props.put("description", Map.of("type", "string", "description", "New description (omit to keep current)"));
        props.put("tags", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Replace all tags (omit to keep current). The 'MCP' tag is always preserved."));
        props.put("actions", Map.of("type", "array", "description",
                "Replace all actions (omit to keep current). Same format as create_rule.", "items",
                Map.of("type", "object", "properties", actionProps, "required", List.of("itemName", "command"))));

        return McpSchema.Tool.builder().name("update_rule")
                .description("Update a rule's name, description, tags, or actions without recreating it. "
                        + "Only provided fields are changed; omitted fields keep their current values. "
                        + "To change the trigger type, delete and recreate the rule instead.")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("ruleUID"), null, null, null)).build();
    }

    /**
     * Handles an {@code update_rule} call.
     * Applies the provided field updates to an existing rule, preserving unchanged fields and the MCP tag.
     *
     * @param request the tool request containing {@code ruleUID} and optional {@code name}, {@code description},
     *            {@code tags}, and {@code actions} arguments
     * @return the result with the updated rule's UID, name, and tags
     */
    @SuppressWarnings("unchecked")
    public CallToolResult handleUpdateRule(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String ruleUID = getStringArg(args, "ruleUID");
        if (ruleUID == null || ruleUID.isBlank()) {
            return errorResult("'ruleUID' is required.");
        }

        Rule existing = ruleRegistry.get(ruleUID);
        if (existing == null) {
            return errorResult("Rule '" + ruleUID + "' not found.");
        }

        RuleBuilder builder = RuleBuilder.create(existing);
        String name = getStringArg(args, "name");
        if (name != null) {
            builder.withName(name);
        }
        String description = getStringArg(args, "description");
        if (description != null) {
            builder.withDescription(description);
        }

        Object tagsObj = args.get("tags");
        if (tagsObj instanceof List<?> tagsList) {
            Set<String> tags = new HashSet<>();
            tags.add(MCP_TAG);
            for (Object t : tagsList) {
                if (t instanceof String s && !s.isBlank()) {
                    tags.add(s);
                }
            }
            builder.withTags(tags);
        }

        Object actionsObj = args.get("actions");
        if (actionsObj instanceof List<?> actionsList && !actionsList.isEmpty()) {
            try {
                builder.withActions(parseActions(actionsList));
            } catch (IllegalArgumentException e) {
                return errorResult(Objects.requireNonNullElse(e.getMessage(), "Invalid actions."));
            }
        }

        Rule updated = builder.build();
        ruleRegistry.update(updated);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ruleUID", ruleUID);
        result.put("name", Objects.requireNonNullElse(updated.getName(), ""));
        result.put("tags", updated.getTags());
        return textResult(jsonMapper, result);
    }

    private Map<String, Object> buildRuleSummary(Rule rule, @Nullable RuleStatus status) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("uid", rule.getUID());
        summary.put("name", Objects.requireNonNullElse(rule.getName(), ""));
        summary.put("description", Objects.requireNonNullElse(rule.getDescription(), ""));
        summary.put("status", status != null ? status.name() : "UNKNOWN");
        if (!rule.getTags().isEmpty()) {
            summary.put("tags", rule.getTags());
        }

        List<String> triggerTypes = rule.getTriggers().stream().map(t -> t.getTypeUID()).toList();
        if (!triggerTypes.isEmpty()) {
            summary.put("triggerTypes", triggerTypes);
        }

        return summary;
    }
}
