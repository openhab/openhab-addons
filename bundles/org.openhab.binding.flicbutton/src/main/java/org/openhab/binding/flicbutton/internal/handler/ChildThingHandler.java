/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChildThingHandler} class is an abstract class for handlers that are dependent from a parent
 * {@link BridgeHandler}.
 *
 * @author Patrick Fink - Initial contribution
 * @param <BridgeHandlerType> The bridge type this child handler depends on
 */
@NonNullByDefault
public abstract class ChildThingHandler<BridgeHandlerType extends BridgeHandler> extends BaseThingHandler {
    private static final Collection<ThingStatus> DEFAULT_TOLERATED_BRIDGE_STATUSES = Set.of(ThingStatus.ONLINE);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected boolean bridgeValid = false;
    protected @Nullable BridgeHandlerType bridgeHandler;

    public ChildThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Bridge unavailable while initializing Thing: {}", thing.getUID());
            return;
        }
        setStatusBasedOnBridge();
        linkBridge(bridge);
    }

    protected void linkBridge(Bridge bridge) {
        try {
            BridgeHandler bridgeHandlerUncasted = bridge.getHandler();
            bridgeHandler = (BridgeHandlerType) bridgeHandlerUncasted;
        } catch (ClassCastException e) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Type is invalid.");
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
        Bridge bridge = getBridge();
        if (bridge == null) {
            bridgeValid = false;
            logger.debug("Bridge unavailable for Thing: {}", thing.getUID());
            return;
        }
        if (toleratedBridgeStatuses.contains(bridge.getStatus())) {
            bridgeValid = true;
        } else {
            bridgeValid = false;
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE, "Bridge in unsupported status: " + bridge.getStatus());
        }
    }
}
