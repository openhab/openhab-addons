/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal.pro3;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * A callback to {@link AtlonaPro3Handler} that will be used by {@link AtlonaPro3PortocolHandler} to update the status
 * and state of the thing.
 *
 * @author Tim Roberts
 *
 */
interface AtlonaPro3HandlerCallback {
    /**
     * Callback to the {@link org.openhab.binding.atlona.internal.pro3.AtlonaPro3Handler} to update the status of the
     * thing.
     *
     * @param status a non-null {@link org.eclipse.smarthome.core.thing.ThingStatus}
     * @param detail a non-null {@link org.eclipse.smarthome.core.thing.ThingStatusDetail}
     * @param msg a possibly null, possibly empty message
     */
    void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg);

    /**
     * Callback to the {@link org.openhab.binding.atlona.internal.pro3.AtlonaPro3Handler} to update the state of an item
     *
     * @param channelId the non-null, non-empty channel id
     * @param state the new non-null {@State}
     */
    void stateChanged(String channelId, State state);
}
