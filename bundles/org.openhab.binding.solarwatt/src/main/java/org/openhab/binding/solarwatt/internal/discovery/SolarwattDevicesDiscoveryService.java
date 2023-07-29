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
package org.openhab.binding.solarwatt.internal.discovery;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.domain.model.BatteryConverter;
import org.openhab.binding.solarwatt.internal.domain.model.Device;
import org.openhab.binding.solarwatt.internal.domain.model.EVStation;
import org.openhab.binding.solarwatt.internal.domain.model.GridFlow;
import org.openhab.binding.solarwatt.internal.domain.model.Inverter;
import org.openhab.binding.solarwatt.internal.domain.model.Location;
import org.openhab.binding.solarwatt.internal.domain.model.PVPlant;
import org.openhab.binding.solarwatt.internal.domain.model.PowerMeter;
import org.openhab.binding.solarwatt.internal.handler.EnergyManagerHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service to discover devices attached to the energy manager.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SolarwattDevicesDiscoveryService extends AbstractDiscoveryService
        implements ThingHandlerService, DiscoveryService {

    private static final int TIMEOUT_SECONDS = 20;

    private final Logger logger = LoggerFactory.getLogger(SolarwattDevicesDiscoveryService.class);
    private @Nullable EnergyManagerHandler energyManagerHandler;

    /**
     * Job which will do the background scanning
     */
    private final EnergymanagerScan scanningRunnable;

    /**
     * Schedule for scanning
     */
    private @Nullable ScheduledFuture<?> scanningJob;

    public SolarwattDevicesDiscoveryService() {
        super(TIMEOUT_SECONDS);
        this.scanningRunnable = new EnergymanagerScan();

        this.activate(null);
    }

    @Override
    public void setThingHandler(final @Nullable ThingHandler handler) {
        if (handler instanceof EnergyManagerHandler) {
            this.energyManagerHandler = (EnergyManagerHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.energyManagerHandler;
    }

    @Override
    public void deactivate() {
        this.stopBackgroundDiscovery();
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> localScanningJob = this.scanningJob;
        if (localScanningJob == null || localScanningJob.isCancelled()) {
            this.scanningJob = this.scheduler.scheduleWithFixedDelay(this.scanningRunnable, 5, 5 * 60,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> localScanningJob = this.scanningJob;
        if (localScanningJob != null && !localScanningJob.isCancelled()) {
            localScanningJob.cancel(true);
            this.scanningJob = null;
        }
    }

    @Override
    protected synchronized void startScan() {
        this.removeOlderResults(this.getTimestampOfLastScan());
        final EnergyManagerHandler localEnergyManagerHandler = this.energyManagerHandler;

        if (localEnergyManagerHandler == null
                || localEnergyManagerHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            this.logger.warn("Energymanager handler not available: {}", localEnergyManagerHandler);
            return;
        }
        this.scanForDeviceThings();
    }

    /**
     * Scans for device things.
     *
     * Walks through the list of devices and adds discovery results for the supported devices.
     */
    private void scanForDeviceThings() {
        EnergyManagerHandler localEnergyManagerHandler = this.energyManagerHandler;
        if (localEnergyManagerHandler != null) {
            final Map<String, Device> devices = localEnergyManagerHandler.getDevices();

            final ThingUID bridgeUID = localEnergyManagerHandler.getThing().getUID();

            if (devices == null) {
                this.logger.warn("No device data for solarwatt devices in discovery for energy manager {}.", bridgeUID);
            } else {
                devices.forEach((key, entry) -> {
                    if (entry instanceof BatteryConverter) {
                        this.discover(bridgeUID, entry, THING_TYPE_BATTERYCONVERTER);
                    } else if (entry instanceof Inverter) {
                        this.discover(bridgeUID, entry, THING_TYPE_INVERTER);
                    } else if (entry instanceof PowerMeter) {
                        this.discover(bridgeUID, entry, THING_TYPE_POWERMETER);
                    } else if (entry instanceof EVStation) {
                        this.discover(bridgeUID, entry, THING_TYPE_EVSTATION);
                    } else if (entry instanceof Location) {
                        this.discover(bridgeUID, entry, THING_TYPE_LOCATION);
                    } else if (entry instanceof PVPlant) {
                        this.discover(bridgeUID, entry, THING_TYPE_PVPLANT);
                    } else if (entry instanceof GridFlow) {
                        this.discover(bridgeUID, entry, THING_TYPE_GRIDFLOW);
                    }
                });
            }
        }
    }

    /**
     * Create a discovery result and add to result.
     *
     * @param bridgeID to which this device belongs
     * @param entry describing the device
     * @param typeUID for matching thing
     */
    private void discover(final ThingUID bridgeID, final Device entry, final ThingTypeUID typeUID) {
        final ThingUID thingUID = new ThingUID(typeUID, bridgeID, this.rewriteGuid(entry.getGuid()));
        final Map<String, Object> properties = new HashMap<>(5);

        properties.put(THING_PROPERTIES_GUID, entry.getGuid());
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeID)
                .withRepresentationProperty(THING_PROPERTIES_GUID).withProperties(properties)
                .withLabel("Solarwatt " + entry.getLabel()).build();
        this.thingDiscovered(discoveryResult);
    }

    /**
     * Rewrite energy manager guids to be acceptable to openhab.
     *
     * @param emGuid from energy manager
     * @return guid for openhab
     */
    private String rewriteGuid(String emGuid) {
        return emGuid.replaceAll(":", "-");
    }

    public class EnergymanagerScan implements Runnable {
        @Override
        public void run() {
            SolarwattDevicesDiscoveryService.this.startScan();
        }
    }
}
