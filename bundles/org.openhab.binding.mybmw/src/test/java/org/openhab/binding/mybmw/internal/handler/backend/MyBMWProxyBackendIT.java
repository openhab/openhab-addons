/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.enums.ExecutionState;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.qos.logback.classic.Level;

/**
 * this integration test runs only if the connected account is set via environment variables
 * CONNECTED_USER
 * CONNECTED_PASSWORD
 * HCAPTCHA_TOKEN
 * 
 * if you want to execute the tests, please set the env variables and remove the disabled annotation
 *
 * @author Martin Grassl - initial contribution
 */
@NonNullByDefault
public class MyBMWProxyBackendIT {

    private final Logger logger = LoggerFactory.getLogger(MyBMWProxyBackendIT.class);

    public MyBMWProxy initializeProxy() {
        String connectedUser = System.getenv("CONNECTED_USER");
        String connectedPassword = System.getenv("CONNECTED_PASSWORD");
        String hCaptchaString = System.getenv("HCAPTCHA_TOKEN");
        assertNotNull(connectedUser);
        assertNotNull(connectedPassword);
        assertNotNull(hCaptchaString);

        MyBMWBridgeConfiguration configuration = new MyBMWBridgeConfiguration();
        configuration.setLanguage("de-DE");
        configuration.setRegion(BimmerConstants.REGION_ROW);
        configuration.setUserName(connectedUser);
        configuration.setPassword(connectedPassword);
        configuration.setHcaptchatoken(hCaptchaString);

        return new MyBMWHttpProxy(new MyHttpClientFactory(), configuration);
    }

    @BeforeEach
    public void setupLogger() {
        Logger root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        ((ch.qos.logback.classic.Logger) root).setLevel(Level.TRACE);

        logger.trace("tracing enabled");
        logger.debug("debugging enabled");
        logger.info("info enabled");
    }

    @Test
    public void testSequence() {
        MyBMWProxy myBMWProxy = initializeProxy();

        // get list of vehicles
        List<VehicleBase> vehicles = null;
        try {
            vehicles = myBMWProxy.requestVehiclesBase();
        } catch (NetworkException e) {
            fail(e.getReason(), e);
        }

        assertNotNull(vehicles);
        assertEquals(2, vehicles.size());

        for (VehicleBase vehicleBase : vehicles) {
            assertNotNull(vehicleBase.getVin());
            assertNotNull(vehicleBase.getAttributes().getBrand());

            // get image
            try {
                byte[] bmwImage = myBMWProxy.requestImage(vehicleBase.getVin(), vehicleBase.getAttributes().getBrand(),
                        new ImageProperties());

                assertNotNull(bmwImage);
            } catch (NetworkException e) {
                fail(e.getReason(), e);
            }

            // get state
            VehicleStateContainer vehicleState = null;
            try {
                vehicleState = myBMWProxy.requestVehicleState(vehicleBase.getVin(),
                        vehicleBase.getAttributes().getBrand());
            } catch (NetworkException e) {
                fail(e.getReason(), e);
            }
            assertNotNull(vehicleState);

            // get charge statistics -> only successful for electric vehicles
            ChargingStatisticsContainer chargeStatisticsContainer = null;
            try {
                chargeStatisticsContainer = myBMWProxy.requestChargeStatistics(vehicleBase.getVin(),
                        vehicleBase.getAttributes().getBrand());
                assertNotNull(chargeStatisticsContainer);
            } catch (NetworkException e) {
                logger.trace("error: {}", e.toString());
            }

            ChargingSessionsContainer chargeSessionsContainer = null;
            try {
                chargeSessionsContainer = myBMWProxy.requestChargeSessions(vehicleBase.getVin(),
                        vehicleBase.getAttributes().getBrand());
                assertNotNull(chargeSessionsContainer);
            } catch (NetworkException e) {
                logger.trace("error: {}", e.toString());
            }

            ExecutionStatusContainer remoteExecutionResponse = null;
            try {
                remoteExecutionResponse = myBMWProxy.executeRemoteServiceCall(vehicleBase.getVin(),
                        vehicleBase.getAttributes().getBrand(), RemoteService.LIGHT_FLASH);
            } catch (NetworkException e) {
                fail(e.getReason(), e);
            }

            assertNotNull(remoteExecutionResponse);
            logger.warn("{}", remoteExecutionResponse.toString());

            ExecutionStatusContainer remoteExecutionStatusResponse = null;
            try {
                remoteExecutionStatusResponse = myBMWProxy.executeRemoteServiceStatusCall(
                        vehicleBase.getAttributes().getBrand(), remoteExecutionResponse.getEventId());

                assertNotNull(remoteExecutionStatusResponse);
                logger.warn("{}", remoteExecutionStatusResponse.toString());

                int counter = 0;
                while (!ExecutionState.EXECUTED.toString().equals(remoteExecutionStatusResponse.getEventStatus())
                        && counter++ < 10) {
                    remoteExecutionStatusResponse = myBMWProxy.executeRemoteServiceStatusCall(
                            vehicleBase.getAttributes().getBrand(), remoteExecutionResponse.getEventId());
                    logger.warn("{}", remoteExecutionStatusResponse.toString());

                    Thread.sleep(5000);
                }
            } catch (NetworkException e) {
                fail(e.getReason(), e);
            } catch (InterruptedException e) {
                fail(e.getMessage(), e);
            }
        }
    }

    @Test
    public void testGetImages() {
        MyBMWProxy myBMWProxy = initializeProxy();

        ImageProperties imageProperties = new ImageProperties();

        try {
            imageProperties.viewport = "VehicleStatus";
            byte[] bmwImage = myBMWProxy.requestImage("please_set_here_your_vin", "bmw", imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "SideViewLeft";
            byte[] bmwImage = myBMWProxy.requestImage("please_set_here_your_vin", "bmw", imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "AngleSideViewForty";
            byte[] bmwImage = myBMWProxy.requestImage("please_set_here_your_vin", "bmw", imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "FrontView";
            byte[] bmwImage = myBMWProxy.requestImage("please_set_here_your_vin", "bmw", imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "FrontLeft";
            byte[] bmwImage = myBMWProxy.requestImage("please_set_here_your_vin", "bmw", imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "FrontRight";
            byte[] bmwImage = myBMWProxy.requestImage("please_set_here_your_vin", "bmw", imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "RearView";
            byte[] bmwImage = myBMWProxy.requestImage("please_set_here_your_vin", "bmw", imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }
    }

    @Test
    @Disabled
    public void testGetVehicles() {
        MyBMWProxy myBMWProxy = initializeProxy();

        try {
            List<Vehicle> vehicles = myBMWProxy.requestVehicles();

            logger.warn(ResponseContentAnonymizer.anonymizeResponseContent(new Gson().toJson(vehicles)));
            assertNotNull(vehicles);
            assertEquals(2, vehicles.size());
        } catch (NetworkException e) {
            fail(e.getReason(), e);
        }
    }
}

/**
 * @author Martin Grassl - initial contribution
 */
@NonNullByDefault
class MyHttpClientFactory implements HttpClientFactory {

    private final Logger logger = LoggerFactory.getLogger(MyHttpClientFactory.class);

    @Override
    public HttpClient createHttpClient(String consumerName) {
        // Instantiate and configure the SslContextFactory
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();

        // Instantiate HttpClient with the SslContextFactory
        HttpClient httpClient = new HttpClient(sslContextFactory);

        // Configure HttpClient, for example:
        httpClient.setFollowRedirects(false);

        // Start HttpClient
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return httpClient;
    }

    @Override
    public HttpClient getCommonHttpClient() {
        return createHttpClient("test");
    }

    @Override
    public HTTP2Client createHttp2Client(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createHttp2Client'");
    }

    @Override
    public HTTP2Client createHttp2Client(String arg0, @Nullable SslContextFactory arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createHttp2Client'");
    }

    @Override
    public HttpClient createHttpClient(String arg0, @Nullable SslContextFactory arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createHttpClient'");
    }
}
