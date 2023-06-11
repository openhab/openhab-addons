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
package org.openhab.binding.webthing.internal;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.webthing.internal.channel.Channels;
import org.openhab.binding.webthing.internal.client.ConsumedThing;
import org.openhab.binding.webthing.internal.client.ConsumedThingFactory;
import org.openhab.binding.webthing.internal.link.ChannelToPropertyLink;
import org.openhab.binding.webthing.internal.link.PropertyToChannelLink;
import org.openhab.binding.webthing.internal.link.UnknownPropertyException;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class WebThingHandler extends BaseThingHandler implements ChannelHandler {
    private static final Duration RECONNECT_PERIOD = Duration.ofHours(23);
    private static final Duration HEALTH_CHECK_PERIOD = Duration.ofSeconds(70);
    private static final ItemChangedListener EMPTY_ITEM_CHANGED_LISTENER = (channelUID, stateCommand) -> {
    };

    private final Logger logger = LoggerFactory.getLogger(WebThingHandler.class);
    private final HttpClient httpClient;
    private final WebSocketClient webSocketClient;
    private final AtomicBoolean isActivated = new AtomicBoolean(true);
    private final Map<ChannelUID, ItemChangedListener> itemChangedListenerMap = new ConcurrentHashMap<>();
    private final AtomicReference<Optional<ConsumedThing>> webThingConnectionRef = new AtomicReference<>(
            Optional.empty());
    private final AtomicReference<Instant> lastReconnect = new AtomicReference<>(Instant.now());
    private final AtomicReference<Optional<ScheduledFuture<?>>> watchdogHandle = new AtomicReference<>(
            Optional.empty());
    private @Nullable URI webThingURI = null;

    public WebThingHandler(Thing thing, HttpClient httpClient, WebSocketClient webSocketClient) {
        super(thing);
        this.httpClient = httpClient;
        this.webSocketClient = webSocketClient;
    }

    private boolean isOnline() {
        return getThing().getStatus() == ThingStatus.ONLINE;
    }

    private boolean isDisconnected() {
        return (getThing().getStatus() == ThingStatus.OFFLINE) || (getThing().getStatus() == ThingStatus.UNKNOWN);
    }

    private boolean isAlive() {
        return webThingConnectionRef.get().map(ConsumedThing::isAlive).orElse(false);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        isActivated.set(true); // set with true, even though the connect may fail. In this case retries will be
                               // triggered

        // perform connect in background
        scheduler.execute(() -> {
            // WebThing URI present?
            if (getWebThingURI() != null) {
                logger.debug("try to connect WebThing {}", webThingURI);
                var connected = tryReconnect();
                if (connected) {
                    logger.debug("WebThing {} connected", getWebThingLabel());
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "webThing uri has not been set");
                logger.warn("could not initialize WebThing. URI is not set or invalid. {}", this.webThingURI);
            }
        });

        // starting watchdog that checks the healthiness of the WebThing connection, periodically
        watchdogHandle
                .getAndSet(Optional.of(scheduler.scheduleWithFixedDelay(this::checkWebThingConnection,
                        HEALTH_CHECK_PERIOD.getSeconds(), HEALTH_CHECK_PERIOD.getSeconds(), TimeUnit.SECONDS)))
                .ifPresent(future -> future.cancel(true));
    }

    private @Nullable URI getWebThingURI() {
        if (webThingURI == null) {
            webThingURI = toUri(getConfigAs(WebThingConfiguration.class).webThingURI);
        }
        return webThingURI;
    }

    private @Nullable URI toUri(@Nullable String uri) {
        try {
            if (uri != null) {
                return URI.create(uri);
            }
        } catch (IllegalArgumentException illegalURIException) {
            return null;
        }
        return null;
    }

    @Override
    public void dispose() {
        try {
            isActivated.set(false); // set to false to avoid reconnecting

            // terminate WebThing connection as well as the alive watchdog
            webThingConnectionRef.getAndSet(Optional.empty()).ifPresent(ConsumedThing::close);
            watchdogHandle.getAndSet(Optional.empty()).ifPresent(future -> future.cancel(true));
        } finally {
            super.dispose();
        }
    }

    private boolean tryReconnect() {
        if (isActivated.get()) { // will try reconnect only, if activated
            try {
                // create the client-side WebThing representation
                var uri = getWebThingURI();
                if (uri != null) {
                    var webThing = ConsumedThingFactory.instance().create(webSocketClient, httpClient, uri, scheduler,
                            this::onError);
                    this.webThingConnectionRef.getAndSet(Optional.of(webThing)).ifPresent(ConsumedThing::close);

                    // update the Thing structure based on the WebThing description
                    thingStructureChanged(webThing);

                    // link the Thing's channels with the WebThing properties to forward properties/item updates
                    establishWebThingChannelLinks(webThing);

                    lastReconnect.set(Instant.now());
                    updateStatus(ThingStatus.ONLINE);
                    return true;
                }
            } catch (IOException e) {
                var msg = e.getMessage();
                if (msg == null) {
                    msg = "";
                }
                onError(msg);
            }
        }
        return false;
    }

    public void onError(String reason) {
        var wasConnectedBefore = isOnline();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);

        // close the WebThing connection. If the handler is still active, the WebThing connection
        // will be re-established within the periodically watchdog task
        webThingConnectionRef.getAndSet(Optional.empty()).ifPresent(ConsumedThing::close);

        if (wasConnectedBefore) { // to reduce log messages, just log in case of connection state changed
            logger.debug("WebThing {} disconnected {}. Try reconnect (each {} sec)", getWebThingLabel(), reason,
                    HEALTH_CHECK_PERIOD.getSeconds());
        } else {
            logger.debug("WebThing {} is offline {}. Try reconnect (each {} sec)", getWebThingLabel(), reason,
                    HEALTH_CHECK_PERIOD.getSeconds());
        }
    }

    private String getWebThingLabel() {
        if (getThing().getLabel() == null) {
            return "" + webThingURI;
        } else {
            return "'" + getThing().getLabel() + "' (" + webThingURI + ")";
        }
    }

    /**
     * updates the thing structure. Refer https://www.openhab.org/docs/developer/bindings/#updating-the-thing-structure
     *
     * @param webThing the WebThing that is used for the new structure
     */
    private void thingStructureChanged(ConsumedThing webThing) {
        var thingBuilder = editThing().withLabel(webThing.getThingDescription().title);

        // create a channel for each WebThing property
        for (var entry : webThing.getThingDescription().properties.entrySet()) {
            var channel = Channels.createChannel(thing.getUID(), entry.getKey(), entry.getValue());
            // add channel (and remove a previous one, if exist)
            thingBuilder.withoutChannel(channel.getUID()).withChannel(channel);
        }
        var thing = thingBuilder.build();

        // and update the thing
        updateThing(thing);
    }

    /**
     * connects each WebThing property with a corresponding openHAB channel. After this changes will be synchronized
     * between a WebThing property and the openHAB channel
     *
     * @param webThing the WebThing to be connected
     * @throws IOException if the channels can not be connected
     */
    private void establishWebThingChannelLinks(ConsumedThing webThing) throws IOException {
        // remove all registered listeners
        itemChangedListenerMap.clear();

        // create new links (listeners will be registered, implicitly)
        for (var namePropertyPair : webThing.getThingDescription().properties.entrySet()) {
            try {
                // determine the name of the associated channel
                var channelUID = Channels.createChannelUID(getThing().getUID(), namePropertyPair.getKey());

                // will try to establish a link, if channel is present
                var channel = getThing().getChannel(channelUID);
                if (channel != null) {
                    // establish downstream link
                    PropertyToChannelLink.establish(webThing, namePropertyPair.getKey(), this, channel);

                    // establish upstream link
                    if (!namePropertyPair.getValue().readOnly) {
                        ChannelToPropertyLink.establish(this, channel, webThing, namePropertyPair.getKey());
                    }
                }
            } catch (UnknownPropertyException upe) {
                logger.warn("WebThing {} property {} could not be linked with a channel", getWebThingLabel(),
                        namePropertyPair.getKey(), upe);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            itemChangedListenerMap.getOrDefault(channelUID, EMPTY_ITEM_CHANGED_LISTENER).onItemStateChanged(channelUID,
                    (State) command);
        } else if (command instanceof RefreshType) {
            tryReconnect();
        }
    }

    /////////////
    // ChannelHandler methods
    @Override
    public void observeChannel(ChannelUID channelUID, ItemChangedListener listener) {
        itemChangedListenerMap.put(channelUID, listener);
    }

    @Override
    public void updateItemState(ChannelUID channelUID, Command command) {
        if (isActivated.get()) {
            postCommand(channelUID, command);
        }
    }
    //
    /////////////

    private void checkWebThingConnection() {
        // try reconnect, if necessary
        if (isDisconnected() || (isOnline() && !isAlive())) {
            logger.debug("try reconnecting WebThing {}", getWebThingLabel());
            if (tryReconnect()) {
                logger.debug("WebThing {} reconnected", getWebThingLabel());
            }

        } else {
            // force reconnecting periodically, to fix erroneous states that occurs for unknown reasons
            var elapsedSinceLastReconnect = Duration.between(lastReconnect.get(), Instant.now());
            if (isOnline() && (elapsedSinceLastReconnect.getSeconds() > RECONNECT_PERIOD.getSeconds())) {
                if (tryReconnect()) {
                    logger.debug("WebThing {} reconnected. Initiated by periodic reconnect", getWebThingLabel());
                } else {
                    logger.debug("could not reconnect WebThing {} (periodic reconnect failed). Next trial in {} sec",
                            getWebThingLabel(), HEALTH_CHECK_PERIOD.getSeconds());
                }

            }
        }
    }
}
