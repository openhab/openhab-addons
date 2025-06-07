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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConfiguration.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.persistence.victoriametrics.internal.ItemTestHelper;
import org.openhab.persistence.victoriametrics.internal.UnexpectedConditionException;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsMetadataService;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository;

/**
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class VictoriaMetricsPersistenceServiceTest {
    private static final Map<String, Object> VALID_CONFIGURATION = Map.of(URL_PARAM, "http://localhost:8428",
            USER_PARAM, "user", PASSWORD_PARAM, "password", MEASUREMENT_PREFIX, "openhab_");

    private static final Map<String, Object> INVALID_CONFIGURATION = Map.of(URL_PARAM, "http://localhost:8428",
            USER_PARAM, "user", MEASUREMENT_PREFIX, "openhab_");

    private @Mock @NonNullByDefault({}) VictoriaMetricsRepository repositoryMock;

    private final VictoriaMetricsMetadataService metadataService = new VictoriaMetricsMetadataService(
            mock(MetadataRegistry.class));

    @Test
    public void activateWithValidConfigShouldConnectRepository() {
        getService(VALID_CONFIGURATION);
        verify(repositoryMock).connect();
    }

    @Test
    public void activateWithInvalidConfigShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> getService(INVALID_CONFIGURATION));
    }

    @Test
    public void storeItemWithConnectedRepository() throws UnexpectedConditionException {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION);
        when(repositoryMock.isConnected()).thenReturn(true);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(repositoryMock, timeout(5000)).write(any());
    }

    @Test
    public void storeItemWithDisconnectedRepositoryIsIgnored() throws UnexpectedConditionException {
        VictoriaMetricsPersistenceService instance = getService(VALID_CONFIGURATION);
        when(repositoryMock.isConnected()).thenReturn(false);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(repositoryMock, never()).write(any());
    }

    private VictoriaMetricsPersistenceService getService(Map<String, Object> config) {
        return new VictoriaMetricsPersistenceService(mock(ItemRegistry.class), metadataService, config) {
            @Override
            protected VictoriaMetricsRepository createVictoriaMetricsRepository() {
                return repositoryMock;
            }
        };
    }
}
