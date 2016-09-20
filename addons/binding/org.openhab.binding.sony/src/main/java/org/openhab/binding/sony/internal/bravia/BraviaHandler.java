/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.bravia;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The thing handler for a Sony Bravia device. This is the entry point provides a full two interaction between openhab
 * and the bravia system.
 *
 * @author Tim Roberts
 * @version $Id: $Id
 */
public class BraviaHandler extends BaseThingHandler {
    // Logger
    private Logger logger = LoggerFactory.getLogger(BraviaHandler.class);

    /**
     * The configuration for the system - will be recreated when the configuration changes and will be null when not
     * online
     */
    private BraviaConfig _config;

    /**
     * The protocol handler being used - will be null if not initialized
     */
    private BraviaProtocol _protocolHandler;

    /**
     * The retry connection event - will only be created when we are retrying the connection attempt
     */
    private ScheduledFuture<?> _retryConnection;

    /**
     * The refresh state event - will only be created when we are connected
     */
    private ScheduledFuture<?> _refreshState;

    /**
     * The ping event - will be non-null when online (null otherwise)
     */
    private ScheduledFuture<?> _ping;

    /**
     * Constructs the handler from the {@link Thing}
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public BraviaHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link BraviaProtocol}. Basically we validate the type of command for the channel then call the
     * {@link BraviaProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link BraviaProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }

        String id = channelUID.getId();

        if (id == null) {
            logger.warn("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(BraviaConstants.CHANNEL_IR)) {
            if (command instanceof DecimalType) {
                _protocolHandler.setIR(((DecimalType) command).intValue());
            } else {
                logger.error("Received a IR channel command with a non StringType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_POWER)) {
            if (command instanceof OnOffType) {
                _protocolHandler.setPower(command == OnOffType.ON);
            } else {
                logger.error("Received a POWER channel command with a non OnOffType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_VOLUME)) {
            if (command instanceof OnOffType) {
                _protocolHandler.setAudioMute(command == OnOffType.ON);
            } else if (command instanceof IncreaseDecreaseType) {
                _protocolHandler.setIR(command == IncreaseDecreaseType.INCREASE ? 30 : 31);
            } else if (command instanceof PercentType) {
                _protocolHandler.setAudioVolume(((PercentType) command).intValue());
            } else {
                logger.error(
                        "Received a AUDIO VOLUME channel command with a non OnOffType/IncreaseDecreaseType/PercentType: "
                                + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_AUDIOMUTE)) {
            if (command instanceof OnOffType) {
                _protocolHandler.setAudioMute(command == OnOffType.ON);
            } else {
                logger.error("Received a AUDIO MUTE channel command with a non OnOffType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_CHANNEL)) {
            if (command instanceof StringType) {
                _protocolHandler.setChannel(command.toString());
            } else {
                logger.error("Received a CHANNEL channel command with a non StringType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_TRIPLETCHANNEL)) {
            if (command instanceof StringType) {
                _protocolHandler.setTripletChannel(command.toString());
            } else {
                logger.error("Received a TRIPLET CHANNEL channel command with a non StringType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_INPUTSOURCE)) {
            if (command instanceof StringType) {
                _protocolHandler.setInputSource(command.toString());
            } else {
                logger.error("Received a INPUT SOURCE channel command with a non StringType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_INPUT)) {
            if (command instanceof StringType) {
                _protocolHandler.setInput(command.toString());
            } else {
                logger.error("Received a INPUT channel command with a non StringType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_PICTUREMUTE)) {
            if (command instanceof OnOffType) {
                _protocolHandler.setPictureMute(command == OnOffType.ON);
            } else {
                logger.error("Received a PICTURE MUTE channel command with a non OnOffType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_TOGGLEPICTUREMUTE)) {
            _protocolHandler.togglePictureMute();
        } else if (id.equals(BraviaConstants.CHANNEL_PICTUREINPICTURE)) {
            if (command instanceof OnOffType) {
                _protocolHandler.setPictureInPicture(command == OnOffType.ON);
            } else {
                logger.error("Received a PICTURE IN PICTURE channel command with a non OnOffType: " + command);
            }
        } else if (id.equals(BraviaConstants.CHANNEL_TOGGLEPICTUREINPICTURE)) {
            _protocolHandler.togglePictureInPicture();
        } else if (id.equals(BraviaConstants.CHANNEL_TOGGLEPIPPOSITION)) {
            _protocolHandler.togglePipPosition();
        } else {
            logger.error("Unknown/Unsupported Channel id: " + id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link BraviaProtocol} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (id.equals(BraviaConstants.CHANNEL_POWER)) {
            _protocolHandler.refreshPower();
        } else if (id.equals(BraviaConstants.CHANNEL_VOLUME)) {
            _protocolHandler.refreshVolume();
        } else if (id.equals(BraviaConstants.CHANNEL_AUDIOMUTE)) {
            _protocolHandler.refreshAudioMute();
        } else if (id.equals(BraviaConstants.CHANNEL_CHANNEL)) {
            _protocolHandler.refreshChannel();
        } else if (id.equals(BraviaConstants.CHANNEL_TRIPLETCHANNEL)) {
            _protocolHandler.refreshTripletChannel();
        } else if (id.equals(BraviaConstants.CHANNEL_INPUTSOURCE)) {
            _protocolHandler.refreshInputSource();
        } else if (id.equals(BraviaConstants.CHANNEL_INPUT)) {
            _protocolHandler.refreshInput();
        } else if (id.equals(BraviaConstants.CHANNEL_PICTUREMUTE)) {
            _protocolHandler.refreshPictureMute();
        } else if (id.equals(BraviaConstants.CHANNEL_PICTUREINPICTURE)) {
            _protocolHandler.refreshPictureInPicture();
        } else if (id.equals(BraviaConstants.CHANNEL_BROADCASTADDRESS)) {
            _protocolHandler.refreshBroadcastAddress(getBraviaConfig().getNetInterface());
        } else if (id.equals(BraviaConstants.CHANNEL_MACADDRESS)) {
            _protocolHandler.refreshBroadcastAddress(getBraviaConfig().getNetInterface());
        } else {
            // logger.error("Unknown/Unsupported Channel id: " + id);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, initialize the
     * {@link BraviaProtocol} and will attempt to connect to the (via {@link #connect()}.
     */
    @Override
    public void initialize() {
        final BraviaConfig config = getBraviaConfig();

        if (config == null) {
            return;
        }

        if (config.getIpAddress() == null || config.getIpAddress().trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP Address of Russound is missing from configuration");
            return;
        }

        try {
            _protocolHandler = new BraviaProtocol(config, new BraviaHandlerCallback() {
                @Override
                public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                    updateStatus(status, detail, msg);
                    if (status != ThingStatus.ONLINE) {
                        disconnect(true);
                    }
                }

                @Override
                public void stateChanged(String channelId, State state) {
                    updateState(channelId, state);
                }

            });
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error connecting to bravia tv");
        }

        // Try initial connection in a scheduled task
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }

        }, 1, TimeUnit.SECONDS);
    }

    /**
     * Attempts to connect to the system via {@link BraviaProtocol#login()}. Once completed, a ping job will be created
     * to keep the connection
     * alive and a refresh job to refresh state (although the broadcast address and mac address is refreshed immediately
     * and only once). If a connection cannot be established (or login failed), the connection attempt will be retried
     * later (via {@link #retryConnect()})
     */
    private void connect() {
        String response = "Server is offline - will try to reconnect later";
        try {
            response = _protocolHandler.login();
            if (response == null) {
                final BraviaConfig config = getBraviaConfig();
                if (config != null) {

                    updateStatus(ThingStatus.ONLINE);

                    _ping = this.scheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            final ThingStatus status = getThing().getStatus();
                            if (status == ThingStatus.ONLINE) {
                                _protocolHandler.ping();
                            }

                        }
                    }, config.getPing(), config.getPing(), TimeUnit.SECONDS);

                    _refreshState = this.scheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            final ThingStatus status = getThing().getStatus();
                            if (status == ThingStatus.ONLINE) {
                                _protocolHandler.refreshState();
                            }

                        }
                    }, config.getRefresh(), config.getRefresh(), TimeUnit.SECONDS);

                    _protocolHandler.refreshBroadcastAddress(config.getNetInterface());
                    _protocolHandler.refreshMacAddress(config.getNetInterface());
                    return;
                }
            }

        } catch (Exception e) {
            logger.error("Error connecting: " + e);
            // do nothing
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response);
        retryConnect();
    }

    /**
     * Attempts to disconnect from the session and will optionally retry the connection attempt. The {@link #_ping} and
     * {@link #_refreshState} will be cancelled and set to null. The {@link BraviaProtocol#dispose()} will be called to
     * disconnect and if we are not trying to reconnect, the {@link #_protocolHandler} will be nulled. If we are
     * retrying a connection attempt, {@link #retryConnect()} will be called.
     *
     * @param retryConnection true to retry connection attempts after the disconnect
     */
    private void disconnect(boolean retryConnection) {
        // Cancel ping
        if (_ping != null) {
            _ping.cancel(true);
            _ping = null;
        }

        if (_refreshState != null) {
            _refreshState.cancel(true);
            _refreshState = null;
        }

        if (_protocolHandler != null) {
            try {
                _protocolHandler.dispose();
            } catch (IOException e) {
                // do nothing
            }
            if (!retryConnection) {
                _protocolHandler = null;
            }
        }

        if (retryConnection) {
            retryConnect();
        }
    }

    /**
     * Retries the connection attempt - schedules a job in {@link BraviaConfig#getRetryPolling()} seconds to
     * call the {@link #connect()} method. If a retry attempt is pending, the request is ignored.
     */
    private void retryConnect() {
        if (_retryConnection == null) {
            final BraviaConfig config = getBraviaConfig();
            if (config != null) {

                logger.info("Will try to reconnect in {} seconds", config.getRetryPolling());
                _retryConnection = this.scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        _retryConnection = null;
                        try {
                            connect();
                        } catch (Exception e) {
                            logger.error("Exception connecting");
                            e.printStackTrace();
                        }
                    }

                }, config.getRetryPolling(), TimeUnit.SECONDS);
            }
        } else {
            logger.debug("RetryConnection called when a retry connection is pending - ignoring request");
        }
    }

    /**
     * Simply gets the {@link BraviaConfig} from the {@link Thing} and will set the status to offline if not
     * found.
     *
     * @return a possible null {@link BraviaConfig}
     */
    private BraviaConfig getBraviaConfig() {
        if (_config == null) {
            final BraviaConfig config = getThing().getConfiguration().as(BraviaConfig.class);

            if (config == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            } else {
                _config = config;
            }
        }

        return _config;
    }

    /**
     * {@inheritDoc}
     *
     * Disposes of the handler. Will simply call {@link #disconnect(boolean)} to disconnect and NOT retry the
     * connection
     */
    @Override
    public void dispose() {
        disconnect(false);
    }
}
