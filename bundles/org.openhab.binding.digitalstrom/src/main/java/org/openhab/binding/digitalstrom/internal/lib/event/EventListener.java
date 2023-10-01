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
package org.openhab.binding.digitalstrom.internal.lib.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.event.types.Event;
import org.openhab.binding.digitalstrom.internal.lib.event.types.EventItem;
import org.openhab.binding.digitalstrom.internal.lib.event.types.JSONEventImpl;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.impl.JSONResponseHandler;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link EventListener} listens for events which will be thrown by the digitalSTROM-Server and it notifies the
 * added {@link EventHandler} about the detected events, if it supports the event-type.<br>
 * You can add {@link EventHandler}'s through the constructors or the methods {@link #addEventHandler(EventHandler)} and
 * {@link #addEventHandlers(List)}.<br>
 * You can also delete an {@link EventHandler} though the method {@link #removeEventHandler(EventHandler)}.<br>
 * If the {@link EventListener} is started, both methods subscribe respectively unsubscribe the event-types of the
 * {@link EventHandler}/s automatically.<br>
 * If you want to dynamically subscribe event-types, e.g. because a configuration has changed and a
 * {@link EventHandler} needs to be informed of another event-type, you can use the methods
 * {@link #addSubscribe(String)} or {@link #addSubscribeEvents(List)} to add more than one event-type. To remove a
 * subscribed event you can use the method {@link #removeSubscribe(String, String)}, you also have to change the return
 * of the {@link org.openhab.binding.digitalstrom.internal.lib.event.EventHandler} methods
 * {@link EventHandler#getSupportedEvents()} and
 * {@link EventHandler#supportsEvent(String)}.
 * <br>
 * To start and stop the listening you have to call the methods {@link #start()} and {@link #stop()}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class EventListener {

    private final Logger logger = LoggerFactory.getLogger(EventListener.class);
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME);
    public static final int SUBSCRIBE_DELAY = 500;
    private ScheduledFuture<?> pollingScheduler;
    private ScheduledFuture<?> subscriptionScheduler;

    private int subscriptionID = 15;
    private final int timeout = 500;
    private final List<String> subscribedEvents = Collections.synchronizedList(new LinkedList<>());
    private boolean subscribed = false;

    // error message
    public static final String INVALID_SESSION = "Invalid session!";
    public static final String TOKEN_NOT_FOUND = "token not found."; // Complete text: "Event subscription token not
                                                                     // found."

    private final ConnectionManager connManager;
    private final List<EventHandler> eventHandlers = Collections.synchronizedList(new LinkedList<>());
    private final Config config;
    private boolean isStarted = false;

    /**
     * Creates a new {@link EventListener} to listen to the supported event-types of the given eventHandler and notify
     * about a detected event.<br>
     * <br>
     * To get notified by events you have to call {@link #start()}.
     *
     * @param connectionManager must not be null
     * @param eventHandler must not be null
     */
    public EventListener(ConnectionManager connectionManager, EventHandler eventHandler) {
        this.connManager = connectionManager;
        this.config = connectionManager.getConfig();
        addEventHandler(eventHandler);
    }

    /**
     * This constructor can add more than one {@link EventHandler} as a list of {@link EventHandler}'s.
     *
     * @param connectionManager must not be null
     * @param eventHandlers list of {@link EventHandler}'s must not be null
     * @see #EventListener(ConnectionManager, EventHandler)
     */
    public EventListener(ConnectionManager connectionManager, List<EventHandler> eventHandlers) {
        this.connManager = connectionManager;
        this.config = connectionManager.getConfig();
        addEventHandlers(eventHandlers);
    }

    /**
     * Creates a new {@link EventListener} without an {@link EventHandler}<br>
     * <br>
     * To get notified by events you have to call {@link #start()} and {@link #addEventHandler(EventHandler)} or
     * {@link #addEventHandlers(List)}.
     *
     * @param connectionManager must not be null
     */
    public EventListener(ConnectionManager connectionManager) {
        this.connManager = connectionManager;
        this.config = connectionManager.getConfig();
    }

    /**
     * Stops this {@link EventListener} and unsubscribe events.
     */
    public synchronized void stop() {
        logger.debug("Stop EventListener");
        isStarted = false;
        internalStop();
    }

    /**
     * Returns true, if the {@link EventListener} is started.
     *
     * @return true, if is started
     */
    public boolean isStarted() {
        return isStarted;
    }

    private void stopSubscriptionScheduler() {
        final ScheduledFuture<?> subscriptionScheduler = this.subscriptionScheduler;
        if (subscriptionScheduler != null) {
            subscriptionScheduler.cancel(true);
            this.subscriptionScheduler = null;
        }
    }

    private void internalStop() {
        stopSubscriptionScheduler();

        ScheduledFuture<?> pollingScheduler = this.pollingScheduler;
        if (pollingScheduler != null) {
            pollingScheduler.cancel(true);
            this.pollingScheduler = null;
            unsubscribe();
            logger.debug("internal stop EventListener");
        }
    }

    /**
     * Starts this {@link EventListener} and subscribe events.
     */
    public synchronized void start() {
        logger.debug("Start EventListener");
        isStarted = true;
        internalStart();
    }

    private void internalStart() {
        if (!eventHandlers.isEmpty() && (pollingScheduler == null || pollingScheduler.isCancelled())) {
            pollingScheduler = scheduler.scheduleWithFixedDelay(runableListener, 0,
                    config.getEventListenerRefreshinterval(), TimeUnit.MILLISECONDS);
            logger.debug("internal start EventListener");
        }
    }

    /**
     * Adds a {@link List} of {@link EventHandler}'s and subscribe the supported event-types, if the
     * {@link EventListener} is started and the event-types are not already subscribed.
     *
     * @param eventHandlers to add
     */
    public void addEventHandlers(List<EventHandler> eventHandlers) {
        if (eventHandlers != null) {
            for (EventHandler eventHandler : eventHandlers) {
                addEventHandler(eventHandler);
            }
        }
    }

    /**
     * Adds an {@link EventHandler}'s and subscribe the supported event-types, if the
     * {@link EventListener} is started and the event-types are not already subscribed.<br>
     * <br>
     * <b>Note:</b><br>
     * If {@link #start()} was called before the {@link EventListener} will start now, otherwise you have to call
     * {@link #start()} to get notified by events.
     *
     * @param eventHandler to add
     */
    public void addEventHandler(EventHandler eventHandler) {
        if (eventHandler != null) {
            boolean handlerExist = false;
            for (EventHandler handler : eventHandlers) {
                if (handler.getUID().equals(eventHandler.getUID())) {
                    handlerExist = true;
                }
            }
            if (!handlerExist) {
                eventHandlers.add(eventHandler);
                addSubscribeEvents(eventHandler.getSupportedEvents());
                logger.debug("eventHandler: {} added", eventHandler.getUID());
                if (isStarted) {
                    internalStart();
                }
            }
        }
    }

    /**
     * Remove an {@link EventHandler} and unsubscribes the supported event-types, if the
     * {@link EventListener} is started and no other {@link EventHandler} needed the event-types.
     *
     * @param eventHandler to remove
     */
    public void removeEventHandler(EventHandler eventHandler) {
        if (eventHandler != null && eventHandlers.contains(eventHandler)) {
            List<String> tempSubsList = new ArrayList<>();
            int index = -1;
            EventHandler intEventHandler = null;
            boolean subscribedEventsChanged = false;
            for (int i = 0; i < eventHandlers.size(); i++) {
                intEventHandler = eventHandlers.get(i);
                if (intEventHandler.getUID().equals(eventHandler.getUID())) {
                    index = i;
                } else {
                    tempSubsList.addAll(intEventHandler.getSupportedEvents());
                }
            }
            if (index != -1) {
                intEventHandler = eventHandlers.remove(index);
                for (String eventName : intEventHandler.getSupportedEvents()) {
                    if (!tempSubsList.contains(eventName)) {
                        subscribedEvents.remove(eventName);
                        subscribedEventsChanged = true;
                    }
                }
            }
            if (subscribedEventsChanged) {
                // Because of the json-call unsubscribe?eventName=XY&subscriptionID=Z doesn't work like it is explained
                // in the dS-JSON-API, the whole EventListener will be restarted. The problem is, that not only the
                // given eventName, rather all events of the subscitionID will be deleted.
                restartListener();
            }
        }
    }

    /**
     * Removes a subscribed event and unsubscibe it, if it is not needed by other {@link EventHandler}'s.
     *
     * @param unsubscribeEvent event name to unsubscibe
     * @param eventHandlerID EventHandler-ID of the EventHandler that unsubscibe an event
     */
    public void removeSubscribe(String unsubscribeEvent, String eventHandlerID) {
        if (subscribedEvents != null && !subscribedEvents.contains(unsubscribeEvent)) {
            boolean eventNeededByAnotherHandler = false;
            for (EventHandler handler : eventHandlers) {
                if (!handler.getUID().equals(eventHandlerID)) {
                    eventNeededByAnotherHandler = handler.getSupportedEvents().contains(unsubscribeEvent);
                }
            }
            if (!eventNeededByAnotherHandler) {
                logger.debug("unsubscribeEvent: {} is not needed by other EventHandler's... unsubscribe it",
                        unsubscribeEvent);
                subscribedEvents.remove(unsubscribeEvent);
                restartListener();
            } else {
                logger.debug("unsubscribeEvent: {} is needed by other EventHandler's... dosen't unsubscribe it",
                        unsubscribeEvent);
            }
        }
    }

    private void restartListener() {
        internalStop();
        if (!eventHandlers.isEmpty() && isStarted) {
            logger.debug("Min one subscribed events was deleted, EventListener will be restarted");
            internalStart();
        }
    }

    /**
     * Adds an event and subscribes it, if it is not subscribed already.
     *
     * @param subscribeEvent event name to subscribe
     */
    public void addSubscribe(String subscribeEvent) {
        if (!subscribedEvents.contains(subscribeEvent)) {
            subscribedEvents.add(subscribeEvent);
            logger.debug("subscibeEvent: {} added", subscribeEvent);
            if (subscribed) {
                subscribe(subscribeEvent);
            }
        }
    }

    /**
     * Adds the events of the {@link List} and subscribe them, if an event is not subscribed already.
     *
     * @param subscribeEvents event name to subscribe
     */
    public void addSubscribeEvents(List<String> subscribeEvents) {
        for (String eventName : subscribeEvents) {
            subscribe(eventName);
        }
    }

    private void getSubscriptionID() {
        boolean subscriptionIDavailable = false;
        while (!subscriptionIDavailable) {
            String response = connManager.getDigitalSTROMAPI().getEvent(connManager.getSessionToken(), subscriptionID,
                    timeout);

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                subscriptionID++;
            } else {
                String errorStr = null;
                if (responseObj != null && responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()) != null) {
                    errorStr = responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()).getAsString();
                }
                if (errorStr != null && errorStr.contains(TOKEN_NOT_FOUND)) {
                    subscriptionIDavailable = true;
                }
            }
        }
    }

    private boolean subscribe(String eventName) {
        if (!subscribed) {
            getSubscriptionID();
        }
        subscribed = connManager.getDigitalSTROMAPI().subscribeEvent(connManager.getSessionToken(), eventName,
                subscriptionID, config.getConnectionTimeout(), config.getReadTimeout());

        if (subscribed) {
            logger.debug("subscribed event: {} to subscriptionID: {}", eventName, subscriptionID);
        } else {
            logger.error(
                    "Couldn't subscribe event {} ... maybe timeout because system is too busy ... event will be tried to subscribe later again ... ",
                    eventName);
        }
        return subscribed;
    }

    private void subscribe(final List<String> eventNames) {
        subscriptionScheduler = scheduler.scheduleWithFixedDelay(() -> {
            eventNames.forEach(this::subscribe);
            stopSubscriptionScheduler();
        }, 0, SUBSCRIBE_DELAY, TimeUnit.MILLISECONDS);
    }

    private final Runnable runableListener = new Runnable() {

        @Override
        public void run() {
            if (subscribed) {
                String response = connManager.getDigitalSTROMAPI().getEvent(connManager.getSessionToken(),
                        subscriptionID, timeout);
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    if (obj != null && obj.get(JSONApiResponseKeysEnum.EVENTS.getKey()).isJsonArray()) {
                        JsonArray array = obj.get(JSONApiResponseKeysEnum.EVENTS.getKey()).getAsJsonArray();
                        handleEvent(array);
                    }
                } else {
                    String errorStr = null;
                    if (responseObj != null && responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()) != null) {
                        errorStr = responseObj.get(JSONApiResponseKeysEnum.MESSAGE.getKey()).getAsString();
                    }
                    if (errorStr != null && (errorStr.equals(INVALID_SESSION) || errorStr.contains(TOKEN_NOT_FOUND))) {
                        unsubscribe();
                        subscribe(subscribedEvents);
                    } else if (errorStr != null) {
                        pollingScheduler.cancel(true);
                        logger.error("Unknown error message at event response: {}", errorStr);
                    }
                }
            } else {
                subscribe(subscribedEvents);
            }
        }
    };

    private void unsubscribe() {
        for (String eventName : this.subscribedEvents) {
            connManager.getDigitalSTROMAPI().unsubscribeEvent(this.connManager.getSessionToken(), eventName,
                    this.subscriptionID, config.getConnectionTimeout(), config.getReadTimeout());
        }
    }

    private void handleEvent(JsonArray array) {
        if (array.size() > 0) {
            Event event = new JSONEventImpl(array);
            for (EventItem item : event.getEventItems()) {
                logger.debug("detect event {}", item.toString());
                for (EventHandler handler : eventHandlers) {
                    if (handler.supportsEvent(item.getName())) {
                        logger.debug("inform handler with id {} about event {}", handler.getUID(), item.toString());
                        handler.handleEvent(item);
                    }
                }
            }
        }
    }
}
