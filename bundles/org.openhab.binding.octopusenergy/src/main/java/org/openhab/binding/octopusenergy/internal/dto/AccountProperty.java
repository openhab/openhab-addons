/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AccountProperty} is a DTO class representing a physical building/location (e.g. a private house).
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class AccountProperty {

    // {
    // "id":2305833,
    // "moved_in_at":"2020-10-09T00:00:00+01:00",
    // "moved_out_at":null,
    // "address_line_1":"99 MyStreet",
    // "address_line_2":"",
    // "address_line_3":"My Hamlet",
    // "town":"MyTown",
    // "county":"MyCounty",
    // "postcode":"MyPostcode",
    // "electricity_meter_points":[
    // ],
    // "gas_meter_points":[
    // ]
    // }

    /**
     * An Octopus Energy generated unique property identifier.
     */
    public long id;

    /**
     * Time when the user moved into the property.
     */
    @Nullable
    public ZonedDateTime movedInAt;
    /**
     * Time when the user moved out of the property.
     */
    @Nullable
    public ZonedDateTime movedOutAt;

    @SerializedName("address_line_1")
    @Nullable
    public String addressLine1;

    @SerializedName("address_line_2")
    @Nullable
    public String addressLine2;

    @SerializedName("address_line_3")

    @Nullable
    public String addressLine3;

    @Nullable
    public String town;

    @Nullable
    public String county;

    @Nullable
    public String postcode;

    ArrayList<ElectricityMeterPoint> electricityMeterPoints = new ArrayList<>();
    ArrayList<GasMeterPoint> gasMeterPoints = new ArrayList<>();
}
