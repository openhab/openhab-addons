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
package org.openhab.binding.miio.internal.basic;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openhab.core.service.WatchService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Tests the registration and the event handling of the {@link MiIoDatabaseWatchService}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoDatabaseWatchServiceTest {

    private final WatchService watchService = mock(WatchService.class);
    private final Bundle bundle = mock(Bundle.class);
    private final MockedStatic<FrameworkUtil> frameworkUtil = mockStatic(FrameworkUtil.class);

    @BeforeEach
    public void setUp() {
        frameworkUtil.when(() -> FrameworkUtil.getBundle(MiIoDatabaseWatchService.class)).thenReturn(bundle);
        when(bundle.findEntries(anyString(), anyString(), anyBoolean()))
                .thenAnswer(invocation -> Collections.emptyEnumeration());
    }

    @AfterEach
    public void tearDown() {
        frameworkUtil.close();
    }

    @Test
    public void listenerIsRegisteredForLocalDatabaseFolder() {
        MiIoDatabaseWatchService service = new MiIoDatabaseWatchService(watchService);

        verify(watchService).registerListener(service, Path.of("misc", "miio"), false);
    }

    @Test
    public void databaseFileEventReloadsDatabase() {
        MiIoDatabaseWatchService service = new MiIoDatabaseWatchService(watchService);
        verify(bundle, times(1)).findEntries(anyString(), anyString(), anyBoolean());

        service.processWatchEvent(WatchService.Kind.MODIFY, Path.of("my.device-miot.json"));

        verify(bundle, times(2)).findEntries(anyString(), anyString(), anyBoolean());
    }

    @Test
    public void otherFileEventIsIgnored() {
        MiIoDatabaseWatchService service = new MiIoDatabaseWatchService(watchService);

        service.processWatchEvent(WatchService.Kind.CREATE, Path.of("notes.txt"));

        verify(bundle, times(1)).findEntries(anyString(), anyString(), anyBoolean());
    }
}
