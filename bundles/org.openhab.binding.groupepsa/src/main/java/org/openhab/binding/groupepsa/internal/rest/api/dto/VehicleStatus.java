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

    private @Nullable ZonedDateTime updatedAt;
    @SerializedName("_embedded")
    private @Nullable Embedded embedded;
    private @Nullable Battery battery;
    private @Nullable DoorsState doorsState;
    private @Nullable List<Energy> energy = null;
    private @Nullable Environment environment;
    private @Nullable Ignition ignition;
    private @Nullable Kinetic kinetic;
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
            return new ToStringBuilder(this).append("extension", this.extension).toString();
        }
    }

    private static class Extension {
        private @Nullable Kinetic kinetic;
        private @Nullable Odometer odometer;

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("kinetic", this.kinetic).append("odometer", this.odometer)
                    .toString();
        }
    }

    public @Nullable Kinetic getKinetic() {
        if (this.kinetic != null) {
            return this.kinetic;
        } else {
            final Embedded finalEmbedded = this.embedded;
            if (finalEmbedded != null) {
                final Extension finalExtension = finalEmbedded.extension;
                if (finalExtension != null) {
                    return finalExtension.kinetic;
                }
            }
            return null;
        }
    }

    public @Nullable Odometer getOdometer() {
        if (this.odometer != null) {
            return this.odometer;
        } else {
            Embedded finalEmbedded = this.embedded;
            if (finalEmbedded != null) {
                final Extension finalExtension = finalEmbedded.extension;
                if (finalExtension != null) {
                    return finalExtension.odometer;
                }
            }
            return null;
        }
    }

    public @Nullable ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public @Nullable Battery getBattery() {
        return this.battery;
    }

    public @Nullable DoorsState getDoorsState() {
        return this.doorsState;
    }

    public @Nullable List<Energy> getEnergy() {
        return this.energy;
    }

    public @Nullable Environment getEnvironment() {
        return this.environment;
    }

    public @Nullable Ignition getIgnition() {
        return this.ignition;
    }

    public @Nullable Position getLastPosition() {
        return this.lastPosition;
    }

    public @Nullable Preconditionning getPreconditionning() {
        return this.preconditionning;
    }

    public @Nullable Privacy getPrivacy() {
        return this.privacy;
    }

    public @Nullable Safety getSafety() {
        return this.safety;
    }

    public @Nullable Service getService() {
        return this.service;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("updatedAt", this.updatedAt).append("_embedded", this.embedded)
                .append("battery", this.battery).append("doorsState", this.doorsState).append("energy", this.energy)
                .append("environment", this.environment).append("ignition", this.ignition)
                .append("kinetic", this.kinetic).append("odometer", this.odometer)
                .append("lastPosition", this.lastPosition).append("preconditionning", this.preconditionning)
                .append("privacy", this.privacy).append("safety", this.safety).append("service", this.service)
                .toString();
    }
}
