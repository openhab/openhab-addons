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
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaBridgeHandler;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaActionGroup;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaDevice;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaGateway;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaRootPlace;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaSetup;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaState;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaSubPlace;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
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
 * The {@link SomfyTahomaItemDiscoveryService} discovers rollershutters and
 * action groups associated with your TahomaLink cloud account.
 *
 * @author Ondrej Pecta - Initial contribution
 * @author Laurent Garnier - Include the place into the inbox label (when defined for the device)
 */
@NonNullByDefault
public class SomfyTahomaItemDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int DISCOVERY_TIMEOUT_SEC = 10;
    private static final int DISCOVERY_REFRESH_SEC = 3600;

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaItemDiscoveryService.class);

    private @Nullable SomfyTahomaBridgeHandler bridgeHandler;

    private @Nullable ScheduledFuture<?> discoveryJob;

    public SomfyTahomaItemDiscoveryService() {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@NonNullByDefault({}) ThingHandler handler) {
        if (handler instanceof SomfyTahomaBridgeHandler) {
            bridgeHandler = (SomfyTahomaBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting SomfyTahoma background discovery");

        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::runDiscovery, 10, DISCOVERY_REFRESH_SEC,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping SomfyTahoma background discovery");
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
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

        SomfyTahomaBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null && ThingStatus.ONLINE == localBridgeHandler.getThing().getStatus()) {
            SomfyTahomaSetup setup = localBridgeHandler.getSetup();

            if (setup == null) {
                return;
            }

            for (SomfyTahomaDevice device : setup.getDevices()) {
                discoverDevice(device, setup);
            }
            for (SomfyTahomaGateway gw : setup.getGateways()) {
                gatewayDiscovered(gw);
            }

            // local mode does not have action groups
            if (!localBridgeHandler.isDevModeReady()) {
                List<SomfyTahomaActionGroup> actions = localBridgeHandler.listActionGroups();

                for (SomfyTahomaActionGroup group : actions) {
                    String oid = group.getOid();
                    String label = group.getLabel();

                    // actiongroups use oid as deviceURL
                    actionGroupDiscovered(label, oid);
                }
            }
        } else {
            logger.debug("Cannot start discovery since the bridge is not online!");
        }
    }

    private void discoverDevice(SomfyTahomaDevice device, SomfyTahomaSetup setup) {
        logger.debug("url: {}", device.getDeviceURL());
        String place = getPlaceLabel(setup, device.getPlaceOID());
        String widget = device.getDefinition().getWidgetName();
        switch (device.getDefinition().getUiClass()) {
            case CLASS_AWNING:
                // widget: PositionableHorizontalAwning
                // widget: DynamicAwning
                // widget: UpDownHorizontalAwning
                deviceDiscovered(device, THING_TYPE_AWNING, place);
                break;
            case CLASS_CONTACT_SENSOR:
                // widget: ContactSensor
                deviceDiscovered(device, THING_TYPE_CONTACTSENSOR, place);
                break;
            case CLASS_CURTAIN:
                deviceDiscovered(device, THING_TYPE_CURTAIN, place);
                break;
            case CLASS_EXTERIOR_SCREEN:
                // widget: PositionableScreen
                deviceDiscovered(device, THING_TYPE_EXTERIORSCREEN, place);
                break;
            case CLASS_EXTERIOR_VENETIAN_BLIND:
                // widget: PositionableExteriorVenetianBlind
                deviceDiscovered(device, THING_TYPE_EXTERIORVENETIANBLIND, place);
                break;
            case CLASS_GARAGE_DOOR:
                deviceDiscovered(device, THING_TYPE_GARAGEDOOR, place);
                break;
            case CLASS_LIGHT:
                if ("DimmerLight".equals(widget) || "DynamicLight".equals(widget)) {
                    // widget: DimmerLight
                    // widget: DynamicLight
                    deviceDiscovered(device, THING_TYPE_DIMMER_LIGHT, place);
                } else {
                    // widget: TimedOnOffLight
                    // widget: StatefulOnOffLight
                    deviceDiscovered(device, THING_TYPE_LIGHT, place);
                }
                break;
            case CLASS_LIGHT_SENSOR:
                deviceDiscovered(device, THING_TYPE_LIGHTSENSOR, place);
                break;
            case CLASS_OCCUPANCY_SENSOR:
                // widget: OccupancySensor
                deviceDiscovered(device, THING_TYPE_OCCUPANCYSENSOR, place);
                break;
            case CLASS_ON_OFF:
                // widget: StatefulOnOff
                deviceDiscovered(device, THING_TYPE_ONOFF, place);
                break;
            case CLASS_ROLLER_SHUTTER:
                if (isSilentRollerShutter(device)) {
                    // widget: PositionableRollerShutterWithLowSpeedManagement
                    deviceDiscovered(device, THING_TYPE_ROLLERSHUTTER_SILENT, place);
                } else if (isUnoRollerShutter(device)) {
                    // widget: PositionableRollerShutterUno
                    deviceDiscovered(device, THING_TYPE_ROLLERSHUTTER_UNO, place);
                } else {
                    // widget: PositionableRollerShutter
                    // widget: PositionableTiltedRollerShutter
                    deviceDiscovered(device, THING_TYPE_ROLLERSHUTTER, place);
                }
                break;
            case CLASS_SHUTTER:
                // widget: DynamicShutter
                deviceDiscovered(device, THING_TYPE_SHUTTER, place);
                break;
            case CLASS_SCREEN:
                // widget: PositionableTiltedScreen
                deviceDiscovered(device, THING_TYPE_SCREEN, place);
                break;
            case CLASS_SMOKE_SENSOR:
                // widget: SmokeSensor
                deviceDiscovered(device, THING_TYPE_SMOKESENSOR, place);
                break;
            case CLASS_VENETIAN_BLIND:
                // widget: DynamicVenetianBlind
                if (hasCommmand(device, "setOrientation")) {
                    deviceDiscovered(device, THING_TYPE_VENETIANBLIND, place);
                } else {
                    // simple venetian blind without orientation
                    deviceDiscovered(device, THING_TYPE_SHUTTER, place);
                }
                break;
            case CLASS_WINDOW:
                // widget: PositionableTiltedWindow
                deviceDiscovered(device, THING_TYPE_WINDOW, place);
                break;
            case CLASS_ALARM:
                if (device.getDeviceURL().startsWith("internal:")) {
                    // widget: TSKAlarmController
                    deviceDiscovered(device, THING_TYPE_INTERNAL_ALARM, place);
                } else if ("MyFoxAlarmController".equals(widget)) {
                    // widget: MyFoxAlarmController
                    deviceDiscovered(device, THING_TYPE_MYFOX_ALARM, place);
                } else {
                    deviceDiscovered(device, THING_TYPE_EXTERNAL_ALARM, place);
                }
                break;
            case CLASS_POD:
                if (hasState(device, CYCLIC_BUTTON_STATE)) {
                    deviceDiscovered(device, THING_TYPE_POD, place);
                }
                break;
            case CLASS_HEATING_SYSTEM:
                if ("SomfyThermostat".equals(widget)) {
                    deviceDiscovered(device, THING_TYPE_THERMOSTAT, place);
                } else if ("ValveHeatingTemperatureInterface".equals(widget)) {
                    deviceDiscovered(device, THING_TYPE_VALVE_HEATING_SYSTEM, place);
                } else if (isOnOffHeatingSystem(device)) {
                    deviceDiscovered(device, THING_TYPE_ONOFF_HEATING_SYSTEM, place);
                } else if (isZwaveHeatingSystem(device)) {
                    deviceDiscovered(device, THING_TYPE_ZWAVE_HEATING_SYSTEM, place);
                } else {
                    logUnsupportedDevice(device);
                }
                break;
            case CLASS_EXTERIOR_HEATING_SYSTEM:
                if ("DimmerExteriorHeating".equals(widget)) {
                    // widget: DimmerExteriorHeating
                    deviceDiscovered(device, THING_TYPE_EXTERIOR_HEATING_SYSTEM, place);
                } else {
                    logUnsupportedDevice(device);
                }
                break;
            case CLASS_HUMIDITY_SENSOR:
                if (hasState(device, WATER_DETECTION_STATE)) {
                    deviceDiscovered(device, THING_TYPE_WATERSENSOR, place);
                } else {
                    // widget: RelativeHumiditySensor
                    deviceDiscovered(device, THING_TYPE_HUMIDITYSENSOR, place);
                }
            case CLASS_DOOR_LOCK:
                // widget: UnlockDoorLockWithUnknownPosition
                deviceDiscovered(device, THING_TYPE_DOOR_LOCK, place);
                break;
            case CLASS_PERGOLA:
                if ("BioclimaticPergola".equals(widget)) {
                    // widget: BioclimaticPergola
                    deviceDiscovered(device, THING_TYPE_BIOCLIMATIC_PERGOLA, place);
                } else {
                    deviceDiscovered(device, THING_TYPE_PERGOLA, place);
                }
                break;
            case CLASS_WINDOW_HANDLE:
                // widget: ThreeWayWindowHandle
                deviceDiscovered(device, THING_TYPE_WINDOW_HANDLE, place);
                break;
            case CLASS_TEMPERATURE_SENSOR:
                // widget: TemperatureSensor
                deviceDiscovered(device, THING_TYPE_TEMPERATURESENSOR, place);
                break;
            case CLASS_GATE:
                deviceDiscovered(device, THING_TYPE_GATE, place);
                break;
            case CLASS_ELECTRICITY_SENSOR:
                if (hasEnergyConsumption(device)) {
                    deviceDiscovered(device, THING_TYPE_ELECTRICITYSENSOR, place);
                } else {
                    logUnsupportedDevice(device);
                }
                break;
            case CLASS_WATER_HEATING_SYSTEM:
                // widget: DomesticHotWaterProduction
                if ("DomesticHotWaterProduction".equals(widget)) {
                    deviceDiscovered(device, THING_TYPE_WATERHEATINGSYSTEM, place);
                } else {
                    logUnsupportedDevice(device);
                }
                break;
            case CLASS_DOCK:
                // widget: Dock
                deviceDiscovered(device, THING_TYPE_DOCK, place);
                break;
            case CLASS_SIREN:
                deviceDiscovered(device, THING_TYPE_SIREN, place);
                break;
            case CLASS_ADJUSTABLE_SLATS_ROLLER_SHUTTER:
                deviceDiscovered(device, THING_TYPE_ADJUSTABLE_SLATS_ROLLERSHUTTER, place);
                break;
            case CLASS_CAMERA:
                if (hasMyfoxShutter(device)) {
                    // widget: MyFoxSecurityCamera
                    deviceDiscovered(device, THING_TYPE_MYFOX_CAMERA, place);
                } else {
                    logUnsupportedDevice(device);
                }
                break;
            case CLASS_HITACHI_HEATING_SYSTEM:
                if ("HitachiAirToWaterHeatingZone".equals(widget)) {
                    // widget: HitachiAirToWaterHeatingZone
                    deviceDiscovered(device, THING_TYPE_HITACHI_ATWHZ, place);
                } else if ("HitachiAirToWaterMainComponent".equals(widget)) {
                    // widget: HitachiAirToWaterMainComponent
                    deviceDiscovered(device, THING_TYPE_HITACHI_ATWMC, place);
                } else if ("HitachiDHW".equals(widget)) {
                    // widget: HitachiDHW
                    deviceDiscovered(device, THING_TYPE_HITACHI_DHW, place);
                } else {
                    logUnsupportedDevice(device);
                }
                break;
            case CLASS_RAIN_SENSOR:
                if ("RainSensor".equals(widget)) {
                    // widget: RainSensor
                    deviceDiscovered(device, THING_TYPE_RAINSENSOR, place);
                } else {
                    logUnsupportedDevice(device);
                }
            case THING_PROTOCOL_GATEWAY:
            case THING_REMOTE_CONTROLLER:
                // widget: AlarmRemoteController
            case THING_NETWORK_COMPONENT:
            case THING_GENERIC:
                // widget: unknown
                break;

            default:
                logUnsupportedDevice(device);
        }
    }

    private @Nullable String getPlaceLabel(SomfyTahomaSetup setup, String oid) {
        SomfyTahomaRootPlace root = setup.getRootPlace();
        if (!oid.isEmpty() && root != null) {
            for (SomfyTahomaSubPlace place : root.getSubPlaces()) {
                if (oid.equals(place.getOid())) {
                    return place.getLabel();
                }
            }
        }
        return null;
    }

    private boolean isStateLess(SomfyTahomaDevice device) {
        return device.getStates().isEmpty() || (device.getStates().size() == 1 && hasState(device, STATUS_STATE));
    }

    private void logUnsupportedDevice(SomfyTahomaDevice device) {
        if (!isStateLess(device)) {
            logger.debug("Detected a new unsupported device: {} with widgetName: {}",
                    device.getDefinition().getUiClass(), device.getDefinition().getWidgetName());
            logger.debug("If you want to add the support, please create a new issue and attach the information below");
            logger.debug("Device definition:\n{}", device.getDefinition());

            StringBuilder sb = new StringBuilder().append('\n');
            for (SomfyTahomaState state : device.getStates()) {
                sb.append(state.toString()).append('\n');
            }
            logger.debug("Current device states: {}", sb);
        }
    }

    private boolean hasState(SomfyTahomaDevice device, String state) {
        return device.getDefinition().getStates().stream().anyMatch(st -> state.equals(st.getQualifiedName()));
    }

    private boolean hasMyfoxShutter(SomfyTahomaDevice device) {
        return hasState(device, MYFOX_SHUTTER_STATUS_STATE);
    }

    private boolean hasEnergyConsumption(SomfyTahomaDevice device) {
        return hasState(device, ENERGY_CONSUMPTION_STATE);
    }

    private boolean isSilentRollerShutter(SomfyTahomaDevice device) {
        return "PositionableRollerShutterWithLowSpeedManagement".equals(device.getDefinition().getWidgetName());
    }

    private boolean isUnoRollerShutter(SomfyTahomaDevice device) {
        return "PositionableRollerShutterUno".equals(device.getDefinition().getWidgetName());
    }

    private boolean isOnOffHeatingSystem(SomfyTahomaDevice device) {
        return hasCommmand(device, COMMAND_SET_HEATINGLEVEL);
    }

    private boolean isZwaveHeatingSystem(SomfyTahomaDevice device) {
        return hasState(device, ZWAVE_SET_POINT_TYPE_STATE);
    }

    private boolean hasCommmand(SomfyTahomaDevice device, String command) {
        return device.getDefinition().getCommands().stream().anyMatch(cmd -> command.equals(cmd.getCommandName()));
    }

    private void deviceDiscovered(SomfyTahomaDevice device, ThingTypeUID thingTypeUID, @Nullable String place) {
        String label = device.getLabel();
        if (place != null && !place.isBlank()) {
            label += " (" + place + ")";
        }
        deviceDiscovered(label, device.getDeviceURL(), thingTypeUID, hasState(device, RSSI_LEVEL_STATE));
    }

    private void deviceDiscovered(String label, String deviceURL, ThingTypeUID thingTypeUID, boolean rssi) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("url", deviceURL);
        properties.put(NAME_STATE, label);
        if (rssi) {
            properties.put(RSSI_LEVEL_STATE, "-1");
        }

        SomfyTahomaBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            ThingUID thingUID = new ThingUID(thingTypeUID, localBridgeHandler.getThing().getUID(),
                    deviceURL.replaceAll("[^a-zA-Z0-9_]", ""));

            logger.debug("Detected a/an {} - label: {} device URL: {}", thingTypeUID.getId(), label, deviceURL);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withRepresentationProperty("url").withLabel(label)
                    .withBridge(localBridgeHandler.getThing().getUID()).build());
        }
    }

    private void actionGroupDiscovered(String label, String deviceURL) {
        deviceDiscovered(label, deviceURL, THING_TYPE_ACTIONGROUP, false);
    }

    private void gatewayDiscovered(SomfyTahomaGateway gw) {
        Map<String, Object> properties = new HashMap<>(1);
        String type = gatewayTypes.getOrDefault(gw.getType(), "UNKNOWN");
        String id = gw.getGatewayId();
        properties.put("id", id);
        properties.put("type", type);

        SomfyTahomaBridgeHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            ThingUID thingUID = new ThingUID(THING_TYPE_GATEWAY, localBridgeHandler.getThing().getUID(), id);

            logger.debug("Detected a gateway with id: {} and type: {}", id, type);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_GATEWAY).withProperties(properties)
                            .withRepresentationProperty("id").withLabel("Somfy Gateway (" + type + ")")
                            .withBridge(localBridgeHandler.getThing().getUID()).build());
        }
    }
}
