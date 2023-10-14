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
package org.openhab.binding.myq.internal;

import static org.openhab.binding.myq.internal.MyQBindingConstants.BINDING_ID;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myq.internal.dto.DeviceDTO;
import org.openhab.binding.myq.internal.handler.MyQAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link MyQDiscoveryService} is responsible for discovering MyQ things
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MyQDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private static final Set<ThingTypeUID> SUPPORTED_DISCOVERY_THING_TYPES_UIDS = Set
            .of(MyQBindingConstants.THING_TYPE_GARAGEDOOR, MyQBindingConstants.THING_TYPE_LAMP);
    private @Nullable MyQAccountHandler accountHandler;

    public MyQDiscoveryService() {
        super(SUPPORTED_DISCOVERY_THING_TYPES_UIDS, 1, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_DISCOVERY_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        MyQAccountHandler accountHandler = this.accountHandler;
        if (accountHandler != null) {
            List<DeviceDTO> devices = accountHandler.devicesCache();
            if (devices != null) {
                devices.forEach(device -> {
                    ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, device.deviceFamily);
                    if (SUPPORTED_DISCOVERY_THING_TYPES_UIDS.contains(thingTypeUID)) {
                        ThingUID thingUID = new ThingUID(thingTypeUID, accountHandler.getThing().getUID(),
                                device.serialNumber.toLowerCase());
                        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel("MyQ " + device.name)
                                .withProperty(Thing.PROPERTY_SERIAL_NUMBER, thingUID.getId())
                                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                                .withBridge(accountHandler.getThing().getUID()).build();
                        thingDiscovered(result);
                    }
                });
            }
        }
    }

    @Override
    public void startBackgroundDiscovery() {
        startScan();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof MyQAccountHandler myqAccountHandler) {
            accountHandler = myqAccountHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
