/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.domain.dto;

import com.google.gson.JsonElement;

/**
 * DTO class to encapsulate the tag values from the energy manager.
 *
 * Properties without setters are only filled by gson JSON parsing.
 * The concrete tag values can be anything. {@link com.google.gson.JsonPrimitive},
 * {@link com.google.gson.JsonObject} or even strings containing json structures.
 *
 * @author Sven Carstens - Initial contribution
 */
public class TagValueDTO {
    private String tagName;
    private String guid;
    private JsonElement value;

    public String getTagName() {
        return this.tagName;
    }

    public String getGuid() {
        return this.guid;
    }

    public JsonElement getValue() {
        return this.value;
    }
}
