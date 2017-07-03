/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sony.internal.SonyUtility;
import org.openhab.binding.sony.internal.StatefulHandlerCallback;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.ircc.IrccProtocol.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The thing handler for a Sony Ircc device. This is the entry point provides a full two interaction between openhab
 * and the ircc system.
 *
 * @author Tim Roberts - Initial contribution
 *
 */
public class IrccHandler extends BaseThingHandler {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(IrccHandler.class);

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<IrccProtocol<StatefulHandlerCallback<String>>> _protocolHandler = new AtomicReference<IrccProtocol<StatefulHandlerCallback<String>>>(
            null);

    /** The refresh state event - will only be created when we are connected. */
    private final AtomicReference<ScheduledFuture<?>> _refreshState = new AtomicReference<ScheduledFuture<?>>(null);

    /** The refresh state event - will only be created when we are connected. */
    private final AtomicReference<ScheduledFuture<?>> _retryConnection = new AtomicReference<ScheduledFuture<?>>(null);

    /** The is authenticating. */
    private final AtomicBoolean _isAuthenticating = new AtomicBoolean(false);

    /**
     * Constructs the handler from the {@link Thing}.
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public IrccHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link IrccProtocol}. Basically we validate the type of command for the channel then call the
     * {@link IrccProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link IrccProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID);
            return;
        }

        final IrccProtocol<StatefulHandlerCallback<String>> protocolHandler = _protocolHandler.get();
        if (protocolHandler == null) {
            logger.debug("Trying to handle a channel command before a protocol handler has been created");
            return;
        }

        final String groupId = channelUID.getGroupId();
        final String channelId = channelUID.getIdWithoutGroup();

        if (groupId == null) {
            logger.debug("Called with a null group id - ignoring");
            return;
        }

        if (channelId == null) {
            logger.debug("Called with a null channel id - ignoring");
            return;
        }

        switch (groupId) {
            case IrccConstants.GRP_PRIMARY:
                switch (channelId) {
                    case IrccConstants.CHANNEL_CMD:
                        if (command instanceof StringType) {
                            protocolHandler.sendCommand(command.toString());
                        } else {
                            logger.debug("Received a COMMAND channel command with a non StringType: {}", command);
                        }
                        break;

                    case IrccConstants.CHANNEL_POWER:
                        if (command instanceof OnOffType) {
                            protocolHandler.sendPower(OnOffType.ON == command);
                        } else {
                            logger.debug("Received a COMMAND channel command with a non OnOffType: {}", command);
                        }
                        break;
                    case IrccConstants.CHANNEL_CONTENTURL:
                        if (command instanceof StringType) {
                            protocolHandler.sendContentUrl(command.toString());
                        } else {
                            logger.debug("Received a COMMAND channel command with a non StringType: {}", command);
                        }
                        break;
                    case IrccConstants.CHANNEL_TEXT:
                        if (command instanceof StringType) {
                            protocolHandler.sendText(command.toString());
                        } else {
                            logger.debug("Received a COMMAND channel command with a non StringType: {}", command);
                        }
                        break;
                    default:
                        logger.debug("Unknown/Unsupported Primary Channel id: {}", channelId);
                        break;
                }

                break;

            case IrccConstants.GRP_VIEWING:
                logger.debug("Unknown/Unsupported Viewing Channel id: {}", channelId);
                break;

            case IrccConstants.GRP_CONTENT:
                logger.debug("Unknown/Unsupported Content Channel id: {}", channelId);
                break;
            default:
                break;
        }

    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link IrccProtocol} to
     * handle the actual refresh based on the channel id.
     *
     * @param channelUID the channel UID
     */
    private void handleRefresh(ChannelUID channelUID) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        final IrccProtocol<StatefulHandlerCallback<String>> protocolHandler = _protocolHandler.get();
        if (protocolHandler == null) {
            logger.debug("Trying to handle a refresh command before a protocol handler has been created");
            return;
        }

        final String groupId = channelUID.getGroupId();
        final String channelId = channelUID.getIdWithoutGroup();

        if (groupId == null) {
            logger.debug("Called with a null group id - ignoring");
            return;
        }

        if (channelId == null) {
            logger.debug("Called with a null channel id - ignoring");
            return;
        }
        // Remove the state to be able to refresh it
        protocolHandler.getCallback().removeState(SonyUtility.createChannelId(groupId, channelId));

        switch (groupId) {
            case IrccConstants.GRP_PRIMARY:
                switch (channelId) {
                    case IrccConstants.CHANNEL_CONTENTURL:
                        protocolHandler.refreshContentUrl();
                        break;
                    case IrccConstants.CHANNEL_TEXT:
                        protocolHandler.refreshText();
                        break;
                    case IrccConstants.CHANNEL_INTEXT:
                        protocolHandler.refreshInText();
                        break;
                    case IrccConstants.CHANNEL_INBROWSER:
                        protocolHandler.refreshInBrowser();
                        break;
                    case IrccConstants.CHANNEL_ISVIEWING:
                        protocolHandler.refreshIsViewing();
                        break;
                    default:
                        break;
                }

                break;

            case IrccConstants.GRP_VIEWING:
                protocolHandler.refreshStatus();
                break;

            case IrccConstants.GRP_CONTENT:
                protocolHandler.refreshContentInformation();
                break;
            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, initialize the
     * {@link IrccProtocol} and will attempt to connect to the (via {@link #connect()}.
     */
    @Override
    public void initialize() {
        final IrccConfig config = getThing().getConfiguration().as(IrccConfig.class);

        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration missing");
            return;
        }

        if (StringUtils.isEmpty(config.getIrccUri())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IRCC URI is missing from configuration");
            return;
        }

        logger.info("Attempting connection to IRCC device...");
        try {
            _isAuthenticating.set(false);

            final IrccProtocol<StatefulHandlerCallback<String>> protocolHandler = new IrccProtocol<StatefulHandlerCallback<String>>(
                    bundleContext, config, new StatefulHandlerCallback<String>(new ThingCallback<String>() {

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
                _isAuthenticating.set(false);
                _protocolHandler.set(protocolHandler);
                startPolling(config);
                updateStatus(ThingStatus.ONLINE);
            } else {
                _isAuthenticating.set(protocolHandler.getProtocolStatus() == Status.Authenticating);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                tryReconnect(config);
            }
        } catch (Exception e) {
            logger.debug("exception: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error connecting to IRCC device (may need to turn it on manually)");
            tryReconnect(config);
        }
    }

    /**
     * Start polling.
     *
     * @param config the config
     */
    private void startPolling(IrccConfig config) {
        if (config.getRefresh() > 0) {
            logger.info("Starting state polling every {} seconds", config.getRefresh());
            SonyUtility.cancel(_refreshState.getAndSet(this.scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ThingStatus status = getThing().getStatus();
                        if (status == ThingStatus.ONLINE) {
                            final IrccProtocol<StatefulHandlerCallback<String>> protocolHandler = _protocolHandler
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
                        e.printStackTrace();
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
    private void tryReconnect(IrccConfig config) {
        // Don't retry connections when we are doing an access code request (retries will cancel the request)
        // only do retries after we've gone online
        final IrccProtocol<StatefulHandlerCallback<String>> protocolHandler = _protocolHandler.get();
        if (!_isAuthenticating.get() && protocolHandler == null) {
            synchronized (this) {
                if (_retryConnection.get() == null) {
                    if (config.getRetryPolling() < 1) {
                        logger.info("Retry connection has been disabled via configuration setting");
                    } else {
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
