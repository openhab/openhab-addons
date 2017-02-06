/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch10.handler;

import java.net.URI;
import java.util.Set;

import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.bosesoundtouch10.BoseSoundTouch10BindingConstants;
import org.openhab.binding.bosesoundtouch10.helper.SimpleCallBackInterface;
import org.openhab.binding.bosesoundtouch10.helper.SimpleSocketListener;
import org.openhab.binding.bosesoundtouch10.helper.WSHelper;
import org.openhab.binding.bosesoundtouch10.helper.WSHelperInterface;
import org.openhab.binding.bosesoundtouch10.helper.XmlHelper;
import org.openhab.binding.bosesoundtouch10.helper.XmlResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoseSoundTouch10Handler} is responsible for handling commands, which are
 * sent to one of the channels, and handling messages coming from the speaker.
 *
 * @author syracom - Initial contribution
 */
public class BoseSoundTouch10Handler extends BaseThingHandler implements SimpleCallBackInterface {

    private Logger logger = LoggerFactory.getLogger(BoseSoundTouch10Handler.class);
    private String soundTouchURL;
    private WebSocketClient client;
    private WebSocketListener socketListener;
    private WSHelperInterface wsHelper;
    private XmlHelper xmlHelper;

    public enum KeyState {
        PRESS("press"),
        RELEASE("release");

        private String value;

        private KeyState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Sources {
        STANDBY,
        AUX,
        BLUETOOTH,
        INTERNET_RADIO
    }

    public enum BSTKeys {
        PLAY,
        PAUSE,
        POWER,
        PRESET_1,
        PRESET_2,
        PRESET_3,
        PRESET_4,
        PRESET_5,
        PRESET_6,
    }

    public BoseSoundTouch10Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String response = "";
        checkForReInit();
        switch (channelUID.getId()) {
            case BoseSoundTouch10BindingConstants.CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.POWER);
                }
                break;
            case BoseSoundTouch10BindingConstants.CHANNEL_VOLUME:
                if (command instanceof RefreshType) {
                    response = wsHelper.get("/volume");
                    if (response.equals("error")) {
                        break;
                    }
                    String volume = xmlHelper.parsePath("/volume/actualvolume", response);
                    if (!volume.isEmpty()) {
                        updateState(BoseSoundTouch10BindingConstants.CHANNEL_VOLUME, new PercentType(volume));
                    }
                }
                if (command instanceof PercentType) {
                    PercentType num = (PercentType) command;
                    response = wsHelper.setVolume(num);
                }
                break;
            case BoseSoundTouch10BindingConstants.CHANNEL_BASS:
                if (command instanceof RefreshType) {
                    response = getBassAndUpdateUI();
                    if (response.equals("error")) {
                        break;
                    }
                }
                if (command instanceof DecimalType) {
                    DecimalType num = (DecimalType) command;
                    response = wsHelper.setBass(num);
                }
                break;
            case BoseSoundTouch10BindingConstants.CHANNEL_CONTROL:
                if (command instanceof PlayPauseType) {
                    PlayPauseType playPause = (PlayPauseType) command;
                    switch (playPause.toString()) {
                        case "PLAY":
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PLAY);
                            break;
                        case "PAUSE":
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PAUSE);
                            break;
                        default:
                            break;
                    }
                }
            case BoseSoundTouch10BindingConstants.CHANNEL_SOURCE:
                if (command instanceof RefreshType) {
                    response = wsHelper.get("/now_playing");
                    if (response.equals("error")) {
                        break;
                    }
                    String source = xmlHelper.parseSource("/nowPlaying", response, "source");
                    if (source.equals(Sources.STANDBY.toString())) {
                        updateState(BoseSoundTouch10BindingConstants.CHANNEL_POWER, OnOffType.OFF);
                        updateState(BoseSoundTouch10BindingConstants.CHANNEL_NOW_PLAYING, new StringType(""));
                    } else {
                        String sourceName = xmlHelper.parsePath("/nowPlaying/ContentItem/itemName", response);
                        response = wsHelper.get("/presets");
                        if (response.equals("error")) {
                            break;
                        }
                        switch (source) {
                            case "BLUETOOTH":
                                refreshChannels(OnOffType.ON, new StringType(Sources.BLUETOOTH.toString()), sourceName,
                                        PlayPauseType.PLAY);
                                break;
                            case "AUX":
                                refreshChannels(OnOffType.ON, new StringType(Sources.AUX.toString()), sourceName,
                                        PlayPauseType.PLAY);
                                break;
                            case "INTERNET_RADIO":
                                if (!sourceName.isEmpty()) {
                                    String id = xmlHelper.parsePresets("/presets/preset", response, sourceName);
                                    if (!id.isEmpty()) {
                                        refreshChannels(OnOffType.ON, new StringType("PRESET_" + id), sourceName,
                                                PlayPauseType.PLAY);
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (command instanceof StringType) {
                    StringType strintType = (StringType) command;
                    switch (strintType.toString()) {
                        case "PRESET_1":
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_1);
                            break;
                        case "PRESET_2":
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_2);
                            break;
                        case "PRESET_3":
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_3);
                            break;
                        case "PRESET_4":
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_4);
                            break;
                        case "PRESET_5":
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_5);
                            break;
                        case "PRESET_6":
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_6);
                            break;
                        case "AUX":
                            response = wsHelper.selectAUX();
                            break;
                        case "BLUETOOTH":
                            response = wsHelper.selectBluetooth();
                            break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
        checkResponseForError(response);
    }

    private void checkResponseForError(String response) {
        if (response.startsWith("error")) {
            socketListener = null;
            try {
                if (client != null) {
                    client.stop();
                }
            } catch (Exception e) {
                logger.debug("Error on stopping websocketclient");
            } finally {
                client = null;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                reinit();
            }
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void checkForReInit() {
        if (socketListener == null && client == null) {
            reinit();
        }
    }

    private synchronized void refreshChannels(OnOffType power, StringType preset, String sourceName,
            PlayPauseType playPause) {
        updateState(BoseSoundTouch10BindingConstants.CHANNEL_POWER, power);
        updateState(BoseSoundTouch10BindingConstants.CHANNEL_SOURCE, preset);
        updateState(BoseSoundTouch10BindingConstants.CHANNEL_NOW_PLAYING, new StringType(sourceName));

        updateState(BoseSoundTouch10BindingConstants.CHANNEL_CONTROL, playPause);
    }

    private synchronized String getBassAndUpdateUI() {
        String response = wsHelper.get("/bass");
        if (response.equals("error")) {
            return "error";
        }
        String bass = xmlHelper.parsePath("/bass/actualbass", response);
        if (!bass.isEmpty()) {
            updateState(BoseSoundTouch10BindingConstants.CHANNEL_BASS, new DecimalType(bass));
        }
        return "";
    }

    @Override
    public synchronized void refreshUI(String message) {
        Set<XmlResult> xmlResultSet = xmlHelper.parseMessage(message);
        for (XmlResult xmlResult : xmlResultSet) {
            switch (xmlResult.getKey()) {
                case XmlHelper.VOLUME:
                    if (!xmlResult.getValue().isEmpty()) {
                        updateState(BoseSoundTouch10BindingConstants.CHANNEL_VOLUME,
                                new PercentType(xmlResult.getValue()));
                    }
                    break;
                case XmlHelper.NOWPLAYING:
                    if (xmlResult.getValue() != null && !xmlResult.getValue().isEmpty()) {
                        updateState(BoseSoundTouch10BindingConstants.CHANNEL_POWER, OnOffType.ON);
                        updateState(BoseSoundTouch10BindingConstants.CHANNEL_CONTROL, PlayPauseType.PLAY);
                        updateState(BoseSoundTouch10BindingConstants.CHANNEL_NOW_PLAYING,
                                new StringType(xmlResult.getValue()));
                    }
                    break;
                case XmlHelper.SOURCE:
                    switch (xmlResult.getValue()) {
                        case "STANDBY":
                            updateState(BoseSoundTouch10BindingConstants.CHANNEL_POWER, OnOffType.OFF);
                            updateState(BoseSoundTouch10BindingConstants.CHANNEL_NOW_PLAYING, new StringType(""));
                            break;
                        case "BLUETOOTH":
                            updateState(BoseSoundTouch10BindingConstants.CHANNEL_POWER, OnOffType.ON);
                            updateState(BoseSoundTouch10BindingConstants.CHANNEL_CONTROL, PlayPauseType.PLAY);
                            updateState(BoseSoundTouch10BindingConstants.CHANNEL_SOURCE,
                                    new StringType(Sources.BLUETOOTH.toString()));
                            break;
                        case "AUX":
                            updateState(BoseSoundTouch10BindingConstants.CHANNEL_POWER, OnOffType.ON);
                            updateState(BoseSoundTouch10BindingConstants.CHANNEL_CONTROL, PlayPauseType.PLAY);
                            updateState(BoseSoundTouch10BindingConstants.CHANNEL_SOURCE,
                                    new StringType(Sources.AUX.toString()));
                            break;
                        default:
                            break;
                    }
                    break;
                case XmlHelper.BASS:
                    getBassAndUpdateUI();
                    break;
                case XmlHelper.SELECTION:
                    String id = xmlResult.getValue();
                    if (!id.isEmpty()) {
                        updateState(BoseSoundTouch10BindingConstants.CHANNEL_SOURCE, new StringType("PRESET_" + id));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void setStatusOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Websocket error");
        socketListener = null;
        try {
            if (client != null) {
                client.stop();
            }
            client = null;
        } catch (Exception e) {
            logger.debug("Error on stopping websocketclient");
        }
    }

    private boolean createAndStartWebsocket() {
        if (soundTouchURL != null && !soundTouchURL.isEmpty() && !soundTouchURL.startsWith("0.0.0.0")) {
            client = new WebSocketClient();
            try {
                client.start();
                URI echoUri = new URI(soundTouchURL.replace("http", "ws").replace("8090", "8080"));
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                request.setSubProtocols("gabbo");
                client.connect(socketListener, echoUri, request);
                return true;
            } catch (Exception e) {
                logger.debug("Exception on Socket");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Websocket error");
                return false;
            }
        } else {
            logger.debug("Websocketlistener not running, no url");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Please check the url for the device");
            return false;
        }
    }

    private void reinit() {
        Configuration config = getThing().getConfiguration();
        soundTouchURL = (String) config.get(BoseSoundTouch10BindingConstants.DEVICEURL);
        wsHelper = new WSHelper(soundTouchURL);
        socketListener = new SimpleSocketListener(this);
        xmlHelper = new XmlHelper();
        boolean ok = createAndStartWebsocket();
        if (ok) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void initialize() {
        reinit();
    }
}
