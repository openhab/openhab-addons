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
package org.openhab.binding.surepetcare.internal.data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link SurePetcarePetLocation} is the Java class used to represent the location of a pet. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcarePetLocation {

    // {"pet_id":70237,"tag_id":60126,"device_id":376236,"where":2,"since":"2019-09-11T13:09:07+00:00"}

    public enum PetLocation {

        UNKNONWN(0, "Unknown"),
        INSIDE(1, "Inside"),
        OUTSIDE(2, "Outside");

        private final Integer locationId;
        private final String name;

        private PetLocation(int locationId, String name) {
            this.locationId = locationId;
            this.name = name;
        }

        public Integer getLocationId() {
            return locationId;
        }

        public String getName() {
            return name;
        }

        public static @NonNull PetLocation findByTypeId(final int locationId) {
            return Arrays.stream(values()).filter(value -> value.locationId.equals(locationId)).findFirst()
                    .orElse(UNKNONWN);
        }
    }

    private Integer pet_id;
    private Integer tag_id;
    private Integer device_id;
    private Integer where;
    private Date since;

    public Integer getPet_id() {
        return pet_id;
    }

    public void setPet_id(Integer pet_id) {
        this.pet_id = pet_id;
    }

    public Integer getTag_id() {
        return tag_id;
    }

    public void setTag_id(Integer tag_id) {
        this.tag_id = tag_id;
    }

    public Integer getDevice_id() {
        return device_id;
    }

    public void setDevice_id(Integer device_id) {
        this.device_id = device_id;
    }

    public Integer getWhere() {
        return where;
    }

    public void setWhere(Integer where) {
        this.where = where;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    @Override
    public String toString() {
        return "Pet [id=" + pet_id + ", location=" + PetLocation.findByTypeId(where).getName() + "]";
    }

    public @NonNull String getLocationName() {
        return PetLocation.findByTypeId(where).getName();
    }

    public @NonNull ZonedDateTime getLocationChanged() {
        return since.toInstant().atZone(ZoneId.systemDefault());
    }

}
