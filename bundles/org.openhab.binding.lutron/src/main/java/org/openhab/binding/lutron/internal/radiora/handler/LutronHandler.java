/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.radiora.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Base class for non bridge handlers for Lutron RadioRA devices
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public abstract class LutronHandler extends BaseThingHandler {

    public LutronHandler(Thing thing) {
        super(thing);
    }

    public @Nullable RS232Handler getRS232Handler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Unable to get bridge");
            return null;
        }
        ThingHandler th = bridge.getHandler();
        if (th instanceof RS232Handler handler) {
            return handler;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not properly configured.");
            return null;
        }
    }

    /**
     * Returns true if system numbers match, meaning that either both are 2 or both are 1 or 0 (n/a).
     */
    public static boolean systemsMatch(int a, int b) {
        return ((a == 2 && b == 2) || ((a == 0 || a == 1) && (b == 0 || b == 1)));
    }

    public abstract void handleFeedback(RadioRAFeedback feedback);

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }
}
