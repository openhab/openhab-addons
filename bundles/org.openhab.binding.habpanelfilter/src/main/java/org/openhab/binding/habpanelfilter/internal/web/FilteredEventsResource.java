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
package org.openhab.binding.habpanelfilter.internal.web;

import static org.openhab.core.io.rest.sse.internal.SseSinkTopicInfo.matchesTopic;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.habpanelfilter.internal.sse.*;
import org.openhab.core.auth.Role;
import org.openhab.core.events.Event;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.openhab.core.io.rest.SseBroadcaster;
import org.openhab.core.io.rest.sse.internal.SseSinkTopicInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Pavel Cuchriajev
 */
@Component(service = { RESTResource.class, SsePublisher.class })
@JaxrsResource
@JaxrsName(FilteredEventsResource.PATH_EVENTS_FILTERED)
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path(FilteredEventsResource.PATH_EVENTS_FILTERED)
@RolesAllowed({ Role.USER, Role.ADMIN })
@Tag(name = FilteredEventsResource.PATH_EVENTS_FILTERED)
@Singleton
@NonNullByDefault
public class FilteredEventsResource implements RESTResource, SsePublisher {

    public static final String PATH_EVENTS_FILTERED = "events-filtered";

    private static final String X_ACCEL_BUFFERING_HEADER = "X-Accel-Buffering";

    private final Logger logger = LoggerFactory.getLogger(FilteredEventsResource.class);

    private @Context @NonNullByDefault({}) Sse sse;

    private final SseBroadcaster<SseSinkItemInfo> itemStatesBroadcaster = new SseBroadcaster<>();
    private final SseItemStatesEventBuilder itemStatesEventBuilder;
    private final SseBroadcaster<SseSinkTopicInfo> topicBroadcaster = new SseBroadcaster<>();

    private ExecutorService executorService;

    @Activate
    public FilteredEventsResource(@Reference SseItemStatesEventBuilder itemStatesEventBuilder) {
        logger.info("FilteredEventsResource created ...");
        this.executorService = Executors.newSingleThreadExecutor();
        this.itemStatesEventBuilder = itemStatesEventBuilder;
    }

    @Deactivate
    public void deactivate() {
        itemStatesBroadcaster.close();
        topicBroadcaster.close();
        executorService.shutdown();
    }

    @Override
    public void broadcast(Event event) {
        if (sse == null) {
            logger.trace("broadcast skipped (no one listened since activation)");
            return;
        }

        executorService.execute(() -> {
            handleEventBroadcastTopic(event);
        });
    }

    private void addCommonResponseHeaders(final HttpServletResponse response) {
        // Disables proxy buffering when using an nginx http server proxy for this response.
        // This allows you to not disable proxy buffering in nginx and still have working sse
        response.addHeader(X_ACCEL_BUFFERING_HEADER, "no");

        // We want to make sure that the response is not compressed and buffered so that the client receives server sent
        // events at the moment of sending them.
        response.addHeader(HttpHeaders.CONTENT_ENCODING, "identity");

        try {
            response.flushBuffer();
        } catch (final IOException ex) {
            logger.trace("flush buffer failed", ex);
        }
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Operation(operationId = "getEvents", summary = "Get all events.", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Topic is empty or contains invalid characters") })
    public void listen(@Context final SseEventSink sseEventSink, @Context final HttpServletResponse response,
            @QueryParam("topics") @Parameter(description = "topics") String eventFilter) {
        if (!SseUtil.isValidTopicFilter(eventFilter)) {
            response.setStatus(Status.BAD_REQUEST.getStatusCode());
            return;
        }

        topicBroadcaster.add(sseEventSink, new SseSinkTopicInfo(eventFilter));

        addCommonResponseHeaders(response);
    }

    private void handleEventBroadcastTopic(Event event) {
        final EventDTO eventDTO = SseUtil.buildDTO(event);
        final OutboundSseEvent sseEvent = SseUtil.buildEvent(sse.newEventBuilder(), eventDTO);

        topicBroadcaster.sendIf(sseEvent, matchesTopic(eventDTO.topic));
    }
}
