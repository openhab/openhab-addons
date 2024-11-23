/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.items.NumberItem;
import org.openhab.persistence.influxdb.internal.InfluxDBConstants;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBVersion;
import org.openhab.persistence.influxdb.internal.InfluxPoint;
import org.openhab.persistence.influxdb.internal.ItemTestHelper;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault(value = { DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
public class ItemToStorePointCreatorTest {

    private static final Map<String, Object> BASE_CONFIGURATION = Map.of( //
            URL_PARAM, "http://localhost:8086", //
            VERSION_PARAM, InfluxDBVersion.V1.name(), //
            USER_PARAM, "user", PASSWORD_PARAM, "password", //
            DATABASE_PARAM, "openhab", //
            RETENTION_POLICY_PARAM, "default");

    private @Mock UnitProvider unitProviderMock;
    private @Mock ItemRegistry itemRegistryMock;
    private @Mock MetadataRegistry metadataRegistry;
    private InfluxDBPersistenceService instance;

    @BeforeEach
    public void setup() {
        instance = getService(false, false, false, false);
    }

    @ParameterizedTest
    @MethodSource
    public void convertBasicItem(Number number) throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", number);
        InfluxPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getMeasurementName(), equalTo(item.getName()));
        assertThat("Must Store item name", point.getTags(), hasEntry("item", item.getName()));
        assertThat(point.getValue(), equalTo(new BigDecimal(number.toString())));
    }

    @SuppressWarnings("unused")
    private static Stream<Number> convertBasicItem() {
        return Stream.of(5, 5.5, 5L);
    }

    @Test
    public void shouldUseAliasAsMeasurementNameIfProvided() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        InfluxPoint point = instance.convert(item, item.getState(), Instant.now(), "aliasName").get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getMeasurementName(), is("aliasName"));
    }

    @Test
    public void shouldStoreCategoryTagIfProvidedAndConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        item.setCategory("categoryValue");

        instance = getService(false, true, false, false);
        InfluxPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(InfluxDBConstants.TAG_CATEGORY_NAME, "categoryValue"));

        instance = getService(false, false, false, false);
        point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), not(hasKey(InfluxDBConstants.TAG_CATEGORY_NAME)));
    }

    @Test
    public void shouldStoreTypeTagIfProvidedAndConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);

        instance = getService(false, false, false, true);
        InfluxPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(InfluxDBConstants.TAG_TYPE_NAME, "Number"));

        instance = getService(false, false, false, false);
        point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), not(hasKey(InfluxDBConstants.TAG_TYPE_NAME)));
    }

    @Test
    public void shouldStoreTypeLabelIfProvidedAndConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        item.setLabel("ItemLabel");

        instance = getService(false, false, true, false);
        InfluxPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(InfluxDBConstants.TAG_LABEL_NAME, "ItemLabel"));

        instance = getService(false, false, false, false);
        point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), not(hasKey(InfluxDBConstants.TAG_LABEL_NAME)));
    }

    @Test
    public void shouldStoreMetadataAsTagsIfProvided() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        MetadataKey metadataKey = new MetadataKey(InfluxDBPersistenceService.SERVICE_NAME, item.getName());

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        InfluxPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry("key1", "val1"));
        assertThat(point.getTags(), hasEntry("key2", "val2"));
    }

    @Test
    public void shouldUseMeasurementNameFromMetadataIfProvided() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        MetadataKey metadataKey = new MetadataKey(InfluxDBPersistenceService.SERVICE_NAME, item.getName());

        InfluxPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMeasurementName(), equalTo(item.getName()));

        point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMeasurementName(), equalTo(item.getName()));
        assertThat(point.getTags(), hasEntry("item", item.getName()));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "measurementName", Map.of("key1", "val1", "key2", "val2")));

        point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMeasurementName(), equalTo("measurementName"));
        assertThat(point.getTags(), hasEntry("item", item.getName()));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMeasurementName(), equalTo(item.getName()));
        assertThat(point.getTags(), hasEntry("item", item.getName()));
    }

    private InfluxDBPersistenceService getService(boolean replaceUnderscore, boolean category, boolean label,
            boolean typeTag) {
        InfluxDBMetadataService influxDBMetadataService = new InfluxDBMetadataService(metadataRegistry);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(BASE_CONFIGURATION);
        configuration.put(REPLACE_UNDERSCORE_PARAM, replaceUnderscore);
        configuration.put(ADD_CATEGORY_TAG_PARAM, category);
        configuration.put(ADD_LABEL_TAG_PARAM, label);
        configuration.put(ADD_TYPE_TAG_PARAM, typeTag);

        InfluxDBPersistenceService instance = new InfluxDBPersistenceService(itemRegistryMock, influxDBMetadataService,
                configuration);
        instance.setItemFactory(new CoreItemFactory(unitProviderMock));

        return instance;
    }
}
