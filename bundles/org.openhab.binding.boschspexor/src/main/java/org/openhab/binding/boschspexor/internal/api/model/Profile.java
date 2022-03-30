/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Representation of a profile
 *
 * @author Marc Fischer - Initial contribution *
 */
@NonNullByDefault
public class Profile {
    /**
     * Profile Types
     *
     * @author Marc Fischer - Initial contribution
     *
     */
    public enum ProfileType {
        House,
        GardenHouse,
        Car,
        Camper
    }

    private String name = "default";
    private ProfileType profileType = ProfileType.House;

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
