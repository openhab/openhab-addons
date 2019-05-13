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
package org.openhab.binding.volvooncall.internal.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PositionData} is responsible for storing
 * informations returned by vehicle position rest
 * answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PositionData {
    public @Nullable Double longitude;
    public @Nullable Double latitude;
    public @Nullable ZonedDateTime timestamp;
    public @Nullable String speed;
    private @Nullable String heading;

    public Boolean isHeading() {
        return "true".equalsIgnoreCase(heading);
    }

    /*
     * Currently unused in the binding, maybe interesting in the future
     * private String streetAddress;
     * private String postalCode;
     * private String city;
     * private String iSO2CountryCode;
     * private String region;
     */
}
