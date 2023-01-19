/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.russound.internal.rio;

import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 *
 * This interface is used to provide a callback mechanism between {@link AbstractRioProtocol} and the associated
 * bridge/thing ({@link AbstractBridgeHandler} and {@link AbstractThingHandler}). This is necessary since the status and
 * state of a bridge/thing is private and the protocol handler cannot access it directly.
 *
 * @author Tim Roberts - Initial contribution
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

    /**
     * Callback to set a property for the thing
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a non-null, possibly empty property value
     */
    void setProperty(String propertyName, String propertyValue);

    /**
     * Adds a listener to changes to the channelId
     *
     * @param channelId a non-null, non-empty channelID
     * @param listener a non-null listener
     * @throws IllegalArgumentException if channelId is null or empty
     * @throws IllegalArgumentException if listener is null
     */
    void addListener(String channelId, RioHandlerCallbackListener listener);

    /**
     * Removes the specified listener for the specified channel
     *
     * @param channelId a non-null, non-empty channelID
     * @param listener a non-null listener
     * @throws IllegalArgumentException if channelId is null or empty
     * @throws IllegalArgumentException if listener is null
     */
    void removeListener(String channelId, RioHandlerCallbackListener listener);
}
