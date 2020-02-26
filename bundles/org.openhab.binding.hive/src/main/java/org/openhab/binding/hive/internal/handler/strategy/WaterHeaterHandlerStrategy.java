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
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.OverrideMode;
import org.openhab.binding.hive.internal.client.WaterHeaterOperatingMode;
import org.openhab.binding.hive.internal.client.feature.WaterHeaterFeature;

/**
 * A {@link ThingHandlerStrategy} for handling
 * {@link WaterHeaterFeature}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class WaterHeaterHandlerStrategy extends ThingHandlerStrategyBase {
    @Override
    public @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    ) {
        return useFeature(hiveNode, WaterHeaterFeature.class, waterHeaterFeature -> {
            @Nullable WaterHeaterFeature newWaterHeaterFeature = null;

            if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_MODE_OPERATING)
                    && command instanceof StringType
            ) {
                final StringType newOperatingMode = (StringType) command;

                newWaterHeaterFeature = waterHeaterFeature.withTargetOperatingMode(
                        WaterHeaterOperatingMode.valueOf(newOperatingMode.toString())
                );
            } else if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_MODE_OPERATING_OVERRIDE)
                    && command instanceof OnOffType
            ) {
                final OnOffType newOverrideMode = (OnOffType) command;
                newWaterHeaterFeature = waterHeaterFeature.withTargetTemporaryOperatingModeOverride(
                        newOverrideMode == OnOffType.ON ? OverrideMode.TRANSIENT : OverrideMode.NONE
                );
            }

            if (newWaterHeaterFeature != null) {
                final Node.Builder nodeBuilder = Node.builder();
                nodeBuilder.from(hiveNode);

                nodeBuilder.putFeature(WaterHeaterFeature.class, newWaterHeaterFeature);

                return nodeBuilder.build();
            } else {
                return null;
            }
        });
    }

    @Override
    public void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    ) {
        useFeature(hiveNode, WaterHeaterFeature.class, waterHeaterFeature -> {
            useAttribute(hiveNode, WaterHeaterFeature.class, HiveApiConstants.ATTRIBUTE_NAME_WATER_HEATER_V1_OPERATING_MODE, waterHeaterFeature.getOperatingMode(), operatingModeAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_MODE_OPERATING, operatingModeChannel -> {
                    thingHandlerCallback.stateUpdated(operatingModeChannel, new StringType(operatingModeAttribute.getDisplayValue().toString()));
                });
            });

            useAttribute(hiveNode, WaterHeaterFeature.class, HiveApiConstants.ATTRIBUTE_NAME_WATER_HEATER_V1_IS_ON, waterHeaterFeature.getIsOn(), isOnAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_EASY_STATE_IS_ON, isOnChannel -> {
                    thingHandlerCallback.stateUpdated(isOnChannel, OnOffType.from(isOnAttribute.getDisplayValue()));
                });
            });

            useAttribute(hiveNode, WaterHeaterFeature.class, HiveApiConstants.ATTRIBUTE_NAME_WATER_HEATER_V1_TEMPORARY_OPERATING_MODE_OVERRIDE, waterHeaterFeature.getTemporaryOperatingModeOverride(), temporaryOperatingModeOverrideAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_MODE_OPERATING_OVERRIDE, operatingModeOverrideChannel -> {
                    thingHandlerCallback.stateUpdated(
                            operatingModeOverrideChannel,
                            OnOffType.from(temporaryOperatingModeOverrideAttribute.getDisplayValue() == OverrideMode.TRANSIENT)
                    );
                });
            });
        });
    }
}
