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
import org.openhab.binding.lametrictime.api.local.model.Application;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ApplicationTypeAdapterFactory extends CustomizedTypeAdapterFactory<Application>
{
    private static final String PROPERTY_ID = "id";
    private static final String PROPERTY_WIDGETS = "widgets";
    private static final String PROPERTY_ACTIONS = "actions";

    public ApplicationTypeAdapterFactory()
    {
        super(Application.class);
    }

    @Override
    protected void beforeWrite(Application source, JsonElement toSerialize)
    {
        if (toSerialize == null || toSerialize.isJsonNull())
        {
            return;
        }

        JsonObject appObj = toSerialize.getAsJsonObject();
        if (appObj == null || appObj.isJsonNull())
        {
            return;
        }

        // remove widget IDs
        JsonElement widgetsElem = appObj.get(PROPERTY_WIDGETS);
        if (widgetsElem != null && !widgetsElem.isJsonNull())
        {
            for (Entry<String, JsonElement> entry : widgetsElem.getAsJsonObject().entrySet())
            {
                JsonElement widgetElem = entry.getValue();
                if (widgetElem == null || widgetElem.isJsonNull())
                {
                    continue;
                }
                widgetElem.getAsJsonObject().remove(PROPERTY_ID);
            }
        }

        // remove action IDs
        JsonElement actionsElem = appObj.get(PROPERTY_ACTIONS);
        if (actionsElem != null && !actionsElem.isJsonNull())
        {
            for (Entry<String, JsonElement> entry : actionsElem.getAsJsonObject().entrySet())
            {
                JsonElement actionElem = entry.getValue();
                if (actionElem == null || actionElem.isJsonNull())
                {
                    continue;
                }
                actionElem.getAsJsonObject().remove(PROPERTY_ID);
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

        JsonObject appObj = deserialized.getAsJsonObject();
        if (appObj == null || appObj.isJsonNull())
        {
            return;
        }

        // inject widget IDs
        JsonElement widgetsElem = appObj.get(PROPERTY_WIDGETS);
        if (widgetsElem != null && !widgetsElem.isJsonNull())
        {
            for (Entry<String, JsonElement> entry : widgetsElem.getAsJsonObject().entrySet())
            {
                JsonElement widgetElem = entry.getValue();
                if (widgetElem == null || widgetElem.isJsonNull())
                {
                    continue;
                }
                widgetElem.getAsJsonObject().addProperty(PROPERTY_ID, entry.getKey());
            }
        }

        // inject action IDs
        JsonElement actionsElem = appObj.get(PROPERTY_ACTIONS);
        if (actionsElem != null && !actionsElem.isJsonNull())
        {
            for (Entry<String, JsonElement> entry : actionsElem.getAsJsonObject().entrySet())
            {
                JsonElement actionElem = entry.getValue();
                if (actionElem == null || actionElem.isJsonNull())
                {
                    continue;
                }
                actionElem.getAsJsonObject().addProperty(PROPERTY_ID, entry.getKey());
            }
        }
    }
}
