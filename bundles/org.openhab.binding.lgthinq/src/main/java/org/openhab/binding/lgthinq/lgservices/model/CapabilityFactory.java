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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapabilityFactoryV1;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapabilityFactoryV2;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherCapabilityFactoryV2;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapabilityFactoryV1;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapabilityFactoryV2;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapabilityFactoryV1;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapabilityFactoryV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Factory class responsible for creating {@link CapabilityDefinition} instances
 * based on the device type and API version.
 * <p>
 * This class follows the singleton pattern and maintains a registry of capability
 * factories for various LG ThinQ devices. It dynamically assigns the correct
 * {@link AbstractCapabilityFactory} based on the provided JSON node representing
 * the device.
 * </p>
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class CapabilityFactory {

    /**
     * Singleton instance of {@code CapabilityFactory}.
     */
    private static final CapabilityFactory INSTANCE = new CapabilityFactory();
    private final Logger logger = LoggerFactory.getLogger(CapabilityFactory.class);
    /**
     * A map that associates device types with their corresponding capability factories
     * based on the API version.
     */
    private final Map<DeviceTypes, Map<LGAPIVerion, AbstractCapabilityFactory<? extends CapabilityDefinition>>> capabilityDeviceFactories = new HashMap<>();

    /**
     * Private constructor to initialize the factory registry.
     * <p>
     * This constructor registers all available capability factories for different
     * device types and API versions.
     * </p>
     */
    private CapabilityFactory() {
        List<AbstractCapabilityFactory<?>> factories = Arrays.asList(new ACCapabilityFactoryV1(),
                new ACCapabilityFactoryV2(), new FridgeCapabilityFactoryV1(), new FridgeCapabilityFactoryV2(),
                new WasherDryerCapabilityFactoryV1(), new WasherDryerCapabilityFactoryV2(),
                new DishWasherCapabilityFactoryV2());

        factories.forEach(factory -> {
            factory.getSupportedDeviceTypes().forEach(deviceType -> {
                Map<LGAPIVerion, AbstractCapabilityFactory<?>> versionMap = capabilityDeviceFactories.get(deviceType);
                if (versionMap == null) {
                    versionMap = new HashMap<>();
                }
                for (LGAPIVerion version : factory.getSupportedAPIVersions()) {
                    versionMap.put(version, factory);
                }
                capabilityDeviceFactories.put(deviceType, versionMap);
            });
        });
    }

    /**
     * Retrieves the singleton instance of {@link CapabilityFactory}.
     *
     * @return The singleton instance.
     */
    public static CapabilityFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a capability definition for a given device type and API version.
     * <p>
     * The method determines the device type and API version from the provided
     * JSON node, then locates and invokes the appropriate factory to create
     * the corresponding capability definition.
     * </p>
     *
     * @param <C> The type of {@link CapabilityDefinition} expected.
     * @param rootNode The JSON node containing device information.
     * @param clazz The class type of the capability definition to be created.
     * @return An instance of the specified {@link CapabilityDefinition} type.
     * @throws LGThinqException If the capability creation fails.
     * @throws IllegalStateException If no suitable factory is found for the given type and version.
     */
    public <C extends CapabilityDefinition> C create(JsonNode rootNode, Class<C> clazz) throws LGThinqException {
        DeviceTypes type = ModelUtils.getDeviceType(rootNode);
        LGAPIVerion version = ModelUtils.discoveryAPIVersion(rootNode);
        logger.debug("Getting factory for device type: {} and version: {}", type.deviceTypeId(), version);

        Map<LGAPIVerion, AbstractCapabilityFactory<? extends CapabilityDefinition>> versionsFactory = capabilityDeviceFactories
                .get(type);
        if (versionsFactory == null || versionsFactory.isEmpty()) {
            throw new IllegalStateException("Unexpected capability. The type " + type + " was not implemented yet");
        }

        AbstractCapabilityFactory<? extends CapabilityDefinition> factory = versionsFactory.get(version);
        if (factory == null) {
            throw new IllegalStateException(
                    "Unexpected capability. The type " + type + " and version " + version + " was not implemented yet");
        }

        return clazz.cast(factory.create(rootNode));
    }
}
