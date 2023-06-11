/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for heater and room handlers
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public abstract class MillheatBaseThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MillheatBaseThingHandler.class);

    public MillheatBaseThingHandler(final Thing thing) {
        super(thing);
    }

    public void updateState(final MillheatModel model) {
        for (final Channel channel : getThing().getChannels()) {
            handleCommand(channel.getUID(), RefreshType.REFRESH, model);
        }
    }

    protected MillheatModel getMillheatModel() {
        final Optional<MillheatAccountHandler> accountHandler = getAccountHandler();
        if (accountHandler.isPresent()) {
            return accountHandler.get().getModel();
        } else {
            logger.warn(
                    "Thing {} cannot exist without a bridge and account handler - returning empty model. No heaters or rooms will be found",
                    getThing().getUID());
            return new MillheatModel(0);
        }
    }

    protected Optional<MillheatAccountHandler> getAccountHandler() {
        final Bridge bridge = getBridge();
        if (bridge != null) {
            MillheatAccountHandler handler = (MillheatAccountHandler) bridge.getHandler();
            if (handler != null) {
                return Optional.of(handler);
            }
        }
        return Optional.empty();
    }

    protected abstract void handleCommand(ChannelUID uid, Command command, MillheatModel model);
}
