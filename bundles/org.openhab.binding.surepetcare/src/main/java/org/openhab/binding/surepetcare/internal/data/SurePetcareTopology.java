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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SurePetcareTopology} is the Java class used to represent a whole Sure Petcare topology. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareTopology {

    private List<SurePetcareDevice> devices = new ArrayList<SurePetcareDevice>();
    private List<SurePetcareHousehold> households = new ArrayList<SurePetcareHousehold>();
    private List<SurePetcarePet> pets = new ArrayList<SurePetcarePet>();
    private List<SurePetcarePhoto> photos = new ArrayList<SurePetcarePhoto>();
    private List<SurePetcareTag> tags = new ArrayList<SurePetcareTag>();
    private SurePetcareUser user;

    public List<SurePetcareDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<SurePetcareDevice> devices) {
        this.devices = devices;
    }

    public List<SurePetcareHousehold> getHouseholds() {
        return households;
    }

    public void setHouseholds(List<SurePetcareHousehold> households) {
        this.households = households;
    }

    public List<SurePetcarePet> getPets() {
        return pets;
    }

    public void setPets(List<SurePetcarePet> pets) {
        this.pets = pets;
    }

    public List<SurePetcarePhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<SurePetcarePhoto> photos) {
        this.photos = photos;
    }

    public List<SurePetcareTag> getTags() {
        return tags;
    }

    public void setTags(List<SurePetcareTag> tags) {
        this.tags = tags;
    }

    public SurePetcareUser getUser() {
        return user;
    }

    public void setUser(SurePetcareUser user) {
        this.user = user;
    }

    public synchronized @Nullable SurePetcareHousehold getHouseholdById(@NonNull String id) {
        for (SurePetcareHousehold household : households) {
            if (id.equals(household.getId().toString())) {
                return household;
            }
        }
        return null;
    }

    public synchronized @Nullable SurePetcareDevice getDeviceById(@NonNull String id) {
        for (SurePetcareDevice device : devices) {
            if (id.equals(device.getId().toString())) {
                return device;
            }
        }
        return null;
    }

    public synchronized @Nullable SurePetcarePet getPetById(@NonNull String id) {
        for (SurePetcarePet pet : pets) {
            if (id.equals(pet.getId().toString())) {
                return pet;
            }
        }
        return null;
    }

    public synchronized @Nullable SurePetcarePhoto getPhotoById(@NonNull String id) {
        for (SurePetcarePhoto photo : photos) {
            if (id.equals(photo.getId().toString())) {
                return photo;
            }
        }
        return null;
    }

    public synchronized @Nullable SurePetcareTag getTagById(@NonNull String id) {
        for (SurePetcareTag tag : tags) {
            if (id.equals(tag.getId().toString())) {
                return tag;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "SurePetcareTopology [# of Devices=" + devices.size() + ", # of Households=" + households.size()
                + ", # of pets=" + pets.size() + "]";
    }

}
