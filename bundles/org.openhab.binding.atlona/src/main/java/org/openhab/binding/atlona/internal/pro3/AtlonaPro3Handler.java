/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.atlona.internal.pro3;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.atlona.internal.AtlonaHandlerCallback;
import org.openhab.binding.atlona.internal.StatefulHandlerCallback;
import org.openhab.binding.atlona.internal.handler.AtlonaHandler;
import org.openhab.binding.atlona.internal.net.SocketChannelSession;
import org.openhab.binding.atlona.internal.net.SocketSession;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link org.openhab.binding.atlona.internal.pro3.AtlonaPro3Handler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Tim Roberts - Initial contribution
 */
public class AtlonaPro3Handler extends AtlonaHandler<AtlonaPro3Capabilities> {

    private final Logger logger = LoggerFactory.getLogger(AtlonaPro3Handler.class);

    /**
     * The {@link AtlonaPro3PortocolHandler} protocol handler
     */
    private AtlonaPro3PortocolHandler atlonaHandler;

    /**
     * The {@link SocketSession} telnet session to the switch. Will be null if not connected.
     */
    private SocketSession session;

    /**
     * The polling job to poll the actual state from the {@link #session}
     */
    private ScheduledFuture<?> polling;

    /**
     * The retry connection event
     */
    private ScheduledFuture<?> retryConnection;

    /**
     * The ping event
     */
    private ScheduledFuture<?> ping;

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
     * Constructs the handler from the {@link org.openhab.core.thing.Thing} with the number of power ports and
     * audio ports the switch supports.
     *
     * @param thing a non-null {@link org.openhab.core.thing.Thing} the handler is for
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
     * where we call {{@link #handleRefresh(ChannelUID)} to handle a refresh of the specific channel (which in turn
     * calls
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
                    if (command instanceof OnOffType onOffCommand) {
                        final boolean makeOn = onOffCommand == OnOffType.ON;
                        atlonaHandler.setPower(makeOn);
                    } else {
                        logger.debug("Received a POWER channel command with a non OnOffType: {}", command);
                    }

                    break;

                case AtlonaPro3Constants.CHANNEL_PANELLOCK:
                    if (command instanceof OnOffType onOffCommand) {
                        final boolean makeOn = onOffCommand == OnOffType.ON;
                        atlonaHandler.setPanelLock(makeOn);
                    } else {
                        logger.debug("Received a PANELLOCK channel command with a non OnOffType: {}", command);
                    }
                    break;

                case AtlonaPro3Constants.CHANNEL_IRENABLE:
                    if (command instanceof OnOffType onOffCommand) {
                        final boolean makeOn = onOffCommand == OnOffType.ON;
                        atlonaHandler.setIrOn(makeOn);
                    } else {
                        logger.debug("Received an IRLOCK channel command with a non OnOffType: {}", command);
                    }

                    break;
                case AtlonaPro3Constants.CHANNEL_MATRIXCMDS:
                    if (command instanceof StringType) {
                        final String matrixCmd = command.toString();
                        Matcher cmd;
                        try {
                            if ((cmd = CMD_MATRIXRESET.matcher(matrixCmd)).matches()) {
                                atlonaHandler.resetMatrix();
                            } else if ((cmd = CMD_MATRIXRESETPORTS.matcher(matrixCmd)).matches()) {
                                atlonaHandler.resetAllPorts();
                            } else if ((cmd = CMD_MATRIXPORTALL.matcher(matrixCmd)).matches()) {
                                if (cmd.groupCount() == 1) {
                                    final int portNbr = Integer.parseInt(cmd.group(1));
                                    atlonaHandler.setPortAll(portNbr);
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
                                    atlonaHandler.saveIoSettings(presetNbr);
                                } else {
                                    logger.debug("Unknown preset save command: '{}'", presetCmd);
                                }
                            } else if ((cmd = CMD_PRESETRECALL.matcher(presetCmd)).matches()) {
                                if (cmd.groupCount() == 1) {
                                    final int presetNbr = Integer.parseInt(cmd.group(1));
                                    atlonaHandler.recallIoSettings(presetNbr);
                                } else {
                                    logger.debug("Unknown preset recall command: '{}'", presetCmd);
                                }
                            } else if ((cmd = CMD_PRESETCLEAR.matcher(presetCmd)).matches()) {
                                if (cmd.groupCount() == 1) {
                                    final int presetNbr = Integer.parseInt(cmd.group(1));
                                    atlonaHandler.clearIoSettings(presetNbr);
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
                            if (command instanceof DecimalType decimalCommand) {
                                final int inpNbr = decimalCommand.intValue();
                                atlonaHandler.setPortSwitch(inpNbr, portNbr);
                            } else {
                                logger.debug("Received a PORTOUTPUT channel command with a non DecimalType: {}",
                                        command);
                            }

                            break;

                        case AtlonaPro3Constants.CHANNEL_PORTPOWER:
                            if (command instanceof OnOffType onOffCommand) {
                                final boolean makeOn = onOffCommand == OnOffType.ON;
                                atlonaHandler.setPortPower(portNbr, makeOn);
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
                            if (command instanceof DecimalType decimalCommand) {
                                final int outPortNbr = decimalCommand.intValue();
                                if (outPortNbr <= 0) {
                                    atlonaHandler.removePortMirror(hdmiPortNbr);
                                } else {
                                    atlonaHandler.setPortMirror(hdmiPortNbr, outPortNbr);
                                }
                            } else {
                                logger.debug("Received a PORTMIRROR channel command with a non DecimalType: {}",
                                        command);
                            }

                            break;
                        case AtlonaPro3Constants.CHANNEL_PORTMIRRORENABLED:
                            if (command instanceof OnOffType) {
                                if (command == OnOffType.ON) {
                                    final StatefulHandlerCallback callback = (StatefulHandlerCallback) atlonaHandler
                                            .getCallback();
                                    final State state = callback.getState(AtlonaPro3Constants.CHANNEL_PORTMIRROR);
                                    int outPortNbr = 1;
                                    if (state instanceof DecimalType decimalCommand) {
                                        outPortNbr = decimalCommand.intValue();
                                    }
                                    atlonaHandler.setPortMirror(hdmiPortNbr, outPortNbr);
                                } else {
                                    atlonaHandler.removePortMirror(hdmiPortNbr);
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
                            if (command instanceof OnOffType onOffCommand) {
                                atlonaHandler.setVolumeMute(portNbr, onOffCommand == OnOffType.ON);
                            } else {
                                logger.debug("Received a VOLUME MUTE channel command with a non OnOffType: {}",
                                        command);
                            }

                            break;
                        case AtlonaPro3Constants.CHANNEL_VOLUME:
                            if (command instanceof DecimalType decimalCommand) {
                                final int level = decimalCommand.intValue();
                                atlonaHandler.setVolume(portNbr, level);
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
        final StatefulHandlerCallback callback = (StatefulHandlerCallback) atlonaHandler.getCallback();

        Matcher m;
        if ((m = GROUP_PRIMARY_PATTERN.matcher(group)).matches()) {
            switch (id) {
                case AtlonaPro3Constants.CHANNEL_POWER:
                    callback.removeState(AtlonaPro3Utilities.createChannelID(group, id));
                    atlonaHandler.refreshPower();
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
                            atlonaHandler.refreshPortStatus(portNbr);
                            break;

                        case AtlonaPro3Constants.CHANNEL_PORTPOWER:
                            atlonaHandler.refreshPortPower(portNbr);
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
                    atlonaHandler.refreshPortMirror(hdmiPortNbr);
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
                            atlonaHandler.refreshVolumeMute(portNbr);
                            break;
                        case AtlonaPro3Constants.CHANNEL_VOLUME:
                            atlonaHandler.refreshVolumeStatus(portNbr);
                            break;

                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Bad Volume Channel (can't parse the port nbr): {}", channelUID.getAsString());
                }

            }
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

        session = new SocketChannelSession(getThing().getUID().getAsString(), config.getIpAddress(), 23);
        atlonaHandler = new AtlonaPro3PortocolHandler(session, config, getCapabilities(),
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
        this.scheduler.schedule(this::connect, 1, TimeUnit.SECONDS);
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
            session.clearListeners();
            session.connect();

            if (this.getCapabilities().isUHDModel()) {
                response = atlonaHandler.loginUHD();
            } else {
                response = atlonaHandler.loginHD();
            }

            if (response == null) {
                final AtlonaPro3Config config = getAtlonaConfig();
                if (config != null) {
                    polling = this.scheduler.scheduleWithFixedDelay(() -> {
                        final ThingStatus status = getThing().getStatus();
                        if (status == ThingStatus.ONLINE) {
                            if (session.isConnected()) {
                                atlonaHandler.refreshAll();
                            } else {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Atlona PRO3 has disconnected. Will try to reconnect later.");
                            }
                        } else if (status == ThingStatus.OFFLINE) {
                            disconnect(true);
                        }
                    }, config.getPolling(), config.getPolling(), TimeUnit.SECONDS);

                    ping = this.scheduler.scheduleWithFixedDelay(() -> {
                        final ThingStatus status = getThing().getStatus();
                        if (status == ThingStatus.ONLINE) {
                            if (session.isConnected()) {
                                atlonaHandler.ping();
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
     * Attempts to disconnect from the session and will optionally retry the connection attempt. The {@link #polling}
     * will be cancelled, the {@link #ping} will be cancelled and both set to null then the {@link #session} will be
     * disconnected.
     *
     * @param retryConnection true to retry connection attempts after the disconnect
     */
    private void disconnect(boolean retryConnection) {
        // Cancel polling
        if (polling != null) {
            polling.cancel(true);
            polling = null;
        }

        // Cancel ping
        if (ping != null) {
            ping.cancel(true);
            ping = null;
        }

        if (session != null) {
            try {
                session.disconnect();
            } catch (IOException e) {
                // ignore - we don't care
            }
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
        if (retryConnection == null) {
            final AtlonaPro3Config config = getAtlonaConfig();
            if (config != null) {
                logger.info("Will try to reconnect in {} seconds", config.getRetryPolling());
                retryConnection = this.scheduler.schedule(() -> {
                    retryConnection = null;
                    connect();
                }, config.getRetryPolling(), TimeUnit.SECONDS);
            }
        } else {
            logger.debug("RetryConnection called when a retry connection is pending - ignoring request");
        }
    }

    /**
     * Simple gets the {@link AtlonaPro3Config} from the {@link Thing} and will set the status to offline if not found.
     *
     * @return {@link AtlonaPro3Config}
     */
    private AtlonaPro3Config getAtlonaConfig() {
        return getThing().getConfiguration().as(AtlonaPro3Config.class);
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
