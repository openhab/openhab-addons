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
package org.openhab.binding.mybmw.internal.handler;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWProxy;
import org.openhab.binding.mybmw.internal.handler.backend.NetworkException;
import org.openhab.binding.mybmw.internal.handler.enums.ExecutionState;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteServiceExecutor} handles executions of remote services
 * towards your Vehicle
 *
 * @see https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/remote_services.py
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit and send of charge profile
 * @author Martin Grassl - rename and refactor for v2
 */
@NonNullByDefault
public class RemoteServiceExecutor {
    private final Logger logger = LoggerFactory.getLogger(RemoteServiceExecutor.class);

    private static final int GIVEUP_COUNTER = 12; // after 12 retries the state update will give up
    private static final int STATE_UPDATE_SEC = HTTPConstants.HTTP_TIMEOUT_SEC + 1; // regular timeout + 1sec

    private final MyBMWProxy proxy;
    private final VehicleHandler handler;

    private int counter = 0;
    private Optional<ScheduledFuture<?>> stateJob = Optional.empty();
    private Optional<String> serviceExecuting = Optional.empty();
    private Optional<String> executingEventId = Optional.empty();

    public RemoteServiceExecutor(VehicleHandler vehicleHandler, MyBMWProxy myBmwProxy) {
        handler = vehicleHandler;
        proxy = myBmwProxy;
    }

    public boolean execute(RemoteService service) {
        synchronized (this) {
            if (serviceExecuting.isPresent()) {
                logger.debug("Execution rejected - {} still pending", serviceExecuting.get());
                // only one service executing
                return false;
            }
            serviceExecuting = Optional.of(service.getId());
        }
        try {
            ExecutionStatusContainer executionStatus = proxy.executeRemoteServiceCall(
                    handler.getVehicleConfiguration().get().getVin(),
                    handler.getVehicleConfiguration().get().getVehicleBrand(), service);
            handleRemoteExecution(executionStatus);
        } catch (NetworkException e) {
            handleRemoteServiceException(e);
        }

        return true;
    }

    private void getState() {
        synchronized (this) {
            serviceExecuting.ifPresentOrElse(service -> {
                if (counter >= GIVEUP_COUNTER) {
                    logger.warn("Giving up updating state for {} after {} times", service, GIVEUP_COUNTER);
                    handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null),
                            ExecutionState.TIMEOUT.name().toLowerCase());
                    reset();
                    // immediately refresh data
                    handler.updateData();
                } else {
                    counter++;
                    try {
                        ExecutionStatusContainer executionStatusContainer = proxy.executeRemoteServiceStatusCall(
                                handler.getVehicleConfiguration().get().getVehicleBrand(), executingEventId.get());
                        handleRemoteExecution(executionStatusContainer);
                    } catch (NetworkException e) {
                        handleRemoteServiceException(e);
                    }
                }
            }, () -> {
                logger.warn("No Service executed to get state");
            });
            stateJob = Optional.empty();
        }
    }

    private void handleRemoteServiceException(NetworkException e) {
        synchronized (this) {
            handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null),
                    ExecutionState.ERROR.name().toLowerCase() + Constants.SPACE + Integer.toString(e.getStatus()));
            reset();
        }
    }

    private void handleRemoteExecution(ExecutionStatusContainer executionStatusContainer) {
        if (!executionStatusContainer.getEventId().isEmpty()) {
            // service initiated - store event id for further MyBMW updates
            executingEventId = Optional.of(executionStatusContainer.getEventId());
            handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null),
                    ExecutionState.INITIATED.name().toLowerCase());
        } else if (!executionStatusContainer.getEventStatus().isEmpty()) {
            // service status updated
            synchronized (this) {
                handler.updateRemoteExecutionStatus(serviceExecuting.orElse(null),
                        executionStatusContainer.getEventStatus().toLowerCase());
                if (ExecutionState.EXECUTED.name().equalsIgnoreCase(executionStatusContainer.getEventStatus())
                        || ExecutionState.ERROR.name().equalsIgnoreCase(executionStatusContainer.getEventStatus())) {
                    // refresh loop ends - update of status handled in the normal refreshInterval.
                    // Earlier update doesn't show better results!
                    reset();
                    return;
                }
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
