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
package org.openhab.binding.hdpowerview.internal.api._v3;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.library.types.PercentType;
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
    // private @Nullable Double velocity;

    @Override
    public State getState(Capabilities shadeCapabilities, CoordinateSystem posKindCoords) {
        Double value;
        switch (posKindCoords) {
            case PRIMARY_POSITION:
                value = primary;
                break;
            case SECONDARY_POSITION:
                value = secondary;
                break;
            case VANE_TILT_POSITION:
                value = tilt;
                break;
            default:
                value = null;
        }
        return value != null ? new PercentType((int) (value.doubleValue() * 100)) : UnDefType.UNDEF;
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
        Double value = Double.valueOf(percent) / 100.0;
        switch (posKindCoords) {
            case PRIMARY_POSITION:
                primary = shadeCapabilities.supportsPrimary() ? value : null;
                break;
            case SECONDARY_POSITION:
                secondary = shadeCapabilities.supportsSecondary() || shadeCapabilities.supportsSecondaryOverlapped()
                        ? value
                        : null;
                break;
            case VANE_TILT_POSITION:
                tilt = shadeCapabilities.supportsTiltOnClosed() || shadeCapabilities.supportsTiltAnywhere() ? value
                        : null;
                break;
            default:
        }
        return this;
    }
}
