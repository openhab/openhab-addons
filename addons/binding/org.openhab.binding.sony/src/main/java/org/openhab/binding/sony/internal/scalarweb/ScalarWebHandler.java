/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sony.internal.SonyUtility;
import org.openhab.binding.sony.internal.StatefulHandlerCallback;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebLoginProtocol;
import org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol;
import org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The thing handler for a Sony Ircc device. This is the entry point provides a full two interaction between openhab
 * and the ircc system.
 *
 * @author Tim Roberts - Initial contribution
 *
 */
public class ScalarWebHandler extends BaseThingHandler {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(ScalarWebHandler.class);

    /** The tracker. */
    private final ScalarWebChannelTracker tracker = new ScalarWebChannelTracker();

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<ScalarWebProtocolFactory<StatefulHandlerCallback<ScalarWebChannel>>> _protocolFactory = new AtomicReference<ScalarWebProtocolFactory<StatefulHandlerCallback<ScalarWebChannel>>>(
            null);

    /** The refresh state event - will only be created when we are connected. */
    private final AtomicReference<ScheduledFuture<?>> _refreshState = new AtomicReference<ScheduledFuture<?>>(null);

    /** The refresh state event - will only be created when we are connected. */
    private final AtomicReference<ScheduledFuture<?>> _retryConnection = new AtomicReference<ScheduledFuture<?>>(null);

    /** The callback. */
    private final StatefulHandlerCallback<ScalarWebChannel> callback;

    /** The initialization lock. */
    private final Lock initializationLock = new ReentrantLock();

    /** The initialization. */
    private final AtomicReference<Future<?>> initialization = new AtomicReference<Future<?>>(null);

    /**
     * Constructs the handler from the {@link Thing}.
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public ScalarWebHandler(Thing thing) {
        super(thing);
        callback = new StatefulHandlerCallback<ScalarWebChannel>(new ThingCallback<ScalarWebChannel>() {

            @Override
            public void statusChanged(ThingStatus state, ThingStatusDetail detail, String msg) {
                updateStatus(state, detail, msg);

            }

            @Override
            public void stateChanged(ScalarWebChannel channel, State newState) {
                updateState(channel.getChannelId(), newState);
            }

            @Override
            public void setProperty(String propertyName, String propertyValue) {
                getThing().setProperty(propertyName, propertyValue);
            }
        });

    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        final ScalarWebChannel scalarChannel = new ScalarWebChannel(channelUID);

        final ScalarWebProtocolFactory<StatefulHandlerCallback<ScalarWebChannel>> protocolFactory = _protocolFactory
                .get();
        if (protocolFactory == null) {
            logger.debug("Trying to handle a channel command before a protocol factory has been created");
            return;
        }

        final ScalarWebProtocol<StatefulHandlerCallback<ScalarWebChannel>> protocol = protocolFactory
                .getProtocol(scalarChannel.getService());
        if (protocol == null) {
            logger.debug("Unknown channel service: {} for {} and command {}", scalarChannel.getService(), channelUID,
                    command);
        } else {
            callback.removeState(scalarChannel);
            if (command instanceof RefreshType) {
                protocol.refreshChannel(scalarChannel);
            } else {
                protocol.setChannel(scalarChannel, command);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, initialize the
     * {@link ScalarWebLoginProtocol} and will attempt to connect to the (via {@link #connect()}.
     */
    @Override
    public void initialize() {
        SonyUtility.cancel(initialization.getAndSet(scheduler.submit(new Runnable() {
            @Override
            public void run() {
                initializeTask();
            }
        })));
    }

    /**
     * Initialize task.
     */
    private void initializeTask() {
        // prevent multiple initializations from occuring at the same time
        // (such as initial initialization and discovery->updateThing->dispose/initialize)
        initializationLock.lock();
        try {
            final ScalarWebConfig config = getThing().getConfiguration().as(ScalarWebConfig.class);

            if (StringUtils.isEmpty(config.getScalarWebUri())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "ScalarWeb URI is missing from configuration");
                return;
            }

            logger.info("Attempting connection to Scalar Web device...");
            try {
                SonyUtility.checkInterrupt();

                final ScalarWebState webState = new ScalarWebState(config.getScalarWebUri());

                final ScalarWebLoginProtocol<StatefulHandlerCallback<ScalarWebChannel>> loginHandler = new ScalarWebLoginProtocol<StatefulHandlerCallback<ScalarWebChannel>>(
                        webState, config, callback);

                final String msg = loginHandler.login();
                SonyUtility.checkInterrupt();

                if (msg == null) {
                    final ScalarWebProtocolFactory<StatefulHandlerCallback<ScalarWebChannel>> factory = new ScalarWebProtocolFactory<StatefulHandlerCallback<ScalarWebChannel>>(
                            tracker, webState, config, bundleContext, callback);

                    final ThingUID thingUid = getThing().getUID();
                    final List<Channel> channels = new ArrayList<Channel>();
                    for (ScalarWebChannelDescriptor descriptor : factory.getChannelDescriptors()) {
                        logger.debug("Creating channel: {}", descriptor);
                        channels.add(descriptor.createChannel(thingUid).build());
                    }
                    SonyUtility.checkInterrupt();

                    final ThingBuilder thingBuilder = editThing();
                    thingBuilder.withChannels(channels);
                    thingBuilder.withLabel(thing.getLabel());// bug to save it
                    updateThing(thingBuilder.build());

                    SonyUtility.checkInterrupt();

                    SonyUtility.close(_protocolFactory.getAndSet(factory));

                    startPolling(config);

                    SonyUtility.checkInterrupt();

                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                    tryReconnect(config);
                }
            } catch (InterruptedException e) {
                logger.debug("Initialization was interrupted");
                // don't try to reconnect

            } catch (IOException | ParserConfigurationException | SAXException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error connecting to Scalar Web device (may need to turn it on manually)");
                tryReconnect(config);
            } catch (Exception e) {
                logger.error("Exception caught: {}", e.getMessage(), e);
            }
        } finally {
            initializationLock.unlock();
        }
    }

    /**
     * Start polling.
     *
     * @param config the config
     */
    private void startPolling(ScalarWebConfig config) {
        if (config.getRefresh() > 0) {
            logger.info("Starting state polling every {} seconds", config.getRefresh());
            SonyUtility.cancel(_refreshState.getAndSet(this.scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ThingStatus status = getThing().getStatus();
                        if (status == ThingStatus.ONLINE) {
                            final ScalarWebProtocolFactory<StatefulHandlerCallback<ScalarWebChannel>> protocolHandler = _protocolFactory
                                    .get();
                            if (protocolHandler == null) {
                                //
                            } else {
                                SonyUtility.checkInterrupt();
                                protocolHandler.refreshAllState(scheduler);
                            }
                        } else {
                            logger.info("Status: {}", status);
                        }
                    } catch (InterruptedException e) {
                        logger.debug("Polling was interrupted");
                    } catch (Exception e) {
                        logger.error(">>> polling: {}", e.getMessage(), e);
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
    private void tryReconnect(ScalarWebConfig config) {
        // Don't retry connections when we are doing an access code request (retries will cancel the request)
        if (!config.getAccessCode().equals(ScalarWebConstants.ACCESSCODE_RQST)) {
            if (_retryConnection.get() == null) {
                if (config.getRetryPolling() < 1) {
                    logger.info("Retry connection has been disabled via configuration setting");
                } else {
                    SonyUtility.cancel(_retryConnection.getAndSet(this.scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SonyUtility.checkInterrupt();
                                _retryConnection.set(null);
                                initialize();
                            } catch (InterruptedException e) {
                                logger.debug("Retry connection was interrupted");
                            }
                        }
                    }, config.getRetryPolling(), TimeUnit.SECONDS)));
                }
            }
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        if (status == ThingStatus.OFFLINE) {
            final ScalarWebConfig config = getThing().getConfiguration().as(ScalarWebConfig.class);
            tryReconnect(config);
        }
        super.updateStatus(status, statusDetail, description);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#channelUnlinked(org.eclipse.smarthome.core.thing.
     * ChannelUID)
     */
    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        final ScalarWebChannel channel = new ScalarWebChannel(channelUID);
        callback.removeState(channel);
        tracker.channelUnlinked(channel);
        super.channelUnlinked(channelUID);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#channelLinked(org.eclipse.smarthome.core.thing.
     * ChannelUID)
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        tracker.channelLinked(new ScalarWebChannel(channelUID));
        super.channelLinked(channelUID);
    }

    /**
     * {@inheritDoc}
     *
     * Disposes of the handler. Will simply call {@link #disconnect(boolean)} to disconnect and NOT retry the
     * connection
     */
    @Override
    public void dispose() {
        SonyUtility.cancel(initialization.getAndSet(null));
        SonyUtility.cancel(_refreshState.getAndSet(null));
        SonyUtility.cancel(_retryConnection.getAndSet(null));
        SonyUtility.close(_protocolFactory.getAndSet(null));
    }
}
