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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl;

/**
 * The {@link DSUID} represents the digitalSTROM-Device unique identifier.
 *
 * @author Alexander Friese - initial contributor
 */
public class DSUID {

    private final String dsuid;
    private final String DEFAULT_DSUID = "3504175fe0000000000000000000000001";

    /**
     * Creates a new {@link DSUID}.
     *
     * @param dsuid to create
     */
    public DSUID(String dsuid) {
        var trimmedDsuid = dsuid != null ? dsuid.trim() : "";
        if (trimmedDsuid.length() == 34) {
            this.dsuid = trimmedDsuid;
        } else {
            this.dsuid = DEFAULT_DSUID;
        }
    }

    /**
     * Returns the dSUID as {@link String}.
     *
     * @return dsuid
     */
    public String getValue() {
        return dsuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSUID) {
            return ((DSUID) obj).getValue().equals(this.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return dsuid.hashCode();
    }

    @Override
    public String toString() {
        return dsuid;
    }
}
