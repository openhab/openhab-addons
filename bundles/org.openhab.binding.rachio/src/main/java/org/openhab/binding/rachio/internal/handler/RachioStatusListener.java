/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;

/**
 * The {@link RachioStatusListener} is notified when the thing state changed.
 *
 * @author Markus Michels - Initial contribution
 */

@NonNullByDefault
public interface RachioStatusListener {
    /**
     * This method will be called whenever a new device/zone status is received by the cloud handler.
     *
     * @param updatedDev On device updates this is the new RachioDevice information, maybe null!!
     * @param supdatedZone On zone updates this is the new RachioZone information, maybe null!!
     */

    public boolean onThingStateChangedl(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone);
}
