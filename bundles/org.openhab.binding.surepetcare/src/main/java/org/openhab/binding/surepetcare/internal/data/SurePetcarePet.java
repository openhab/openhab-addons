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

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
//import java.util.Date;

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

        private final Integer id;
        private final String name;

        private PetGender(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static PetGender findByTypeId(final int id) {
            return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst().orElse(UNKNONWN);
        }
    }

    public enum PetSpecies {

        UNKNONWN(0, "Unknown"),
        CAT(1, "Cat"),
        DOG(2, "Dog");

        private final Integer id;
        private final String name;

        private PetSpecies(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static PetSpecies findByTypeId(final int id) {
            return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst().orElse(UNKNONWN);
        }
    }

    private String name = "";

    @SerializedName("gender")
    private Integer genderId;
    private Date dateOfBirth;
    private BigDecimal weight;
    private String comments;
    private Integer householdId;
    private Integer breedId;
    private Integer photoId;
    private Integer speciesId;
    private Integer tagId;
    private SurePetcarePhoto photo;

    @SerializedName("status")
    private SurePetcarePetStatus status = new SurePetcarePetStatus();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGenderId() {
        return genderId;
    }

    public void setGenderId(Integer genderId) {
        this.genderId = genderId;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(Integer householdId) {
        this.householdId = householdId;
    }

    public Integer getBreedId() {
        return breedId;
    }

    public void setBreedId(Integer breedId) {
        this.breedId = breedId;
    }

    public Integer getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Integer photoId) {
        this.photoId = photoId;
    }

    public Integer getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(Integer speciesId) {
        this.speciesId = speciesId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public SurePetcarePhoto getPhoto() {
        return photo;
    }

    public void setPhoto(SurePetcarePhoto photo) {
        this.photo = photo;
    }

    public SurePetcarePetStatus getPetStatus() {
        return status;
    }

    public void setPetStatus(SurePetcarePetStatus status) {
        this.status = status;
    }

    public ZonedDateTime getDateOfBirthAsZonedDateTime() {
        return dateOfBirth == null ? null : dateOfBirth.toInstant().atZone(ZoneId.systemDefault());
    }

    @Override
    public String toString() {
        return "Pet [id=" + id + ", name=" + name + ", tagId=" + tagId + "]";
    }

    @Override
    public Map<String, Object> getThingProperties() {
        Map<String, Object> properties = super.getThingProperties();
        properties.put("householdId", householdId.toString());
        return properties;
    }

}
