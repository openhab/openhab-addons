/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.AutoUpdatePolicy;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT lock, following the https://www.home-assistant.io/integrations/lock.mqtt specification.
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Support OPEN, full state, and optimistic mode.
 */
@NonNullByDefault
public class Lock extends AbstractComponent<Lock.ChannelConfiguration> {
    public static final String LOCK_CHANNEL_ID = "lock";
    public static final String STATE_CHANNEL_ID = "state";

    public static final String PAYLOAD_LOCK = "LOCK";
    public static final String PAYLOAD_UNLOCK = "UNLOCK";
    public static final String PAYLOAD_OPEN = "OPEN";

    private static final Map<String, String> COMMAND_LABELS = Map.of(PAYLOAD_LOCK, "@text/command.lock.lock",
            PAYLOAD_UNLOCK, "@text/command.lock.unlock", PAYLOAD_OPEN, "@text/command.lock.open");

    public static final String STATE_JAMMED = "JAMMED";
    public static final String STATE_LOCKED = "LOCKED";
    public static final String STATE_LOCKING = "LOCKING";
    public static final String STATE_UNLOCKED = "UNLOCKED";
    public static final String STATE_UNLOCKING = "UNLOCKING";

    private static final Map<String, String> STATE_LABELS = Map.of(STATE_JAMMED, "@text/state.lock.jammed",
            STATE_LOCKED, "@text/state.lock.locked", STATE_LOCKING, "@text/state.lock.locking", STATE_UNLOCKED,
            "@text/state.lock.unlocked", STATE_UNLOCKING, "@text/state.lock.unlocking");

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Lock");
        }

        protected boolean optimistic = false;

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("state_topic")
        protected String stateTopic = "";
        @SerializedName("payload_lock")
        protected String payloadLock = PAYLOAD_LOCK;
        @SerializedName("payload_unlock")
        protected String payloadUnlock = PAYLOAD_UNLOCK;
        @SerializedName("payload_open")
        protected @Nullable String payloadOpen;
        @SerializedName("state_jammed")
        protected String stateJammed = STATE_JAMMED;
        @SerializedName("state_locked")
        protected String stateLocked = STATE_LOCKED;
        @SerializedName("state_locking")
        protected String stateLocking = STATE_LOCKING;
        @SerializedName("state_unlocked")
        protected String stateUnlocked = STATE_UNLOCKED;
        @SerializedName("state_unlocking")
        protected String stateUnlocking = STATE_UNLOCKING;
    }

    private boolean optimistic = false;
    private OnOffValue lockValue;
    private TextValue stateValue;

    public Lock(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        this.optimistic = channelConfiguration.optimistic || channelConfiguration.stateTopic.isBlank();

        lockValue = new OnOffValue(new String[] { channelConfiguration.stateLocked },
                new String[] { channelConfiguration.stateUnlocked, channelConfiguration.stateLocking,
                        channelConfiguration.stateUnlocking, channelConfiguration.stateJammed },
                channelConfiguration.payloadLock, channelConfiguration.payloadUnlock);

        buildChannel(LOCK_CHANNEL_ID, ComponentChannelType.SWITCH, lockValue, "Lock",
                componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).commandFilter(command -> {
                    if (command instanceof OnOffType) {
                        autoUpdate(command.equals(OnOffType.ON));
                    }
                    return true;
                }).build();

        Map<String, String> commands = new LinkedHashMap<>();
        commands.put(PAYLOAD_LOCK, channelConfiguration.payloadLock);
        commands.put(PAYLOAD_UNLOCK, channelConfiguration.payloadUnlock);
        String payloadOpen = channelConfiguration.payloadOpen;
        if (payloadOpen != null) {
            commands.put(PAYLOAD_OPEN, payloadOpen);
        }
        Map<String, String> states = new LinkedHashMap<>();
        states.put(channelConfiguration.stateLocked, STATE_LOCKED);
        states.put(channelConfiguration.stateUnlocked, STATE_UNLOCKED);
        states.put(channelConfiguration.stateLocking, STATE_LOCKING);
        states.put(channelConfiguration.stateUnlocking, STATE_UNLOCKING);
        states.put(channelConfiguration.stateJammed, STATE_JAMMED);
        stateValue = new TextValue(states, commands, STATE_LABELS, COMMAND_LABELS);

        buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING, stateValue, "State",
                componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .isAdvanced(true).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).commandFilter(command -> {
                    if (command instanceof StringType stringCommand) {
                        if (stringCommand.toString().equals(PAYLOAD_LOCK)) {
                            autoUpdate(true);
                        } else if (stringCommand.toString().equals(PAYLOAD_UNLOCK)
                                || (channelConfiguration.payloadOpen != null
                                        && stringCommand.toString().equals(PAYLOAD_OPEN))) {
                            autoUpdate(false);
                        }
                    }
                    return true;
                }).build();

        finalizeChannels();
    }

    private void autoUpdate(boolean locking) {
        if (!optimistic) {
            return;
        }

        final ChannelUID lockChannelUID = buildChannelUID(LOCK_CHANNEL_ID);
        final ChannelUID stateChannelUID = buildChannelUID(STATE_CHANNEL_ID);
        final ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        if (locking) {
            stateValue.update(new StringType(STATE_LOCKED));
            updateListener.updateChannelState(stateChannelUID, stateValue.getChannelState());
            lockValue.update(OnOffType.ON);
            updateListener.updateChannelState(lockChannelUID, OnOffType.ON);
        } else {
            stateValue.update(new StringType(STATE_UNLOCKED));
            updateListener.updateChannelState(stateChannelUID, stateValue.getChannelState());
            lockValue.update(OnOffType.OFF);
            updateListener.updateChannelState(lockChannelUID, OnOffType.OFF);
        }
    }
}
