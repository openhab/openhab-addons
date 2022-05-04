/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.handler.utils;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;

/**
 * The {@link ExtendedBaseBridgeHandler} extended the {@link BaseBridgeHandler} interface and adds <B>publicly
 * visible</B> convenience methods for property handling.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public abstract class ExtendedBaseBridgeHandler extends BaseBridgeHandler {

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * @see BaseBridgeHandler
     * @param bridge which will be created.
     */
    protected ExtendedBaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Returns a copy of the properties map, that can be modified. The method {@link #updateProperties} must be called
     * to persist the properties.
     *
     * @return copy of the thing properties (not null)
     */
    @Override
    public Map<String, String> editProperties() {
        return super.editProperties();
    }

    /**
     * Informs the framework, that the given properties map of the thing was updated. This method performs a check, if
     * the properties were updated. If the properties did not change, the framework is not informed about changes.
     *
     * @param properties properties map, that was updated and should be persisted
     */
    @Override
    public void updateProperties(@Nullable Map<String, String> properties) {
        super.updateProperties(properties);
    }

    /**
     * Returns whether at least one item is linked for the given UID of the channel.
     *
     * @param channelUID UID of the channel (must not be null)
     * @return true if at least one item is linked, false otherwise
     */
    @Override
    public boolean isLinked(ChannelUID channelUID) {
        return super.isLinked(channelUID);
    }
}
