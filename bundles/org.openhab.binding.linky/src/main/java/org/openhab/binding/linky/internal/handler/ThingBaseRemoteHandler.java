/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.config.LinkyThingRemoteConfiguration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

/**
 * The {@link ThingBaseRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

@NonNullByDefault
public class ThingBaseRemoteHandler extends BaseThingHandler {

    protected LinkyThingRemoteConfiguration config;

    public ThingBaseRemoteHandler(Thing thing) {
        super(thing);

        config = getConfigAs(LinkyThingRemoteConfiguration.class);
    }

    @Override
    public synchronized void initialize() {
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
    }

    public @Nullable LinkyThingRemoteConfiguration getLinkyConfig() {
        return config;
    }
}
