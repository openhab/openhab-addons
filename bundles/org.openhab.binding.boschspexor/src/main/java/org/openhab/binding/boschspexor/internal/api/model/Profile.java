package org.openhab.binding.boschspexor.internal.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Profile {
    public enum ProfileType {
        House,
        GardenHouse,
        Car,
        Camper
    }

    private String name;
    @JsonProperty("ProfileType")
    private ProfileType profileType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }
}
