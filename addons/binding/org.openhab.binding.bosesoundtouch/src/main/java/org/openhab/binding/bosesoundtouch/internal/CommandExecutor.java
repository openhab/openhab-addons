/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import static org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.openhab.binding.bosesoundtouch.internal.exceptions.BoseSoundTouchNotFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.ContentItemNotPresetableException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoInternetRadioPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoStoredMusicPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.OperationModeNotAvailableException;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.openhab.binding.bosesoundtouch.types.RemoteKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandExecutor} class executes commands on the websocket
 *
 * @author Thomas Traunbauer
 */
public class CommandExecutor implements AvailableSources {
    private Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private BoseSoundTouchHandler handler;
    private PresetContainer presetContainer;

    private BoseSoundTouchHandler zoneMaster;
    private List<BoseSoundTouchHandler> listOfZoneMembers;

    private boolean currentMuted;
    private ContentItem currentContentItem;
    private OperationModeType currentOperationMode;

    private Map<String, Boolean> mapOfAvailableFunctions;

    /**
     * Creates a new instance of this class
     *
     * @param handler the handler that created this CommandExecutor
     */
    public CommandExecutor(BoseSoundTouchHandler handler) {
        this.handler = handler;
        init();
    }

    /**
     * Adds a ContentItem to the PresetContainer
     *
     * @param id the id the ContentItem should be reached
     * @param contentItem the contentItem that should be saved as PRESET. Note that a eventually set presetID of the
     *            ContentItem will be overwritten with id
     */
    public void addContentItemToPresetContainer(int id, ContentItem contentItem) {
        contentItem.setPresetID(id);
        try {
            presetContainer.put(id, contentItem);
        } catch (ContentItemNotPresetableException e) {
            logger.debug("{}: ContentItem is not presetable", handler.getDeviceName());
        } catch (IOException e) {
            logger.warn("{}: Could not save presets to file", handler.getDeviceName());
        }
    }

    /**
     * Adds the current selected ContentItem to the PresetContainer
     *
     * @param command the command is a DecimalType, thats intValue will be used as id. The id the ContentItem should be
     *            reached
     */
    public void addCurrentContentItemToPresetContainer(DecimalType command) {
        if (command.intValue() > 6) {
            addContentItemToPresetContainer(command.intValue(), currentContentItem);
        } else {
            logger.warn("{}: Only PresetID >6 is allowed", handler.getDeviceName());
        }
    }

    /**
     * Initializes a API Request on this device
     *
     * @param apiRequest the apiRequest thats informations should be collected
     */
    public void getInformations(APIRequest apiRequest) {
        String msg = "<msg><header " + "deviceID=\"" + handler.getMacAddress() + "\"" + " url=\"" + apiRequest
                + "\" method=\"GET\"><request requestID=\"0\"><info type=\"new\"/></request></header></msg>";
        try {
            handler.getSession().getRemote().sendString(msg);
        } catch (IOException e) {
            handler.onWebSocketError(e);
        }
    }

    /**
     * Returns the Zone Master
     *
     * @return the Zone Master
     */
    public BoseSoundTouchHandler getZoneMaster() {
        return zoneMaster;
    }

    /**
     * Sets the current ContentItem if it is valid, and inits an update of the operating values
     *
     * @param contentItem
     */
    public void setCurrentContentItem(ContentItem contentItem) {
        if ((contentItem != null) && (contentItem.isValid())) {
            ContentItem psFound = null;
            if (presetContainer != null) {
                Collection<ContentItem> listOfPresets = presetContainer.getAllPresets();
                for (ContentItem ps : listOfPresets) {
                    if (ps.isPresetable()) {
                        if (ps.getLocation().equals(contentItem.getLocation())) {
                            psFound = ps;
                        }
                    }
                }
                int presetID = 0;
                if (psFound != null) {
                    presetID = psFound.getPresetID();
                }
                contentItem.setPresetID(presetID);

                currentContentItem = contentItem;
            }
        }
        updateOperatingValues();
    }

    /**
     * Sets the device is currently muted
     *
     * @param muted
     */
    public void setCurrentMuted(boolean muted) {
        currentMuted = muted;
    }

    /**
     * Sets the device zone definition
     *
     * @param zoneState
     * @param zoneMaster
     * @param listOfZoneMembers
     */
    public void setZone(BoseSoundTouchHandler zoneMaster, List<BoseSoundTouchHandler> listOfZoneMembers) {
        this.zoneMaster = zoneMaster;
        this.listOfZoneMembers = listOfZoneMembers;

        updateZoneInfoGUIState();
    }

    /**
     * Post Bass on the device
     *
     * @param command the command is Type of DecimalType
     */
    public void postBass(DecimalType command) {
        if (isBassAvailable()) {
            sendPostRequestInWebSocket("bass",
                    "<bass deviceID=\"" + handler.getMacAddress() + "\"" + ">" + command.intValue() + "</bass>");
        } else {
            logger.warn("{}: Bass modification not supported for this device", handler.getDeviceName());
        }
    }

    /**
     * Post OperationMode on the device
     *
     * @param command the command is Type of OperationModeType
     */
    public void postOperationMode(OperationModeType command) {
        if (command == OperationModeType.STANDBY) {
            postPower(OnOffType.OFF);
        } else {
            try {
                ContentItemMaker contentItemMaker = new ContentItemMaker(this, presetContainer);
                ContentItem contentItem = contentItemMaker.getContentItem(command);
                postContentItem(contentItem);
            } catch (OperationModeNotAvailableException e) {
                logger.warn("{}: OperationMode \"{}\" is not supported yet", handler.getDeviceName(),
                        command.toString());
            } catch (NoInternetRadioPresetFoundException e) {
                logger.warn("{}: Unable to switch to mode \"INTERNET_RADIO\". No PRESET defined",
                        handler.getDeviceName());
            } catch (NoStoredMusicPresetFoundException e) {
                logger.warn("{}: Unable to switch to mode: \"STORED_MUSIC\". No PRESET defined",
                        handler.getDeviceName());
            }
            updateOperatingValues();
        }
    }

    /**
     * Post PlayerControl on the device
     *
     * @param command the command is Type of Command
     */
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

    /**
     * Post Power on the device
     *
     * @param command the command is Type of OnOffType
     */
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
        updateOperatingValues();
    }

    /**
     * Post Preset on the device
     *
     * @param command the command is Type of DecimalType
     */
    public void postPreset(DecimalType command) {
        ContentItem item = null;
        try {
            item = presetContainer.get(command.intValue());
            postContentItem(item);
        } catch (NoPresetFoundException e) {
            logger.warn("{}: No preset found at id: {}", handler.getDeviceName(), command.intValue());
        }
    }

    /**
     * Post Preset on the device
     *
     * @param command the command is Type of NextPreviousType
     */
    public void postPreset(NextPreviousType command) {
        ContentItem item = null;
        if (command.equals(NextPreviousType.NEXT)) {
            try {
                item = presetContainer.get(currentContentItem.getPresetID() + 1);
            } catch (NoPresetFoundException e) {
                logger.warn("{}: No preset found at id: {}", handler.getDeviceName(),
                        currentContentItem.getPresetID() + 1);
            }
        } else if (command.equals(NextPreviousType.PREVIOUS)) {
            try {
                item = presetContainer.get(currentContentItem.getPresetID() - 1);
            } catch (NoPresetFoundException e) {
                logger.warn("{}: No preset found at id: {}", handler.getDeviceName(),
                        currentContentItem.getPresetID() - 1);
            }
        }
        postContentItem(item);
    }

    /**
     * Post RemoteKey on the device
     *
     * @param command the command is Type of RemoteKeyType
     */
    public void postRemoteKey(RemoteKeyType key) {
        sendPostRequestInWebSocket("key", "mainNode=\"keyPress\"",
                "<key state=\"press\" sender=\"Gabbo\">" + key.name() + "</key>");
        sendPostRequestInWebSocket("key", "mainNode=\"keyRelease\"",
                "<key state=\"release\" sender=\"Gabbo\">" + key.name() + "</key>");
    }

    /**
     * Post Volume on the device
     *
     * @param command the command is Type of PercentType
     */
    public void postVolume(PercentType command) {
        sendPostRequestInWebSocket("volume",
                "<volume deviceID=\"" + handler.getMacAddress() + "\"" + ">" + command.intValue() + "</volume>");
    }

    /**
     * Post VolumeMute on the device
     *
     * @param command the command is Type of OnOffType
     */
    public void postVolumeMuted(OnOffType command) {
        if (command.equals(OnOffType.ON)) {
            if (!currentMuted) {
                currentMuted = true;
                postRemoteKey(RemoteKeyType.MUTE);
            }
        } else if (command.equals(OnOffType.OFF)) {
            if (currentMuted) {
                currentMuted = false;
                postRemoteKey(RemoteKeyType.MUTE);
            }
        }
    }

    /**
     * Post Zone Add on the device
     *
     * @param command the command is Type of StringType
     */
    public void postZoneAdd(StringType command) {
        // try to parse string command...
        String identifier = command.toString().toLowerCase();
        try {
            BoseSoundTouchHandler handlerFound = findHandlerByNameOrMAC(identifier);
            addToZone(handlerFound);
        } catch (BoseSoundTouchNotFoundException e) {
            logger.warn("{}: Could not find Soundtouchd: {}", handler.getDeviceName(), identifier);
        }

    }

    /**
     * Post Zone Remove on the device
     *
     * @param command the command is Type of StringType
     */
    public void postZoneRemove(StringType command) {
        // try to parse string command...
        String identifier = command.toString().toLowerCase();
        try {
            BoseSoundTouchHandler handlerFound = findHandlerByNameOrMAC(identifier);
            removeFromZone(handlerFound);
        } catch (BoseSoundTouchNotFoundException e) {
            logger.warn("{}: Could not find Soundtouchd: {}", handler.getDeviceName(), identifier);
        }
    }

    /**
     * Update GUI for Basslevel
     *
     * @param state the state is Type of DecimalType
     */
    public void updateBassLevelGUIState(DecimalType state) {
        handler.updateState(handler.getChannelUID(CHANNEL_BASS), state);
    }

    /**
     * Update GUI for Volume
     *
     * @param state the state is Type of PercentType
     */
    public void updateVolumeGUIState(PercentType state) {
        handler.updateState(handler.getChannelUID(CHANNEL_VOLUME), state);
    }

    /**
     * Update GUI for OperationMode
     *
     * @param state the state is Type of StringType
     */
    public void updateOperationModeGUIState(StringType state) {
        handler.updateState(handler.getChannelUID(CHANNEL_OPERATIONMODE), state);
    }

    /**
     * Update GUI for PlayerControl
     *
     * @param state the state is Type of State
     */
    public void updatePlayerControlGUIState(State state) {
        handler.updateState(handler.getChannelUID(CHANNEL_PLAYER_CONTROL), state);
    }

    /**
     * Update GUI for Power
     *
     * @param state the state is Type of OnOffType
     */
    public void updatePowerStateGUIState(OnOffType state) {
        handler.updateState(handler.getChannelUID(CHANNEL_POWER), state);
    }

    /**
     * Update GUI for Preset
     *
     * @param state the state is Type of DecimalType
     */
    public void updatePresetGUIState(DecimalType state) {
        handler.updateState(handler.getChannelUID(CHANNEL_PRESET), state);
    }

    /**
     * Update GUI for ZoneInfo
     *
     * @param state the state is Type of StringType
     */
    public void updateZoneInfoGUIState(StringType state) {
        handler.updateState(handler.getChannelUID(CHANNEL_ZONE_INFO), state);
    }

    private void addToZone(BoseSoundTouchHandler memberHandler) {
        if (memberHandler != null) {
            boolean found = false;
            for (BoseSoundTouchHandler m : listOfZoneMembers) {
                if (memberHandler.getMacAddress().equals(m.getMacAddress())) {
                    logger.warn("{}: Zone add: ID {} is already member in zone!", memberHandler.getDeviceName(),
                            memberHandler.getMacAddress());
                    found = true;
                    break;
                }
            }
            if (!found) {
                listOfZoneMembers.add(memberHandler);

                StringBuilder sb = new StringBuilder();
                sb.append("<zone master=\"").append(handler.getMacAddress()).append("\">");
                for (BoseSoundTouchHandler mbr : listOfZoneMembers) {
                    sb.append("<member ipaddress=\"").append(mbr.getIPAddress()).append("\">")
                            .append(mbr.getMacAddress()).append("</member>");
                }
                sb.append("</zone>");
                sendPostRequestInWebSocket("setZone", "mainNode=\"newZone\"", sb.toString());
            }
        }
    }

    private BoseSoundTouchHandler findHandlerByNameOrMAC(String identifier) throws BoseSoundTouchNotFoundException {
        BoseSoundTouchHandler handlerFound = null;
        Collection<BoseSoundTouchHandler> colOfHandlers = handler.getFactory().getAllBoseSoundTouchHandler();
        for (BoseSoundTouchHandler curHandler : colOfHandlers) {
            // try by mac
            String mac = curHandler.getMacAddress();
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

    private void init() {
        getInformations(APIRequest.INFO);
        listOfZoneMembers = new ArrayList<BoseSoundTouchHandler>();
        zoneMaster = null;
        currentOperationMode = OperationModeType.OFFLINE;
        currentContentItem = null;

        mapOfAvailableFunctions = new HashMap<>();

        presetContainer = new PresetContainer();
    }

    private void postContentItem(ContentItem contentItem) {
        if (contentItem != null) {
            setCurrentContentItem(contentItem);
            sendPostRequestInWebSocket("select", "", contentItem.generateXML());
        }
    }

    private void removeFromZone(BoseSoundTouchHandler handler) {
        if (handler != null) {
            boolean removed = false;
            for (int i = 0; i < listOfZoneMembers.size(); i++) {
                if (handler.equals(listOfZoneMembers.get(i))) {
                    BoseSoundTouchHandler memberHandler = listOfZoneMembers.get(i);

                    StringBuilder sb = new StringBuilder();
                    sb.append("<zone master=\"").append(zoneMaster.getMacAddress()).append("\">");
                    sb.append("<member ipaddress=\"").append(memberHandler.getIPAddress()).append("\">")
                            .append(memberHandler.getMacAddress()).append("</member>");
                    sb.append("</zone>");
                    sendPostRequestInWebSocket("removeZoneSlave", sb.toString());

                    listOfZoneMembers.remove(i);
                    removed = true;
                }
            }
            if (!removed) {
                logger.warn("{}: Zone remove: ID {} is not a member in zone!", handler.getDeviceName(),
                        handler.getMacAddress());
            }
        }
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
            handler.getSession().getRemote().sendString(msg);
        } catch (IOException e) {
            handler.onWebSocketError(e);
        }
    }

    private void updateOperatingValues() {
        OperationModeType operationMode;
        if (currentContentItem != null) {
            updatePresetGUIState(new DecimalType(currentContentItem.getPresetID()));
            operationMode = currentContentItem.getOperationMode();
        } else {
            operationMode = OperationModeType.STANDBY;
        }

        updateOperationModeGUIState(new StringType(operationMode.toString()));
        currentOperationMode = operationMode;
        if (currentOperationMode == OperationModeType.STANDBY) {
            listOfZoneMembers.clear();
            if (zoneMaster != null) {
                zoneMaster.getCommandExecutor().removeFromZone(handler);
                zoneMaster = null;
            }
            updatePowerStateGUIState(OnOffType.OFF);
            updatePlayerControlGUIState(PlayPauseType.PAUSE);

        } else {
            updatePowerStateGUIState(OnOffType.ON);
        }
        updateZoneInfoGUIState();
    }

    private void updateZoneInfoGUIState() {
        String zoneData = "";
        if (zoneMaster == null) {
            zoneData = "Standalone";
        } else {
            if (zoneMaster.equals(handler)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Master; Members: ");
                for (int i = 0; i < listOfZoneMembers.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(listOfZoneMembers.get(i).getDeviceName());
                }
                zoneData = sb.toString();
            } else {
                if (zoneMaster == null) {
                    zoneData = "";
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Member; Master is: ");
                    sb.append(zoneMaster.getDeviceName());
                    zoneData = sb.toString();
                }
            }
        }
        logger.debug("{}: zoneInfo updated: {}", handler.getDeviceName(), zoneData);
        updateZoneInfoGUIState(new StringType(zoneData));
    }

    @Override
    public boolean isBluetoothAvailable() {
        return isSourceAvailable("bluetooth");
    }

    @Override
    public boolean isAUXAvailable() {
        return isSourceAvailable("aux");
    }

    @Override
    public boolean isAUX1Available() {
        return isSourceAvailable("aux1");
    }

    @Override
    public boolean isAUX2Available() {
        return isSourceAvailable("aux2");
    }

    @Override
    public boolean isAUX3Available() {
        return isSourceAvailable("aux3");
    }

    @Override
    public boolean isTVAvailable() {
        return isSourceAvailable("tv");
    }

    @Override
    public boolean isHDMI1Available() {
        return isSourceAvailable("hdmi1");
    }

    @Override
    public boolean isInternetRadioAvailable() {
        return isSourceAvailable("internetRadio");
    }

    @Override
    public boolean isStoredMusicAvailable() {
        return isSourceAvailable("storedMusic");
    }

    @Override
    public boolean isBassAvailable() {
        return isSourceAvailable("bass");
    }

    @Override
    public void setBluetoothAvailable(boolean bluetooth) {
        mapOfAvailableFunctions.put("bluetooth", bluetooth);
    }

    @Override
    public void setAUXAvailable(boolean aux) {
        mapOfAvailableFunctions.put("aux", aux);
    }

    @Override
    public void setAUX1Available(boolean aux1) {
        mapOfAvailableFunctions.put("aux1", aux1);
    }

    @Override
    public void setAUX2Available(boolean aux2) {
        mapOfAvailableFunctions.put("aux2", aux2);
    }

    @Override
    public void setAUX3Available(boolean aux3) {
        mapOfAvailableFunctions.put("aux3", aux3);
    }

    @Override
    public void setStoredMusicAvailable(boolean storedMusic) {
        mapOfAvailableFunctions.put("storedMusic", storedMusic);
    }

    @Override
    public void setInternetRadioAvailable(boolean internetRadio) {
        mapOfAvailableFunctions.put("internetRadio", internetRadio);
    }

    @Override
    public void setTVAvailable(boolean tv) {
        mapOfAvailableFunctions.put("tv", tv);
    }

    @Override
    public void setHDMI1Available(boolean hdmi1) {
        mapOfAvailableFunctions.put("hdmi1", hdmi1);
    }

    @Override
    public void setBassAvailable(boolean bass) {
        mapOfAvailableFunctions.put("bass", bass);
    }

    private boolean isSourceAvailable(String source) {
        Boolean isAvailable = mapOfAvailableFunctions.get(source);
        if (isAvailable == null) {
            return false;
        } else {
            return isAvailable;
        }
    }
}
