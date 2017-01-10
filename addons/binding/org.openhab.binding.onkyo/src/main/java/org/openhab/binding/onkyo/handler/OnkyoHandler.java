/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.handler;

import static org.openhab.binding.onkyo.OnkyoBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.EventObject;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.onkyo.internal.OnkyoConnection;
import org.openhab.binding.onkyo.internal.OnkyoEventListener;
import org.openhab.binding.onkyo.internal.ServiceType;
import org.openhab.binding.onkyo.internal.eiscp.EiscpCommand;
import org.openhab.binding.onkyo.internal.eiscp.EiscpCommandRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OnkyoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Frank - Initial contribution
 * @author Marcel Verpaalen - parsing additional commands
 */
public class OnkyoHandler extends UpnpAudioSinkHandler implements OnkyoEventListener {

    private Logger logger = LoggerFactory.getLogger(OnkyoHandler.class);

    private OnkyoConnection connection;
    private ScheduledFuture<?> statusCheckerFuture;
    private int currentInput = -1;
    private PercentType volume;

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
        if (this.getConfig().get(HOST_PARAMETER) != null) {
            String host = (String) this.getConfig().get(HOST_PARAMETER);
            Integer port = 60128;
            Object portObj = this.getConfig().get(TCP_PORT_PARAMETER);
            if (portObj != null) {
                if (portObj instanceof Number) {
                    port = ((Number) portObj).intValue();
                } else if (portObj instanceof String) {
                    port = Integer.parseInt(portObj.toString());
                }
            }

            long refreshInterval;
            try {
                refreshInterval = ((BigDecimal) this.getConfig().get(REFRESH_INTERVAL)).longValue();
            } catch (Exception e) {
                refreshInterval = 60;
                logger.warn("No refresh Interval defined using {}s", refreshInterval);
            }
            logger.warn("No refresh Interval defined using {}s", refreshInterval);

            connection = new OnkyoConnection(host, port);
            connection.addEventListener(this);

            logger.debug("Connected to Onkyo Receiver @{}", connection.getConnectionName());

            // Start the status checker
            Runnable statusChecker = new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.debug("Checking status of  Onkyo Receiver @{}", connection.getConnectionName());
                        checkStatus();
                    } catch (LinkageError e) {
                        logger.warn("Failed to check the status for  Onkyo Receiver @{}. Cause: {}",
                                connection.getConnectionName(), e.getMessage());
                    } catch (Exception ex) {
                        logger.warn("Exception in update Status Thread Onkyo Receiver @{}. Cause: {}",
                                connection.getConnectionName(), ex.getMessage());

                    }
                }
            };
            if (refreshInterval > 0) {
                statusCheckerFuture = scheduler.scheduleWithFixedDelay(statusChecker, 1, refreshInterval,
                        TimeUnit.SECONDS);
            } else {
                statusCheckerFuture = scheduler.schedule(statusChecker, 1, TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to receiver. IP address not set.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (statusCheckerFuture != null) {
            statusCheckerFuture.cancel(true);
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
            case CHANNEL_POWER:
                if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.POWER_ON);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.POWER_OFF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.POWER_QUERY);
                }
                break;
            case CHANNEL_MUTE:
                if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.MUTE);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.UNMUTE);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.MUTE_QUERY);
                }
                break;
            case CHANNEL_VOLUME:
                handleVolume(command);
                break;
            case CHANNEL_INPUT:
                if (command instanceof DecimalType) {
                    selectInput(((DecimalType) command).intValue());
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.SOURCE_QUERY);
                }
                break;
            case CHANNEL_CONTROL:
                if (command instanceof PlayPauseType) {
                    if (command.equals(PlayPauseType.PLAY)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_PLAY);
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_PAUSE);
                    }
                } else if (command instanceof NextPreviousType) {
                    if (command.equals(NextPreviousType.NEXT)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_TRACKUP);
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_TRACKDWN);
                    }
                } else if (command instanceof RewindFastforwardType) {
                    if (command.equals(RewindFastforwardType.REWIND)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_REW);
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_FF);
                    }
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_PLAY_STATUS_QUERY);
                }
                break;
            case CHANNEL_PLAY_URI:
                handlePlayUri(command);
                break;
            case CHANNEL_ALBUM_ART:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_ALBUM_ART_REQ);
                }
                break;
            case CHANNEL_LISTENMODE:
                if (command instanceof DecimalType) {
                    sendCommand(EiscpCommandRef.LISTEN_MODE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.LISTEN_MODE_QUERY);
                }
                break;
            case CHANNEL_ARTIST:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_SONG_ARTIST_QUERY);
                }
                break;
            case CHANNEL_ALBUM:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_SONG_ALBUM_QUERY);
                }
                break;
            case CHANNEL_TITLE:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_SONG_TITLE_QUERY);
                }
                break;
            case CHANNEL_NET_MENU_TITLE:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_TITLE_QUERY);
                }
                break;
            case CHANNEL_CURRENTPLAYINGTIME:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_SONG_ELAPSEDTIME_QUERY);
                }
                break;
            case CHANNEL_POWERZONE2:
                if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.ZONE2_POWER_ON);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.ZONE2_POWER_SBY);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.ZONE2_POWER_QUERY);
                }
                break;
            case CHANNEL_MUTEZONE2:
                if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.ZONE2_MUTE);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.ZONE2_UNMUTE);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.ZONE2_MUTE_QUERY);
                }
                break;
            case CHANNEL_VOLUMEZONE2:
                if (command instanceof PercentType) {
                    sendCommand(EiscpCommandRef.ZONE2_VOLUME_SET, command);
                } else if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    sendCommand(EiscpCommandRef.ZONE2_VOLUME_UP);
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    sendCommand(EiscpCommandRef.ZONE2_VOLUME_DOWN);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.ZONE2_MUTE);
                } else if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.ZONE2_UNMUTE);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.ZONE2_VOLUME_QUERY);
                }
                break;
            case CHANNEL_INPUTZONE2:
                if (command instanceof DecimalType) {
                    sendCommand(EiscpCommandRef.ZONE2_SOURCE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.ZONE2_SOURCE_QUERY);
                }
                break;
            case CHANNEL_NET_MENU_CONTROL:
                if (command instanceof StringType) {
                    final String cmdName = command.toString();
                    handleNetMenuCommand(cmdName);
                }
                break;
            default:
                logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                break;
        }
    }

    @Override
    public void statusUpdateReceived(EventObject event, String ip, String data) {
        logger.debug("Received status update from Onkyo Receiver @{}: data={}", connection.getConnectionName(), data);

        updateStatus(ThingStatus.ONLINE);

        try {
            EiscpCommand receivedCommand = null;
            for (EiscpCommand candidate : EiscpCommand.values()) {
                String deviceCmd = candidate.getCommand();
                if (data.startsWith(deviceCmd)) {
                    receivedCommand = candidate;
                    break;
                }
            }

            if (receivedCommand != null) {
                switch (receivedCommand.getCommandRef()) {
                    case POWER_OFF:
                        updateState(CHANNEL_POWER, OnOffType.OFF);
                        break;
                    case POWER_ON:
                        updateState(CHANNEL_POWER, OnOffType.ON);
                        break;
                    case MUTE:
                        updateState(CHANNEL_MUTE, OnOffType.ON);
                        break;
                    case UNMUTE:
                        updateState(CHANNEL_MUTE, OnOffType.OFF);
                        break;
                    case VOLUME_SET:
                        if (!data.substring(3, 5).contentEquals("N/")) {
                            volume = new PercentType(Integer.parseInt(data.substring(3, 5), 16));
                            updateState(CHANNEL_VOLUME, volume);
                        }
                        break;
                    case SOURCE_SET:
                        if (!data.substring(3, 5).contentEquals("N/")) {
                            int input = Integer.parseInt(data.substring(3, 5), 16);
                            updateState(CHANNEL_INPUT, new DecimalType(input));
                            onInputChanged(input);
                        }
                        break;
                    case NETUSB_SONG_ARTIST_QUERY:
                        updateState(CHANNEL_ARTIST, new StringType(data.substring(3, data.length())));
                        break;
                    case NETUSB_SONG_ALBUM_QUERY:
                        updateState(CHANNEL_ALBUM, new StringType(data.substring(3, data.length())));
                        break;
                    case NETUSB_SONG_TITLE_QUERY:
                        updateState(CHANNEL_TITLE, new StringType(data.substring(3, data.length())));
                        break;
                    case NETUSB_SONG_ELAPSEDTIME_QUERY:
                        updateState(CHANNEL_CURRENTPLAYINGTIME, new StringType(data.substring(3, data.length())));
                        break;
                    case NETUSB_PLAY_STATUS_QUERY:
                        updateNetUsbPlayStatus(data.charAt(3));
                        break;
                    case NETUSB_TITLE:
                        ServiceType service = ServiceType.getType(Integer.parseInt(data.substring(3, 5), 16));
                        String title = data.substring(25, data.length());
                        updateState(CHANNEL_NET_MENU_TITLE,
                                new StringType(service.toString() + ((title.length() > 0) ? ": " + title : "")));
                        break;
                    case NETUSB_ALBUM_ART:
                        if (data.substring(3, 5).contentEquals("2-")) {
                            //TODO: Replace this with ImageType once available
                            updateState(CHANNEL_ALBUM_ART, new StringType(data.substring(5, data.length())));
                        } else {
                            logger.debug("Not supported album art type: {}", data.substring(3, data.length()));
                        }
                        break;
                    case RECEIVER_INFO:
                        logger.debug("Info message: \n{}", data.substring(3, data.length()));
                        break;
                    case LISTEN_MODE_SET:
                        String listenModeStr = data.substring(3, 5);
                        // update only when listen mode is available
                        if (!listenModeStr.equals("N/")) {
                            int listenMode = Integer.parseInt(listenModeStr, 16);
                            updateState(CHANNEL_LISTENMODE, new DecimalType(listenMode));
                        }
                        break;
                    case ZONE2_POWER_SBY:
                        updateState(CHANNEL_POWERZONE2, OnOffType.OFF);
                        break;
                    case ZONE2_POWER_ON:
                        updateState(CHANNEL_POWERZONE2, OnOffType.ON);
                        break;
                    case ZONE2_MUTE:
                        updateState(CHANNEL_MUTEZONE2, OnOffType.ON);
                        break;
                    case ZONE2_UNMUTE:
                        updateState(CHANNEL_MUTEZONE2, OnOffType.OFF);
                        break;
                    case ZONE2_VOLUME_SET:
                        if (!data.substring(3, 5).contentEquals("N/")) {
                            updateState(CHANNEL_VOLUMEZONE2,
                                    new PercentType(Integer.parseInt(data.substring(3, 5), 16)));
                        }
                        break;
                    case ZONE2_SOURCE_SET:
                        if (!data.substring(3, 5).contentEquals("N/")) {
                            int inputZone2 = Integer.parseInt(data.substring(3, 5), 16);
                            updateState(CHANNEL_INPUTZONE2, new DecimalType(inputZone2));
                        }
                        break;
                    case NETUSB_MENU0:
                        updateState(CHANNEL_NET_MENU0, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU1:
                        updateState(CHANNEL_NET_MENU1, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU2:
                        updateState(CHANNEL_NET_MENU2, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU3:
                        updateState(CHANNEL_NET_MENU3, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU4:
                        updateState(CHANNEL_NET_MENU4, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU5:
                        updateState(CHANNEL_NET_MENU5, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU6:
                        updateState(CHANNEL_NET_MENU6, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU7:
                        updateState(CHANNEL_NET_MENU7, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU8:
                        updateState(CHANNEL_NET_MENU8, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU9:
                        updateState(CHANNEL_NET_MENU9, new StringType(data.substring(6)));
                        break;
                    case NETUSB_MENU_POSITION:
                        String txt = data.substring(4);
                        int pos = -1;
                        try {
                            pos = Integer.parseInt(txt.substring(0, 1));
                        } catch (NumberFormatException nfe) {
                            // pos already is -1
                        }

                        logger.debug("Updating menu {} : {}", txt.charAt(1), pos);

                        if (txt.endsWith("P")) {
                            resetNetMenu();
                        }

                        updateState(CHANNEL_NET_MENU_SELECTION, new DecimalType(pos));
                        break;
                    default:
                        logger.debug("Received unhandled status update from Onkyo Receiver @{}: data={}",
                                connection.getConnectionName(), data);

                }
            } else {
                logger.debug("Received unknown status update from Onkyo Receiver @{}: data={}",
                        connection.getConnectionName(), data);
            }
        } catch (Exception ex) {
            logger.error("Exception in statusUpdateReceived for Onkyo Receiver @{}. Cause: {}, data received: {}",
                    connection.getConnectionName(), ex.getMessage(), data);
        }
    }

    private void resetNetMenu() {
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

    private void handleNetMenuCommand(String cmdName) {
        if ("Up".equals(cmdName)) {
            sendCommand(EiscpCommandRef.NETUSB_OP_UP);
        } else if ("Down".equals(cmdName)) {
            sendCommand(EiscpCommandRef.NETUSB_OP_DOWN);
        } else if ("Select".equals(cmdName)) {
            sendCommand(EiscpCommandRef.NETUSB_OP_SELECT);
        } else if ("PageUp".equals(cmdName)) {
            sendCommand(EiscpCommandRef.NETUSB_OP_LEFT);
        } else if ("PageDown".equals(cmdName)) {
            sendCommand(EiscpCommandRef.NETUSB_OP_RIGHT);
        } else if ("Back".equals(cmdName)) {
            sendCommand(EiscpCommandRef.NETUSB_OP_RETURN);
        } else if (cmdName.matches("Select[0-9]")) {
            int pos = Integer.parseInt(cmdName.substring(6));
            sendCommand(EiscpCommandRef.NETUSB_MENU_SELECT, new DecimalType(pos));
        } else {
            logger.debug("Received unknown menucommand {}", cmdName);
        }
    }

    private void selectInput(int inputId) {
        sendCommand(EiscpCommandRef.SOURCE_SET, new DecimalType(inputId));
        currentInput = inputId;
    }

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

    private void updateNetUsbPlayStatus(char c) {
        switch (c) {
            case 'P':
                updateState(CHANNEL_CONTROL, PlayPauseType.PLAY);
                break;
            case 'p':
            case 'S':
                updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                break;
            case 'F':
                updateState(CHANNEL_CONTROL, RewindFastforwardType.FASTFORWARD);
                break;
            case 'R':
                updateState(CHANNEL_CONTROL, RewindFastforwardType.REWIND);
                break;

        }
    }

    private void sendCommand(EiscpCommandRef commandRef) {
        if (connection != null) {
            EiscpCommand deviceCommand = EiscpCommand.getCommandByCommandRef(commandRef.getCommand());
            connection.send(deviceCommand.getCommand());
        } else {
            logger.debug("Connect send command to onkyo receiver since the onkyo binding is not initialized");
        }
    }

    private void sendCommand(EiscpCommandRef commandRef, Command command) {
        if (connection != null) {
            EiscpCommand deviceCommand = EiscpCommand.getCommandByCommandRef(commandRef.getCommand());

            String cmdTemplate = deviceCommand.getCommand();
            String deviceCmd = null;

            if (command instanceof OnOffType) {
                deviceCmd = String.format(cmdTemplate, command == OnOffType.ON ? 1 : 0);

            } else if (command instanceof StringType) {
                deviceCmd = String.format(cmdTemplate, command);

            } else if (command instanceof DecimalType) {
                deviceCmd = String.format(cmdTemplate, ((DecimalType) command).intValue());

            } else if (command instanceof PercentType) {
                deviceCmd = String.format(cmdTemplate, ((DecimalType) command).intValue());
            }

            logger.debug("Sending command to onkyo receiver: {}", deviceCmd);
            connection.send(deviceCmd);
        } else {
            logger.debug("Connect send command to onkyo receiver since the onkyo binding is not initialized");
        }
    }

    /**
     * Check the status of the AVR. Return true if the AVR is online, else return false.
     *
     * @return
     */
    private void checkStatus() {

        sendCommand(EiscpCommandRef.POWER_QUERY);
        sendCommand(EiscpCommandRef.VOLUME_QUERY);
        sendCommand(EiscpCommandRef.SOURCE_QUERY);
        sendCommand(EiscpCommandRef.MUTE_QUERY);
        sendCommand(EiscpCommandRef.ZONE2_POWER_QUERY);
        sendCommand(EiscpCommandRef.ZONE2_VOLUME_QUERY);
        sendCommand(EiscpCommandRef.ZONE2_SOURCE_QUERY);
        sendCommand(EiscpCommandRef.ZONE2_MUTE_QUERY);
        sendCommand(EiscpCommandRef.NETUSB_TITLE_QUERY);
        sendCommand(EiscpCommandRef.LISTEN_MODE_QUERY);

        if (connection != null && connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void handleVolume(final Command command) {
        if (command instanceof PercentType) {
            sendCommand(EiscpCommandRef.VOLUME_SET, command);
        } else if (command.equals(IncreaseDecreaseType.INCREASE)) {
            sendCommand(EiscpCommandRef.VOLUME_UP);
        } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
            sendCommand(EiscpCommandRef.VOLUME_DOWN);
        } else if (command.equals(OnOffType.OFF)) {
            sendCommand(EiscpCommandRef.MUTE);
        } else if (command.equals(OnOffType.ON)) {
            sendCommand(EiscpCommandRef.UNMUTE);
        } else if (command.equals(RefreshType.REFRESH)) {
            sendCommand(EiscpCommandRef.VOLUME_QUERY);
        }
    }

    @Override
    public PercentType getVolume() throws IOException {
        return volume;
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        handleVolume(volume);
    }

}
