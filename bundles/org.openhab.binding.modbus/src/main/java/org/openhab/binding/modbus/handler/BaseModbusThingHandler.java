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
package org.openhab.binding.modbus.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.io.transport.modbus.ModbusFailureCallback;
import org.openhab.io.transport.modbus.ModbusReadCallback;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusWriteCallback;
import org.openhab.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.io.transport.modbus.PollTask;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * This is a convenience class to interact with the Thing's {@link ModbusCommunicationInterface}.
 *
 * @author Fabian Wolter - Initial contribution
 *
 */
@NonNullByDefault
public abstract class BaseModbusThingHandler extends BaseThingHandler {
    private List<PollTask> periodicPollers = Collections.synchronizedList(new ArrayList<>());
    private List<Future<?>> oneTimePollers = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean initialized;

    public BaseModbusThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * This method must be invoked in the base class' initialize() method before any other initialization is done.
     * It will throw an unchecked exception if the {@link ModbusCommunicationInterface} is not accessible (fail-fast).
     * This prevents any further initialization of the Thing. The framework will set the ThingStatus to
     * HANDLER_INITIALIZING_ERROR and display the exception's message.
     */
    @Override
    public void initialize() {
        getModbus();

        initialized = true;
    }

    /**
     * Register regularly polled task. The method returns immediately, and the execution of the poll task will happen in
     * the background.
     *
     * One can register only one regular poll task for triplet of (endpoint, request, callback).
     *
     * @param request request to send
     * @param pollPeriodMillis poll interval, in milliseconds
     * @param initialDelayMillis initial delay before starting polling, in milliseconds
     * @param callback callback to call with data
     * @param callback callback to call in case of failure
     * @return poll task representing the regular poll
     * @throws IllegalStateException when this communication has been closed already
     */
    public PollTask registerRegularPoll(ModbusReadRequestBlueprint request, long pollPeriodMillis,
            long initialDelayMillis, ModbusReadCallback resultCallback,
            ModbusFailureCallback<ModbusReadRequestBlueprint> failureCallback) {
        checkInitialized();

        PollTask task = getModbus().registerRegularPoll(request, pollPeriodMillis, initialDelayMillis, resultCallback,
                failureCallback);
        periodicPollers.add(task);

        return task;
    }

    /**
     * Unregister regularly polled task
     *
     * If this communication interface is closed already, the method returns immediately with false return value
     *
     * @param task poll task to unregister
     * @return whether poll task was unregistered. Poll task is not unregistered in case of unexpected errors or
     *         in the case where the poll task is not registered in the first place
     */
    public boolean unregisterRegularPoll(PollTask task) {
        periodicPollers.remove(task);
        return getModbus().unregisterRegularPoll(task);
    }

    /**
     * Submit one-time poll task. The method returns immediately, and the execution of the poll task will happen in
     * background.
     *
     * @param request request to send
     * @param callback callback to call with data
     * @param callback callback to call in case of failure
     * @return future representing the polled task
     * @throws IllegalStateException when this communication has been closed already
     */
    public Future<?> submitOneTimePoll(ModbusReadRequestBlueprint request, ModbusReadCallback resultCallback,
            ModbusFailureCallback<ModbusReadRequestBlueprint> failureCallback) {
        checkInitialized();

        Future<?> future = getModbus().submitOneTimePoll(request, resultCallback, failureCallback);
        oneTimePollers.add(future);
        oneTimePollers.removeIf(Future::isDone);

        return future;
    }

    /**
     * Submit one-time write task. The method returns immediately, and the execution of the task will happen in
     * background.
     *
     * @param request request to send
     * @param callback callback to call with response
     * @param callback callback to call in case of failure
     * @return future representing the task
     * @throws IllegalStateException when this communication has been closed already
     */
    public Future<?> submitOneTimeWrite(ModbusWriteRequestBlueprint request, ModbusWriteCallback resultCallback,
            ModbusFailureCallback<ModbusWriteRequestBlueprint> failureCallback) {
        checkInitialized();

        Future<?> future = getModbus().submitOneTimeWrite(request, resultCallback, failureCallback);
        oneTimePollers.add(future);
        oneTimePollers.removeIf(Future::isDone);

        return future;
    }

    /**
     * Get endpoint associated with this communication interface
     *
     * @return modbus slave endpoint
     */
    public ModbusSlaveEndpoint getEndpoint() {
        return getModbus().getEndpoint();
    }

    /**
     * Retrieves the {@link ModbusCommunicationInterface} and does some validity checking.
     * Sets the ThingStatus to offline if it couldn't be retrieved and throws an unchecked exception.
     *
     * The unchecked exception should not be caught by the implementing class, as the initialization of the Thing
     * already fails if the {@link ModbusCommunicationInterface} cannot be retrieved.
     *
     * @throws IllegalStateException if the {@link ModbusCommunicationInterface} couldn't be retrieved.
     * @return the {@link ModbusCommunicationInterface}
     */
    private ModbusCommunicationInterface getModbus() {
        try {
            Bridge bridge = getBridge();
            if (bridge == null) {
                throw new IllegalStateException("Thing has no Bridge set");
            }

            BridgeHandler handler = bridge.getHandler();

            if (handler instanceof ModbusEndpointThingHandler) {
                ModbusCommunicationInterface communicationInterface = ((ModbusEndpointThingHandler) handler)
                        .getCommunicationInterface();

                if (communicationInterface == null) {
                    throw new IllegalStateException("Failed to retrieve Modbus communication interface");
                } else {
                    return communicationInterface;
                }
            } else {
                throw new IllegalStateException("Bridge is not a Modbus bridge: " + handler);
            }
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Modbus initialization failed: " + e.getMessage());
            throw e;
        }
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " not initialized. Please call super.initialize().");
        }
    }

    @Override
    public void dispose() {
        oneTimePollers.forEach(p -> p.cancel(true));
        oneTimePollers.clear();

        ModbusCommunicationInterface modbus = getModbus();
        periodicPollers.forEach(p -> modbus.unregisterRegularPoll(p));
        periodicPollers.clear();

        super.dispose();
    }
}
