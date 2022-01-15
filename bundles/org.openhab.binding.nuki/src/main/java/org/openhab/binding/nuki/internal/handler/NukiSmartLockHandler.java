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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nuki.internal.configuration.NukiSmartLockConfiguration;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.constants.SmartLockAction;
import org.openhab.binding.nuki.internal.dataexchange.BridgeLockActionResponse;
import org.openhab.binding.nuki.internal.dto.BridgeApiDeviceStateDto;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link NukiSmartLockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Katter - Initial contribution
 * @contributer Christian Hoefler - Door sensor integration
 * @contributer Jan Vybíral - Refactoring, added more channels
 */
@NonNullByDefault
public class NukiSmartLockHandler extends AbstractNukiDeviceHandler<NukiSmartLockConfiguration> {

    public NukiSmartLockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void refreshState(BridgeApiDeviceStateDto state) {
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK,
                state.getState() == NukiBindingConstants.LOCK_STATES_LOCKED, OnOffType::from);
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_BATTERY_LEVEL, state.getBatteryChargeState(),
                DecimalType::new);
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_BATTERY_CHARGING, state.getBatteryCharging(),
                OnOffType::from);
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_LOW_BATTERY, state.isBatteryCritical(), OnOffType::from);
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_KEYPAD_LOW_BATTERY, state.getKeypadBatteryCritical(),
                OnOffType::from);
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_STATE, state.getState(), DecimalType::new);
        updateState(NukiBindingConstants.CHANNEL_SMARTLOCK_DOOR_STATE, state.getDoorsensorState(), DecimalType::new);
    }

    @Override
    protected int getDeviceType() {
        return this.configuration.deviceType;
    }

    @Override
    protected boolean doHandleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK:
                if (command instanceof OnOffType) {
                    final SmartLockAction action;

                    if (command == OnOffType.OFF) {
                        action = configuration.unlatch ? SmartLockAction.UNLATCH : SmartLockAction.UNLOCK;
                    } else {
                        action = SmartLockAction.LOCK;
                    }

                    withHttpClient(client -> {
                        BridgeLockActionResponse bridgeLockActionResponse = client
                                .getSmartLockAction(configuration.nukiId, action, getDeviceType());
                        handleResponse(bridgeLockActionResponse, channelUID.getAsString(), command.toString());
                    });

                    return true;
                }
                break;
            case NukiBindingConstants.CHANNEL_SMARTLOCK_STATE:
                if (command instanceof DecimalType) {
                    DecimalType cmd = (DecimalType) command;
                    SmartLockAction action = SmartLockAction.fromAction(cmd.intValue());
                    if (action != null) {
                        withHttpClient(client -> {
                            BridgeLockActionResponse bridgeLockActionResponse = client
                                    .getSmartLockAction(configuration.nukiId, action, getDeviceType());
                            handleResponse(bridgeLockActionResponse, channelUID.getAsString(), command.toString());
                        });
                    }
                    return true;
                }
        }
        return false;
    }

    @Override
    protected Class<NukiSmartLockConfiguration> getConfigurationClass() {
        return NukiSmartLockConfiguration.class;
    }
}
