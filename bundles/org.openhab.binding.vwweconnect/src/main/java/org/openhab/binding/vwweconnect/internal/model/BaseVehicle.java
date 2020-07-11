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
package org.openhab.binding.vwweconnect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A base Vehicle.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class BaseVehicle {
    public static final int UNDEFINED = -1;

    protected @Nullable String status;

    /**
     *
     * @return
     *         The status
     */
    public @Nullable String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     *            The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @SuppressWarnings("null")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BaseVehicle)) {
            return false;
        }

        BaseVehicle other = (BaseVehicle) obj;

        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (status != null && !status.equals(other.status)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseVehicle [");

        if (status != null) {
            builder.append(", status=");
            builder.append(status);
        }

        builder.append("]");
        return builder.toString();
    }
}
