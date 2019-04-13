/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;

public abstract class AbstractOmnilinkHandler extends BaseThingHandler {

    public AbstractOmnilinkHandler(Thing thing) {
        super(thing);
    }

    public OmnilinkBridgeHandler getOmnilinkBridgeHander() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (OmnilinkBridgeHandler) bridge.getHandler();
        } else {
            return null;
        }
    }

    /**
     * Gets the configured number for a thing.
     *
     * @return Configured number for a thing.
     */
    protected int getThingNumber() {
        return ((Number) getThing().getConfiguration().get(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER))
                .intValue();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

}