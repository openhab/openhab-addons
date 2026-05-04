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
package org.openhab.binding.unifi.protect.internal;

import static org.openhab.binding.unifi.protect.internal.UnifiProtectBindingConstants.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.protect.internal.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.system.Bootstrap;
import org.openhab.binding.unifi.protect.internal.api.pub.dto.Camera;
import org.openhab.binding.unifi.protect.internal.api.pub.dto.Light;
import org.openhab.binding.unifi.protect.internal.api.pub.dto.Sensor;
import org.openhab.binding.unifi.protect.internal.handler.UnifiProtectNVRHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for UniFi Access Door things.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = UnifiProtectDiscoveryService.class)
@NonNullByDefault
public class UnifiProtectDiscoveryService extends AbstractThingHandlerDiscoveryService<UnifiProtectNVRHandler> {

    private final Logger logger = LoggerFactory.getLogger(UnifiProtectDiscoveryService.class);

    public UnifiProtectDiscoveryService() {
        super(UnifiProtectNVRHandler.class,
                Set.of(THING_TYPE_CAMERA, THING_TYPE_LIGHT, THING_TYPE_SENSOR, THING_TYPE_DOORLOCK, THING_TYPE_CHIME),
                30, false);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        logger.debug("setThingHandler {}", handler);
        if (handler instanceof UnifiProtectNVRHandler childDiscoveryHandler) {
            childDiscoveryHandler.setDiscoveryService(this);
            this.thingHandler = childDiscoveryHandler;
        }
    }

    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        final UniFiProtectHybridClient client = thingHandler.getApiClient();
        if (client == null) {
            return;
        }
        try {
            Bootstrap bootstrap = client.getPrivateClient().getBootstrap().get(30, TimeUnit.SECONDS);
            bootstrap.cameras.forEach((id, camera) -> {
                discoverDevice(id, camera.name, camera.type, THING_TYPE_CAMERA, "Camera");
            });
            bootstrap.lights.forEach((id, light) -> {
                discoverDevice(id, light.name, light.type, THING_TYPE_LIGHT, "Light");
            });
            bootstrap.sensors.forEach((id, sensor) -> {
                discoverDevice(id, sensor.name, sensor.type, THING_TYPE_SENSOR, "Sensor");
            });
            bootstrap.doorlocks.forEach((id, doorlock) -> {
                discoverDevice(id, doorlock.name, doorlock.type, THING_TYPE_DOORLOCK, "Doorlock");
            });
            bootstrap.chimes.forEach((id, chime) -> {
                discoverDevice(id, chime.name, chime.type, THING_TYPE_CHIME, "Chime");
            });
        } catch (Exception e) {
            logger.trace("Discovery scan failed", e);
        }
    }

    public void discoverDevice(String id, @Nullable String name, @Nullable String type, ThingTypeUID thingTypeUID,
            String label) {
        ThingUID uid = new ThingUID(thingTypeUID, thingHandler.getThing().getUID(), id);
        Map<String, Object> props = Map.of(DEVICE_ID, id);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                .withThingType(thingTypeUID).withProperties(props).withRepresentationProperty(DEVICE_ID)
                .withLabel("UniFi Protect " + label + ": " + (name != null ? name : type != null ? type : id)).build();
        thingDiscovered(result);
    }

    public void discoverCamera(Camera c) {
        ThingUID uid = new ThingUID(THING_TYPE_CAMERA, thingHandler.getThing().getUID(), c.id);
        Map<String, Object> props = Map.of(DEVICE_ID, c.id);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                .withThingType(THING_TYPE_CAMERA).withProperties(props).withRepresentationProperty(DEVICE_ID)
                .withLabel("UniFi Protect Camera: " + c.name).build();
        thingDiscovered(result);
    }

    public void discoverLight(Light l) {
        ThingUID uid = new ThingUID(THING_TYPE_LIGHT, thingHandler.getThing().getUID(), l.id);
        Map<String, Object> props = Map.of(DEVICE_ID, l.id);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                .withThingType(THING_TYPE_LIGHT).withProperties(props).withRepresentationProperty(DEVICE_ID)
                .withLabel("UniFi Protect Light: " + l.name).build();
        thingDiscovered(result);
    }

    public void discoverSensor(Sensor s) {
        ThingUID uid = new ThingUID(THING_TYPE_SENSOR, thingHandler.getThing().getUID(), s.id);
        Map<String, Object> props = Map.of(DEVICE_ID, s.id);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                .withThingType(THING_TYPE_SENSOR).withProperties(props).withRepresentationProperty(DEVICE_ID)
                .withLabel("UniFi Protect Sensor: " + s.name).build();
        thingDiscovered(result);
    }
}
