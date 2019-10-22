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
package org.openhab.binding.doorbird.internal.config;

/**
 * The {@link DoorbellConfig} class contains fields mapping thing configuration parameters
 * for doorbell thing types.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class DoorbellConfiguration {
    /**
     * Hostname or IP address of doorbell
     */
    public String doorbirdHost;

    /**
     * User ID used for API requests
     */
    public String userId;

    /**
     * Password used in API requests, and to decrypt doorbird events
     */
    public String userPassword;

    /**
     * Rate at which image channel will be updated
     */
    public Integer imageRefreshRate;

    /**
     * Delay to set doorbell channel OFF after doorbell event
     */
    public Integer doorbellOffDelay;

    /**
     * Delay to set motion channel OFF after motion event
     */
    public Integer motionOffDelay;

    /**
     * Number of images in doorbell and motion montages
     */
    public Integer montageNumImages;

    /**
     * Scale factor for montages
     */
    public Integer montageScaleFactor;
}
