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
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import tuwien.auto.calimero.datapoint.Datapoint;

/**
 * Information about a data point which is queued to be read from the KNX bus.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class ReadDatapoint {

    private final Datapoint datapoint;
    private int retries;
    private final int limit;

    public ReadDatapoint(Datapoint datapoint, int limit) {
        this.datapoint = datapoint;
        this.retries = 0;
        this.limit = limit;
    }

    public Datapoint getDatapoint() {
        return datapoint;
    }

    public int getRetries() {
        return retries;
    }

    public void incrementRetries() {
        this.retries++;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((datapoint.getMainAddress() == null) ? 0 : datapoint.getMainAddress().hashCode());
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
        @Nullable
        ReadDatapoint other = (ReadDatapoint) obj;
        return datapoint.getMainAddress().equals(other.datapoint.getMainAddress());
    }
}
