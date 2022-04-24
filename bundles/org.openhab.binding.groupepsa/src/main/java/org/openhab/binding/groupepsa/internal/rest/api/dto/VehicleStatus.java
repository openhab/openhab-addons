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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class VehicleStatus {

    private @Nullable ZonedDateTime createdAt;
    private @Nullable Embedded _embedded;
    private @Nullable Battery battery;
    private @Nullable DoorsState doorsState;
    private @Nullable List<Energy> energy = null;
    private @Nullable Environment environment;
    private @Nullable Ignition ignition;
    private @Nullable Kinetic kinetic;
    @SerializedName("timed.odometer")
    private @Nullable Odometer odometer;
    private @Nullable Position lastPosition;
    private @Nullable Preconditionning preconditionning;
    private @Nullable Privacy privacy;
    private @Nullable Safety safety;
    private @Nullable Service service;

    private static class Embedded {
        private @Nullable Extension extension;

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("extension", extension).toString();
        }
    }

    private static class Extension {
        private @Nullable Kinetic kinetic;
        private @Nullable Odometer odometer;

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("kinetic", kinetic).append("odometer", odometer).toString();
        }
    }

    public @Nullable Kinetic getKinetic() {
        if (kinetic != null)
            return kinetic;
        else {
            final Embedded final_embedded = _embedded;
            if (final_embedded != null) {
                final Extension final_extension = final_embedded.extension;
                if (final_extension != null)
                    return final_extension.kinetic;
            }
            return null;
        }
    }

    public @Nullable Odometer getOdometer() {
        if (odometer != null)
            return odometer;
        else {
            Embedded final_embedded = _embedded;
            if (final_embedded != null) {
                final Extension final_extension = final_embedded.extension;
                if (final_extension != null)
                    return final_extension.odometer;
            }
            return null;
        }
    }

    public @Nullable ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable Battery getBattery() {
        return battery;
    }

    public @Nullable DoorsState getDoorsState() {
        return doorsState;
    }

    public @Nullable List<Energy> getEnergy() {
        return energy;
    }

    public @Nullable Environment getEnvironment() {
        return environment;
    }

    public @Nullable Ignition getIgnition() {
        return ignition;
    }

    public @Nullable Position getLastPosition() {
        return lastPosition;
    }

    public @Nullable Preconditionning getPreconditionning() {
        return preconditionning;
    }

    public @Nullable Privacy getPrivacy() {
        return privacy;
    }

    public @Nullable Safety getSafety() {
        return safety;
    }

    public @Nullable Service getService() {
        return service;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("createdAt", createdAt).append("_embedded", _embedded)
                .append("battery", battery).append("doorsState", doorsState).append("energy", energy)
                .append("environment", environment).append("ignition", ignition).append("kinetic", kinetic)
                .append("odometer", odometer).append("lastPosition", lastPosition)
                .append("preconditionning", preconditionning).append("privacy", privacy).append("safety", safety)
                .append("service", service).toString();
    }
}
