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

    private static final String RESPONSE_OK = "Request succeeded!";
    private static final String RESPONSE_STARTED_UPDATE = "Started OpenHAB self update process";

    private static final String BAD_REQUEST = "Bad request!";
    private static final String BAD_QUERYPARAM_TARGETVERSION = "Invalid 'targetVersion' parameter.";
    private static final Object BAD_QUERYPARAM_SLEEPTIME = "Invalid 'sleepTime' parameter.";
    private static final String BAD_QUERYPARAM_PASSWORD = "Invalid 'password' query parameter.";

    private static final String SERVER_ERROR_UPDATER_MISSING = "Updater class not initialized.";

    public static final String URL_PATH = "updater";

    private @Nullable BaseUpdater updater;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Activate
    public UpdaterResource() {
        this.updater = UpdaterFactory.newUpdater();
    }

    @GET
    @Path("/getstatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getStatus", summary = "Get the updater status.", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response getStatus() {
        BaseUpdater updater = this.updater;
        // server error if updater is null
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }
        // populate and return the DTO
        UpdaterDTO dto = new UpdaterDTO();
        dto.runningVersion = BaseUpdater.getActualVersion();
        dto.targetVersionType = updater.getTargetVersionType().name();
        dto.remoteVersion = updater.getRemoteVersion();
        dto.remoteVersionHigher = updater.getRemoteVersionHigher().label;
        return Response.ok(dto).build();
    }

    @GET
    @Path("/setvalue")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(operationId = "postTargetVerionType", summary = "Set the target update version type.", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response postTargetVerionType(@QueryParam("targetVersion") @Nullable String targetVersion,
            @QueryParam("sleepTime") @Nullable String sleepTime) {
        // server error if updater is null
        BaseUpdater updater = this.updater;
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }

        // process any query parameters
        boolean modified = false;
        if (targetVersion != null) {
            try {
                updater.setTargetVersion(targetVersion);
                modified = true;
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(BAD_QUERYPARAM_TARGETVERSION).build();
            }
        }
        if (sleepTime != null) {
            try {
                updater.setSleepTime(sleepTime);
                modified = true;
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(BAD_QUERYPARAM_SLEEPTIME).build();
            }
        }

        return modified ? Response.ok(RESPONSE_OK).build() : Response.notModified().build();
    }

    // FOR TESTING BUILD: un- comment the following line
    @GET
    // FOR RELEASE BUILD: un-comment the following two lines
    // @ POST
    // @ RolesAllowed({ Role.ADMIN })
    @Path("/update")
    @Operation(operationId = "executeUpdate", summary = "Update OpenHAB to the latest available (stable/milestone/snapshot) version.", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response executeUpdate(@QueryParam("targetVersion") @Nullable String targetVersion,
            @QueryParam("sleepTime") @Nullable String sleepTime, @QueryParam("password") @Nullable String password,
            @QueryParam("say") @Nullable String say) {
        // server error if updater is null
        BaseUpdater updater = this.updater;
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }

        // check the magic word
        if (!"please".equalsIgnoreCase(say)) {
            return Response.status(Status.BAD_REQUEST).entity(BAD_REQUEST).build();
        }

        // process any query parameters
        if (targetVersion != null) {
            try {
                updater.setTargetVersion(targetVersion);
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(BAD_QUERYPARAM_TARGETVERSION).build();
            }
        }
        if (sleepTime != null) {
            try {
                updater.setSleepTime(sleepTime);
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(BAD_QUERYPARAM_SLEEPTIME).build();
            }
        }
        if (password != null) {
            try {
                updater.setPassword(password);
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(BAD_QUERYPARAM_PASSWORD).build();
            }
        }

        // start the update process
        executorService.submit(updater);
        return Response.ok(RESPONSE_STARTED_UPDATE).build();
    }
}
