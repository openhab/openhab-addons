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
package org.openhab.binding.semsportal.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.semsportal.internal.dto.Inverter;
import org.openhab.binding.semsportal.internal.dto.SEMSToken;
import org.openhab.binding.semsportal.internal.dto.Station;
import org.openhab.binding.semsportal.internal.dto.StationStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests the communication with the portal, with the responses of the portal replaced by files.
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PortalHandlerTest {
    private static final String STATION = "00000000-0000-0000-0000-0000000000a1";
    private static final String GATEWAY = "https://us-gateway.semsportal.com/web/sems";
    private static final String SESSION_TOKEN = "0123456789abcdef0123456789abcdef";

    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    // the requests that the handler sent, and the responses to give by the last part of the url (the last is repeated)
    private final List<String> requests = Collections.synchronizedList(new ArrayList<>());
    private final List<SEMSToken> tokens = Collections.synchronizedList(new ArrayList<>());
    private final List<String> payloads = Collections.synchronizedList(new ArrayList<>());
    // every login gives a session token of its own, which the portal can then find invalid
    private final AtomicInteger logins = new AtomicInteger();
    private final Set<String> invalidTokens = ConcurrentHashMap.newKeySet();
    private final Map<String, Deque<String>> responses = new HashMap<>();
    // runs while the request is on its way, before the portal answers it, by the last part of the url
    private final Map<String, Runnable> onRequest = new ConcurrentHashMap<>();

    private @NonNullByDefault({}) PortalHandler handler;

    @BeforeEach
    public void setUp() throws IOException {
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        when(bridge.getUID()).thenReturn(new ThingUID("semsportal", "portal", "test"));
        when(bridge.getConfiguration()).thenReturn(new Configuration());
        when(bridge.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        handler = new PortalHandler(bridge, httpClientFactory) {
            @Override
            @Nullable
            String send(HttpMethod method, String url, SEMSToken token, Map<String, String> params,
                    @Nullable String payload) {
                requests.add(method + " " + url);
                tokens.add(token);
                payloads.add(payload == null ? "" : payload);
                String last = url.substring(url.lastIndexOf('/') + 1);
                Runnable hook = onRequest.get(last);
                if (hook != null) {
                    hook.run();
                }
                if (!"cross-login".equals(last) && invalidTokens.contains(token.getToken())) {
                    return "{\"code\": \"C0602\", \"description\": \"expired\"}";
                }
                Deque<String> queue = responses.get(last);
                if (queue == null) {
                    return null;
                }
                String response = queue.size() > 1 ? queue.poll() : queue.peek();
                return "cross-login".equals(last) ? response.replace(SESSION_TOKEN, "token-" + logins.incrementAndGet())
                        : response;
            }
        };
        handler.setCallback(callback);

        respond("cross-login", file("success_login.json"));
        respond("flow", file("success_flow.json"));
        respond("all-status", file("success_devices.json"));
        respond("telecounting", file("success_counting.json"));
        respond("production", file("success_production.json"),
                "{\"code\": \"00000\", \"data\": {\"profitProStats\": 31947.5}}");
        respond("page", file("success_list.json"));
    }

    @Test
    public void testStationStatus() throws Exception {
        StationStatus status = requireStatus();

        assertEquals(2044.0, status.getCurrentOutput(), "kW of the portal is Watt");
        // the counters of two inverters are added up
        assertEquals(18.2, status.getDayTotal(), 0.0001);
        assertEquals(1579.2, status.getMonthTotal(), 0.0001);
        assertEquals(98238.6, status.getOverallTotal(), 0.0001);
        assertEquals(9.1, status.getDayIncome());
        assertEquals(31947.5, status.getTotalIncome());
        assertTrue(status.isOperational(), "One of the inverters is producing");
        assertEquals(ZonedDateTime.of(2026, 11, 1, 10, 44, 0, 0, ZoneId.systemDefault()), status.getLastUpdate());
        verify(callback, atLeastOnce()).statusUpdated(eq(bridge),
                argThat(info -> info.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    public void testRequests() throws Exception {
        handler.getStationStatus(STATION);

        // one login, everything else on the gateway that the login gave, with the session of the login
        assertEquals(List.of("POST https://semsplus.goodwe.com/web/sems/sems-user/api/v1/auth/cross-login",
                "GET " + GATEWAY + "/sems-plant/api/stations/flow",
                "GET " + GATEWAY + "/sems-plant/api/stations/device/all-status",
                "GET " + GATEWAY + "/sems-plant/api/equipments/99999MSU99XX0001/telecounting",
                "GET " + GATEWAY + "/sems-plant/api/equipments/99999MSU99XX0002/telecounting",
                "POST " + GATEWAY + "/sems-plant/api/stations/production",
                "POST " + GATEWAY + "/sems-plant/api/stations/production"), requests);
        assertEquals("semsPlusWeb", tokens.get(0).getClient(), "The login has no session yet, only a client type");
        assertEquals("", tokens.get(0).getToken());
        assertEquals("token-1", tokens.get(1).getToken());
    }

    @Test
    public void testIncomeRequest() throws Exception {
        handler.getStationStatus(STATION);

        List<String> productionRequests = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).endsWith("stations/production")) {
                productionRequests.add(payloads.get(i));
            }
        }
        assertEquals(2, productionRequests.size());
        // the income of today, and since long before the first installations
        assertTrue(productionRequests.get(0).contains("\"dimension\":\"day\""), productionRequests.get(0));
        assertTrue(productionRequests.get(1).contains("\"dimension\":\"year\""), productionRequests.get(1));
        assertTrue(productionRequests.get(1).contains("\"startTime\":\"2000-01-01 00:00:00\""));
        for (String request : productionRequests) {
            assertTrue(request.contains("\"stationId\":\"" + STATION + "\""), request);
            // the portal answers an error when only the income is requested
            assertTrue(request.contains("\"items\":[\"proSystemTotalStats\",\"profitProStats\"]"), request);
        }
    }

    @Test
    public void testRenewalByConcurrentRequestsLogsInOnce() throws Exception {
        handler.getStationStatus(STATION);
        // the session of the first login is not valid anymore, for the requests of both threads
        invalidTokens.add("token-1");
        requests.clear();

        Callable<@Nullable StationStatus> update = () -> handler.getStationStatus(STATION);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (Future<@Nullable StationStatus> result : executor.invokeAll(List.of(update, update), 30,
                    TimeUnit.SECONDS)) {
                assertNotNull(result.get(), "Every request got its status");
            }
        } finally {
            executor.shutdownNow();
        }

        // a second login would have made the session of the first renewal invalid
        assertEquals(1, requests.stream().filter(request -> request.endsWith("cross-login")).count());
        assertEquals(2, logins.get());
    }

    @Test
    public void testNoStatusAfterDispose() throws Exception {
        respond("cross-login", file("error_login.json"));
        handler.dispose();

        assertThrows(CommunicationException.class, () -> handler.getStationStatus(STATION));

        verify(callback, never()).statusUpdated(any(), any());
    }

    @Test
    public void testLoginFinishingAfterDisposeIsDropped() throws Exception {
        // the handler is stopped while the login request is on its way
        onRequest.put("cross-login", () -> handler.dispose());

        assertThrows(CommunicationException.class, () -> handler.getStationStatus(STATION));

        // the session of that login is not kept, and nothing is requested with it
        assertEquals(1, requests.size());
        verify(callback, never()).statusUpdated(any(), any());
    }

    @Test
    public void testNoLoginAfterDisposeWhenTheSessionIsInvalid() throws Exception {
        // the handler is stopped while the request is on its way, and the portal says that the session is not valid
        respond("flow", "{\"code\": \"C0602\", \"description\": \"expired\"}", file("success_flow.json"));
        onRequest.put("flow", () -> handler.dispose());

        assertThrows(CommunicationException.class, () -> handler.getStationStatus(STATION));

        // not logged in again, and the request not repeated on the stopped bridge
        assertEquals(1, requests.stream().filter(request -> request.endsWith("cross-login")).count());
        assertEquals(1, requests.stream().filter(request -> request.endsWith("stations/flow")).count());
    }

    @Test
    public void testSessionIsReused() throws Exception {
        handler.getStationStatus(STATION);
        handler.getStationStatus(STATION);

        assertEquals(1, requests.stream().filter(request -> request.endsWith("cross-login")).count());
    }

    @Test
    public void testTotalsNeedTheCounterOfEveryInverter() throws Exception {
        // the second inverter has no month counter, and its total is not a number
        respond("telecounting", file("success_counting.json"),
                "{\"code\": \"00000\", \"data\": [{\"code\": \"telecounting_today\", \"factors\": "
                        + "[{\"code\": \"proPvStatsToday\", \"data\": \"9.1\"}]}, {\"code\": \"telecounting_total\", "
                        + "\"factors\": [{\"code\": \"proPvStatsTotal\", \"data\": \"n/a\"}]}]}");

        StationStatus status = requireStatus();

        assertEquals(18.2, status.getDayTotal(), 0.0001);
        assertNull(status.getMonthTotal(), "A total without an inverter would drop, and jump back later");
        assertNull(status.getOverallTotal());
    }

    @Test
    public void testOfflineStation() throws Exception {
        respond("all-status", "{\"code\": \"00000\", \"data\": {\"deviceDetailList\": [{\"deviceType\": \"INVERTER\", "
                + "\"statusDetailList\": [{\"status\": -1, \"snList\": [\"99999MSU99XX0002\"]}]}]}}");

        StationStatus status = requireStatus();

        assertFalse(status.isOperational());
        assertEquals(9.1, status.getDayTotal(), "The counters are available while the station is not producing");
    }

    @Test
    public void testExpiredSession() throws Exception {
        respond("flow", "{\"code\": \"C0602\", \"description\": \"expired\"}", file("success_flow.json"));

        requireStatus();
        // logged in at the first request, and again after the portal said that the session is not valid
        assertEquals(2, requests.stream().filter(request -> request.endsWith("cross-login")).count());
        assertEquals(2, requests.stream().filter(request -> request.endsWith("stations/flow")).count());
    }

    @Test
    public void testIncomeIsOptional() throws Exception {
        respond("production", "{\"code\": \"100025\", \"description\": \"no access\"}");

        StationStatus status = requireStatus();

        assertNull(status.getDayIncome());
        assertNull(status.getTotalIncome());
        assertEquals(2044.0, status.getCurrentOutput());
    }

    @Test
    public void testLoginRefusedWhileRequestingTheIncomeStaysVisible() throws Exception {
        // the session is not valid anymore when the income is requested, and the login that follows is refused
        respond("cross-login", file("success_login.json"), file("error_login.json"));
        respond("production", "{\"code\": \"C0602\", \"description\": \"expired\"}");

        StationStatus status = requireStatus();

        assertNull(status.getDayIncome());
        ArgumentCaptor<ThingStatusInfo> info = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(bridge), info.capture());
        ThingStatusInfo last = info.getValue();
        assertEquals(ThingStatus.OFFLINE, last.getStatus(), "Not hidden by the status of the telemetry");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, last.getStatusDetail());
    }

    @Test
    public void testUnknownStation() throws Exception {
        respond("flow", file("error_access.json"));

        assertThrows(ConfigurationException.class, () -> handler.getStationStatus(STATION));
    }

    @Test
    public void testUnknownErrorCode() throws Exception {
        respond("flow", "{\"code\": \"B0001\", \"description\": \"system_execution_error\"}");

        CommunicationException e = assertThrows(CommunicationException.class, () -> handler.getStationStatus(STATION));

        String message = Objects.requireNonNull(e.getMessage());
        assertTrue(message.contains("B0001"), "The message tells the code: " + message);
        assertTrue(message.contains("system_execution_error"), "The message tells the cause: " + message);
    }

    @Test
    public void testSessionStillInvalidAfterLoginAgain() throws Exception {
        respond("flow", "{\"code\": \"C0602\", \"description\": \"expired\"}");

        assertThrows(CommunicationException.class, () -> handler.getStationStatus(STATION));

        // logged in, found the session invalid, logged in again, and gave up until the next interval
        assertEquals(2, requests.stream().filter(request -> request.endsWith("cross-login")).count());
        assertEquals(2, requests.stream().filter(request -> request.endsWith("stations/flow")).count());
    }

    @Test
    public void testWrongCredentials() throws Exception {
        respond("cross-login", file("error_login.json"));

        assertThrows(CommunicationException.class, () -> handler.getStationStatus(STATION));

        ArgumentCaptor<ThingStatusInfo> info = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(bridge), info.capture());
        ThingStatusInfo last = info.getValue();
        assertEquals(ThingStatus.OFFLINE, last.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, last.getStatusDetail());
        assertEquals(1, requests.size(), "No other requests without a session");
    }

    @Test
    public void testLoginRefused() throws Exception {
        respond("cross-login", "{\"code\": \"100004\", \"description\": \"login refused\"}");

        assertThrows(CommunicationException.class, () -> handler.getStationStatus(STATION));

        ArgumentCaptor<ThingStatusInfo> info = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(bridge), info.capture());
        ThingStatusInfo last = info.getValue();
        assertEquals(ThingStatus.OFFLINE, last.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, last.getStatusDetail(),
                "Not the credentials, the next login might work");
        String description = last.getDescription();
        assertTrue(description != null && description.contains("100004") && description.contains("login refused"),
                "The status tells why: " + description);
    }

    @Test
    public void testNoLoginAfterConfigurationError() throws Exception {
        when(bridge.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.OFFLINE,
                ThingStatusDetail.CONFIGURATION_ERROR, "Check username / password"));

        assertNull(handler.getStationStatus(STATION));
        assertTrue(requests.isEmpty(), "A login that was refused is not repeated every interval");
    }

    @Test
    public void testNoResponse() throws Exception {
        responses.remove("flow");

        assertThrows(CommunicationException.class, () -> handler.getStationStatus(STATION));
    }

    @Test
    public void testGetInverters() {
        List<Inverter> inverters = handler.getInverters(STATION);

        assertEquals(2, inverters.size());
        assertEquals("99999MSU99XX0001", inverters.get(0).getSerialNumber());
        assertEquals("GW8500", inverters.get(0).getModel());
    }

    @Test
    public void testGetInvertersWithoutResponse() {
        responses.remove("all-status");

        assertTrue(handler.getInverters(STATION).isEmpty(), "Discovery goes on without the details");
    }

    @Test
    public void testGetAllStations() {
        List<Station> stations = handler.getAllStations();

        assertEquals(1, stations.size());
        assertEquals(STATION, stations.get(0).getStationId());
        assertEquals(List.of("POST https://semsplus.goodwe.com/web/sems/sems-user/api/v1/auth/cross-login",
                "POST " + GATEWAY + "/sems-plant/api/portal/stations/page"), requests);
    }

    @Test
    public void testGetAllStationsWithoutLogin() {
        respond("cross-login", file("error_login.json"));

        assertTrue(handler.getAllStations().isEmpty());
    }

    private StationStatus requireStatus() throws Exception {
        return Objects.requireNonNull(handler.getStationStatus(STATION), "The status of the station");
    }

    private void respond(String lastPartOfUrl, String... bodies) {
        responses.put(lastPartOfUrl, new ArrayDeque<>(List.of(bodies)));
    }

    private String file(String name) {
        try {
            return Files.readString(Paths.get("src/test/resources/" + name));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
