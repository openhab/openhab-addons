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
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.RWConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.AutoUpdatePolicy;

/**
 * A MQTT lock, following the https://www.home-assistant.io/integrations/lock.mqtt specification.
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Support OPEN, full state, and optimistic mode.
 */
@NonNullByDefault
public class Lock extends AbstractComponent<Lock.Configuration> {
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

    public static class Configuration extends EntityConfiguration implements RWConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Lock");
        }

        @Nullable
        Value getCommandTemplate() {
            return getOptionalValue("command_template");
        }

        String getPayloadLock() {
            return getString("payload_lock");
        }

        String getPayloadUnlock() {
            return getString("payload_unlock");
        }

        @Nullable
        String getPayloadOpen() {
            return getOptionalString("payload_open");
        }

        String getPayloadReset() {
            return getString("payload_reset");
        }

        String getStateJammed() {
            return getString("state_jammed");
        }

        String getStateLocked() {
            return getString("state_locked");
        }

        String getStateLocking() {
            return getString("state_locking");
        }

        String getStateOpen() {
            return getString("state_open");
        }

        String getStateOpening() {
            return getString("state_opening");
        }

        String getStateUnlocked() {
            return getString("state_unlocked");
        }

        String getStateUnlocking() {
            return getString("state_unlocking");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }
    }

    private boolean optimistic = false;
    private OnOffValue lockValue;
    private TextValue stateValue;

    public Lock(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        String stateTopic = config.getStateTopic();
        String commandTopic = config.getCommandTopic();
        Value commandTemplate = config.getCommandTemplate();
        this.optimistic = config.isOptimistic() || stateTopic == null;

        String payloadLock = config.getPayloadLock();
        String payloadUnlock = config.getPayloadUnlock();
        String payloadOpen = config.getPayloadOpen();
        String stateLocked = config.getStateLocked();
        String stateUnlocked = config.getStateUnlocked();
        String stateLocking = config.getStateLocking();
        String stateUnlocking = config.getStateUnlocking();
        String stateJammed = config.getStateJammed();

        lockValue = new OnOffValue(new String[] { config.getStateLocked() },
                new String[] { stateLocked, stateUnlocked, stateLocking, stateUnlocking, stateJammed }, payloadLock,
                payloadUnlock);

        buildChannel(LOCK_CHANNEL_ID, ComponentChannelType.SWITCH, lockValue, "Lock",
                componentContext.getUpdateListener()).stateTopic(stateTopic, config.getValueTemplate())
                .commandTopic(commandTopic, config.isRetain(), config.getQos(), commandTemplate)
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).commandFilter(command -> {
                    if (command instanceof OnOffType) {
                        autoUpdate(command.equals(OnOffType.ON));
                    }
                    return true;
                }).build();

        Map<String, String> commands = new LinkedHashMap<>();
        commands.put(PAYLOAD_LOCK, payloadLock);
        commands.put(PAYLOAD_UNLOCK, payloadUnlock);
        if (payloadOpen != null) {
            commands.put(PAYLOAD_OPEN, payloadOpen);
        }
        Map<String, String> states = new LinkedHashMap<>();
        states.put(stateLocked, STATE_LOCKED);
        states.put(stateUnlocked, STATE_UNLOCKED);
        states.put(stateLocking, STATE_LOCKING);
        states.put(stateUnlocking, STATE_UNLOCKING);
        states.put(stateJammed, STATE_JAMMED);
        stateValue = new TextValue(states, commands, STATE_LABELS, COMMAND_LABELS);

        buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING, stateValue, "State",
                componentContext.getUpdateListener()).stateTopic(stateTopic, config.getValueTemplate())
                .commandTopic(commandTopic, config.isRetain(), config.getQos(), commandTemplate).isAdvanced(true)
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).commandFilter(command -> {
                    if (command instanceof StringType stringCommand) {
                        if (stringCommand.toString().equals(PAYLOAD_LOCK)) {
                            autoUpdate(true);
                        } else if (stringCommand.toString().equals(PAYLOAD_UNLOCK)
                                || (payloadOpen != null && stringCommand.toString().equals(PAYLOAD_OPEN))) {
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
        final ChannelStateUpdateListener updateListener = componentContext.getUpdateListener();

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
