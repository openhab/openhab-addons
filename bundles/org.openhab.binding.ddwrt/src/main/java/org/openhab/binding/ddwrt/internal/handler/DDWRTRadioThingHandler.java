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
package org.openhab.binding.ddwrt.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal handler for a DD‑WRT Radio thing.
 *
 * @author Lee Ballard - Initial contribution (adapted)
 */
@NonNullByDefault
public class DDWRTRadioThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DDWRTRadioThingHandler.class);

    public DDWRTRadioThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DD‑WRT Radio Thing handler for '{}'", getThing().getUID());
        // For now we just set the thing online – real implementation can be added later.
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing DD‑WRT Radio Thing handler for '{}'", getThing().getUID());
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID arg0, Command arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleCommand'");
    }

    // Add command handling, channel updates, etc. when you need them.
}
