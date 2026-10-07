/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e2e.tests;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.internal.ModbusHandlerFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.model.yaml.internal.YamlModelRepositoryImpl;
import org.openhab.core.service.WatchService;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * End-to-end coverage for poller2 Things supplied by the native YAML model provider.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusPoller2YamlOSGiTest extends JavaOSGiTest {
    private static final ThingUID ENDPOINT_UID = new ThingUID("modbus:tcp:yaml-endpoint");
    private static final ThingUID POLLER_UID = new ThingUID("modbus:poller2:yaml-endpoint:holding");

    private @NonNullByDefault({}) ManagedThingProvider thingProvider;
    private @NonNullByDefault({}) ThingRegistry thingRegistry;
    private @NonNullByDefault({}) YamlModelRepositoryImpl yamlRepository;
    private @NonNullByDefault({}) Path yamlFile;

    @BeforeEach
    public void setUp() throws IOException {
        registerVolatileStorageService();
        thingProvider = getService(ManagedThingProvider.class);
        thingRegistry = getService(ThingRegistry.class);
        yamlRepository = getService(YamlModelRepositoryImpl.class);
        assertNotNull(thingProvider);
        assertNotNull(thingRegistry);
        assertNotNull(yamlRepository);

        ModbusHandlerFactory factory = getService(ThingHandlerFactory.class, ModbusHandlerFactory.class);
        ModbusManager manager = getService(ModbusManager.class);
        assertNotNull(factory);
        assertNotNull(manager);

        Bridge endpoint = BridgeBuilder.create(new ThingTypeUID("modbus", "tcp"), ENDPOINT_UID)
                .withConfiguration(new Configuration(Map.of("host", "127.0.0.1", "port", 502))).build();
        thingProvider.add(endpoint);
        waitForAssert(() -> assertNotNull(thingRegistry.get(ENDPOINT_UID)));

        WatchService watchService = getService(WatchService.class);
        assertNotNull(watchService);
        yamlFile = watchService.getWatchPath().resolve("things/poller2-e2e.yaml");
        Files.createDirectories(yamlFile.getParent());
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (Files.exists(yamlFile)) {
            Files.delete(yamlFile);
            yamlRepository.processWatchEvent(WatchService.Kind.DELETE, yamlFile);
        }
        thingProvider.remove(ENDPOINT_UID);
    }

    @Test
    public void nativeYamlCreatesDimensionedPoller2ChannelAndInitializesThing() throws IOException {
        loadYaml("""
                version: 1
                things:
                  modbus:poller2:yaml-endpoint:holding:
                    bridge: modbus:tcp:yaml-endpoint
                    config:
                      start: 100
                      length: 2
                      type: holding
                      refresh: 0
                    channels:
                      temperature:
                        itemType: Number
                        itemDimension: Temperature
                        config:
                          address: "100"
                          valueType: int16
                """);

        waitForAssert(() -> {
            Thing poller = requireNonNull(thingRegistry.get(POLLER_UID));
            assertEquals(ThingStatus.ONLINE, poller.getStatus());
            Channel temperature = poller.getChannel("temperature");
            assertNotNull(temperature);
            assertEquals("Number:Temperature", temperature.getAcceptedItemType());
        });
    }

    @Test
    public void nativeYamlProviderRefreshReinitializesPoller2Thing() throws IOException {
        loadYaml("""
                version: 1
                things:
                  modbus:poller2:yaml-endpoint:holding:
                    bridge: modbus:tcp:yaml-endpoint
                    config:
                      start: 100
                      length: 2
                      type: holding
                      refresh: 0
                    channels:
                      temperature:
                        itemType: Number
                        itemDimension: Temperature
                        config:
                          address: "100"
                          valueType: int16
                """);

        waitForAssert(() -> {
            Thing poller = requireNonNull(thingRegistry.get(POLLER_UID));
            assertNotNull(poller.getChannel("temperature"));
        });

        Files.writeString(yamlFile, """
                version: 1
                things:
                  modbus:poller2:yaml-endpoint:holding:
                    bridge: modbus:tcp:yaml-endpoint
                    config:
                      start: 100
                      length: 2
                      type: holding
                      refresh: 0
                    channels:
                      humidity:
                        itemType: Number
                        itemDimension: Dimensionless
                        config:
                          address: "101"
                          valueType: int16
                """);
        yamlRepository.processWatchEvent(WatchService.Kind.MODIFY, yamlFile);

        waitForAssert(() -> {
            Thing poller = requireNonNull(thingRegistry.get(POLLER_UID));
            assertEquals(ThingStatus.ONLINE, poller.getStatus());
            assertNull(poller.getChannel("temperature"));
            Channel humidity = poller.getChannel("humidity");
            assertNotNull(humidity);
            assertEquals("Number:Dimensionless", humidity.getAcceptedItemType());
        });
    }

    @Test
    public void nativeYamlReportsInvalidPoller2ChannelConfiguration() throws IOException {
        loadYaml("""
                version: 1
                things:
                  modbus:poller2:yaml-endpoint:holding:
                    bridge: modbus:tcp:yaml-endpoint
                    config:
                      start: 100
                      length: 1
                      type: holding
                      refresh: 0
                    channels:
                      outside-window:
                        itemType: Number
                        config:
                          address: "101"
                          valueType: int16
                """);

        waitForAssert(() -> {
            Thing poller = thingRegistry.get(POLLER_UID);
            assertNotNull(poller);
            assertEquals(ThingStatus.OFFLINE, poller.getStatus());
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, poller.getStatusInfo().getStatusDetail());
            assertEquals("Channel 'outside-window': Address 101 is outside poll window 100..100",
                    poller.getStatusInfo().getDescription());
        });
    }

    private void loadYaml(String yaml) throws IOException {
        Files.writeString(yamlFile, yaml);
        yamlRepository.processWatchEvent(WatchService.Kind.CREATE, yamlFile);
    }
}
