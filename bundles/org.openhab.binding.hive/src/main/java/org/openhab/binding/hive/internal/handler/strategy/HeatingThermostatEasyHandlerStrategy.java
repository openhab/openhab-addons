/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.handler.strategy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.*;
import org.openhab.binding.hive.internal.client.feature.HeatingThermostatFeature;
import org.openhab.binding.hive.internal.client.feature.OnOffDeviceFeature;

/**
 * A {@link ThingHandlerStrategy} that handles channels that provide a
 * simplified interface with Hive Active Heating zones.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HeatingThermostatEasyHandlerStrategy extends ThingHandlerStrategyBase {
    @Override
    public @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    ) {
        return useFeature(hiveNode, HeatingThermostatFeature.class, heatingThermostatFeature -> {
            return useFeature(hiveNode, OnOffDeviceFeature.class, onOffDeviceFeature -> {
                return handleCommand(
                        channelUID,
                        command,
                        hiveNode,
                        heatingThermostatFeature,
                        onOffDeviceFeature
                );
            });
        });
    }

    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        useFeature(hiveNode, HeatingThermostatFeature.class, heatingThermostatFeature -> {
            useFeature(hiveNode, OnOffDeviceFeature.class, onOffDeviceFeature -> {
                handleUpdate(
                        hiveNode,
                        thing,
                        thingHandlerCallback,
                        heatingThermostatFeature,
                        onOffDeviceFeature
                );
            });
        });
    }

    private @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode,
            final HeatingThermostatFeature heatingThermostatFeature,
            final OnOffDeviceFeature onOffDeviceFeature
    ) {
        @Nullable HeatingThermostatFeature newHeatingThermostatFeature = null;
        @Nullable OnOffDeviceFeature newOnOffDeviceFeature = null;

        if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_EASY_MODE_OPERATING)
                && command instanceof StringType
        ) {
            final StringType newOperatingMode = (StringType) command;
            if (newOperatingMode.toString().equals(HiveBindingConstants.HEATING_EASY_MODE_OPERATING_MANUAL)) {
                newHeatingThermostatFeature = heatingThermostatFeature.withTargetOperatingMode(HeatingThermostatOperatingMode.MANUAL);
                newOnOffDeviceFeature = onOffDeviceFeature.withTargetMode(OnOffMode.ON);
            } else if (newOperatingMode.toString().equals(HiveBindingConstants.HEATING_EASY_MODE_OPERATING_SCHEDULE)) {
                newHeatingThermostatFeature = heatingThermostatFeature.withTargetOperatingMode(HeatingThermostatOperatingMode.SCHEDULE);
                newOnOffDeviceFeature = onOffDeviceFeature.withTargetMode(OnOffMode.ON);
            } else {
                // easy-mode-operating: OFF
                newOnOffDeviceFeature = onOffDeviceFeature.withTargetMode(OnOffMode.OFF);
            }
        } else if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_EASY_MODE_BOOST)
                && command instanceof OnOffType
        ) {
            final OnOffType newOverrideMode = (OnOffType) command;
            newHeatingThermostatFeature = heatingThermostatFeature.withTargetTemporaryOperatingModeOverride(
                    newOverrideMode == OnOffType.ON ? OverrideMode.TRANSIENT : OverrideMode.NONE
            );
        }

        if (newHeatingThermostatFeature != null
                || newOnOffDeviceFeature != null
        ) {
            final Node.Builder nodeBuilder = Node.builder();
            nodeBuilder.from(hiveNode);

            if (newHeatingThermostatFeature != null) {
                nodeBuilder.putFeature(HeatingThermostatFeature.class, newHeatingThermostatFeature);
            }

            if (newOnOffDeviceFeature != null) {
                nodeBuilder.putFeature(OnOffDeviceFeature.class, newOnOffDeviceFeature);
            }

            return nodeBuilder.build();
        } else {
            return null;
        }
    }

    private void handleUpdate(
            final Node hiveNode,
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final HeatingThermostatFeature heatingThermostatFeature,
            final OnOffDeviceFeature onOffDeviceFeature
    ) {
        useAttribute(hiveNode, HeatingThermostatFeature.class, HiveApiConstants.ATTRIBUTE_NAME_HEATING_THERMOSTAT_V1_OPERATING_MODE, heatingThermostatFeature.getOperatingMode(), operatingModeAttribute -> {
            useAttribute(hiveNode, OnOffDeviceFeature.class, HiveApiConstants.ATTRIBUTE_NAME_ON_OFF_DEVICE_V1_MODE, onOffDeviceFeature.getMode(), onOffModeAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_EASY_MODE_OPERATING, easyModeOperatingChannel -> {
                    if (onOffModeAttribute.getDisplayValue() == OnOffMode.OFF) {
                        thingHandlerCallback.stateUpdated(easyModeOperatingChannel, new StringType(HiveBindingConstants.HEATING_EASY_MODE_OPERATING_OFF));
                    } else if (operatingModeAttribute.getDisplayValue() == HeatingThermostatOperatingMode.SCHEDULE) {
                        thingHandlerCallback.stateUpdated(easyModeOperatingChannel, new StringType(HiveBindingConstants.HEATING_EASY_MODE_OPERATING_SCHEDULE));
                    } else {
                        thingHandlerCallback.stateUpdated(easyModeOperatingChannel, new StringType(HiveBindingConstants.HEATING_EASY_MODE_OPERATING_MANUAL));
                    }
                });
            });
        });

        useAttribute(hiveNode, HeatingThermostatFeature.class, HiveApiConstants.ATTRIBUTE_NAME_HEATING_THERMOSTAT_V1_TEMPORARY_OPERATING_MODE_OVERRIDE, heatingThermostatFeature.getTemporaryOperatingModeOverride(), heatingOverrideAttribute -> {
            useChannel(thing, HiveBindingConstants.CHANNEL_EASY_MODE_BOOST, easyModeBoostChannel -> {
                final OnOffType boostActive = OnOffType.from(heatingOverrideAttribute.getDisplayValue() == OverrideMode.TRANSIENT);
                thingHandlerCallback.stateUpdated(easyModeBoostChannel, boostActive);
            });
        });

        useAttribute(hiveNode, HeatingThermostatFeature.class, HiveApiConstants.ATTRIBUTE_NAME_HEATING_THERMOSTAT_V1_OPERATING_STATE, heatingThermostatFeature.getOperatingState(), operatingStateAttribute -> {
            useChannel(thing, HiveBindingConstants.CHANNEL_EASY_STATE_IS_ON, easyStateIsOnChannel -> {
                final OnOffType isOn = OnOffType.from(operatingStateAttribute.getDisplayValue().equals(HeatingThermostatOperatingState.HEAT));
                thingHandlerCallback.stateUpdated(easyStateIsOnChannel, isOn);
            });
        });
    }
}
