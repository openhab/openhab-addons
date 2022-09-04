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
package org.openhab.binding.hdpowerview.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.types.State;

/**
 * Abstract class for position of a Shade as returned by an HD PowerView hub.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public abstract class ShadePosition {

    /**
     * Get the shade's State for the given actuator class resp. coordinate system.
     *
     * @param shadeCapabilities the shade Thing capabilities.
     * @param posKindCoords the actuator class (coordinate system) whose state is to be returned.
     * @return the current state.
     */
    public abstract State getState(Capabilities shadeCapabilities, CoordinateSystem posKindCoords);

    /**
     * Detect if the ShadePosition has a posKindN value indicating potential support for a secondary rail.
     *
     * @return true if the ShadePosition supports a secondary rail.
     */
    public abstract boolean secondaryRailDetected();

    /**
     * Detect if the ShadePosition has both a posKindN value indicating potential support for tilt, AND a posKindN
     * indicating support for a primary rail. i.e. it potentially supports tilt anywhere functionality.
     *
     * @return true if potential support for tilt anywhere functionality was detected.
     */
    public abstract boolean tiltAnywhereDetected();

    /**
     * Set the shade's position for the given actuator class resp. coordinate system.
     *
     * @param shadeCapabilities the shade Thing capabilities.
     * @param posKindCoords the actuator class (coordinate system) whose state is to be changed.
     * @param percent the new position value.
     * @return this object.
     */
    public abstract ShadePosition setPosition(Capabilities shadeCapabilities, CoordinateSystem posKindCoords,
            int percent);
}
