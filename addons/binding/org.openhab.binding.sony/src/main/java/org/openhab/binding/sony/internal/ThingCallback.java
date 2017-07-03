/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sony.internal.simpleip.SimpleIpHandler;

// TODO: Auto-generated Javadoc
/**
 * This interface is used to provide a callback mechanism between {@link SimpleIpProtocol} and the associated
 * {@link SimpleIpHandler}. This is necessary since the status and state of a thing is private and the protocol handler
 * cannot access it directly.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
public interface ThingCallback<T> {

    /**
     * Callback to the bridge/thing to update the status of the bridge/thing.
     *
     * @param state the state
     * @param detail a non-null {@link ThingStatusDetail}
     * @param msg a possibly null, possibly empty message
     */
    void statusChanged(ThingStatus state, ThingStatusDetail detail, String msg);

    /**
     * Callback to the bridge/thing to update the state of a channel in the bridge/thing.
     *
     * @param channelId the non-null, non-empty channel id
     * @param newState the new state
     */
    void stateChanged(T channelId, State newState);

    /**
     * Callback to set a property in the bridge/thing.
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a non-null, possibly empty property value
     */
    void setProperty(String propertyName, String propertyValue);
}
