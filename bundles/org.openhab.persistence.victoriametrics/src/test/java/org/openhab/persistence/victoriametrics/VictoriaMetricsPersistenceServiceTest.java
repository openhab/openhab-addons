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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConfiguration.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.persistence.victoriametrics.internal.ItemTestHelper;
import org.openhab.persistence.victoriametrics.internal.UnexpectedConditionException;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsMetadataService;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository;

/**
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 * @author Michael Weger - Add configuration validation tests
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
    public void deactivateShouldDisconnectRepository() {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION_WITHOUT_AUTHENTICATION);
        instance.deactivate();
        verify(repositoryMock).disconnect();
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
        verify(repositoryMock).disconnect();
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
        return new VictoriaMetricsPersistenceService(mock(ItemRegistry.class), metadataService,
                mock(HttpClientFactory.class), config) {
            @Override
            protected VictoriaMetricsRepository createVictoriaMetricsRepository() {
                return repositoryMock;
            }
        };
    }
}
