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
package org.openhab.binding.homekit.internal.discovery;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.dto.Content;
import org.openhab.binding.homekit.internal.handler.HomekitBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery service component that publishes newly discovered bridged accessories of a HomeKit bridge
 * accessory. Discovered devices are published as Things with thingUID based on accessory ID (aid) of type
 * {@link org.openhab.binding.homekit.internal.HomekitBindingConstants#THING_TYPE_BRIDGED_ACCESSORY} .
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class)
public class HomekitBridgedAccessoryDiscoveryService
        extends AbstractThingHandlerDiscoveryService<HomekitBridgeHandler> {

    private static final int TIMEOUT_SECONDS = 10;

    public HomekitBridgedAccessoryDiscoveryService() {
        super(HomekitBridgeHandler.class, Set.of(THING_TYPE_BRIDGED_ACCESSORY), TIMEOUT_SECONDS);
    }

    @Override
    public void initialize() {
        super.initialize();
        thingHandler.registerDiscoveryService(this);
    }

    @Override
    public void dispose() {
        thingHandler.unregisterDiscoveryService();
        super.dispose();
    }

    @Override
    public void startScan() {
        if (thingHandler instanceof HomekitBridgeHandler handler) {
            discoverBridgedAccessories(handler);
        }
    }

    private void discoverBridgedAccessories(HomekitBridgeHandler handler) {
        String bridgeUniqueId = thingHandler.getThing().getConfiguration()
                .get(CONFIG_UNIQUE_ID) instanceof String uniqueId ? uniqueId : null;
        if (bridgeUniqueId == null) {
            return;
        }

        ThingUID bridgeUid = handler.getThing().getUID();
        TranslationProvider i18n = handler.i18nProvider;
        Bundle bundle = handler.bundle;

        handler.getAccessories().values().forEach(accessory -> {
            if (accessory.aid instanceof Long aid && accessory.services != null) {
                ThingUID uid = new ThingUID(THING_TYPE_BRIDGED_ACCESSORY, bridgeUid, aid.toString());
                String uniqueId = STRING_AID_FMT.formatted(bridgeUniqueId, aid);

                String label = null; // null flags that no thing shall be discovered
                if (aid != 1L) {
                    label = THING_LABEL_FMT.formatted(accessory.getAccessoryInstanceLabel(), uniqueId);
                } else if (Optional.ofNullable(accessory.services).stream().flatMap(List::stream)
                        .flatMap(service -> Optional.ofNullable(service.characteristics).stream()).flatMap(List::stream)
                        .map(characteristic -> characteristic.getContent(uid, null, i18n, bundle))
                        .anyMatch(Content.ChannelDefinition.class::isInstance)) {
                    // at least one characteristic yields a channel definition so discover an embedded thing for it
                    label = THING_LABEL_FMT.formatted(accessory.getAccessoryInstanceLabel(),
                            i18n.getText(bundle, HARDWARE_EMBEDDED_THING_LABEL, null, null));
                }

                if (label != null) {
                    thingDiscovered(DiscoveryResultBuilder.create(uid) //
                            .withBridge(bridgeUid) //
                            .withLabel(label) //
                            .withProperty(CONFIG_ACCESSORY_ID, aid.toString()) //
                            .withProperty(PROPERTY_UNIQUE_ID, uniqueId).withRepresentationProperty(PROPERTY_UNIQUE_ID)
                            .build());
                }
            }
        });
    }
}
