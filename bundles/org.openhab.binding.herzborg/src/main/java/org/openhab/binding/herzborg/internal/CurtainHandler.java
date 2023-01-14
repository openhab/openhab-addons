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
package org.openhab.binding.herzborg.internal;

import static org.openhab.binding.herzborg.internal.HerzborgBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.herzborg.internal.dto.HerzborgProtocol.ControlAddress;
import org.openhab.binding.herzborg.internal.dto.HerzborgProtocol.DataAddress;
import org.openhab.binding.herzborg.internal.dto.HerzborgProtocol.Function;
import org.openhab.binding.herzborg.internal.dto.HerzborgProtocol.Packet;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CurtainHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pavel Fedin - Initial contribution
 */
@NonNullByDefault
public class CurtainHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(CurtainHandler.class);

    private CurtainConfiguration config = new CurtainConfiguration();
    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable Bus bus;

    public CurtainHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String ch = channelUID.getId();
        Packet pkt = null;

        switch (ch) {
            case CHANNEL_POSITION:
                if (command instanceof UpDownType) {
                    pkt = buildPacket(Function.CONTROL,
                            (command == UpDownType.UP) ? ControlAddress.OPEN : ControlAddress.CLOSE);
                } else if (command instanceof StopMoveType) {
                    pkt = buildPacket(Function.CONTROL, ControlAddress.STOP);
                } else if (command instanceof DecimalType) {
                    pkt = buildPacket(Function.CONTROL, ControlAddress.PERCENT, ((DecimalType) command).byteValue());
                }
                break;
            case CHANNEL_REVERSE:
                if (command instanceof OnOffType) {
                    pkt = buildPacket(Function.WRITE, DataAddress.DEFAULT_DIR, command.equals(OnOffType.ON) ? 1 : 0);
                }
                break;
            case CHANNEL_HAND_START:
                if (command instanceof OnOffType) {
                    pkt = buildPacket(Function.WRITE, DataAddress.HAND_START, command.equals(OnOffType.ON) ? 0 : 1);
                }
                break;
            case CHANNEL_EXT_SWITCH:
                if (command instanceof StringType) {
                    pkt = buildPacket(Function.WRITE, DataAddress.EXT_SWITCH, Byte.valueOf(command.toString()));
                }
                break;
            case CHANNEL_HV_SWITCH:
                if (command instanceof StringType) {
                    pkt = buildPacket(Function.WRITE, DataAddress.EXT_HV_SWITCH, Byte.valueOf(command.toString()));
                }
                break;
        }

        if (pkt != null) {
            final Packet p = pkt;
            scheduler.schedule(() -> {
                Packet reply = doPacket(p);

                if (reply != null) {
                    logger.trace("Function {} addr {} reply {}", p.getFunction(), p.getDataAddress(),
                            DatatypeConverter.printHexBinary(reply.getBuffer()));
                }
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    private Packet buildPacket(byte function, byte data_addr) {
        return new Packet((short) config.address, function, data_addr);
    }

    private Packet buildPacket(byte function, byte data_addr, byte value) {
        return new Packet((short) config.address, function, data_addr, value);
    }

    private Packet buildPacket(byte function, byte data_addr, int value) {
        return buildPacket(function, data_addr, (byte) value);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();

        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Bridge not present");
            return;
        }

        BridgeHandler handler = bridge.getHandler();

        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Bridge has no handler");
            return;
        }

        bus = ((BusHandler) handler).getBus();
        config = getConfigAs(CurtainConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        logger.trace("Successfully initialized, starting poll");
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, config.pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        stopPoll();
    }

    private void stopPoll() {
        ScheduledFuture<?> poll = pollFuture;
        pollFuture = null;

        if (poll != null) {
            poll.cancel(true);
        }
    }

    private @Nullable synchronized Packet doPacket(Packet pkt) {
        Bus bus = this.bus;

        if (bus == null) {
            // This is an impossible situation but Eclipse forces us to handle it
            logger.warn("No Bridge sending commands");
            return null;
        }

        try {
            Packet reply = bus.doPacket(pkt);

            if (reply == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return null;
            }

            if (reply.isValid()) {
                updateStatus(ThingStatus.ONLINE);
                return reply;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid response received: " + DatatypeConverter.printHexBinary(reply.getBuffer()));
                bus.flush();
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        return null;
    }

    private void poll() {
        Packet reply = doPacket(buildPacket(Function.READ, DataAddress.POSITION, 4));

        if (reply != null) {
            byte position = reply.getData(0);
            byte reverse = reply.getData(1);
            byte handStart = reply.getData(2);
            byte mode = reply.getData(3);

            // If calibration has been lost, position is reported as -1.
            updateState(CHANNEL_POSITION,
                    (position > 100 || position < 0) ? UnDefType.UNDEF : new PercentType(position));
            updateState(CHANNEL_REVERSE, reverse != 0 ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_HAND_START, handStart == 0 ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_MODE, new StringType(String.valueOf(mode)));
        }

        Packet extReply = doPacket(buildPacket(Function.READ, DataAddress.EXT_SWITCH, 2));

        if (extReply != null) {
            byte extSwitch = extReply.getData(0);
            byte hvSwitch = extReply.getData(1);

            updateState(CHANNEL_EXT_SWITCH, new StringType(String.valueOf(extSwitch)));
            updateState(CHANNEL_HV_SWITCH, new StringType(String.valueOf(hvSwitch)));
        }
    }
}
