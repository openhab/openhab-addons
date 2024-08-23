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

import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported.CustomizedTypeAdapterFactory;
import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Adapter factory for update actions.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class UpdateActionTypeAdapterFactory extends CustomizedTypeAdapterFactory<UpdateAction> {
    private static final String PROPERTY_PARAMETERS = "params";
    private static final String PROPERTY_VALUE = "value";

    public UpdateActionTypeAdapterFactory() {
        super(UpdateAction.class);
    }

    @Override
    protected void beforeWrite(UpdateAction source, JsonElement toSerialize) {
        if (toSerialize == null || toSerialize.isJsonNull()) {
            return;
        }

        JsonObject actionObj = toSerialize.getAsJsonObject();
        if (actionObj == null || actionObj.isJsonNull()) {
            return;
        }

        // rewrite parameters map from {name => Parameter} to {name => value}
        JsonElement paramsElem = actionObj.get(PROPERTY_PARAMETERS);
        if (paramsElem != null && !paramsElem.isJsonNull()) {
            JsonObject paramsObj = paramsElem.getAsJsonObject();
            actionObj.remove(PROPERTY_PARAMETERS);

            JsonObject newParamsObj = new JsonObject();
            for (Entry<String, JsonElement> entry : paramsObj.entrySet()) {
                newParamsObj.add(entry.getKey(), entry.getValue().getAsJsonObject().getAsJsonPrimitive(PROPERTY_VALUE));
            }
            actionObj.add(PROPERTY_PARAMETERS, newParamsObj);
        }
    }

    @Override
    protected void afterRead(@Nullable JsonElement deserialized) {
        throw new UnsupportedOperationException(UpdateAction.class.getName() + " cannot be derialized");
    }
}
