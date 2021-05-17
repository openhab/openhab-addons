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

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.habpanelfilter.internal.HabPanelFilterConfig;
import org.openhab.binding.habpanelfilter.internal.sse.*;
import org.openhab.core.auth.Role;
import org.openhab.core.events.Event;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.openhab.core.io.rest.SseBroadcaster;
import org.openhab.core.io.rest.core.item.EnrichedItemDTO;
import org.openhab.core.io.rest.core.item.EnrichedItemDTOMapper;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.GroupItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.items.events.ItemUpdatedEvent;
import org.openhab.core.types.State;
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
 * @author Unspecified
 */
@Component
@JaxrsResource
@JaxrsName(FilteredEventsResource.PATH_EVENTS_FILTERED)
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path(FilteredEventsResource.PATH_EVENTS_FILTERED)
@Tag(name = FilteredEventsResource.PATH_EVENTS_FILTERED)
@Singleton
@NonNullByDefault
public class FilteredEventsResource implements RESTResource {

    public static final String PATH_EVENTS_FILTERED = "events-filtered";

    private static final String X_ACCEL_BUFFERING_HEADER = "X-Accel-Buffering";

    private final Logger logger = LoggerFactory.getLogger(FilteredEventsResource.class);

    private @NonNullByDefault({}) ItemRegistry itemRegistry;

    private @Context @NonNullByDefault({}) Sse sse;

    private final SseBroadcaster<Object> topicBroadcaster = new SseBroadcaster<>();

    private ExecutorService executorService;

    @Activate
    public FilteredEventsResource(@Reference ItemRegistry itemRegistry) {
        logger.info("FilteredEventsResource created ...");
        this.executorService = Executors.newSingleThreadExecutor();
        this.itemRegistry = itemRegistry;
    }

    @Deactivate
    public void deactivate() {
        topicBroadcaster.close();
        executorService.shutdown();
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
        topicBroadcaster.add(sseEventSink, new Object());
        addCommonResponseHeaders(response);
    }

    private Map<String, State> itemStates = new HashMap<>();

    public void broadcastEvent(final Event event) {
        executorService.execute(new Runnable() {
            private @Nullable Item getItem(String itemname) {
                return itemRegistry.get(itemname);
            }

            private @Nullable OutboundSseEvent buildEvent(Event event) {
                String type = event.getType();
                if (type.equals(ItemStateChangedEvent.TYPE) || type.equals(ItemStateEvent.TYPE)
                        || type.equals(ItemUpdatedEvent.TYPE) || type.equals(GroupItemStateChangedEvent.TYPE)) {

                    EventDTO eventBean = new EventDTO();
                    eventBean.type = event.getType();
                    eventBean.topic = event.getTopic();
                    eventBean.payload = event.getPayload();

                    String[] topicSplits = eventBean.topic.split("/");
                    if (topicSplits.length < 3) {
                        return null;
                    }
                    String itemName = topicSplits[2];
                    Item item = getItem(itemName);
                    if (item == null) {
                        return null;
                    }
                    if (!item.getGroupNames().contains(HabPanelFilterConfig.FILTER_GROUP_NAME)) {
                        return null;
                    }

                    if (!itemStates.containsKey(item.getName()))
                        itemStates.put(item.getName(), item.getState());

                    if (!itemStates.get(item.getName()).equals(item.getState())) {
                        itemStates.replace(item.getName(), item.getState());

                        EnrichedItemDTO dto = EnrichedItemDTOMapper.map(item, true, null, null, Locale.US);
                        eventBean.payload = (new Gson()).toJson(dto);

                        OutboundSseEvent sseEvent = sse.newEventBuilder().name("message").mediaType(MediaType.APPLICATION_JSON_TYPE).data(event)
                                .build();
                        return sseEvent;
                    }
                }
                return null;
            }

            @Override
            public void run() {
                OutboundSseEvent outboundEvent = buildEvent(event);
                if (outboundEvent == null)
                    return;
                topicBroadcaster.send(outboundEvent);
            }
        });
    }
}
