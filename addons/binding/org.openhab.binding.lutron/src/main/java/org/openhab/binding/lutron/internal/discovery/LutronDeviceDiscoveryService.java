/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery;

import static org.openhab.binding.lutron.LutronBindingConstants.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.lutron.handler.IPBridgeHandler;
import org.openhab.binding.lutron.internal.LutronHandlerFactory;
import org.openhab.binding.lutron.internal.discovery.project.Area;
import org.openhab.binding.lutron.internal.discovery.project.Device;
import org.openhab.binding.lutron.internal.discovery.project.DeviceGroup;
import org.openhab.binding.lutron.internal.discovery.project.DeviceNode;
import org.openhab.binding.lutron.internal.discovery.project.DeviceType;
import org.openhab.binding.lutron.internal.discovery.project.Output;
import org.openhab.binding.lutron.internal.discovery.project.OutputType;
import org.openhab.binding.lutron.internal.discovery.project.Project;
import org.openhab.binding.lutron.internal.xml.DbXmlInfoReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LutronDeviceDiscoveryService} finds all devices paired with a Lutron bridge.
 *
 * @author Allan Tong - Initial contribution
 */
public class LutronDeviceDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(LutronDeviceDiscoveryService.class);

    private IPBridgeHandler bridgeHandler;
    private DbXmlInfoReader dbXmlInfoReader = new DbXmlInfoReader();

    private ScheduledFuture<?> scanTask;

    public LutronDeviceDiscoveryService(IPBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(LutronHandlerFactory.DISCOVERABLE_DEVICE_TYPES_UIDS, 10);

        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected synchronized void startScan() {
        if (this.scanTask == null || this.scanTask.isDone()) {
            this.scanTask = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        readDeviceDatabase();
                    } catch (Exception e) {
                        logger.error("Error scanning for devices", e);

                        if (scanListener != null) {
                            scanListener.onErrorOccurred(e);
                        }
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    private void readDeviceDatabase() throws IOException {
        String address = "http://" + this.bridgeHandler.getIPBridgeConfig().getIpAddress() + "/DbXmlInfo.xml";
        URL dbXmlInfoUrl = new URL(address);

        Project project = this.dbXmlInfoReader.readFromXML(dbXmlInfoUrl);

        if (project != null) {
            Stack<String> locationContext = new Stack<>();

            for (Area area : project.getAreas()) {
                processArea(area, locationContext);
            }
        } else {
            logger.info("Could not read project file at {}", address);
        }
    }

    private void processArea(Area area, Stack<String> context) {
        context.push(area.getName());

        for (DeviceNode deviceNode : area.getDeviceNodes()) {
            if (deviceNode instanceof DeviceGroup) {
                processDeviceGroup((DeviceGroup) deviceNode, context);
            } else if (deviceNode instanceof Device) {
                processDevice((Device) deviceNode, context);
            }
        }

        for (Output output : area.getOutputs()) {
            processOutput(output, context);
        }

        for (Area subarea : area.getAreas()) {
            processArea(subarea, context);
        }

        context.pop();
    }

    private void processDeviceGroup(DeviceGroup deviceGroup, Stack<String> context) {
        context.push(deviceGroup.getName());

        for (Device device : deviceGroup.getDevices()) {
            processDevice(device, context);
        }

        context.pop();
    }

    private void processDevice(Device device, Stack<String> context) {
        DeviceType type = device.getDeviceType();

        if (type != null) {
            String label = generateLabel(context, device.getName());

            switch (type) {
                case MOTION_SENSOR:
                    notifyDiscovery(THING_TYPE_OCCUPANCYSENSOR, device.getIntegrationId(), label);
                    break;

                case SEETOUCH_KEYPAD:
                case HYBRID_SEETOUCH_KEYPAD:
                    notifyDiscovery(THING_TYPE_KEYPAD, device.getIntegrationId(), label);
                    break;

                case MAIN_REPEATER:
                    // Ignore bridges
                    break;
            }
        } else {
            logger.warn("Unrecognized device type {}", device.getType());
        }
    }

    private void processOutput(Output output, Stack<String> context) {
        OutputType type = output.getOutputType();

        if (type != null) {
            String label = generateLabel(context, output.getName());

            switch (type) {
                case INC:
                case MLV:
                    notifyDiscovery(THING_TYPE_DIMMER, output.getIntegrationId(), label);
                    break;

                case NON_DIM:
                    notifyDiscovery(THING_TYPE_SWITCH, output.getIntegrationId(), label);
                    break;
            }
        } else {
            logger.warn("Unrecognized output type {}", output.getType());
        }
    }

    private void notifyDiscovery(ThingTypeUID thingTypeUID, Integer integrationId, String label) {
        if (integrationId == null) {
            logger.info("Discovered {} with no integration ID", label);

            return;
        }

        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(thingTypeUID, bridgeUID, integrationId.toString());

        Map<String, Object> properties = new HashMap<>();

        properties.put(INTEGRATION_ID, integrationId);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(label)
                .withProperties(properties).withRepresentationProperty(INTEGRATION_ID).build();

        thingDiscovered(result);

        logger.debug("Discovered {}", uid);
    }

    private String generateLabel(Stack<String> context, String deviceName) {
        return String.join(" ", context) + " " + deviceName;
    }
}
