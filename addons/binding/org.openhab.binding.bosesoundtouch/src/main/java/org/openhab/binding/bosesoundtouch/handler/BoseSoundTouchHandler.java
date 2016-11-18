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
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.internal.ZoneState;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItem;
import org.openhab.binding.bosesoundtouch.internal.items.Preset;
import org.openhab.binding.bosesoundtouch.internal.items.RemoteKey;
import org.openhab.binding.bosesoundtouch.internal.items.ZoneMember;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
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
 * @author Thomas Traunbauer
 */
public class BoseSoundTouchHandler extends BaseThingHandler implements WebSocketListener {

    private Logger logger = LoggerFactory.getLogger(BoseSoundTouchHandler.class);

    // map for of all registered devices for zone membership lookup...
    private Map<String, BoseSoundTouchHandler> mapOfAllSoundTouchDevices = new HashMap<>();

    private String attrDeviceId; // deviceID attribute for XML building...

    private ChannelUID channelPowerUID;
    private ChannelUID channelVolumeUID;
    private ChannelUID channelMuteUID;
    private ChannelUID channelOperationModeUID;
    private ChannelUID channelZoneInfoUID;
    private ChannelUID channelPlayerControlUID;
    private ChannelUID channelZoneControlUID;
    private ChannelUID channelKeyCodeUID;
    private ChannelUID channelNowPlayingAlbumUID;
    private ChannelUID channelNowPlayingArtistUID;
    private ChannelUID channelNowPlayingArtworkUID;
    private ChannelUID channelNowPlayingDescriptionUID;
    private ChannelUID channelNowPlayingItemNameUID;
    private ChannelUID channelNowPlayingPlayStatusUID;
    private ChannelUID channelNowPlayingStationLocationUID;
    private ChannelUID channelNowPlayingStationNameUID;
    private ChannelUID channelNowPlayingTrackUID;

    private String currentSourceString;
    private ContentItem currentContentItem;
    private String macAddress;
    private boolean muted;
    private OperationModeType operationMode;
    private HashMap<Integer, Preset> mapOfPresets;
    private WebSocket socket;
    private int socketRequestId;
    private ZoneState zoneState;
    private BoseSoundTouchHandler masterZoneSoundTouchHandler;
    private List<ZoneMember> zoneMembers;

    public BoseSoundTouchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        macAddress = thing.getUID().getId();
        attrDeviceId = "deviceID=\"" + macAddress + "\"";
        operationMode = OperationModeType.OFFLINE;
        mapOfPresets = new HashMap<>();
        currentSourceString = "";

        channelPowerUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_POWER);
        channelVolumeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_VOLUME);
        channelMuteUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_MUTE);
        channelOperationModeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_OPERATIONMODE);
        channelZoneInfoUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_ZONEINFO);
        channelPlayerControlUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_PLAYER_CONTROL);
        channelZoneControlUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_ZONE_CONTROL);
        channelKeyCodeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_KEY_CODE);

        channelNowPlayingAlbumUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGALBUM);
        channelNowPlayingArtworkUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGARTWORK);
        channelNowPlayingArtistUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGARTIST);
        channelNowPlayingDescriptionUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGDESCRIPTION);
        channelNowPlayingItemNameUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGITEMNAME);
        channelNowPlayingPlayStatusUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGPLAYSTATUS);
        channelNowPlayingStationLocationUID = getChannelUID(
                BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGSTATIONLOCATION);
        channelNowPlayingStationNameUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGSTATIONNAME);
        channelNowPlayingTrackUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGTRACK);

        mapOfAllSoundTouchDevices.put(macAddress, this);
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
        mapOfAllSoundTouchDevices.remove(macAddress);
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(" + channelUID + ", " + command + ");");
        if (thing.getStatus() != ThingStatus.ONLINE) {
            openConnection(); // try to reconnect....
        }
        if (channelUID.equals(channelPowerUID)) {
            if (command instanceof OnOffType) {
                OnOffType onOffType = (OnOffType) command;
                if (operationMode == OperationModeType.STANDBY && onOffType == OnOffType.ON) {
                    simulateRemoteKey(RemoteKey.POWER);
                }
                if (operationMode != OperationModeType.STANDBY && onOffType == OnOffType.OFF) {
                    simulateRemoteKey(RemoteKey.POWER);
                }
            }
        } else if (channelUID.equals(channelVolumeUID)) {
            if (command instanceof PercentType) {
                PercentType percentType = (PercentType) command;
                sendRequestInWebSocket("volume", null,
                        "<volume " + attrDeviceId + ">" + percentType.intValue() + "</volume>");
            }
        } else if (channelUID.equals(channelMuteUID)) {
            if (command instanceof OnOffType) {
                OnOffType onOffType = (OnOffType) command;
                if (muted && onOffType == OnOffType.OFF) {
                    simulateRemoteKey(RemoteKey.MUTE);
                }
                if (!muted && onOffType == OnOffType.ON) {
                    simulateRemoteKey(RemoteKey.MUTE);
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
        } else if (channelUID.equals(channelZoneControlUID)) {
            if (command instanceof StringType) {
                // try to parse string command...
                String cmd = command.toString();
                String cmdlc = cmd.toLowerCase();
                if (cmdlc.startsWith("zone ")) {
                    int sp = cmdlc.indexOf(' ', 5);
                    if (sp > 0) {
                        String action = cmdlc.substring(5, sp);
                        String other = cmd.substring(sp + 1);
                        BoseSoundTouchHandler oh = null;
                        for (Entry<String, BoseSoundTouchHandler> e : mapOfAllSoundTouchDevices.entrySet()) {
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
                                    String host = (String) props
                                            .get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST);
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
            } else {
                logger.warn("Invalid command type: " + command.getClass() + ": " + command);
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
        masterZoneSoundTouchHandler = null;
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

    public int sendRequestInWebSocket(String url) {
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
                if (masterZoneSoundTouchHandler == null) {
                    sb.append("<null>");
                } else {
                    sb.append(masterZoneSoundTouchHandler.getDeviceName());
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
        updateState(channelZoneInfoUID, new StringType(sb.toString()));
    }

    // WebSocketListener interface
    @Override
    public void onClose(int code, String reason) {
        logger.debug("onClose(" + code + ", \"" + reason + "\")");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        this.operationMode = OperationModeType.OFFLINE;
        this.currentContentItem = null;
        this.checkOperationMode();
    }

    @Override
    public void onFailure(IOException e, Response response) {
        logger.error(thing + ": Error during websocket communication: ", e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        this.operationMode = OperationModeType.OFFLINE;
        this.currentContentItem = null;
        this.checkOperationMode();
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
        this.socketRequestId = 0;
        updateStatus(ThingStatus.ONLINE);
        // socket.newMessageSink(PayloadType.TEXT);
        sendRequestInWebSocket("info");
    }

    @Override
    public void onPong(Buffer payload) {
        logger.debug("onPong(\"" + payload + "\")");
    }

    protected void checkOperationMode() {
        OperationModeType om = OperationModeType.OTHER;
        if (thing.getStatus() == ThingStatus.ONLINE) {
            if (currentContentItem != null) {
                om = null;
                for (Preset ps : mapOfPresets.values()) {
                    if (ps.getContentItem().equals(currentContentItem)) {
                        if (ps.posIsValid()) {
                            om = OperationModeType.INTERNET_RADIO;
                        } else {
                            logger.warn(thing + ": Invalid preset active: " + ps.getPos());
                        }
                    }
                }
                if (om == null) {
                    om = currentContentItem.getOperationMode();
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
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public BoseSoundTouchHandler getSoundTouchDevices(String mac) {
        return mapOfAllSoundTouchDevices.get(mac);
    }

    public void updateNowPlayingSource(State state) {
        currentSourceString = state.toString();
    }

    public void updateNowPlayingAlbum(State state) {
        updateState(channelNowPlayingAlbumUID, state);
    }

    public void updateNowPlayingArtwork(State state) {
        updateState(channelNowPlayingArtworkUID, state);
    }

    public void updateNowPlayingArtist(State state) {
        updateState(channelNowPlayingArtistUID, state);
    }

    public void updateNowPlayingDescription(State state) {
        updateState(channelNowPlayingDescriptionUID, state);
    }

    public void updateNowPlayingItemName(State state) {
        updateState(channelNowPlayingItemNameUID, state);
    }

    public void updateNowPlayingPlayStatus(State state) {
        updateState(channelNowPlayingPlayStatusUID, state);
    }

    public void updateNowPlayingStationLocation(State state) {
        updateState(channelNowPlayingStationLocationUID, state);
    }

    public void updateNowPlayingStationName(State state) {
        updateState(channelNowPlayingStationNameUID, state);
    }

    public void updateNowPlayingTrack(State state) {
        updateState(channelNowPlayingTrackUID, state);
    }

    public void updateVolume(State state) {
        updateState(channelVolumeUID, state);
    }

    public void updateVolumeMuted(State state) {
        updateState(channelMuteUID, state);
    }

    public void clearMapOfPresets() {
        mapOfPresets.clear();
    }

    public void addPresetToMap(Preset preset) {
        mapOfPresets.put(preset.getPos(), preset);
    }

    public String getCurrentSourceString() {
        return currentSourceString;
    }

    public void setCurrentSourceString(String currentSourceString) {
        this.currentSourceString = currentSourceString;
    }

    public ContentItem getCurrentContentItem() {
        return currentContentItem;
    }

    public void setCurrentContentItem(ContentItem currentContentItem) {
        this.currentContentItem = currentContentItem;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public ZoneState getZoneState() {
        return zoneState;
    }

    public void setZoneState(ZoneState zoneState) {
        this.zoneState = zoneState;
    }

    public BoseSoundTouchHandler getMasterZoneSoundTouchHandler() {
        return masterZoneSoundTouchHandler;
    }

    public void setMasterZoneSoundTouchHandler(BoseSoundTouchHandler masterZoneSoundTouchHandler) {
        this.masterZoneSoundTouchHandler = masterZoneSoundTouchHandler;
    }

    public void addZoneMember(ZoneMember zoneMember) {
        if (zoneMembers == null) {
            zoneMembers = new ArrayList<ZoneMember>();
        }
        zoneMembers.add(zoneMember);
    }

}
