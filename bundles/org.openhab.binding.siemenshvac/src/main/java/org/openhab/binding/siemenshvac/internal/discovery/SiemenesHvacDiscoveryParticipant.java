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
package org.openhab.binding.siemenshvac.internal.discovery;

import static org.openhab.core.thing.Thing.PROPERTY_SERIAL_NUMBER;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.config.discovery.upnp.internal.UpnpDiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * The {@link SiemensHvacDiscoveryParticipant} is responsible for discovering new and
 * removed siemensHvac bridges. It uses the central {@link UpnpDiscoveryService}.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "discovery.siemenshvac", immediate = true)
public class SiemenesHvacDiscoveryParticipant implements UpnpDiscoveryParticipant {

    @Activate
    public void activate(@Nullable Map<String, Object> configProperties) {
    }

    @Modified
    public void modified(@Nullable Map<String, Object> configProperties) {
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SiemensHvacBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>();
            String ipAddress = device.getDetails().getPresentationURI().getHost();
            properties.put(SiemensHvacBindingConstants.BASE_URL, "https://" + ipAddress + "/");

            String label = "";

            if (uid.getAsString().contains("ozw672")) {
                label = "OZW672 IP Gateway";
            } else if (uid.getAsString().contains("ozw772")) {
                label = "OZW772 IP Gateway";
            }

            String serialNumber = device.getDetails().getSerialNumber();
            DiscoveryResult result;
            if (serialNumber != null && !serialNumber.isBlank()) {
                properties.put(PROPERTY_SERIAL_NUMBER, serialNumber.toLowerCase());

                result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                        .withRepresentationProperty(PROPERTY_SERIAL_NUMBER).build();
            } else {
                result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ModelDetails modelDetails = details.getModelDetails();
            String serialNumber = details.getSerialNumber();
            if (modelDetails != null && serialNumber != null && !serialNumber.isBlank()) {
                String modelName = modelDetails.getModelName();
                if (modelName != null) {
                    if (modelName.startsWith("Web Server OZW672")) {
                        return new ThingUID(SiemensHvacBindingConstants.THING_TYPE_OZW, "ozw672-" + serialNumber);
                    } else if (modelName.startsWith("Web Server OZW772")) {
                        return new ThingUID(SiemensHvacBindingConstants.THING_TYPE_OZW, "ozw772-" + serialNumber);
                    }
                }
            }
        }
        return null;
    }
}
