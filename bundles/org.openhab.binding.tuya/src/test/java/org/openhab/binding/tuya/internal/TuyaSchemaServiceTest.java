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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_PRODUCT_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_PROJECT;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.cloud.TuyaOpenAPI;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;

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
    private static final ThingUID PROJECT_UID = new ThingUID(THING_TYPE_PROJECT, "first");
    private static final ThingUID OTHER_PROJECT_UID = new ThingUID(THING_TYPE_PROJECT, "second");
    private static final ThingUID DEVICE_UID = new ThingUID(THING_TYPE_TUYA_DEVICE, DEVICE_ID);
    private static final ThingUID DISABLED_DEVICE_UID = new ThingUID(THING_TYPE_TUYA_DEVICE, "disabled");
    private static final ThingUID OTHER_DEVICE_UID = new ThingUID(THING_TYPE_TUYA_DEVICE, "otherproduct");

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistryMock;
    private @Mock @NonNullByDefault({}) ThingManager thingManagerMock;
    private @Mock @NonNullByDefault({}) TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProviderMock;
    private @Mock @NonNullByDefault({}) TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProviderMock;
    private @Mock @NonNullByDefault({}) Thing projectThingMock;
    private @Mock @NonNullByDefault({}) ProjectHandler projectHandlerMock;
    private @Mock @NonNullByDefault({}) TuyaOpenAPI apiMock;
    private @Mock @NonNullByDefault({}) Thing otherProjectThingMock;
    private @Mock @NonNullByDefault({}) ProjectHandler otherProjectHandlerMock;
    private @Mock @NonNullByDefault({}) TuyaOpenAPI otherApiMock;

    private @NonNullByDefault({}) TuyaSchemaService schemaService;

    @BeforeEach
    public void setUp() {
        schemaService = new TuyaSchemaService(thingRegistryMock, thingManagerMock,
                dynamicCommandDescriptionProviderMock, dynamicStateDescriptionProviderMock);

        lenient().when(projectThingMock.getUID()).thenReturn(PROJECT_UID);
        lenient().when(projectThingMock.getThingTypeUID()).thenReturn(THING_TYPE_PROJECT);
        lenient().when(projectThingMock.getHandler()).thenReturn(projectHandlerMock);
        lenient().when(projectHandlerMock.getThing()).thenReturn(projectThingMock);
        lenient().when(projectHandlerMock.getApi()).thenReturn(apiMock);
        lenient().when(apiMock.isConnected()).thenReturn(true);
        lenient().when(otherProjectThingMock.getUID()).thenReturn(OTHER_PROJECT_UID);
        lenient().when(otherProjectThingMock.getThingTypeUID()).thenReturn(THING_TYPE_PROJECT);
        lenient().when(otherProjectThingMock.getHandler()).thenReturn(otherProjectHandlerMock);
        lenient().when(otherProjectHandlerMock.getThing()).thenReturn(otherProjectThingMock);
        lenient().when(otherProjectHandlerMock.getApi()).thenReturn(otherApiMock);
        lenient().when(otherApiMock.isConnected()).thenReturn(true);
        lenient().when(thingRegistryMock.getAll()).thenReturn(List.of(projectThingMock));
    }

    @AfterEach
    public void tearDown() {
        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @Test
    public void reloadReplacesStoredSchema() throws Exception {
        storeSchema(schemaDp(22, "fault", "raw"));
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.completedFuture(cloudSchema()));

        List<SchemaDp> schemaDps = schemaService.reloadSchema(PRODUCT_ID, DEVICE_ID).get();

        assertEquals(3, schemaDps.size());
        Map<String, SchemaDp> schema = TuyaSchemaDB.get(PRODUCT_ID);
        assertNotNull(schema);
        assertEquals(List.of("switch", "switch_1", "fault"), List.copyOf(schema.keySet()));
        SchemaDp fault = schema.get("fault");
        assertNotNull(fault);
        assertEquals("enum", fault.type);
        assertEquals(List.of("E1", "E2"), fault.range);
        assertEquals(Boolean.TRUE, fault.readOnly);
        SchemaDp power = schema.get("switch");
        assertNotNull(power);
        assertEquals("bool", power.type);
        assertEquals(Boolean.FALSE, power.readOnly);
    }

    @Test
    public void reloadKeepsStoredSchemaWhenCloudRequestFails() {
        storeSchema(schemaDp(22, "fault", "raw"));
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("permission denied")));

        SchemaReloadException e = assertReloadFails(PRODUCT_ID, DEVICE_ID);

        assertEquals("retrieving the schema of device '" + DEVICE_ID + "' from project '" + PROJECT_UID
                + "' failed: permission denied", e.getMessage());
        SchemaDp fault = TuyaSchemaDB.get(PRODUCT_ID, "fault");
        assertNotNull(fault);
        assertEquals("raw", fault.type);
    }

    @Test
    public void reloadTriesEveryConnectedProject() throws Exception {
        when(thingRegistryMock.getAll()).thenReturn(List.of(projectThingMock, otherProjectThingMock));
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.completedFuture(new DeviceSchema()));
        when(otherProjectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.completedFuture(cloudSchema()));

        List<SchemaDp> schemaDps = schemaService.reloadSchema(PRODUCT_ID, DEVICE_ID).get();

        assertEquals(3, schemaDps.size());
        assertNotNull(TuyaSchemaDB.get(PRODUCT_ID, "fault"));
    }

    @Test
    public void reloadReportsFailureWhenNoProjectKnowsTheDevice() {
        when(thingRegistryMock.getAll()).thenReturn(List.of(projectThingMock, otherProjectThingMock));
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("permission denied")));
        when(otherProjectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("device not found")));

        SchemaReloadException e = assertReloadFails(PRODUCT_ID, DEVICE_ID);

        assertEquals("retrieving the schema of device '" + DEVICE_ID + "' from project '" + OTHER_PROJECT_UID
                + "' failed: device not found", e.getMessage());
        assertNull(TuyaSchemaDB.get(PRODUCT_ID));
    }

    @Test
    public void reloadKeepsStoredSchemaWhenCloudReportsNoDataPoints() {
        storeSchema(schemaDp(22, "fault", "raw"));
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID))
                .thenReturn(CompletableFuture.completedFuture(new DeviceSchema()));

        SchemaReloadException e = assertReloadFails(PRODUCT_ID, DEVICE_ID);

        assertEquals("retrieving the schema of device '" + DEVICE_ID + "' from project '" + PROJECT_UID
                + "' failed: no data points", e.getMessage());
        SchemaDp fault = TuyaSchemaDB.get(PRODUCT_ID, "fault");
        assertNotNull(fault);
        assertEquals("raw", fault.type);
    }

    @Test
    public void reloadReportsUnreadableSpecification() {
        DeviceSchema.Description broken = new DeviceSchema.Description();
        broken.code = "fault";
        broken.dp_id = 22;
        broken.type = "Enum";
        broken.values = "{\"range\":[";
        DeviceSchema schema = new DeviceSchema();
        schema.status = List.of(broken);
        when(projectHandlerMock.getDeviceSchema(DEVICE_ID)).thenReturn(CompletableFuture.completedFuture(schema));

        SchemaReloadException e = assertReloadFails(PRODUCT_ID, DEVICE_ID);

        assertNotNull(e.getMessage());
        assertNull(TuyaSchemaDB.get(PRODUCT_ID));
    }

    @Test
    public void reloadRequiresConnectedProject() {
        when(apiMock.isConnected()).thenReturn(false);

        assertReloadFails(PRODUCT_ID, DEVICE_ID);

        assertNull(TuyaSchemaDB.get(PRODUCT_ID));
        verify(projectHandlerMock, never()).getDeviceSchema(anyString());
    }

    @Test
    public void reloadRequiresProductAndDeviceId() {
        assertReloadFails("", DEVICE_ID);
        assertReloadFails(PRODUCT_ID, "");

        verify(projectHandlerMock, never()).getDeviceSchema(anyString());
    }

    @Test
    public void reinitializeThingsCoversEnabledThingsOfTheProductOnly() {
        Thing device = deviceThing(DEVICE_UID, PRODUCT_ID);
        Thing disabledDevice = deviceThing(DISABLED_DEVICE_UID, PRODUCT_ID);
        Thing otherDevice = deviceThing(OTHER_DEVICE_UID, "otherprod");
        when(thingRegistryMock.getAll()).thenReturn(List.of(projectThingMock, device, disabledDevice, otherDevice));
        when(thingManagerMock.isEnabled(DEVICE_UID)).thenReturn(true);
        when(thingManagerMock.isEnabled(DISABLED_DEVICE_UID)).thenReturn(false);

        List<ThingUID> reinitialized = schemaService.reinitializeThings(PRODUCT_ID);

        assertEquals(List.of(DEVICE_UID), reinitialized);
        InOrder inOrder = inOrder(thingManagerMock, dynamicCommandDescriptionProviderMock,
                dynamicStateDescriptionProviderMock);
        inOrder.verify(thingManagerMock).setEnabled(DEVICE_UID, false);
        inOrder.verify(dynamicCommandDescriptionProviderMock).removeCommandDescriptions(DEVICE_UID);
        inOrder.verify(dynamicStateDescriptionProviderMock).removeStateDescriptions(DEVICE_UID);
        inOrder.verify(thingManagerMock).setEnabled(DEVICE_UID, true);
        verify(thingManagerMock, never()).setEnabled(eq(DISABLED_DEVICE_UID), anyBoolean());
        verify(thingManagerMock, never()).setEnabled(eq(OTHER_DEVICE_UID), anyBoolean());
        verify(dynamicCommandDescriptionProviderMock).removeCommandDescriptions(DEVICE_UID);
        verify(dynamicCommandDescriptionProviderMock).removeCommandDescriptions(DISABLED_DEVICE_UID);
        verify(dynamicCommandDescriptionProviderMock, never()).removeCommandDescriptions(OTHER_DEVICE_UID);
        verify(dynamicStateDescriptionProviderMock).removeStateDescriptions(DEVICE_UID);
        verify(dynamicStateDescriptionProviderMock).removeStateDescriptions(DISABLED_DEVICE_UID);
        verify(dynamicStateDescriptionProviderMock, never()).removeStateDescriptions(OTHER_DEVICE_UID);
    }

    private SchemaReloadException assertReloadFails(String productId, String deviceId) {
        ExecutionException e = assertThrows(ExecutionException.class,
                () -> schemaService.reloadSchema(productId, deviceId).get());
        return assertInstanceOf(SchemaReloadException.class, e.getCause());
    }

    private static Thing deviceThing(ThingUID thingUID, String productId) {
        Thing thing = mock(Thing.class);
        lenient().when(thing.getUID()).thenReturn(thingUID);
        lenient().when(thing.getThingTypeUID()).thenReturn(THING_TYPE_TUYA_DEVICE);
        lenient().when(thing.getConfiguration()).thenReturn(new Configuration(Map.of(CONFIG_PRODUCT_ID, productId)));
        return thing;
    }

    private static DeviceSchema cloudSchema() {
        DeviceSchema.Description power = new DeviceSchema.Description();
        power.code = "switch";
        power.dp_id = 1;
        power.type = "Boolean";
        power.values = "{}";

        // Some devices report the same code for two data points, the second one gets an index.
        DeviceSchema.Description power2 = new DeviceSchema.Description();
        power2.code = "switch_v2";
        power2.dp_id = 2;
        power2.type = "Boolean";
        power2.values = "{}";

        DeviceSchema.Description fault = new DeviceSchema.Description();
        fault.code = "fault";
        fault.dp_id = 22;
        fault.type = "Enum";
        fault.values = "{\"range\":[\"E1\",\"E2\"]}";

        DeviceSchema schema = new DeviceSchema();
        schema.functions = List.of(power, power2);
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
