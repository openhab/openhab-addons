/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lametrictime.api.common.impl.typeadapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.openhab.binding.lametrictime.api.common.impl.typeadapters.imported.CustomizedTypeAdapterFactory;
import org.openhab.binding.lametrictime.api.local.model.Action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ActionTypeAdapterFactory extends CustomizedTypeAdapterFactory<Action>
{
    private static final String PROPERTY_ID = "id";
    private static final String PROPERTY_PARAMETERS = "params";

    public ActionTypeAdapterFactory()
    {
        super(Action.class);
    }

    @Override
    protected void beforeWrite(Action source, JsonElement toSerialize)
    {
        if (toSerialize == null || toSerialize.isJsonNull())
        {
            return;
        }

        JsonObject actionObj = toSerialize.getAsJsonObject();
        if (actionObj == null || actionObj.isJsonNull())
        {
            return;
        }

        // rewrite parameters from a nested object (map) to properties on the action
        JsonElement paramsElem = actionObj.get(PROPERTY_PARAMETERS);
        if (paramsElem != null && !paramsElem.isJsonNull())
        {
            JsonObject paramsObj = paramsElem.getAsJsonObject();
            actionObj.remove(PROPERTY_PARAMETERS);

            for (Entry<String, JsonElement> entry : paramsObj.entrySet())
            {
                actionObj.add(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    protected void afterRead(JsonElement deserialized)
    {
        if (deserialized == null || deserialized.isJsonNull())
        {
            return;
        }

        JsonObject actionObj = deserialized.getAsJsonObject();
        if (actionObj == null || actionObj.isJsonNull())
        {
            return;
        }

        if (actionObj.has(PROPERTY_PARAMETERS))
        {
            throw new IllegalArgumentException("Attempting to deserialize Action that contains a colliding "
                                               + PROPERTY_PARAMETERS
                                               + " property");
        }

        // temporary list of field names
        List<String> fields = new ArrayList<>();

        // rewrite parameters to a nested object (map)
        JsonObject paramsObj = new JsonObject();
        for (Entry<String, JsonElement> entry : actionObj.entrySet())
        {
            // skip ID field
            if (PROPERTY_ID.equals(entry.getKey()))
            {
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
