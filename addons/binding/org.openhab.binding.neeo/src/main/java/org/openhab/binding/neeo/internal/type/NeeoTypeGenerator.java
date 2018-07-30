/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoRoom;

/**
 * The interface for NeeoTypeGenerator to generate thing types for a given {@link NeeoRoom}
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public interface NeeoTypeGenerator {
    /**
     * Generates the ThingType for the given room
     *
     * @param brainId the non-null, non-empty brain id
     * @param room the non-null room
     */
    public void generate(String brainId, NeeoRoom room);

    /**
     * Generates the ThingType for the given device
     *
     * @param brainId the non-null, non-empty brain id
     * @param roomUid the non-null room UID
     * @param device the non-null device
     */
    public void generate(String brainId, ThingTypeUID roomUid, NeeoDevice device);

    /**
     * Removes the specified brain from the type generator
     *
     * @param brainId a non-null, non-empty brain id
     */
    public void removeBrain(String brainId);

    /**
     * Removes the specified room from the type generator
     *
     * @param brainId a non-null, non-empty brain id
     * @param roomKey a non-null, non-empty room key
     */
    public void removeRoom(String brainId, String roomKey);

    /**
     * Removes the specified device from the type generator
     *
     * @param brainId a non-null, non-empty brain id
     * @param deviceKey a non-null, non-empty device key
     */
    public void removeDevice(String brainId, String deviceKey);
}
