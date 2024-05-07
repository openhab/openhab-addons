/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.iotawatt.internal.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.iotawatt.internal.exception.ThingStatusOfflineException;
import org.openhab.binding.iotawatt.internal.model.StatusResponse;

import com.google.gson.Gson;

/**
 * @author Peter Rosenberg - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class IoTaWattClientTest {
    private static final String DEVICE_STATUS_RESPONSE_FILE = "apiResponses/device-status-response.json";

    @Mock
    @NonNullByDefault({})
    private HttpClient httpClient;
    private final Gson gson = new Gson();

    @Test
    void fetchStatus_whenValidJson_returnObject() throws ThingStatusOfflineException, IOException, ExecutionException,
            InterruptedException, TimeoutException {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        Request request = mock(Request.class);
        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getContentAsString()).thenReturn(readFile(DEVICE_STATUS_RESPONSE_FILE));

        // when
        Optional<StatusResponse> resultOptional = client.fetchStatus();

        // then
        // noinspection OptionalGetWithoutIsPresent
        StatusResponse result = resultOptional.get();
        assertThat(result.inputs().size(), is(2));
        StatusResponse.Input input0 = result.inputs().get(0);
        assertThat(input0.channel(), is(0));
        assertThat(input0.vrms(), is(254.2972F));
        assertThat(input0.hz(), is(50.02768F));
        assertThat(input0.phase(), is(0.92F));
        StatusResponse.Input input1 = result.inputs().get(1);
        assertThat(input1.channel(), is(1));
        assertThat(input1.watts(), is(1.42F));
        assertThat(input1.phase(), is(2.2F));
    }

    @Test
    void fetchStatus_whenWrongHost_throwException() {
        // given
        final IoTaWattClient client = new IoTaWattClient(" ", 10, httpClient, mock(Gson.class));

        // when
        assertThrows(ThingStatusOfflineException.class, client::fetchStatus);
    }

    @Test
    void fetchStatus_whenInputsAndOutputsEmpty_returnEmpty()
            throws ThingStatusOfflineException, ExecutionException, InterruptedException, TimeoutException {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        Request request = mock(Request.class);
        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getContentAsString()).thenReturn("{}");
        final StatusResponse statusResponse = new StatusResponse(null, null);

        // when
        Optional<StatusResponse> resultOptional = client.fetchStatus();

        // then
        // noinspection OptionalGetWithoutIsPresent
        StatusResponse result = resultOptional.get();
        assertNull(result.inputs());
        assertNull(result.outputs());
    }

    private String readFile(String filename) throws IOException {
        final Path workingDir = Path.of("", "src/test/resources");
        final Path file = workingDir.resolve(filename);
        return Files.readString(file);
    }
}
