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
package org.openhab.binding.sony.internal.providers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * This interface extends the {@link DynamicStateDescriptionProvider} to allow adding of state overrides and retrieval
 * of a state description by thingUID/channelID
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface SonyDynamicStateProvider extends DynamicStateDescriptionProvider {
    /**
     * Adds a state override for the given thingUID/channelID
     *
     * @param thingUID a non-null thing uid
     * @param channelId a non-null, non-empty channel ID
     * @param stateDescription a non-null state description to add
     */
    public void addStateOverride(ThingUID thingUID, String channelId, StateDescription stateDescription);

    /**
     * Returns a state description for a thing UID/channel ID. Please note this will only return those items that were
     * added via {@link #addStateOverride(ThingUID, String, StateDescription)}
     *
     * @param thingUID a non-null thing UID
     * @param channelId a non-null, non-empty channel ID
     * @return null if no state description found or non-null if found
     */
    public @Nullable StateDescription getStateDescription(ThingUID thingUID, String channelId);
}
