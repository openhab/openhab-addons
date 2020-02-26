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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.OnOffMode;
import org.openhab.binding.hive.internal.client.feature.OnOffDeviceFeature;

/**
 * A {@link ThingHandlerStrategy} for handling
 * {@link OnOffDeviceFeature}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class OnOffDeviceHandlerStrategy extends ThingHandlerStrategyBase {
    @Override
    public @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    ) {
        return useFeature(hiveNode, OnOffDeviceFeature.class, onOffDeviceFeature -> {
            @Nullable OnOffDeviceFeature newOnOffDeviceFeature = null;

            if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_MODE_ON_OFF)
                    && command instanceof OnOffType) {
                // Set the new target heating temperature.
                final OnOffType value = (OnOffType) command;
                final OnOffMode mode = value == OnOffType.ON ? OnOffMode.ON : OnOffMode.OFF;

                newOnOffDeviceFeature = onOffDeviceFeature.withTargetMode(mode);
            }

            if (newOnOffDeviceFeature != null) {
                final Node.Builder nodeBuilder = Node.builder();
                nodeBuilder.from(hiveNode);

                nodeBuilder.putFeature(OnOffDeviceFeature.class, newOnOffDeviceFeature);

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
        useFeature(hiveNode, OnOffDeviceFeature.class, onOffDeviceFeature -> {
            useAttribute(hiveNode, OnOffDeviceFeature.class, HiveApiConstants.ATTRIBUTE_NAME_ON_OFF_DEVICE_V1_MODE, onOffDeviceFeature.getMode(), modeAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_MODE_ON_OFF, onOffModeChannel -> {
                    final OnOffType value = modeAttribute.getDisplayValue() == OnOffMode.ON ? OnOffType.ON : OnOffType.OFF;
                    thingHandlerCallback.stateUpdated(onOffModeChannel, value);
                });
            });
        });
    }
}
