/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.persistence.influxdb.InfluxDBPersistenceService;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null") // In case of any NPE it will cause test fail that it's the expected result
@NonNullByDefault(value = { DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
public class ItemToStorePointCreatorTest {

    private @Mock InfluxDBConfiguration influxDBConfiguration;
    private @Mock MetadataRegistry metadataRegistry;
    private ItemToStorePointCreator instance;

    @BeforeEach
    public void before() {
        when(influxDBConfiguration.isAddCategoryTag()).thenReturn(false);
        when(influxDBConfiguration.isAddLabelTag()).thenReturn(false);
        when(influxDBConfiguration.isAddTypeTag()).thenReturn(false);
        when(influxDBConfiguration.isReplaceUnderscore()).thenReturn(false);

        instance = new ItemToStorePointCreator(influxDBConfiguration, metadataRegistry);
    }

    @AfterEach
    public void after() {
        instance = null;
        influxDBConfiguration = null;
        metadataRegistry = null;
    }

    @ParameterizedTest
    @MethodSource
    public void convertBasicItem(Number number) {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", number);
        InfluxPoint point = instance.convert(item, null);

        assertThat(point.getMeasurementName(), equalTo(item.getName()));
        assertThat("Must Store item name", point.getTags(), hasEntry("item", item.getName()));
        assertThat(point.getValue(), equalTo(new BigDecimal(number.toString())));
    }

    private static Stream<Number> convertBasicItem() {
        return Stream.of(5, 5.5, 5L);
    }

    @Test
    public void shouldUseAliasAsMeasurementNameIfProvided() {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        InfluxPoint point = instance.convert(item, "aliasName");
        assertThat(point.getMeasurementName(), is("aliasName"));
    }

    @Test
    public void shouldStoreCategoryTagIfProvidedAndConfigured() {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        item.setCategory("categoryValue");

        when(influxDBConfiguration.isAddCategoryTag()).thenReturn(true);
        InfluxPoint point = instance.convert(item, null);
        assertThat(point.getTags(), hasEntry(InfluxDBConstants.TAG_CATEGORY_NAME, "categoryValue"));

        when(influxDBConfiguration.isAddCategoryTag()).thenReturn(false);
        point = instance.convert(item, null);
        assertThat(point.getTags(), not(hasKey(InfluxDBConstants.TAG_CATEGORY_NAME)));
    }

    @Test
    public void shouldStoreTypeTagIfProvidedAndConfigured() {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);

        when(influxDBConfiguration.isAddTypeTag()).thenReturn(true);
        InfluxPoint point = instance.convert(item, null);
        assertThat(point.getTags(), hasEntry(InfluxDBConstants.TAG_TYPE_NAME, "Number"));

        when(influxDBConfiguration.isAddTypeTag()).thenReturn(false);
        point = instance.convert(item, null);
        assertThat(point.getTags(), not(hasKey(InfluxDBConstants.TAG_TYPE_NAME)));
    }

    @Test
    public void shouldStoreTypeLabelIfProvidedAndConfigured() {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        item.setLabel("ItemLabel");

        when(influxDBConfiguration.isAddLabelTag()).thenReturn(true);
        InfluxPoint point = instance.convert(item, null);
        assertThat(point.getTags(), hasEntry(InfluxDBConstants.TAG_LABEL_NAME, "ItemLabel"));

        when(influxDBConfiguration.isAddLabelTag()).thenReturn(false);
        point = instance.convert(item, null);
        assertThat(point.getTags(), not(hasKey(InfluxDBConstants.TAG_LABEL_NAME)));
    }

    @Test
    public void shouldStoreMetadataAsTagsIfProvided() {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        MetadataKey metadataKey = new MetadataKey(InfluxDBPersistenceService.SERVICE_NAME, item.getName());

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        InfluxPoint point = instance.convert(item, null);
        assertThat(point.getTags(), hasEntry("key1", "val1"));
        assertThat(point.getTags(), hasEntry("key2", "val2"));
    }

    @Test
    public void shouldUseMeasurementNameFromMetadataIfProvided() {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        MetadataKey metadataKey = new MetadataKey(InfluxDBPersistenceService.SERVICE_NAME, item.getName());

        InfluxPoint point = instance.convert(item, null);
        assertThat(point.getMeasurementName(), equalTo(item.getName()));

        point = instance.convert(item, null);
        assertThat(point.getMeasurementName(), equalTo(item.getName()));
        assertThat(point.getTags(), hasEntry("item", item.getName()));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "measurementName", Map.of("key1", "val1", "key2", "val2")));

        point = instance.convert(item, null);
        assertThat(point.getMeasurementName(), equalTo("measurementName"));
        assertThat(point.getTags(), hasEntry("item", item.getName()));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        point = instance.convert(item, null);
        assertThat(point.getMeasurementName(), equalTo(item.getName()));
        assertThat(point.getTags(), hasEntry("item", item.getName()));
    }
}
