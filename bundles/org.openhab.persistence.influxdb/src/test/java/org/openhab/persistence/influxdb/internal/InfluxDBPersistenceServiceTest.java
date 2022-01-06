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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
@NonNullByDefault(value = { DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE })
public class InfluxDBPersistenceServiceTest {
    private InfluxDBPersistenceService instance;

    private @Mock InfluxDBRepository influxDBRepository;

    private Map<String, Object> validConfig;
    private Map<String, Object> invalidConfig;

    @BeforeEach
    public void before() {
        instance = new InfluxDBPersistenceService(mock(ItemRegistry.class), mock(MetadataRegistry.class)) {
            @Override
            protected InfluxDBRepository createInfluxDBRepository() {
                return influxDBRepository;
            }
        };

        validConfig = ConfigurationTestHelper.createValidConfigurationParameters();
        invalidConfig = ConfigurationTestHelper.createInvalidConfigurationParameters();
    }

    @AfterEach
    public void after() {
        validConfig = null;
        invalidConfig = null;
        instance = null;
        influxDBRepository = null;
    }

    @Test
    public void activateWithValidConfigShouldConnectRepository() {
        instance.activate(validConfig);
        verify(influxDBRepository).connect();
    }

    @Test
    public void activateWithInvalidConfigShouldNotConnectRepository() {
        instance.activate(invalidConfig);
        verify(influxDBRepository, never()).connect();
    }

    @Test
    public void activateWithNullConfigShouldNotConnectRepository() {
        instance.activate(null);
        verify(influxDBRepository, never()).connect();
    }

    @Test
    public void deactivateShouldDisconnectRepository() {
        instance.activate(validConfig);
        instance.deactivate();
        verify(influxDBRepository).disconnect();
    }

    @Test
    public void storeItemWithConnectedRepository() {
        instance.activate(validConfig);
        when(influxDBRepository.isConnected()).thenReturn(true);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(influxDBRepository).write(any());
    }

    @Test
    public void storeItemWithDisconnectedRepositoryIsIgnored() {
        instance.activate(validConfig);
        when(influxDBRepository.isConnected()).thenReturn(false);
        instance.store(ItemTestHelper.createNumberItem("number", 5));
        verify(influxDBRepository, never()).write(any());
    }
}
