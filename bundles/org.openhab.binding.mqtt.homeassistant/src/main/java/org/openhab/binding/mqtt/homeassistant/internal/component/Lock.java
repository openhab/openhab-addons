/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
        protected String payloadLock = "LOCK";
        @SerializedName("payload_unlock")
        protected String payloadUnlock = "UNLOCK";
        @SerializedName("payload_open")
        protected @Nullable String payloadOpen;
        @SerializedName("state_jammed")
        protected String stateJammed = "JAMMED";
        @SerializedName("state_locked")
        protected String stateLocked = "LOCKED";
        @SerializedName("state_locking")
        protected String stateLocking = "LOCKING";
        @SerializedName("state_unlocked")
        protected String stateUnlocked = "UNLOCKED";
        @SerializedName("state_unlocking")
        protected String stateUnlocking = "UNLOCKING";
    }

    private boolean optimistic = false;
    private OnOffValue lockValue;
    private TextValue stateValue;

    public Lock(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

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

        String[] commands;
        if (channelConfiguration.payloadOpen == null) {
            commands = new String[] { channelConfiguration.payloadLock, channelConfiguration.payloadUnlock, };
        } else {
            commands = new String[] { channelConfiguration.payloadLock, channelConfiguration.payloadUnlock,
                    channelConfiguration.payloadOpen };
        }
        stateValue = new TextValue(new String[] { channelConfiguration.stateJammed, channelConfiguration.stateLocked,
                channelConfiguration.stateLocking, channelConfiguration.stateUnlocked,
                channelConfiguration.stateUnlocking }, commands);
        buildChannel(STATE_CHANNEL_ID, ComponentChannelType.STRING, stateValue, "State",
                componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .isAdvanced(true).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).commandFilter(command -> {
                    if (command instanceof StringType stringCommand) {
                        if (stringCommand.toString().equals(channelConfiguration.payloadLock)) {
                            autoUpdate(true);
                        } else if (stringCommand.toString().equals(channelConfiguration.payloadUnlock)
                                || stringCommand.toString().equals(channelConfiguration.payloadOpen)) {
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
            stateValue.update(new StringType(channelConfiguration.stateLocked));
            updateListener.updateChannelState(stateChannelUID, stateValue.getChannelState());
            lockValue.update(OnOffType.ON);
            updateListener.updateChannelState(lockChannelUID, OnOffType.ON);
        } else {
            stateValue.update(new StringType(channelConfiguration.stateUnlocked));
            updateListener.updateChannelState(stateChannelUID, stateValue.getChannelState());
            lockValue.update(OnOffType.OFF);
            updateListener.updateChannelState(lockChannelUID, OnOffType.OFF);
        }
    }
}
