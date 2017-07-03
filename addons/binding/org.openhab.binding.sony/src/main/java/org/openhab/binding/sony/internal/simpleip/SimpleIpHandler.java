/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.simpleip;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
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
import org.openhab.binding.sony.internal.SonyUtility;
import org.openhab.binding.sony.internal.StatefulHandlerCallback;
import org.openhab.binding.sony.internal.ThingCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The thing handler for a Sony Simple IP device. This is the entry point provides a full two interaction between
 * openhab
 * and the simple IP system.
 *
 * @author Tim Roberts - Initial contribution
 *
 */
public class SimpleIpHandler extends BaseThingHandler {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(SimpleIpHandler.class);

    /** The connect lock. */
    private Lock _connectLock = new ReentrantLock();

    /**
     * The configuration for the system - will be recreated when the configuration changes and will be null when not
     * online.
     */
    private final AtomicReference<SimpleIpConfig> _config = new AtomicReference<SimpleIpConfig>();

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<SimpleIpProtocol> _protocolHandler = new AtomicReference<SimpleIpProtocol>();

    /** The retry connection event - will only be created when we are retrying the connection attempt. */
    private final AtomicBoolean _retrying = new AtomicBoolean(false);

    /** The refresh state event - will only be created when we are connected. */
    private final AtomicReference<ScheduledFuture<?>> _refreshState = new AtomicReference<ScheduledFuture<?>>();

    /** The ping event - will be non-null when online (null otherwise). */
    private final AtomicReference<ScheduledFuture<?>> _ping = new AtomicReference<ScheduledFuture<?>>();

    /**
     * Constructs the handler from the {@link Thing}.
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public SimpleIpHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link SimpleIpProtocol}. Basically we validate the type of command for the channel then call the
     * {@link SimpleIpProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link SimpleIpProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }

        final String id = channelUID.getId();

        if (id == null) {
            logger.debug("Called with a null channel id - ignoring");
            return;
        }

        final SimpleIpProtocol handler = _protocolHandler.get();
        if (handler == null) {
            logger.debug("Protocol handler wasn't set for handleCommand - ignoring '{}': '{}'", channelUID, command);
            return;
        }

        switch (id) {
            case SimpleIpConstants.CHANNEL_IR:
                if (command instanceof StringType) {
                    handler.setIR(command.toString());
                } else {
                    logger.warn("Received a IR channel command with a non StringType: {}", command);
                }

                break;

            case SimpleIpConstants.CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    handler.setPower(command == OnOffType.ON);
                } else {
                    logger.warn("Received a POWER channel command with a non OnOffType: {}", command);
                }

                break;

            case SimpleIpConstants.CHANNEL_VOLUME:
                if (command instanceof OnOffType) {
                    handler.setAudioMute(command == OnOffType.ON);
                } else if (command instanceof IncreaseDecreaseType) {
                    handler.setIR(command == IncreaseDecreaseType.INCREASE ? "Volume Up" : "Volume Down");
                } else if (command instanceof PercentType) {
                    handler.setAudioVolume(((PercentType) command).intValue());
                } else {
                    logger.warn(
                            "Received a AUDIO VOLUME channel command with a non OnOffType/IncreaseDecreaseType/PercentType: {}",
                            command);
                }

                break;

            case SimpleIpConstants.CHANNEL_AUDIOMUTE:
                if (command instanceof OnOffType) {
                    handler.setAudioMute(command == OnOffType.ON);
                } else {
                    logger.warn("Received a AUDIO MUTE channel command with a non OnOffType: {}", command);
                }

                break;

            case SimpleIpConstants.CHANNEL_CHANNEL:
                if (command instanceof StringType) {
                    handler.setChannel(command.toString());
                } else {
                    logger.warn("Received a CHANNEL channel command with a non StringType: {}", command);
                }

                break;

            case SimpleIpConstants.CHANNEL_TRIPLETCHANNEL:
                if (command instanceof StringType) {
                    handler.setTripletChannel(command.toString());
                } else {
                    logger.warn("Received a TRIPLET CHANNEL channel command with a non StringType: {}", command);
                }

                break;
            case SimpleIpConstants.CHANNEL_INPUTSOURCE:
                if (command instanceof StringType) {
                    handler.setInputSource(command.toString());
                } else {
                    logger.warn("Received a INPUT SOURCE channel command with a non StringType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_INPUT:
                if (command instanceof StringType) {
                    handler.setInput(command.toString());
                } else {
                    logger.warn("Received a INPUT channel command with a non StringType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_PICTUREMUTE:
                if (command instanceof OnOffType) {
                    handler.setPictureMute(command == OnOffType.ON);
                } else {
                    logger.warn("Received a PICTURE MUTE channel command with a non OnOffType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_TOGGLEPICTUREMUTE:
                handler.togglePictureMute();
                break;
            case SimpleIpConstants.CHANNEL_PICTUREINPICTURE:
                if (command instanceof OnOffType) {
                    handler.setPictureInPicture(command == OnOffType.ON);
                } else {
                    logger.warn("Received a PICTURE IN PICTURE channel command with a non OnOffType: {}", command);
                }
                break;
            case SimpleIpConstants.CHANNEL_TOGGLEPICTUREINPICTURE:
                handler.togglePictureInPicture();
                break;
            case SimpleIpConstants.CHANNEL_TOGGLEPIPPOSITION:
                handler.togglePipPosition();
                break;

            default:
                logger.warn("Unknown/Unsupported Channel id: {}", id);
                break;
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link SimpleIpProtocol} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        final SimpleIpProtocol handler = _protocolHandler.get();
        if (handler == null) {
            logger.debug("Protocol handler wasn't set for handleRefresh - ignoring '{}'", id);
            return;
        }

        switch (id) {
            case SimpleIpConstants.CHANNEL_POWER:
                handler.refreshPower();
                break;
            case SimpleIpConstants.CHANNEL_VOLUME:
                handler.refreshVolume();
                break;
            case SimpleIpConstants.CHANNEL_AUDIOMUTE:
                handler.refreshAudioMute();
                break;
            case SimpleIpConstants.CHANNEL_CHANNEL:
                handler.refreshChannel();
                break;
            case SimpleIpConstants.CHANNEL_TRIPLETCHANNEL:
                handler.refreshTripletChannel();
                break;
            case SimpleIpConstants.CHANNEL_INPUTSOURCE:
                handler.refreshInputSource();
                break;
            case SimpleIpConstants.CHANNEL_INPUT:
                handler.refreshInput();
                break;
            case SimpleIpConstants.CHANNEL_PICTUREMUTE:
                handler.refreshPictureMute();
                break;
            case SimpleIpConstants.CHANNEL_PICTUREINPICTURE:
                handler.refreshPictureInPicture();
                break;

        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, initialize the
     * {@link SimpleIpProtocol} and will attempt to connect to the (via {@link #connect()}.
     */
    @Override
    public void initialize() {
        // get and save the configuration
        _config.set(getThing().getConfiguration().as(SimpleIpConfig.class));
        retryConnect(1);
    }

    /**
     * Attempts to connect to the system via {@link SimpleIpProtocol#login()}. Once completed, a ping job will be
     * created
     * to keep the connection
     * alive and a refresh job to refresh state (although the broadcast address and mac address is refreshed immediately
     * and only once). If a connection cannot be established (or login failed), the connection attempt will be retried
     * later (via {@link #retryConnect()})
     */
    private void connect() {
        _connectLock.lock();
        try {

            final SimpleIpConfig config = _config.get();

            if (config == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration missing");
                return;
            }

            if (config.getIpAddress() == null || config.getIpAddress().trim().length() == 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "IP Address of Sony is missing from configuration");
                return;
            }

            SonyUtility.close(_protocolHandler.getAndSet(null));

            SimpleIpProtocol newHandler;
            try {
                newHandler = new SimpleIpProtocol(config, bundleContext,
                        new StatefulHandlerCallback<String>(new ThingCallback<String>() {
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

                            @Override
                            public void setProperty(String propertyName, String propertyValue) {
                                getThing().setProperty(propertyName, propertyValue);

                                if (SimpleIpConstants.PROP_MACADDRESS.equals(propertyName)) {
                                    // start wol
                                }
                            }

                        }));

                final String response = newHandler.login();
                if (!StringUtils.isEmpty(response)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response);
                    SonyUtility.close(newHandler);
                    return;
                }

                _protocolHandler.set(newHandler);

                logger.debug("Simple IP TV System now connected");
                updateStatus(ThingStatus.ONLINE);

                newHandler.postLogin();

                SonyUtility.cancel(_ping.getAndSet(this.scheduler.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        final SimpleIpProtocol newHandler = _protocolHandler.get();
                        if (newHandler != null) {
                            final ThingStatus status = getThing().getStatus();
                            if (status == ThingStatus.ONLINE) {
                                newHandler.ping();
                            }
                        }
                    }
                }, config.getPing(), config.getPing(), TimeUnit.SECONDS)));

                SonyUtility.cancel(_refreshState.getAndSet(this.scheduler.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        final SimpleIpProtocol newHandler = _protocolHandler.get();
                        if (newHandler != null) {
                            final ThingStatus status = getThing().getStatus();
                            if (status == ThingStatus.ONLINE) {
                                newHandler.refreshState();
                            }
                        }

                    }
                }, config.getRefresh(), config.getRefresh(), TimeUnit.SECONDS)));

            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error connecting to simple IP tv");
            }

        } finally {
            _connectLock.unlock();
        }
    }

    /**
     * Attempts to disconnect from the session and will optionally retry the connection attempt. The {@link #_ping} and
     * {@link #_refreshState} will be cancelled and set to null. The {@link SimpleIpProtocol#dispose()} will be called
     * to
     * disconnect and if we are not trying to reconnect, the {@link #_protocolHandler} will be nulled. If we are
     * retrying a connection attempt, {@link #retryConnect()} will be called.
     *
     * @param retryConnection true to retry connection attempts after the disconnect
     */
    private void disconnect(boolean retryConnection) {
        // Cancel ping
        SonyUtility.cancel(_ping.getAndSet(null));
        SonyUtility.cancel(_refreshState.getAndSet(null));

        SonyUtility.close(_protocolHandler.getAndSet(null));

        if (retryConnection) {
            final SimpleIpConfig config = _config.get();
            if (config == null) {
                retryConnect(10);
            } else {
                retryConnect(config.getRetryPolling());
            }
        }
    }

    /**
     * Retries the connection attempt - schedules a job in {@link SimpleIpConfig#getRetryPolling()} seconds to
     * call the {@link #connect()} method. If a retry attempt is pending, the request is ignored.
     *
     * @param delaySeconds the delay seconds
     */
    private void retryConnect(int delaySeconds) {
        final boolean retrying = _retrying.getAndSet(true);
        if (retrying) {
            logger.debug("RetryConnection called when a retry connection is pending - ignoring request");
        } else {
            logger.debug("Will try to reconnect in {} seconds", delaySeconds);
            this.scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        connect();
                    } catch (Exception e) {
                        logger.debug("Exception occurred retrying connect: {}", e.getMessage(), e);
                    } finally {
                        _retrying.set(false);
                    }
                    if (getThing().getStatus() == ThingStatus.OFFLINE) {
                        retryConnect(delaySeconds);
                    }
                }

            }, delaySeconds, TimeUnit.SECONDS);
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
        disconnect(false);
    }
}
