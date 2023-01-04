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
package org.openhab.binding.plugwise.internal.protocol.field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The media access control (MAC) address of a Plugwise device, e.g.: 000D6F0000A1B2C3
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class MACAddress {

    private final String macAddress;

    public MACAddress(String macAddress) {
        this.macAddress = macAddress.toUpperCase();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + macAddress.hashCode();
        return result;
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
        MACAddress other = (MACAddress) obj;
        if (!macAddress.equals(other.macAddress)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return macAddress;
    }
}
