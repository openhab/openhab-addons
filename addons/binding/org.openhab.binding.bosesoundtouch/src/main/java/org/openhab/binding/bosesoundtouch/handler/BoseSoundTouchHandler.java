/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.handler;

import static org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
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
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.bosesoundtouch.BoseSoundTouchBindingConstants;
import org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchHandlerFactory;
import org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchHandlerParent;
import org.openhab.binding.bosesoundtouch.internal.CommandExecutor;
import org.openhab.binding.bosesoundtouch.internal.XMLResponseProcessor;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.openhab.binding.bosesoundtouch.types.RemoteKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoseSoundTouchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public class BoseSoundTouchHandler extends BoseSoundTouchHandlerParent
        implements WebSocketListener, BoseSoundTouchTypeInterface {

    private final Logger logger = LoggerFactory.getLogger(BoseSoundTouchHandler.class);

    private boolean bluetooth;
    private boolean aux;
    private boolean aux1;
    private boolean aux2;
    private boolean aux3;
    private boolean internetRadio;
    private boolean storedMusic;
    private boolean hdmi1;
    private boolean tv;

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
    private ChannelUID channelSaveAsPresetUID;

    private ScheduledFuture<?> connectionChecker;
    private WebSocketClient client;
    private Session session;
    private ByteBuffer pingPayload = ByteBuffer.wrap("Are you still here?".getBytes());

    private XMLResponseProcessor xmlResponseProcessor;
    private BoseSoundTouchHandlerFactory factory;
    private CommandExecutor commandExecutor;

    public BoseSoundTouchHandler(Thing thing, BoseSoundTouchHandlerFactory factory) {
        super(thing);
        this.factory = factory;
        xmlResponseProcessor = new XMLResponseProcessor(this);
    }

    @Override
    public void initialize() {
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
        channelSaveAsPresetUID = getChannelUID(BoseSoundTouchBindingConstants.CHANNEL_SAVE_AS_PRESET);

        bluetooth = false;
        aux = false;
        aux1 = false;
        aux2 = false;
        aux3 = false;
        internetRadio = false;
        storedMusic = false;
        hdmi1 = false;
        tv = false;

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
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    commandExecutor.setPower((OnOffType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_VOLUME:
                if (command instanceof PercentType) {
                    commandExecutor.setVolume((PercentType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_MUTE:
                if (command instanceof OnOffType) {
                    commandExecutor.setMuted((OnOffType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_OPERATIONMODE:
                if (command instanceof StringType) {
                    String cmd = command.toString().toUpperCase().trim();
                    try {
                        command = OperationModeType.valueOf(cmd);
                    } catch (IllegalArgumentException iae) {
                        logger.warn("{}: OperationMode \"{}\" is not valid!", getDeviceName(), cmd);
                    }
                }
                if (command instanceof OperationModeType) {
                    commandExecutor.setOperationMode((OperationModeType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_PLAYER_CONTROL:
                if (command instanceof StringType) {
                    String cmd = command.toString();
                    if (cmd.equals("PLAY")) {
                        command = PlayPauseType.PLAY;
                    } else if (cmd.equals("PAUSE")) {
                        command = PlayPauseType.PAUSE;
                    } else if (cmd.equals("NEXT")) {
                        command = NextPreviousType.NEXT;
                    } else if (cmd.equals("PREVIOUS")) {
                        command = NextPreviousType.PREVIOUS;
                    }
                }
                if ((command instanceof PlayPauseType) || (command instanceof NextPreviousType)) {
                    commandExecutor.setPlayerControl(command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                updateState(channelPlayerControlUID, UnDefType.UNDEF);
                break;
            case CHANNEL_PRESET:
                if (command instanceof DecimalType) {
                    commandExecutor.setPreset((DecimalType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_PRESET_CONTROL:
                if (command instanceof StringType) {
                    String cmd = command.toString();
                    if (cmd.equals("NEXT")) {
                        command = NextPreviousType.NEXT;
                    } else if (cmd.equals("PREVIOUS")) {
                        command = NextPreviousType.PREVIOUS;
                    }
                }
                if (command instanceof NextPreviousType) {
                    commandExecutor.setPreset((NextPreviousType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                updateState(channelPlayerControlUID, UnDefType.UNDEF);
                break;
            case CHANNEL_BASS:
                if (command instanceof DecimalType) {
                    commandExecutor.setBass((DecimalType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_SAVE_AS_PRESET:
                if (command instanceof DecimalType) {
                    commandExecutor.setContentItemAsPreset((DecimalType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                updateState(channelSaveAsPresetUID, UnDefType.UNDEF);
                break;
            case CHANNEL_KEY_CODE:
                if (command instanceof StringType) {
                    String cmd = command.toString().toUpperCase().trim();
                    try {
                        command = RemoteKeyType.valueOf(cmd);
                    } catch (IllegalArgumentException e) {
                        logger.warn("{}: Invalid remote key: {}", getDeviceName(), cmd);
                    }
                }
                if (command instanceof RemoteKeyType) {
                    commandExecutor.simulateRemoteKey((RemoteKeyType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                updateState(channelKeyCodeUID, UnDefType.UNDEF);
                break;
            case CHANNEL_ZONE_CONTROL:
                if (command instanceof StringType) {
                    commandExecutor.setZone((StringType) command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    // TODO RefreshType
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                updateState(channelZoneControlUID, UnDefType.UNDEF);
                break;
            default:
                logger.warn("{} : Got command '{}' for channel '{}' which is unhandled!", getDeviceName(), command,
                        channelUID.getId());
                break;
        }

    }

    public BoseSoundTouchHandlerFactory getFactory() {
        return factory;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
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

    public void updatePlayerControl(State state) {
        updateState(channelPlayerControlUID, state);
    }

    public void updateMuteState(OnOffType state) {
        updateState(channelMuteUID, state);
    }

    public void updateVolume(PercentType state) {
        updateState(channelVolumeUID, state);
    }

    public void updateBassLevel(DecimalType state) {
        updateState(channelBassUID, state);
    }

    public void updateZoneInfo(StringType state) {
        updateState(channelZoneInfoUID, state);
    }

    public void updatePreset(DecimalType state) {
        updateState(channelPresetUID, state);
    }

    public void updateOperationMode(StringType state) {
        updateState(channelOperationModeUID, state);
    }

    public void updatePowerState(OnOffType state) {
        updateState(channelPowerUID, state);
    }

    @Override
    public boolean hasBluetooth() {
        return bluetooth;
    }

    @Override
    public boolean hasAUX() {
        return aux;
    }

    @Override
    public boolean hasAUX1() {
        return aux1;
    }

    @Override
    public boolean hasAUX2() {
        return aux2;
    }

    @Override
    public boolean hasAUX3() {
        return aux3;
    }

    @Override
    public boolean hasTV() {
        return tv;
    }

    @Override
    public boolean hasHDMI1() {
        return hdmi1;
    }

    @Override
    public boolean hasInternetRadio() {
        return internetRadio;
    }

    @Override
    public boolean hasStoredMusic() {
        return storedMusic;
    }

    public void setAUX(boolean aux) {
        this.aux = aux;
    }

    public void setAUX1(boolean aux1) {
        this.aux1 = aux1;
    }

    public void setAUX2(boolean aux2) {
        this.aux2 = aux2;
    }

    public void setAUX3(boolean aux3) {
        this.aux3 = aux3;
    }

    public void setStoredMusic(boolean storedMusic) {
        this.storedMusic = storedMusic;
    }

    public void setInternetRadio(boolean internetRadio) {
        this.internetRadio = internetRadio;
    }

    public void setBluetooth(boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public void setTV(boolean tv) {
        this.tv = tv;
    }

    public void setHDMI1(boolean hdmi1) {
        this.hdmi1 = hdmi1;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.debug("{}: onWebSocketConnect('{}')", getDeviceName(), session);
        this.session = session;
        commandExecutor = new CommandExecutor(session, this);
        updateStatus(ThingStatus.ONLINE);
        // socket.newMessageSink(PayloadType.TEXT);
        commandExecutor.getInfo();
    }

    @Override
    public void onWebSocketError(Throwable e) {
        logger.error("{}: Error during websocket communication: {}", getDeviceName(), e.getMessage(), e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        commandExecutor.setOperationMode(OperationModeType.OFFLINE);
        commandExecutor.checkOperationMode();
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
        logger.debug("{}: onWebSocketBinary({}, {}, '{}')", pos, len, Arrays.toString(arr));
    }

    @Override
    public void onWebSocketClose(int code, String reason) {
        logger.debug("{}: onClose({}, '{}')", getDeviceName(), code, reason);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        commandExecutor.setOperationMode(OperationModeType.OFFLINE);
        commandExecutor.checkOperationMode();
    }

    private void openConnection() {
        closeConnection();
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
