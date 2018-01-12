/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model;

import java.util.Date;
import java.util.List;

import org.openhab.binding.gardena.internal.GardenaSmartCommandName;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Gardena property.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Property {

    private String name;
    private String value;
    private Date timestamp;
    private String unit;
    private boolean writeable;

    @SerializedName("supported_values")
    private List<String> supportedValues;
    private transient Ability ability;

    public Property() {
    }

    public Property(GardenaSmartCommandName commandName, String value) {
        this.name = commandName.toString().toLowerCase();
        this.value = value;
    }

    /**
     * Returns the name of the property.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the property.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the property.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the timestamp of the property.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the unit of the property.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns true, if the property is writeable.
     */
    public boolean isWriteable() {
        return writeable;
    }

    /**
     * Returns a list of supported values.
     */
    public List<String> getSupportedValues() {
        return supportedValues;
    }

    /**
     * Returns the ability of the property.
     */

    public Ability getAbility() {
        return ability;
    }

    /**
     * Sets the ability of the property.
     */
    public void setAbility(Ability ability) {
        this.ability = ability;
    }

}
