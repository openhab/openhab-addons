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
package org.openhab.binding.livisismarthome.internal.client.api.entity.location;

/**
 * Defines a {@link LocationDTO} structure.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class LocationDTO {

    /**
     * Identifier of the location – must be unique.
     */
    private String id;

    /**
     * Configuration properties of the {@link LocationDTO}.
     */
    private LocationConfigDTO config;

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
     * @return the config
     */
    public LocationConfigDTO getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(LocationConfigDTO config) {
        this.config = config;
    }

    /**
     * @return the location name
     */
    public String getName() {
        return getConfig().getName();
    }

    /**
     * @return the location type
     */
    public String getType() {
        return getConfig().getType();
    }
}
