/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.handler;

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
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.helper.SimpleCallBackInterface;
import org.openhab.binding.bosesoundtouch.helper.SimpleSocketListener;
import org.openhab.binding.bosesoundtouch.helper.WSHelper;
import org.openhab.binding.bosesoundtouch.helper.WSHelperInterface;
import org.openhab.binding.bosesoundtouch.helper.XmlHelper;
import org.openhab.binding.bosesoundtouch.helper.XmlResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoseSoundTouchHandler} is responsible for handling commands, which are
 * sent to one of the channels, and handling messages coming from the speaker.
 *
 * @author syracom - Initial contribution
 */
public class BoseSoundTouchHandler extends BaseThingHandler implements SimpleCallBackInterface {

    private Logger logger = LoggerFactory.getLogger(BoseSoundTouchHandler.class);
    private String soundTouchURL;
    private WebSocketClient client;
    private WebSocketListener socketListener;
    private WSHelperInterface wsHelper;
    private XmlHelper xmlHelper;

    private static final String PROTOCOL_HTTP = "http";
    private static final String PROTOCOL_WS = "ws";
    private static final String HTTP_PORT = "8090";
    private static final String WS_PORT = "8080";

    private static final String SOURCE_BLUETOOTH = "BLUETOOTH";
    private static final String SOURCE_STANDBY = "STANDBY";
    private static final String SOURCE_AUX = "AUX";
    private static final String SOURCE_INTERNET_RADIO = "INTERNET_RADIO";
    private static final String SOURCE_PRESET_1 = "PRESET_1";
    private static final String SOURCE_PRESET_2 = "PRESET_2";
    private static final String SOURCE_PRESET_3 = "PRESET_3";
    private static final String SOURCE_PRESET_4 = "PRESET_4";
    private static final String SOURCE_PRESET_5 = "PRESET_5";
    private static final String SOURCE_PRESET_6 = "PRESET_6";
    private static final String SOURCE_PRESET_PREFIX = "PRESET_";

    private static final String REST_SERVICE_VOLUME = "/volume";
    private static final String REST_SERVICE_BASS = "/bass";
    private static final String REST_SERVICE_PRESETS = "/presets";
    private static final String REST_SERVICE_NOWPLAYING = "/now_playing";

    private static final String ERROR_STRING = "error";

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

    public BoseSoundTouchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String response = "";
        checkForReInit();
        switch (channelUID.getId()) {
            case BoseSoundTouchBindingConstants.CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.POWER);
                }
                break;
            case BoseSoundTouchBindingConstants.CHANNEL_VOLUME:
                if (command instanceof RefreshType) {
                    response = wsHelper.get(REST_SERVICE_VOLUME);
                    if (response.equals(ERROR_STRING)) {
                        break;
                    }
                    String volume = xmlHelper.parsePath(XmlHelper.REST_CURRENT_VOLUME, response);
                    if (!volume.isEmpty()) {
                        updateState(BoseSoundTouchBindingConstants.CHANNEL_VOLUME, new PercentType(volume));
                    }
                }
                if (command instanceof PercentType) {
                    PercentType num = (PercentType) command;
                    response = wsHelper.setVolume(num);
                }
                break;
            case BoseSoundTouchBindingConstants.CHANNEL_BASS:
                if (command instanceof RefreshType) {
                    response = getBassAndUpdateUI();
                    if (response.equals(ERROR_STRING)) {
                        break;
                    }
                }
                if (command instanceof DecimalType) {
                    DecimalType num = (DecimalType) command;
                    response = wsHelper.setBass(num);
                }
                break;
            case BoseSoundTouchBindingConstants.CHANNEL_CONTROL:
                if (command instanceof PlayPauseType) {
                    PlayPauseType playPause = (PlayPauseType) command;
                    switch (playPause) {
                        case PLAY:
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PLAY);
                            break;
                        case PAUSE:
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PAUSE);
                            break;
                        default:
                            break;
                    }
                }
            case BoseSoundTouchBindingConstants.CHANNEL_SOURCE:
                if (command instanceof RefreshType) {
                    response = wsHelper.get(REST_SERVICE_NOWPLAYING);
                    if (response.equals(ERROR_STRING)) {
                        break;
                    }
                    String source = xmlHelper.parseSource(XmlHelper.REST_NOWPLAYING, response,
                            XmlHelper.NODE_ATTRIBUTE_SOURCE);
                    if (source.equals(SOURCE_STANDBY)) {
                        updateState(BoseSoundTouchBindingConstants.CHANNEL_POWER, OnOffType.OFF);
                        updateState(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING, new StringType(""));
                    } else {
                        String sourceName = xmlHelper.parsePath(XmlHelper.REST_NOWPLAYING_ITEMNAME, response);
                        response = wsHelper.get(REST_SERVICE_PRESETS);
                        if (response.equals(ERROR_STRING)) {
                            break;
                        }
                        switch (source) {
                            case SOURCE_BLUETOOTH:
                                refreshChannels(OnOffType.ON, new StringType(SOURCE_BLUETOOTH), sourceName,
                                        PlayPauseType.PLAY);
                                break;
                            case SOURCE_AUX:
                                refreshChannels(OnOffType.ON, new StringType(SOURCE_AUX), sourceName,
                                        PlayPauseType.PLAY);
                                break;
                            case SOURCE_INTERNET_RADIO:
                                if (!sourceName.isEmpty()) {
                                    String id = xmlHelper.parsePresets(XmlHelper.REST_CURRENT_PRESET, response,
                                            sourceName);
                                    if (!id.isEmpty()) {
                                        refreshChannels(OnOffType.ON, new StringType(SOURCE_PRESET_PREFIX + id),
                                                sourceName, PlayPauseType.PLAY);
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
                        case SOURCE_PRESET_1:
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_1);
                            break;
                        case SOURCE_PRESET_2:
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_2);
                            break;
                        case SOURCE_PRESET_3:
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_3);
                            break;
                        case SOURCE_PRESET_4:
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_4);
                            break;
                        case SOURCE_PRESET_5:
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_5);
                            break;
                        case SOURCE_PRESET_6:
                            response = wsHelper.pressAndReleaseButtonOnSpeaker(BSTKeys.PRESET_6);
                            break;
                        case SOURCE_AUX:
                            response = wsHelper.selectAUX();
                            break;
                        case SOURCE_BLUETOOTH:
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
        if (response.startsWith(ERROR_STRING)) {
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
        updateState(BoseSoundTouchBindingConstants.CHANNEL_POWER, power);
        updateState(BoseSoundTouchBindingConstants.CHANNEL_SOURCE, preset);
        updateState(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING, new StringType(sourceName));

        updateState(BoseSoundTouchBindingConstants.CHANNEL_CONTROL, playPause);
    }

    private synchronized String getBassAndUpdateUI() {
        String response = wsHelper.get(REST_SERVICE_BASS);
        if (response.equals(ERROR_STRING)) {
            return ERROR_STRING;
        }
        String bass = xmlHelper.parsePath(XmlHelper.REST_CURRENT_BASS, response);
        if (!bass.isEmpty()) {
            updateState(BoseSoundTouchBindingConstants.CHANNEL_BASS, new DecimalType(bass));
        }
        return "";
    }

    @Override
    public synchronized void refreshUI(String message) {
        Set<XmlResult> xmlResultSet = xmlHelper.parseMessage(message);
        for (XmlResult xmlResult : xmlResultSet) {
            switch (xmlResult.getKey()) {
                case XmlHelper.WS_UPDATE_VOLUME:
                    if (!xmlResult.getValue().isEmpty()) {
                        updateState(BoseSoundTouchBindingConstants.CHANNEL_VOLUME,
                                new PercentType(xmlResult.getValue()));
                    }
                    break;
                case XmlHelper.WS_UPDATE_NOWPLAYING:
                    if (xmlResult.getValue() != null && !xmlResult.getValue().isEmpty()) {
                        updateState(BoseSoundTouchBindingConstants.CHANNEL_POWER, OnOffType.ON);
                        updateState(BoseSoundTouchBindingConstants.CHANNEL_CONTROL, PlayPauseType.PLAY);
                        updateState(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING,
                                new StringType(xmlResult.getValue()));
                    }
                    break;
                case XmlHelper.WS_UPDATE_SOURCE:
                    switch (xmlResult.getValue()) {
                        case SOURCE_STANDBY:
                            updateState(BoseSoundTouchBindingConstants.CHANNEL_POWER, OnOffType.OFF);
                            updateState(BoseSoundTouchBindingConstants.CHANNEL_NOW_PLAYING, new StringType(""));
                            break;
                        case SOURCE_BLUETOOTH:
                            updateState(BoseSoundTouchBindingConstants.CHANNEL_POWER, OnOffType.ON);
                            updateState(BoseSoundTouchBindingConstants.CHANNEL_CONTROL, PlayPauseType.PLAY);
                            updateState(BoseSoundTouchBindingConstants.CHANNEL_SOURCE,
                                    new StringType(SOURCE_BLUETOOTH));
                            break;
                        case SOURCE_AUX:
                            updateState(BoseSoundTouchBindingConstants.CHANNEL_POWER, OnOffType.ON);
                            updateState(BoseSoundTouchBindingConstants.CHANNEL_CONTROL, PlayPauseType.PLAY);
                            updateState(BoseSoundTouchBindingConstants.CHANNEL_SOURCE, new StringType(SOURCE_AUX));
                            break;
                        default:
                            break;
                    }
                    break;
                case XmlHelper.WS_UPDATE_BASS:
                    getBassAndUpdateUI();
                    break;
                case XmlHelper.WS_UPDATE_SELECTION:
                    String id = xmlResult.getValue();
                    if (!id.isEmpty()) {
                        updateState(BoseSoundTouchBindingConstants.CHANNEL_SOURCE,
                                new StringType(SOURCE_PRESET_PREFIX + id));
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
                URI echoUri = new URI(soundTouchURL.replace(PROTOCOL_HTTP, PROTOCOL_WS).replace(HTTP_PORT, WS_PORT));
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
        soundTouchURL = (String) config.get(BoseSoundTouchBindingConstants.DEVICEURL);
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
