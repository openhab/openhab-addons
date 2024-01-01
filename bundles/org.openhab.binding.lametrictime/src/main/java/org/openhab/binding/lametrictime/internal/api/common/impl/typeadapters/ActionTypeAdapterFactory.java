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
package org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported.CustomizedTypeAdapterFactory;
import org.openhab.binding.lametrictime.internal.api.local.dto.Action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Adapter factory for actions.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class ActionTypeAdapterFactory extends CustomizedTypeAdapterFactory<Action> {
    private static final String PROPERTY_ID = "id";
    private static final String PROPERTY_PARAMETERS = "params";

    public ActionTypeAdapterFactory() {
        super(Action.class);
    }

    @Override
    protected void beforeWrite(Action source, JsonElement toSerialize) {
        if (toSerialize == null || toSerialize.isJsonNull()) {
            return;
        }

        JsonObject actionObj = toSerialize.getAsJsonObject();
        if (actionObj == null || actionObj.isJsonNull()) {
            return;
        }

        // rewrite parameters from a nested object (map) to properties on the action
        JsonElement paramsElem = actionObj.get(PROPERTY_PARAMETERS);
        if (paramsElem != null && !paramsElem.isJsonNull()) {
            JsonObject paramsObj = paramsElem.getAsJsonObject();
            actionObj.remove(PROPERTY_PARAMETERS);

            for (Entry<String, JsonElement> entry : paramsObj.entrySet()) {
                actionObj.add(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    protected void afterRead(@Nullable JsonElement deserialized) {
        if (deserialized == null || deserialized.isJsonNull()) {
            return;
        }

        JsonObject actionObj = deserialized.getAsJsonObject();
        if (actionObj == null || actionObj.isJsonNull()) {
            return;
        }

        if (actionObj.has(PROPERTY_PARAMETERS)) {
            throw new IllegalArgumentException(
                    "Attempting to deserialize Action that contains a colliding " + PROPERTY_PARAMETERS + " property");
        }

        // temporary list of field names
        List<String> fields = new ArrayList<>();

        // rewrite parameters to a nested object (map)
        JsonObject paramsObj = new JsonObject();
        for (Entry<String, JsonElement> entry : actionObj.entrySet()) {
            // skip ID field
            if (PROPERTY_ID.equals(entry.getKey())) {
                continue;
            }

            String paramId = entry.getKey();
            fields.add(paramId); // to be removed later

            paramsObj.add(paramId, entry.getValue());
        }
        actionObj.add(PROPERTY_PARAMETERS, paramsObj);

        // remove all fields other than the list
        fields.forEach(field -> actionObj.remove(field));
    }
}
