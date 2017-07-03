/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sony.internal.SonyUtility;
import org.openhab.binding.sony.internal.StatefulHandlerCallback;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.dial.models.DialApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The thing handler for a Sony DIAL device. This is the entry point provides a full two interaction between openhab
 * and the ircc system.
 *
 * @author Tim Roberts - Initial contribution
 *
 */
public class DialHandler extends BaseThingHandler {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(DialHandler.class);

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<DialProtocol<StatefulHandlerCallback<String>>> _protocolHandler = new AtomicReference<DialProtocol<StatefulHandlerCallback<String>>>(
            null);

    /** The refresh state event - will only be created when we are connected. */
    private final AtomicReference<ScheduledFuture<?>> _refreshState = new AtomicReference<ScheduledFuture<?>>(null);

    /** The refresh state event - will only be created when we are connected. */
    private final AtomicReference<ScheduledFuture<?>> _retryConnection = new AtomicReference<ScheduledFuture<?>>(null);

    /**
     * Constructs the handler from the {@link Thing}.
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public DialHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link DialProtocol}. Basically we validate the type of command for the channel then call the
     * {@link DialProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link DialProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID);
            return;
        }

        final String id = channelUID.getId();

        if (id == null) {
            logger.warn("Called with a null channel id - ignoring");
            return;
        }

        final DialProtocol<StatefulHandlerCallback<String>> protocolHandler = _protocolHandler.get();
        if (protocolHandler == null) {
            logger.warn("Trying to handle a channel command before a protocol handler has been created");
            return;
        }

        if (id.startsWith(DialConstants.CHANNEL_STATE)) {
            if (command instanceof OnOffType) {
                protocolHandler.setState(getAppId(id), OnOffType.ON == command);
            } else {
                logger.warn("Received a STATE channel command with a non OnOffType: {}", command);
            }

        } else {
            logger.warn("Unknown/Unsupported Channel id: {}", id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link DialProtocol} to
     * handle the actual refresh based on the channel id.
     *
     * @param channelId the channel id
     */
    private void handleRefresh(ChannelUID channelId) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        final DialProtocol<StatefulHandlerCallback<String>> protocolHandler = _protocolHandler.get();
        if (protocolHandler == null) {
            logger.warn("Trying to handle a refresh command before a protocol handler has been created");
            return;
        }

        final String id = channelId.getId();
        // Remove the state to be able to refresh it
        protocolHandler.getCallback().removeState(id);

        if (id.startsWith(DialConstants.CHANNEL_STATE)) {
            protocolHandler.refreshState(getAppId(id));
        } else if (id.startsWith(DialConstants.CHANNEL_ICON)) {
            protocolHandler.refreshIcon(getAppId(id));
        }
    }

    /**
     * Gets the app id.
     *
     * @param channelId the channel id
     * @return the app id
     */
    private String getAppId(String channelId) {
        final int idx = channelId.indexOf('-');
        if (idx < 0) {
            return null;
        } else {
            return channelId.substring(idx + 1).replace('-', '.');
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, initialize the
     * {@link DialProtocol} and will attempt to connect to the (via {@link #connect()}.
     */
    @Override
    public void initialize() {
        final DialConfig config = getThing().getConfiguration().as(DialConfig.class);

        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration missing");
            return;
        }

        if (StringUtils.isEmpty(config.getDialUri())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "DIAL URI is missing from configuration");
            return;
        }

        logger.info("Attempting connection to DIAL device...");
        try {
            final DialProtocol<StatefulHandlerCallback<String>> protocolHandler = new DialProtocol<StatefulHandlerCallback<String>>(
                    config, new StatefulHandlerCallback<String>(new ThingCallback<String>() {

                        @Override
                        public void statusChanged(ThingStatus state, ThingStatusDetail detail, String msg) {
                            updateStatus(state, detail, msg);

                        }

                        @Override
                        public void stateChanged(String channelId, State newState) {
                            updateState(channelId, newState);
                        }

                        @Override
                        public void setProperty(String propertyName, String propertyValue) {
                            getThing().setProperty(propertyName, propertyValue);
                        }
                    }));

            final String msg = protocolHandler.login();
            if (msg == null) {

                // Build dynamic channels...
                final ChannelTypeUID dialIconTypeUID = new ChannelTypeUID(DialConstants.THING_TYPE_DIAL.getBindingId(),
                        "dialicon");
                final ChannelTypeUID dialStateTypeUID = new ChannelTypeUID(DialConstants.THING_TYPE_DIAL.getBindingId(),
                        "dialState");

                final ThingUID thingUid = getThing().getUID();

                final Map<String, DialApp> apps = protocolHandler.getDialApps();
                final List<Channel> channels = new ArrayList<Channel>();
                for (DialApp app : apps.values()) {
                    final Channel channel = ChannelBuilder
                            .create(new ChannelUID(thingUid,
                                    DialConstants.CHANNEL_STATE + "-" + app.getId().replace('.', '-')), "Switch")
                            .withDescription(app.getName() + " Service").withLabel(app.getName())
                            .withType(dialStateTypeUID).build();
                    channels.add(channel);

                    final Channel iconChannel = ChannelBuilder
                            .create(new ChannelUID(thingUid,
                                    DialConstants.CHANNEL_ICON + "-" + app.getId().replace('.', '-')), "Image")
                            .withDescription(app.getName() + " Icon").withLabel(app.getName() + " Icon")
                            .withType(dialIconTypeUID).build();
                    channels.add(iconChannel);
                    logger.info("Creating channel '{}' with an id of '{}'", app.getName(), app.getId());
                }

                final ThingBuilder thingBuilder = editThing();
                thingBuilder.withChannels(channels);
                thingBuilder.withLabel(thing.getLabel());// bug to save it
                updateThing(thingBuilder.build());

                _protocolHandler.set(protocolHandler);
                startPolling(config);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                tryReconnect(config);
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error connecting to DIAL device (may need to turn it on manually)");
            tryReconnect(config);
        }
    }

    /**
     * Start polling.
     *
     * @param config the config
     */
    private void startPolling(DialConfig config) {
        if (config.getRefresh() > 0) {
            logger.info("Starting state polling every {} seconds", config.getRefresh());
            SonyUtility.cancel(_refreshState.getAndSet(this.scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ThingStatus status = getThing().getStatus();
                        if (status == ThingStatus.ONLINE) {
                            final DialProtocol<StatefulHandlerCallback<String>> protocolHandler = _protocolHandler
                                    .get();
                            if (protocolHandler == null) {
                                //
                            } else {
                                protocolHandler.refreshState();
                            }
                        } else {
                            logger.info("Status: {}", status);
                        }
                    } catch (Exception e) {
                        logger.debug("Exception while refreshing state: {}", e.getMessage(), e);
                    }
                }
            }, config.getRefresh(), config.getRefresh(), TimeUnit.SECONDS)));
        } else {
            logger.info("Refresh not a positive number - polling has been disabled");
        }

    }

    /**
     * Try reconnect.
     *
     * @param config the config
     */
    private void tryReconnect(DialConfig config) {
        if (config.getRetryPolling() < 1) {
            logger.info("Retry connection has been disabled via configuration setting");
        } else {
            synchronized (this) {
                if (_retryConnection.get() == null) {
                    _retryConnection.set(this.scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            _retryConnection.set(null);
                            initialize();
                        }
                    }, config.getRetryPolling(), TimeUnit.SECONDS));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Disposes of the handler. Will simply call {@link #disconnect(boolean)} to disconnect and NOT retry the
     * connection
     */
    @Override
    public void dispose() {
        SonyUtility.cancel(_refreshState.getAndSet(null));
        SonyUtility.cancel(_retryConnection.getAndSet(null));
        SonyUtility.close(_protocolHandler.getAndSet(null));
    }
}
