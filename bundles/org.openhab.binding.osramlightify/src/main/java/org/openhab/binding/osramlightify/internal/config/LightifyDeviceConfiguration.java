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
 * Configuration class for {@link LightifyDeviceHandler}.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyDeviceConfiguration {

    /**
     * How long transitions should take in seconds.
     */
    public Double transitionTime;

    /**
     * How long transitions should take in seconds when turning off.
     */
    public Double transitionToOffTime;

    /**
     * How much to step brightness and temperature percentages in response
     * to an INCREASE or DESCREASE.
     */
    public Integer increaseDecreaseStep;

    /**
     * Minimum white temperature as configured via UI.
     */
    public Integer whiteTemperatureMin;

    /**
     * Maximum white temperature as configured via UI.
     */
    public Integer whiteTemperatureMax;
}
