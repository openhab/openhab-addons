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

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SurePetcarePet} is a DTO class used to represent a pet. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcarePet extends SurePetcareBaseObject {

    // {
    // "id":34675,
    // "name":"Cat",
    // "gender":0,
    // "comments":"",
    // "household_id":87435,
    // "breed_id":382,
    // "photo_id":23412,
    // "species_id":1,
    // "tag_id":234523,
    // "version":"MQ==",
    // "created_at":"2019-09-02T09:27:17+00:00",
    // "updated_at":"2019-09-02T09:31:08+00:00",
    // "photo":{
    // "id":23412,
    // "location":"https:\/\/surehub.s3.amazonaws.com\/user-photos\/thm\/56231\/z70LUtqLKJGKJHgjkGLyhuiykfKJhgkjghfptCgeZU.jpg",
    // "uploading_user_id":52815,
    // "version":"MA==",
    // "created_at":"2019-09-02T09:31:07+00:00",
    // "updated_at":"2019-09-02T09:31:07+00:00"
    // },
    // "position":{
    // "tag_id":234523,
    // "device_id":876348,
    // "where":2,
    // "since":"2019-09-11T09:24:13+00:00"
    // },
    // "status":{
    // "activity":{
    // "tag_id":234523,
    // "device_id":318966,
    // "where":2,
    // "since":"2019-09-11T09:24:13+00:00"
    // }
    // }
    // }

    public enum PetGender {

        UNKNONWN(-1, "Unknown"),
        FEMALE(0, "Female"),
        MALE(1, "Male");

        private final Integer genderId;
        private final String name;

        private PetGender(int locationId, String name) {
            this.genderId = locationId;
            this.name = name;
        }

        public Integer getGenderId() {
            return genderId;
        }

        public String getName() {
            return name;
        }

        public static PetGender findByTypeId(final int genderId) {
            return Arrays.stream(values()).filter(value -> value.genderId.equals(genderId)).findFirst()
                    .orElse(UNKNONWN);
        }
    }

    public enum PetSpecies {

        UNKNONWN(0, "Unknown"),
        CAT(1, "Cat"),
        DOG(2, "Dog");

        private final Integer speciesId;
        private final String name;

        private PetSpecies(int speciesId, String name) {
            this.speciesId = speciesId;
            this.name = name;
        }

        public Integer getGenderId() {
            return speciesId;
        }

        public String getName() {
            return name;
        }

        public static PetSpecies findByTypeId(final int speciesId) {
            return Arrays.stream(values()).filter(value -> value.speciesId.equals(speciesId)).findFirst()
                    .orElse(UNKNONWN);
        }
    }

    private String name = "";
    private Integer gender = 0;
    private String comments = "";
    private Integer household_id = 0;
    private Integer breed_id = 0;
    private Integer photo_id = 0;
    private Integer species_id = 0;
    private Integer tag_id = 0;
    private SurePetcarePhoto photo = new SurePetcarePhoto();
    private SurePetcarePetLocation position = new SurePetcarePetLocation();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getHousehold_id() {
        return household_id;
    }

    public void setHousehold_id(Integer household_id) {
        this.household_id = household_id;
    }

    public Integer getBreed_id() {
        return breed_id;
    }

    public void setBreed_id(Integer breed_id) {
        this.breed_id = breed_id;
    }

    public Integer getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(Integer photo_id) {
        this.photo_id = photo_id;
    }

    public Integer getSpecies_id() {
        return species_id;
    }

    public void setSpecies_id(Integer species_id) {
        this.species_id = species_id;
    }

    public Integer getTag_id() {
        return tag_id;
    }

    public void setTag_id(Integer tag_id) {
        this.tag_id = tag_id;
    }

    public SurePetcarePhoto getPhoto() {
        return photo;
    }

    public void setPhoto(SurePetcarePhoto photo) {
        this.photo = photo;
    }

    public SurePetcarePetLocation getLocation() {
        return position;
    }

    public void setPosition(SurePetcarePetLocation position) {
        this.position = position;
    }

    public String getGenderName() {
        return PetGender.findByTypeId(gender).getName();
    }

    public String getSpeciesName() {
        return PetSpecies.findByTypeId(species_id).getName();
    }

    public String getBreedName() {
        return breed_id.toString();
    }

    @Override
    public String toString() {
        return "Pet [id=" + id + ", name=" + name + "]";
    }

    @Override
    public Map<String, Object> getThingProperties() {
        Map<String, Object> properties = super.getThingProperties();
        properties.put("householdId", household_id.toString());
        return properties;
    }

}
