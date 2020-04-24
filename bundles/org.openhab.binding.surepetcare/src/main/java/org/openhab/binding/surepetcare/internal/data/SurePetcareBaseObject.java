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
package org.openhab.binding.surepetcare.internal.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link SurePetcareBaseObject} is the Java class used as a base DTO for other primary JSON objects.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareBaseObject {

    protected Integer id;
    protected String version;
    protected Date createdAt;
    protected Date updatedAt;

    public SurePetcareBaseObject() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public @NonNull Map<String, Object> getThingProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("id", id.toString());
        properties.put("version", version);
        return properties;
    }

}
