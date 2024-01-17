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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Jetty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openhab.binding.http.internal.config.HttpThingConfig;
import org.openhab.binding.http.internal.http.HttpStatusListener;
import org.openhab.binding.http.internal.http.RateLimitedHttpClient;
import org.openhab.binding.http.internal.http.RefreshingUrlCache;
import org.openhab.core.thing.binding.generic.ChannelHandlerContent;

/**
 * The {@link RefreshingUrlCacheTest} implements tests for the {@link RefreshingUrlCache}
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RefreshingUrlCacheTest extends AbstractWireMockTest {
    private static final String TEST_LOCATION = "/testlocation";
    private static final String TEST_CONTENT = "TESTCONTENT";

    private @NonNullByDefault({}) RateLimitedHttpClient rateLimitedHttpClient;
    private @NonNullByDefault({}) HttpThingConfig thingConfig;
    private @NonNullByDefault({}) String url;
    private @NonNullByDefault({}) HttpStatusListener statusListener;

    private final List<@Nullable ChannelHandlerContent> contentWrappers = new CopyOnWriteArrayList<>();

    @BeforeEach
    public void initTest() {
        // this is usually done inside the HttpHandlerFactory when creating the clients
        httpClient.setUserAgentField(null);

        // create a RateLimitedHttpClient
        rateLimitedHttpClient = new RateLimitedHttpClient(httpClient, scheduler);
        rateLimitedHttpClient.setDelay(0);
        statusListener = mock(HttpStatusListener.class);

        // initialize thing config with some default values
        thingConfig = new HttpThingConfig();
        thingConfig.baseURL = "http://localhost:" + port;
        thingConfig.timeout = 500;
        thingConfig.refresh = 1;

        url = thingConfig.baseURL + TEST_LOCATION;
    }

    @AfterEach
    public void cleanUpTest() {
        rateLimitedHttpClient.shutdown();
        contentWrappers.clear();
        super.cleanUpTest();
    }

    @Test
    public void testUpdateOnSuccessfulRequest() {
        stubFor(get(urlEqualTo(TEST_LOCATION)).willReturn(aResponse().withBody(TEST_CONTENT)));

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // wait until we got at least four results or timeout (after 10s)
        waitForAssert(() -> assertEquals(4, contentWrappers.size()));
        urlCache.stop();

        // verify we did not have errors and the number of responses matches the number of success calls
        verify(statusListener, never()).onHttpError(any());
        verify(statusListener, times(contentWrappers.size())).onHttpSuccess();

        // assert all content equals the correct value
        assertTrue(contentWrappers.stream().map(Objects::requireNonNull).map(ChannelHandlerContent::getAsString)
                .allMatch(TEST_CONTENT::equals));
    }

    @Test
    public void testNoUpdateOn404ErrorInNormalMode() {
        stubFor(get(urlEqualTo(TEST_LOCATION)).willReturn(aResponse().withStatus(404)));

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // verify we get at least two error reports in 3s
        verify(statusListener, timeout(3000).atLeast(2)).onHttpError(any());
        verify(statusListener, never()).onHttpSuccess();
        urlCache.stop();

        // assert all content equals the correct value
        assertEquals(true, contentWrappers.isEmpty());
    }

    @Test
    public void testNullUpdateOn404ErrorInStrictMode() {
        stubFor(get(urlEqualTo(TEST_LOCATION)).willReturn(aResponse().withStatus(404)));
        thingConfig.strictErrorHandling = true;

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // verify we get at least two error reports in 3s
        verify(statusListener, timeout(3000).atLeast(2)).onHttpError(any());
        verify(statusListener, never()).onHttpSuccess();
        urlCache.stop();

        int totalErrorCalls = mockingDetails(statusListener).getInvocations().size();

        // assert we have the same number of consumer calls as error calls and all are null
        assertEquals(totalErrorCalls, contentWrappers.size());
        assertEquals(true, contentWrappers.stream().allMatch(Objects::isNull));
    }

    @Test
    public void testNoUpdateOnRequestTimedOutInNormalMode() {
        stubFor(get(urlEqualTo(TEST_LOCATION)).willReturn(aResponse().withFixedDelay(1000).withStatus(200)));

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // verify we get at least two error reports in 3s
        verify(statusListener, timeout(3000).atLeast(2)).onHttpError(any());
        verify(statusListener, never()).onHttpSuccess();
        urlCache.stop();

        // assert all content equals the correct value
        assertEquals(true, contentWrappers.isEmpty());
    }

    @Test
    public void testNullUpdateOnRequestTimedOutInStrictMode() {
        stubFor(get(urlEqualTo(TEST_LOCATION)).willReturn(aResponse().withFixedDelay(1000).withStatus(200)));
        thingConfig.strictErrorHandling = true;

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // verify we get at least two error reports in 3s
        verify(statusListener, timeout(3000).atLeast(2)).onHttpError(any());
        verify(statusListener, never()).onHttpSuccess();
        urlCache.stop();

        int totalErrorCalls = mockingDetails(statusListener).getInvocations().size();

        // assert we have the same number of consumer calls as error calls and all are null
        assertEquals(totalErrorCalls, contentWrappers.size());
        assertEquals(true, contentWrappers.stream().allMatch(Objects::isNull));
    }

    @Test
    public void testAdditionalHeaderIsSentWithRequest() {
        String testHeaderKey = "X-SMARTHOME";
        String testHeaderValue = "TESTVALUE";

        stubFor(get(urlEqualTo(TEST_LOCATION)).willReturn(aResponse()
                .withBody("{{request.headers." + testHeaderKey + "}}").withTransformers("response-template")));
        thingConfig.headers = new ArrayList<>(List.of(testHeaderKey + "=" + testHeaderValue));

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // we need only one answer
        waitForAssert(() -> assertFalse(contentWrappers.isEmpty()));
        urlCache.stop();

        String returnedHeaderValue = Objects.requireNonNull(contentWrappers.get(0)).getAsString();
        assertEquals(testHeaderValue, returnedHeaderValue);
    }

    @Test
    public void testUserAgentIsJettyWhenNotConfigured() {
        stubFor(get(urlEqualTo(TEST_LOCATION)).willReturn(
                aResponse().withBody("{{request.headers.User-Agent}}").withTransformers("response-template")));

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // we need only one answer
        waitForAssert(() -> assertFalse(contentWrappers.isEmpty()));
        urlCache.stop();

        String returnedHeaderValue = Objects.requireNonNull(contentWrappers.get(0)).getAsString();
        assertEquals("Jetty/" + Jetty.VERSION, returnedHeaderValue);
    }

    @Test
    public void testContentSentAlongWithPost() {
        stubFor(post(urlEqualTo(TEST_LOCATION))
                .willReturn(aResponse().withBody("{{request.body}}").withTransformers("response-template")));
        thingConfig.stateMethod = HttpMethod.POST;

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // we need only one answer
        waitForAssert(() -> assertFalse(contentWrappers.isEmpty()));
        urlCache.stop();

        String returnedBody = Objects.requireNonNull(contentWrappers.get(0)).getAsString();
        assertEquals(TEST_CONTENT, returnedBody);
    }

    @Test
    public void testDateIsFormattedInURL() {
        stubFor(get(urlPathEqualTo(TEST_LOCATION))
                .willReturn(aResponse().withBody("{{request.query.date}}").withTransformers("response-template")));
        url += "?date=%1$tY-%1$tm-%1$td";

        RefreshingUrlCache urlCache = getUrlCache(TEST_CONTENT);

        // we need only one answer
        waitForAssert(() -> assertFalse(contentWrappers.isEmpty()));
        urlCache.stop();

        String returnedQueryValue = Objects.requireNonNull(contentWrappers.get(0)).getAsString();
        assertTrue(returnedQueryValue.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * helper method to create a {@link RefreshingUrlCache} and add a test listener
     *
     * @param content HTTP content
     * @return the cache object
     */
    private RefreshingUrlCache getUrlCache(String content) {
        RefreshingUrlCache urlCache = new RefreshingUrlCache(rateLimitedHttpClient, url, thingConfig, content, null,
                statusListener);
        urlCache.addConsumer(contentWrappers::add);
        urlCache.start(scheduler, thingConfig.refresh);
        return urlCache;
    }
}
