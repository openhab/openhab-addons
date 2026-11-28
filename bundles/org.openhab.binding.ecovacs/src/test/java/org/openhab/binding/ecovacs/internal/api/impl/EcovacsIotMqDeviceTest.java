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
package org.openhab.binding.ecovacs.internal.api.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.Device;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalCleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.model.CleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;

import com.google.gson.Gson;

/**
 * @author Andreas Rothstein - Initial contribution
 */
@NonNullByDefault
class EcovacsIotMqDeviceTest {
    private static final String PRIMARY_URL = "https://example.invalid/new-map";
    private static final String LEGACY_URL = "https://example.invalid/legacy-map";
    private final Gson gson = new Gson();
    private final EcovacsApiImpl api = mock(EcovacsApiImpl.class);
    private final Device device = mock(Device.class);
    private final CleanLogRecord record = new CleanLogRecord(1700000000L, 120, 10, Optional.of(PRIMARY_URL),
            CleanMode.AUTO);

    @Test
    void successfulPrimaryImageDoesNotFetchLegacyLogs() throws Exception {
        byte[] image = { 1, 2, 3 };
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.of(image));
        assertArrayEquals(image, robot(true).downloadCleanMapImage(record).orElseThrow());
        verify(api, never()).fetchCleanLogs(any());
    }

    @Test
    void usesAlternateImageForSameRunOnly() throws Exception {
        byte[] image = { 4, 5, 6 };
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.empty());
        when(api.fetchCleanLogs(device)).thenReturn(
                List.of(log(1699999999L, "https://example.invalid/other-run"), log(1700000000L, LEGACY_URL)));
        when(api.downloadCleanMapImage(device, LEGACY_URL, false)).thenReturn(Optional.of(image));
        assertArrayEquals(image, robot(true).downloadCleanMapImage(record).orElseThrow());
        verify(api, never()).downloadCleanMapImage(device, "https://example.invalid/other-run", false);
    }

    @Test
    void doesNotDisplayMapOfAnotherRun() throws Exception {
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.empty());
        when(api.fetchCleanLogs(device)).thenReturn(List.of(log(1700000001L, LEGACY_URL)));
        assertTrue(robot(true).downloadCleanMapImage(record).isEmpty());
        verify(api, never()).downloadCleanMapImage(device, LEGACY_URL, false);
    }

    @Test
    void doesNotRetrySameUrlOrEmptyUrl() throws Exception {
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.empty());
        when(api.fetchCleanLogs(device)).thenReturn(List.of(log(1700000000L, PRIMARY_URL), log(1700000000L, ""),
                Objects.requireNonNull(gson.fromJson("{\"ts\":1700000000}", PortalCleanLogRecord.class))));
        assertTrue(robot(true).downloadCleanMapImage(record).isEmpty());
        verify(api, times(1)).downloadCleanMapImage(any(), anyString(), anyBoolean());
    }

    @Test
    void failedOptionalLookupDoesNotInterruptPolling() throws Exception {
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.empty());
        when(api.fetchCleanLogs(device)).thenThrow(new EcovacsApiException("test"));
        assertTrue(robot(true).downloadCleanMapImage(record).isEmpty());
    }

    @Test
    void failedAlternateDownloadDoesNotInterruptPolling() throws Exception {
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.empty());
        when(api.fetchCleanLogs(device)).thenReturn(List.of(log(1700000000L, LEGACY_URL)));
        when(api.downloadCleanMapImage(device, LEGACY_URL, false)).thenThrow(new EcovacsApiException("test"));
        assertTrue(robot(true).downloadCleanMapImage(record).isEmpty());
    }

    @Test
    void interruptionIsPropagated() throws Exception {
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.empty());
        when(api.fetchCleanLogs(device)).thenThrow(new InterruptedException("test"));
        assertThrows(InterruptedException.class, () -> robot(true).downloadCleanMapImage(record));
    }

    @Test
    void otherProfilesKeepTheirOriginalDownloadPath() throws Exception {
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.empty());
        assertTrue(robot(false).downloadCleanMapImage(record).isEmpty());
        verify(api, never()).fetchCleanLogs(any());
    }

    @Test
    void recordWithoutImageUrlDoesNotCallCloud() throws Exception {
        CleanLogRecord missing = new CleanLogRecord(1700000000L, 120, 10, Optional.empty(), CleanMode.AUTO);
        assertTrue(robot(true).downloadCleanMapImage(missing).isEmpty());
        verifyNoInteractions(api);
    }

    @Test
    void interruptionDuringAlternateDownloadIsPropagated() throws Exception {
        when(api.downloadCleanMapImage(device, PRIMARY_URL, true)).thenReturn(Optional.empty());
        when(api.fetchCleanLogs(device)).thenReturn(List.of(log(1700000000L, LEGACY_URL)));
        when(api.downloadCleanMapImage(device, LEGACY_URL, false)).thenThrow(new InterruptedException("cancelled"));
        assertThrows(InterruptedException.class, () -> robot(true).downloadCleanMapImage(record));
    }

    @Test
    void fallbackCapabilityDoesNotChangeUnsignedImageDownloads() throws Exception {
        when(api.downloadCleanMapImage(device, PRIMARY_URL, false)).thenReturn(Optional.empty());
        assertTrue(robot(Set.of(DeviceCapability.LEGACY_CLEAN_MAP_FALLBACK)).downloadCleanMapImage(record).isEmpty());
        verify(api, never()).fetchCleanLogs(any());
    }

    private EcovacsIotMqDevice robot(boolean fallback) throws EcovacsApiException {
        Set<DeviceCapability> capabilities = fallback
                ? Set.of(DeviceCapability.USES_CLEAN_RESULTS_LOG_API, DeviceCapability.LEGACY_CLEAN_MAP_FALLBACK)
                : Set.of(DeviceCapability.USES_CLEAN_RESULTS_LOG_API);
        return robot(capabilities);
    }

    private EcovacsIotMqDevice robot(Set<DeviceCapability> capabilities) throws EcovacsApiException {
        DeviceDescription desc = new DeviceDescription("test", "tj7mri", null, ProtocolVersion.JSON_V2, true,
                capabilities);
        return new EcovacsIotMqDevice(device, desc, api, gson);
    }

    private PortalCleanLogRecord log(long timestamp, String url) {
        return Objects.requireNonNull(
                gson.fromJson("{\"ts\":" + timestamp + ",\"imageUrl\":\"" + url + "\"}", PortalCleanLogRecord.class));
    }
}
