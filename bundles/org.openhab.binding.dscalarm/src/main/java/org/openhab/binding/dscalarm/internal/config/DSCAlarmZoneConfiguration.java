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
package org.openhab.binding.dscalarm.internal.config;

/**
 * Configuration class for the DSC Alarm Zone Thing.
 *
 * @author Russell Stephens - Initial contribution
 */

public class DSCAlarmZoneConfiguration {

    // Zone Thing constants
    public static final String PARTITION_NUMBER = "partitionNumber";
    public static final String ZONE_NUMBER = "zoneNumber";

    /**
     * The Partition Number. Can be in the range of 1-8. This is not required. Defaults to 1.
     */
    public Integer partitionNumber;

    /**
     * The Zone Number. Can be in the range of 1-64. This is a required parameter for a zone.
     */
    public Integer zoneNumber;
}
