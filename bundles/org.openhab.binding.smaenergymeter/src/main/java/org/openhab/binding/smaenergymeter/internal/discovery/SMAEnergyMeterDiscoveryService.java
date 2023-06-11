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
package org.openhab.binding.smaenergymeter.internal.discovery;

import static org.openhab.binding.smaenergymeter.internal.SMAEnergyMeterBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.smaenergymeter.internal.handler.EnergyMeter;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SMAEnergyMeterDiscoveryService} class implements a service
 * for discovering the SMA Energy Meter.
 *
 * @author Osman Basha - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.smaenergymeter")
public class SMAEnergyMeterDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SMAEnergyMeterDiscoveryService.class);

    public SMAEnergyMeterDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 15, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start SMAEnergyMeter background discovery");
        scheduler.schedule(this::discover, 0, TimeUnit.SECONDS);
    }

    @Override
    public void startScan() {
        logger.debug("Start SMAEnergyMeter scan");
        discover();
    }

    private synchronized void discover() {
        logger.debug("Try to discover a SMA Energy Meter device");

        EnergyMeter energyMeter = new EnergyMeter(EnergyMeter.DEFAULT_MCAST_GRP, EnergyMeter.DEFAULT_MCAST_PORT);
        try {
            energyMeter.update();
        } catch (IOException e) {
            logger.debug("No SMA Energy Meter found.");
            logger.debug("Diagnostic: ", e);
            return;
        }

        logger.debug("Adding a new SMA Engergy Meter with S/N '{}' to inbox", energyMeter.getSerialNumber());
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, "SMA");
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, energyMeter.getSerialNumber());
        ThingUID uid = new ThingUID(THING_TYPE_ENERGY_METER, energyMeter.getSerialNumber());
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel("SMA Energy Meter").build();
        thingDiscovered(result);

        logger.debug("Thing discovered '{}'", result);
    }
}
