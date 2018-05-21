/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.discovery;

import static org.openhab.binding.tado.TadoBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.tado.TadoBindingConstants;
import org.openhab.binding.tado.handler.TadoHomeHandler;
import org.openhab.binding.tado.internal.api.TadoClientException;
import org.openhab.binding.tado.internal.api.model.MobileDevice;
import org.openhab.binding.tado.internal.api.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

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

    public final static Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ZONE,
            THING_TYPE_MOBILE_DEVICE);

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
        } catch (IOException | TadoClientException e) {
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
                .withProperties(properties).build();

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
        } catch (IOException | TadoClientException e) {
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
                .withProperties(properties).build();

        thingDiscovered(result);

        logger.debug("Discovered mobile device '{}' with id {} ({})", device.getName(), device.getId().toString(), uid);
    }
}
