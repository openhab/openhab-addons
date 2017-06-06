/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.discovery

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import javax.servlet.http.HttpServlet

import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openhab.binding.mihome.MiHomeBindingConstants
import org.openhab.binding.mihome.handler.MiHomeSubdevicesHandler
import org.openhab.binding.mihome.internal.api.JSONResponseHandler
import org.openhab.binding.mihome.internal.api.constants.DeviceTypesConstants
import org.openhab.binding.mihome.internal.api.manager.*
import org.openhab.binding.mihome.test.AbstractMiHomeOSGiTest
import org.openhab.binding.mihome.test.JsonGateway
import org.openhab.binding.mihome.test.JsonSubdevice
import org.openhab.binding.mihome.test.MiHomeServlet

import com.google.gson.JsonObject
/**
 * Tests for the {@link MiHomeSubdeviceDiscoveryService}
 *
 * @author Svilen Valkanov
 *
 */
class MiHomeSubdeviceDiscoveryTest extends AbstractMiHomeOSGiTest {

    private Thing subdeviceThing
    private MiHomeSubdeviceDiscoveryService discoveryService;
    private Inbox inbox
    private JsonObject serverResponseBody

    @Before
    public void setUp() {
        setUpServices()

        // in order to test subdevices we need to register servlet representing successful gateway registration
        JsonGateway gatewayDevice = new JsonGateway()
        String content = generateShowJsonDeviceServerResponse("success", gatewayDevice)
        MiHomeServlet successfullGatewayRegistrationServlet = new MiHomeServlet(content)
        registerServlet(PATH_CREATE_GATEWAY, successfullGatewayRegistrationServlet)

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID)

        thingRegistry.add(gatewayThing)

        waitForAssert {
            ThingStatus status = gatewayThing.getStatus()
            assertThat "Gateway is in status ${status}.", status, is(ThingStatus.ONLINE)
        }

        // The mocked client has to be installed after the BridgeHandler is completely initialized, otherwise it will be overwritten
        setMiHomeApiManager()

        discoveryService = getService(DiscoveryService, MiHomeSubdeviceDiscoveryService)
        assertThat "Discovery service wasn't registered by the ThingHandlerFactory for gateway ${gatewayThing.getUID()}", discoveryService, is(notNullValue())

        inbox = getService(Inbox)
        assertThat inbox, is(notNullValue())
        assertThat "Inbox should be empty before the test", inbox.getAll().size(), is(0)
    }

    @Test
    public void 'create result for subdevice without a Thing'(){
        int testDeviceID = 51816
        String testDeviceType = DeviceTypesConstants.MOTION_SENSOR_TYPE
        ThingTypeUID expectedThingTypeUID = MiHomeBindingConstants.THING_TYPE_MOTION_SENSOR

        // Set up the backend response
        JsonSubdevice subdevice = new JsonSubdevice(testDeviceID, TEST_GATEWAY_ID, testDeviceType)
        serverResponseBody = generateJsonDevicesListServerResponse("success", subdevice)

        discoveryService.startScan();

        List<DiscoveryResult> results
        waitForAssert {
            results = inbox.getAll()
            int expectedResults = 1
            assertThat "Exactly ${expectedResults} DiscoveryResult was expected", results.size(), is(expectedResults)
        }

        DiscoveryResult result = results.get(0)
        assertDiscoveryResult(result, testDeviceID, testDeviceType, expectedThingTypeUID)

        subdeviceThing = inbox.approve(result.getThingUID(), result.getLabel())
        waitForAssert{
            ThingHandler handler =  subdeviceThing.getHandler()
            assertThat "ThingHandler should be created after inbox approval for DiscoveryResult ${result}", handler, is(notNullValue())
            ThingUID uid = handler.getThing().getUID()
            assertThat "Thing UID and DiscoveryResult UID aren't the same", uid, is(equalTo(result.getThingUID()))
            assertThat "Thing isn't intialized completely", handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }
        discoveryService.stopScan()
    }

    @Test
    public void 'dont create a result when no subdevice are found' () {

        serverResponseBody = generateJsonDevicesListServerResponse("success")

        discoveryService.startScan();

        List<DiscoveryResult> results = inbox.getAll()
        int expectedResults = 0
        assertThat "Exaclty ${expectedResults} DiscoveryResults were expected", results.size(), is(expectedResults)

        discoveryService.stopScan()
    }

    @Test
    public void 'dont create result for subdevice with a Thing'() {
        int testDeviceID = 51816
        String testDeviceType = DeviceTypesConstants.OPEN_SENSOR_TYPE
        ThingTypeUID expectedThingTypeUID = MiHomeBindingConstants.THING_TYPE_OPEN_SENSOR

        // Register a thing
        subdeviceThing = createThing(thingRegistry, gatewayThing, expectedThingTypeUID, testDeviceID)
        thingRegistry.add(subdeviceThing)

        waitForAssert{
            MiHomeSubdevicesHandler handler = subdeviceThing.getHandler()
            assertThat handler, is(notNullValue())
            assertThat subdeviceThing.getStatus(), is(ThingStatus.ONLINE)
        }

        // Set up the backend response
        JsonSubdevice subdevice = new JsonSubdevice(testDeviceID,TEST_GATEWAY_ID,testDeviceType)
        serverResponseBody = generateJsonDevicesListServerResponse("success", subdevice)

        discoveryService.startScan();

        List<DiscoveryResult> results = inbox.getAll()
        int expectedResults = 0
        assertThat "Exactly ${expectedResults} DiscoveryResults were expected", results.size(), is(expectedResults)

        discoveryService.stopScan();
    }

    @After
    public void tearDown(){

        unregisterServlet(PATH_CREATE_GATEWAY)

        removeBridge(thingRegistry, gatewayThing)
        gatewayThing = null

        removeThing(thingRegistry, gatewayThing)
        subdeviceThing = null

        waitForAssert {
            discoveryService = getService(DiscoveryService, MiHomeSubdeviceDiscoveryService)
            assertThat "DiscoveryService has to be unregistered after the BridgeHandler is removed.", discoveryService, is(nullValue())
        }
    }

    private assertDiscoveryResult(DiscoveryResult result, Integer testDeviceID,String testDeviceType, ThingTypeUID expectedThingType){
        assertThat "DiscoveryResult has incorrect BridgeUID", result.getBridgeUID(), is(equalTo(gatewayThing.getUID()))

        ThingUID thingUID = new ThingUID(expectedThingType,testDeviceID.toString())
        assertThat "DiscoveryResult has incorrect ThingUID", result.getThingUID(), is(equalTo(thingUID))

        Map<String,Object> properties = result.getProperties()
        assertThat "DiscoveryResult has incorrect ${MiHomeBindingConstants.PROPERTY_DEVICE_ID} property", properties.get(MiHomeBindingConstants.PROPERTY_DEVICE_ID), is(equalTo(testDeviceID))
        assertThat "DiscoveryResult has incorrect ${MiHomeBindingConstants.PROPERTY_GATEWAY_ID} property", properties.get(MiHomeBindingConstants.PROPERTY_GATEWAY_ID), is(equalTo(TEST_GATEWAY_ID))
        assertThat "DiscoveryResult has incorrect ${MiHomeBindingConstants.PROPERTY_TYPE} property", properties.get(MiHomeBindingConstants.PROPERTY_TYPE), is(equalTo(testDeviceType))
        assertThat "DiscoveryResult doesn't contain default required configuration paramter ${MiHomeBindingConstants.CONFIG_UPDATE_ITNERVAL}", properties.get(MiHomeBindingConstants.CONFIG_UPDATE_ITNERVAL), is(equalTo(MiHomeSubdevicesHandler.DEFAULT_UPDATE_INTERVAL))
        assertThat "DiscoveryResult has incorrect label", result.getLabel(), is(equalTo(JsonSubdevice.DEFAULT_LABEL))
    }

    private void setMiHomeApiManager (){
        MiHomeApiManager mockedRestClient = [
            // We need to mock this call to ensure the thing is removed properly
            unregisterGateway :  { int id ->
                return JSONResponseHandler.responseStringtoJsonObject("{}")
            },
            listSubdevices : { return serverResponseBody  },
            // Returning empty device data is enough to initialize the thing
            showSubdeviceInfo : {int id ->
                return JSONResponseHandler.responseStringtoJsonObject("{data:{}}")}
        ] as MiHomeApiManager
        gatewayThing.getHandler().apiManager = mockedRestClient

    }
}
