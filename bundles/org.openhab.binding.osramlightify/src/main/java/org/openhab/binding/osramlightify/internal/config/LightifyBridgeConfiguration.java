/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.internal.config;

/**
 * Configuration class for {@link LightifyBridgeHandler}.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyBridgeConfiguration {

    /**
     * Static IP address (optional).
     */
    public String ipAddress;

    /**
     * The interval between device discovery updates in seconds.
     */
    public double discoveryInterval;

    /**
     * How often to poll for device state changes.
     */
    public double minPollInterval;
    public double maxPollInterval;

    /**
     * How long transitions should take in seconds.
     */
    public Double transitionTime;

    /**
     * How long transitions should take in seconds when turning off.
     */
    public Double transitionToOffTime;

    /**
     * Minimum white temperature
     */
    public Integer whiteTemperatureMin;

    /**
     * Maximum white temperature
     */
    public Integer whiteTemperatureMax;

    /**
     * How much to step brightness and temperature percentages in response
     * to an INCREASE or DESCREASE.
     */
    public Integer increaseDecreaseStep;

    /**
     * Nanosecond equivalent of {@link minPollInterval}/{@link maxPollInterval}.
     * Calculated by the bridge handler when it loads the configuration.
     */
    public long discoveryIntervalNanos;
    public long minPollIntervalNanos;
    public long maxPollIntervalNanos;
}
