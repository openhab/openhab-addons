/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.buienradar.internal;

/**
 * The {@link BuienradarConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Edwin de Jong - Initial contribution
 */
public class BuienradarConfiguration {

    /**
     * Location of the forecast from buienradar
     */
    public String location;

    /**
     * Refresh interval for retrieving results from buienradar.
     */
    public Integer refreshIntervalMinutes;

    /**
     * Number of retries to try to retrieve buienradar results.
     */
    public Integer retries;

    /**
     * Exponential backoff base value for retries in seconds. For example, when this is 2 seconds, will retry at 2, 4,
     * 8, 16, 32, 64 seconds.
     */
    public Integer exponentialBackoffRetryBaseInSeconds;
}
