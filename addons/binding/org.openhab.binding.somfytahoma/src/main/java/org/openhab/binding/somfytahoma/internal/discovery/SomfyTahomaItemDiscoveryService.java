/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.internal.discovery;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.somfytahoma.handler.SomfyTahomaBridgeHandler;
import org.openhab.binding.somfytahoma.model.SomfyTahomaDevice;
import org.openhab.binding.somfytahoma.model.SomfyTahomaDeviceDefinition;
import org.openhab.binding.somfytahoma.model.SomfyTahomaDeviceDefinitionCommand;
import org.openhab.binding.somfytahoma.model.SomfyTahomaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaItemDiscoveryService} discovers rollershutters and
 * action groups associated with your TahomaLink cloud account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaItemDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaItemDiscoveryService.class);
    private SomfyTahomaBridgeHandler bridge = null;
    private DiscoveryServiceCallback discoveryServiceCallback;

    private static final int DISCOVERY_TIMEOUT_SEC = 10;

    public SomfyTahomaItemDiscoveryService(SomfyTahomaBridgeHandler bridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
        this.bridge = bridgeHandler;
        bridgeHandler.setDiscoveryService(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return new HashSet<>(Arrays.asList(THING_TYPE_GATEWAY, THING_TYPE_ROLLERSHUTTER,
                THING_TYPE_ROLLERSHUTTER_SILENT, THING_TYPE_SCREEN, THING_TYPE_VENETIANBLIND, THING_TYPE_EXTERIORSCREEN,
                THING_TYPE_EXTERIORVENETIANBLIND, THING_TYPE_GARAGEDOOR, THING_TYPE_ACTIONGROUP, THING_TYPE_AWNING,
                THING_TYPE_ONOFF, THING_TYPE_LIGHT, THING_TYPE_LIGHTSENSOR, THING_TYPE_SMOKESENSOR,
                THING_TYPE_CONTACTSENSOR, THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_WINDOW, THING_TYPE_EXTERNAL_ALARM,
                THING_TYPE_INTERNAL_ALARM, THING_TYPE_POD));
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scanning for items...");
        bridge.setDiscoveryService(this);
        bridge.startDiscovery();
    }

    public void discoverDevice(SomfyTahomaDevice device) {
        logger.debug("url: {}", device.getDeviceURL());
        switch (device.getUiClass()) {
            case AWNING:
                deviceDiscovered(device, THING_TYPE_AWNING);
                break;
            case CONTACTSENSOR:
                deviceDiscovered(device, THING_TYPE_CONTACTSENSOR);
                break;
            case EXTERIORSCREEN:
                deviceDiscovered(device, THING_TYPE_EXTERIORSCREEN);
                break;
            case EXTERIORVENETIANBLIND:
                deviceDiscovered(device, THING_TYPE_EXTERIORVENETIANBLIND);
                break;
            case GARAGEDOOR:
                deviceDiscovered(device, THING_TYPE_GARAGEDOOR);
                break;
            case LIGHT:
                deviceDiscovered(device, THING_TYPE_LIGHT);
                break;
            case LIGHTSENSOR:
                deviceDiscovered(device, THING_TYPE_LIGHTSENSOR);
                break;
            case OCCUPANCYSENSOR:
                deviceDiscovered(device, THING_TYPE_OCCUPANCYSENSOR);
                break;
            case ONOFF:
                deviceDiscovered(device, THING_TYPE_ONOFF);
                break;
            case ROLLERSHUTTER:
                if (isSilentRollerShutter(device)) {
                    deviceDiscovered(device, THING_TYPE_ROLLERSHUTTER_SILENT);
                } else {
                    deviceDiscovered(device, THING_TYPE_ROLLERSHUTTER);
                }
                break;
            case SCREEN:
                deviceDiscovered(device, THING_TYPE_SCREEN);
                break;
            case SMOKESENSOR:
                deviceDiscovered(device, THING_TYPE_SMOKESENSOR);
                break;
            case VENETIANBLIND:
                deviceDiscovered(device, THING_TYPE_VENETIANBLIND);
                break;
            case WINDOW:
                deviceDiscovered(device, THING_TYPE_WINDOW);
                break;
            case ALARM:
                if (device.getDeviceURL().startsWith("internal:")) {
                    deviceDiscovered(device, THING_TYPE_INTERNAL_ALARM);
                } else {
                    deviceDiscovered(device, THING_TYPE_EXTERNAL_ALARM);
                }
                break;
            case POD:
                deviceDiscovered(device, THING_TYPE_POD);
                break;
            case PROTOCOLGATEWAY:
                break;
            default:
                logger.warn("Detected a new unsupported device: {}", device.getUiClass());
                logger.warn("Supported commands: {}", device.getDefinition().toString());

                StringBuilder sb = new StringBuilder().append('\n');
                for (SomfyTahomaState state : device.getStates()) {
                    sb.append(state.toString()).append('\n');
                }
                logger.warn("Device states: {}", sb.toString());
        }
    }

    private boolean isSilentRollerShutter(SomfyTahomaDevice device) {
        SomfyTahomaDeviceDefinition def = device.getDefinition();
        for (SomfyTahomaDeviceDefinitionCommand cmd : def.getCommands()) {
            if (cmd.getCommandName().equals(COMMAND_SET_CLOSURESPEED)) {
                return true;
            }
        }
        return false;
    }

    private void deviceDiscovered(SomfyTahomaDevice device, ThingTypeUID thingTypeUID) {
        deviceDiscovered(device.getLabel(), device.getDeviceURL(), device.getOid(), thingTypeUID);
    }

    private void deviceDiscovered(String label, String deviceURL, String oid, ThingTypeUID thingTypeUID) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("url", deviceURL);

        ThingUID thingUID = new ThingUID(thingTypeUID, bridge.getThing().getUID(), oid);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.debug("Detected a/an {} - label: {} oid: {}", thingTypeUID.getId(), label, oid);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withRepresentationProperty("url").withLabel(label)
                    .withBridge(bridge.getThing().getUID()).build());
        }
    }

    public void actionGroupDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_ACTIONGROUP);
    }

    public void gatewayDiscovered(String id) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("id", id);

        ThingUID thingUID = new ThingUID(THING_TYPE_GATEWAY, bridge.getThing().getUID(), id);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.debug("Detected a gateway with id: {}", id);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_GATEWAY)
                    .withProperties(properties).withRepresentationProperty("id").withLabel("Somfy Tahoma Gateway")
                    .withBridge(bridge.getThing().getUID()).build());
        }
    }

}
