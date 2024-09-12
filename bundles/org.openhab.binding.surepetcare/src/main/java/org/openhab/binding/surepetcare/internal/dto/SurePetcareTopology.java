/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SurePetcareTopology} is the Java class used to represent a whole Sure Petcare topology. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareTopology {

    public List<SurePetcareDevice> devices = new ArrayList<>();
    public List<SurePetcareHousehold> households = new ArrayList<>();
    public List<SurePetcarePet> pets = new ArrayList<>();
    public List<SurePetcarePhoto> photos = new ArrayList<>();
    public List<SurePetcareTag> tags = new ArrayList<>();
    @Nullable
    public SurePetcareUser user;

    public @Nullable <T extends SurePetcareBaseObject> T getById(List<T> elements, String id) {
        for (T e : elements) {
            if (id.equals(e.id.toString())) {
                return e;
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
