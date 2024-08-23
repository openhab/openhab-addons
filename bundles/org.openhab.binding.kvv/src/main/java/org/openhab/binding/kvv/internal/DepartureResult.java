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
package org.openhab.binding.kvv.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the result of a call to the KVV API to fetch the next departures at a specific stop.
 *
 * @author Maximilian Hess - Initial contribution
 *
 */
@NonNullByDefault
public class DepartureResult {

    @SerializedName(value = "departureList")
    public List<Departure> departures = new ArrayList<>();

    @Override
    public String toString() {
        return "DepartureResult [departures=" + departures + "]";
    }

    /**
     * Encapsulates a single {@link Departure}. Most important are 'route', 'destination' and 'destination'.
     *
     * @author Maximilian Hess - Initial contribution
     *
     */
    @NonNullByDefault
    public static class Departure {

        @SerializedName(value = "servingLine")
        public Route route = new Route();

        @SerializedName(value = "countdown")
        public String eta = "";

        @Override
        public String toString() {
            return "Departure [" + route + ", eta=" + eta + "]";
        }
    }

    @NonNullByDefault
    public static class Route {

        @SerializedName(value = "number")
        public String name = "";

        public String direction = "";

        @Override
        public String toString() {
            return "name=" + name + ", direction=" + direction;
        }
    }
}
