/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;

/**
 * This interface is used to provide a callback mechanism between listener (usually a protocol of some sort) and the
 * associated {@link ThingHandler}. This is necessary since the status and state of a thing is private and the protocol
 * handler cannot access it directly.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type of the key to track state
 */
@NonNullByDefault
public interface ThingCallback<T> {

    /**
     * Callback to the bridge/thing to update the status of the bridge/thing.
     *
     * @param state the non-null {@link ThingStatus}
     * @param detail a non-null {@link ThingStatusDetail}
     * @param msg a possibly null, possibly empty message
     */
    void statusChanged(ThingStatus state, ThingStatusDetail detail, @Nullable String msg);

    /**
     * Callback to the bridge/thing to update the state of a channel in the bridge/thing.
     *
     * @param channelId the non-null, non-empty channel id
     * @param newState the possibly null new state
     */
    void stateChanged(T channelId, State newState);

    /**
     * Callback to set a property in the bridge/thing.
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a possibly null, possibly empty property value
     */
    void setProperty(String propertyName, @Nullable String propertyValue);
}
