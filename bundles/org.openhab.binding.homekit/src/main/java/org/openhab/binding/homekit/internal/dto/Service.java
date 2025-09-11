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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.ServiceType;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;

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
    public @Nullable @SerializedName("type") String serviceId; // e.g. '96' = 'public.hap.service.battery'
    public @Nullable @SerializedName("iid") Integer instanceId; // e.g. 10
    public @Nullable List<Characteristic> characteristics;

    public ServiceType getServiceType() {
        Integer iid = this.iid;
        if (iid == null) {
            return ServiceType.UNKNOWN;
        }
        return ServiceType.from(iid);
    }

    public @Nullable ChannelGroupType getChannelType() {
        String serviceId = this.serviceId;
        List<Characteristic> characteristics = this.characteristics;
        if (serviceId == null || characteristics == null) {
            return null;
        }

        ServiceType serviceType = ServiceType.from(serviceId);

        String label = "label"; // TODO determine label based on characType
        String category = "sensor"; // TODO determine category based on characType

        List<ChannelDefinition> channelDefinitions = new ArrayList<>();
        for (Characteristic characteristic : characteristics) {
            ChannelType ct = characteristic.getChannelType();
            if (ct == null) {
                continue;
            }
            channelDefinitions.add(new ChannelDefinitionBuilder(ct.getUID().getId(), ct.getUID())
                    .withLabel(ct.getLabel()).withDescription(ct.getDescription()).build());
        }

        return ChannelGroupTypeBuilder.instance(new ChannelGroupTypeUID(BINDING_ID, serviceId), label)
                .withChannelDefinitions(channelDefinitions).withCategory(category).build();
    }

}
