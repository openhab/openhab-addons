/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client.entity;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * Defines a {@link Location} structure.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Location extends ConfigPropertyList {
    private static final String CONFIG_PROPERTY_TYPE = "Type";

    /**
     * Identifier of the location – must be unique.
     */
    @Key("id")
    private String id;

    /**
     * Reference to the description of the message.
     * Optional.
     */
    @Key("desc")
    private String desc;

    /**
     * Container for tagging the location, e.g. if the location is on a certain floor in the house.
     * Optional.
     */
    @Key("Tags")
    private List<Property> tagsList;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @return the tagsList
     */
    public List<Property> getTagsList() {
        return tagsList;
    }

    /**
     * @param tagsList the tagsList to set
     */
    public void setTagsList(List<Property> tagsList) {
        this.tagsList = tagsList;
    }

    @Override
    public String getName() {
        return getPropertyValueAsString(CONFIG_PROPERTY_NAME);
    }

    public String getType() {
        return getPropertyValueAsString(CONFIG_PROPERTY_TYPE);
    }
}
