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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.openhab.binding.bmwconnecteddrive.internal.VehicleConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.binding.bmwconnecteddrive.internal.utils.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link RemoteServiceHandler} handles executions of remote services towards your Vehicle
 *
 * @see https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/remote_services.py
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 */
@NonNullByDefault
public class RemoteServiceHandler implements StringResponseCallback {
    private final Logger logger = LoggerFactory.getLogger(RemoteServiceHandler.class);

    // after 60 retries the state update will give up
    private static final String SERVICE_TYPE = "serviceType";
    private static final String DATA = "data";
    private static final int GIVEUP_COUNTER = 6;
    private static final int STATE_UPDATE_SEC = HTTPConstants.HTTP_TIMEOUT_SEC + 1; // regular timeout + 1sec
    private int counter = 0;

    public enum ExecutionState {
        READY,
        INITIATED,
        PENDING,
        DELIVERED,
        EXECUTED,
        ERROR,
    }

    public enum RemoteService {
        LIGHT_FLASH("LIGHT_FLASH"),
        VEHICLE_FINDER("VEHICLE_FINDER"),
        DOOR_LOCK("DOOR_LOCK"),
        DOOR_UNLOCK("DOOR_UNLOCK"),
        HORN("HORN_BLOW"),
        AIR_CONDITIONING("CLIMATE_NOW"),
        CHARGE_NOW("CHARGE_NOW"),
        CHARGING_CONTROL("CHARGING_CONTROL");

        private final String service;

        RemoteService(String s) {
            service = s;
        }

        @Override
        public String toString() {
            return service;
        }
    }

    private final ConnectedDriveProxy proxy;
    private final VehicleHandler handler;
    private Optional<String> serviceExecuting = Optional.empty();

    private final String serviceExecutionAPI;
    private final String serviceExecutionStateAPI;

    public RemoteServiceHandler(VehicleHandler vehicleHandler, ConnectedDriveProxy connectedDriveProxy) {
        handler = vehicleHandler;
        proxy = connectedDriveProxy;
        if (handler.getConfiguration().isPresent()) {
            final VehicleConfiguration config = handler.getConfiguration().get();
            serviceExecutionAPI = proxy.baseUrl + config.vin + proxy.serviceExecutionAPI;
            serviceExecutionStateAPI = proxy.baseUrl + config.vin + proxy.serviceExecutionStateAPI;
        } else {
            serviceExecutionAPI = Constants.INVALID;
            serviceExecutionStateAPI = Constants.INVALID;
            logger.warn("No configuration for VehicleHandler available");
        }
    }

    boolean execute(RemoteService service, String... data) {
        synchronized (this) {
            if (serviceExecuting.isPresent()) {
                // only one service executing
                return false;
            }
            serviceExecuting = Optional.of(service.toString());
        }
        final MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add(SERVICE_TYPE, service.toString());
        if (data.length > 0) {
            dataMap.add(DATA, data[0]);
        }
        proxy.post(serviceExecutionAPI, dataMap, this);
        return true;
    }

    public void getState() {
        synchronized (this) {
            serviceExecuting.ifPresentOrElse(service -> {
                if (counter >= GIVEUP_COUNTER) {
                    logger.warn("Giving up updating state for {} after {} times", service, GIVEUP_COUNTER);
                    reset();
                    // immediately refresh data
                    handler.getData();
                }
                counter++;
                final MultiMap<String> dataMap = new MultiMap<String>();
                dataMap.add(SERVICE_TYPE, service);
                proxy.get(serviceExecutionStateAPI, dataMap, this);
            }, () -> {
                logger.warn("No Service executed to get state");
            });
        }
    }

    @Override
    public void onResponse(@Nullable String result) {
        if (result != null) {
            try {
                ExecutionStatusContainer esc = Converter.getGson().fromJson(result, ExecutionStatusContainer.class);
                if (esc != null && esc.executionStatus != null) {
                    String status = esc.executionStatus.status;
                    synchronized (this) {
                        handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null), status);
                        if (ExecutionState.EXECUTED.name().equals(status)) {
                            // refresh loop ends - update of status handled in the normal refreshInterval. Earlier
                            // update
                            // doesn't
                            // show better results!
                            reset();
                            return;
                        }
                    }
                }
            } catch (JsonSyntaxException jse) {
                logger.debug("RemoteService response is unparseable: {} {}", result, jse.getMessage());
            }
        }
        // schedule even if no result is present until retries exceeded
        handler.getScheduler().schedule(this::getState, STATE_UPDATE_SEC, TimeUnit.SECONDS);
    }

    @Override
    public void onError(NetworkError error) {
        synchronized (this) {
            handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null),
                    ExecutionState.ERROR.name() + Constants.SPACE + Integer.toString(error.status));
            reset();
        }
    }

    private void reset() {
        serviceExecuting = Optional.empty();
        counter = 0;
    }
}
