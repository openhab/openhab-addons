/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable POJO representing a device queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class Device {
    @Nullable
    private Ident ident;
    @Nullable
    private State state;

    public Optional<Ident> getIdent() {
        return Optional.ofNullable(ident);
    }

    public Optional<State> getState() {
        return Optional.ofNullable(state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, state);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Device other = (Device) obj;
        return Objects.equals(ident, other.ident) && Objects.equals(state, other.state);
    }

    @Override
    public String toString() {
        return "Device [ident=" + ident + ", state=" + state + "]";
    }
}
