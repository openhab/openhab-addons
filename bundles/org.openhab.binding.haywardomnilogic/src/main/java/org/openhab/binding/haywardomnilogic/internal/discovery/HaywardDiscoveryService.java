/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.binding.haywardomnilogic.internal.HaywardTypeToRequest;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the discovery results and details
 *
 * @author Matt Myers - Initial Contribution
 */

@NonNullByDefault
public class HaywardDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(HaywardDiscoveryService.class);
    private @Nullable HaywardBridgeHandler handler;

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
        // this.bridge = bridge;
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
        try {
            String xmlResults = handler.getMspConfig();
            mspConfigDiscovery(xmlResults);
        } catch (Exception e) {
            logger.warn("Exception during discovery scan", e);
        }
    }

    public synchronized boolean mspConfigDiscovery(String xmlResponse) throws Exception {
        List<String> systemIDs = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> bowName = new ArrayList<>();
        List<String> bowID = new ArrayList<>();
        List<String> property1 = new ArrayList<>();
        List<String> property2 = new ArrayList<>();
        List<String> property3 = new ArrayList<>();
        List<String> property4 = new ArrayList<>();

        // Get Units (Standard, Metric)
        property1 = handler.evaluateXPath("//System/Units/text()", xmlResponse);
        handler.account.units = property1.get(0);

        // Get Variable Speed Pump Units (percent, RPM)
        property2 = handler.evaluateXPath("//System/Msp-Vsp-Speed-Format/text()", xmlResponse);
        handler.account.vspSpeedFormat = property2.get(0);

        // Find Backyard
        names = handler.evaluateXPath("//Backyard/Name/text()", xmlResponse);

        for (String name : names) {
            onBackyardDiscovered(Integer.parseInt(handler.account.mspSystemID), name);
        }

        // Find Bodies of Water
        systemIDs = handler.evaluateXPath("//Body-of-water/System-Id/text()", xmlResponse);
        names = handler.evaluateXPath("//Body-of-water/Name/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            onBOWDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i));
        }

        // Find Chlorinators
        systemIDs = handler.evaluateXPath("//Chlorinator/System-Id/text()", xmlResponse);
        names = handler.evaluateXPath("//Chlorinator/Name/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            bowID = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
            bowName = handler.evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                    xmlResponse);
            onChlorinatorDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i), bowID.get(0), bowName.get(0));
        }

        // Find ColorLogic Lights
        systemIDs = handler.evaluateXPath("//ColorLogic-Light/System-Id/text()", xmlResponse);
        names = handler.evaluateXPath("//ColorLogic-Light/Name/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            bowID = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
            bowName = handler.evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                    xmlResponse);
            onColorLogicDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i), bowID.get(0), bowName.get(0));
        }

        // Find Filters
        systemIDs = handler.evaluateXPath("//Filter/System-Id/text()", xmlResponse);
        names = handler.evaluateXPath("//Filter/Name/text()", xmlResponse);
        property1 = handler.evaluateXPath("//Filter/Min-Pump-Speed/text()", xmlResponse);
        property2 = handler.evaluateXPath("//Filter/Max-Pump-Speed/text()", xmlResponse);
        property3 = handler.evaluateXPath("//Filter/Min-Pump-RPM/text()", xmlResponse);
        property4 = handler.evaluateXPath("//Filter/Max-Pump-RPM/text()", xmlResponse);
        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            bowID = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
            bowName = handler.evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                    xmlResponse);
            onFilterDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i), bowID.get(0), bowName.get(0),
                    property1.get(i), property2.get(i), property3.get(i), property4.get(i));
        }

        // Find Heaters
        systemIDs = handler.evaluateXPath("//Heater-Equipment/System-Id/text()", xmlResponse);
        names = handler.evaluateXPath("//Heater-Equipment/Name/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            bowID = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()", xmlResponse);
            bowName = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()", xmlResponse);
            onHeaterDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i), bowID.get(0), bowName.get(0));
        }

        // Find Pumps
        systemIDs = handler.evaluateXPath("//Pump/System-Id/text()", xmlResponse);
        names = handler.evaluateXPath("//Pump/Name/text()", xmlResponse);
        names = handler.evaluateXPath("//Filter/Name/text()", xmlResponse);
        property1 = handler.evaluateXPath("//Filter/Min-Pump-Speed/text()", xmlResponse);
        property2 = handler.evaluateXPath("//Filter/Max-Pump-Speed/text()", xmlResponse);
        property3 = handler.evaluateXPath("//Filter/Min-Pump-RPM/text()", xmlResponse);
        property4 = handler.evaluateXPath("//Filter/Max-Pump-RPM/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            bowID = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()", xmlResponse);
            bowName = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()", xmlResponse);
            onPumpDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i), bowID.get(0), bowName.get(0),
                    property1.get(i), property2.get(i), property3.get(i), property4.get(i));
        }

        // Find Relays
        systemIDs = handler.evaluateXPath("//Relay/System-Id/text()", xmlResponse);
        names = handler.evaluateXPath("//Relay/Name/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            bowID = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
            bowName = handler.evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                    xmlResponse);
            if (!(bowID.isEmpty())) {
                onRelayDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i), bowID.get(0), bowName.get(0));
            } else {
                onRelayDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i), "", "");
            }

        }

        // Find Virtual Heaters
        systemIDs = handler.evaluateXPath("//Heater/System-Id/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            bowID = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
            bowName = handler.evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                    xmlResponse);
            onVirtualHeaterDiscovered(Integer.parseInt(systemIDs.get(i)), "Virtual Heater", bowID.get(0),
                    bowName.get(0));
        }

        // Find Sensors
        // Flow and water temp sensor aren't showing up in telemetry. Need example to determine how to differentiate
        // "system" sensors
        // that are reported in the BOW water temp, Filter flow switch, ORP, etc.
        systemIDs = handler.evaluateXPath("//Sensor/System-Id/text()", xmlResponse);
        names = handler.evaluateXPath("//Sensor/Name/text()", xmlResponse);
        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            bowID = handler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()", xmlResponse);
            bowName = handler.evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                    xmlResponse);
            // Do not add backyard sensors that do not exist in the BOW thus bowID is null
            if (!(bowID.isEmpty())) {
                onSensorDiscovered(Integer.parseInt(systemIDs.get(i)), names.get(i), bowID.get(0), bowName.get(0));
            }
        }
        return true;
    }

    public void onBackyardDiscovered(int systemID, String label) {
        logger.trace("Hayward Backyard {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_BACKYARD, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.BACKYARD);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onBOWDiscovered(int systemID, String label) {
        logger.trace("Hayward BOW {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_BOW, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.BOW);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onChlorinatorDiscovered(int systemID, String label, String bowID, String bowName) {
        logger.trace("Hayward Chlorinator {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_CHLORINATOR, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.CHLORINATOR);
        properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID);
        properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onColorLogicDiscovered(int systemID, String label, String bowID, String bowName) {
        logger.trace("Hayward Color Logic Light {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_COLORLOGIC, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.COLORLOGIC);
        properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID);
        properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onFilterDiscovered(int systemID, String label, String bowID, String bowName, String minPumpSpeed,
            String maxPumpSpeed, String minPumpRpm, String maxPumpRpm) {
        logger.trace("Hayward Filter {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_FILTER, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.FILTER);
        properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID);
        properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName);
        properties.put(HaywardBindingConstants.PROPERTY_FILTER_MINPUMPSPEED, minPumpSpeed);
        properties.put(HaywardBindingConstants.PROPERTY_FILTER_MAXPUMPSPEED, maxPumpSpeed);
        properties.put(HaywardBindingConstants.PROPERTY_FILTER_MINPUMPRPM, minPumpRpm);
        properties.put(HaywardBindingConstants.PROPERTY_FILTER_MAXPUMPRPM, maxPumpRpm);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onHeaterDiscovered(int systemID, String label, String bowID, String bowName) {
        logger.trace("Hayward Heater {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_HEATER, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.HEATER);
        properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID);
        properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onPumpDiscovered(int systemID, String label, String bowID, String bowName, String property1,
            String property2, String property3, String property4) {
        logger.trace("Hayward Pump {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_PUMP, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.PUMP);
        properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID);
        properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName);
        properties.put(HaywardBindingConstants.PROPERTY_PUMP_MINPUMPSPEED, property1);
        properties.put(HaywardBindingConstants.PROPERTY_PUMP_MAXPUMPSPEED, property2);
        properties.put(HaywardBindingConstants.PROPERTY_PUMP_MINPUMPRPM, property3);
        properties.put(HaywardBindingConstants.PROPERTY_PUMP_MAXPUMPRPM, property4);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onRelayDiscovered(int systemID, String label, String bowID, String bowName) {
        logger.trace("Hayward Relay {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_RELAY, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.RELAY);
        properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID);
        properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onSensorDiscovered(int systemID, String label, String bowID, String bowName) {
        logger.trace("Hayward Sensor {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_SENSOR, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.SENSOR);
        properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID);
        properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    public void onVirtualHeaterDiscovered(int systemID, String label, String bowID, String bowName) {
        logger.trace("Hayward Virtual Heater {} Discovered: {}", systemID, label);
        ThingUID thingUID = new ThingUID(HaywardBindingConstants.THING_TYPE_VIRTUALHEATER, handler.getThing().getUID(),
                Integer.toString(systemID));
        Map<String, Object> properties = new HashMap<>();
        properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, String.valueOf(systemID));
        properties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.VIRTUALHEATER.toString());
        properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID);
        properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName);
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getThing().getUID())
                .withRepresentationProperty(HaywardBindingConstants.PROPERTY_SYSTEM_ID).withLabel("Hayward " + label)
                .withProperties(properties).build();
        thingDiscovered(result);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HaywardBridgeHandler) {
            this.handler = (HaywardBridgeHandler) handler;
            this.handler.setHaywardDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
