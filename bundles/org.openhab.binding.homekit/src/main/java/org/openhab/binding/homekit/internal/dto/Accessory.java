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
import org.openhab.binding.homekit.internal.enums.AccessoryCategory;
import org.openhab.binding.homekit.internal.enums.CharacteristicType;
import org.openhab.binding.homekit.internal.enums.ServiceType;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Equipment;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.osgi.framework.Bundle;

import com.google.gson.JsonElement;

/**
 * HomeKit accessory DTO
 * Used to deserialize individual accessories from the /accessories endpoint of a HomeKit bridge.
 * Each accessory has an accessory ID (aid) and a list of services.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Accessory {
    public @NonNullByDefault({}) Integer aid; // e.g. 1
    public @NonNullByDefault({}) List<Service> services;
    public @NonNullByDefault({}) String name;
    public @NonNullByDefault({}) String manufacturer;
    public @NonNullByDefault({}) String model;
    public @NonNullByDefault({}) String serialNumber;
    public @NonNullByDefault({}) String firmwareRevision;
    public @NonNullByDefault({}) String hardwareRevision;
    public @NonNullByDefault({}) Integer category;

    /**
     * Builds and registers channel group definitions for all services of this accessory.
     * Each child service registers a ChannelGroupType and returns a ChannelGroupDefinition thereof.
     * Each grandchild category registers a ChannelType and returns a ChannelDefinition thereof.
     * Child services that do not map to a channel group definition are ignored.
     * Grandchild categories that do not map to a channel definition are ignored.
     *
     * @param thingUID the ThingUID to associate the ChannelGroupDefinitions with
     * @param typeProvider the HomeKit type provider used to look up channel group definitions.
     * @return a list of channel group definition instances for the services of this accessory.
     */
    public List<ChannelGroupDefinition> buildAndRegisterChannelGroupDefinitions(ThingUID thingUID,
            HomekitTypeProvider typeProvider, TranslationProvider i18nProvider, Bundle bundle) {
        return services.stream()
                .map(s -> s.buildAndRegisterChannelGroupDefinition(thingUID, typeProvider, i18nProvider, bundle))
                .filter(Objects::nonNull).toList();
    }

    public AccessoryCategory getAccessoryType() {
        Integer category = this.category;
        if (category == null) {
            return AccessoryCategory.OTHER;
        }
        return AccessoryCategory.from(category);
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
            case AUDIO_RECEIVER:
                return Equipment.RECEIVER;
            case RANGE_EXTENDER:
                return Equipment.NETWORK_APPLIANCE;
            case ROUTER:
                return Equipment.ROUTER;
            case SMART_SPEAKER:
                return Equipment.SPEAKER;
            case TV_SET_TOP_BOX:
            case TV_STREAMING_STICK:
                return Equipment.MEDIA_PLAYER;
            case OTHER:
                break;
        }
        return null;
    }

    /**
     * Gets the label for this accessory instance. If the accessory has a non-blank name, that is returned. Otherwise,
     * if the accessory has an Accessory Information service with a Name characteristic, that is returned. Otherwise,
     * the accessory type is returned in Title Case.
     */
    public String getAccessoryInstanceLabel() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (services instanceof List<Service> serviceList) {
            for (Service s : serviceList) {
                if (s.getServiceType() == ServiceType.ACCESSORY_INFORMATION) {
                    if (s.characteristics instanceof List<Characteristic> characteristics) {
                        for (Characteristic c : characteristics) {
                            if (c.getCharacteristicType() == CharacteristicType.NAME) {
                                if (c.value instanceof JsonElement v && v.isJsonPrimitive()
                                        && v.getAsJsonPrimitive().isString()) {
                                    return v.getAsJsonPrimitive().getAsString();
                                }
                            }
                        }
                    }
                }
            }
        }
        return toString();
    }

    public @Nullable Service getService(Integer iid) {
        return services.stream().filter(s -> iid.equals(s.iid)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return getAccessoryType().toString();
    }
}
