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
package org.openhab.binding.hue.internal.handler;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.action.LightActions;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

/**
 * The {@link HueLightActionsHandler} defines interface handlers to handle {@link LightActions}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public interface HueLightActionsHandler extends ThingHandler {

    @Override
    default Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(LightActions.class);
    }

    /**
     * Handles a command for a given channel.
     *
     * @param channel the id of the channel to which the command was sent
     * @param command the {@link Command}
     * @param fadeTime duration for execution of the command
     */
    void handleCommand(String channel, Command command, long fadeTime);
}
