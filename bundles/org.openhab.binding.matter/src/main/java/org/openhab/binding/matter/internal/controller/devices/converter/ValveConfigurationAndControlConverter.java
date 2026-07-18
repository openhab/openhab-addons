/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster.ValveFaultBitmap;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster.ValveStateEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * A converter for translating {@link ValveConfigurationAndControlCluster} events and attributes to openHAB channels and
 * back again.
 *
 * @author Jason Hubbard - Initial contribution
 */
@NonNullByDefault
public class ValveConfigurationAndControlConverter extends GenericConverter<ValveConfigurationAndControlCluster> {

    private final boolean levelSupported;

    public ValveConfigurationAndControlConverter(ValveConfigurationAndControlCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
        this.levelSupported = cluster.featureMap != null && cluster.featureMap.level;
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();

        Channel stateChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_VALVE_STATE), CoreItemFactory.SWITCH)
                .withType(CHANNEL_VALVE_STATE).build();
        channels.put(stateChannel, null);

        Channel currentStateChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_VALVE_CURRENT_STATE), CoreItemFactory.NUMBER)
                .withType(CHANNEL_VALVE_CURRENT_STATE).build();
        channels.put(currentStateChannel, null);

        if (levelSupported) {
            Channel levelChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, CHANNEL_ID_VALVE_LEVEL), CoreItemFactory.DIMMER)
                    .withType(CHANNEL_VALVE_LEVEL).build();
            channels.put(levelChannel, null);
        }

        Channel durationChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_VALVE_DURATION), CoreItemFactory.NUMBER + ":Time")
                .withType(CHANNEL_VALVE_DURATION).build();
        channels.put(durationChannel, null);

        Channel remainingChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_VALVE_REMAINING_DURATION),
                        CoreItemFactory.NUMBER + ":Time")
                .withType(CHANNEL_VALVE_REMAINING_DURATION).build();
        channels.put(remainingChannel, null);

        Channel faultChannel = ChannelBuilder.create(new ChannelUID(channelGroupUID, CHANNEL_ID_VALVE_FAULT), null)
                .withType(CHANNEL_VALVE_FAULT).withKind(ChannelKind.TRIGGER).build();
        channels.put(faultChannel, null);

        return channels;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        switch (channelId) {
            case CHANNEL_ID_VALVE_STATE:
                if (command instanceof OnOffType onOffType) {
                    if (onOffType == OnOffType.ON) {
                        sendClusterCommand(ValveConfigurationAndControlCluster.open(null, null));
                    } else {
                        sendClusterCommand(ValveConfigurationAndControlCluster.close());
                    }
                }
                break;
            case CHANNEL_ID_VALVE_LEVEL:
                if (command instanceof PercentType percentType) {
                    if (percentType.intValue() == 0) {
                        sendClusterCommand(ValveConfigurationAndControlCluster.close());
                    } else {
                        sendClusterCommand(ValveConfigurationAndControlCluster.open(null, percentType.intValue()));
                    }
                } else if (command instanceof OnOffType onOffType) {
                    if (onOffType == OnOffType.ON) {
                        sendClusterCommand(ValveConfigurationAndControlCluster.open(null, 100));
                    } else {
                        sendClusterCommand(ValveConfigurationAndControlCluster.close());
                    }
                }
                break;
            case CHANNEL_ID_VALVE_DURATION:
                Integer seconds = durationSeconds(command);
                if (seconds != null) {
                    handler.writeAttribute(endpointNumber, ValveConfigurationAndControlCluster.CLUSTER_NAME,
                            ValveConfigurationAndControlCluster.ATTRIBUTE_DEFAULT_OPEN_DURATION,
                            String.valueOf(seconds));
                }
                break;
            default:
                break;
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case ValveConfigurationAndControlCluster.ATTRIBUTE_CURRENT_STATE:
                updateValveState(message.value instanceof ValveStateEnum valveState ? valveState : null);
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_TARGET_STATE:
                // Some valves report only TargetState (the requested position) and do not continuously
                // report CurrentState. Reflect a non-null TargetState so the state channel still tracks.
                if (message.value instanceof ValveStateEnum valveState) {
                    updateValveState(valveState);
                }
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_CURRENT_LEVEL:
                if (levelSupported && message.value instanceof Number number) {
                    updateState(CHANNEL_ID_VALVE_LEVEL, new PercentType(number.intValue()));
                }
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_REMAINING_DURATION:
                updateDuration(CHANNEL_ID_VALVE_REMAINING_DURATION, message.value);
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_DEFAULT_OPEN_DURATION:
                updateDuration(CHANNEL_ID_VALVE_DURATION, message.value);
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_VALVE_FAULT:
                if (message.value instanceof ValveFaultBitmap faultBitmap) {
                    triggerFault(faultBitmap);
                }
                break;
            default:
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
        if ("valveFault".equals(message.path.eventName) && message.events != null) {
            for (var event : message.events) {
                if (event.data instanceof ValveConfigurationAndControlCluster.ValveFault valveFault) {
                    triggerFault(valveFault.valveFault);
                }
            }
        }
    }

    @Override
    public void initState() {
        // Prefer CurrentState (actual position); fall back to TargetState for valves that only report the latter.
        updateValveState(initializingCluster.currentState != null ? initializingCluster.currentState
                : initializingCluster.targetState);
        if (levelSupported && initializingCluster.currentLevel != null) {
            updateState(CHANNEL_ID_VALVE_LEVEL, new PercentType(initializingCluster.currentLevel));
        }
        updateDuration(CHANNEL_ID_VALVE_REMAINING_DURATION, initializingCluster.remainingDuration);
        updateDuration(CHANNEL_ID_VALVE_DURATION, initializingCluster.defaultOpenDuration);
    }

    private void sendClusterCommand(ClusterCommand command) {
        handler.sendClusterCommand(endpointNumber, ValveConfigurationAndControlCluster.CLUSTER_NAME, command);
    }

    /**
     * Maps a valve state (CurrentState or TargetState) to the on/off state channel. A {@code TRANSITIONING} state does
     * not update the channel, so it keeps its last stable value rather than flipping to a wrong terminal value
     * mid-move.
     */
    private void updateValveState(@Nullable ValveStateEnum valveState) {
        if (valveState == null) {
            updateState(CHANNEL_ID_VALVE_STATE, UnDefType.UNDEF);
            updateState(CHANNEL_ID_VALVE_CURRENT_STATE, UnDefType.UNDEF);
            return;
        }
        // The read-only current-state channel exposes all states, including TRANSITIONING, for notifications.
        updateState(CHANNEL_ID_VALVE_CURRENT_STATE, new DecimalType(valveState.getValue()));
        switch (valveState) {
            case OPEN:
                updateState(CHANNEL_ID_VALVE_STATE, OnOffType.ON);
                break;
            case CLOSED:
                updateState(CHANNEL_ID_VALVE_STATE, OnOffType.OFF);
                break;
            case TRANSITIONING:
            default:
                // Keep the switch at its last stable value rather than flipping mid-move.
                break;
        }
    }

    private void updateDuration(String channelId, @Nullable Object value) {
        State state = value instanceof Number number ? new QuantityType<>(number.longValue(), Units.SECOND)
                : UnDefType.UNDEF;
        updateState(channelId, state);
    }

    private @Nullable Integer durationSeconds(Command command) {
        if (command instanceof QuantityType<?> quantityType) {
            QuantityType<?> seconds = quantityType.toUnit(Units.SECOND);
            return seconds == null ? null : seconds.intValue();
        }
        if (command instanceof DecimalType decimalType) {
            return decimalType.intValue();
        }
        return null;
    }

    /**
     * Fires one trigger event per set fault bit, so each payload is a single fault name that rules can match
     * directly, rather than a combined comma-separated payload.
     */
    private void triggerFault(@Nullable ValveFaultBitmap fault) {
        if (fault == null) {
            return;
        }
        if (fault.generalFault) {
            triggerChannel(CHANNEL_ID_VALVE_FAULT, "generalFault");
        }
        if (fault.blocked) {
            triggerChannel(CHANNEL_ID_VALVE_FAULT, "blocked");
        }
        if (fault.leaking) {
            triggerChannel(CHANNEL_ID_VALVE_FAULT, "leaking");
        }
        if (fault.notConnected) {
            triggerChannel(CHANNEL_ID_VALVE_FAULT, "notConnected");
        }
        if (fault.shortCircuit) {
            triggerChannel(CHANNEL_ID_VALVE_FAULT, "shortCircuit");
        }
        if (fault.currentExceeded) {
            triggerChannel(CHANNEL_ID_VALVE_FAULT, "currentExceeded");
        }
    }
}
