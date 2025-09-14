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
import org.openhab.binding.homekit.internal.enums.ServiceType;
import org.openhab.binding.homekit.internal.persistance.HomekitTypeProvider;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * HomeKit service DTO.
 * Used to deserialize individual services from the /accessories endpoint of a HomeKit bridge.
 * Each service has a type, instance ID (iid), and a list of characteristics.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Service {
    public @NonNullByDefault({}) @SerializedName("type") String serviceId; // e.g. '96' => 'public.hap.service.battery'
    public @NonNullByDefault({}) @SerializedName("iid") Integer instanceId; // e.g. 10
    public @NonNullByDefault({}) List<Characteristic> characteristics;

    /**
     * Builds a ChannelGroupDefinition and a ChannelGroupType based on the service properties.
     * Registers the ChannelGroupType with the provided HomekitTypeProvider.
     * Returns a ChannelGroupDefinition that is specific instance of ChannelGroupType.
     * Returns null if the service type is unknown or if no valid channel definitions can be created.
     *
     * @param typeProvider the HomekitStorageBasedTypeProvider to register the channel group type with
     * @return the created ChannelGroupDefinition or null if creation failed
     */
    public @Nullable ChannelGroupDefinition buildAndRegisterChannelGroupDefinition(HomekitTypeProvider typeProvider) {
        ServiceType serviceType = ServiceType.from(Integer.parseInt(serviceId));
        try {
            serviceType = ServiceType.from(Integer.parseInt(serviceId));
        } catch (IllegalArgumentException e) {
            return null;
        }

        List<ChannelDefinition> channelDefinitions = characteristics.stream()
                .map(c -> c.buildAndRegisterChannelDefinition(typeProvider)).filter(Objects::nonNull).toList();

        if (channelDefinitions.isEmpty()) {
            return null;
        }

        ChannelGroupTypeUID groupTypeUID = new ChannelGroupTypeUID(BINDING_ID, serviceType.getChannelTypeId());
        ChannelGroupType groupType = ChannelGroupTypeBuilder.instance(groupTypeUID, GROUP_TYPE_LABEL) //
                .withDescription(serviceType.toString()) //
                .withChannelDefinitions(channelDefinitions) //
                .build();

        // persist the group _type_, and return the definition of a specific _instance_ of that type
        typeProvider.putChannelGroupType(groupType);
        return new ChannelGroupDefinition(Integer.toString(instanceId), groupTypeUID, serviceType.toString(), null);
    }
}
