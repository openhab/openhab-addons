/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.handler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItem;
import org.openhab.binding.bosesoundtouch.internal.items.Preset;
import org.openhab.binding.bosesoundtouch.internal.items.RemoteKey;
import org.openhab.binding.bosesoundtouch.internal.items.ZoneMember;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.openhab.binding.bosesoundtouch.types.RadioStationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import okio.Buffer;

/**
 * The {@link BoseSoundTouchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Niessner - Initial contribution
 */
public class BoseSoundTouchHandler extends BaseThingHandler implements WebSocketListener {

    static private Logger logger = LoggerFactory.getLogger(BoseSoundTouchHandler.class);

    // map for of all registered devices for zone membership lookup...
    static private Map<String, BoseSoundTouchHandler> allSoundTouchDevices = new HashMap<>();

    private String attrDeviceId; // deviceID attribute for XML building...

    private ChannelUID channelOperationModeUID;
    private ChannelUID channelVolumeUID;
    private ChannelUID channelMuteUID;
    private ChannelUID channelRadioStationUID;
    private ChannelUID channelPlayerControlUID;
    private ChannelUID channelZoneUID;

    private ChannelUID channelKeyCodeUID;

    private ChannelUID channelNowPlayingAlbumUID;
    private ChannelUID channelNowPlayingArtworkUID;
    private ChannelUID channelNowPlayingArtistUID;
    private ChannelUID channelNowPlayingDescriptionUID;
    private ChannelUID channelNowPlayingItemNameUID;
    private ChannelUID channelNowPlayingStationLocationUID;
    private ChannelUID channelNowPlayingStationNameUID;
    private ChannelUID channelNowPlayingPlayStatusUID;
    private ChannelUID channelNowPlayingTrackUID;

    private ContentItem currentContentItem;

    private OperationModeType operationMode;
    private RadioStationType radioStation;

    private String currentSource;
    private String macAddress;
    private boolean muted;
    private HashMap<Integer, Preset> presets;
    private WebSocket socket;
    private int socketRequestId;

    private RadioStationType defaultRadioStation;

    private static enum ZoneState {
        None,
        Master,
        Member
    };

    private ZoneState zoneState;
    private BoseSoundTouchHandler zoneMaster;
    private List<ZoneMember> zoneMembers;

    public BoseSoundTouchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        macAddress = thing.getUID().getId();
        attrDeviceId = "deviceID=\"" + macAddress + "\"";
        operationMode = OperationModeType.OFFLINE;
        presets = new HashMap<>();
        currentSource = null;
        channelOperationModeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_OPERATION_MODE);
        channelVolumeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_VOLUME);
        channelMuteUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_MUTE);
        channelRadioStationUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_RADIO_STATION);
        channelPlayerControlUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_PLAYER_CONTROL);

        channelNowPlayingAlbumUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_ALBUM);
        channelNowPlayingArtworkUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_ARTWORK);
        channelNowPlayingArtistUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_ARTIST);
        channelNowPlayingDescriptionUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_DESCRIPTION);
        channelNowPlayingItemNameUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_ITEMNAME);
        channelNowPlayingStationLocationUID = getChannelUID(
                BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_STATION_LOCATION);
        channelNowPlayingStationNameUID = getChannelUID(
                BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_STATION_NAME);
        channelNowPlayingPlayStatusUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_PLAY_STATUS);
        channelNowPlayingTrackUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING_TRACK);
        channelZoneUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_ZONE);

        if (getConfig().get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_DEFAULT_RADIO_STATION) != null) {
            String defaultRadioStationString = (String) getConfig()
                    .get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_DEFAULT_RADIO_STATION);
            if (defaultRadioStationString.equals("PRESET_1")) {
                defaultRadioStation = RadioStationType.PRESET_1;
            } else if (defaultRadioStationString.equals("PRESET_2")) {
                defaultRadioStation = RadioStationType.PRESET_2;
            } else if (defaultRadioStationString.equals("PRESET_3")) {
                defaultRadioStation = RadioStationType.PRESET_3;
            } else if (defaultRadioStationString.equals("PRESET_4")) {
                defaultRadioStation = RadioStationType.PRESET_4;
            } else if (defaultRadioStationString.equals("PRESET_5")) {
                defaultRadioStation = RadioStationType.PRESET_5;
            } else if (defaultRadioStationString.equals("PRESET_6")) {
                defaultRadioStation = RadioStationType.PRESET_6;
            } else {
                defaultRadioStation = RadioStationType.PRESET_1;
                logger.warn("Error reading default radio station. PRESET_1 is selected");
            }
        }

        allSoundTouchDevices.put(macAddress, this);

        openConnection();

    }

    private ChannelUID getChannelUID(String channelId) {
        Channel chann = thing.getChannel(channelId);
        if (chann == null) {
            // refresh thing...
            Thing newThing = ThingFactory.createThing(TypeResolver.resolve(thing.getThingTypeUID()), thing.getUID(),
                    thing.getConfiguration());
            updateThing(newThing);
            chann = thing.getChannel(channelId);
        }
        return chann.getUID();
    }

    @Override
    public void handleRemoval() {
        allSoundTouchDevices.remove(macAddress);
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(" + channelUID + ", " + command + ");");
        if (thing.getStatus() != ThingStatus.ONLINE) {
            openConnection(); // try to reconnect....
        }
        if (channelUID.equals(channelVolumeUID)) {
            if (command instanceof PercentType) {
                sendRequestInWebSocket("volume", null,
                        "<volume " + attrDeviceId + ">" + ((PercentType) command).intValue() + "</volume>");
            }
        } else if (channelUID.equals(channelOperationModeUID)) {
            // try to parse string command...
            String cmd = command.toString();
            if (cmd.equals("STANDBY")) {
                if (operationMode != OperationModeType.STANDBY) {
                    simulateRemoteKey(RemoteKey.POWER);
                }
            } else if (cmd.equals("INTERNET_RADIO")) {
                if (defaultRadioStation == RadioStationType.PRESET_1) {
                    simulateRemoteKey(RemoteKey.PRESET_1);
                } else if (defaultRadioStation == RadioStationType.PRESET_2) {
                    simulateRemoteKey(RemoteKey.PRESET_2);
                } else if (defaultRadioStation == RadioStationType.PRESET_3) {
                    simulateRemoteKey(RemoteKey.PRESET_3);
                } else if (defaultRadioStation == RadioStationType.PRESET_4) {
                    simulateRemoteKey(RemoteKey.PRESET_4);
                } else if (defaultRadioStation == RadioStationType.PRESET_5) {
                    simulateRemoteKey(RemoteKey.PRESET_5);
                } else if (defaultRadioStation == RadioStationType.PRESET_6) {
                    simulateRemoteKey(RemoteKey.PRESET_6);
                }
            } else if (cmd.equals("BLUETOOTH")) {
                while (operationMode != OperationModeType.BLUETOOTH) {
                    simulateRemoteKey(RemoteKey.AUX_INPUT);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            } else if (cmd.equals("AUX")) {
                while (operationMode != OperationModeType.AUX) {
                    simulateRemoteKey(RemoteKey.AUX_INPUT);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            } else {
                logger.warn("Invalid operationMode: " + cmd);
            }
        } else if (channelUID.equals(channelMuteUID)) {
            OnOffType onOffType = (OnOffType) command;
            if (muted && onOffType == OnOffType.OFF) {
                simulateRemoteKey(RemoteKey.MUTE);
            }
            if (!muted && onOffType == OnOffType.ON) {
                simulateRemoteKey(RemoteKey.MUTE);
            }
        } else if (channelUID.equals(channelRadioStationUID)) {
            if (operationMode != OperationModeType.STANDBY) {
                if (command instanceof StringType) {
                    String cmd = command.toString();
                    if (cmd.equals("PRESET_1")) {
                        simulateRemoteKey(RemoteKey.PRESET_1);
                    } else if (cmd.equals("PRESET_2")) {
                        simulateRemoteKey(RemoteKey.PRESET_2);
                    } else if (cmd.equals("PRESET_3")) {
                        simulateRemoteKey(RemoteKey.PRESET_3);
                    } else if (cmd.equals("PRESET_4")) {
                        simulateRemoteKey(RemoteKey.PRESET_4);
                    } else if (cmd.equals("PRESET_5")) {
                        simulateRemoteKey(RemoteKey.PRESET_5);
                    } else if (cmd.equals("PRESET_6")) {
                        simulateRemoteKey(RemoteKey.PRESET_6);
                    } else {
                        logger.warn("Invalid source: " + cmd);
                    }
                }
            }
        } else if (channelUID.equals(channelPlayerControlUID)) {
            if (command instanceof PlayPauseType) {
                PlayPauseType type = (PlayPauseType) command;
                if (type == PlayPauseType.PLAY) {
                    simulateRemoteKey(RemoteKey.PLAY);
                }
                if (type == PlayPauseType.PAUSE) {
                    simulateRemoteKey(RemoteKey.PAUSE);
                }
            } else if (command instanceof NextPreviousType) {
                NextPreviousType type = (NextPreviousType) command;
                if (type == NextPreviousType.NEXT) {
                    simulateRemoteKey(RemoteKey.NEXT_TRACK);
                }
                if (type == NextPreviousType.PREVIOUS) {
                    simulateRemoteKey(RemoteKey.PREV_TRACK);
                }
            } else if (command instanceof StringType) {
                String cmd = command.toString();
                if (cmd.equals("PLAY")) {
                    simulateRemoteKey(RemoteKey.PLAY);
                }
                if (cmd.equals("PAUSE")) {
                    simulateRemoteKey(RemoteKey.PAUSE);
                }
                if (cmd.equals("NEXT")) {
                    simulateRemoteKey(RemoteKey.NEXT_TRACK);
                }
                if (cmd.equals("PREVIOUS")) {
                    simulateRemoteKey(RemoteKey.PREV_TRACK);
                }
            } else {
                logger.warn("Invalid command type: " + command.getClass() + ": " + command);
            }
        } else if (channelUID.equals(channelZoneUID)) {
            String cmd = command.toString();
            String cmdlc = command.toString().toLowerCase();
            if (cmdlc.startsWith("zone ")) {
                int sp = cmdlc.indexOf(' ', 5);
                if (sp > 0) {
                    String action = cmdlc.substring(5, sp);
                    String other = cmd.substring(sp + 1);
                    BoseSoundTouchHandler oh = null;
                    for (Entry<String, BoseSoundTouchHandler> e : allSoundTouchDevices.entrySet()) {
                        BoseSoundTouchHandler o = e.getValue();
                        // try by mac id
                        if (other.equalsIgnoreCase(e.getKey())) {
                            oh = o;
                            break;
                        }
                        // try by name
                        if (other.equalsIgnoreCase(o.getDeviceName())) {
                            oh = o;
                            break;
                        }
                    }
                    if (oh == null) {
                        logger.warn("Invalid / unknown device: \"" + other + "\" in command " + cmd);
                    } else {
                        if ("add".equals(action)) {
                            boolean found = false;
                            for (ZoneMember m : zoneMembers) {
                                if (oh.macAddress.equals(m.getMac())) {
                                    logger.warn("Zone add: ID " + oh.macAddress + " is already member in zone!");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                ZoneMember nm = new ZoneMember();
                                nm.setHandler(oh);
                                nm.setMac(oh.macAddress);
                                Map<String, Object> props = oh.thing.getConfiguration().getProperties();
                                String host = (String) props.get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST);
                                nm.setIp(host);
                                zoneMembers.add(nm);
                                updateZones();
                            }
                        } else if ("remove".equals(action)) {
                            boolean found = false;
                            for (Iterator<ZoneMember> mi = zoneMembers.iterator(); mi.hasNext();) {
                                ZoneMember m = mi.next();
                                if (oh.macAddress.equals(m.getMac())) {
                                    mi.remove();
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                logger.warn("Zone remove: ID " + oh.macAddress + " is not a member in zone!");
                            } else {
                                updateZones();
                            }
                        } else {
                            logger.warn("Invalid zone command: " + cmd);
                        }
                    }
                } else {
                    logger.warn("Invalid zone command: " + cmd);
                }
            } else {
                logger.warn("Invalid command: " + cmd);
            }
        } else if (channelUID.equals(channelKeyCodeUID)) {
            if (command instanceof StringType) {
                String cmd = command.toString();
                if (cmd.equals("PLAY")) {
                    simulateRemoteKey(RemoteKey.PLAY);
                } else if (cmd.equals("PAUSE")) {
                    simulateRemoteKey(RemoteKey.PAUSE);
                } else if (cmd.equals("STOP")) {
                    simulateRemoteKey(RemoteKey.STOP);
                } else if (cmd.equals("PREV_TRACK")) {
                    simulateRemoteKey(RemoteKey.PREV_TRACK);
                } else if (cmd.equals("NEXT_TRACK")) {
                    simulateRemoteKey(RemoteKey.NEXT_TRACK);
                } else if (cmd.equals("THUMBS_UP")) {
                    simulateRemoteKey(RemoteKey.THUMBS_UP);
                } else if (cmd.equals("THUMBS_DOWN")) {
                    simulateRemoteKey(RemoteKey.THUMBS_DOWN);
                } else if (cmd.equals("BOOKMARK")) {
                    simulateRemoteKey(RemoteKey.BOOKMARK);
                } else if (cmd.equals("POWER")) {
                    simulateRemoteKey(RemoteKey.POWER);
                } else if (cmd.equals("MUTE")) {
                    simulateRemoteKey(RemoteKey.MUTE);
                } else if (cmd.equals("VOLUME_UP")) {
                    simulateRemoteKey(RemoteKey.VOLUME_UP);
                } else if (cmd.equals("VOLUME_DOWN")) {
                    simulateRemoteKey(RemoteKey.VOLUME_DOWN);
                } else if (cmd.equals("PRESET_1")) {
                    simulateRemoteKey(RemoteKey.PRESET_1);
                } else if (cmd.equals("PRESET_2")) {
                    simulateRemoteKey(RemoteKey.PRESET_2);
                } else if (cmd.equals("PRESET_3")) {
                    simulateRemoteKey(RemoteKey.PRESET_3);
                } else if (cmd.equals("PRESET_4")) {
                    simulateRemoteKey(RemoteKey.PRESET_4);
                } else if (cmd.equals("PRESET_5")) {
                    simulateRemoteKey(RemoteKey.PRESET_5);
                } else if (cmd.equals("PRESET_6")) {
                    simulateRemoteKey(RemoteKey.PRESET_6);
                } else if (cmd.equals("AUX_INPUT")) {
                    simulateRemoteKey(RemoteKey.AUX_INPUT);
                } else if (cmd.equals("SHUFFLE_OFF")) {
                    simulateRemoteKey(RemoteKey.SHUFFLE_OFF);
                } else if (cmd.equals("SHUFFLE_ON")) {
                    simulateRemoteKey(RemoteKey.SHUFFLE_ON);
                } else if (cmd.equals("REPEAT_OFF")) {
                    simulateRemoteKey(RemoteKey.REPEAT_OFF);
                } else if (cmd.equals("REPEAT_ONE")) {
                    simulateRemoteKey(RemoteKey.REPEAT_ONE);
                } else if (cmd.equals("REPEAT_ALL")) {
                    simulateRemoteKey(RemoteKey.REPEAT_ALL);
                } else if (cmd.equals("PLAY_PAUSE")) {
                    simulateRemoteKey(RemoteKey.PLAY_PAUSE);
                } else if (cmd.equals("ADD_FAVORITE")) {
                    simulateRemoteKey(RemoteKey.ADD_FAVORITE);
                } else if (cmd.equals("REMOVE_FAVORITE")) {
                    simulateRemoteKey(RemoteKey.REMOVE_FAVORITE);
                } else {
                    logger.warn("Invalid preset: " + cmd);
                }
            }
        } else {
            logger.warn(
                    "Got command \"" + command + "\" for channel \"" + channelUID.getId() + "\" which is unhandled!");
        }
    }

    private String getDeviceName() {
        return thing.getProperties().get(BoseSoundTouchBindingConstants.DEVICE_INFO_NAME);
    }

    private void updateZones() {
        StringBuilder sb = new StringBuilder();
        sb.append("<zone master=\"").append(macAddress).append("\">");
        for (ZoneMember mbr : zoneMembers) {
            sb.append("<member ipaddress=\"").append(mbr.getIp()).append("\">").append(mbr.getMac())
                    .append("</member>");
        }
        sb.append("</zone>");
        sendRequestInWebSocket("setZone", "mainNode=\"newZone\"", sb.toString());
    }

    protected void openConnection() {
        zoneState = ZoneState.None;
        zoneMaster = null;
        zoneMembers = Collections.emptyList();
        updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.NONE);
        OkHttpClient client = new OkHttpClient();
        // we need longer timeouts for websocket.
        client.setReadTimeout(300, TimeUnit.SECONDS);
        Map<String, Object> props = thing.getConfiguration().getProperties();
        String host = (String) props.get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST);

        // try {
        // BigDecimal port = (BigDecimal) props.get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_PORT);
        // String urlBase = "http://" + host + ":" + port + "/";
        // Request request = new Request.Builder().url(urlBase + "info").build();
        // Response response = client.newCall(request).execute();
        // if (response.code() != 200) {
        // throw new IOException("Invalid response code: " + response.code());
        // }
        // String resp = response.body().string();
        // } catch (IOException e) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        // }
        String wsUrl = "http://" + host + ":8080/"; // TODO port 8080 is hardcoded ?
        Request request = new Request.Builder().url(wsUrl).addHeader("Sec-WebSocket-Protocol", "gabbo").build();
        WebSocketCall call = WebSocketCall.create(client, request);
        call.enqueue(this);
    }

    // Helper methods.

    private int sendRequestInWebSocket(String url) {
        int myId = socketRequestId++;
        String msg = "<msg><header " + attrDeviceId + " url=\"" + url + "\" method=\"GET\"><request requestID=\"" + myId
                + "\"><info type=\"new\"/></request></header></msg>";
        try {
            socket.sendMessage(RequestBody.create(WebSocket.TEXT, msg));
        } catch (IOException e) {
            onFailure(e, null);
            return -1;
        }
        return myId;
    }

    private int sendRequestInWebSocket(String url, String infoAddon, String postData) {
        int myId = socketRequestId++;
        String msg = "<msg><header " + attrDeviceId + " url=\"" + url + "\" method=\"POST\"><request requestID=\""
                + myId + "\"><info " + (infoAddon == null ? "" : infoAddon) + " type=\"new\"/></request></header><body>"
                + postData + "</body></msg>";
        try {
            socket.sendMessage(RequestBody.create(WebSocket.TEXT, msg));
        } catch (IOException e) {
            onFailure(e, null);
            return -1;
        }
        return myId;
    }

    private void simulateRemoteKey(RemoteKey key, boolean press) {
        sendRequestInWebSocket("key", press ? "mainNode=\"keyPress\"" : "mainNode=\"keyRelease\"",
                "<key state=\"" + (press ? "press" : "release") + "\" sender=\"Gabbo\">" + key.name() + "</key>");

    }

    private void simulateRemoteKey(RemoteKey key) {
        simulateRemoteKey(key, true);
        simulateRemoteKey(key, false);
    }

    public void zonesChanged() {
        StringBuilder sb = new StringBuilder();
        switch (zoneState) {
            case Master:
                sb.append("Master; Members: ");
                break;
            case Member:
                sb.append("Member; Master is: ");
                if (zoneMaster == null) {
                    sb.append("<null>");
                } else {
                    sb.append(zoneMaster.getDeviceName());
                }
                sb.append("; Members: ");
                break;
            case None:
                sb.append("");
                break;
        }
        for (int i = 0; i < zoneMembers.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(zoneMembers.get(i).getHandler().getDeviceName());
        }
        updateState(channelZoneUID, new StringType(sb.toString()));
    }

    // WebSocketListener interface
    @Override
    public void onClose(int code, String reason) {
        logger.debug("onClose(" + code + ", \"" + reason + "\")");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        operationMode = OperationModeType.OFFLINE;
        currentContentItem = null;
        checkOperationMode();
    }

    @Override
    public void onFailure(IOException e, Response response) {
        logger.error(thing + ": Error during websocket communication: ", e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        operationMode = OperationModeType.OFFLINE;
        currentContentItem = null;
        checkOperationMode();
        try {
            socket.close(1011, "Failure: " + e.getMessage());
        } catch (IOException e1) {
            logger.error(thing + ": Error while closing websocket communication (during error handling): ", e);
        }
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        String msg = message.string();
        logger.debug("onMessage(\"" + msg + "\")");
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(new ResponseHandler(this));
            reader.parse(new InputSource(new StringReader(msg)));
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            logger.error("Could not parse XML from string '{}'; exception is: ", msg, e);
        } catch (Throwable s) {
            logger.error("Could not parse XML from string '{}'; exception is: ", msg, s);
        }
    }

    @Override
    public void onOpen(WebSocket socket, Response resp) {
        logger.debug("onOpen(\"" + resp + "\")");
        this.socket = socket;
        socketRequestId = 0;
        updateStatus(ThingStatus.ONLINE);
        // socket.newMessageSink(PayloadType.TEXT);
        sendRequestInWebSocket("info");
    }

    @Override
    public void onPong(Buffer payload) {
        logger.debug("onPong(\"" + payload + "\")");
    }

    // XML Handlers
    /**
     * @author marvin
     *
     */
    static private class ResponseHandler extends DefaultHandler {
        enum State {
            INIT,
            Msg,
            MsgHeader,
            MsgBody,
            ContentItem,
            ContentItemItemName,
            Info,
            InfoName,
            InfoType,
            Presets,
            Preset,
            NowPlaying,
            NowPlayingAlbum,
            NowPlayingArt,
            NowPlayingArtist,
            NowPlayingDescription,
            NowPlayingPlayStatus,
            NowPlayingStationLocation,
            NowPlayingStationName,
            NowPlayingTrack,
            Unprocessed, // unprocessed / ignored data
            UnprocessedNoTextExpected, // unprocessed / ignored data
            Updates,
            Volume,
            VolumeActual,
            VolumeMuteEnabled,
            Zone,
            ZoneMember,
            ZoneUpdated
        }

        private static Map<ResponseHandler.State, Map<String, ResponseHandler.State>> stateSwitchingMap;

        static {
            stateSwitchingMap = new HashMap<>();
            Map<String, ResponseHandler.State> msgBodyMap = new HashMap<>();
            stateSwitchingMap.put(State.MsgBody, msgBodyMap);
            msgBodyMap.put("info", State.Info);
            msgBodyMap.put("volume", State.Volume);
            msgBodyMap.put("presets", State.Presets);
            msgBodyMap.put("key", State.Unprocessed); // only confirmation of our key presses...
            msgBodyMap.put("zone", State.Zone); // only confirmation of our key presses...

            // info message states
            Map<String, ResponseHandler.State> infoMap = new HashMap<>();
            stateSwitchingMap.put(State.Info, infoMap);
            infoMap.put("components", State.Unprocessed); // TODO read software version and serial number
            infoMap.put("name", State.InfoName);
            infoMap.put("type", State.InfoType);
            infoMap.put("networkInfo", State.Unprocessed);
            infoMap.put("margeAccountUUID", State.Unprocessed);
            infoMap.put("margeURL", State.Unprocessed);
            infoMap.put("moduleType", State.Unprocessed);
            infoMap.put("variant", State.Unprocessed);
            infoMap.put("variantMode", State.Unprocessed);
            infoMap.put("countryCode", State.Unprocessed);
            infoMap.put("regionCode", State.Unprocessed);

            Map<String, State> updatesMap = new HashMap<>();
            stateSwitchingMap.put(State.Updates, updatesMap);
            updatesMap.put("clockDisplayUpdated", State.Unprocessed); // can we get anything useful of that?
            updatesMap.put("connectionStateUpdated", State.UnprocessedNoTextExpected);
            updatesMap.put("infoUpdated", State.Unprocessed);
            updatesMap.put("nowPlayingUpdated", State.MsgBody);
            updatesMap.put("recentsUpdated", State.Unprocessed);
            updatesMap.put("volumeUpdated", State.MsgBody);
            updatesMap.put("zoneUpdated", State.ZoneUpdated); // just notifies but dosn't provide details

            Map<String, State> volume = new HashMap<>();
            stateSwitchingMap.put(State.Volume, volume);
            volume.put("targetvolume", State.Unprocessed);
            volume.put("actualvolume", State.VolumeActual);
            volume.put("muteenabled", State.VolumeMuteEnabled);

            Map<String, State> nowPlayingMap = new HashMap<>();
            stateSwitchingMap.put(State.NowPlaying, nowPlayingMap);
            nowPlayingMap.put("album", State.NowPlayingAlbum);
            nowPlayingMap.put("art", State.NowPlayingArt);
            nowPlayingMap.put("artist", State.NowPlayingArtist);
            nowPlayingMap.put("ContentItem", State.ContentItem);
            nowPlayingMap.put("description", State.NowPlayingDescription);
            nowPlayingMap.put("playStatus", State.NowPlayingPlayStatus);
            nowPlayingMap.put("stationLocation", State.NowPlayingStationLocation);
            nowPlayingMap.put("stationName", State.NowPlayingStationName);
            nowPlayingMap.put("track", State.NowPlayingTrack);
            nowPlayingMap.put("connectionStatusInfo", State.Unprocessed); // TODO active when Source==Bluetooth
            // TODO active when Source==Pandora and maybe also other sources - seems to be rating related
            nowPlayingMap.put("time", State.Unprocessed);
            nowPlayingMap.put("rating", State.Unprocessed);
            nowPlayingMap.put("skipEnabled", State.Unprocessed);
            nowPlayingMap.put("rateEnabled", State.Unprocessed);

            Map<String, State> contentItemMap = new HashMap<>();
            stateSwitchingMap.put(State.ContentItem, contentItemMap);
            contentItemMap.put("itemName", State.ContentItemItemName);

            Map<String, State> presetMap = new HashMap<>();
            stateSwitchingMap.put(State.Preset, presetMap);
            presetMap.put("ContentItem", State.ContentItem);

            Map<String, State> zoneMap = new HashMap<>();
            stateSwitchingMap.put(State.Zone, zoneMap);
            zoneMap.put("member", State.ZoneMember);
        }

        private ContentItem contentItem;
        private Stack<State> states;
        private State state;
        private BoseSoundTouchHandler handler;
        private boolean msgHeaderWasValid;
        private Preset preset;
        private boolean volumeMuteEnabled;
        private ZoneMember zoneMember;

        ResponseHandler(BoseSoundTouchHandler handler) {
            states = new Stack<>();
            state = State.INIT;
            this.handler = handler;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            logger.debug("startElement(\"" + localName + "\"; state: " + state + ")");
            states.push(state);
            State curState = state; // save for switch statement
            Map<String, ResponseHandler.State> stateMap = stateSwitchingMap.get(state);
            state = State.Unprocessed; // set default value; we avoid default in select to have the compiler showing a
                                       // warning for unhandled states
            switch (curState) {
                case INIT:
                    if ("updates".equals(localName)) {
                        // it just seems to be a ping - havn't seen any data on it..
                        if (checkDeviceId(localName, attributes)) {
                            state = State.Updates;
                        } else {
                            state = State.Unprocessed;
                        }
                    } else if ("msg".equals(localName)) {
                        // message
                        state = State.Msg;
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                        }
                        state = State.Unprocessed;
                    }
                    break;
                case Msg:
                    if ("header".equals(localName)) {
                        // message
                        if (checkDeviceId(localName, attributes)) {
                            state = State.MsgHeader;
                            msgHeaderWasValid = true;
                        } else {
                            state = State.Unprocessed;
                        }
                    } else if ("body".equals(localName)) {
                        if (msgHeaderWasValid) {
                            state = State.MsgBody;
                        } else {
                            state = State.Unprocessed;
                        }
                    } else {
                        logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                        state = State.Unprocessed;
                    }
                    break;
                case MsgHeader:
                    if ("request".equals(localName)) {
                        state = State.Unprocessed; // TODO implement request id / response tracking...
                    } else {
                        logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                        state = State.Unprocessed;
                    }
                    break;
                case MsgBody:
                    if ("nowPlaying".equals(localName)) {
                        if (!checkDeviceId(localName, attributes)) {
                            state = State.Unprocessed;
                            break;
                        }
                        state = State.NowPlaying;
                        String source = attributes.getValue("source");
                        if (handler.currentSource == null || !handler.currentSource.equals(source)) {
                            // source changed
                            handler.currentSource = source;
                            // clear all "nowPlaying" details on source change...
                            StringType ste = new StringType("");
                            handler.updateState(handler.channelNowPlayingAlbumUID, ste);
                            handler.updateState(handler.channelNowPlayingArtworkUID, ste);
                            handler.updateState(handler.channelNowPlayingArtistUID, ste);
                            handler.updateState(handler.channelNowPlayingDescriptionUID, ste);
                            handler.updateState(handler.channelNowPlayingItemNameUID, ste);
                            handler.updateState(handler.channelNowPlayingPlayStatusUID, ste);
                            handler.updateState(handler.channelNowPlayingStationLocationUID, ste);
                            handler.updateState(handler.channelNowPlayingStationNameUID, ste);
                            handler.updateState(handler.channelNowPlayingTrackUID, ste);
                        }
                    } else if ("zone".equals(localName)) {
                        handler.zoneMembers = new ArrayList<>();
                        String master = attributes.getValue("master");
                        if (master == null || master.isEmpty()) {
                            handler.zoneMaster = null;
                            handler.zoneState = ZoneState.None;
                        } else {
                            if (master.equals(handler.macAddress)) {
                                // we are the master...
                                handler.zoneState = ZoneState.Master;
                            } else {
                                // an other device is the master
                                handler.zoneState = ZoneState.Master;
                                handler.zoneMaster = allSoundTouchDevices.get(master);
                                if (handler.zoneMaster == null) {
                                    logger.warn("Zone update: Unable to find master with ID " + master);
                                }
                            }
                        }
                        state = State.Zone;
                    } else {
                        state = stateMap.get(localName);
                        if (state == null) {
                            logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                            state = State.Unprocessed;
                        } else if (state != State.Volume && state != State.Presets) {
                            if (!checkDeviceId(localName, attributes)) {
                                state = State.Unprocessed;
                                break;
                            }
                        }
                    }
                    break;
                case Presets:
                    if ("preset".equals(localName)) {
                        state = State.Preset;
                        String id = attributes.getValue("id");
                        preset = new Preset();
                        preset.setPos(Integer.parseInt(id));
                    } else {
                        logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                        state = State.Unprocessed;
                    }
                    break;
                case Zone:
                    zoneMember = new ZoneMember();
                    zoneMember.setIp(attributes.getValue("ipaddress"));
                    handler.zoneMembers.add(zoneMember);
                    state = stateMap.get(localName);
                    if (state == null) {
                        logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                        state = State.Unprocessed;
                    }
                    break;
                case ContentItem:
                case Info:
                case NowPlaying:
                case Preset:
                case Updates:
                case Volume:
                    state = stateMap.get(localName);
                    if (state == null) {
                        logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                        state = State.Unprocessed;
                    }
                    break;
                // all entities without any children expected..
                case ContentItemItemName:
                case InfoName:
                case InfoType:
                case NowPlayingAlbum:
                case NowPlayingArt:
                case NowPlayingArtist:
                case NowPlayingDescription:
                case NowPlayingPlayStatus:
                case NowPlayingStationLocation:
                case NowPlayingStationName:
                case NowPlayingTrack:
                case VolumeActual:
                case VolumeMuteEnabled:
                case ZoneMember:
                case ZoneUpdated: // currently this dosn't provide any zone details..
                    logger.warn("Unhandled XML entity during " + curState + ": " + localName);
                    state = State.Unprocessed;
                    break;
                case Unprocessed:
                    // all further things are also unprocessed
                    state = State.Unprocessed;
                    break;
                case UnprocessedNoTextExpected:
                    state = State.UnprocessedNoTextExpected;
                    break;
            }
            if (state == State.ContentItem) {
                // we started a content item. process data.
                contentItem = new ContentItem();
                String source = attributes.getValue("source");
                if (source.equals("INTERNET_RADIO")) {
                    contentItem.setOperationMode(OperationModeType.INTERNET_RADIO);
                } else if (source.equals("STANDBY")) {
                    contentItem.setOperationMode(OperationModeType.STANDBY);
                } else if (source.equals("AUX")) {
                    contentItem.setOperationMode(OperationModeType.AUX);
                } else if (source.equals("BLUETOOTH")) {
                    contentItem.setOperationMode(OperationModeType.BLUETOOTH);
                } else {
                    contentItem.setOperationMode(OperationModeType.OTHER);
                    logger.error(handler.thing + ": Unknown SourceType: " + source + " - needs to be defined!");
                }
                contentItem.setLocation(attributes.getValue("location"));
                contentItem.setSourceAccount(attributes.getValue("sourceAccount"));
            }
            if (state == State.Presets) {
                handler.presets.clear();
            }
            if (state == State.Volume) {
                volumeMuteEnabled = false;
            }
        }

        private boolean checkDeviceId(String localName, Attributes attributes) {
            String did = attributes.getValue("deviceID");
            if (did == null) {
                logger.warn("No Device-ID in Entity " + localName);
                return false;
            }
            if (!did.equals(handler.macAddress)) {
                logger.warn("Wrong Device-ID in Entity " + localName + ": Got: " + did + " expected: "
                        + handler.macAddress);
                return false;
            }
            return true;
        }

        void setConfigOption(String option, String value) {
            Map<String, String> prop = handler.thing.getProperties();
            String cur = prop.get(option);
            if (cur == null || !cur.equals(value)) {
                logger.info("Option \"" + option + "\" updated: From \"" + cur + "\" to \"" + value + "\"");
                handler.thing.setProperty(option, value);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            logger.debug("Text data during " + state + ": " + new String(ch, start, length));
            super.characters(ch, start, length);
            switch (state) {
                case INIT:
                case Msg:
                case MsgHeader:
                case MsgBody:
                case Updates:
                case Volume:
                case Info:
                case Preset:
                case Presets:
                case NowPlaying:
                case ContentItem:
                case UnprocessedNoTextExpected:
                case Zone:
                case ZoneUpdated:
                    logger.warn("Unexpected text data during " + state + ": " + new String(ch, start, length));
                    break;
                case Unprocessed:
                    // drop quietly..
                    break;
                case InfoName:
                    setConfigOption(BoseSoundTouchBindingConstants.DEVICE_INFO_NAME, new String(ch, start, length));
                    break;
                case InfoType:
                    setConfigOption(BoseSoundTouchBindingConstants.DEVICE_INFO_TYPE, new String(ch, start, length));
                    break;
                case NowPlayingAlbum:
                    handler.updateState(handler.channelNowPlayingAlbumUID,
                            new StringType(new String(ch, start, length)));
                    break;
                case NowPlayingArt:
                    handler.updateState(handler.channelNowPlayingArtworkUID,
                            new StringType(new String(ch, start, length)));
                    break;
                case NowPlayingArtist:
                    handler.updateState(handler.channelNowPlayingArtistUID,
                            new StringType(new String(ch, start, length)));
                    break;
                case ContentItemItemName:
                    contentItem.setItemName(new String(ch, start, length));
                    break;
                case NowPlayingDescription:
                    handler.updateState(handler.channelNowPlayingDescriptionUID,
                            new StringType(new String(ch, start, length)));
                    break;
                case NowPlayingPlayStatus:
                    handler.updateState(handler.channelNowPlayingPlayStatusUID,
                            new StringType(new String(ch, start, length)));
                    break;
                case NowPlayingStationLocation:
                    handler.updateState(handler.channelNowPlayingStationLocationUID,
                            new StringType(new String(ch, start, length)));
                    break;
                case NowPlayingStationName:
                    handler.updateState(handler.channelNowPlayingStationNameUID,
                            new StringType(new String(ch, start, length)));
                    break;
                case NowPlayingTrack:
                    handler.updateState(handler.channelNowPlayingTrackUID,
                            new StringType(new String(ch, start, length)));
                    break;
                case VolumeActual:
                    handler.updateState(handler.channelVolumeUID,
                            new PercentType(Integer.parseInt(new String(ch, start, length))));
                    break;
                case VolumeMuteEnabled:
                    volumeMuteEnabled = Boolean.parseBoolean(new String(ch, start, length));
                    break;
                case ZoneMember:
                    String mac = new String(ch, start, length);
                    zoneMember.setMac(mac);
                    zoneMember.setHandler(allSoundTouchDevices.get(mac));
                    if (zoneMember.getHandler() == null) {
                        logger.warn("Zone update: Unable to find member with ID " + mac);
                    }
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            logger.debug("endElement(\"" + localName + "\")");
            final State prevState = state;
            state = states.pop();
            if (prevState == State.Info) {
                handler.sendRequestInWebSocket("volume");
                handler.sendRequestInWebSocket("presets");
                handler.sendRequestInWebSocket("now_playing");
                handler.sendRequestInWebSocket("getZone");
            }
            if (prevState == State.ContentItem && state == State.NowPlaying) {
                // update now playing name...
                if (contentItem.getItemName() == null) {
                    contentItem.setItemName(""); // null values cause exceptions in openhab...
                }
                handler.updateState(handler.channelNowPlayingItemNameUID, new StringType(contentItem.getItemName()));
                handler.currentContentItem = contentItem;
                handler.checkOperationMode();
            }
            if (prevState == State.ContentItem && state == State.Preset) {
                preset.setContentItem(contentItem);
            }
            if (prevState == State.Preset && state == State.Presets) {
                handler.presets.put(preset.getPos(), preset);
                handler.checkOperationMode();
            }
            if (prevState == State.Volume) {
                if (handler.muted != volumeMuteEnabled) {
                    handler.muted = volumeMuteEnabled;
                    handler.updateState(handler.channelMuteUID, handler.muted ? OnOffType.ON : OnOffType.OFF);
                }
            }
            if (prevState == State.ZoneUpdated) {
                handler.sendRequestInWebSocket("getZone");
            }
            if (prevState == State.Zone) {
                handler.zonesChanged();
            }
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            super.skippedEntity(name);
        }
    }

    protected void checkOperationMode() {
        OperationModeType om = OperationModeType.OTHER;
        RadioStationType rs = RadioStationType.UNKNOWN;
        if (thing.getStatus() == ThingStatus.ONLINE) {
            if (currentContentItem != null) {
                om = null;
                for (Preset ps : presets.values()) {
                    if (ps.getContentItem().equals(currentContentItem)) {
                        if (ps.posIsValid()) {
                            om = OperationModeType.INTERNET_RADIO;
                            rs = ps.getRadioStation();
                        } else {
                            logger.warn(thing + ": Invalid preset active: " + ps.getPos());
                        }
                    }
                }
                if (om == null) {
                    om = OperationModeType.OTHER;
                    switch (currentContentItem.getOperationMode()) {
                        case STANDBY:
                            om = OperationModeType.STANDBY;
                            break;
                        case INTERNET_RADIO:
                            om = OperationModeType.INTERNET_RADIO;
                            break;
                        case BLUETOOTH:
                            om = OperationModeType.BLUETOOTH;
                            break;
                        case AUX:
                            om = OperationModeType.AUX;
                            break;
                        case OTHER:
                            om = OperationModeType.OTHER;
                            break;
                        default:
                            om = OperationModeType.OTHER;
                            break;
                    }
                }
            } else {
                om = OperationModeType.STANDBY;
            }
        } else {
            om = OperationModeType.OTHER;
        }
        if (operationMode != om) {
            updateState(channelOperationModeUID, new StringType(om.name()));
            operationMode = om;
        }
        if (radioStation != rs) {
            updateState(channelRadioStationUID, new StringType(rs.name()));
            radioStation = rs;
        }
    }
}
