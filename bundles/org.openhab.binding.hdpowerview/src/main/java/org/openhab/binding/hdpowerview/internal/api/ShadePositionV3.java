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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The position of a single shade, as returned by an HD PowerView hub of Generation 3
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadePositionV3 extends ShadePosition {

    private @Nullable Double primary;
    private @Nullable Double secondary;
    private @Nullable Double tilt;
    private @Nullable Double velocity;

    @Override
    public State getState(Capabilities shadeCapabilities, CoordinateSystem posKindCoords) {
        // TODO
        return UnDefType.UNDEF;
    }

    @Override
    public boolean secondaryRailDetected() {
        return secondary != null;
    }

    @Override
    public boolean tiltAnywhereDetected() {
        return tilt != null;
    }

    @Override
    public ShadePositionV3 setPosition(Capabilities shadeCapabilities, CoordinateSystem posKindCoords, int percent) {
        // TODO
        return this;
    }
}
