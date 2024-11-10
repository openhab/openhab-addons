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
package org.openhab.binding.ism8.server;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DataPointFactory} creates the data points depending on the types
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 * @author Holger Friedrich - Extend to new data types (fw 1.80 and 1.90)
 */
@NonNullByDefault
public class DataPointFactory {

    /**
     * Creates the concrete data-point based on the type.
     *
     */
    @Nullable
    public static IDataPoint createDataPoint(int id, String knxType, String description) {
        IDataPoint dataPoint = null;
        switch (knxType) {
            case "1.001":
            case "1.002":
            case "1.003":
            case "1.009":
                dataPoint = new DataPointBool(id, knxType, description);
                break;
            case "5.001":
                dataPoint = new DataPointScaling(id, knxType, description);
                break;
            case "7.001":
                dataPoint = new DataPointIntegerValue(id, knxType, description);
                break;
            case "9.001":
            case "9.002":
            case "9.006":
            case "9.024":
            case "9.025":
                dataPoint = new DataPointValue(id, knxType, description);
                break;
            // TODO 10.001 (Time, CWL)
            // TODO 11.001 (Date, CWL)
            case "13.002":
                dataPoint = new DataPointLongValue(id, knxType, description);
                break;
            case "5.010":
            case "20.102":
            case "20.103":
            case "20.105":
                dataPoint = new DataPointByteValue(id, knxType, description);
                break;
        }
        return dataPoint;
    }
}
