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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.client.Node;

/**
 * A strategy for how a {@link org.eclipse.smarthome.core.thing.binding.ThingHandler}
 * should handle commands on and updates from {@link Node}s.
 *
 * <p>
 *     This is intended to decouple the handling of specific
 *     {@link org.openhab.binding.hive.internal.client.feature.Feature}s
 *     so it can be re-used by multiple
 *     {@link org.eclipse.smarthome.core.thing.binding.ThingHandler}s.
 * </p>
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public interface ThingHandlerStrategy {
    /**
     * Called when a {@link org.eclipse.smarthome.core.thing.Channel}
     * has received a command that needs to be handled.
     *
     * @param channelUID
     *      The UID of the channel that received the command.
     *
     * @param command
     *      The command that was received.
     *
     * @param hiveNode
     *      The {@linkplain Node} that the {@code command} needs to be
     *      performed on.
     *
     * @return
     *      {@code null} - If the {@linkplain Node} does not need to be updated.
     *      {@code updatedNode} - If the {@linkplain Node} needs to be updated.
     */
    @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    );

    /**
     * Called when a {@link Thing} must be updated with new information
     * from a {@link Node}.
     *
     * @param thing
     *      The {@linkplain Thing} to be updated.
     *
     * @param thingHandlerCallback
     *      The {@linkplain ThingHandlerCallback} to use to update
     *      {@code thing}.
     *
     * @param hiveNode
     *      The {@linkplain Node} containing the information to update
     *      {@code thing} with.
     */
    void handleUpdate(
            final Thing thing,
            final ThingHandlerCallback thingHandlerCallback,
            final Node hiveNode
    );
}
