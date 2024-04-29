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
package org.openhab.binding.mielecloud.internal.discovery;

import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.*;
import static org.openhab.binding.mielecloud.internal.handler.MieleHandlerFactory.SUPPORTED_THING_TYPES;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.handler.MieleBridgeHandler;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for things linked to a Miele cloud account.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Do not directly listen to webservice events
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ThingDiscoveryService.class)
@NonNullByDefault
public class ThingDiscoveryService extends AbstractThingHandlerDiscoveryService<MieleBridgeHandler> {
    private static final int BACKGROUND_DISCOVERY_TIMEOUT_IN_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean discoveringDevices = false;

    /**
     * Creates a new {@link ThingDiscoveryService}.
     */
    public ThingDiscoveryService() {
        super(MieleBridgeHandler.class, SUPPORTED_THING_TYPES, BACKGROUND_DISCOVERY_TIMEOUT_IN_SECONDS);
    }

    @Nullable
    private ThingUID getBridgeUid() {
        var bridgeHandler = this.thingHandler;
        if (bridgeHandler == null) {
            return null;
        } else {
            return bridgeHandler.getThing().getUID();
        }
    }

    @Override
    protected void startScan() {
    }

    @Override
    public void activate() {
        super.activate(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, true));
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(System.currentTimeMillis(), getBridgeUid());
    }

    /**
     * Invoked when a device state update is received from the Miele cloud.
     */
    public void onDeviceStateUpdated(DeviceState deviceState) {
        if (!discoveringDevices) {
            return;
        }

        Optional<ThingTypeUID> thingTypeUid = getThingTypeUID(deviceState);
        if (thingTypeUid.isPresent()) {
            createDiscoveryResult(deviceState, thingTypeUid.get());
        } else {
            logger.debug("Unsupported Miele device type: {}", deviceState.getType().orElse("<Empty>"));
        }
    }

    private void createDiscoveryResult(DeviceState deviceState, ThingTypeUID thingTypeUid) {
        ThingUID thingUid = new ThingUID(thingTypeUid, thingHandler.getThing().getUID(),
                deviceState.getDeviceIdentifier());

        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(thingUid)
                .withBridge(thingHandler.getThing().getUID()).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                .withLabel(getLabel(deviceState));

        ThingInformationExtractor.extractProperties(thingTypeUid, deviceState).entrySet()
                .forEach(entry -> discoveryResultBuilder.withProperty(entry.getKey(), entry.getValue()));

        DiscoveryResult result = discoveryResultBuilder.build();

        thingDiscovered(result);
    }

    private Optional<ThingTypeUID> getThingTypeUID(DeviceState deviceState) {
        switch (deviceState.getRawType()) {
            case COFFEE_SYSTEM:
                return Optional.of(THING_TYPE_COFFEE_SYSTEM);
            case TUMBLE_DRYER:
                return Optional.of(THING_TYPE_DRYER);
            case WASHING_MACHINE:
                return Optional.of(THING_TYPE_WASHING_MACHINE);
            case WASHER_DRYER:
                return Optional.of(THING_TYPE_WASHER_DRYER);
            case FREEZER:
                return Optional.of(THING_TYPE_FREEZER);
            case FRIDGE:
                return Optional.of(THING_TYPE_FRIDGE);
            case FRIDGE_FREEZER_COMBINATION:
                return Optional.of(THING_TYPE_FRIDGE_FREEZER);
            case HOB_INDUCTION:
            case HOB_HIGHLIGHT:
                return Optional.of(THING_TYPE_HOB);
            case DISHWASHER:
                return Optional.of(THING_TYPE_DISHWASHER);
            case OVEN:
            case OVEN_MICROWAVE:
            case STEAM_OVEN:
            case STEAM_OVEN_COMBINATION:
            case STEAM_OVEN_MICROWAVE_COMBINATION:
            case DIALOGOVEN:
                return Optional.of(THING_TYPE_OVEN);
            case WINE_CABINET:
            case WINE_STORAGE_CONDITIONING_UNIT:
            case WINE_CONDITIONING_UNIT:
            case WINE_CABINET_FREEZER_COMBINATION:
                return Optional.of(THING_TYPE_WINE_STORAGE);
            case HOOD:
                return Optional.of(THING_TYPE_HOOD);
            case DISH_WARMER:
                return Optional.of(THING_TYPE_DISH_WARMER);
            case VACUUM_CLEANER:
                return Optional.of(THING_TYPE_ROBOTIC_VACUUM_CLEANER);

            default:
                if (deviceState.getRawType() != DeviceType.UNKNOWN) {
                    logger.warn("Found no matching thing type for device type {}", deviceState.getRawType());
                }
                return Optional.empty();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting background discovery");

        removeOlderResults(System.currentTimeMillis(), getBridgeUid());
        discoveringDevices = true;
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping background discovery");
        discoveringDevices = false;
    }

    /**
     * Invoked when a device is removed from the Miele cloud.
     */
    public void onDeviceRemoved(String deviceIdentifier) {
        removeOlderResults(System.currentTimeMillis(), getBridgeUid());
    }

    private String getLabel(DeviceState deviceState) {
        Optional<String> deviceName = deviceState.getDeviceName();
        if (deviceName.isPresent()) {
            return deviceName.get();
        }

        return ThingInformationExtractor.getDeviceAndTechType(deviceState).orElse("Miele Device");
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }
}
