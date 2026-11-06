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
package org.openhab.persistence.victoriametrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConfiguration.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.persistence.victoriametrics.internal.ItemTestHelper;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConstants;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsMetadataService;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsPoint;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository;

/**
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 * @author Michael Weger - Add metric name, unsupported state, unit tag and string state tests
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault(value = { DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
public class ItemToStorePointCreatorTest {

    private static final Map<String, Object> BASE_CONFIGURATION = Map.of( //
            URL_PARAM, "http://localhost:8428");

    private @Mock UnitProvider unitProviderMock;
    private @Mock ItemRegistry itemRegistryMock;
    private @Mock MetadataRegistry metadataRegistry;
    private VictoriaMetricsPersistenceService instance;

    @BeforeEach
    public void setup() {
        instance = getService(false, false, false, false, "");
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

        assertThat(point.getMetricName(), equalTo(item.getName()));
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

        assertThat(point.getMetricName(), is("aliasName"));
        assertThat(point.getTags(), hasEntry("item", "aliasName"));
    }

    @Test
    public void shouldAddMetricPrefixIfConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);

        instance = getService(false, false, false, false, "openhab_");
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getMetricName(), is("openhab_myitem"));
        assertThat(point.getTags(), hasEntry("item", "myitem"));
    }

    @Test
    public void shouldKeepItemNamesUnchanged() throws ExecutionException, InterruptedException {
        // item names are valid metric names, so similar names must not be converted to the same metric name
        for (String itemName : new String[] { "WashingMachine_Door", "WashingMachineDoor", "my_lamp", "MyLamp" }) {
            NumberItem item = ItemTestHelper.createNumberItem(itemName, 5);
            VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

            if (point == null) {
                Assertions.fail("'point' is null");
                return;
            }

            assertThat(point.getMetricName(), is(itemName));
            assertThat(point.getTags(), hasEntry("item", itemName));
        }
    }

    @Test
    public void shouldSanitizeAliasButKeepItemTag() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), "outside.temp").get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getMetricName(), is("outside_temp"));
        assertThat(point.getTags(), hasEntry("item", "outside.temp"));
    }

    @Test
    public void shouldNotConvertStringStatesWithoutOptIn() throws ExecutionException, InterruptedException {
        StringItem stringItem = new StringItem("myString");
        assertThat(instance.convert(stringItem, new StringType("text"), Instant.now(), null).get(), nullValue());

        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, "myString");
        when(metadataRegistry.get(metadataKey)).thenReturn(
                new Metadata(metadataKey, "", Map.of(VictoriaMetricsConstants.METADATA_STRING_STATES, "false")));
        assertThat(instance.convert(stringItem, new StringType("text"), Instant.now(), null).get(), nullValue());
    }

    @ParameterizedTest
    @MethodSource
    public void shouldConvertStringStatesWithOptIn(Object optIn) throws ExecutionException, InterruptedException {
        StringItem stringItem = new StringItem("myString");
        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, "myString");
        when(metadataRegistry.get(metadataKey)).thenReturn(new Metadata(metadataKey, "",
                Map.of(VictoriaMetricsConstants.METADATA_STRING_STATES, optIn, "room", "kitchen")));

        VictoriaMetricsPoint point = instance.convert(stringItem, new StringType("HEATING"), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getStringState(), is("HEATING"));
        assertThat(point.getTags(), hasEntry("item", "myString"));
        assertThat(point.getTags(), hasEntry("room", "kitchen"));
        assertThat(point.getTags(), not(hasKey(VictoriaMetricsConstants.METADATA_STRING_STATES)));
    }

    @SuppressWarnings("unused")
    private static Stream<Object> shouldConvertStringStatesWithOptIn() {
        // Boolean when configured in the UI, String in .items files
        return Stream.of(true, "true");
    }

    @Test
    public void shouldConvertEmptyStringState() throws ExecutionException, InterruptedException {
        StringItem stringItem = new StringItem("myString");
        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, "myString");
        when(metadataRegistry.get(metadataKey)).thenReturn(
                new Metadata(metadataKey, "", Map.of(VictoriaMetricsConstants.METADATA_STRING_STATES, true)));

        VictoriaMetricsPoint point = instance.convert(stringItem, new StringType(""), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getStringState(), is(""));
        assertThat(point.toPrometheusFormat(), Matchers.startsWith("myString{item=\"myString\"} 1 "));
    }

    @Test
    public void shouldNotConvertTooLongStringStates() throws ExecutionException, InterruptedException {
        StringItem stringItem = new StringItem("myString");
        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, "myString");
        when(metadataRegistry.get(metadataKey)).thenReturn(
                new Metadata(metadataKey, "", Map.of(VictoriaMetricsConstants.METADATA_STRING_STATES, true)));

        assertThat(instance.convert(stringItem, new StringType("x".repeat(4097)), Instant.now(), null).get(),
                nullValue());
        // the limit is in bytes, not characters
        assertThat(instance.convert(stringItem, new StringType("ä".repeat(2049)), Instant.now(), null).get(),
                nullValue());
        assertThat(instance.convert(stringItem, new StringType("x".repeat(4096)), Instant.now(), null).get(),
                notNullValue());
    }

    @Test
    public void shouldConvertColorToComponents() throws ExecutionException, InterruptedException {
        ColorItem colorItem = new ColorItem("myColor");
        VictoriaMetricsPoint point = instance.convert(colorItem, new HSBType("120,100,50"), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getMetricName(), is("myColor"));
        assertThat(point.getTags(), hasEntry("item", "myColor"));
        assertThat(point.getComponents(), is(Map.of("hue", new BigDecimal("120"), "saturation", new BigDecimal("100"),
                "brightness", new BigDecimal("50"))));
    }

    @Test
    public void shouldStoreDimmerOnAsPercent() throws ExecutionException, InterruptedException {
        DimmerItem dimmerItem = new DimmerItem("myDimmer");
        dimmerItem.setState(OnOffType.ON);
        VictoriaMetricsPoint point = instance.convert(dimmerItem, dimmerItem.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getValue(), is(new BigDecimal("100")));
    }

    @Test
    public void shouldStoreCategoryTagIfProvidedAndConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        item.setCategory("categoryValue");

        instance = getService(true, false, false, false, "");
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(VictoriaMetricsConstants.TAG_CATEGORY_NAME, "categoryValue"));

        instance = getService(false, false, false, false, "");
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

        instance = getService(false, false, true, false, "");
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(VictoriaMetricsConstants.TAG_TYPE_NAME, "Number"));

        instance = getService(false, false, false, false, "");
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

        instance = getService(false, true, false, false, "");
        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(VictoriaMetricsConstants.TAG_LABEL_NAME, "ItemLabel"));

        instance = getService(false, false, false, false, "");
        point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), not(hasKey(VictoriaMetricsConstants.TAG_LABEL_NAME)));
    }

    @Test
    public void shouldStoreUnitTagIfProvidedAndConfigured() throws ExecutionException, InterruptedException {
        NumberItem item = new NumberItem("Number:Temperature", "myitem", unitProviderMock);
        QuantityType<?> state = new QuantityType<>(21.5, SIUnits.CELSIUS);

        instance = getService(false, false, false, true, "");
        VictoriaMetricsPoint point = instance.convert(item, state, Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry(VictoriaMetricsConstants.TAG_UNIT_NAME, "°C"));
        assertThat(point.getValue(), equalTo(new BigDecimal("21.5")));

        instance = getService(false, false, false, false, "");
        point = instance.convert(item, state, Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), not(hasKey(VictoriaMetricsConstants.TAG_UNIT_NAME)));
    }

    @Test
    public void shouldStoreMetadataAsTagsIfProvided() throws ExecutionException, InterruptedException {
        NumberItem item = ItemTestHelper.createNumberItem("myitem", 5);
        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, item.getName());

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key-2", "val2")));

        VictoriaMetricsPoint point = instance.convert(item, item.getState(), Instant.now(), null).get();

        if (point == null) {
            Assertions.fail("'point' is null");
            return;
        }

        assertThat(point.getTags(), hasEntry("key1", "val1"));
        assertThat(point.getTags(), hasEntry("key_2", "val2"));
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
        assertThat(point.getMetricName(), equalTo(item.getName()));
        assertThat(point.getTags(), hasEntry("item", item.getName()));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "measurementName", Map.of("key1", "val1", "key2", "val2")));

        point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMetricName(), equalTo("measurementName"));
        assertThat(point.getTags(), hasEntry("item", item.getName()));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        point = instance.convert(item, item.getState(), Instant.now(), null).get();
        if (point == null) {
            Assertions.fail();
            return;
        }
        assertThat(point.getMetricName(), equalTo(item.getName()));
        assertThat(point.getTags(), hasEntry("item", item.getName()));
    }

    private VictoriaMetricsPersistenceService getService(boolean category, boolean label, boolean typeTag,
            boolean unitTag, String metricPrefix) {
        VictoriaMetricsMetadataService metadataService = new VictoriaMetricsMetadataService(metadataRegistry);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(BASE_CONFIGURATION);
        configuration.put(ADD_CATEGORY_TAG_PARAM, category);
        configuration.put(ADD_LABEL_TAG_PARAM, label);
        configuration.put(ADD_TYPE_TAG_PARAM, typeTag);
        configuration.put(ADD_UNIT_TAG_PARAM, unitTag);
        configuration.put(METRIC_PREFIX_PARAM, metricPrefix);

        VictoriaMetricsPersistenceService instance = new VictoriaMetricsPersistenceService(itemRegistryMock,
                metadataService, mock(HttpClientFactory.class), configuration) {
            @Override
            protected VictoriaMetricsRepository createVictoriaMetricsRepository() {
                return mock(VictoriaMetricsRepository.class);
            }
        };

        return instance;
    }
}
