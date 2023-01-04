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
package org.openhab.binding.nuki.internal.handler;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nuki.internal.configuration.NukiDeviceConfiguration;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.constants.OpenerAction;
import org.openhab.binding.nuki.internal.dataexchange.BridgeLockActionResponse;
import org.openhab.binding.nuki.internal.dto.BridgeApiDeviceStateDto;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Thing handler for Nuki Opener
 *
 * @author Jan Vybíral - Initial contribution
 */
@NonNullByDefault
public class NukiOpenerHandler extends AbstractNukiDeviceHandler<NukiDeviceConfiguration> {

    public NukiOpenerHandler(Thing thing, boolean readOnly) {
        super(thing, readOnly);
    }

    private volatile Instant lastRingAction = Instant.EPOCH;

    @Override
    public void refreshState(BridgeApiDeviceStateDto state) {
        updateState(NukiBindingConstants.CHANNEL_OPENER_LOW_BATTERY, state.isBatteryCritical(), OnOffType::from);
        updateState(NukiBindingConstants.CHANNEL_OPENER_STATE, state.getState(), DecimalType::new);
        updateState(NukiBindingConstants.CHANNEL_OPENER_MODE, state.getMode(), DecimalType::new);
        updateState(NukiBindingConstants.CHANNEL_OPENER_RING_ACTION_TIMESTAMP, state.getRingactionTimestamp(),
                this::toDateTime);

        if (Objects.equals(state.getRingactionState(), true)
                && Duration.between(lastRingAction, Instant.now()).getSeconds() > 30) {
            triggerChannel(NukiBindingConstants.CHANNEL_OPENER_RING_ACTION_STATE, NukiBindingConstants.EVENT_RINGING);
            lastRingAction = Instant.now();
        }
    }

    @Override
    protected int getDeviceType() {
        return NukiBindingConstants.DEVICE_OPENER;
    }

    @Override
    protected boolean doHandleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case NukiBindingConstants.CHANNEL_OPENER_STATE:
                if (command instanceof DecimalType) {
                    OpenerAction action = OpenerAction.fromAction(((DecimalType) command).intValue());
                    if (action != null) {
                        return withHttpClient(client -> {
                            BridgeLockActionResponse response = client.getOpenerAction(configuration.nukiId, action);
                            return handleResponse(response, channelUID.getAsString(), command.toString());
                        }, false);
                    }
                }
                break;
        }
        return false;
    }

    @Override
    protected Class<NukiDeviceConfiguration> getConfigurationClass() {
        return NukiDeviceConfiguration.class;
    }
}
