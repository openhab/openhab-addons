/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feed.test;

import static java.lang.Thread.sleep;
import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openhab.binding.feed.FeedBindingConstants;
import org.openhab.binding.feed.handler.FeedHandler;
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
    private static final int MOCK_SERVLET_PORT = 9090;
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
    private StringType currentItemState = null;

    // Required services for the test
    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService;
    private ThingRegistry thingRegistry;

    private FeedServiceMock servlet;
    private Thing feedThing;
    private FeedHandler feedHandler;
    private ChannelUID channelUID;

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
            feedContent = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(path));
        }
    }

    @Before
    public void setUp() {
        volatileStorageService = new VolatileStorageService();
        registerService(volatileStorageService);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));

        thingRegistry = getService(ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));

        registerFeedTestServlet();
    }

    @After
    public void tearDown() {
        currentItemState = null;
        if (feedThing != null) {
            // Remove the feed thing. The handler will be also disposed automatically
            Thing removedThing = thingRegistry.forceRemove(feedThing.getUID());
            assertThat("The feed thing cannot be deleted", removedThing, is(notNullValue()));
        }

        unregisterFeedTestServlet();

        // Wait for FeedHandler to be unregistered
        waitForAssert(() -> {
            feedHandler = (FeedHandler) feedThing.getHandler();
            assertThat(feedHandler, is(nullValue()));
        });
    }

    private void registerFeedTestServlet() {
        HttpService httpService = getService(HttpService.class);
        assertThat(httpService, is(notNullValue()));
        servlet = new FeedServiceMock(DEFAULT_MOCK_CONTENT);
        try {
            httpService.registerServlet(MOCK_SERVLET_PATH, servlet, null, null);
        } catch (ServletException | NamespaceException e) {
            throw new IllegalStateException("Failed to register feed test servlet", e);
        }
    }

    private void unregisterFeedTestServlet() {
        HttpService httpService = getService(HttpService.class);
        assertThat(httpService, is(notNullValue()));
        httpService.unregister(MOCK_SERVLET_PATH);
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
            assertThat(feedThing.getStatus(), anyOf(is(ONLINE), is(OFFLINE)));
        }, 30000, DFL_SLEEP_TIME);
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
            };

            @Override
            public void stateUpdated(Item item, State state) {
                currentItemState = (StringType) state;
            };
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
            assertThat("Feed Thing can not be initialized", feedThing.getStatus(), is(equalTo(ONLINE)));
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
                    feedThing.getStatus(), is(equalTo(ONLINE)));
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
    @Category(SlowTests.class)
    public void assertThatItemsStateIsNotUpdatedOnAutoRefreshIfContentIsNotChanged()
            throws IOException, InterruptedException {
        boolean commandReceived = false;
        boolean contentChanged = false;
        testIfItemStateIsUpdated(commandReceived, contentChanged);
    }

    @Test
    @Category(SlowTests.class)
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
            assertThat(feedThing.getStatus(), is(equalTo(OFFLINE)));
        });

        servlet.httpStatus = HttpStatus.OK_200;

        // Before this time has expired, the refresh command will no trigger a request to the server
        sleep(FeedBindingConstants.MINIMUM_REFRESH_TIME);

        feedHandler.handleCommand(channelUID, RefreshType.REFRESH);

        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), is(equalTo(ONLINE)));
        });
    }

    @Test
    public void createThingWithInvalidUrlProtocol() {
        String invalidProtocol = "gdfs";
        String invalidURL = generateURLString(invalidProtocol, MOCK_SERVLET_HOSTNAME, MOCK_SERVLET_PORT,
                MOCK_SERVLET_PATH);

        initializeFeedHandler(invalidURL);
        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), is(equalTo(OFFLINE)));
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
            assertThat(feedThing.getStatus(), is(equalTo(OFFLINE)));
            assertThat(feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)));
        });
    }

    @Test
    public void createThingWithInvalidUrlPath() {
        String invalidPath = "/invalid/path";
        String invalidURL = generateURLString(MOCK_SERVLET_PROTOCOL, MOCK_SERVLET_HOSTNAME, MOCK_SERVLET_PORT,
                invalidPath);

        initializeFeedHandler(invalidURL);
        waitForAssert(() -> {
            assertThat(feedThing.getStatus(), is(equalTo(OFFLINE)));
            assertThat(feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)));
        });
    }

}
