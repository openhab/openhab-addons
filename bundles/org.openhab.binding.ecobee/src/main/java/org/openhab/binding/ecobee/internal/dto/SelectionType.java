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
package org.openhab.binding.ecobee.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SelectionType} represents the valid selection types that can be passed in
 * a SelectionDTO object.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum SelectionType {

    /*
     * Select only those thermostats listed in the CSV match criteria. No spaces in the CSV string. There is a limit
     * of 25 identifiers per request.
     */
    @SerializedName("thermostats")
    THERMOSTATS("thermostats"),

    /*
     * When this is set the thermostats registered to the current user will be returned. This is only usable with
     * Smart thermostats registered to a user. It does not work on EMS thermostats and may not be used by a Utility
     * who is not the owner of thermostats.
     */
    @SerializedName("registered")
    REGISTERED("registered"),

    /*
     * Selects all thermostats for a given management set defined by the Management/Utility account. This is only
     * available to Management/Utility accounts. "/" is the root, represented by the "My Sets" set.
     */
    @SerializedName("managementSet")
    MANAGEMENT_SET("managementSet");

    private final String type;

    private SelectionType(final String type) {
        this.type = type;
    }

    public static SelectionType forValue(@Nullable String v) {
        if (v != null) {
            for (SelectionType at : SelectionType.values()) {
                if (at.type.equals(v)) {
                    return at;
                }
            }
        }
        throw new IllegalArgumentException("Invalid or null selection type: " + v);
    }

    @Override
    public String toString() {
        return this.type;
    }
}
