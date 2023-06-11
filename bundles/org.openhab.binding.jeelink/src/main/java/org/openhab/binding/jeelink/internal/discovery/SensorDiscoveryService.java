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
package org.openhab.binding.jeelink.internal.discovery;

import static org.openhab.binding.jeelink.internal.JeeLinkBindingConstants.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openhab.binding.jeelink.internal.JeeLinkHandler;
import org.openhab.binding.jeelink.internal.Reading;
import org.openhab.binding.jeelink.internal.ReadingHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;
import org.openhab.binding.jeelink.internal.config.JeeLinkSensorConfig;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for sensors connected to a JeeLink USB Receiver.
 *
 * @author Volker Bier - Initial contribution
 */
public class SensorDiscoveryService extends AbstractDiscoveryService implements ReadingHandler<Reading> {
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(SensorDiscoveryService.class);

    JeeLinkHandler bridge;
    AtomicBoolean capture = new AtomicBoolean();

    /**
     * Creates the discovery service for the given handler and converter.
     */
    public SensorDiscoveryService(JeeLinkHandler jeeLinkHandler) {
        super(SUPPORTED_SENSOR_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, true);

        bridge = jeeLinkHandler;
    }

    @Override
    protected synchronized void startScan() {
        if (!capture.getAndSet(true)) {
            logger.debug("discovery started for bridge {}", bridge.getThing().getUID());

            // start listening for new sensor values
            bridge.addReadingHandler(this);
            capture.set(true);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }

    @Override
    protected synchronized void stopScan() {
        if (capture.getAndSet(false)) {
            bridge.removeReadingHandler(this);
            logger.debug("discovery stopped for bridge {}", bridge.getThing().getUID());
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
    }

    private boolean idExistsAtBridge(String id) {
        List<Thing> existingThings = bridge.getThing().getThings();
        boolean idExists = false;
        for (Thing t : existingThings) {
            idExists = idExists || t.getUID().getId().equals(id);
        }
        return idExists;
    }

    @Override
    public void handleReading(Reading reading) {
        final String id = reading.getSensorId();

        List<Thing> existingThings = bridge.getThing().getThings();
        boolean sensorThingExists = false;

        for (Thing t : existingThings) {
            sensorThingExists = sensorThingExists
                    || id.equals(t.getConfiguration().as(JeeLinkSensorConfig.class).sensorId);
        }

        ThingUID bridgeUID = bridge.getThing().getUID();

        if (!sensorThingExists) {
            SensorDefinition<?> def = SensorDefinition.getSensorDefinition(reading);
            logger.debug("discovery for bridge {} found unknown sensor of type {} with id {}", bridgeUID,
                    def.getThingTypeUID(), id);

            boolean idExists = idExistsAtBridge(id);
            String newId = id;

            if (idExists) {
                logger.debug("bridge {} already has a connected sensor with thing id {}", bridgeUID, id);

                int idx = 1;
                while (idExists) {
                    newId = id + "-" + idx++;
                    idExists = idExistsAtBridge(newId);
                }

                logger.debug("Bridge {} uses thing id {} instead of {}", bridgeUID, newId, id);
            }

            ThingUID sensorThing = new ThingUID(def.getThingTypeUID(), bridgeUID, newId);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(sensorThing).withLabel(def.getName())
                    .withBridge(bridgeUID).withRepresentationProperty("id").withProperty(PROPERTY_SENSOR_ID, id)
                    .build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovery for bridge {} found already known sensor id {}", bridgeUID, id);
        }
    }

    @Override
    public Class<Reading> getReadingClass() {
        return Reading.class;
    }

    @Override
    public String getSensorType() {
        return SensorDefinition.ALL_TYPE;
    }
}
