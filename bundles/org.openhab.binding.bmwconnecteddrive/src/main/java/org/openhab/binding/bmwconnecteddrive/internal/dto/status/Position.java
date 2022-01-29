/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.status;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link Position} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Position {
    public float lat;// ": 46.55605,
    public float lon;// ": 10.495669,
    public int heading;// ": 219,
    public String status;// ": "OK"

    public String getCoordinates() {
        return new StringBuilder(Float.toString(lat)).append(Constants.COMMA).append(Float.toString(lon)).toString();
    }

    @Override
    public String toString() {
        return getCoordinates();
    }
}
