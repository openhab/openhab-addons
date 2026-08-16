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
package org.openhab.binding.mikrotik.internal;

import static org.openhab.binding.mikrotik.internal.MikrotikBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.handler.MikrotikRouterosBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link MikrotikThingDiscoveryService} Discovers and adds any Kid Controls that are found by the bridge
 * device.
 *
 * @author Matthew Skinner - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MikrotikThingDiscoveryService.class)
@NonNullByDefault
public class MikrotikThingDiscoveryService extends AbstractThingHandlerDiscoveryService<MikrotikRouterosBridgeHandler>
        implements DiscoveryService, ThingHandlerService {
    private static final int DISCOVERY_TIMEOUT = 11;
    private @Nullable Inbox inbox;

    public MikrotikThingDiscoveryService() {
        super(MikrotikRouterosBridgeHandler.class, Set.of(THING_TYPE_KID_CONTROL, THING_TYPE_DEVICE),
                DISCOVERY_TIMEOUT);
    }

    @Reference
    public void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }

    public void unsetInbox(Inbox inbox) {
        this.inbox = null;
    }

    private void clearBridgeInboxEntries(ThingUID bridgeUid) {
        Inbox localInbox = this.inbox;
        if (localInbox == null) {
            return;
        }
        // remove all entries that are not found in this current scan. Andriod can randomly change the MAC address of
        // phones and clog up the INBOX.
        Set<ThingTypeUID> supportedTypes = getSupportedThingTypes();
        localInbox.stream().filter(result -> supportedTypes.contains(result.getThingTypeUID()))
                .filter(result -> bridgeUid.equals(result.getBridgeUID())).map(DiscoveryResult::getThingUID).toList()
                .forEach(localInbox::remove);
    }

    @Override
    protected void startScan() {
        MikrotikRouterosBridgeHandler handler = thingHandler;
        clearBridgeInboxEntries(handler.getThing().getUID());

        Set<String> kids = handler.getKidControlNames();
        for (String kid : kids) {
            String cleanedKid = kid.replaceAll("[^a-zA-Z0-9]", "");
            ThingUID thingUID = new ThingUID(THING_TYPE_KID_CONTROL, handler.getThing().getUID(), cleanedKid);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withBridge(handler.getThing().getUID()).withLabel("Kid Controls for " + kid)
                    .withProperty("name", kid).withRepresentationProperty("name").build();
            thingDiscovered(discoveryResult);
        }

        List<Map<String, String>> devices = handler.getDevices();
        for (Map<String, String> device : devices) {
            String name = device.get("name");
            String mac = device.get("mac-address");
            if (mac != null && !mac.isEmpty() && name != null && !name.isEmpty()) {
                String cleanedName = name.replaceAll("[^a-zA-Z0-9]", "");
                ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, handler.getThing().getUID(), cleanedName);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withBridge(handler.getThing().getUID()).withLabel(name)
                        .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac).withProperty(CONFIG_MAC_ADDRESS, mac)
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Set.of(THING_TYPE_KID_CONTROL, THING_TYPE_DEVICE);
    }
}
