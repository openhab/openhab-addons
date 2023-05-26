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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapabilityFactoryV1;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapabilityFactoryV2;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapabilityFactoryV1;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapabilityFactoryV2;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapabilityFactoryV1;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapabilityFactoryV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link CapabilityFactory}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class CapabilityFactory {
    Map<DeviceTypes, Map<LGAPIVerion, AbstractCapabilityFactory<? extends CapabilityDefinition>>> capabilityDeviceFactories = new HashMap<>();

    private CapabilityFactory() {
        List<AbstractCapabilityFactory<?>> factories = Arrays.asList(new ACCapabilityFactoryV1(),
                new ACCapabilityFactoryV2(), new FridgeCapabilityFactoryV1(), new FridgeCapabilityFactoryV2(),
                new WasherDryerCapabilityFactoryV1(), new WasherDryerCapabilityFactoryV2());
        factories.forEach(f -> {
            f.getSupportedDeviceTypes().forEach(d -> {
                Map<LGAPIVerion, AbstractCapabilityFactory<?>> versionMap = capabilityDeviceFactories.get(d);
                if (versionMap == null) {
                    versionMap = new HashMap<>();
                }
                for (LGAPIVerion v : f.getSupportedAPIVersions()) {
                    versionMap.put(v, f);
                }
                ;
                capabilityDeviceFactories.put(d, versionMap);
            });
        });
    }

    private static final CapabilityFactory instance;
    static {
        instance = new CapabilityFactory();
    }
    private static final Logger logger = LoggerFactory.getLogger(CapabilityFactory.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CapabilityFactory getInstance() {
        return instance;
    }

    public <C extends CapabilityDefinition> C create(JsonNode rootNode, Class<C> clazz) throws LGThinqException {
        DeviceTypes type = ModelUtils.getDeviceType(rootNode);
        LGAPIVerion version = ModelUtils.discoveryAPIVersion(rootNode);
        logger.info("Getting factory for device type:{} and version:{}", type.deviceTypeId(), version);
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
