/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Representation of a Bosch spexor
 *
 * @author Marc Fischer - Initial contribution *
 */
@NonNullByDefault
public class Spexor {

    private String id = "1";
    private String name = "spexor";
    private Profile profile = new Profile();
    private StatusShort status = new StatusShort();

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
