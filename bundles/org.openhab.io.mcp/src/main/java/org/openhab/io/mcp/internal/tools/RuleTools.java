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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
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
    private static final String JS_MIME_TYPE = "application/javascript";

    private final RuleRegistry ruleRegistry;
    private final RuleManager ruleManager;
    private final McpJsonMapper jsonMapper;
    private final boolean scriptingEnabled;

    /**
     * Creates a new {@code RuleTools} instance.
     *
     * @param ruleRegistry the rule registry for CRUD operations on automation rules
     * @param ruleManager the rule manager for querying status and controlling rule execution
     * @param jsonMapper the JSON mapper used to serialize tool results
     * @param scriptingEnabled when true, allows {@code script}-typed actions and conditions; when false,
     *            those types are rejected with an error pointing at the {@code enableScripting} config flag
     */
    public RuleTools(RuleRegistry ruleRegistry, RuleManager ruleManager, McpJsonMapper jsonMapper,
            boolean scriptingEnabled) {
        this.ruleRegistry = ruleRegistry;
        this.ruleManager = ruleManager;
        this.jsonMapper = jsonMapper;
        this.scriptingEnabled = scriptingEnabled;
    }

    /**
     * Returns the {@code get_rules} tool schema.
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
     */
    public McpSchema.Tool getCreateRuleTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", Map.of("type", "string", "description", "Human-readable name for the rule"));
        properties.put("description", Map.of("type", "string", "description", "Optional description"));
        properties.put("trigger",
                Map.of("type", "object", "description",
                        "Single-trigger shorthand. For multiple triggers, use 'triggers' instead.", "properties",
                        triggerProps(), "required", List.of("type")));
        properties.put("triggers",
                Map.of("type", "array", "description", "One or more triggers (any matching trigger fires the rule).",
                        "items", Map.of("type", "object", "properties", triggerProps(), "required", List.of("type"))));
        properties.put("conditions",
                Map.of("type", "array", "description", "Optional conditions; all must pass for actions to run.",
                        "items",
                        Map.of("type", "object", "properties", conditionProps(), "required", List.of("type"))));
        properties.put("actions", Map.of("type", "array", "description", actionsDescription(), "items",
                Map.of("type", "object", "properties", actionProps(), "required", List.of("type"))));

        return McpSchema.Tool.builder().name("create_rule").description(createRuleDescription())
                .inputSchema(
                        new McpSchema.JsonSchema("object", properties, List.of("name", "actions"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code create_rule} call.
     */
    public CallToolResult handleCreateRule(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();

        String name = getStringArg(args, "name");
        if (name == null || name.isBlank()) {
            return errorResult("'name' is required.");
        }
        String description = getStringArg(args, "description");

        List<Map<String, Object>> triggerMaps;
        try {
            triggerMaps = collectTriggerMaps(args);
        } catch (IllegalArgumentException e) {
            return errorResult(Objects.requireNonNullElse(e.getMessage(), "Invalid trigger configuration."));
        }

        Object actionsObj = args.get("actions");
        if (!(actionsObj instanceof List<?> actionsList) || actionsList.isEmpty()) {
            return errorResult("'actions' must be a non-empty array.");
        }

        List<Trigger> triggers;
        try {
            triggers = buildTriggers(triggerMaps);
        } catch (IllegalArgumentException e) {
            return errorResult(Objects.requireNonNullElse(e.getMessage(), "Invalid trigger configuration."));
        }

        List<Condition> conditions;
        try {
            conditions = parseConditions(args.get("conditions"));
        } catch (IllegalArgumentException e) {
            return errorResult(Objects.requireNonNullElse(e.getMessage(), "Invalid conditions."));
        }

        List<Action> actions;
        try {
            actions = parseActions(actionsList);
        } catch (IllegalArgumentException e) {
            return errorResult(Objects.requireNonNullElse(e.getMessage(), "Invalid actions."));
        }

        boolean isOneShot = triggerMaps.size() == 1 && "datetime".equals(triggerMaps.get(0).get("type"));
        Set<String> tags = new HashSet<>();
        tags.add(MCP_TAG);
        if (isOneShot) {
            tags.add(MCP_ONESHOT_TAG);
        }

        String ruleUID = "mcp-" + UUID.randomUUID().toString();
        Configuration ruleConfig = new Configuration();
        if (isOneShot) {
            // buildTrigger already validated this is a non-blank string, so resolveDatetime won't NPE
            LocalDateTime fireAt = resolveDatetime(String.valueOf(triggerMaps.get(0).get("datetime")));
            ruleConfig.put("mcpFireAt", fireAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        Rule rule = RuleBuilder.create(ruleUID).withName(name).withDescription(description != null ? description : "")
                .withTags(tags).withTriggers(triggers).withConditions(conditions).withActions(actions)
                .withConfiguration(ruleConfig).build();

        ruleRegistry.add(rule);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ruleUID", ruleUID);
        result.put("name", name);
        result.put("tags", tags);
        List<String> triggerTypes = triggerMaps.stream().map(m -> String.valueOf(m.get("type"))).toList();
        result.put("triggerType", triggerTypes.get(0));
        result.put("triggerTypes", triggerTypes);
        return textResult(jsonMapper, result);
    }

    /**
     * Returns the {@code update_rule} tool schema.
     */
    public McpSchema.Tool getUpdateRuleTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("ruleUID", Map.of("type", "string", "description", "UID of the rule to update"));
        props.put("name", Map.of("type", "string", "description", "New name (omit to keep current)"));
        props.put("description", Map.of("type", "string", "description", "New description (omit to keep current)"));
        props.put("tags", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Replace all tags (omit to keep current). The 'MCP' tag is always preserved."));
        props.put("conditions",
                Map.of("type", "array", "description",
                        "Replace all conditions (omit to keep current). Same format as create_rule.", "items",
                        Map.of("type", "object", "properties", conditionProps(), "required", List.of("type"))));
        props.put("actions",
                Map.of("type", "array", "description",
                        "Replace all actions (omit to keep current). Same format as create_rule.", "items",
                        Map.of("type", "object", "properties", actionProps(), "required", List.of("type"))));

        return McpSchema.Tool.builder().name("update_rule").description("""
                Update a rule's name, description, tags, conditions, or actions without recreating it. \
                Only provided fields are changed; omitted fields keep their current values. \
                To change the triggers, delete and recreate the rule instead.""")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("ruleUID"), null, null, null)).build();
    }

    /**
     * Handles an {@code update_rule} call.
     */
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

        Object conditionsObj = args.get("conditions");
        if (conditionsObj instanceof List<?>) {
            try {
                builder.withConditions(parseConditions(conditionsObj));
            } catch (IllegalArgumentException e) {
                return errorResult(Objects.requireNonNullElse(e.getMessage(), "Invalid conditions."));
            }
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

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> collectTriggerMaps(Map<String, Object> args) {
        Object triggersObj = args.get("triggers");
        if (triggersObj instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> m)) {
                    throw new IllegalArgumentException("Each entry in 'triggers' must be an object.");
                }
                result.add((Map<String, Object>) m);
            }
            if (result.isEmpty()) {
                throw new IllegalArgumentException("'triggers' array must not be empty.");
            }
            return result;
        }
        Object triggerObj = args.get("trigger");
        if (triggerObj instanceof Map<?, ?> m) {
            return List.of((Map<String, Object>) m);
        }
        throw new IllegalArgumentException("Either 'trigger' (object) or 'triggers' (array) is required.");
    }

    private static List<Trigger> buildTriggers(List<Map<String, Object>> triggerMaps) {
        List<Trigger> triggers = new ArrayList<>();
        for (int i = 0; i < triggerMaps.size(); i++) {
            triggers.add(buildTrigger(triggerMaps.get(i), "trigger" + i));
        }
        return triggers;
    }

    private static Trigger buildTrigger(Map<String, Object> triggerMap, String id) {
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
                yield ModuleBuilder.createTrigger().withId(id).withTypeUID("timer.GenericCronTrigger")
                        .withConfiguration(config).build();
            }
            case "time_of_day" -> {
                String time = (String) triggerMap.get("time");
                if (time == null || time.isBlank()) {
                    throw new IllegalArgumentException("trigger.time (HH:MM) is required for type 'time_of_day'.");
                }
                config.put("time", time);
                yield ModuleBuilder.createTrigger().withId(id).withTypeUID("timer.TimeOfDayTrigger")
                        .withConfiguration(config).build();
            }
            case "cron" -> {
                String cronExpression = (String) triggerMap.get("cronExpression");
                if (cronExpression == null || cronExpression.isBlank()) {
                    throw new IllegalArgumentException("trigger.cronExpression is required for type 'cron'.");
                }
                config.put("cronExpression", cronExpression);
                yield ModuleBuilder.createTrigger().withId(id).withTypeUID("timer.GenericCronTrigger")
                        .withConfiguration(config).build();
            }
            case "item_state_change" -> {
                String itemName = requireString(triggerMap, "itemName", "item_state_change");
                config.put("itemName", itemName);
                putIfString(config, triggerMap, "state");
                putIfString(config, triggerMap, "previousState");
                yield ModuleBuilder.createTrigger().withId(id).withTypeUID("core.ItemStateChangeTrigger")
                        .withConfiguration(config).build();
            }
            case "item_command" -> {
                String itemName = requireString(triggerMap, "itemName", "item_command");
                config.put("itemName", itemName);
                putIfString(config, triggerMap, "command");
                yield ModuleBuilder.createTrigger().withId(id).withTypeUID("core.ItemCommandTrigger")
                        .withConfiguration(config).build();
            }
            default -> throw new IllegalArgumentException("Unknown trigger type '" + type
                    + "'. Use: datetime, time_of_day, cron, item_state_change, item_command.");
        };
    }

    private static final Pattern SIMPLE_OFFSET = Pattern.compile("^([+-]?)\\s*(\\d+)\\s*([smhdwSMHDW])$");

    /**
     * Resolves a datetime string into an absolute {@link LocalDateTime} against the server's clock.
     *
     * <p>
     * Accepted forms:
     * <ul>
     * <li>Absolute ISO-8601 local date-time: {@code 2026-04-17T15:00:00}</li>
     * <li>Simple offset: {@code +30s}, {@code +5m}, {@code +2h}, {@code +1d}, {@code +1w} (negative allowed)</li>
     * <li>ISO time duration: {@code PT30S}, {@code PT5M}, {@code PT2H}, {@code PT1H30M}</li>
     * <li>ISO date period: {@code P1D}, {@code P2W}</li>
     * </ul>
     */
    static LocalDateTime resolveDatetime(String input) {
        String s = input.trim();

        Matcher m = SIMPLE_OFFSET.matcher(s);
        if (m.matches()) {
            long sign = "-".equals(m.group(1)) ? -1L : 1L;
            long n = Long.parseLong(m.group(2));
            long seconds = switch (Character.toLowerCase(m.group(3).charAt(0))) {
                case 's' -> n;
                case 'm' -> n * 60L;
                case 'h' -> n * 3600L;
                case 'd' -> n * 86_400L;
                case 'w' -> n * 604_800L;
                default -> throw new IllegalArgumentException("Unknown offset unit in '" + input + "'.");
            };
            return LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(sign * seconds);
        }

        // ISO time duration must start with PT (positive or negative); strip an optional leading '+'.
        String iso = s.startsWith("+") ? s.substring(1) : s;
        if (iso.startsWith("PT") || iso.startsWith("-PT")) {
            try {
                return LocalDateTime.now(ZoneId.systemDefault()).plus(Duration.parse(iso));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Cannot parse ISO duration '" + input + "'.");
            }
        }
        if (iso.startsWith("P") || iso.startsWith("-P")) {
            try {
                return LocalDateTime.now(ZoneId.systemDefault()).plus(Period.parse(iso));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Cannot parse ISO period '" + input + "'.");
            }
        }

        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Cannot parse datetime '" + input
                    + "'. Expected ISO-8601 datetime (2026-04-17T15:00:00), simple offset (+30s, +5m, +2h, +1d, +1w),"
                    + " ISO duration (PT30M, PT2H), or ISO period (P1D, P2W).");
        }
    }

    private static String datetimeToCron(String input) {
        LocalDateTime dt = resolveDatetime(input);
        return String.format("%d %d %d %d %d ? %d", dt.getSecond(), dt.getMinute(), dt.getHour(), dt.getDayOfMonth(),
                dt.getMonthValue(), dt.getYear());
    }

    private List<Action> parseActions(List<?> actionsList) {
        List<Action> actions = new ArrayList<>();
        int idx = 0;
        for (Object actionObj : actionsList) {
            if (!(actionObj instanceof Map<?, ?> raw)) {
                throw new IllegalArgumentException("Each action must be an object.");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> actionMap = (Map<String, Object>) raw;
            String type = resolveActionType(actionMap);
            actions.add(buildAction(type, actionMap, "action" + idx));
            idx++;
        }
        return actions;
    }

    private static String resolveActionType(Map<String, Object> actionMap) {
        Object t = actionMap.get("type");
        if (t instanceof String s && !s.isBlank()) {
            return s;
        }
        throw new IllegalArgumentException("Each action requires a 'type' field. Allowed: item_command, "
                + "item_state_update, notification, run_rule, rule_enablement, script.");
    }

    private Action buildAction(String type, Map<String, Object> a, String id) {
        return switch (type) {
            case "item_command" -> {
                Configuration c = new Configuration();
                c.put("itemName", requireString(a, "itemName", "item_command"));
                c.put("command", requireString(a, "command", "item_command"));
                yield ModuleBuilder.createAction().withId(id).withTypeUID("core.ItemCommandAction").withConfiguration(c)
                        .build();
            }
            case "item_state_update" -> {
                Configuration c = new Configuration();
                c.put("itemName", requireString(a, "itemName", "item_state_update"));
                c.put("state", requireString(a, "state", "item_state_update"));
                yield ModuleBuilder.createAction().withId(id).withTypeUID("core.ItemStateUpdateAction")
                        .withConfiguration(c).build();
            }
            case "notification" -> buildNotificationAction(a, id);
            case "run_rule" -> {
                Configuration c = new Configuration();
                c.put("ruleUIDs", requireStringList(a, "ruleUIDs", "run_rule"));
                Object cc = a.get("considerConditions");
                c.put("considerConditions", cc instanceof Boolean b ? b : Boolean.TRUE);
                yield ModuleBuilder.createAction().withId(id).withTypeUID("core.RunRuleAction").withConfiguration(c)
                        .build();
            }
            case "rule_enablement" -> {
                Configuration c = new Configuration();
                c.put("ruleUIDs", requireStringList(a, "ruleUIDs", "rule_enablement"));
                Object en = a.get("enable");
                if (!(en instanceof Boolean)) {
                    throw new IllegalArgumentException("rule_enablement action requires boolean 'enable'.");
                }
                c.put("enable", en);
                yield ModuleBuilder.createAction().withId(id).withTypeUID("core.RuleEnablementAction")
                        .withConfiguration(c).build();
            }
            case "script" -> {
                requireScriptingEnabled("Script actions");
                Configuration c = new Configuration();
                c.put("type", JS_MIME_TYPE);
                c.put("script", requireString(a, "script", "script"));
                yield ModuleBuilder.createAction().withId(id).withTypeUID("script.ScriptAction").withConfiguration(c)
                        .build();
            }
            default -> throw new IllegalArgumentException("Unknown action type '" + type
                    + "'. Allowed: item_command, item_state_update, notification, run_rule, rule_enablement, script.");
        };
    }

    private static Action buildNotificationAction(Map<String, Object> a, String id) {
        String scope = a.get("scope") instanceof String s && !s.isBlank() ? s.toLowerCase(Locale.ROOT) : "user";
        String message = requireString(a, "message", "notification");
        Configuration c = new Configuration();
        c.put("message", message);

        String typeUID = switch (scope) {
            case "user" -> {
                c.put("userId", requireString(a, "userId", "notification (scope=user)"));
                applyExtended2Fields(c, a);
                yield "notification.SendExtended2Notification";
            }
            case "broadcast" -> {
                applyExtended2Fields(c, a);
                yield "notification.SendExtended2BroadcastNotification";
            }
            case "log" -> {
                putIfString(c, a, "icon");
                putIfString(c, a, "severity");
                yield "notification.SendExtendedLogNotification";
            }
            default -> throw new IllegalArgumentException(
                    "notification.scope must be 'user', 'broadcast', or 'log' (got '" + scope + "').");
        };

        return ModuleBuilder.createAction().withId(id).withTypeUID(typeUID).withConfiguration(c).build();
    }

    private static void applyExtended2Fields(Configuration c, Map<String, Object> a) {
        putIfString(c, a, "icon");
        putIfString(c, a, "tag");
        putIfString(c, a, "title");
        putIfString(c, a, "referenceId");
        putIfString(c, a, "onClickAction");
        putIfString(c, a, "mediaAttachmentUrl");

        List<String> buttons = collectActionButtons(a);
        if (!buttons.isEmpty()) {
            c.put("actionButton1", buttons.get(0));
        }
        if (buttons.size() >= 2) {
            c.put("actionButton2", buttons.get(1));
        }
        if (buttons.size() >= 3) {
            c.put("actionButton3", buttons.get(2));
        }
    }

    /**
     * Collects action button strings in the cloud's {@code "Label=action"} format, preferring the
     * structured {@code actionButtons} array when present and falling back to the raw
     * {@code actionButton1/2/3} string fields. Max 3 buttons (cloud limit).
     */
    private static List<String> collectActionButtons(Map<String, Object> a) {
        List<String> out = new ArrayList<>();
        Object arr = a.get("actionButtons");
        if (arr instanceof List<?> list) {
            for (Object item : list) {
                if (out.size() >= 3) {
                    break;
                }
                if (!(item instanceof Map<?, ?> raw)) {
                    throw new IllegalArgumentException(
                            "Each actionButtons entry must be an object with 'label' and 'action'.");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> btn = (Map<String, Object>) raw;
                String label = requireString(btn, "label", "actionButtons entry");
                String action = requireString(btn, "action", "actionButtons entry");
                if (label.contains("=")) {
                    throw new IllegalArgumentException(
                            "actionButtons label must not contain '=' (got '" + label + "').");
                }
                out.add(label + "=" + action);
            }
            return out;
        }
        // Fallback: raw "Label=action" strings (back-compat / power users)
        for (String key : List.of("actionButton1", "actionButton2", "actionButton3")) {
            Object v = a.get(key);
            if (v instanceof String s && !s.isBlank()) {
                out.add(s);
            }
        }
        return out;
    }

    private List<Condition> parseConditions(@Nullable Object conditionsObj) {
        List<Condition> conditions = new ArrayList<>();
        if (conditionsObj == null) {
            return conditions;
        }
        if (!(conditionsObj instanceof List<?> list)) {
            throw new IllegalArgumentException("'conditions' must be an array.");
        }
        int idx = 0;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> raw)) {
                throw new IllegalArgumentException("Each condition must be an object.");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> c = (Map<String, Object>) raw;
            String type = c.get("type") instanceof String s ? s : null;
            if (type == null) {
                throw new IllegalArgumentException("Each condition requires a 'type' field.");
            }
            conditions.add(buildCondition(type, c, "condition" + idx));
            idx++;
        }
        return conditions;
    }

    private Condition buildCondition(String type, Map<String, Object> c, String id) {
        return switch (type) {
            case "item_state" -> {
                Configuration cfg = new Configuration();
                cfg.put("itemName", requireString(c, "itemName", "item_state"));
                Object op = c.get("operator");
                cfg.put("operator", op instanceof String s && !s.isBlank() ? s : "=");
                cfg.put("state", requireString(c, "state", "item_state"));
                yield ModuleBuilder.createCondition().withId(id).withTypeUID("core.ItemStateCondition")
                        .withConfiguration(cfg).build();
            }
            case "time_of_day" -> {
                Configuration cfg = new Configuration();
                cfg.put("startTime", requireString(c, "startTime", "time_of_day"));
                cfg.put("endTime", requireString(c, "endTime", "time_of_day"));
                yield ModuleBuilder.createCondition().withId(id).withTypeUID("core.TimeOfDayCondition")
                        .withConfiguration(cfg).build();
            }
            case "day_of_week" -> {
                Configuration cfg = new Configuration();
                cfg.put("days", requireStringList(c, "days", "day_of_week"));
                yield ModuleBuilder.createCondition().withId(id).withTypeUID("timer.DayOfWeekCondition")
                        .withConfiguration(cfg).build();
            }
            case "ephemeris" -> buildEphemerisCondition(c, id);
            case "script" -> {
                requireScriptingEnabled("Script conditions");
                Configuration cfg = new Configuration();
                cfg.put("type", JS_MIME_TYPE);
                cfg.put("script", requireString(c, "script", "script"));
                yield ModuleBuilder.createCondition().withId(id).withTypeUID("script.ScriptCondition")
                        .withConfiguration(cfg).build();
            }
            default -> throw new IllegalArgumentException("Unknown condition type '" + type
                    + "'. Allowed: item_state, time_of_day, day_of_week, ephemeris, script.");
        };
    }

    private static Condition buildEphemerisCondition(Map<String, Object> c, String id) {
        String kind = c.get("kind") instanceof String s ? s.toLowerCase(Locale.ROOT) : "weekend";
        Configuration cfg = new Configuration();
        Object offset = c.get("offset");
        if (offset instanceof Number n) {
            cfg.put("offset", n.intValue());
        }
        String typeUID = switch (kind) {
            case "weekend" -> "ephemeris.WeekendCondition";
            case "weekday" -> "ephemeris.WeekdayCondition";
            case "holiday" -> "ephemeris.HolidayCondition";
            case "not_holiday" -> "ephemeris.NotHolidayCondition";
            case "dayset" -> {
                cfg.put("dayset", requireString(c, "dayset", "ephemeris (kind=dayset)"));
                yield "ephemeris.DaysetCondition";
            }
            default -> throw new IllegalArgumentException(
                    "ephemeris.kind must be 'weekend', 'weekday', 'holiday', 'not_holiday', or 'dayset' (got '" + kind
                            + "').");
        };
        return ModuleBuilder.createCondition().withId(id).withTypeUID(typeUID).withConfiguration(cfg).build();
    }

    private Map<String, Object> triggerProps() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("type",
                Map.of("type", "string", "description",
                        "Trigger type. 'datetime' = one-time, others recurring/event-driven.", "enum",
                        List.of("datetime", "time_of_day", "cron", "item_state_change", "item_command")));
        p.put("datetime", Map.of("type", "string", "description", """
                When to fire (one-shot). Accepts either an absolute ISO-8601 local date-time \
                (e.g. '2026-04-17T15:00:00') OR a relative offset that the server resolves against ITS OWN clock — \
                prefer the relative form for 'in N seconds/minutes' requests so you don't have to fetch the server \
                time first. Relative forms: simple offset like '+30s', '+5m', '+2h', '+1d', '+1w' (negative ok); \
                ISO time duration like 'PT30M', 'PT2H', 'PT1H30M'; ISO date period like 'P1D', 'P2W'."""));
        p.put("time", Map.of("type", "string", "description", "HH:MM time for 'time_of_day' trigger"));
        p.put("cronExpression", Map.of("type", "string", "description", "Cron expression for 'cron' trigger"));
        p.put("itemName", Map.of("type", "string", "description",
                "Item name for 'item_state_change' or 'item_command' triggers"));
        p.put("state", Map.of("type", "string", "description", "Only trigger when item changes TO this state"));
        p.put("previousState",
                Map.of("type", "string", "description", "Only trigger when item changes FROM this state"));
        p.put("command", Map.of("type", "string", "description", "Only trigger on this specific command"));
        return p;
    }

    private Map<String, Object> actionProps() {
        Map<String, Object> p = new LinkedHashMap<>();
        List<String> types = new ArrayList<>(
                List.of("item_command", "item_state_update", "notification", "run_rule", "rule_enablement"));
        if (scriptingEnabled) {
            types.add("script");
        }
        p.put("type", Map.of("type", "string", "description", "Action type discriminator.", "enum", types));
        p.put("itemName",
                Map.of("type", "string", "description", "Item to act on (for 'item_command' or 'item_state_update')."));
        p.put("command",
                Map.of("type", "string", "description", "Command to send (for 'item_command', e.g. ON, OFF, 0-100)."));
        p.put("state",
                Map.of("type", "string", "description", "State to set (for 'item_state_update', e.g. ON, 42.5)."));
        p.put("scope", Map.of("type", "string", "description",
                "Notification scope: 'user' (default, requires userId), 'broadcast' (all users/devices), or 'log' (cloud log only).",
                "enum", List.of("user", "broadcast", "log")));
        p.put("userId", Map.of("type", "string", "description", "Cloud user id for 'user' scope notifications."));
        p.put("message", Map.of("type", "string", "description", "Notification body."));
        p.put("title", Map.of("type", "string", "description", "Optional notification title."));
        p.put("icon", Map.of("type", "string", "description", "Optional notification icon name."));
        p.put("tag",
                Map.of("type", "string", "description", "Optional notification tag (severity for legacy clients)."));
        p.put("severity", Map.of("type", "string", "description", "Severity for 'log' scope notifications."));
        p.put("referenceId", Map.of("type", "string", "description", """
                Optional reference id. A later notification with the same referenceId REPLACES this one on the device \
                instead of stacking — use it for periodic alerts (battery digests, leak alerts) so the user sees the \
                latest state, not a pile of duplicates."""));
        p.put("onClickAction", Map.of("type", "string", "description", """
                Action invoked when the notification body is tapped. Uses the same syntax as actionButtons.action \
                below (e.g. 'command:Office_Light:OFF', 'ui:navigate:/page/cameras', 'https://example.com', \
                'rule:my-rule-uid')."""));
        p.put("mediaAttachmentUrl", Map.of("type", "string", "description", """
                Optional image attachment. Accepts a full http(s):// URL reachable by the device, an absolute path \
                on this openHAB instance starting with '/', or 'item:MyImageItem' for an Image-typed item (use this \
                for local-network camera snapshots)."""));
        p.put("actionButtons", Map.of("type", "array", "maxItems", 3, "description",
                """
                        Up to 3 tap-able buttons on the notification. STRONGLY RECOMMENDED whenever the notification is \
                        about something the user might want to act on, even if they did NOT explicitly ask for buttons. \
                        CRITICAL: do not restrict buttons to the literal verb in the user's request — think about what the \
                        user will plausibly want to do WHEN THE NOTIFICATION FIRES, which often includes the opposite \
                        action or a 'view details' navigation. A reminder to turn the lights ON should still offer \
                        'Turn off' alongside 'Turn on' (plans change between scheduling and firing). Examples by \
                        notification type: REMINDER ('remind me to turn on office lights') -> \
                        {'Turn on', 'command:OfficeLight:ON'} AND {'Turn off', 'command:OfficeLight:OFF'}; \
                        ALERT 'lights still on at 10pm' -> {'Turn off', 'command:OfficeLight:OFF'} AND \
                        {'Leave on', 'ui:popup:oh-light-card'}; \
                        'garage left open' -> {'Close', 'command:GarageDoor:DOWN'} AND {'Ignore', 'ui:navigate:/page/garage'}; \
                        'doorbell' -> {'Unlock', 'command:FrontDoor_Lock:OFF'} AND {'View camera', 'ui:navigate:/page/doorbell'}; \
                        'leak detected' -> {'Acknowledge', 'ui:navigate:/page/plumbing'}. \
                        BAD: a 'remind me to lock the door' notification with only a 'Lock' button (missing 'View door' or \
                        'Unlock by mistake' recovery). GOOD: same notification with 'Lock' + 'View door'. \
                        Skip buttons only for purely informational alerts (weather, daily digests, log-scope notifications) \
                        where there is truly nothing to do.""",
                "items",
                Map.of("type", "object", "properties", Map.of("label",
                        Map.of("type", "string", "description", "Button label shown to the user (no '=' character)."),
                        "action",
                        Map.of("type", "string", "description",
                                """
                                        What the button does. Action syntax: \
                                        'command:ItemName:COMMAND' (send item command, e.g. 'command:Light:ON', 'command:Blind:50'); \
                                        'ui:navigate:/page/PAGE_ID' or 'ui:popup:WIDGET_ID' or 'ui:/absolute/path' (UI navigation); \
                                        'http://...' or 'https://...' (open embedded browser); \
                                        'rule:RULE_UID' (run a rule, iOS only); \
                                        'app:android=com.x.y,ios=scheme://' (open native app, iOS only).""")),
                        "required", List.of("label", "action"))));
        p.put("actionButton1", Map.of("type", "string", "description",
                "Raw 'Label=action' string (advanced; prefer the structured actionButtons array)."));
        p.put("actionButton2", Map.of("type", "string", "description",
                "Raw 'Label=action' string (advanced; prefer the structured actionButtons array)."));
        p.put("actionButton3", Map.of("type", "string", "description",
                "Raw 'Label=action' string (advanced; prefer the structured actionButtons array)."));
        p.put("ruleUIDs", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Rule UIDs to run or enable/disable (for 'run_rule' or 'rule_enablement')."));
        p.put("considerConditions", Map.of("type", "boolean", "description",
                "For 'run_rule': whether to honor the target rule's conditions (default true)."));
        p.put("enable", Map.of("type", "boolean", "description",
                "For 'rule_enablement': true to enable, false to disable the listed rules."));
        if (scriptingEnabled) {
            p.put("script", Map.of("type", "string", "description",
                    "JavaScript source for 'script' action. Has access to openhab-js globals (items, actions, things, rules, etc.). Test with execute_script first."));
        }
        return p;
    }

    private Map<String, Object> conditionProps() {
        Map<String, Object> p = new LinkedHashMap<>();
        List<String> types = new ArrayList<>(List.of("item_state", "time_of_day", "day_of_week", "ephemeris"));
        if (scriptingEnabled) {
            types.add("script");
        }
        p.put("type", Map.of("type", "string", "description", "Condition type discriminator.", "enum", types));
        p.put("itemName", Map.of("type", "string", "description", "Item to check (for 'item_state')."));
        p.put("operator", Map.of("type", "string", "description", "Comparison operator (default '=').", "enum",
                List.of("=", "!=", "<", "<=", ">", ">=")));
        p.put("state", Map.of("type", "string", "description", "State to compare against (for 'item_state')."));
        p.put("startTime", Map.of("type", "string", "description", "Start time HH:MM inclusive (for 'time_of_day')."));
        p.put("endTime", Map.of("type", "string", "description", "End time HH:MM exclusive (for 'time_of_day')."));
        p.put("days",
                Map.of("type", "array", "items",
                        Map.of("type", "string", "enum",
                                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")),
                        "description", "Days of week the condition matches (for 'day_of_week')."));
        p.put("kind",
                Map.of("type", "string", "description",
                        "Ephemeris kind (default 'weekend'). 'dayset' requires the 'dayset' field.", "enum",
                        List.of("weekend", "weekday", "holiday", "not_holiday", "dayset")));
        p.put("dayset", Map.of("type", "string", "description", "Named dayset (for ephemeris kind='dayset')."));
        p.put("offset", Map.of("type", "integer", "description",
                "Optional day offset (+1=tomorrow, -1=yesterday) for ephemeris conditions."));
        if (scriptingEnabled) {
            p.put("script", Map.of("type", "string", "description",
                    "JavaScript source for 'script' condition. Last expression must be truthy to pass."));
        }
        return p;
    }

    private String actionsDescription() {
        String types = """
                List of actions to perform. Each action has a 'type': \
                'item_command' (send command to item), 'item_state_update' (update item state without commanding), \
                'notification' (openHAB Cloud push - requires openhabcloud add-on; pick scope 'user'/'broadcast'/'log'), \
                'run_rule' (execute other rules by UID), 'rule_enablement' (enable/disable other rules by UID)""";
        String scriptClause = scriptingEnabled
                ? ", 'script' (run a JavaScript snippet - use execute_script to dry-run first)"
                : "";
        String guidance = """
                . IMPORTANT — before emitting any 'notification' action, decide its actionButtons FIRST: any \
                actionable subject (reminder, alert, request, anything mentioning a device the user might want to \
                control) MUST include actionButtons covering the likely user responses. Do not anchor on the literal \
                verb in the request — a 'remind me to turn lights ON' notification still needs both 'Turn on' and \
                'Turn off' buttons (plans change between scheduling and firing). See the actionButtons field for \
                the full rules and worked examples (REMINDER, ALERT, doorbell, etc.). Only skip buttons for purely \
                informational alerts (weather, daily digests, log scope).""";
        return types + scriptClause + guidance;
    }

    private String createRuleDescription() {
        String base = """
                Create an automation rule, scheduled task, or reminder. Use this whenever the user asks to \
                'remind me to X', 'notify me later', 'in N seconds/minutes/hours', 'at TIME', 'every day at X', \
                'when X happens, do Y', or any other persistent/scheduled automation. A rule has one or more \
                triggers, optional conditions, and one or more actions. Use 'trigger' for a single trigger or \
                'triggers' for multiple. Prefer recurring triggers ('time_of_day', 'cron', 'item_state_change', \
                'item_command') for persistent automations. Use 'datetime' (one-time, auto-deleted after firing) \
                for one-shots and reminders. For RELATIVE one-shots ('in 30 seconds', 'in 2 hours'), pass a \
                relative offset like '+30s' or 'PT2H' as trigger.datetime — the server resolves it against its \
                OWN clock, so you don't need to fetch the time first. Use absolute ISO datetimes \
                ('2026-04-17T15:00:00') only when the user names a specific date. \
                Actions can be item commands, item state updates, notifications, or rule orchestration""";
        String scriptClause = scriptingEnabled
                ? ", or JavaScript snippets (dry-run with execute_script first to catch errors before scheduling)"
                : "";
        String tail = """
                . Created rules are tagged 'MCP' — list with get_rules(tag='MCP'), remove with \
                manage_rule(action='remove').""";
        return base + scriptClause + tail;
    }

    private void requireScriptingEnabled(String feature) {
        if (!scriptingEnabled) {
            throw new IllegalArgumentException(feature
                    + " are disabled. Set the 'enableScripting' option on the MCP server config (io:mcp) to enable, "
                    + "and ensure the openhab-automation-jsscripting add-on is installed.");
        }
    }

    private static String requireString(Map<String, Object> map, String key, String context) {
        Object v = map.get(key);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        throw new IllegalArgumentException("'" + key + "' (string) is required for " + context + ".");
    }

    private static List<String> requireStringList(Map<String, Object> map, String key, String context) {
        Object v = map.get(key);
        if (v instanceof List<?> list && !list.isEmpty()) {
            List<String> out = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s && !s.isBlank()) {
                    out.add(s);
                }
            }
            if (!out.isEmpty()) {
                return out;
            }
        }
        throw new IllegalArgumentException(
                "'" + key + "' (non-empty array of strings) is required for " + context + ".");
    }

    private static void putIfString(Configuration config, Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof String s && !s.isBlank()) {
            config.put(key, s);
        }
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
