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
package org.openhab.binding.hue.internal.api.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.Archetype;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.CategoryType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 product metadata.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class MetaData {
    private @Nullable String archetype;
    private @Nullable String name;
    private @Nullable @SerializedName("control_id") Integer controlId;
    private @Nullable String category;

    public Archetype getArchetype() {
        return Archetype.of(archetype);
    }

    public @Nullable String getName() {
        return name;
    }

    public CategoryType getCategory() {
        return CategoryType.of(category);
    }

    public int getControlId() {
        Integer controlId = this.controlId;
        return controlId != null ? controlId.intValue() : 0;
    }
}
