/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.draytonwiser.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.draytonwiser.internal.DraytonWiserItemUpdateListener;

/**
 * The {@link DraytonWiserThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
public abstract class DraytonWiserThingHandler extends BaseThingHandler implements DraytonWiserItemUpdateListener {

    protected HeatHubHandler bridgeHandler;

    protected DraytonWiserThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        bridgeHandler = getBridgeHandler();
        bridgeHandler.registerItemListener(this);
        refresh();
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.unregisterItemListener(this);
        }
    }

    @Override
    public void onItemUpdate() {
        refresh();
    }

    protected abstract void refresh();

    private synchronized HeatHubHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return ((HeatHubHandler) bridge.getHandler());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        }
    }

}
