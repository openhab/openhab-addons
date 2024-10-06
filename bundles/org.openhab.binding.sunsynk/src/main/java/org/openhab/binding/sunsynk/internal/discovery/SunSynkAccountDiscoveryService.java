/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.SunSynkBindingConstants;
import org.openhab.binding.sunsynk.internal.api.dto.Inverter;
import org.openhab.binding.sunsynk.internal.handler.SunSynkAccountHandler;
import org.openhab.binding.sunsynk.internal.handler.SunSynkHandlerFactory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SunSynkAccountDiscoveryService} is responsible for starting the discovery procedure
 * that connects to SunSynk Web and imports all registered inverters.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkAccountDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SunSynkAccountDiscoveryService.class);
    private static final int TIMEOUT = 15;
    private final SunSynkAccountHandler handler;
    private final ThingUID bridgeUID;
    private @Nullable ScheduledFuture<?> scanTask;

    public SunSynkAccountDiscoveryService(SunSynkAccountHandler handler) {
        super(SunSynkHandlerFactory.DISCOVERABLE_THING_TYPE_UIDS, TIMEOUT);
        this.handler = handler;
        this.bridgeUID = handler.getThing().getUID();
    }

    private void findInverters() {
        List<Inverter> inverters = handler.getInvertersFromSunSynk();
        for (Inverter inverter : inverters) {
            addThing(inverter);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        findInverters();
    }

    @Override
    protected void startScan() {
        this.scanTask = scheduler.schedule(this::findInverters, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    private void addThing(Inverter inverter) {
        logger.debug("addThing(): Adding new SunSynk Inverter unit ({}) to the inbox", inverter.getAlias());
        Map<String, Object> properties = new HashMap<>();
        ThingUID thingUID = new ThingUID(SunSynkBindingConstants.THING_TYPE_INVERTER, bridgeUID, inverter.getUID());
        properties.put(SunSynkBindingConstants.CONFIG_GATE_SERIAL, inverter.getGateSerialNo());
        properties.put(SunSynkBindingConstants.CONFIG_SERIAL, inverter.getSerialNo());
        properties.put(Thing.PROPERTY_MODEL_ID, inverter.getID());
        properties.put(SunSynkBindingConstants.CONFIG_NAME, inverter.getAlias());
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withLabel(inverter.getAlias()).withBridge(bridgeUID)
                .withProperty("serialnumber", inverter.getSerialNo()).withRepresentationProperty("serialnumber")
                .withProperties(properties).build());
    }
}
