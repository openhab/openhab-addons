/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
    private static final int GIVEUP_COUNTER = 12; // after 12 retries the state update will give up
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
        TIMEOUT
    }

    public enum RemoteService {
        LIGHT_FLASH("Flash Lights", REMOTE_SERVICE_LIGHT_FLASH, REMOTE_SERVICE_LIGHT_FLASH),
        VEHICLE_FINDER("Vehicle Finder", REMOTE_SERVICE_VEHICLE_FINDER, REMOTE_SERVICE_VEHICLE_FINDER),
        DOOR_LOCK("Door Lock", REMOTE_SERVICE_DOOR_LOCK, REMOTE_SERVICE_DOOR_LOCK),
        DOOR_UNLOCK("Door Unlock", REMOTE_SERVICE_DOOR_UNLOCK, REMOTE_SERVICE_DOOR_UNLOCK),
        HORN_BLOW("Horn Blow", REMOTE_SERVICE_HORN, REMOTE_SERVICE_HORN),
        CLIMATE_NOW_START("Start Climate", REMOTE_SERVICE_AIR_CONDITIONING_START, "climate-now?action=START"),
        CLIMATE_NOW_STOP("Stop Climate", REMOTE_SERVICE_AIR_CONDITIONING_STOP, "climate-now?action=STOP");

        private final String label;
        private final String id;
        private final String command;

        RemoteService(final String label, final String id, String command) {
            this.label = label;
            this.id = id;
            this.command = command;
        }

        public String getLabel() {
            return label;
        }

        public String getId() {
            return id;
        }

        public String getCommand() {
            return command;
        }
    }

    public RemoteServiceHandler(VehicleHandler vehicleHandler, MyBMWProxy myBmwProxy) {
        handler = vehicleHandler;
        proxy = myBmwProxy;
        final VehicleConfiguration config = handler.getConfiguration().get();
        serviceExecutionAPI = proxy.remoteCommandUrl + config.vin + "/";
        serviceExecutionStateAPI = proxy.remoteStatusUrl;
    }

    boolean execute(RemoteService service, String... data) {
        synchronized (this) {
            if (serviceExecuting.isPresent()) {
                logger.debug("Execution rejected - {} still pending", serviceExecuting.get());
                // only one service executing
                return false;
            }
            serviceExecuting = Optional.of(service.getId());
        }
        final MultiMap<String> dataMap = new MultiMap<String>();
        if (data.length > 0) {
            dataMap.add(DATA, data[0]);
            proxy.post(serviceExecutionAPI + service.getCommand(), CONTENT_TYPE_JSON_ENCODED, data[0],
                    handler.getConfiguration().get().vehicleBrand, this);
        } else {
            proxy.post(serviceExecutionAPI + service.getCommand(), null, null,
                    handler.getConfiguration().get().vehicleBrand, this);
        }
        return true;
    }

    public void getState() {
        synchronized (this) {
            serviceExecuting.ifPresentOrElse(service -> {
                if (counter >= GIVEUP_COUNTER) {
                    logger.warn("Giving up updating state for {} after {} times", service, GIVEUP_COUNTER);
                    handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null),
                            ExecutionState.TIMEOUT.name().toLowerCase());
                    reset();
                    // immediately refresh data
                    handler.getData();
                } else {
                    counter++;
                    final MultiMap<String> dataMap = new MultiMap<String>();
                    dataMap.add(EVENT_ID, executingEventId.get());
                    final String encoded = UrlEncoded.encode(dataMap, StandardCharsets.UTF_8, false);
                    proxy.post(serviceExecutionStateAPI + Constants.QUESTION + encoded, null, null,
                            handler.getConfiguration().get().vehicleBrand, this);
                }
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
                if (esc != null) {
                    if (esc.eventId != null) {
                        // service initiated - store event id for further MyBMW updates
                        executingEventId = Optional.of(esc.eventId);
                        handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null),
                                ExecutionState.INITIATED.name().toLowerCase());
                    } else if (esc.eventStatus != null) {
                        // service status updated
                        synchronized (this) {
                            handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null),
                                    esc.eventStatus.toLowerCase());
                            if (ExecutionState.EXECUTED.name().equalsIgnoreCase(esc.eventStatus)
                                    || ExecutionState.ERROR.name().equalsIgnoreCase(esc.eventStatus)) {
                                // refresh loop ends - update of status handled in the normal refreshInterval.
                                // Earlier update doesn't show better results!
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
                    ExecutionState.ERROR.name().toLowerCase() + Constants.SPACE + Integer.toString(error.status));
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
