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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.SchemaReloadException;
import org.openhab.binding.tuya.internal.TuyaSchemaDB;
import org.openhab.binding.tuya.internal.TuyaSchemaService;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.console.Console;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
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
    private static final ThingUID OTHER_DEVICE_UID = new ThingUID(THING_TYPE_TUYA_DEVICE, "consoletestother");
    private static final ThingUID PROJECT_UID = new ThingUID(THING_TYPE_PROJECT, "cloud");

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistryMock;
    private @Mock @NonNullByDefault({}) ThingManager thingManagerMock;
    private @Mock @NonNullByDefault({}) TuyaSchemaService schemaServiceMock;
    private @Mock @NonNullByDefault({}) Console consoleMock;
    private @Mock @NonNullByDefault({}) Thing deviceThingMock;
    private @Mock @NonNullByDefault({}) Thing otherDeviceThingMock;
    private @Mock @NonNullByDefault({}) Thing projectThingMock;

    private @NonNullByDefault({}) TuyaCommandExtension extension;

    @BeforeEach
    public void setUp() {
        extension = new TuyaCommandExtension(thingRegistryMock, thingManagerMock, schemaServiceMock);

        lenient().when(deviceThingMock.getUID()).thenReturn(DEVICE_UID);
        lenient().when(deviceThingMock.getThingTypeUID()).thenReturn(THING_TYPE_TUYA_DEVICE);
        lenient().when(deviceThingMock.getConfiguration())
                .thenReturn(new Configuration(Map.of(CONFIG_PRODUCT_ID, PRODUCT_ID, CONFIG_DEVICE_ID, DEVICE_ID)));
        lenient().when(otherDeviceThingMock.getUID()).thenReturn(OTHER_DEVICE_UID);
        lenient().when(otherDeviceThingMock.getThingTypeUID()).thenReturn(THING_TYPE_TUYA_DEVICE);
        lenient().when(otherDeviceThingMock.getConfiguration()).thenReturn(
                new Configuration(Map.of(CONFIG_PRODUCT_ID, PRODUCT_ID, CONFIG_DEVICE_ID, "consoletestother")));
        lenient().when(projectThingMock.getUID()).thenReturn(PROJECT_UID);
        lenient().when(projectThingMock.getThingTypeUID()).thenReturn(THING_TYPE_PROJECT);
        lenient().when(thingRegistryMock.get(DEVICE_UID)).thenReturn(deviceThingMock);
        lenient().when(thingRegistryMock.get(PROJECT_UID)).thenReturn(projectThingMock);
        lenient().when(thingRegistryMock.getAll())
                .thenReturn(List.of(projectThingMock, deviceThingMock, otherDeviceThingMock));
        lenient().when(thingManagerMock.isEnabled(DEVICE_UID)).thenReturn(true);
        lenient().when(thingManagerMock.isEnabled(OTHER_DEVICE_UID)).thenReturn(true);

        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @AfterEach
    public void tearDown() {
        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @Test
    public void reloadReinitializesAllThingsOfTheProduct() throws SchemaReloadException {
        when(schemaServiceMock.reloadSchema(PRODUCT_ID, DEVICE_ID)).thenReturn(List.of(schemaDp(22, "fault", "enum")));

        extension.execute(new String[] { "reload", DEVICE_UID.getAsString() }, consoleMock);

        InOrder inOrder = inOrder(thingManagerMock);
        inOrder.verify(thingManagerMock).setEnabled(DEVICE_UID, false);
        inOrder.verify(thingManagerMock).setEnabled(DEVICE_UID, true);
        inOrder.verify(thingManagerMock).setEnabled(OTHER_DEVICE_UID, false);
        inOrder.verify(thingManagerMock).setEnabled(OTHER_DEVICE_UID, true);
    }

    @Test
    public void reloadDoesNotReinitializeWhenReloadFails() throws SchemaReloadException {
        when(schemaServiceMock.reloadSchema(PRODUCT_ID, DEVICE_ID))
                .thenThrow(new SchemaReloadException("no Tuya cloud project is connected"));

        extension.execute(new String[] { "reload", DEVICE_UID.getAsString() }, consoleMock);

        verify(thingManagerMock, never()).setEnabled(any(), anyBoolean());
        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(consoleMock, atLeastOnce()).println(lines.capture());
        assertTrue(lines.getAllValues().stream().anyMatch(line -> line.contains("no Tuya cloud project is connected")));
    }

    @Test
    public void reloadDoesNotEnableDisabledThing() throws SchemaReloadException {
        when(thingManagerMock.isEnabled(DEVICE_UID)).thenReturn(false);
        when(schemaServiceMock.reloadSchema(PRODUCT_ID, DEVICE_ID)).thenReturn(List.of(schemaDp(22, "fault", "enum")));

        extension.execute(new String[] { "reload", DEVICE_UID.getAsString() }, consoleMock);

        verify(thingManagerMock, never()).setEnabled(DEVICE_UID, true);
        verify(thingManagerMock, never()).setEnabled(DEVICE_UID, false);
        verify(thingManagerMock).setEnabled(OTHER_DEVICE_UID, true);
    }

    @Test
    public void reloadRejectsThingsThatAreNoTuyaDevices() throws SchemaReloadException {
        extension.execute(new String[] { "reload", PROJECT_UID.getAsString() }, consoleMock);

        verify(schemaServiceMock, never()).reloadSchema(anyString(), anyString());
        verify(thingManagerMock, never()).setEnabled(any(), anyBoolean());
    }

    @Test
    public void schemaPrintsStoredDatapoints() {
        Map<String, SchemaDp> schema = new LinkedHashMap<>();
        schema.put("fault", schemaDp(22, "fault", "bitmap"));
        TuyaSchemaDB.cache.put(PRODUCT_ID, schema);

        extension.execute(new String[] { "schema", PRODUCT_ID }, consoleMock);

        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(consoleMock, atLeastOnce()).println(lines.capture());
        assertTrue(lines.getAllValues().stream().anyMatch(line -> line.contains("fault") && line.contains("bitmap")));
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
