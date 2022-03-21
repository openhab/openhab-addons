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
package org.openhab.binding.miele.internal.api.dto;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link DeviceMetaData} class represents the Metadata node in the response JSON.
 *
 * @author Jacob Laursen - Initial contribution
 */
public class DeviceMetaData {
    public String Filter;
    public String description;
    public String LocalizedID;
    public String LocalizedValue;
    public JsonObject MieleEnum;
    public String access;

    public String getMieleEnum(String s) {
        if (this.MieleEnum == null) {
            return null;
        }

        for (Entry<String, JsonElement> enumEntry : this.MieleEnum.entrySet()) {
            if (enumEntry.getValue().getAsString().trim().equals(s.trim())) {
                return enumEntry.getKey();
            }
        }

        return null;
    }
}
