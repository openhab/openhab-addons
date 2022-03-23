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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

/**
 * The {@link Person} merges answers provided in event and in webhook to provide the
 * same interface to the binding
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class Person extends NAThing {
    // Provided by events
    // TODO : not 100% sure this is still used since new API.
    private @Nullable Snapshot face;

    // Provided by webhooks
    private @Nullable String faceId;
    private @Nullable String faceKey;
    private boolean isKnown;

    @Override
    public ModuleType getType() {
        return ModuleType.PERSON;
    }

    public @Nullable String getFaceUrl() {
        Snapshot localFace = face;
        if (localFace != null) {
            return localFace.getUrl();
        }
        String fId = faceId;
        String key = faceKey;
        if (face == null && fId != null && key != null) {
            return new Snapshot(fId, key).getUrl();
        }
        return null;
    }

    public boolean isKnown() {
        return isKnown;
    }
}
