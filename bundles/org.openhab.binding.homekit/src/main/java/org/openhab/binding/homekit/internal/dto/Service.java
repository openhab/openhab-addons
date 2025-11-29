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

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.CharacteristicType;
import org.openhab.binding.homekit.internal.enums.ServiceType;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.osgi.framework.Bundle;

import com.google.gson.JsonElement;

/**
 * HomeKit service DTO.
 * Used to deserialize individual services from the /accessories endpoint of a HomeKit bridge.
 * Each service has a type, instance ID (iid), and a list of characteristics.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Service {
    public @NonNullByDefault({}) String type; // e.g. '96' => 'public.hap.service.battery'
    public @NonNullByDefault({}) Long iid; // e.g. 10
    public @NonNullByDefault({}) String name;
    public @NonNullByDefault({}) List<Characteristic> characteristics;
    public @NonNullByDefault({}) Boolean primary;

    /**
     * Builds a ChannelGroupDefinition and a ChannelGroupType based on the service properties.
     * Registers the ChannelGroupType with the provided HomekitTypeProvider.
     * Returns a ChannelGroupDefinition that is specific instance of ChannelGroupType.
     * Returns null if the service type is unknown or if no valid channel definitions can be created.
     *
     * @param thingUID the ThingUID to associate the ChannelGroupDefinition with
     * @param typeProvider the HomekitStorageBasedTypeProvider to register the channel group type with
     * @return the created ChannelGroupDefinition or null if creation failed
     */
    public @Nullable ChannelGroupDefinition buildAndRegisterChannelGroupDefinition(ThingUID thingUID,
            HomekitTypeProvider typeProvider, TranslationProvider i18nProvider, Bundle bundle) {
        ServiceType serviceType = getServiceType();
        if (serviceType == null || ServiceType.ACCESSORY_INFORMATION == serviceType) {
            return null;
        }

        List<ChannelDefinition> channelDefinitions = characteristics.stream()
                .map(c -> c.buildAndRegisterChannelDefinition(thingUID, typeProvider, i18nProvider, bundle))
                .filter(Objects::nonNull).toList();

        if (channelDefinitions.isEmpty()) {
            return null;
        }

        String serviceIdentifier = serviceType.getOpenhabType();
        String channelGroupTypeIdentifier = thingUID.getBridgeIds().isEmpty()
                ? CHANNEL_GROUP_TYPE_ID_FMT.formatted(serviceIdentifier, iid, thingUID.getId(), "1")
                : CHANNEL_GROUP_TYPE_ID_FMT.formatted(serviceIdentifier, iid, thingUID.getBridgeIds().getFirst(),
                        thingUID.getId());
        ChannelGroupTypeUID channelGroupTypeUID = new ChannelGroupTypeUID(BINDING_ID, channelGroupTypeIdentifier);

        String channelGroupTypeLabel = serviceType.toString();

        ChannelGroupType channelGroupType = ChannelGroupTypeBuilder.instance(channelGroupTypeUID, channelGroupTypeLabel) //
                .withChannelDefinitions(channelDefinitions) //
                .build();

        // persist the group _type_, and return the definition of a specific _instance_ of that type
        typeProvider.putChannelGroupType(channelGroupType);
        return new ChannelGroupDefinition(serviceType.getOpenhabType(), channelGroupTypeUID,
                getChannelGroupInstanceLabel(), null);
    }

    /*
     * Returns the 'name' field if it is present. Otherwise searches for a characterstic of type
     * CharacteristicType.NAME and if present returns that value. Otherwise returns the service
     * type in Title Case..
     */
    public String getChannelGroupInstanceLabel() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (characteristics instanceof List<Characteristic> characteristics) {
            for (Characteristic c : characteristics) {
                if (c.getCharacteristicType() == CharacteristicType.NAME) {
                    if (c.value instanceof JsonElement v && v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                        return v.getAsJsonPrimitive().getAsString();
                    }
                }
            }
        }
        return Objects.requireNonNull(getServiceType()).toString();
    }

    public @Nullable ServiceType getServiceType() {
        try {
            // convert "00000113-0000-1000-8000-0026BB765291" to "00000113"
            String firstPart = type.split("-")[0];
            return ServiceType.from(Integer.parseUnsignedInt(firstPart, 16));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public @Nullable Characteristic getCharacteristic(Long iid) {
        return characteristics.stream().filter(c -> iid.equals(c.iid)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return getServiceType() instanceof ServiceType st ? st.getType() : "Unknown";
    }
}
