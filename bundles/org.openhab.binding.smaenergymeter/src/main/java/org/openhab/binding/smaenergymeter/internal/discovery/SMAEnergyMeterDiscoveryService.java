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
package org.openhab.binding.smaenergymeter.internal.discovery;

import static org.openhab.binding.smaenergymeter.internal.SMAEnergyMeterBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smaenergymeter.internal.handler.EnergyMeter;
import org.openhab.binding.smaenergymeter.internal.packet.PacketListener;
import org.openhab.binding.smaenergymeter.internal.packet.PacketListenerRegistry;
import org.openhab.binding.smaenergymeter.internal.packet.PayloadHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SMAEnergyMeterDiscoveryService} class implements a service
 * for discovering the SMA Energy Meter.
 *
 * @author Osman Basha - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.smaenergymeter")
public class SMAEnergyMeterDiscoveryService extends AbstractDiscoveryService implements PayloadHandler {

    private final Logger logger = LoggerFactory.getLogger(SMAEnergyMeterDiscoveryService.class);
    private final PacketListenerRegistry listenerRegistry;
    private @Nullable PacketListener packetListener;

    @Activate
    public SMAEnergyMeterDiscoveryService(@Reference PacketListenerRegistry listenerRegistry) {
        super(SUPPORTED_THING_TYPES_UIDS, 15, true);
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        PacketListener packetListener = this.packetListener;
        if (packetListener != null) {
            return;
        }

        logger.debug("Start SMAEnergyMeter background discovery");
        try {
            packetListener = listenerRegistry.getListener(PacketListener.DEFAULT_MCAST_GRP,
                    PacketListener.DEFAULT_MCAST_PORT);
        } catch (IOException e) {
            logger.warn("Could not start background discovery", e);
            return;
        }

        packetListener.addPayloadHandler(this);
        this.packetListener = packetListener;
    }

    @Override
    protected void stopBackgroundDiscovery() {
        PacketListener packetListener = this.packetListener;
        if (packetListener != null) {
            packetListener.removePayloadHandler(this);
            this.packetListener = null;
        }
    }

    @Override
    public void startScan() {
    }

    @Override
    public void handle(EnergyMeter energyMeter) throws IOException {
        String identifier = energyMeter.getSerialNumber();
        logger.debug("Adding a new SMA Energy Meter with S/N '{}' to inbox", identifier);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, "SMA");
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, identifier);
        ThingUID uid = new ThingUID(THING_TYPE_ENERGY_METER, identifier);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).withLabel("SMA Energy Meter #" + identifier)
                .build();
        thingDiscovered(result);

        logger.debug("Thing discovered '{}'", result);
    }
}
