/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.handler.EnergenieGatewayHandler;
import org.openhab.binding.energenie.handler.EnergenieSubdevicesHandler;
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes;
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiConfiguration;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A discovery service for Energenie Mi|Home devices using the Energenie Mi|Home REST API.
 * A {@link DiscoveryResult} is created for every registered/paired device in the user's profile
 * excluding those which already have a registered {@link Thing}
 *
 * @author Mihaela Memova - Initial contribution
 *
 */
public class EnergenieDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {

    private static final int DISCOVERY_TIMEOUT_SEC = 10;

    private DiscoveryServiceCallback discoveryServiceCallback;

    private EnergenieApiManager apiManager;
    private final Logger logger = LoggerFactory.getLogger(EnergenieDiscoveryService.class);

    public EnergenieDiscoveryService(EnergenieApiManager apiManager) {
        super(DISCOVERY_TIMEOUT_SEC);
        this.apiManager = apiManager;
    }

    @Override
    public void setDiscoveryServiceCallback(@NonNull DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    public void activate() {
        logger.debug("Starting Mi|Home discovery...");
    }

    @Override
    public void deactivate() {
        logger.debug("Stopping Mi|Home gateway discovery...");
    }

    @Override
    protected void startScan() {
        activate();
        EnergenieApiConfiguration configuration = apiManager.getConfiguration();

        if (configuration.getUserName() == null || configuration.getPassword() == null) {
            logger.debug("Your binding is not configured yet. Please set the credentials first.");
        } else {
            // List all registered gateways from profile
            JsonGateway[] allRegisteredGateways = apiManager.listGateways();
            if (allRegisteredGateways != null) {
                for (JsonGateway gateway : allRegisteredGateways) {
                    createGatewayDiscoveryResult(gateway);
                }
            } else {
                logger.error("Request for gateways information to Mi|Home server was not successful.");
            }

            // List all subdevices
            JsonSubdevice[] allRegisteredSubdevices = apiManager.listSubdevices();
            if (allRegisteredSubdevices != null) {
                for (JsonSubdevice subdevice : allRegisteredSubdevices) {
                    createSubdeviceDiscoveryResult(subdevice);
                }
            } else {
                logger.error("Request for subdevice information to Mi|Home server was not successful.");
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        deactivate();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        Set<ThingTypeUID> supported = new HashSet<>();
        supported.addAll(EnergenieSubdevicesHandler.SUPPORTED_THING_TYPES_UIDS);
        supported.add(EnergenieBindingConstants.THING_TYPE_GATEWAY);
        return supported;
    }

    private void createGatewayDiscoveryResult(JsonGateway gateway) {
        int deviceID = gateway.getID();
        logger.debug("Creating discovery result for gateway with id {}", deviceID);
        String deviceType = gateway.getType().toString();
        String label = gateway.getLabel();
        String gatewayCode = gateway.getAuthCode();
        ThingTypeUID thingTypeUID = EnergenieBindingConstants.THING_TYPE_GATEWAY;
        ThingUID thingUID = new ThingUID(thingTypeUID, Integer.toString(deviceID));
        System.out.println("Thing UID is " + thingUID);

        Thing existingThing = discoveryServiceCallback.getExistingThing(thingUID);
        System.out.println(existingThing);

        if (existingThing != null) {
            logger.debug("Gateway {} already exists", thingUID);
        } else {
            Map<String, Object> properties = new HashMap<>();
            EnergenieApiConfiguration configuration = this.apiManager.getConfiguration();
            properties.put(EnergenieBindingConstants.CONFIG_USERNAME, configuration.getUserName());
            properties.put(EnergenieBindingConstants.CONFIG_PASSWORD, configuration.getPassword());
            properties.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, deviceID);
            properties.put(EnergenieBindingConstants.PROPERTY_TYPE, deviceType);
            properties.put(EnergenieBindingConstants.PROPERTY_AUTH_CODE, gatewayCode);
            properties.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL,
                    EnergenieGatewayHandler.DEFAULT_UPDATE_INTERVAL);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withLabel(label).build();
            thingDiscovered(discoveryResult);
        }
    }

    private void createSubdeviceDiscoveryResult(JsonSubdevice subdevice) {
        int subdeviceID = subdevice.getID();
        int gatewayID = subdevice.getParentID();
        String label = subdevice.getLabel();

        logger.debug("Creating discovery result for device with id {} and gateway id {}", subdeviceID, gatewayID);
        EnergenieDeviceTypes deviceType = subdevice.getType();

        ThingTypeUID thingTypeUID = EnergenieBindingConstants.DEVICE_TYPE_TO_THING_TYPE.get(deviceType);
        ThingUID thingUID = new ThingUID(thingTypeUID, Integer.toString(subdeviceID));

        Thing existingThing = discoveryServiceCallback.getExistingThing(thingUID);

        if (existingThing != null) {
            logger.debug("Subdevice {} already exists", thingUID);
        } else {
            Map<String, Object> properties = new HashMap<>();
            properties.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, subdeviceID);
            properties.put(EnergenieBindingConstants.PROPERTY_TYPE, deviceType);
            properties.put(EnergenieBindingConstants.PROPERTY_GATEWAY_ID, gatewayID);
            properties.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL,
                    EnergenieSubdevicesHandler.DEFAULT_UPDATE_INTERVAL);

            DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID)
                    .withProperties(properties);
            ThingUID bridgeUID = new ThingUID(EnergenieBindingConstants.THING_TYPE_GATEWAY,
                    Integer.toString(gatewayID));
            Thing bridge = discoveryServiceCallback.getExistingThing(bridgeUID);

            if (bridge != null) {
                discoveryResultBuilder = discoveryResultBuilder.withBridge(bridge.getUID()).withLabel(label);
                thingDiscovered(discoveryResultBuilder.build());
            } else {
                logger.debug("Bridge with id {} does not exist", gatewayID);
            }
        }
    }

}
