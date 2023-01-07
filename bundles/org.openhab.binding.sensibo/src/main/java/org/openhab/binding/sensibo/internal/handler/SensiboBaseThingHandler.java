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
package org.openhab.binding.sensibo.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
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
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public abstract class SensiboBaseThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SensiboBaseThingHandler.class);

    public SensiboBaseThingHandler(final Thing thing) {
        super(thing);
    }

    public void updateState(final SensiboModel model) {
        for (final Channel channel : getThing().getChannels()) {
            handleCommand(channel.getUID(), RefreshType.REFRESH, model);
        }
    }

    public SensiboModel getSensiboModel() {
        final Optional<SensiboAccountHandler> accountHandler = getAccountHandler();
        if (accountHandler.isPresent()) {
            return accountHandler.get().getModel();
        } else {
            logger.debug(
                    "Thing {} cannot exist without a bridge and account handler - returning empty model. No heaters or rooms will be found",
                    getThing().getUID());
            return new SensiboModel(0);
        }
    }

    protected Optional<SensiboAccountHandler> getAccountHandler() {
        final Bridge bridge = getBridge();
        if (bridge != null) {
            final SensiboAccountHandler accountHandler = (SensiboAccountHandler) bridge.getHandler();
            if (accountHandler != null) {
                return Optional.of(accountHandler);
            }
        }
        return Optional.empty();
    }

    protected abstract void handleCommand(ChannelUID uid, Command command, SensiboModel model);
}
