/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.discovery;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.LutronHandlerFactory;
import org.openhab.binding.lutron.internal.handler.LeapBridgeHandler;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Area;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Device;
import org.openhab.binding.lutron.internal.protocol.leap.dto.OccupancyGroup;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LeapDeviceDiscoveryService} discovers devices paired with Lutron bridges using the LEAP protocol.
 *
 * @author Bob Adair - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = LeapDeviceDiscoveryService.class)
@NonNullByDefault
public class LeapDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<LeapBridgeHandler> {

    private static final int DISCOVERY_SERVICE_TIMEOUT = 0; // seconds

    private final Logger logger = LoggerFactory.getLogger(LeapDeviceDiscoveryService.class);

    /** Area number to name map **/
    private @Nullable Map<Integer, String> areaMap;
    private @Nullable List<OccupancyGroup> oGroupList;

    public LeapDeviceDiscoveryService() {
        super(LeapBridgeHandler.class, LutronHandlerFactory.DISCOVERABLE_DEVICE_TYPES_UIDS, DISCOVERY_SERVICE_TIMEOUT);
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }

    @Override
    protected void startScan() {
        logger.debug("Active discovery scan started");
        thingHandler.queryDiscoveryData();
    }

    public void processDeviceDefinitions(List<Device> deviceList, Map<Integer, String> zoneIdToName,
            Map<Integer, String> areaIdToName) {
        for (Device device : deviceList) {
            Integer zoneId = device.getZone();
            Integer deviceId = device.getDevice();
            Integer areaId = device.getArea();

            String label = device.getFullyQualifiedName();
            // RA3 doesn't provide a name for the above instead you
            // need to get the Area and Zone name to have it make sense
            if (label.isEmpty()) {
                String areaName = areaIdToName.getOrDefault(areaId, "");
                if (areaName.isEmpty()) {
                    label = zoneIdToName.getOrDefault(zoneId, "");
                } else {
                    label = String.format("%s - %s", areaName, zoneIdToName.getOrDefault(zoneId, ""));
                }
            }
            if (deviceId > 0) {
                logger.debug("Discovered device: {} type: {} id: {}", label, device.deviceType, deviceId);
                if (device.deviceType != null) {
                    switch (device.deviceType) {
                        case "SmartBridge":
                        case "RA2SelectMainRepeater":
                            notifyDiscovery(THING_TYPE_VIRTUALKEYPAD, deviceId, label, "model", "Caseta");
                            break;
                        case "RadioRa3Processor":
                            notifyDiscovery(THING_TYPE_VIRTUALKEYPAD, deviceId, label, "model", "RadioRA 3");
                            break;
                        case "MaestroDimmer":
                        case "SunnataDimmer":
                        case "WallDimmer":
                        case "PlugInDimmer":
                        case "DivaSmartDimmer":
                            notifyDiscovery(THING_TYPE_DIMMER, deviceId, label);
                            break;
                        case "WallSwitch":
                        case "PlugInSwitch":
                        case "DivaSmartSwitch":
                            notifyDiscovery(THING_TYPE_SWITCH, deviceId, label);
                            break;
                        case "CasetaFanSpeedController":
                        case "MaestroFanSpeedController":
                            notifyDiscovery(THING_TYPE_FAN, deviceId, label);
                            break;
                        case "Pico2Button":
                        case "PaddleSwitchPico":
                            notifyDiscovery(THING_TYPE_PICO, deviceId, label, "model", "2B");
                            break;
                        case "Pico2ButtonRaiseLower":
                            notifyDiscovery(THING_TYPE_PICO, deviceId, label, "model", "2BRL");
                            break;
                        case "Pico3ButtonRaiseLower":
                            notifyDiscovery(THING_TYPE_PICO, deviceId, label, "model", "3BRL");
                            break;
                        case "SerenaRollerShade":
                        case "SerenaHoneycombShade":
                        case "TriathlonRollerShade":
                        case "TriathlonHoneycombShade":
                        case "QsWirelessShade":
                            notifyDiscovery(THING_TYPE_SHADE, deviceId, label);
                            break;
                        case "RPSWallMountedOccupancySensor":
                            // TODO: Handle ra3 OccupancySensors, will need to get area
                            // that the sensor is associated with to get at the sensor
                            // status
                            break;
                        case "RPSOccupancySensor":
                            // Don't discover sensors. Using occupancy groups instead.
                            break;
                        default:
                            logger.info("Unrecognized device type: {} id: {}", device.deviceType, deviceId);
                            break;
                    }
                }
            }
        }
    }

    private void processOccupancyGroups() {
        Map<Integer, String> areaMap = this.areaMap;
        List<OccupancyGroup> oGroupList = this.oGroupList;

        if (areaMap != null && oGroupList != null) {
            logger.trace("Processing occupancy groups");
            for (OccupancyGroup oGroup : oGroupList) {
                logger.trace("Processing OccupancyGroup: {}", oGroup.href);
                int groupNum = oGroup.getOccupancyGroup();
                // Only process occupancy groups with associated occupancy sensors
                if (groupNum > 0 && oGroup.associatedSensors != null) {
                    String areaName;
                    if (oGroup.associatedAreas.length > 0) {
                        // If multiple associated areas are listed, use only the first
                        areaName = areaMap.get(oGroup.associatedAreas[0].getAreaNumber());
                    } else {
                        areaName = "Occupancy Group";
                    }
                    if (areaName != null) {
                        logger.debug("Discovered occupancy group: {} areas: {} area name: {}", groupNum,
                                oGroup.associatedAreas.length, areaName);
                        notifyDiscovery(THING_TYPE_OGROUP, groupNum, areaName);
                    }
                }
            }
            this.areaMap = null;
            this.oGroupList = null;
        }
    }

    public void setOccupancyGroups(List<OccupancyGroup> oGroupList) {
        logger.trace("Setting occupancy groups list");
        this.oGroupList = oGroupList;

        if (areaMap != null) {
            processOccupancyGroups();
        }
    }

    public void setAreas(List<Area> areaList) {
        Map<Integer, String> areaMap = new HashMap<>();

        logger.trace("Setting areas map");
        for (Area area : areaList) {
            int areaNum = area.getArea();
            logger.trace("Inserting area into map - num: {} name: {}", areaNum, area.name);
            if (areaNum > 0) {
                areaMap.put(areaNum, area.name);
            } else {
                logger.debug("Ignoring area with unparsable href {}", area.href);
            }
        }
        this.areaMap = areaMap;

        if (oGroupList != null) {
            processOccupancyGroups();
        }
    }

    private void notifyDiscovery(ThingTypeUID thingTypeUID, @Nullable Integer integrationId, String label,
            @Nullable String propName, @Nullable Object propValue) {
        if (integrationId == null) {
            logger.debug("Discovered {} with no integration ID or thinghandler", label);
            return;
        }
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID uid = new ThingUID(thingTypeUID, bridgeUID, integrationId.toString());

        Map<String, Object> properties = new HashMap<>();

        properties.put(INTEGRATION_ID, integrationId);
        if (propName != null && propValue != null) {
            properties.put(propName, propValue);
        }

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(label)
                .withProperties(properties).withRepresentationProperty(INTEGRATION_ID).build();
        thingDiscovered(result);
        logger.trace("Discovered {}", uid);
    }

    private void notifyDiscovery(ThingTypeUID thingTypeUID, Integer integrationId, String label) {
        notifyDiscovery(thingTypeUID, integrationId, label, null, null);
    }
}
