/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.updateopenhab.restapi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.updateopenhab.updaters.BaseUpdater;
import org.openhab.binding.updateopenhab.updaters.TargetVersionType;
import org.openhab.binding.updateopenhab.updaters.UpdaterFactory;
import org.openhab.core.auth.Role;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This is a REST resource for OpenHAB self updating features.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@Component
@JaxrsResource
@JaxrsName(UpdaterResource.URL_PATH)
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path(UpdaterResource.URL_PATH)
@RolesAllowed({ Role.USER, Role.ADMIN })
@Tag(name = UpdaterResource.URL_PATH)
@NonNullByDefault
public class UpdaterResource implements RESTResource {

    private static final String OK_REQUEST = "Request succeeded!";
    private static final String BAD_REQUEST = "Bad request!";
    private static final String BAD_REQUEST_TYPE_PARAMETER = "Invalid 'type' parameter.";
    private static final String BAD_REQUEST_VALUE_PARAMETER = "Missing 'value' parameter.";
    private static final String SERVER_ERROR_UPDATER_MISSING = "Updater class not initialized.";

    public static final String URL_PATH = "updater";

    // default values
    private TargetVersionType targetVersion = BaseUpdater.DEF_TARGET_VER;
    private String linuxPassword = "";
    private int sleepTime = BaseUpdater.DEF_SLEEP_SECS;

    private @Nullable BaseUpdater updater;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Activate
    public UpdaterResource() {
        BaseUpdater updater = this.updater = UpdaterFactory.newUpdater();
        if (updater != null) {
            updater.setTargetVersion(targetVersion);
            updater.setPassword(linuxPassword);
            updater.setSleepTime(sleepTime);
        }
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getStatus", summary = "Get the updater status.", responses = {
            @ApiResponse(responseCode = "200", description = OK_REQUEST),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response getStatus() {
        BaseUpdater updater = this.updater;
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }
        UpdaterDTO dto = new UpdaterDTO();
        dto.runningVersion = BaseUpdater.getActualVersion();
        dto.targetVersionType = updater.getTargetVersionType().name();
        dto.remoteVersion = updater.getRemoteVersion();
        dto.remoteVersionHigher = updater.getRemoteVersionHigher().label;
        return Response.ok(dto).build();
    }

    @GET
    @Path("/password")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(operationId = "postPassword", summary = "Set the updater system password.", responses = {
            @ApiResponse(responseCode = "200", description = OK_REQUEST),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST_VALUE_PARAMETER),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response postPassword(@QueryParam("value") @Nullable String value) {
        BaseUpdater updater = this.updater;
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }
        if (value == null) {
            return Response.status(Status.BAD_REQUEST).entity(BAD_REQUEST_VALUE_PARAMETER).build();
        }
        updater.setPassword(value);
        return Response.ok("Password set to: " + value).build();
    }

    @GET
    @Path("/targetversion")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(operationId = "postTargetVerionType", summary = "Set the target update version type.", responses = {
            @ApiResponse(responseCode = "200", description = OK_REQUEST),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST_TYPE_PARAMETER),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response postTargetVerionType(@QueryParam("type") @Nullable String type) {
        BaseUpdater updater = this.updater;
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }
        if (type != null) {
            try {
                TargetVersionType targetVersion = TargetVersionType.valueOf(type.toUpperCase());
                updater.setTargetVersion(targetVersion);
                return Response.ok("Target version type set to: " + targetVersion.name()).build();
            } catch (IllegalArgumentException e) {
            }
        }
        return Response.status(Status.BAD_REQUEST).entity(BAD_REQUEST_TYPE_PARAMETER).build();
    }

    // FOR TESTING BUILD: un- comment the following line
    @GET
    // FOR RELEASE BUILD: un-comment the following two lines
    // @ POST
    // @ RolesAllowed({ Role.ADMIN })
    @Path("/executeupdate")
    @Operation(operationId = "executeUpdate", summary = "Update OpenHAB to the latest available (stable/milestone/snapshot) version.", responses = {
            @ApiResponse(responseCode = "200", description = OK_REQUEST),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response executeUpdate(@QueryParam("say") @Nullable String say) {
        BaseUpdater updater = this.updater;
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }
        if (!"please".equalsIgnoreCase(say)) {
            return Response.status(Status.BAD_REQUEST).entity(BAD_REQUEST).build();
        }
        executorService.submit(updater);
        return Response.ok("OpenHAB self update process started!").build();
    }
}
