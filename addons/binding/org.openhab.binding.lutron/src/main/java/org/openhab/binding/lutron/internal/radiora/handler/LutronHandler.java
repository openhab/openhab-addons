/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for non bridge handlers for Lutron RadioRA devices
 *
 * @author Jeff Lauterbach
 *
 */
public abstract class LutronHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(LutronHandler.class);

    private RS232Handler bridgeHandler;

    public LutronHandler(Thing thing) {
        super(thing);
    }

    public RS232Handler getChronosHandler() {

        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.error("Unable to get bridge");
                return null;
            }
            ThingHandler th = bridge.getHandler();
            if (th instanceof RS232Handler) {
                this.bridgeHandler = (RS232Handler) th;
            } else {
                logger.error("Bridge not properly configured.");
            }
        }

        return this.bridgeHandler;
    }

    public abstract void handleFeedback(RadioRAFeedback feedback);
}
