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

package org.openhab.binding.flicbutton.internal.handler;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * The {@link ChildThingHandler} class is an abstract class for handlers that are dependent from a parent
 * {@link BridgeHandler}.
 *
 * @author Patrick Fink - Initial contribution
 * @param <BridgeHandlerType> The bridge type this child handler depends on
 */
@NonNullByDefault
public abstract class ChildThingHandler<BridgeHandlerType extends BridgeHandler> extends BaseThingHandler {
    private static final Collection<ThingStatus> DEFAULT_TOLERATED_BRIDGE_STATUSES = Collections
            .singleton(ThingStatus.ONLINE);
    protected boolean bridgeValid = false;
    protected @Nullable BridgeHandlerType bridgeHandler;

    public ChildThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        setStatusBasedOnBridge();
        if (getBridge() != null) {
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
        setStatusBasedOnBridge(DEFAULT_TOLERATED_BRIDGE_STATUSES);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        this.setStatusBasedOnBridge();
    }

    protected void setStatusBasedOnBridge(Collection<ThingStatus> toleratedBridgeStatuses) {
        if (getBridge() != null) {
            if (toleratedBridgeStatuses.contains(getBridge().getStatus())) {
                bridgeValid = true;
            } else {
                bridgeValid = false;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Bridge in unsupported status: " + getBridge().getStatus());
            }
        } else {
            bridgeValid = false;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge missing.");
        }
    }
}
