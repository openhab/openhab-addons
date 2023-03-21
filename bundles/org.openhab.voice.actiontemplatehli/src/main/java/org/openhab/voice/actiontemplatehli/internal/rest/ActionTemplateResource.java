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
package org.openhab.voice.actiontemplatehli.internal.rest;

import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.SERVICE_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.auth.Role;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.openhab.core.voice.VoiceManager;
import org.openhab.core.voice.text.InterpretationException;
import org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreter;
import org.openhab.voice.actiontemplatehli.internal.configuration.ActionTemplateConfiguration;
import org.openhab.voice.actiontemplatehli.internal.configuration.ActionTemplatePlaceholder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * The {@link ActionTemplateResource} class implements the REST endpoints for the actions configuration
 *
 * @author Miguel Álvarez - Initial contribution
 */
@Component
@JaxrsResource
@JaxrsName(SERVICE_ID)
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@RolesAllowed({ Role.USER, Role.ADMIN })
@Path(SERVICE_ID)
@Tag(name = SERVICE_ID)
@NonNullByDefault
public class ActionTemplateResource implements RESTResource {
    private final VoiceManager voiceManager;
    private final LocaleProvider localeProvider;

    @Activate
    public ActionTemplateResource(final @Reference VoiceManager voiceManager,
            @Reference LocaleProvider localeProvider) {
        this.voiceManager = voiceManager;
        this.localeProvider = localeProvider;
    }

    private ActionTemplateInterpreter getInterpreter() {
        var interpreter = (ActionTemplateInterpreter) voiceManager.getHLI("actiontemplatehli");
        if (interpreter == null) {
            throw new ClientErrorException("Interpreter is unavailable", 500);
        }
        return interpreter;
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List available action templates.", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ActionTemplateConfiguration.class)))),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public Response getActions() {
        ArrayList<ActionTemplateConfiguration> actionTemplateConfigurations = new ArrayList<>();
        getInterpreter().actionTemplateStorage.getValues().forEach(at -> {
            if (at != null) {
                actionTemplateConfigurations.add(at);
            }
        });
        return Response.ok(actionTemplateConfigurations).build();
    }

    @POST
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create action template.", responses = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = ActionTemplateConfiguration.class))),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public ActionTemplateConfiguration createAction(ActionTemplateConfiguration action) {
        action.id = UUID.randomUUID().toString();
        var interpreter = getInterpreter();
        if (interpreter.actionTemplateStorage.containsKey(action.id)) {
            throw new ClientErrorException("Error generating id", 500);
        }
        interpreter.actionTemplateStorage.put(action.id, action);
        interpreter.invalidateItemCache();
        return action;
    }

    @PUT
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/actions/{action_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update action template.", responses = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = ActionTemplateConfiguration.class))),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public ActionTemplateConfiguration updateAction(ActionTemplateConfiguration action,
            @PathParam("action_id") String id) {
        var interpreter = getInterpreter();
        if (!interpreter.actionTemplateStorage.containsKey(id)) {
            throw new NotFoundException();
        }
        action.id = id;
        interpreter.actionTemplateStorage.put(id, action);
        interpreter.invalidateItemCache();
        return action;
    }

    @DELETE
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/actions/{action_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update action template.", responses = {
            @ApiResponse(responseCode = "204", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public Response deleteAction(@PathParam("action_id") String id) {
        var interpreter = getInterpreter();
        if (interpreter.actionTemplateStorage.remove(id) == null) {
            throw new NotFoundException();
        }
        interpreter.invalidateItemCache();
        return Response.noContent().build();
    }

    @POST
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create action template.", responses = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = TestOutput.class))),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public TestOutput testAction(TestBody body) {
        var interpreter = getInterpreter();
        ActionTemplateInterpreter.ActionTemplateInterpretation result;
        try {
            result = interpreter.interpretInternal(localeProvider.getLocale(), body.text, body.dryRun);
        } catch (InterpretationException e) {
            return new TestOutput("", "Negative response: " + e.getMessage(), "");
        }
        return new TestOutput(result.interpretation.actionConfig.template, "Affirmative response: " + result.response,
                result.interpretation.targetItem.getName());
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/placeholders")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List available action placeholders.", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ActionTemplateConfiguration.class)))),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public List<ActionTemplatePlaceholder> getPlaceholders() {
        ArrayList<ActionTemplatePlaceholder> placeholders = new ArrayList<>();
        getInterpreter().placeholderStorage.getValues().forEach(at -> {
            if (at != null) {
                placeholders.add(at);
            }
        });
        return placeholders;
    }

    @POST
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/placeholders")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create placeholder template.", responses = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = ActionTemplateConfiguration.class))),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public ActionTemplatePlaceholder createPlaceholder(ActionTemplatePlaceholder placeholder) {
        var interpreter = getInterpreter();
        if (interpreter.placeholderStorage.containsKey(placeholder.label)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST).entity("Duplicated placeholder").build());
        }
        interpreter.placeholderStorage.put(placeholder.label, placeholder);
        return placeholder;
    }

    @PUT
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/placeholders/{placeholder_label}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update placeholder template.", responses = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = ActionTemplateConfiguration.class))),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public ActionTemplatePlaceholder updatePlaceholder(ActionTemplatePlaceholder placeholder,
            @PathParam("placeholder_label") String label) {
        var interpreter = getInterpreter();
        if (!interpreter.placeholderStorage.containsKey(label)) {
            throw new NotFoundException();
        }
        interpreter.placeholderStorage.put(placeholder.label, placeholder);
        if (!label.equals(placeholder.label)) {
            interpreter.placeholderStorage.remove(label);
        }
        return placeholder;
    }

    @DELETE
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/placeholders/{placeholder_label}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update placeholder template.", responses = {
            @ApiResponse(responseCode = "204", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error") })
    public Response deletePlaceholder(@PathParam("placeholder_label") String label) {
        var interpreter = getInterpreter();
        if (interpreter.placeholderStorage.remove(label) == null) {
            throw new NotFoundException();
        }
        return Response.noContent().build();
    }

    private class TestBody {
        public String text = "";
        public boolean dryRun;
    }

    private class TestOutput {
        public String response;
        public String actionTemplate;
        public String itemName;

        public TestOutput(String actionTemplate, String response, String itemName) {
            this.actionTemplate = actionTemplate;
            this.response = response;
            this.itemName = itemName;
        }
    }
}
