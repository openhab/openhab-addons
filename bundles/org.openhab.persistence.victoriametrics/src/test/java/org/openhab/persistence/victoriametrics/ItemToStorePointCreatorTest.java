/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.persistence.victoriametrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConfiguration.*;

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
import org.openhab.persistence.victoriametrics.internal.ItemTestHelper;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConstants;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsMetadataService;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsPoint;

/**
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault(value = { DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
public class ItemToStorePointCreatorTest {

    private static final Map<String, Object> BASE_CONFIGURATION = Map.of(URL_PARAM, "http://localhost:8428", USER_PARAM,
            "user", PASSWORD_PARAM, "password", MEASUREMENT_PREFIX, "oh_");

    private @Mock UnitProvider unitProviderMock;
    private @Mock ItemRegistry itemRegistryMock;
    private @Mock MetadataRegistry metadataRegistry;
    private VictoriaMetricsPersistenceService instance;

    @BeforeEach
    public void setup() {
        instance = getService(false, false, false);
    }

    @ParameterizedTest
    @MethodSource
    public void convertBasicItem(Number number) throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", number);
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getMetricName(), equalTo("oh_" + item.getName()));
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
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), "aliasName").get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getMetricName(), is("oh_alias_name"));
    }

    @Test
    public void shouldStoreCategoryTagIfProvidedAndConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        item.setCategory("categoryValue");

        instance = getService(true, false, false);
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(VictoriaMetricsConstants.TAG_CATEGORY_NAME, "categoryValue"));

        instance = getService(false, false, false);
        point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), not(hasKey(VictoriaMetricsConstants.TAG_CATEGORY_NAME)));
    }

    @Test
    public void shouldStoreTypeTagIfProvidedAndConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);

        instance = getService(false, false, true);
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(VictoriaMetricsConstants.TAG_TYPE_NAME, "Number"));

        instance = getService(false, false, false);
        point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), not(hasKey(VictoriaMetricsConstants.TAG_TYPE_NAME)));
    }

    @Test
    public void shouldStoreTypeLabelIfProvidedAndConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        item.setLabel("ItemLabel");

        instance = getService(false, true, false);
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(VictoriaMetricsConstants.TAG_LABEL_NAME, "ItemLabel"));

        instance = getService(false, false, false);
        point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), not(hasKey(VictoriaMetricsConstants.TAG_LABEL_NAME)));
    }

    @Test
    public void shouldStoreMetadataAsTagsIfProvided() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, item.getName());

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

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
        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, item.getName());

        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMetricName(), equalTo("oh_" + item.getName()));

        point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMetricName(), equalTo("oh_" + item.getName()));
        assertThat(point.getTags(), hasEntry("item", item.getName()));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "measurementName", Map.of("key1", "val1", "key2", "val2")));

        point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMetricName(), equalTo("oh_measurement_name"));
        assertThat(point.getTags(), hasEntry("item", item.getName()));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMetricName(), equalTo("oh_" + item.getName()));
        assertThat(point.getTags(), hasEntry("item", item.getName()));
    }

    private VictoriaMetricsPersistenceService getService(boolean category, boolean label, boolean typeTag) {
        VictoriaMetricsMetadataService metadataService = new VictoriaMetricsMetadataService(metadataRegistry);
        Map<String, Object> configuration = new HashMap<>(BASE_CONFIGURATION);
        configuration.put(ADD_CATEGORY_TAG_PARAM, category);
        configuration.put(ADD_LABEL_TAG_PARAM, label);
        configuration.put(ADD_TYPE_TAG_PARAM, typeTag);
        VictoriaMetricsPersistenceService instance = new VictoriaMetricsPersistenceService(itemRegistryMock,
                metadataService, configuration);
        instance.setItemFactory(new CoreItemFactory(unitProviderMock));
        return instance;
    }
}
