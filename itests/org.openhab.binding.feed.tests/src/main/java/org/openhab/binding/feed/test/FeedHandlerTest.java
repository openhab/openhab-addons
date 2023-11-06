/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.feed.test;

import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.feed.internal.FeedBindingConstants;
import org.openhab.binding.feed.internal.handler.FeedHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.StateChangeListener;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.StringType;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ManagedItemChannelLinkProvider;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * Tests for {@link FeedHandler}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Wouter Born - Migrate Groovy to Java tests
 */
public class FeedHandlerTest extends JavaOSGiTest {

    // Servlet URL configuration
    private static final String MOCK_SERVLET_PROTOCOL = "http";
    private static final String MOCK_SERVLET_HOSTNAME = "localhost";
    private static final int MOCK_SERVLET_PORT = Integer.getInteger("org.osgi.service.http.port", 8080);
    private static final String MOCK_SERVLET_PATH = "/test/feed";

    // Files used for the test as input. They are located in /src/test/resources directory
    /**
     * The default mock content in the test is RSS 2.0 format, as this is the most popular format
     */
    private static final String DEFAULT_MOCK_CONTENT = "rss_2.0.xml";

    /**
     * One new entry is added to {@link #DEFAULT_MOCK_CONTENT}
     */
    private static final String MOCK_CONTENT_CHANGED = "rss_2.0_changed.xml";

    private static final String ITEM_NAME = "testItem";
    private static final String THING_NAME = "testFeedThing";

    /**
     * Default auto refresh interval for the test is 1 Minute.
     */
    private static final int DEFAULT_TEST_AUTOREFRESH_TIME = 1;

    /**
     * It is updated from mocked {@link StateChangeListener#stateUpdated() }
     */
    private StringType currentItemState;

    // Required services for the test
    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService;
    private ThingRegistry thingRegistry;

    private FeedServiceMock servlet;
    private Thing feedThing;
    private FeedHandler feedHandler;
    private ChannelUID channelUID;
    private HttpService httpService;

    /**
     * This class is used as a mock for HTTP web server, serving XML feed content.
     */
    class FeedServiceMock extends HttpServlet {
        private static final long serialVersionUID = -7810045624309790473L;

        String feedContent;
        int httpStatus;

        public FeedServiceMock(String feedContentFile) {
            super();
            try {
                setFeedContent(feedContentFile);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error loading feed content from: " + feedContentFile);
            }
            // By default the servlet returns HTTP Status code 200 OK
            this.httpStatus = HttpStatus.OK_200;
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.getOutputStream().println(feedContent);
            // Recommended RSS MIME type - http://www.rssboard.org/rss-mime-type-application.txt
            // Atom MIME type is - application/atom+xml
            // Other MIME types - text/plan, text/xml, text/html are tested and accepted as well
            response.setContentType("application/rss+xml");
            response.setStatus(httpStatus);
        }

        public void setFeedContent(String feedContentFile) throws IOException {
            String path = "input/" + feedContentFile;
            feedContent = new String(getClass().getClassLoader().getResourceAsStream(path).readAllBytes(),
                    StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    public void setUp() {
        volatileStorageService = new VolatileStorageService();
        registerService(volatileStorageService);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));

        thingRegistry = getService(ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));

        registerFeedTestServlet();
    }

    @AfterEach
    public void tearDown() {
        currentItemState = null;
        if (feedThing != null) {
            // Remove the feed thing. The handler will be also disposed automatically
            Thing removedThing = thingRegistry.forceRemove(feedThing.getUID());
            assertThat("The feed thing cannot be deleted", removedThing, is(notNullValue()));
        }

        unregisterFeedTestServlet();

        if (feedThing != null) {
            // Wait for FeedHandler to be unregistered
            waitForAssert(() -> {
                feedHandler = (FeedHandler) feedThing.getHandler();
                assertThat(feedHandler, is(nullValue()));
            });
        }
    }

    private synchronized void registerFeedTestServlet() {
        waitForAssert(() -> assertThat(httpService = getService(HttpService.class), is(notNullValue())));
        servlet = new FeedServiceMock(DEFAULT_MOCK_CONTENT);
        try {
            httpService.registerServlet(MOCK_SERVLET_PATH, servlet, null, null);
        } catch (ServletException | NamespaceException e) {
            throw new IllegalStateException("Failed to register feed test servlet", e);
        }
    }

    private synchronized void unregisterFeedTestServlet() {
        waitForAssert(() -> assertThat(httpService = getService(HttpService.class), is(notNullValue())));
        try {
            httpService.unregister(MOCK_SERVLET_PATH);
        } catch (IllegalArgumentException ignore) {
        }
        servlet = null;
    }

    private String generateURLString(String protocol, String hostname, int port, String path) {
        return protocol + "://" + hostname + ":" + port + path;
    }

    private void initializeDefaultFeedHandler() {
        String mockServletURL = generateURLString(MOCK_SERVLET_PROTOCOL, MOCK_SERVLET_HOSTNAME, MOCK_SERVLET_PORT,
                MOCK_SERVLET_PATH);
        // One minute update time is used for the tests
        BigDecimal defaultTestRefreshInterval = new BigDecimal(DEFAULT_TEST_AUTOREFRESH_TIME);
        initializeFeedHandler(mockServletURL, defaultTestRefreshInterval);
    }

    private void initializeFeedHandler(String url) {
        initializeFeedHandler(url, null);
    }

    private void initializeFeedHandler(String url, BigDecimal refreshTime) {
        // Set up configuration
        Configuration configuration = new Configuration();
        configuration.put((FeedBindingConstants.URL), url);
        configuration.put((FeedBindingConstants.REFRESH_TIME), refreshTime);

        // Create Feed Thing
        ThingUID feedUID = new ThingUID(FeedBindingConstants.FEED_THING_TYPE_UID, THING_NAME);
        channelUID = new ChannelUID(feedUID, FeedBindingConstants.CHANNEL_LATEST_DESCRIPTION);
        Channel channel = ChannelBuilder.create(channelUID, "String").build();
        feedThing = ThingBuilder.create(FeedBindingConstants.FEED_THING_TYPE_UID, feedUID)
                .withConfiguration(configuration).withChannel(channel).build();

        managedThingProvider.add(feedThing);

        // Wait for FeedHandler to be registered
        waitForAssert(() -> {
            feedHandler = (FeedHandler) feedThing.getHandler();
            assertThat("FeedHandler is not registered", feedHandler, is(notNullValue()));
        });

        // This will ensure that the configuration is read before the channelLinked() method in FeedHandler is called !
        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), anyOf(is(ThingStatus.ONLINE), is(ThingStatus.OFFLINE)));
        }, 60000, DFL_SLEEP_TIME);
        initializeItem(channelUID);
    }

    private void initializeItem(ChannelUID channelUID) {
        // Create new item
        ItemRegistry itemRegistry = getService(ItemRegistry.class);
        assertThat(itemRegistry, is(notNullValue()));

        StringItem newItem = new StringItem(ITEM_NAME);

        // Add item state change listener
        StateChangeListener updateListener = new StateChangeListener() {
            @Override
            public void stateChanged(Item item, State oldState, State newState) {
            }

            @Override
            public void stateUpdated(Item item, State state) {
                currentItemState = (StringType) state;
            }
        };

        newItem.addStateChangeListener(updateListener);
        itemRegistry.add(newItem);

        // Add item channel link
        ManagedItemChannelLinkProvider itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider.class);
        assertThat(itemChannelLinkProvider, is(notNullValue()));
        itemChannelLinkProvider.add(new ItemChannelLink(ITEM_NAME, channelUID));
    }

    private void testIfItemStateIsUpdated(boolean commandReceived, boolean contentChanged)
            throws IOException, InterruptedException {
        initializeDefaultFeedHandler();

        waitForAssert(() -> {
            assertThat("Feed Thing can not be initialized", feedThing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
            assertThat("Item's state is not updated on initialize", currentItemState, is(notNullValue()));
        });

        assertThat(currentItemState, is(instanceOf(StringType.class)));
        StringType firstItemState = currentItemState;

        if (contentChanged) {
            // The content on the mocked server should be changed
            servlet.setFeedContent(MOCK_CONTENT_CHANGED);
        }

        if (commandReceived) {
            // Before this time has expired, the refresh command will no trigger a request to the server
            sleep(FeedBindingConstants.MINIMUM_REFRESH_TIME);

            feedHandler.handleCommand(channelUID, RefreshType.REFRESH);
        } else {
            // The auto refresh task will handle the update after the default wait time
            sleep(DEFAULT_TEST_AUTOREFRESH_TIME * 60 * 1000);
        }

        waitForAssert(() -> {
            assertThat("Error occurred while trying to connect to server. Content is not downloaded!",
                    feedThing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        });

        waitForAssert(() -> {
            if (contentChanged) {
                assertThat("Content is not updated!", currentItemState, not(equalTo(firstItemState)));
            } else {
                assertThat(currentItemState, is(equalTo(firstItemState)));
            }
        });
    }

    @Test
    public void assertThatInvalidConfigurationFallsBackToDefaultValues() {
        String mockServletURL = generateURLString(MOCK_SERVLET_PROTOCOL, MOCK_SERVLET_HOSTNAME, MOCK_SERVLET_PORT,
                MOCK_SERVLET_PATH);
        BigDecimal defaultTestRefreshInterval = new BigDecimal(-10);
        initializeFeedHandler(mockServletURL, defaultTestRefreshInterval);
    }

    @Test
    public void assertThatItemsStateIsNotUpdatedOnAutoRefreshIfContentIsNotChanged()
            throws IOException, InterruptedException {
        boolean commandReceived = false;
        boolean contentChanged = false;
        testIfItemStateIsUpdated(commandReceived, contentChanged);
    }

    @Test
    public void assertThatItemsStateIsUpdatedOnAutoRefreshIfContentChanged() throws IOException, InterruptedException {
        boolean commandReceived = false;
        boolean contentChanged = true;
        testIfItemStateIsUpdated(commandReceived, contentChanged);
    }

    @Test
    public void assertThatThingsStatusIsUpdatedWhenHTTP500ErrorCodeIsReceived() throws InterruptedException {
        testIfThingStatusIsUpdated(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void assertThatThingsStatusIsUpdatedWhenHTTP401ErrorCodeIsReceived() throws InterruptedException {
        testIfThingStatusIsUpdated(HttpStatus.UNAUTHORIZED_401);
    }

    @Test
    public void assertThatThingsStatusIsUpdatedWhenHTTP403ErrorCodeIsReceived() throws InterruptedException {
        testIfThingStatusIsUpdated(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void assertThatThingsStatusIsUpdatedWhenHTTP404ErrorCodeIsReceived() throws InterruptedException {
        testIfThingStatusIsUpdated(HttpStatus.NOT_FOUND_404);
    }

    private void testIfThingStatusIsUpdated(Integer serverStatus) throws InterruptedException {
        initializeDefaultFeedHandler();

        servlet.httpStatus = serverStatus;

        // Before this time has expired, the refresh command will no trigger a request to the server
        sleep(FeedBindingConstants.MINIMUM_REFRESH_TIME);

        // Invalid channel UID is used for the test, because otherwise
        feedHandler.handleCommand(channelUID, RefreshType.REFRESH);

        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        });

        servlet.httpStatus = HttpStatus.OK_200;

        // Before this time has expired, the refresh command will no trigger a request to the server
        sleep(FeedBindingConstants.MINIMUM_REFRESH_TIME);

        feedHandler.handleCommand(channelUID, RefreshType.REFRESH);

        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        });
    }

    @Test
    public void createThingWithInvalidUrlProtocol() {
        String invalidProtocol = "gdfs";
        String invalidURL = generateURLString(invalidProtocol, MOCK_SERVLET_HOSTNAME, MOCK_SERVLET_PORT,
                MOCK_SERVLET_PATH);

        initializeFeedHandler(invalidURL);
        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
        });
    }

    @Test
    public void createThingWithInvalidUrlHostname() {
        String invalidHostname = "invalidhost";
        String invalidURL = generateURLString(MOCK_SERVLET_PROTOCOL, invalidHostname, MOCK_SERVLET_PORT,
                MOCK_SERVLET_PATH);

        initializeFeedHandler(invalidURL);
        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)));
        }, 30000, DFL_SLEEP_TIME);
    }

    @Test
    public void createThingWithInvalidUrlPath() {
        String invalidPath = "/invalid/path";
        String invalidURL = generateURLString(MOCK_SERVLET_PROTOCOL, MOCK_SERVLET_HOSTNAME, MOCK_SERVLET_PORT,
                invalidPath);

        initializeFeedHandler(invalidURL);
        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat(feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)));
        });
    }
}
