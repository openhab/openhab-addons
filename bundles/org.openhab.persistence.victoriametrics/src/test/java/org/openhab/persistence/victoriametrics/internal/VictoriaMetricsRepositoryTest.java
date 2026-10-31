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
package org.openhab.persistence.victoriametrics.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository.VictoriaMetricsRow;

/**
 * @author Michael Weger - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
public class VictoriaMetricsRepositoryTest {
    private static final String URL = "http://localhost:8428/";

    private @Mock HttpClient httpClient;
    private @Mock ContentResponse response;
    private Request request;

    @BeforeEach
    public void setup() throws InterruptedException, TimeoutException, ExecutionException {
        request = mock(Request.class, RETURNS_SELF);
        when(httpClient.newRequest(any(String.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(204);
        when(response.getContentAsString()).thenReturn("");
    }

    private VictoriaMetricsRepository createRepository(Map<String, Object> config) {
        VictoriaMetricsConfiguration configuration = new VictoriaMetricsConfiguration(config);
        VictoriaMetricsMetricNameResolver resolver = new VictoriaMetricsMetricNameResolver(configuration,
                new VictoriaMetricsMetadataService(mock(MetadataRegistry.class)));
        return new VictoriaMetricsRepository(configuration, resolver, httpClient);
    }

    private VictoriaMetricsRepository createRepository() {
        return createRepository(Map.of(VictoriaMetricsConfiguration.URL_PARAM, URL));
    }

    @Test
    public void connectChecksHealthEndpoint() {
        when(response.getStatus()).thenReturn(200);
        VictoriaMetricsRepository repository = createRepository();

        assertThat(repository.connect(), is(true));
        assertThat(repository.isConnected(), is(true));
        verify(httpClient).newRequest("http://localhost:8428/health");
        verify(request).timeout(anyLong(), any(TimeUnit.class));

        repository.disconnect();
        assertThat(repository.isConnected(), is(false));
    }

    @Test
    public void connectFailsOnCommunicationError() throws InterruptedException, TimeoutException, ExecutionException {
        when(request.send()).thenThrow(new ExecutionException(new IOException("Connection refused")));
        VictoriaMetricsRepository repository = createRepository();

        assertThat(repository.connect(), is(false));
        assertThat(repository.isConnected(), is(false));
    }

    @Test
    public void writeUsesPrometheusFormatWithMilliseconds() {
        VictoriaMetricsRepository repository = createRepository();
        Instant time = Instant.ofEpochMilli(1_767_225_600_123L);
        VictoriaMetricsPoint point1 = VictoriaMetricsPoint.newBuilder("Temperature").withTime(time)
                .withValue(new BigDecimal("21.50")).withTag("item", "Temperature").withTag("label", "Say \"hi\"\\")
                .build();
        VictoriaMetricsPoint point2 = VictoriaMetricsPoint.newBuilder("Switch").withTime(time).withValue(1)
                .withTag("item", "Switch").build();

        assertThat(repository.write(List.of(point1, point2)), is(true));

        verify(httpClient).newRequest("http://localhost:8428/api/v1/import/prometheus");
        verify(request).method(HttpMethod.POST);
        assertThat(capturedContent(), is("""
                Temperature{item="Temperature",label="Say \\"hi\\"\\\\"} 21.50 1767225600123
                Switch{item="Switch"} 1 1767225600123
                """));
    }

    @Test
    public void writeIsRetriedOnServerError() {
        when(response.getStatus()).thenReturn(503);
        assertThat(createRepository().write(List.of(createPoint())), is(false));
    }

    @Test
    public void writeIsNotRetriedOnBadRequest() {
        when(response.getStatus()).thenReturn(400);
        assertThat(createRepository().write(List.of(createPoint())), is(true));
    }

    @Test
    public void basicAuthenticationIsUsedIfUserIsSet() {
        createRepository(Map.of(VictoriaMetricsConfiguration.URL_PARAM, URL, VictoriaMetricsConfiguration.USER_PARAM,
                "user", VictoriaMetricsConfiguration.PASSWORD_PARAM, "pass", VictoriaMetricsConfiguration.TOKEN_PARAM,
                "token")).connect();
        verify(request).header(HttpHeader.AUTHORIZATION, "Basic dXNlcjpwYXNz");
    }

    @Test
    public void bearerAuthenticationIsUsedIfTokenIsSet() {
        createRepository(
                Map.of(VictoriaMetricsConfiguration.URL_PARAM, URL, VictoriaMetricsConfiguration.TOKEN_PARAM, "token"))
                .connect();
        verify(request).header(HttpHeader.AUTHORIZATION, "Bearer token");
    }

    @Test
    public void noAuthenticationByDefault() {
        createRepository().connect();
        verify(request, never()).header(eq(HttpHeader.AUTHORIZATION), any());
    }

    @Test
    public void removeDeletesSeriesOfItem() {
        VictoriaMetricsRepository repository = createRepository();
        assertThat(repository.remove(new FilterCriteria().setItemName("MyItem")), is(true));

        verify(httpClient).newRequest("http://localhost:8428/api/v1/admin/tsdb/delete_series");
        verify(request).method(HttpMethod.POST);
        verify(request).param("match[]", "{__name__=\"MyItem\",item=\"MyItem\"}");
    }

    @Test
    public void removeWithTimeRangeOrStateIsRejected() {
        VictoriaMetricsRepository repository = createRepository();

        assertThat(
                repository.remove(
                        new FilterCriteria().setItemName("MyItem").setBeginDate(ZonedDateTime.now(ZoneOffset.UTC))),
                is(false));
        assertThat(
                repository.remove(
                        new FilterCriteria().setItemName("MyItem").setEndDate(ZonedDateTime.now(ZoneOffset.UTC))),
                is(false));
        assertThat(repository.remove(new FilterCriteria().setItemName("MyItem").setState(new DecimalType(1))),
                is(false));
        verifyNoInteractions(httpClient);
    }

    @Test
    public void parseExport() throws IOException {
        String export = """
                {"metric":{"__name__":"MyItem","item":"MyItem"},"values":[1,2.5,1.7e12],"timestamps":[1000,2000,3000]}

                {"metric":{"__name__":"MyItem","item":"MyItem","unit":"°C"},"values":[3,NaN],"timestamps":[4000,5000]}
                """;
        List<VictoriaMetricsRow> rows = createRepository()
                .parseExport(new ByteArrayInputStream(export.getBytes(StandardCharsets.UTF_8)));

        assertThat(rows,
                contains(new VictoriaMetricsRow(Instant.ofEpochMilli(1000), "MyItem", new BigDecimal("1")),
                        new VictoriaMetricsRow(Instant.ofEpochMilli(2000), "MyItem", new BigDecimal("2.5")),
                        new VictoriaMetricsRow(Instant.ofEpochMilli(3000), "MyItem", new BigDecimal("1.7e12")),
                        new VictoriaMetricsRow(Instant.ofEpochMilli(4000), "MyItem", new BigDecimal("3"))));
    }

    @Test
    public void getStoredItemsCount() {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("""
                {"status":"success","data":{"resultType":"vector","result":[
                {"metric":{"item":"ItemA"},"value":[1767225600,"42"]},
                {"metric":{"item":"ItemB"},"value":[1767225600,"7"]}]}}
                """);

        Map<String, Integer> result = createRepository().getStoredItemsCount();

        assertThat(result, is(Map.of("ItemA", 42, "ItemB", 7)));
        verify(httpClient).newRequest("http://localhost:8428/api/v1/query");
        verify(request).param("query", "sum by (item) (count_over_time({item!=\"\"}[365d]))");
    }

    @Test
    public void getStoredItemsCountWithPrefix() {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"status\":\"success\",\"data\":{\"result\":[]}}");

        createRepository(Map.of(VictoriaMetricsConfiguration.URL_PARAM, URL,
                VictoriaMetricsConfiguration.METRIC_PREFIX_PARAM, "openhab_")).getStoredItemsCount();

        verify(request).param("query", "sum by (item) (count_over_time({__name__=~\"openhab_.*\",item!=\"\"}[365d]))");
    }

    @Test
    public void getStoredItemsCountWithUnexpectedResponse() {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("[]");

        assertThat(createRepository().getStoredItemsCount(), is(Map.of()));
    }

    @Test
    public void timestampIsFormattedInSecondsWithMilliseconds() {
        assertThat(VictoriaMetricsRepository.formatTimestamp(Instant.ofEpochMilli(1_767_225_600_123L)),
                is("1767225600.123"));
    }

    private VictoriaMetricsPoint createPoint() {
        return VictoriaMetricsPoint.newBuilder("MyItem").withTime(Instant.now()).withValue(1).withTag("item", "MyItem")
                .build();
    }

    private String capturedContent() {
        ArgumentCaptor<ContentProvider> captor = ArgumentCaptor.forClass(ContentProvider.class);
        verify(request).content(captor.capture());
        StringBuilder content = new StringBuilder();
        for (ByteBuffer buffer : captor.getValue()) {
            content.append(StandardCharsets.UTF_8.decode(buffer));
        }
        return content.toString();
    }
}
