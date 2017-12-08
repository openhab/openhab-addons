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
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;

/**
 * Base class for non bridge handlers for Lutron RadioRA devices
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public abstract class LutronHandler extends BaseThingHandler {

    public LutronHandler(Thing thing) {
        super(thing);
    }

    public RS232Handler getRS232Handler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Unable to get bridge");
            return null;
        }
        ThingHandler th = bridge.getHandler();
        if (th instanceof RS232Handler) {
            return (RS232Handler) th;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not properly configured.");
            return null;
        }
    }

    public abstract void handleFeedback(RadioRAFeedback feedback);
}
