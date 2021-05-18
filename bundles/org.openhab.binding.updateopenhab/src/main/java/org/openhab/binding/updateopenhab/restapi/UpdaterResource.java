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
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    private static final Object BAD_QUERYPARAM_USER = "Invalid 'user' query parameter.";

    private static final String SERVER_ERROR_UPDATER_MISSING = "Updater class not initialized.";

    public static final String URL_PATH = "updater";

    private static final String ROOT_HTML =
    // @formatter:off
            "<html>"
            + "<body>"
            + "    <h2>OpenHAB Updater</h2>"
            + "    <p><a href=\"updater/getStatus\">Get updater status</a></p>"
            + "    <p><a href=\"updater/setParams\">Set updater parameters</a></p>"
            + "    <p><a href=\"updater/doUpdate\">Start updating OpenHAB</a></p>"
            + "</body>"
            + "</html>";
    // @formatter:on

    private static final String SET_PARAMS_HTML =
    // @formatter:off
            "<html>"
            + "<body>"
            + "    <h2>ChangeOpenHAB Updater Parameters</h2>"
            + "    <form action=\"setParams\" method=\"post\">"
            + "        <p>Enter the new paramater values..</p>"
            +"         <p><label for=\"targetVersion\">Target version: </label>"
            + "        <select id=\"targetVersion\" name=\"targetVersion\">"
            + "          <option selected>STABLE</option>"
            + "          <option>MILESTONE</option>"
            + "          <option>SNAPSHOT</option>"
            + "        </select></p>"
            + "        <p>Sleep time (seconds): <input type=\"number\" name=\"sleepTime\" value=\"20\" min=\"10\" max=\"30\" step=\"5\"></p>"
            + "        <p>Save new values: <input type=\"submit\" value=\"Save\"></p>"
            + "    </form>"
            + "</body>"
            + "</html>";
    // @formatter:on

    private static final String DO_UPDATE_HTML =
    // @formatter:off
            "<html>"
            + "<body>"
            + "    <h2>Update OpenHAB</h2>"
            + "    <form action=\"doUpdate\" method=\"post\">"
            + "        <p>Enter the system's user credentials..</p>"
            + "        <p>User name: <input type=\"text\" name=\"user\"></p>"
            + "        <p>Password: <input type=\"password\" name=\"password\"></p>"
            + "        <p>Note: some systems (e.g. Windows) do not require user credentials.</p>"
            + "        <p>Start updating OpenHAB now: <input type=\"submit\" value=\"Execute\"></p>"
            + "    </form>"
            + "</body>"
            + "</html>";
    // @formatter:on

    private @Nullable BaseUpdater updater;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Activate
    public UpdaterResource() {
        this.updater = UpdaterFactory.newUpdater();
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    @Operation(operationId = "rootGET", summary = "Root page.", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response rootGET() {
        BaseUpdater updater = this.updater;

        // return server error if updater is null
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }

        // return the HTML form
        return Response.status(Response.Status.OK).entity(ROOT_HTML).type(MediaType.TEXT_HTML).build();
    }

    @GET
    @Path("/getStatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getStatus", summary = "Get the updater status.", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response getStatus() {
        BaseUpdater updater = this.updater;

        // return server error if updater is null
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }

        // populate and return the DTO
        UpdaterDTO dto = new UpdaterDTO();
        dto.runningVersion = BaseUpdater.getActualVersion();
        dto.targetVersionType = updater.getTargetVersionType().name();
        dto.remoteVersion = updater.getRemoteLatestVersion();
        dto.remoteVersionHigher = updater.getRemoteVersionHigher().label;
        return Response.ok(dto).build();
    }

    @GET
    @Path("/setParams")
    @Produces(MediaType.TEXT_HTML)
    @Operation(operationId = "setParamsGET", summary = "Set updater parameter(s).", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response setParamsGET() {
        BaseUpdater updater = this.updater;

        // return server error if updater is null
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }

        // return the HTML form
        return Response.status(Response.Status.OK).entity(SET_PARAMS_HTML).type(MediaType.TEXT_HTML).build();
    }

    @POST
    @Path("/setParams")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(operationId = "setParamsPOST", summary = "Set updater parameter(s).", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response setParamsPOST(@FormParam("targetVersion") @Nullable String targetVersion,
            @FormParam("sleepTime") @Nullable String sleepTime) {
        BaseUpdater updater = this.updater;

        // return server error if updater is null
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

    @GET
    @Path("/doUpdate")
    @Produces(MediaType.TEXT_HTML)
    @Operation(operationId = "doUpdateGET", summary = "Initiate update of OpenHAB.", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response doUpdateGET() {
        BaseUpdater updater = this.updater;

        // return server error if updater is null
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }

        // return the HTML form
        return Response.status(Response.Status.OK).entity(DO_UPDATE_HTML).type(MediaType.TEXT_HTML).build();
    }

    @POST
    // FOR RELEASE BUILD: un-comment the following line
    // @ RolesAllowed({ Role.ADMIN })
    @Path("/doUpdate")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(operationId = "doUpdatePOST", summary = "Initiate update of OpenHAB.", responses = {
            @ApiResponse(responseCode = "200", description = RESPONSE_OK),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST),
            @ApiResponse(responseCode = "500", description = SERVER_ERROR_UPDATER_MISSING) })
    public Response doUpdatePOST(@FormParam("user") @Nullable String user,
            @FormParam("password") @Nullable String password) {
        BaseUpdater updater = this.updater;

        // return server error if updater is null
        if (updater == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(SERVER_ERROR_UPDATER_MISSING).build();
        }

        // check user name e.g. on Linux
        if (user != null) {
            try {
                updater.setUserName(user);
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(BAD_QUERYPARAM_USER).build();
            }
        }

        // check password e.g. on Linux
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
