/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

/**
 * The model representing an Neeo Brain(serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoBrain {

    /** The brain name */
    private final String name;

    /** The version of the brain */
    private final String version;

    /** The brain's label */
    private final String label;

    /** Whether the brain has been configured */
    private final boolean configured;

    /** The brain key */
    private final String key;

    /** ?? The brain airkey ?? */
    private final String airkey;

    /** Last time the brain was changed */
    private final long lastchange;

    /** The rooms in the brain */
    private final NeeoRooms rooms;

    /**
     * Instantiates a new neeo brain.
     *
     * @param name the name
     * @param version the version
     * @param label the label
     * @param configured the configured
     * @param key the key
     * @param airkey the airkey
     * @param lastchange the lastchange
     * @param rooms the rooms
     */
    public NeeoBrain(String name, String version, String label, boolean configured, String key, String airkey,
            int lastchange, NeeoRooms rooms) {
        this.name = name;
        this.version = version;
        this.label = label;
        this.configured = configured;
        this.key = key;
        this.airkey = airkey;
        this.lastchange = lastchange;
        this.rooms = rooms;
    }

    /**
     * Gets the brain name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the version of the brain
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the brain's label
     *
     * @return the label
     */
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
    public String getKey() {
        return key;
    }

    /**
     * Gets the brain's airkey
     *
     * @return the airkey
     */
    public String getAirkey() {
        return airkey;
    }

    /**
     * Gets the last time the brain was changed
     *
     * @return the lastchange
     */
    public long getLastchange() {
        return lastchange;
    }

    /**
     * Gets the rooms in the brain
     *
     * @return the rooms
     */
    public NeeoRooms getRooms() {
        return rooms;
    }

    /**
     * Gets a specified room in the brain
     *
     * @param key the key
     * @return the room
     */
    public NeeoRoom getRoom(String key) {
        return rooms.getRoom(key);
    }

    @Override
    public String toString() {
        return "NeeoBrain [name=" + name + ", version=" + version + ", label=" + label + ", configured=" + configured
                + ", key=" + key + ", airkey=" + airkey + ", lastchange=" + lastchange + ", rooms=" + rooms + "]";
    }

}
