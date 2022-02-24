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
package org.openhab.binding.livisismarthome.internal.client.api.entity;

/**
 * Defines a {@link PropertyDTO}, that is a basic key/value structure used for several data types in the LIVISI API.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class PropertyDTO {

    private String name;
    private Object value;

    @SuppressWarnings("unused")
    public PropertyDTO() {
        // used for serialization
    }

    /**
     * Constructs a new {@link PropertyDTO} with the given name and value.
     *
     * @param name name
     * @param value value
     */
    public PropertyDTO(String name, Object value) {
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
}
