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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link TuyaSchemaDBTest} verifies updating and removing stored schemas.
 *
 * @author Carlo Dischler - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class TuyaSchemaDBTest {
    private static final String PRODUCT_ID = "schemadbtestprod";

    private @Mock @NonNullByDefault({}) StorageService storageServiceMock;
    private @Mock @NonNullByDefault({}) Storage<String> storageMock;

    @AfterEach
    public void tearDown() {
        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @Test
    public void parsedUnitIsNeitherPersistedNorRequiredWhenLoading() {
        Gson gson = new Gson();
        Type storageType = TypeToken.getParameterized(List.class, SchemaDp.class).getType();

        SchemaDp schemaDp = schemaDp(1, "temperature", "value");
        schemaDp.unit = "°C";
        schemaDp.parsedUnit = SIUnits.CELSIUS;
        String json = gson.toJson(List.of(schemaDp));
        assertFalse(json.contains("parsedUnit"));

        // Entries written before parsedUnit was excluded contain a serialized Unit object.
        List<SchemaDp> loaded = gson.fromJson(
                "[{\"id\":1,\"code\":\"temperature\",\"type\":\"value\",\"unit\":\"°C\",\"parsedUnit\":{\"symbol\":\"°C\"}}]",
                storageType);
        assertNotNull(loaded);
        assertEquals(1, loaded.size());
        assertEquals("temperature", loaded.get(0).code);
        assertNull(loaded.get(0).parsedUnit);
    }

    @Test
    public void putReplacesExistingSchema() {
        TuyaSchemaDB.put(PRODUCT_ID, List.of(schemaDp(22, "fault", "raw")));
        TuyaSchemaDB.put(PRODUCT_ID, List.of(schemaDp(22, "fault", "bitmap"), schemaDp(1, "switch", "bool")));

        Map<String, SchemaDp> schema = TuyaSchemaDB.get(PRODUCT_ID);
        assertNotNull(schema);
        assertEquals(List.of("switch", "fault"), List.copyOf(schema.keySet()));
        SchemaDp fault = schema.get("fault");
        assertNotNull(fault);
        assertEquals("bitmap", fault.type);
    }

    @Test
    public void putIgnoresEmptySchema() {
        when(storageServiceMock.<String> getStorage(anyString())).thenReturn(storageMock);
        TuyaSchemaDB.setStorage(storageServiceMock, "test");

        TuyaSchemaDB.put(PRODUCT_ID, List.of(schemaDp(22, "fault", "bitmap")));
        TuyaSchemaDB.put(PRODUCT_ID, List.of());

        assertTrue(TuyaSchemaDB.contains(PRODUCT_ID));
        // the stored schema must not be overwritten with an empty one either
        verify(storageMock, times(1)).put(eq(PRODUCT_ID), anyString());
    }

    @Test
    public void removeDropsSchema() {
        TuyaSchemaDB.put(PRODUCT_ID, List.of(schemaDp(22, "fault", "bitmap")));
        TuyaSchemaDB.remove(PRODUCT_ID);

        assertFalse(TuyaSchemaDB.contains(PRODUCT_ID));
    }

    private static SchemaDp schemaDp(int id, String code, String type) {
        SchemaDp schemaDp = new SchemaDp();
        schemaDp.id = id;
        schemaDp.code = code;
        schemaDp.type = type;
        return schemaDp;
    }
}
