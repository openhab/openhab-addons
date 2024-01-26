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
package org.openhab.binding.network.internal.utils;

import static org.openhab.binding.network.internal.utils.NetworkUtils.durationToMillis;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Information about the ping result.
 *
 * @author Andreas Hirsch - Initial contribution
 */
@NonNullByDefault
public class PingResult {

    private boolean success;
    private @Nullable Duration responseTime;
    private Duration executionTime;

    /**
     * @param success <code>true</code> if the device was reachable, <code>false</code> if not.
     * @param executionTime execution time of the ping command.
     */
    public PingResult(boolean success, Duration executionTime) {
        this.success = success;
        this.executionTime = executionTime;
    }

    /**
     * @return <code>true</code> if the device was reachable, <code>false</code> if not.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return response time which was returned by the ping command. <code>null</code> if response time provided
     *         by ping command is not available.
     */
    public @Nullable Duration getResponseTime() {
        return responseTime;
    }

    /**
     * @param responseTime the response time which was returned by the ping command.
     */
    public void setResponseTime(@Nullable Duration responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public String toString() {
        Duration responseTime = this.responseTime;
        String rt = responseTime == null ? "null" : durationToMillis(responseTime) + "ms";
        String et = durationToMillis(executionTime) + "ms";
        return "PingResult{success=" + success + ", responseTime=" + rt + ", executionTime=" + et + "}";
    }

    /**
     * @return Execution time of the ping command.
     */
    public Duration getExecutionTime() {
        return executionTime;
    }
}
