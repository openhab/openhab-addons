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
package org.openhab.binding.hdpowerview.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for Things that are managed through an HD PowerView hub
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
abstract class AbstractHubbedThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractHubbedThingHandler.class);

    public AbstractHubbedThingHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable HDPowerViewHubHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.error("Thing {} must belong to a hub", getThing().getThingTypeUID().getId());
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (!(handler instanceof HDPowerViewHubHandler)) {
            logger.debug("Thing {} belongs to the wrong hub type", getThing().getThingTypeUID().getId());
            return null;
        }
        return (HDPowerViewHubHandler) handler;
    }
}
