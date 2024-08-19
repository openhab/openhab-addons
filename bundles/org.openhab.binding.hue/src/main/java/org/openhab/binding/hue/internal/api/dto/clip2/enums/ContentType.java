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
package org.openhab.binding.hue.internal.api.dto.clip2.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Enum for content type of Resource instances
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum ContentType {
    @SerializedName("add") // resource being added; contains (maybe) all fields
    ADD,
    @SerializedName("delete") // resource being deleted; contains id and type only
    DELETE,
    @SerializedName("update") // resource being updated; contains id, type and updated fields
    UPDATE,
    @SerializedName("error") // resource error event
    ERROR,
    // existing resource being downloaded; contains all fields; excluded from (de-)serialization
    FULL_STATE
}
