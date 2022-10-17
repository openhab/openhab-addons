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
package org.openhab.binding.echonetlite.internal;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.echonetlite.internal.HexUtil.hex;

import java.net.InetSocketAddress;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class InstanceKey {
    final InetSocketAddress address;
    final EchonetClass klass;
    final int instance;

    public InstanceKey(final InetSocketAddress address, final EchonetClass klass, final int instance) {
        this.address = requireNonNull(address);
        this.klass = requireNonNull(klass);
        this.instance = instance;
    }

    public String toString() {
        return "InstanceKey{" + "address=" + address + ", klass=" + klass + ", instance=" + instance + '}';
    }

    public String representationProperty() {
        return address.getAddress().getHostAddress() + "_" + hex(klass.groupCode()) + ":" + hex(klass.classCode()) + ":"
                + hex(instance);
    }

    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InstanceKey that = (InstanceKey) o;
        return instance == that.instance && address.equals(that.address) && klass == that.klass;
    }

    public int hashCode() {
        return Objects.hash(address, klass, instance);
    }
}
