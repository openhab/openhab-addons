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

import static org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lghorizon.internal.api.dto.CustomerDto;
import org.openhab.binding.lghorizon.internal.api.dto.CustomerDto.DeviceDto.SettingsDto;
import org.openhab.binding.lghorizon.internal.handler.LGHorizonAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Discovers the set-top boxes registered on an LG Horizon account as child things of the {@code account} bridge, once
 * the bridge has successfully
 * logged in.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = ThingHandlerService.class)
public class LGHorizonDiscoveryService extends AbstractThingHandlerDiscoveryService<LGHorizonAccountHandler> {

    private static final int TIMEOUT_SECONDS = 10;

    public LGHorizonDiscoveryService() {
        super(LGHorizonAccountHandler.class, Set.of(THING_TYPE_BOX), TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        super.setThingHandler(handler);
        if (handler instanceof LGHorizonAccountHandler accountHandler) {
            accountHandler.setDiscoveryService(this);
        }
    }

    public void discoverDevices() {
        LGHorizonAccountHandler account = thingHandler;
        ThingUID bridgeUid = account.getThing().getUID();

        for (CustomerDto.DeviceDto device : account.getAssignedDevices()) {
            String deviceId = device.deviceId;
            if (deviceId == null) {
                continue;
            }
            ThingUID thingUid = new ThingUID(THING_TYPE_BOX, bridgeUid, deviceId);
            SettingsDto settings = device.settings;
            String label = settings != null && settings.deviceFriendlyName != null ? settings.deviceFriendlyName
                    : "LG Horizon box " + deviceId;

            DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUid).withBridge(bridgeUid)
                    .withProperty(CONFIG_DEVICE_ID, deviceId).withRepresentationProperty(CONFIG_DEVICE_ID)
                    .withLabel(label);
            String platformType = device.platformType;
            if (platformType != null) {
                builder = builder.withProperty(PROPERTY_PLATFORM_TYPE, platformType);
            }
            DiscoveryResult result = builder.build();
            thingDiscovered(result);
        }
    }
}
