/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class User {
    private @Nullable String email;
    private @Nullable String firstName;
    private @Nullable String lastName;
    private @Nullable Embedded _embedded;
    private @Nullable ZonedDateTime createdAt;
    private @Nullable ZonedDateTime updatedAt;

    private static class Embedded {
        private @Nullable List<Vehicle> vehicles;

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("vehicles", vehicles).toString();
        }
    }

    public @Nullable String getEmail() {
        return email;
    }

    public @Nullable String getLastName() {
        return lastName;
    }

    public @Nullable String getFirstName() {
        return firstName;
    }

    public @Nullable List<Vehicle> getVehicles() {
        final Embedded res_embedded = _embedded;
        if (res_embedded != null)
            return res_embedded.vehicles;
        else
            return null;
    }

    public @Nullable ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("createdAt", createdAt).append("updatedAt", createdAt)
                .append("email", email).append("firstName", firstName).append("lastName", lastName)
                .append("vehicles", _embedded != null ? _embedded : null).toString();
    }
}
