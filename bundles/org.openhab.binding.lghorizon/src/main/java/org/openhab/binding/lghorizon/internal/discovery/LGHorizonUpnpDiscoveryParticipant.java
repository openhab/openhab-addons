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
package org.openhab.binding.lghorizon.internal.discovery;

import static org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants.THING_TYPE_ACCOUNT;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LG Horizon set-top boxes advertise via UPnP/SSDP.
 * The Telenet IP TV Box is identified with manufacturer "TELENETTVBOX" in the UPnP device manufacturer field.
 * Other providers and boxes may use another identifier.
 * <p>
 * Unlike most {@link UpnpDiscoveryParticipant}s, this one does not represent the physical box as a discovered
 * Thing: the box itself is only meaningfully addressable once an {@code account} bridge has authenticated against
 * the cloud backend and enumerated it (see {@link LGHorizonDiscoveryService}), and none of the credentials that
 * requires can be learned from the local network. Instead, seeing this fingerprint on the network is used purely
 * as a hint to suggest adding and configuring an {@code account} bridge in the first place.
 * <p>
 * All matches collapse onto a single, stable {@code lghorizon:account:detected} suggestion (regardless of how many
 * physical boxes respond), and the suggestion is withheld entirely once at least one {@code account} thing already
 * exists, since one configured account already covers however many boxes are in the household.
 *
 * @author Mark Herwege - Initial contribution
 */
@Component(service = UpnpDiscoveryParticipant.class)
@NonNullByDefault
public class LGHorizonUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private static final Set<String> EXPECTED_MANUFACTURERS = Set.of("TELENETTVBOX");
    private static final ThingUID SUGGESTED_ACCOUNT_UID = new ThingUID(THING_TYPE_ACCOUNT, "detected");

    private final Logger logger = LoggerFactory.getLogger(LGHorizonUpnpDiscoveryParticipant.class);
    private final ThingRegistry thingRegistry;

    @Activate
    public LGHorizonUpnpDiscoveryParticipant(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_ACCOUNT);
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (!isLGHorizonBox(device) || hasExistingAccount()) {
            return null;
        }
        return SUGGESTED_ACCOUNT_UID;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }

        DeviceDetails details = device.getDetails();
        String friendlyName = details == null ? null : details.getFriendlyName();
        ModelDetails modelDetails = details == null ? null : details.getModelDetails();
        String modelName = modelDetails == null ? null : modelDetails.getModelName();

        logger.debug("Detected an LG Horizon set-top box on the network (name={}, model={})", friendlyName, modelName);

        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID)
                .withLabel("LG Horizon set-top box detected - add an account to control it");
        if (friendlyName != null) {
            builder = builder.withProperty("detectedDeviceName", friendlyName);
        }
        if (modelName != null) {
            builder = builder.withProperty("detectedDeviceModel", modelName);
        }
        return builder.build();
    }

    private boolean isLGHorizonBox(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details == null) {
            return false;
        }
        ManufacturerDetails manufacturerDetails = details.getManufacturerDetails();
        String manufacturer = manufacturerDetails == null ? null : manufacturerDetails.getManufacturer();
        return manufacturer != null
                && EXPECTED_MANUFACTURERS.stream().anyMatch(m -> m.equalsIgnoreCase(manufacturer.trim()));
    }

    private boolean hasExistingAccount() {
        return thingRegistry.getAll().stream().anyMatch(thing -> THING_TYPE_ACCOUNT.equals(thing.getThingTypeUID()));
    }
}
