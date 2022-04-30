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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class Environment {

    private @Nullable ZonedDateTime updatedAt;
    private @Nullable ZonedDateTime createdAt;
    private @Nullable Air air;
    private @Nullable Luminosity luminosity;

    public @Nullable ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public @Nullable ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable Air getAir() {
        return air;
    }

    public @Nullable Luminosity getLuminosity() {
        return luminosity;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("updatedAt", updatedAt).append("createdAt", createdAt)
                .append("air", air).append("luminosity", luminosity).toString();
    }
}
