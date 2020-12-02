/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webthing.internal.channel.Channels;
import org.openhab.binding.webthing.internal.client.*;
import org.openhab.binding.webthing.internal.link.ChannelToPropertyLink;
import org.openhab.binding.webthing.internal.link.PropertyToChannelLink;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
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
public class WebThingHandler extends BaseThingHandler implements ChannelHandler, ConnectionListener {
    private static final Duration RECONNECT_PERIOD = Duration.ofHours(32);
    private static final Duration HEALTH_CHECK_PERIOD = Duration.ofSeconds(80);
    private static final ItemChangedListener EMPTY_ITEM_CHANGED_LISTENER = (channelUID, stateCommand) -> {
    };

    private final Logger logger = LoggerFactory.getLogger(WebThingHandler.class);
    private final AtomicBoolean isActivated = new AtomicBoolean(true);
    private final Map<ChannelUID, ItemChangedListener> itemChangedListenerMap = new ConcurrentHashMap<>();
    private final AtomicReference<Optional<ConsumedThing>> webThingConnectionRef = new AtomicReference<>(
            Optional.empty());
    private final AtomicReference<Instant> lastReconnect = new AtomicReference<>(Instant.now());
    private final AtomicReference<Optional<AliveWatchdog>> aliveWatchdogRef = new AtomicReference<>(Optional.empty());

    public WebThingHandler(Thing thing) {
        super(thing);
    }

    private boolean isConnected() {
        return (getThing().getStatus() == ThingStatus.ONLINE);
    }

    private boolean isDisconnected() {
        return (getThing().getStatus() == ThingStatus.OFFLINE) || (getThing().getStatus() == ThingStatus.UNKNOWN);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        isActivated.set(true); // set with true, even though the connect may fail. In this case retries will be
                               // triggered

        // perform connect in background
        scheduler.execute(() -> {
            // WebThing URI present?
            var optionalWebThingURI = getWebThingURI();
            if (optionalWebThingURI.isPresent()) {

                logger.info("try to connect WebThing {}", optionalWebThingURI.get());
                var connected = tryReconnect(optionalWebThingURI.get());
                if (connected) {
                    logger.info("WebThing {} connected", getWebThingLabel());
                } else {
                    logger.warn("could not connect WebThing {}. Try it later (each {} sec)", getWebThingLabel(),
                            HEALTH_CHECK_PERIOD.getSeconds());
                }

                // starting alive watchdog that checks the healthiness of the WebThing connection, periodically
                var aliveWatchdog = new AliveWatchdog();
                aliveWatchdog.start();
                aliveWatchdogRef.getAndSet(Optional.of(aliveWatchdog)).ifPresent(AliveWatchdog::destroy);
            } else {
                logger.warn("could not initialize WebThing. URI is not set or invalid. {}",
                        getConfigAs(WebThingConfiguration.class).webThingURI);
            }
        });
    }

    @Override
    public void dispose() {
        try {
            isActivated.set(false); // set to false to avoid reconnecting

            // terminate WebThing connection as well as the alive watchdog
            webThingConnectionRef.getAndSet(Optional.empty()).ifPresent(ConsumedThing::destroy);
            aliveWatchdogRef.getAndSet(Optional.empty()).ifPresent(AliveWatchdog::destroy);
        } finally {
            super.dispose();
        }
    }

    private boolean tryReconnect(URI webThingURI) {
        if (isActivated.get()) { // will try reconnect only, if activated
            try {
                // create the client-side WebThing representation (if success, {@link WebThingHandler#onConnected} will
                // be called, implicitly)
                var webThing = ConsumedThingFactory.instance().create(webThingURI, this);
                this.webThingConnectionRef.getAndSet(Optional.of(webThing)).ifPresent(ConsumedThing::destroy);

                // update the Thing structure based on the WebThing description
                thingStructureChanged(webThing);

                // link the Thing's channels with the WebThing properties to forward properties/item updates
                establishWebThingChannelLinks(webThing);

                lastReconnect.set(Instant.now());
                return true;
            } catch (Exception e) {
                onDisconnected("connecting " + webThingURI + " failed (" + e.getMessage() + ")");
            }
        }
        return false;
    }

    private Optional<URI> getWebThingURI() {
        var webThingConfiguration = getConfigAs(WebThingConfiguration.class);
        var webThingUriString = webThingConfiguration.webThingURI;
        if (webThingUriString == null) {
            logger.warn("webThing uri has not been set");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "webThing uri has not been set");
        } else {
            try {
                return Optional.of(URI.create(webThingUriString));
            } catch (Exception illegalURIException) {
                logger.warn("webThing uri {} is invalid {}", webThingUriString, illegalURIException.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "invalid uri " + webThingUriString);
            }
        }
        return Optional.empty();
    }

    private String getWebThingLabel() {
        var uri = getWebThingURI().map(Object::toString).orElse("");
        if (getThing().getLabel() == null) {
            return uri;
        } else {
            return "'" + getThing().getLabel() + "' (" + uri + ")";
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
            thingBuilder.withoutChannel(channel.getUID()).withChannel(channel);
        }

        var thing = thingBuilder.build();

        // and update the thing
        updateThing(thing);
        logger.debug("thing updated: {}", print(thing));
    }

    private String print(Thing thing) {
        var desc = thing.getLabel();
        desc = desc + "\n - " + thing.getUID().getAsString();
        for (var channel : thing.getChannels()) {
            desc = desc + print(channel);
        }
        return desc;
    }

    private String print(Channel channel) {
        var desc = "\n - " + channel.getLabel();
        desc = desc + "\n    * uid            " + channel.getUID();
        desc = desc + "\n    * type uid       " + channel.getChannelTypeUID();
        desc = desc + "\n    * accepted type: " + channel.getAcceptedItemType();
        for (var entry : channel.getConfiguration().getProperties().entrySet()) {
            desc = desc + "\n    * config:   " + entry.getKey() + "=" + entry.getValue();
        }
        for (var entry : channel.getProperties().entrySet()) {
            desc = desc + "\n    * config:   " + entry.getKey() + "=" + entry.getValue();
        }
        return desc;
    }

    /**
     * connects each WebThing property with a corresponding openHAB channel. After this changes will be synchronized
     * between a WebThing property and the openHAB channe
     *
     * @param webThing the WebThing to be connected
     * @throws IOException if the channels can not be connected
     */
    private void establishWebThingChannelLinks(ConsumedThing webThing) throws IOException {
        // remove all registered listeners
        itemChangedListenerMap.clear();

        // create new links (listeners will be registered, implicitly)
        for (var namePropertyPair : webThing.getThingDescription().properties.entrySet()) {
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
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            try {
                itemChangedListenerMap.getOrDefault(channelUID, EMPTY_ITEM_CHANGED_LISTENER)
                        .onItemStateChanged(channelUID, (State) command);
            } catch (RuntimeException e) {
                logger.warn("updating webthing property with {} failed", command.toString(), e);
            }
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
        postCommand(channelUID, command);
    }
    //
    /////////////

    /////////////
    // ConnectionListener methods
    @Override
    public void onConnected() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onDisconnected(String reason) {
        var wasConnectedBefore = isConnected();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);

        if (wasConnectedBefore) {
            logger.info("WebThing {} disconnected. Try reconnect (each {} sec)", getWebThingLabel(),
                    HEALTH_CHECK_PERIOD.getSeconds());
        }
    }
    //
    /////////////

    private final class AliveWatchdog extends Thread {
        private final AtomicBoolean isRunning = new AtomicBoolean(true);

        AliveWatchdog() {
            setDaemon(true);
        }

        public void destroy() {
            isRunning.set(false);
        }

        @Override
        public void run() {
            while (isRunning.get()) {
                try {
                    // pause
                    try {
                        Thread.sleep(HEALTH_CHECK_PERIOD.toMillis());
                    } catch (InterruptedException ignore) {
                    }

                    // try reconnect, if necessary
                    if (isDisconnected()) {
                        var reconnected = getWebThingURI().map(WebThingHandler.this::tryReconnect).orElse(false);
                        if (reconnected) {
                            logger.info("WebThing {} reconnected", getWebThingLabel());
                        }
                    } else {
                        // force reconnecting periodically, to fix erroneous states that occurs for unknown reasons
                        var elapsedSinceLastReconnect = Duration.between(lastReconnect.get(), Instant.now());
                        var isReconnectRequired = isConnected()
                                && (elapsedSinceLastReconnect.getSeconds() > RECONNECT_PERIOD.getSeconds());
                        if (isReconnectRequired) {
                            var reconnected = getWebThingURI().map(WebThingHandler.this::tryReconnect).orElse(false);
                            if (reconnected) {
                                logger.info("WebThing {} reconnected. Triggered by periodically reconnect (each {} h)",
                                        getWebThingLabel(), RECONNECT_PERIOD.toHours());
                            } else {
                                logger.debug(
                                        "could not reconnect WebThing {} triggered by periodically reconnect (each {} h). Try it later (periodically each {} sec)",
                                        getWebThingLabel(), RECONNECT_PERIOD.toHours(),
                                        HEALTH_CHECK_PERIOD.getSeconds());
                            }

                        }
                    }
                } catch (Exception e) {
                    logger.warn("error occurred by running watchdog task", e);
                }
            }
        }
    }
}
