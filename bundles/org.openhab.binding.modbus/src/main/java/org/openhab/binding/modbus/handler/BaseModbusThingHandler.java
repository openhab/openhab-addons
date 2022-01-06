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
package org.openhab.binding.modbus.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusFailureCallback;
import org.openhab.core.io.transport.modbus.ModbusReadCallback;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteCallback;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;

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

    public BaseModbusThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * This method is called when the Thing is being initialized, but only if the Modbus Bridge is configured correctly.
     * The code that normally goes into `BaseThingHandler.initialize()` like configuration reading and validation goes
     * here.
     */
    public abstract void modbusInitialize();

    @Override
    public final void initialize() {
        try {
            // check if the Bridge is configured correctly (fail-fast)
            getModbus();
            getSlaveId();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, e.getMessage());
        }

        modbusInitialize();
    }

    /**
     * Get Slave ID, also called as unit id, represented by the thing
     *
     * @return slave id represented by this thing handler
     */
    public int getSlaveId() {
        try {
            return getBridgeHandler().getSlaveId();
        } catch (EndpointNotInitializedException e) {
            throw new IllegalStateException("Bridge not initialized");
        }
    }

    /**
     * Return true if auto discovery is enabled for this endpoint
     *
     * @return boolean true if the discovery is enabled
     */
    public boolean isDiscoveryEnabled() {
        return getBridgeHandler().isDiscoveryEnabled();
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
     * @throws IllegalStateException when this communication has been closed already
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
        Future<?> future = getModbus().submitOneTimeWrite(request, resultCallback, failureCallback);
        oneTimePollers.add(future);
        oneTimePollers.removeIf(Future::isDone);

        return future;
    }

    private ModbusCommunicationInterface getModbus() {
        ModbusCommunicationInterface communicationInterface = getBridgeHandler().getCommunicationInterface();

        if (communicationInterface == null) {
            throw new IllegalStateException("Bridge not initialized");
        } else {
            return communicationInterface;
        }
    }

    private ModbusEndpointThingHandler getBridgeHandler() {
        try {
            Bridge bridge = getBridge();
            if (bridge == null) {
                throw new IllegalStateException("No Bridge configured");
            }

            BridgeHandler handler = bridge.getHandler();

            if (handler instanceof ModbusEndpointThingHandler) {
                return (ModbusEndpointThingHandler) handler;
            } else {
                throw new IllegalStateException("Not a Modbus Bridge: " + handler);
            }
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, e.getMessage());
            throw e;
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
