/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal;

import static org.openhab.binding.vesync.internal.VeSyncConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vesync.internal.api.VesyncV2ApiHelper;
import org.openhab.binding.vesync.internal.handlers.VeSyncBridgeHandler;
import org.openhab.binding.vesync.internal.handlers.VeSyncDeviceAirHumidifierHandler;
import org.openhab.binding.vesync.internal.handlers.VeSyncDeviceAirPurifierHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link org.openhab.binding.vesync.internal.VeSyncHandlerFactory} is responsible for creating
 * things and thing handlers.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.vesync", service = ThingHandlerFactory.class)
public class VeSyncHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE,
            THING_TYPE_AIR_PURIFIER, THING_TYPE_AIR_HUMIDIFIER);

    private final VesyncV2ApiHelper api = new VesyncV2ApiHelper();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (VeSyncDeviceAirPurifierHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new VeSyncDeviceAirPurifierHandler(thing);
        } else if (VeSyncDeviceAirHumidifierHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new VeSyncDeviceAirHumidifierHandler(thing);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new VeSyncBridgeHandler((Bridge) thing, api);
        }

        return null;
    }

    private ThingUID getDeviceUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration,
            @Nullable ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        } else {
            if (bridgeUID != null) {
                return new ThingUID(thingTypeUID, configuration.get(DEVICE_MAC_ID).toString(), bridgeUID.getId());
            } else {
                return new ThingUID(thingTypeUID, configuration.get(DEVICE_MAC_ID).toString());
            }
        }
    }

    @Override
    public @Nullable Thing createThing(final ThingTypeUID thingTypeUID, final Configuration configuration,
            final @Nullable ThingUID thingUID, final @Nullable ThingUID bridgeUID) {
        // Use the specific Handler Factory if required
        // otherwise fallback to the default
        if (VeSyncDeviceAirPurifierHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)
                || VeSyncDeviceAirHumidifierHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            final ThingUID deviceUID = getDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
        } else if (VeSyncHandlerFactory.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        } else {
            throw new IllegalArgumentException(
                    "The thing type " + thingTypeUID + " is not supported by the VeSync binding.");
        }
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        api.setHttpClient(httpClientFactory.getCommonHttpClient());
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        api.setHttpClient(null);
    }
}
