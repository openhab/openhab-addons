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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import org.openhab.core.library.types.DateTimeType;
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

    // Matter timestamps count from 2000-01-01 UTC, which is the Unix epoch plus 10957 days.
    private static final long MATTER_EPOCH_OFFSET_SECONDS = 946684800L;

    private final boolean levelSupported;
    private final boolean timeSyncSupported;
    private final int levelStep;
    private final Set<String> activeFaults = new LinkedHashSet<>();
    private @Nullable Long autoCloseTime;
    private @Nullable Instant derivedCloseTime;

    public ValveConfigurationAndControlConverter(ValveConfigurationAndControlCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
        this.levelSupported = cluster.featureMap != null && cluster.featureMap.level;
        this.timeSyncSupported = cluster.featureMap != null && cluster.featureMap.timeSync;
        // LevelStep is a fixed attribute, so the value read at startup stays valid. It defaults to 1 (every
        // level supported) when the valve does not have the attribute.
        this.levelStep = cluster.levelStep == null ? 1 : cluster.levelStep;
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

        Channel targetStateChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_VALVE_TARGET_STATE), CoreItemFactory.NUMBER)
                .withType(CHANNEL_VALVE_TARGET_STATE).build();
        channels.put(targetStateChannel, null);

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

        Channel closeTimeChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_VALVE_CLOSE_TIME), CoreItemFactory.DATETIME)
                .withType(CHANNEL_VALVE_CLOSE_TIME).build();
        channels.put(closeTimeChannel, null);

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
                    int level = supportedLevel(percentType.intValue());
                    if (level == 0) {
                        sendClusterCommand(ValveConfigurationAndControlCluster.close());
                    } else {
                        sendClusterCommand(ValveConfigurationAndControlCluster.open(null, level));
                    }
                } else if (command instanceof OnOffType onOffType) {
                    if (onOffType == OnOffType.ON) {
                        sendClusterCommand(ValveConfigurationAndControlCluster.open(null, null));
                    } else {
                        sendClusterCommand(ValveConfigurationAndControlCluster.close());
                    }
                }
                break;
            case CHANNEL_ID_VALVE_DURATION:
                Integer seconds = durationSeconds(command);
                if (seconds != null && seconds >= 0) {
                    // DefaultOpenDuration is constrained to a minimum of 1, and null -- not 0 -- is the value that
                    // means no default duration is set, so the valve stays open until it is closed. Send a 0 command
                    // as that null write rather than an out-of-constraint 0 the device should reject.
                    handler.writeAttribute(endpointNumber, ValveConfigurationAndControlCluster.CLUSTER_NAME,
                            ValveConfigurationAndControlCluster.ATTRIBUTE_DEFAULT_OPEN_DURATION,
                            seconds == 0 ? "null" : String.valueOf(seconds));
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
                updateTargetState(message.value instanceof ValveStateEnum valveState ? valveState : null);
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_CURRENT_LEVEL:
                if (levelSupported) {
                    updateState(CHANNEL_ID_VALVE_LEVEL,
                            message.value instanceof Number number ? new PercentType(number.intValue())
                                    : UnDefType.UNDEF);
                }
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_AUTO_CLOSE_TIME:
                autoCloseTime = message.value instanceof Number autoClose ? autoClose.longValue() : null;
                updateCloseTime();
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_REMAINING_DURATION:
                derivedCloseTime = deriveCloseTime(
                        message.value instanceof Number remaining ? remaining.intValue() : null);
                updateCloseTime();
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_DEFAULT_OPEN_DURATION:
                updateDuration(CHANNEL_ID_VALVE_DURATION, message.value);
                break;
            case ValveConfigurationAndControlCluster.ATTRIBUTE_VALVE_FAULT:
                // The attribute and the event are independently optional, so a valve may report only one of them.
                triggerFault(message.value instanceof ValveFaultBitmap faultBitmap ? faultBitmap : null);
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
        updateValveState(initializingCluster.currentState);
        updateTargetState(initializingCluster.targetState);
        if (levelSupported) {
            updateState(CHANNEL_ID_VALVE_LEVEL, initializingCluster.currentLevel == null ? UnDefType.UNDEF
                    : new PercentType(initializingCluster.currentLevel));
        }
        autoCloseTime = initializingCluster.autoCloseTime == null ? null
                : initializingCluster.autoCloseTime.longValue();
        derivedCloseTime = deriveCloseTime(initializingCluster.remainingDuration);
        updateCloseTime();
        updateDuration(CHANNEL_ID_VALVE_DURATION, initializingCluster.defaultOpenDuration);
        // Seed the faults that are already active, without firing them: the ValveFault event carries the whole
        // bitmap, so an unseeded set would re-report them on the first fault change after startup.
        activeFaults.addAll(faultNames(initializingCluster.valveFault));
    }

    private void sendClusterCommand(ClusterCommand command) {
        handler.sendClusterCommand(endpointNumber, ValveConfigurationAndControlCluster.CLUSTER_NAME, command);
    }

    /**
     * Maps CurrentState (the actual valve position) to the switch and the read-only current-state channel. A
     * {@code TRANSITIONING} state does not update the switch, so it keeps its last stable value rather than flipping to
     * a wrong terminal value mid-move.
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

    /**
     * Maps TargetState (the state the valve is moving toward) to the read-only target-state channel. Per the cluster
     * definition the target is a terminal state — {@code OPEN} or {@code CLOSED} — and is null once the change is
     * either done or failed, which maps to {@code UNDEF}. Combined with CurrentState this gives the direction of a
     * move that {@code TRANSITIONING} alone does not: current {@code TRANSITIONING} plus target {@code OPEN} is
     * opening, plus target {@code CLOSED} is closing.
     */
    private void updateTargetState(@Nullable ValveStateEnum targetState) {
        updateState(CHANNEL_ID_VALVE_TARGET_STATE,
                targetState == null ? UnDefType.UNDEF : new DecimalType(targetState.getValue()));
    }

    /**
     * Publishes the time at which the valve will close.
     *
     * A valve with the TimeSync feature reports AutoCloseTime, which is the valve's own answer, so it is preferred
     * when it is set. The feature only means the valve can synchronize time, not that it currently has UTC time, and
     * AutoCloseTime is null until it does -- so a null there falls back to the same derivation used by a valve without
     * the feature. That derives the time from RemainingDuration, which the cluster reports only when it becomes or
     * stops being null, when it reaches 0, when it increases, or when the closing time changes -- published as a
     * remaining duration it would sit at a stale value throughout the countdown, whereas a close time stays correct
     * without being reported at all.
     */
    private void updateCloseTime() {
        Long autoClose = autoCloseTime;
        Instant derived = derivedCloseTime;
        State state = UnDefType.UNDEF;
        if (timeSyncSupported && autoClose != null) {
            state = new DateTimeType(
                    Instant.ofEpochSecond(MATTER_EPOCH_OFFSET_SECONDS).plus(autoClose, ChronoUnit.MICROS));
        } else if (derived != null) {
            state = new DateTimeType(derived);
        }
        updateState(CHANNEL_ID_VALVE_CLOSE_TIME, state);
    }

    /**
     * Derives the close time as RemainingDuration is reported. The duration is an offset from that report, so it is
     * converted once, here, rather than re-based on a later clock reading.
     */
    private @Nullable Instant deriveCloseTime(@Nullable Integer remaining) {
        return remaining == null || remaining <= 0 ? null : Instant.now().plusSeconds(remaining);
    }

    private void updateDuration(String channelId, @Nullable Object value) {
        State state = value instanceof Number number ? new QuantityType<>(number.longValue(), Units.SECOND)
                : UnDefType.UNDEF;
        updateState(channelId, state);
    }

    /**
     * Rounds a level to one the valve actually supports, which is a multiple of LevelStep, or 100, which is always
     * supported regardless of the step. Sending an unsupported level makes the valve reject the Open command with
     * CONSTRAINT_ERROR, so a 50% command to a valve with a step of 15 would otherwise do nothing at all.
     */
    private int supportedLevel(int level) {
        if (levelStep <= 1) {
            return level;
        }
        int stepped = Math.min((level + levelStep / 2) / levelStep * levelStep, 100);
        return 100 - level < Math.abs(level - stepped) ? 100 : stepped;
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
     * Fires one trigger event per newly set fault bit, so each payload is a single fault name that rules can match
     * directly, rather than a combined comma-separated payload.
     *
     * The ValveFault attribute and event each carry the whole fault bitmap rather than just what changed, so only bits
     * that were not already set are fired. That stops a valve reporting both from firing twice, an unrelated fault
     * appearing, or one of several clearing, from re-firing faults that have already been reported.
     */
    private void triggerFault(@Nullable ValveFaultBitmap fault) {
        Set<String> faults = faultNames(fault);
        for (String name : faults) {
            if (activeFaults.add(name)) {
                triggerChannel(CHANNEL_ID_VALVE_FAULT, name);
            }
        }
        activeFaults.retainAll(faults);
    }

    /**
     * Maps a fault bitmap to the names of the bits that are set, in a stable order.
     */
    private Set<String> faultNames(@Nullable ValveFaultBitmap fault) {
        Set<String> faults = new LinkedHashSet<>();
        if (fault != null) {
            if (fault.generalFault) {
                faults.add("generalFault");
            }
            if (fault.blocked) {
                faults.add("blocked");
            }
            if (fault.leaking) {
                faults.add("leaking");
            }
            if (fault.notConnected) {
                faults.add("notConnected");
            }
            if (fault.shortCircuit) {
                faults.add("shortCircuit");
            }
            if (fault.currentExceeded) {
                faults.add("currentExceeded");
            }
        }
        return faults;
    }
}
