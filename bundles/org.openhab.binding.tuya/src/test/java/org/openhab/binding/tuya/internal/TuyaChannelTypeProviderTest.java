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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.BINDING_ID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link TuyaChannelTypeProviderTest} verifies that the channel type provider
 * only sets a unit hint when the accepted item type is a {@code Number:<Dimension>}.
 * <p>
 * Previously, an unparseable or dimensionless unit on a {@code value}-type schema
 * produced an item type of {@code Number} while still calling
 * {@code withUnitHint(...)}, which made {@code ChannelType} throw
 * {@code IllegalArgumentException: A unit hint must not be set if the item type is
 * not a number with dimension!} and left the thing in
 * {@code HANDLER_INITIALIZING_ERROR}. See issue #20616.
 *
 * @author Caglar Eker - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class TuyaChannelTypeProviderTest {

    private @Mock @NonNullByDefault({}) ChannelTypeI18nLocalizationService localizationServiceMock;

    private static final String PRODUCT_ID = "testprod";

    @BeforeEach
    public void setUp() {
        // Return the input channel type unchanged so the test focuses on
        // TuyaChannelTypeProvider behavior, not on localization.
        lenient().when(localizationServiceMock.createLocalizedChannelType(any(), any(ChannelType.class), any()))
                .thenAnswer(inv -> inv.getArgument(1));
        TuyaSchemaDB.cache.put(PRODUCT_ID, new ConcurrentHashMap<>());
    }

    @AfterEach
    public void tearDown() {
        TuyaSchemaDB.cache.remove(PRODUCT_ID);
    }

    @Test
    public void parseableDimensionalUnitSetsNumberDimensionAndUnitHint() {
        SchemaDp dp = valueDp("°C");
        putDp("temp", dp);

        ChannelType ct = getChannelType("temp");

        assertNotNull(ct);
        assertEquals("Number:Temperature", ct.getItemType());
        assertEquals("°C", ct.getUnitHint());
    }

    @Test
    public void unparseableUnitAbortsChannelCreation() {
        // A unit string openHAB can't parse used to trigger
        // "A unit hint must not be set if the item type is not a number with dimension!"
        SchemaDp dp = valueDp("bogusunit");
        putDp("garbage", dp);

        ChannelType ct = getChannelType("garbage");

        assertNull(ct);
    }

    @Test
    public void boolSchemaProducesSwitchWithoutUnitHint() {
        // Defensive check: even if unit is somehow populated, non-numeric item
        // types must never receive a unit hint.
        SchemaDp dp = new SchemaDp();
        dp.type = "bool";
        dp.label = "Power";
        dp.unit = "";
        putDp("power", dp);

        ChannelType ct = getChannelType("power");

        assertNotNull(ct);
        assertEquals("Switch", ct.getItemType());
        assertNull(ct.getUnitHint());
    }

    private SchemaDp valueDp(String unit) {
        SchemaDp dp = new SchemaDp();
        dp.type = "value";
        dp.label = "Test";
        dp.unit = unit;
        return dp;
    }

    private void putDp(String channelTypeId, SchemaDp dp) {
        Map<String, SchemaDp> schema = TuyaSchemaDB.cache.get(PRODUCT_ID);
        assertNotNull(schema);
        schema.put(channelTypeId, dp);
    }

    private @Nullable ChannelType getChannelType(String channelTypeId) {
        TuyaChannelTypeProvider provider = new TuyaChannelTypeProvider(localizationServiceMock);
        ChannelTypeUID uid = new ChannelTypeUID(BINDING_ID, PRODUCT_ID + "_" + channelTypeId);
        return provider.getChannelType(uid, null);
    }
}
