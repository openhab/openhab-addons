package org.openhab.binding.feed.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openhab.binding.feed.FeedBindingConstants
import org.openhab.binding.feed.handler.FeedHandler
import org.osgi.service.http.HttpService

public class FeedHandlerTest extends OSGiTest {
    private final String MOCK_SERVLET_PROTOCOL = "http"
    private final String MOCK_SERVLET_HOSTNAME = "localhost"
    private final String MOCK_SERVLET_PORT = "9090"
    private final String MOCK_SERVLET_PATH = "/test/feed";


    //servlet content
    def ITEM_NAME = "testChannel"
    def THING_NAME = "testFeed"

    ManagedThingProvider managedThingProvider
    VolatileStorageService volatileStorageService
    ThingRegistry thingRegistry
    EventPublisher eventPublisher

    private FeedServiceMock servlet
    private Thing feedThing;
    private FeedHandler feedHandler = null;
    //other classes like ThingReg, ManagedProvide,ItemChannel
    final ThingTypeUID THING_TYPE_UID = new ThingTypeUID(FeedBindingConstants.BINDING_ID, "feed");
    private ChannelUID channelUID =  null

    class FeedServiceMock extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
            ServletOutputStream outputStream = response.getOutputStream()
            def content ='''
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">

                <channel>
                  <title>W3Schools Home Page</title>
                  <link>http://www.w3schools.com</link>
                  <description>Free web building tutorials</description>
                  <item>
                    <title>RSS Tutorial</title>
                    <link>http://www.w3schools.com/xml/xml_rss.asp</link>
                    <description>New RSS tutorial on W3Schools</description>
                  </item>
                  <item>
                    <title>XML Tutorial</title>
                    <link>http://www.w3schools.com/xml</link>
                    <description>New XML tutorial on W3Schools</description>
                  </item>
                </channel>
                </rss>
            '''
            outputStream.write(content);
        }
    }
    private void registerFeedTestServlet (){
        HttpService httpService = getService(HttpService)
        assertThat httpService,is(notNullValue())
        servlet = new FeedServiceMock();
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
        //TODO should I create Item and link in separate method
        println ("Set Up finished");
    }

    private void initializeFeedHandler() {
        def String mockServletURL = "$MOCK_SERVLET_PROTOCOL://$MOCK_SERVLET_HOSTNAME:$MOCK_SERVLET_PORT$MOCK_SERVLET_PATH";
        Configuration configuration = new Configuration()
        configuration.put((FeedBindingConstants.URL), "http://technews.bg/feed")

        ThingUID feedUID = new ThingUID(THING_TYPE_UID,THING_NAME)
        channelUID = new ChannelUID(feedUID,FeedBindingConstants.FEED_CHANNEL)
        feedThing = managedThingProvider.createThing(THING_TYPE_UID, feedUID, null, null, configuration)

        // wait for FeedHandler to be registered
        waitForAssert({
            feedHandler = getService(ThingHandler, FeedHandler)
            assertThat feedHandler, is(notNullValue())

        },  10000)
    }

    private void initializeFeedThing(){
        initializeFeedHandler()

        def ItemRegistry itemRegistry = getService(ItemRegistry)
        assertThat itemRegistry, is(notNullValue())
        itemRegistry.add(new StringItem(ITEM_NAME))

        def ManagedItemChannelLinkProvider itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
        assertThat itemChannelLinkProvider,is(notNullValue())
        itemChannelLinkProvider.add(new ItemChannelLink(ITEM_NAME,channelUID))
    }

    @Test
    public void 'assert' () {

        //registerFeedTestServlet();
        initializeFeedThing()
    }
    @After
    public void tearDown() {
        managedThingProvider.remove(feedThing.getUID())
        /*if(feedThing != null){
         // Remove the camera thing. The handler will be also disposed automatically
         Thing removedThing = thingRegistry.remove(feedThing.getUID())
         assertThat("The feed thing cannot be deleted",removedThing,is(notNullValue()))
         }
         */
        //unregisterFeedTestServlet();

        // wait for FeedHandler to be unregistered
        waitForAssert({
            feedHandler = getService(ThingHandler, FeedHandler)
            assertThat feedHandler, is(nullValue())
        }, 10000)
        // ? check if thread is still running
    }
}
