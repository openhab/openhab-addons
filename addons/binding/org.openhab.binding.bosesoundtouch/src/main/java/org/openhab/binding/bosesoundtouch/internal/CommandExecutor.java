/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import static org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchTypeInterface;
import org.openhab.binding.bosesoundtouch.internal.exceptions.BoseSoundTouchNotFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.ContentItemNotPresetableException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoInternetRadioPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoStoredMusicPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.OperationModeNotAvailableException;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItem;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItemMaker;
import org.openhab.binding.bosesoundtouch.internal.items.PresetContainer;
import org.openhab.binding.bosesoundtouch.internal.items.ZoneMember;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.openhab.binding.bosesoundtouch.types.RemoteKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandExecutor} class executes commands on the websocket
 *
 * @author Thomas Traunbauer
 */
public class CommandExecutor implements BoseSoundTouchTypeInterface {
    private Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private Session session;
    private BoseSoundTouchHandler handler;

    private ZoneState zoneState;
    private BoseSoundTouchHandler zoneMaster;
    private List<ZoneMember> zoneMembers;

    private File presetFile;
    private boolean muted;
    private ContentItem currentContentItem;
    private OperationModeType currentOperationMode;
    private PresetContainer presetContainer;

    private boolean bluetooth;
    private boolean aux;
    private boolean aux1;
    private boolean aux2;
    private boolean aux3;
    private boolean internetRadio;
    private boolean storedMusic;
    private boolean hdmi1;
    private boolean tv;
    private boolean bass;

    public CommandExecutor(Session session, BoseSoundTouchHandler handler) {
        this.session = session;
        this.handler = handler;
        init();
    }

    public void addContentItemToPresetList(int presetID, ContentItem preset) {
        try {
            presetContainer.put(presetID, preset);
        } catch (ContentItemNotPresetableException e) {
            logger.debug("{}: ContentItem is not presetable", handler.getDeviceName());
        }
    }

    public void checkOperationMode() {
        OperationModeType operationMode = OperationModeType.OTHER;
        if (currentContentItem != null) {
            ContentItem psFound = null;
            Collection<ContentItem> listOfPresets = presetContainer.values();
            for (ContentItem ps : listOfPresets) {
                if (ps.isPresetable()) {
                    if (ps.getLocation().equals(currentContentItem.getLocation())) {
                        psFound = ps;
                    }
                }
            }
            int presetID = 0;
            if (psFound != null) {
                presetID = psFound.getPresetID();
            }
            updatePreset(new DecimalType(presetID));

            operationMode = currentContentItem.getOperationMode();
        } else {
            operationMode = OperationModeType.STANDBY;
        }

        updateOperationMode(new StringType(operationMode.getName()));
        currentOperationMode = operationMode;
        if (currentOperationMode == OperationModeType.STANDBY) {
            // zone is leaved / destroyed if turned off
            zoneMembers.clear();
            if (zoneMaster != null) {
                zoneMaster.getCommandExecutor().removeZoneMember(handler);
                zoneMaster = null;
            }
            updatePowerState(OnOffType.OFF);
            updatePlayerControl(PlayPauseType.PAUSE);
        } else {
            updatePowerState(OnOffType.ON);
        }
        refreshZone();
    }

    public void getRequest(APIRequest apiRequest) {
        String msg = "<msg><header " + "deviceID=\"" + handler.getMacAddress() + "\"" + " url=\"" + apiRequest.getName()
                + "\" method=\"GET\"><request requestID=\"0\"><info type=\"new\"/></request></header></msg>";
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            handler.onWebSocketError(e);
        }
    }

    public BoseSoundTouchHandler getZoneMaster() {
        return zoneMaster;
    }

    public void setContentItemAsPreset(DecimalType command) {
        if (command.intValue() > 6) {
            try {
                presetContainer.put(command.intValue(), currentContentItem);
                presetContainer.writeToFile(presetFile);
            } catch (ContentItemNotPresetableException e) {
                logger.warn("{}: Selected item is not presetable", handler.getDeviceName());
            } catch (IOException e) {
                logger.warn("{}: Could not save presets to file", handler.getDeviceName());
            }
        } else {
            logger.warn("{}: Only PresetID >6 is allowed", handler.getDeviceName());
        }
    }

    public void setCurrentContentItem(ContentItem currentContentItem) {
        this.currentContentItem = currentContentItem;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void postBass(DecimalType command) {
        if (isBassAvailable()) {
            sendPostRequestInWebSocket("bass",
                    "<bass deviceID=\"" + handler.getMacAddress() + "\"" + ">" + command.intValue() + "</bass>");
        } else {
            logger.warn("{}: Bass modification not supported for this device", handler.getDeviceName());
        }
    }

    public void postOperationMode(OperationModeType operationModeType) {
        if (operationModeType == OperationModeType.STANDBY) {
            if (currentOperationMode != OperationModeType.STANDBY) {
                postRemoteKey(RemoteKeyType.POWER);
            }
        } else {
            try {
                ContentItemMaker contentItemMaker = new ContentItemMaker(this, presetContainer);
                ContentItem contentItem = contentItemMaker.getContentItem(operationModeType);
                postContentItem(contentItem);
            } catch (OperationModeNotAvailableException e) {
                logger.warn("{}: OperationMode \"{}\" is not supported yet", handler.getDeviceName(),
                        operationModeType.name());
            } catch (NoInternetRadioPresetFoundException e) {
                logger.warn("{}: Unable to switch to mode \"INTERNET_RADIO\". No PRESET defined",
                        handler.getDeviceName());
            } catch (NoStoredMusicPresetFoundException e) {
                logger.warn("{}: Unable to switch to mode: \"STORED_MUSIC\". No PRESET defined",
                        handler.getDeviceName());
            } finally {
                checkOperationMode();
            }
        }
    }

    public void postPlayerControl(Command command) {
        if (command.equals(PlayPauseType.PLAY)) {
            postRemoteKey(RemoteKeyType.PLAY);
        } else if (command.equals(PlayPauseType.PAUSE)) {
            postRemoteKey(RemoteKeyType.PAUSE);
        } else if (command.equals(NextPreviousType.NEXT)) {
            postRemoteKey(RemoteKeyType.NEXT_TRACK);
        } else if (command.equals(NextPreviousType.PREVIOUS)) {
            postRemoteKey(RemoteKeyType.PREV_TRACK);
        }
    }

    public void postPower(OnOffType command) {
        if (command.equals(OnOffType.ON)) {
            if (currentOperationMode == OperationModeType.STANDBY) {
                postRemoteKey(RemoteKeyType.POWER);
            }
        } else if (command.equals(OnOffType.OFF)) {
            if (currentOperationMode != OperationModeType.STANDBY) {
                postRemoteKey(RemoteKeyType.POWER);
            }
        }
    }

    public void postPreset(DecimalType command) {
        ContentItem item = null;
        try {
            item = presetContainer.get(command.intValue());
            postContentItem(item);
        } catch (NoPresetFoundException e) {
            logger.warn("{}: No preset found at id: {}", handler.getDeviceName(), command.intValue());
        }
    }

    public void postPreset(NextPreviousType command) {
        ContentItem item = null;
        if (command.equals(NextPreviousType.NEXT)) {
            try {
                item = presetContainer.getNext(currentContentItem);
            } catch (NoPresetFoundException e) {
                logger.warn("{}: No preset found at id: {}", handler.getDeviceName(),
                        currentContentItem.getPresetID() + 1);
            }
        } else if (command.equals(NextPreviousType.PREVIOUS)) {
            try {
                item = presetContainer.getPrev(currentContentItem);
            } catch (NoPresetFoundException e) {
                logger.warn("{}: No preset found at id: {}", handler.getDeviceName(),
                        currentContentItem.getPresetID() - 1);
            }
        }
        postContentItem(item);
    }

    public void postRemoteKey(RemoteKeyType key) {
        sendPostRequestInWebSocket("key", "mainNode=\"keyPress\"",
                "<key state=\"press\" sender=\"Gabbo\">" + key.name() + "</key>");
        sendPostRequestInWebSocket("key", "mainNode=\"keyRelease\"",
                "<key state=\"release\" sender=\"Gabbo\">" + key.name() + "</key>");
    }

    public void postVolume(PercentType command) {
        sendPostRequestInWebSocket("volume",
                "<volume deviceID=\"" + handler.getMacAddress() + "\"" + ">" + command.intValue() + "</volume>");
    }

    public void postVolumeMuted(OnOffType command) {
        updateMuteState(command);
        if (command.equals(OnOffType.ON)) {
            if (!muted) {
                muted = true;
                postRemoteKey(RemoteKeyType.MUTE);
            }
        } else if (command.equals(OnOffType.OFF)) {
            if (muted) {
                muted = false;
                postRemoteKey(RemoteKeyType.MUTE);
            }
        }
    }

    public void postZone(StringType command) {
        // try to parse string command...
        String cmd = command.toString();
        String cmdlc = cmd.toLowerCase();
        if (cmdlc.split(" ").length == 2) {
            String action = cmdlc.split(" ")[0];
            String identifier = cmdlc.split(" ")[1];

            BoseSoundTouchHandler handlerFound = null;
            try {
                handlerFound = findHandlerByNameOrMAC(identifier);
            } catch (BoseSoundTouchNotFoundException e) {
                logger.warn("{}: Could not find Soundtouchd: {}", handler.getDeviceName(), identifier);
            }

            if ("add".equals(action)) {
                addToZone(handlerFound);
            } else if ("remove".equals(action)) {
                removeFromZone(handlerFound);
            } else {
                logger.warn("{}: Invalid zone command: {}", handler.getDeviceName(), cmd);
            }
        } else {
            logger.warn("{}: Invalid zone command: {}", handler.getDeviceName(), cmd);
        }
    }

    public void updateBassLevel(DecimalType state) {
        handler.updateState(getChannelUID(CHANNEL_BASS), state);
    }

    public void updateKeyCode(RemoteKeyType state) {
        handler.updateState(getChannelUID(CHANNEL_KEY_CODE), state);
    }

    public void updateMuteState(OnOffType state) {
        handler.updateState(getChannelUID(CHANNEL_MUTE), state);
    }

    public void updateOperationMode(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_OPERATIONMODE), state);// TODO
    }

    public void updatePlayerControl(State state) {
        handler.updateState(getChannelUID(CHANNEL_PLAYER_CONTROL), state);
    }

    public void updatePowerState(OnOffType state) {
        handler.updateState(getChannelUID(CHANNEL_POWER), state);
    }

    public void updatePreset(DecimalType state) {
        handler.updateState(getChannelUID(CHANNEL_PRESET), state);
    }

    public void updatePresetControl(State state) {
        handler.updateState(getChannelUID(CHANNEL_PRESET_CONTROL), state);
    }

    public void updateSaveAsPreset(DecimalType state) {
        handler.updateState(getChannelUID(CHANNEL_SAVE_AS_PRESET), state);
    }

    public void updateVolume(PercentType state) {
        handler.updateState(getChannelUID(CHANNEL_VOLUME), state);
    }

    public void updateZoneControl(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_ZONE_CONTROL), state);
    }

    public void updateZoneInfo(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_ZONE_INFO), state);
    }

    public void updateZoneState(ZoneState zoneState, BoseSoundTouchHandler zoneMaster, List<ZoneMember> zoneMembers) {
        this.zoneState = zoneState;
        this.zoneMaster = zoneMaster;
        this.zoneMembers = zoneMembers;

        refreshZone();
    }

    public void updateNowPlayingAlbum(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_ALBUM), state);
    }

    public void updateNowPlayingArtwork(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_ARTWORK), state);
    }

    public void updateNowPlayingArtist(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_ARTIST), state);
    }

    public void updateNowPlayingDescription(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_DESCRIPTION), state);
    }

    public void updateNowPlayingGenre(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_GENRE), state);
    }

    public void updateNowPlayingItemName(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_ITEMNAME), state);
    }

    public void updateNowPlayingStationLocation(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_STATIONLOCATION), state);
    }

    public void updateNowPlayingStationName(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_STATIONNAME), state);
    }

    public void updateNowPlayingTrack(StringType state) {
        handler.updateState(getChannelUID(CHANNEL_NOWPLAYING_TRACK), state);
    }

    public void updateRateEnabled(OnOffType state) {
        handler.updateState(getChannelUID(CHANNEL_RATEENABLED), state);
    }

    public void updateSkipEnabled(OnOffType state) {
        handler.updateState(getChannelUID(CHANNEL_SKIPENABLED), state);
    }

    public void updateSkipPreviousEnabled(OnOffType state) {
        handler.updateState(getChannelUID(CHANNEL_SKIPPREVIOUSENABLED), state);
    }

    private void addToZone(BoseSoundTouchHandler handler) {
        if (handler != null) {
            boolean found = false;
            for (ZoneMember m : zoneMembers) {
                if (handler.getMacAddress().equals(m.getMac())) {
                    logger.warn("{}: Zone add: ID {} is already member in zone!", handler.getDeviceName(),
                            handler.getMacAddress());
                    found = true;
                    break;
                }
            }
            if (!found) {
                ZoneMember nm = new ZoneMember();
                nm.setHandler(handler);
                nm.setMac(handler.getMacAddress());
                Map<String, Object> props = handler.getThing().getConfiguration().getProperties();
                String host = (String) props.get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST);
                nm.setIp(host);
                // zoneMembers.add(nm);
                addZoneMember(nm);
                updateZones();
            }
        }
    }

    private void addZoneMember(ZoneMember zoneMember) {
        boolean found = false;
        for (ZoneMember m : zoneMembers) {
            if (zoneMember.getHandler().getMacAddress().equals(m.getMac())) {
                logger.warn("{}: Zone add: ID '{}' is already member in zone!", handler.getDeviceName(),
                        zoneMember.getHandler().getMacAddress());
                found = true;
                break;
            }
        }
        if (!found) {
            zoneMembers.add(zoneMember);
        }
    }

    private BoseSoundTouchHandler findHandlerByNameOrMAC(String identifier) throws BoseSoundTouchNotFoundException {
        BoseSoundTouchHandler handlerFound = null;
        BoseSoundTouchHandlerFactory factory = handler.getFactory();
        for (Entry<String, BoseSoundTouchHandler> entry : factory.getAllSoundTouchDevices().entrySet()) {
            BoseSoundTouchHandler curHandler = entry.getValue();
            // try by mac
            String mac = entry.getKey();
            if (identifier.equalsIgnoreCase(mac)) {
                handlerFound = curHandler;
                break;
            }
            // try by name
            String devName = curHandler.getDeviceName();
            if (identifier.equalsIgnoreCase(devName)) {
                handlerFound = curHandler;
                break;
            }
        }

        if (handlerFound != null) {
            return handlerFound;
        } else {
            throw new BoseSoundTouchNotFoundException();
        }
    }

    private ChannelUID getChannelUID(String channelId) {
        Thing thing = handler.getThing();
        Channel chann = thing.getChannel(channelId);
        if (chann == null) {
            // refresh thing...
            Thing newThing = ThingFactory.createThing(TypeResolver.resolve(thing.getThingTypeUID()), thing.getUID(),
                    thing.getConfiguration());
            handler.updateThing(newThing);
            chann = thing.getChannel(channelId);
        }
        return chann.getUID();
    }

    private void init() {
        zoneMembers = new ArrayList<ZoneMember>();
        zoneMaster = null;
        currentOperationMode = OperationModeType.OFFLINE;
        presetContainer = new PresetContainer();
        currentContentItem = null;

        bluetooth = false;
        aux = false;
        aux1 = false;
        aux2 = false;
        aux3 = false;
        internetRadio = false;
        storedMusic = false;
        hdmi1 = false;
        tv = false;
        bass = false;

        File folder = new File(ConfigConstants.getUserDataFolder() + "/" + BoseSoundTouchBindingConstants.BINDING_ID);
        if (!folder.exists()) {
            logger.debug("Creating directory {}", folder.getPath());
            folder.mkdirs();
        }
        presetFile = new File(folder, "presets.txt");
        try {
            presetContainer.readFromFile(presetFile);
        } catch (IOException e) {
            logger.warn("{}: Could not load presets from file", handler.getDeviceName());
        }
    }

    private void postContentItem(ContentItem contentItem) {
        if (contentItem != null) {
            setCurrentContentItem(contentItem);
            sendPostRequestInWebSocket("select", "", contentItem.generateXML());
        }
    }

    private void refreshZone() {
        if ((zoneState != null) && (zoneMaster != null) && (zoneMembers != null)) {
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
                case Standalone:
                    sb.append("Standalone");
                    break;
            }
            for (int i = 0; i < zoneMembers.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(zoneMembers.get(i).getHandler().getDeviceName());
            }
            String zoneData = sb.toString();
            logger.debug("{}: zoneInfo updated: {}", handler.getDeviceName(), zoneData);
            updateZoneInfo(new StringType(zoneData));
        }
    }

    private void removeFromZone(BoseSoundTouchHandler handler) {
        if (handler != null) {
            if (!removeZoneMember(handler)) {
                logger.warn("{}: Zone remove: ID {} is not a member in zone!", handler.getDeviceName(),
                        handler.getMacAddress());
            } else {
                updateZones();
            }
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

    private void sendPostRequestInWebSocket(String url, String postData) {
        sendPostRequestInWebSocket(url, "", postData);
    }

    private void sendPostRequestInWebSocket(String url, String infoAddon, String postData) {
        int id = 0;
        String msg = "<msg><header " + "deviceID=\"" + handler.getMacAddress() + "\"" + " url=\"" + url
                + "\" method=\"POST\"><request requestID=\"" + id + "\"><info " + infoAddon
                + " type=\"new\"/></request></header><body>" + postData + "</body></msg>";
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            handler.onWebSocketError(e);
        }
    }

    private void updateZones() {
        StringBuilder sb = new StringBuilder();
        sb.append("<zone master=\"").append(handler.getMacAddress()).append("\">");
        for (ZoneMember mbr : zoneMembers) {
            sb.append("<member ipaddress=\"").append(mbr.getIp()).append("\">").append(mbr.getMac())
                    .append("</member>");
        }
        sb.append("</zone>");
        sendPostRequestInWebSocket("setZone", "mainNode=\"newZone\"", sb.toString());
    }

    @Override
    public boolean isBluetoothAvailable() {
        return bluetooth;
    }

    @Override
    public boolean isAUXAvailable() {
        return aux;
    }

    @Override
    public boolean isAUX1Available() {
        return aux1;
    }

    @Override
    public boolean isAUX2Available() {
        return aux2;
    }

    @Override
    public boolean isAUX3Available() {
        return aux3;
    }

    @Override
    public boolean isTVAvailable() {
        return tv;
    }

    @Override
    public boolean isHDMI1Available() {
        return hdmi1;
    }

    @Override
    public boolean isInternetRadioAvailable() {
        return internetRadio;
    }

    @Override
    public boolean isStoredMusicAvailable() {
        return storedMusic;
    }

    @Override
    public boolean isBassAvailable() {
        return bass;
    }

    @Override
    public void setAUXAvailable(boolean aux) {
        this.aux = aux;
    }

    @Override
    public void setAUX1Available(boolean aux1) {
        this.aux1 = aux1;
    }

    @Override
    public void setAUX2Available(boolean aux2) {
        this.aux2 = aux2;
    }

    @Override
    public void setAUX3Available(boolean aux3) {
        this.aux3 = aux3;
    }

    @Override
    public void setStoredMusicAvailable(boolean storedMusic) {
        this.storedMusic = storedMusic;
    }

    @Override
    public void setInternetRadioAvailable(boolean internetRadio) {
        this.internetRadio = internetRadio;
    }

    @Override
    public void setBluetoothAvailable(boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    @Override
    public void setTVAvailable(boolean tv) {
        this.tv = tv;
    }

    @Override
    public void setHDMI1Available(boolean hdmi1) {
        this.hdmi1 = hdmi1;
    }

    @Override
    public void setBassAvailable(boolean bass) {
        this.bass = bass;
    }

}
