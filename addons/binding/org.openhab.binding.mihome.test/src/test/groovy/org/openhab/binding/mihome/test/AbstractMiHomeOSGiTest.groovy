/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.time.*

import javax.servlet.http.HttpServlet

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.BridgeHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.test.OSGiTest
import org.openhab.binding.mihome.MiHomeBindingConstants
import org.openhab.binding.mihome.internal.api.JSONResponseHandler
import org.openhab.binding.mihome.internal.api.manager.*
import org.openhab.binding.mihome.internal.rest.RestClient
import org.osgi.service.http.HttpService

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

/**
 * Base class for OSGi Tests
 *
 * @author Svilen Valkanov
 *
 */
public abstract class AbstractMiHomeOSGiTest extends OSGiTest {

    // Server information
    public static final String PROTOCOL = "http"
    public static final String HOST = "localhost"
    public static final int SECURE_PORT = 9090;
    public static final String TEST_URL = "${PROTOCOL}://${HOST}:${SECURE_PORT}";

    //Gateway information used for the purposes of the tests
    public static final String TEST_PASSWORD = "pass"
    public static final String TEST_USERNAME = "test@my.com"
    public static final String TEST_GATEWAY_CODE = "FGSG5RSDFS"
    public static final int TEST_UPDATE_INTERVAL = 10
    public static final int TEST_GATEWAY_ID = 4541

    /**
     * Test update interval for the subdevices in seconds
     */
    public static final BigDecimal TEST_SUBDEVICE_UPDATE_INTERVAL = 1

    public static final String PATH_CREATE_GATEWAY = "/${MiHomeApiManagerImpl.CONTROLLER_DEVICES}/${MiHomeApiManagerImpl.ACTION_CREATE}"
    public static final String PATH_LIST_GATEWAYS = "/${MiHomeApiManagerImpl.CONTROLLER_DEVICES}/${MiHomeApiManagerImpl.ACTION_LIST}"
    public static final String PATH_DELETE_GATEWAYS = "/${MiHomeApiManagerImpl.CONTROLLER_DEVICES}/${MiHomeApiManagerImpl.ACTION_DELETE}"

    protected ManagedThingProvider managedThingProvider
    protected ThingRegistry thingRegistry
    protected HttpService httpService

    protected RestClient client
    protected Bridge gatewayThing

    protected Bridge createBridge(ThingRegistry thingRegistry, String apiKey, String userName, String gatewayCode) {
        return createBridge(thingRegistry, apiKey, userName, gatewayCode, null)
    }

    protected Bridge createBridge(ThingRegistry thingRegistry, String apiKey, String userName, String gatewayCode, Integer gatewayID) {
        Map<String,Object> properties = new HashMap<String, Object>()
        properties.put(MiHomeBindingConstants.CONFIG_PASSWORD, apiKey)
        properties.put(MiHomeBindingConstants.CONFIG_USERNAME, userName)
        properties.put(MiHomeBindingConstants.CONFIG_GATEWAY_CODE, gatewayCode)
        // We set the deviceID to pretend that the device registration has passed correctly
        if(gatewayID != null) {
            properties.put(MiHomeBindingConstants.PROPERTY_DEVICE_ID, new BigDecimal(gatewayID))
        }
        String bridgeID
        if(gatewayID != null) {
            bridgeID = Integer.toString(gatewayID)
        } else {
            bridgeID = "Unknown"
        }

        Configuration configuration = new Configuration(properties)

        Bridge gateway = thingRegistry.createThingOfType(
                MiHomeBindingConstants.THING_TYPE_GATEWAY,
                new ThingUID(MiHomeBindingConstants.THING_TYPE_GATEWAY, bridgeID),
                null, "Label", configuration)
        return gateway
    }
    protected Thing createThing (ThingRegistry thingRegistry, Bridge bridge, ThingTypeUID thingTypeUID) {
        return createThing(thingRegistry, bridge, thingTypeUID, null)
    }

    protected Thing createThing (ThingRegistry thingRegistry, Bridge bridge, ThingTypeUID thingTypeUID, Integer subdeviceID) {
        Map<String,Object> properties = new HashMap<String, Object>()
        properties.put(MiHomeBindingConstants.CONFIG_UPDATE_ITNERVAL, TEST_SUBDEVICE_UPDATE_INTERVAL)

        // We set the deviceID to skip the new device registration
        if(subdeviceID != null) {
            properties.put(MiHomeBindingConstants.PROPERTY_DEVICE_ID, new BigDecimal(subdeviceID))
        }
        Configuration configuration = new Configuration(properties)

        String thingID
        if(subdeviceID != null) {
            thingID = Integer.toString(subdeviceID)
        } else {
            thingID = "Unknown"
        }
        Thing thing = thingRegistry.createThingOfType(
                thingTypeUID,
                new ThingUID(thingTypeUID,thingID),
                bridge.getUID(), JsonSubdevice.DEFAULT_LABEL, configuration)

        return thing
    }

    protected void removeBridge(ThingRegistry thingRegistry, Bridge gateway) {
        if(gateway !=null) {
            thingRegistry.forceRemove(gateway.getUID())
            waitForAssert {
                BridgeHandler gatewayHandler = gateway.getHandler()
                assertThat "The bridge ${gateway.getUID()} cannot be deleted", gatewayHandler, is(nullValue())
            }
        }
    }

    protected void removeThing(ThingRegistry thingRegistry, Thing thing) {
        if(thing !=null) {
            thingRegistry.forceRemove(thing.getUID())
            waitForAssert {
                ThingHandler thingHandler = thing.getHandler()
                assertThat "The thing ${thing.getUID()} cannot be deleted", thingHandler, is(nullValue())
            }
        }
    }

    protected JsonObject generateShowJsonDeviceServerResponse(String status, JsonDevice device) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        ShowDeviceResponse responseObj = new ShowDeviceResponse(status, device)
        String response = gson.toJson(responseObj)
        return JSONResponseHandler.responseStringtoJsonObject(response)
    }

    protected JsonObject generateJsonDevicesListServerResponse(String status, JsonDevice ... devices){
        Gson gson = new Gson();
        ListDevicesResponse responseObj = new ListDevicesResponse(devices, status)
        String response = gson.toJson(responseObj)
        return JSONResponseHandler.responseStringtoJsonObject(response)
    }

    protected void registerServlet(String path, HttpServlet servlet){
        try {
            httpService.registerServlet(path, servlet, null, null)
        } catch (Exception e) {
        }
    }

    protected void unregisterServlet(String path) {
        try {
            httpService.unregister(path)
        } catch (IllegalArgumentException e) {
            // Do nothing, the servlet hasn't been registered
        }
    }

    // Helper classes to build the server response

    /**
     * Represents a response to the Mi|Home REST API for the requests /subdevices/show and /devices/create
     */
    class ShowDeviceResponse {
        JsonDevice data;
        String status

        ShowDeviceResponse(String status, JsonDevice data) {
            this.data = data
            this.status = status
        }
    }

    /**
     * Represents a response to the Mi|Home REST API for the request /subdevices/list and devices/list
     */
    class ListDevicesResponse {
        JsonDevice [] data;
        String status

        ListDevicesResponse(JsonDevice [] data, String status) {
            this.data = data
            this.status = status
        }
    }

    public void setUpServices(){
        registerVolatileStorageService()

        thingRegistry = getService(ThingRegistry)
        assertThat thingRegistry, is(notNullValue())

        httpService = getService(HttpService)
        assertThat httpService, is(notNullValue())

        managedThingProvider = getService(ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())

        client = getService(RestClient)
        client.setBaseURL(TEST_URL)
    }
}
