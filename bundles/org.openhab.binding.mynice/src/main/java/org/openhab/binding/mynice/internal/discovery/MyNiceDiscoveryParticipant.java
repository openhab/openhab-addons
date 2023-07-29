/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mynice.internal.discovery;

import static org.openhab.binding.mynice.internal.MyNiceBindingConstants.BRIDGE_TYPE_IT4WIFI;
import static org.openhab.binding.mynice.internal.config.It4WifiConfiguration.HOSTNAME;
import static org.openhab.core.thing.Thing.PROPERTY_MAC_ADDRESS;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MyNiceDiscoveryParticipant} is responsible for discovering the IT4Wifi bridge using mDNS discovery service
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component
@NonNullByDefault
public class MyNiceDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final String PROPERTY_MODEL = "model";
    private static final String PROPERTY_DEVICE_ID = "deviceid";
    private static final String MAC_REGEX = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})|([0-9a-fA-F]{4}\\.[0-9a-fA-F]{4}\\.[0-9a-fA-F]{4})$";
    private static final Pattern MAC_PATTERN = Pattern.compile(MAC_REGEX);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(BRIDGE_TYPE_IT4WIFI);
    }

    @Override
    public String getServiceType() {
        return "_nap._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID thingUID = getThingUID(service);
        String[] hostNames = service.getHostAddresses();
        if (thingUID != null && hostNames.length > 0) {
            String label = service.getPropertyString(PROPERTY_MODEL);
            String macAddress = service.getPropertyString(PROPERTY_DEVICE_ID);

            return DiscoveryResultBuilder.create(thingUID).withLabel(label)
                    .withRepresentationProperty(PROPERTY_MAC_ADDRESS).withThingType(BRIDGE_TYPE_IT4WIFI)
                    .withProperties(Map.of(HOSTNAME, hostNames[0], PROPERTY_MAC_ADDRESS, macAddress)).build();
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String macAddress = service.getPropertyString(PROPERTY_DEVICE_ID);
        if (macAddress != null && validate(macAddress)) {
            macAddress = macAddress.replaceAll("[^a-fA-F0-9]", "").toLowerCase();
            return new ThingUID(BRIDGE_TYPE_IT4WIFI, macAddress);
        }
        return null;
    }

    private boolean validate(String mac) {
        return MAC_PATTERN.matcher(mac).find();
    }
}
