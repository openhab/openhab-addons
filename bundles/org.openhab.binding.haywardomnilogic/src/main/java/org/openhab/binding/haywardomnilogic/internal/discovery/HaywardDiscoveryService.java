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
package org.openhab.binding.haywardomnilogic.internal.discovery;

import static org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants.THING_TYPES_UIDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.HaywardException;
import org.openhab.binding.haywardomnilogic.internal.HaywardTypeToRequest;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the discovery results and details
 *
 * @author Matt Myers - Initial contribution
 */

@NonNullByDefault
public class HaywardDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(HaywardDiscoveryService.class);
    private @Nullable HaywardBridgeHandler discoveryBridgehandler;

    public HaywardDiscoveryService() {
        super(THING_TYPES_UIDS, 0, false);
    }

    /**
     * Constructs a zone discovery service.
     * Registers this zone discovery service programmatically.
     * Call {@link ZoneDiscoveryService#destroy()} to unregister the service after use.
     */
    public HaywardDiscoveryService(HaywardBridgeHandler bridge) throws IllegalArgumentException {
        super(THING_TYPES_UIDS, 0, false);
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
    protected void startScan() {
        HaywardBridgeHandler bridgehandler = discoveryBridgehandler;
        try {
            if (bridgehandler != null) {
                String xmlResults = bridgehandler.getMspConfig();
                mspConfigDiscovery(xmlResults);
            }
        } catch (HaywardException e) {
            logger.warn("Exception during discovery scan: {}", e.getMessage());
        } catch (InterruptedException e) {
            return;
        }
    }

    public synchronized boolean mspConfigDiscovery(String xmlResponse) {
        List<String> systemIDs = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> bowName = new ArrayList<>();
        List<String> bowID = new ArrayList<>();
        List<String> property1 = new ArrayList<>();
        List<String> property2 = new ArrayList<>();
        List<String> property3 = new ArrayList<>();
        List<String> property4 = new ArrayList<>();
        Map<String, Object> properties = new HashMap<>();

        HaywardBridgeHandler bridgehandler = discoveryBridgehandler;

        if (bridgehandler != null) {
            // Find Backyard
            names = bridgehandler.evaluateXPath("//Backyard/Name/text()", xmlResponse);

            for (int i = 0; i < names.size(); i++) {
                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.BACKYARD);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, bridgehandler.account.mspSystemID);

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_BACKYARD, names.get(i), properties);
            }

            // Find Bodies of Water
            systemIDs = bridgehandler.evaluateXPath("//Body-of-water/System-Id/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//Body-of-water/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.BOW);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_BOW, names.get(i), properties);
            }

            // Find Chlorinators
            systemIDs = bridgehandler.evaluateXPath("//Chlorinator/System-Id/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//Chlorinator/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
                bowName = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()", xmlResponse);

                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.CHLORINATOR);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(i));

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_CHLORINATOR, names.get(i), properties);
            }

            // Find ColorLogic Lights
            systemIDs = bridgehandler.evaluateXPath("//ColorLogic-Light/System-Id/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//ColorLogic-Light/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
                bowName = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()", xmlResponse);

                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.COLORLOGIC);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(i));

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_CHLORINATOR, names.get(i), properties);
            }

            // Find Filters
            systemIDs = bridgehandler.evaluateXPath("//Filter/System-Id/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//Filter/Name/text()", xmlResponse);
            property1 = bridgehandler.evaluateXPath("//Filter/Min-Pump-Speed/text()", xmlResponse);
            property2 = bridgehandler.evaluateXPath("//Filter/Max-Pump-Speed/text()", xmlResponse);
            property3 = bridgehandler.evaluateXPath("//Filter/Min-Pump-RPM/text()", xmlResponse);
            property4 = bridgehandler.evaluateXPath("//Filter/Max-Pump-RPM/text()", xmlResponse);
            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
                bowName = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()", xmlResponse);

                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.FILTER);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_FILTER_MINPUMPSPEED, property1.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_FILTER_MAXPUMPSPEED, property2.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_FILTER_MINPUMPRPM, property3.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_FILTER_MAXPUMPRPM, property4.get(i));

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_FILTER, names.get(i), properties);
            }

            // Find Heaters
            systemIDs = bridgehandler.evaluateXPath("//Heater-Equipment/System-Id/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//Heater-Equipment/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()", xmlResponse);

                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.HEATER);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(i));

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_HEATER, names.get(i), properties);
            }

            // Find Pumps
            systemIDs = bridgehandler.evaluateXPath("//Pump/System-Id/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//Pump/Name/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//Filter/Name/text()", xmlResponse);
            property1 = bridgehandler.evaluateXPath("//Filter/Min-Pump-Speed/text()", xmlResponse);
            property2 = bridgehandler.evaluateXPath("//Filter/Max-Pump-Speed/text()", xmlResponse);
            property3 = bridgehandler.evaluateXPath("//Filter/Min-Pump-RPM/text()", xmlResponse);
            property4 = bridgehandler.evaluateXPath("//Filter/Max-Pump-RPM/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()", xmlResponse);

                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.PUMP);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(i));

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_PUMP, names.get(i), properties);
            }

            // Find Relays
            systemIDs = bridgehandler.evaluateXPath("//Relay/System-Id/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//Relay/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
                bowName = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()", xmlResponse);

                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.RELAY);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(0));
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(0));

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_RELAY, names.get(i), properties);
            }

            // Find Virtual Heaters
            systemIDs = bridgehandler.evaluateXPath("//Heater/System-Id/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
                bowName = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()", xmlResponse);

                properties.clear();
                properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.VIRTUALHEATER);
                properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(i));
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(i));

                onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_VIRTUALHEATER, "Heater", properties);
            }

            // Find Sensors
            // Flow and water temp sensor aren't showing up in telemetry. Need example to determine how to differentiate
            // "system" sensors
            // that are reported in the BOW water temp, Filter flow switch, ORP, etc.
            systemIDs = bridgehandler.evaluateXPath("//Sensor/System-Id/text()", xmlResponse);
            names = bridgehandler.evaluateXPath("//Sensor/Name/text()", xmlResponse);
            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
                bowName = bridgehandler.evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()", xmlResponse);
                // Do not add backyard sensors that do not exist in the BOW thus bowID is null
                if (!(bowID.isEmpty())) {
                    properties.clear();
                    properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.SENSOR);
                    properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
                    properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(0));
                    properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(0));

                    onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_SENSOR, names.get(i), properties);
                }
            }
        }
        return true;
    }

    public void onDeviceDiscovered(ThingTypeUID thingType, String label, Map<String, Object> properties) {
        HaywardBridgeHandler bridgehandler = discoveryBridgehandler;
        String systemID = (String) properties.get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
        if (bridgehandler != null) {
            if (systemID != null) {
                ThingUID thingUID = new ThingUID(thingType, bridgehandler.getThing().getUID(), systemID);
                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                        .withBridge(bridgehandler.getThing().getUID())
                        .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID)
                        .withLabel("Hayward " + label).withProperties(properties).build();
                thingDiscovered(result);
            }
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HaywardBridgeHandler) {
            this.discoveryBridgehandler = (HaywardBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        HaywardBridgeHandler bridgehandler = discoveryBridgehandler;
        return bridgehandler;
    }
}
