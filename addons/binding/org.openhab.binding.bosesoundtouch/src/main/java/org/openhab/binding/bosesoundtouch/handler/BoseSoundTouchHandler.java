/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.handler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchHandlerFactory;
import org.openhab.binding.bosesoundtouch.internal.XMLResponseHandler;
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

    private ChannelUID channelPowerUID;
    private ChannelUID channelVolumeUID;
    private ChannelUID channelMuteUID;
    private ChannelUID channelOperationModeUID;
    private ChannelUID channelZoneInfoUID;
    private ChannelUID channelPlayerControlUID;
    private ChannelUID channelZoneControlUID;
    private ChannelUID channelPresetUID;
    private ChannelUID channelRateEnabled;
    private ChannelUID channelSkipEnabled;
    private ChannelUID channelSkipPreviousEnabled;
    private ChannelUID channelKeyCodeUID;
    private ChannelUID channelNowPlayingAlbumUID;
    private ChannelUID channelNowPlayingArtistUID;
    private ChannelUID channelNowPlayingArtworkUID;
    private ChannelUID channelNowPlayingDescriptionUID;
    private ChannelUID channelNowPlayingGenreUID;
    private ChannelUID channelNowPlayingItemNameUID;
    private ChannelUID channelNowPlayingPlayStatusUID;
    private ChannelUID channelNowPlayingStationLocationUID;
    private ChannelUID channelNowPlayingStationNameUID;
    private ChannelUID channelNowPlayingTrackUID;

    private ArrayList<Preset> listOfPresets;

    private State nowPlayingSource;
    private ContentItem currentContentItem;
    private boolean muted;
    private OperationModeType currentOperationMode;

    private WebSocket socket;

    private BoseSoundTouchHandlerFactory factory;
    private ZoneState zoneState;
    private BoseSoundTouchHandler zoneMaster;
    private List<ZoneMember> zoneMembers;

    public BoseSoundTouchHandler(Thing thing, BoseSoundTouchHandlerFactory factory) {
        super(thing);
        this.factory = factory;
    }

    @Override
    public void initialize() {
        currentOperationMode = OperationModeType.OFFLINE;
        listOfPresets = new ArrayList<Preset>();
        zoneMembers = new ArrayList<ZoneMember>();

        nowPlayingSource = null;

        channelPowerUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_POWER);
        channelVolumeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_VOLUME);
        channelMuteUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_MUTE);
        channelOperationModeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_OPERATIONMODE);
        channelZoneInfoUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_ZONEINFO);
        channelPlayerControlUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_PLAYER_CONTROL);
        channelZoneControlUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_ZONE_CONTROL);
        channelPresetUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_PRESET);
        channelRateEnabled = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_RATEENABLED);
        channelSkipEnabled = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_SKIPENABLED);
        channelSkipPreviousEnabled = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_SKIPPREVIOUSENABLED);
        channelKeyCodeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_KEY_CODE);

        channelNowPlayingAlbumUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGALBUM);
        channelNowPlayingArtworkUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGARTWORK);
        channelNowPlayingArtistUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGARTIST);
        channelNowPlayingDescriptionUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGDESCRIPTION);
        channelNowPlayingItemNameUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGGENRE);
        channelNowPlayingGenreUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGITEMNAME);
        channelNowPlayingPlayStatusUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGPLAYSTATUS);
        channelNowPlayingStationLocationUID = getChannelUID(
                BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGSTATIONLOCATION);
        channelNowPlayingStationNameUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGSTATIONNAME);
        channelNowPlayingTrackUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYINGTRACK);

        factory.registerSoundTouchDevice(this);
        openConnection();
    }

    @Override
    public void dispose() {
        super.dispose();
        closeConnection();
    }

    @Override
    public void handleRemoval() {
        factory.removeSoundTouchDevice(this);
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug(getDeviceName() + ": handleCommand(" + channelUID + ", " + command + ");");
        if (thing.getStatus() != ThingStatus.ONLINE) {
            openConnection(); // try to reconnect....
        }
        if (command instanceof RefreshType) {
            checkOperationMode();
            // TODO implement RefreshType
        } else {
            if (channelUID.equals(channelPowerUID)) {
                if (command instanceof OnOffType) {
                    OnOffType onOffType = (OnOffType) command;
                    if (currentOperationMode == OperationModeType.STANDBY && onOffType == OnOffType.ON) {
                        simulateRemoteKey(RemoteKey.POWER);
                    }
                    if (currentOperationMode != OperationModeType.STANDBY && onOffType == OnOffType.OFF) {
                        simulateRemoteKey(RemoteKey.POWER);
                    }
                }
            } else if (channelUID.equals(channelOperationModeUID)) {
                if (command instanceof StringType) {
                    // try to parse string command...
                    String cmd = command.toString();
                    if (cmd.equals("STANDBY")) {
                        if (currentOperationMode != OperationModeType.STANDBY) {
                            simulateRemoteKey(RemoteKey.POWER);
                        }
                    } else if (cmd.equals("INTERNET_RADIO")) {
                        if (currentOperationMode == OperationModeType.STANDBY) {
                            simulateRemoteKey(RemoteKey.POWER);
                        }
                        Preset psFound = null;
                        for (Preset ps : listOfPresets) {
                            if ((psFound == null)
                                    && (ps.getContentItem().getOperationMode() == OperationModeType.INTERNET_RADIO)) {
                                psFound = ps;
                            }
                        }
                        if (psFound != null) {
                            simulateRemoteKey(psFound.getKey());
                        } else {
                            logger.warn(
                                    getDeviceName() + ": Unable to switch to mode: INTERNET_RADIO. No PRESET defined");
                        }
                    } else if (cmd.equals("BLUETOOTH")) {
                        if (currentOperationMode == OperationModeType.STANDBY) {
                            simulateRemoteKey(RemoteKey.POWER);
                        }
                        int counter = 0;
                        while ((currentOperationMode != OperationModeType.BLUETOOTH) && counter < 5) {
                            simulateRemoteKey(RemoteKey.AUX_INPUT);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            counter++;
                        }
                        if (counter == 5) {
                            logger.warn(getDeviceName()
                                    + ": Unable to switch to mode: BLUETOOTH. Mayby no device available");
                        }
                    } else if (cmd.equals("AUX")) {
                        if (currentOperationMode == OperationModeType.STANDBY) {
                            simulateRemoteKey(RemoteKey.POWER);
                        }
                        while (currentOperationMode != OperationModeType.AUX) {
                            simulateRemoteKey(RemoteKey.AUX_INPUT);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                        }
                    } else if (cmd.equals("MEDIA")) {
                        logger.warn(getDeviceName() + ": \"" + cmd + "\" " + "OperationMode not supported yet");
                        // TODO
                    } else if (cmd.equals("SPOTIFY")) {
                        logger.warn(getDeviceName() + ": \"" + cmd + "\" " + "OperationMode not supported yet");
                        // TODO
                    } else if (cmd.equals("PANDORA")) {
                        logger.warn(getDeviceName() + ": \"" + cmd + "\" " + "OperationMode not supported yet");
                        // TODO
                    } else if (cmd.equals("DEEZER")) {
                        logger.warn(getDeviceName() + ": \"" + cmd + "\" " + "OperationMode not supported yet");
                        // TODO
                    } else if (cmd.equals("SIRIUSXM")) {
                        logger.warn(getDeviceName() + ": \"" + cmd + "\" " + "OperationMode not supported yet");
                        // TODO
                    } else if (cmd.equals("STORED_MUSIC")) {
                        logger.warn(getDeviceName() + ": \"" + cmd + "\" " + "OperationMode not supported yet");
                        // TODO
                    } else if (cmd.equals("GROUPMEMBER")) {
                        logger.warn(getDeviceName() + ": \"" + cmd + "\" " + "OperationMode not supported yet");
                        // TODO
                    }
                }
            } else if (channelUID.equals(channelVolumeUID)) {
                if (command instanceof PercentType) {
                    PercentType percentType = (PercentType) command;
                    sendRequestInWebSocket("volume", null, "<volume " + "deviceID=\"" + getMacAddress() + "\"" + ">"
                            + percentType.intValue() + "</volume>");
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
                    logger.warn(getDeviceName() + ": Invalid command type: " + command.getClass() + ": " + command);
                }
            } else if (channelUID.equals(channelZoneControlUID)) {
                if (command instanceof StringType) {
                    // try to parse string command...
                    String cmd = command.toString();
                    String cmdlc = cmd.toLowerCase();
                    int sp = cmdlc.indexOf(' ');
                    if (sp > 0) {
                        String action = cmdlc.split(" ")[0];
                        String other = cmdlc.split(" ")[1];
                        BoseSoundTouchHandler oh = null;
                        for (Entry<String, BoseSoundTouchHandler> e : factory.getAllSoundTouchDevices().entrySet()) {
                            BoseSoundTouchHandler o = e.getValue();
                            // try by mac
                            String mac = e.getKey();
                            if (other.equalsIgnoreCase(mac)) {
                                oh = o;
                                break;
                            }
                            // try by name
                            String devName = o.getDeviceName();
                            if (other.equalsIgnoreCase(devName)) {
                                oh = o;
                                break;
                            }
                        }
                        if (oh == null) {
                            logger.warn(getDeviceName() + ": Invalid / unknown device: \"" + other + "\" in command "
                                    + cmd);
                        } else {
                            if ("add".equals(action)) {
                                boolean found = false;
                                for (ZoneMember m : zoneMembers) {
                                    if (oh.getMacAddress().equals(m.getMac())) {
                                        logger.warn(getDeviceName() + ": Zone add: ID " + oh.getMacAddress()
                                                + " is already member in zone!");
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    ZoneMember nm = new ZoneMember();
                                    nm.setHandler(oh);
                                    nm.setMac(oh.getMacAddress());
                                    Map<String, Object> props = oh.thing.getConfiguration().getProperties();
                                    String host = (String) props
                                            .get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST);
                                    nm.setIp(host);
                                    // zoneMembers.add(nm);
                                    addZoneMember(nm);
                                    updateZones();
                                }
                            } else if ("remove".equals(action)) {
                                if (!removeZoneMember(oh)) {
                                    logger.warn(getDeviceName() + ": Zone remove: ID " + oh.getMacAddress()
                                            + " is not a member in zone!");
                                } else {
                                    updateZones();
                                }
                            } else {
                                logger.warn(getDeviceName() + ": Invalid zone command: " + cmd);
                            }
                        }
                    } else {
                        logger.warn(getDeviceName() + ": Invalid zone command: " + cmd);
                    }
                } else {
                    logger.warn(getDeviceName() + ":Invalid command type: " + command.getClass() + ": " + command);
                }
            } else if (channelUID.equals(channelPresetUID)) {
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
                        logger.warn(getDeviceName() + ": Invalid preset: " + cmd);
                    }
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
                        logger.warn(getDeviceName() + ": Invalid remote key: " + cmd);
                    }
                }
            } else {
                logger.warn(getDeviceName() + ": Got command \"" + command + "\" for channel \"" + channelUID.getId()
                        + "\" which is unhandled!");
            }
        }
    }

    public void sendRequestInWebSocket(String url) {
        int id = 0;
        String msg = "<msg><header " + "deviceID=\"" + getMacAddress() + "\"" + " url=\"" + url
                + "\" method=\"GET\"><request requestID=\"" + id + "\"><info type=\"new\"/></request></header></msg>";
        try {
            socket.sendMessage(RequestBody.create(WebSocket.TEXT, msg));
        } catch (IOException e) {
            onFailure(e, null);
        }
    }

    private void sendRequestInWebSocket(String url, String infoAddon, String postData) {
        int id = 0;
        String msg = "<msg><header " + "deviceID=\"" + getMacAddress() + "\"" + " url=\"" + url
                + "\" method=\"POST\"><request requestID=\"" + id + "\"><info " + (infoAddon == null ? "" : infoAddon)
                + " type=\"new\"/></request></header><body>" + postData + "</body></msg>";
        try {
            socket.sendMessage(RequestBody.create(WebSocket.TEXT, msg));
        } catch (IOException e) {
            onFailure(e, null);
        }
    }

    private void simulateRemoteKey(RemoteKey key) {
        sendRequestInWebSocket("key", "mainNode=\"keyPress\"",
                "<key state=\"press\" sender=\"Gabbo\">" + key.name() + "</key>");
        sendRequestInWebSocket("key", "mainNode=\"keyRelease\"",
                "<key state=\"release\" sender=\"Gabbo\">" + key.name() + "</key>");
    }

    public void checkOperationMode() {
        OperationModeType om = OperationModeType.OTHER;
        if (thing.getStatus() == ThingStatus.ONLINE) {
            if (currentContentItem != null) {
                Preset psFound = null;
                for (Preset ps : listOfPresets) {
                    if (ps.getContentItem().equals(currentContentItem)) {
                        psFound = ps;
                    }
                }
                if (psFound != null) {
                    updateState(channelPresetUID, new StringType(psFound.toString()));
                } else {
                    logger.warn(getDeviceName() + ": Invalid preset active");
                    updateState(channelPresetUID, new StringType(""));
                }

                if (om == OperationModeType.OTHER) {
                    om = currentContentItem.getOperationMode();
                }
            } else {
                om = OperationModeType.STANDBY;
            }
        }

        updateState(channelOperationModeUID, new StringType(om.name()));
        currentOperationMode = om;
        if (om == OperationModeType.STANDBY) {
            // zone is leaved / destroyed if turned off
            zoneMembers.clear();
            if (zoneMaster != null) {
                zoneMaster.removeZoneMember(this);
                zoneMaster = null;
            }
            updateState(channelPowerUID, OnOffType.OFF);
        } else {
            updateState(channelPowerUID, OnOffType.ON);
        }
    }

    public BoseSoundTouchHandlerFactory getFactory() {
        return factory;
    }

    public void clearListOfPresets() {
        listOfPresets.clear();
    }

    public void addPresetToList(Preset preset) {
        listOfPresets.add(preset);
    }

    public State getNowPlayingSource() {
        return nowPlayingSource;
    }

    public void setCurrentContentItem(ContentItem currentContentItem) {
        this.currentContentItem = currentContentItem;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void updateZoneState(ZoneState zoneState, BoseSoundTouchHandler zoneMaster, List<ZoneMember> zoneMembers) {
        this.zoneState = zoneState;
        this.zoneMaster = zoneMaster;
        this.zoneMembers = zoneMembers;
        zonesChanged();
    }

    private void addZoneMember(ZoneMember zoneMember) {
        boolean found = false;
        for (ZoneMember m : zoneMembers) {
            if (zoneMember.getHandler().getMacAddress().equals(m.getMac())) {
                logger.warn(getDeviceName() + ": Zone add: ID " + zoneMember.getHandler().getMacAddress()
                        + " is already member in zone!");
                found = true;
                break;
            }
        }
        if (!found) {
            zoneMembers.add(zoneMember);
        }
    }

    private boolean removeZoneMember(BoseSoundTouchHandler oh) {
        boolean found = false;
        for (Iterator<ZoneMember> mi = zoneMembers.iterator(); mi.hasNext();) {
            ZoneMember m = mi.next();
            if (oh == m.getHandler()) {
                mi.remove();
                found = true;
                break;
            }
        }
        return found;
    }

    public BoseSoundTouchHandler getZoneMaster() {
        return zoneMaster;
    }

    public void updateNowPlayingSource(State state) {
        nowPlayingSource = state;
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

    public void updateNowPlayingGenre(State state) {
        updateState(channelNowPlayingGenreUID, state);
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

    public void updateRateEnabled(State state) {
        updateState(channelRateEnabled, state);
    }

    public void updateSkipEnabled(State state) {
        updateState(channelSkipEnabled, state);
    }

    public void updateSkipPreviousEnabled(State state) {
        updateState(channelSkipPreviousEnabled, state);
    }

    public void updateVolume(State state) {
        updateState(channelVolumeUID, state);
    }

    public void updateVolumeMuted(State state) {
        updateState(channelMuteUID, state);
    }

    @Override
    public void onOpen(WebSocket socket, Response resp) {
        logger.debug(getDeviceName() + ": onOpen(\"" + resp + "\")");
        this.socket = socket;
        updateStatus(ThingStatus.ONLINE);
        // socket.newMessageSink(PayloadType.TEXT);
        sendRequestInWebSocket("info");
    }

    @Override
    public void onFailure(IOException e, Response response) {
        logger.error(getDeviceName() + ": Error during websocket communication: ", e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        this.currentOperationMode = OperationModeType.OFFLINE;
        this.currentContentItem = null;
        this.checkOperationMode();
        try {
            socket.close(1011, getDeviceName() + ": Failure: " + e.getMessage());
        } catch (IOException e1) {
            logger.error(getDeviceName() + ": Error while closing websocket communication (during error handling): ",
                    e);
        }
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        String msg = message.string();
        logger.debug(getDeviceName() + ": onMessage(\"" + msg + "\")");
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(new XMLResponseHandler(this));
            reader.parse(new InputSource(new StringReader(msg)));
        } catch (IOException e) {
            // This should never happen - we're not performing I/O!
            logger.error(getDeviceName() + ": Could not parse XML from string '{}'; exception is: ", msg, e);
        } catch (Throwable s) {
            logger.error(getDeviceName() + ": Could not parse XML from string '{}'; exception is: ", msg, s);
        }
    }

    @Override
    public void onPong(Buffer payload) {
        logger.debug(getDeviceName() + ": onPong(\"" + payload + "\")");
    }

    @Override
    public void onClose(int code, String reason) {
        logger.debug(getDeviceName() + ": onClose(" + code + ", \"" + reason + "\")");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        this.currentOperationMode = OperationModeType.OFFLINE;
        this.currentContentItem = null;
        this.checkOperationMode();
    }

    public String getMacAddress() {
        return thing.getUID().getId();
    }

    public Logger getLogger() {
        return logger;
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

    public String getDeviceName() {
        return thing.getProperties().get(BoseSoundTouchBindingConstants.DEVICE_INFO_NAME);
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
                sb.append("Standalone");
                break;
        }
        for (int i = 0; i < zoneMembers.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(zoneMembers.get(i).getHandler().getDeviceName());
        }
        logger.debug(getDeviceName() + ": zoneInfo updated: " + sb.toString());
        updateState(channelZoneInfoUID, new StringType(sb.toString()));
    }

    private void updateZones() {
        StringBuilder sb = new StringBuilder();
        sb.append("<zone master=\"").append(getMacAddress()).append("\">");
        for (ZoneMember mbr : zoneMembers) {
            sb.append("<member ipaddress=\"").append(mbr.getIp()).append("\">").append(mbr.getMac())
                    .append("</member>");
        }
        sb.append("</zone>");
        sendRequestInWebSocket("setZone", "mainNode=\"newZone\"", sb.toString());
    }

    private void openConnection() {
        closeConnection();
        zoneState = ZoneState.None;
        zoneMaster = null;
        // updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.NONE);
        OkHttpClient client = new OkHttpClient();
        // we need longer timeouts for websocket.
        client.setReadTimeout(300, TimeUnit.SECONDS);
        Map<String, Object> props = thing.getConfiguration().getProperties();
        String host = (String) props.get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST);

        // Port seems to be hardcoded, therefore no userinput or discovery is necessary
        String wsUrl = "http://" + host + ":8080/";
        logger.debug(getDeviceName() + ": Connecting to: " + wsUrl);
        Request request = new Request.Builder().url(wsUrl).addHeader("Sec-WebSocket-Protocol", "gabbo").build();
        WebSocketCall call = WebSocketCall.create(client, request);
        call.enqueue(this);
    }

    private void closeConnection() {
        if (socket != null) {
            try {
                socket.close(1000, "Binding shutdown");
            } catch (IOException | IllegalStateException e) {
                logger.error(getDeviceName() + ": Error while closing websocket communication: "
                        + e.getClass().getName() + "(" + e.getMessage() + ")");
            }
            socket = null;
        }
    }
}
