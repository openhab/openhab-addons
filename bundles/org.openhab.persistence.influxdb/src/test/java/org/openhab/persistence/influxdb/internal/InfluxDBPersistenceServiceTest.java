/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.DATABASE_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.PASSWORD_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.RETENTION_POLICY_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.TOKEN_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.URL_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.USER_PARAM;
import static org.openhab.persistence.influxdb.internal.InfluxDBConfiguration.VERSION_PARAM;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.persistence.influxdb.InfluxDBPersistenceService;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
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

    private @Mock @NonNullByDefault({}) InfluxDBRepository influxDBRepositoryMock;

    private final InfluxDBMetadataService influxDBMetadataService = new InfluxDBMetadataService(
            mock(MetadataRegistry.class));

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
    public void activateWithInvalidV1ConfigShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> getService(INVALID_V1_CONFIGURATION));
    }

    @Test
    public void activateWithInvalidV2ShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> getService(INVALID_V2_CONFIGURATION));
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
        verify(influxDBRepositoryMock).write(any());
    }

    @Test
    public void storeItemWithDisconnectedRepositoryIsIgnored() throws UnexpectedConditionException {
        InfluxDBPersistenceService instance = getService(VALID_V2_CONFIGURATION);
        when(influxDBRepositoryMock.isConnected()).thenReturn(false);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(influxDBRepositoryMock, never()).write(any());
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
