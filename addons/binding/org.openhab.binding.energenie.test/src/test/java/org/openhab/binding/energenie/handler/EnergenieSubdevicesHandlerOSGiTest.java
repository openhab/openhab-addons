package org.openhab.binding.energenie.handler;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes;
import org.openhab.binding.energenie.internal.api.JsonDevice;
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.openhab.binding.energenie.internal.api.constants.JsonResponseConstants;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManagerImpl;
import org.openhab.binding.energenie.test.AbstractEnergenieOSGiTest;
import org.openhab.binding.energenie.test.EnergenieServlet;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;

public class EnergenieSubdevicesHandlerOSGiTest extends AbstractEnergenieOSGiTest {

    private Thing subdevice;

    public static final String PATH_LIST_SUBDEVICES = "/" + EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES + "/"
            + EnergenieApiManagerImpl.ACTION_LIST;
    public static final String PATH_SHOW_SUBDEVICE = "/" + EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES + "/"
            + EnergenieApiManagerImpl.ACTION_SHOW;

    private EnergenieServlet listSubdevicesServlet;
    private EnergenieServlet showSubdeviceServlet;

    // Subdevice information
    public static final int TEST_SUBDEVICE_ID = 53412;
    public static final EnergenieDeviceTypes TEST_SUBDEVICE_TYPE = EnergenieDeviceTypes.MOTION_SENSOR;
    public static final String TEST_SUBDEVICE_LABEL = JsonSubdevice.DEFAULT_LABEL;

    @Before
    public void setUp() {
        setUpServices();

        // Register Subdevices Servlets
        JsonSubdevice subdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE);
        JsonObject listSubdevice = generateJsonDevicesListServerResponse(JsonResponseConstants.RESPONSE_SUCCESS,
                subdevice);
        listSubdevicesServlet = new EnergenieServlet(listSubdevice.toString());
        registerServlet(PATH_LIST_SUBDEVICES, listSubdevicesServlet);

        JsonObject showSubdevice = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_SUCCESS,
                subdevice);
        showSubdeviceServlet = new EnergenieServlet(showSubdevice.toString());
        registerServlet(PATH_SHOW_SUBDEVICE, showSubdeviceServlet);

        // Register Gateway Servlets

        // in order to test subdevices we need to register servlet representing successful gateway registration
        JsonGateway gatewayDevice = createTestGateway();

        // Register servlet with content representing list of gateways. It is needed for the refresh thread.
        JsonObject listContent = generateJsonDevicesListServerResponse(JsonResponseConstants.RESPONSE_SUCCESS,
                gatewayDevice);
        registerServlet(PATH_LIST_GATEWAYS, new EnergenieServlet(listContent.toString()));

    }

    @After
    public void tearDown() {
        removeBridge(thingRegistry, gatewayThing);
        removeThing(thingRegistry, subdevice);

        // Unregister Servlets
        unregisterServlet(PATH_LIST_SUBDEVICES);
        unregisterServlet(PATH_SHOW_SUBDEVICE);
        unregisterServlet(PATH_LIST_GATEWAYS);
    }

    @Test
    public void assertSubdeviceDoesNotInitializeWhenBridgeIsOFFLINE() {
        // We set an invalid username, so the bridge will be set to OFFLINE by the handler
        String invalidUsername = "should_be_email";
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, invalidUsername, TEST_GATEWAY_ID, TEST_AUTH_CODE);
        thingRegistry.add(gatewayThing);

        assertGatewayStatus(ThingStatus.OFFLINE);

        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR);
        thingRegistry.add(subdevice);

        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
    }

    @Test
    public void assertSubdeviceDoesNotInitializeWhenGatewayIDIsMissing() {
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_ID, TEST_AUTH_CODE);
        thingRegistry.add(gatewayThing);

        assertGatewayStatus(ThingStatus.ONLINE);

        EnergenieGatewayHandler gatewayHandler = (EnergenieGatewayHandler) gatewayThing.getHandler();
        gatewayHandler.setGatewayId(0);

        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR,
                TEST_SUBDEVICE_ID);
        thingRegistry.add(subdevice);

        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
    }

    @Test
    public void assertInitializedThingGoesOFFLINEWhenTheGatewayGoesOFFLINE() {
        initializePairedThing();

        // Edit the configuration with invalid username
        ThingHandler handler = gatewayThing.getHandler();
        Configuration config = gatewayThing.getConfiguration();
        handler.handleConfigurationUpdate(config.getProperties());

        // As we have no servlet registered to return the gateway state (and the last_seen information), the bridge will
        // be set to OFFLINE state
        assertGatewayStatus(ThingStatus.OFFLINE);
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Test
    public void assertInitializedThingUpdatesTheConfigurationAndProperties() {
        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE);
        JsonObject showSubdeviceServletContent = generateShowJsonDeviceServerResponse("success", jsonSubdevice);
        showSubdeviceServlet.setContent(showSubdeviceServletContent.toString());

        initializePairedThing();

        assertThingConfiguration(subdevice.getConfiguration());
        assertThingProperties(subdevice.getProperties());
    }

    @Test
    public void assertSubdeviceHandlesConfigurationChange() {
        initializePairedThing();

        // Save the initial values
        EnergenieSubdevicesHandler esHandler = (EnergenieSubdevicesHandler) subdevice.getHandler();
        if (esHandler != null) {
            ScheduledFuture<?> initialUpdateTask = esHandler.getUpdateTask();
            String updateIntervalString = String
                    .valueOf(subdevice.getConfiguration().get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL));
            long initialUpdateInterval = Long.parseLong(updateIntervalString);

            // Update the configuration
            long newUpdateInterval = EnergenieSubdevicesHandler.DEFAULT_UPDATE_INTERVAL.longValue() + 10;
            Configuration config = subdevice.getConfiguration();
            config.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL, new BigDecimal(newUpdateInterval));
            thingRegistry.updateConfiguration(subdevice.getUID(), config.getProperties());

            waitForAssert(() -> {
                String modifiedUpdateIntervalString = String
                        .valueOf(subdevice.getConfiguration().get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL));
                long modifiedUpdateInterval = Long.parseLong(modifiedUpdateIntervalString);
                ScheduledFuture<?> newUpdateTask = esHandler.getUpdateTask();

                assertNotEquals("Configuration parameter " + EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL
                        + " wasn't changed", modifiedUpdateInterval, initialUpdateInterval);
                assertNotEquals("Update task wasn't restarted after changing the update interval", newUpdateTask,
                        initialUpdateInterval);
            });
        }
    }

    @Test
    public void assertSubdeviceHandlerLocationChange() {
        initializePairedThing();

        Thing updatedThing = createThing(thingRegistry, gatewayThing,
                EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR, TEST_SUBDEVICE_ID);
        String newLocation = "Bedroom";
        updatedThing.setLocation(newLocation);
        managedThingProvider.update(updatedThing);

        waitForAssert(() -> assertEquals(subdevice.getLocation(), newLocation));
    }

    @Test
    public void assertMotionSensorUpdateStateToOFF() {
        Integer sensorState = 0;
        State expectedState = OnOffType.OFF;
        String channelID = EnergenieBindingConstants.CHANNEL_STATE;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.MOTION_SENSOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertMotionSensorUpdateStateToON() {
        Integer sensorState = 1;
        State expectedState = OnOffType.ON;
        String channelID = EnergenieBindingConstants.CHANNEL_STATE;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.MOTION_SENSOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertMotionSensorUpdatesStateToNULL() {
        Integer sensorState = null;
        State expectedState = UnDefType.NULL;
        String channelID = EnergenieBindingConstants.CHANNEL_STATE;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.MOTION_SENSOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertOpenSensorUpdatesStateToCLOSED() {
        Integer sensorState = 0;
        State expectedState = OpenClosedType.CLOSED;
        String channelID = EnergenieBindingConstants.CHANNEL_STATE;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.OPEN_SENSOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertOpenSensorUpdatesStateToOPEN() {
        Integer sensorState = 1;
        State expectedState = OpenClosedType.OPEN;
        String channelID = EnergenieBindingConstants.CHANNEL_STATE;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.OPEN_SENSOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertOpenSensorUpdatesStateToNULL() {
        Integer sensorState = null;
        State expectedState = UnDefType.NULL;
        String channelID = EnergenieBindingConstants.CHANNEL_STATE;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.OPEN_SENSOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, sensorState);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertHouseMonitorUpdatesVoltage() {
        double voltage = 4.3646;
        State expectedState = new DecimalType(voltage);
        String channelID = EnergenieBindingConstants.CHANNEL_VOLTAGE;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.HOUSE_MONITOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, voltage, 0,
                0);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertHouseMonitorUpdatesPower() {
        int power = 56;
        State expectedState = new DecimalType(power);
        String channelID = EnergenieBindingConstants.CHANNEL_REAL_POWER;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.HOUSE_MONITOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, 0, power, 0);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertHouseMonitorUpdatesConsumption() {
        int consumption = 563;
        State expectedState = new DecimalType(consumption);
        String channelID = EnergenieBindingConstants.CHANNEL_TODAY_CONSUMPTION;
        EnergenieDeviceTypes subdeviceType = EnergenieDeviceTypes.HOUSE_MONITOR;

        JsonSubdevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, subdeviceType, 0, 0,
                consumption);

        testDeviceState(subdeviceType, channelID, jsonSubdevice, expectedState);
    }

    @Test
    public void assertSubdeviceWillNotBeRemovedWhenServerIsOFFLINE() throws InterruptedException {
        initializePairedThing();

        // We haven`t registered servlet to /subdevices/delete
        thingRegistry.remove(subdevice.getUID());
        Thread.sleep(1000);
        assertThingStatus(ThingStatus.REMOVING, ThingStatusDetail.NONE);
    }

    @Test
    public void assertThingStatusIsOFFLINEOnREFRESHWhenDeviceIsRemovedFromTheMiHomeServer() {
        JsonDevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE);
        JsonObject showSubdeviceServletContent = generateShowJsonDeviceServerResponse(
                JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice);
        showSubdeviceServlet.setContent(showSubdeviceServletContent.toString());

        initializePairedThing();

        assertGatewayStatus(ThingStatus.ONLINE);
        assertThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

        // The device has been removed from the MiHome server
        showSubdeviceServletContent = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_NOT_FOUND,
                jsonSubdevice);
        showSubdeviceServlet.setContent(showSubdeviceServletContent.toString());

        // Send a command to refresh the channel state
        Channel channelState = subdevice.getChannel(EnergenieBindingConstants.CHANNEL_STATE);
        subdevice.getHandler().handleCommand(channelState.getUID(), RefreshType.REFRESH);

        assertGatewayStatus(ThingStatus.ONLINE);
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void assertThingStatusIsOFFLINEWhenDeviceIsRemovedFromTheMiHomeServer() throws InterruptedException {
        JsonDevice jsonSubdevice = new JsonSubdevice(TEST_SUBDEVICE_ID, TEST_GATEWAY_ID, TEST_SUBDEVICE_TYPE);
        JsonObject showSubdeviceServletContent = generateShowJsonDeviceServerResponse(
                JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice);
        showSubdeviceServlet.setContent(showSubdeviceServletContent.toString());

        initializePairedThing();

        assertGatewayStatus(ThingStatus.ONLINE);
        assertThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

        // The device has been removed from the MiHome server
        showSubdeviceServletContent = generateShowJsonDeviceServerResponse(JsonResponseConstants.RESPONSE_NOT_FOUND,
                jsonSubdevice);
        showSubdeviceServlet.setContent(showSubdeviceServletContent.toString());

        // Wait for the refresh thread
        Thread.sleep(TEST_SUBDEVICE_UPDATE_INTERVAL * 1000);

        assertGatewayStatus(ThingStatus.ONLINE);
        assertThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    private void testDeviceState(EnergenieDeviceTypes subdeviceType, String channelID, JsonSubdevice jsonSubdevice,
            State expectedState) {
        JsonObject showSubdeviceServletContent = generateShowJsonDeviceServerResponse(
                JsonResponseConstants.RESPONSE_SUCCESS, jsonSubdevice);
        showSubdeviceServlet.setContent(showSubdeviceServletContent.toString());

        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_ID, TEST_AUTH_CODE);
        thingRegistry.add(gatewayThing);
        assertGatewayStatus(ThingStatus.ONLINE);

        // We create a thing that has a deviceID in the configuration to skip the pairing
        ThingTypeUID thingTypeUID = EnergenieBindingConstants.DEVICE_TYPE_TO_THING_TYPE.get(subdeviceType);
        subdevice = createThing(thingRegistry, gatewayThing, thingTypeUID, TEST_SUBDEVICE_ID);
        thingRegistry.add(subdevice);
        assertThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

        Channel stateChannel = subdevice.getChannel(channelID);
        assertNotNull(stateChannel);
        ChannelUID stateChannelUID = stateChannel.getUID();
        ItemChannelLinkRegistry linkRegistry = getService(ItemChannelLinkRegistry.class);
        assertNotNull("ItemChannelLinkRegistry is missing", linkRegistry);

        waitForAssert(() -> {
            Set<Item> items = linkRegistry.getLinkedItems(stateChannelUID);
            assertNotNull("No items linked to channel " + stateChannelUID.getAsString(), items);
            assertEquals("Single item was expected, but found " + items.size() + " items linked to channel "
                    + stateChannelUID.getAsString(), items.size(), 1);
            Item item = Iterables.get(items, 0);
            State state = item.getState();
            assertEquals("Unexpected state of item " + item, expectedState, state);
        });
    }

    private void assertThingConfiguration(Configuration configuration) {
        String updateIntervalString = String
                .valueOf(configuration.get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL));
        long updateInterval = Long.parseLong(updateIntervalString);
        assertNotNull("Configuration parameter " + EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL + " is missing",
                updateInterval);
        BigDecimal expectedInterval = new BigDecimal(TEST_SUBDEVICE_UPDATE_INTERVAL);
        assertEquals("Unexpected value of configuration parameter " + EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL,
                expectedInterval, updateInterval);
    }

    private void assertThingProperties(Map<String, String> properties) {
        String id = properties.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_DEVICE_ID + " is missing", id);
        assertEquals("Unexpected value of property " + EnergenieBindingConstants.PROPERTY_DEVICE_ID,
                Integer.toString(TEST_SUBDEVICE_ID), id);
        String gatewayId = properties.get(EnergenieBindingConstants.PROPERTY_GATEWAY_ID);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_GATEWAY_ID + " is missing", gatewayId);
        assertEquals("Unexpected value of property " + EnergenieBindingConstants.PROPERTY_GATEWAY_ID,
                Integer.toString(TEST_GATEWAY_ID), gatewayId);
        String type = properties.get(EnergenieBindingConstants.PROPERTY_TYPE);
        assertNotNull("Property " + EnergenieBindingConstants.PROPERTY_TYPE + " is missing", type);
        assertEquals("Unexpected value of property " + EnergenieBindingConstants.PROPERTY_TYPE,
                TEST_SUBDEVICE_TYPE.toString(), type);
    }

    private void assertGatewayStatus(ThingStatus expectedStatus) {
        waitForAssert(() -> {
            BridgeHandler gatewayHandler = gatewayThing.getHandler();
            assertNotNull(gatewayHandler);

            ThingStatus status = gatewayThing.getStatus();
            ThingStatusInfo info = gatewayThing.getStatusInfo();
            assertEquals("Unexpected gateway status. ThingStatus detail is " + info.getStatusDetail()
                    + " and description is " + info.getDescription(), expectedStatus, status);
        });
    }

    private void assertThingStatus(ThingStatus expectedStatus, ThingStatusDetail expectedStatusDetail) {
        waitForAssert(() -> {
            ThingHandler handler = subdevice.getHandler();
            assertNotNull(handler);
            ThingStatus status = handler.getThing().getStatus();
            ThingStatusInfo info = handler.getThing().getStatusInfo();
            ThingStatusDetail detail = info.getStatusDetail();
            assertEquals("Unexpected thing status. ThingStatus description is " + info.getDescription(), expectedStatus,
                    status);
            assertEquals("Unexpected thing status detail. ThingStatus description is ${info.getDescription()}",
                    expectedStatusDetail, detail);
        });
    }

    private void initializePairedThing() {
        gatewayThing = createBridge(thingRegistry, TEST_PASSWORD, TEST_USERNAME, TEST_GATEWAY_ID, TEST_AUTH_CODE);
        thingRegistry.add(gatewayThing);
        assertGatewayStatus(ThingStatus.ONLINE);

        // We create a thing that has a deviceID in the configuration to skip the pairing
        subdevice = createThing(thingRegistry, gatewayThing, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR,
                TEST_SUBDEVICE_ID);
        thingRegistry.add(subdevice);
        assertThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

}
