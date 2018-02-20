/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openhab.binding.gardena.internal.exception.GardenaException;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Gardena device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Device {

    private String id;
    private String name;
    private String description;
    private String category;
    @SerializedName("configuration_synchronized")
    private boolean configurationSynchronized;
    private List<Ability> abilities = new ArrayList<>();
    @SerializedName("scheduled_events")
    private List<ScheduledEvent> scheduledEvents = new ArrayList<>();
    private transient Location location;
    private List<Setting> settings = new ArrayList<>();

    /**
     * Returns the id of the device.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the device.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the device.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the category of the device.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns true, if all configurations are synchronized.
     */
    public boolean isConfigurationSynchronized() {
        return configurationSynchronized;
    }

    /**
     * Returns a list of abilities of the device.
     */
    public List<Ability> getAbilities() {
        return abilities;
    }

    /**
     * Returns a list of scheduled events of the device.
     */
    public List<ScheduledEvent> getScheduledEvents() {
        return scheduledEvents;
    }

    /**
     * Returns the location of the device.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location of the device.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Returns the ability with the specified name.
     */
    public Ability getAbility(String name) throws GardenaException {
        for (Ability ability : abilities) {
            if (ability.getName().equals(name)) {
                return ability;
            }
        }
        throw new GardenaException("Ability '" + name + "' not found in device '" + this.name + "'");
    }

    public List<Setting> getSettings() {
        return settings;
    }

    /**
     * Returns the setting with the specified name.
     */
    public Setting getSetting(String name) throws GardenaException {
        for (Setting setting : settings) {
            if (setting.getName().equals(name)) {
                return setting;
            }
        }
        throw new GardenaException("Setting '" + name + "' not found in device '" + this.name + "'");
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Device)) {
            return false;
        }
        Device comp = (Device) obj;
        return new EqualsBuilder().append(comp.getId(), id).isEquals();
    }

}
