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
package org.openhab.io.transport.modbus;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Interface for interacting with a particular modbus slave.
 *
 * When no further communication is expected with the slave, close the interface so that any underlying resources can be
 * freed.
 *
 * Close unregisters all the regular polls registered with registerRegularPoll. When endpoint's last
 * communication interface is closed, the connection is closed as well, no matter the what EndpointPoolConfiguration
 * says.
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ModbusCommunicationInterface extends AutoCloseable {

    /**
     * Get endpoint associated with this communication interface
     *
     * @return modbus slave endpoint
     */
    public ModbusSlaveEndpoint getEndpoint();

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
            ModbusFailureCallback<ModbusReadRequestBlueprint> failureCallback);

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
            ModbusFailureCallback<ModbusReadRequestBlueprint> failureCallback);

    /**
     * Unregister regularly polled task
     *
     * @param task poll task to unregister
     * @return whether poll task was unregistered. Poll task is not unregistered in case of unexpected errors or
     *         in the case where the poll task is not registered in the first place
     * @throws IllegalStateException when this communication has been closed already
     */
    public boolean unregisterRegularPoll(PollTask task);

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
            ModbusFailureCallback<ModbusWriteRequestBlueprint> failureCallback);
}
