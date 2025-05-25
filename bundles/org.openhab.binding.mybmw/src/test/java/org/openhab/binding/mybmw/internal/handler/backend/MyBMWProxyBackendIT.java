/*
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

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleBase;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleStateContainer;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.enums.ExecutionState;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.oauth2client.internal.OAuthFactoryImpl;
import org.openhab.core.auth.oauth2client.internal.OAuthStoreHandler;
import org.openhab.core.auth.oauth2client.internal.OAuthStoreHandlerImpl;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.qos.logback.classic.Level;

/**
 * this integration test runs only if the connected account is set via environment variables
 * CONNECTED_USER
 * CONNECTED_PASSWORD
 * HCAPTCHA_TOKEN
 * VEHICLE_COUNT
 * VIN
 * BRAND
 *
 * if you want to execute the tests, please set the env variables or static fields with the same name.
 *
 * @author Martin Grassl - initial contribution
 * @author Mark Herwege - adapted to be able to always compile and run
 */
@NonNullByDefault
public class MyBMWProxyBackendIT {

    // The following block of static constants can be used instead of environment variables with the same name.
    private static final String CONNECTED_USER = "";
    private static final String CONNECTED_PASSWORD = "";
    private static final String HCAPTCHA_TOKEN = "";
    private static final int VEHICLE_COUNT = 0; // number of vehicles in account
    private static final String VIN = ""; // vin to use for testing images on account
    private static final String BRAND = ""; // brand to use for testing images on account (bmw or mini)

    private int vehicleCount;

    private final Logger logger = LoggerFactory.getLogger(MyBMWProxyBackendIT.class);

    private @Nullable MyBMWProxy myBMWProxy;

    public @Nullable MyBMWProxy initializeProxy() {
        String connectedUser = System.getenv("CONNECTED_USER");
        connectedUser = connectedUser != null ? connectedUser : CONNECTED_USER;
        String connectedPassword = System.getenv("CONNECTED_PASSWORD");
        connectedPassword = connectedPassword != null ? connectedPassword : CONNECTED_PASSWORD;
        String hCaptchaToken = System.getenv("HCAPTCHA_TOKEN");
        hCaptchaToken = hCaptchaToken != null ? hCaptchaToken : HCAPTCHA_TOKEN;
        String vehicleCount = System.getenv("VEHICLE_COUNT");
        this.vehicleCount = vehicleCount != null ? Integer.valueOf(vehicleCount) : VEHICLE_COUNT;

        if (connectedUser.isEmpty() || connectedPassword.isEmpty() || hCaptchaToken.isEmpty()) {
            return null;
        }

        MyBMWBridgeConfiguration configuration = new MyBMWBridgeConfiguration();
        configuration.setLanguage("de-DE");
        configuration.setRegion(BimmerConstants.REGION_ROW);
        configuration.setUserName(connectedUser);
        configuration.setPassword(connectedPassword);
        configuration.setHCaptchaToken(hCaptchaToken);

        HttpClientFactory clientFactory = new MyHttpClientFactory();
        StorageService storageService = new MyStorageService();
        OAuthStoreHandler oAuthStoreHandler = new OAuthStoreHandlerImpl(storageService);
        OAuthFactory oAuthFactory = new OAuthFactoryImpl(clientFactory, oAuthStoreHandler);
        MyBMWBridgeHandler myBMWBridgeHandlerMock = Mockito.mock(MyBMWBridgeHandler.class);
        Bridge myBMWBridgeMock = Mockito.mock(Bridge.class);
        Mockito.when(myBMWBridgeHandlerMock.getThing()).thenReturn(myBMWBridgeMock);
        Mockito.when(myBMWBridgeMock.getUID()).thenReturn(new ThingUID("mybmw", "bridge", "test"));

        return new MyBMWHttpProxy(myBMWBridgeHandlerMock, clientFactory.createHttpClient("test"), oAuthFactory,
                configuration);
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
    public void testAll() {
        myBMWProxy = initializeProxy();

        if (myBMWProxy == null) {
            logger.info("not running backend integration tests, no test credentials provided");
            return;
        }

        // Do it all in one wrapper test to reuse the same proxy. If done separately, the proxy will be created multiple
        // times with the same hCaptchaToken. This does not work.
        testGetVehicles();
        testSequence();
        testGetImages();
    }

    private void testSequence() {
        MyBMWProxy myBMWProxy = this.myBMWProxy;
        if (myBMWProxy == null) {
            return;
        }

        // get list of vehicles
        List<VehicleBase> vehicles = null;
        try {
            vehicles = myBMWProxy.requestVehiclesBase();
        } catch (NetworkException e) {
            fail(e.getReason(), e);
            return;
        }

        assertNotNull(vehicles);
        assertEquals(vehicleCount, vehicles.size());

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

    private void testGetImages() {
        MyBMWProxy myBMWProxy = this.myBMWProxy;
        if (myBMWProxy == null) {
            return;
        }

        String vin = System.getenv("VIN");
        vin = vin != null ? vin : VIN;
        String brand = System.getenv("BRAND");
        brand = brand != null ? brand : BRAND;

        if (vin.isEmpty() || brand.isEmpty()) {
            return;
        }

        ImageProperties imageProperties = new ImageProperties();

        try {
            imageProperties.viewport = "VehicleStatus";
            byte[] bmwImage = myBMWProxy.requestImage(vin, brand, imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
            return;
        }

        try {
            imageProperties.viewport = "SideViewLeft";
            byte[] bmwImage = myBMWProxy.requestImage(vin, brand, imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "AngleSideViewForty";
            byte[] bmwImage = myBMWProxy.requestImage(vin, brand, imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "FrontView";
            byte[] bmwImage = myBMWProxy.requestImage(vin, brand, imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "FrontLeft";
            byte[] bmwImage = myBMWProxy.requestImage(vin, brand, imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "FrontRight";
            byte[] bmwImage = myBMWProxy.requestImage(vin, brand, imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }

        try {
            imageProperties.viewport = "RearView";
            byte[] bmwImage = myBMWProxy.requestImage(vin, brand, imageProperties);
            Files.write(new File("./" + imageProperties.viewport + ".jpg").toPath(), bmwImage);
            assertNotNull(bmwImage);
        } catch (NetworkException | IOException e) {
            logger.error("error retrieving image", e);
        }
    }

    private void testGetVehicles() {
        MyBMWProxy myBMWProxy = this.myBMWProxy;
        if (myBMWProxy == null) {
            return;
        }

        try {
            List<Vehicle> vehicles = myBMWProxy.requestVehicles();

            logger.warn(ResponseContentAnonymizer.anonymizeResponseContent(new Gson().toJson(vehicles)));
            assertNotNull(vehicles);
            assertEquals(vehicleCount, vehicles.size());
        } catch (NetworkException e) {
            fail(e.getReason(), e);
        }
    }

    /**
     * @author Martin Grassl - initial contribution
     */
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
            throw new UnsupportedOperationException("Unimplemented method 'createHttp2Client'");
        }

        @Override
        public HTTP2Client createHttp2Client(String arg0, @Nullable SslContextFactory arg1) {
            throw new UnsupportedOperationException("Unimplemented method 'createHttp2Client'");
        }

        @Override
        public HttpClient createHttpClient(String arg0, @Nullable SslContextFactory arg1) {
            throw new UnsupportedOperationException("Unimplemented method 'createHttpClient'");
        }
    }

    class MyStorageService implements StorageService {

        @Override
        public <T> Storage<T> getStorage(String name) {
            return new VolatileStorage<T>();
        }

        @Override
        public <T> Storage<T> getStorage(String name, @Nullable ClassLoader classLoader) {
            return getStorage("test");
        }
    }
}
