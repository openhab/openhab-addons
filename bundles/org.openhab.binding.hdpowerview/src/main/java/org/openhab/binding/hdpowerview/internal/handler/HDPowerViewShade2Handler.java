/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Thing;

/**
 * Handles commands for an HD PowerView Shade -- with new secondary mode.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class HDPowerViewShade2Handler extends HDPowerViewShadeHandler {

    public HDPowerViewShade2Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected int fixupPosition(PercentType position) {
        // New Secondary Mode: return the inverse of the position value.
        return 100 - position.intValue();
    }
}
