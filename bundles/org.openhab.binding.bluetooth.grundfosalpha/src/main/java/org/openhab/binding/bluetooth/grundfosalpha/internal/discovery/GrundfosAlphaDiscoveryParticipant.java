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
package org.openhab.binding.bluetooth.grundfosalpha.internal.discovery;

import static org.openhab.binding.bluetooth.BluetoothBindingConstants.*;
import static org.openhab.binding.bluetooth.grundfosalpha.internal.GrundfosAlphaBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This discovery participant is able to recognize Grundfos Alpha devices and create discovery results for them.
 *
 * @author Markus Heberling - Initial contribution
 * @author Jacob Laursen - Added support for Alpha3
 */
@NonNullByDefault
@Component
public class GrundfosAlphaDiscoveryParticipant implements BluetoothDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(GrundfosAlphaDiscoveryParticipant.class);

    private final TranslationProvider translationProvider;

    @Activate
    public GrundfosAlphaDiscoveryParticipant(final @Reference TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return false;
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        Integer manufacturerId = device.getManufacturerId();
        String name = device.getName();
        logger.debug("Discovered device {} with manufacturerId {} and name {}", device.getAddress(), manufacturerId,
                name);

        if (name == null) {
            return null;
        }

        ThingTypeUID thingTypeUID = switch (name) {
            case "Alpha3" -> THING_TYPE_ALPHA3;
            case "MI401" -> THING_TYPE_MI401;
            default -> null;
        };

        if (thingTypeUID == null) {
            return null;
        }

        return new ThingUID(thingTypeUID, device.getAdapter().getUID(),
                device.getAddress().toString().toLowerCase().replace(":", ""));
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }

        String thingID = thingUID.getAsString().split(ThingUID.SEPARATOR)[1];
        String label = translationProvider.getText(FrameworkUtil.getBundle(getClass()),
                "discovery.%s.label".formatted(thingID), null, null);

        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIGURATION_ADDRESS, device.getAddress().toString());
        String deviceName = device.getName();
        if (deviceName != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, deviceName);
        }
        Integer txPower = device.getTxPower();
        if (txPower != null) {
            properties.put(PROPERTY_TXPOWER, Integer.toString(txPower));
        }

        // Create the discovery result and add to the inbox
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(CONFIGURATION_ADDRESS).withBridge(device.getAdapter().getUID())
                .withLabel(label).build();
    }
}
