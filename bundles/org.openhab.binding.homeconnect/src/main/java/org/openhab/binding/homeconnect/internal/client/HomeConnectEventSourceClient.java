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
package org.openhab.binding.homeconnect.internal.client;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.listener.HomeConnectEventListener;
import org.openhab.binding.homeconnect.internal.client.model.Event;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-Sent-Events client for Home Connect API.
 *
 * @author Jonas Brüstel - Initial contribution
 * @author Laurent Garnier - Replace okhttp SSE by JAX-RS SSE
 *
 */
@NonNullByDefault
public class HomeConnectEventSourceClient {

    private static final int SSE_REQUEST_READ_TIMEOUT = 90;
    private static final int EVENT_QUEUE_SIZE = 300;

    private final String apiUrl;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final OAuthClientService oAuthClientService;
    private final Map<HomeConnectEventListener, SseEventSource> eventSourceConnections;
    private final Map<SseEventSource, HomeConnectEventSourceListener> eventSourceListeners;
    private final ScheduledExecutorService scheduler;
    private final CircularQueue<Event> eventQueue;

    private final Logger logger = LoggerFactory.getLogger(HomeConnectEventSourceClient.class);

    public HomeConnectEventSourceClient(ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory,
            OAuthClientService oAuthClientService, boolean simulated, ScheduledExecutorService scheduler,
            @Nullable List<Event> eventHistory) {
        this.scheduler = scheduler;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.oAuthClientService = oAuthClientService;

        apiUrl = simulated ? API_SIMULATOR_BASE_URL : API_BASE_URL;
        eventSourceConnections = new HashMap<>();
        eventSourceListeners = new HashMap<>();
        eventQueue = new CircularQueue<>(EVENT_QUEUE_SIZE);
        if (eventHistory != null) {
            eventQueue.addAll(eventHistory);
        }
    }

    /**
     * Register {@link HomeConnectEventListener} to receive events by Home Connect API. This helps to reduce the
     * amount of request you would usually need to update all channels.
     *
     * Checkout rate limits of the API at. https://developer.home-connect.com/docs/general/ratelimiting
     *
     * @param haId appliance id
     * @param eventListener appliance event listener
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     */
    public synchronized void registerEventListener(final String haId, final HomeConnectEventListener eventListener)
            throws CommunicationException, AuthorizationException {
        logger.debug("Register event listener for '{}': {}", haId, eventListener);

        if (!eventSourceConnections.containsKey(eventListener)) {
            logger.debug("Create new event source listener for '{}'.", haId);
            String target = apiUrl + "/api/homeappliances/" + haId + "/events";

            Client client = createClient(target);

            SseEventSource eventSource = eventSourceFactory.newSource(client.target(target));
            HomeConnectEventSourceListener eventSourceListener = new HomeConnectEventSourceListener(haId, eventListener,
                    this, scheduler, eventQueue);
            eventSource.register(eventSourceListener::onEvent, eventSourceListener::onError,
                    eventSourceListener::onComplete);
            eventSourceListeners.put(eventSource, eventSourceListener);
            eventSourceConnections.put(eventListener, eventSource);
            eventSource.open();
        }
    }

    /**
     * Unregister {@link HomeConnectEventListener}.
     *
     * @param eventListener appliance event listener
     */
    public synchronized void unregisterEventListener(HomeConnectEventListener eventListener) {
        unregisterEventListener(eventListener, false, false);
    }

    /**
     * Unregister {@link HomeConnectEventListener}.
     *
     * @param eventListener appliance event listener
     * @param completed true when the event source is known as already completed by the server
     */
    public synchronized void unregisterEventListener(HomeConnectEventListener eventListener, boolean completed) {
        unregisterEventListener(eventListener, false, completed);
    }

    /**
     * Unregister {@link HomeConnectEventListener}.
     *
     * @param eventListener appliance event listener
     * @param immediate true when the unregistering of the event source has to be fast
     * @param completed true when the event source is known as already completed by the server
     */
    public synchronized void unregisterEventListener(HomeConnectEventListener eventListener, boolean immediate,
            boolean completed) {
        if (eventSourceConnections.containsKey(eventListener)) {
            SseEventSource eventSource = eventSourceConnections.get(eventListener);
            if (eventSource != null) {
                closeEventSource(eventSource, immediate, completed);
                eventSourceListeners.remove(eventSource);
            }
            eventSourceConnections.remove(eventListener);
        }
    }

    private void closeEventSource(SseEventSource eventSource, boolean immediate, boolean completed) {
        var open = eventSource.isOpen();
        logger.debug("Closing event source. open={}, completed={}, immediate={}", open, completed, immediate);
        if (open && !completed) {
            eventSource.close(immediate ? 0 : 5, TimeUnit.SECONDS);
            logger.debug("Event source closed.");
        }
        HomeConnectEventSourceListener eventSourceListener = eventSourceListeners.get(eventSource);
        if (eventSourceListener != null) {
            eventSourceListener.stopMonitor();
        }
    }

    private Client createClient(String target) throws CommunicationException, AuthorizationException {
        boolean filterRegistered = clientBuilder.getConfiguration()
                .isRegistered(HomeConnectStreamingRequestFilter.class);

        Client client;
        HomeConnectStreamingRequestFilter filter;
        if (filterRegistered) {
            filter = clientBuilder.getConfiguration().getInstances().stream()
                    .filter(instance -> instance instanceof HomeConnectStreamingRequestFilter)
                    .map(instance -> (HomeConnectStreamingRequestFilter) instance).findAny().orElseThrow();
            client = clientBuilder.readTimeout(SSE_REQUEST_READ_TIMEOUT, TimeUnit.SECONDS).build();
        } else {
            filter = new HomeConnectStreamingRequestFilter();
            client = clientBuilder.readTimeout(SSE_REQUEST_READ_TIMEOUT, TimeUnit.SECONDS).register(filter).build();
        }
        filter.setAuthorizationHeader(target, HttpHelper.getAuthorizationHeader(oAuthClientService));

        return client;
    }

    /**
     * Connection count.
     *
     * @return connection count
     */
    public synchronized int connectionSize() {
        return eventSourceConnections.size();
    }

    /**
     * Dispose event source client
     *
     * @param immediate true to request a fast execution
     */
    public synchronized void dispose(boolean immediate) {
        eventSourceConnections.forEach((key, eventSource) -> closeEventSource(eventSource, immediate, false));
        eventSourceListeners.clear();
        eventSourceConnections.clear();
    }

    /**
     * Get latest events
     *
     * @return event queue
     */
    public Collection<Event> getLatestEvents() {
        return eventQueue.getAll();
    }

    /**
     * Get latest events by haId
     *
     * @param haId appliance id
     * @return event queue
     */
    public List<Event> getLatestEvents(String haId) {
        return eventQueue.getAll().stream().filter(event -> haId.equals(event.getHaId())).collect(Collectors.toList());
    }
}
