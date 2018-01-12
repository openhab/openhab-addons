/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.handler;

import static org.openhab.binding.onkyo.OnkyoBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.onkyo.internal.OnkyoAlbumArt;
import org.openhab.binding.onkyo.internal.OnkyoConnection;
import org.openhab.binding.onkyo.internal.OnkyoEventListener;
import org.openhab.binding.onkyo.internal.ServiceType;
import org.openhab.binding.onkyo.internal.config.OnkyoDeviceConfiguration;
import org.openhab.binding.onkyo.internal.eiscp.EiscpCommand;
import org.openhab.binding.onkyo.internal.eiscp.EiscpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OnkyoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Frank - Initial contribution
 * @author Marcel Verpaalen - parsing additional commands
 * @author Pauli Anttila - lot of refactoring
 */
public class OnkyoHandler extends UpnpAudioSinkHandler implements OnkyoEventListener {

    private Logger logger = LoggerFactory.getLogger(OnkyoHandler.class);

    private OnkyoDeviceConfiguration configuration;

    private OnkyoConnection connection;
    private ScheduledFuture<?> resourceUpdaterFuture;
    @SuppressWarnings("unused")
    private int currentInput = -1;
    private State volumeLevelZone1 = UnDefType.UNDEF;
    private State volumeLevelZone2 = UnDefType.UNDEF;
    private State volumeLevelZone3 = UnDefType.UNDEF;

    private OnkyoAlbumArt onkyoAlbumArt = new OnkyoAlbumArt();

    private final int NET_USB_ID = 43;

    public OnkyoHandler(Thing thing, UpnpIOService upnpIOService, AudioHTTPServer audioHTTPServer, String callbackUrl) {
        super(thing, upnpIOService, audioHTTPServer, callbackUrl);
    }

    /**
     * Initialize the state of the receiver.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing handler for Onkyo Receiver");
        configuration = getConfigAs(OnkyoDeviceConfiguration.class);
        logger.info("Using configuration: {}", configuration.toString());

        connection = new OnkyoConnection(configuration.ipAddress, configuration.port);
        connection.addEventListener(this);

        scheduler.execute(new Runnable() {

            @Override
            public void run() {
                logger.debug("Open connection to Onkyo Receiver @{}", connection.getConnectionName());
                connection.openConnection();
                if (connection.isConnected()) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        });

        if (configuration.refreshInterval > 0) {
            // Start resource refresh updater
            resourceUpdaterFuture = scheduler.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    try {
                        logger.debug("Send resource update requests to Onkyo Receiver @{}",
                                connection.getConnectionName());
                        checkStatus();
                    } catch (LinkageError e) {
                        logger.warn("Failed to send resource update requests to Onkyo Receiver @{}. Cause: {}",
                                connection.getConnectionName(), e.getMessage());
                    } catch (Exception ex) {
                        logger.warn("Exception in resource refresh Thread Onkyo Receiver @{}. Cause: {}",
                                connection.getConnectionName(), ex.getMessage());
                    }
                }
            }, configuration.refreshInterval, configuration.refreshInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (resourceUpdaterFuture != null) {
            resourceUpdaterFuture.cancel(true);
        }
        if (connection != null) {
            connection.removeEventListener(this);
            connection.closeConnection();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand for channel {}: {}", channelUID.getId(), command.toString());
        switch (channelUID.getId()) {

            /*
             * ZONE 1
             */

            case CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    sendCommand(EiscpCommand.POWER_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.POWER_QUERY);
                }
                break;
            case CHANNEL_MUTE:
                if (command instanceof OnOffType) {
                    sendCommand(EiscpCommand.MUTE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.MUTE_QUERY);
                }
                break;
            case CHANNEL_VOLUME:
                handleVolumeSet(EiscpCommand.Zone.ZONE1, volumeLevelZone1, command);
                break;
            case CHANNEL_INPUT:
                if (command instanceof DecimalType) {
                    selectInput(((DecimalType) command).intValue());
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.SOURCE_QUERY);
                }
                break;
            case CHANNEL_LISTENMODE:
                if (command instanceof DecimalType) {
                    sendCommand(EiscpCommand.LISTEN_MODE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.LISTEN_MODE_QUERY);
                }
                break;

            /*
             * ZONE 2
             */

            case CHANNEL_POWERZONE2:
                if (command instanceof OnOffType) {
                    sendCommand(EiscpCommand.ZONE2_POWER_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.ZONE2_POWER_QUERY);
                }
                break;
            case CHANNEL_MUTEZONE2:
                if (command instanceof OnOffType) {
                    sendCommand(EiscpCommand.ZONE2_MUTE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.ZONE2_MUTE_QUERY);
                }
                break;
            case CHANNEL_VOLUMEZONE2:
                handleVolumeSet(EiscpCommand.Zone.ZONE2, volumeLevelZone2, command);
                break;
            case CHANNEL_INPUTZONE2:
                if (command instanceof DecimalType) {
                    sendCommand(EiscpCommand.ZONE2_SOURCE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.ZONE2_SOURCE_QUERY);
                }
                break;

            /*
             * ZONE 3
             */

            case CHANNEL_POWERZONE3:
                if (command instanceof OnOffType) {
                    sendCommand(EiscpCommand.ZONE3_POWER_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.ZONE3_POWER_QUERY);
                }
                break;
            case CHANNEL_MUTEZONE3:
                if (command instanceof OnOffType) {
                    sendCommand(EiscpCommand.ZONE3_MUTE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.ZONE3_MUTE_QUERY);
                }
                break;
            case CHANNEL_VOLUMEZONE3:
                handleVolumeSet(EiscpCommand.Zone.ZONE3, volumeLevelZone3, command);
                break;
            case CHANNEL_INPUTZONE3:
                if (command instanceof DecimalType) {
                    sendCommand(EiscpCommand.ZONE3_SOURCE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.ZONE3_SOURCE_QUERY);
                }
                break;

            /*
             * NET PLAYER
             */

            case CHANNEL_CONTROL:
                if (command instanceof PlayPauseType) {
                    if (command.equals(PlayPauseType.PLAY)) {
                        sendCommand(EiscpCommand.NETUSB_OP_PLAY);
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        sendCommand(EiscpCommand.NETUSB_OP_PAUSE);
                    }
                } else if (command instanceof NextPreviousType) {
                    if (command.equals(NextPreviousType.NEXT)) {
                        sendCommand(EiscpCommand.NETUSB_OP_TRACKUP);
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        sendCommand(EiscpCommand.NETUSB_OP_TRACKDWN);
                    }
                } else if (command instanceof RewindFastforwardType) {
                    if (command.equals(RewindFastforwardType.REWIND)) {
                        sendCommand(EiscpCommand.NETUSB_OP_REW);
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        sendCommand(EiscpCommand.NETUSB_OP_FF);
                    }
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.NETUSB_PLAY_STATUS_QUERY);
                }
                break;
            case CHANNEL_PLAY_URI:
                handlePlayUri(command);
                break;
            case CHANNEL_ALBUM_ART:
            case CHANNEL_ALBUM_ART_URL:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.NETUSB_ALBUM_ART_QUERY);
                }
                break;
            case CHANNEL_ARTIST:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.NETUSB_SONG_ARTIST_QUERY);
                }
                break;
            case CHANNEL_ALBUM:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.NETUSB_SONG_ALBUM_QUERY);
                }
                break;
            case CHANNEL_TITLE:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.NETUSB_SONG_TITLE_QUERY);
                }
                break;
            case CHANNEL_CURRENTPLAYINGTIME:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.NETUSB_SONG_ELAPSEDTIME_QUERY);
                }
                break;

            /*
             * NET MENU
             */

            case CHANNEL_NET_MENU_CONTROL:
                if (command instanceof StringType) {
                    final String cmdName = command.toString();
                    handleNetMenuCommand(cmdName);
                }
                break;
            case CHANNEL_NET_MENU_TITLE:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommand.NETUSB_TITLE_QUERY);
                }
                break;

            /*
             * MISC
             */

            default:
                logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                break;
        }
    }

    @Override
    public void statusUpdateReceived(String ip, EiscpMessage data) {
        logger.debug("Received status update from Onkyo Receiver @{}: data={}", connection.getConnectionName(), data);

        updateStatus(ThingStatus.ONLINE);

        try {
            EiscpCommand receivedCommand = null;

            try {
                receivedCommand = EiscpCommand.getCommandByCommandAndValueStr(data.getCommand(), "");
            } catch (IllegalArgumentException ex) {
                logger.debug("Received unknown status update from Onkyo Receiver @{}: data={}",
                        connection.getConnectionName(), data);
                return;
            }

            logger.debug("Received command {}", receivedCommand);

            switch (receivedCommand) {

                /*
                 * ZONE 1
                 */
                case POWER:
                    updateState(CHANNEL_POWER, convertDeviceValueToOpenHabState(data.getValue(), OnOffType.class));
                    break;
                case MUTE:
                    updateState(CHANNEL_MUTE, convertDeviceValueToOpenHabState(data.getValue(), OnOffType.class));
                    break;
                case VOLUME:
                    volumeLevelZone1 = handleReceivedVolume(
                            convertDeviceValueToOpenHabState(data.getValue(), PercentType.class));
                    updateState(CHANNEL_VOLUME, volumeLevelZone1);
                    break;
                case SOURCE:
                    updateState(CHANNEL_INPUT, convertDeviceValueToOpenHabState(data.getValue(), DecimalType.class));
                    break;
                case LISTEN_MODE:
                    updateState(CHANNEL_LISTENMODE,
                            convertDeviceValueToOpenHabState(data.getValue(), DecimalType.class));
                    break;

                /*
                 * ZONE 2
                 */
                case ZONE2_POWER:
                    updateState(CHANNEL_POWERZONE2, convertDeviceValueToOpenHabState(data.getValue(), OnOffType.class));
                    break;
                case ZONE2_MUTE:
                    updateState(CHANNEL_MUTEZONE2, convertDeviceValueToOpenHabState(data.getValue(), OnOffType.class));
                    break;
                case ZONE2_VOLUME:
                    volumeLevelZone2 = handleReceivedVolume(
                            convertDeviceValueToOpenHabState(data.getValue(), PercentType.class));
                    updateState(CHANNEL_VOLUMEZONE2, volumeLevelZone2);
                    break;
                case ZONE2_SOURCE:
                    updateState(CHANNEL_INPUTZONE2,
                            convertDeviceValueToOpenHabState(data.getValue(), DecimalType.class));
                    break;

                /*
                 * ZONE 3
                 */
                case ZONE3_POWER:
                    updateState(CHANNEL_POWERZONE3, convertDeviceValueToOpenHabState(data.getValue(), OnOffType.class));
                    break;
                case ZONE3_MUTE:
                    updateState(CHANNEL_MUTEZONE3, convertDeviceValueToOpenHabState(data.getValue(), OnOffType.class));
                    break;
                case ZONE3_VOLUME:
                    volumeLevelZone3 = handleReceivedVolume(
                            convertDeviceValueToOpenHabState(data.getValue(), PercentType.class));
                    updateState(CHANNEL_VOLUMEZONE3, volumeLevelZone3);
                    break;
                case ZONE3_SOURCE:
                    updateState(CHANNEL_INPUTZONE3,
                            convertDeviceValueToOpenHabState(data.getValue(), DecimalType.class));
                    break;

                /*
                 * NET PLAYER
                 */

                case NETUSB_SONG_ARTIST:
                    updateState(CHANNEL_ARTIST, convertDeviceValueToOpenHabState(data.getValue(), StringType.class));
                    break;
                case NETUSB_SONG_ALBUM:
                    updateState(CHANNEL_ALBUM, convertDeviceValueToOpenHabState(data.getValue(), StringType.class));
                    break;
                case NETUSB_SONG_TITLE:
                    updateState(CHANNEL_TITLE, convertDeviceValueToOpenHabState(data.getValue(), StringType.class));
                    break;
                case NETUSB_SONG_ELAPSEDTIME:
                    updateState(CHANNEL_CURRENTPLAYINGTIME,
                            convertDeviceValueToOpenHabState(data.getValue(), StringType.class));
                    break;
                case NETUSB_PLAY_STATUS:
                    updateState(CHANNEL_CONTROL, convertNetUsbPlayStatus(data.getValue()));
                    break;
                case NETUSB_ALBUM_ART:
                    updateAlbumArt(data.getValue());
                    break;
                case NETUSB_TITLE:
                    updateNetTitle(data.getValue());
                    break;
                case NETUSB_MENU:
                    updateNetMenu(data.getValue());
                    break;

                /*
                 * MISC
                 */

                case INFO:
                    logger.debug("Info message: '{}'", data.getValue());
                    break;

                default:
                    logger.debug("Received unhandled status update from Onkyo Receiver @{}: data={}",
                            connection.getConnectionName(), data);

            }

        } catch (Exception ex) {
            logger.error("Exception in statusUpdateReceived for Onkyo Receiver @{}. Cause: {}, data received: {}",
                    connection.getConnectionName(), ex.getMessage(), data);
        }
    }

    @Override
    public void connectionError(String ip) {
        logger.debug("Connection error occurred to Onkyo Receiver @{}", ip);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    private State convertDeviceValueToOpenHabState(String data, Class<?> classToConvert) {
        State state = UnDefType.UNDEF;

        try {
            int index;

            if (data.contentEquals("N/A")) {
                state = UnDefType.UNDEF;

            } else if (classToConvert == OnOffType.class) {
                index = Integer.parseInt(data, 16);
                state = index == 0 ? OnOffType.OFF : OnOffType.ON;

            } else if (classToConvert == DecimalType.class) {
                index = Integer.parseInt(data, 16);
                state = new DecimalType(index);

            } else if (classToConvert == PercentType.class) {
                index = Integer.parseInt(data, 16);
                state = new PercentType(index);

            } else if (classToConvert == StringType.class) {
                state = new StringType(data);

            }
        } catch (Exception e) {
            logger.debug("Cannot convert value '{}' to data type {}", data, classToConvert);
        }

        logger.debug("Converted data '{}' to openHAB state '{}' ({})", data, state, classToConvert);
        return state;
    }

    private void handleNetMenuCommand(String cmdName) {
        if ("Up".equals(cmdName)) {
            sendCommand(EiscpCommand.NETUSB_OP_UP);
        } else if ("Down".equals(cmdName)) {
            sendCommand(EiscpCommand.NETUSB_OP_DOWN);
        } else if ("Select".equals(cmdName)) {
            sendCommand(EiscpCommand.NETUSB_OP_SELECT);
        } else if ("PageUp".equals(cmdName)) {
            sendCommand(EiscpCommand.NETUSB_OP_LEFT);
        } else if ("PageDown".equals(cmdName)) {
            sendCommand(EiscpCommand.NETUSB_OP_RIGHT);
        } else if ("Back".equals(cmdName)) {
            sendCommand(EiscpCommand.NETUSB_OP_RETURN);
        } else if (cmdName.matches("Select[0-9]")) {
            int pos = Integer.parseInt(cmdName.substring(6));
            sendCommand(EiscpCommand.NETUSB_MENU_SELECT, new DecimalType(pos));
        } else {
            logger.debug("Received unknown menucommand {}", cmdName);
        }
    }

    private void selectInput(int inputId) {
        sendCommand(EiscpCommand.SOURCE_SET, new DecimalType(inputId));
        currentInput = inputId;
    }

    @SuppressWarnings("unused")
    private void onInputChanged(int newInput) {
        currentInput = newInput;

        if (newInput != NET_USB_ID) {
            resetNetMenu();

            updateState(CHANNEL_ARTIST, UnDefType.UNDEF);
            updateState(CHANNEL_ALBUM, UnDefType.UNDEF);
            updateState(CHANNEL_TITLE, UnDefType.UNDEF);
            updateState(CHANNEL_CURRENTPLAYINGTIME, UnDefType.UNDEF);
        }
    }

    private void updateAlbumArt(String data) {
        onkyoAlbumArt.addFrame(data);

        if (onkyoAlbumArt.isAlbumCoverReady()) {
            try {
                byte[] imgData = onkyoAlbumArt.getAlbumArt();
                if (imgData != null && imgData.length > 0) {
                    updateState(CHANNEL_ALBUM_ART, new RawType(imgData));
                } else {
                    updateState(CHANNEL_ALBUM_ART, UnDefType.UNDEF);
                }
            } catch (IllegalArgumentException e) {
                updateState(CHANNEL_ALBUM_ART, UnDefType.UNDEF);
            }
            onkyoAlbumArt.clearAlbumArt();
        }

        if (data.startsWith("2-")) {
            updateState(CHANNEL_ALBUM_ART_URL, new StringType(data.substring(2, data.length())));
        } else if (data.startsWith("n-")) {
            updateState(CHANNEL_ALBUM_ART_URL, UnDefType.UNDEF);
        } else {
            logger.debug("Not supported album art URL type: {}", data.substring(0, 2));
            updateState(CHANNEL_ALBUM_ART_URL, UnDefType.UNDEF);
        }

    }

    private void updateNetTitle(String data) {
        // first 2 characters is service type
        int type = Integer.parseInt(data.substring(0, 2), 16);
        ServiceType service = ServiceType.getType(type);

        String title = "";
        if (data.length() > 21) {
            title = data.substring(22, data.length());
        }

        updateState(CHANNEL_NET_MENU_TITLE,
                new StringType(service.toString() + ((title.length() > 0) ? ": " + title : "")));

    }

    private void updateNetMenu(String data) {
        switch (data.charAt(0)) {
            case 'U':
                String itemData = data.substring(3, data.length());
                switch (data.charAt(1)) {
                    case '0':
                        updateState(CHANNEL_NET_MENU0, new StringType(itemData));
                        break;
                    case '1':
                        updateState(CHANNEL_NET_MENU1, new StringType(itemData));
                        break;
                    case '2':
                        updateState(CHANNEL_NET_MENU2, new StringType(itemData));
                        break;
                    case '3':
                        updateState(CHANNEL_NET_MENU3, new StringType(itemData));
                        break;
                    case '4':
                        updateState(CHANNEL_NET_MENU4, new StringType(itemData));
                        break;
                    case '5':
                        updateState(CHANNEL_NET_MENU5, new StringType(itemData));
                        break;
                    case '6':
                        updateState(CHANNEL_NET_MENU6, new StringType(itemData));
                        break;
                    case '7':
                        updateState(CHANNEL_NET_MENU7, new StringType(itemData));
                        break;
                    case '8':
                        updateState(CHANNEL_NET_MENU8, new StringType(itemData));
                        break;
                    case '9':
                        updateState(CHANNEL_NET_MENU9, new StringType(itemData));
                        break;
                }
                break;

            case 'C':
                updateMenuPosition(data);
                break;
        }
    }

    private void updateMenuPosition(String data) {
        char position = data.charAt(1);
        int pos = Character.getNumericValue(position);

        logger.debug("Updating menu position to {}", pos);

        if (pos == -1) {
            updateState(CHANNEL_NET_MENU_SELECTION, UnDefType.UNDEF);
        } else {
            updateState(CHANNEL_NET_MENU_SELECTION, new DecimalType(pos));
        }

        if (data.endsWith("P")) {
            resetNetMenu();
        }
    }

    private void resetNetMenu() {
        logger.debug("Reset net menu");
        updateState(CHANNEL_NET_MENU0, new StringType("-"));
        updateState(CHANNEL_NET_MENU1, new StringType("-"));
        updateState(CHANNEL_NET_MENU2, new StringType("-"));
        updateState(CHANNEL_NET_MENU3, new StringType("-"));
        updateState(CHANNEL_NET_MENU4, new StringType("-"));
        updateState(CHANNEL_NET_MENU5, new StringType("-"));
        updateState(CHANNEL_NET_MENU6, new StringType("-"));
        updateState(CHANNEL_NET_MENU7, new StringType("-"));
        updateState(CHANNEL_NET_MENU8, new StringType("-"));
        updateState(CHANNEL_NET_MENU9, new StringType("-"));
    }

    private State convertNetUsbPlayStatus(String data) {
        State state = UnDefType.UNDEF;
        switch (data.charAt(0)) {
            case 'P':
                state = PlayPauseType.PLAY;
                break;
            case 'p':
            case 'S':
                state = PlayPauseType.PAUSE;
                break;
            case 'F':
                state = RewindFastforwardType.FASTFORWARD;
                break;
            case 'R':
                state = RewindFastforwardType.REWIND;
                break;

        }
        return state;
    }

    private void sendCommand(EiscpCommand deviceCommand) {
        if (connection != null) {
            connection.send(deviceCommand.getCommand(), deviceCommand.getValue());
        } else {
            logger.debug("Connect send command to onkyo receiver since the onkyo binding is not initialized");
        }
    }

    private void sendCommand(EiscpCommand deviceCommand, Command command) {
        if (connection != null) {

            final String cmd = deviceCommand.getCommand();
            String valTemplate = deviceCommand.getValue();
            String val;

            if (command instanceof OnOffType) {
                val = String.format(valTemplate, command == OnOffType.ON ? 1 : 0);

            } else if (command instanceof StringType) {
                val = String.format(valTemplate, command);

            } else if (command instanceof DecimalType) {
                val = String.format(valTemplate, ((DecimalType) command).intValue());

            } else if (command instanceof PercentType) {
                val = String.format(valTemplate, ((DecimalType) command).intValue());
            } else {
                val = valTemplate;
            }

            logger.debug("Sending command '{}' with value '{}' to Onkyo Receiver @{}", cmd, val,
                    connection.getConnectionName());
            connection.send(cmd, val);
        } else {
            logger.debug("Connect send command to onkyo receiver since the onkyo binding is not initialized");
        }
    }

    /**
     * Check the status of the AVR.
     *
     * @return
     */
    private void checkStatus() {
        sendCommand(EiscpCommand.POWER_QUERY);

        if (connection != null && connection.isConnected()) {

            sendCommand(EiscpCommand.VOLUME_QUERY);
            sendCommand(EiscpCommand.SOURCE_QUERY);
            sendCommand(EiscpCommand.MUTE_QUERY);
            sendCommand(EiscpCommand.NETUSB_TITLE_QUERY);
            sendCommand(EiscpCommand.LISTEN_MODE_QUERY);

            if (isChannelAvailable(CHANNEL_POWERZONE2)) {
                sendCommand(EiscpCommand.ZONE2_POWER_QUERY);
                sendCommand(EiscpCommand.ZONE2_VOLUME_QUERY);
                sendCommand(EiscpCommand.ZONE2_SOURCE_QUERY);
                sendCommand(EiscpCommand.ZONE2_MUTE_QUERY);
            }

            if (isChannelAvailable(CHANNEL_POWERZONE3)) {
                sendCommand(EiscpCommand.ZONE3_POWER_QUERY);
                sendCommand(EiscpCommand.ZONE3_VOLUME_QUERY);
                sendCommand(EiscpCommand.ZONE3_SOURCE_QUERY);
                sendCommand(EiscpCommand.ZONE3_MUTE_QUERY);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private boolean isChannelAvailable(String channel) {
        List<Channel> channels = getThing().getChannels();
        if (channels != null) {
            for (Channel c : channels) {
                if (c.getUID().getId().equals(channel)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleVolumeSet(EiscpCommand.Zone zone, final State currentValue, final Command command) {
        if (command instanceof PercentType) {
            sendCommand(EiscpCommand.getCommandForZone(zone, EiscpCommand.VOLUME_SET),
                    downScaleVolume((PercentType) command));
        } else if (command.equals(IncreaseDecreaseType.INCREASE)) {
            if (currentValue instanceof PercentType) {
                if (((DecimalType) currentValue).intValue() < configuration.volumeLimit) {
                    sendCommand(EiscpCommand.getCommandForZone(zone, EiscpCommand.VOLUME_UP));
                } else {
                    logger.info("Volume level is limited to {}, ignore volume up command.", configuration.volumeLimit);
                }
            }
        } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
            sendCommand(EiscpCommand.getCommandForZone(zone, EiscpCommand.VOLUME_DOWN));
        } else if (command.equals(OnOffType.OFF)) {
            sendCommand(EiscpCommand.getCommandForZone(zone, EiscpCommand.MUTE_SET), command);
        } else if (command.equals(OnOffType.ON)) {
            sendCommand(EiscpCommand.getCommandForZone(zone, EiscpCommand.MUTE_SET), command);
        } else if (command.equals(RefreshType.REFRESH)) {
            sendCommand(EiscpCommand.getCommandForZone(zone, EiscpCommand.VOLUME_QUERY));
            sendCommand(EiscpCommand.getCommandForZone(zone, EiscpCommand.MUTE_QUERY));
        }
    }

    private State handleReceivedVolume(State volume) {
        if (volume instanceof PercentType) {
            return upScaleVolume(((PercentType) volume));
        }
        return volume;
    }

    private PercentType upScaleVolume(PercentType volume) {
        PercentType newVolume = volume;

        if (configuration.volumeLimit < 100) {
            double scaleCoefficient = 100d / configuration.volumeLimit;
            newVolume = new PercentType(((Double) (volume.doubleValue() * scaleCoefficient)).intValue());
            logger.debug("Up scaled volume level '{}' to '{}'", volume, newVolume);
        }

        return newVolume;
    }

    private PercentType downScaleVolume(PercentType volume) {
        PercentType newVolume = volume;

        if (configuration.volumeLimit < 100) {
            double scaleCoefficient = configuration.volumeLimit / 100d;
            newVolume = new PercentType(((Double) (volume.doubleValue() * scaleCoefficient)).intValue());
            logger.debug("Down scaled volume level '{}' to '{}'", volume, newVolume);
        }

        return newVolume;
    }

    @Override
    public PercentType getVolume() throws IOException {
        if (volumeLevelZone1 instanceof PercentType) {
            return (PercentType) volumeLevelZone1;
        }

        throw new IOException();
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        handleVolumeSet(EiscpCommand.Zone.ZONE1, volumeLevelZone1, downScaleVolume(volume));
    }
}
