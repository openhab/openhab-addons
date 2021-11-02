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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

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

    public synchronized void mspConfigDiscovery(String xmlResponse) {
        List<String> systemIDs = new ArrayList<>();
        List<String> names = new ArrayList<>();
        Map<String, Object> backyardProperties = new HashMap<>();
        Map<String, Object> bowProperties = new HashMap<>();
        HaywardBridgeHandler bridgehandler = discoveryBridgehandler;

        if (bridgehandler == null) {
            return;
        }

        // Find Backyard
        names = bridgehandler.evaluateXPath("//Backyard/Name/text()", xmlResponse);

        for (int i = 0; i < names.size(); i++) {
            backyardProperties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.BACKYARD);
            backyardProperties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, bridgehandler.account.mspSystemID);

            onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_BACKYARD, names.get(i), backyardProperties);
        }

        // Find Bodies of Water
        systemIDs = bridgehandler.evaluateXPath("//Body-of-water/System-Id/text()", xmlResponse);
        names = bridgehandler.evaluateXPath("//Body-of-water/Name/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            bowProperties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.BOW);
            bowProperties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));

            onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_BOW, names.get(i), bowProperties);
        }

        // Find Chlorinators
        discoverDevices(bridgehandler, xmlResponse, "Chlorinator", HaywardTypeToRequest.CHLORINATOR,
                HaywardBindingConstants.THING_TYPE_CHLORINATOR, null);

        // Find ColorLogic Lights
        discoverDevices(bridgehandler, xmlResponse, "ColorLogic-Light", HaywardTypeToRequest.COLORLOGIC,
                HaywardBindingConstants.THING_TYPE_COLORLOGIC, null);

        // Find Filters
        final List<String> filterProperty1 = bridgehandler.evaluateXPath("//Filter/Min-Pump-Speed/text()", xmlResponse);
        final List<String> filterProperty2 = bridgehandler.evaluateXPath("//Filter/Max-Pump-Speed/text()", xmlResponse);
        final List<String> filterProperty3 = bridgehandler.evaluateXPath("//Filter/Min-Pump-RPM/text()", xmlResponse);
        final List<String> filterProperty4 = bridgehandler.evaluateXPath("//Filter/Max-Pump-RPM/text()", xmlResponse);

        discoverDevices(bridgehandler, xmlResponse, "Filter", HaywardTypeToRequest.FILTER,
                HaywardBindingConstants.THING_TYPE_FILTER, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MINPUMPSPEED, filterProperty1.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MAXPUMPSPEED, filterProperty2.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MINPUMPRPM, filterProperty3.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MAXPUMPRPM, filterProperty4.get(i));
                });

        // Find Heaters
        discoverDevices(bridgehandler, xmlResponse, "Heater-Equipment", HaywardTypeToRequest.HEATER,
                HaywardBindingConstants.THING_TYPE_HEATER, null);

        // Find Pumps
        final List<String> pumpProperty1 = bridgehandler.evaluateXPath("//Pump/Min-Pump-Speed/text()", xmlResponse);
        final List<String> pumpProperty2 = bridgehandler.evaluateXPath("//Pump/Max-Pump-Speed/text()", xmlResponse);
        final List<String> pumpProperty3 = bridgehandler.evaluateXPath("//Pump/Min-Pump-RPM/text()", xmlResponse);
        final List<String> pumpProperty4 = bridgehandler.evaluateXPath("//Pump/Max-Pump-RPM/text()", xmlResponse);

        discoverDevices(bridgehandler, xmlResponse, "Pump", HaywardTypeToRequest.PUMP,
                HaywardBindingConstants.THING_TYPE_FILTER, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MINPUMPSPEED, pumpProperty1.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MAXPUMPSPEED, pumpProperty2.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MINPUMPRPM, pumpProperty3.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MAXPUMPRPM, pumpProperty4.get(i));
                });

        // Find Relays
        discoverDevices(bridgehandler, xmlResponse, "Relay", HaywardTypeToRequest.RELAY,
                HaywardBindingConstants.THING_TYPE_RELAY, null);

        // Find Virtual Heaters
        discoverDevices(bridgehandler, xmlResponse, "Heater", HaywardTypeToRequest.VIRTUALHEATER,
                HaywardBindingConstants.THING_TYPE_VIRTUALHEATER, null);

        // Find Sensors
        discoverDevices(bridgehandler, xmlResponse, "Sensor", HaywardTypeToRequest.SENSOR,
                HaywardBindingConstants.THING_TYPE_SENSOR, null);
    }

    private void discoverDevices(HaywardBridgeHandler bridgehandler, String xmlResponse, String xmlSearchTerm,
            HaywardTypeToRequest type, ThingTypeUID thingType,
            @Nullable BiConsumer<Map<String, Object>, Integer> additionalPropertyConsumer) {
        List<String> systemIDs = bridgehandler.evaluateXPath("//" + xmlSearchTerm + "/System-Id/text()", xmlResponse);
        List<String> names;

        // Set Virtual Heater Name
        if (HaywardBindingConstants.THING_TYPE_VIRTUALHEATER.equals(thingType)) {
            names = new ArrayList<>(systemIDs);
            Collections.fill(names, "Heater");
        } else {
            names = bridgehandler.evaluateXPath("//" + xmlSearchTerm + "/Name/text()", xmlResponse);
        }

        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            List<String> bowID = bridgehandler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()", xmlResponse);
            List<String> bowName = bridgehandler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()", xmlResponse);

            // skip system sensors with no BOW
            if (bowID.isEmpty()) {
                continue;
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put(HaywardBindingConstants.PROPERTY_TYPE, type);
            properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
            properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(0));
            properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(0));
            if (additionalPropertyConsumer != null) {
                additionalPropertyConsumer.accept(properties, i);
            }
            onDeviceDiscovered(thingType, names.get(i), properties);
        }
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
        return discoveryBridgehandler;
    }
}
