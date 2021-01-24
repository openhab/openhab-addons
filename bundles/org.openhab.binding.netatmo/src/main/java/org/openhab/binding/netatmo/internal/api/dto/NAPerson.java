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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ModuleType;

/**
 * NAHomePerson
 * This class merges answers provided in event and in webhook to provide the
 * same interface to the binding
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAPerson extends NAModule {
    // Provided by events
    private boolean outOfSight;
    private @Nullable NASnapshot face;

    // Provided by webhooks
    private @Nullable String faceId;
    private @Nullable String faceKey;
    boolean isKnown;

    @Override
    public ModuleType getType() {
        return ModuleType.NAPerson;
    }

    public boolean isOutOfSight() {
        return outOfSight;
    }

    public @Nullable NASnapshot getFace() {
        String fId = faceId;
        String key = faceKey;
        if (face == null && fId != null && key != null) {
            face = new NASnapshot(fId, key);
        }
        return face;
    }

    public boolean isKnown() {
        return isKnown;
    }
}
