/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dreamscreen.internal.handler;

import static org.eclipse.smarthome.core.library.types.OnOffType.*;
import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.openhab.binding.dreamscreen.internal.DreamScreenBindingConstants.*;
import static org.openhab.binding.dreamscreen.internal.model.DreamScreenMode.*;
import static org.openhab.binding.dreamscreen.internal.model.DreamScreenScene.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.dreamscreen.internal.DreamScreenConfiguration;
import org.openhab.binding.dreamscreen.internal.DreamScreenServer;
import org.openhab.binding.dreamscreen.internal.message.AmbientModeTypeMessage;
import org.openhab.binding.dreamscreen.internal.message.ColorMessage;
import org.openhab.binding.dreamscreen.internal.message.DreamScreenMessage;
import org.openhab.binding.dreamscreen.internal.message.ModeMessage;
import org.openhab.binding.dreamscreen.internal.message.RefreshMessage;
import org.openhab.binding.dreamscreen.internal.message.SceneMessage;
import org.openhab.binding.dreamscreen.internal.message.SerialNumberMessage;
import org.openhab.binding.dreamscreen.internal.model.DreamScreenMode;
import org.openhab.binding.dreamscreen.internal.model.DreamScreenScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DreamScreenBaseHandler} is responsible for handling DreamScreen commands
 *
 * @author Bruce Brouwer - Initial contribution
 * @author Markus Michels - Adapted to 2.5.x
 */
@NonNullByDefault
public abstract class DreamScreenBaseHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DreamScreenBaseHandler.class);

    public final DreamScreenServer server;
    private Queue<DreamScreenMessage> writes = new ConcurrentLinkedQueue<>();
    private Queue<DreamScreenMessage> reads = new ConcurrentLinkedQueue<>();
    private @Nullable ScheduledFuture<?> sending;
    private boolean messagesPaused = false;

    protected int serialNumber;
    private @Nullable InetAddress address;
    protected byte group = 0;

    private byte mode = 0;
    private DreamScreenMode powerOnMode = VIDEO; // TODO: consider persisting this
    private byte ambientModeType = COLOR.ambientModeType;
    private byte ambientScene = RANDOM_COLOR.ambientScene;
    private @Nullable DreamScreenScene newScene = null;
    private HSBType color = HSBType.WHITE;
    private boolean isOnline = false;

    public DreamScreenBaseHandler(DreamScreenServer server, final Thing thing) {
        super(thing);
        this.server = server;
    }

    @Override
    public void initialize() {
        DreamScreenConfiguration config = getConfigAs(DreamScreenConfiguration.class);
        logger.debug("Initializing {}", this.getThing().getUID());

        updateStatus(UNKNOWN);
        this.serialNumber = Integer.valueOf(config.serialNumber);

        // Attach to the server
        server.addHandler(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                powerCommand(command);
                break;
            case CHANNEL_MODE:
                modeCommand(command);
                break;
            case CHANNEL_SCENE:
                sceneCommand(command);
                break;
            case CHANNEL_COLOR:
                colorCommand(command);
                break;
        }
    }

    protected void online() {
        if (!this.isOnline) {
            updateStatus(ONLINE);
        }
    }

    public final boolean message(final DreamScreenMessage msg, final InetAddress address) {
        pauseMessages();
        try {
            if (msg instanceof SerialNumberMessage) {
                return link(((SerialNumberMessage) msg).getSerialNumber(), address);
            } else if (!address.equals(this.address)) {
                return false;
            }
            return processMsg(msg, address);
        } finally {
            resumeMessages();
        }
    }

    protected boolean processMsg(final DreamScreenMessage msg, final InetAddress address) {
        if (msg instanceof RefreshMessage) {
            return refreshMsg((RefreshMessage) msg);
        } else if (msg instanceof ModeMessage) {
            return modeMsg((ModeMessage) msg);
        } else if (msg instanceof ColorMessage) {
            return colorMsg((ColorMessage) msg);
        } else if (msg instanceof AmbientModeTypeMessage) {
            return ambientModeTypeMsg((AmbientModeTypeMessage) msg);
        } else if (msg instanceof SceneMessage) {
            return ambientSceneMsg((SceneMessage) msg);
        }
        return true;
    }

    public boolean link(final int serialNumber, final InetAddress address) {
        if (this.serialNumber == serialNumber) {
            logger.debug("Linking {} to {}", serialNumber, address);
            this.address = address;

            write(new RefreshMessage());
            return true;
        }
        return false;
    }

    protected boolean refreshMsg(final RefreshMessage msg) {
        online();
        this.group = msg.getGroup();
        modeRefresh(msg.getMode());
        colorRefresh(msg.getRed(), msg.getGreen(), msg.getBlue());
        this.ambientScene = msg.getScene(); // ambientSceneRefresh(msg.getScene());
        read(new AmbientModeTypeMessage(this.group, this.ambientModeType));
        return true;
    }

    private void powerCommand(Command command) {
        if (command instanceof OnOffType) {
            logger.debug("Changing {} power to {}", this.serialNumber, command);
            write(new ModeMessage(this.group, command == ON ? powerOnMode.deviceMode : 0));
        } else if (command instanceof RefreshType) {
            updateState(CHANNEL_POWER, this.mode == 0 ? OFF : ON);
        }
    }

    private void modeCommand(Command command) {
        if (command instanceof StringType) {
            logger.debug("{}: Changing mode to {}", serialNumber, command);
            final DreamScreenMode mode = DreamScreenMode.fromState((StringType) command);
            if (this.mode != 0) {
                write(new ModeMessage(this.group, mode.deviceMode));
            } else {
                this.powerOnMode = mode;
            }
        } else if (command instanceof RefreshType) {
            updateState(CHANNEL_MODE,
                    this.mode == 0 ? this.powerOnMode.state() : DreamScreenMode.fromDevice(this.mode).state());
        }
    }

    private boolean modeMsg(final ModeMessage msg) {
        online();
        modeRefresh(msg.getMode());
        if (msg.getMode() == AMBIENT.deviceMode) {
            DreamScreenScene updateToScene = this.newScene;
            if (updateToScene != null) {
                write(new AmbientModeTypeMessage(this.group, updateToScene.ambientModeType));
            }
        }
        return true;
    }

    private void modeRefresh(final byte newDeviceMode) {
        this.mode = newDeviceMode;

        final DreamScreenMode newMode = DreamScreenMode.fromDevice(newDeviceMode);
        if (newMode == null) {
            updateState(CHANNEL_POWER, OFF);
        } else {
            updateState(CHANNEL_POWER, ON);
            this.powerOnMode = newMode;
            updateState(CHANNEL_MODE, newMode.state());
        }
    }

    private void sceneCommand(Command command) {
        if (command instanceof DecimalType) {
            logger.debug("Changing {} scene to {}", this.serialNumber, command);
            final DreamScreenScene scene = DreamScreenScene.fromState((StringType) command);
            if (this.mode != AMBIENT.deviceMode) {
                this.newScene = scene;
                write(new ModeMessage(this.group, AMBIENT.deviceMode));
            } else if (scene.ambientModeType != this.ambientModeType) {
                this.newScene = scene;
                write(new AmbientModeTypeMessage(this.group, scene.ambientModeType));
            } else {
                this.newScene = null;
                write(new SceneMessage(this.group, scene.ambientScene));
            }
        } else if (command instanceof RefreshType) {
            updateState(CHANNEL_SCENE, DreamScreenScene.fromDevice(this.ambientModeType, this.ambientScene).state());
        }
    }

    private boolean ambientModeTypeMsg(final AmbientModeTypeMessage msg) {
        online();
        this.ambientModeType = msg.getAmbientModeType();

        final DreamScreenScene updateToScene = newScene;
        if (updateToScene != null && updateToScene.ambientModeType == msg.getAmbientModeType()) {
            if (msg.getAmbientModeType() == COLOR.ambientModeType) {
                updateState(CHANNEL_SCENE, COLOR.state());
            } else {
                write(new SceneMessage(this.group, updateToScene.ambientScene));
            }
        } else {
            updateState(CHANNEL_SCENE,
                    DreamScreenScene.fromDevice(msg.getAmbientModeType(), this.ambientScene).state());
        }
        this.newScene = null;
        return true;
    }

    private boolean ambientSceneMsg(final SceneMessage msg) {
        online();
        DreamScreenScene scene = DreamScreenScene.fromDeviceScene(msg.getScene());
        this.ambientModeType = scene.ambientModeType;
        ambientSceneRefresh(scene.ambientScene);
        this.newScene = null;
        return true;
    }

    private void ambientSceneRefresh(final byte newAmbientScene) {
        this.ambientScene = newAmbientScene;
        updateState(CHANNEL_SCENE, DreamScreenScene.fromDevice(this.ambientModeType, this.ambientScene).state());
    }

    private void colorCommand(Command command) {
        if (command instanceof HSBType) {
            logger.debug("Changing {} color to {}", this.serialNumber, command);
            final HSBType color = (HSBType) command;

            write(buildColorMsg(color));
            if (this.mode != AMBIENT.deviceMode) {
                this.newScene = COLOR;
                this.color = color;
                write(new ModeMessage(this.group, AMBIENT.deviceMode));
            } else if (this.ambientModeType != COLOR.ambientModeType) {
                this.newScene = COLOR;
                this.color = color;
                write(new AmbientModeTypeMessage(this.group, COLOR.ambientModeType));
            }
        } else if (command instanceof RefreshType) {
            updateState(CHANNEL_COLOR, this.color);
        }
    }

    private ColorMessage buildColorMsg(HSBType color) {
        final PercentType[] rgb = color.toRGB();
        final byte red = colorByte(rgb[0]);
        final byte green = colorByte(rgb[1]);
        final byte blue = colorByte(rgb[2]);
        return new ColorMessage(this.group, red, green, blue);
    }

    private byte colorByte(PercentType percent) {
        return percent.toBigDecimal().multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).byteValue();
    }

    private boolean colorMsg(final ColorMessage msg) {
        online();
        colorRefresh(msg.getRed(), msg.getGreen(), msg.getBlue());
        return true;
    }

    private void colorRefresh(final byte red, final byte green, final byte blue) {
        this.color = HSBType.fromRGB(red & 0xFF, green & 0xFF, blue & 0xFF);
        updateState(CHANNEL_COLOR, this.color);
    }

    protected void read(final DreamScreenMessage msg) {
        this.reads.add(msg);
        sendMessages(false);
    }

    protected void write(final DreamScreenMessage msg) {
        this.writes.add(msg);
        sendMessages(false);
    }

    private void pauseMessages() {
        synchronized (this) {
            final ScheduledFuture<?> sending = this.sending;
            this.messagesPaused = true;
            if (sending != null && !sending.isCancelled()) {
                sending.cancel(false);
            }
        }
    }

    private void resumeMessages() {
        synchronized (this) {
            this.messagesPaused = false;
            sendMessages(true);
        }
    }

    private void sendMessages(final boolean delayed) {
        synchronized (this) {
            final ScheduledFuture<?> sending = this.sending;
            if (!this.messagesPaused && (!this.writes.isEmpty() || !this.reads.isEmpty())) {
                if (sending == null || sending.isCancelled()) {
                    this.sending = this.scheduler.scheduleWithFixedDelay(this::sendMsg, delayed ? 15 : 0, 100,
                            TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private void sendMsg() {
        final DreamScreenServer server = this.server;
        final InetAddress address = this.address;
        if (server != null && address != null) {
            synchronized (this) {
                final ScheduledFuture<?> sending = this.sending;

                if (!this.writes.isEmpty()) {
                    final DreamScreenMessage msg = this.writes.poll();
                    try {
                        server.write(msg, address);
                    } catch (IOException e) {
                        logger.debug("Unable to send write message {} to {}", msg, this.serialNumber, e);
                        updateStatus(OFFLINE, COMMUNICATION_ERROR, "Cannot send message");
                    }
                } else if (!this.reads.isEmpty()) {
                    final DreamScreenMessage msg = this.reads.poll();
                    try {
                        server.read(msg, address);
                    } catch (IOException e) {
                        logger.debug("Unable to send read message {} to {}", msg, this.serialNumber, e);
                        updateStatus(OFFLINE, COMMUNICATION_ERROR, "Cannot send message");
                    }
                } else if (sending != null) {
                    sending.cancel(false);
                }
            }
        } else {
            logger.debug("DreamScreen {} is not online", this.serialNumber);
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        this.isOnline = status == ONLINE;
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public void dispose() {
        final DreamScreenServer server = this.server;
        if (server != null) {
            server.removeHandler(this);
        }
        super.dispose();
    }
}
