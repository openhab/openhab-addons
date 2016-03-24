/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feed.test

import static org.apache.commons.httpclient.HttpStatus.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.httpclient.HttpStatus
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.StateChangeListener
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.types.StringType
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openhab.binding.feed.FeedBindingConstants
import org.openhab.binding.feed.handler.FeedHandler
import org.osgi.service.event.Event
import org.osgi.service.event.EventConstants
import org.osgi.service.event.EventHandler
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
    def MOCK_SERVLET_PORT = '9090'
    def MOCK_SERVLET_PATH = '/test/feed';

    //Servlet content paths
    def MOCK_CONTENT_ROOT = 'src/test/resources/'
    def MOCK_CONTENT_URI = 'src/test/resources/mockContent.xml'
    def MOCK_CONTENT_CHANGED_URI = 'src/test/resources/mockContentChanged.xml'
    def MOCK_CONTENT_UNESCAPED_URI = 'src/test/resources/mockContentUnescaped.xml'
    def MOCK_CONTENT_EMPTY_URI = 'src/test/resources/mockContentEmpty.xml'
    def MOCK_CONTENT_INVALID_XML_URI = 'src/test/resources/mockContentInvalid.xml'
    def MOCK_CONTENT_MISSING_XML_DECLARATION_URI = 'src/test/resources/mockContentMissingXmlDeclaration.xml'

    def ITEM_NAME = 'testItem'
    def THING_NAME = 'testFeedThing'

    int DEFAULT_MAX_WAIT_TIME = 3000

    /**
     * Default auto refresh interval for the test is 1 Minute.
     */
    int DEFAULT_AUTOREFRESH_TIME = 60 * 1000

    /**
     * It is updated from mocked {@link StateChangeListener#stateUpdated() }
     */
    def currentItemState = null

    /**
     * It is updated from mocked {@link FeedStatusChangeListner#handleEvent(Event event)}
     */
    boolean isThingStatusChanged = false

    //Required services for the test
    private ManagedThingProvider managedThingProvider
    private VolatileStorageService volatileStorageService
    private ThingRegistry thingRegistry
    private EventPublisher eventPublisher

    private FeedServiceMock servlet
    private Thing feedThing;
    private FeedHandler feedHandler = null;
    private ChannelUID channelUID =  null

    /**
     * This class is used as a mock for HTTP web server, serving XML feed content.
     */
    class FeedServiceMock extends HttpServlet {
        def feedContent;
        def httpStatus;

        public FeedServiceMock(def feedContent) {
            super()
            this.feedContent = new File(feedContent).text;
            //By default the servlet returns HTTP Status code 200 OK
            this.httpStatus = HttpStatus.SC_OK
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
            ServletOutputStream outputStream = response.getOutputStream()
            outputStream.println(feedContent)
            response.setContentType("text/xml")
            response.setStatus(httpStatus)
        }

        public void setFeedContent(String URI) {
            feedContent = new File(URI).text;
        }

    }

    class FeedStatusChangeListner implements EventHandler {

        @Override
        public void handleEvent(Event event) {
            isThingStatusChanged = true;
        }

    }

    private void registerFeedTestServlet (){
        HttpService httpService = getService(HttpService)
        assertThat httpService,is(notNullValue())
        servlet = new FeedServiceMock(MOCK_CONTENT_URI);
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

        eventPublisher = getService(EventPublisher)
        assertThat eventPublisher, is(notNullValue())

        registerFeedTestServlet();
        println ("Set Up finished");
    }

    private void initializeDefaultFeedHandler (){
        def String mockServletURL = "$MOCK_SERVLET_PROTOCOL://$MOCK_SERVLET_HOSTNAME:$MOCK_SERVLET_PORT$MOCK_SERVLET_PATH";
        def defaultFormat = FeedBindingConstants.DEFAULT_FEED_FORMAT
        //one minute update time is used for the tests
        def defaultTestRefreshInterval = new BigDecimal(1)
        def defaultNumberOfEntries = new BigDecimal(FeedBindingConstants.DEFAULT_NUMBER_OF_ENTRIES)
        initializeFeedHandler(mockServletURL,defaultTestRefreshInterval,defaultFormat,defaultNumberOfEntries)
    }

    private void initializeFeedHandler (String URL) {
        initializeFeedHandler(URL,null,null,null)
    }

    private void initializeFeedHandler (String URL,BigDecimal numberOfEntries) {
        initializeFeedHandler(URL,null,null,numberOfEntries)
    }

    private initializeFeedHandler(String URL,String format) {
        initializeFeedHandler(URL,null,format,null)
    }

    private void initializeFeedHandler(String URL,BigDecimal refreshTime,String format,BigDecimal numberOfEntries) {
        //set up configuration
        Configuration configuration = new Configuration()
        configuration.put((FeedBindingConstants.URL), URL)
        configuration.put((FeedBindingConstants.REFRESH_TIME),refreshTime)
        configuration.put((FeedBindingConstants.FEED_FORMAT), format)
        configuration.put((FeedBindingConstants.NUMBER_OF_ENTRIES), numberOfEntries)

        //create Feed Thing
        ThingUID feedUID = new ThingUID(FeedBindingConstants.FEED_THING_TYPE_UID,THING_NAME)
        channelUID = new ChannelUID(feedUID,FeedBindingConstants.FEED_CHANNEL)
        feedThing = managedThingProvider.createThing(FeedBindingConstants.FEED_THING_TYPE_UID, feedUID, null, null, configuration)

        //wait for FeedHandler to be registered
        waitForAssert({
            feedHandler = getService(ThingHandler, FeedHandler)
            assertThat "FeedHandler is not registered",feedHandler, is(notNullValue())
        },  DEFAULT_MAX_WAIT_TIME)

        //add listener for status changes
        FeedStatusChangeListner statusListener =  new FeedStatusChangeListner();
        Dictionary props = new Hashtable();
        props.put(EventConstants.EVENT_TOPIC,"smarthome");
        props.put(EventConstants.EVENT_FILTER, "(type=ThingStatusInfoEvent)")
        registerService(statusListener,props)

    }

    private void initializeItem (){
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
        initializeItem()

        waitForAssert({
            assertThat "Feed Thing is not online",feedThing.getStatus(),is(equalTo(ThingStatus.ONLINE))
            assertThat "Item's state is not updated on initialize", currentItemState, is(notNullValue())
        },  DEFAULT_MAX_WAIT_TIME)

        assertThat currentItemState,is(StringType)
        String firstItemState = currentItemState;

        if(contentChanged) {
            //The content on the mocked server should be changed
            servlet.setFeedContent(MOCK_CONTENT_CHANGED_URI)
        }

        if(commandReceived){
            feedThing.handler.handleCommand(channelUID,RefreshType.REFRESH)
        } else {
            //The auto refresh task will handle the update after the default wait time
            sleep(DEFAULT_AUTOREFRESH_TIME)
        }

        //check if item\'s state is updated
        waitForAssert({
            assertThat "Connection to the server failed or timed out.",isThingStatusChanged,is(true)
            waitForAssert({
                if(contentChanged) {
                    assertThat "Content is not updated !",currentItemState,not(equalTo(firstItemState))
                } else {
                    assertThat currentItemState,is(equalTo(firstItemState))
                }
            },DEFAULT_MAX_WAIT_TIME)
        }, DEFAULT_MAX_WAIT_TIME)

    }
    @Test
    public void 'assert that invalid configuration falls back to default values'() {
        def String mockServletURL = "$MOCK_SERVLET_PROTOCOL://$MOCK_SERVLET_HOSTNAME:$MOCK_SERVLET_PORT$MOCK_SERVLET_PATH";
        def defaultFormat = "none existing format"
        def defaultTestRefreshInterval = new BigDecimal(-10)
        def defaultNumberOfEntries = new BigDecimal(0)
        initializeFeedHandler(mockServletURL,defaultTestRefreshInterval,defaultFormat,defaultNumberOfEntries)

        waitForAssert({
            assertThat feedThing.getStatus(),is(equalTo(ThingStatus.ONLINE))
        },DEFAULT_MAX_WAIT_TIME)

        Configuration feedConfig = feedThing.getConfiguration();
        def url = feedConfig.get(FeedBindingConstants.URL)
        def format =  feedConfig.get(FeedBindingConstants.FEED_FORMAT)
        def interval = feedConfig.get(FeedBindingConstants.REFRESH_TIME)
        def numberOfEntries = feedConfig.get(FeedBindingConstants.NUMBER_OF_ENTRIES)

        assertThat url, is(equalTo(mockServletURL))
        assertThat format, is(equalTo(FeedBindingConstants.DEFAULT_FEED_FORMAT))
        assertThat interval, is(equalTo(FeedBindingConstants.DEFAULT_REFRESH_TIME))
        assertThat numberOfEntries, is(equalTo(FeedBindingConstants.DEFAULT_NUMBER_OF_ENTRIES))
    }

    @Test
    public void 'assert that item\'s state is updated on refresh command if content changed' () {
        boolean commandRecevied = true;
        boolean contentChanged = true;
        testIfItemStateIsUpdated(commandRecevied,contentChanged);
    }

    @Test
    public void 'assert that item\'s state is not updated on refresh command if content is not changed' () {
        boolean commandRecevied = true;
        boolean contentChanged = false;
        testIfItemStateIsUpdated(commandRecevied,contentChanged);
    }

    @Test
    public void 'assert that item\'s state is not updated on auto refresh if content is not changed' () {
        boolean commandRecevied = false;
        boolean contentChanged = false;
        testIfItemStateIsUpdated(commandRecevied,contentChanged);
    }

    @Test
    public void 'assert that item\'s state is updated on auto refresh if content changed' () {
        boolean commandRecevied = false;
        boolean contentChanged = true;
        testIfItemStateIsUpdated(commandRecevied,contentChanged);
    }

    @Test
    public void 'assert that thing\'s status is updated when HTTP 500 error code is received' () {
        testIfThingStatusIsUpdated(HttpStatus.SC_INTERNAL_SERVER_ERROR)
    }

    @Test
    public void 'assert that thing\'s status is updated when HTTP 401 error code is received' () {
        testIfThingStatusIsUpdated(HttpStatus.SC_UNAUTHORIZED)
    }

    @Test
    public void 'assert that thing\'s status is updated when HTTP 403 error code is received' () {
        testIfThingStatusIsUpdated(HttpStatus.SC_FORBIDDEN)
    }

    @Test
    public void 'assert that thing\'s status is updated when HTTP 404 error code is received' () {
        testIfThingStatusIsUpdated(HttpStatus.SC_NOT_FOUND)
    }


    private void testIfThingStatusIsUpdated (Integer serverStatus) {
        initializeDefaultFeedHandler()

        servlet.httpStatus = serverStatus

        feedThing.handler.handleCommand(channelUID,RefreshType.REFRESH)

        waitForAssert({
            assertThat feedThing.getStatus(),is(equalTo(ThingStatus.OFFLINE))
        },2*DEFAULT_MAX_WAIT_TIME)

        servlet.httpStatus = HttpStatus.SC_OK

        feedThing.handler.handleCommand(channelUID,RefreshType.REFRESH)

        waitForAssert({
            assertThat feedThing.getStatus(),is(equalTo(ThingStatus.ONLINE))
        },2*DEFAULT_MAX_WAIT_TIME)

    }

    @Test
    public void 'create thing with invalid URL protocol' () {
        def invalidURL = "gdfs://$MOCK_SERVLET_HOSTNAME:$MOCK_SERVLET_PORT$MOCK_SERVLET_PATH";

        initializeFeedHandler(invalidURL)
        waitForAssert({
            assertThat feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE))
            assertThat feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR))
        },DEFAULT_MAX_WAIT_TIME)

    }

    @Test
    public void 'create thing with invalid URL hostname' () {
        def invalidURL = "$MOCK_SERVLET_PROTOCOL://invalidhost:$MOCK_SERVLET_PORT$MOCK_SERVLET_PATH";

        initializeFeedHandler(invalidURL)
        waitForAssert({
            assertThat feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE))
            assertThat feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR))
        },DEFAULT_MAX_WAIT_TIME)
    }

    @Test
    public void 'create thing with invalid URL path' () {
        def invalidURL = "$MOCK_SERVLET_PROTOCOL://$MOCK_SERVLET_HOSTNAME:$MOCK_SERVLET_PORT/otherpath";

        initializeFeedHandler(invalidURL)
        waitForAssert({
            assertThat feedThing.getStatus(), is(equalTo(ThingStatus.OFFLINE))
            assertThat feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR))
        },DEFAULT_MAX_WAIT_TIME)
    }

    @Test
    public void 'assert feed format is changed to RSS 2' () {
        String ouputFormat = "rss_2.0"
        checkIfContentIsConverted(ouputFormat)
    }

    @Test
    public void 'assert feed format is changed to RSS 091 Netscape' () {
        String ouputFormat = "rss_0.91N"
        checkIfContentIsConverted(ouputFormat)
    }

    @Test
    public void 'assert feed format is changed to RSS 091 Userland' () {
        String ouputFormat = "rss_0.91U"
        checkIfContentIsConverted(ouputFormat)
    }

    @Test
    public void 'assert feed format is changed to RSS 092' () {
        String ouputFormat = "rss_0.92"
        checkIfContentIsConverted(ouputFormat)
    }

    @Test
    public void 'assert feed format is changed to RSS 093' () {
        String ouputFormat = "rss_0.93"
        checkIfContentIsConverted(ouputFormat)
    }

    @Test
    public void 'assert feed format is changed to RSS 094' () {
        String ouputFormat = "rss_0.94"
        checkIfContentIsConverted(ouputFormat)
    }

    @Test
    public void 'assert feed format is changed to atom 1' () {
        String ouputFormat = "atom_1.0"
        checkIfContentIsConverted(ouputFormat)
    }

    @Test
    public void 'assert feed format is changed to atom 03' () {
        String ouputFormat = "atom_0.3"
        checkIfContentIsConverted(ouputFormat)
    }

    /**
     * This method compares the default feed content in {@link #MOCK_CONTENT_URI} with the expected content for the selected format.
     * @param outputFormat - output feed format - {@link #FeedBindingConstants.SUPPORTED_FEED_FORMATS}
     * */
    private void checkIfContentIsConverted (String outputFormat) {
        def String mockServletURL = "$MOCK_SERVLET_PROTOCOL://$MOCK_SERVLET_HOSTNAME:$MOCK_SERVLET_PORT$MOCK_SERVLET_PATH";
        initializeFeedHandler(mockServletURL,outputFormat)
        initializeItem()

        waitForAssert({
            assertThat "Feed Thing is not online",feedThing.getStatus(),is(equalTo(ThingStatus.ONLINE))
            assertThat "Item's state is not updated on initialize", currentItemState, is(notNullValue())
        },  DEFAULT_MAX_WAIT_TIME)

        assertThat currentItemState,is(StringType)


        String output = currentItemState;
        output = output.replace("\r", "")
        String expectedOutput = new File("$MOCK_CONTENT_ROOT${outputFormat}.xml").text

        assertThat (output,is(equalTo(expectedOutput)))
    }

    @Test
    public void 'assert unescaped XML content is parsed'() {
        servlet.setFeedContent(MOCK_CONTENT_UNESCAPED_URI)
        boolean shouldBeParsed = true
        testIfContentIsParsed(shouldBeParsed)
    }

    @Test
    public void 'assert empty XML file is not parsed' () {
        servlet.setFeedContent(MOCK_CONTENT_EMPTY_URI)
        boolean shouldBeParsed = false
        testIfContentIsParsed(shouldBeParsed)
    }

    @Test
    public void 'assert content with missing XML declaration is parsed' () {
        servlet.setFeedContent(MOCK_CONTENT_MISSING_XML_DECLARATION_URI)
        boolean shouldBeParsed = true
        testIfContentIsParsed(shouldBeParsed)
    }

    @Test
    public void 'assert invalid XML is not parsed' () {
        servlet.setFeedContent(MOCK_CONTENT_INVALID_XML_URI)
        boolean shouldBeParsed = false
        testIfContentIsParsed(shouldBeParsed)
    }

    private void testIfContentIsParsed(boolean shouldBeParsed) {
        initializeDefaultFeedHandler()
        initializeItem()

        waitForAssert({
            if(shouldBeParsed) {
                assertThat feedThing.getStatus(),is(equalTo(ThingStatus.ONLINE))
            } else{
                assertThat feedThing.getStatus(),is(equalTo(ThingStatus.OFFLINE))
                assertThat feedThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR))
            }
        },  DEFAULT_MAX_WAIT_TIME)

        waitForAssert( {
            if(shouldBeParsed) {
                assertThat currentItemState, is(notNullValue())
                assertThat currentItemState, is(StringType)
            } else {
                assertThat currentItemState, is(nullValue())
            }

        },DEFAULT_MAX_WAIT_TIME)
    }

    @Test
    public void 'assert only 2 entries are stored' () {
        testIfEntriesAreStored(2)
    }

    @Test
    public void 'assert only 1 entry is stored' () {
        testIfEntriesAreStored(1)
    }

    @Test
    public void 'assert all entries are stored' () {
        testIfEntriesAreStored(999)
    }

    private void testIfEntriesAreStored(int expectedNumberOfEntries) {
        def String mockServletURL = "$MOCK_SERVLET_PROTOCOL://$MOCK_SERVLET_HOSTNAME:$MOCK_SERVLET_PORT$MOCK_SERVLET_PATH";
        BigDecimal numberOfEntries = new BigDecimal(expectedNumberOfEntries);

        initializeFeedHandler(mockServletURL,numberOfEntries)
        initializeItem()

        waitForAssert({
            assertThat feedThing.getStatus(), is(equalTo(ThingStatus.ONLINE))
        },DEFAULT_MAX_WAIT_TIME)

        def rssInput = new XmlSlurper().parseText(servlet.feedContent)
        def inputEntryNumber

        inputEntryNumber = rssInput.channel.item.size()

        if(expectedNumberOfEntries > inputEntryNumber) {
            expectedNumberOfEntries = inputEntryNumber
        }

        waitForAssert({
            assertThat currentItemState, is(notNullValue())
            assertThat currentItemState, is(StringType)
        },DEFAULT_MAX_WAIT_TIME)

        def atomOutput = new XmlSlurper().parseText((String)currentItemState)
        def outputEntryNumber

        outputEntryNumber = atomOutput.entry.size()

        assertThat(outputEntryNumber,is(equalTo(expectedNumberOfEntries)))
    }

    @After
    public void tearDown() {
        isThingStatusChanged = false
        currentItemState = null
        if(feedThing != null){
            // Remove the feed thing. The handler will be also disposed automatically
            Thing removedThing = thingRegistry.remove(feedThing.getUID())
            assertThat("The feed thing cannot be deleted",removedThing,is(notNullValue()))
        }

        unregisterFeedTestServlet();

        // wait for FeedHandler to be unregistered
        waitForAssert({
            feedHandler = getService(ThingHandler, FeedHandler)
            assertThat feedHandler, is(nullValue())
        }, DEFAULT_MAX_WAIT_TIME)
    }
}
