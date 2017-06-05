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

import java.time.*
import java.time.format.DateTimeFormatter

import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openhab.binding.energenie.EnergenieBindingConstants
import org.openhab.binding.energenie.internal.api.JsonGateway
import org.openhab.binding.energenie.internal.api.constants.*
import org.openhab.binding.energenie.test.AbstractEnergenieOSGiTest
import org.openhab.binding.energenie.test.EnergenieServlet

/**
 * Tests for the {@link MiHomeGatewayHandler}
 *
 * @author Mihaela Memova
 *
 */
class EnergenieGatewayHandlerOSGiTest extends AbstractEnergenieOSGiTest {

    VolatileStorageService volatileStorageService = new VolatileStorageService()

    @Before
    public void setUp(){
        setUpServices()
    }

    @After
    public void tearDown(){
        removeBridge(thingRegistry, gatewayThing)
        unregisterServlet(PATH_LIST_GATEWAYS)
        unregisterServlet(PATH_CREATE_GATEWAY)
    }

    @Test
    public void 'assert OFFLINE gateway status when username is not an email address'() {

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, "not-email", TEST_GATEWAY_CODE, TEST_GATEWAY_ID)
        assertThat gatewayThing,is(notNullValue())

        thingRegistry.add(gatewayThing)

        waitForAssert({
            assertThat gatewayThing.getHandler(), is(notNullValue())
            assertThat gatewayThing.getStatus(), is(ThingStatus.OFFLINE)
        })
    }

    @Test
    public void 'assert OFFLINE gateway status when gateway code is not valid'() {

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, "notTenCapitalLetters", TEST_GATEWAY_ID)
        assertThat gatewayThing,is(notNullValue())

        thingRegistry.add(gatewayThing)

        waitForAssert({
            assertThat gatewayThing.getHandler(), is(notNullValue())
            assertThat gatewayThing.getStatus(), is(ThingStatus.OFFLINE)
        })
    }


    @Test
    public void 'assert initialized Thing updates the properties' () {
        // register servlet representing successful gateway registration
        JsonGateway gatewayDevice = createTestGateway()
        String content = generateShowJsonDeviceServerResponse(JSONResponseConstants.RESPONSE_SUCCESS, gatewayDevice)
        EnergenieServlet successfullGatewayRegistrationServlet = new EnergenieServlet(content)
        registerServlet(PATH_CREATE_GATEWAY, successfullGatewayRegistrationServlet)

        // create gateway Thing
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID)
        assertThat gatewayThing, is(notNullValue())

        thingRegistry.add(gatewayThing)

        waitForAssert{
            assertThat gatewayThing.getHandler(), is(notNullValue())
            assertThat gatewayThing.getStatus(), is(ThingStatus.ONLINE)
        }

        // verify that the gateway thing's properties are equal to their corresponding gatewayDevice's values
        verifyGatewayProperties(gatewayThing, gatewayDevice)
    }

    @Test
    public void 'assert OFLLINE status of a gateway that has not been active for more than two minutes'() {

        // register servlet representing successful gateway registration
        JsonGateway gatewayDevice = createTestGateway()
        String succesfulRegistrationServletContent = generateShowJsonDeviceServerResponse(JSONResponseConstants.RESPONSE_SUCCESS, gatewayDevice)
        EnergenieServlet successfullRegistrationServlet = new EnergenieServlet(succesfulRegistrationServletContent)
        registerServlet(PATH_CREATE_GATEWAY, successfullRegistrationServlet)

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID)
        assertThat gatewayThing, is(notNullValue())

        thingRegistry.add(gatewayThing)

        EnergenieGatewayHandler gatewayHandler = gatewayThing.getHandler()

        waitForAssert({
            assertThat gatewayHandler, is(notNullValue())
            assertThat gatewayThing.getStatus(), is(ThingStatus.ONLINE)
        })

        /* Sets the 'inactive' period of the gateway to be more than two minutes
         * in order to verify its status update
         */
        ZonedDateTime curentDateTime = ZonedDateTime.now()
        ZonedDateTime previousDateTime = curentDateTime.minusMinutes(3)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(EnergenieBindingConstants.DATE_TIME_PATTERN)
        String previousDate = formatter.format(previousDateTime)

        gatewayDevice.setLastSeenAt(previousDate)
        String listGatewaysServletContent = generateJsonDevicesListServerResponse(JSONResponseConstants.RESPONSE_SUCCESS, gatewayDevice)
        EnergenieServlet listGatewaysServlet = new EnergenieServlet(listGatewaysServletContent)
        registerServlet(PATH_LIST_GATEWAYS, listGatewaysServlet)

        waitForAssert ({
            assertThat gatewayThing.getStatus(), is(ThingStatus.OFFLINE)
        }, getWaitingTime(gatewayHandler))
    }

    @Test
    public void 'assert that changing the gateway configuration is handled properly'() {

        // register servlet representing successful gateway registration
        JsonGateway gatewayDevice = createTestGateway()
        String content = generateShowJsonDeviceServerResponse(JSONResponseConstants.RESPONSE_SUCCESS, gatewayDevice)
        EnergenieServlet successfullGatewayRegistrationServlet = new EnergenieServlet(content)
        registerServlet(PATH_CREATE_GATEWAY, successfullGatewayRegistrationServlet)

        //Create a gateway with valid configuration and verify its online status
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID)
        assertThat gatewayThing, is(notNullValue())

        thingRegistry.add(gatewayThing)

        EnergenieGatewayHandler gatewayHandler = gatewayThing.getHandler()

        waitForAssert({
            assertThat gatewayHandler, is(notNullValue())
            assertThat gatewayThing.getStatus(), is(ThingStatus.ONLINE)
        })

        // update the gateway with a non-valid configuration (email address and gateway code) and verify its offline status
        HashMap invalidConfig = new HashMap().with {
            put(EnergenieBindingConstants.CONFIG_USERNAME, "notValidEmailAddress")
            put(EnergenieBindingConstants.CONFIG_GATEWAY_CODE, "not10capitalLetters")
            put(EnergenieBindingConstants.CONFIG_PASSWORD, TEST_PASSWORD)
            put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL, new BigDecimal(TEST_UPDATE_INTERVAL))
            it
        }

        gatewayHandler.handleConfigurationUpdate(invalidConfig)

        waitForAssert {
            assertThat gatewayThing.getStatus(), is(ThingStatus.OFFLINE)
        }

        // change the configuration back to valid and verify gateway's online status
        HashMap validConfig = new HashMap().with {
            put(EnergenieBindingConstants.CONFIG_USERNAME, TEST_USERNAME)
            put(EnergenieBindingConstants.CONFIG_GATEWAY_CODE, TEST_GATEWAY_CODE)
            put(EnergenieBindingConstants.CONFIG_PASSWORD, TEST_PASSWORD)
            put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL, new BigDecimal(TEST_UPDATE_INTERVAL))
            it
        }
        gatewayHandler.handleConfigurationUpdate(validConfig)

        waitForAssert ({
            assertThat gatewayThing.getStatus(), is(ThingStatus.ONLINE)
        }, getWaitingTime(gatewayHandler))
    }

    @Test
    public void 'assert offline status of a gateway that has been unregistered'() {

        // register servlet representing successful gateway registration
        JsonGateway gatewayDevice = createTestGateway()
        String showContent = generateShowJsonDeviceServerResponse(JSONResponseConstants.RESPONSE_SUCCESS, gatewayDevice)
        EnergenieServlet successfullGatewayRegistrationServlet = new EnergenieServlet(showContent)
        registerServlet(PATH_CREATE_GATEWAY, successfullGatewayRegistrationServlet)

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID)
        assertThat gatewayThing,is(notNullValue())

        // Register servlet which returns a list of gateways containing the one we need
        String listContent = generateJsonDevicesListServerResponse(JSONResponseConstants.RESPONSE_SUCCESS, gatewayDevice)
        EnergenieServlet nonEmptyGatewaysListServlet = new EnergenieServlet(listContent)
        registerServlet(PATH_CREATE_GATEWAY, nonEmptyGatewaysListServlet)

        thingRegistry.add(gatewayThing)

        EnergenieGatewayHandler gatewayHandler = gatewayThing.getHandler();
        waitForAssert({
            assertThat gatewayHandler, is(notNullValue())
            assertThat gatewayThing.getStatus(), is(ThingStatus.ONLINE)
        }, getWaitingTime(gatewayHandler))

        unregisterServlet(PATH_LIST_GATEWAYS)

        // Unregistering of a gateway can be represented by registering a servlet which returns an empty gateways list '
        String emptyListContent = generateJsonDevicesListServerResponse(JSONResponseConstants.RESPONSE_SUCCESS)
        EnergenieServlet empyListGatewaysServlet = new EnergenieServlet(emptyListContent)
        registerServlet(PATH_LIST_GATEWAYS, empyListGatewaysServlet)

        /*
         * When a gateway has been unregistered, the refresh thread should take care of the status update.
         * That thread is executed periodically so the timeout to wait for the status update should be equal to
         * the thread's refresh interval
         */
        waitForAssert ({
            assertThat gatewayThing.getStatus(), is(ThingStatus.OFFLINE)
        }, getWaitingTime(gatewayHandler))
    }

    @Test
    public void 'assert that thing is successfuly removed when the unregister gateway request is successfull'() {
        // register servlet representing successful gateway registration
        JsonGateway registeredGateway = createTestGateway()
        String succesfulRegistrationServletContent = generateShowJsonDeviceServerResponse(JSONResponseConstants.RESPONSE_SUCCESS, registeredGateway)
        EnergenieServlet successfullRegistrationServlet = new EnergenieServlet(succesfulRegistrationServletContent)
        registerServlet(PATH_CREATE_GATEWAY, successfullRegistrationServlet)

        // Create a gateway thing and verify its online status
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_CODE, TEST_GATEWAY_ID)
        assertThat gatewayThing,is(notNullValue())

        thingRegistry.add(gatewayThing)
        EnergenieGatewayHandler gatewayHandler = gatewayThing.getHandler()

        waitForAssert({
            assertThat gatewayHandler, is(notNullValue())
            assertThat gatewayThing.getStatus(), is(ThingStatus.ONLINE)
        })

        /* Register servlet representing successful gateway deletion. According to the Mi|Home API,
         * when a deletion request is successful the server will respond with details of the removed device.
         * So basically the response is the same as the response from successful registration."
         */
        JsonGateway deletedGateway = createTestGateway()
        String succesfulDeletionServletContent = generateShowJsonDeviceServerResponse(JSONResponseConstants.RESPONSE_SUCCESS, deletedGateway)
        EnergenieServlet successfullDeletionServlet = new EnergenieServlet(succesfulDeletionServletContent)
        registerServlet(PATH_DELETE_GATEWAYS, successfullDeletionServlet)

        //try to remove the gateway thing
        thingRegistry.remove(gatewayThing.getUID())

        // verify that the removal was successful
        waitForAssert({
            gatewayHandler = gatewayThing.getHandler()
            assertThat gatewayHandler, is(nullValue())
        })
    }
    public void verifyGatewayProperties(Bridge gatewayThing, JsonGateway gatewayDevice) {
        Properties props = gatewayThing.getProperties()

        String expectedType = gatewayDevice.getType()
        String deviceType = props.getProperty(EnergenieBindingConstants.PROPERTY_TYPE)
        assertThat "Property ${EnergenieBindingConstants.PROPERTY_TYPE} is missing", deviceType, is(notNullValue())
        assertThat "Unexpected value of the parameter ${EnergenieBindingConstants.PROPERTY_TYPE}", deviceType, is(equalTo(expectedType))

        String expectedId = gatewayDevice.getID()
        String deviceId = props.getProperty(EnergenieBindingConstants.PROPERTY_DEVICE_ID)
        assertThat "Property ${EnergenieBindingConstants.PROPERTY_DEVICE_ID} is missing", deviceId, is(notNullValue())
        assertThat "Unexpected value of the parameter ${EnergenieBindingConstants.PROPERTY_DEVICE_ID}", deviceId, is(equalTo(expectedId))

        String expectedUserId = gatewayDevice.getUserID()
        String userId = props.getProperty(EnergenieBindingConstants.PROPERTY_USER_ID)
        assertThat "Property ${EnergenieBindingConstants.PROPERTY_USER_ID} is missing",userId,is(notNullValue())
        assertThat "Unexpected value of the parameter ${EnergenieBindingConstants.PROPERTY_USER_ID}", userId, is(equalTo(expectedUserId))

        String expectedMac = gatewayDevice.getMacAddress()
        String deviceMac = props.getProperty(EnergenieBindingConstants.PROPERTY_MAC_ADDRESS)
        assertThat "Property ${EnergenieBindingConstants.PROPERTY_MAC_ADDRESS} is missing", deviceMac, is(notNullValue())
        assertThat "Unexpected value of the parameter ${EnergenieBindingConstants.PROPERTY_MAC_ADDRESS}", deviceMac, is(equalTo(expectedMac))

        String expectedIp = gatewayDevice.getIpAddress()
        String ip = props.getProperty(EnergenieBindingConstants.PROPERTY_IP_ADDRESS)
        assertThat "Property ${EnergenieBindingConstants.PROPERTY_IP_ADDRESS} is missing", ip ,is(notNullValue())
        assertThat "Unexpected value of the parameter ${EnergenieBindingConstants.PROPERTY_IP_ADDRESS}", ip, is(equalTo(expectedIp))

        String expectedPort = gatewayDevice.getPort()
        String port = props.getProperty(EnergenieBindingConstants.PROPERTY_PORT)
        assertThat "Property ${EnergenieBindingConstants.PROPERTY_PORT} is missing", port, is(notNullValue())
        assertThat "Unexpected value of the parameter ${EnergenieBindingConstants.PROPERTY_PORT}",port, is(equalTo(expectedPort))

        String expectedFirmaware = gatewayDevice.getFirmwareVersionID()
        String firmware = props.getProperty(EnergenieBindingConstants.PROPERTY_FIRMWARE_VERSION)
        assertThat "Property ${EnergenieBindingConstants.PROPERTY_FIRMWARE_VERSION} is missing", firmware, is(notNullValue())
        assertThat "Unexpected value of the parameter ${EnergenieBindingConstants.PROPERTY_FIRMWARE_VERSION}", firmware, is(equalTo(expectedFirmaware))
    }

    public int getWaitingTime(EnergenieGatewayHandler handler) {
        /*
         * When a gateway has been unregistered, the refresh thread should take care of the status update.
         * That thread is executed periodically so the timeout to wait for the status update should be a little bit longer than
         * the thread's refresh interval
         */
        return handler.getUpdateInterval().intValue() * 1000 + 200
    }
}
