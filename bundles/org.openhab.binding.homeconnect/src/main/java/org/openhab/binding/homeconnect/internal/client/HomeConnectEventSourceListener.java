/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.InternalServerErrorException;
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
    private static final int SSE_MONITOR_INITIAL_DELAY_MIN = 1;
    private static final int SSE_MONITOR_INTERVAL_MIN = 5;
    private static final int SSE_MONITOR_BROKEN_CONNECTION_TIMEOUT_MIN = 3;

    private final String haId;
    private final HomeConnectEventListener eventListener;
    private final HomeConnectEventSourceClient client;
    private final Logger logger = LoggerFactory.getLogger(HomeConnectEventSourceListener.class);
    private final ScheduledFuture<?> eventSourceMonitorFuture;
    private final CircularQueue<Event> eventQueue;
    private final ScheduledExecutorService scheduledExecutorService;

    private @Nullable LocalDateTime lastEventReceived;

    public HomeConnectEventSourceListener(String haId, final HomeConnectEventListener eventListener,
            final HomeConnectEventSourceClient client, final ScheduledExecutorService scheduler,
            CircularQueue<Event> eventQueue) {
        this.haId = haId;
        this.eventListener = eventListener;
        this.client = client;
        this.eventQueue = eventQueue;
        this.scheduledExecutorService = scheduler;

        eventSourceMonitorFuture = createMonitor(scheduler);
    }

    public void onEvent(InboundSseEvent inboundEvent) {
        String id = inboundEvent.getId();
        String type = inboundEvent.getName();
        String data = inboundEvent.readData();

        lastEventReceived = now();

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

        client.unregisterEventListener(eventListener, true);

        try {
            eventListener.onClosed();
        } catch (Exception e) {
            logger.error("Could not publish closed event to listener ({})!", haId, e);
        }
        stopMonitor();
    }

    public void onError(Throwable error) {
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
                            "Event source listener connection failure due to unauthorized exception : wait 20 seconds... haId={}",
                            haId);
                    scheduledExecutorService.schedule(() -> eventListener.onClosed(), 20, TimeUnit.SECONDS);
                } else if (error instanceof InternalServerErrorException) {
                    logger.debug(
                            "Event source listener connection failure due to internal server exception : wait 2 seconds... haId={}",
                            haId);
                    scheduledExecutorService.schedule(() -> eventListener.onClosed(), 2, TimeUnit.SECONDS);
                } else {
                    eventListener.onClosed();
                }
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
            if (lastEventReceived != null && ChronoUnit.MINUTES.between(lastEventReceived,
                    now()) > SSE_MONITOR_BROKEN_CONNECTION_TIMEOUT_MIN) {
                logger.warn("Dead event source connection detected ({}).", haId);

                client.unregisterEventListener(eventListener);

                try {
                    eventListener.onClosed();
                } catch (Exception e) {
                    logger.error("Could not publish closed event to listener ({})!", haId, e);
                }
                stopMonitor();
            }
        }, SSE_MONITOR_INITIAL_DELAY_MIN, SSE_MONITOR_INTERVAL_MIN, TimeUnit.MINUTES);
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
                JsonObject responseObject = HttpHelper.parseString(data).getAsJsonObject();
                JsonArray items = responseObject.getAsJsonArray("items");

                items.forEach(item -> {
                    JsonObject obj = (JsonObject) item;
                    String key = getJsonElementAsString(obj, "key").orElse(null);
                    String value = getJsonElementAsString(obj, "value").orElse(null);
                    String unit = getJsonElementAsString(obj, "unit").orElse(null);
                    String name = getJsonElementAsString(obj, "name").orElse(null);
                    String uri = getJsonElementAsString(obj, "uri").orElse(null);
                    EventLevel level = getJsonElementAsString(obj, "level").map(EventLevel::valueOfLevel).orElse(null);
                    EventHandling handling = getJsonElementAsString(obj, "handling").map(EventHandling::valueOfHandling)
                            .orElse(null);
                    ZonedDateTime creation = getJsonElementAsLong(obj, "timestamp").map(timestamp -> ZonedDateTime
                            .ofInstant(Instant.ofEpochSecond(timestamp), TimeZone.getDefault().toZoneId()))
                            .orElse(ZonedDateTime.now());

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

    private Optional<Long> getJsonElementAsLong(JsonObject jsonObject, String elementName) {
        var element = jsonObject.get(elementName);
        return element == null || element.isJsonNull() ? Optional.empty() : Optional.of(element.getAsLong());
    }

    private Optional<String> getJsonElementAsString(JsonObject jsonObject, String elementName) {
        var element = jsonObject.get(elementName);
        return element == null || element.isJsonNull() ? Optional.empty() : Optional.of(element.getAsString());
    }
}
