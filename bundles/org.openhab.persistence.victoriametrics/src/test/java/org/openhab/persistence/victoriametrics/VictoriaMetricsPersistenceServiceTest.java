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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConfiguration.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.persistence.victoriametrics.internal.ItemTestHelper;
import org.openhab.persistence.victoriametrics.internal.UnexpectedConditionException;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsMetadataService;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsPoint;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository;

/**
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 * @author Michael Weger - Add configuration validation, deactivation, stop deadline, queue, filter unit and string
 *         state read tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class VictoriaMetricsPersistenceServiceTest {
    private static final Map<String, Object> VALID_CONFIGURATION_WITHOUT_AUTHENTICATION = Map.of( //
            URL_PARAM, "http://localhost:8428");

    private static final Map<String, Object> VALID_CONFIGURATION_WITH_USER = Map.of( //
            URL_PARAM, "http://localhost:8428", //
            USER_PARAM, "user", PASSWORD_PARAM, "password", //
            METRIC_PREFIX_PARAM, "openhab_");

    private static final Map<String, Object> VALID_CONFIGURATION_WITH_TOKEN = Map.of( //
            URL_PARAM, "http://localhost:8428", //
            TOKEN_PARAM, "sampletoken");

    private static final Map<String, Object> INVALID_CONFIGURATION_WITHOUT_PASSWORD = Map.of( //
            URL_PARAM, "http://localhost:8428", //
            USER_PARAM, "user");

    private static final Map<String, Object> INVALID_CONFIGURATION_WITH_INVALID_PREFIX = Map.of( //
            URL_PARAM, "http://localhost:8428", //
            METRIC_PREFIX_PARAM, "open-hab.");

    private @Mock @NonNullByDefault({}) VictoriaMetricsRepository repositoryMock;

    private final ItemRegistry itemRegistry = mock(ItemRegistry.class);
    private final MetadataRegistry metadataRegistry = mock(MetadataRegistry.class);
    private final VictoriaMetricsMetadataService metadataService = new VictoriaMetricsMetadataService(metadataRegistry);

    @Test
    public void activateWithValidConfigWithoutAuthenticationShouldConnectRepository() {
        getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        verify(repositoryMock).connect();
    }

    @Test
    public void activateWithValidConfigWithUserShouldConnectRepository() {
        getService(VALID_CONFIGURATION_WITH_USER);
        verify(repositoryMock).connect();
    }

    @Test
    public void activateWithValidConfigWithTokenShouldConnectRepository() {
        getService(VALID_CONFIGURATION_WITH_TOKEN);
        verify(repositoryMock).connect();
    }

    @Test
    public void activateWithUserWithoutPasswordShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> getService(INVALID_CONFIGURATION_WITHOUT_PASSWORD));
    }

    @Test
    public void activateWithInvalidPrefixShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> getService(INVALID_CONFIGURATION_WITH_INVALID_PREFIX));
    }

    @Test
    public void activateWithInvalidSuffixShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> getService(Map.of(URL_PARAM, "http://localhost:8428", METRIC_SUFFIX_PARAM, ".value")));
    }

    @Test
    public void activateWithInvalidMaxStringStatesShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> getService(Map.of(URL_PARAM, "http://localhost:8428", MAX_STRING_STATES_PARAM, 0)));
    }

    @Test
    public void activateWithInvalidMaxQueueSizeShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> getService(Map.of(URL_PARAM, "http://localhost:8428", MAX_QUEUE_SIZE_PARAM, 0)));
    }

    @Test
    public void activateWithInvalidQueueOverflowShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> getService(Map.of(URL_PARAM, "http://localhost:8428", QUEUE_OVERFLOW_PARAM, "dropAll")));
    }

    @Test
    public void backlogIsWrittenInBatches() throws Exception {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        List<Integer> batchSizes = new CopyOnWriteArrayList<>();
        when(repositoryMock.write(any())).thenAnswer(invocation -> {
            batchSizes.add(invocation.<List<?>> getArgument(0).size());
            return true;
        });

        for (int i = 0; i < 10_001; i++) {
            instance.store(ItemTestHelper.createNumberItem("number", i));
        }
        CompletableFuture.runAsync(instance::deactivate).get(30, TimeUnit.SECONDS);

        assertThat(batchSizes.stream().mapToInt(Integer::intValue).sum(), is(10_001));
        assertThat(batchSizes, everyItem(lessThanOrEqualTo(10_000)));
    }

    @Test
    public void failedStringStateReadOnlyDefersStatesOfThatItem() throws Exception {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        MetadataKey modeKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, "Mode");
        when(metadataRegistry.get(modeKey)).thenReturn(new Metadata(modeKey, "", Map.of("stringStates", true)));
        // the first read fails, e.g. because the database is busy, the retry succeeds
        when(repositoryMock.getStringStates("Mode", "Mode")).thenReturn(null).thenReturn(Set.of());
        List<String> written = new CopyOnWriteArrayList<>();
        when(repositoryMock.write(any())).thenAnswer(invocation -> {
            invocation.<List<VictoriaMetricsPoint>> getArgument(0).forEach(point -> written.add(point.getMetricName()));
            return true;
        });

        StringItem mode = new StringItem("Mode");
        mode.setState(new StringType("IDLE"));
        instance.store(mode);
        instance.store(ItemTestHelper.createNumberItem("number", 5));

        // the scheduled commit (every 3 seconds) writes the other item, although the read for Mode failed
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (!written.contains("number") && System.nanoTime() < deadline) {
            Thread.sleep(100);
        }
        assertThat(written, is(List.of("number")));
        verify(repositoryMock, never()).disconnect();

        // the deferred string state is written with the next commit
        CompletableFuture.runAsync(instance::deactivate).get(30, TimeUnit.SECONDS);
        assertThat(written, is(List.of("number", "Mode")));
    }

    @Test
    public void quantityFilterIsConvertedToItemUnit() {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        UnitProvider unitProvider = mock(UnitProvider.class);
        when(unitProvider.getUnit(Temperature.class)).thenReturn(SIUnits.CELSIUS);
        NumberItem item = new NumberItem("Number:Temperature", "temperature", unitProvider);
        when(itemRegistry.get("temperature")).thenReturn(item);
        FilterCriteria filter = new FilterCriteria().setItemName("temperature")
                .setState(new QuantityType<>(68, ImperialUnits.FAHRENHEIT));

        instance.query(filter);

        verify(repositoryMock).query(
                argThat(converted -> converted.getState() instanceof QuantityType<?> quantity
                        && quantity.getUnit().equals(SIUnits.CELSIUS) && quantity.doubleValue() == 20.0),
                isNull(), eq(SIUnits.CELSIUS));
        // the filter of the caller is not changed
        assertThat(filter.getState(), is(new QuantityType<>(68, ImperialUnits.FAHRENHEIT)));
    }

    @Test
    public void quantityFilterWithIncompatibleUnitReturnsNothing() {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        UnitProvider unitProvider = mock(UnitProvider.class);
        when(unitProvider.getUnit(Temperature.class)).thenReturn(SIUnits.CELSIUS);
        NumberItem item = new NumberItem("Number:Temperature", "temperature", unitProvider);
        when(itemRegistry.get("temperature")).thenReturn(item);
        FilterCriteria filter = new FilterCriteria().setItemName("temperature")
                .setState(new QuantityType<>(5, SIUnits.METRE));

        assertThat(instance.query(filter).iterator().hasNext(), is(false));
        verify(repositoryMock, never()).query(any(), any(), any());
    }

    @Test
    public void deactivateShouldCloseRepository() {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        instance.deactivate();
        verify(repositoryMock).close();
    }

    @Test
    public void storeItemWithConnectedRepository() throws UnexpectedConditionException {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(repositoryMock, timeout(5000)).write(any());
    }

    @Test
    public void storeItemWithDisconnectedRepositoryIsIgnored() throws UnexpectedConditionException {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(false);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(repositoryMock, never()).write(any());
    }

    @Test
    public void deactivateWritesPointsOfPendingConversions() throws Exception {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        when(repositoryMock.write(any())).thenReturn(true);
        CountDownLatch conversionStarted = new CountDownLatch(1);
        CountDownLatch releaseConversion = new CountDownLatch(1);
        // the metadata is read during the asynchronous conversion
        when(metadataRegistry.get(any())).thenAnswer(invocation -> {
            conversionStarted.countDown();
            releaseConversion.await(10, TimeUnit.SECONDS);
            return null;
        });

        instance.store(ItemTestHelper.createNumberItem("number", 5));
        assertTrue(conversionStarted.await(10, TimeUnit.SECONDS));
        CompletableFuture<Void> deactivation = CompletableFuture.runAsync(instance::deactivate);
        // deactivation waits for the pending conversion instead of finishing with an empty queue
        assertThrows(TimeoutException.class, () -> deactivation.get(1, TimeUnit.SECONDS));
        releaseConversion.countDown();
        deactivation.get(30, TimeUnit.SECONDS);

        verify(repositoryMock).write(argThat(points -> points.size() == 1));
    }

    @Test
    public void deactivateWaitsForRunningCommit() throws Exception {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        CountDownLatch writeStarted = new CountDownLatch(1);
        CountDownLatch releaseWrite = new CountDownLatch(1);
        when(repositoryMock.write(any())).thenAnswer(invocation -> {
            writeStarted.countDown();
            releaseWrite.await(10, TimeUnit.SECONDS);
            return true;
        });

        instance.store(ItemTestHelper.createNumberItem("number", 5));
        // the scheduled commit runs every 3 seconds
        assertTrue(writeStarted.await(10, TimeUnit.SECONDS));
        CompletableFuture<Void> deactivation = CompletableFuture.runAsync(instance::deactivate);
        assertThrows(TimeoutException.class, () -> deactivation.get(1, TimeUnit.SECONDS));
        releaseWrite.countDown();
        deactivation.get(30, TimeUnit.SECONDS);

        verify(repositoryMock, times(1)).write(any());
        verify(repositoryMock).close();
    }

    @Test
    public void deactivateAbortsHangingWriteAtDeadline() throws Exception {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION,
                Duration.ofSeconds(1));
        CountDownLatch closed = new CountDownLatch(1);
        CountDownLatch writeStarted = new CountDownLatch(1);
        AtomicBoolean finalCommitRan = new AtomicBoolean();
        when(repositoryMock.isConnected()).thenAnswer(invocation -> {
            // only the final commit checks the connection after the abort, it runs once the aborted commit returned
            finalCommitRan.compareAndSet(false, closed.getCount() == 0);
            return closed.getCount() > 0;
        });
        doAnswer(invocation -> {
            closed.countDown();
            return null;
        }).when(repositoryMock).close();
        // like a request that only ends when it is aborted
        when(repositoryMock.write(any())).thenAnswer(invocation -> {
            writeStarted.countDown();
            closed.await(20, TimeUnit.SECONDS);
            return false;
        });

        instance.store(ItemTestHelper.createNumberItem("number", 5));
        // the scheduled commit runs every 3 seconds
        assertTrue(writeStarted.await(10, TimeUnit.SECONDS));
        CompletableFuture.runAsync(instance::deactivate).get(10, TimeUnit.SECONDS);

        assertThat(closed.getCount(), is(0L));
        // the running commit was aborted at the deadline instead of making the final commit wait for the lock
        assertTrue(finalCommitRan.get());
        // the re-queued point is not written after the abort
        verify(repositoryMock, times(1)).write(any());
    }

    @Test
    public void storeAfterDeactivateIsRejected() {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        instance.deactivate();

        instance.store(ItemTestHelper.createNumberItem("number", 5));

        verifyNoInteractions(metadataRegistry);
        verify(repositoryMock, never()).write(any());
    }

    @Test
    public void failedConversionDoesNotBlockDeactivation() throws Exception {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        CountDownLatch conversionFailed = new CountDownLatch(1);
        when(metadataRegistry.get(any())).thenAnswer(invocation -> {
            conversionFailed.countDown();
            throw new IllegalStateException("conversion failed");
        });

        instance.store(ItemTestHelper.createNumberItem("number", 5));
        assertTrue(conversionFailed.await(10, TimeUnit.SECONDS));
        // the deactivation would wait the full timeout if the failed store stayed pending
        CompletableFuture.runAsync(instance::deactivate).get(5, TimeUnit.SECONDS);

        verify(repositoryMock, never()).write(any());
    }

    @Test
    public void serviceIsNotModifiable() {
        // VictoriaMetrics cannot replace samples with the same timestamp or delete time ranges
        assertFalse(getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION) instanceof ModifiablePersistenceService);
    }

    private VictoriaMetricsPersistenceService getService(Map<String, Object> config) {
        return getService(config, Duration.ofSeconds(30));
    }

    private VictoriaMetricsPersistenceService getService(Map<String, Object> config, Duration stopTimeout) {
        return new VictoriaMetricsPersistenceService(itemRegistry, metadataService, mock(HttpClientFactory.class),
                config) {
            @Override
            protected VictoriaMetricsRepository createVictoriaMetricsRepository() {
                return repositoryMock;
            }

            @Override
            protected Duration getStopTimeout() {
                return stopTimeout;
            }
        };
    }
}
