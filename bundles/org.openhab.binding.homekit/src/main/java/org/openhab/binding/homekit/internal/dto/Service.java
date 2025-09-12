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

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.BINDING_ID;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.ServiceType;
import org.openhab.binding.homekit.internal.provider.HomekitStorageBasedTypeProvider;
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
public class Service {
    public @SerializedName("type") String serviceId; // e.g. '96' => 'public.hap.service.battery'
    public @SerializedName("iid") Integer instanceId; // e.g. 10
    public List<Characteristic> characteristics;

    /**
     * The hash only includes the invariant fields as needed to define a fully unique channel group type.
     * The instanceId is excluded as it depends on the accessory instance.
     * The characteristics are included as they define the channels within the channel group.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(serviceId, instanceId, characteristics);
    }

    /**
     * Builds a {@link ChannelGroupDefinition} and {@link ChannelGroupType} based on the service properties.
     * Registers the {@link ChannelGroupType} with the provided {@link HomekitStorageBasedTypeProvider}.
     * Returns null if the service type is unknown or if no valid channel definitions can be created.
     *
     * @param typeProvider the HomekitStorageBasedTypeProvider to register the channel group type with
     * @return the created ChannelGroupDefinition or null if creation failed
     */
    public @Nullable ChannelGroupDefinition buildAndRegisterChannelGroupDefinition(
            HomekitStorageBasedTypeProvider typeProvider) {
        ServiceType serviceType = ServiceType.from(Integer.parseInt(serviceId));
        try {
            serviceType = ServiceType.from(Integer.parseInt(serviceId));
        } catch (IllegalArgumentException e) {
            return null;
        }

        List<@NonNull ChannelDefinition> channelDefinitions = characteristics.stream()
                .map(c -> c.buildAndRegisterChannelDefinition(typeProvider)).filter(Objects::nonNull).toList();

        if (channelDefinitions.isEmpty()) {
            return null;
        }

        ChannelGroupTypeUID uid = new ChannelGroupTypeUID(BINDING_ID, Integer.toHexString(hashCode()));
        ChannelGroupType type = ChannelGroupTypeBuilder.instance(uid, serviceId) //
                .withDescription(serviceType.toString()) //
                .withChannelDefinitions(channelDefinitions) //
                .build();

        typeProvider.putChannelGroupType(type);

        return new ChannelGroupDefinition(Integer.toString(instanceId), uid, serviceType.getTypeSuffix(), null);
    }
}
