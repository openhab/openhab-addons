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
import java.util.Map;
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
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.automation.util.RuleBuilder;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.HueEmulationService;
import org.openhab.io.hueemulation.internal.NetworkUtils;
import org.openhab.io.hueemulation.internal.StateUtils;
import org.openhab.io.hueemulation.internal.automation.dto.ItemCommandActionConfig;
import org.openhab.io.hueemulation.internal.dto.AbstractHueState;
import org.openhab.io.hueemulation.internal.dto.HueSceneEntry;
import org.openhab.io.hueemulation.internal.dto.HueSceneWithLightstates;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueChangeSceneEntry;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueStateChange;
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
 * Handles Hue scenes via the automation subsystem and the corresponding REST interface
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = false, service = Scenes.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + HueEmulationService.REST_APP_NAME + ")")
@NonNullByDefault
@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Scenes implements RegistryChangeListener<Rule> {
    private final Logger logger = LoggerFactory.getLogger(Scenes.class);

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
    public void added(Rule scene) {
        if (!scene.getTags().contains("scene")) {
            return;
        }
        HueSceneEntry entry = new HueSceneEntry(scene.getName());
        String desc = scene.getDescription();
        if (desc != null) {
            entry.description = desc;
        }

        List<String> items = new ArrayList<>();

        for (Action a : scene.getActions()) {
            if (!a.getTypeUID().equals("core.ItemCommandAction")) {
                continue;
            }
            ItemCommandActionConfig config = a.getConfiguration().as(ItemCommandActionConfig.class);
            Item item;
            try {
                item = itemRegistry.getItem(config.itemName);
            } catch (ItemNotFoundException e) {
                logger.warn("Rule {} is referring to a non existing item {}", scene.getName(), config.itemName);
                continue;
            }
            if (scene.getActions().size() == 1 && item instanceof GroupItem) {
                entry.type = HueSceneEntry.TypeEnum.GroupScene;
                entry.group = cs.mapItemUIDtoHueID(item);
            } else {
                items.add(cs.mapItemUIDtoHueID(item));
            }
        }

        if (!items.isEmpty()) {
            entry.lights = items;
        }

        cs.ds.scenes.put(scene.getUID(), entry);
    }

    @Override
    public void removed(Rule element) {
        cs.ds.scenes.remove(element.getUID());
    }

    @Override
    public void updated(Rule oldElement, Rule element) {
        removed(oldElement);
        added(element);
    }

    @GET
    @Path("{username}/scenes")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Return all scenes", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getScenesApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.scenes)).build();
    }

    @SuppressWarnings({ "unused", "null" })
    @GET
    @Path("{username}/scenes/{id}")
    @Operation(summary = "Return a scene", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getSceneApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "scene id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        HueSceneEntry sceneEntry = cs.ds.scenes.get(id);
        if (sceneEntry == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Scene does not exist!");
        }
        HueSceneWithLightstates s = new HueSceneWithLightstates(sceneEntry);
        for (String itemID : s.lights) {
            Item item;
            try {
                item = itemRegistry.getItem(itemID);
            } catch (ItemNotFoundException e) {
                logger.warn("Scene {} is referring to a non existing item {}", sceneEntry.name, itemID);
                continue;
            }
            AbstractHueState state = StateUtils.colorStateFromItemState(item.getState(), null);
            s.lightstates.put(cs.mapItemUIDtoHueID(item), state);
        }

        return Response.ok(cs.gson.toJson(s)).build();
    }

    @DELETE
    @Path("{username}/scenes/{id}")
    @Operation(summary = "Deletes a scene", responses = {
            @ApiResponse(responseCode = "200", description = "The user got removed"),
            @ApiResponse(responseCode = "403", description = "Access denied") })
    public Response removeSceneApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "Scene to remove") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        Rule rule = ruleRegistry.remove(id);
        if (rule == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Scene does not exist!");
        }

        return NetworkUtils.singleSuccess(cs.gson, "/scenes/" + id + " deleted.");
    }

    protected static Action actionFromState(String itemID, State state) {
        final Configuration actionConfig = new Configuration();
        actionConfig.put("itemName", itemID);
        actionConfig.put("command", StateUtils.commandByItemState(state).toFullString());
        return ModuleBuilder.createAction().withId(itemID).withTypeUID("core.ItemCommandAction")
                .withConfiguration(actionConfig).build();
    }

    protected static Action actionFromState(String itemID, Command command) {
        final Configuration actionConfig = new Configuration();
        actionConfig.put("itemName", itemID);
        actionConfig.put("command", command.toFullString());
        return ModuleBuilder.createAction().withId(itemID).withTypeUID("core.ItemCommandAction")
                .withConfiguration(actionConfig).build();
    }

    /**
     * Either assigns a new name, description, lights to a scene or directly assign
     * a new light state for an entry to a scene
     */
    @PUT
    @Path("{username}/scenes/{id}")
    @Operation(summary = "Set scene attributes", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response modifySceneApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "scene id") String id, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        final HueChangeSceneEntry changeRequest = cs.gson.fromJson(body, HueChangeSceneEntry.class);

        Rule rule = ruleRegistry.remove(id);
        if (rule == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Scene does not exist!");
        }

        RuleBuilder builder = RuleBuilder.create(rule);

        String temp = changeRequest.name;
        if (temp != null) {
            builder.withName(temp);
        }
        temp = changeRequest.description;
        if (temp != null) {
            builder.withDescription(temp);
        }

        List<String> lights = changeRequest.lights;
        if (changeRequest.storelightstate && lights != null) {
            @SuppressWarnings("null")
            @NonNullByDefault({})
            List<Action> actions = lights.stream().map(itemID -> itemRegistry.get(itemID)).filter(Objects::nonNull)
                    .map(item -> actionFromState(item.getUID(), item.getState())).collect(Collectors.toList());
            builder.withActions(actions);
        }
        Map<String, HueStateChange> lightStates = changeRequest.lightstates;
        if (changeRequest.storelightstate && lightStates != null) {
            List<Action> actions = new ArrayList<>(rule.getActions());
            for (Map.Entry<String, HueStateChange> entry : lightStates.entrySet()) {
                // Remove existing action
                actions.removeIf(action -> action.getId().equals(entry.getKey()));
                // Assign new action
                Command command = StateUtils.computeCommandByChangeRequest(entry.getValue());
                if (command == null) {
                    logger.warn("Failed to compute command for {}", body);
                    return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Cannot compute command!");
                }
                actions.add(actionFromState(entry.getKey(), command));
            }
            builder.withActions(actions);
        }

        try {
            ruleRegistry.add(builder.build());
        } catch (IllegalStateException e) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID, e.getMessage());
        }

        List<String> lightsList = changeRequest.lights;
        return NetworkUtils.successList(cs.gson, Arrays.asList( //
                new HueSuccessGeneric(changeRequest.name, "/scenes/" + id + "/name"), //
                new HueSuccessGeneric(changeRequest.description, "/scenes/" + id + "/description"), //
                new HueSuccessGeneric(lightsList != null ? String.join(",", lightsList) : null,
                        "/scenes/" + id + "/lights") //
        ));
    }

    @SuppressWarnings({ "null" })
    @POST
    @Path("{username}/scenes")
    @Operation(summary = "Create a new scene", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response postNewScene(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        HueSceneEntry newScheduleData = cs.gson.fromJson(body, HueSceneEntry.class);
        if (newScheduleData == null || newScheduleData.name.isEmpty()) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.INVALID_JSON,
                    "Invalid request: No name or localtime!");
        }

        String uid = UUID.randomUUID().toString();
        RuleBuilder builder = RuleBuilder.create(uid).withName(newScheduleData.name).withTags("scene");

        if (!newScheduleData.description.isEmpty()) {
            builder.withDescription(newScheduleData.description);
        }

        List<String> lights = newScheduleData.lights;
        if (lights != null) {
            List<Action> actions = new ArrayList<>();
            for (String itemID : lights) {
                Item item = itemRegistry.get(itemID);
                if (item == null) {
                    continue;
                }
                actions.add(actionFromState(cs.mapItemUIDtoHueID(item), item.getState()));
            }
            builder.withActions(actions);
        }
        String groupid = newScheduleData.group;
        if (groupid != null) {
            Item groupItem = itemRegistry.get(groupid);
            if (groupItem == null) {
                return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID, "Group does not exist!");
            }
            List<Action> actions = Collections
                    .singletonList(actionFromState(cs.mapItemUIDtoHueID(groupItem), groupItem.getState()));
            builder.withActions(actions);
        }

        try {
            ruleRegistry.add(builder.build());
        } catch (IllegalStateException e) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID, e.getMessage());
        }

        return NetworkUtils.singleSuccess(cs.gson, uid, "id");
    }

    @PUT
    @Path("{username}/scenes/{id}/lightstates/{lightid}")
    @Operation(summary = "Set scene attributes", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response modifySceneLightStateApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "scene id") String id,
            @PathParam("lightid") @Parameter(description = "light id") String lightid, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        final HueStateChange changeRequest = Objects.requireNonNull(cs.gson.fromJson(body, HueStateChange.class));

        Rule rule = ruleRegistry.remove(id);
        if (rule == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Scene does not exist!");
        }

        RuleBuilder builder = RuleBuilder.create(rule);

        List<Action> actions = new ArrayList<>(rule.getActions());
        // Remove existing action
        actions.removeIf(action -> action.getId().equals(lightid));
        // Assign new action
        Command command = StateUtils.computeCommandByChangeRequest(changeRequest);
        if (command == null) {
            logger.warn("Failed to compute command for {}", body);
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Cannot compute command!");
        }

        actions.add(actionFromState(lightid, command));

        builder.withActions(actions);

        try {
            ruleRegistry.add(builder.build());
        } catch (IllegalStateException e) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID, e.getMessage());
        }

        return NetworkUtils.successList(cs.gson, Arrays.asList( //
                new HueSuccessGeneric(changeRequest.on, "/scenes/" + id + "/lightstates/" + lightid + "/on"), //
                new HueSuccessGeneric(changeRequest.hue, "/scenes/" + id + "/lightstates/" + lightid + "/hue"), //
                new HueSuccessGeneric(changeRequest.sat, "/scenes/" + id + "/lightstates/" + lightid + "/sat"), //
                new HueSuccessGeneric(changeRequest.bri, "/scenes/" + id + "/lightstates/" + lightid + "/bri"), //
                new HueSuccessGeneric(changeRequest.transitiontime,
                        "/scenes/" + id + "/lightstates/" + lightid + "/transitiontime")));
    }
}
