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

import java.util.Map.Entry;

import org.openhab.binding.lametrictime.api.common.impl.typeadapters.imported.CustomizedTypeAdapterFactory;
import org.openhab.binding.lametrictime.api.local.model.UpdateAction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UpdateActionTypeAdapterFactory extends CustomizedTypeAdapterFactory<UpdateAction>
{
    private static final String PROPERTY_PARAMETERS = "params";
    private static final String PROPERTY_VALUE = "value";

    public UpdateActionTypeAdapterFactory()
    {
        super(UpdateAction.class);
    }

    @Override
    protected void beforeWrite(UpdateAction source, JsonElement toSerialize)
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

        // rewrite parameters map from {name => Parameter} to {name => value}
        JsonElement paramsElem = actionObj.get(PROPERTY_PARAMETERS);
        if (paramsElem != null && !paramsElem.isJsonNull())
        {
            JsonObject paramsObj = paramsElem.getAsJsonObject();
            actionObj.remove(PROPERTY_PARAMETERS);

            JsonObject newParamsObj = new JsonObject();
            for (Entry<String, JsonElement> entry : paramsObj.entrySet())
            {
                newParamsObj.add(entry.getKey(),
                                 entry.getValue()
                                      .getAsJsonObject()
                                      .getAsJsonPrimitive(PROPERTY_VALUE));
            }
            actionObj.add(PROPERTY_PARAMETERS, newParamsObj);
        }
    }

    @Override
    protected void afterRead(JsonElement deserialized)
    {
        throw new UnsupportedOperationException(UpdateAction.class.getName()
                                                + " cannot be derialized");
    }
}
