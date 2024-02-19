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
package org.openhab.binding.cloudrain.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Zone} class represents a Cloudrain Zone API result
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class Zone extends CloudrainAPIItem {

    private static final String ZONE_NAME_DEFAULT = "Unknwon Zone";

    @SerializedName("zoneId")
    private @Nullable String id;

    @SerializedName("zoneName")
    private @Nullable String name;

    /**
     * Creates a Zone with required attributes. Useful for test implementations. Typically objects of
     * this type will be created through reflection by the GSON library when parsing the JSON response of the API
     *
     * @param id the ID of this zone
     * @param name the name of this zone
     * @param controllerId the ID of the controller managing this zone
     * @param controllerName the name of the controller managing this zone
     */
    public Zone(String id, String name, String controllerId, String controllerName) {
        super(controllerId, controllerName);
        this.id = id;
        this.name = name;
    }

    public @Nullable String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns a non-null name. Either the name property or a default value.
     * In case of an error parsing the name attribute from the API response this is convenient to avoid null checks
     * The name attribute is not mandatory for further API interactions.
     *
     * @return the non-null name
     */
    public String getNameWithDefault() {
        String nullableName = getName();
        if (nullableName == null || nullableName.isBlank()) {
            return ZONE_NAME_DEFAULT;
        }
        return nullableName;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUID() {
        return this.controllerId + "-" + this.id;
    }
}
