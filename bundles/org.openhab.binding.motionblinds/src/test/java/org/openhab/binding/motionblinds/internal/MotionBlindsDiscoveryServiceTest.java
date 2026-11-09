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
package org.openhab.binding.motionblinds.internal;

import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;

/**
 * Tests that {@link MotionBlindsDiscoveryService} relies on the scan timeout of the framework.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class MotionBlindsDiscoveryServiceTest {

    private @Mock @NonNullByDefault({}) MotionBlindsCommunicationManager communicationManager;
    private @Mock @NonNullByDefault({}) TranslationProvider i18nProvider;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) MotionBlindsDiscoveryService discoveryService;

    @BeforeEach
    public void setUp() {
        discoveryService = new MotionBlindsDiscoveryService(communicationManager, i18nProvider, localeProvider);
    }

    @AfterEach
    public void tearDown() {
        discoveryService.abortScan();
    }

    @Test
    public void scanIsStoppedOnlyByTheFrameworkTimeout() throws InterruptedException {
        discoveryService.startScan(null);
        verify(communicationManager).startDiscovery(discoveryService);
        // stopScan() is called before every scan
        verify(communicationManager, times(1)).stopDiscovery(discoveryService);

        // after the scan timeout of 5 seconds the framework calls stopScan() once; no timer of the binding
        Thread.sleep(6000);
        verify(communicationManager, times(2)).stopDiscovery(discoveryService);
    }

    @Test
    public void abortedScanStopsDiscovery() {
        discoveryService.startScan(null);
        // stopScan() is called before every scan
        verify(communicationManager, times(1)).stopDiscovery(discoveryService);

        discoveryService.abortScan();
        verify(communicationManager, times(2)).stopDiscovery(discoveryService);
    }
}
