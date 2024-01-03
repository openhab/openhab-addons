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
package org.openhab.binding.nikohomecontrol.internal.discovery;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcEnergyMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If a Niko Home Control bridge is added or if the user scans manually for things this
 * {@link NikoHomeControlDiscoveryService} is used to return Niko Home Control Actions as things to the framework.
 *
 * @author Mark Herwege - Initial Contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = NikoHomeControlDiscoveryService.class)
@NonNullByDefault
public class NikoHomeControlDiscoveryService
        extends AbstractThingHandlerDiscoveryService<NikoHomeControlBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlDiscoveryService.class);

    private volatile @Nullable ScheduledFuture<?> nhcDiscoveryJob;

    private static final int TIMEOUT_S = 5;
    private static final int INITIAL_DELAY_S = 5; // initial delay for polling to allow time for initial request to NHC
                                                  // controller to complete
    private static final int REFRESH_INTERVAL_S = 60;

    private @Nullable ThingUID bridgeUID;

    public NikoHomeControlDiscoveryService() {
        super(NikoHomeControlBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, TIMEOUT_S, true);
        logger.debug("device discovery service started");
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now().toEpochMilli());
    }

    /**
     * Discovers devices connected to a Niko Home Control controller
     */
    public void discoverDevices() {
        NikoHomeControlCommunication nhcComm = thingHandler.getCommunication();

        if ((nhcComm == null) || !nhcComm.communicationActive()) {
            logger.warn("not connected");
            return;
        }
        logger.debug("getting devices on {}", thingHandler.getThing().getUID().getId());

        Map<String, NhcAction> actions = nhcComm.getActions();

        actions.forEach((actionId, nhcAction) -> {
            String thingName = nhcAction.getName();
            String thingLocation = nhcAction.getLocation();

            switch (nhcAction.getType()) {
                case TRIGGER:
                    addActionDevice(new ThingUID(THING_TYPE_PUSHBUTTON, thingHandler.getThing().getUID(), actionId),
                            actionId, thingName, thingLocation);
                    break;
                case RELAY:
                    addActionDevice(new ThingUID(THING_TYPE_ON_OFF_LIGHT, thingHandler.getThing().getUID(), actionId),
                            actionId, thingName, thingLocation);
                    break;
                case DIMMER:
                    addActionDevice(new ThingUID(THING_TYPE_DIMMABLE_LIGHT, thingHandler.getThing().getUID(), actionId),
                            actionId, thingName, thingLocation);
                    break;
                case ROLLERSHUTTER:
                    addActionDevice(new ThingUID(THING_TYPE_BLIND, thingHandler.getThing().getUID(), actionId),
                            actionId, thingName, thingLocation);
                    break;
                default:
                    logger.debug("unrecognized action type {} for {} {}", nhcAction.getType(), actionId, thingName);
            }
        });

        Map<String, NhcThermostat> thermostats = nhcComm.getThermostats();

        thermostats.forEach((thermostatId, nhcThermostat) -> {
            String thingName = nhcThermostat.getName();
            String thingLocation = nhcThermostat.getLocation();
            addThermostatDevice(new ThingUID(THING_TYPE_THERMOSTAT, thingHandler.getThing().getUID(), thermostatId),
                    thermostatId, thingName, thingLocation);
        });

        Map<String, NhcEnergyMeter> energyMeters = nhcComm.getEnergyMeters();

        energyMeters.forEach((energyMeterId, nhcEnergyMeter) -> {
            String thingName = nhcEnergyMeter.getName();
            String thingLocation = nhcEnergyMeter.getLocation();
            addEnergyMeterDevice(new ThingUID(THING_TYPE_ENERGYMETER, thingHandler.getThing().getUID(), energyMeterId),
                    energyMeterId, thingName, thingLocation);
        });
    }

    private void addActionDevice(ThingUID uid, String actionId, String thingName, @Nullable String thingLocation) {
        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(thingName).withProperty(CONFIG_ACTION_ID, actionId)
                .withRepresentationProperty(CONFIG_ACTION_ID);
        if (thingLocation != null) {
            discoveryResultBuilder.withProperty("Location", thingLocation);
        }
        thingDiscovered(discoveryResultBuilder.build());
    }

    private void addThermostatDevice(ThingUID uid, String thermostatId, String thingName,
            @Nullable String thingLocation) {
        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(thingName).withProperty(CONFIG_THERMOSTAT_ID, thermostatId)
                .withRepresentationProperty(CONFIG_THERMOSTAT_ID);
        if (thingLocation != null) {
            discoveryResultBuilder.withProperty("Location", thingLocation);
        }
        thingDiscovered(discoveryResultBuilder.build());
    }

    private void addEnergyMeterDevice(ThingUID uid, String energyMeterId, String thingName,
            @Nullable String thingLocation) {
        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(thingName).withProperty(CONFIG_ENERGYMETER_ID, energyMeterId)
                .withRepresentationProperty(CONFIG_ENERGYMETER_ID);
        if (thingLocation != null) {
            discoveryResultBuilder.withProperty("Location", thingLocation);
        }
        thingDiscovered(discoveryResultBuilder.build());
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start device background discovery");
        ScheduledFuture<?> job = nhcDiscoveryJob;
        if (job == null || job.isCancelled()) {
            nhcDiscoveryJob = scheduler.scheduleWithFixedDelay(this::discoverDevices, INITIAL_DELAY_S,
                    REFRESH_INTERVAL_S, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop device background discovery");
        ScheduledFuture<?> job = nhcDiscoveryJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            nhcDiscoveryJob = null;
        }
    }

    @Override
    public void initialize() {
        bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }
}
