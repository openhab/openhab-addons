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
package org.openhab.binding.airquality.internal.handler;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airquality.internal.api.ApiBridge;
import org.openhab.binding.airquality.internal.discovery.AirQualityDiscoveryService;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

/**
 * The {@link AirQualityBridgeHandler} is responsible for handling communication
 * with the service via the API.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirQualityBridgeHandler extends BaseBridgeHandler {
    private final LocationProvider locationProvider;
    private @Nullable ApiBridge apiBridge;

    public AirQualityBridgeHandler(Bridge bridge, LocationProvider locationProvider) {
        super(bridge);
        this.locationProvider = locationProvider;
    }

    @Override
    public void initialize() {
        String apiKey = (String) getConfig().get("apiKey");
        if (apiKey != null && apiKey.length() != 0) {
            apiBridge = new ApiBridge(apiKey);
            updateStatus(ThingStatus.ONLINE);
        } else {
            apiBridge = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/null-or-empty-api-key");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // We do nothing
    }

    public @Nullable ApiBridge getApiBridge() {
        return apiBridge;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AirQualityDiscoveryService.class);
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }
}
