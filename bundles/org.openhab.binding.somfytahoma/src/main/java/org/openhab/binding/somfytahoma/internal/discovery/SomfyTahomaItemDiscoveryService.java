/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal.discovery;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaBridgeHandler;
import org.openhab.binding.somfytahoma.internal.model.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaItemDiscoveryService} discovers rollershutters and
 * action groups associated with your TahomaLink cloud account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaItemDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaItemDiscoveryService.class);

    private SomfyTahomaBridgeHandler bridge;

    private @Nullable DiscoveryServiceCallback discoveryServiceCallback;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private static final int DISCOVERY_TIMEOUT_SEC = 10;
    private static final int DISCOVERY_REFRESH_SEC = 1800;

    public SomfyTahomaItemDiscoveryService(SomfyTahomaBridgeHandler bridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
        this.bridge = bridgeHandler;
    }

    /**
     * Called on component activation.
     */
    @Override
    @Activate
    public void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Deactivate
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
                    TimeUnit.SECONDS);
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
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        runDiscovery();
    }

    private synchronized void runDiscovery() {
        logger.debug("Starting scanning for things...");

        if (bridge.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            SomfyTahomaSetup devices = bridge.listDevices();

            if (devices == null) {
                return;
            }

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
                if (hasState(device, CYCLIC_BUTTON_STATE)) {
                    deviceDiscovered(device, THING_TYPE_POD);
                }
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
            case PERGOLA:
                deviceDiscovered(device, THING_TYPE_PERGOLA);
                break;
            case WINDOWHANDLE:
                deviceDiscovered(device, THING_TYPE_WINDOW_HANDLE);
                break;
            case TEMPERATURESENSOR:
                deviceDiscovered(device, THING_TYPE_TEMPERATURESENSOR);
                break;
            case GATE:
                deviceDiscovered(device, THING_TYPE_GATE);
                break;
            case ELECTRICITYSENSOR:
                if (hasEnergyConsumption(device)) {
                    deviceDiscovered(device, THING_TYPE_ELECTRICITYSENSOR);
                } else {
                    logUnsupportedDevice(device);
                }
                break;
            case DOCK:
                deviceDiscovered(device, THING_TYPE_DOCK);
                break;
            case SIREN:
                deviceDiscovered(device, THING_TYPE_SIREN);
                break;
            case PROTOCOLGATEWAY:
            case REMOTECONTROLLER:
            case NETWORKCOMPONENT:
                break;
            default:
                logUnsupportedDevice(device);
        }
    }

    private boolean isStateLess(SomfyTahomaDevice device) {
        return device.getStates().size() == 0 || (device.getStates().size() == 1 && hasState(device, STATUS_STATE));
    }

    private void logUnsupportedDevice(SomfyTahomaDevice device) {
        if (!isStateLess(device)) {
            logger.info("Detected a new unsupported device: {}", device.getUiClass());
            logger.info("If you want to add the support, please create a new issue and attach the information below");
            logger.info("Supported commands: {}", device.getDefinition());

            StringBuilder sb = new StringBuilder().append('\n');
            for (SomfyTahomaState state : device.getStates()) {
                sb.append(state.toString()).append('\n');
            }
            logger.info("Device states: {}", sb);
        }
    }

    private boolean hasState(SomfyTahomaDevice device, String state) {
        for (SomfyTahomaState st : device.getStates()) {
            if (state.equals(st.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEnergyConsumption(SomfyTahomaDevice device) {
        return hasState(device, ENERGY_CONSUMPTION_STATE);
    }

    private boolean isSilentRollerShutter(SomfyTahomaDevice device) {
        return hasCommmand(device, COMMAND_SET_CLOSURESPEED);
    }

    private boolean isOnOffHeatingSystem(SomfyTahomaDevice device) {
        return hasCommmand(device, COMMAND_SET_HEATINGLEVEL);
    }

    private boolean hasCommmand(SomfyTahomaDevice device, String command) {
        SomfyTahomaDeviceDefinition def = device.getDefinition();
        for (SomfyTahomaDeviceDefinitionCommand cmd : def.getCommands()) {
            if (command.equals(cmd.getCommandName())) {
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
