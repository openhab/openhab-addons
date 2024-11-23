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
package org.openhab.binding.caddx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for the Caddx Partition Thing.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxPartitionConfiguration {

    // Partition Thing constants
    public static final String PARTITION_NUMBER = "partitionNumber";

    /**
     * The Partition Number. Can be in the range of 1-8. This is a required parameter for a partition.
     */
    private int partitionNumber;

    public int getPartitionNumber() {
        return partitionNumber;
    }
}
