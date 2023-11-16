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
package org.openhab.binding.atlona.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 *
 * A callback to {@link org.openhab.binding.atlona.internal.handler.AtlonaHandler}
 * that can be used to update the status, properties and state of the thing.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface AtlonaHandlerCallback {
    /**
     * Callback to the {@link org.openhab.binding.atlona.internal.handler.AtlonaHandler}
     * to update the status of the thing.
     *
     * @param status a non-null {@link org.openhab.core.thing.ThingStatus}
     * @param detail a non-null {@link org.openhab.core.thing.ThingStatusDetail}
     * @param msg a possibly null, possibly empty message
     */
    void statusChanged(ThingStatus status, ThingStatusDetail detail, @Nullable String msg);

    /**
     * Callback to the {@link org.openhab.binding.atlona.internal.handler.AtlonaHandler}
     * to update the state of an item
     *
     * @param channelId the non-null, non-empty channel id
     * @param state the new non-null {@link State}
     */
    void stateChanged(String channelId, State state);

    /**
     * Callback to the {@link org.openhab.binding.atlona.internal.handler.AtlonaHandler}
     * to update the property of a thing
     *
     * @param propertyName a non-null, non-empty property name
     * @param propertyValue a non-null, possibly empty property value
     */
    void setProperty(String propertyName, String propertyValue);
}
