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
package org.openhab.binding.unifiprotect.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifiprotect.internal.api.UniFiProtectApiClient;
import org.openhab.binding.unifiprotect.internal.api.dto.Camera;
import org.openhab.binding.unifiprotect.internal.api.dto.Light;
import org.openhab.binding.unifiprotect.internal.api.dto.Sensor;
import org.openhab.binding.unifiprotect.internal.handler.UnifiProtectNVRHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
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
        super(UnifiProtectNVRHandler.class, Set.of(UnifiProtectBindingConstants.THING_TYPE_CAMERA,
                UnifiProtectBindingConstants.THING_TYPE_LIGHT, UnifiProtectBindingConstants.THING_TYPE_SENSOR), 30,
                false);
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
        final UniFiProtectApiClient client = thingHandler.getApiClient();
        if (client == null) {
            return;
        }
        discoverCameras(client);
        discoverLights(client);
        discoverSensors(client);
    }

    public void discoverCameras(UniFiProtectApiClient client) {
        try {
            for (Camera c : client.listCameras()) {
                discoverCamera(c);
            }
        } catch (IOException e) {
            logger.trace("Camera discovery failed", e);
        }
    }

    public void discoverLights(UniFiProtectApiClient client) {
        try {
            for (Light l : client.listLights()) {
                discoverLight(l);
            }
        } catch (IOException e) {
            logger.trace("Light discovery failed", e);
        }
    }

    public void discoverSensors(UniFiProtectApiClient client) {
        try {
            for (Sensor s : client.listSensors()) {
                discoverSensor(s);
            }
        } catch (IOException e) {
            logger.trace("Light discovery failed", e);
        }
    }

    public void discoverCamera(Camera c) {
        ThingUID uid = new ThingUID(UnifiProtectBindingConstants.THING_TYPE_CAMERA, thingHandler.getThing().getUID(),
                c.id);
        Map<String, Object> props = Map.of(UnifiProtectBindingConstants.DEVICE_ID, c.id);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                .withThingType(UnifiProtectBindingConstants.THING_TYPE_CAMERA).withProperties(props)
                .withRepresentationProperty(UnifiProtectBindingConstants.DEVICE_ID)
                .withLabel("UniFi Protect Camera: " + c.name).build();
        thingDiscovered(result);
    }

    public void discoverLight(Light l) {
        ThingUID uid = new ThingUID(UnifiProtectBindingConstants.THING_TYPE_LIGHT, thingHandler.getThing().getUID(),
                l.id);
        Map<String, Object> props = Map.of(UnifiProtectBindingConstants.DEVICE_ID, l.id);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                .withThingType(UnifiProtectBindingConstants.THING_TYPE_LIGHT).withProperties(props)
                .withRepresentationProperty(UnifiProtectBindingConstants.DEVICE_ID)
                .withLabel("UniFi Protect Light: " + l.name).build();
        thingDiscovered(result);
    }

    public void discoverSensor(Sensor s) {
        ThingUID uid = new ThingUID(UnifiProtectBindingConstants.THING_TYPE_SENSOR, thingHandler.getThing().getUID(),
                s.id);
        Map<String, Object> props = Map.of(UnifiProtectBindingConstants.DEVICE_ID, s.id);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                .withThingType(UnifiProtectBindingConstants.THING_TYPE_SENSOR).withProperties(props)
                .withRepresentationProperty(UnifiProtectBindingConstants.DEVICE_ID)
                .withLabel("UniFi Protect Sensor: " + s.name).build();
        thingDiscovered(result);
    }
}
