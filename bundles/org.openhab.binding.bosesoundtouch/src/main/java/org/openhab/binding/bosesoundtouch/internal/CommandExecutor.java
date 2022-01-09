/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bosesoundtouch.internal;

import static org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchBindingConstants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.bosesoundtouch.internal.handler.BoseSoundTouchHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommandExecutor} class executes commands on the websocket
 *
 * @author Thomas Traunbauer - Initial contribution
 * @author Kai Kreuzer - code clean up
 */
public class CommandExecutor implements AvailableSources {
    private final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private final BoseSoundTouchHandler handler;

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
     * Synchronizes the underlying storage container with the current value for the presets stored on the player
     * by updating the available ones and deleting the cleared ones
     *
     * @param playerPresets a Map<Integer, ContentItems> containing the items currently stored on the player
     */
    public void updatePresetContainerFromPlayer(Map<Integer, ContentItem> playerPresets) {
        playerPresets.forEach((k, v) -> {
            try {
                if (v != null) {
                    handler.getPresetContainer().put(k, v);
                } else {
                    handler.getPresetContainer().remove(k);
                }
            } catch (ContentItemNotPresetableException e) {
                logger.debug("{}: ContentItem is not presetable", handler.getDeviceName());
            }
        });

        handler.refreshPresetChannel();
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
            handler.getPresetContainer().put(id, contentItem);
        } catch (ContentItemNotPresetableException e) {
            logger.debug("{}: ContentItem is not presetable", handler.getDeviceName());
        }
        handler.refreshPresetChannel();
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
        handler.getSession().getRemote().sendStringByFuture(msg);
        logger.debug("{}: sending request: {}", handler.getDeviceName(), msg);
    }

    /**
     * Sets the current ContentItem if it is valid, and inits an update of the operating values
     *
     * @param contentItem
     */
    public void setCurrentContentItem(ContentItem contentItem) {
        if ((contentItem != null) && (contentItem.isValid())) {
            ContentItem psFound = null;
            if (handler.getPresetContainer() != null) {
                Collection<ContentItem> listOfPresets = handler.getPresetContainer().getAllPresets();
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
                ContentItemMaker contentItemMaker = new ContentItemMaker(this, handler.getPresetContainer());
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
            if (currentOperationMode == OperationModeType.STANDBY) {
                postRemoteKey(RemoteKeyType.POWER);
            } else {
                postRemoteKey(RemoteKeyType.PLAY);
            }
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
            item = handler.getPresetContainer().get(command.intValue());
            postContentItem(item);
        } catch (NoPresetFoundException e) {
            logger.warn("{}: No preset found at id: {}", handler.getDeviceName(), command.intValue());
        }
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
     * Update GUI for Basslevel
     *
     * @param state the state is Type of DecimalType
     */
    public void updateBassLevelGUIState(DecimalType state) {
        handler.updateState(CHANNEL_BASS, state);
    }

    /**
     * Update GUI for Volume
     *
     * @param state the state is Type of PercentType
     */
    public void updateVolumeGUIState(PercentType state) {
        handler.updateState(CHANNEL_VOLUME, state);
    }

    /**
     * Update GUI for OperationMode
     *
     * @param state the state is Type of StringType
     */
    public void updateOperationModeGUIState(StringType state) {
        handler.updateState(CHANNEL_OPERATIONMODE, state);
    }

    /**
     * Update GUI for PlayerControl
     *
     * @param state the state is Type of State
     */
    public void updatePlayerControlGUIState(State state) {
        handler.updateState(CHANNEL_PLAYER_CONTROL, state);
    }

    /**
     * Update GUI for Power
     *
     * @param state the state is Type of OnOffType
     */
    public void updatePowerStateGUIState(OnOffType state) {
        handler.updateState(CHANNEL_POWER, state);
    }

    /**
     * Update GUI for Preset
     *
     * @param state the state is Type of DecimalType
     */
    public void updatePresetGUIState(DecimalType state) {
        handler.updateState(CHANNEL_PRESET, state);
    }

    private void init() {
        getInformations(APIRequest.INFO);
        currentOperationMode = OperationModeType.OFFLINE;
        currentContentItem = null;

        mapOfAvailableFunctions = new HashMap<>();
    }

    private void postContentItem(ContentItem contentItem) {
        if (contentItem != null) {
            setCurrentContentItem(contentItem);
            sendPostRequestInWebSocket("select", "", contentItem.generateXML());
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
            handler.getSession().getRemote().sendStringByFuture(msg);
            logger.debug("{}: sending request: {}", handler.getDeviceName(), msg);
        } catch (NullPointerException e) {
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
            updatePowerStateGUIState(OnOffType.OFF);
            updatePlayerControlGUIState(PlayPauseType.PAUSE);
        } else {
            updatePowerStateGUIState(OnOffType.ON);
        }
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

    public void playNotificationSound(String appKey, BoseSoundTouchNotificationChannelConfiguration notificationConfig,
            String fileUrl) {
        String msg = "<play_info>" + "<app_key>" + appKey + "</app_key>" + "<url>" + fileUrl + "</url>" + "<service>"
                + notificationConfig.notificationService + "</service>"
                + (notificationConfig.notificationReason != null
                        ? "<reason>" + notificationConfig.notificationReason + "</reason>"
                        : "")
                + (notificationConfig.notificationMessage != null
                        ? "<message>" + notificationConfig.notificationMessage + "</message>"
                        : "")
                + (notificationConfig.notificationVolume != null
                        ? "<volume>" + notificationConfig.notificationVolume + "</volume>"
                        : "")
                + "</play_info>";

        sendPostRequestInWebSocket("speaker", msg);
    }
}
