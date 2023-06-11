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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.HueEmulationService;
import org.openhab.io.hueemulation.internal.NetworkUtils;
import org.openhab.io.hueemulation.internal.dto.HueNewLights;
import org.openhab.io.hueemulation.internal.dto.HueSensorEntry;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueChangeRequest;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
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
 * Listens to the ItemRegistry and add all DecimalType, OnOffType, ContactType, DimmerType items
 * as sensors.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = false, service = Sensors.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + HueEmulationService.REST_APP_NAME + ")")
@NonNullByDefault
@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Sensors implements RegistryChangeListener<Item> {
    private final Logger logger = LoggerFactory.getLogger(Sensors.class);
    private static final Set<String> ALLOWED_ITEM_TYPES = Stream.of(CoreItemFactory.COLOR, CoreItemFactory.DIMMER,
            CoreItemFactory.ROLLERSHUTTER, CoreItemFactory.SWITCH, CoreItemFactory.CONTACT, CoreItemFactory.NUMBER)
            .collect(Collectors.toSet());

    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) UserManagement userManagement;
    @Reference
    protected @NonNullByDefault({}) ItemRegistry itemRegistry;

    /**
     * Registers to the {@link ItemRegistry} and enumerates currently existing items.
     * Call {@link #close(ItemRegistry)} when you are done with this object.
     *
     * Only call this after you have set the filter tags with {@link #setFilterTags(Set, Set, Set)}.
     */
    @Activate
    protected void activate() {
        cs.ds.resetSensors();

        itemRegistry.removeRegistryChangeListener(this);
        itemRegistry.addRegistryChangeListener(this);

        for (Item item : itemRegistry.getItems()) {
            added(item);
        }
        logger.debug("Added as sensor: {}",
                cs.ds.sensors.values().stream().map(l -> l.name).collect(Collectors.joining(", ")));
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

        if (!ALLOWED_ITEM_TYPES.contains(element.getType())) {
            return;
        }

        String hueID = cs.mapItemUIDtoHueID(element);

        HueSensorEntry sensor = new HueSensorEntry(element);
        cs.ds.sensors.put(hueID, sensor);
    }

    @Override
    public synchronized void removed(Item element) {
        String hueID = cs.mapItemUIDtoHueID(element);
        logger.debug("Remove item {}", hueID);
        cs.ds.sensors.remove(hueID);
    }

    @Override
    public synchronized void updated(Item oldElement, Item newElement) {
        if (!(newElement instanceof GenericItem)) {
            return;
        }
        GenericItem element = (GenericItem) newElement;

        String hueID = cs.mapItemUIDtoHueID(element);

        HueSensorEntry sensor = new HueSensorEntry(element);
        cs.ds.sensors.put(hueID, sensor);
    }

    @GET
    @Path("{username}/sensors")
    @Operation(summary = "Return all sensors", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getAllSensorsApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.sensors)).build();
    }

    @GET
    @Path("{username}/sensors/new")
    @Operation(summary = "Return new sensors since last scan. Returns an empty list for openHAB as we do not cache that information.", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response getNewSensors(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(new HueNewLights())).build();
    }

    @POST
    @Path("{username}/sensors")
    @Operation(summary = "Starts a new scan for compatible items. This is usually not necessary, because we are observing the item registry.", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response postNewLights(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return NetworkUtils.singleSuccess(cs.gson, "Searching for new sensors", "/sensors");
    }

    @GET
    @Path("{username}/sensors/{id}")
    @Operation(summary = "Return a sensor", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response getSensorApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "sensor id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.sensors.get(id))).build();
    }

    @SuppressWarnings({ "null", "unused" })
    @GET
    @Path("{username}/sensors/{id}/config")
    @Operation(summary = "Return a sensor config. Always empty", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response getSensorConfigApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "sensor id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        HueSensorEntry sensor = cs.ds.sensors.get(id);
        if (sensor == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Sensor does not exist");
        }

        return Response.ok(cs.gson.toJson(sensor.config)).build();
    }

    @SuppressWarnings({ "null", "unused" })
    @DELETE
    @Path("{username}/sensors/{id}")
    @Operation(summary = "Deletes the sensor that is represented by this id", responses = {
            @ApiResponse(responseCode = "200", description = "The item got removed"),
            @ApiResponse(responseCode = "403", description = "Access denied") })
    public Response removeSensorAPI(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "id") String id) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        HueSensorEntry sensor = cs.ds.sensors.get(id);
        if (sensor == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Sensor does not exist");
        }

        if (itemRegistry.remove(id) != null) {
            return NetworkUtils.singleSuccess(cs.gson, "/sensors/" + id + " deleted.");
        } else {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Sensor does not exist");
        }
    }

    @SuppressWarnings({ "null", "unused" })
    @PUT
    @Path("{username}/sensors/{id}")
    @Operation(summary = "Rename a sensor", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response renameLightApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "light id") String id, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        HueSensorEntry sensor = cs.ds.sensors.get(id);
        if (sensor == null) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.NOT_AVAILABLE, "Sensor not existing");
        }

        final HueChangeRequest changeRequest = cs.gson.fromJson(body, HueChangeRequest.class);

        String name = changeRequest.name;
        if (name == null || name.isEmpty()) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.INVALID_JSON, "Invalid request: No name set");
        }

        sensor.item.setLabel(name);
        itemRegistry.update(sensor.item);

        return NetworkUtils.singleSuccess(cs.gson, name, "/sensors/" + id + "/name");
    }

    @PUT
    @Path("{username}/sensors/{id}/state")
    @Operation(summary = "Set sensor state", responses = { @ApiResponse(responseCode = "200", description = "OK") })
    public Response setSensorStateApi(@Context UriInfo uri, //
            @PathParam("username") @Parameter(description = "username") String username,
            @PathParam("id") @Parameter(description = "sensor id") String id, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }

        return NetworkUtils.singleError(cs.gson, uri, HueResponse.SENSOR_NOT_CLIP_SENSOR,
                "Invalid request: Not a clip sensor");
    }
}
