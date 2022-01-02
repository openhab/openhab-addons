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
package org.openhab.binding.mybmw.internal.handler;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.*;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CONTENT_TYPE_JSON_ENCODED;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.mybmw.internal.VehicleConfiguration;
import org.openhab.binding.mybmw.internal.dto.network.NetworkError;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.HTTPConstants;
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

    private static final String EVENT_ID = "eventId";
    private static final String DATA = "data";
    private static final int GIVEUP_COUNTER = 6; // after 6 retries the state update will give up
    private static final int STATE_UPDATE_SEC = HTTPConstants.HTTP_TIMEOUT_SEC + 1; // regular timeout + 1sec

    private final MyBMWProxy proxy;
    private final VehicleHandler handler;
    private final String serviceExecutionAPI;
    private final String serviceExecutionStateAPI;

    private int counter = 0;
    private Optional<ScheduledFuture<?>> stateJob = Optional.empty();
    private Optional<String> serviceExecuting = Optional.empty();
    private Optional<String> executingEventId = Optional.empty();

    public enum ExecutionState {
        READY,
        INITIATED,
        PENDING,
        DELIVERED,
        EXECUTED,
        ERROR,
    }

    public enum RemoteService {
        LIGHT_FLASH("Flash Lights", REMOTE_SERVICE_LIGHT_FLASH),
        VEHICLE_FINDER("Vehicle Finder", REMOTE_SERVICE_VEHICLE_FINDER),
        DOOR_LOCK("Door Lock", REMOTE_SERVICE_DOOR_LOCK),
        DOOR_UNLOCK("Door Unlock", REMOTE_SERVICE_DOOR_UNLOCK),
        HORN_BLOW("Horn Blow", REMOTE_SERVICE_HORN),
        CLIMATE_NOW_START("Start Climate", REMOTE_SERVICE_AIR_CONDITIONING_START),
        CLIMATE_NOW_STOP("Stop Climate", REMOTE_SERVICE_AIR_CONDITIONING_STOP);

        private final String label;
        private final String remoteCommand;

        RemoteService(final String label, final String remoteCommand) {
            this.label = label;
            this.remoteCommand = remoteCommand;
        }

        public String getLabel() {
            return label;
        }

        public String getRemoteCommand() {
            return remoteCommand;
        }
    }

    public RemoteServiceHandler(VehicleHandler vehicleHandler, MyBMWProxy connectedDriveProxy) {
        handler = vehicleHandler;
        proxy = connectedDriveProxy;
        final VehicleConfiguration config = handler.getConfiguration().get();
        serviceExecutionAPI = proxy.remoteCommandUrl + config.vin + "/";
        serviceExecutionStateAPI = proxy.remoteStatusUrl;
    }

    boolean execute(RemoteService service, String... data) {
        logger.info("Execute {} with parameters {}", service.getRemoteCommand(), data);
        synchronized (this) {
            if (serviceExecuting.isPresent()) {
                logger.debug("Execution rejected - {} still pending", serviceExecuting.get());
                // only one service executing
                return false;
            }
            serviceExecuting = Optional.of(service.name());
        }
        final MultiMap<String> dataMap = new MultiMap<String>();
        if (data.length > 0) {
            dataMap.add(DATA, data[0]);
            proxy.post(serviceExecutionAPI + service.getRemoteCommand(), CONTENT_TYPE_JSON_ENCODED, data[0],
                    handler.getConfiguration().get().vehicleBrand, this);
        } else {
            proxy.post(serviceExecutionAPI + service.getRemoteCommand(), null, null,
                    handler.getConfiguration().get().vehicleBrand, this);
        }
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
                dataMap.add(EVENT_ID, executingEventId.get());
                final String encoded = UrlEncoded.encode(dataMap, StandardCharsets.UTF_8, false);
                proxy.post(serviceExecutionStateAPI + Constants.QUESTION + encoded, null, null,
                        handler.getConfiguration().get().vehicleBrand, this);
            }, () -> {
                logger.warn("No Service executed to get state");
            });
            stateJob = Optional.empty();
        }
    }

    @Override
    public void onResponse(@Nullable String result) {
        logger.info("Remote execution result {}", result);
        if (result != null) {
            try {
                ExecutionStatusContainer esc = Converter.getGson().fromJson(result, ExecutionStatusContainer.class);
                if (esc != null) {
                    if (esc.executionStatus != null) {
                        // handling of BMW ConnectedDrive updates
                        String status = esc.executionStatus.status;
                        if (status != null) {
                            synchronized (this) {
                                handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null), status);
                                if (ExecutionState.EXECUTED.name().equals(status)) {
                                    // refresh loop ends - update of status handled in the normal refreshInterval.
                                    // Earlier
                                    // update doesn't show better results!
                                    reset();
                                    return;
                                }
                            }
                        }
                    } else if (esc.eventId != null) {
                        // store event id for further MyBMW updates
                        executingEventId = Optional.of(esc.eventId);
                    } else if (esc.eventStatus != null) {
                        // update status for MyBMW API
                        synchronized (this) {
                            handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null), esc.eventStatus);
                            if (ExecutionState.EXECUTED.name().equals(esc.eventStatus)) {
                                // refresh loop ends - update of status handled in the normal refreshInterval.
                                // Earlier
                                // update doesn't show better results!
                                reset();
                                return;
                            }
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
        executingEventId = Optional.empty();
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
