/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.rio;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * This interface is used to provide a callback mechanism between {@link AbstractRioProtocol} and the associated
 * bridge/thing ({@link AbstractBridgeHandler} and {@link AbstractThingHandler}). This is necessary since the status and
 * state of a bridge/thing is private and the protocol handler cannot access it directly.
 *
 * @author Tim Roberts
 * @version $Id: $Id
 *
 */
public interface RioHandlerCallback {
    /**
     * Callback to the bridge/thing to update the status of the bridge/thing.
     *
     * @param status a non-null {@link ThingStatus}
     * @param detail a non-null {@link ThingStatusDetail}
     * @param msg a possibly null, possibly empty message
     */
    void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg);

    /**
     * Callback to the bridge/thing to update the state of a channel in the bridge/thing.
     *
     * @param channelId the non-null, non-empty channel id
     * @param state the new non-null {@State}
     */
    void stateChanged(String channelId, State state);
}
