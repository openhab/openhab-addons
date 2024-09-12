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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Failure} holds the Failure response from the sleepiq API.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Failure {
    @SerializedName("Error")
    private Error error;

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public Failure withError(Error error) {
        setError(error);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((error == null) ? 0 : error.hashCode());
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
        if (!(obj instanceof Failure)) {
            return false;
        }
        Failure other = (Failure) obj;
        if (error == null) {
            if (other.error != null) {
                return false;
            }
        } else if (!error.equals(other.error)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Failure [error=");
        builder.append(error);
        builder.append("]");
        return builder.toString();
    }
}
