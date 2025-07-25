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
package org.openhab.binding.mspa.internal.discovery;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mspa.internal.MSpaConstants;
import org.openhab.binding.mspa.internal.handler.MSpaBaseAccount;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * {@link MSpaDiscoveryService} starts manual scan on all Accounts. No background scan.
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class, MSpaDiscoveryService.class }, configurationPid = "discovery.mspa")
public class MSpaDiscoveryService extends AbstractDiscoveryService {

    private List<MSpaBaseAccount> accountList = new ArrayList<>();

    public MSpaDiscoveryService() {
        super(Set.of(MSpaConstants.THING_TYPE_POOL), 0, false);
    }

    @Override
    protected void startScan() {
        accountList.forEach(account -> {
            account.startDiscovery();
        });
    }

    public void addAccount(MSpaBaseAccount account) {
        accountList.add(account);
    }

    public void removeAccount(MSpaBaseAccount account) {
        accountList.remove(account);
    }

    public void deviceDiscovered(ThingTypeUID ttUid, ThingUID bridgeUid, Map<String, Object> properties) {
        Object deviceId = properties.get(PROPERTY_DEVICE_ID);
        String label = "MSpa Pool";
        Object model = properties.get(PROPERTY_PRODUCT_SERIES);
        if (model != null) {
            label = label.concat(" " + model.toString());
        }
        if (deviceId != null) {
            thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(ttUid, bridgeUid, deviceId.toString()))
                    .withBridge(bridgeUid).withProperties(properties).withRepresentationProperty(PROPERTY_DEVICE_ID)
                    .withLabel(label).build());
        }
    }
}
