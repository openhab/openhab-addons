/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.HTTPConstants.*;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.bmwconnecteddrive.internal.dto.remote.ExecutionStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteServiceHandler} handles executions of remote services towards your Vehicle
 *
 * @see https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/remote_services.py
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RemoteServiceHandler {
    private final Logger logger = LoggerFactory.getLogger(RemoteServiceHandler.class);

    // after 60 retries the state update will give up
    private static final String SERVICE_TYPE = "serviceType";
    private static final int GIVEUP_COUNTER = 12;
    private static final int STATE_UPDATE_SEC = 5;
    private int counter = 0;

    public enum ExecutionState {
        READY("READY"),
        INITIATED("INITIATED"),
        PENDING("PENDING"),
        DELIVERED("DELIVERED"),
        EXECUTED("EXECUTED");

        private final String state;

        ExecutionState(String s) {
            state = s;
        }

        @Override
        public String toString() {
            return state;
        }
    }

    public enum RemoteService {
        LIGHT_FLASH("LIGHT_FLASH"),
        VEHICLE_FINDER("VEHICLE_FINDER"),
        DOOR_LOCK("DOOR_LOCK"),
        DOOR_UNLOCK("DOOR_UNLOCK"),
        HORN("HORN_BLOW"),
        AIR_CONDITIONING("CLIMATE_NOW");

        private final String service;

        RemoteService(String s) {
            service = s;
        }

        @Override
        public String toString() {
            return service;
        }
    }

    private ConnectedDriveProxy proxy;
    private VehicleHandler handler;
    private HttpClient httpClient;
    private Optional<String> serviceExecuting = Optional.empty();

    private String serviceExecutionAPI;
    private String serviceExecutionStateAPI;

    public RemoteServiceHandler(VehicleHandler vehicleHandler, ConnectedDriveProxy connectedDriveProxy, HttpClient hc) {
        handler = vehicleHandler;
        proxy = connectedDriveProxy;
        httpClient = hc;
        if (handler.getConfiguration().isPresent()) {
            serviceExecutionAPI = proxy.baseUrl + handler.getConfiguration().get().vin + proxy.serviceExecutionAPI;
            serviceExecutionStateAPI = proxy.baseUrl + handler.getConfiguration().get().vin
                    + proxy.serviceExecutionStateAPI;
        } else {
            serviceExecutionAPI = Constants.INVALID;
            serviceExecutionStateAPI = Constants.INVALID;
            logger.warn("No configuration for VehicleHandler available");
        }
    }

    boolean execute(RemoteService service) {
        synchronized (this) {
            if (serviceExecuting.isPresent()) {
                // only one service executing
                return false;
            }
            serviceExecuting = Optional.of(service.toString());
        }
        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add(SERVICE_TYPE, service.toString());
        String urlEncodedData = UrlEncoded.encode(dataMap, Charset.defaultCharset(), false);

        Request req = httpClient.POST(serviceExecutionAPI);
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
        req.header(CONTENT_LENGTH, Integer.toString(urlEncodedData.length()));
        req.header(HttpHeader.AUTHORIZATION, proxy.getToken().getBearerToken());
        req.content(new StringContentProvider(urlEncodedData));

        try {
            ContentResponse contentResponse = req.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
            if (contentResponse.getStatus() != 200) {
                reset();
                return false;
            } else {
                handler.getScheduler().schedule(this::getState, 1, TimeUnit.SECONDS);
                return true;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Execute Service Exception {}", e.getMessage());
            reset();
            return false;
        }
    }

    public void getState() {
        if (!serviceExecuting.isPresent()) {
            logger.warn("No Service executed to get state");
            return;
        }
        if (counter >= GIVEUP_COUNTER) {
            logger.warn("Giving up updating state for {} after {} times", serviceExecuting, GIVEUP_COUNTER);
            reset();
            // immediately refresh data
            handler.getData();
        }
        counter++;
        Request req = httpClient.newRequest(serviceExecutionStateAPI + serviceExecuting.get());
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.TRANSFER_ENCODING, CHUNKED);
        req.header(HttpHeader.AUTHORIZATION, proxy.getToken().getBearerToken());

        try {
            ContentResponse contentResponse = req.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
            if (contentResponse.getStatus() == 200) {
                String state = contentResponse.getContentAsString();
                ExecutionStatusContainer esc = Converter.getGson().fromJson(state, ExecutionStatusContainer.class);
                ExecutionStatus execStatus = esc.executionStatus;

                handler.updateRemoteExecutionStatus(serviceExecuting.get(), execStatus.status);
                if (!ExecutionState.EXECUTED.toString().equals(execStatus.status)) {
                    handler.getScheduler().schedule(this::getState, STATE_UPDATE_SEC, TimeUnit.SECONDS);
                } else {
                    reset();
                    // immediately refresh data
                    handler.getData();
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Execute Service Exception {}", e.getMessage());
            handler.updateRemoteExecutionStatus(serviceExecuting.get(), e.getMessage());
            reset();
        }
    }

    private void reset() {
        synchronized (this) {
            serviceExecuting = Optional.empty();
            counter = 0;
        }
        handler.switchRemoteServicesOff();
    }
}
