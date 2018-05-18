/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.atlona.handler.AtlonaHandler;

/**
 *
 * A callback to {@link AtlonaHandler} that can be used to update the status, properties and state of the thing.
 *
 * @author Tim Roberts - Initial contribution
 */
public interface AtlonaHandlerCallback {
    /**
     * Callback to the {@link AtlonaHandler} to update the status of the thing.
     *
     * @param status a non-null {@link org.eclipse.smarthome.core.thing.ThingStatus}
     * @param detail a non-null {@link org.eclipse.smarthome.core.thing.ThingStatusDetail}
     * @param msg a possibly null, possibly empty message
     */
    void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg);

    /**
     * Callback to the {@link AtlonaHandler} to update the state of an item
     *
     * @param channelId the non-null, non-empty channel id
     * @param state the new non-null {@State}
     */
    void stateChanged(String channelId, State state);

    /**
     * Callback to the {@link AtlonaHandler} to update the property of a thing
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a non-null, possibly empty property value
     */
    void setProperty(String propertyName, String propertyValue);
}
