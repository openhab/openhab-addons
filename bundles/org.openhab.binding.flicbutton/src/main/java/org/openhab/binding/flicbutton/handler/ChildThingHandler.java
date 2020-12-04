/**
 * Copyright (c) 2016 - 2020 Patrick Fink
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 3
 * with the GNU Classpath Exception 2.0 which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-3.0 WITH Classpath-exception-2.0
 */

package org.openhab.binding.flicbutton.handler;

import java.util.Collection;
import java.util.Collections;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * @author Patrick Fink
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
