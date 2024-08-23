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
package org.openhab.binding.lutron.internal.grxprg;

import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 *
 * A callback to {@link PrgBridgeHandler} that will be used by the {@link PrgProtocolHandler} to update the status and
 * state of the {@link PrgBridgeHandler} or a specific {@link GrafikEyeHandler}
 *
 * @author Tim Roberts - Initial contribution
 *
 */
interface PrgHandlerCallback {
    /**
     * Callback to the {@link PrgBridgeHandler} to update the status of the {@link Bridge}
     *
     * @param status a non-null {@link org.openhab.core.thing.ThingStatus}
     * @param detail a non-null {@link org.openhab.core.thing.ThingStatusDetail}
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
