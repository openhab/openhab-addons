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
package org.openhab.binding.sleepiq.internal.api.dto;

import java.util.List;

/**
 * The {@link FamilyStatusResponse} holds the FamilyStatusResponse response from the sleepiq API.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class FamilyStatusResponse {
    private List<BedStatus> beds;

    public List<BedStatus> getBeds() {
        return beds;
    }

    public void setBeds(List<BedStatus> beds) {
        this.beds = beds;
    }

    public FamilyStatusResponse withBeds(List<BedStatus> beds) {
        setBeds(beds);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((beds == null) ? 0 : beds.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FamilyStatusResponse)) {
            return false;
        }
        FamilyStatusResponse other = (FamilyStatusResponse) obj;
        if (beds == null) {
            if (other.beds != null) {
                return false;
            }
        } else if (!beds.equals(other.beds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FamilyStatus [beds=");
        builder.append(beds);
        builder.append("]");
        return builder.toString();
    }
}
