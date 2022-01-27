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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.types.Command;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.DeviceType;
import org.openhab.io.hueemulation.internal.HueEmulationService;
import org.openhab.io.hueemulation.internal.NetworkUtils;
import org.openhab.io.hueemulation.internal.StateUtils;
import org.openhab.io.hueemulation.internal.dto.HueGroupEntry;
import org.openhab.io.hueemulation.internal.dto.HueLightEntry;
import org.openhab.io.hueemulation.internal.dto.HueNewLights;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueChangeRequest;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueStateChange;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Listens to the ItemRegistry for items that fulfill one of these criteria:
 * <ul>
 * <li>Type is any of SWITCH, DIMMER, COLOR, or Group
 * <li>The category is "ColorLight" for coloured lights or "Light" for switchables.
 * <li>The item is tagged, according to what is set with {@link #setFilterTags(Set, Set, Set)}.
 * </ul>
 *
 * <p>
 * A {@link HueLightEntry} instances is created for each found item.
 * Those are kept in the given {@link org.openhab.io.hueemulation.internal.dto.HueDataStore}.
 * </p>
 *
 * <p>
 * The HUE Rest API requires a unique string based ID for every listed light.
 * We are using item names here. Not all hue clients might be compatible with non
 * numeric Ics.ds. A solution could be an ItemMetaData provider and to store a
 * generated integer id for each item.
 * </p>
 *
 * <p>
 * </p>
 *
 * @author David Graeff - Initial contribution
 * @author Florian Schmidt - Removed base type restriction from Group items
 */
@Component(immediate = false, service = LightsAndGroups.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + HueEmulationService.REST_APP_NAME + ")")
@NonNullByDefault
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class LightsAndGroups implements RegistryChangeListener<Item> {
    public static final String EXPOSE_AS_DEVICE_TAG = "huelight";
    private final Logger logger = LoggerFactory.getLogger(LightsAndGroups.class);
    private static final String ITEM_TYPE_GROUP = "Group";
    private static final Set<String> ALLOWED_ITEM_TYPES = Stream.of(CoreItemFactory.COLOR, CoreItemFactory.DIMMER,
            CoreItemFactory.ROLLERSHUTTER, CoreItemFactory.SWITCH, ITEM_TYPE_GROUP).collect(Collectors.toSet());

    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) UserManagement userManagement;
    @Reference
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;
    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
    protected volatile @Nullable EventPublisher eventPublisher;

    /**
     * Registers to the {@link ItemRegistry} and enumerates currently existing items.
     */
    @Activate
    protected void activate() {
        cs.ds.resetGroupsAndLights();

        itemRegistry.removeRegistryChangeListener(this);
        itemRegistry.addRegistryChangeListener(this);

        for (Item item : itemRegistry.getItems()) {
            added(item);
        }
    }

    /**
     * Unregisters from the {@link ItemRegistry}.
     */
    @Deactivate
    protected void deactivate() {
        itemRegistry.removeRegistryChangeListener(this);
    }

    @Override
    public synchronized void added(Item newElement) {
        if (!(newElement instanceof GenericItem)) {
            return;
        }
        GenericItem element = (GenericItem) newElement;

        if (!(element instanceof GroupItem) && !ALLOWED_ITEM_TYPES.contains(element.getType())) {
            return;
        }

        DeviceType deviceType = StateUtils.determineTargetType(cs, element);
        if (deviceType == null) {
            return;
        }

        String hueID = cs.mapItemUIDtoHueID(element);

        if (element instanceof GroupItem && !element.hasTag(EXPOSE_AS_DEVICE_TAG)) {
            GroupItem g = (GroupItem) element;
            HueGroupEntry group = new HueGroupEntry(g.getName(), g, deviceType);

            // Restore group type and room class from tags
            for (String tag : g.getTags()) {
                if (tag.startsWith("huetype_")) {
                    group.type = tag.split("huetype_")[1];
                } else if (tag.startsWith("hueroom_")) {
                    group.roomclass = tag.split("hueroom_")[1];
                }
            }

            // Add group members
            group.lights = new ArrayList<>();
            for (Item item : g.getMembers()) {
                group.lights.add(cs.mapItemUIDtoHueID(item));
            }

            cs.ds.groups.put(hueID, group);
        } else {
            HueLightEntry device = new HueLightEntry(element, cs.getHueUniqueId(hueID), deviceType);
            device.item = element;
            cs.ds.lights.put(hueID, device);
            updateGroup0();
        }
    }

    /**
     * The HUE API enforces a Group 0 that contains all lights.
     */
    private void updateGroup0() {
        cs.ds.groups.get("0").lights = cs.ds.lights.keySet().stream().map(v -> String.valueOf(v))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void removed(Item element) {
        String hueID = cs.mapItemUIDtoHueID(element);
        logger.debug("Remove item {}", hueID);
        cs.ds.lights.remove(hueID);
        cs.ds.groups.remove(hueID);
        updateGroup0();
    }

    /**
     * The tags might have changed
     */
    @SuppressWarnings({ "null", "unused" })
    @Override
    public synchronized void updated(Item oldElement, Item newElement) {
        if (!(newElement instanceof GenericItem)) {
            return;
        }
        GenericItem element = (GenericItem) newElement;

        String hueID = cs.mapItemUIDtoHueID(element);

        HueGroupEntry hueGroup = cs.ds.groups.get(hueID);
        if (hueGroup != null) {
            DeviceType t = StateUtils.determineTargetType(cs, element);
            if (t != null && element instanceof GroupItem) {
                hueGroup.updateItem((GroupItem) element);
            } else {
                cs.ds.groups.remove(hueID);
            }
        }

        HueLightEntry hueDevice = cs.ds.lights.get(hueID);
        if (hueDevice == null) {
            // If the correct tags got added -> use the logic within added()
            added(element);
            return;
        }

        // Check if type can still be determined (tags and category is still sufficient)
        DeviceType t = StateUtils.determineTargetType(cs, element);
        if (t == null) {
            removed(element);
            return;
        }

        hueDevice.updateItem(element);
    }

    @GET
    @Path("{username}/lights")
    @Operation(summary = "Return all lights", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getAllLightsApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.lights)).build();
    }

    @GET
    @Path("{username}/lights/new")
    @Operation(summary = "Return new lights since last scan. Returns an empty list for openHAB as we do not cache that information.", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response getNewLights(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(new HueNewLights())).build();
    }

    @POST
    @Path("{username}/lights")
    @Operation(summary = "Starts a new scan for compatible items. This is usually not necessary, because we are observing the item registry.", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response postNewLights(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return NetworkUtils.singleSuccess(cs.gson, "Searching for new devices", "/lights");
    }

    @GET
    @Path("{username}/lights/{id}")
    @Operation(summary = "Return a light", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getLightApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "light id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.lights.get(id))).build();
    }

    @SuppressWarnings({ "null", "unused" })
    @DELETE
    @Path("{username}/lights/{id}")
    @Operation(summary = "Deletes the item that is represented by this id", responses = {
            @ApiResponse(responseCode = "200", description = "The item got removed"),
            @ApiResponse(responseCode = "403", description = "Access denied") })
    public Response removeLightAPI(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        HueLightEntry hueDevice = cs.ds.lights.get(id);
        if (hueDevice == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Light does not exist");
        }

        if (itemRegistry.remove(id) != null) {
            return NetworkUtils.singleSuccess(cs.gson, "/lights/" + id + " deleted.");
        } else {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Light does not exist");
        }
    }

    @SuppressWarnings({ "null", "unused" })
    @PUT
    @Path("{username}/lights/{id}")
    @Operation(summary = "Rename a light", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response renameLightApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "light id") String id, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        HueLightEntry hueDevice = cs.ds.lights.get(id);
        if (hueDevice == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Light not existing");
        }

        final HueChangeRequest changeRequest = cs.gson.fromJson(body, HueChangeRequest.class);

        String name = changeRequest.name;
        if (name == null || name.isEmpty()) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.INVALID_JSON, "Invalid request: No name set");
        }

        hueDevice.item.setLabel(name);
        itemRegistry.update(hueDevice.item);

        return NetworkUtils.singleSuccess(cs.gson, name, "/lights/" + id + "/name");
    }

    @SuppressWarnings({ "null", "unused" })
    @PUT
    @Path("{username}/lights/{id}/state")
    @Operation(summary = "Set light state", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response setLightStateApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "light id") String id, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        HueLightEntry hueDevice = cs.ds.lights.get(id);
        if (hueDevice == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Light not existing");
        }

        HueStateChange newState = cs.gson.fromJson(body, HueStateChange.class);
        if (newState == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.INVALID_JSON,
                    "Invalid request: No state change data received!");
        }

        hueDevice.state = StateUtils.colorStateFromItemState(hueDevice.item.getState(), hueDevice.deviceType);

        String itemUID = hueDevice.item.getUID();
        List<HueResponse> responses = new ArrayList<>();
        Command command = StateUtils.computeCommandByState(responses, "/lights/" + id + "/state", hueDevice.state,
                newState);

        // If a command could be created, post it to the framework now
        if (command != null) {
            EventPublisher localEventPublisher = eventPublisher;
            if (localEventPublisher != null) {
                logger.debug("sending {} to {}", command, itemUID);
                localEventPublisher.post(ItemEventFactory.createCommandEvent(itemUID, command, "hueemulation"));
            } else {
                logger.warn("No event publisher. Cannot post item '{}' command!", itemUID);
            }
            hueDevice.lastCommand = command;
            hueDevice.lastHueChange = newState;
        }

        return Response.ok(cs.gson.toJson(responses, new TypeToken<List<?>>() {
        }.getType())).build();
    }

    @SuppressWarnings({ "null", "unused" })
    @PUT
    @Path("{username}/groups/{id}/action")
    @Operation(summary = "Initiate group action", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response setGroupActionApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "group id") String id, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        HueGroupEntry hueDevice = cs.ds.groups.get(id);
        GroupItem groupItem = hueDevice.groupItem;
        if (hueDevice == null || groupItem == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Group not existing");
        }

        HueStateChange state = cs.gson.fromJson(body, HueStateChange.class);
        if (state == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.INVALID_JSON,
                    "Invalid request: No state change data received!");
        }

        // First synchronize the internal state information with the framework
        hueDevice.action = StateUtils.colorStateFromItemState(groupItem.getState(), hueDevice.deviceType);

        List<HueResponse> responses = new ArrayList<>();
        Command command = StateUtils.computeCommandByState(responses, "/groups/" + id + "/state/", hueDevice.action,
                state);

        // If a command could be created, post it to the framework now
        if (command != null) {
            logger.debug("sending {} to {}", command, id);
            EventPublisher localEventPublisher = eventPublisher;
            if (localEventPublisher != null) {
                localEventPublisher
                        .post(ItemEventFactory.createCommandEvent(groupItem.getUID(), command, "hueemulation"));
            } else {
                logger.warn("No event publisher. Cannot post item '{}' command!", groupItem.getUID());
            }
        }

        return Response.ok(cs.gson.toJson(responses, new TypeToken<List<?>>() {
        }.getType())).build();
    }

    @GET
    @Path("{username}/groups")
    @Operation(summary = "Return all groups", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getAllGroupsApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.groups)).build();
    }

    @GET
    @Path("{username}/groups/{id}")
    @Operation(summary = "Return a group", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getGroupApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "group id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.groups.get(id))).build();
    }

    @SuppressWarnings({ "null", "unused" })
    @POST
    @Path("{username}/groups")
    @Operation(summary = "Create a new group", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response postNewGroup(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        HueGroupEntry state = cs.gson.fromJson(body, HueGroupEntry.class);
        if (state == null || state.name.isEmpty()) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.INVALID_JSON,
                    "Invalid request: No state change data received!");
        }

        String groupid = cs.ds.nextGroupID();
        GroupItem groupItem = new GroupItem(groupid);

        if (!HueGroupEntry.TypeEnum.LightGroup.name().equals(state.type)) {
            groupItem.addTag("huetype_" + state.type);
        }

        if (HueGroupEntry.TypeEnum.Room.name().equals(state.type) && !state.roomclass.isEmpty()) {
            groupItem.addTag("hueroom_" + state.roomclass);
        }

        List<Item> groupItems = new ArrayList<>();
        for (String id : state.lights) {
            Item item = itemRegistry.get(id);
            if (item == null) {
                logger.debug("Could not create group {}. Item {} not existing!", state.name, id);
                return NetworkUtils.singleError(cs.gson, uri, HueResponse.ARGUMENTS_INVALID,
                        "Invalid request: Item not existing");
            }
            groupItem.addMember(item);
        }

        itemRegistry.add(groupItem);

        return NetworkUtils.singleSuccess(cs.gson, groupid, "id");
    }

    @SuppressWarnings({ "null", "unused" })
    @DELETE
    @Path("{username}/groups/{id}")
    @Operation(summary = "Deletes the item that is represented by this id", responses = {
            @ApiResponse(responseCode = "200", description = "The item got removed"),
            @ApiResponse(responseCode = "403", description = "Access denied") })
    public Response removeGroupAPI(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        HueLightEntry hueDevice = cs.ds.lights.get(id);
        if (hueDevice == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Group does not exist");
        }

        if (itemRegistry.remove(id) != null) {
            return NetworkUtils.singleSuccess(cs.gson, "/groups/" + id + " deleted.");
        } else {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Group does not exist");
        }
    }
}
