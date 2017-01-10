/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feed.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.custommonkey.xmlunit.*
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.StateChangeListener
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.types.StringType
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingTypeMigrationService
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.openhab.binding.feed.FeedBindingConstants
import org.openhab.binding.feed.handler.FeedHandler
import org.openhab.binding.feed.internal.FeedHandlerFactory
import org.osgi.service.http.HttpService

/**
 * Tests for {@link FeedHandler}
 *
 * @author Svilen Valkanov
 */

public class FeedHandlerTest extends OSGiTest {

    //Servlet URL configuration
    def MOCK_SERVLET_PROTOCOL = 'http'
    def MOCK_SERVLET_HOSTNAME = 'localhost'
    def MOCK_SERVLET_PORT = 9090
    def MOCK_SERVLET_PATH = '/test/feed';

    //Files used for the test as input. They are located in /src/test/resources directory
    /**
     * The default mock content in the test is RSS 2.0 format, as this is the most popular format
     */
    def DEFAULT_MOCK_CONTENT = 'rss_2.0.xml'

    /**
     * One new entry is added to {@link #DEFAULT_MOCK_CONTENT}
     */
    def MOCK_CONTENT_CHANGED = 'rss_2.0_changed.xml'

    def ITEM_NAME = 'testItem'
    def THING_NAME = 'testFeedThing'


    int DEFAULT_MAX_WAIT_TIME = 6000

    /**
     * Default auto refresh interval for the test is 1 Minute.
     */
    int DEFAULT_TEST_AUTOREFRESH_TIME = 1

    /**
     * It is updated from mocked {@link StateChangeListener#stateUpdated() }
     */
    def currentItemState = null

    //Required services for the test
    private ManagedThingProvider managedThingProvider
    private VolatileStorageService volatileStorageService
    private ThingRegistry thingRegistry

    private FeedServiceMock servlet
    private Thing feedThing;
    private FeedHandler feedHandler = null;
    private ChannelUID channelUID = null

    /**
     * This class is used as a mock for HTTP web server, serving XML feed content.
     */
    class FeedServiceMock extends HttpServlet {
        def feedContent;
        def httpStatus;

        public FeedServiceMock(def feedContentFile) {
            super()
            setFeedContent(feedContentFile)
            //By default the servlet returns HTTP Status code 200 OK
            this.httpStatus = HttpStatus.OK_200
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
            ServletOutputStream outputStream = response.getOutputStream()
            outputStream.println(feedContent)
            //Recommended RSS MIME type - http://www.rssboard.org/rss-mime-type-application.txt
            //Atom MIME type is  - application/atom+xml
            //Other MIME types - text/plan, text/xml, text/html are tested and accepted as well
            response.setContentType("application/rss+xml")
            response.setStatus(httpStatus)
        }

        public void setFeedContent(String feedContentFile) {
            def path = "input/${feedContentFile}"
            feedContent = getClass().getClassLoader().getResourceAsStream(path).getText();
        }

    }

    private void registerFeedTestServlet (){
        HttpService httpService = getService(HttpService)
        assertThat httpService,is(notNullValue())
        servlet = new FeedServiceMock(DEFAULT_MOCK_CONTENT);
        httpService.registerServlet(MOCK_SERVLET_PATH, servlet, null, null)
    }

    private void unregisterFeedTestServlet(){
        HttpService httpService = getService(HttpService)
        assertThat httpService,is(notNullValue())
        httpService.unregister(MOCK_SERVLET_PATH)
        servlet=null
    }

    @Before
    public void setUp() {
        volatileStorageService = new VolatileStorageService()
        registerService(volatileStorageService)

        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())

        thingRegistry = getService(ThingRegistry)
        assertThat thingRegistry, is(notNullValue())

        registerFeedTestServlet();
    }

    private String generateURLString(String protocol,String hostname,int port,String path) {
        def String mockServletURL = "$protocol://$hostname:$port$path";
        return mockServletURL
    }

    private void initializeDefaultFeedHandler (){
        def mockServletURL = generateURLString(MOCK_SERVLET_PROTOCOL,MOCK_SERVLET_HOSTNAME,MOCK_SERVLET_PORT,MOCK_SERVLET_PATH)
        //one minute update time is used for the tests
        def defaultTestRefreshInterval = new BigDecimal(DEFAULT_TEST_AUTOREFRESH_TIME)
        initializeFeedHandler(mockServletURL,defaultTestRefreshInterval)
    }

    private void initializeFeedHandler (String URL) {
        initializeFeedHandler(URL,null)
    }

    private void initializeFeedHandler(String URL,BigDecimal refreshTime) {
        //set up configuration
        Configuration configuration = new Configuration()
        configuration.put((FeedBindingConstants.URL), URL)
        configuration.put((FeedBindingConstants.REFRESH_TIME),refreshTime)

        //create Feed Thing
        ThingUID feedUID = new ThingUID(FeedBindingConstants.FEED_THING_TYPE_UID,THING_NAME)
        channelUID = new ChannelUID(feedUID,FeedBindingConstants.CHANNEL_LATEST_DESCRIPTION)
        Channel channel = new Channel(channelUID,"String")
        feedThing = ThingBuilder.create(FeedBindingConstants.FEED_THING_TYPE_UID, feedUID).withConfiguration(configuration).withChannel(channel).build()

        managedThingProvider.add(feedThing)

        //wait for FeedHandler to be registered
        waitForAssert({
            feedHandler = getThingHandler(ThingHandler.class)
            assertThat "FeedHandler is not registered",feedHandler, is(notNullValue())
        },  DEFAULT_MAX_WAIT_TIME)

        //This will ensure that the configuration is read before the channelLinked() method in FeedHandler is called !
        waitForAssert({
            feedThing.getStatus() == ThingStatus.ONLINE;
        })
        initializeItem(channelUID)
    }

    private void initializeItem (ChannelUID channelUID){
        //Create new item
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        assertThat itemRegistry, is(notNullValue())

        StringItem newItem = new StringItem(ITEM_NAME)

        // Add item state change listener
        def updateListener = [
            stateUpdated: { item, state ->
                currentItemState = state
            },
            stateChanged: { item, oldState, state ->
            }
        ] as StateChangeListener

        newItem.addStateChangeListener(updateListener)
        itemRegistry.add(newItem)

        //Add item channel link
        def ManagedItemChannelLinkProvider itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
        assertThat itemChannelLinkProvider,is(notNullValue())
        itemChannelLinkProvider.add(new ItemChannelLink(ITEM_NAME,channelUID))
    }

    private void testIfItemStateIsUpdated (boolean commandReceived, boolean contentChanged) {
        initializeDefaultFeedHandler()

        waitForAssert({
            assertThat "Feed Thing can not be initialized",feedThing.getStatus(),is(equalTo(ThingStatus.ONLINE))
            assertThat "Item's state is not updated on initialize", currentItemState, is(notNullValue())
        },  DEFAULT_MAX_WAIT_TIME)

        assertThat currentItemState,is(StringType)
        String firstItemState = currentItemState;

        if(contentChanged) {
            //The content on the mocked server should be changed
            servlet.setFeedContent(MOCK_CONTENT_CHANGED)
        }

        if(commandReceived){
            //Before this time has expired, the refresh command will no trigger a request to the server
            sleep(FeedBindingConstants.MINIMUM_REFRESH_TIME)

            feedThing.handler.handleCommand(channelUID,RefreshType.REFRESH)
        } else {
            //The auto refresh task will handle the update after the default wait time
            sleep(DEFAULT_TEST_AUTOREFRESH_TIME*60*1000)
        }

        waitForAssert({
            assertThat "Error occurred while trying to connect to server. Content is not downloaded!",
                    feedThing.getStatus(),is(equalTo(ThingStatus.ONLINE))
        }, DEFAULT_MAX_WAIT_TIME)

        waitForAssert({
            if(contentChanged) {
                assertThat "Content is not updated!",currentItemState,not(equalTo(firstItemState))
            } else {
                assertThat currentItemState,is(equalTo(firstItemState))
            }
        },DEFAULT_MAX_WAIT_TIME)
    }

    @Test
    public void 'assert that invalid configuration falls back to default values'() {
        def String mockServletURL = generateURLString(MOCK_SERVLET_PROTOCOL,MOCK_SERVLET_HOSTNAME,MOCK_SERVLET_PORT,MOCK_SERVLET_PATH)
        def defaultTestRefreshInterval = new BigDecimal(-10)
        initializeFeedHandler(mockServletURL,defaultTestRefreshInterval)
    }

    @Test
    public void 'assert that item\'s state is updated on refresh command if content changed' () {
        boolean commandReceived= true;
        boolean contentChanged = true;
        testIfItemStateIsUpdated(commandReceived,contentChanged);
    }

    @Test
    public void 'assert that item\'s state is not updated on refresh command if content is not changed' () {
        boolean commandReceived= true;
        boolean contentChanged = false;
        testIfItemStateIsUpdated(commandReceived,contentChanged);
    }

    @Category(SlowTests.class)
    @Test
    public void 'assert that item\'s state is not updated on auto refresh if content is not changed' () {
        boolean commandReceived= false;
        boolean contentChanged = false;
        testIfItemStateIsUpdated(commandReceived,contentChanged);
    }

    @Category(SlowTests.class)
    @Test
    public void 'assert that item\'s state is updated on auto refresh if content changed' () {
        boolean commandReceived= false;
        boolean contentChanged = true;
        testIfItemStateIsUpdated(commandReceived,contentChanged);
    }

    @Test
    public void 'assert that thing\'s status is updated when HTTP 500 error code is received' () {
        testIfThingStatusIsUpdated(HttpStatus.INTERNAL_SERVER_ERROR_500)
    }

    @Test
    public void 'assert that thing\'s status is updated when HTTP 401 error code is received' () {
        testIfThingStatusIsUpdated(HttpStatus.UNAUTHORIZED_401)
    }

    @Test
    public void 'assert that thing\'s status is updated when HTTP 403 error code is received' () {
        testIfThingStatusIsUpdated(HttpStatus.FORBIDDEN_403)
    }

    @Test
    public void 'assert that thing\'s status is updated when HTTP 404 error code is received' () {
        testIfThingStatusIsUpdated(HttpStatus.NOT_FOUND_404)
    }


    private void testIfThingStatusIsUpdated (Integer serverStatus) {

        initializeDefaultFeedHandler()

        servlet.httpStatus = serverStatus

        //Before this time has expired, the refresh command will no trigger a request to the server
        sleep(FeedBindingConstants.MINIMUM_REFRESH_TIME)

        //invalid channel UID is used for the test, because otherwise
        feedThing.handler.handleCommand(channelUID,RefreshType.REFRESH)

        waitForAssert({
            assertThat feedThing.getStatus(),is(equalTo(ThingStatus.OFFLINE))
        },DEFAULT_MAX_WAIT_TIME)

        servlet.httpStatus = HttpStatus.OK_200

        //Before this time has expired, the refresh command will no trigger a request to the server
        sleep(FeedBindingConstants.MINIMUM_REFRESH_TIME)

        feedThing.handler.handleCommand(channelUID,RefreshType.REFRESH)

        waitForAssert({
            assertThat feedThing.getStatus(),is(equalTo(ThingStatus.ONLINE))
        },DEFAULT_MAX_WAIT_TIME)
    }

    @Test
    public void 'create thing with invalid URL protocol' () {
        def invalidProtocol = "gdfs"
        def invalidURL = generateURLString(invalidProtocol,MOCK_SERVLET_HOSTNAME,MOCK_SERVLET_PORT,MOCK_SERVLET_PATH)

        initializeFeedHandler(invalidURL)
        waitForAssert({
            assertThat feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE))
            assertThat feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR))
        },DEFAULT_MAX_WAIT_TIME)
    }

    @Test
    public void 'create thing with invalid URL hostname' () {
        def invalidHostname = "invalidhost"
        def invalidURL = generateURLString(MOCK_SERVLET_PROTOCOL,invalidHostname,MOCK_SERVLET_PORT,MOCK_SERVLET_PATH)

        initializeFeedHandler(invalidURL)
        waitForAssert({
            assertThat feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE))
            assertThat feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR))
        },DEFAULT_MAX_WAIT_TIME)
    }

    @Test
    public void 'create thing with invalid URL path' () {
        def invalidPath = "/invalid/path"
        def invalidURL = generateURLString(MOCK_SERVLET_PROTOCOL,MOCK_SERVLET_HOSTNAME,MOCK_SERVLET_PORT,invalidPath)

        initializeFeedHandler(invalidURL)
        waitForAssert({
            assertThat feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE))
            assertThat feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR))
        },DEFAULT_MAX_WAIT_TIME)
    }


    @After
    public void tearDown() {
        currentItemState = null
        if(feedThing != null){
            // Remove the feed thing. The handler will be also disposed automatically
            Thing removedThing = thingRegistry.remove(feedThing.getUID())
            assertThat("The feed thing cannot be deleted",removedThing,is(notNullValue()))
        }

        unregisterFeedTestServlet();

        // wait for FeedHandler to be unregistered
        waitForAssert({
            feedHandler = getThingHandler(ThingHandler.class)
            assertThat feedHandler, is(nullValue())
        }, DEFAULT_MAX_WAIT_TIME)
    }

    /**
     * Gets a thing handler of a specific type.
     *
     * @param clazz type of thing handler
     *
     * @return the thing handler
     */
    protected <T extends ThingHandler> T getThingHandler(Class<T> clazz){
        FeedHandlerFactory factory
        waitForAssert{
            factory = getService(ThingHandlerFactory, FeedHandlerFactory)
            assertThat factory, is(notNullValue())
        }
        def handlers = getThingHandlers(factory)

        for(ThingHandler handler : handlers) {
            if(clazz.isInstance(handler)) {
                return handler
            }
        }
        return null
    }

    private Set<ThingHandler> getThingHandlers(FeedHandlerFactory factory) {
        def thingManager = getService(ThingTypeMigrationService.class, { "org.eclipse.smarthome.core.thing.internal.ThingManager" } )
        assertThat thingManager, not(null)
        thingManager.thingHandlersByFactory.get(factory)
    }
}
