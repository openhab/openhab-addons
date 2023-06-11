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
package org.openhab.binding.dscalarm.internal.config;

/**
 * Configuration class for the DSC Alarm Partition Thing.
 *
 * @author Russell Stephens - Initial contribution
 */

public class DSCAlarmPartitionConfiguration {

    // Partition Thing constants
    public static final String PARTITION_NUMBER = "partitionNumber";

    /**
     * The Partition Number. Can be in the range of 1-8. This is a required parameter for a partition.
     */
    public Integer partitionNumber;
}
