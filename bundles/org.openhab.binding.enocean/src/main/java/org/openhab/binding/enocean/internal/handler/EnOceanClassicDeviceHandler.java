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
package org.openhab.binding.enocean.internal.handler;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.enocean.internal.config.EnOceanActuatorConfig;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchConfigBase.SwitchMode;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchListenerConfig;
import org.openhab.binding.enocean.internal.config.EnOceanChannelVirtualRockerSwitchConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 *         This class defines base functionality for sending eep messages. This class extends EnOceanBaseSensorHandler
 *         class as most actuator things send status or response messages, too.
 */
public class EnOceanClassicDeviceHandler extends EnOceanBaseActuatorHandler {

    // List of thing types which support sending of eep messages
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_CLASSICDEVICE);

    private StringType lastTriggerEvent = StringType.valueOf(CommonTriggerEvents.DIR1_PRESSED);
    ScheduledFuture<?> releaseFuture = null;

    public EnOceanClassicDeviceHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    void initializeConfig() {
        super.initializeConfig();
        ((EnOceanActuatorConfig) config).broadcastMessages = true;
        ((EnOceanActuatorConfig) config).enoceanId = EMPTYENOCEANID;
    }

    @Override
    public long getEnOceanIdToListenTo() {
        return 0;
    }

    @Override
    public void channelLinked(@NonNull ChannelUID channelUID) {
        super.channelLinked(channelUID);

        // if linked channel is a listening channel => put listener
        Channel channel = getThing().getChannel(channelUID);
        addListener(channel);
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        // it seems that there does not exist a channel update callback
        // => remove all listeners and add them again
        getBridgeHandler().removePacketListener(this);

        this.getThing().getChannels().forEach(c -> {
            if (isLinked(c.getUID()) && !addListener(c)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong channel configuration");
            }
        });
    }

    @Override
    public void channelUnlinked(@NonNull ChannelUID channelUID) {
        super.channelUnlinked(channelUID);

        // if unlinked channel is listening channel => remove listener
        Channel channel = getThing().getChannel(channelUID);
        removeListener(channel);
    }

    protected boolean addListener(Channel channel) {
        if (channel == null) {
            return true;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String id = channelTypeUID == null ? "" : channelTypeUID.getId();

        if (id.startsWith(CHANNEL_ROCKERSWITCHLISTENER_START)) {
            EnOceanChannelRockerSwitchListenerConfig config = channel.getConfiguration()
                    .as(EnOceanChannelRockerSwitchListenerConfig.class);
            try {
                getBridgeHandler().addPacketListener(this, Long.parseLong(config.enoceanId, 16));
                return true;
            } catch (NumberFormatException e) {
            }

            return false;
        }
        return true;
    }

    protected void removeListener(Channel channel) {
        if (channel == null) {
            return;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String id = channelTypeUID == null ? "" : channelTypeUID.getId();

        if (id.startsWith(CHANNEL_ROCKERSWITCHLISTENER_START)) {
            EnOceanChannelRockerSwitchListenerConfig config = channel.getConfiguration()
                    .as(EnOceanChannelRockerSwitchListenerConfig.class);
            try {
                getBridgeHandler().removePacketListener(this, Long.parseLong(config.enoceanId, 16));
            } catch (NumberFormatException e) {
            }
        }
    }

    @Override
    protected Predicate<Channel> channelFilter(EEPType eepType, byte[] senderId) {
        return c -> {
            ChannelTypeUID channelTypeUID = c.getChannelTypeUID();
            String id = channelTypeUID == null ? "" : channelTypeUID.getId();

            return id.startsWith(CHANNEL_ROCKERSWITCHLISTENER_START)
                    && c.getConfiguration().as(EnOceanChannelRockerSwitchListenerConfig.class).enoceanId
                            .equalsIgnoreCase(HexUtils.bytesToHex(senderId));
        };
    }

    @SuppressWarnings("unlikely-arg-type")
    private StringType convertToReleasedCommand(StringType command) {
        return command.equals(CommonTriggerEvents.DIR1_PRESSED) ? StringType.valueOf(CommonTriggerEvents.DIR1_RELEASED)
                : StringType.valueOf(CommonTriggerEvents.DIR2_RELEASED);
    }

    private StringType convertToPressedCommand(Command command, SwitchMode switchMode) {
        if (command instanceof StringType) {
            return (StringType) command;
        } else if (command instanceof OnOffType) {
            switch (switchMode) {
                case RockerSwitch:
                    return (command == OnOffType.ON) ? StringType.valueOf(CommonTriggerEvents.DIR1_PRESSED)
                            : StringType.valueOf(CommonTriggerEvents.DIR2_PRESSED);
                case ToggleDir1:
                    return StringType.valueOf(CommonTriggerEvents.DIR1_PRESSED);
                case ToggleDir2:
                    return StringType.valueOf(CommonTriggerEvents.DIR2_PRESSED);
                default:
                    return null;
            }
        } else if (command instanceof UpDownType) {
            switch (switchMode) {
                case RockerSwitch:
                    return (command == UpDownType.UP) ? StringType.valueOf(CommonTriggerEvents.DIR1_PRESSED)
                            : StringType.valueOf(CommonTriggerEvents.DIR2_PRESSED);
                case ToggleDir1:
                    return StringType.valueOf(CommonTriggerEvents.DIR1_PRESSED);
                case ToggleDir2:
                    return StringType.valueOf(CommonTriggerEvents.DIR2_PRESSED);
                default:
                    return null;
            }
        } else if (command instanceof StopMoveType) {
            if (command == StopMoveType.STOP) {
                return lastTriggerEvent;
            }
        }

        return null;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // We must have a valid sendingEEPType and sender id to send commands
        if (sendingEEPType == null || senderId == null || command == RefreshType.REFRESH) {
            return;
        }

        String channelId = channelUID.getId();
        Channel channel = getThing().getChannel(channelUID);
        if (channel == null) {
            return;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String channelTypeId = (channelTypeUID != null) ? channelTypeUID.getId() : "";
        if (channelTypeId.contains("Listener")) {
            return;
        }

        EnOceanChannelVirtualRockerSwitchConfig channelConfig = channel.getConfiguration()
                .as(EnOceanChannelVirtualRockerSwitchConfig.class);

        StringType result = convertToPressedCommand(command, channelConfig.getSwitchMode());

        if (result != null) {
            lastTriggerEvent = result;

            EEP eep = EEPFactory.createEEP(sendingEEPType);
            if (eep.setSenderId(senderId).setDestinationId(destinationId).convertFromCommand(channelId, channelTypeId,
                    result, id -> this.getCurrentState(id), channel.getConfiguration()).hasData()) {
                BasePacket press = eep.setSuppressRepeating(getConfiguration().suppressRepeating).getERP1Message();

                getBridgeHandler().sendMessage(press, null);

                if (channelConfig.duration > 0) {
                    releaseFuture = scheduler.schedule(() -> {
                        if (eep.convertFromCommand(channelId, channelTypeId, convertToReleasedCommand(lastTriggerEvent),
                                id -> this.getCurrentState(id), channel.getConfiguration()).hasData()) {
                            BasePacket release = eep.getERP1Message();
                            getBridgeHandler().sendMessage(release, null);
                        }
                    }, channelConfig.duration, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    @Override
    public void handleRemoval() {
        if (releaseFuture != null && !releaseFuture.isDone()) {
            releaseFuture.cancel(true);
        }

        releaseFuture = null;
        super.handleRemoval();
    }
}
