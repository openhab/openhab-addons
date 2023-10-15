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
package org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl;

import java.util.Iterator;

import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.BaseSensorValues;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.ZoneIdentifier;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link SensorValues} acts as container for the digitalSTROM json-method <i>getSensorValues</i>. So the
 * {@link SensorValues} contains all
 * {@link org.openhab.binding.digitalstrom.internal.lib.climate.datatypes.CachedSensorValue}s of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SensorValues extends BaseSensorValues implements ZoneIdentifier {

    private Integer zoneID;
    private String zoneName;

    /**
     * Creates a new {@link SensorValues} through the {@link JsonObject} that will be returned by an apartment call.
     *
     * @param jObject must not be null
     */
    public SensorValues(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.zoneID = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.zoneName = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        init(jObject);
    }

    /**
     * Creates a new {@link SensorValues} through the {@link JsonObject} which will be returned by a zone call.
     * Because of zone calls does not include a zoneID or zoneName in the json response, the zoneID and zoneName have to
     * be handed over the constructor.
     *
     * @param jObject must not be null
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public SensorValues(JsonObject jObject, Integer zoneID, String zoneName) {
        this.zoneID = zoneID;
        this.zoneName = zoneName;
        init(jObject);
    }

    private void init(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.VALUES.getKey()).isJsonArray()) {
            JsonArray jArray = jObject.get(JSONApiResponseKeysEnum.VALUES.getKey()).getAsJsonArray();
            if (jArray.size() != 0) {
                Iterator<JsonElement> iter = jArray.iterator();
                while (iter.hasNext()) {
                    JsonObject cachedSensorValue = iter.next().getAsJsonObject();
                    super.addSensorValue(cachedSensorValue, false);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SensorValues [zoneID=" + zoneID + ", zoneName=" + zoneName + ", " + super.toString() + "]";
    }

    @Override
    public Integer getZoneID() {
        return zoneID;
    }

    @Override
    public String getZoneName() {
        return zoneName;
    }
}
