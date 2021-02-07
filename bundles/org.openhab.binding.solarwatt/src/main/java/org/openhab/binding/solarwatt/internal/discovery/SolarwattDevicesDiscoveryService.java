/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.openhab.binding.solarwatt.internal.domain.model.*;
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
 * Discovery service to discovery devices attached to the energy manager.
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
        this.logger.debug("{} created", this);
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
    public void activate() {
        this.logger.debug("activated");
        super.activate(null);
    }

    @Override
    public void deactivate() {
        this.logger.debug("deactivated");
        this.stopBackgroundDiscovery();
    }

    @Override
    protected void startBackgroundDiscovery() {
        this.logger.debug("Start Energymanager device background discovery");
        @Nullable
        ScheduledFuture<?> localScanningJob = this.scanningJob;
        if (localScanningJob == null || localScanningJob.isCancelled()) {
            this.scanningJob = this.scheduler.scheduleWithFixedDelay(this.scanningRunnable, 5, 5 * 60,
                    TimeUnit.SECONDS);
        } else {
            this.logger.trace("scanningJob active");
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        this.logger.debug("Stop EnergyManager device background discovery");

        @Nullable
        ScheduledFuture<?> localScanningJob = this.scanningJob;
        if (localScanningJob != null && !localScanningJob.isCancelled()) {
            localScanningJob.cancel(true);
            this.scanningJob = null;
        }
    }

    @Override
    protected synchronized void startScan() {
        this.logger.debug("startScan");
        this.removeOlderResults(this.getTimestampOfLastScan());
        final EnergyManagerHandler energyManagerHandler = this.energyManagerHandler;

        if (energyManagerHandler == null || energyManagerHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            this.logger.debug("Energymanager handler not available: {}", energyManagerHandler);
            return;
        }
        this.scanForDeviceThings();
        this.logger.debug("done Scan");
    }

    /**
     * Scans for device things.
     */
    private void scanForDeviceThings() {
        @Nullable
        EnergyManagerHandler localEnergyManagerHandler = this.energyManagerHandler;
        if (localEnergyManagerHandler != null) {
            @Nullable
            final Map<String, Device> devices = localEnergyManagerHandler.getDevices();

            final ThingUID bridgeUID = localEnergyManagerHandler.getThing().getUID();

            if (devices == null) {
                this.logger.debug("No device data for solarwatt devices in discovery for energy manager {}.",
                        bridgeUID);
            } else {
                devices.forEach((key, entry) -> {
                    if (entry instanceof EnergyManager) {
                        // energy manager is our bridge
                    } else if (entry instanceof BatteryConverter) {
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
                        this.discover(bridgeUID, entry, THING_TYPE_PVPPLANT);
                    } else if (entry instanceof GridFlow) {
                        this.discover(bridgeUID, entry, THING_TYPE_GRIDFLOW);
                    } else if (entry instanceof SmartEnergyManagement || entry instanceof SimpleSwitcher) {
                        // deprecated class
                        this.logger.trace("Ignoring deprecated device {}", entry.getClass().getName());
                    } else {
                        this.logger.warn("Ignoring device {}", entry.getClass().getName());
                    }
                });
            }
        }
    }

    private void discover(final ThingUID bridgeID, final Device entry, final ThingTypeUID typeUID) {
        final ThingUID thingUID = new ThingUID(typeUID, bridgeID, this.rewriteGuid(entry.getGuid()));
        final Map<String, Object> properties = new HashMap<>(5);

        properties.put(THING_PROPERTIES_GUID, entry.getGuid());
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeID)
                .withRepresentationProperty(THING_PROPERTIES_GUID).withProperties(properties)
                .withLabel("Solarwatt " + entry.getLabel()).build();
        this.thingDiscovered(discoveryResult);
    }

    private String rewriteGuid(String serialNumber) {
        return serialNumber.replaceAll(":", "-");
    }

    public class EnergymanagerScan implements Runnable {
        @Override
        public void run() {
            SolarwattDevicesDiscoveryService.this.logger.debug("starting auto scan");
            SolarwattDevicesDiscoveryService.this.startScan();
        }
    }
}
