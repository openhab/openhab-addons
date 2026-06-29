/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.handler.RachioZoneHandler;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RachioZone} stores zone state and Thing linkage for one Rachio irrigation zone.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioZone extends RachioCloudZone {
    private final Logger logger = LoggerFactory.getLogger(RachioZone.class);
    private @Nullable ThingUID devUID;
    private @Nullable ThingUID zoneUID;
    private @Nullable RachioZoneHandler thingHandler;
    private String uniqueId = "";

    private int startRunTime = 0;
    private String lastEvent = "";
    private @Nullable DateTimeType lastEventTime;
    private double moistureLevel = Double.NaN;
    private double moisturePercent = Double.NaN;
    private String imageDownloadUrl = "";

    /**
     * Use reflection to shallow copy simple type fields with matching names from one object to another
     *
     * @param fromObj the object to copy from
     * @param toObj the object to copy to
     */
    public RachioZone(RachioCloudZone zone, String uniqueId) {
        try {
            RachioApi.copyMatchingFields(zone, this);
            imageDownloadUrl = zone.imageUrl;
            if (zone.imageUrl.startsWith(SERVLET_IMAGE_URL_BASE)) {
                // when trying to load the imageUrl Rachio doesn't add a ".png" and doesn't set the mime type. As a
                // result the binding provides a servlet, which acts like a proxy. We redirect the load request to the
                // local servlet. The servlet loads the provided image and then writes it as binary data to the output
                // stream with the correct mime type.
                String uri = zone.imageUrl.substring(zone.imageUrl.lastIndexOf("/"));
                if (!uri.isEmpty()) {
                    this.imageUrl = SERVLET_IMAGE_PATH + uri;
                    logger.trace("Zone image URL rewritten to local image servlet path for zone '{}'", name);
                }
            }

            this.uniqueId = uniqueId;
            logger.trace("Zone '{}' (number={}, id={}, enable={}) initialized.", zone.name, zone.zoneNumber, zone.id,
                    zone.enabled);
        } catch (RuntimeException e) {
            logger.warn("Unable to initialize: {}", e.getMessage());
        }
    }

    public void setThingHandler(RachioZoneHandler zoneHandler) {
        thingHandler = zoneHandler;
    }

    public @Nullable RachioZoneHandler getThingHandler() {
        return thingHandler;
    }

    public boolean compare(@Nullable RachioZone czone) {
        if (czone == null || !name.equals(czone.name) || zoneNumber != czone.zoneNumber || enabled != czone.enabled
                || availableWater != czone.availableWater || efficiency != czone.efficiency
                || lastWateredDate != czone.lastWateredDate || depthOfWater != czone.depthOfWater
                || saturatedDepthOfWater != czone.saturatedDepthOfWater
                || managementAllowedDepletion != czone.managementAllowedDepletion
                || rootZoneDepth != czone.rootZoneDepth || yardAreaSquareFeet != czone.yardAreaSquareFeet
                || scheduleDataModified != czone.scheduleDataModified || fixedRuntime != czone.fixedRuntime
                || maxRuntime != czone.maxRuntime || runtimeNoMultiplier != czone.runtimeNoMultiplier
                || runtime != czone.runtime || !imageUrl.equals(czone.imageUrl)
                || !imageDownloadUrl.equals(czone.imageDownloadUrl)) {
            return false;
        }
        return true;
    }

    public void update(RachioZone updatedZone) {
        if (!id.equalsIgnoreCase(updatedZone.id)) {
            return;
        }
        name = updatedZone.name;
        zoneNumber = updatedZone.zoneNumber;
        enabled = updatedZone.enabled;
        availableWater = updatedZone.availableWater;
        efficiency = updatedZone.efficiency;
        saturatedDepthOfWater = updatedZone.saturatedDepthOfWater;
        managementAllowedDepletion = updatedZone.managementAllowedDepletion;
        rootZoneDepth = updatedZone.rootZoneDepth;
        yardAreaSquareFeet = updatedZone.yardAreaSquareFeet;
        depthOfWater = updatedZone.depthOfWater;
        fixedRuntime = updatedZone.fixedRuntime;
        maxRuntime = updatedZone.maxRuntime;
        runtimeNoMultiplier = updatedZone.runtimeNoMultiplier;
        scheduleDataModified = updatedZone.scheduleDataModified;
        runtime = updatedZone.runtime;
        lastWateredDate = updatedZone.lastWateredDate;
        imageUrl = updatedZone.imageUrl;
        imageDownloadUrl = updatedZone.imageDownloadUrl;
    }

    public void setUID(@Nullable ThingUID deviceUID, @Nullable ThingUID zoneUID) {
        this.devUID = deviceUID;
        this.zoneUID = zoneUID;
    }

    public @Nullable ThingUID getUID() {
        return zoneUID;
    }

    public @Nullable ThingUID getDevUID() {
        return devUID;
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

    public String getImageDownloadUrl() {
        return imageDownloadUrl.isBlank() ? imageUrl : imageDownloadUrl;
    }

    public OnOffType getEnabled() {
        return enabled ? OnOffType.ON : OnOffType.OFF;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public void setEvent(String event, DateTimeType ts) {
        lastEvent = event;
        lastEventTime = ts;
    }

    public String getEvent() {
        return lastEvent;
    }

    public @Nullable DateTimeType getEventTime() {
        return lastEventTime;
    }

    public void setMoistureLevel(double moistureLevel) {
        this.moistureLevel = moistureLevel;
    }

    public double getMoistureLevel() {
        return moistureLevel;
    }

    public void setMoisturePercent(double moisturePercent) {
        this.moisturePercent = moisturePercent;
    }

    public double getMoisturePercent() {
        return moisturePercent;
    }
}
