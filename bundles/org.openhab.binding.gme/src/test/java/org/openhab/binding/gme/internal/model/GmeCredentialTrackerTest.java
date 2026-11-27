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
package org.openhab.binding.gme.internal.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.storage.Storage;

@NonNullByDefault
@SuppressWarnings("null")
class GmeCredentialTrackerTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");
    private static final Instant NOW = Instant.parse("2026-09-30T20:00:00Z");

    @Test
    void usesInitialPasswordChangeDateOnFirstSuccessfulAuthentication() {
        Storage<String> storage = mock(Storage.class);
        GmeCredentialTracker tracker = tracker(storage);

        Instant changedAt = tracker.updateAfterSuccessfulAuthentication("user", "password", "2026-08-15");

        assertEquals(LocalDate.of(2026, 8, 15).atStartOfDay(ROME).toInstant(), changedAt);
        verify(storage).put("passwordChangedAt", changedAt.toString());
    }

    @Test
    void usesCurrentTimeWhenNoInitialDateIsConfigured() {
        Storage<String> storage = mock(Storage.class);
        GmeCredentialTracker tracker = tracker(storage);

        Instant changedAt = tracker.updateAfterSuccessfulAuthentication("user", "password", "");

        assertEquals(NOW, changedAt);
        verify(storage).put("passwordChangedAt", NOW.toString());
    }

    @Test
    void preservesChangeTimeWhenCredentialsHaveNotChanged() {
        Storage<String> storage = mock(Storage.class);
        GmeCredentialTracker firstTracker = tracker(storage);

        firstTracker.updateAfterSuccessfulAuthentication("user", "password", "");

        ArgumentCaptor<String> fingerprint = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> salt = ArgumentCaptor.forClass(String.class);
        verify(storage).put(eq("credentialFingerprint"), fingerprint.capture());
        verify(storage).put(eq("credentialSalt"), salt.capture());

        reset(storage);

        Instant originalChangedAt = Instant.parse("2026-08-01T10:00:00Z");
        when(storage.get("credentialFingerprint")).thenReturn(fingerprint.getValue());
        when(storage.get("credentialSalt")).thenReturn(salt.getValue());
        when(storage.get("passwordChangedAt")).thenReturn(originalChangedAt.toString());

        GmeCredentialTracker restartedTracker = tracker(storage);
        Instant changedAt = restartedTracker.updateAfterSuccessfulAuthentication("user", "password", "");

        assertEquals(originalChangedAt, changedAt);
        verify(storage, never()).put(anyString(), anyString());
    }

    @Test
    void recordsCurrentTimeWhenCredentialsChange() {
        Storage<String> storage = mock(Storage.class);
        GmeCredentialTracker firstTracker = tracker(storage);

        firstTracker.updateAfterSuccessfulAuthentication("user", "old-password", "");

        ArgumentCaptor<String> fingerprint = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> salt = ArgumentCaptor.forClass(String.class);
        verify(storage).put(eq("credentialFingerprint"), fingerprint.capture());
        verify(storage).put(eq("credentialSalt"), salt.capture());

        reset(storage);

        when(storage.get("credentialFingerprint")).thenReturn(fingerprint.getValue());
        when(storage.get("credentialSalt")).thenReturn(salt.getValue());
        when(storage.get("passwordChangedAt")).thenReturn("2026-08-01T10:00:00Z");

        GmeCredentialTracker tracker = tracker(storage);
        Instant changedAt = tracker.updateAfterSuccessfulAuthentication("user", "new-password", "");

        assertEquals(NOW, changedAt);
        verify(storage).put("passwordChangedAt", NOW.toString());
    }

    @Test
    void preservesChangeTimeWhenMigratingLegacyUnsaltedFingerprint() {
        Storage<String> storage = mock(Storage.class);
        Instant originalChangedAt = Instant.parse("2026-08-01T10:00:00Z");

        when(storage.get("credentialFingerprint")).thenReturn("legacy-unsalted-fingerprint");
        when(storage.get("credentialSalt")).thenReturn(null);
        when(storage.get("passwordChangedAt")).thenReturn(originalChangedAt.toString());

        GmeCredentialTracker tracker = tracker(storage);
        Instant changedAt = tracker.updateAfterSuccessfulAuthentication("user", "password", "");

        assertEquals(originalChangedAt, changedAt);
        verify(storage).put(eq("credentialSalt"), anyString());
        verify(storage).put(eq("credentialFingerprint"), anyString());
        verify(storage, never()).put(eq("passwordChangedAt"), anyString());
    }

    @Test
    void repairsInvalidPersistedChangeTime() {
        Storage<String> storage = mock(Storage.class);
        when(storage.get("credentialFingerprint")).thenReturn("existing-fingerprint");
        when(storage.get("passwordChangedAt")).thenReturn("not-an-instant");

        GmeCredentialTracker tracker = tracker(storage);
        Instant changedAt = tracker.updateAfterSuccessfulAuthentication("user", "password", "");

        assertEquals(NOW, changedAt);
        verify(storage).put("passwordChangedAt", NOW.toString());
    }

    @Test
    void fallsBackToCurrentTimeForInvalidInitialDate() {
        Storage<String> storage = mock(Storage.class);
        GmeCredentialTracker tracker = tracker(storage);

        Instant changedAt = tracker.updateAfterSuccessfulAuthentication("user", "password", "invalid-date");

        assertEquals(NOW, changedAt);
    }

    private static GmeCredentialTracker tracker(Storage<String> storage) {
        return new GmeCredentialTracker(storage, ROME, Clock.fixed(NOW, ROME));
    }
}
