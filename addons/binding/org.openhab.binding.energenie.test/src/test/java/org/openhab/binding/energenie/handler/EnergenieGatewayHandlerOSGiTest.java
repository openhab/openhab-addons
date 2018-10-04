package org.openhab.binding.energenie.handler;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.test.java.JavaTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.constants.JsonResponseConstants;
import org.openhab.binding.energenie.test.AbstractEnergenieOSGiTest;
import org.openhab.binding.energenie.test.EnergenieServlet;

import com.google.gson.JsonObject;

public class EnergenieGatewayHandlerOSGiTest extends AbstractEnergenieOSGiTest {

    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        setUpServices();
    }

    @After
    public void tearDown() {
        if (gatewayThing != null) {
            removeBridge(thingRegistry, gatewayThing);
            unregisterServlet(PATH_LIST_GATEWAYS);
            unregisterServlet(PATH_FIRMWARE_INFORMATION);
        }
    }

    @Test
    public void аssertOFFLINEGatewayStatusWhenUsernameIsNotEmailAddress() {
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, "not-email", TEST_GATEWAY_ID);
        assertNotNull(gatewayThing);

        thingRegistry.add(gatewayThing);

        waitForAssert(() -> {
            assertNotNull(gatewayThing.getHandler());
            assertEquals(ThingStatus.OFFLINE, gatewayThing.getStatus());
        });
    }

    @Test
    public void assertInitializedThingUpdatesTheProperties() {
        // register servlet representing successful gateway registration
        JsonGateway gatewayDevice = createTestGateway();

        // create gateway Thing
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_ID);
        assertNotNull(gatewayThing);

        thingRegistry.add(gatewayThing);

        waitForAssert(() -> {
            assertNotNull(gatewayThing.getHandler());
            assertEquals(ThingStatus.ONLINE, gatewayThing.getStatus());
        });

        // verify that the gateway thing's properties are equal to their corresponding gatewayDevice's values
        verifyGatewayProperties(gatewayThing, gatewayDevice);
    }

    @Test
    public void assertOFFLINEStatusOfAGatewayThatHasNotBeenActiveForMoreThanTwoMinutes() {

        // register servlet representing successful gateway registration
        JsonGateway gatewayDevice = createTestGateway();
        // JsonObject succesfulRegistrationServletContentResponse = generateShowJsonDeviceServerResponse(
        // JsonResponseConstants.RESPONSE_SUCCESS, gatewayDevice);
        // String content = succesfulRegistrationServletContentResponse.toString();
        // EnergenieServlet successfullRegistrationServlet = new EnergenieServlet(content);

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_ID);
        assertNotNull(gatewayThing);

        thingRegistry.add(gatewayThing);

        EnergenieGatewayHandler gatewayHandler = (EnergenieGatewayHandler) gatewayThing.getHandler();

        waitForAssert(() -> {
            assertNotNull(gatewayHandler);
            assertEquals(ThingStatus.ONLINE, gatewayThing.getStatus());
        });

        /*
         * Sets the 'inactive' period of the gateway to be more than two minutes
         * in order to verify its status update
         */
        ZonedDateTime curentDateTime = ZonedDateTime.now();
        ZonedDateTime previousDateTime = curentDateTime.minusMinutes(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(EnergenieBindingConstants.DATE_TIME_PATTERN);
        String previousDate = formatter.format(previousDateTime);

        gatewayDevice.setLastSeenAt(previousDate);
        JsonObject listGatewaysServletContentResponse = generateJsonDevicesListServerResponse(
                JsonResponseConstants.RESPONSE_SUCCESS, gatewayDevice);
        String listGatewaysServletContent = listGatewaysServletContentResponse.toString();
        EnergenieServlet listGatewaysServlet = new EnergenieServlet(listGatewaysServletContent);
        registerServlet(PATH_LIST_GATEWAYS, listGatewaysServlet);

        waitForAssert(() -> {
            assertEquals(ThingStatus.OFFLINE, gatewayThing.getStatus());
        }, getWaitingTime(gatewayHandler), JavaTest.DFL_SLEEP_TIME);
    }

    public void verifyGatewayProperties(Bridge gatewayThing, JsonGateway gatewayDevice) {
        Map<String, String> props = gatewayThing.getProperties();

        String expectedType = gatewayDevice.getType().toString();
        String deviceType = props.get(EnergenieBindingConstants.PROPERTY_TYPE);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_TYPE + " is missing", deviceType);
        assertEquals("Unexpected value of the parameter " + EnergenieBindingConstants.PROPERTY_TYPE, expectedType,
                deviceType);

        String expectedId = String.valueOf(gatewayDevice.getID());
        String deviceId = props.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_DEVICE_ID + " is missing", deviceId);
        assertEquals("Unexpected value of the parameter " + EnergenieBindingConstants.PROPERTY_DEVICE_ID, expectedId,
                deviceId);

        String expectedUserId = String.valueOf(gatewayDevice.getUserID());
        String userId = props.get(EnergenieBindingConstants.PROPERTY_USER_ID);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_USER_ID + " is missing", userId);
        assertEquals("Unexpected value of the parameter " + EnergenieBindingConstants.PROPERTY_USER_ID, expectedUserId,
                userId);

        String expectedMac = gatewayDevice.getMacAddress();
        String deviceMac = props.get(EnergenieBindingConstants.PROPERTY_MAC_ADDRESS);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_MAC_ADDRESS + " is missing", deviceMac);
        assertEquals("Unexpected value of the parameter " + EnergenieBindingConstants.PROPERTY_MAC_ADDRESS, expectedMac,
                deviceMac);

        String expectedIp = gatewayDevice.getIpAddress();
        String ip = props.get(EnergenieBindingConstants.PROPERTY_IP_ADDRESS);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_IP_ADDRESS + " is missing", ip);
        assertEquals("Unexpected value of the parameter " + EnergenieBindingConstants.PROPERTY_IP_ADDRESS, expectedIp,
                ip);

        String expectedPort = String.valueOf(gatewayDevice.getPort());
        String port = props.get(EnergenieBindingConstants.PROPERTY_PORT);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_PORT + " is missing", port);
        assertEquals("Unexpected value of the parameter " + EnergenieBindingConstants.PROPERTY_PORT, expectedPort,
                port);

        String expectedFirmware = gatewayDevice.getFirmwareVersionID();
        String firmware = props.get(EnergenieBindingConstants.PROPERTY_FIRMWARE_VERSION);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_FIRMWARE_VERSION + " is missing", firmware);
        assertEquals("Unexpected value of the parameter " + EnergenieBindingConstants.PROPERTY_FIRMWARE_VERSION,
                expectedFirmware, firmware);
    }

    public long getWaitingTime(EnergenieGatewayHandler handler) {
        /*
         * When a gateway has been unregistered, the refresh thread should take care of the status update.
         * That thread is executed periodically so the timeout to wait for the status update should be a little bit
         * longer than the thread's refresh interval
         */
        return handler.getUpdateInterval() * 1000 + 200;
    }

}
