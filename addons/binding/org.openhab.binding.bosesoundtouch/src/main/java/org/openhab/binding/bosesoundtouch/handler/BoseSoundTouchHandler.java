/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.handler;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchHandlerFactory;
import org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchHandlerParent;
import org.openhab.binding.bosesoundtouch.internal.XMLResponseProcessor;
import org.openhab.binding.bosesoundtouch.internal.ZoneState;
import org.openhab.binding.bosesoundtouch.internal.items.ContentItem;
import org.openhab.binding.bosesoundtouch.internal.items.Preset;
import org.openhab.binding.bosesoundtouch.internal.items.RemoteKey;
import org.openhab.binding.bosesoundtouch.internal.items.ZoneMember;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoseSoundTouchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public class BoseSoundTouchHandler extends BoseSoundTouchHandlerParent implements WebSocketListener {

    private final Logger logger = LoggerFactory.getLogger(BoseSoundTouchHandler.class);

    private ChannelUID channelPowerUID;
    private ChannelUID channelVolumeUID;
    private ChannelUID channelMuteUID;
    private ChannelUID channelOperationModeUID;
    private ChannelUID channelZoneInfoUID;
    private ChannelUID channelPlayerControlUID;
    private ChannelUID channelZoneControlUID;
    private ChannelUID channelPresetUID;
    private ChannelUID channelBassUID;
    private ChannelUID channelKeyCodeUID;

    private ArrayList<Preset> listOfPresets;

    private State nowPlayingSource;
    private ContentItem currentContentItem;
    private boolean muted;
    private OperationModeType currentOperationMode;

    private ScheduledFuture<?> connectionChecker;
    private WebSocketClient client;
    private Session session;
    private ByteBuffer pingPayload = ByteBuffer.wrap("Are you still here?".getBytes());

    private XMLResponseProcessor xmlResponseProcessor;

    private BoseSoundTouchHandlerFactory factory;
    private ZoneState zoneState;
    private BoseSoundTouchHandler zoneMaster;
    private List<ZoneMember> zoneMembers;

    public BoseSoundTouchHandler(Thing thing, BoseSoundTouchHandlerFactory factory) {
        super(thing);
        this.factory = factory;
        xmlResponseProcessor = new XMLResponseProcessor(this);
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
        channelBassUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_BASS);
        channelKeyCodeUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_KEY_CODE);

        factory.registerSoundTouchDevice(this);
        connectionChecker = scheduler.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                checkConnection();
            }
        }, 300, 300, TimeUnit.SECONDS);
        openConnection();
    }

    @Override
    public void dispose() {
        super.dispose();
        closeConnection();
        if (connectionChecker != null && !connectionChecker.isCancelled()) {
            connectionChecker.cancel(false);
        }
    }

    @Override
    public void handleRemoval() {
        factory.removeSoundTouchDevice(this);
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{}: handleCommand({}, {});", getDeviceName(), channelUID, command);
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
                    String cmd = command.toString().toUpperCase().trim();
                    try {
                        OperationModeType selectedMode = OperationModeType.valueOf(cmd);
                        if (currentOperationMode == OperationModeType.STANDBY
                                && selectedMode != OperationModeType.STANDBY) {
                            // turn device on first...
                            simulateRemoteKey(RemoteKey.POWER);
                        }
                        switch (selectedMode) {
                            case STANDBY:
                                if (currentOperationMode != OperationModeType.STANDBY) {
                                    simulateRemoteKey(RemoteKey.POWER);
                                }
                                break;
                            case INTERNET_RADIO:
                                Preset psFound = null;
                                for (Preset ps : listOfPresets) {
                                    if ((psFound == null) && (ps.getContentItem()
                                            .getOperationMode() == OperationModeType.INTERNET_RADIO)) {
                                        psFound = ps;
                                    }
                                }
                                if (psFound != null) {
                                    simulateRemoteKey(psFound.getKey());
                                } else {
                                    logger.warn("{}: Unable to switch to mode: INTERNET_RADIO. No PRESET defined",
                                            getDeviceName());
                                }
                                break;
                            case BLUETOOTH:
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
                                    logger.warn("{}: Unable to switch to mode: BLUETOOTH. Mayby no device available",
                                            getDeviceName());
                                }
                                break;
                            case AUX:
                                simulateRemoteKey(RemoteKey.AUX_INPUT);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                }
                                break;
                            case STORED_MUSIC:
                            case SPOTIFY:
                            case DEEZER:
                            case SIRIUSXM:
                            case PANDORA:
                            case OFFLINE:
                            case OTHER:
                                logger.warn("{}: \"{}\" OperationMode selection not supported yet", getDeviceName(),
                                        cmd);
                                break;

                        }
                    } catch (IllegalArgumentException iae) {
                        logger.error("{}: OperationMode \"{}\" is not valid!", getDeviceName(), cmd);
                    }
                }
            } else if (channelUID.equals(channelVolumeUID)) {
                if (command instanceof PercentType) {
                    PercentType percentType = (PercentType) command;
                    sendRequestInWebSocket("volume", null, "<volume deviceID=\"" + getMacAddress() + "\"" + ">"
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
                        if (currentOperationMode == OperationModeType.STANDBY) {
                            simulateRemoteKey(RemoteKey.POWER);
                        } else {
                            simulateRemoteKey(RemoteKey.PLAY);
                        }
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
                    if (cmd.equals(RemoteKey.PLAY.name)) {
                        if (currentOperationMode == OperationModeType.STANDBY) {
                            simulateRemoteKey(RemoteKey.POWER);
                        } else {
                            simulateRemoteKey(RemoteKey.PLAY);
                        }
                    }
                    if (cmd.equals(RemoteKey.PAUSE.name)) {
                        simulateRemoteKey(RemoteKey.PAUSE);
                    }
                    if (cmd.equals("NEXT")) {
                        simulateRemoteKey(RemoteKey.NEXT_TRACK);
                    }
                    if (cmd.equals("PREVIOUS")) {
                        simulateRemoteKey(RemoteKey.PREV_TRACK);
                    }
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
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
                            logger.warn("{}: Invalid / unknown device: '{}' in command {}", getDeviceName(), other,
                                    cmd);
                        } else {
                            if ("add".equals(action)) {
                                boolean found = false;
                                for (ZoneMember m : zoneMembers) {
                                    if (oh.getMacAddress().equals(m.getMac())) {
                                        logger.warn("{}: Zone add: ID {} is already member in zone!", getDeviceName(),
                                                oh.getMacAddress());
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
                                    logger.warn("{}: Zone remove: ID {} is not a member in zone!", getDeviceName(),
                                            oh.getMacAddress());
                                } else {
                                    updateZones();
                                }
                            } else {
                                logger.warn("{}: Invalid zone command: {}", getDeviceName(), cmd);
                            }
                        }
                    } else {
                        logger.warn("{}: Invalid zone command: {}", getDeviceName(), cmd);
                    }
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
            } else if (channelUID.equals(channelPresetUID)) {
                if (command instanceof StringType) {
                    String cmd = command.toString();
                    if (cmd.equals(RemoteKey.PRESET_1.name)) {
                        simulateRemoteKey(RemoteKey.PRESET_1);
                    } else if (cmd.equals(RemoteKey.PRESET_2.name)) {
                        simulateRemoteKey(RemoteKey.PRESET_2);
                    } else if (cmd.equals(RemoteKey.PRESET_3.name)) {
                        simulateRemoteKey(RemoteKey.PRESET_3);
                    } else if (cmd.equals(RemoteKey.PRESET_4.name)) {
                        simulateRemoteKey(RemoteKey.PRESET_4);
                    } else if (cmd.equals(RemoteKey.PRESET_5.name)) {
                        simulateRemoteKey(RemoteKey.PRESET_5);
                    } else if (cmd.equals(RemoteKey.PRESET_6.name)) {
                        simulateRemoteKey(RemoteKey.PRESET_6);
                    } else {
                        logger.warn("{}: Invalid preset: {}", getDeviceName(), cmd);
                    }
                }
            } else if (channelUID.equals(channelBassUID)) {
                if (command instanceof DecimalType) {
                    int bassLevel = ((DecimalType) command).intValue();
                    sendRequestInWebSocket("bass", null, "<bass>" + bassLevel + "</bass>");
                }
            } else if (channelUID.equals(channelKeyCodeUID)) {
                if (command instanceof StringType) {
                    String cmd = command.toString().toUpperCase().trim();
                    try {
                        simulateRemoteKey(RemoteKey.valueOf(cmd));
                    } catch (IllegalArgumentException e) {
                        logger.warn("{}: Invalid remote key: {}", getDeviceName(), cmd);
                    }
                }
            } else {
                logger.warn("{} : Got command '{}' for channel '{}' which is unhandled!", getDeviceName(), command,
                        channelUID.getId());
            }
        }
    }

    public void sendRequestInWebSocket(String url) {
        int id = 0;
        String msg = "<msg><header " + "deviceID=\"" + getMacAddress() + "\"" + " url=\"" + url
                + "\" method=\"GET\"><request requestID=\"" + id + "\"><info type=\"new\"/></request></header></msg>";
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            onWebSocketError(e);
        }
    }

    private void sendRequestInWebSocket(String url, String infoAddon, String postData) {
        int id = 0;
        String msg = "<msg><header " + "deviceID=\"" + getMacAddress() + "\"" + " url=\"" + url
                + "\" method=\"POST\"><request requestID=\"" + id + "\"><info " + (infoAddon == null ? "" : infoAddon)
                + " type=\"new\"/></request></header><body>" + postData + "</body></msg>";
        try {
            session.getRemote().sendString(msg);
        } catch (IOException e) {
            onWebSocketError(e);
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
            updateState(channelPlayerControlUID, PlayPauseType.PAUSE);
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
                logger.warn("{}: Zone add: ID '{}' is already member in zone!", getDeviceName(),
                        zoneMember.getHandler().getMacAddress());
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

    public void updatePlayerControl(PlayPauseType state) {
        updateState(channelPlayerControlUID, state);
    }

    public void updateVolume(State state) {
        updateState(channelVolumeUID, state);
    }

    public void updateVolumeMuted(State state) {
        updateState(channelMuteUID, state);
    }

    public void updateBassLevel(DecimalType state) {
        updateState(channelBassUID, state);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.debug("{}: onWebSocketConnect('{}')", getDeviceName(), session);
        this.session = session;
        updateStatus(ThingStatus.ONLINE);
        // socket.newMessageSink(PayloadType.TEXT);
        sendRequestInWebSocket("info");
    }

    @Override
    public void onWebSocketError(Throwable e) {
        logger.error("{}: Error during websocket communication: {}", getDeviceName(), e.getMessage(), e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        this.currentOperationMode = OperationModeType.OFFLINE;
        this.currentContentItem = null;
        this.checkOperationMode();
        if (session != null) {
            session.close(StatusCode.SERVER_ERROR, getDeviceName() + ": Failure: " + e.getMessage());
        }
    }

    @Override
    public void onWebSocketText(String msg) {
        logger.debug("{}: onWebSocketText('{}')", getDeviceName(), msg);
        xmlResponseProcessor.handleMessage(msg);
    }

    @Override
    public void onWebSocketBinary(byte[] arr, int pos, int len) {
        // we don't expect binary data so just dump if we get some...
        logger.info("{}: onWebSocketBinary({}, {}, '{}')", pos, len, Arrays.toString(arr));
    }

    @Override
    public void onWebSocketClose(int code, String reason) {
        logger.debug("{}: onClose({}, '{}')", getDeviceName(), code, reason);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        this.currentOperationMode = OperationModeType.OFFLINE;
        this.currentContentItem = null;
        this.checkOperationMode();
    }

    public String getMacAddress() {
        return thing.getUID().getId();
    }

    public ChannelUID getChannelUID(String channelId) {
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
        String zoneData = sb.toString();
        logger.debug("{}: zoneInfo updated: {}", getDeviceName(), zoneData);
        updateState(channelZoneInfoUID, new StringType(zoneData));
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
        try {
            client = new WebSocketClient();
            // we need longer timeouts for web socket.
            client.setMaxIdleTimeout(360 * 1000);
            Map<String, Object> props = thing.getConfiguration().getProperties();
            String host = (String) props.get(BoseSoundTouchBindingConstants.DEVICE_PARAMETER_HOST);

            // Port seems to be hard coded, therefore no user input or discovery is necessary
            String wsUrl = "ws://" + host + ":8080/";
            logger.debug("{}: Connecting to: {}", getDeviceName(), wsUrl);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setSubProtocols("gabbo");
            client.start();
            client.connect(this, new URI(wsUrl), request);
        } catch (Exception e) {
            onWebSocketError(e);
        }
    }

    private void closeConnection() {
        if (session != null) {
            try {
                session.close(StatusCode.NORMAL, "Binding shutdown");
            } catch (Throwable e) {
                logger.error("{}: Error while closing websocket communication: {} ({})", getDeviceName(),
                        e.getClass().getName(), e.getMessage());
            }
            session = null;
        }
        if (client != null) {
            try {
                client.stop();
                client.destroy();
            } catch (Exception e) {
                logger.error("{}: Error while closing websocket communication: {} ({})", getDeviceName(),
                        e.getClass().getName(), e.getMessage());
            }
            client = null;
        }
    }

    private void checkConnection() {
        if (thing.getStatus() != ThingStatus.ONLINE) {
            openConnection(); // try to reconnect....
        }
        if (thing.getStatus() == ThingStatus.ONLINE) {
            try {
                session.getRemote().sendPing(pingPayload);
            } catch (Throwable e) {
                onWebSocketError(e);
                closeConnection();
                openConnection();
            }

        }
    }
}
