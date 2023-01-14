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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.automation.util.RuleBuilder;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.HueEmulationService;
import org.openhab.io.hueemulation.internal.NetworkUtils;
import org.openhab.io.hueemulation.internal.RuleUtils;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueScheduleEntry;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueChangeScheduleEntry;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Enables the schedule part of the Hue REST API. Uses automation rules with GenericCronTrigger, TimerTrigger and
 * AbsoluteDateTimeTrigger depending on the schedule time pattern.
 * <p>
 * If the scheduled task should remove itself after completion, a RemoveRuleAction is used in the rule.
 * <p>
 * The actual command execution uses HttpAction.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = false, service = Schedules.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + HueEmulationService.REST_APP_NAME + ")")
@NonNullByDefault
@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Schedules implements RegistryChangeListener<Rule> {
    public static final String SCHEDULE_TAG = "hueemulation_schedule";
    private final Logger logger = LoggerFactory.getLogger(Schedules.class);

    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) UserManagement userManagement;

    @Reference
    protected @NonNullByDefault({}) RuleManager ruleManager;
    @Reference
    protected @NonNullByDefault({}) RuleRegistry ruleRegistry;

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

    /**
     * Called by the registry when a rule got added (and when a rule got modified).
     * <p>
     * Converts the rule into a {@link HueScheduleEntry} object and add that to the hue datastore.
     */
    @Override
    public void added(Rule rule) {
        if (!rule.getTags().contains(SCHEDULE_TAG)) {
            return;
        }
        HueScheduleEntry entry = new HueScheduleEntry();
        entry.name = rule.getName();
        entry.description = rule.getDescription();
        entry.autodelete = rule.getActions().stream().anyMatch(p -> p.getId().equals("autodelete"));
        entry.status = ruleManager.isEnabled(rule.getUID()) ? "enabled" : "disabled";

        String timeStringFromTrigger = RuleUtils.timeStringFromTrigger(rule.getTriggers());
        if (timeStringFromTrigger == null) {
            logger.warn("Schedule from rule '{}' invalid!", rule.getName());
            return;
        }

        entry.localtime = timeStringFromTrigger;

        for (Action a : rule.getActions()) {
            if (!a.getTypeUID().equals("rules.HttpAction")) {
                continue;
            }
            HueCommand command = RuleUtils.httpActionToHueCommand(cs.ds, a, rule.getName());
            if (command == null) {
                continue;
            }
            entry.command = command;
        }

        cs.ds.schedules.put(rule.getUID(), entry);
    }

    @Override
    public void removed(Rule element) {
        cs.ds.schedules.remove(element.getUID());
    }

    @Override
    public void updated(Rule oldElement, Rule element) {
        removed(oldElement);
        added(element);
    }

    /**
     * Creates a new rule that executes a http rule action, triggered by the scheduled time
     *
     * @param uid A rule unique id.
     * @param builder A rule builder that will be used for creating the rule. It must have been created with the given
     *            uid.
     * @param oldActions Old actions. Useful if `data` is only partially set and old actions should be preserved
     * @param data The configuration for the http action and trigger time is in here
     * @return A new rule with the given uid
     * @throws IllegalStateException If a required parameter is not set or if a light / group that is referred to is not
     *             existing
     */
    protected static Rule createRule(String uid, RuleBuilder builder, List<Action> oldActions,
            List<Trigger> oldTriggers, HueChangeScheduleEntry data, HueDataStore ds) throws IllegalStateException {
        HueCommand command = data.command;
        Boolean autodelete = data.autodelete;

        String temp;

        temp = data.name;
        if (temp != null) {
            builder.withName(temp);
        } else if (oldActions.isEmpty()) { // This is a new rule without a name yet
            throw new IllegalStateException("Name not set!");
        }

        temp = data.description;
        if (temp != null) {
            builder.withDescription(temp);
        }

        temp = data.localtime;
        if (temp != null) {
            builder.withTriggers(RuleUtils.createTriggerForTimeString(temp));
        } else if (oldTriggers.isEmpty()) { // This is a new rule without triggers yet
            throw new IllegalStateException("localtime not set!");
        }

        List<Action> actions = new ArrayList<>(oldActions);

        if (command != null) {
            RuleUtils.validateHueHttpAddress(ds, command.address);
            actions.removeIf(a -> a.getId().equals("command")); // Remove old command action if any and add new one
            actions.add(RuleUtils.createHttpAction(command, "command"));
        } else if (oldActions.isEmpty()) { // This is a new rule without an action yet
            throw new IllegalStateException("No command set!");
        }

        if (autodelete != null) {
            // Remove action to remove rule after execution
            actions = actions.stream().filter(e -> !e.getId().equals("autodelete"))
                    .collect(Collectors.toCollection(() -> new ArrayList<>()));
            if (autodelete) { // Add action to remove this rule again after execution
                final Configuration actionConfig = new Configuration();
                actionConfig.put("removeuid", uid);
                actions.add(ModuleBuilder.createAction().withId("autodelete").withTypeUID("rules.RemoveRuleAction")
                        .withConfiguration(actionConfig).build());
            }
        }

        builder.withActions(actions);

        return builder.withVisibility(Visibility.VISIBLE).withTags(SCHEDULE_TAG).build();
    }

    @GET
    @Path("{username}/schedules")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Return all schedules", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getSchedulesApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.schedules)).build();
    }

    @GET
    @Path("{username}/schedules/{id}")
    @Operation(summary = "Return a schedule", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getScheduleApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "schedule id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.schedules.get(id))).build();
    }

    @DELETE
    @Path("{username}/schedules/{id}")
    @Operation(summary = "Deletes a schedule", responses = {
            @ApiResponse(responseCode = "200", description = "The user got removed"),
            @ApiResponse(responseCode = "403", description = "Access denied") })
    public Response removeScheduleApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "Schedule to remove") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        Rule rule = ruleRegistry.remove(id);
        if (rule == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Schedule does not exist!");
        }

        return NetworkUtils.singleSuccess(cs.gson, "/schedules/" + id + " deleted.");
    }

    @PUT
    @Path("{username}/schedules/{id}")
    @Operation(summary = "Set schedule attributes", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response modifyScheduleApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "schedule id") String id, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        final HueChangeScheduleEntry changeRequest = Objects
                .requireNonNull(cs.gson.fromJson(body, HueChangeScheduleEntry.class));

        Rule rule = ruleRegistry.remove(id);
        if (rule == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Schedule does not exist!");
        }

        RuleBuilder builder = RuleBuilder.create(rule);

        try {
            ruleRegistry.add(
                    createRule(rule.getUID(), builder, rule.getActions(), rule.getTriggers(), changeRequest, cs.ds));
        } catch (IllegalStateException e) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID, e.getMessage());
        }

        return NetworkUtils.successList(cs.gson, Arrays.asList( //
                new HueSuccessGeneric(changeRequest.name, "/schedules/" + id + "/name"), //
                new HueSuccessGeneric(changeRequest.description, "/schedules/" + id + "/description"), //
                new HueSuccessGeneric(changeRequest.localtime, "/schedules/" + id + "/localtime"), //
                new HueSuccessGeneric(changeRequest.status, "/schedules/" + id + "/status"), //
                new HueSuccessGeneric(changeRequest.autodelete, "/schedules/1/autodelete"), //
                new HueSuccessGeneric(changeRequest.command, "/schedules/1/command") //
        ));
    }

    @SuppressWarnings({ "null" })
    @POST
    @Path("{username}/schedules")
    @Operation(summary = "Create a new schedule", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response postNewSchedule(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        HueScheduleEntry newScheduleData = cs.gson.fromJson(body, HueScheduleEntry.class);
        if (newScheduleData == null || newScheduleData.name.isEmpty() || newScheduleData.localtime.isEmpty()) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.INVALID_JSON,
                    "Invalid request: No name or localtime!");
        }

        String uid = UUID.randomUUID().toString();
        RuleBuilder builder = RuleBuilder.create(uid);

        Rule rule;
        try {
            rule = createRule(uid, builder, Collections.emptyList(), Collections.emptyList(), newScheduleData, cs.ds);
        } catch (IllegalStateException e) { // No stacktrace required, we just need the exception message
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID, e.getMessage());
        }

        ruleRegistry.add(rule);

        return NetworkUtils.singleSuccess(cs.gson, uid, "id");
    }
}
