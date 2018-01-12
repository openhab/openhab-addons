/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal.pro3;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.atlona.handler.AtlonaHandler;
import org.openhab.binding.atlona.internal.AtlonaHandlerCallback;
import org.openhab.binding.atlona.internal.StatefulHandlerCallback;
import org.openhab.binding.atlona.internal.net.SocketChannelSession;
import org.openhab.binding.atlona.internal.net.SocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link org.openhab.binding.atlona.internal.pro3.AtlonaPro3Handler} is responsible for handling commands, which
 * are
 * sent to one of the channels.
 *
 * @author Tim Roberts
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

    // List of all the groups patterns we recognize
    private static final Pattern GROUP_PRIMARY_PATTERN = Pattern.compile("^" + AtlonaPro3Constants.GROUP_PRIMARY + "$");
    private static final Pattern GROUP_PORT_PATTERN = Pattern
            .compile("^" + AtlonaPro3Constants.GROUP_PORT + "(\\d{1,2})$");
    private static final Pattern GROUP_MIRROR_PATTERN = Pattern
            .compile("^" + AtlonaPro3Constants.GROUP_MIRROR + "(\\d{1,2})$");
    private static final Pattern GROUP_VOLUME_PATTERN = Pattern
            .compile("^" + AtlonaPro3Constants.GROUP_VOLUME + "(\\d{1,2})$");

    // List of preset commands we recognize
    private static final Pattern CMD_PRESETSAVE = Pattern
            .compile("^" + AtlonaPro3Constants.CMD_PRESETSAVE + "(\\d{1,2})$");
    private static final Pattern CMD_PRESETRECALL = Pattern
            .compile("^" + AtlonaPro3Constants.CMD_PRESETRECALL + "(\\d{1,2})$");
    private static final Pattern CMD_PRESETCLEAR = Pattern
            .compile("^" + AtlonaPro3Constants.CMD_PRESETCLEAR + "(\\d{1,2})$");

    // List of matrix commands we recognize
    private static final Pattern CMD_MATRIXRESET = Pattern.compile("^" + AtlonaPro3Constants.CMD_MATRIXRESET + "$");
    private static final Pattern CMD_MATRIXRESETPORTS = Pattern
            .compile("^" + AtlonaPro3Constants.CMD_MATRIXRESETPORTS + "$");
    private static final Pattern CMD_MATRIXPORTALL = Pattern
            .compile("^" + AtlonaPro3Constants.CMD_MATRIXPORTALL + "(\\d{1,2})$");

    /**
     * Constructs the handler from the {@link org.eclipse.smarthome.core.thing.Thing} with the number of power ports and
     * audio ports the switch supports.
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
            handleRefresh(channelUID);
            return;
        }

        final String group = channelUID.getGroupId().toLowerCase();
        final String id = channelUID.getIdWithoutGroup().toLowerCase();

        Matcher m;
        if ((m = GROUP_PRIMARY_PATTERN.matcher(group)).matches()) {
            switch (id) {
                case AtlonaPro3Constants.CHANNEL_POWER:
                    if (command instanceof OnOffType) {
                        final boolean makeOn = ((OnOffType) command) == OnOffType.ON;
                        _atlonaHandler.setPower(makeOn);
                    } else {
                        logger.debug("Received a POWER channel command with a non OnOffType: {}", command);
                    }

                    break;

                case AtlonaPro3Constants.CHANNEL_PANELLOCK:
                    if (command instanceof OnOffType) {
                        final boolean makeOn = ((OnOffType) command) == OnOffType.ON;
                        _atlonaHandler.setPanelLock(makeOn);
                    } else {
                        logger.debug("Received a PANELLOCK channel command with a non OnOffType: {}", command);
                    }
                    break;

                case AtlonaPro3Constants.CHANNEL_IRENABLE:
                    if (command instanceof OnOffType) {
                        final boolean makeOn = ((OnOffType) command) == OnOffType.ON;
                        _atlonaHandler.setIrOn(makeOn);
                    } else {
                        logger.debug("Received a IRLOCK channel command with a non OnOffType: {}", command);
                    }

                    break;
                case AtlonaPro3Constants.CHANNEL_MATRIXCMDS:
                    if (command instanceof StringType) {
                        final String matrixCmd = command.toString();
                        Matcher cmd;
                        try {
                            if ((cmd = CMD_MATRIXRESET.matcher(matrixCmd)).matches()) {
                                _atlonaHandler.resetMatrix();
                            } else if ((cmd = CMD_MATRIXRESETPORTS.matcher(matrixCmd)).matches()) {
                                _atlonaHandler.resetAllPorts();
                            } else if ((cmd = CMD_MATRIXPORTALL.matcher(matrixCmd)).matches()) {
                                if (cmd.groupCount() == 1) {
                                    final int portNbr = Integer.parseInt(cmd.group(1));
                                    _atlonaHandler.setPortAll(portNbr);
                                } else {
                                    logger.debug("Unknown matirx set port command: '{}'", matrixCmd);
                                }

                            } else {
                                logger.debug("Unknown matrix command: '{}'", cmd);
                            }
                        } catch (NumberFormatException e) {
                            logger.debug("Could not parse the port number from the command: '{}'", matrixCmd);
                        }
                    }
                    break;
                case AtlonaPro3Constants.CHANNEL_PRESETCMDS:
                    if (command instanceof StringType) {
                        final String presetCmd = command.toString();
                        Matcher cmd;
                        try {
                            if ((cmd = CMD_PRESETSAVE.matcher(presetCmd)).matches()) {
                                if (cmd.groupCount() == 1) {
                                    final int presetNbr = Integer.parseInt(cmd.group(1));
                                    _atlonaHandler.saveIoSettings(presetNbr);
                                } else {
                                    logger.debug("Unknown preset save command: '{}'", presetCmd);
                                }
                            } else if ((cmd = CMD_PRESETRECALL.matcher(presetCmd)).matches()) {
                                if (cmd.groupCount() == 1) {
                                    final int presetNbr = Integer.parseInt(cmd.group(1));
                                    _atlonaHandler.recallIoSettings(presetNbr);
                                } else {
                                    logger.debug("Unknown preset recall command: '{}'", presetCmd);
                                }
                            } else if ((cmd = CMD_PRESETCLEAR.matcher(presetCmd)).matches()) {
                                if (cmd.groupCount() == 1) {
                                    final int presetNbr = Integer.parseInt(cmd.group(1));
                                    _atlonaHandler.clearIoSettings(presetNbr);
                                } else {
                                    logger.debug("Unknown preset clear command: '{}'", presetCmd);
                                }

                            } else {
                                logger.debug("Unknown preset command: '{}'", cmd);
                            }
                        } catch (NumberFormatException e) {
                            logger.debug("Could not parse the preset number from the command: '{}'", presetCmd);
                        }
                    }
                    break;

                default:
                    logger.debug("Unknown/Unsupported Primary Channel: {}", channelUID.getAsString());
                    break;
            }

        } else if ((m = GROUP_PORT_PATTERN.matcher(group)).matches()) {
            if (m.groupCount() == 1) {
                try {
                    final int portNbr = Integer.parseInt(m.group(1));

                    switch (id) {
                        case AtlonaPro3Constants.CHANNEL_PORTOUTPUT:
                            if (command instanceof DecimalType) {
                                final int inpNbr = ((DecimalType) command).intValue();
                                _atlonaHandler.setPortSwitch(inpNbr, portNbr);
                            } else {
                                logger.debug("Received a PORTOUTPUT channel command with a non DecimalType: {}",
                                        command);
                            }

                            break;

                        case AtlonaPro3Constants.CHANNEL_PORTPOWER:
                            if (command instanceof OnOffType) {
                                final boolean makeOn = ((OnOffType) command) == OnOffType.ON;
                                _atlonaHandler.setPortPower(portNbr, makeOn);
                            } else {
                                logger.debug("Received a PORTPOWER channel command with a non OnOffType: {}", command);
                            }
                            break;
                        default:
                            logger.debug("Unknown/Unsupported Port Channel: {}", channelUID.getAsString());
                            break;
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Bad Port Channel (can't parse the port nbr): {}", channelUID.getAsString());
                }
            }
        } else if ((m = GROUP_MIRROR_PATTERN.matcher(group)).matches()) {
            if (m.groupCount() == 1) {
                try {
                    final int hdmiPortNbr = Integer.parseInt(m.group(1));

                    switch (id) {
                        case AtlonaPro3Constants.CHANNEL_PORTMIRROR:
                            if (command instanceof DecimalType) {
                                final int outPortNbr = ((DecimalType) command).intValue();
                                if (outPortNbr <= 0) {
                                    _atlonaHandler.removePortMirror(hdmiPortNbr);
                                } else {
                                    _atlonaHandler.setPortMirror(hdmiPortNbr, outPortNbr);
                                }
                            } else {
                                logger.debug("Received a PORTMIRROR channel command with a non DecimalType: {}",
                                        command);
                            }

                            break;
                        case AtlonaPro3Constants.CHANNEL_PORTMIRRORENABLED:
                            if (command instanceof OnOffType) {
                                if (command == OnOffType.ON) {
                                    final StatefulHandlerCallback callback = (StatefulHandlerCallback) _atlonaHandler
                                            .getCallback();
                                    final State state = callback.getState(AtlonaPro3Constants.CHANNEL_PORTMIRROR);
                                    int outPortNbr = 1;
                                    if (state != null && state instanceof DecimalType) {
                                        outPortNbr = ((DecimalType) state).intValue();
                                    }
                                    _atlonaHandler.setPortMirror(hdmiPortNbr, outPortNbr);
                                } else {
                                    _atlonaHandler.removePortMirror(hdmiPortNbr);
                                }
                            } else {
                                logger.debug("Received a PORTMIRROR channel command with a non DecimalType: {}",
                                        command);
                            }

                            break;
                        default:
                            logger.debug("Unknown/Unsupported Mirror Channel: {}", channelUID.getAsString());
                            break;
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Bad Mirror Channel (can't parse the port nbr): {}", channelUID.getAsString());
                }
            }
        } else if ((m = GROUP_VOLUME_PATTERN.matcher(group)).matches()) {
            if (m.groupCount() == 1) {
                try {
                    final int portNbr = Integer.parseInt(m.group(1));

                    switch (id) {
                        case AtlonaPro3Constants.CHANNEL_VOLUME_MUTE:
                            if (command instanceof OnOffType) {
                                _atlonaHandler.setVolumeMute(portNbr, ((OnOffType) command) == OnOffType.ON);
                            } else {
                                logger.debug("Received a VOLUME MUTE channel command with a non OnOffType: {}",
                                        command);
                            }

                            break;
                        case AtlonaPro3Constants.CHANNEL_VOLUME:
                            if (command instanceof DecimalType) {
                                final double level = ((DecimalType) command).doubleValue();
                                _atlonaHandler.setVolume(portNbr, level);
                            } else {
                                logger.debug("Received a VOLUME channel command with a non DecimalType: {}", command);
                            }
                            break;

                        default:
                            logger.debug("Unknown/Unsupported Volume Channel: {}", channelUID.getAsString());
                            break;
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Bad Volume Channel (can't parse the port nbr): {}", channelUID.getAsString());
                }
            }
        } else {
            logger.debug("Unknown/Unsupported Channel: {}", channelUID.getAsString());
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link AtlonaPro3PortocolHandler} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(ChannelUID channelUID) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        final String group = channelUID.getGroupId().toLowerCase();
        final String id = channelUID.getIdWithoutGroup().toLowerCase();
        final StatefulHandlerCallback callback = (StatefulHandlerCallback) _atlonaHandler.getCallback();

        Matcher m;
        if ((m = GROUP_PRIMARY_PATTERN.matcher(group)).matches()) {
            switch (id) {
                case AtlonaPro3Constants.CHANNEL_POWER:
                    callback.removeState(AtlonaPro3Utilities.createChannelID(group, id));
                    _atlonaHandler.refreshPower();
                    break;

                default:
                    break;
            }

        } else if ((m = GROUP_PORT_PATTERN.matcher(group)).matches()) {
            if (m.groupCount() == 1) {
                try {
                    final int portNbr = Integer.parseInt(m.group(1));
                    callback.removeState(AtlonaPro3Utilities.createChannelID(group, portNbr, id));

                    switch (id) {
                        case AtlonaPro3Constants.CHANNEL_PORTOUTPUT:
                            _atlonaHandler.refreshPortStatus(portNbr);
                            break;

                        case AtlonaPro3Constants.CHANNEL_PORTPOWER:
                            _atlonaHandler.refreshPortPower(portNbr);
                            break;
                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Bad Port Channel (can't parse the port nbr): {}", channelUID.getAsString());
                }

            }
        } else if ((m = GROUP_MIRROR_PATTERN.matcher(group)).matches()) {
            if (m.groupCount() == 1) {
                try {
                    final int hdmiPortNbr = Integer.parseInt(m.group(1));
                    callback.removeState(AtlonaPro3Utilities.createChannelID(group, hdmiPortNbr, id));
                    _atlonaHandler.refreshPortMirror(hdmiPortNbr);
                } catch (NumberFormatException e) {
                    logger.debug("Bad Mirror Channel (can't parse the port nbr): {}", channelUID.getAsString());
                }

            }
        } else if ((m = GROUP_VOLUME_PATTERN.matcher(group)).matches()) {
            if (m.groupCount() == 1) {
                try {
                    final int portNbr = Integer.parseInt(m.group(1));
                    callback.removeState(AtlonaPro3Utilities.createChannelID(group, portNbr, id));

                    switch (id) {
                        case AtlonaPro3Constants.CHANNEL_VOLUME_MUTE:
                            _atlonaHandler.refreshVolumeMute(portNbr);
                            break;
                        case AtlonaPro3Constants.CHANNEL_VOLUME:
                            _atlonaHandler.refreshVolumeStatus(portNbr);
                            break;

                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Bad Volume Channel (can't parse the port nbr): {}", channelUID.getAsString());
                }

            }
        } else {
            // nothing else matters...
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

        _session = new SocketChannelSession(config.getIpAddress(), 23);
        _atlonaHandler = new AtlonaPro3PortocolHandler(_session, config, getCapabilities(),
                new StatefulHandlerCallback(new AtlonaHandlerCallback() {
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

                    @Override
                    public void setProperty(String propertyName, String propertyValue) {
                        getThing().setProperty(propertyName, propertyValue);
                    }
                }));

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
            // clear listeners to avoid any 'old' listener from handling initial messages
            _session.clearListeners();
            _session.connect();

            response = _atlonaHandler.login();
            if (response == null) {
                final AtlonaPro3Config config = getAtlonaConfig();
                if (config != null) {

                    _polling = this.scheduler.scheduleWithFixedDelay(new Runnable() {
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

                    _ping = this.scheduler.scheduleWithFixedDelay(new Runnable() {
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
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
