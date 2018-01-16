/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.discovery;

import static org.openhab.binding.jeelink.JeeLinkBindingConstants.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.jeelink.internal.JeeLinkHandler;
import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.internal.Reading;
import org.openhab.binding.jeelink.internal.ReadingHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;
import org.openhab.binding.jeelink.internal.config.JeeLinkSensorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for sensors connected to a JeeLink USB Receiver.
 *
 * @author Volker Bier - Initial contribution
 */
public class SensorDiscoveryService extends AbstractDiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(SensorDiscoveryService.class);

    JeeLinkHandler bridge;
    Set<MyReadingHandler> rhs = new HashSet<>();
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
        if (!capture.get()) {
            for (JeeLinkReadingConverter<?> c : SensorDefinition.getConverters(bridge)) {
                MyReadingHandler rh = new MyReadingHandler(c.getSketchName());
                rhs.add(rh);

                bridge.addReadingHandler(rh);
            }

            capture.set(true);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }

    @Override
    protected synchronized void stopScan() {
        if (capture.get()) {
            capture.set(false);

            for (MyReadingHandler rh : rhs) {
                bridge.removeReadingHandler(rh);
            }
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
    }

    class MyReadingHandler implements ReadingHandler<Reading> {
        private final String sketchName;

        private MyReadingHandler(String sketch) {
            sketchName = sketch;
        }

        @Override
        public String getSketchName() {
            return sketchName;
        }

        @Override
        public void handleReading(Reading reading) {
            if (capture.get()) {
                final String id = reading.getSensorId();

                List<Thing> existingThings = bridge.getThing().getThings();
                boolean sensorThingExists = false;

                for (Thing t : existingThings) {
                    sensorThingExists = sensorThingExists
                            || id.equals(t.getConfiguration().as(JeeLinkSensorConfig.class).sensorId);
                }

                if (!sensorThingExists) {
                    SensorDefinition<?> def = SensorDefinition.getSensorDefinition(reading);
                    logger.debug("read unknown sensor of type {} with id {}", def.getThingTypeUID(), id);

                    boolean idExists = idExistsAtBridge(id);
                    String newId = id;

                    if (idExists) {
                        logger.debug("bridge already has a connected sensor with thing id {}", id);

                        int idx = 1;
                        while (idExists) {
                            newId = id + "-" + idx++;
                            idExists = idExistsAtBridge(newId);
                        }

                        logger.debug("Using thing id {} instead", newId);
                    }

                    ThingUID sensorThing = new ThingUID(def.getThingTypeUID(), newId);

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(sensorThing)
                            .withLabel(def.getName()).withBridge(bridge.getThing().getUID())
                            .withRepresentationProperty("id").withProperty(PROPERTY_SENSOR_ID, id).build();
                    thingDiscovered(discoveryResult);
                } else {
                    logger.debug("read already known sensor id {}", id);
                }
            }
        }

        private boolean idExistsAtBridge(String id) {
            List<Thing> existingThings = bridge.getThing().getThings();
            boolean idExists = false;
            for (Thing t : existingThings) {
                idExists = idExists || t.getUID().getId().equals(id);
            }
            return idExists;
        }
    }
}
