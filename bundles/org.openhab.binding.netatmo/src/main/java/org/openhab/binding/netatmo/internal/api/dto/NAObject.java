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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NAObject} class is the base class for all objects
 * returned by the Netatmo API.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NAObject {
    @SerializedName(value = "id", alternate = { "program_id", "_id", "event_id" })
    protected String id = "";
    @SerializedName(value = "name", alternate = { "module_name", "station_name", "pseudo", "message", "key" })
    protected @Nullable String description;
    private boolean ignoredForThingUpdate;

    public String getId() {
        return id;
    }

    public @Nullable String getName() {
        return description;
    }

    public boolean isIgnoredForThingUpdate() {
        return ignoredForThingUpdate;
    }

    public void setIgnoredForThingUpdate(boolean ignoredForThingUpdate) {
        this.ignoredForThingUpdate = ignoredForThingUpdate;
    }
}
