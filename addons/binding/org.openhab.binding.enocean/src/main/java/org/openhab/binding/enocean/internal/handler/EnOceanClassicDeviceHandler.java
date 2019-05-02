/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.enocean.internal.config.EnOceanActuatorConfig;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchConfigBase.SwitchMode;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRockerSwitchListenerConfig;
import org.openhab.binding.enocean.internal.config.EnOceanChannelVirtualRockerSwitchConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.binding.enocean.internal.messages.ESP3Packet;

/**
 *
 * @author Daniel Weber - Initial contribution
 *         This class defines base functionality for sending eep messages. This class extends EnOceanBaseSensorHandler
 *         class as most actuator things send status or response messages, too.
 */
public class EnOceanClassicDeviceHandler extends EnOceanBaseActuatorHandler {

    // List of thing types which support sending of eep messages
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_CLASSICDEVICE));

    private Hashtable<String, EnOceanChannelRockerSwitchListenerConfig> channelConfigById;
    private String currentEnOceanId;
    private StringType lastTriggerEvent = StringType.valueOf(CommonTriggerEvents.DIR1_PRESSED);

    public EnOceanClassicDeviceHandler(Thing thing) {
        super(thing);

        channelConfigById = new Hashtable<>();
    }

    @Override
    void initializeConfig() {
        super.initializeConfig();
        ((EnOceanActuatorConfig) config).broadcastMessages = true;
        ((EnOceanActuatorConfig) config).enoceanId = EMPTYENOCEANID;
    }

    @Override
    public long getSenderIdToListenTo() {
        return Long.parseLong(currentEnOceanId, 16);
    }

    @Override
    public void channelLinked(@NonNull ChannelUID channelUID) {
        super.channelLinked(channelUID);

        // if linked channel is a listening channel => put listener
        String id = channelUID.getId();
        Channel channel = getThing().getChannel(id);
        addListener(channel);
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        // it seems that there does not exist a channel update callback
        // => remove all listeners and add them again
        while (!channelConfigById.isEmpty()) {
            removeListener(getThing().getChannel(channelConfigById.keys().nextElement()));
        }

        getLinkedChannels().forEach(c -> {
            if (!addListener(c)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong channel configuration");
            }
        });
    }

    @Override
    public void channelUnlinked(@NonNull ChannelUID channelUID) {
        super.channelUnlinked(channelUID);

        // if unlinked channel is listening channel => remove listener
        String id = channelUID.getId();
        Channel channel = getThing().getChannel(id);
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
                Long.parseLong(config.enoceanId, 16);
                channelConfigById.put(channel.getUID().getId(), config);
                currentEnOceanId = config.enoceanId;
                getBridgeHandler().addPacketListener(this);

                return true;
            } catch (Exception e) {

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
            String channelId = channel.getUID().getId();

            if (channelConfigById.containsKey(channelId)) {
                currentEnOceanId = channelConfigById.get(channelId).enoceanId;
                channelConfigById.remove(channelId);
                getBridgeHandler().removePacketListener(this);
            }
        }
    }

    @Override
    protected State getCurrentState(String channelId) {
        // Always use the same channelId of CHANNEL_VIRTUALSWITCHA
        return super.getCurrentState(CHANNEL_VIRTUALSWITCHA);
    }

    @Override
    protected void setCurrentState(String channelId, State state) {
        // Always use the same channelId of CHANNEL_VIRTUALSWITCHA
        super.setCurrentState(CHANNEL_VIRTUALSWITCHA, state);
    }

    @Override
    protected Predicate<Channel> stateFilter(EEPType eepType, byte[] senderId) {
        return c -> {
            ChannelTypeUID channelTypeUID = c.getChannelTypeUID();
            String id = channelTypeUID == null ? "" : channelTypeUID.getId();

            return ChannelKind.STATE.equals(c.getKind()) && id.startsWith(CHANNEL_ROCKERSWITCHLISTENER_START)
                    && c.getConfiguration().as(EnOceanChannelRockerSwitchListenerConfig.class).enoceanId
                            .equalsIgnoreCase(HexUtils.bytesToHex(senderId));
        };
    }

    @Override
    protected Predicate<Channel> triggerFilter(EEPType eepType, byte[] senderId) {
        return c -> {
            ChannelTypeUID channelTypeUID = c.getChannelTypeUID();
            String id = channelTypeUID == null ? "" : channelTypeUID.getId();

            return ChannelKind.TRIGGER.equals(c.getKind()) && id.startsWith(CHANNEL_ROCKERSWITCHLISTENER_START)
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
        Channel channel = getThing().getChannel(channelId);
        // check if the channel is linked otherwise do nothing
        if (channel == null || !getLinkedChannels().contains(channel)) {
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
            eep.convertFromCommand(channelId, channelTypeId, result, channelState, channel.getConfiguration());

            if (eep.hasData()) {
                ESP3Packet press = eep.setSenderId(senderId).setDestinationId(destinationId)
                        .setSuppressRepeating(getConfiguration().suppressRepeating).getERP1Message();

                getBridgeHandler().sendMessage(press, null);

                if (channelConfig.duration > 0) {
                    scheduler.schedule(() -> {
                        eep.convertFromCommand(channelId, channelTypeId, convertToReleasedCommand(lastTriggerEvent),
                                channelState, channel.getConfiguration());

                        ESP3Packet release = eep.getERP1Message();

                        getBridgeHandler().sendMessage(release, null);

                    }, channelConfig.duration, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    @Override
    public void handleRemoval() {
        for (Channel channel : getLinkedChannels()) {
            removeListener(channel);
        }
        currentEnOceanId = EMPTYENOCEANID;
        super.handleRemoval();
    }
}
