/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.grxprg;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * A callback to {@link PrgBridgeHandler} that will be used by the {@link PrgProtocolHandler} to update the status and
 * state of the {@link PrgBridgeHandler} or a specific {@link GrafikEyeHandler}
 *
 * @author Tim Roberts
 *
 */
interface PrgHandlerCallback {
    /**
     * Callback to the {@link PrgBridgeHandler} to update the status of the {@link Bridge}
     *
     * @param status a non-null {@link org.eclipse.smarthome.core.thing.ThingStatus}
     * @param detail a non-null {@link org.eclipse.smarthome.core.thing.ThingStatusDetail}
     * @param msg a possibly null, possibly empty message
     */
    void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg);

    /**
     * Callback to the {@link PrgBridgeHandler} to update the state of an item
     *
     * @param channelId the non-null, non-empty channel id
     * @param state the new non-null {@State}
     */
    void stateChanged(String channelId, State state);

    /**
     * Callback to the {@link PrgBridgeHandler} to update the state of an item in a specific {@link GrafikEyeHandler}.
     *
     * @param controlUnit the control unit identifier to update
     * @param channelId the non-null, non-empty channel id
     * @param state the new non-null {@State}
     */
    void stateChanged(int controlUnit, String channelId, State state);

    /**
     * Callback to the {@link PrgBridgeHandler} to determine if the specific zone on a specific control unit is a shade
     * or not
     *
     * @param controlUnit the control unit identifier
     * @param zone the zone identify
     * @return true if a shade zone, false otherwise
     */
    boolean isShade(int controlUnit, int zone);
}
