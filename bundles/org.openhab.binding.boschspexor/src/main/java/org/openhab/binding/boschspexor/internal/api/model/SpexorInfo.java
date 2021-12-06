package org.openhab.binding.boschspexor.internal.api.model;

import java.util.ArrayList;
import java.util.List;

public class SpexorInfo {
    private String id;
    private String name;
    private Profile profile;
    private Status status;
    private List<SensorType> sensors = new ArrayList<>();

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<SensorType> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorType> sensors) {
        this.sensors = sensors;
    }
}
