/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing a Neeo Brain(serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoBrain {

    /** The brain name */
    @Nullable
    private String name;

    /** The version of the brain */
    @Nullable
    private String version;

    /** The brain's label */
    @Nullable
    private String label;

    /** Whether the brain has been configured */
    private boolean configured;

    /** The brain key */
    @Nullable
    private String key;

    /** ?? The brain airkey ?? */
    @Nullable
    private String airkey;

    /** Last time the brain was changed */
    private long lastchange;

    /** The rooms in the brain */
    @Nullable
    private NeeoRooms rooms;

    /**
     * Gets the brain name
     *
     * @return the name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Gets the version of the brain
     *
     * @return the version
     */
    @Nullable
    public String getVersion() {
        return version;
    }

    /**
     * Gets the brain's label
     *
     * @return the label
     */
    @Nullable
    public String getLabel() {
        return label;
    }

    /**
     * Checks if the brain is configured
     *
     * @return true, if is configured
     */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Gets the brain key
     *
     * @return the key
     */
    @Nullable
    public String getKey() {
        return key;
    }

    /**
     * Gets the brain's airkey
     *
     * @return the airkey
     */
    @Nullable
    public String getAirkey() {
        return airkey;
    }

    /**
     * Gets the last time the brain was changed
     *
     * @return the lastchange
     */
    public long getLastChange() {
        return lastchange;
    }

    /**
     * Gets the rooms in the brain
     *
     * @return the rooms
     */
    public NeeoRooms getRooms() {
        final NeeoRooms localRooms = rooms;
        return localRooms == null ? new NeeoRooms(new NeeoRoom[0]) : localRooms;
    }

    @Override
    public String toString() {
        return "NeeoBrain [name=" + name + ", version=" + version + ", label=" + label + ", configured=" + configured
                + ", key=" + key + ", airkey=" + airkey + ", lastchange=" + lastchange + ", rooms=" + rooms + "]";
    }
}
