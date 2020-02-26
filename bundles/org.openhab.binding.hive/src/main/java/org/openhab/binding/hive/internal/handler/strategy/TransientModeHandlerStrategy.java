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
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.TransientModeFeature;

import java.time.Duration;

/**
 * A {@link ThingHandlerStrategy} for handling
 * {@link TransientModeFeature}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class TransientModeHandlerStrategy extends ThingHandlerStrategyBase {
    @Override
    public @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    ) {
        return useFeature(hiveNode, TransientModeFeature.class, transientModeFeature -> {
            @Nullable TransientModeFeature newTransientModeFeature = null;

            if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_TRANSIENT_DURATION)
                    && command instanceof DecimalType
            ) {
                final DecimalType newDurationMins = (DecimalType) command;

                newTransientModeFeature = transientModeFeature.withTargetDuration(Duration.ofMinutes(newDurationMins.longValue()));
            }

            if (newTransientModeFeature != null) {
                final Node.Builder nodeBuilder = Node.builder();
                nodeBuilder.from(hiveNode);

                nodeBuilder.putFeature(TransientModeFeature.class, newTransientModeFeature);

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
        useFeature(hiveNode, TransientModeFeature.class, transientModeFeature -> {
            useAttribute(hiveNode, TransientModeFeature.class, HiveApiConstants.ATTRIBUTE_NAME_TRANSIENT_MODE_V1_DURATION, transientModeFeature.getDuration(), durationAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_TRANSIENT_DURATION, transientDurationChannel -> {
                    final long durationMins = durationAttribute.getDisplayValue().toMinutes();
                    thingHandlerCallback.stateUpdated(transientDurationChannel, new DecimalType(durationMins));
                });
            });

            useAttribute(hiveNode, TransientModeFeature.class, HiveApiConstants.ATTRIBUTE_NAME_TRANSIENT_MODE_V1_IS_ENABLED, transientModeFeature.getIsEnabled(), isEnabledAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_TRANSIENT_ENABLED, transientEnabledChannel -> {
                    thingHandlerCallback.stateUpdated(transientEnabledChannel, OnOffType.from(isEnabledAttribute.getDisplayValue()));
                });
            });

            useAttribute(hiveNode, TransientModeFeature.class, HiveApiConstants.ATTRIBUTE_NAME_TRANSIENT_MODE_V1_START_DATETIME, transientModeFeature.getStartDatetime(), startDatetimeAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_TRANSIENT_START_TIME, transientStartTimeChannel -> {
                    thingHandlerCallback.stateUpdated(transientStartTimeChannel, new DateTimeType(startDatetimeAttribute.getDisplayValue()));
                });
            });

            useAttribute(hiveNode, TransientModeFeature.class, HiveApiConstants.ATTRIBUTE_NAME_TRANSIENT_MODE_V1_END_DATETIME, transientModeFeature.getEndDatetime(), endDatetimeAttribute -> {
                useChannel(thing, HiveBindingConstants.CHANNEL_TRANSIENT_END_TIME, transientEndTimeChannel -> {
                    thingHandlerCallback.stateUpdated(transientEndTimeChannel, new DateTimeType(endDatetimeAttribute.getDisplayValue()));
                });
            });
        });
    }
}
