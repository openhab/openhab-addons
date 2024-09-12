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
package org.openhab.binding.ecovacs.internal.discovery;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.util.SchedulerTask;
import org.openhab.binding.ecovacs.internal.handler.EcovacsApiHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcovacsDeviceDiscoveryService} is used for discovering devices registered in the cloud account.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = DiscoveryService.class, configurationPid = "discovery.ecovacs")
public class EcovacsDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<EcovacsApiHandler> {
    private final Logger logger = LoggerFactory.getLogger(EcovacsDeviceDiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private Optional<EcovacsApi> api = Optional.empty();
    private final SchedulerTask onDemandScanTask = new SchedulerTask(scheduler, logger, "OnDemandScan",
            this::scanForDevices);
    private final SchedulerTask backgroundScanTask = new SchedulerTask(scheduler, logger, "BackgroundScan",
            this::scanForDevices);

    public EcovacsDeviceDiscoveryService() {
        super(EcovacsApiHandler.class, Set.of(THING_TYPE_VACUUM), DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        backgroundScanTask.scheduleRecurring(60);
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        backgroundScanTask.cancel();
    }

    public synchronized void startScanningWithApi(EcovacsApi api) {
        this.api = Optional.of(api);
        onDemandScanTask.cancel();
        startScan();
    }

    @Override
    public synchronized void startScan() {
        logger.debug("Starting Ecovacs discovery scan");
        onDemandScanTask.submit();
    }

    @Override
    public synchronized void stopScan() {
        logger.debug("Stopping Ecovacs discovery scan");
        onDemandScanTask.cancel();
        super.stopScan();
    }

    private void scanForDevices() {
        this.api.ifPresent(api -> {
            long timestampOfLastScan = getTimestampOfLastScan();
            try {
                List<EcovacsDevice> devices = api.getDevices();
                logger.debug("Ecovacs discovery found {} devices", devices.size());

                for (EcovacsDevice device : devices) {
                    deviceDiscovered(device);
                }
                for (Thing thing : thingHandler.getThing().getThings()) {
                    String serial = thing.getUID().getId();
                    if (!devices.stream().anyMatch(d -> serial.equals(d.getSerialNumber()))) {
                        thingRemoved(thing.getUID());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (EcovacsApiException e) {
                logger.debug("Could not retrieve devices from Ecovacs API", e);
            } finally {
                removeOlderResults(timestampOfLastScan);
            }
        });
    }

    private void deviceDiscovered(EcovacsDevice device) {
        ThingUID thingUID = new ThingUID(THING_TYPE_VACUUM, thingHandler.getThing().getUID(), device.getSerialNumber());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withBridge(thingHandler.getThing().getUID()).withLabel(device.getModelName())
                .withProperty(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber())
                .withProperty(Thing.PROPERTY_MODEL_ID, device.getModelName())
                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
        thingDiscovered(discoveryResult);
    }
}
