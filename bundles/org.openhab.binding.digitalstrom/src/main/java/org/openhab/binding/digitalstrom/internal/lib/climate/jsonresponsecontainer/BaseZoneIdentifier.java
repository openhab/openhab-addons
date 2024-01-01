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
package org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link BaseZoneIdentifier} is a base implementation of the {@link ZoneIdentifier}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public abstract class BaseZoneIdentifier implements ZoneIdentifier {

    protected Integer zoneID;
    protected String zoneName;

    /**
     * Creates a new {@link BaseZoneIdentifier} with a zone id and zone name.
     *
     * @param zoneID must not be null
     * @param zoneName can be null
     */
    public BaseZoneIdentifier(Integer zoneID, String zoneName) {
        this.zoneID = zoneID;
        this.zoneName = zoneName;
    }

    /**
     * Creates a new {@link BaseZoneIdentifier} through the {@link JsonObject} of the response of a digitalSTROM-API
     * apartment call.
     *
     * @param jObject must not be null
     */
    public BaseZoneIdentifier(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.zoneID = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.zoneName = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
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
