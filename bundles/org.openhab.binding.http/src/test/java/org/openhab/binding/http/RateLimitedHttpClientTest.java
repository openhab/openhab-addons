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
package org.openhab.binding.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openhab.binding.http.internal.http.RateLimitedHttpClient;

/**
 * The {@link RateLimitedHttpClientTest} implements tests for the {@link RateLimitedHttpClient}
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RateLimitedHttpClientTest extends AbstractWireMockTest {
    private static final String TEST_LOCATION = "/testlocation";
    private static final String TEST_CONTENT = "TESTCONTENT";

    private List<Response> responses = new CopyOnWriteArrayList<>();

    @AfterEach
    public void cleanUpTest() {
        responses.clear();
        super.cleanUpTest();
    }

    @Test
    public void testWithoutLimit() {
        doLimitTest(0, List.of(false, false));

        // we except to receive the responses in the correct order
        assertEquals(0, responses.get(0).seqNumber);
        assertEquals(1, responses.get(1).seqNumber);

        // we expect a short delay between both requests, but less than 100ms
        long msBetween = responses.get(1).time - responses.get(0).time;
        assertThat((int) msBetween, allOf(greaterThanOrEqualTo(0), lessThan(100)));
    }

    @Test
    public void testWithLimit() {
        doLimitTest(500, List.of(false, false));
        // we except to receive the responses in the correct order
        assertEquals(0, responses.get(0).seqNumber);
        assertEquals(1, responses.get(1).seqNumber);

        // we expect at least 500ms delay between both requests, but less than 500+100=600ms
        long msBetween = responses.get(1).time - responses.get(0).time;
        assertThat((int) msBetween, allOf(greaterThanOrEqualTo(500), lessThan(600)));
    }

    @Test
    public void testWithLimitAndPriority() {
        doLimitTest(500, List.of(false, false, true));

        // we expect to receive the responses of request 3 before request two, exact order of 1 and 3 depends on timing,
        // so accept both
        assertThat(responses.get(0).seqNumber, anyOf(equalTo(0), equalTo(2)));
        assertThat(responses.get(1).seqNumber, anyOf(equalTo(0), equalTo(2)));
        assertNotEquals(responses.get(1).seqNumber, responses.get(0).seqNumber);
        assertEquals(1, responses.get(2).seqNumber);

        // we expect at least 2*500=1000ms delay between the first and last request, but less than 2*500+100=1100 ms
        long msBetween = responses.get(2).time - responses.get(0).time;
        assertThat((int) msBetween, allOf(greaterThanOrEqualTo(1000), lessThan(1100)));
    }

    private void doLimitTest(int setDelay, List<Boolean> config) {
        stubFor(get(urlEqualTo(TEST_LOCATION)).willReturn(aResponse().withBody(TEST_CONTENT)));

        RateLimitedHttpClient rateLimitedHttpClient = new RateLimitedHttpClient(httpClient, scheduler);
        rateLimitedHttpClient.setDelay(setDelay);

        URI url = URI.create("http://localhost:" + port + TEST_LOCATION);
        int seqNumber = 0;

        for (boolean priority : config) {
            int nextSeqNumber = seqNumber++;
            CompletableFuture<Request> requestFuture;

            if (priority) {
                requestFuture = rateLimitedHttpClient.newPriorityRequest(url, HttpMethod.GET, "", null);
            } else {
                requestFuture = rateLimitedHttpClient.newRequest(url, HttpMethod.GET, "", null);
            }

            requestFuture.thenAccept(request -> {
                try {
                    responses.add(new Response(nextSeqNumber, request.send()));
                } catch (Exception e) {
                }
            });
        }

        // wait until we got all results
        waitForAssert(() -> assertEquals(config.size(), responses.size()));
        rateLimitedHttpClient.shutdown();
    }

    private static class Response {
        public final int seqNumber;
        public final long time = System.currentTimeMillis();
        public final String content;

        public Response(int seqNumber, ContentResponse contentResponse) {
            this.seqNumber = seqNumber;
            this.content = contentResponse.getContentAsString();
        }
    }
}
