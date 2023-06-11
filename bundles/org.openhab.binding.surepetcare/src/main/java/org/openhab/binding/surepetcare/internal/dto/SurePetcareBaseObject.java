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
package org.openhab.binding.surepetcare.internal.dto;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SurePetcareBaseObject} is the Java class used as a base DTO for other primary JSON objects.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareBaseObject {

    public Long id = 0L;
    public String version = "";
    public ZonedDateTime createdAt = ZonedDateTime.now();
    public ZonedDateTime updatedAt = ZonedDateTime.now();

    public Map<String, String> getThingProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", id.toString());
        properties.put("version", version);
        properties.put("createdAt", createdAt.toString());
        properties.put("updatedAt", updatedAt.toString());
        return properties;
    }

    public SurePetcareBaseObject assign(SurePetcareBaseObject newdev) {
        this.id = newdev.id;
        this.version = newdev.version;
        this.createdAt = newdev.createdAt;
        this.updatedAt = newdev.updatedAt;
        return this;
    }
}
