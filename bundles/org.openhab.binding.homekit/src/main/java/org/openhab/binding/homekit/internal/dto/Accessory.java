/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.dto;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.AccessoryType;
import org.openhab.binding.homekit.internal.provider.HomekitTypeProvider;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Equipment;
import org.openhab.core.thing.type.ChannelGroupDefinition;

import com.google.gson.annotations.SerializedName;

/**
 * HomeKit accessory DTO
 * Used to deserialize individual accessories from the /accessories endpoint of a HomeKit bridge.
 * Each accessory has an accessory ID (aid) and a list of services.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Accessory {
    public @NonNullByDefault({}) @SerializedName("aid") Integer accessoryId; // e.g. 1
    public @NonNullByDefault({}) List<Service> services;

    @Override
    public String toString() {
        return getAccessoryType().toString();
    }

    public AccessoryType getAccessoryType() {
        Integer aid = this.accessoryId;
        if (aid == null) {
            return AccessoryType.OTHER;
        }
        return AccessoryType.from(aid);
    }

    /**
     * Maps the accessory type to a corresponding semantic equipment tag.
     * Returns null if there is no suitable mapping.
     *
     * @return the corresponding SemanticTag or null if none exists
     */
    public @Nullable SemanticTag getSemanticEquipmentTag() {
        switch (getAccessoryType()) {
            case BRIDGE:
                return Equipment.NETWORK_APPLIANCE;
            case FAN:
                return Equipment.FAN;
            case OUTLET:
                return Equipment.POWER_OUTLET;
            case SWITCH:
                return Equipment.CONTROL_DEVICE;
            case THERMOSTAT:
                return Equipment.THERMOSTAT;
            case WINDOW:
                return Equipment.WINDOW;
            case WINDOW_COVERING:
                return Equipment.WINDOW_COVERING;
            case DOOR:
                return Equipment.DOOR;
            case AIR_PURIFIER:
                return Equipment.AIR_FILTER;
            case AIR_CONDITIONER:
                return Equipment.AIR_CONDITIONER;
            case SECURITY_SYSTEM:
                return Equipment.ALARM_SYSTEM;
            case SENSOR:
                return Equipment.SENSOR;
            case AIRPORT:
                return Equipment.NETWORK_APPLIANCE;
            case APPLE_TV:
                return Equipment.MEDIA_PLAYER;
            case DEHUMIDIFIER:
                return Equipment.DEHUMIDIFIER;
            case DOOR_LOCK:
                return Equipment.LOCK;
            case FAUCET:
                return Equipment.HOT_WATER_FAUCET;
            case GARAGE_DOOR:
                return Equipment.GARAGE_DOOR;
            case HEATER:
                return Equipment.HVAC;
            case HUMIDIFIER:
                return Equipment.HUMIDIFIER;
            case IP_CAMERA:
                return Equipment.CAMERA;
            case LIGHTING:
                return Equipment.LIGHT_SOURCE;
            case PROGRAMMABLE_SWITCH:
                return Equipment.CONTROL_DEVICE;
            case REMOTE:
                return Equipment.REMOTE_CONTROL;
            case SHOWER_HEAD:
                return Equipment.SHOWER;
            case SPEAKER:
                return Equipment.SPEAKER;
            case SPRINKLER:
                return Equipment.IRRIGATION;
            case TELEVISION:
                return Equipment.TELEVISION;
            case VIDEO_DOORBELL:
                return Equipment.DOORBELL;
            case OTHER:
            case RESERVED:
        }
        return null;
    }

    /**
     * Builds and registers channel group definitions for all services of this accessory.
     * Services that do not map to a channel group definition are ignored.
     *
     * @param typeProvider the HomeKit type provider used to look up channel group definitions
     * @return a list of channel group definitions for the services of this accessory
     */
    public List<ChannelGroupDefinition> buildAndRegisterChannelGroupDefinitions(HomekitTypeProvider typeProvider) {
        return services.stream().map(s -> s.buildAndRegisterChannelGroupDefinition(typeProvider))
                .filter(Objects::nonNull).toList();
    }
}
