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
package org.openhab.binding.tuya.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.cloud.TuyaOpenAPI;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;

/**
 * The {@link TuyaSchemaServiceTest} verifies reloading a device schema from the cloud.
 *
 * @author Carlo Dischler - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class TuyaSchemaServiceTest {
    private static final String PRODUCT_ID = "schemaservicetestprod";
    private static final String DEVICE_ID = "schemaservicetestdevice";

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistryMock;
    private @Mock @NonNullByDefault({}) TuyaChannelTypeProvider channelTypeProviderMock;
    private @Mock @NonNullByDefault({}) Thing projectThingMock;
    private @Mock @NonNullByDefault({}) ProjectHandler projectHandlerMock;
    private @Mock @NonNullByDefault({}) TuyaOpenAPI apiMock;

    private @NonNullByDefault({}) TuyaSchemaService schemaService;

    @BeforeEach
    public void setUp() {
        schemaService = new TuyaSchemaService(thingRegistryMock, channelTypeProviderMock);

        lenient().when(projectThingMock.getHandler()).thenReturn(projectHandlerMock);
        lenient().when(projectHandlerMock.getApi()).thenReturn(apiMock);
        lenient().when(apiMock.isConnected()).thenReturn(true);
        lenient().when(thingRegistryMock.getAll()).thenReturn(List.of(projectThingMock));

        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @AfterEach
    public void tearDown() {
        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @Test
    public void reloadReplacesStoredSchemaAndInvalidatesChannelTypes() throws SchemaReloadException {
        storeSchema(schemaDp(22, "fault", "raw"));
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.completedFuture(cloudSchema()));

        List<SchemaDp> schemaDps = schemaService.reloadSchema(PRODUCT_ID, DEVICE_ID);

        assertEquals(2, schemaDps.size());
        SchemaDp fault = TuyaSchemaDB.get(PRODUCT_ID, "fault");
        assertNotNull(fault);
        assertEquals("enum", fault.type);
        assertEquals(List.of("E1", "E2"), fault.range);
        assertEquals(Boolean.TRUE, fault.readOnly);
        SchemaDp power = TuyaSchemaDB.get(PRODUCT_ID, "switch");
        assertNotNull(power);
        assertEquals("bool", power.type);
        assertEquals(Boolean.FALSE, power.readOnly);

        verify(channelTypeProviderMock).removeChannelTypes(PRODUCT_ID);
    }

    @Test
    public void reloadKeepsStoredSchemaWhenCloudRequestFails() {
        storeSchema(schemaDp(22, "fault", "raw"));
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("permission denied")));

        SchemaReloadException e = assertThrows(SchemaReloadException.class,
                () -> schemaService.reloadSchema(PRODUCT_ID, DEVICE_ID));

        assertEquals("retrieving the schema of device '" + DEVICE_ID + "' failed: permission denied", e.getMessage());
        SchemaDp fault = TuyaSchemaDB.get(PRODUCT_ID, "fault");
        assertNotNull(fault);
        assertEquals("raw", fault.type);
        verify(channelTypeProviderMock, never()).removeChannelTypes(anyString());
    }

    @Test
    public void reloadKeepsStoredSchemaWhenCloudReportsNoDatapoints() {
        storeSchema(schemaDp(22, "fault", "raw"));
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.completedFuture(new DeviceSchema()));

        assertThrows(SchemaReloadException.class, () -> schemaService.reloadSchema(PRODUCT_ID, DEVICE_ID));

        SchemaDp fault = TuyaSchemaDB.get(PRODUCT_ID, "fault");
        assertNotNull(fault);
        assertEquals("raw", fault.type);
        verify(channelTypeProviderMock, never()).removeChannelTypes(anyString());
    }

    @Test
    public void reloadRequiresConnectedProject() {
        when(apiMock.isConnected()).thenReturn(false);

        assertThrows(SchemaReloadException.class, () -> schemaService.reloadSchema(PRODUCT_ID, DEVICE_ID));

        assertNull(TuyaSchemaDB.get(PRODUCT_ID));
        verify(projectHandlerMock, never()).getDeviceSchema(anyString());
    }

    @Test
    public void reloadRequiresProductAndDeviceId() {
        assertThrows(SchemaReloadException.class, () -> schemaService.reloadSchema("", DEVICE_ID));
        assertThrows(SchemaReloadException.class, () -> schemaService.reloadSchema(PRODUCT_ID, ""));

        verify(projectHandlerMock, never()).getDeviceSchema(anyString());
    }

    private static DeviceSchema cloudSchema() {
        DeviceSchema.Description power = new DeviceSchema.Description();
        power.code = "switch";
        power.dp_id = 1;
        power.type = "Boolean";
        power.values = "{}";

        DeviceSchema.Description fault = new DeviceSchema.Description();
        fault.code = "fault";
        fault.dp_id = 22;
        fault.type = "Enum";
        fault.values = "{\"range\":[\"E1\",\"E2\"]}";

        DeviceSchema schema = new DeviceSchema();
        schema.functions = List.of(power);
        schema.status = List.of(power, fault);
        return schema;
    }

    private static SchemaDp schemaDp(int id, String code, String type) {
        SchemaDp schemaDp = new SchemaDp();
        schemaDp.id = id;
        schemaDp.code = code;
        schemaDp.type = type;
        schemaDp.readOnly = Boolean.TRUE;
        return schemaDp;
    }

    private static void storeSchema(SchemaDp... schemaDps) {
        Map<String, SchemaDp> schema = new LinkedHashMap<>();
        for (SchemaDp schemaDp : schemaDps) {
            schema.put(schemaDp.code, schemaDp);
        }
        TuyaSchemaDB.cache.put(PRODUCT_ID, schema);
    }
}
