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
import org.openhab.binding.hive.internal.client.feature.OnOffDeviceFeature;
import org.openhab.binding.hive.internal.client.feature.TransientModeFeature;
import org.openhab.binding.hive.internal.client.feature.WaterHeaterFeature;

/**
 * A {@link ThingHandlerStrategy} that handles channels that provide a
 * simplified interface with Hive Hot Water.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class WaterHeaterEasyHandlerStrategy extends ThingHandlerStrategyBase {
    @Override
    public @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    ) {
        return useFeature(hiveNode, WaterHeaterFeature.class, waterHeaterFeature -> {
            return useFeature(hiveNode, OnOffDeviceFeature.class, onOffDeviceFeature -> {
                return useFeature(hiveNode, TransientModeFeature.class, transientModeFeature -> {
                    return handleCommand(
                            channelUID,
                            command,
                            hiveNode,
                            waterHeaterFeature,
                            onOffDeviceFeature,
                            transientModeFeature
                    );
                });
            });
        });
    }

    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        useFeature(hiveNode, WaterHeaterFeature.class, waterHeaterFeature -> {
            useFeature(hiveNode, OnOffDeviceFeature.class, onOffDeviceFeature -> {
                useFeature(hiveNode, TransientModeFeature.class, transientModeFeature -> {
                    handleUpdate(
                            hiveNode,
                            thing,
                            thingHandlerCallback,
                            waterHeaterFeature,
                            onOffDeviceFeature,
                            transientModeFeature
                    );
                });
            });
        });
    }

    private @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode,
            final WaterHeaterFeature waterHeaterFeature,
            final OnOffDeviceFeature onOffDeviceFeature,
            final TransientModeFeature transientModeFeature
    ) {
        @Nullable WaterHeaterFeature newWaterHeaterFeature = null;
        @Nullable OnOffDeviceFeature newOnOffDeviceFeature = null;
        @Nullable TransientModeFeature newTransientModeFeature = null;

        if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_EASY_MODE_OPERATING)
                && command instanceof StringType
        ) {
            final StringType newOperatingMode = (StringType) command;
            if (newOperatingMode.toString().equals(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_SCHEDULE)) {
                newWaterHeaterFeature = waterHeaterFeature.withTargetOperatingMode(WaterHeaterOperatingMode.SCHEDULE);
                newOnOffDeviceFeature = onOffDeviceFeature.withTargetMode(OnOffMode.ON);
            } else if (newOperatingMode.toString().equals(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_ON)) {
                newWaterHeaterFeature = waterHeaterFeature.withTargetOperatingMode(WaterHeaterOperatingMode.ON);
                newOnOffDeviceFeature = onOffDeviceFeature.withTargetMode(OnOffMode.ON);
            } else {
                // easy-mode-operating: OFF
                newOnOffDeviceFeature = onOffDeviceFeature.withTargetMode(OnOffMode.OFF);
            }
        } else if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_EASY_MODE_BOOST)
                && command instanceof OnOffType
        ) {
            final OnOffType newOverrideMode = (OnOffType) command;

            if (newOverrideMode == OnOffType.ON) {
                newTransientModeFeature = transientModeFeature.withTargetIsEnabled(true);
                newWaterHeaterFeature = waterHeaterFeature.withTargetTemporaryOperatingModeOverride(OverrideMode.TRANSIENT);
            } else {
                newTransientModeFeature = transientModeFeature.withTargetIsEnabled(false);
                newWaterHeaterFeature = waterHeaterFeature.withTargetTemporaryOperatingModeOverride(OverrideMode.NONE);
            }
        }

        if (newWaterHeaterFeature != null
                || newOnOffDeviceFeature != null
                || newTransientModeFeature != null
        ) {
            final Node.Builder nodeBuilder = Node.builder();
            nodeBuilder.from(hiveNode);

            if (newWaterHeaterFeature != null) {
                nodeBuilder.putFeature(WaterHeaterFeature.class, newWaterHeaterFeature);
            }

            if (newOnOffDeviceFeature != null) {
                nodeBuilder.putFeature(OnOffDeviceFeature.class, newOnOffDeviceFeature);
            }

            if (newTransientModeFeature != null) {
                nodeBuilder.putFeature(TransientModeFeature.class, newTransientModeFeature);
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
            final WaterHeaterFeature waterHeaterFeature,
            final OnOffDeviceFeature onOffDeviceFeature,
            final TransientModeFeature transientModeFeature
    ) {
        useAttribute(hiveNode, WaterHeaterFeature.class, HiveApiConstants.ATTRIBUTE_NAME_WATER_HEATER_V1_OPERATING_MODE, waterHeaterFeature.getOperatingMode(), operatingModeAttribute -> {
            useAttribute(hiveNode, OnOffDeviceFeature.class, HiveApiConstants.ATTRIBUTE_NAME_ON_OFF_DEVICE_V1_MODE, onOffDeviceFeature.getMode(), onOffModeAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_EASY_MODE_OPERATING, easyModeOperatingChannel -> {
                    if (onOffModeAttribute.getDisplayValue() == OnOffMode.OFF) {
                        thingHandlerCallback.stateUpdated(easyModeOperatingChannel, new StringType(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_OFF));
                    } else if (operatingModeAttribute.getDisplayValue() == WaterHeaterOperatingMode.SCHEDULE) {
                        thingHandlerCallback.stateUpdated(easyModeOperatingChannel, new StringType(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_SCHEDULE));
                    } else {
                        thingHandlerCallback.stateUpdated(easyModeOperatingChannel, new StringType(HiveBindingConstants.HOT_WATER_EASY_MODE_OPERATING_ON));
                    }
                });
            });
        });

        useAttribute(hiveNode, WaterHeaterFeature.class, HiveApiConstants.ATTRIBUTE_NAME_WATER_HEATER_V1_TEMPORARY_OPERATING_MODE_OVERRIDE, waterHeaterFeature.getTemporaryOperatingModeOverride(), waterHeaterOverrideAttribute -> {
            useAttribute(hiveNode, TransientModeFeature.class, HiveApiConstants.ATTRIBUTE_NAME_TRANSIENT_MODE_V1_IS_ENABLED, transientModeFeature.getIsEnabled(), transientModeEnabledAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_EASY_MODE_BOOST, easyModeBoostChannel -> {
                    final OnOffType boostActive = OnOffType.from(waterHeaterOverrideAttribute.getDisplayValue() == OverrideMode.TRANSIENT
                            && transientModeEnabledAttribute.getDisplayValue());
                    thingHandlerCallback.stateUpdated(easyModeBoostChannel, boostActive);
                });
            });
        });
    }
}
