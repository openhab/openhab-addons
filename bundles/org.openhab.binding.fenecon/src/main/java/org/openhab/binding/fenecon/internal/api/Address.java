/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.fenecon.internal.api;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Address} is a small helper class to split a REST-API Address in component and channel.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public final class Address {

    private final String address;
    private final AddressComponent component;
    private final AddressChannel channel;

    public Address(@NotNull String address) {
        this.address = address;

        String[] parts = address.split("/");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid address format 'component/channel' for: " + address);
        }

        component = new AddressComponent(parts[0]);
        channel = new AddressChannel(parts[1]);
    }

    public String getAddress() {
        return address;
    }

    public AddressComponent getComponent() {
        return component;
    }

    public AddressChannel getChannel() {
        return channel;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof Address)) {
            return false;
        }
        Address address = (Address) other;
        if (address.address.equals(this.address)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return address;
    }
}
