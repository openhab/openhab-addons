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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.*;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

/**
 * The {@link Destination} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Destination {
    public float lat;
    public float lon;
    public String country;
    public String city;
    public String street;
    public String streetNumber;
    public String type;
    public String createdAt;

    public String getAddress() {
        StringBuilder buf = new StringBuilder();
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
            return UNDEF;
        } else {
            return Converter.toTitleCase(buf.toString());
        }
    }

    public String getCoordinates() {
        return lat + Constants.COMMA + lon;
    }
}
