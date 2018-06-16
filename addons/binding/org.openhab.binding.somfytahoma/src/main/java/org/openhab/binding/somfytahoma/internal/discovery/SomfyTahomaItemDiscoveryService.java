/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.somfytahoma.handler.SomfyTahomaBaseThingHandler;
import org.openhab.binding.somfytahoma.handler.SomfyTahomaBridgeHandler;
import org.openhab.binding.somfytahoma.internal.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaItemDiscoveryService} discovers rollershutters and
 * action groups associated with your TahomaLink cloud account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaItemDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaItemDiscoveryService.class);
    private SomfyTahomaBridgeHandler bridge;
    private DiscoveryServiceCallback discoveryServiceCallback;
    private ScheduledFuture<?> discoveryJob;


    private static final int DISCOVERY_TIMEOUT_SEC = 10;
    private static final int DISCOVERY_REFRESH_SEC = 30;

    public SomfyTahomaItemDiscoveryService(SomfyTahomaBridgeHandler bridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
        this.bridge = bridgeHandler;
    }

    /**
     * Called on component activation.
     */
    public void activate() {
        super.activate(new HashMap<>());
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting SomfyTahoma background discovery");

        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::runDiscovery, 10, DISCOVERY_REFRESH_SEC,
                    TimeUnit.MINUTES);
        }

    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping SomfyTahoma background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public boolean isBackgroundDiscoveryEnabled() {
        return true;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return new HashSet<>(Arrays.asList(THING_TYPE_GATEWAY, THING_TYPE_ROLLERSHUTTER,
                THING_TYPE_ROLLERSHUTTER_SILENT, THING_TYPE_SCREEN, THING_TYPE_VENETIANBLIND, THING_TYPE_EXTERIORSCREEN,
                THING_TYPE_EXTERIORVENETIANBLIND, THING_TYPE_GARAGEDOOR, THING_TYPE_ACTIONGROUP, THING_TYPE_AWNING,
                THING_TYPE_ONOFF, THING_TYPE_LIGHT, THING_TYPE_LIGHTSENSOR, THING_TYPE_SMOKESENSOR,
                THING_TYPE_CONTACTSENSOR, THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_WINDOW, THING_TYPE_EXTERNAL_ALARM,
                THING_TYPE_INTERNAL_ALARM, THING_TYPE_POD, THING_TYPE_HEATING_SYSTEM, THING_TYPE_ONOFF_HEATING_SYSTEM, THING_TYPE_DOOR_LOCK));
    }

    @Override
    protected void startScan() {
        runDiscovery();
    }

    private synchronized void runDiscovery() {
        logger.debug("Starting scanning for things...");

        if (bridge.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            SomfyTahomaSetup devices = bridge.listDevices();
            for (SomfyTahomaDevice device : devices.getDevices()) {
                discoverDevice(device);
            }
            for (SomfyTahomaGateway gw : devices.getGateways()) {
                gatewayDiscovered(gw.getGatewayId());
            }

            List<SomfyTahomaActionGroup> actions = bridge.listActionGroups();

            for (SomfyTahomaActionGroup group : actions) {
                String oid = group.getOid();
                String label = group.getLabel();

                //actiongroups use oid as deviceURL
                actionGroupDiscovered(label, oid, oid);
            }
        } else {
            logger.debug("Cannot start discovery since the bridge is not online!");
        }
    }

    private void discoverDevice(SomfyTahomaDevice device) {
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
            case HEATINGSYSTEM:
                if (isOnOffHeatingSystem(device)) {
                    deviceDiscovered(device, THING_TYPE_ONOFF_HEATING_SYSTEM);
                } else {
                    deviceDiscovered(device, THING_TYPE_HEATING_SYSTEM);
                }
                break;
            case DOORLOCK:
                deviceDiscovered(device, THING_TYPE_DOOR_LOCK);
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

    private boolean isOnOffHeatingSystem(SomfyTahomaDevice device) {
        SomfyTahomaDeviceDefinition def = device.getDefinition();
        for (SomfyTahomaDeviceDefinitionCommand cmd : def.getCommands()) {
            if (cmd.getCommandName().equals(COMMAND_SET_HEATINGLEVEL)) {
                return true;
            }
        }
        return false;
    }

    private void deviceDiscovered(SomfyTahomaDevice device, ThingTypeUID thingTypeUID) {
        deviceDiscovered(device.getLabel(), device.getDeviceURL(), device.getOid(), thingTypeUID);
    }

    private void deviceDiscovered(String label, String deviceURL, String oid, ThingTypeUID thingTypeUID) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("url", deviceURL);
        properties.put("label", label);

        ThingUID thingUID = new ThingUID(thingTypeUID, bridge.getThing().getUID(), oid);

        logger.debug("Detected a/an {} - label: {} oid: {}", thingTypeUID.getId(), label, oid);
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withRepresentationProperty("url").withLabel(label)
                .withBridge(bridge.getThing().getUID()).build());
    }

    private void actionGroupDiscovered(String label, String deviceURL, String oid) {
        deviceDiscovered(label, deviceURL, oid, THING_TYPE_ACTIONGROUP);
    }

    private void gatewayDiscovered(String id) {
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
