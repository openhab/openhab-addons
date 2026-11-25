/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.miflora.internal;

import static org.openhab.binding.bluetooth.miflora.internal.MiFloraBindingConstants.*;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Recognizes Mi Flora sensors by the name they advertise.
 *
 * @author Lukas Brinias - Initial contribution
 */
@NonNullByDefault
@Component
public class MiFloraDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    private final TranslationProvider translationProvider;

    @Activate
    public MiFloraDiscoveryParticipant(final @Reference TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_HHCCJCY01);
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        if (ADVERTISED_NAME.equalsIgnoreCase(device.getName())) {
            return new ThingUID(THING_TYPE_HHCCJCY01, device.getAdapter().getUID(),
                    device.getAddress().toString().toLowerCase().replace(":", ""));
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }
        // The Bluetooth discovery service resolves "@text/" against its own bundle, not this one
        String label = translationProvider.getText(FrameworkUtil.getBundle(getClass()), "discovery.miflora.label", null,
                null);
        return DiscoveryResultBuilder.create(thingUID)
                .withProperties(Map.of(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString()))
                .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS)
                .withBridge(device.getAdapter().getUID()).withLabel(label).build();
    }
}
