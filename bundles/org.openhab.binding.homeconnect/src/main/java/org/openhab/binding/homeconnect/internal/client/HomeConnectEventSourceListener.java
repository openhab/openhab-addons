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
package org.openhab.binding.homeconnect.internal.client;

import static java.time.LocalDateTime.now;
import static org.openhab.binding.homeconnect.internal.client.model.EventType.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.sse.InboundSseEvent;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnect.internal.client.listener.HomeConnectEventListener;
import org.openhab.binding.homeconnect.internal.client.model.Event;
import org.openhab.binding.homeconnect.internal.client.model.EventHandling;
import org.openhab.binding.homeconnect.internal.client.model.EventLevel;
import org.openhab.binding.homeconnect.internal.client.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Event source listener (Server-Sent-Events).
 *
 * @author Jonas Brüstel - Initial contribution
 * @author Laurent Garnier - Replace okhttp SSE by JAX-RS SSE
 *
 */
@NonNullByDefault
public class HomeConnectEventSourceListener {
    private static final String EMPTY_DATA = "\"\"";
    private static final int SSE_MONITOR_INITIAL_DELAY = 1;
    private static final int SSE_MONITOR_INTERVAL = 5; // in min
    private static final int SSE_MONITOR_BROKEN_CONNECTION_TIMEOUT = 3; // in min

    private final String haId;
    private final HomeConnectEventListener eventListener;
    private final HomeConnectEventSourceClient client;
    private final Logger logger = LoggerFactory.getLogger(HomeConnectEventSourceListener.class);
    private final JsonParser jsonParser;
    private final ScheduledFuture<?> eventSourceMonitorFuture;
    private final CircularQueue<Event> eventQueue;

    private @Nullable LocalDateTime lastEventReceived;

    public HomeConnectEventSourceListener(String haId, final HomeConnectEventListener eventListener,
            final HomeConnectEventSourceClient client, final ScheduledExecutorService scheduler,
            CircularQueue<Event> eventQueue) {
        this.haId = haId;
        this.eventListener = eventListener;
        this.client = client;
        this.eventQueue = eventQueue;
        jsonParser = new JsonParser();

        eventSourceMonitorFuture = createMonitor(scheduler);
    }

    public void onEvent(InboundSseEvent inboundEvent) {
        @Nullable
        String id = inboundEvent.getId();
        @Nullable
        String type = inboundEvent.getName();
        @Nullable
        String data = inboundEvent.readData();

        lastEventReceived = now();

        @Nullable
        EventType eventType = valueOfType(type);
        if (eventType != null) {
            mapEventSourceEventToEvent(haId, eventType, data).forEach(event -> {
                eventQueue.add(event);
                logger.debug("Received event ({}): {}", haId, event);
                try {
                    eventListener.onEvent(event);
                } catch (Exception e) {
                    logger.error("Could not publish event to Listener!", e);
                }
            });
        } else {
            logger.warn("Received unknown event source type! haId={}, id={}, type={}, data={}", haId, id, type, data);
        }
    }

    public void onComplete() {
        logger.debug("Event source listener channel closed ({}).", haId);

        client.unregisterEventListener(eventListener);

        try {
            eventListener.onClosed();
        } catch (Exception e) {
            logger.error("Could not publish closed event to listener ({})!", haId, e);
        }
        stopMonitor();
    }

    public void onError(Throwable error) {
        @Nullable
        String throwableMessage = error.getMessage();
        String throwableClass = error.getClass().getName();

        logger.debug("Event source listener connection failure occurred. haId={}, throwable={}, throwableMessage={}",
                haId, throwableClass, throwableMessage);

        client.unregisterEventListener(eventListener);

        try {
            if (throwableMessage != null
                    && throwableMessage.contains(String.valueOf(HttpStatus.TOO_MANY_REQUESTS_429))) {
                logger.warn(
                        "More than 10 active event monitoring channels was reached. Further event monitoring requests are blocked. haId={}",
                        haId);
                eventListener.onRateLimitReached();
            } else {
                // The SSE connection is closed by the server every 24 hours.
                // When you try to reconnect, it often fails with a NotAuthorizedException (401) for the next few
                // seconds. So we wait few seconds before trying again.
                if (error instanceof NotAuthorizedException) {
                    logger.debug(
                            "Event source listener connection failure due to unauthorized exception : wait 5 seconds... haId={}",
                            haId);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                    }
                }
                eventListener.onClosed();
            }
        } catch (Exception e) {
            logger.error("Could not publish closed event to listener ({})!", haId, e);
        }
        stopMonitor();
    }

    private ScheduledFuture<?> createMonitor(ScheduledExecutorService scheduler) {
        return scheduler.scheduleWithFixedDelay(() -> {
            logger.trace("Check event source connection ({}). Last event package received at {}.", haId,
                    lastEventReceived);
            if (lastEventReceived != null
                    && ChronoUnit.MINUTES.between(lastEventReceived, now()) > SSE_MONITOR_BROKEN_CONNECTION_TIMEOUT) {
                logger.warn("Dead event source connection detected ({}).", haId);

                client.unregisterEventListener(eventListener);

                try {
                    eventListener.onClosed();
                } catch (Exception e) {
                    logger.error("Could not publish closed event to listener ({})!", haId, e);
                }
                stopMonitor();
            }
        }, SSE_MONITOR_INITIAL_DELAY, SSE_MONITOR_INTERVAL, TimeUnit.MINUTES);
    }

    public void stopMonitor() {
        if (!eventSourceMonitorFuture.isDone()) {
            logger.debug("Dispose event source connection monitor of appliance ({}).", haId);
            eventSourceMonitorFuture.cancel(true);
        }
    }

    private List<Event> mapEventSourceEventToEvent(String haId, EventType type, @Nullable String data) {
        List<Event> events = new ArrayList<>();

        if ((STATUS.equals(type) || EVENT.equals(type) || NOTIFY.equals(type)) && data != null && !data.trim().isEmpty()
                && !EMPTY_DATA.equals(data)) {
            try {
                JsonObject responseObject = jsonParser.parse(data).getAsJsonObject();
                JsonArray items = responseObject.getAsJsonArray("items");

                items.forEach(item -> {
                    JsonObject obj = (JsonObject) item;
                    @Nullable
                    String key = obj.get("key") != null ? obj.get("key").getAsString() : null;
                    @Nullable
                    String value = obj.get("value") != null && !obj.get("value").isJsonNull()
                            ? obj.get("value").getAsString()
                            : null;
                    @Nullable
                    String unit = obj.get("unit") != null ? obj.get("unit").getAsString() : null;
                    @Nullable
                    String name = obj.get("name") != null ? obj.get("name").getAsString() : null;
                    @Nullable
                    String uri = obj.get("uri") != null ? obj.get("uri").getAsString() : null;
                    @Nullable
                    EventLevel level = obj.get("level") != null
                            ? EventLevel.valueOfLevel(obj.get("level").getAsString())
                            : null;
                    @Nullable
                    EventHandling handling = obj.get("handling") != null
                            ? EventHandling.valueOfHandling(obj.get("handling").getAsString())
                            : null;
                    @Nullable
                    Long timestamp = obj.get("timestamp") != null ? obj.get("timestamp").getAsLong() : null;
                    @Nullable
                    ZonedDateTime creation = timestamp != null
                            ? ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp),
                                    TimeZone.getDefault().toZoneId())
                            : null;

                    events.add(new Event(haId, type, key, name, uri, creation, level, handling, value, unit));
                });
            } catch (IllegalStateException e) {
                logger.error("Could not parse event! haId={}, error={}", haId, e.getMessage());
            }
        } else {
            events.add(new Event(haId, type));
        }

        return events;
    }
}
