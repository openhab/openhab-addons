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
package org.openhab.binding.tado.internal.discovery;

import static org.openhab.binding.tado.internal.TadoBindingConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.tado.internal.TadoBindingConstants;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.MobileDevice;
import org.openhab.binding.tado.internal.api.model.Zone;
import org.openhab.binding.tado.internal.handler.TadoHomeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for zones and mobile devices.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoDiscoveryService extends AbstractDiscoveryService {
    private static final int TIMEOUT = 5;
    private static final long REFRESH = 600;

    private final Logger logger = LoggerFactory.getLogger(TadoDiscoveryService.class);

    private ScheduledFuture<?> discoveryFuture;

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_ZONE, THING_TYPE_MOBILE_DEVICE).collect(Collectors.toSet()));

    private TadoHomeHandler homeHandler;

    public TadoDiscoveryService(TadoHomeHandler tadoHomeHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, TIMEOUT);
        this.homeHandler = tadoHomeHandler;
    }

    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        if (homeHandler.getHomeId() == null) {
            return;
        }

        discoverZones();
        discoverMobileDevices();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Tado background discovery");
        if (discoveryFuture == null || discoveryFuture.isCancelled()) {
            logger.debug("Start Scan");
            discoveryFuture = scheduler.scheduleWithFixedDelay(this::startScan, 30, REFRESH, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Tado background discovery");
        if (discoveryFuture != null && !discoveryFuture.isCancelled()) {
            discoveryFuture.cancel(true);
            discoveryFuture = null;
        }
    }

    private void discoverZones() {
        Long homeId = homeHandler.getHomeId();
        try {
            List<Zone> zoneList = homeHandler.getApi().listZones(homeId);

            if (zoneList != null) {
                for (Zone zone : zoneList) {
                    notifyZoneDiscovery(homeId, zone);
                }
            }
        } catch (IOException | ApiException e) {
            logger.debug("Could not discover tado zones: {}", e.getMessage(), e);
        }
    }

    private void notifyZoneDiscovery(Long homeId, Zone zone) {
        Integer zoneId = zone.getId();

        ThingUID bridgeUID = this.homeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(TadoBindingConstants.THING_TYPE_ZONE, bridgeUID, zoneId.toString());

        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_ZONE_ID, zoneId);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(zone.getName())
                .withProperties(properties).withRepresentationProperty(CONFIG_ZONE_ID).build();

        thingDiscovered(result);

        logger.debug("Discovered zone '{}' with id {} ({})", zone.getName(), zoneId.toString(), uid);
    }

    private void discoverMobileDevices() {
        Long homeId = homeHandler.getHomeId();
        try {
            List<MobileDevice> mobileDeviceList = homeHandler.getApi().listMobileDevices(homeId);

            if (mobileDeviceList != null) {
                for (MobileDevice mobileDevice : mobileDeviceList) {
                    if (mobileDevice.getSettings().isGeoTrackingEnabled()) {
                        notifyMobileDeviceDiscovery(homeId, mobileDevice);
                    }
                }
            }
        } catch (IOException | ApiException e) {
            logger.debug("Could not discover tado zones: {}", e.getMessage(), e);
        }
    }

    private void notifyMobileDeviceDiscovery(Long homeId, MobileDevice device) {
        ThingUID bridgeUID = this.homeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(TadoBindingConstants.THING_TYPE_MOBILE_DEVICE, bridgeUID,
                device.getId().toString());

        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_MOBILE_DEVICE_ID, device.getId());

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(device.getName())
                .withProperties(properties).withRepresentationProperty(CONFIG_MOBILE_DEVICE_ID).build();

        thingDiscovered(result);

        logger.debug("Discovered mobile device '{}' with id {} ({})", device.getName(), device.getId().toString(), uid);
    }
}
