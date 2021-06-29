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
package org.openhab.binding.cul.max.internal.discovery;

import static org.openhab.binding.cul.max.internal.MaxCulBindingConstants.PROPERTY_RFADDRESS;

import java.util.Date;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.max.internal.MaxCulBindingConstants;
import org.openhab.binding.cul.max.internal.handler.DevicePairingListener;
import org.openhab.binding.cul.max.internal.handler.MaxCulCunBridgeHandler;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulDevice;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxDeviceDiscoveryService} class is used to discover MAX!
 * devices that are connected to the Cul/Cun.
 *
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@NonNullByDefault
public class MaxDeviceDiscoveryService extends AbstractDiscoveryService
        implements DevicePairingListener, DiscoveryService, ThingHandlerService {

    private static final int SEARCH_TIME = 60;
    private final Logger logger = LoggerFactory.getLogger(MaxDeviceDiscoveryService.class);

    private @Nullable MaxCulCunBridgeHandler maxCulCunBridgeHandler;

    public MaxDeviceDiscoveryService() {
        super(MaxCulBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME, true);
    }

    @Override
    public void setThingHandler(@NonNullByDefault({}) ThingHandler handler) {
        if (handler instanceof MaxCulCunBridgeHandler) {
            this.maxCulCunBridgeHandler = (MaxCulCunBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return maxCulCunBridgeHandler;
    }

    @Override
    public void activate() {
        MaxCulCunBridgeHandler localMaxCulCunBridgeHandler = maxCulCunBridgeHandler;
        if (localMaxCulCunBridgeHandler != null) {
            localMaxCulCunBridgeHandler.registerDeviceStatusListener(this);
        }
    }

    @Override
    public void deactivate() {
        MaxCulCunBridgeHandler localMaxCulCunBridgeHandler = maxCulCunBridgeHandler;
        if (localMaxCulCunBridgeHandler != null) {
            localMaxCulCunBridgeHandler.unregisterDeviceStatusListener(this);
            removeOlderResults(new Date().getTime(), localMaxCulCunBridgeHandler.getThing().getUID());
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return MaxCulBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    public void onDeviceAdded(Bridge bridge, String rfAddress, MaxCulDevice deviceType, String serialNumber) {
        logger.trace("Adding new MAX! {} with id '{}' to inbox", deviceType, serialNumber);
        ThingUID thingUID = null;
        switch (deviceType) {
            case WALL_THERMOSTAT:
                thingUID = new ThingUID(MaxCulBindingConstants.WALLTHERMOSTAT_THING_TYPE, bridge.getUID(),
                        serialNumber);
                break;
            case RADIATOR_THERMOSTAT:
                thingUID = new ThingUID(MaxCulBindingConstants.HEATINGTHERMOSTAT_THING_TYPE, bridge.getUID(),
                        serialNumber);
                break;
            case RADIATOR_THERMOSTAT_PLUS:
                thingUID = new ThingUID(MaxCulBindingConstants.HEATINGTHERMOSTATPLUS_THING_TYPE, bridge.getUID(),
                        serialNumber);
                break;
            case SHUTTER_CONTACT:
                thingUID = new ThingUID(MaxCulBindingConstants.SHUTTERCONTACT_THING_TYPE, bridge.getUID(),
                        serialNumber);
                break;
            case PUSH_BUTTON:
                thingUID = new ThingUID(MaxCulBindingConstants.ECOSWITCH_THING_TYPE, bridge.getUID(), serialNumber);
                break;
            default:
                break;
        }
        if (thingUID != null) {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withProperty(Thing.PROPERTY_SERIAL_NUMBER, serialNumber)
                    .withProperty(PROPERTY_RFADDRESS, rfAddress).withBridge(bridge.getUID())
                    .withLabel(deviceType + ": " + serialNumber)
                    .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered MAX! device is unsupported: type '{}' with id '{}'", deviceType, serialNumber);
        }
    }

    @Override
    protected void startScan() {
        // this can be ignored here
    }
}
