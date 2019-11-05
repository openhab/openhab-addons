/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.api;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.rachio.internal.api.json.RachioCloudZone;
import org.openhab.binding.rachio.internal.handler.RachioZoneHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RachioDevice} provides zone level functions.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioZone extends RachioCloudZone {
    private final Logger        logger       = LoggerFactory.getLogger(RachioZone.class);
    @Nullable
    protected ThingUID          dev_uid;
    @Nullable
    protected ThingUID          zone_uid;
    @Nullable
    protected RachioZoneHandler thingHandler;
    protected String            uniqueId     = "";

    protected int               startRunTime = 0;

    /**
     * Use reflection to shallow copy simple type fields with matching names from one object to another
     *
     * @param fromObj the object to copy from
     * @param toObj the object to copy to
     */
    public RachioZone(RachioCloudZone zone, String uniqueId) {
        Validate.notNull(zone);
        try {
            RachioApi.copyMatchingFields(zone, this);
            if (zone.imageUrl.substring(0, SERVLET_IMAGE_URL_BASE.length()).equalsIgnoreCase(SERVLET_IMAGE_URL_BASE)) {
                // when trying to load the imageUrl Rachio doesn't add a ".png" and doesn't set the mime type. As a
                // result the binding provides a servlet, which acts like a proxy. We redirect the load request to the
                // local servlet. The serverlet loads the provided image and then writs it as binary data to the output
                // stream with the correct mime type.
                String uri = zone.imageUrl.substring(zone.imageUrl.lastIndexOf("/"));
                if (!uri.equals("")) {
                    this.imageUrl = SERVLET_IMAGE_PATH + uri;
                    logger.trace("RachioZone: imageUrl rewritten to '{}' for zone '{}'", imageUrl, name);
                }
            }

            this.uniqueId = uniqueId;
            logger.trace("RachioZone: Zone '{}' (number={}, id={}, enable={}) initialized.", zone.name, zone.zoneNumber,
                    zone.id, zone.enabled);
        } catch (Exception e) {
            logger.error("RachioZone: Unable to initialized: {}", e.getMessage());
        }
    }

    public void setThingHandler(RachioZoneHandler zoneHandler) {
        Validate.notNull(zoneHandler);
        thingHandler = zoneHandler;
    }

    @Nullable
    public RachioZoneHandler getThingHandler() {
        Validate.notNull(thingHandler);
        return thingHandler;
    }

    public boolean compare(@Nullable RachioZone czone) {
        if ((czone == null) || (zoneNumber != czone.zoneNumber) || (enabled != czone.enabled)
                || (availableWater != czone.availableWater) || (efficiency != czone.efficiency)
                || (lastWateredDate != czone.lastWateredDate) || (depthOfWater != czone.depthOfWater)
                || (runtime != czone.runtime)) {
            return false;
        }
        return true;
    } // compare()

    @SuppressWarnings("null")
    public void update(@Nullable RachioZone updatedZone) {
        Validate.notNull(updatedZone);
        if ((updatedZone == null) || !id.equalsIgnoreCase(updatedZone.id)) {
            return;
        }
        zoneNumber = updatedZone.zoneNumber;
        enabled = updatedZone.enabled;
        availableWater = updatedZone.availableWater;
        efficiency = updatedZone.efficiency;
        depthOfWater = updatedZone.depthOfWater;
        runtime = updatedZone.runtime;
        lastWateredDate = updatedZone.lastWateredDate;
    }

    public void setUID(@Nullable ThingUID deviceUID, @Nullable ThingUID zoneUID) {
        Validate.notNull(deviceUID);
        Validate.notNull(zoneUID);
        dev_uid = deviceUID;
        zone_uid = zoneUID;
    }

    @Nullable
    public ThingUID getUID() {
        return zone_uid;
    }

    @Nullable
    public ThingUID getDevUID() {
        return dev_uid;
    }

    public String getThingID() {
        // build thing name like rachio_zone_1_74C63B174B7B_7
        return uniqueId + "-" + zoneNumber;
    }

    public Map<String, String> fillProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(PROPERTY_NAME, name);
        properties.put(PROPERTY_ZONE_ID, id);
        return properties;
    }

    public OnOffType getEnabled() {
        return enabled ? OnOffType.ON : OnOffType.OFF;
    }

    public void setStartRunTime(int runtime) {
        startRunTime = runtime;
    }

    public int getStartRunTime() {
        return startRunTime;
    }

    public boolean isEnable() {
        return enabled;
    }
}
