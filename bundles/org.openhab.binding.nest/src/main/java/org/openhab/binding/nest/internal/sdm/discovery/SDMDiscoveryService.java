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
package org.openhab.binding.nest.internal.sdm.discovery;

import static org.openhab.binding.nest.internal.sdm.SDMBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.sdm.config.SDMDeviceConfiguration;
import org.openhab.binding.nest.internal.sdm.dto.SDMDevice;
import org.openhab.binding.nest.internal.sdm.dto.SDMDeviceType;
import org.openhab.binding.nest.internal.sdm.dto.SDMParentRelation;
import org.openhab.binding.nest.internal.sdm.exception.FailedSendingSDMDataException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidSDMAccessTokenException;
import org.openhab.binding.nest.internal.sdm.handler.SDMAccountHandler;
import org.openhab.binding.nest.internal.sdm.handler.SDMBaseHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SDMDiscoveryService} is discovers devices using the SDM API list devices method.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 *
 * @see <a href="https://developers.google.com/nest/device-access/reference/rest/v1/enterprises.devices/list">
 *      https://developers.google.com/nest/device-access/reference/rest/v1/enterprises.devices/list</a>
 */
@NonNullByDefault
public class SDMDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(SDMDiscoveryService.class);
    private @NonNullByDefault({}) SDMAccountHandler accountHandler;
    private @Nullable Future<?> discoveryJob;

    public SDMDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 30, false);
    }

    protected void activate(ComponentContext context) {
    }

    @Override
    public void deactivate() {
        cancelDiscoveryJob();
        super.deactivate();
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof SDMAccountHandler sdmAccountHandler) {
            accountHandler = sdmAccountHandler;
        }
    }

    @Override
    protected void startScan() {
        cancelDiscoveryJob();
        discoveryJob = scheduler.submit(this::discoverDevices);
    }

    @Override
    protected synchronized void stopScan() {
        cancelDiscoveryJob();
        super.stopScan();
    }

    private void cancelDiscoveryJob() {
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
        }
    }

    private void discoverDevices() {
        ThingUID bridgeUID = accountHandler.getThing().getUID();
        logger.debug("Starting discovery scan for {}", bridgeUID);
        try {
            accountHandler.getAPI().listDevices().forEach(device -> addDeviceDiscoveryResult(bridgeUID, device));
        } catch (FailedSendingSDMDataException | InvalidSDMAccessTokenException e) {
            logger.debug("Exception during discovery scan for {}", bridgeUID, e);
        }
        logger.debug("Finished discovery scan for {}", bridgeUID);
    }

    private void addDeviceDiscoveryResult(ThingUID bridgeUID, SDMDevice device) {
        SDMDeviceType type = device.type;
        ThingTypeUID thingTypeUID = type == null ? null : SDM_THING_TYPE_MAPPING.get(type);
        if (type == null || thingTypeUID == null) {
            logger.debug("Ignoring unsupported device type: {}", type);
            return;
        }

        String deviceId = device.name.deviceId;
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceId);

        thingDiscovered(DiscoveryResultBuilder.create(thingUID) //
                .withThingType(thingTypeUID) //
                .withLabel(getDeviceLabel(device, type)) //
                .withBridge(bridgeUID) //
                .withProperty(SDMDeviceConfiguration.DEVICE_ID, deviceId) //
                .withProperties(new HashMap<>(SDMBaseHandler.getDeviceProperties(device))) //
                .withRepresentationProperty(SDMDeviceConfiguration.DEVICE_ID) //
                .build() //
        );
    }

    private String getDeviceLabel(SDMDevice device, SDMDeviceType type) {
        String label = device.traits.deviceInfo.customName;
        if (!label.isBlank()) {
            return label;
        }

        List<SDMParentRelation> parentRelations = device.parentRelations;
        String displayName = !parentRelations.isEmpty() ? parentRelations.get(0).displayName : "";
        String typeLabel = type.toLabel();

        return displayName.isBlank() ? String.format("Nest %s", typeLabel)
                : String.format("Nest %s %s", displayName, typeLabel);
    }
}
