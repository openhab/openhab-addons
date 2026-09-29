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
package org.openhab.binding.tuya.internal.console;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_DEVICE_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_PRODUCT_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_PROJECT;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.SchemaReloadException;
import org.openhab.binding.tuya.internal.TuyaSchemaDB;
import org.openhab.binding.tuya.internal.TuyaSchemaService;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.console.Console;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link TuyaCommandExtensionTest} verifies the console commands for inspecting and reloading schemas.
 *
 * @author Carlo Dischler - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class TuyaCommandExtensionTest {
    private static final String PRODUCT_ID = "consoletestprod";
    private static final String DEVICE_ID = "consoletestdevice";
    private static final ThingUID DEVICE_UID = new ThingUID(THING_TYPE_TUYA_DEVICE, DEVICE_ID);
    private static final ThingUID PROJECT_UID = new ThingUID(THING_TYPE_PROJECT, "cloud");

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistryMock;
    private @Mock @NonNullByDefault({}) TuyaSchemaService schemaServiceMock;
    private @Mock @NonNullByDefault({}) Console consoleMock;
    private @Mock @NonNullByDefault({}) Thing deviceThingMock;
    private @Mock @NonNullByDefault({}) Thing projectThingMock;

    private @NonNullByDefault({}) TuyaCommandExtension extension;

    @BeforeEach
    public void setUp() {
        extension = new TuyaCommandExtension(thingRegistryMock, schemaServiceMock);

        lenient().when(deviceThingMock.getUID()).thenReturn(DEVICE_UID);
        lenient().when(deviceThingMock.getThingTypeUID()).thenReturn(THING_TYPE_TUYA_DEVICE);
        lenient().when(deviceThingMock.getConfiguration())
                .thenReturn(new Configuration(Map.of(CONFIG_PRODUCT_ID, PRODUCT_ID, CONFIG_DEVICE_ID, DEVICE_ID)));
        lenient().when(projectThingMock.getUID()).thenReturn(PROJECT_UID);
        lenient().when(projectThingMock.getThingTypeUID()).thenReturn(THING_TYPE_PROJECT);
        lenient().when(thingRegistryMock.get(DEVICE_UID)).thenReturn(deviceThingMock);
        lenient().when(thingRegistryMock.get(PROJECT_UID)).thenReturn(projectThingMock);
        lenient().when(thingRegistryMock.getAll()).thenReturn(List.of(projectThingMock, deviceThingMock));
    }

    @AfterEach
    public void tearDown() {
        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @Test
    public void reloadStoresSchemaAndReinitializesThingsOfTheProduct() throws Exception {
        when(schemaServiceMock.reloadSchema(PRODUCT_ID, DEVICE_ID))
                .thenReturn(CompletableFuture.completedFuture(List.of(schemaDp(22, "fault", "enum"))));
        when(schemaServiceMock.reinitializeThings(PRODUCT_ID)).thenReturn(List.of(DEVICE_UID));

        extension.execute(new String[] { "reload", DEVICE_UID.getAsString() }, consoleMock);

        verify(schemaServiceMock).reinitializeThings(PRODUCT_ID);
        assertTrue(printedLines().stream().anyMatch(line -> line.contains(DEVICE_UID.getAsString())));
    }

    @Test
    public void reloadDoesNotReinitializeWhenReloadFails() throws Exception {
        when(schemaServiceMock.reloadSchema(PRODUCT_ID, DEVICE_ID)).thenReturn(
                CompletableFuture.failedFuture(new SchemaReloadException("no Tuya cloud project is connected")));

        extension.execute(new String[] { "reload", DEVICE_UID.getAsString() }, consoleMock);

        verify(schemaServiceMock, never()).reinitializeThings(anyString());
        assertTrue(printedLines().stream().anyMatch(line -> line.contains("no Tuya cloud project is connected")));
    }

    @Test
    public void reloadRejectsThingsThatAreNoTuyaDevices() throws Exception {
        extension.execute(new String[] { "reload", PROJECT_UID.getAsString() }, consoleMock);

        verify(schemaServiceMock, never()).reloadSchema(anyString(), anyString());
    }

    @Test
    public void schemaPrintsStoredDatapoints() {
        Map<String, SchemaDp> schema = new LinkedHashMap<>();
        schema.put("fault", schemaDp(22, "fault", "bitmap"));
        TuyaSchemaDB.cache.put(PRODUCT_ID, schema);

        extension.execute(new String[] { "schema", PRODUCT_ID }, consoleMock);

        ArgumentCaptor<Object[]> arguments = ArgumentCaptor.forClass(Object[].class);
        verify(consoleMock, atLeastOnce()).printf(anyString(), arguments.capture());
        assertTrue(arguments.getAllValues().stream()
                .anyMatch(line -> List.of(line).contains("fault") && List.of(line).contains("bitmap")));
    }

    private List<String> printedLines() {
        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(consoleMock, atLeastOnce()).println(lines.capture());
        return lines.getAllValues();
    }

    private static SchemaDp schemaDp(int id, String code, String type) {
        SchemaDp schemaDp = new SchemaDp();
        schemaDp.id = id;
        schemaDp.code = code;
        schemaDp.type = type;
        schemaDp.readOnly = Boolean.TRUE;
        return schemaDp;
    }
}
