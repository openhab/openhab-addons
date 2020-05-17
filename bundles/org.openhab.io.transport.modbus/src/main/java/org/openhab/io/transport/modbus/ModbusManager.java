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

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * ModbusManager is the main interface for interacting with Modbus slaves
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusManager {

    /**
     * Submit one-time poll task. The method returns immediately, and the execution of the poll task will happen in
     * background.
     *
     * @param endpoint modbus endpoint to poll
     * @param request request to send
     * @param callback callback to call with errors and data
     * @return future representing the polled task
     */
    public ScheduledFuture<?> submitOneTimePoll(ModbusSlaveEndpoint endpoint, ModbusReadRequestBlueprint request,
            @Nullable ModbusReadCallback callback);

    /**
     * Register regularly polled task. The method returns immediately, and the execution of the poll task will happen in
     * the background.
     *
     * One can register only one regular poll task for triplet of (endpoint, request, callback).
     *
     * @param endpoint modbus endpoint to poll
     * @param request request to send
     * @param pollPeriodMillis poll interval, in milliseconds
     * @param initialDelayMillis initial delay before starting polling, in milliseconds
     * @param callback callback to call with errors and data
     * @return poll task representing the regular poll
     */
    public PollTask registerRegularPoll(ModbusSlaveEndpoint endpoint, ModbusReadRequestBlueprint request,
            long pollPeriodMillis, long initialDelayMillis, @Nullable ModbusReadCallback callback);

    /**
     * Unregister regularly polled task
     *
     * @param task poll task to unregister
     * @return whether poll task was unregistered. Poll task is not unregistered in case of unexpected errors or
     *         in the case where the poll task is not registered in the first place
     */
    public boolean unregisterRegularPoll(PollTask task);

    /**
     * Submit one-time write task. The method returns immediately, and the execution of the task will happen in
     * background.
     *
     * @param endpoint modbus endpoint to poll
     * @param request request to write
     * @param callback callback to call with errors and response
     * @return future representing the task
     */
    public ScheduledFuture<?> submitOneTimeWrite(ModbusSlaveEndpoint endpoint, ModbusWriteRequestBlueprint request,
            ModbusWriteCallback callback);

    /**
     * Configure general connection settings with a given endpoint
     *
     * @param endpoint endpoint to configure
     * @param configuration configuration for the endpoint. Use null to reset the configuration to default settings.
     */
    public void setEndpointPoolConfiguration(ModbusSlaveEndpoint endpoint,
            @Nullable EndpointPoolConfiguration configuration);

    /**
     * Get general configuration settings applied to a given endpoint
     *
     * Note that default configuration settings are returned in case the endpoint has not been configured.
     *
     * @param endpoint endpoint to query
     * @return general connection settings of the given endpoint
     */
    public @Nullable EndpointPoolConfiguration getEndpointPoolConfiguration(ModbusSlaveEndpoint endpoint);

    /**
     * Register listener for changes
     *
     * @param listener
     */
    public void addListener(ModbusManagerListener listener);

    /**
     * Remove listener for changes
     *
     * @param listener
     */
    public void removeListener(ModbusManagerListener listener);

}
