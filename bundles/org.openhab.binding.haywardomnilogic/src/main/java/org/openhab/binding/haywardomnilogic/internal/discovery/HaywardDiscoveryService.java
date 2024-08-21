/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Sets up the discovery results and details
 *
 * @author Matt Myers - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = HaywardDiscoveryService.class)
@NonNullByDefault
public class HaywardDiscoveryService extends AbstractThingHandlerDiscoveryService<HaywardBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(HaywardDiscoveryService.class);

    public HaywardDiscoveryService() {
        super(HaywardBridgeHandler.class, THING_TYPES_UIDS, 0, false);
    }

    @Override
    protected void startScan() {
        try {
            String xmlResults = thingHandler.getMspConfig();
            mspConfigDiscovery(xmlResults);
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

        // Find Backyard
        names = thingHandler.evaluateXPath("//Backyard/Name/text()", xmlResponse);

        for (int i = 0; i < names.size(); i++) {
            backyardProperties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.BACKYARD);
            backyardProperties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, thingHandler.account.mspSystemID);

            onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_BACKYARD, names.get(i), backyardProperties);
        }

        // Find Bodies of Water
        systemIDs = thingHandler.evaluateXPath("//Body-of-water/System-Id/text()", xmlResponse);
        names = thingHandler.evaluateXPath("//Body-of-water/Name/text()", xmlResponse);

        final List<String> bowProperty1 = thingHandler.evaluateXPath("//Body-of-water/Type/text()", xmlResponse);
        final List<String> bowProperty2 = thingHandler.evaluateXPath("//Body-of-water/Shared-Type/text()", xmlResponse);
        final List<String> bowProperty3 = thingHandler.evaluateXPath("//Body-of-water/Shared-Priority/text()",
                xmlResponse);
        final List<String> bowProperty4 = thingHandler
                .evaluateXPath("//Body-of-water/Shared-Equipment-System-ID/text()", xmlResponse);
        final List<String> bowProperty5 = thingHandler.evaluateXPath("//Body-of-water/Supports-Spillover/text()",
                xmlResponse);
        final List<String> bowProperty6 = thingHandler.evaluateXPath("//Body-of-water/Size-In-Gallons/text()",
                xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            bowProperties.put(HaywardBindingConstants.PROPERTY_TYPE, HaywardTypeToRequest.BOW);
            bowProperties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));
            bowProperties.put(HaywardBindingConstants.PROPERTY_BOW_TYPE, bowProperty1.get(i));
            bowProperties.put(HaywardBindingConstants.PROPERTY_BOW_SHAREDTYPE, bowProperty2.get(i));
            bowProperties.put(HaywardBindingConstants.PROPERTY_BOW_SHAREDPRIORITY, bowProperty3.get(i));
            bowProperties.put(HaywardBindingConstants.PROPERTY_BOW_SHAREDEQUIPID, bowProperty4.get(i));
            bowProperties.put(HaywardBindingConstants.PROPERTY_BOW_SUPPORTSSPILLOVER, bowProperty5.get(i));
            bowProperties.put(HaywardBindingConstants.PROPERTY_BOW_SIZEINGALLONS, bowProperty6.get(i));

            onDeviceDiscovered(HaywardBindingConstants.THING_TYPE_BOW, names.get(i), bowProperties);
        }

        // Find Chlorinators
        final List<String> chlorinatorProperty1 = thingHandler
                .evaluateXPath("//Body-of-water/Chlorinator/Shared-Type/text()", xmlResponse);
        final List<String> chlorinatorProperty2 = thingHandler.evaluateXPath("//Body-of-water/Chlorinator/Mode/text()",
                xmlResponse);
        final List<String> chlorinatorProperty3 = thingHandler
                .evaluateXPath("//Body-of-water/Chlorinator/Cell-Type/text()", xmlResponse);
        final List<String> chlorinatorProperty4 = thingHandler
                .evaluateXPath("//Body-of-water/Chlorinator/Dispenser-Type/text()", xmlResponse);

        discoverDevices(thingHandler, xmlResponse, "Chlorinator", HaywardTypeToRequest.CHLORINATOR,
                HaywardBindingConstants.THING_TYPE_CHLORINATOR, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_CHLORINATOR_SHAREDTYPE, chlorinatorProperty1.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_CHLORINATOR_MODE, chlorinatorProperty2.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_CHLORINATOR_CELLTYPE, chlorinatorProperty3.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_CHLORINATOR_DISPENSERTYPE, chlorinatorProperty4.get(i));
                });

        // Find ColorLogic Lights
        final List<String> colorLogicProperty1 = thingHandler.evaluateXPath("//Backyard//ColorLogic-Light/Type/text()",
                xmlResponse);

        final List<String> colorLogicProperty2 = thingHandler
                .evaluateXPath("//Backyard//ColorLogic-Light/V2-Active/text()", xmlResponse);

        for (int i = 0; i < colorLogicProperty2.size(); i++) {
            if (colorLogicProperty1.get(i).equals("COLOR_LOGIC_UCL") && colorLogicProperty2.get(i).equals("yes")) {
                colorLogicProperty1.set(i, "COLOR_LOGIC_UCL_V2");
            }
        }

        discoverDevices(thingHandler, xmlResponse, "ColorLogic-Light", HaywardTypeToRequest.COLORLOGIC,
                HaywardBindingConstants.THING_TYPE_COLORLOGIC, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_COLORLOGIC_TYPE, colorLogicProperty1.get(i));
                });

        // Find Filters
        final List<String> filterProperty1 = thingHandler.evaluateXPath("//Body-of-water/Filter/Shared-Type/text()",
                xmlResponse);
        final List<String> filterProperty2 = thingHandler.evaluateXPath("//Body-of-water/Filter/Filter-Type/text()",
                xmlResponse);
        final List<String> filterProperty3 = thingHandler.evaluateXPath("//Body-of-water/Filter/Priming-Enabled/text()",
                xmlResponse);
        final List<String> filterProperty4 = thingHandler.evaluateXPath("//Body-of-water/Filter/Min-Pump-Speed/text()",
                xmlResponse);
        final List<String> filterProperty5 = thingHandler.evaluateXPath("//Body-of-water/Filter/Max-Pump-Speed/text()",
                xmlResponse);
        final List<String> filterProperty6 = thingHandler.evaluateXPath("//Body-of-water/Filter/Min-Pump-RPM/text()",
                xmlResponse);
        final List<String> filterProperty7 = thingHandler.evaluateXPath("//Body-of-water/Filter/Max-Pump-RPM/text()",
                xmlResponse);
        final List<String> filterProperty8 = thingHandler
                .evaluateXPath("//Body-of-water/Filter/Vsp-Low-Pump-Speed/text()", xmlResponse);
        final List<String> filterProperty9 = thingHandler
                .evaluateXPath("//Body-of-water/Filter/Vsp-Medium-Pump-Speed/text()", xmlResponse);
        final List<String> filterProperty10 = thingHandler
                .evaluateXPath("//Body-of-water/Filter/Vsp-High-Pump-Speed/text()", xmlResponse);
        final List<String> filterProperty11 = thingHandler
                .evaluateXPath("//Body-of-water/Filter/Vsp-Custom-Pump-Speed/text()", xmlResponse);
        final List<String> filterProperty12 = thingHandler
                .evaluateXPath("//Body-of-water/Filter/Freeze-Protect-Override-Interval/text()", xmlResponse);

        discoverDevices(thingHandler, xmlResponse, "Filter", HaywardTypeToRequest.FILTER,
                HaywardBindingConstants.THING_TYPE_FILTER, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_SHAREDTYPE, filterProperty1.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_FILTERTYPE, filterProperty2.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_PRIMINGENABLED, filterProperty3.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MINSPEED, filterProperty4.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MAXSPEED, filterProperty5.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MINRPM, filterProperty6.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MAXRPM, filterProperty7.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_LOWSPEED, filterProperty8.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_MEDSPEED, filterProperty9.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_HIGHSPEED, filterProperty10.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_CUSTOMSPEED, filterProperty11.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_FILTER_FREEZEPROTECTOVERRIDEINTERVAL,
                            filterProperty12.get(i));
                });

        // Find Heaters
        final List<String> heaterProperty1 = thingHandler
                .evaluateXPath("//Body-of-water/Heater/Operation/Heater-Equipment/Type/text()", xmlResponse);
        final List<String> heaterProperty2 = thingHandler
                .evaluateXPath("//Body-of-water/Heater/Operation/Heater-Equipment/Heater-Type/text()", xmlResponse);
        final List<String> heaterProperty3 = thingHandler.evaluateXPath(
                "//Body-of-water/Heater/Operation/Heater-Equipment/Shared-Equipment-System-ID/text()", xmlResponse);

        discoverDevices(thingHandler, xmlResponse, "Heater-Equipment", HaywardTypeToRequest.HEATER,
                HaywardBindingConstants.THING_TYPE_HEATER, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_HEATER_TYPE, heaterProperty1.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_HEATER_HEATERTYPE, heaterProperty2.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_HEATER_SHAREDEQUIPID, heaterProperty3.get(i));
                });

        // Find Pumps
        final List<String> pumpProperty1 = thingHandler.evaluateXPath("//Body-of-water/Pump/Type/text()", xmlResponse);
        final List<String> pumpProperty2 = thingHandler.evaluateXPath("//Body-of-water/Pump/Function/text()",
                xmlResponse);
        final List<String> pumpProperty3 = thingHandler.evaluateXPath("//Body-of-water/Pump/Priming-Enabled/text()",
                xmlResponse);
        final List<String> pumpProperty4 = thingHandler.evaluateXPath("//Body-of-water/Pump/Min-Pump-Speed/text()",
                xmlResponse);
        final List<String> pumpProperty5 = thingHandler.evaluateXPath("//Body-of-water/Pump/Max-Pump-Speed/text()",
                xmlResponse);
        final List<String> pumpProperty6 = thingHandler.evaluateXPath("//Body-of-water/Pump/Min-Pump-RPM/text()",
                xmlResponse);
        final List<String> pumpProperty7 = thingHandler.evaluateXPath("//Body-of-water/Pump/Max-Pump-RPM/text()",
                xmlResponse);
        final List<String> pumpProperty8 = thingHandler.evaluateXPath("//Body-of-water/Pump/Vsp-Low-Pump-Speed/text()",
                xmlResponse);
        final List<String> pumpProperty9 = thingHandler
                .evaluateXPath("//Body-of-water/Pump/Vsp-Medium-Pump-Speed/text()", xmlResponse);
        final List<String> pumpProperty10 = thingHandler
                .evaluateXPath("//Body-of-water/Pump/Vsp-High-Pump-Speed/text()", xmlResponse);
        final List<String> pumpProperty11 = thingHandler
                .evaluateXPath("//Body-of-water/Pump/Vsp-Custom-Pump-Speed/text()", xmlResponse);

        discoverDevices(thingHandler, xmlResponse, "Pump", HaywardTypeToRequest.PUMP,
                HaywardBindingConstants.THING_TYPE_PUMP, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_TYPE, pumpProperty1.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_FUNCTION, pumpProperty2.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_PRIMINGENABLED, pumpProperty3.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_MINSPEED, pumpProperty4.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_MAXSPEED, pumpProperty5.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_MINRPM, pumpProperty6.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_MAXRPM, pumpProperty7.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_LOWSPEED, pumpProperty8.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_MEDSPEED, pumpProperty9.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_HIGHSPEED, pumpProperty10.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_PUMP_CUSTOMSPEED, pumpProperty11.get(i));
                });

        // Find Relays
        final List<String> relayProperty1 = thingHandler.evaluateXPath("//Backyard//Relay/Type/text()", xmlResponse);
        final List<String> relayProperty2 = thingHandler.evaluateXPath("//Backyard//Relay/Function/text()",
                xmlResponse);

        discoverDevices(thingHandler, xmlResponse, "Relay", HaywardTypeToRequest.RELAY,
                HaywardBindingConstants.THING_TYPE_RELAY, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_RELAY_TYPE, relayProperty1.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_RELAY_FUNCTION, relayProperty2.get(i));
                });

        // Find Virtual Heaters
        final List<String> virtualHeaterProperty1 = thingHandler
                .evaluateXPath("//Body-of-water/Heater/Shared-Type/text()", xmlResponse);
        final List<String> virtualHeaterProperty2 = thingHandler
                .evaluateXPath("//Body-of-water/Heater/Min-Settable-Water-Temp/text()", xmlResponse);
        final List<String> virtualHeaterProperty3 = thingHandler
                .evaluateXPath("//Body-of-water/Heater/Max-Settable-Water-Temp/text()", xmlResponse);
        final List<String> virtualHeaterProperty4 = thingHandler
                .evaluateXPath("//Body-of-water/Heater/Max-Water-Temp/text()", xmlResponse);

        discoverDevices(thingHandler, xmlResponse, "Heater", HaywardTypeToRequest.VIRTUALHEATER,
                HaywardBindingConstants.THING_TYPE_VIRTUALHEATER, (props, i) -> {
                    props.put(HaywardBindingConstants.PROPERTY_VIRTUALHEATER_SHAREDTYPE, virtualHeaterProperty1.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_VIRTUALHEATER_MINSETTABLEWATERTEMP,
                            virtualHeaterProperty2.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_VIRTUALHEATER_MAXSETTABLEWATERTEMP,
                            virtualHeaterProperty3.get(i));
                    props.put(HaywardBindingConstants.PROPERTY_VIRTUALHEATER_MAXWATERTEMP,
                            virtualHeaterProperty4.get(i));
                });
    }

    private void discoverDevices(HaywardBridgeHandler bridgehandler, String xmlResponse, String xmlSearchTerm,
            HaywardTypeToRequest type, ThingTypeUID thingType,
            @Nullable BiConsumer<Map<String, Object>, Integer> additionalPropertyConsumer) {
        List<String> systemIDs = bridgehandler.evaluateXPath("//Backyard//" + xmlSearchTerm + "/System-Id/text()",
                xmlResponse);
        List<String> names;

        // Set Virtual Heater Name
        if (HaywardBindingConstants.THING_TYPE_VIRTUALHEATER.equals(thingType)) {
            names = new ArrayList<>(systemIDs);
            Collections.fill(names, "Heater");
        } else {
            names = bridgehandler.evaluateXPath("//Backyard//" + xmlSearchTerm + "/Name/text()", xmlResponse);
        }

        for (int i = 0; i < systemIDs.size(); i++) {
            // get Body of Water for each item
            List<String> bowID = bridgehandler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()", xmlResponse);
            List<String> bowName = bridgehandler.evaluateXPath(
                    "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()", xmlResponse);

            Map<String, Object> properties = new HashMap<>();
            properties.put(HaywardBindingConstants.PROPERTY_TYPE, type);
            properties.put(HaywardBindingConstants.PROPERTY_SYSTEM_ID, systemIDs.get(i));

            if (!bowID.isEmpty()) {
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, bowID.get(0));
            } else {
                // Set BOWID = 0 for backyard items
                properties.put(HaywardBindingConstants.PROPERTY_BOWID, "0");
            }

            if (!bowName.isEmpty()) {
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, bowName.get(0));
            } else {
                // Set BOWNAME = Backyard for backyard items
                properties.put(HaywardBindingConstants.PROPERTY_BOWNAME, "Backyard");
            }

            if (additionalPropertyConsumer != null) {
                additionalPropertyConsumer.accept(properties, i);
            }

            onDeviceDiscovered(thingType, names.get(i), properties);
        }
    }

    public void onDeviceDiscovered(ThingTypeUID thingType, String label, Map<String, Object> properties) {
        HaywardBridgeHandler bridgehandler = thingHandler;
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
}
