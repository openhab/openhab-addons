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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.*;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

/**
 * The {@link Destination} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Destination {
    public float lat;// ": 50.55577087402344,
    public float lon;// ": 8.495763778686523,
    public String country;// ": "DEUTSCHLAND",
    public String city;// ": "WETZLAR",
    public String street;// ": "UFERSTRASSE",
    public String streetNumber;// ": "4",
    public String type;// ": "DESTINATION",
    public String createdAt;// ": "2020-08-16T12:52:58+0000"

    public String getAddress() {
        StringBuffer buf = new StringBuffer();
        if (street != null) {
            buf.append(street);
            if (streetNumber != null) {
                buf.append(SPACE).append(streetNumber);
            }
        }
        if (city != null) {
            if (buf.length() > 0) {
                buf.append(COMMA).append(SPACE).append(city);
            } else {
                buf.append(city);
            }
        }
        if (buf.length() == 0) {
            return Converter.toTitleCase(UNKNOWN);
        } else {
            return Converter.toTitleCase(buf.toString());
        }
    }
}
