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
 * The {@link BedsResponse} holds the BedsResponse response from the sleepiq API.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class BedsResponse {
    private List<Bed> beds;

    public List<Bed> getBeds() {
        return beds;
    }

    public void setBeds(List<Bed> beds) {
        this.beds = beds;
    }

    public BedsResponse withBeds(List<Bed> beds) {
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
        if (!(obj instanceof BedsResponse)) {
            return false;
        }
        BedsResponse other = (BedsResponse) obj;
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
        builder.append("BedsResponse [beds=");
        builder.append(beds);
        builder.append("]");
        return builder.toString();
    }
}
