/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import java.util.Set;
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
     * @param task
     * @return future representing the polled task
     */
    public ScheduledFuture<?> submitOneTimePoll(PollTask task);

    /**
     * Register regularly polled task. The method returns immediately, and the execution of the poll task will happen in
     * the background.
     *
     * @param task
     * @return
     */
    public void registerRegularPoll(PollTask task, long pollPeriodMillis, long initialDelayMillis);

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
     * @param task
     * @return future representing the task
     */
    public ScheduledFuture<?> submitOneTimeWrite(WriteTask task);

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

    /**
     * Get registered regular polls
     *
     * @return set of registered regular polls
     */
    public Set<PollTask> getRegisteredRegularPolls();

}
