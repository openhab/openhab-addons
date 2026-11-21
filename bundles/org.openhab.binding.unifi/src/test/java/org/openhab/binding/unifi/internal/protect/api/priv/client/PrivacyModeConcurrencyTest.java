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
package org.openhab.binding.unifi.internal.protect.api.priv.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.unifi.internal.api.UniFiSession;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Camera;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifi.internal.protect.util.PrivacyZoneHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Exercises privacy transactions with explicitly controlled asynchronous API responses.
 *
 * @author Joseph Matz - Initial contribution
 */
@NonNullByDefault
@Timeout(10)
class PrivacyModeConcurrencyTest {

    private final ManualClient client = new ManualClient();

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    void rapidOnOffWaitsForReadBackAndPreservesIndependentSettings() {
        JsonObject baseline = cameraJson(new JsonArray());
        baseline.addProperty("isMicEnabled", true);
        JsonObject recording = new JsonObject();
        recording.addProperty("mode", "motion");
        baseline.add("recordingSettings", recording);

        CompletableFuture<Camera> on = client.setCameraPrivacyMode("camera1", true);
        CompletableFuture<Camera> off = client.setCameraPrivacyMode("camera1", false);
        assertEquals(1, client.calls.size());
        assertFalse(on.isDone());
        assertFalse(off.isDone());

        client.answer(0, baseline);
        assertEquals(2, client.calls.size());
        Call patch = client.calls.get(1);
        assertEquals(HttpMethod.PATCH, patch.method());
        Map<?, ?> body = assertInstanceOf(Map.class, patch.body());
        assertEquals(List.of("privacyZones"), new ArrayList<>(body.keySet()));
        JsonArray masked = assertInstanceOf(JsonArray.class, body.get("privacyZones"));
        assertEquals(1, masked.size());

        client.answer(1, camera(masked));
        assertEquals(3, client.calls.size(), "OFF must not begin until the ON read-back completes");
        assertFalse(on.isDone());
        client.answer(2, camera(masked));
        assertTrue(PrivacyZoneHelper.isPrivacyModeEnabled(completed(on).privacyZones));
        assertEquals(4, client.calls.size());
        assertEquals(HttpMethod.GET, client.calls.get(3).method());

        client.answer(3, cameraJson(masked));
        assertEquals(5, client.calls.size());
        Map<?, ?> offBody = assertInstanceOf(Map.class, client.calls.get(4).body());
        assertEquals(List.of("privacyZones"), new ArrayList<>(offBody.keySet()));
        JsonArray restored = assertInstanceOf(JsonArray.class, offBody.get("privacyZones"));
        assertTrue(restored.isEmpty());
        client.answer(4, camera(restored));
        assertFalse(off.isDone());
        client.answer(5, camera(restored));
        assertFalse(PrivacyZoneHelper.isPrivacyModeEnabled(completed(off).privacyZones));
        assertEquals(6, client.calls.size());
        assertTrue(baseline.get("isMicEnabled").getAsBoolean());
        assertEquals("motion", baseline.getAsJsonObject("recordingSettings").get("mode").getAsString());
    }

    @Test
    void differentCamerasDoNotBlockEachOther() {
        CompletableFuture<Camera> first = client.setCameraPrivacyMode("camera1", false);
        CompletableFuture<Camera> second = client.setCameraPrivacyMode("camera2", false);
        assertEquals(2, client.calls.size());
        assertEquals("cameras/camera1", client.calls.get(0).path());
        assertEquals("cameras/camera2", client.calls.get(1).path());
        client.answer(1, cameraJson(new JsonArray()));
        assertTrue(second.isDone());
        assertFalse(first.isDone());
        client.answer(0, cameraJson(new JsonArray()));
        assertTrue(first.isDone());
    }

    @Test
    void failedReadDoesNotBlockNextCommand() {
        verifyFailureRecovery(0);
    }

    @Test
    void failedPatchDoesNotBlockNextCommand() {
        verifyFailureRecovery(1);
    }

    @Test
    void failedReadBackDoesNotBlockNextCommand() {
        verifyFailureRecovery(2);
    }

    private void verifyFailureRecovery(int failingCall) {
        CompletableFuture<Camera> first = client.setCameraPrivacyMode("camera1", true);
        CompletableFuture<Camera> second = client.setCameraPrivacyMode("camera1", false);
        if (failingCall > 0) {
            client.answer(0, cameraJson(new JsonArray()));
        }
        if (failingCall > 1) {
            client.answer(1, camera(new JsonArray()));
        }
        IllegalStateException failure = new IllegalStateException("Simulated API failure");
        client.calls.get(failingCall).response().completeExceptionally(failure);
        assertTrue(first.isCompletedExceptionally());
        assertSame(failure, assertThrows(CompletionException.class, first::join).getCause());
        assertEquals(failingCall + 2, client.calls.size());
        client.answer(failingCall + 1, cameraJson(new JsonArray()));
        assertFalse(PrivacyZoneHelper.isPrivacyModeEnabled(completed(second).privacyZones));
    }

    @Test
    void callerCancellationDoesNotReleaseAnInFlightTransaction() {
        CompletableFuture<Camera> first = client.setCameraPrivacyMode("camera1", true);
        CompletableFuture<Camera> second = client.setCameraPrivacyMode("camera1", false);
        first.cancel(false);
        assertEquals(1, client.calls.size());
        client.answer(0, cameraJson(new JsonArray()));
        JsonArray masked = PrivacyZoneHelper.updatePrivacyZones(new JsonArray(), true);
        client.answer(1, camera(masked));
        assertEquals(3, client.calls.size());
        assertFalse(second.isDone());
        client.answer(2, camera(masked));
        assertEquals(4, client.calls.size());
        client.answer(3, cameraJson(new JsonArray()));
        assertTrue(second.isDone());
        assertTrue(first.isCancelled());
    }

    @Test
    void missingZoneArrayFailsWithoutWritingAndQueueRecovers() {
        CompletableFuture<Camera> first = client.setCameraPrivacyMode("camera1", true);
        CompletableFuture<Camera> second = client.setCameraPrivacyMode("camera1", false);
        client.answer(0, new JsonObject());
        assertTrue(first.isCompletedExceptionally());
        assertEquals(2, client.calls.size());
        assertEquals(HttpMethod.GET, client.calls.get(1).method());
        client.answer(1, cameraJson(new JsonArray()));
        assertTrue(second.isDone());
    }

    @Test
    void missingReadBackZonesAreNotReportedAsPrivacyOff() {
        CompletableFuture<Camera> result = client.setCameraPrivacyMode("camera1", true);
        client.answer(0, cameraJson(new JsonArray()));
        client.answer(1, camera(new JsonArray()));
        client.answer(2, new Camera());
        assertTrue(result.isCompletedExceptionally());
    }

    @Test
    void closePreventsQueuedUpdatesAndLateReadFromWriting() {
        CompletableFuture<Camera> first = client.setCameraPrivacyMode("camera1", true);
        CompletableFuture<Camera> second = client.setCameraPrivacyMode("camera1", false);
        client.close();
        assertTrue(second.isCompletedExceptionally());
        client.answer(0, cameraJson(new JsonArray()));
        assertTrue(first.isCompletedExceptionally());
        assertEquals(1, client.calls.size());
        assertTrue(client.setCameraPrivacyMode("camera1", true).isCompletedExceptionally());
    }

    @Test
    void closeDuringPatchPreventsReadBackAndQueuedUpdates() {
        CompletableFuture<Camera> first = client.setCameraPrivacyMode("camera1", true);
        CompletableFuture<Camera> second = client.setCameraPrivacyMode("camera1", false);
        client.answer(0, cameraJson(new JsonArray()));
        client.close();
        client.answer(1, camera(new JsonArray()));
        assertTrue(first.isCompletedExceptionally());
        assertTrue(second.isCompletedExceptionally());
        assertEquals(2, client.calls.size());
    }

    @Test
    void concurrentSubmittersStillHaveOnlyOneTransactionInFlight() throws Exception {
        int count = 12;
        List<Future<CompletableFuture<Camera>>> submitted = new ArrayList<>();
        List<CompletableFuture<Camera>> results = new ArrayList<>();
        CountDownLatch start = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                submitted.add(workers.submit(() -> {
                    assertTrue(start.await(5, TimeUnit.SECONDS));
                    return client.setCameraPrivacyMode("camera1", false);
                }));
            }
            start.countDown();
            for (var task : submitted) {
                results.add(task.get(5, TimeUnit.SECONDS));
            }
            for (int i = 0; i < count; i++) {
                assertEquals(i + 1, client.calls.size());
                client.answer(i, cameraJson(new JsonArray()));
            }
            assertTrue(results.stream().allMatch(CompletableFuture::isDone));
            assertEquals(count, client.calls.size());
        }
    }

    @Test
    void restartUsesCameraMaskAndDoesNotRequireLocalBackups() {
        JsonArray zones = PrivacyZoneHelper.updatePrivacyZones(new JsonArray(), true);
        CompletableFuture<Camera> result = client.setCameraPrivacyMode("camera1", true);
        client.answer(0, cameraJson(zones));
        assertTrue(PrivacyZoneHelper.isPrivacyModeEnabled(completed(result).privacyZones));
        assertEquals(1, client.calls.size(), "An existing mask is a no-op on a new client");
    }

    private static Camera completed(CompletableFuture<Camera> result) {
        assertTrue(result.isDone(), "Controlled responses must have completed the operation");
        return result.join();
    }

    private static JsonObject cameraJson(JsonArray zones) {
        JsonObject camera = new JsonObject();
        camera.add("privacyZones", zones.deepCopy());
        return camera;
    }

    private static Camera camera(JsonArray zones) {
        return Objects.requireNonNull(JsonUtil.getGson().fromJson(cameraJson(zones), Camera.class));
    }

    private record Call(HttpMethod method, String path, @Nullable Object body, Class<?> responseType,
            CompletableFuture<Object> response) {
    }

    private static class ManualClient extends UniFiProtectPrivateClient {
        private final List<Call> calls = new CopyOnWriteArrayList<>();

        ManualClient() {
            super(mock(HttpClient.class), mock(ScheduledExecutorService.class), "https://console",
                    mock(UniFiSession.class));
        }

        @Override
        public <T> CompletableFuture<T> apiRequest(HttpMethod method, String path, @Nullable Object body,
                Class<T> responseType) {
            CompletableFuture<Object> response = new CompletableFuture<>();
            calls.add(new Call(method, path, body, responseType, response));
            return response.thenApply(responseType::cast);
        }

        void answer(int index, Object response) {
            Call call = calls.get(index);
            assertTrue(call.responseType().isInstance(response));
            assertTrue(call.response().complete(response));
        }
    }
}
