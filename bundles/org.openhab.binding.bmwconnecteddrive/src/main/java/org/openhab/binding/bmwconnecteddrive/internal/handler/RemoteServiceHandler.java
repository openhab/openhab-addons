/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
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

    private static final String SERVICE_TYPE = "serviceType";
    private static final String DATA = "data";
    // after 6 retries the state update will give up
    private static final int GIVEUP_COUNTER = 6;
    private static final int STATE_UPDATE_SEC = HTTPConstants.HTTP_TIMEOUT_SEC + 1; // regular timeout + 1sec

    private final ConnectedDriveProxy proxy;
    private final VehicleHandler handler;
    private final String serviceExecutionAPI;
    private final String serviceExecutionStateAPI;

    private int counter = 0;
    private Optional<ScheduledFuture<?>> stateJob = Optional.empty();
    private Optional<String> serviceExecuting = Optional.empty();

    public enum ExecutionState {
        READY,
        INITIATED,
        PENDING,
        DELIVERED,
        EXECUTED,
        ERROR,
    }

    public enum RemoteService {
        LIGHT_FLASH(REMOTE_SERVICE_LIGHT_FLASH, "Flash Lights"),
        VEHICLE_FINDER(REMOTE_SERVICE_VEHICLE_FINDER, "Vehicle Finder"),
        DOOR_LOCK(REMOTE_SERVICE_DOOR_LOCK, "Door Lock"),
        DOOR_UNLOCK(REMOTE_SERVICE_DOOR_UNLOCK, "Door Unlock"),
        HORN_BLOW(REMOTE_SERVICE_HORN, "Horn Blow"),
        CLIMATE_NOW(REMOTE_SERVICE_AIR_CONDITIONING, "Climate Control"),
        CHARGE_NOW(REMOTE_SERVICE_CHARGE_NOW, "Start Charging"),
        CHARGING_CONTROL(REMOTE_SERVICE_CHARGING_CONTROL, "Send Charging Profile");

        private final String command;
        private final String label;

        RemoteService(final String command, final String label) {
            this.command = command;
            this.label = label;
        }

        public String getCommand() {
            return command;
        }

        public String getLabel() {
            return label;
        }
    }

    public RemoteServiceHandler(VehicleHandler vehicleHandler, ConnectedDriveProxy connectedDriveProxy) {
        handler = vehicleHandler;
        proxy = connectedDriveProxy;
        final VehicleConfiguration config = handler.getConfiguration().get();
        serviceExecutionAPI = proxy.baseUrl + config.vin + proxy.serviceExecutionAPI;
        serviceExecutionStateAPI = proxy.baseUrl + config.vin + proxy.serviceExecutionStateAPI;
    }

    boolean execute(RemoteService service, String... data) {
        synchronized (this) {
            if (serviceExecuting.isPresent()) {
                // only one service executing
                return false;
            }
            serviceExecuting = Optional.of(service.name());
        }
        final MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add(SERVICE_TYPE, service.name());
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
            stateJob = Optional.empty();
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
                            // update doesn't show better results!
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
        synchronized (this) {
            stateJob.ifPresent(job -> {
                if (!job.isDone()) {
                    job.cancel(true);
                }
            });
            stateJob = Optional.of(handler.getScheduler().schedule(this::getState, STATE_UPDATE_SEC, TimeUnit.SECONDS));
        }
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

    public void cancel() {
        synchronized (this) {
            stateJob.ifPresent(action -> {
                if (!action.isDone()) {
                    action.cancel(true);
                }
                stateJob = Optional.empty();
            });
        }
    }
}
