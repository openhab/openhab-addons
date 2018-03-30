/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity;

import com.google.api.client.util.Key;

/**
 * Defines a {@link Property}, that is a basic key/value structure used for several data types in the innogy API.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Property {

    @Key("name")
    private String name;

    @Key("value")
    private Object value;

    @Key("lastchanged")
    private String lastchanged;

    public Property() {
        // used for serialization
    }

    /**
     * Constructs a new {@link Property} with the given name and value.
     *
     * @param name
     * @param value
     */
    public Property(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @return the lastchanged
     */
    public String getLastchanged() {
        return lastchanged;
    }

    /**
     * @param lastchanged the lastchanged to set
     */
    public void setLastchanged(String lastchanged) {
        this.lastchanged = lastchanged;
    }
}
