/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.milight.internal.handler;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.milight.internal.MilightBindingConstants;
import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractLedHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractLedHandler extends BaseThingHandler implements LedHandlerInterface {
    private final Logger logger = LoggerFactory.getLogger(AbstractLedHandler.class);

    protected final QueuedSend sendQueue;
    /** Each bulb type including zone has to be unique. -> Each type has an offset. */
    protected final int typeOffset;
    protected final MilightThingState state = new MilightThingState();
    protected LedHandlerConfig config = new LedHandlerConfig();
    protected int port = 0;

    protected @NonNullByDefault({}) InetAddress address;
    protected @NonNullByDefault({}) DatagramSocket socket;

    protected int delayTimeMS = 50;
    protected int repeatTimes = 3;

    protected int bridgeOffset;

    /**
     * A bulb always belongs to a zone in the milight universe and we need a way to queue commands for being send.
     *
     * @param typeOffset Each bulb type including its zone has to be unique. To realise this, each type has an offset.
     * @param sendQueue The send queue.
     * @param zone A zone, usually 0 means all bulbs of the same type. [0-4]
     * @throws SocketException
     */
    public AbstractLedHandler(Thing thing, QueuedSend sendQueue, int typeOffset) {
        super(thing);
        this.typeOffset = typeOffset;
        this.sendQueue = sendQueue;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case MilightBindingConstants.CHANNEL_COLOR:
                    updateState(channelUID, new HSBType(new DecimalType(state.hue360),
                            new PercentType(state.saturation), new PercentType(state.brightness)));
                    break;
                case MilightBindingConstants.CHANNEL_BRIGHTNESS:
                    updateState(channelUID, new PercentType(state.brightness));
                    break;
                case MilightBindingConstants.CHANNEL_SATURATION:
                    updateState(channelUID, new PercentType(state.saturation));
                    break;
                case MilightBindingConstants.CHANNEL_TEMP:
                    updateState(channelUID, new PercentType(state.colorTemperature));
                    break;
                case MilightBindingConstants.CHANNEL_ANIMATION_MODE:
                    updateState(channelUID, new DecimalType(state.animationMode));
                    break;
            }
            return;
        }

        switch (channelUID.getId()) {
            case MilightBindingConstants.CHANNEL_COLOR: {
                if (command instanceof HSBType) {
                    HSBType hsb = (HSBType) command;
                    this.setHSB(hsb.getHue().intValue(), hsb.getSaturation().intValue(), hsb.getBrightness().intValue(),
                            state);
                    updateState(MilightBindingConstants.CHANNEL_SATURATION, new PercentType(state.saturation));
                } else if (command instanceof OnOffType) {
                    OnOffType hsb = (OnOffType) command;
                    this.setPower(hsb == OnOffType.ON, state);
                } else if (command instanceof PercentType) {
                    PercentType p = (PercentType) command;
                    this.setBrightness(p.intValue(), state);
                } else if (command instanceof IncreaseDecreaseType) {
                    this.changeBrightness((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? 1 : -1,
                            state);
                } else {
                    logger.error(
                            "CHANNEL_COLOR channel only supports OnOffType/IncreaseDecreaseType/HSBType/PercentType");
                }
                updateState(MilightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(state.brightness));
                break;
            }
            case MilightBindingConstants.CHANNEL_NIGHTMODE: {
                this.nightMode(state);
                updateState(channelUID, UnDefType.UNDEF);
                break;
            }
            case MilightBindingConstants.CHANNEL_WHITEMODE: {
                this.whiteMode(state);
                updateState(channelUID, UnDefType.UNDEF);
                break;
            }
            case MilightBindingConstants.CHANNEL_BRIGHTNESS: {
                if (command instanceof OnOffType) {
                    OnOffType hsb = (OnOffType) command;
                    this.setPower(hsb == OnOffType.ON, state);
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    this.setBrightness(d.intValue(), state);
                } else if (command instanceof IncreaseDecreaseType) {
                    this.changeBrightness((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? 1 : -1,
                            state);
                } else {
                    logger.error("CHANNEL_BRIGHTNESS channel only supports OnOffType/IncreaseDecreaseType/DecimalType");
                }
                updateState(MilightBindingConstants.CHANNEL_COLOR, new HSBType(new DecimalType(state.hue360),
                        new PercentType(state.saturation), new PercentType(state.brightness)));

                break;
            }
            case MilightBindingConstants.CHANNEL_SATURATION: {
                if (command instanceof OnOffType) {
                    OnOffType s = (OnOffType) command;
                    this.setSaturation((s == OnOffType.ON) ? 100 : 0, state);
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    this.setSaturation(d.intValue(), state);
                } else if (command instanceof IncreaseDecreaseType) {
                    this.changeSaturation((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? 1 : -1,
                            state);
                } else {
                    logger.error("CHANNEL_SATURATION channel only supports OnOffType/IncreaseDecreaseType/DecimalType");
                }
                updateState(MilightBindingConstants.CHANNEL_COLOR, new HSBType(new DecimalType(state.hue360),
                        new PercentType(state.saturation), new PercentType(state.brightness)));

                break;
            }
            case MilightBindingConstants.CHANNEL_TEMP: {
                if (command instanceof OnOffType) {
                    OnOffType s = (OnOffType) command;
                    this.setColorTemperature((s == OnOffType.ON) ? 100 : 0, state);
                } else if (command instanceof IncreaseDecreaseType) {
                    this.changeColorTemperature(
                            (IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? 1 : -1, state);
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    this.setColorTemperature(d.intValue(), state);
                } else {
                    logger.error("CHANNEL_TEMP channel only supports OnOffType/IncreaseDecreaseType/DecimalType");
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_SPEED_REL: {
                if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType id = (IncreaseDecreaseType) command;
                    if (id == IncreaseDecreaseType.INCREASE) {
                        this.changeSpeed(1, state);
                    } else if (id == IncreaseDecreaseType.DECREASE) {
                        this.changeSpeed(-1, state);
                    }
                } else {
                    logger.error("CHANNEL_SPEED channel only supports IncreaseDecreaseType");
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_ANIMATION_MODE: {
                if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    this.setLedMode(d.intValue(), state);
                } else {
                    logger.error("Animation mode channel only supports DecimalType");
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_ANIMATION_MODE_REL: {
                if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType id = (IncreaseDecreaseType) command;
                    if (id == IncreaseDecreaseType.INCREASE) {
                        this.nextAnimationMode(state);
                    } else if (id == IncreaseDecreaseType.DECREASE) {
                        this.previousAnimationMode(state);
                    }
                } else {
                    logger.error("Relative animation mode channel only supports IncreaseDecreaseType");
                }
                break;
            }
            default:
                logger.error("Channel unknown {}", channelUID.getId());
        }
    }

    /**
     * Return the bride handler.
     */
    public @Nullable AbstractBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        return (AbstractBridgeHandler) bridge.getHandler();
    }

    /**
     * Return the bridge status.
     */
    public ThingStatusInfo getBridgeStatus() {
        Bridge b = getBridge();
        if (b != null) {
            return b.getStatusInfo();
        } else {
            return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }

    /**
     * Generates a unique command id for the {@see QueuedSend}. It incorporates the bridge, zone, bulb type and command
     * category.
     *
     * @param commandCategory The category of the command.
     *
     * @return
     */
    public int uidc(int commandCategory) {
        return (bridgeOffset + config.zone + typeOffset + 1) * 64 + commandCategory;
    }

    protected void start(AbstractBridgeHandler handler) {
    }

    @Override
    public void bridgeStatusChanged(@NonNull ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        AbstractBridgeHandler h = getBridgeHandler();
        if (h == null) {
            logger.warn("Bridge handler not found!");
            return;
        }
        final InetAddress inetAddress = h.address;
        if (inetAddress == null) {
            logger.warn("Bridge handler has not yet determined the IP address!");
            return;
        }

        state.reset();
        configUpdated(h, inetAddress);

        if (h.getThing().getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            start(h);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Called by the bridge if a configuration update happened after initialisation has been done
     *
     * @param h The bridge handler
     */
    public void configUpdated(AbstractBridgeHandler h, InetAddress address) {
        this.port = h.config.port;
        this.address = address;
        this.socket = h.socket;
        this.delayTimeMS = h.config.delayTime;
        this.repeatTimes = h.config.repeat;
        this.bridgeOffset = h.bridgeOffset;
    }

    @Override
    public void initialize() {
        config = getConfigAs(LedHandlerConfig.class);
        bridgeStatusChanged(getBridgeStatus());
    }
}
