/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mybmw.internal.handler.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * this test tests the different MyBMWProxy request types (GET, POST) and their errors (SUCCESS, other)
 * 
 * @author Martin Grassl - initial contribution
 */
@NonNullByDefault
public class MyBMWHttpProxyTest {

    private final Logger logger = LoggerFactory.getLogger(MyBMWHttpProxyTest.class);

    @BeforeEach
    public void setupLogger() {
        Logger root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        if ("debug".equals(System.getenv("LOG_LEVEL"))) {
            ((ch.qos.logback.classic.Logger) root).setLevel(Level.DEBUG);
        } else if ("trace".equals(System.getenv("LOG_LEVEL"))) {
            ((ch.qos.logback.classic.Logger) root).setLevel(Level.TRACE);
        }

        logger.trace("tracing enabled");
        logger.debug("debugging enabled");
        logger.info("info enabled");
    }

    @Test
    void testWrongBrand() {
        // test successful GET for vehicle state
        String responseContent = FileReader.fileToString("responses/BEV/vehicles_state.json");
        MyBMWHttpProxy myBMWProxy = generateMyBmwProxy(200, responseContent);

        try {
            myBMWProxy.requestVehicleState("testVin", "WRONG_BRAND");
        } catch (NetworkException e) {
            assertEquals("Unknown Brand WRONG_BRAND", e.getMessage());
        }
    }

    @Test
    void testSuccessfulGet() {
        // test successful GET for vehicle state
        String responseContent = FileReader.fileToString("responses/BEV/vehicles_state.json");
        MyBMWHttpProxy myBMWProxy = generateMyBmwProxy(200, responseContent);

        try {
            VehicleStateContainer vehicleStateContainer = myBMWProxy.requestVehicleState("testVin",
                    BimmerConstants.BRAND_BMW);
            assertEquals(2686, vehicleStateContainer.getState().getCurrentMileage());
        } catch (NetworkException e) {
            fail(e.toString());
        }
    }

    @Test
    void testErrorGet() {
        // test successful GET for vehicle state
        String responseContent = FileReader.fileToString("responses/BEV/vehicles_state.json");
        MyBMWHttpProxy myBMWProxy = generateMyBmwProxy(400, responseContent);

        try {
            myBMWProxy.requestVehicleState("testVin", BimmerConstants.BRAND_BMW);

            fail("here an exception should be thrown");
        } catch (NetworkException e) {
            assertEquals(400, e.getStatus());
        }
    }

    @Test
    void testSuccessfulPost() {
        // test successful POST for remote service execution
        String responseContent = FileReader.fileToString("responses/MILD_HYBRID/remote_service_call.json");
        MyBMWHttpProxy myBMWProxy = generateMyBmwProxy(200, responseContent);

        try {
            ExecutionStatusContainer executionStatusContainer = myBMWProxy.executeRemoteServiceCall("testVin",
                    BimmerConstants.BRAND_BMW, RemoteService.LIGHT_FLASH);
            assertNotNull(executionStatusContainer.getCreationTime());
            assertNotNull(executionStatusContainer.getEventId());
            assertEquals("", executionStatusContainer.getEventStatus());
        } catch (NetworkException e) {
            fail(e.toString());
        }
    }

    @Test
    void testErrorPost() {
        // test successful POST for remote service execution
        String responseContent = FileReader.fileToString("responses/MILD_HYBRID/remote_service_call.json");
        MyBMWHttpProxy myBMWProxy = generateMyBmwProxy(400, responseContent);

        try {
            myBMWProxy.executeRemoteServiceCall("testVin", BimmerConstants.BRAND_BMW, RemoteService.LIGHT_FLASH);
            fail("here an exception should be thrown");
        } catch (NetworkException e) {
            assertEquals(400, e.getStatus());
        }
    }

    @Test
    void testSuccessfulImage() {
        // test successful POST for remote service execution
        MyBMWHttpProxy myBMWProxy = generateMyBmwProxy(200, "test");

        try {
            byte[] image = myBMWProxy.requestImage("testVin", BimmerConstants.BRAND_BMW, new ImageProperties());
            assertNotNull(image);
        } catch (NetworkException e) {
            fail(e.toString());
        }
    }

    @Test
    void testSuccessfulGetVehicles() {
        HttpClientFactory httpClientFactoryMock = Mockito.mock(HttpClientFactory.class);
        HttpClient httpClientMock = Mockito.mock(HttpClient.class);
        Mockito.when(httpClientFactoryMock.getCommonHttpClient()).thenReturn(httpClientMock);

        MyBMWBridgeConfiguration myBMWBridgeConfiguration = new MyBMWBridgeConfiguration();

        MyBMWHttpProxy myBMWProxyMock = Mockito
                .spy(new MyBMWHttpProxy(httpClientFactoryMock, myBMWBridgeConfiguration));

        String vehiclesBaseString = FileReader.fileToString("responses/BEV/vehicles_base.json");
        List<VehicleBase> baseVehicles = JsonStringDeserializer.getVehicleBaseList(vehiclesBaseString);

        String vehicleStateString = FileReader.fileToString("responses/BEV/vehicles_state.json");
        VehicleStateContainer vehicleStateContainer = JsonStringDeserializer.getVehicleState(vehicleStateString);

        try {
            doReturn(baseVehicles).when(myBMWProxyMock).requestVehiclesBase();
            doReturn(vehicleStateContainer).when(myBMWProxyMock).requestVehicleState(anyString(), anyString());

            List<Vehicle> vehicles = myBMWProxyMock.requestVehicles();

            logger.debug("found vehicles {}", vehicles.toString());

            assertNotNull(vehicles);
            assertEquals(1, vehicles.size());
            assertEquals("I20", vehicles.get(0).getVehicleBase().getAttributes().getBodyType());

        } catch (NetworkException e) {
            fail("vehicles not loaded properly", e);
        }
    }

    MyBMWHttpProxy generateMyBmwProxy(int statuscode, String responseContent) {
        HttpClientFactory httpClientFactoryMock = Mockito.mock(HttpClientFactory.class);
        HttpClient httpClientMock = Mockito.mock(HttpClient.class);
        Request requestMock = Mockito.mock(Request.class);
        Mockito.when(httpClientMock.newRequest(Mockito.anyString())).thenReturn(requestMock);
        Mockito.when(httpClientMock.POST(Mockito.anyString())).thenReturn(requestMock);
        MyBMWBridgeConfiguration myBMWBridgeConfiguration = new MyBMWBridgeConfiguration();
        Mockito.when(httpClientFactoryMock.getCommonHttpClient()).thenReturn(httpClientMock);

        ContentResponse responseMock = Mockito.mock(ContentResponse.class);
        Mockito.when(responseMock.getStatus()).thenReturn(statuscode);
        Mockito.when(responseMock.getContent()).thenReturn(responseContent.getBytes());
        Mockito.when(responseMock.getContentAsString()).thenReturn(responseContent);
        try {
            Mockito.when(requestMock.timeout(anyLong(), any())).thenReturn(requestMock);
            Mockito.when(requestMock.send()).thenReturn(responseMock);
        } catch (InterruptedException e1) {
            logger.error(e1.getMessage(), e1);
        } catch (TimeoutException e1) {
            logger.error(e1.getMessage(), e1);
        } catch (ExecutionException e1) {
            logger.error(e1.getMessage(), e1);
        }

        return new MyBMWHttpProxy(httpClientFactoryMock, myBMWBridgeConfiguration);
    }
}
