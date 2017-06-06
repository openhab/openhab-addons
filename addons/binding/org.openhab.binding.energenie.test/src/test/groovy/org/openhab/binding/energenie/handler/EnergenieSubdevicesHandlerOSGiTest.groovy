/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.handler

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.openhab.binding.energenie.EnergenieBindingConstants.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.library.types.DecimalType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.OpenClosedType
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingStatusInfo
import org.eclipse.smarthome.core.thing.UID
import org.eclipse.smarthome.core.thing.binding.BridgeHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.core.types.UnDefType
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openhab.binding.energenie.EnergenieBindingConstants
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes
import org.openhab.binding.energenie.internal.api.JsonDevice
import org.openhab.binding.energenie.internal.api.JsonGateway
import org.openhab.binding.energenie.internal.api.JsonSubdevice
import org.openhab.binding.energenie.internal.api.constants.JsonResponseConstants
import org.openhab.binding.energenie.internal.api.manager.*
import org.openhab.binding.energenie.internal.rest.*
import org.openhab.binding.energenie.test.AbstractEnergenieOSGiTest
import org.openhab.binding.energenie.test.EnergenieServlet

import com.google.common.collect.Iterables

/**
 * Tests for the {@link EnergenieSubdevicesHandler}
 *
 * @author Svilen Valkanov
 */
public class EnergenieSubdevicesHandlerOSGiTest extends AbstractEnergenieOSGiTest{

    private Thing subdevice

    public static final String PATH_CREATE_SUBDEVICE = "/${EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES}/${EnergenieApiManagerImpl.ACTION_CREATE}"
    public static final String PATH_LIST_SUBDEVICES = "/${EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES}/${EnergenieApiManagerImpl.ACTION_LIST}"
    public static final String PATH_SHOW_SUBDEVICE = "/${EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES}/${EnergenieApiManagerImpl.ACTION_SHOW}"
    public static final String PATH_UPDATE_SUBDEVICE = "/${EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES}/${EnergenieApiManagerImpl.ACTION_UPDATE}"
    public static final String PATH_DELETE_SUBDEVICE = "/${EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES}/${EnergenieApiManagerImpl.ACTION_DELETE}"

    private EnergenieServlet listSubdevicesServlet
    private EnergenieServlet showSubdeviceServlet
    private EnergenieServlet createSubdeviceServlet = new EnergenieServlet(EnergenieServlet.EMPTY_DATA_OBJECT)

    // Subdevice information
    public static final int TEST_SUBDEVICE_ID = 53412
    public static final EnergenieDeviceTypes TEST_SUBDEVICE_TYPE = EnergenieDeviceTypes.MOTION_SENSOR
    public static final String TEST_SUBDEVICE_LABEL = JsonSubdevice.DEFAULT_LABEL

    @Before
    public void setUp(){
        setUpServices()

        //Register Subdevices Servlets
        JsonSubdevice subdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE)
        String listSubdevice = generateJsonDevicesListServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, subdevice)
        listSubdevicesServlet = new EnergenieServlet(listSubdevice)
        registerServlet(PATH_LIST_SUBDEVICES, listSubdevicesServlet)

        registerServlet(PATH_CREATE_SUBDEVICE, createSubdeviceServlet)

        String showSubdevice = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, subdevice)
        showSubdeviceServlet = new EnergenieServlet(showSubdevice)
        registerServlet(PATH_SHOW_SUBDEVICE, showSubdeviceServlet)

        // Register Gateway Servlets

        // in order to test subdevices we need to register servlet representing successful gateway registration
        JsonGateway gatewayDevice = createTestGateway()
        String showContent = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, gatewayDevice)
        registerServlet(PATH_CREATE_GATEWAY, new EnergenieServlet(showContent))

        // Register servlet with content representing list of gateways. It is needed for the refresh thread.
        String listContent = generateJsonDevicesListServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, gatewayDevice)
        registerServlet(PATH_LIST_GATEWAYS, new EnergenieServlet(listContent))

    }

    @After
    public void tearDown(){
        removeBridge(thingRegistry, gatewayThing)
        removeThing(thingRegistry, subdevice)

        //Unregister Servlets
        unregisterServlet(PATH_LIST_SUBDEVICES)
        unregisterServlet(PATH_CREATE_SUBDEVICE)
        unregisterServlet(PATH_SHOW_SUBDEVICE)
        unregisterServlet(PATH_CREATE_GATEWAY)
        unregisterServlet(PATH_LIST_GATEWAYS)
        unregisterServlet(PATH_DELETE_SUBDEVICE)
        unregisterServlet(PATH_DELETE_GATEWAYS)
        unregisterServlet(PATH_UPDATE_SUBDEVICE)
    }


    @Test
    public void 'assert Subdevice doesn`t initialize when bridge is OFFLINE'() {
        // We set an invalid username, so the bridge will be set to OFFLINE by the handler
        String invalidUsername = "should_be_email"
        gatewayThing = createBridge(thingRegistry,TEST_PASSWORD, invalidUsername, TEST_GATEWAY_CODE,TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)

        assertGatewayStatus(ThingStatus.OFFLINE)

        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR)
        thingRegistry.add(subdevice)

        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR)
    }

    @Test
    public void 'assert Subdevice doesn`t initialize when gatewayID is missing'() {
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)

        assertGatewayStatus(ThingStatus.ONLINE)
        gatewayThing.getHandler().gatewayId = 0

        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR, TEST_SUBDEVICE_ID)
        thingRegistry.add(subdevice)

        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR)
    }

    @Test
    public void 'assert paired Thing doesn`t initialize when there is no connection to server'() {
        unregisterServlet(PATH_SHOW_SUBDEVICE)

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)
        assertGatewayStatus(ThingStatus.ONLINE)

        // We create a thing that has a deviceID in the configuration to skip the pairing
        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR, TEST_SUBDEVICE_ID)
        thingRegistry.add(subdevice)
        assertThingStatus(ThingStatus.UNKNOWN,ThingStatusDetail.HANDLER_INITIALIZING_ERROR)
    }

    @Test
    public void 'assert pairing fails when getting devices information is unsuccessful' () {
        unregisterServlet(PATH_LIST_SUBDEVICES)

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)
        assertGatewayStatus(ThingStatus.ONLINE)

        // We create a thing that does not contain a deviceID in the configuration
        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR)
        thingRegistry.add(subdevice)
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR)

        thingRegistry.remove(subdevice.getUID())
        waitForAssert {
            ThingHandler thingHandler = subdevice.getHandler()
            assertThat "The thing ${subdevice.getUID()} cannot be deleted", thingHandler, is(nullValue())
        }
        subdevice = null
    }

    @Test
    public void 'assert pairing fails when device registration is unsuccessful' () {
        unregisterServlet(PATH_CREATE_SUBDEVICE)

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)
        assertGatewayStatus(ThingStatus.ONLINE)

        // We create a thing that does not contain a deviceID in the configuration
        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR)
        thingRegistry.add(subdevice)
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR)
    }

    @Test
    public void 'assert user is notified that the pairing button must be pressed' () {
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID)
        thingRegistry.add(gatewayThing)
        assertGatewayStatus(ThingStatus.ONLINE)

        // We create a thing that does not contain a deviceID in the configuration
        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR)
        thingRegistry.add(subdevice)
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)
    }

    @Test
    public void 'assert pairing fails when no new devices are found' () {
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)
        assertGatewayStatus(ThingStatus.ONLINE)

        // We create a thing that does not contain a deviceID in the configuration
        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR)
        thingRegistry.add(subdevice)
        assertThingStatus(ThingStatus.OFFLINE,ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)

        sleep(EnergenieSubdevicesHandler.PARING_WAIT_TIME_MSEC)

        // The status of the Thing will be OFFLINE, the status detail may vary between HANDLER_INITIALIZING_ERROR and HANDLER_CONFIGURATION_PENDING
        waitForAssert {
            ThingHandler handler = subdevice.getHandler()
            assertThat handler, is(notNullValue())

            ThingStatus status = handler.getThing().getStatus()
            ThingStatusInfo info = handler.getThing().getStatusInfo()
            ThingStatusDetail detail = info.getStatusDetail()
            assertThat "Unexpected thing status. ThingStatus description is ${info.getDescription()}", status, is(ThingStatus.OFFLINE)
            assertThat "Unexpected thing status detail. ThingStatus description is ${info.getDescription()}", detail, anyOf(is(ThingStatusDetail.HANDLER_INITIALIZING_ERROR), is(ThingStatusDetail.HANDLER_CONFIGURATION_PENDING))
        }
    }

    @Test
    public void 'assert pairing succeeds when new devices are found' () {
        // Before the pairing no new devices are found
        listSubdevicesServlet.setContent(EnergenieServlet.EMPTY_DATA_ARRAY)

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)
        assertGatewayStatus(ThingStatus.ONLINE)

        // We create a thing that does not contain a deviceID in the configuration
        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR)
        thingRegistry.add(subdevice)
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING)

        // Pairing button was pressed and pairing was successful, MiHome REST API returns a new device
        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE)
        String newSubdeviceList = generateJsonDevicesListServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice)
        listSubdevicesServlet.setContent(newSubdeviceList)

        // The label will be changed only if a request to the MiHome REST API is successful
        String newSubdevice = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice)
        registerServlet(PATH_UPDATE_SUBDEVICE,new EnergenieServlet(newSubdevice))

        sleep(EnergenieSubdevicesHandler.PARING_WAIT_TIME_MSEC)

        assertThingStatus(ThingStatus.ONLINE,ThingStatusDetail.NONE)
        assertThingProperties(subdevice.getProperties())
        assertThingConfiguration(subdevice.getConfiguration())
    }

    @Test
    public void 'assert initialized Thing goes OFFLINE when the gateway goes OFFLINE' () {
        initializePairedThing()

        // Edit the configuration with invalid username
        ThingHandler handler = gatewayThing.getHandler()
        Configuration config = gatewayThing.getConfiguration()
        config.put(EnergenieBindingConstants.CONFIG_GATEWAY_CODE, "invalid_gateway_code")
        handler.handleConfigurationUpdate(config.getProperties())

        // As we have no servlet registered to return the gateway state (and the last_seen information), the bridge will be set to OFFLINE state
        assertGatewayStatus(ThingStatus.OFFLINE)
        assertThingStatus(ThingStatus.OFFLINE,ThingStatusDetail.BRIDGE_OFFLINE)
    }

    @Test
    public void 'assert initialized Thing updates the configuration and properties' () {
        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID,TEST_GATEWAY_ID,TEST_SUBDEVICE_TYPE)
        String showSubdeviceServletContent = generateShowJsonDeviceServerResponse("success", jsonSubdevice)
        showSubdeviceServlet.setContent(showSubdeviceServletContent)

        initializePairedThing()

        assertThingConfiguration(subdevice.getConfiguration())
        assertThingProperties(subdevice.getProperties())
    }

    @Test
    public void 'assert Subdevice handles configuration change' (){
        initializePairedThing()

        // Save the initial values
        def initialUpdateTask = subdevice.getHandler().updateTask
        def initialUpdateInterval = subdevice.getConfiguration().get(CONFIG_UPDATE_INTERVAL)

        // Update the configuration
        int newUpdateInterval = EnergenieSubdevicesHandler.DEFAULT_UPDATE_INTERVAL + 10
        Configuration config = subdevice.getConfiguration()
        config.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL, new BigDecimal(newUpdateInterval))
        thingRegistry.updateConfiguration(subdevice.getUID(), config.getProperties())

        waitForAssert {
            def modifiedUpdateInterval = subdevice.getConfiguration().get(CONFIG_UPDATE_INTERVAL)
            def newUpdateTask = subdevice.getHandler().updateTask

            assertThat "Configuration parameter {CONFIG_UPDATE_ITNERVAL} wasn't changed", modifiedUpdateInterval, not(equals(initialUpdateInterval))
            assertThat "Update task wasn't restarted after changing the update interval", newUpdateTask, not(equals(initialUpdateInterval))
        }
    }

    @Test
    void 'assert Subdevice handles label change'() {
        initializePairedThing()

        // The label will be changed only if a request to the MiHome REST API is successful
        registerServlet(PATH_UPDATE_SUBDEVICE,new EnergenieServlet(EnergenieServlet.EMPTY_DATA_OBJECT))

        Thing updatedThing = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR, TEST_SUBDEVICE_ID)
        String newLabel = "Updated label"
        updatedThing.setLabel(newLabel)
        managedThingProvider.update(updatedThing)

        waitForAssert {
            assertThat subdevice.getLabel(), is(equalTo(newLabel))
        }
    }

    @Test
    void 'assert Subdevice handles location change' () {
        initializePairedThing()

        Thing updatedThing = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR, TEST_SUBDEVICE_ID)
        String newLocation = "Bedroom"
        updatedThing.setLocation(newLocation)
        managedThingProvider.update(updatedThing)

        waitForAssert {
            assertThat subdevice.getLocation(), is(equalTo(newLocation))
        }
    }


    @Test
    public void 'assert motions sensor updates state to OFF' () {
        def sensorState = 0
        State expectedState = OnOffType.OFF
        String channelID = EnergenieBindingConstants.CHANNEL_STATE
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.MOTION_SENSOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID,TEST_GATEWAY_ID, subdeviceType, sensorState)

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState)
    }

    @Test
    public void 'assert motion sensor updates state to ON' () {
        def sensorState = 1
        State expectedState = OnOffType.ON
        String channelID = EnergenieBindingConstants.CHANNEL_STATE
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.MOTION_SENSOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState)

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState)
    }

    @Test
    public void 'assert motion sensor updates state to NULL' () {
        def sensorState = null
        State expectedState = UnDefType.NULL
        String channelID = EnergenieBindingConstants.CHANNEL_STATE
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.MOTION_SENSOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState)

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState)
    }

    @Test
    public void 'assert open sensor updates state to CLOSED' () {
        def sensorState = 0
        State expectedState = OpenClosedType.CLOSED
        String channelID = EnergenieBindingConstants.CHANNEL_STATE
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.OPEN_SENSOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState)

        testDeviceState(subdeviceType,channelID,jsonSubdevice,expectedState)
    }

    @Test
    public void 'assert open sensor updates state to OPEN'() {
        def sensorState = 1
        State expectedState = OpenClosedType.OPEN
        String channelID = EnergenieBindingConstants.CHANNEL_STATE
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.OPEN_SENSOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState)

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState)
    }

    @Test
    public void 'assert open sensor updates state to NULL' () {
        def sensorState = null
        State expectedState = UnDefType.NULL
        String channelID = EnergenieBindingConstants.CHANNEL_STATE
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.OPEN_SENSOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState)

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState)
    }

    @Test
    public void 'assert house monitor updates voltage' () {
        double voltage = 4.3646
        State expectedState = new DecimalType(voltage)
        String channelID = EnergenieBindingConstants.CHANNEL_VOLTAGE
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.HOUSE_MONITOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, voltage, 0, 0)

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState)
    }

    @Test
    public void 'assert house monitor updates power' () {
        int power = 56
        State expectedState = new DecimalType(power)
        String channelID = EnergenieBindingConstants.CHANNEL_REAL_POWER
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.HOUSE_MONITOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, 0, power, 0)

        testDeviceState(subdeviceType,channelID,jsonSubdevice,expectedState)
    }

    @Test
    public void 'assert house monitor updates consumption' () {
        int consumption = 563
        State expectedState = new DecimalType(consumption)
        String channelID = EnergenieBindingConstants.CHANNEL_TODAY_CONSUMPTION
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.HOUSE_MONITOR

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, 0, 0, consumption)

        testDeviceState(subdeviceType,channelID,jsonSubdevice,expectedState)
    }


    @Test
    public void 'assert Subdevice won`t be removed, when server is offline' () {
        initializePairedThing()

        // We haven`t registered servlet to /subdevices/delete
        thingRegistry.remove(subdevice.getUID())
        sleep 1000
        assertThingStatus(ThingStatus.REMOVING, ThingStatusDetail.NONE)
    }

    @Test
    public void 'assert Subdevice will be removed, when server is online' () {
        registerServlet(PATH_DELETE_SUBDEVICE, new EnergenieServlet(EnergenieServlet.EMPTY_DATA_OBJECT))

        initializePairedThing()

        thingRegistry.remove(subdevice.getUID())

        waitForAssert {
            ThingHandler handler = subdevice.getHandler()
            assertThat handler, is(nullValue())
        }
    }

    //Wait for Gateway removal test
    @Test
    public void 'assert Subdevice will be removed when Gateway is removed' (){
        // Mock the MiHome server response
        JsonDevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE)
        String content = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice)
        EnergenieServlet servlet = new EnergenieServlet(content);
        registerServlet(PATH_DELETE_GATEWAYS, servlet)

        initializePairedThing()
        assertGatewayStatus(ThingStatus.ONLINE)
        assertThingStatus(ThingStatus.ONLINE,ThingStatusDetail.NONE)

        /* Register servlet representing successful gateway deletion. According to the Mi|Home API,
         * when a deletion request is successful the server will respond with details of the removed device.
         * So basically the response is the same as the response from successful registration."
         */
        JsonGateway gatewayDevice = createTestGateway()
        String succesfulDeletionServletContent = generateShowJsonDeviceServerResponse("success", gatewayDevice)
        EnergenieServlet successfullDeletionServlet = new EnergenieServlet(succesfulDeletionServletContent)
        registerServlet(PATH_DELETE_GATEWAYS, successfullDeletionServlet)

        // Remove the gateway
        thingRegistry.remove(gatewayThing.getUID())
        waitForAssert {
            BridgeHandler gatewayHandler = gatewayThing.getHandler()
            assertThat "The bridge ${gatewayThing.getUID()} cannot be deleted", gatewayHandler, is(nullValue())
        }

        // Check that the thing is removed
        waitForAssert {
            ThingHandler thingHandler = subdevice.getHandler()
            assertThat "The thing ${subdevice.getUID()} cannot be deleted", thingHandler, is(nullValue())
        }
    }

    @Test
    public void 'assert ThingStatus is OFFLINE on REFRESH when device is removed from the MiHome server'() {
        JsonDevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE)
        String showSubdeviceServletContent = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice)
        showSubdeviceServlet.setContent(showSubdeviceServletContent)

        initializePairedThing()

        assertGatewayStatus(ThingStatus.ONLINE)
        assertThingStatus(ThingStatus.ONLINE,ThingStatusDetail.NONE)

        // The device has been removed from the MiHome server
        showSubdeviceServletContent = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_NOT_FOUND, jsonSubdevice)
        showSubdeviceServlet.setContent(showSubdeviceServletContent)

        // Send a command to refresh the channel state
        Channel channelState = subdevice.getChannel(EnergenieBindingConstants.CHANNEL_STATE)
        subdevice.getHandler().handleCommand(channelState.getUID(), RefreshType.REFRESH)

        assertGatewayStatus(ThingStatus.ONLINE)
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR)
    }

    @Test
    public void 'assert ThingStatus is OFFLINE when device is removed from the MiHome server'() {
        JsonDevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE)
        String showSubdeviceServletContent = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice)
        showSubdeviceServlet.setContent(showSubdeviceServletContent)

        initializePairedThing()

        assertGatewayStatus(ThingStatus.ONLINE)
        assertThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE)

        // The device has been removed from the MiHome server
        showSubdeviceServletContent = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_NOT_FOUND, jsonSubdevice)
        showSubdeviceServlet.setContent(showSubdeviceServletContent)

        // Wait for the refresh thread
        sleep(TEST_SUBDEVICE_UPDATE_INTERVAL.longValue() * 1000)

        assertGatewayStatus(ThingStatus.ONLINE)
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR)
    }

    private void testDeviceState (EnergenieDeviceTypes subdeviceType, String channelID, JsonSubdevice jsonSubdevice, State expectedState) {
        String showSubdeviceServletContent = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice)
        showSubdeviceServlet.setContent(showSubdeviceServletContent)

        gatewayThing = createBridge(thingRegistry,TEST_PASSWORD,TEST_USERNAME,TEST_GATEWAY_CODE,TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)
        assertGatewayStatus(ThingStatus.ONLINE)

        // We create a thing that has a deviceID in the configuration to skip the pairing
        def thingTypeUID = EnergenieBindingConstants.DEVICE_TYPE_TO_THING_TYPE.get(subdeviceType)
        subdevice = createThing(thingRegistry, gatewayThing, thingTypeUID, TEST_SUBDEVICE_ID)
        thingRegistry.add(subdevice)
        assertThingStatus(ThingStatus.ONLINE,ThingStatusDetail.NONE)

        Channel stateChannel = subdevice.getChannel(channelID)
        assertThat stateChannel, is(notNullValue())
        UID stateChannelUID = stateChannel.getUID()
        ItemChannelLinkRegistry linkRegistry = getService(ItemChannelLinkRegistry)
        assertThat "ItemChannelLinkRegistry is missing", linkRegistry, is(notNullValue())

        waitForAssert{
            Set<Item> items = linkRegistry.getLinkedItems(stateChannelUID)
            assertThat "No items linked to channel ${stateChannelUID.getAsString()}", items, is(notNullValue())
            assertThat "Single item was expected, but found {items.toString()} items linked to channel ${stateChannelUID.getAsString()}", items.size(), is(1)
            Item item = Iterables.get(items,0)
            State state = item.getState()
            assertThat "Unexpected state of item ${item}", state, is(expectedState)
        }
    }

    private void assertThingConfiguration(Configuration configuration) {
        def updateInterval = configuration.get(CONFIG_UPDATE_INTERVAL)
        assertThat "Configuration parameter ${CONFIG_UPDATE_INTERVAL} is missing", updateInterval, is(notNullValue())
        BigDecimal  expectedInterval= new BigDecimal(TEST_SUBDEVICE_UPDATE_INTERVAL)
        assertThat "Unexpected value of configuration parameter ${CONFIG_UPDATE_INTERVAL}", updateInterval, is(equalTo(expectedInterval))
    }

    private void assertThingProperties(Map<String,Object> properties) {
        def id = properties.get(PROPERTY_DEVICE_ID)
        assertThat "Property ${PROPERTY_DEVICE_ID} is missing", id, is(notNullValue())
        assertThat "Unexpected value of property ${PROPERTY_DEVICE_ID}", id, is(equalTo(Integer.toString(TEST_SUBDEVICE_ID)))
        def gatewayId = properties.get(PROPERTY_GATEWAY_ID)
        assertThat "Property ${PROPERTY_GATEWAY_ID} is missing",gatewayId, is(notNullValue())
        assertThat "Unexpected value of property ${PROPERTY_GATEWAY_ID}", gatewayId , is(equalTo(Integer.toString(TEST_GATEWAY_ID)))
        def type = properties.get(PROPERTY_TYPE)
        assertThat "Property ${PROPERTY_TYPE} is missing", type, is(notNullValue())
        assertThat "Unexpected value of property ${PROPERTY_TYPE}", type, is(equalTo(TEST_SUBDEVICE_TYPE.toString()))
    }

    private void assertGatewayStatus(ThingStatus expectedStatus) {
        waitForAssert {
            BridgeHandler gatewayHandler = gatewayThing.getHandler()
            assertThat gatewayHandler, is(notNullValue())

            ThingStatus status = gatewayThing.getStatus()
            ThingStatusInfo info = gatewayThing.getStatusInfo()
            assertThat "Unexpected gateway status. ThingStatus detail is ${info.getStatusDetail()} and description is ${info.getDescription()}", status,is(expectedStatus)
        }
    }

    private void assertThingStatus(ThingStatus expectedStatus, ThingStatusDetail expectedStatusDetail){
        waitForAssert {
            ThingHandler handler = subdevice.getHandler()
            assertThat handler, is(notNullValue())
            ThingStatus status = handler.getThing().getStatus()
            ThingStatusInfo info = handler.getThing().getStatusInfo()
            ThingStatusDetail detail = info.getStatusDetail()
            assertThat "Unexpected thing status. ThingStatus description is ${info.getDescription()}", status, is(expectedStatus)
            assertThat "Unexpected thing status detail. ThingStatus description is ${info.getDescription()}", detail, is(expectedStatusDetail)
        }
    }

    private void initializePairedThing() {
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID, TEST_AUTH_CODE)
        thingRegistry.add(gatewayThing)
        assertGatewayStatus(ThingStatus.ONLINE)

        // We create a thing that has a deviceID in the configuration to skip the pairing
        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR, TEST_SUBDEVICE_ID)
        thingRegistry.add(subdevice)
        assertThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE)
    }

}
