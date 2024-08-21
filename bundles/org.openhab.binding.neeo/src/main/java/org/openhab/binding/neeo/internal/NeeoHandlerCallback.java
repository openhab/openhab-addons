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
package org.openhab.binding.neeo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 *
 * This interface is used to provide a callback mechanism between a {@link org.openhab.core.thing.binding.ThingHandler}
 * and the associated protocol.
 * This is necessary since the status and state of a bridge/thing is private and the protocol handler cannot access it
 * directly.
 *
 * @author Tim Roberts - Initial contribution
 *
 */
@NonNullByDefault
public interface NeeoHandlerCallback {
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
     * @param state the new non-null {@link State}
     */
    void stateChanged(String channelId, State state);

    /**
     * Callback to set a property for the thing.
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a non-null, possibly empty property value
     */
    void setProperty(String propertyName, String propertyValue);

    /**
     * Schedule a task to be executed in the future
     *
     * @param task the non-null task
     * @param milliSeconds the milliseconds (>0)
     */
    void scheduleTask(Runnable task, long milliSeconds);

    /**
     * Callback to trigger an event
     *
     * @param channelID a non-null, non-empty channel id
     * @param event a possibly null, possibly empty event
     */
    void triggerEvent(String channelID, String event);

    /**
     * Callback to retrieve the current {@link NeeoBrainApi}
     *
     * @return a possibly null {@link NeeoBrainApi}
     */
    @Nullable
    NeeoBrainApi getApi();
}
