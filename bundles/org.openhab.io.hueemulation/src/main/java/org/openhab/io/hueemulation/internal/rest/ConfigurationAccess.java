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

import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.HueEmulationService;
import org.openhab.io.hueemulation.internal.NetworkUtils;
import org.openhab.io.hueemulation.internal.dto.HueUnauthorizedConfig;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueChangeRequest;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse.HueErrorMessage;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import com.google.gson.reflect.TypeToken;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * @author David Graeff - Initial contribution
 */
@Component(immediate = false, service = ConfigurationAccess.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + HueEmulationService.REST_APP_NAME + ")")
@NonNullByDefault
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationAccess {
    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) UserManagement userManagement;
    @Reference
    protected @NonNullByDefault({}) ConfigurationAdmin configAdmin;

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Return the reduced configuration", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response getReducedConfigApi() {
        return Response.ok(cs.gson.toJson(cs.ds.config, new TypeToken<HueUnauthorizedConfig>() {
        }.getType())).build();
    }

    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Return the full data store", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response getAllApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds)).build();
    }

    @GET
    @Path("{username}/config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Return the configuration", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response getFullConfigApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        return Response.ok(cs.gson.toJson(cs.ds.config)).build();
    }

    @PUT
    @Path("{username}/config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Return the reduced configuration", responses = {
            @ApiResponse(responseCode = "200", description = "OK") })
    public Response putFullConfigApi(@Context UriInfo uri,
            @PathParam("username") @Parameter(description = "username") String username, String body) {
        if (!userManagement.authorizeUser(username)) {
            return NetworkUtils.singleError(cs.gson, uri, HueResponse.UNAUTHORIZED, "Not Authorized");
        }
        final HueChangeRequest changes;
        changes = cs.gson.fromJson(body, HueChangeRequest.class);
        String devicename = changes.devicename;
        if (devicename != null) {
            cs.ds.config.devicename = devicename;
        }
        Boolean dhcp = changes.dhcp;
        if (dhcp != null) {
            cs.ds.config.dhcp = dhcp;
        }
        Boolean linkbutton = changes.linkbutton;
        if (linkbutton != null) {
            cs.setLinkbutton(linkbutton, cs.getConfig().createNewUserOnEveryEndpoint,
                    cs.getConfig().temporarilyEmulateV1bridge);
        }
        return Response.ok(cs.gson.toJson(cs.ds.config)).build();
    }

    @Path("{username}/{var:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response catchAll(@Context UriInfo uri) {
        HueResponse e = new HueResponse(
                new HueErrorMessage(HueResponse.INVALID_JSON, uri.getPath().replace("/api", ""), "Invalid request: "));
        String str = cs.gson.toJson(Collections.singleton(e), new TypeToken<List<?>>() {
        }.getType());
        return Response.status(404).entity(str).build();
    }
}
