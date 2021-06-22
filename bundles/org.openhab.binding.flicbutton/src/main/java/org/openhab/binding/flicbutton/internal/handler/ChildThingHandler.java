/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.flicbutton.internal.handler;

import java.util.Collection;
import java.util.Collections;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * @author Patrick Fink - Initial contribution
 * @param <BridgeHandlerType> The bridge type this child handler depends on
 */
public abstract class ChildThingHandler<BridgeHandlerType extends BridgeHandler> extends BaseThingHandler {
    private static final Collection<ThingStatus> defaultToleratedBridgeStatuses = Collections
            .singleton(ThingStatus.ONLINE);
    protected boolean bridgeValid = false;
    protected BridgeHandlerType bridgeHandler;

    public ChildThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        setStatusBasedOnBridge();
        if (bridgeValid) {
            linkBridge();
        }
    }

    protected void linkBridge() {
        try {
            BridgeHandler bridgeHandlerUncasted = getBridge().getHandler();
            bridgeHandler = (BridgeHandlerType) bridgeHandlerUncasted;
        } catch (ClassCastException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Type is invalid.");
        }
    }

    protected void setStatusBasedOnBridge() {
        setStatusBasedOnBridge(defaultToleratedBridgeStatuses);
    }

    protected void setStatusBasedOnBridge(Collection<ThingStatus> toleratedBridgeStatuses) {
        if (getBridge() != null) {
            if (toleratedBridgeStatuses.contains(getBridge().getStatus())) {
                bridgeValid = true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Bridge in unsupported status: " + getBridge().getStatus());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge missing.");
        }
    }
}
