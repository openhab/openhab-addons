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
package org.openhab.persistence.influxdb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.DATABASE_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.PASSWORD_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.RETENTION_POLICY_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.TOKEN_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.URL_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.USER_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.VERSION_PARAM;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository.WriteResult;
import org.openhab.persistence.influxdb.internal.InfluxDBVersion;
import org.openhab.persistence.influxdb.internal.InfluxPoint;
import org.openhab.persistence.influxdb.internal.ItemTestHelper;
import org.openhab.persistence.influxdb.internal.UnexpectedConditionException;

/**
 * @author Joan Pujol Espinar - Initial contribution
 * @author Eugen Freiter - Tests for retried and rejected writes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class InfluxDBPersistenceServiceTest {
    private static final Map<String, Object> VALID_V1_CONFIGURATION = Map.of( //
            URL_PARAM, "http://localhost:8086", //
            VERSION_PARAM, InfluxDBVersion.V1.name(), //
            USER_PARAM, "user", PASSWORD_PARAM, "password", //
            DATABASE_PARAM, "openhab", //
            RETENTION_POLICY_PARAM, "default");

    private static final Map<String, Object> VALID_V2_CONFIGURATION = Map.of( //
            URL_PARAM, "http://localhost:8086", //
            VERSION_PARAM, InfluxDBVersion.V2.name(), //
            TOKEN_PARAM, "sampletoken", //
            DATABASE_PARAM, "openhab", //
            RETENTION_POLICY_PARAM, "default");

    private static final Map<String, Object> INVALID_V1_CONFIGURATION = Map.of(//
            URL_PARAM, "http://localhost:8086", //
            VERSION_PARAM, InfluxDBVersion.V1.name(), //
            USER_PARAM, "user", //
            DATABASE_PARAM, "openhab", //
            RETENTION_POLICY_PARAM, "default");

    private static final Map<String, Object> INVALID_V2_CONFIGURATION = Map.of( //
            URL_PARAM, "http://localhost:8086", //
            VERSION_PARAM, InfluxDBVersion.V2.name(), //
            DATABASE_PARAM, "openhab", //
            RETENTION_POLICY_PARAM, "default");

    private static final Map<String, Object> VALID_V3_CONFIGURATION = Map.of( //
            URL_PARAM, "http://localhost:8181", //
            VERSION_PARAM, InfluxDBVersion.V3.name(), //
            TOKEN_PARAM, "sampletoken", //
            DATABASE_PARAM, "openhab", //
            RETENTION_POLICY_PARAM, "default");

    private static final Map<String, Object> INVALID_V3_CONFIGURATION = Map.of( //
            URL_PARAM, "http://localhost:8181", //
            VERSION_PARAM, InfluxDBVersion.V3.name(), //
            DATABASE_PARAM, "openhab", //
            RETENTION_POLICY_PARAM, "default");

    private static final Map<String, Object> VALID_V3_CONFIGURATION_WITHOUT_RETENTION_POLICY = Map.of( //
            URL_PARAM, "http://localhost:8181", //
            VERSION_PARAM, InfluxDBVersion.V3.name(), //
            TOKEN_PARAM, "sampletoken", //
            DATABASE_PARAM, "openhab", //
            RETENTION_POLICY_PARAM, "");

    private @Mock @NonNullByDefault({}) InfluxDBRepository influxDBRepositoryMock;

    private final InfluxDBMetadataService influxDBMetadataService = new InfluxDBMetadataService(
            mock(MetadataRegistry.class));

    @BeforeEach
    public void before() {
        when(influxDBRepositoryMock.write(any())).thenReturn(WriteResult.SUCCESS);
    }

    @Test
    public void activateWithValidV1ConfigShouldConnectRepository() {
        getService(VALID_V1_CONFIGURATION);
        verify(influxDBRepositoryMock).connect();
    }

    @Test
    public void activateWithValidV2ConfigShouldConnectRepository() {
        getService(VALID_V2_CONFIGURATION);
        verify(influxDBRepositoryMock).connect();
    }

    @Test
    public void activateWithValidV3ConfigShouldConnectRepository() {
        getService(VALID_V3_CONFIGURATION);
        verify(influxDBRepositoryMock).connect();
    }

    @Test
    public void activateWithValidV3ConfigWithoutRetentionPolicyShouldConnectRepository() {
        // InfluxDB 3 databases have no retention-policy concept, so an explicitly empty value must still be valid.
        getService(VALID_V3_CONFIGURATION_WITHOUT_RETENTION_POLICY);
        verify(influxDBRepositoryMock).connect();
    }

    @Test
    public void activateWithInvalidV1ConfigShouldNotConnectRepository() {
        getService(INVALID_V1_CONFIGURATION);
        verify(influxDBRepositoryMock, never()).connect();
    }

    @Test
    public void activateWithInvalidV2ShouldNotConnectRepository() {
        getService(INVALID_V2_CONFIGURATION);
        verify(influxDBRepositoryMock, never()).connect();
    }

    @Test
    public void activateWithInvalidV3ShouldNotConnectRepository() {
        getService(INVALID_V3_CONFIGURATION);
        verify(influxDBRepositoryMock, never()).connect();
    }

    @Test
    public void serviceWithInvalidConfigRejectsAllOperations() {
        InfluxDBPersistenceService instance = getService(INVALID_V2_CONFIGURATION);
        FilterCriteria filter = new FilterCriteria().setItemName("number");

        instance.store(ItemTestHelper.createNumberItem("number", 5));

        assertFalse(instance.query(filter).iterator().hasNext());
        assertFalse(instance.remove(filter));
        assertTrue(instance.getItemInfo().isEmpty());
        instance.deactivate();
        verifyNoInteractions(influxDBRepositoryMock);
    }

    @Test
    public void deactivateShouldDisconnectRepository() {
        InfluxDBPersistenceService instance = getService(VALID_V2_CONFIGURATION);
        instance.deactivate();
        verify(influxDBRepositoryMock).disconnect();
    }

    @Test
    public void storeItemWithConnectedRepository() throws UnexpectedConditionException {
        InfluxDBPersistenceService instance = getService(VALID_V2_CONFIGURATION);
        when(influxDBRepositoryMock.isConnected()).thenReturn(true);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(influxDBRepositoryMock, timeout(5000)).write(any());
    }

    @Test
    public void storeItemWithDisconnectedRepositoryIsIgnored() throws UnexpectedConditionException {
        InfluxDBPersistenceService instance = getService(VALID_V2_CONFIGURATION);
        when(influxDBRepositoryMock.isConnected()).thenReturn(false);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(influxDBRepositoryMock, never()).write(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void failedWriteIsRetried() throws UnexpectedConditionException {
        InfluxDBPersistenceService instance = getService(VALID_V2_CONFIGURATION);
        when(influxDBRepositoryMock.isConnected()).thenReturn(true);
        when(influxDBRepositoryMock.write(any())).thenReturn(WriteResult.retry("connection refused"),
                WriteResult.SUCCESS);

        instance.store(ItemTestHelper.createNumberItem("number", 5));

        ArgumentCaptor<List<InfluxPoint>> captor = ArgumentCaptor.forClass(List.class);
        verify(influxDBRepositoryMock, timeout(10000).times(2)).write(captor.capture());
        assertThat(captor.getAllValues().get(1).size(), is(1));
        assertThat(captor.getAllValues().get(1).get(0), is(captor.getAllValues().get(0).get(0)));
        verify(influxDBRepositoryMock).disconnect();
        instance.deactivate();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rejectedWriteIsNotRetried() throws UnexpectedConditionException {
        InfluxDBPersistenceService instance = getService(VALID_V2_CONFIGURATION);
        when(influxDBRepositoryMock.isConnected()).thenReturn(true);
        when(influxDBRepositoryMock.write(any())).thenReturn(WriteResult.rejected("field type conflict"),
                WriteResult.SUCCESS);

        instance.store(ItemTestHelper.createNumberItem("rejected", 5));
        verify(influxDBRepositoryMock, timeout(10000)).write(any());
        instance.store(ItemTestHelper.createNumberItem("accepted", 6));

        ArgumentCaptor<List<InfluxPoint>> captor = ArgumentCaptor.forClass(List.class);
        verify(influxDBRepositoryMock, timeout(10000).times(2)).write(captor.capture());
        List<InfluxPoint> secondBatch = captor.getAllValues().get(1);
        assertThat(secondBatch.size(), is(1));
        assertThat(secondBatch.get(0).getTags().get("item"), is("accepted"));
        verify(influxDBRepositoryMock, never()).disconnect();
        instance.deactivate();
    }

    @Test
    public void timestampedStateIsConvertedToTheItemStateType()
            throws UnexpectedConditionException, InterruptedException {
        InfluxDBPersistenceService instance = getService(VALID_V2_CONFIGURATION);
        instance.setItemFactory(new CoreItemFactory(mock(UnitProvider.class)));
        when(influxDBRepositoryMock.isConnected()).thenReturn(true);
        List<InfluxPoint> points = new CopyOnWriteArrayList<>();
        when(influxDBRepositoryMock.write(any())).thenAnswer(invocation -> {
            points.addAll(invocation.<List<InfluxPoint>> getArgument(0));
            return WriteResult.SUCCESS;
        });

        instance.store(new DimmerItem("dimmer"), ZonedDateTime.now(), OnOffType.ON);
        instance.store(new ColorItem("color"), ZonedDateTime.now(), PercentType.HUNDRED);

        // each conversion is asynchronous, so the two points may be written in separate batches
        long deadline = System.currentTimeMillis() + 10000;
        while (points.size() < 2 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertThat(points.size(), is(2));
        InfluxPoint dimmer = points.stream().filter(p -> "dimmer".equals(p.getTags().get("item"))).findFirst().get();
        InfluxPoint color = points.stream().filter(p -> "color".equals(p.getTags().get("item"))).findFirst().get();
        // ON is stored as 100 (%), not as the digital value 1
        assertThat(dimmer.getValue(), is(PercentType.HUNDRED.toBigDecimal()));
        // a Color item is restored with new HSBType(String), so it needs an HSB string, not a scalar
        assertThat(color.getValue(), is("0,0,100"));
        instance.deactivate();
    }

    @Test
    public void numericStatesAndTypedGroupsKeepTheirStoredValues()
            throws UnexpectedConditionException, InterruptedException {
        InfluxDBPersistenceService instance = getService(VALID_V2_CONFIGURATION);
        instance.setItemFactory(new CoreItemFactory(mock(UnitProvider.class)));
        when(influxDBRepositoryMock.isConnected()).thenReturn(true);
        List<InfluxPoint> points = new CopyOnWriteArrayList<>();
        when(influxDBRepositoryMock.write(any())).thenAnswer(invocation -> {
            points.addAll(invocation.<List<InfluxPoint>> getArgument(0));
            return WriteResult.SUCCESS;
        });

        // numeric states that were already written correctly are not rescaled or dropped
        instance.store(new NumberItem("number"), ZonedDateTime.now(), PercentType.valueOf("50"));
        instance.store(new DimmerItem("plain"), ZonedDateTime.now(), new DecimalType(50));
        // a typed group stores like its base item
        instance.store(new GroupItem("group", new DimmerItem("base")), ZonedDateTime.now(), OnOffType.ON);

        long deadline = System.currentTimeMillis() + 10000;
        while (points.size() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertThat(points.size(), is(3));
        assertThat(valueOf(points, "number"), is(new BigDecimal("50")));
        assertThat(valueOf(points, "plain"), is(new BigDecimal("50")));
        assertThat(valueOf(points, "group"), is(PercentType.HUNDRED.toBigDecimal()));
        instance.deactivate();
    }

    private Object valueOf(List<InfluxPoint> points, String itemName) {
        return points.stream().filter(p -> itemName.equals(p.getTags().get("item"))).findFirst().get().getValue();
    }

    private InfluxDBPersistenceService getService(Map<String, Object> config) {
        return new InfluxDBPersistenceService(mock(ItemRegistry.class), influxDBMetadataService, config) {
            @Override
            protected InfluxDBRepository createInfluxDBRepository() {
                return influxDBRepositoryMock;
            }
        };
    }
}
