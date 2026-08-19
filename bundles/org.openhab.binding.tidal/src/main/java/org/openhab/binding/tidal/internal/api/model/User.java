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
package org.openhab.binding.tidal.internal.api.model;

import com.google.gson.annotations.JsonAdapter;

/**
 * Tidal Api Track data class.
 *
 * @author Laurent Arnal - Initial contribution
 */

@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class User {
    private String id;
    private String type;
    private String username;
    private String country;
    private String email;
    private boolean emailVerified;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUserName() {
        return username;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail() {
        return email;
    }

    public boolean getEmailVerified() {
        return emailVerified;
    }

}
