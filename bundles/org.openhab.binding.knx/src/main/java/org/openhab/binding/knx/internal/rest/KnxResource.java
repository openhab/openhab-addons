/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.rest.dto.ChannelRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.PreviewRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalRequestDTO;
import org.openhab.core.OpenHAB;
import org.openhab.core.auth.Role;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.JakartarsWhiteboardConstants;
import org.osgi.service.jakartars.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

/**
 * The KNX add-on's REST resource under {@code /rest/knx}, for admins only. A thin shell: every method hands the
 * texts it got to {@link KnxRestService} and turns the result into a response with {@link KnxRestCall}.
 * <p>
 * Every parameter is a nullable {@code String}: a number or a list declared as such would be refused by the JAX-RS
 * runtime before this code runs, with a body that is not this add-on's error body.
 *
 * @author Stamate Viorel - Initial contribution
 */
@Component(service = { RESTResource.class, KnxResource.class })
@JakartarsResource
@JakartarsName("knx")
@JakartarsApplicationSelect("(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path("knx")
@RolesAllowed({ Role.ADMIN })
@Tag(name = "knx")
@Produces(MediaType.APPLICATION_JSON)
@NonNullByDefault
public class KnxResource implements RESTResource {
    static final String BRIDGE = "b";
    static final String BRIDGE_PATH = "bridges/{" + BRIDGE + "}";
    static final String DEVICE = "device";
    static final String PROJECT_PATH = BRIDGE_PATH + "/project";
    static final String DEVICE_PATH = PROJECT_PATH + "/devices/{" + DEVICE + "}";

    private final KnxRestService service;
    private final ProjectUpload projectUpload;
    private final KeyringUpload keyringUpload;
    private final TelegramStream telegramStream;

    @Activate
    public KnxResource(final @Reference KNXHandlerFactory factory, final @Reference KnxProjectStore store,
            final @Reference ThingRegistry thingRegistry, final @Reference ItemRegistry itemRegistry,
            final @Reference ItemChannelLinkRegistry linkRegistry) {
        this(factory, store, thingRegistry, itemRegistry, linkRegistry,
                java.nio.file.Path.of(OpenHAB.getUserDataFolder(), "knx"),
                java.nio.file.Path.of(OpenHAB.getConfigFolder(), "misc"),
                TelegramStream.Timer.of(ThreadPoolManager.getScheduledPool("common")));
    }

    /**
     * @param knxFolder the add-on's folder in userdata; the upload and the keyring folder lie inside it
     * @param configMisc the folder a bridge looks for its keyring in first
     * @param timer runs the passes and ticks of the stream
     */
    KnxResource(KNXHandlerFactory factory, KnxProjectStore store, ThingRegistry thingRegistry,
            ItemRegistry itemRegistry, ItemChannelLinkRegistry linkRegistry, java.nio.file.Path knxFolder,
            java.nio.file.Path configMisc, TelegramStream.Timer timer) {
        this.service = new KnxRestService(factory, store, thingRegistry, itemRegistry, linkRegistry);
        this.telegramStream = new TelegramStream(service, timer);
        Uploads uploads = new Uploads(knxFolder.resolve("upload"));
        uploads.clean();
        this.projectUpload = new ProjectUpload(service, store, uploads);
        this.keyringUpload = new KeyringUpload(service, uploads, knxFolder.resolve("keyring"), configMisc);
    }

    @GET
    @Path("bridges")
    @RolesAllowed({ Role.ADMIN })
    public Response bridges() {
        return respond(KnxRestCall.run("bridges", service::bridges));
    }

    @GET
    @Path(BRIDGE_PATH)
    @RolesAllowed({ Role.ADMIN })
    public Response bridge(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.run("bridge", () -> service.bridge(bridge)));
    }

    @GET
    @Path(BRIDGE_PATH + "/monitor")
    @RolesAllowed({ Role.ADMIN })
    public Response monitor(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.run("monitor", () -> service.monitor(bridge)));
    }

    @GET
    @Path(BRIDGE_PATH + "/telegrams")
    @RolesAllowed({ Role.ADMIN })
    public Response telegrams(@PathParam(BRIDGE) @Nullable String bridge, @QueryParam("src") @Nullable String src,
            @QueryParam("dst") @Nullable String dst, @QueryParam("type") @Nullable String type,
            @QueryParam("dir") @Nullable String dir, @QueryParam("dpt") @Nullable String dpt,
            @QueryParam("from") @Nullable String from, @QueryParam("to") @Nullable String to,
            @QueryParam("context") @Nullable String context, @QueryParam("limit") @Nullable String limit,
            @QueryParam("after") @Nullable String after, @QueryParam("monitor") @Nullable String monitor) {
        Map<String, @Nullable String> filter = new LinkedHashMap<>();
        filter.put("src", src);
        filter.put("dst", dst);
        filter.put("type", type);
        filter.put("dir", dir);
        filter.put("dpt", dpt);
        filter.put("from", from);
        filter.put("to", to);
        filter.put("context", context);
        return respond(KnxRestCall.run("telegrams", () -> service.telegrams(bridge, filter, limit, after, monitor)));
    }

    /**
     * The live telegrams as server-sent events. Every answer is status 200: a failure, also before the stream
     * starts, is an {@code end} event with the error's code, after which the server closes the stream.
     */
    @GET
    @Path(BRIDGE_PATH + "/telegrams/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RolesAllowed({ Role.ADMIN })
    public void stream(@Context SseEventSink sink, @Context Sse sse, @Context HttpServletResponse response,
            @PathParam(BRIDGE) @Nullable String bridge, @QueryParam("src") @Nullable String src,
            @QueryParam("dst") @Nullable String dst, @QueryParam("type") @Nullable String type,
            @QueryParam("dir") @Nullable String dir, @QueryParam("dpt") @Nullable String dpt,
            @QueryParam("from") @Nullable String from, @QueryParam("to") @Nullable String to,
            @QueryParam("context") @Nullable String context, @QueryParam("limit") @Nullable String limit,
            @QueryParam("after") @Nullable String after, @QueryParam("monitor") @Nullable String monitor) {
        StreamClient client = new SseStreamClient(sink, sse);
        try {
            response.setHeader("X-Accel-Buffering", "no");
            response.setHeader("Content-Encoding", "identity");
            try {
                response.flushBuffer();
            } catch (IOException e) {
                LoggerFactory.getLogger(KnxResource.class).debug("KNX REST stream: flushing the headers failed ({})",
                        e.getClass().getName());
            }
            Map<String, @Nullable String> filter = new LinkedHashMap<>();
            filter.put("src", src);
            filter.put("dst", dst);
            filter.put("type", type);
            filter.put("dir", dir);
            filter.put("dpt", dpt);
            filter.put("from", from);
            filter.put("to", to);
            filter.put("context", context);
            filter.put("limit", limit);
            telegramStream.open(client, bridge, filter, after, monitor);
        } catch (RuntimeException e) {
            Object error = KnxRestCall.internal("telegram-stream", e).entity();
            if (error != null) {
                client.send(TelegramStream.END, error);
            }
            client.close();
        }
    }

    @DELETE
    @Path(BRIDGE_PATH + "/telegrams")
    @RolesAllowed({ Role.ADMIN })
    public Response clearTelegrams(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.runWithoutBody("clear-telegrams", () -> service.clearTelegrams(bridge)));
    }

    @GET
    @Path(BRIDGE_PATH + "/last-values")
    @RolesAllowed({ Role.ADMIN })
    public Response lastValues(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.run("last-values", () -> service.lastValues(bridge)));
    }

    /**
     * Sends one group read or one group write; refused unless the bridge's setting {@code groupMonitorSend} is on.
     */
    @POST
    @Path(BRIDGE_PATH + "/group-telegram")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ Role.ADMIN })
    public Response groupTelegram(@PathParam(BRIDGE) @Nullable String bridge,
            @Nullable GroupTelegramRequestDTO request) {
        return respond(KnxRestCall.run("group-telegram", () -> service.groupTelegram(bridge, request)));
    }

    @GET
    @Path(BRIDGE_PATH + "/keyring")
    @RolesAllowed({ Role.ADMIN })
    public Response keyring(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.run("keyring", () -> service.keyring(bridge)));
    }

    /**
     * The body is the keyring file; the password header is optional and only checks the signature. The answer's
     * {@code fileName} is the value for the bridge option {@code keyringFile}.
     */
    @PUT
    @Path(BRIDGE_PATH + "/keyring")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({ Role.ADMIN })
    public Response uploadKeyring(@PathParam(BRIDGE) @Nullable String bridge,
            @HeaderParam(Uploads.PASSWORD_HEADER) @Nullable String password,
            @HeaderParam(HttpHeaders.CONTENT_LENGTH) @Nullable String contentLength, @Nullable InputStream body) {
        return respond(
                KnxRestCall.run("keyring-upload", () -> keyringUpload.upload(bridge, body, contentLength, password)));
    }

    @GET
    @Path(PROJECT_PATH)
    @RolesAllowed({ Role.ADMIN })
    public Response project(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.run("project", () -> service.project(bridge)));
    }

    /** The body is the project file; the password, if any, comes in a header, never in the path or the query. */
    @PUT
    @Path(PROJECT_PATH)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({ Role.ADMIN })
    public Response uploadProject(@PathParam(BRIDGE) @Nullable String bridge,
            @HeaderParam(Uploads.PASSWORD_HEADER) @Nullable String password,
            @HeaderParam(HttpHeaders.CONTENT_LENGTH) @Nullable String contentLength,
            @QueryParam("language") @Nullable String language, @QueryParam("name") @Nullable String name,
            @Nullable InputStream body) {
        return respond(KnxRestCall.run("project-upload",
                () -> projectUpload.upload(bridge, body, contentLength, password, language, name)));
    }

    @DELETE
    @Path(PROJECT_PATH)
    @RolesAllowed({ Role.ADMIN })
    public Response removeProject(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.runWithoutBody("project-remove", () -> service.removeProject(bridge)));
    }

    @GET
    @Path(PROJECT_PATH + "/group-addresses")
    @RolesAllowed({ Role.ADMIN })
    public Response groupAddresses(@PathParam(BRIDGE) @Nullable String bridge,
            @QueryParam("offset") @Nullable String offset, @QueryParam("limit") @Nullable String limit) {
        return respond(KnxRestCall.run("group-addresses", () -> service.groupAddresses(bridge, offset, limit)));
    }

    @GET
    @Path(PROJECT_PATH + "/group-address")
    @RolesAllowed({ Role.ADMIN })
    public Response groupAddress(@PathParam(BRIDGE) @Nullable String bridge,
            @QueryParam("address") @Nullable String address) {
        return respond(KnxRestCall.run("group-address", () -> service.groupAddress(bridge, address)));
    }

    @GET
    @Path(PROJECT_PATH + "/devices")
    @RolesAllowed({ Role.ADMIN })
    public Response devices(@PathParam(BRIDGE) @Nullable String bridge, @QueryParam("offset") @Nullable String offset,
            @QueryParam("limit") @Nullable String limit) {
        return respond(KnxRestCall.run("devices", () -> service.devices(bridge, offset, limit)));
    }

    @GET
    @Path(DEVICE_PATH)
    @RolesAllowed({ Role.ADMIN })
    public Response device(@PathParam(BRIDGE) @Nullable String bridge, @PathParam(DEVICE) @Nullable String device) {
        return respond(KnxRestCall.run("device", () -> service.device(bridge, device)));
    }

    @GET
    @Path(DEVICE_PATH + "/channels")
    @RolesAllowed({ Role.ADMIN })
    public Response deviceChannels(@PathParam(BRIDGE) @Nullable String bridge,
            @PathParam(DEVICE) @Nullable String device) {
        return respond(KnxRestCall.run("device-channels", () -> service.deviceChannels(bridge, device)));
    }

    /** A read with a body, as the list of addresses can be long; it changes nothing. */
    @POST
    @Path(PROJECT_PATH + "/channels")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ Role.ADMIN })
    public Response channels(@PathParam(BRIDGE) @Nullable String bridge, @Nullable ChannelRequestDTO request) {
        return respond(KnxRestCall.run("channels", () -> service.channels(bridge, request)));
    }

    /** The wizard's proposal for a selection; it changes nothing. */
    @POST
    @Path(PROJECT_PATH + "/proposal")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ Role.ADMIN })
    public Response proposal(@PathParam(BRIDGE) @Nullable String bridge, @Nullable ProposalRequestDTO request) {
        return respond(KnxRestCall.run("proposal", () -> service.proposal(bridge, request)));
    }

    /** The checked proposal as text and create steps; it changes nothing, the page creates. */
    @POST
    @Path(PROJECT_PATH + "/proposal/preview")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ Role.ADMIN })
    public Response preview(@PathParam(BRIDGE) @Nullable String bridge, @Nullable PreviewRequestDTO request) {
        return respond(KnxRestCall.run("proposal-preview", () -> service.preview(bridge, request)));
    }

    @GET
    @Path(PROJECT_PATH + "/topology")
    @RolesAllowed({ Role.ADMIN })
    public Response topology(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.run("topology", () -> service.topology(bridge)));
    }

    @GET
    @Path(PROJECT_PATH + "/locations")
    @RolesAllowed({ Role.ADMIN })
    public Response locations(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.run("locations", () -> service.locations(bridge)));
    }

    @GET
    @Path(PROJECT_PATH + "/check")
    @RolesAllowed({ Role.ADMIN })
    public Response check(@PathParam(BRIDGE) @Nullable String bridge) {
        return respond(KnxRestCall.run("check", () -> service.check(bridge)));
    }

    /** Ends the open streams with {@code shutdown}. */
    @Deactivate
    void deactivate() {
        telegramStream.close();
    }

    private static Response respond(KnxRestCall.Answer answer) {
        ResponseBuilder response = Response.status(answer.status());
        Object entity = answer.entity();
        if (entity != null) {
            response.entity(entity).type(MediaType.APPLICATION_JSON_TYPE);
        }
        return response.build();
    }
}
