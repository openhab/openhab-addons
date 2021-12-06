package org.openhab.binding.boschspexor.internal.api.model;

public class Spexor {

    private String id;
    private String name;
    private Profile profile;
    private StatusShort status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public StatusShort getStatus() {
        return status;
    }

    public void setStatus(StatusShort status) {
        this.status = status;
    }
}
