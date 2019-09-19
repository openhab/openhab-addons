/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * The {@link DSID} represents the digitalSTROM-Device identifier.
 *
 * @author Alexander Betker - initial contributer
 */
public class DSID {

    private String dsid;
    private final String DEFAULT_DSID = "3504175fe000000000000001";
    private final String PRE = "3504175fe0000000";

    /**
     * Creates a new {@link DSID}.
     *
     * @param dsid to create
     */
    public DSID(String dsid) {
        this.dsid = dsid;
        if (dsid != null && !dsid.trim().equals("")) {
            if (dsid.trim().length() == 24) {
                this.dsid = dsid;
            } else if (dsid.trim().length() == 8) {
                this.dsid = this.PRE + dsid;
            } else if (dsid.trim().toUpperCase().equals("ALL")) {
                this.dsid = "ALL";
            } else {
                this.dsid = DEFAULT_DSID;
            }
        } else {
            this.dsid = DEFAULT_DSID;
        }
    }

    /**
     * Returns the dSID as {@link String}.
     *
     * @return dSID
     */
    public String getValue() {
        return dsid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSID) {
            return ((DSID) obj).getValue().equals(this.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return dsid.hashCode();
    }

    @Override
    public String toString() {
        return dsid;
    }
}
