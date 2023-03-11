/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported.CustomizedTypeAdapterFactory;
import org.openhab.binding.lametrictime.internal.api.local.dto.Application;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Adapter factory for applications.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class ApplicationTypeAdapterFactory extends CustomizedTypeAdapterFactory<Application> {
    private static final String PROPERTY_ID = "id";
    private static final String PROPERTY_WIDGETS = "widgets";
    private static final String PROPERTY_ACTIONS = "actions";

    public ApplicationTypeAdapterFactory() {
        super(Application.class);
    }

    @Override
    protected void beforeWrite(Application source, JsonElement toSerialize) {
        if (toSerialize == null || toSerialize.isJsonNull()) {
            return;
        }

        JsonObject appObj = toSerialize.getAsJsonObject();
        if (appObj == null || appObj.isJsonNull()) {
            return;
        }

        // remove widget IDs
        JsonElement widgetsElem = appObj.get(PROPERTY_WIDGETS);
        if (widgetsElem != null && !widgetsElem.isJsonNull()) {
            for (Entry<String, JsonElement> entry : widgetsElem.getAsJsonObject().entrySet()) {
                JsonElement widgetElem = entry.getValue();
                if (widgetElem == null || widgetElem.isJsonNull()) {
                    continue;
                }
                widgetElem.getAsJsonObject().remove(PROPERTY_ID);
            }
        }

        // remove action IDs
        JsonElement actionsElem = appObj.get(PROPERTY_ACTIONS);
        if (actionsElem != null && !actionsElem.isJsonNull()) {
            for (Entry<String, JsonElement> entry : actionsElem.getAsJsonObject().entrySet()) {
                JsonElement actionElem = entry.getValue();
                if (actionElem == null || actionElem.isJsonNull()) {
                    continue;
                }
                actionElem.getAsJsonObject().remove(PROPERTY_ID);
            }
        }
    }

    @Override
    protected void afterRead(@Nullable JsonElement deserialized) {
        if (deserialized == null || deserialized.isJsonNull()) {
            return;
        }

        JsonObject appObj = deserialized.getAsJsonObject();
        if (appObj == null || appObj.isJsonNull()) {
            return;
        }

        // inject widget IDs
        JsonElement widgetsElem = appObj.get(PROPERTY_WIDGETS);
        if (widgetsElem != null && !widgetsElem.isJsonNull()) {
            for (Entry<String, JsonElement> entry : widgetsElem.getAsJsonObject().entrySet()) {
                JsonElement widgetElem = entry.getValue();
                if (widgetElem == null || widgetElem.isJsonNull()) {
                    continue;
                }
                widgetElem.getAsJsonObject().addProperty(PROPERTY_ID, entry.getKey());
            }
        }

        // inject action IDs
        JsonElement actionsElem = appObj.get(PROPERTY_ACTIONS);
        if (actionsElem != null && !actionsElem.isJsonNull()) {
            for (Entry<String, JsonElement> entry : actionsElem.getAsJsonObject().entrySet()) {
                JsonElement actionElem = entry.getValue();
                if (actionElem == null || actionElem.isJsonNull()) {
                    continue;
                }
                actionElem.getAsJsonObject().addProperty(PROPERTY_ID, entry.getKey());
            }
        }
    }
}
