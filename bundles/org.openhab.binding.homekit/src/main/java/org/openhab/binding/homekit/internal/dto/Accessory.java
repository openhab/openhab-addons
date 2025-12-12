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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public @NonNullByDefault({}) Long aid; // e.g. 1
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
     * Each nested service registers a ChannelGroupType and returns a ChannelGroupDefinition thereof.
     * Each sub-nested characteristic registers a ChannelType and returns a ChannelDefinition thereof.
     * Nested services that do not map to a channel group definition are ignored.
     * Sub-nested characteristics that do not map to a channel definition are ignored.
     *
     * @param thingUID the ThingUID to associate the ChannelGroupDefinitions with
     * @param typeProvider the HomeKit type provider used to look up channel group definitions.
     * @return a list of channel group definition instances for the services of this accessory.
     */
    public List<ChannelGroupDefinition> getChannelGroupDefinitions(ThingUID thingUID, HomekitTypeProvider typeProvider,
            TranslationProvider i18nProvider, Bundle bundle) {
        return services.stream().map(s -> s.getChannelGroupDefinition(thingUID, typeProvider, i18nProvider, bundle))
                .filter(Objects::nonNull).toList();
    }

    /**
     * Returns a property map from all characteristics of all services. In which if multiple characteristics
     * provide the same property name, their values are concatenated. This may for example occur if an accessory
     * hosts multiple services each having a characteristic for e.g. a "name" property.
     *
     * DEVELOPER NOTE: strictly speaking merging "name" properties from multiple characteristics is somewhat
     * dubious, since in reality each is the name of a channel-group and neither is the name of the thing. But
     * we are ignoring this for the time being.
     */
    public Map<String, String> getProperties(ThingUID thingUID, HomekitTypeProvider typeProvider,
            TranslationProvider i18nProvider, Bundle bundle) {
        return services.stream()
                .flatMap(s -> s.getProperties(thingUID, typeProvider, i18nProvider, bundle).entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                        (v1, v2) -> v1.contains(v2) ? v1 : v1 + ", " + v2, LinkedHashMap::new));
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
        return getSemanticEquipmentTag(getAccessoryType());
    }

    public @Nullable SemanticTag getSemanticEquipmentTag(AccessoryCategory accessoryCategory) {
        switch (accessoryCategory) {
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
     * Returns the SemanticTag of the accessory by parsing its services, or null if no match.
     */
    public @Nullable SemanticTag getSemanticEquipmentTagFromServices() {
        for (Service service : services) {
            ServiceType serviceType = service.getServiceType();
            if (serviceType != null) {
                switch (serviceType) {
                    case GARAGE_DOOR_OPENER:
                        return Equipment.GARAGE_DOOR;
                    case LIGHT_BULB:
                        return Equipment.LIGHT_SOURCE;
                    case LOCK_MANAGEMENT:
                    case LOCK_MECHANISM:
                        return Equipment.LOCK;
                    case OUTLET:
                        return Equipment.POWER_OUTLET;
                    case SWITCH:
                        return Equipment.CONTROL_DEVICE;
                    case THERMOSTAT:
                        return Equipment.THERMOSTAT;
                    case SENSOR_AIR_QUALITY:
                    case SENSOR_CARBON_DIOXIDE:
                    case SENSOR_CARBON_MONOXIDE:
                    case SENSOR_CONTACT:
                    case SENSOR_HUMIDITY:
                    case SENSOR_LEAK:
                    case SENSOR_LIGHT:
                    case SENSOR_MOTION:
                    case SENSOR_OCCUPANCY:
                    case SENSOR_SMOKE:
                    case SENSOR_TEMPERATURE:
                        return Equipment.SENSOR;
                    case SECURITY_SYSTEM:
                        return Equipment.ALARM_SYSTEM;
                    case DOOR:
                        return Equipment.DOOR;
                    case WINDOW:
                        return Equipment.WINDOW;
                    case WINDOW_COVERING:
                        return Equipment.WINDOW_COVERING;
                    case AIR_PURIFIER:
                        return Equipment.AIR_FILTER;
                    case HEATER_COOLER:
                        return Equipment.HVAC;
                    case HUMIDIFIER_DEHUMIDIFIER:
                        return Equipment.HUMIDIFIER;
                    case FAUCET:
                        return Equipment.HOT_WATER_FAUCET;
                    case SPEAKER:
                    case SMART_SPEAKER:
                        return Equipment.SPEAKER;
                    case TELEVISION:
                        return Equipment.TELEVISION;
                    case AUDIO_STREAM_MANAGEMENT:
                        return Equipment.AUDIO_VISUAL;
                    case BATTERY:
                        return Equipment.BATTERY;
                    case CAMERA_RTP_STREAM_MANAGEMENT:
                        return Equipment.CAMERA;
                    case DOORBELL:
                        return Equipment.DOORBELL;
                    case FAN:
                    case FANV2:
                        return Equipment.FAN;
                    case FILTER_MAINTENANCE:
                        return Equipment.AIR_FILTER;
                    case IRRIGATION_SYSTEM:
                        return Equipment.IRRIGATION;
                    case SIRI:
                        return Equipment.VOICE_ASSISTANT;
                    case STATELESS_PROGRAMMABLE_SWITCH:
                        return Equipment.CONTROL_DEVICE;
                    case VALVE:
                        return Equipment.VALVE;
                    case VERTICAL_SLAT:
                        return Equipment.WINDOW_COVERING;
                    default:
                        break;
                }
            }
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

    public @Nullable Service getService(Long iid) {
        return services.stream().filter(s -> iid.equals(s.iid)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return getAccessoryType().toString();
    }
}
