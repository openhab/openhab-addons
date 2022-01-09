/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.automation.util.RuleBuilder;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.HueEmulationService;
import org.openhab.io.hueemulation.internal.NetworkUtils;
import org.openhab.io.hueemulation.internal.RuleUtils;
import org.openhab.io.hueemulation.internal.dto.HueRuleEntry;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueCommand;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessGeneric;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Handles Hue rules via the automation subsystem and the corresponding REST interface
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = false, service = Rules.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + HueEmulationService.REST_APP_NAME + ")")
@NonNullByDefault
@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Rules implements RegistryChangeListener<Rule> {
    public static final String RULES_TAG = "hueemulation_rule";

    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) UserManagement userManagement;
    @Reference
    protected @NonNullByDefault({}) RuleRegistry ruleRegistry;
    @Reference
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;

    /**
     * Registers to the {@link RuleRegistry} and enumerates currently existing rules.
     */
    @Activate
    public void activate() {
        ruleRegistry.removeRegistryChangeListener(this);
        ruleRegistry.addRegistryChangeListener(this);

        for (Rule item : ruleRegistry.getAll()) {
            added(item);
        }
    }

    @Deactivate
    public void deactivate() {
        ruleRegistry.removeRegistryChangeListener(this);
    }

    @Override
    public void added(Rule rule) {
        if (!rule.getTags().contains(RULES_TAG)) {
            return;
        }
        HueRuleEntry entry = new HueRuleEntry(rule.getName());
        String desc = rule.getDescription();
        if (desc != null) {
            entry.description = desc;
        }

        rule.getConditions().stream().filter(c -> c.getTypeUID().equals("hue.ruleCondition")).forEach(c -> {
            HueRuleEntry.Condition condition = c.getConfiguration().as(HueRuleEntry.Condition.class);
            // address with pattern "/sensors/2/state/buttonevent"
            String[] parts = condition.address.split("/");
            if (parts.length < 3) {
                return;
            }

            entry.conditions.add(condition);
        });

        rule.getActions().stream().filter(a -> a.getTypeUID().equals("rules.HttpAction")).forEach(a -> {
            HueCommand command = RuleUtils.httpActionToHueCommand(cs.ds, a, rule.getName());
            if (command == null) {
                return;
            }
            // Remove the "/api/{user}" part
            String[] parts = command.address.split("/");
            command.address = "/" + String.join("/", Arrays.copyOfRange(parts, 3, parts.length));
            entry.actions.add(command);
        });

        cs.ds.rules.put(rule.getUID(), entry);
    }

    @Override
    public void removed(Rule element) {
        cs.ds.rules.remove(element.getUID());
    }

    @Override
    public void updated(Rule oldElement, Rule element) {
        removed(oldElement);
        added(element);
    }

    protected static Map.Entry<Trigger, Condition> hueConditionToAutomation(String id, HueRuleEntry.Condition condition,
            ItemRegistry itemRegistry) {
        // pattern: "/sensors/2/state/buttonevent"
        String[] parts = condition.address.split("/");
        if (parts.length < 3) {
            throw new IllegalStateException("Condition address invalid: " + condition.address);
        }

        final Configuration triggerConfig = new Configuration();

        String itemName = parts[2];

        Item item = itemRegistry.get(itemName);
        if (item == null) {
            throw new IllegalStateException("Item of address does not exist: " + itemName);
        }

        triggerConfig.put("itemName", itemName);

        // There might be multiple triggers for the same item. Due to the map, we are only creating one though

        Trigger trigger = ModuleBuilder.createTrigger().withId(id).withTypeUID("core.ItemStateChangeTrigger")
                .withConfiguration(triggerConfig).build();

        // Connect the outputs of the trigger with the inputs of the condition
        Map<String, String> inputs = new TreeMap<>();
        inputs.put("newState", id);
        inputs.put("oldState", id);

        // Config for condition
        final Configuration conditionConfig = new Configuration();
        conditionConfig.put("operator", condition.operator.name());
        conditionConfig.put("address", condition.address);
        String value = condition.value;
        if (value != null) {
            conditionConfig.put("value", value);
        }

        Condition conditon = ModuleBuilder.createCondition().withId(id + "-condition").withTypeUID("hue.ruleCondition")
                .withConfiguration(conditionConfig).withInputs(inputs).build();

        return new AbstractMap.SimpleEntry<>(trigger, conditon);
    }

    protected static RuleBuilder createHueRuleConditions(List<HueRuleEntry.Condition> hueConditions,
            RuleBuilder builder, List<Trigger> oldTriggers, List<Condition> oldConditions, ItemRegistry itemRegistry) {
        // Preserve all triggers, conditions that are not part of hue rules
        Map<String, Trigger> triggers = new TreeMap<>();
        triggers.putAll(oldTriggers.stream().filter(a -> !a.getTypeUID().equals("core.ItemStateChangeTrigger"))
                .collect(Collectors.toMap(e -> e.getId(), e -> e)));

        Map<String, Condition> conditions = new TreeMap<>();
        conditions.putAll(oldConditions.stream().filter(a -> !a.getTypeUID().equals("hue.ruleCondition"))
                .collect(Collectors.toMap(e -> e.getId(), e -> e)));

        for (HueRuleEntry.Condition condition : hueConditions) {
            String id = condition.address.replace("/", "-");
            Entry<Trigger, Condition> entry = hueConditionToAutomation(id, condition, itemRegistry);
            triggers.put(id, entry.getKey());
            conditions.put(id, entry.getValue());
        }

        builder.withTriggers(new ArrayList<>(triggers.values())).withConditions(new ArrayList<>(conditions.values()));
        return builder;
    }

    protected static List<Action> createActions(String uid, List<HueCommand> hueActions, List<Action> oldActions,
            String apikey) {
        // Preserve all actions that are not "rules.HttpAction"
        List<Action> actions = new ArrayList<>(oldActions);
        actions.removeIf(a -> a.getTypeUID().equals("rules.HttpAction"));

        for (HueCommand command : hueActions) {
            command.address = "/api/" + apikey + command.address;
            actions.add(RuleUtils.createHttpAction(command, command.address.replace("/", "-")));
        }
        return actions;
    }

    @GET
    @Path("{username}/rules")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Return all rules", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getRulesApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.rules)).build();
    }

    @GET
    @Path("{username}/rules/{id}")
    @Operation(summary = "Return a rule", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getRuleApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "rule id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.rules.get(id))).build();
    }

    @DELETE
    @Path("{username}/rules/{id}")
    @Operation(summary = "Deletes a rule", responses = {
            @ApiResponse(responseCode = "200", description = "The user got removed"),
            @ApiResponse(responseCode = "403", description = "Access denied") })
    public Response removeRuleApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "Rule to remove") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        Rule rule = ruleRegistry.remove(id);
        if (rule == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Rule does not exist!");
        }

        return NetworkUtils.singleSuccess(cs.gson, "/rules/" + id + " deleted.");
    }

    @PUT
    @Path("{username}/rules/{id}")
    @Operation(summary = "Set rule attributes", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response modifyRuleApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "rule id") String id, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        final HueRuleEntry changeRequest = cs.gson.fromJson(body, HueRuleEntry.class);

        Rule rule = ruleRegistry.remove(id);
        if (rule == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Rule does not exist!");
        }

        RuleBuilder builder = RuleBuilder.create(rule);

        String temp;

        temp = changeRequest.name;
        if (!temp.isEmpty()) {
            builder.withName(changeRequest.name);
        }

        temp = changeRequest.description;
        if (!temp.isEmpty()) {
            builder.withDescription(temp);
        }

        try {
            if (!changeRequest.actions.isEmpty()) {
                builder.withActions(createActions(rule.getUID(), changeRequest.actions, rule.getActions(), username));
            }
            if (!changeRequest.conditions.isEmpty()) {
                builder = createHueRuleConditions(changeRequest.conditions, builder, rule.getTriggers(),
                        rule.getConditions(), itemRegistry);
            }

            ruleRegistry.add(builder.build());
        } catch (IllegalStateException e) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID, e.getMessage());
        }

        return NetworkUtils.successList(cs.gson, Arrays.asList( //
                new HueSuccessGeneric(changeRequest.name, "/rules/" + id + "/name"), //
                new HueSuccessGeneric(changeRequest.description, "/rules/" + id + "/description"), //
                new HueSuccessGeneric(changeRequest.actions.toString(), "/rules/" + id + "/actions"), //
                new HueSuccessGeneric(changeRequest.conditions.toString(), "/rules/" + id + "/conditions") //
        ));
    }

    @SuppressWarnings({ "null" })
    @POST
    @Path("{username}/rules")
    @Operation(summary = "Create a new rule", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response postNewRule(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        HueRuleEntry newRuleData = cs.gson.fromJson(body, HueRuleEntry.class);
        if (newRuleData == null || newRuleData.name.isEmpty() || newRuleData.actions.isEmpty()
                || newRuleData.conditions.isEmpty()) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.INVALID_JSON,
                    "Invalid request: No name or actions or conditons!");
        }

        String uid = UUID.randomUUID().toString();
        RuleBuilder builder = RuleBuilder.create(uid).withName(newRuleData.name);

        String description = newRuleData.description;
        if (description != null) {
            builder.withDescription(description);
        }

        try {
            builder.withActions(createActions(uid, newRuleData.actions, Collections.emptyList(), username));
            builder = createHueRuleConditions(newRuleData.conditions, builder, Collections.emptyList(),
                    Collections.emptyList(), itemRegistry);
            ruleRegistry.add(builder.withTags(RULES_TAG).build());
        } catch (IllegalStateException e) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID, e.getMessage());
        }

        return NetworkUtils.singleSuccess(cs.gson, uid, "id");
    }
}
