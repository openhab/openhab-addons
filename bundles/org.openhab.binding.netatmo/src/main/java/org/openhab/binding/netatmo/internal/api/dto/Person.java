/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link Person} holds answers provided in webhook events
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class Person extends NAThing {
    private @Nullable String faceUrl;
    private boolean isKnown;

    @Override
    public ModuleType getType() {
        return ModuleType.PERSON;
    }

    public @Nullable String getFaceUrl() {
        return faceUrl;
    }

    public boolean isKnown() {
        return isKnown;
    }
}
