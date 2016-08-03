/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal.pro3;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.atlona.handler.AtlonaHandler;
import org.openhab.binding.atlona.internal.SocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link org.openhab.binding.atlona.internal.pro3.AtlonaPro3Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tim Roberts
 * @version $Id: $Id
 */
public class AtlonaPro3Handler extends AtlonaHandler<AtlonaPro3Capabilities> {

    private Logger logger = LoggerFactory.getLogger(AtlonaPro3Handler.class);

    /**
     * The {@link AtlonaPro3PortocolHandler} protocol handler
     */
    private AtlonaPro3PortocolHandler _atlonaHandler;

    /**
     * The {@link SocketSession} telnet session to the switch. Will be null if not connected.
     */
    private SocketSession _session;

    /**
     * The polling job to poll the actual state from the {@link #_session}
     */
    private ScheduledFuture<?> _polling;

    /**
     * The retry connection event
     */
    private ScheduledFuture<?> _retryConnection;

    /**
     * The ping event
     */
    private ScheduledFuture<?> _ping;

    /**
     * Constructs the handler from the {@link org.eclipse.smarthome.core.thing.Thing} with the number of power ports and audio ports the switch supports.
     *
     * @param thing a non-null {@link org.eclipse.smarthome.core.thing.Thing} the handler is for
     * @param capabilities a non-null {@link org.openhab.binding.atlona.internal.pro3.AtlonaPro3Capabilities}
     */
    public AtlonaPro3Handler(Thing thing, AtlonaPro3Capabilities capabilities) {
        super(thing, capabilities);

        if (thing == null) {
            throw new IllegalArgumentException("thing cannot be null");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link AtlonaPro3PortocolHandler}. Basically we validate the type of command for the channel then call the
     * {@link AtlonaPro3PortocolHandler} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link AtlonaPro3PortocolHandler} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }

        // if (getThing().getStatus() != ThingStatus.ONLINE) {
        // // Ignore any command if not online
        // return;
        // }

        String id = channelUID.getId();

        if (id == null) {
            logger.warn("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(AtlonaPro3Constants.CHANNEL_POWER)) {
            if (command instanceof OnOffType) {
                final boolean makeOn = ((OnOffType) command) == OnOffType.ON;
                _atlonaHandler.setPower(makeOn);
            } else {
                logger.error("Received a POWER channel command with a non OnOffType: " + command);
            }

        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_PORTOUTPUT)) {
            final Integer portNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_PORTOUTPUT);
            if (portNbr != null) {
                if (command instanceof DecimalType) {
                    final int inpNbr = ((DecimalType) command).intValue();
                    _atlonaHandler.setPortSwitch(inpNbr, portNbr);
                } else {
                    logger.error("Received a PORTOUTPUT channel command with a non DecimalType: " + command);
                }
            }

        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_VOLUME_MUTE)) {
            final Integer portNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_VOLUME_MUTE);

            if (portNbr != null) {
                if (command instanceof OnOffType) {
                    _atlonaHandler.setVolumeMute(portNbr, ((OnOffType) command) == OnOffType.ON);
                } else {
                    logger.error("Received a VOLUME MUTE channel command with a non OnOffType: " + command);
                }
            }

        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_VOLUME)) {
            final Integer portNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_VOLUME);

            if (portNbr != null) {
                if (command instanceof DecimalType) {
                    final double level = ((DecimalType) command).doubleValue();
                    _atlonaHandler.setVolume(portNbr, level);
                } else {
                    logger.error("Received a VOLUME channel command with a non DecimalType: " + command);
                }
            }
        } else if (id.equals(AtlonaPro3Constants.CHANNEL_VERSION)) {
            _atlonaHandler.refreshVersion();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_TYPE)) {
            _atlonaHandler.refreshType();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_PANELLOCK)) {
            if (command instanceof OnOffType) {
                final boolean makeOn = ((OnOffType) command) == OnOffType.ON;
                _atlonaHandler.setPanelLock(makeOn);
            } else {
                logger.error("Received a PANELLOCK channel command with a non OnOffType: " + command);
            }

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_RESETPORTS)) {
            _atlonaHandler.resetAllPorts();

        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_PORTPOWER)) {
            final Integer portNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_PORTPOWER);

            if (portNbr != null) {
                if (command instanceof OnOffType) {
                    final boolean makeOn = ((OnOffType) command) == OnOffType.ON;
                    _atlonaHandler.setPortPower(portNbr, makeOn);
                } else {
                    logger.error("Received a PORTPOWER channel command with a non OnOffType: " + command);
                }
            }

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_PORTALL)) {
            if (command instanceof DecimalType) {
                final int portNbr = ((DecimalType) command).intValue();
                _atlonaHandler.setPortAll(portNbr);
            } else {
                logger.error("Received a PORTMIRROR channel command with a non DecimalType: " + command);
            }

        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_PORTMIRROR)) {
            final Integer hdmiPortNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_PORTMIRROR);
            if (hdmiPortNbr != null) {
                if (command instanceof DecimalType) {
                    final int outPortNbr = ((DecimalType) command).intValue();
                    if (outPortNbr <= 0) {
                        _atlonaHandler.removePortMirror(hdmiPortNbr);
                    } else {
                        _atlonaHandler.setPortMirror(hdmiPortNbr, outPortNbr);
                    }
                } else {
                    logger.error("Received a PORTMIRROR channel command with a non DecimalType: " + command);
                }
            }

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_IRENABLE)) {
            if (command instanceof OnOffType) {
                final boolean makeOn = ((OnOffType) command) == OnOffType.ON;
                _atlonaHandler.setIrOn(makeOn);
            } else {
                logger.error("Received a IRLOCK channel command with a non OnOffType: " + command);
            }

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_SAVEIO)) {
            if (command instanceof DecimalType) {
                final int presetNbr = ((DecimalType) command).intValue();
                _atlonaHandler.saveIoSettings(presetNbr);
            } else {
                logger.error("Received a SAVEIO channel command with a non DecimalType: " + command);
            }

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_RECALLIO)) {
            if (command instanceof DecimalType) {
                final int presetNbr = ((DecimalType) command).intValue();
                _atlonaHandler.recallIoSettings(presetNbr);
            } else {
                logger.error("Received a RECALLIO channel command with a non DecimalType: " + command);
            }

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_CLEARIO)) {
            if (command instanceof DecimalType) {
                final int presetNbr = ((DecimalType) command).intValue();
                _atlonaHandler.clearIoSettings(presetNbr);
            } else {
                logger.error("Received a CLEARIO channel command with a non DecimalType: " + command);
            }

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_RESETMATRIX)) {
            _atlonaHandler.resetMatrix();

            // } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_RS232)) {
            // final Integer zoneNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_PORTMIRROR);
            // if (zoneNbr != null) {
            // if (command instanceof StringType) {
            // final String rs232Cmd = ((StringType) command).toString();
            // _atlonaHandler.sendRS232Command(zoneNbr, rs232Cmd);
            // } else {
            // logger.error("Received a RS232 channel command with a non DecimalType: " + command);
            // }
            //
            // }

        } else {
            logger.error("Unknown/Unsupported Channel id: " + id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link AtlonaPro3PortocolHandler} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (id.equals(AtlonaPro3Constants.CHANNEL_POWER)) {
            _atlonaHandler.refreshPower();

        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_PORTOUTPUT)) {
            final Integer portNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_PORTOUTPUT);
            if (portNbr != null) {
                _atlonaHandler.refreshPortStatus(portNbr);
            }

            // Since we are using startsWith - "volumemute" must come before "volume" check
        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_VOLUME_MUTE)) {
            final Integer portNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_VOLUME_MUTE);

            if (portNbr != null) {
                _atlonaHandler.refreshVolumeMute(portNbr);
            }

        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_VOLUME)) {
            final Integer portNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_VOLUME);

            if (portNbr != null) {
                _atlonaHandler.refreshVolumeStatus(portNbr);
            }
        } else if (id.equals(AtlonaPro3Constants.CHANNEL_VERSION)) {
            _atlonaHandler.refreshVersion();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_TYPE)) {
            _atlonaHandler.refreshType();

        } else if (id.startsWith(AtlonaPro3Constants.CHANNEL_PORTPOWER)) {
            final Integer portNbr = getTrailingNbr(id, AtlonaPro3Constants.CHANNEL_PORTPOWER);

            if (portNbr != null) {
                _atlonaHandler.refreshPortPower(portNbr);
            }

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_PANELLOCK)) {
            _atlonaHandler.refreshPanelLock();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_RESETPORTS)) {
            _atlonaHandler.refreshResetAllPorts();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_PORTALL)) {
            _atlonaHandler.refreshPortAll();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_IRENABLE)) {
            _atlonaHandler.refreshIrEnable();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_SAVEIO)) {
            _atlonaHandler.refreshSaveIo();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_RECALLIO)) {
            _atlonaHandler.refreshRecallIo();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_CLEARIO)) {
            _atlonaHandler.refreshClearIo();

        } else if (id.equals(AtlonaPro3Constants.CHANNEL_RESETMATRIX)) {
            _atlonaHandler.refreshResetMatrix();

        } else {
            // Can't refresh any others...
        }
    }

    /**
     * Gets the trailing number from the channel id (which usually represents the output port number).
     *
     * @param id a non-null, possibly empty channel id
     * @param channelConstant a non-null, non-empty channel id constant to use in the parse.
     * @return the trailing number or null if a parse exception occurs
     */
    private Integer getTrailingNbr(String id, String channelConstant) {
        try {
            return Integer.parseInt(id.substring(channelConstant.length()));
        } catch (NumberFormatException e) {
            logger.warn("Unknown channel port #: " + id);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, then will create the
     * {@link SocketSession}, initialize the {@link AtlonaPro3PortocolHandler} and will attempt to connect to the switch
     * (via {{@link #retryConnect()}.
     */
    @Override
    public void initialize() {
        final AtlonaPro3Config config = getAtlonaConfig();

        if (config == null) {
            return;
        }

        if (config.getIpAddress() == null || config.getIpAddress().trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP Address of Atlona Pro3 is missing from configuration");
            return;
        }

        _session = new SocketSession(config.getIpAddress(), 23);
        _atlonaHandler = new AtlonaPro3PortocolHandler(_session, config, getCapabilities(),
                new AtlonaPro3HandlerCallback() {
                    @Override
                    public void stateChanged(String channelId, State state) {
                        updateState(channelId, state);
                    }

                    @Override
                    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                        updateStatus(status, detail, msg);

                        if (status != ThingStatus.ONLINE) {
                            disconnect(true);
                        }
                    }
                });

        // Try initial connection in a scheduled task
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }

        }, 1, TimeUnit.SECONDS);
    }

    /**
     * Attempts to connect to the switch. If successfully connect, the {@link AtlonaPro3PortocolHandler#login()} will be
     * called to log into the switch (if needed). Once completed, a polling job will be created to poll the switch's
     * actual state and a ping job to ping the server. If a connection cannot be established (or login failed), the
     * connection attempt will be retried later (via {@link #retryConnect()})
     */
    private void connect() {
        String response = "Server is offline - will try to reconnect later";
        try {
            _session.connect();

            response = _atlonaHandler.login();
            if (response == null) {
                final AtlonaPro3Config config = getAtlonaConfig();
                if (config != null) {

                    _polling = this.scheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            final ThingStatus status = getThing().getStatus();
                            if (status == ThingStatus.ONLINE) {
                                if (_session.isConnected()) {
                                    _atlonaHandler.refreshAll();
                                } else {
                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                            "Atlona PRO3 has disconnected. Will try to reconnect later.");

                                }
                            } else if (status == ThingStatus.OFFLINE) {
                                disconnect(true);
                            }

                        }
                    }, config.getPolling(), config.getPolling(), TimeUnit.SECONDS);

                    _ping = this.scheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            final ThingStatus status = getThing().getStatus();
                            if (status == ThingStatus.ONLINE) {
                                if (_session.isConnected()) {
                                    _atlonaHandler.ping();
                                }
                            }

                        }
                    }, config.getPing(), config.getPing(), TimeUnit.SECONDS);

                    updateStatus(ThingStatus.ONLINE);
                    return;
                }
            }

        } catch (Exception e) {
            // do nothing
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response);
        retryConnect();
    }

    /**
     * Attempts to disconnect from the session and will optionally retry the connection attempt. The {@link #_polling}
     * will be cancelled, the {@link #_ping} will be cancelled and both set to null then the {@link #_session} will be
     * disconnected.
     *
     * @param retryConnection true to retry connection attempts after the disconnect
     */
    private void disconnect(boolean retryConnection) {
        // Cancel polling
        if (_polling != null) {
            _polling.cancel(true);
            _polling = null;
        }

        // Cancel ping
        if (_ping != null) {
            _ping.cancel(true);
            _ping = null;
        }

        try {
            _session.disconnect();
        } catch (IOException e) {
            // ignore - we don't care
        }

        if (retryConnection) {
            retryConnect();
        }
    }

    /**
     * Retries the connection attempt - schedules a job in {@link AtlonaPro3Config#getRetryPolling()} seconds to call
     * the
     * {@link #connect()} method. If a retry attempt is pending, the request is ignored.
     */
    private void retryConnect() {
        if (_retryConnection == null) {
            final AtlonaPro3Config config = getAtlonaConfig();
            if (config != null) {

                logger.info("Will try to reconnect in {} seconds", config.getRetryPolling());
                _retryConnection = this.scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        _retryConnection = null;
                        connect();
                    }

                }, config.getRetryPolling(), TimeUnit.SECONDS);
            }
        } else {
            logger.debug("RetryConnection called when a retry connection is pending - ignoring request");
        }
    }

    /**
     * Simple gets the {@link AtlonaPro3Config} from the {@link Thing} and will set the status to offline if not found.
     *
     * @return a possible null {@link AtlonaPro3Config}
     */
    private AtlonaPro3Config getAtlonaConfig() {
        final AtlonaPro3Config config = getThing().getConfiguration().as(AtlonaPro3Config.class);

        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
        }

        return config;
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
