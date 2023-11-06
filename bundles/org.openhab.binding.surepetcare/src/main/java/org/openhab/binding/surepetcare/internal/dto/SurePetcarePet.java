/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcarePet} is a DTO class used to represent a pet. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcarePet extends SurePetcareBaseObject {

    public enum PetGender {

        UNKNONWN(-1, "Unknown"),
        FEMALE(0, "Female"),
        MALE(1, "Male");

        public final Integer id;
        public final String name;

        private PetGender(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static PetGender findByTypeId(final int id) {
            return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst().orElse(UNKNONWN);
        }
    }

    public enum PetSpecies {

        UNKNONWN(0, "Unknown"),
        CAT(1, "Cat"),
        DOG(2, "Dog");

        public final Integer id;
        public final String name;

        private PetSpecies(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static PetSpecies findByTypeId(final int id) {
            return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst().orElse(UNKNONWN);
        }
    }

    public String name = "";

    @SerializedName("gender")
    public Integer genderId;
    public LocalDate dateOfBirth;
    public BigDecimal weight;
    public String comments;
    public Long householdId;
    public Integer breedId;
    public Long photoId;
    public Integer speciesId;
    public Long tagId;
    public SurePetcarePhoto photo;

    public SurePetcarePetStatus status = new SurePetcarePetStatus();

    @Override
    public String toString() {
        return "Pet [id=" + id + ", name=" + name + ", tagId=" + tagId + "]";
    }

    @Override
    public @NonNull Map<@NonNull String, @NonNull String> getThingProperties() {
        @NonNull
        Map<@NonNull String, @NonNull String> properties = super.getThingProperties();
        properties.put("householdId", householdId.toString());
        return properties;
    }
}
